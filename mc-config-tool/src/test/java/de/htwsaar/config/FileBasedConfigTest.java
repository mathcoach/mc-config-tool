package de.htwsaar.config;

import java.nio.file.Path;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;


/**
 *
 * @author hbui
 */
class FileBasedConfigTest {

    @Test
    void testGetConfigValue() {
        final Path config = Path.of("./src/test/resources/test-config.properties");
        FileBasedConfig cfg = new FileBasedConfig(config);
        assertThat(cfg.getConfigValue("param-a")).isEqualTo("a");
        assertThat(cfg.getConfigValue("param-b")).isEqualTo("b");
        assertThat(cfg.getConfigValue("param-c")).isEmpty();
    }
    
    @Test
    void testGetAllParameters() {
        final Path config = Path.of("./src/test/resources/test-config.properties");
        FileBasedConfig cfg = new FileBasedConfig(config);
        Set<String> params = cfg.getAllConfigKeys();
        assertThat(params).containsOnly("param-a", "param-b", "param-c", "param-d");
    }
    
}
