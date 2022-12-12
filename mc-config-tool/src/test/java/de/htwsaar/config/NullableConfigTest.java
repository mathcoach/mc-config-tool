package de.htwsaar.config;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hbui
 */
class NullableConfigTest {
    
   
    @Test
    void testNullConfigIsAllow() {
        Map<String,String> musterconfig = new HashMap<String,String>(){{
            put("config-key", null);
        }};
        NullableConfig config = new NullableConfig(musterconfig);
        assertThat(config.getConfigValue("config-key")).isNull();
    }
    
}
