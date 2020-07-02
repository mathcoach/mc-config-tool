package de.htwsaar.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.fest.assertions.api.Assertions.*;
import org.fest.assertions.data.MapEntry;
/**
 *
 * @author hbui
 */
public class EnvConfigurationTest {
	

	@Test
	public void resolveHOME_Variable() {
		String pathWithHomeVar = "${HOME}/mypath/test.xml";
		String resolveSystemProperties = EnvConfiguration.resolveSystemProperties(pathWithHomeVar);
		String home = System.getProperty("user.home");
		assertThat(resolveSystemProperties)
			.startsWith(home)
			.endsWith("/mypath/test.xml")
			.hasSize(home.length() + "/mypath/test.xml".length());
	}
	
	@Test
	public void resolveHOME_Variable2() {
		String pathWithHomeVar = "$HOME/mypath/test.xml";
		String resolveSystemProperties = EnvConfiguration.resolveSystemProperties(pathWithHomeVar);
		String home = System.getProperty("user.home");
		assertThat(resolveSystemProperties)
			.startsWith(home)
			.endsWith("/mypath/test.xml")
			.hasSize(home.length() + "/mypath/test.xml".length());
	}

	@Test
	public void resolveVariables(){
		Map<String,String> originConfig = new HashMap<String,String>(){{
			put("a", "A");
			put("b", "${a}");
		}};
		Map<String,String> config = EnvConfiguration.resolveConfigVariables(originConfig);
		assertThat(config).hasSameSizeAs(originConfig.entrySet())
				.contains(MapEntry.entry("a", "A"))
				.contains(MapEntry.entry("b", "A"));
	}
	
	@Test
	public void detectCycleInVariableResolution(){
		Map<String,String> originConfig = new HashMap<String,String>(){{
			put("a", "A");
			put("b", "${a}");
			
			put("c", "${d}");
			put("d", "${e}");
			put("e", "${c}");
		}};
		try{
			EnvConfiguration.resolveConfigVariables(originConfig);
		}catch(LSConfigException ex){
			assertThat(ex).hasMessageContaining("Infinite loop in property interpolation of ${d}");
		}
	}
	
	@Test
	public void resolveSystemVar(){
		Map<String,String> originConfig = new HashMap<String,String>(){{
			put("home", "${HOME}");
			put("working-dir", "${user.dir}");
		}};
		Map<String,String> config = EnvConfiguration.resolveConfigVariables(originConfig);
		assertThat(config).hasSameSizeAs(originConfig.entrySet())
			.contains(MapEntry.entry("home", System.getProperty("user.home")))
			.contains(MapEntry.entry("working-dir", System.getProperty("user.dir")));
	}
	
	
	@Test
	public void doNotSetNewConfigIfVarNotSolved(){
		Map<String,String> originConfig = new HashMap<String,String>(){{
			put("param-a", "a");
			put("pram-b", "b");
		}};
		try{
			EnvConfiguration.setConfigValue("new-param","string mit var ${not-def}",originConfig);
		}catch(LSConfigException ex){
			assertThat(ex).hasMessageContaining("${not-def}")
				.hasMessageContaining("Cannot find");
			assertThat(originConfig).doesNotContainKey("new-param");
		}
	}
	
	@Test
	public void doNotSetNewConfigIfVarNotSolved2(){
		Map<String,String> originConfig = new HashMap<String,String>(){{
			put("param-a", "a");
			put("param-b", "b");
		}};
		try{
			EnvConfiguration.setConfigValue("new-param","string mit var ${not-def} und var ${param-a}",originConfig);
		}catch(LSConfigException ex){
			assertThat(ex).hasMessageContaining("${not-def}")
				.hasMessageContaining("Cannot find");
			assertThat(originConfig).hasSize(2)
					.doesNotContainKey("new-param")
					.contains(MapEntry.entry("param-b", "b"))
					.contains(MapEntry.entry("param-a", "a"));
		}
	}
	
	@Test
	public void setNewConfigIfVarSolved(){
		Map<String,String> originConfig = new HashMap<String,String>(){{
			put("param-a", "a");
			put("param-b", "b");
		}};
		EnvConfiguration.setConfigValue("new-param", "use param-a |${param-a}|",originConfig);
		assertThat(originConfig)
			.contains(MapEntry.entry("param-a", "a"))
			.contains(MapEntry.entry("param-b", "b"))
			.contains(MapEntry.entry("new-param", "use param-a |a|"));
	}
	
	@Test
	public void setNewConfigIfNoVarRef(){
		Map<String,String> originConfig = new HashMap<String,String>(){{
			put("param-a", "a");
			put("param-b", "b");
		}};
		EnvConfiguration.setConfigValue("new-param", "new-config", originConfig);
		assertThat(originConfig)
			.contains(MapEntry.entry("param-a", "a"))
			.contains(MapEntry.entry("param-b", "b"))
			.contains(MapEntry.entry("new-param", "new-config"));
	}
	
	@Test
	public void parserAConfigFileWithImportRelativeFile() {
		
		File simpleConfig = new File(MAIN_CONFIG_FILE);//Dummy file only
		Map<String,String> config = EnvConfiguration.resolveImportConfig(simpleConfig, new DummyConfigParser());
		assertThat(config.get("param-a")).isEqualTo("a");        //only main config has param-a
		assertThat(config.get("import-param-a")).isEqualTo("A"); //only imported config has import-param-a
		assertThat(config.get("param-b")).isEqualTo("B");        //imported config has precedence
		assertThat(config.get("param-c")).isEqualTo("c");        //like param-a, other name
		assertThat(config.get("param-e")).isNull();              //neither nor is configed
	}
	
	@Test
	public void throwExceptionIfImportCycle() {
		
		File simpleConfig = new File(MAIN_CONFIG_FILE);
		try{
			EnvConfiguration.resolveImportConfig(simpleConfig, new CycleConfigParser() );
			failBecauseExceptionWasNotThrown(LSConfigException.class);
		}catch(LSConfigException ex){
			//ex.printStackTrace();
			assertThat(ex).hasMessageContaining("Import too many level");
		}
	}

	@Test
	public void useDefaultValueWhenConfigurationNotExist() {
		EnvConfiguration configuration = new DynamicConfig();
		String defaultValue = "myValue";
		String configValue = configuration.getConfigValue("not-exist-config", defaultValue);
		assertThat(configValue).isEqualTo(defaultValue);
	}

	@Test
	public void useConfiguredValueWhenConfigurationExist() {
		String myConfig = "my-config";
		String myValue = "my-value";
		EnvConfiguration configuration = new DynamicConfig(
			new HashMap<String,String>(){{
				put(myConfig, myValue);
			}}
		);
		String defaultValue = "default-value";
		String configValue = configuration.getConfigValue(myConfig, defaultValue);
		assertThat(configValue).isEqualTo(myValue);
	}

	@Test
	public void useHandlerWhenConfigurationNotExist() {
		EnvConfiguration configuration = new DynamicConfig();
		String defaultValue = "myValue";
		String configValue = configuration.getConfigValue("not-exist-config", (config) -> defaultValue );
		assertThat(configValue).isEqualTo(defaultValue);
	}

	@Test
	public void doNotCallHandlerWhenConfigurationExist() {
		String myConfig = "my-config";
		String myValue = "my-value";
		EnvConfiguration configuration = new DynamicConfig(
			new HashMap<String,String>(){{
				put(myConfig, myValue);
			}}
		);
		String defaultValue = "default-value";
		String configValue = configuration.getConfigValue(myConfig, (config) -> defaultValue );
		assertThat(configValue).isEqualTo(myValue);
	}


	final String IMPORT_CONFIG_FILE = "imported-config";
	final String MAIN_CONFIG_FILE = "main-config";
	class DummyConfigParser implements ConfigParser{
		
		final Map<String,String> masterConfig = new HashMap<String,String>(){{
			put(EnvConfiguration.IMPORT_KEY, IMPORT_CONFIG_FILE);
			put("param-a", "a");
			put("param-b", "b");
			put("param-c", "c");
		}};
		
		final Map<String,String> importedConfig = new HashMap<String,String>(){{
			put("import-param-a","A");
			put("param-b", "B");
		}};
		
		
		@Override
		public Map<String, String> parseConfigFile(File configFile) {
			String fileName = configFile.getName();
			if (fileName.endsWith(MAIN_CONFIG_FILE)){
				return masterConfig;
			}else if(fileName.endsWith(IMPORT_CONFIG_FILE)) {
				return importedConfig;
			}else{
				throw new IllegalStateException("Geht nicht");
			}
		}
	}
	
	class CycleConfigParser implements ConfigParser{
		
		final Map<String,String> masterConfig = new HashMap<String,String>(){{
			put(EnvConfiguration.IMPORT_KEY, IMPORT_CONFIG_FILE);
		}};
		
		final Map<String,String> importedConfig = new HashMap<String,String>(){{
			put(EnvConfiguration.IMPORT_KEY, MAIN_CONFIG_FILE);
		}};
		
		@Override
		public Map<String, String> parseConfigFile(File configFile) {
			String fileName = configFile.getName();
			if (fileName.endsWith(MAIN_CONFIG_FILE)){
				return masterConfig;
			}else if(fileName.endsWith(IMPORT_CONFIG_FILE)) {
				return importedConfig;
			}else{
				throw new IllegalStateException("Geht nicht");
			}
		}
		
	}
}
