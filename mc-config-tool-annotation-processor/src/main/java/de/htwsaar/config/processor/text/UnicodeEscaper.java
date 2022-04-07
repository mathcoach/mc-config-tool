package de.htwsaar.config.processor.text;

import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author hbui
 */
final class UnicodeEscaper extends CharSequenceTranslator{
    
    /** int value representing the lowest codepoint boundary. */
    private final int below;
    /** int value representing the highest codepoint boundary. */
    private final int above;
    /** whether to escape between the boundaries or outside them. */
    private final boolean between;
    
    /**
     * <p>Constructs a {@code UnicodeEscaper} for the specified range. This is
     * the underlying method for the other constructors/builders. The {@code below}
     * and {@code above} boundaries are inclusive when {@code between} is
     * {@code true} and exclusive when it is {@code false}.</p>
     *
     * @param below int value representing the lowest codepoint boundary
     * @param above int value representing the highest codepoint boundary
     * @param between whether to escape between the boundaries or outside them
     */
    private UnicodeEscaper(final int below, final int above, final boolean between) {
        this.below = below;
        this.above = above;
        this.between = between;
    }
    
    
    static UnicodeEscaper outsideOf(int below, int above) {
        return new UnicodeEscaper(below, above, false);
    }
    
    /**
     * {@inheritDoc}
     */    
    @Override
    public final int translate(final CharSequence input, final int index, final Writer out) throws IOException {
        final int codepoint = Character.codePointAt(input, index);
        final boolean consumed = translate(codepoint, out);
        return consumed ? 1 : 0;
    }
    
    private boolean translate(final int codepoint, final Writer out) throws IOException {
        if (between) {
            if (codepoint < below || codepoint > above) {
                return false;
            }
        } else {
            if (codepoint >= below && codepoint <= above) {
                return false;
            }
        }

        if (codepoint > 0xffff) {
            out.write(toUtf16Escape(codepoint));
        } else {
          out.write("\\u");
          out.write(HEX_DIGITS[(codepoint >> 12) & 15]);
          out.write(HEX_DIGITS[(codepoint >> 8) & 15]);
          out.write(HEX_DIGITS[(codepoint >> 4) & 15]);
          out.write(HEX_DIGITS[(codepoint) & 15]);
        }
        return true;
    }
    
    /**
     * Converts the given codepoint to a hex string of the form {@code "\\uXXXX"}.
     *
     * @param codepoint
     *            a Unicode code point
     * @return The hex string for the given codepoint
     *
     */
    private String toUtf16Escape(final int codepoint) {
        final char[] surrogatePair = Character.toChars(codepoint);
        return "\\u" + hex(surrogatePair[0]) + "\\u" + hex(surrogatePair[1]);
    }
}
