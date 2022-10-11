package de.htwsaar.config;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hbui
 */
class LSConfigExceptionTest {

    @Test
    void constructInstance() {
        EnvConfiguration ec = new DynamicConfig();
        try {
            //EnvConfiguration ec = new ClasspathBasedConfig("config-test.properties", null);            
            LSConfigException ex = new LSConfigException(ec, "Dummy config error");
            throw ex;
        } catch (LSConfigException ex) {
            String source = "" + ec;
            assertThat(ex).hasMessageContaining(source)
                    .hasMessageContaining("Dummy config error");
        }
    }

    @Test
    void constructInstanceWithNullArgument() {
        try {
            LSConfigException ex = new LSConfigException(null, "Dummy config error");
            throw ex;
        } catch (LSConfigException ex) {
            assertThat(ex).hasMessageContaining("null")
                    .hasMessageContaining("Dummy config error");
        }
    }

    @Test
    void constructInstanceWithNullArgument2() {
        try {
            LSConfigException ex = new LSConfigException(null, "Dummy config error", null);
            throw ex;
        } catch (LSConfigException ex) {
            assertThat(ex).hasMessageContaining("null")
                    .hasMessageContaining("Dummy config error");
        }
    }

    @Test
    void constructInstanceWithNullArgument3() {
        EnvConfiguration ec = new DynamicConfig();
        try {
            // EnvConfiguration ec = new ClasspathBasedConfig("config-test.properties", null);            
            LSConfigException ex = new LSConfigException(ec, "Dummy config error", new IOException("Dummy"));
            throw ex;
        } catch (LSConfigException ex) {
            String source = "" + ec;
            assertThat(ex).hasMessageContaining(source)
                    .hasMessageContaining("Dummy config error");
        }
    }
}
