package de.htwsaar.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author hbui
 */
class JarConfigFinderTest {
    
    private static final String NOT_EXISTING_CONFIG_FILE = "no-existing-config.properties";
    private static final String EXISTING_CONFIG_FILE = "jar-testing-config.properties";
    
    private static final String JAR_FILENAME = "../mc-config-tool-jar-test/target/mc-config-tool-jar-test.jar";
    
    private static File jarFile;
    private static URI jarUri;
    
    @BeforeAll
    static void resolveJarFile() {
        Path jarPath = Paths.get(JAR_FILENAME).toAbsolutePath().normalize();
        jarFile = jarPath.toFile();
        if( ! jarFile.canRead()) {
            throw new RuntimeException("Jar-File `" + jarPath.toString() + "` is not readable. Run `mvn -f mc-config-tool-jar-test clean package` before run Unittest");
        }        
        jarUri = jarFile.toURI();
    }
    
    @Test
    void testInitConfigByJar() {
        JarConfigFinder finder = new JarConfigFinder(NOT_EXISTING_CONFIG_FILE, EXISTING_CONFIG_FILE);
        Path p = finder.findConfigFileInJar(jarUri);
        String fileSystemName = p.getFileSystem().getClass().getCanonicalName();
        assertThat(fileSystemName).endsWith("ZipFileSystem");
        URI revertedJarUri = p.toUri();        
        assertThat(revertedJarUri.getScheme()).isEqualTo("jar");        
        assertThat(revertedJarUri.toString().endsWith("!/" + EXISTING_CONFIG_FILE)).isTrue();        
    }
    
    @Test
    void testInitConfigByJarWithPrimaryConfigOnly() {
        JarConfigFinder finder = new JarConfigFinder(EXISTING_CONFIG_FILE, NOT_EXISTING_CONFIG_FILE);
        Path p = finder.findConfigFileInJar(jarUri);
        String fileSystemName = p.getFileSystem().getClass().getCanonicalName();
        assertThat(fileSystemName).endsWith("ZipFileSystem");
        URI revertedJarUri = p.toUri();        
        assertThat(revertedJarUri.getScheme()).isEqualTo("jar");        
        assertThat(revertedJarUri.toString().endsWith("!/" + EXISTING_CONFIG_FILE)).isTrue();        
    }
    
    @Test
    void testInitConfigByJarWithNoConfigFiles() {
        try{
            JarConfigFinder finder = new JarConfigFinder("trivial-"+EXISTING_CONFIG_FILE, NOT_EXISTING_CONFIG_FILE);
            Path p = finder.findConfigFileInJar(jarUri);
            failBecauseExceptionWasNotThrown(ConfigFileNotFoundException.class);
        }catch(ConfigFileNotFoundException ex) {
            assertThat(ex).hasMessageContaining(EXISTING_CONFIG_FILE);
        }
    }
    
    @Test
    void testParseConfigByJar() {
        try{
            Function<InputStream, String> fn = (stream) -> {
                try(stream) {
                    return (new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)))
                        .lines()
                        .collect(Collectors.joining("\n"));
                }catch(IOException ex) {
                    throw new RuntimeException(ex);
                }
            };
            JarConfigFinder finder = new JarConfigFinder(NOT_EXISTING_CONFIG_FILE, EXISTING_CONFIG_FILE);
            Path p = finder.findConfigFileInJar(jarUri);
            String text = finder.parseConfigFromJar(p, fn);
            String expected = "import=${PWD}/configuration-for-jar-testing.properties\n" +
    "some.config.param=some-value\n";
            assertThat(text).isEqualTo(expected);
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
