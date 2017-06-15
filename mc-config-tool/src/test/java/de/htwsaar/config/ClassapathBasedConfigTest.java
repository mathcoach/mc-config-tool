package de.htwsaar.config;

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
	
	@Test
	public void resolveSimpleRef(){
		EnvConfiguration ec = new ClasspathBasedConfig("test-config-subs-var.xml", null);
		String b = ec.getConfigValue("b");
		assertThat(b).isEqualTo(ec.getConfigValue("a"));
	}
	
	@Test
	public void detectCycle(){
		try{
			EnvConfiguration ec = new ClasspathBasedConfig("test-config-subs-var-cycle.xml", null);
			failBecauseExceptionWasNotThrown(LSConfigException.class);
		}catch(LSConfigException ex){
			assertThat(ex).hasMessageContaining("Infinite loop in property interpolation of ${d}");
		}
	}
	
	@Test
	public void resolveSystemVar(){
		EnvConfiguration ec = new ClasspathBasedConfig("test-config-subs-system-var.xml", null);
		String home = ec.getConfigValue("home");
		System.out.println("home:" + home);
		assertThat(home).isEqualTo(System.getProperty("user.home"));
		String workingdir = ec.getConfigValue("working-dir");
		System.out.println("working dir:" + workingdir);
		assertThat(workingdir).isEqualTo(System.getProperty("user.dir"));
	}
}