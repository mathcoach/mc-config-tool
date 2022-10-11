package de.htwsaar.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class is intended to be use in Unit test as a Mock Object of
 * {@link EnvConfiguration}.
 *
 * @author hbui
 */
public class DynamicConfig implements EnvConfiguration {

    private final Map<String, String> config;

    public DynamicConfig() {
        config = new HashMap<>();
    }

    public DynamicConfig(EnvConfiguration origin) {
        config = new HashMap<>();
        _mergeConfig(origin);
    }

    public DynamicConfig(Map<String, String> musterConfig) {
        config = new HashMap<>();
        Set<Map.Entry<String, String>> origin = EnvConfiguration.resolveConfigVariables(musterConfig).entrySet();
        for (Map.Entry<String, String> e : origin) {
            config.put(e.getKey(), e.getValue().trim());
        }
    }

    @Override
    public String getConfigValue(String configParameter) {
        return config.get(configParameter);
    }

    @Override
    public Set<String> getAllConfigKeys() {
        return config.keySet();
    }

    private void _mergeConfig(EnvConfiguration origin) { //NOSONAR (_ is used for internal)
        origin.getAllConfigKeys().stream().forEach(configKey
                -> config.put(configKey, origin.getConfigValue(configKey))
        );
    }

    public DynamicConfig mergeConfig(EnvConfiguration origin) {
        _mergeConfig(origin);
        return this;
    }

    public DynamicConfig config(String key, String value) {
        EnvConfiguration.setConfigValue(key, value, config);
        return this;
    }

}
