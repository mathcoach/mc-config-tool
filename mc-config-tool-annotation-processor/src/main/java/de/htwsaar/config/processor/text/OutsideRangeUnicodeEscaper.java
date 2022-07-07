package de.htwsaar.config.processor.text;

import java.io.IOException;
import java.io.Writer;

/**
 *
 * @author hbui
 */
final class OutsideRangeUnicodeEscaper extends CharSequenceTranslator{
    
    /** int value representing the lowest codepoint boundary. */
    private final int below;
    /** int value representing the highest codepoint boundary. */
    private final int above;

    
    /**
     * <p>Constructs a {@code UnicodeEscaper} for the specified range. This is
     * the underlying method for the other constructors/builders. The {@code below}
     * and {@code above} boundaries are inclusive when {@code between} is
     * {@code true} and exclusive when it is {@code false}.</p>
     *
     * @param below int value representing the lowest codepoint boundary
     * @param above int value representing the highest codepoint boundary
     */
    private OutsideRangeUnicodeEscaper(final int below, final int above) {
        this.below = below;
        this.above = above;
    }
    
    
    static OutsideRangeUnicodeEscaper outsideOf(int below, int above) {
        return new OutsideRangeUnicodeEscaper(below, above);
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
        if (codepoint >= below && codepoint <= above) {
            return false;
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
