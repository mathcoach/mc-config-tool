package de.htwsaar.config.text;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hbui
 */
class StringMatcherFactoryTest {


    @Test
    void test_NoneMatcher() {
        char[] nullChar = null;
        StringMatcher f = StringMatcherFactory.INSTANCE.stringMatcher(nullChar);
        assertThat(f).isInstanceOf(AbstractStringMatcher.NoneMatcher.class);
    }

    @Test
    void test_CharMatcher() {
        char[] nullChar = {';'};
        StringMatcher f = StringMatcherFactory.INSTANCE.stringMatcher(nullChar);
        assertThat(f).isInstanceOf(AbstractStringMatcher.CharArrayMatcher.class);
    }

    @Test
    void test_MultiCharMatcher() {
        char[] nullChar = {';', '.'};
        StringMatcher f = StringMatcherFactory.INSTANCE.stringMatcher(nullChar);
        assertThat(f).isInstanceOf(AbstractStringMatcher.CharArrayMatcher.class);
    }

    @Test
    void test_EmptyStringMatcher() {
        String empty = "";
        StringMatcher f = StringMatcherFactory.INSTANCE.stringMatcher(empty);
        assertThat(f).isInstanceOf(AbstractStringMatcher.NoneMatcher.class);
    }

    @Test
    void test_StringMatcher() {
        String empty = "{}";
        StringMatcher f = StringMatcherFactory.INSTANCE.stringMatcher(empty);
        assertThat(f).isInstanceOf(AbstractStringMatcher.CharArrayMatcher.class);
    }
}
