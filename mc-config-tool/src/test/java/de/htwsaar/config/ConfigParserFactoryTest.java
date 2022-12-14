package de.htwsaar.config;

import java.io.File;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hbui
 */
class ConfigParserFactoryTest {
	
	
	
	@Test
	void pickPropertiesParser(){
		File f = new File("my-config.properties");
		ConfigParser p = ConfigParserFactory.getParserForFile(f);
		assertThat(p).isInstanceOf(PropertiesConfigParser.class);
	}
	
	@Test
	void throwExcption(){
		File f = new File("my-config.not-know");
		try{
			ConfigParser p = ConfigParserFactory.getParserForFile(f);
			failBecauseExceptionWasNotThrown(LSConfigException.class);
		}catch(LSConfigException ex){
			assertThat(ex).hasMessageContaining("not-know");
		}
	}
}
