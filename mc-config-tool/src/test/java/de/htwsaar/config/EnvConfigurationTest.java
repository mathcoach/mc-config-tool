package de.htwsaar.config;

import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;
/**
 *
 * @author hbui
 */
public class EnvConfigurationTest {
	

	@Test
	public void resolveHOME_Variable() {
		String pathWithHomeVar = "${HOME}/mypath/test.xml";
		String resolveSystemProperties = EnvConfiguration.resolveSystemProperties(pathWithHomeVar);
		String home = System.getProperty("user.home");
		assertThat(resolveSystemProperties)
			.startsWith(home)
			.endsWith("/mypath/test.xml")
			.hasSize(home.length() + "/mypath/test.xml".length());
	}
	
	@Test
	public void resolveHOME_Variable2() {
		String pathWithHomeVar = "$HOME/mypath/test.xml";
		String resolveSystemProperties = EnvConfiguration.resolveSystemProperties(pathWithHomeVar);
		String home = System.getProperty("user.home");
		assertThat(resolveSystemProperties)
			.startsWith(home)
			.endsWith("/mypath/test.xml")
			.hasSize(home.length() + "/mypath/test.xml".length());
	}


}
