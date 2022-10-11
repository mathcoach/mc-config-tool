package de.htwsaar.config;

import java.io.File;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

/**
 *
 * @author hbui
 */
class PropertiesConfigParserTest {

    private PropertiesConfigParser parser;

    @BeforeEach
    void init() {
        parser = new PropertiesConfigParser();
    }

    @Test
    void parserASimpleConfigFile() {
        String path = "./src/test/resources/test-config.properties";
        File simpleConfig = new File(path);
        Map<String, String> config = parser.parseConfigFile(simpleConfig);
        assertThat(config)
                .containsEntry("param-a", "a")
                .containsEntry("param-b", "b")
                .containsEntry("param-c", "")
                .containsEntry("param-d", "");
        assertThat(config.get("param-e")).isNull();
    }

    @Test
    void parseConfigFileWithImport() {
        String path = "./src/test/resources/test-config-with-import.properties";
        File simpleConfig = new File(path);
        Map<String, String> config = parser.parseConfigFile(simpleConfig);
        assertThat(config)
                .containsKey("import")
                .containsEntry("import", "${PWD}/src/test/resources/import-config.properties");
    }

    @Test
    void parseNotExistFile() {
        final File notExistingPropertiesFile = new File("not-exist-path.properties");
        try {
            parser.parseConfigFile(notExistingPropertiesFile);
            failBecauseExceptionWasNotThrown(LSConfigException.class);
        } catch (LSConfigException ex) {
            System.out.println("error msg:" + ex.getMessage());
        }
    }

    @Test
    void parserNullFile() {
        try {
            parser.parseConfigFile((File)null);
            failBecauseExceptionWasNotThrown(LSConfigException.class);
        } catch (LSConfigException ex) {
            System.out.println("error msg:" + ex.getMessage());
        }
    }
}
