package de.htwsaar.config;

import java.util.Map;
import java.util.Set;

/**
 * this class allows Configuration parameter with null value. This class is also
 * only for unit-test.
 * 
 * @author hbui
 */
public class NullableConfig extends DynamicConfig {
    
    public NullableConfig(Map<String, String> musterConfig) {
        super();
        Set<Map.Entry<String, String>> origin = EnvConfiguration.resolveConfigVariables(musterConfig).entrySet();
        for (Map.Entry<String, String> e : origin) {
            String value = e.getValue();
            value = (value == null) ? value : value.trim();
            config.put(e.getKey(),  value);
        }
    }
    
}
