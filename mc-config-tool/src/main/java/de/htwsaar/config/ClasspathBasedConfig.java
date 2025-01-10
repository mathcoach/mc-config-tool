package de.htwsaar.config;

import static de.htwsaar.config.EnvConfiguration.resolveConfigVariables;
import static de.htwsaar.config.EnvConfiguration.resolveImportConfig;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.ProviderNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hbui
 * @version $Id: $Id
 */
public class ClasspathBasedConfig implements EnvConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathBasedConfig.class);

    private final HashSet<Path> classPathDir;
    private Path configPath;
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
     *
     */
    public ClasspathBasedConfig(String primaryConfigFileName, String secondaryConfigFileName) {
        classPathDir = new HashSet<>(10);
        configTable = new HashMap<>();
        try {
            configPath = initConfigByClasspath(primaryConfigFileName, secondaryConfigFileName);
            File cf = configPath.toFile();
            Map<String,String> configWithResolvedImport = resolveImportConfig(cf, ConfigParserFactory.getParserForFile(cf));
            Map<String,String> configWithResolvedVariables = resolveConfigVariables(configWithResolvedImport);
            configTable.putAll(configWithResolvedVariables);
        }catch (ConfigFileNotFoundException ex) {            
            configPath = initConfigByJar(primaryConfigFileName, secondaryConfigFileName);
            Map<String,String> configWithResolvedImport = parseConfigFromJar(configPath);
            Map<String,String> configWithResolvedVariables = resolveConfigVariables(configWithResolvedImport);
            configTable.putAll(configWithResolvedVariables);
            
        }
    }

    private Path initConfigByClasspath(String primaryConfigFileName, String secondaryConfigFileName) {
        
        collectDirInClassPathLoader(Thread.currentThread().getContextClassLoader(), classPathDir);        
        collectDirInClassPathLoader(getClass().getClassLoader(), classPathDir);        
        collectDirInSystemClassPath(classPathDir);

        Path configFilePath = searchConfigPathInDir(classPathDir, primaryConfigFileName);
        if(configFilePath == null) {
            LOGGER.info("Test Config file {} not found in following directories:", primaryConfigFileName);
            if (LOGGER.isInfoEnabled()) {
                classPathDir.forEach(f -> LOGGER.info(" -> {}", f));
            }
            configFilePath = searchConfigPathInDir(classPathDir, secondaryConfigFileName);
            if (configFilePath == null) {
                LOGGER.error("Config file {} NOT found! in following directories:", secondaryConfigFileName);
                if (LOGGER.isErrorEnabled()) {
                    classPathDir.forEach(f -> LOGGER.error(" -> {}", f));
                }
                throw new ConfigFileNotFoundException(primaryConfigFileName, secondaryConfigFileName);
            } else {
                LOGGER.info("Use secondary config file '{}'", configFilePath.toAbsolutePath());
            }
        } else {
            LOGGER.info("Use primary config file '{}'", configFilePath.toAbsolutePath());
        }
        return configFilePath;
    }

    /**
     *
     *
     * @return all configuration keys
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
        LOGGER.info("Collect directories in the classloader {}", loader.getClass().getName());
        try {            
            URLClassLoader urlCL = (URLClassLoader) loader;
            URL[] url = urlCL.getURLs();
            for (URL u : url) {
                collectDirFromURL(u, classPathDir);
            }
        } catch (ClassCastException ex) {
            LOGGER.warn("Cannot search config file from classloader {}",
                    loader.getClass().getName());
            LOGGER.trace("Cause:", ex);
        }
    }

    protected final void collectDirFromURL(URL url, final Set<Path> classPathDir) {
        LOGGER.trace("collect directory from URL {}", url);
        try {
            final Path classPathFile = resolvePathFromUrl(url);
            LOGGER.trace("resolved path: {}", classPathFile);
            if (classPathFile.toFile().isDirectory()) {
                if (classPathDir.add(classPathFile)) {
                    LOGGER.trace("Add dir '{}' to search dir", classPathFile);
                }
            }
            else if (classPathFile.toFile().isFile()) {
                Path searchDir = classPathFile.getParent();
                if (classPathDir.add(searchDir)) {
                    LOGGER.trace("Add parent '{}' to search dir", searchDir);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Processing URL `{}` caused error", url, e);
        }
    }



    protected final void collectDirInSystemClassPath(Set<Path> classPathDir) {
        LOGGER.info("Collect directories in System classpath defined by java.class.path");
        String sessionClassPath = System.getProperty("java.class.path");
        String[] classpath = sessionClassPath.split(File.pathSeparator);
        for (String path : classpath) {
            File f = new File(path);
            if (f.isDirectory()) {
                Path absolutPath = f.toPath().toAbsolutePath().normalize();
                if (!classPathDir.contains(absolutPath)) {
                    classPathDir.add(absolutPath);
                    LOGGER.trace("Add '{}' to search dir", f.getAbsolutePath());
                } else {
                    LOGGER.trace("Duplex path {}", absolutPath);
                }
            } else if (f.isFile()) {
                File parentFile = f.getParentFile();
                Path absolutPath = parentFile.toPath().toAbsolutePath().normalize();
                if (!classPathDir.contains(absolutPath)) {
                    classPathDir.add(absolutPath);
                }
            }
        }
    }

    /**
     * 
     * Separate this method to easy debug path-resolving on different platforms. 
     * DONOT inline it in caller.
     */
    private Path resolvePathFromUrl(URL url) throws URISyntaxException {        
        return Path.of(url.toURI()).toAbsolutePath().normalize();
    }

    /**
     *
     *
     * @param classPathDir
     * @param configFileName
     * @return the path to configuration file if found, else null
     */
    protected final Path searchConfigPathInDir(Set<Path> classPathDir, String configFileName) {
        for (Path dir : classPathDir) {
            try {
                LOGGER.debug("Search config file name '{}' in '{}' but not in sub-directory", configFileName, dir);
                final Optional<Path> optinalPath;
                try ( Stream<Path> stream = Files.list(dir)) {
                    optinalPath = stream
                            .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().equals(configFileName))
                            .findFirst();
                }
                if (optinalPath.isPresent()) {
                    return optinalPath.get().normalize();
                }
            } catch (IOException ex) { //Doof
                LOGGER.info("Search config file {} in path {} caused IOException", configFileName, dir);
            }
        }
        return null;
    }

    private Path initConfigByJar(String primaryConfigFileName, String secondaryConfigFileName) {        
        try {
            final URI jarUlr =  getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            JarConfigFinder finder = new JarConfigFinder(primaryConfigFileName, secondaryConfigFileName);
            return finder.findConfigFileInJar(jarUlr);
        }catch(URISyntaxException ex) {
            throw new LSConfigException(ex);
        }
    }
    

    private Map<String,String> parseConfigFromJar(Path configFileInJarPath) {
        final String sourceName = configFileInJarPath.getFileSystem().toString();
        final ConfigParser parser = ConfigParserFactory.getParserForFileName(sourceName);
        Function<InputStream, Map<String,String>> fn = (in) -> {
            return resolveImportConfig(in, sourceName, parser);
        };
        return JarConfigFinder.parseConfigFromJar(configPath, fn);        
    }


    /**
     * @param configFileName
     *
     *
     *
     * @deprecated Do not use this
     *
     *
     */
    @Deprecated(since="4.0", forRemoval=true)
    protected final void searchConfigFileInDir(String configFileName) {
        configPath = searchConfigPathInDir(classPathDir, configFileName);
    }

    @Override
    public String toString() {
        return "[configuration source: "
                + configPath
                + "]";
    }

    public final String getSearchDir() {
        return classPathDir.stream()
                .map(Path::toString)
                .collect(Collectors.joining(File.pathSeparator));
    }
}
