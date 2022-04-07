package de.htwsaar.config.text;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hbui
 */
class StringMatcherTest {
    
    private class LengthMatcher implements StringMatcher {

        @Override
        public int isMatch(char[] buffer, int start, int bufferStart, int bufferEnd) {
            return buffer == null ? 0 :buffer.length;
        }
        
    }
    
    private LengthMatcher m = new LengthMatcher();
    
    @Test
    void testIsMatch_ArrayOfChar() {
        int l = m.isMatch(new char[] {'0', '1', '2'}, 0);
        assertThat(l).isEqualTo(3);
    }
    
    @Test
    void testIsMatch_CharSequence() {
        int l = m.isMatch("012", 0);
        assertThat(l).isEqualTo(3);
    }
    
    @Test
    void testSize() {
        int l = m.size();
        assertThat(l).isZero();
    }
    
}
