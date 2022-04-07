package de.htwsaar.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
/**
 *
 * @author hbui
 */
class DynamicConfigTest {
	
	@Test
	void createAEmptyConfig(){
		DynamicConfig config = new DynamicConfig();
		assertThat(config.getAllConfigKeys() ).isEmpty();
	}
	
	@Test
	void variablesInMusterConfiIsReolved(){
		Map<String,String> musterConfig = new HashMap<String,String>(){{
			put("config-dir","${HOME}/my-config"); // System variable
			put("param-a", "a");
			put("param-b", "${param-a}b");
		}};
		DynamicConfig config = new DynamicConfig(musterConfig);
		assertThat(config.getAllConfigKeys()).hasSize(3);
		assertThat(config.getConfigValue("config-dir"))
			.isEqualTo(System.getProperty("user.home").replaceAll(File.pathSeparator, "/")+"/my-config");
		assertThat(config.getConfigValue("param-b")).isEqualTo("ab");
	}
	
	@Test
	void originConfigIsAdapted(){
		Map<String,String> musterConfig = new HashMap<String,String>(){{
			put("config-dir","${HOME}/my-config"); // System variable
			put("param-a", "a");
			put("param-b", "${param-a}b");
		}};
		DynamicConfig origin = new DynamicConfig(musterConfig);
		DynamicConfig copy = new DynamicConfig(origin);
		assertThat(copy.getAllConfigKeys()).hasSize(3);
		assertThat(copy.getConfigValue("config-dir"))
			.isEqualTo(System.getProperty("user.home").replaceAll(File.pathSeparator, "/")+"/my-config");
		assertThat(copy.getConfigValue("param-b")).isEqualTo("ab");
	}
	
	@Test
	void variableIsResolvedBySetANewConfig(){
		Map<String,String> musterConfig = new HashMap<String,String>(){{
			put("config-dir","${HOME}/my-config"); // System variable
			put("param-a", "a");
			put("param-b", "${param-a}b");
		}};
		DynamicConfig origin = new DynamicConfig(musterConfig);
		origin.config("param-c", "${param-a}${param-b}");
		assertThat(origin.getConfigValue("param-c")).isEqualTo("aab");
	}
	
	@Test
	void mergeConfigOverrideOldValue(){
		Map<String,String> musterConfig = new HashMap<String,String>(){{
			put("config-dir","${HOME}/my-config"); // System variable
			put("param-a", "a");
			put("param-b", "${param-a}b");
		}};
		DynamicConfig origin = new DynamicConfig(musterConfig);
		DynamicConfig otherConfig = new DynamicConfig()
				.config("param-a", "A")
				.config("param-c", "C");
		origin.mergeConfig(otherConfig);
		assertThat(origin.getAllConfigKeys()).hasSize(4);
		assertThat(origin.getConfigValue("param-a")).isEqualTo("A");
		assertThat(origin.getConfigValue("param-b")).isEqualTo("ab"); // param-a is resoled only once
		assertThat(origin.getConfigValue("param-c")).isEqualTo("C");
	}
}
