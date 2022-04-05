package de.htwsaar.config.text;

/**
 *
 * @author hbui
 */
class StringUtils {
    
    
    /**
     * Gets a CharSequence length or {@code 0} if the CharSequence is
     * {@code null}.
     *
     * @param cs
     *            a CharSequence or {@code null}
     * @return CharSequence length or {@code 0} if the CharSequence is
     *         {@code null}.
     *
     */
    static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }
    
    /**
     * <p>Checks if a CharSequence is empty ("") or null.</p>
     *
     * <pre>
 StringUtils.isEmpty(null)      = true
 StringUtils.isEmpty("")        = true
 StringUtils.isEmpty(" ")       = false
 StringUtils.isEmpty("bob")     = false
 StringUtils.isEmpty("  bob  ") = false
 </pre>
     *
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is empty or null
     * @since 3.0 Changed signature from isEmpty(String) to isEmpty(CharSequence)
     */
    static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }
    
    /**
     * (Origin from TextStringBuilder)
     * internal method to extract some characters from the middle of the string builder without throwing an exception.
     * <p>
     * This method extracts {@code length} characters from the builder at the specified index. If the index is negative
     * it is treated as zero. If the index is greater than the builder size, it is treated as the builder size. If the
     * length is negative, the empty string is returned. If insufficient characters are available in the builder, as
     * much as possible is returned. Thus the returned string may be shorter than the length requested.
     * </p>
     *
     * @param origin the origin StringBuilder
     * @param index the index to start at, negative means zero
     * @param length the number of characters to extract, negative returns empty string
     * @return The new string
     */
    static String substr(StringBuilder origin, int index, int length) {
        final int size = origin.length();
        if(index < 0) {
            index = 0;
        }
        if(length <= 0 || index >= size ) {
            return "";
        }
        int lastIndex = index + length ;
        if(size <= lastIndex) {
            lastIndex = size;
        }
        return origin.substring(index, lastIndex);        
    }
    
    
    static final char[] EMPTY_CHAR_ARRAY = new char[0];
    static char[] toCharArray(final CharSequence source) {
        final int len = length(source);
        if (len == 0) {
            return EMPTY_CHAR_ARRAY;
        }
        if (source instanceof String) {
            return ((String) source).toCharArray();
        }
        final char[] array = new char[len];
        for (int i = 0; i < len; i++) {
            array[i] = source.charAt(i);
        }
        return array;
    }
}
