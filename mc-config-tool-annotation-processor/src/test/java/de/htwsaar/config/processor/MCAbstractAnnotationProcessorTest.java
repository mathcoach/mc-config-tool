package de.htwsaar.config.processor;

import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author hbui
 */
class MCAbstractAnnotationProcessorTest {

    static class RegTester extends MCAbstractAnnotationProcessor {

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
    RegTester regtester = new RegTester();

    @ParameterizedTest
    @ValueSource(strings = {"aAaa.Bbb.ccC", "a1A.b2Bb.cC3", "a_1.b2.ccc", "abc"})
    void regexReconizeOkPackageName(String arg) {
        boolean valid = regtester.validePackageName(arg);
        assertThat(valid).isTrue();
    }

    // Test of ReDoS, s. https://www.owasp.org/index.php/Regular_expression_Denial_of_Service_-_ReDoS
    // this tesh should not take too much time, about < 6 ms
    @Test
    void regexRecognizeCorrectPackage5() {
        StringBuilder b = new StringBuilder(MCAbstractAnnotationProcessor.MAX_PACKAGE_NAME_LENGTH);
        while (b.length() < MCAbstractAnnotationProcessor.MAX_PACKAGE_NAME_LENGTH) {
            b.append("a");
        }
        boolean valid = regtester.validePackageName(b.toString());
        assertThat(valid).isTrue();
    }

    @Test
    void regexRecognizeCorrectPackage6() {
        StringBuilder b = new StringBuilder(MCAbstractAnnotationProcessor.MAX_PACKAGE_NAME_LENGTH);
        while (b.length() < MCAbstractAnnotationProcessor.MAX_PACKAGE_NAME_LENGTH) {
            b.append("a");
        }
        b.append("b");
        boolean valid = regtester.validePackageName(b.toString());
        assertThat(valid).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1a.b2.ccc", "$a.b2.ccc", "a.!b2.ccc",
        "a!b.b2.ccc", "aab.2b.ccc", "aab.2b.ccc"})
    void regexNotRecognizeIncorrectPackage(String arg) {
        boolean valid = regtester.validePackageName(arg);
        assertThat(valid).isFalse();
    }
}
