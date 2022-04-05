package de.htwsaar.config.text;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author hbui
 */
class FunctionStringLookupTest {
        

    @Test
    void testLookupAMap() {
        FunctionStringLookup<String> propertiesLookup = FunctionStringLookup.on(
            Map.of("a", "A", "b", "B")
        );
        String value = propertiesLookup.lookup("a");
        assertThat(value).isEqualTo("A");
    }
    
    @Test
    void testLookupALambda() {
        FunctionStringLookup<String> lambdaLookup = FunctionStringLookup.on(
            key -> key + "-Value"
        );
        String value = lambdaLookup.lookup("A");
        assertThat(value).isEqualTo("A-Value");
    }
    
    @Test
    void testLookupWithExceptionThrown() {
        FunctionStringLookup<String> exceptionThrownLookup = FunctionStringLookup.on(
            key -> { 
                if(key == null) throw new NullPointerException("key is null"); 
                return "X"; 
            }
        );
        String value = exceptionThrownLookup.lookup(null);
        assertThat(value).isNull();
    }
    
    @Test
    void testToStringMustNotThrowAnyException() {
        FunctionStringLookup<String> fnLookup = FunctionStringLookup.on(
            key -> key + "X"
        );
        try {
            String representation = fnLookup.toString();
            assertThat(representation).contains("function");
        }catch(Exception ex) {
            fail("Not expected any exception by FunctionStringLookup.toString but got one", ex);
        }
    }
}
