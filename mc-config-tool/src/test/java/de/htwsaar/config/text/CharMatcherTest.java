package de.htwsaar.config.text;

import de.htwsaar.config.text.AbstractStringMatcher.CharMatcher;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
 
/**
 *
 * @author hbui
 */
class CharMatcherTest {

    //Tests for CharMatcher
    @Test
    void testMatchArrayOfChar() {
        final char test = '$';
        final CharMatcher m = new CharMatcher(test);        
        final String bufferStr = "test @ ${xxx}";
        // test for array of chars
        final char buffer[] = bufferStr.toCharArray();        
        int matchLength = m.isMatch(buffer, 7, -1, -1);
        assertThat(matchLength).isEqualTo(1);
        
    }
    
    @Test
    void testMatchString() {
        final char test = '$';
        final CharMatcher m = new CharMatcher(test);        
        final String bufferStr = "test @ ${xxx}";        
        // test for CharSequence
        int matchSeqLength = m.isMatch(bufferStr, 7, -1, -1);
        assertThat(matchSeqLength).isEqualTo(1);
    }
    
    @Test
    void testToString() {        
        final CharMatcher m = new CharMatcher('$');
        final String s = m.toString();
        assertThat(s).contains("['$']");
    }
    
    @Test
    void testSize() {        
        final CharMatcher m = new CharMatcher('$');        
        assertThat(m.size()).isEqualTo(1);
    }
    
}




