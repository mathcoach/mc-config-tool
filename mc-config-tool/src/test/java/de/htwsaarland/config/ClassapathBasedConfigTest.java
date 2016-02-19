package de.htwsaarland.config;

import java.util.Set;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 *
 * @author hbui
 */
public class ClassapathBasedConfigTest {

	@Test
	public void constructInstance(){
		EnvConfiguration ec = new ClasspathBasedConfig("config-test.xml", null);
		String configValue = ec.getConfigValue("class-path");
		assertThat(configValue).isEqualTo("target/test-classes");
	}
	
	@Test
	public void countTheConfigParam(){
		EnvConfiguration ec = new ClasspathBasedConfig("config-count-test.xml", null);
		Set<String> configParam = ec.getAllConfigKeys();
		assertThat(configParam).hasSize(3);
	}
	
	@Test
	public void useSecondaryConfigFileIfPrimaryNotFound(){
		EnvConfiguration ec = new ClasspathBasedConfig("config-file-not-found.xml", "config-test.xml");
		String configValue = ec.getConfigValue("class-path");
		assertThat(configValue).isEqualTo("target/test-classes");
	}

	@Test
	public void thowExceptionIfBothConfigFilesNotFound(){
		try{
			EnvConfiguration ec = new ClasspathBasedConfig("config-file-not-found.xml", "secondary-config-test.xml");
			failBecauseExceptionWasNotThrown(LSConfigException.class);
		}catch(LSConfigException ex){
			assertThat(ex).hasMessage("No Config file found");
		}
	}
}