package de.htwsaarland.config;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static de.htwsaarland.config.EnvConfiguration.parseConfigFile;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * @author hbui
 * @version $Id: $Id
 */
public class ClasspathBasedConfig implements EnvConfiguration {

	private static final Logger CLASSPATH_BASE_LOGGER = LoggerFactory.getLogger(ClasspathBasedConfig.class);
	private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");
	private final Set<File> classPathDir;
	private File configFile;
	private final HashMap<String,String> configTable;


	/**
	 * 
	 * @param primaryConfigFileName this File will be search firstly in the 
	 * classpath and will be used. If this file is found in Classpath, the search-Process
	 * is stopped.
	 * 
	 * @param secondaryConfigFileName if the primaryConfigFileName is not found
	 * in the classpath, this file will be search in class-path and will be used.
	 * 
	 * <p>Constructor for EnvConfiguration.</p>
	 */
	public ClasspathBasedConfig(String primaryConfigFileName, String secondaryConfigFileName) {
		classPathDir = new HashSet<>(10);
		// collect classpath'directory in Thread' class loader
		collectDirInClassPathLoader(Thread.currentThread().getContextClassLoader(), classPathDir);
		// collect classpath in the own classloader
		collectDirInClassPathLoader(getClass().getClassLoader(), classPathDir);
		// collect classpath in String sessionClassPath = System.getProperty("java.class.path");
		collectDirInSystemClassPath(classPathDir);
		
		searchConfigFileInDir(primaryConfigFileName);
		if (configFile == null) {
			CLASSPATH_BASE_LOGGER.info("Test Config file {} not found!", primaryConfigFileName);
			CLASSPATH_BASE_LOGGER.info("Test Config file is not in following folders");
			for (File f : classPathDir) {
				CLASSPATH_BASE_LOGGER.info(f.getAbsolutePath());
			}
			searchConfigFileInDir(secondaryConfigFileName);
			if (configFile == null) {
				CLASSPATH_BASE_LOGGER.error("Config file {} NOT found!", secondaryConfigFileName);
				CLASSPATH_BASE_LOGGER.error("Config file is NOT in following folders:");
				for (File f : classPathDir) {
					CLASSPATH_BASE_LOGGER.error(f.getAbsolutePath());
				}
				throw new LSConfigException("No Config file found");
			}
			//return;
		} else {
			CLASSPATH_BASE_LOGGER.info("Use config file '{}'", configFile.getAbsoluteFile());
		}
		Map<String,String> temp = new HashMap<>();
		parseConfigFile(configFile, temp, 0);
		configTable = new HashMap();
		try{
			for (Map.Entry<String,String> e : temp.entrySet() ){
				String k = e.getKey();
				String v = StrSubstitutor.replace( e.getValue(), temp) ;
				configTable.put(k, v);
			}
		}catch(IllegalStateException ex){
			throw new LSConfigException(ex.getMessage(), ex);
		}
	}
	
	/**
	 * <p>Constructor for EnvConfiguration.</p>
	 *
	 * @param configFile a {@link java.io.File} object.
	 */
	@Override
	public Set<String> getAllConfigKeys() {
		return configTable.keySet();
	}

	
	protected ClasspathBasedConfig setConfigValue(String configParameter, String newValue){
		Matcher matcher = VAR_PATTERN.matcher(newValue);
		if (matcher.find() ){// If the value has a variable
			StrBuilder b = new StrBuilder(newValue);
			StrSubstitutor st = new StrSubstitutor(configTable);
			boolean substable = st.replaceIn(b);
			if (substable){
				String resolvedValue = b.build();
				Matcher afterSubstMatcher = VAR_PATTERN.matcher(resolvedValue);
				if (afterSubstMatcher.find()){//If there is one or more not resolveable variables
					throw new LSConfigException("Cannot find the variable '" + afterSubstMatcher.group(0) + "in '" + newValue +"'");
				}else{
					configTable.put(configParameter, resolvedValue );
				}
			}else{// nothing is replaced!!!
				throw new LSConfigException("Cannot find any variables in " + newValue);
			}
		}else{
			configTable.put(configParameter, newValue);
		}
		return this;
	}
	
	
	/**
	 * Gibt den Konfiguration-Wert zurück oder {@code null} wenn die
	 * Konfiguration-Param nicht in dem {@link #configTable} existiert.
	 *
	 * @param configParameter a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	@Override
	public String getConfigValue(String configParameter) {
		return configTable.get(configParameter);
	}

	protected final void collectDirInClassPathLoader(ClassLoader loader,final Set<File> classPathDir) {
		try {
			CLASSPATH_BASE_LOGGER.info("Collect classpath from {}", loader.getClass().getName());
			URLClassLoader urlCL = (URLClassLoader) loader;
			URL[] url = urlCL.getURLs();
			for (URL u : url) {
				String path = u.getPath();
				File classPathFile = new File(path);
				File parentDir = classPathFile.getParentFile();

				if (classPathFile.isDirectory()) {
					File searchDir = classPathFile.getAbsoluteFile();
					if (!classPathDir.contains(searchDir)) {
						CLASSPATH_BASE_LOGGER.trace("Add dir '{}' to search dir", searchDir.getAbsolutePath());
						classPathDir.add(searchDir);
					}
				} else if (classPathFile.isFile()) {
					File searchDir = parentDir.getAbsoluteFile();
					if (!classPathDir.contains(searchDir) ) {
						CLASSPATH_BASE_LOGGER.trace("Add parent '{}' to search dir", searchDir.getAbsoluteFile());
						classPathDir.add(searchDir.getAbsoluteFile());
					}
				}
			}
		} catch (ClassCastException ex) {
			CLASSPATH_BASE_LOGGER.warn("Cannot serch config file from classloader {}",
					loader.getClass().getName());
			CLASSPATH_BASE_LOGGER.trace("Cause: {}", ex);
		}
	}

	protected final void searchConfigFileInDir(String configFileName) {
		for (File dir : classPathDir) {
			CLASSPATH_BASE_LOGGER.debug("Search config file in '{}' but not in subdir", dir.getAbsoluteFile() );
			Collection<File> listFiles = FileUtils.listFiles(dir, new NameFileFilter(configFileName), null);
			if (!listFiles.isEmpty()) {
				configFile = listFiles.iterator().next();
				return;
			}
		}
	}

	protected final void collectDirInSystemClassPath(Set<File> classPathDir) {
		CLASSPATH_BASE_LOGGER.trace("Collect directories in java.class.path");
		String sessionClassPath = System.getProperty("java.class.path");
		String[] classpath = sessionClassPath.split(":");
		for (String path : classpath) {
			File f = new File(path);
			if (f.isDirectory()) {
				String absolutPath = f.getAbsolutePath();
				if (!classPathDir.contains(f.getAbsoluteFile())){
					classPathDir.add(f.getAbsoluteFile());
					CLASSPATH_BASE_LOGGER.trace("Add '{}' to search dir", f.getAbsolutePath());
				}else{
					CLASSPATH_BASE_LOGGER.trace("Duplex path {}",absolutPath);
				}
			} else if (f.isFile()) {
				File parentFile = f.getParentFile();
				if(!classPathDir.contains(parentFile.getAbsoluteFile())){
					classPathDir.add(f.getParentFile().getParentFile().getAbsoluteFile());
				}
			}
		}
	}
}
