package de.htwsaar.config;

import java.io.File;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 *
 * @author hbui
 */
public class ConfigParserFactoryTest {
	
	@Test
	public void pickXMLParser(){
		File f = new File("my-config.xml");
		ConfigParser p = ConfigParserFactory.getParserForFile(f);
		assertThat(p).isInstanceOf(XMLConfigParser.class);
	}
	
	@Test
	public void pickPropertiesParser(){
		File f = new File("my-config.properties");
		ConfigParser p = ConfigParserFactory.getParserForFile(f);
		assertThat(p).isInstanceOf(PropertiesConfigParser.class);
	}
	
	@Test
	public void throwExcption(){
		File f = new File("my-config.not-know");
		try{
			ConfigParser p = ConfigParserFactory.getParserForFile(f);
			failBecauseExceptionWasNotThrown(LSConfigException.class);
		}catch(LSConfigException ex){
			assertThat(ex).hasMessageContaining("not-know");
		}
	}
}
