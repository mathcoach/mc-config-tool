package de.htwsaarland.config;

import de.htwsaarland.config.EnvConfiguration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author hbui
 */
public final class DynamicConfig implements EnvConfiguration{
	
	private final Map<String,String> config;

	public DynamicConfig(){
		config = new HashMap<>();
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
	
}
