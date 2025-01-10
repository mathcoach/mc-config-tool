/*
 * Copyright 2025 hbui.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.htwsaar.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.ProviderNotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is not intended to be used outsize this artifact!
 * Origin url 
 * `file:/home/hbui/aggregate-mc/other-libs/mc-config-tool/mc-config-tool-jar-jar-test/target/mc-config-tool-jar-jar-test-jar-with-dependencies.jar`
 * 
 * @author hbui
 */
final class JarConfigFinder {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JarConfigFinder.class);
    private final String primaryConfigFilename; 
    private final String secondaryConfigFilename;
    
    JarConfigFinder(String primaryConfigFilename, String secondaryConfigFilename) {
        this.primaryConfigFilename = primaryConfigFilename;
        this.secondaryConfigFilename = secondaryConfigFilename;
    }
    
    
    
    Path findConfigFileInJar(URI jarUlr) {
        try{            
            String jarPath = jarUlr.getPath();
            final URI uri = URI.create("jar:file:" + jarPath);
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
        } catch(IOException ex) {
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
     * @param configPathInJar <code>jar:file:///path/to/some-file.jar!/config-file.properties</code>
     * @param fn
     */
    static <T> T parseConfigFromJar(Path configPathInJar, Function<InputStream, T> fn) {        
        URI uri = configPathInJar.toUri();
        try( FileSystem zfs = FileSystems.newFileSystem(uri, Map.of() ) ) {            
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            Path pathInZipFile = zfs.getPath(configPathInJar.getFileName().toString());
            Files.copy(pathInZipFile, out);
            InputStream in = new ByteArrayInputStream( out.toByteArray() );
            return fn.apply(in);
        }catch(IOException ex) {
            throw new LSConfigException(ex);
        }
    }
}
