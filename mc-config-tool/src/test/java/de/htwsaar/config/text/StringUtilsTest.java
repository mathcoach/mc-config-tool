package de.htwsaar.config.text;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author hbui
 */
public class StringUtilsTest {
    
    @Test
    void testLengthOfAregularString() {
        int l = StringUtils.length("0123456789");
        assertThat(l).isEqualTo(10);
    }
    
    @Test
    void testLengthOfEmptyString() {
        int l = StringUtils.length("");
        assertThat(l).isEqualTo(0);
    }
    
    @Test
    void testLengthOfNullString() {
        int l = StringUtils.length(null);
        assertThat(l).isEqualTo(0);
    }
    
    
    @Test
    void testCheckEmptyOfNull() {
        boolean empty = StringUtils.isEmpty(null);
        assertThat(empty).isTrue();
    }
    
    @Test
    void testCheckEmptyOfZeroLengthString() {
        boolean empty = StringUtils.isEmpty("");
        assertThat(empty).isTrue();
    }
    
    
    @Test
    void testCheckEmptyOfSpaceOnyString() {
        boolean empty = StringUtils.isEmpty(" ");
        assertThat(empty).isFalse();
    }
    
    @Test
    void testCheckEmptyOfSpaceAroundingeOnyString() {
        boolean empty = StringUtils.isEmpty(" someting ");
        assertThat(empty).isFalse();
    }
    
    @Test
    void testCheckEmptyOfRegularString() {
        boolean empty = StringUtils.isEmpty("someting");
        assertThat(empty).isFalse();
    }
    
    @Test
    void testSubstr_regular() {
        String origin = "0123456789_10";        
        StringBuilder dummy = new StringBuilder(origin);
        String sub = StringUtils.substr(dummy, 3, 5);
        assertThat(sub).isEqualTo("34567");
    }
    
    @Test
    void testSubstr_negativIndex() {
        String origin = "0123456789_10";        
        StringBuilder dummy = new StringBuilder(origin);        
        String sub = StringUtils.substr(dummy, -3, 5);
        assertThat(sub).isEqualTo("01234");
    }
    
    @Test
    void testSubstr_IndexGreaterThanSize() {
        String origin = "0123456789_10";
        StringBuilder dummy = new StringBuilder(origin);        
        String sub = StringUtils.substr(dummy, 13, 5);
        assertThat(sub).isEqualTo("");
    }
    
    @Test
    void testSubstr_ZeroLength() {
        String origin = "0123456789_10";
        StringBuilder dummy = new StringBuilder(origin);        
        String sub = StringUtils.substr(dummy, 3, 0);
        assertThat(sub).isEqualTo("");
    }
    
    @Test
    void testSubstr_NegativeLength() {
        String origin = "0123456789_10";
        StringBuilder dummy = new StringBuilder(origin);        
        String sub = StringUtils.substr(dummy, 3, -1);
        assertThat(sub).isEqualTo("");
    }
    
    @Test
    void testSubstr_size_less_thanIndexPlusLength() {
        String origin = "0123456789_10";
        StringBuilder dummy = new StringBuilder(origin);        
        String sub = StringUtils.substr(dummy, 3, 20);
        assertThat(sub).isEqualTo("3456789_10");
    }
 
    @Test
    void testToCharArray() {
        String origin = "0123456789";
        char[] array = StringUtils.toCharArray(origin);
        assertThat(array).containsExactly('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
    }
    
    @Test
    void testToCharArray_ofEmpty() {
        String origin = "";
        char[] array = StringUtils.toCharArray(origin);
        assertThat(array).isEmpty();
    }
    
    @Test
    void testToArray_ofNoneStringArgument() {
        StringBuilder origin = new StringBuilder();
        origin.append("0123456789");
        char[] array = StringUtils.toCharArray(origin);
        assertThat(array).containsExactly('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
    }
}
