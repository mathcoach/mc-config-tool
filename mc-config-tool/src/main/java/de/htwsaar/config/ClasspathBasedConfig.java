package de.htwsaar.config;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(EnvConfiguration.class);
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
		// collect classpath in String
		collectDirInSystemClassPath(classPathDir);
		
		searchConfigFileInDir(primaryConfigFileName);
		if (configFile == null) {
			LOGGER.info("Test Config file {} not found!", primaryConfigFileName);
			LOGGER.info("Test Config file is not in following folders");
			for (File f : classPathDir) {
				LOGGER.info(f.getAbsolutePath());
			}
			searchConfigFileInDir(secondaryConfigFileName);
			if (configFile == null) {
				LOGGER.error("Config file {} NOT found!", secondaryConfigFileName);
				LOGGER.error("Config file is NOT in following folders:");
				for (File f : classPathDir) {
					LOGGER.error(f.getAbsolutePath());
				}
				throw new LSConfigException("No Config file found");
			}
		} else {
			LOGGER.info("Use config file '{}'", configFile.getAbsoluteFile());
		}
		XMLConfigParser parser = new XMLConfigParser();
		Map<String,String> temp = parser.parseConfigFile(configFile);
		//parseXMLConfigFile(configFile, temp, 0);
		Map<String,String> envVar = new HashMap<String,String>(3){{
			put("HOME",System.getProperty("user.home"));
		}};
		configTable = new HashMap();
		try{
			temp.entrySet().stream().forEach(entry -> {
				String k = entry.getKey();
				String v = StrSubstitutor.replace(entry.getValue(), temp) ;
				v = StrSubstitutor.replaceSystemProperties(v);
				v = StrSubstitutor.replace(v, envVar);
				LOGGER.trace("{} -> {}", k, v);
				configTable.put(k, v);
			});
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
			LOGGER.info("Collect classpath from {}", loader.getClass().getName());
			URLClassLoader urlCL = (URLClassLoader) loader;
			URL[] url = urlCL.getURLs();
			for (URL u : url) {
				String path = u.getPath();
				File classPathFile = new File(path);
				File parentDir = classPathFile.getParentFile();

				if (classPathFile.isDirectory()) {
					File searchDir = classPathFile.getAbsoluteFile();
					if (!classPathDir.contains(searchDir)) {
						LOGGER.trace("Add dir '{}' to search dir", searchDir.getAbsolutePath());
						classPathDir.add(searchDir);
					}
				} else if (classPathFile.isFile()) {
					File searchDir = parentDir.getAbsoluteFile();
					if (!classPathDir.contains(searchDir) ) {
						LOGGER.trace("Add parent '{}' to search dir", searchDir.getAbsoluteFile());
						classPathDir.add(searchDir.getAbsoluteFile());
					}
				}
			}
		} catch (ClassCastException ex) {
			LOGGER.warn("Cannot search config file from classloader {}",
					loader.getClass().getName());
			LOGGER.trace("Cause: {}", ex);
		}
	}

	protected final void searchConfigFileInDir(String configFileName) {
		for (File dir : classPathDir) {
			LOGGER.debug("Search config file in '{}' but not in subdir", dir.getAbsoluteFile() );
			Collection<File> listFiles = FileUtils.listFiles(dir, new NameFileFilter(configFileName), null);
			if (!listFiles.isEmpty()) {
				configFile = listFiles.iterator().next();
				return;
			}
		}
	}

	protected final void collectDirInSystemClassPath(Set<File> classPathDir) {
		LOGGER.trace("Collect directories in java.class.path");
		String sessionClassPath = System.getProperty("java.class.path");
		String[] classpath = sessionClassPath.split(File.pathSeparator);
		for (String path : classpath) {
			File f = new File(path);
			if (f.isDirectory()) {
				String absolutPath = f.getAbsolutePath();
				if (!classPathDir.contains(f.getAbsoluteFile())){
					classPathDir.add(f.getAbsoluteFile());
					LOGGER.trace("Add '{}' to search dir", f.getAbsolutePath());
				}else{
					LOGGER.trace("Duplex path {}",absolutPath);
				}
			} else if (f.isFile()) {
				File parentFile = f.getParentFile();
				if(!classPathDir.contains(parentFile.getAbsoluteFile())){
					classPathDir.add(f.getParentFile().getParentFile().getAbsoluteFile());
				}
			}
		}
	}

	@Override
	public String toString() {
		return "[configuration source: " 
				+ configFile.toPath().toAbsolutePath().normalize().toString()
				+ "]";
	}
}
