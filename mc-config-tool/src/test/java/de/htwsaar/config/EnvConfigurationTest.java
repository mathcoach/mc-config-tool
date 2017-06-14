package de.htwsaar.config;

import java.io.File;
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

	//@Ignore
	@Test
	public void parserAConfigFileWithImportRelativeFile() {
		String path = "./src/test/resources/test-config-with-import.xml";
		File simpleConfig = new File(path);
		//Map<String,String> config = parser.parseConfigFile(simpleConfig);
		Map<String,String> config = EnvConfiguration.resolveImportConfig(simpleConfig, new XMLConfigParser());
		assertThat(config.get("param-a")).isEqualTo("a");        //only main config has param-a
		assertThat(config.get("import-param-a")).isEqualTo("A"); //only imported config has import-param-a
		assertThat(config.get("param-b")).isEqualTo("B");        //imported config has precedence
		assertThat(config.get("param-c")).isEqualTo("c");        //like param-a, other name
		assertThat(config.get("param-e")).isNull();              //neither nor is configed
	}
	
	//@Ignore
	@Test
	public void throwExceptionIfImportCycle() {
		String path = "./src/test/resources/test-config-cycle.xml";
		File simpleConfig = new File(path);
		try{
			//parser.parseConfigFile(simpleConfig);
			EnvConfiguration.resolveImportConfig(simpleConfig, new XMLConfigParser() );
			failBecauseExceptionWasNotThrown(LSConfigException.class);
		}catch(LSConfigException ex){
			//ex.printStackTrace();
			assertThat(ex).hasMessageContaining("Import too many level");
		}
	}
}
