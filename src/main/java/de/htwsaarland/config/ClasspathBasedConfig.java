package de.htwsaarland.config;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static de.htwsaarland.config.EnvConfiguration.parseConfigFile;

/**
 * Verwaltet die Konfigurationen in einer Umgebung, wo Laplace Script und
 * Laplace Compiler laufen. Diese Klasse sucht zu erst die Konfig Datei
 * "laplus-config-test.xml" in der Klasse Path, Begin mit 
 * {@code Thread.currentThread().getContextClassLoader()}, 
 * dann in {@code getClass().getClassLoader()}, letzendlich in
 * {@code System.getProperty("java.class.path")}. Findet diese Klasse die Konfig-Datei
 * "laplus-config-test.xml", dann nutzen sie es. Findet sie nicht, dann sucht sie
 * die Datein "laplus-config.xml" auch in der Klasse Path. Wenn die Datei "laplus-config.xml"
 * auch nicht gefunden wird, wird die Exception "LSConfigException" ausgeworfen.
 *
 *
 * @author hbui
 * @version $Id: $Id
 */
public class ClasspathBasedConfig implements EnvConfiguration {

	private static final Logger CLASSPATH_BASE_LOGGER = LoggerFactory.getLogger(ClasspathBasedConfig.class);
	private final Map<String, String> configTable;
	private final List<File> classPathDir;
	private File configFile;


	/**
	 * Usage by coding a library, which will be used in many other projects.
	 * 
	 * <ul>
	 * 		<li>
	 * 			Put the file "test-config.xml" into the ClassPath for test only. 
	 * 			Importance: This file <i>must not be found</i>
	 * 			in productive Runtime of the library.
	 * 		</li>
	 * 		<li>
	 * 			For each Project, which uses this the coding library: put the file "productive-config.xml" in to its
	 * 			Runtime-Classpath.
	 * 		</li>
	 * 		<li>
	 * 			Use a static factory class in the project ( <b>Not in the library</b>) to manage 
	 * 			the config, like the code below.
	 * 		</li>
	 * </ul>
	 * {@code
	 * // This Class is a static factory class
	 * class ProjectConfigManager{
	 * 		private static ClasspathBasedConfig instance;
	 * 
	 * 		public static EnvConfiguration getConfig(){
	 * 			// check null instance 
	 * 			if (instance = null){
	 * 				instance = new ClasspathBasedConfig("test-config.xml", "productive-config.xml");
	 * 			}
	 * 			return instance;
	 * 		}
	 * }
	 * }
	 * 
	 * Now you can use the class ProjectConfigManager to get configuration in Test and Productive Class
	 * without changing any code.
	 * 
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
		configTable = new HashMap<>();
		classPathDir = new ArrayList<>(10);
		// collect classpath'directory in Thread' class loader
		collectDirInClassPathLoader(Thread.currentThread().getContextClassLoader());
		// collect classpath in the own classloader
		collectDirInClassPathLoader(getClass().getClassLoader());
		// collect classpath in String sessionClassPath = System.getProperty("java.class.path");
		collectDirInSystemClassPath();
		
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
		parseConfigFile(configFile, configTable);
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
		configTable.put(configParameter, newValue);
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

	private void collectDirInClassPathLoader(ClassLoader loader) {
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

	private void searchConfigFileInDir(String configFileName) {
		for (File dir : classPathDir) {
			CLASSPATH_BASE_LOGGER.debug("Search config file in '{}' but not in subdir", dir.getAbsoluteFile() );
			Collection<File> listFiles = FileUtils.listFiles(dir, new NameFileFilter(configFileName), null);
			if (!listFiles.isEmpty()) {
				configFile = listFiles.iterator().next();
				return;
			}
		}
	}

	private void collectDirInSystemClassPath() {
		CLASSPATH_BASE_LOGGER.trace("Collect directories in java.class.path");
		String sessionClassPath = System.getProperty("java.class.path");
		String[] classpath = sessionClassPath.split(":");
		for (String path : classpath) {
			File f = new File(path);
			if (f.isDirectory()) {
				String absolutPath = f.getAbsolutePath();
				if (!classPathDir.contains(f.getAbsoluteFile())){
					classPathDir.add(f.getAbsoluteFile());
//					classPath.add(absolutPath);
					CLASSPATH_BASE_LOGGER.trace("Add '{}' to search dir", f.getAbsolutePath());
				}else{
					CLASSPATH_BASE_LOGGER.trace("Duplex path {}",absolutPath);
				}
			} else if (f.isFile()) {
				File parentFile = f.getParentFile();
//				String absolutePath = parentFile.getAbsolutePath();
				if(!classPathDir.contains(parentFile.getAbsoluteFile())){
					//classPath.add(absolutePath);
					classPathDir.add(f.getParentFile().getParentFile().getAbsoluteFile());
				}
			}
		}
	}
}