package de.htwsaar.config.text;

import de.htwsaar.config.text.AbstractStringMatcher.NoneMatcher;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
 
/**
 *
 * @author hbui
 */
class NoneMatcherTest {

    
    @Test
    void testMatchString() {
        final NoneMatcher m = new NoneMatcher();
        final int matchLength = m.isMatch("test", 0);
        assertThat(matchLength).isEqualTo(0);
    }
    
    
    @Test
    void testMatchArray() {
        final NoneMatcher m = new NoneMatcher();
        final int matchLength = m.isMatch( new char[]{'a'}, 0);
        assertThat(matchLength).isEqualTo(0);
    }
    
    @Test
    void testSize() {
        final NoneMatcher m = new NoneMatcher();
        assertThat(m.size()).isEqualTo(0);
    }
    
}