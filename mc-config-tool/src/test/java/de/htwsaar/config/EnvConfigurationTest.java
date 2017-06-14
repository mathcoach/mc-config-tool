package de.htwsaar.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;
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
