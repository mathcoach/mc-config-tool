/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.htwsaar.config;

import java.io.File;
import java.util.Map;
import static org.fest.assertions.api.Assertions.*;
import org.junit.Test;
import org.junit.Before;

/**
 *
 * @author hbui
 */
public class PropertiesConfigParserTest {

	private PropertiesConfigParser parser;
	
	@Before
	public void init(){
		parser = new PropertiesConfigParser();
	}

	@Test
	public void parserASimpleConfigFile() {
		String path = "./src/test/resources/test-config.properties";
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
		String path = "./src/test/resources/test-config-with-import.properties";
		File simpleConfig = new File(path);
		Map<String,String> config = parser.parseConfigFile(simpleConfig);
		assertThat(config).containsKey("import");
		assertThat(config.get("import")).isEqualTo("${PWD}/src/test/resources/import-config.properties");
	}
	
	@Test
	public void parseNotExistFile() {
		try{
			parser.parseConfigFile(new File("not-exist-path.properties"));
			failBecauseExceptionWasNotThrown(LSConfigException.class);
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
