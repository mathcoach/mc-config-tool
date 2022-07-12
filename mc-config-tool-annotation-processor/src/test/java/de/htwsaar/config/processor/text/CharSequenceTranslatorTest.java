/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */

package de.htwsaar.config.processor.text;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hbui
 */
class CharSequenceTranslatorTest {
   

    @Test
    public void testDonNotThrowAnythingWhenInputNull() {
        DummyCharSequenceTranslator t = new DummyCharSequenceTranslator();
        try {
            String translated = t.translate(null);
            assertThat(translated).isNull();
        }catch(Exception ex) {
            fail("Not expected exception but got one", ex);
        }
    }
    
    @Test
    public void testDonNotThrowAnythingWhenInputNull_1() {
        DummyCharSequenceTranslator t = new DummyCharSequenceTranslator();
        StringWriter w = new StringWriter();
        try {
            t.translate(null, w);
            assertThat(w).hasToString("");
        }catch(Exception ex) {
            fail("Not expected exception but got one", ex);
        }
    }

}


class DummyCharSequenceTranslator extends CharSequenceTranslator {

    @Override
    int translate(CharSequence input, int index, Writer out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}