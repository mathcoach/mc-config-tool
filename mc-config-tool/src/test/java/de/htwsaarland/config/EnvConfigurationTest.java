package de.htwsaarland.config;

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
	public void parserASimpleConfigFile() {
		String path = "./src/test/resources/test-config.xml";
		File simpleConfig = new File(path);
		Map<String,String> config = new HashMap<>();
		EnvConfiguration.parseConfigFile(simpleConfig, config);
		assertThat(config.get("param-a")).isEqualTo("a");
		assertThat(config.get("param-b")).isEqualTo("b");
		assertThat(config.get("param-c")).isEqualTo("");
		assertThat(config.get("param-d")).isEqualTo("");
		assertThat(config.get("param-e")).isNull();
	}

	@Test
	public void parserAConfigFileWithImportRelativeFile() {
		String path = "./src/test/resources/test-config-with-import.xml";
		File simpleConfig = new File(path);
		Map<String,String> config = new HashMap<>();
		EnvConfiguration.parseConfigFile(simpleConfig, config);
		assertThat(config.get("param-a")).isEqualTo("a");        //only main config has param-a
		assertThat(config.get("import-param-a")).isEqualTo("A"); //only imported config has param-b
		assertThat(config.get("param-b")).isEqualTo("b");        //main-config has precedence
		assertThat(config.get("param-c")).isEqualTo("c");        //like param-a, other name
		assertThat(config.get("param-e")).isNull();              //neither nor is configed
	}

	@Test
	public void throwExceptionIfImportCycle() {
		String path = "./src/test/resources/test-config-cycle.xml";
		File simpleConfig = new File(path);
		Map<String,String> config = new HashMap<>();
		try{
			EnvConfiguration.parseConfigFile(simpleConfig, config);
			fail("Expected a " +LSConfigException.class.getName());
		}catch(LSConfigException ex){
			ex.printStackTrace();
			//OK
		}
	}
}
