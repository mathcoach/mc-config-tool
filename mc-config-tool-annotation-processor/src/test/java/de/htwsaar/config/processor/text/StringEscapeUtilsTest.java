package de.htwsaar.config.processor.text;

import java.io.IOException;
import java.io.StringWriter;
import java.security.InvalidParameterException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hbui
 */
class StringEscapeUtilsTest {

    @Test
    void testLookupTranslator_ThrowExceptionWhenLookupMapNull() {
        Map<CharSequence, CharSequence> lookupMap = null;
        try {
            new LookupTranslator(lookupMap);
            failBecauseExceptionWasNotThrown(InvalidParameterException.class);
        }catch(InvalidParameterException ex) {
            assertThat(ex).hasMessage("lookupMap cannot be null");
        }
    }

    @Test
    void testLookupTranslator_translateUsingAMap() throws IOException {
        Map<CharSequence,CharSequence> upperLower = Map.of(
                "A", "a",
                "Ö", "ö"
        );
        String input = "BaAÄ";
        StringWriter w = new StringWriter();
        LookupTranslator t = new LookupTranslator(upperLower);
        int i = t.translate(input, 0, w);
        assertThat(i).isZero();
        assertThat(w.toString()).isEmpty();
    }

    @Test
    void testLookupTranslator_translateUsingAMap_2() throws IOException {
        Map<CharSequence,CharSequence> upperLower = Map.of(
                "A", "a",
                "Ö", "ö"
        );
        String input = "AaÄ";
        StringWriter w = new StringWriter();
        LookupTranslator t = new LookupTranslator(upperLower);
        int i = t.translate(input, 0, w);
        assertThat(i).isEqualTo(1);
        assertThat(w).hasToString("a");
    }

}