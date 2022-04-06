package de.htwsaar.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.BeforeAll;

/**
 *
 * @author hbui
 */
public class EnvConfigurationTest {
	

	@Test
	void resolveHOME_Variable() {
		String pathWithHomeVar = "${HOME}/mypath/test.xml";
		String resolveSystemProperties = EnvConfiguration.resolveSystemProperties(pathWithHomeVar);
		String home = System.getProperty("user.home");
		assertThat(resolveSystemProperties)
			.startsWith(home)
			.endsWith("/mypath/test.xml")
			.hasSize(home.length() + "/mypath/test.xml".length());
	}
	
	@Test
	void resolveHOME_Variable2() {
		String pathWithHomeVar = "$HOME/mypath/test.xml";
		String resolveSystemProperties = EnvConfiguration.resolveSystemProperties(pathWithHomeVar);
		String home = System.getProperty("user.home");
		assertThat(resolveSystemProperties)
			.startsWith(home)
			.endsWith("/mypath/test.xml")
			.hasSize(home.length() + "/mypath/test.xml".length());
	}

	@Test
	void resolveVariables(){
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
	void detectCycleInVariableResolution(){
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
	void resolveSystemVar(){
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
	void doNotSetNewConfigIfVarNotSolved(){
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
	void doNotSetNewConfigIfVarNotSolved2(){
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
	void setNewConfigIfVarSolved(){
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
	void setNewConfigIfNoVarRef(){
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
	
	static final Path IMPORT_CONFIG_FILE = Paths.get("./target/import-config.txt");// <= problematic with path
	static final Path MAIN_CONFIG_FILE = Paths.get("./target/main-config");
	
	@BeforeAll
	static void initDummyConfigFiles() throws IOException{
		//Path configFile = Paths.get(IMPORT_CONFIG_FILE);
		try{
			Files.createFile(IMPORT_CONFIG_FILE);
		}catch(FileAlreadyExistsException ex) {
			// Nothing to do
		}
	}
	
	@Test
	void parserAConfigFileWithImportRelativeFile() {
		
		File simpleConfig = MAIN_CONFIG_FILE.toFile();//Dummy file only
		Map<String,String> config = EnvConfiguration.resolveImportConfig(simpleConfig, new DummyConfigParser());
		assertThat(config.get("param-a")).isEqualTo("a");        //only main config has param-a
		assertThat(config.get("import-param-a")).isEqualTo("A"); //only imported config has import-param-a
		assertThat(config.get("param-b")).isEqualTo("B");        //imported config has precedence
		assertThat(config.get("param-c")).isEqualTo("c");        //like param-a, other name
		assertThat(config.get("param-e")).isNull();              //neither nor is configed
	}
	
	class DummyConfigParser implements ConfigParser{
		
		final Map<String,String> masterConfig = new HashMap<String,String>(){{
			put(EnvConfiguration.IMPORT_KEY, IMPORT_CONFIG_FILE.toString() );
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
			Path normalizeConfig = configFile.toPath().normalize();
			Path normalizeMainCfg = MAIN_CONFIG_FILE.normalize();
			Path normalizeImportCfg = IMPORT_CONFIG_FILE.normalize();
			
			if ( normalizeConfig.endsWith(normalizeMainCfg) ){
				return masterConfig;
			}else if( normalizeConfig.endsWith(normalizeImportCfg) ) {
				return importedConfig;
			}else{
				throw new IllegalStateException(configFile.getAbsolutePath() + " not match " + normalizeMainCfg. toString() + " or " + normalizeImportCfg);
			}
		}
	}
	
	@Test
	void throwExceptionIfImportCycle() {		
		File simpleConfig = MAIN_CONFIG_FILE.toFile();
		try{
			EnvConfiguration.resolveImportConfig(simpleConfig, new CycleConfigParser() );
			failBecauseExceptionWasNotThrown(LSConfigException.class);
		}catch(LSConfigException ex){			
			assertThat(ex).hasMessageContaining("Import too many level");
		}
	}

	class CycleConfigParser implements ConfigParser{
		
		final Map<String,String> masterConfig = new HashMap<String,String>(){{
			put(EnvConfiguration.IMPORT_KEY, IMPORT_CONFIG_FILE.toString());
		}};
		
		final Map<String,String> importedConfig = new HashMap<String,String>(){{
			put(EnvConfiguration.IMPORT_KEY, MAIN_CONFIG_FILE.toString() );
		}};
		
		@Override
		public Map<String, String> parseConfigFile(File configFile) {
			Path normalizeConfig = configFile.toPath().normalize();
			Path normalizeMainCfg = MAIN_CONFIG_FILE.normalize();
			Path normalizeImportCfg = IMPORT_CONFIG_FILE.normalize();
			
			if ( normalizeConfig.endsWith(normalizeMainCfg) ){
				return masterConfig;
			}else if( normalizeConfig.endsWith(normalizeImportCfg) ) {
				return importedConfig;
			}else{
				throw new IllegalStateException(configFile.getAbsolutePath() + " not match " + normalizeMainCfg. toString() + " or " + normalizeImportCfg);
			}
		}
		
	}
	
	@Test
	void useDefaultValueWhenConfigurationNotExist() {
		EnvConfiguration configuration = new DynamicConfig();
		String defaultValue = "myValue";
		String configValue = configuration.getConfigValue("not-exist-config", defaultValue);
		assertThat(configValue).isEqualTo(defaultValue);
	}

	@Test
	void useConfiguredValueWhenConfigurationExist() {
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
	void useHandlerWhenConfigurationNotExist() {
		EnvConfiguration configuration = new DynamicConfig();
		String defaultValue = "myValue";
		String configValue = configuration.getConfigValue("not-exist-config", (config) -> defaultValue );
		assertThat(configValue).isEqualTo(defaultValue);
	}

	@Test
	void doNotCallHandlerWhenConfigurationExist() {
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

	
	@Test
	void raiseExceptionIfImportFileNotExist() {
		final String dummyNotExistFile = "dummy_not_exist_file";
		try{
			ConfigParser p = new ImportNotExistFileParser(dummyNotExistFile);
			File ignoredCfgFile = new File("ignore_me");
			EnvConfiguration.resolveImportConfig(ignoredCfgFile, p);
			failBecauseExceptionWasNotThrown(ImportCfgFileNotFound.class);
		}catch(ImportCfgFileNotFound ex) {
			assertThat(ex).hasMessageContaining(dummyNotExistFile);
            Path dummy = ex.getImportedPath();
            assertThat(dummy).doesNotExist();
		}
	}
	
	class ImportNotExistFileParser implements ConfigParser {

		Map<String,String> config ;

		public ImportNotExistFileParser(String notExistFile) {
			config = Map.of(EnvConfiguration.IMPORT_KEY, notExistFile);
		}
		
		
		@Override
		public Map<String, String> parseConfigFile(File configFile) {
			// Ignore the config file
			return config;
		}
		
	}
	
	
	
}
