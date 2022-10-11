package de.htwsaar.config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author hbui
 */
public class JarTesting {

    public static void main(String[] args) throws Exception {        
        JarTesting t = new JarTesting();
        EnvConfiguration conf = new ClasspathBasedConfig("no-existing-config.properties", "jar-testing-config.properties");
        Set<String> config = conf.getAllConfigKeys();
        config.forEach(key -> {
            System.out.println("    " + key + " -> " + conf.getConfigValue(key));
        });        
    }
    
    // Get all paths from a folder that inside the JAR file
    private List<Path> getPathsFromResourceJAR(String folder) throws URISyntaxException, IOException {

        List<Path> result;

        // get path of the current running JAR
        String jarPath = getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();
        System.out.println("JAR Path :" + jarPath);

        // file walks JAR
        URI uri = URI.create("jar:file:" + jarPath);
        try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            result = Files.walk(fs.getPath(folder))
                    //.filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
        return result;

    }

}
