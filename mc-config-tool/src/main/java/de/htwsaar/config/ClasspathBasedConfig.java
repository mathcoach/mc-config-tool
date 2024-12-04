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
import java.nio.file.Paths;
import java.nio.file.ProviderNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
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
        LOGGER.info("Collect directories in Thread's class loader");
        collectDirInClassPathLoader(Thread.currentThread().getContextClassLoader(), classPathDir);
        LOGGER.info("Collect directories in the own classloader");
        collectDirInClassPathLoader(getClass().getClassLoader(), classPathDir);
        LOGGER.info("Collect directories in System classpath defined by java.class.path");
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

    private Path initConfigByJar(String primaryConfigFilename, String secondaryConfigFilename) {
        try{
            URI jarUlr =  getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
            LOGGER.info("Search config files in {}", jarUlr);
            String jarPath = jarUlr.getPath();
            URI uri = URI.create("jar:file:" + jarPath);
            LOGGER.trace("search {} and {} in {}", primaryConfigFilename, secondaryConfigFilename, uri);
            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                Path rootPath = fs.getRootDirectories().iterator().next();
                try (Stream<Path> walker = Files.list(rootPath)) {
                    Optional<Path> primaryConfigPath = walker.filter(p ->
                            Files.isReadable(p) && Files.isRegularFile(p) && p.getFileName().toString().equals(primaryConfigFilename)
                    ).findFirst();
                    if (primaryConfigPath.isPresent()) {
                        return primaryConfigPath.get().normalize();
                    } else {
                        LOGGER.info("Test Config file {} not found in jar file {}!", primaryConfigFilename, jarPath);
                        try (Stream<Path> secondaryWalker = Files.list(rootPath)) {
                            Optional<Path> secondaryConfigPath = secondaryWalker.filter(p ->
                                    Files.isReadable(p) && Files.isRegularFile(p) && p.getFileName().toString().equals(secondaryConfigFilename)
                            ).findFirst();
                            if (secondaryConfigPath.isPresent()) {
                                return secondaryConfigPath.get().normalize();
                            } else {
                                LOGGER.error("Config file {} NOT found in jar file {}!", secondaryConfigFilename, jarPath);
                                throw new ConfigFileNotFoundException(primaryConfigFilename, secondaryConfigFilename);
                            }
                        }
                    }
                }
            }
        } catch(URISyntaxException|IOException ex) {
            throw new LSConfigException(ex);
        } catch(ProviderNotFoundException ex) {
            LOGGER.warn("No FileSystem Provider found for URI schema `jar:file:`");
            LOGGER.trace("", ex);
            throw new ConfigFileNotFoundException(primaryConfigFilename, secondaryConfigFilename);
        }
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
            LOGGER.trace("Cause:", ex);
        }
    }

    protected final void collectDirFromURL(URL classPath, final Set<Path> classPathDir) {
        try {
            String path = classPath.getPath();
            if (path.startsWith("jar:file:")) {
                path = path.substring(4, path.indexOf("!"));
            }
            final Path classPathFile = Paths.get(path).toAbsolutePath().normalize();
            LOGGER.trace("path: {}", path);

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
            LOGGER.error("Error processing classpath URL: {}", classPath, e);
        }
    }



    protected final void collectDirInSystemClassPath(Set<Path> classPathDir) {
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


    private Map<String,String> parseConfigFromJar(Path configFileInJarPath) {
        String pathInfo =  configFileInJarPath.toUri().toString() ;
        LOGGER.trace("get InputStream from {}", pathInfo);
        String fileName = configFileInJarPath.getFileName().toString();
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        if(inputStream == null) {
            throw new LSConfigException("Cannot read " + configFileInJarPath);
        } else {
            String sourceName = configFileInJarPath.getFileName().toString();
            File configFile = new File(sourceName);
            return resolveImportConfig(inputStream, sourceName, ConfigParserFactory.getParserForFile(configFile));
        }
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
