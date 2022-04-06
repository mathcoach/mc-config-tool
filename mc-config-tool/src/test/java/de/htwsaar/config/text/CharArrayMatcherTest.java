package de.htwsaar.config.text;

import de.htwsaar.config.text.AbstractStringMatcher.CharArrayMatcher;
import de.htwsaar.config.text.AbstractStringMatcher.CharMatcher;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
 
/**
 *
 * @author hbui
 */
public class CharArrayMatcherTest {

    //Tests for CharArrayMatcher
    @Test
    void testMatchArrayOfChar() {
        final char test[] = {'$', '{'};
        final CharArrayMatcher m = new CharArrayMatcher(test);
        final String bufferStr = "test @ ${xxx}";
        final char buffer[] = bufferStr.toCharArray();
        
        int matchLength = m.isMatch(buffer, 7);
        assertThat(matchLength).isEqualTo(2);        
        // assert that buffer is not changed
        assertThat(buffer).containsExactly(bufferStr.toCharArray());
        
    }
    
    
    @Test
    void testMatchString() {
        final char test[] = {'$', '{'};
        final CharArrayMatcher m = new CharArrayMatcher(test);
        final String bufferStr = "test @ ${xxx}";
        // test for CharSequence
        int matchSeqLength = m.isMatch(bufferStr, 7);
        assertThat(matchSeqLength).isEqualTo(2);
    }
    
    
    @Test
    void testToString() {
        final char test[] = {'$', '{'};
        final CharArrayMatcher m = new CharArrayMatcher(test);
        final String s = m.toString();
        assertThat(s).contains("[\"${\"]");
    }
    
    @Test
    void testSize() {
        final char test[] = {'$', '{'};
        final CharArrayMatcher m = new CharArrayMatcher(test);        
        assertThat(m.size()).isEqualTo(test.length);
    }
    
    
}