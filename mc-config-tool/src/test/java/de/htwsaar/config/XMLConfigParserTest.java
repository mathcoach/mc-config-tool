package de.htwsaar.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;
import org.junit.Before;
/**
 *
 * @author hbui
 */
public class XMLConfigParserTest {
	
	private XMLConfigParser parser;
	
	@Before
	public void init(){
		parser = new XMLConfigParser();
	}

	@Test
	public void parserASimpleConfigFile() {
		String path = "./src/test/resources/test-config.xml";
		File simpleConfig = new File(path);
		Map<String,String> config = parser.parseConfigFile(simpleConfig);
		
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
		Map<String,String> config = parser.parseConfigFile(simpleConfig);
		assertThat(config.get("param-a")).isEqualTo("a");        //only main config has param-a
		assertThat(config.get("import-param-a")).isEqualTo("A"); //only imported config has param-b
		assertThat(config.get("param-b")).isEqualTo("b");        //main-config has precedence
		assertThat(config.get("param-c")).isEqualTo("c");        //like param-a, other name
		assertThat(config.get("param-e")).isNull();              //neither nor is configed
	}

	@Test
	public void parserSyntaxError() {
		try{
			String path = "./src/test/resources/test-config-with-syntax-error.xml";
			File simpleConfig = new File(path);
			Map<String,String> config = new HashMap<>();
			//EnvConfiguration.parseXMLConfigFile(simpleConfig, config, 0);
			parser.parseConfigFile(simpleConfig);
		}catch(LSConfigException ex){
			System.out.println("error msg:" + ex.getMessage());
		}
	}

	@Test
	public void parserNullFile() {
		try{
			parser.parseConfigFile(null);
			failBecauseExceptionWasNotThrown(LSConfigException.class);
		}catch(LSConfigException ex){
			System.out.println("error msg:" + ex.getMessage());
		}
	}
	
	@Test
	public void throwExceptionIfImportCycle() {
		String path = "./src/test/resources/test-config-cycle.xml";
		File simpleConfig = new File(path);
		try{
			parser.parseConfigFile(simpleConfig);
			failBecauseExceptionWasNotThrown(LSConfigException.class);
		}catch(LSConfigException ex){
			ex.printStackTrace();
			//OK
		}
	}
	
}
