package de.htwsaar.config;

import static de.htwsaar.config.EnvConfiguration.resolveConfigVariables;
import static de.htwsaar.config.EnvConfiguration.resolveImportConfig;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author hbui
 * @version $Id: $Id
 */
public class ClasspathBasedConfig implements EnvConfiguration {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathBasedConfig.class);
	
	private final HashSet<Path> classPathDir;
	private File configFile;
	private final Map<String, String> configTable;

	/**
	 *
	 * @param primaryConfigFileName this File will be search firstly in the
	 * classpath and will be used. If this file is found in Classpath, the
	 * search-Process is stopped.
	 *
	 * @param secondaryConfigFileName if the primaryConfigFileName is not found
	 * in the classpath, this file will be search in class-path and will be
	 * used.
	 *
	 * <p>
	 * Constructor for EnvConfiguration.</p>
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
			if (LOGGER.isInfoEnabled()) {
				classPathDir.forEach( f -> 	LOGGER.info( f.toString() ) );
			}
			searchConfigFileInDir(secondaryConfigFileName);
			if (configFile == null) {
				LOGGER.error("Config file {} NOT found!", secondaryConfigFileName);
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Config file is NOT in following folders:");
					classPathDir.forEach( f -> LOGGER.error("{}" ,f ) );
				}
				throw new ConfigFileNotFoundException(primaryConfigFileName ,secondaryConfigFileName);
			}else{
				LOGGER.info("Use secondary config file '{}'", configFile.getAbsoluteFile());
			}
		} else {
			LOGGER.info("Use primary config file '{}'", configFile.getAbsoluteFile());
		}
		configTable = resolveConfigVariables(resolveImportConfig(configFile,  ConfigParserFactory.getParserForFile(configFile) ));
	}

	/**
	 * <p>
	 * Constructor for EnvConfiguration.</p>
	 *
	 * @param configFile a {@link java.io.File} object.
	 */
	@Override
	public Set<String> getAllConfigKeys() {
		return configTable.keySet();
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
	

	protected final void collectDirInClassPathLoader(ClassLoader loader, final Set<Path> classPathDir) {
		try {
			LOGGER.info("Collect classpath from {}", loader.getClass().getName());
			URLClassLoader urlCL = (URLClassLoader) loader;
			URL[] url = urlCL.getURLs();
			for (URL u : url) {
				collectDirFromURL(u, classPathDir);
			}
		} catch (ClassCastException ex) {
			LOGGER.warn("Cannot search config file from classloader {}",
					loader.getClass().getName());
			LOGGER.trace("Cause: {}", ex);
		}
	}
	protected final void collectDirFromURL(URL classPath, final Set<Path> classPathDir) {
		final String path = classPath.getPath();
		final Path classPathFile = Paths.get(path).toAbsolutePath().normalize();
		LOGGER.trace("path: {}", path);
		if ( Files.isDirectory(classPathFile) ) {
			if ( classPathDir.add(classPathFile) ){
				LOGGER.trace("Add dir '{}' to search dir", classPathFile);
			}
		} else if ( Files.isRegularFile(classPathFile) ) {
			Path searchDir = classPathFile.getParent(); 
			if (classPathDir.add(searchDir)){
				LOGGER.trace("Add parent '{}' to search dir", searchDir);
			}
		}
	}

	protected final void searchConfigFileInDir(String configFileName) {
		for(Path dir : classPathDir){
			try{
				LOGGER.debug("Search config file name '{}' in '{}' but not in subdir",configFileName, dir);
				Optional<Path> configPath = Files.list( dir )
						.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().equals(configFileName) )
						.findFirst();
				if (configPath.isPresent() ){
					configFile = configPath.get().toFile();
					return;
				}
			}catch(IOException ex){ //Doof
				configFile = null;
			}
		}
	}
	
	protected final void collectDirInSystemClassPath(Set<Path> classPathDir) {
		LOGGER.trace("Collect directories in java.class.path");
		String sessionClassPath = System.getProperty("java.class.path");
		String[] classpath = sessionClassPath.split(File.pathSeparator);
		for (String path : classpath) {
			File f = new File(path);
			if (f.isDirectory()) {
				Path absolutPath = f.toPath().toAbsolutePath().normalize();
				if (!classPathDir.contains( absolutPath )) {
					classPathDir.add( absolutPath );
					LOGGER.trace("Add '{}' to search dir", f.getAbsolutePath());
				} else {
					LOGGER.trace("Duplex path {}", absolutPath);
				}
			} else if (f.isFile()) {
				File parentFile = f.getParentFile();
				Path absolutPath = parentFile.toPath().toAbsolutePath().normalize();
				if (!classPathDir.contains( absolutPath )) {
					classPathDir.add( absolutPath);
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
	
	public final String getSearchDir(){
		return classPathDir.stream()
				.map( Path::toString )
				.collect(Collectors.joining(File.pathSeparator));
	}
}
