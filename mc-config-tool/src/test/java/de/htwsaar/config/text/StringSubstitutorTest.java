package de.htwsaar.config.text;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hbui
 */
class StringSubstitutorTest {
    
    @Test
    void testReplaceIn_Regular() {
        final StringBuilder source = new StringBuilder("The ${animal} jumps over the ${target}.");
        final Map<String,String> values = Map.of("animal", "quick brown fox", "target", "lazy dog");
        StringSubstitutor substitutor = new StringSubstitutor(values);
        boolean isReplaced = substitutor.replaceIn(source);
        assertThat(isReplaced).isTrue();
        assertThat(source.toString()).isEqualTo("The quick brown fox jumps over the lazy dog.");
    }
    
    @Test
    void testReplaceIn_WithNullSourceBuffer() {
        final StringBuilder source = null;
        final Map<String,String> values = Map.of(
            "animal", "quick brown fox", 
            "target", "lazy dog"
        );
        StringSubstitutor substitutor = new StringSubstitutor(values);
        boolean isReplaced = substitutor.replaceIn(source);
        assertThat(isReplaced).isFalse();
        assertThat((CharSequence)source).isNull();
    }
    
    @Test 
    void testReplaceIn_NotChangeInNoneVariableSource() {
        final StringBuilder source = new StringBuilder("nothing to change");
        final Map<String,String> values = Map.of("animal", "quick brown fox", "target", "lazy dog");
        StringSubstitutor substitutor = new StringSubstitutor(values);
        boolean isReplaced = substitutor.replaceIn(source);
        assertThat(isReplaced).isFalse();
        assertThat(source.toString()).isEqualTo("nothing to change");
    }
    
    
    @Test
    void testReplace() {
        final String source = "The ${animal} jumps over the ${target}.";
        final Map<String,String> values = Map.of("animal", "quick brown fox", "target", "lazy dog");
        final String result = StringSubstitutor.replace(source, values);
        assertThat(result).isEqualTo("The quick brown fox jumps over the lazy dog.");
    }
    
    @Test
    void testReplace_NullSource() {
        final String source = null;
        final Map<String,String> values = Map.of("animal", "quick brown fox", "target", "lazy dog");
        final String result = StringSubstitutor.replace(source, values);
        assertThat(result).isNull();
    }
    
    @Test
    void testReplace_Cylic() {
        final String source = "a = ${b}; b = ${c},c = ${a}";
        final Map<String,String> values = Map.of(
            "a", "${b}",
            "b", "${c}",
            "c", "${a}"
        );
        try{
            final String result = StringSubstitutor.replace(source, values);
        }catch(IllegalStateException ex) {
            assertThat(ex).hasMessageContaining("b->c->a");
        }
    }
    
    
    @Test
    void testReplace_DefaultValue() {
        final String source = "The ${animal} jumps over the ${target:-lazy dog}.";
        final Map<String,String> values = Map.of("animal", "quick brown fox");
        final String result = StringSubstitutor.replace(source, values);
        assertThat(result).isEqualTo("The quick brown fox jumps over the lazy dog.");
    }
    
    @Test
    void testReplace_nested_variable_is_not_resolved() {
        final String source = "The variable ${${name}} must be used.";
        final Map<String,String> values = Map.of(
            "name", "x",
            "x", "Y"
        );
        final String result = StringSubstitutor.replace(source, values);
        assertThat(result).isEqualTo("The variable ${${name}} must be used.");
    }
    
    
    @Test
    void testReplaceSystemPropertiesRegular() {
        String source = "java.version = ${java.version}";
        String expected = "java.version = " + System.getProperty("java.version");
        String result = StringSubstitutor.replaceSystemProperties(source);
        assertThat(result).isEqualTo(expected);
    }
    
    @Test
    void restReplaceSystemPropertiesCausesExceptionWhenVariableNotFound() {
        String source = "undef.var = ${undefined.var}";
        try {
            StringSubstitutor.replaceSystemProperties(source);
        }catch(IllegalArgumentException ex) {
            assertThat(ex).hasMessageContaining("undefined.var");
        }
    }
    
    
    
}
