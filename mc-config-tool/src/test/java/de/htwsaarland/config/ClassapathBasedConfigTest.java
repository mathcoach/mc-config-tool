package de.htwsaarland.config;

import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 *
 * @author hbui
 */
public class ClassapathBasedConfigTest {
	
	public ClassapathBasedConfigTest() {
	}

	@Test
	public void constructInstance(){
		EnvConfiguration ec = new ClasspathBasedConfig("config-test.xml", null);
		String configValue = ec.getConfigValue("class-path");
		assertThat(configValue).isEqualTo("target/test-classes");
	}
}