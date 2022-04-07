package de.htwsaar.config.processor.text;

import java.io.IOException;
import java.io.Writer;
import java.security.InvalidParameterException;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author hbui
 */
public final class StringEscapeUtils {
    
    
    private StringEscapeUtils() {
        //Nothing!
    }
    
    private static final Map<CharSequence, CharSequence> JAVA_CTRL_CHARS_ESCAPE = Map.of(
        "\b", "\\b",
        "\n", "\\n",
        "\t", "\\t",
        "\f", "\\f",
        "\r", "\\r"
    );
    
    /**
     * Translator object for escaping Java.
     *
     * While {@link #escapeJava(String)} is the expected method of use, this
     * object allows the Java escaping functionality to be used
     * as the foundation for a custom translator.
     */
    private static final CharSequenceTranslator ESCAPE_JAVA;
    static {
        final Map<CharSequence, CharSequence> escapeJavaMap = new HashMap<>();
        escapeJavaMap.put("\"", "\\\"");
        escapeJavaMap.put("\\", "\\\\");
        ESCAPE_JAVA = new AggregateTranslator(
                new LookupTranslator(Collections.unmodifiableMap(escapeJavaMap)),
                new LookupTranslator(JAVA_CTRL_CHARS_ESCAPE),
                //JavaUnicodeEscaper.outsideOf(32, 0x7f)
                UnicodeEscaper.outsideOf(32, 0x7f)
        );
    }
    
    
    public static final String escapeJava(final String input) {
        return ESCAPE_JAVA.translate(input);
    }
        
}


final class LookupTranslator extends CharSequenceTranslator {

    /** The mapping to be used in translation. */
    private final Map<String, String> lookupMap;
    /** The first character of each key in the lookupMap. */
    private final BitSet prefixSet;
    /** The length of the shortest key in the lookupMap. */
    private final int shortest;
    /** The length of the longest key in the lookupMap. */
    private final int longest;

    /**
     * Define the lookup table to be used in translation
     *
     * Note that, as of Lang 3.1 (the origin of this code), the key to the lookup
     * table is converted to a java.lang.String. This is because we need the key
     * to support hashCode and equals(Object), allowing it to be the key for a
     * HashMap. See LANG-882.
     *
     * @param lookupMap Map&lt;CharSequence, CharSequence&gt; table of translator
     *                  mappings
     */
    LookupTranslator(final Map<CharSequence, CharSequence> lookupMap) {
        if (lookupMap == null) {
            throw new InvalidParameterException("lookupMap cannot be null");
        }
        this.lookupMap = new HashMap<>();
        this.prefixSet = new BitSet();
        int currentShortest = Integer.MAX_VALUE;
        int currentLongest = 0;

        for (final Map.Entry<CharSequence, CharSequence> pair : lookupMap.entrySet()) {
            this.lookupMap.put(pair.getKey().toString(), pair.getValue().toString());
            this.prefixSet.set(pair.getKey().charAt(0));
            final int sz = pair.getKey().length();
            if (sz < currentShortest) {
                currentShortest = sz;
            }
            if (sz > currentLongest) {
                currentLongest = sz;
            }
        }
        this.shortest = currentShortest;
        this.longest = currentLongest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    int translate(final CharSequence input, final int index, final Writer out) throws IOException {
        // check if translation exists for the input at position index
        if (prefixSet.get(input.charAt(index))) {
            int max = longest;
            if (index + longest > input.length()) {
                max = input.length() - index;
            }
            // implement greedy algorithm by trying maximum match first
            for (int i = max; i >= shortest; i--) {
                final CharSequence subSeq = input.subSequence(index, index + i);
                final String result = lookupMap.get(subSeq.toString());

                if (result != null) {
                    out.write(result);
                    return i;
                }
            }
        }
        return 0;
    }
}

