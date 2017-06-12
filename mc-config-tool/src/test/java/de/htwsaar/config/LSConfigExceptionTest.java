package de.htwsaar.config;

import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 *
 * @author hbui
 */
public class LSConfigExceptionTest {

	@Test
	public void constructInstance(){
		try{
			EnvConfiguration ec = new ClasspathBasedConfig("config-test.xml", null);
			LSConfigException ex = new LSConfigException(ec, "Dummy config error");
			throw ex;
		}catch(LSConfigException ex){
			assertThat(ex).hasMessageContaining("config-test.xml")
				.hasMessageContaining("Dummy config error");
		}
	}
}