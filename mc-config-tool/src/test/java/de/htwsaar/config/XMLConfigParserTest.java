package de.htwsaar.config;

import java.io.File;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
/**
 *
 * @author hbui
 */
public class XMLConfigParserTest {
	
	private XMLConfigParser parser;
	
	@BeforeAll
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
	public void parseConfigFileWithImport(){
		String path = "./src/test/resources/test-config-with-import.xml";
		File simpleConfig = new File(path);
		Map<String,String> config = parser.parseConfigFile(simpleConfig);
		assertThat(config).containsKey("import");
		assertThat(config.get("import")).isEqualTo("${PWD}/src/test/resources/import-config.xml");
	}

	@Test
	public void parserSyntaxError() {
		try{
			String path = "./src/test/resources/test-config-with-syntax-error.xml";
			File simpleConfig = new File(path);
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
	
}
