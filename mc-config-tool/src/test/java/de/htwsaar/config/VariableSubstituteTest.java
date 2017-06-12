package de.htwsaar.config;

import de.htwsaar.config.ClasspathBasedConfig;
import de.htwsaar.config.LSConfigException;
import de.htwsaar.config.EnvConfiguration;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;
/**
 *
 * @author hbui
 */
public class VariableSubstituteTest {
	
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
			//OK
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
	
	@Test
	public void doNotSetNewConfigIfVarNotSolved(){
		ConfigTest ct = new ConfigTest("test-config.xml", null);
		try{
			ct.testAddNewConfig("new-param", "string mit var ${not-def}");
			failBecauseExceptionWasNotThrown(LSConfigException.class);
		}catch(LSConfigException ex){
			assertThat(ex).hasMessageContaining("${not-def}")
				.hasMessageContaining("Cannot find");
			assertThat(ct.getConfigValue("new-param")).isNull();
		}
	}
	
	@Test
	public void doNotSetNewConfigIfVarNotSolved2(){
		ConfigTest ct = new ConfigTest("test-config.xml", null);
		try{
			ct.testAddNewConfig("new-param", "string mit var ${not-def} und var |${param-a}|");
			failBecauseExceptionWasNotThrown(LSConfigException.class);
		}catch(LSConfigException ex){
			//OK
			assertThat(ex).hasMessageContaining("${not-def}")
				.hasMessageContaining("Cannot find");
			assertThat( ct.getConfigValue("new-param") ).isNull();//Nothing should be written in config table
				
		}
	}
	
	@Test
	public void setNewConfigIfVarSolved(){
		ConfigTest ct = new ConfigTest("test-config.xml", null);
		ct.testAddNewConfig("new-param", "use param-a |${param-a}|");
		String newParam = ct.getConfigValue("new-param");
		assertThat(newParam).isEqualTo("use param-a |a|");
	}

	@Test
	public void setNewConfigIfNoVarRef(){
		ConfigTest ct = new ConfigTest("test-config.xml", null);
		ct.testAddNewConfig("new-param", "new config");
		String newParam = ct.getConfigValue("new-param");
		assertThat(newParam).isEqualTo("new config");
	}
	
	private class ConfigTest extends ClasspathBasedConfig{
		
		public ConfigTest(String primaryConfigFileName, String secondaryConfigFileName) {
			super(primaryConfigFileName, secondaryConfigFileName);
		}

		public void testAddNewConfig(String parameter, String value){
			super.setConfigValue(parameter, value);
		}
	}
}
