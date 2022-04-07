package de.htwsaar.config.text;

import java.lang.reflect.Array;

/**
 *
 * @author hbui
 */
class StringMatcherFactory {
    /**
     * Defines the singleton for this class.
     */
    static final StringMatcherFactory INSTANCE = new StringMatcherFactory();
    
    /**
     * prevent to build an instance of this class outside this class.
     */
    private StringMatcherFactory() {
        // empty
    }
    
    /**
     * Matches no characters.
     */
    private static final AbstractStringMatcher.NoneMatcher NONE_MATCHER = new AbstractStringMatcher.NoneMatcher();
    
    
    
    
    
    /**
     * Creates a matcher from a string.
     *
     * @param chars the string to match, null or empty matches nothing
     * @return a new Matcher for the given String
     * @since 1.9
     */
    StringMatcher stringMatcher(final char... chars) {
        final int length = getArrayLength(chars);
        if(length == 0) return NONE_MATCHER;
        if(length == 1) return new AbstractStringMatcher.CharArrayMatcher(chars[0]);
        return new AbstractStringMatcher.CharArrayMatcher(chars);
    }
    
    /**
     * Creates a matcher from a string.
     *
     * @param str the string to match, null or empty matches nothing
     * @return a new Matcher for the given String
     */
    StringMatcher stringMatcher(final String str) {
        return StringUtils.isEmpty(str) ? NONE_MATCHER : stringMatcher(str.toCharArray());
    }
 
    
    
    private static int getArrayLength(final Object array) {
        if (array == null) {
            return 0;
        }
        return Array.getLength(array);
    }
}
