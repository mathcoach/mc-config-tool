package de.htwsaar.config.processor.text;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hbui
 */
class UnicodeEscaperTest {
   
    final int MIN_ASCII = 48; // codepoint of 0
    final int MAX_ASCII = 122; // codepoint of z

    @Test
    void testTranslationInsideASCII() throws IOException {               
        OutsideRangeUnicodeEscaper e  = OutsideRangeUnicodeEscaper.outsideOf(MIN_ASCII, MAX_ASCII);
        
        String asciiChars = "0123456789" + "ABCDFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz";
        StringWriter out = new StringWriter(  );
        int translateCount = 0;
        for(int i = 0; i < asciiChars.length(); ++i) {
            translateCount += e.translate(asciiChars, i, out);
        }
        assertThat(translateCount).isZero();
        String outStr = out.toString();
        assertThat(outStr).isEmpty();
    }
    
    @Test
    void testTranslateOutsideASCII() throws IOException {
        OutsideRangeUnicodeEscaper e  = OutsideRangeUnicodeEscaper.outsideOf(MIN_ASCII, MAX_ASCII);
        String umlauter = "+-äÄöÖüÜß";
        StringWriter out = new StringWriter();
        int translateCount = 0;
        for(int i = 0; i < umlauter.length(); ++i) {
            translateCount += e.translate(umlauter, i, out);
        }
        assertThat(translateCount).isEqualTo(umlauter.length());
        assertThat(out.toString()).isEqualTo("\\u002B\\u002D\\u00E4\\u00C4\\u00F6\\u00D6\\u00FC\\u00DC\\u00DF");
    }
    
    @Test
    void testTranslateUTF16() throws IOException {
        OutsideRangeUnicodeEscaper e  = OutsideRangeUnicodeEscaper.outsideOf(MIN_ASCII, MAX_ASCII);
        String umlauter = "𐀀"; // \u10000
        StringWriter out = new StringWriter();
        int translateCount = 0;
        for(int i = 0; i < umlauter.length(); ++i) {
            int offset = e.translate(umlauter, i, out);
            translateCount += offset;
            i += offset;
        }
        assertThat(translateCount).isEqualTo(1);  // only 1 character with 2 bytes
        assertThat(out.toString()).isEqualTo("\\uD800\\uDC00");
    }
}