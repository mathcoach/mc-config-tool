package de.htwsaar.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class is intended to be used in tests.
 * @author hbui
 */
public final class DynamicConfig implements EnvConfiguration{
	
	private final Map<String,String> config;

	public DynamicConfig(){
		config = new HashMap<>();
	}
	
	public DynamicConfig(EnvConfiguration origin){
		config = new HashMap<>();
		_mergConfig(origin);
	}
	
	public DynamicConfig(Map<String,String> musterConfig){
		config = new HashMap<>();
		config.putAll(musterConfig);
	}
	
	@Override
	public String getConfigValue(String configParameter) {
		return config.get(configParameter);
	}

	@Override
	public Set<String> getAllConfigKeys() {
		return config.keySet();
	}
	private void _mergConfig(EnvConfiguration origin){
		origin.getAllConfigKeys().stream().forEach((configKey) -> {
			config.put(configKey, origin.getConfigValue(configKey));
		});
	}
	public DynamicConfig mergeConfig(EnvConfiguration origin){
		_mergConfig(origin);
		return this;
	}
	public DynamicConfig config(String key, String value){
		config.put(key, value);
		return this;
	}
}
