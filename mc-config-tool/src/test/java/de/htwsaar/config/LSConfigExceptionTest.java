package de.htwsaar.config;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hbui
 */
public class LSConfigExceptionTest {

	@Test
	public void constructInstance(){
		try{
			EnvConfiguration ec = new ClasspathBasedConfig("config-test.properties", null);
			LSConfigException ex = new LSConfigException(ec, "Dummy config error");
			throw ex;
		}catch(LSConfigException ex){
			assertThat(ex).hasMessageContaining("config-test.properties")
				.hasMessageContaining("Dummy config error");
		}
	}
	
	@Test
	public void constructInstanceWithNullArgument(){
		try{
			LSConfigException ex = new LSConfigException(null, "Dummy config error");
			throw ex;
		}catch(LSConfigException ex){
			assertThat(ex).hasMessageContaining("null")
				.hasMessageContaining("Dummy config error");
		}
	}
	
	@Test
	public void constructInstanceWithNullArgument2(){
		try{
			LSConfigException ex = new LSConfigException(null, "Dummy config error", null);
			throw ex;
		}catch(LSConfigException ex){
			assertThat(ex).hasMessageContaining("null")
				.hasMessageContaining("Dummy config error");
		}
	}
	
	@Test
	public void constructInstanceWithNullArgument3(){
		try{
			EnvConfiguration ec = new ClasspathBasedConfig("config-test.properties", null);
			LSConfigException ex = new LSConfigException(ec, "Dummy config error", new IOException("Dummy"));
			throw ex;
		}catch(LSConfigException ex){
			assertThat(ex).hasMessageContaining("Dummy")
				.hasMessageContaining("Dummy config error");
		}
	}
}