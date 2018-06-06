package de.htwsaar.config.processor;

import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import org.junit.Test;
import static org.fest.assertions.api.Assertions.*;

/**
 *
 * @author hbui
 */
public class MCAbstractAnnotationProcessorTest {
	
	static class RegTester extends MCAbstractAnnotationProcessor{

		@Override
		public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
			throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		}
		
	}
	RegTester regtester = new RegTester();
	
	@Test
	public void regexRecognizeCorrectPackage(){
		boolean valid = regtester.validePackageName("aAaa.Bbb.ccC");
		assertThat(valid).isTrue();
	}
	
	@Test
	public void regexRecognizeCorrectPackage2(){
		boolean valid = regtester.validePackageName("a1A.b2Bb.cC3");
		assertThat(valid).isTrue();
	}
	
	@Test
	public void regexRecognizeCorrectPackage3(){
		boolean valid = regtester.validePackageName("a_1.b2.ccc");
		assertThat(valid).isTrue();
	}
	
	@Test
	public void regexRecognizeCorrectPackage4(){
		boolean valid = regtester.validePackageName("abc");
		assertThat(valid).isTrue();
	}
	
	// Test of ReDoS, s. https://www.owasp.org/index.php/Regular_expression_Denial_of_Service_-_ReDoS
	// this tesh should not take too much time, about < 6 ms
	@Test 
	public void regexRecognizeCorrectPackage5(){
		StringBuffer b = new StringBuffer(MCAbstractAnnotationProcessor.MAX_PACKAGE_NAME_LENGTH);
		while(b.length() < MCAbstractAnnotationProcessor.MAX_PACKAGE_NAME_LENGTH){
			b.append("a");
		}
		boolean valid = regtester.validePackageName(b.toString());
		assertThat(valid).isTrue();
	}
	
	@Test 
	public void regexRecognizeCorrectPackage6(){
		StringBuffer b = new StringBuffer(MCAbstractAnnotationProcessor.MAX_PACKAGE_NAME_LENGTH);
		while(b.length() < MCAbstractAnnotationProcessor.MAX_PACKAGE_NAME_LENGTH){
			b.append("a");
		}
		b.append("b");
		boolean valid = regtester.validePackageName(b.toString());
		assertThat(valid).isFalse();
	}
	
	@Test
	public void regexNotRecognizeIncorrectPackage(){
		boolean valid = regtester.validePackageName("1a.b2.ccc");
		assertThat(valid).isFalse();
	}
	
	@Test
	public void regexNotRecognizeIncorrectPackage2(){
		boolean valid = regtester.validePackageName("$a.b2.ccc");
		assertThat(valid).isFalse();
	}
	
	@Test
	public void regexNotRecognizeIncorrectPackage3(){
		boolean valid = regtester.validePackageName("!a.b2.ccc");
		assertThat(valid).isFalse();
	}
	
	@Test
	public void regexNotRecognizeIncorrectPackage4(){
		boolean valid = regtester.validePackageName("a!b.b2.ccc");
		assertThat(valid).isFalse();
	}
	
	@Test
	public void regexNotRecognizeIncorrectPackage5(){
		boolean valid = regtester.validePackageName("aab.2b.ccc");
		assertThat(valid).isFalse();
	}
}
