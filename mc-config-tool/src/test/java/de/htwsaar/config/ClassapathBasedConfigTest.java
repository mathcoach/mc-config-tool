package de.htwsaar.config;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Set;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 *
 * @author hbui
 */
public class ClassapathBasedConfigTest {

	@Test
	public void constructInstance() {
		EnvConfiguration ec = new ClasspathBasedConfig("config-test.xml", null);
		String configValue = ec.getConfigValue("class-path");
		assertThat(configValue).isEqualTo("target/test-classes");
	}

	@Test
	public void experimental() throws Exception{

		Path p = addTheTestClasseDirToClassPath();

		ClasspathBasedConfig ec = new ClasspathBasedConfig("config-test.xml", null);
		String configValue = ec.getConfigValue("class-path");
		assertThat(configValue).isEqualTo("target/test-classes");
		
		assertThat(ec.getSearchDir()).contains(p.toString());
	}

	@Test
	public void countTheConfigParam() {
		EnvConfiguration ec = new ClasspathBasedConfig("config-count-test.xml", null);
		Set<String> configParam = ec.getAllConfigKeys();
		assertThat(configParam).hasSize(3);
	}

	@Test
	public void useSecondaryConfigFileIfPrimaryNotFound() {
		EnvConfiguration ec = new ClasspathBasedConfig("config-file-not-found.xml", "config-test.xml");
		String configValue = ec.getConfigValue("class-path");
		assertThat(configValue).isEqualTo("target/test-classes");
	}

	@Test
	public void thowExceptionIfBothConfigFilesNotFound() {
		try {
			EnvConfiguration ec = new ClasspathBasedConfig("config-file-not-found.xml", "secondary-config-test.xml");
			failBecauseExceptionWasNotThrown(LSConfigException.class);
		} catch (LSConfigException ex) {
			assertThat(ex).hasMessageContaining("Primary config file").hasMessageContaining("not found")
					.hasMessageContaining("secondary config file");
		}
	}

	@Test
	public void resolveSimpleRef() {
		EnvConfiguration ec = new ClasspathBasedConfig("test-config-subs-var.xml", null);
		String b = ec.getConfigValue("b");
		assertThat(b).isEqualTo(ec.getConfigValue("a"));
	}

	@Test
	public void detectCycle() {
		try {
			EnvConfiguration ec = new ClasspathBasedConfig("test-config-subs-var-cycle.xml", null);
			failBecauseExceptionWasNotThrown(LSConfigException.class);
		} catch (LSConfigException ex) {
			assertThat(ex).hasMessageContaining("Infinite loop in property interpolation of ${d}");
		}
	}

	@Test
	public void resolveSystemVar() {
		EnvConfiguration ec = new ClasspathBasedConfig("test-config-subs-system-var.xml", null);
		String home = ec.getConfigValue("home");
		System.out.println("home:" + home);
		assertThat(home).isEqualTo(System.getProperty("user.home"));
		String workingdir = ec.getConfigValue("working-dir");
		System.out.println("working dir:" + workingdir);
		assertThat(workingdir).isEqualTo(System.getProperty("user.dir"));
	}
	
	
	private Path addTheTestClasseDirToClassPath() throws Exception{
		File f = new File("./target/test-classes"); // add an directory to classpath
		Path path = f.toPath().toAbsolutePath().normalize();
		URL u = path.toFile().toURL();
		URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class urlClass = URLClassLoader.class;
		Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
		method.setAccessible(true);
		method.invoke(urlClassLoader, new Object[]{u});
		return path;
	}
}
