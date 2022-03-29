package de.htwsaar.config.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author hbui
 */
public final class StringSubstitutor {
    
    
    private final StringLookup variableResolver;
    
    private static final String DEFAULT_VAR_START = "${";
    private final StringMatcher prefixMatcher = StringMatcherFactory.INSTANCE.stringMatcher(DEFAULT_VAR_START);
    
    private static final String DEFAULT_VAR_END = "}";
    private final StringMatcher suffixMatcher = StringMatcherFactory.INSTANCE.stringMatcher (DEFAULT_VAR_END);
    
    private final char escapeCh = '$';
    
    private static final String DEFAULT_VAR_DEFAULT = ":-";
    private final StringMatcher valueDelimMatcher = StringMatcherFactory.INSTANCE.stringMatcher(DEFAULT_VAR_DEFAULT);
    
    private final boolean substitutionInVariablesEnabled = false;
    
    private final boolean substitutionInValuesDisabled = false;
    
    private final boolean undefinedVariableException = false;
    
    private final boolean preserveEscapes = false;
    
    /**
     * Creates a new instance and initializes it. Uses defaults for variable prefix and suffix and the escaping
     * character.
     * @param valueMap the map with the variables' values, may be null
     */
    public StringSubstitutor(Map<String,String> valueMap) {
        this(StringLookupFactory.INSTANCE.mapStringLookup(valueMap));    
    }
    
    private StringSubstitutor(StringLookup variableResolver) {
        Objects.requireNonNull(variableResolver, "variableResolver must not be null");
        this.variableResolver = variableResolver;
    }
    
    
    /**
     * Replaces all the occurrences of variables within the given source buffer with their matching values from the
     * resolver. The buffer is updated with the result.
     *
     * @param source the buffer to replace in, updated
     * @return true if altered
     */
    public boolean replaceIn(final StringBuilder source) {
        if(source == null) {
            return false;
        }
        final int offset = 0;
        final int length = source.length();
        final /*Text*/StringBuilder buf = new /*Text*/StringBuilder(length).append(source, offset, length);
        if (!substitute(buf, 0, length)) {
            return false;
        }
        source.replace(offset, offset + length, buf.toString());
        return true;        
    }
    
    /**
     * Replaces all the occurrences of variables in the given source object with their matching values from the map.
     * @param source the source text containing the variables to substitute, null returns null
     * @param valueMap the map with the values, may be null
     * @return The result of the replace operation
     * @throws IllegalArgumentException if a variable is not found
     */
    public static String replace(final String source, final Map<String,String> valueMap) {
        return new StringSubstitutor(valueMap).replace(source);
    }
    
    /**
     * Replaces all the occurrences of variables in the given source object with their matching values from the system
     * properties.
     *
     * @param source the source text containing the variables to substitute, null returns null
     * @return The result of the replace operation
     * @throws IllegalArgumentException if a variable is not found
     */
    public static String replaceSystemProperties(String source) {
        return new StringSubstitutor(StringLookupFactory.INSTANCE.systemPropertyStringLookup()).replace(source);        
    }
    
    /**
     * Internal method that substitutes the variables.
     * <p>
     * Most users of this class do not need to call this method. This method will be called automatically by another
     * (public) method.
     * </p>
     * <p>
     * Writers of subclasses can override this method if they need access to the substitution process at the start or
     * end.
     * </p>
     *
     * @param builder the string builder to substitute into, not null
     * @param offset the start offset within the builder, must be valid
     * @param length the length within the builder to be processed, must be valid
     * @return true if altered
     */
    private boolean substitute(final /*Text*/StringBuilder builder, final int offset, final int length) {
        return substitute(builder, offset, length, null).altered;
    }
    
    private Result substitute(final /*Text*/StringBuilder builder, final int offset, final int length, List<String> priorVariables) {
        Objects.requireNonNull(builder, "builder");
        /*final StringMatcher prefixMatcher = getVariablePrefixMatcher();*/
        /*final StringMatcher suffixMatcher = getVariableSuffixMatcher();*/
        /*final char escapeCh = getEscapeChar();*/
        /*final StringMatcher valueDelimMatcher = getValueDelimiterMatcher();*/
        /*final boolean substitutionInVariablesEnabled = isEnableSubstitutionInVariables();*/
        /*final boolean substitutionInValuesDisabled = isDisableSubstitutionInValues();*/
        /*final boolean undefinedVariableException = isEnableUndefinedVariableException();*/
        /*final boolean preserveEscapes = isPreserveEscapes();*/
        
        boolean altered = false;
        int lengthChange = 0;
        int bufEnd = offset + length;
        int pos = offset;
        int escPos = -1;
        outer: while (pos < bufEnd) {
            final int startMatchLen = prefixMatcher.isMatch(builder, pos, offset, bufEnd);
            if (startMatchLen == 0) {
                pos++;
            } else {
                // found variable start marker
                if (pos > offset && builder.charAt(pos - 1) == escapeCh) {
                    // escape detected
                    if (preserveEscapes) {
                        // keep escape
                        pos++;
                        continue;
                    }
                    // mark esc ch for deletion if we find a complete variable
                    escPos = pos - 1;
                }
                // find suffix
                int startPos = pos;
                pos += startMatchLen;
                int endMatchLen = 0;
                int nestedVarCount = 0;
                while (pos < bufEnd) {
                    if (substitutionInVariablesEnabled && prefixMatcher.isMatch(builder, pos, offset, bufEnd) != 0) {
                        // found a nested variable start
                        endMatchLen = prefixMatcher.isMatch(builder, pos, offset, bufEnd);
                        nestedVarCount++;
                        pos += endMatchLen;
                        continue;
                    }

                    endMatchLen = suffixMatcher.isMatch(builder, pos, offset, bufEnd);
                    if (endMatchLen == 0) {
                        pos++;
                    } else {
                        // found variable end marker
                        if (nestedVarCount == 0) {
                            if (escPos >= 0) {
                                // delete escape
                                builder.deleteCharAt(escPos);
                                escPos = -1;
                                lengthChange--;
                                altered = true;
                                bufEnd--;
                                pos = startPos + 1;
                                startPos--;
                                continue outer;
                            }
                            // get var name
                            final int varIndex = startPos + startMatchLen;
                            final int varLength = pos - startPos - startMatchLen;
                            /*String varNameExpr = builder.midString(startPos + startMatchLen,
                                pos - startPos - startMatchLen);*/
                            String varNameExpr = substr(builder, varIndex, length);
                            if (substitutionInVariablesEnabled) {
                                final /*Text*/StringBuilder bufName = new /*Text*/StringBuilder(varNameExpr);
                                substitute(bufName, 0, bufName.length());
                                varNameExpr = bufName.toString();
                            }
                            pos += endMatchLen;
                            final int endPos = pos;

                            String varName = varNameExpr;
                            String varDefaultValue = null;

                            if (valueDelimMatcher != null) {
                                final char[] varNameExprChars = varNameExpr.toCharArray();
                                int valueDelimiterMatchLen = 0;
                                for (int i = 0; i < varNameExprChars.length; i++) {
                                    // if there's any nested variable when nested variable substitution disabled,
                                    // then stop resolving name and default value.
                                    if (!substitutionInVariablesEnabled && prefixMatcher.isMatch(varNameExprChars, i, i,
                                        varNameExprChars.length) != 0) {
                                        break;
                                    }
                                    if (valueDelimMatcher.isMatch(varNameExprChars, i, 0,
                                        varNameExprChars.length) != 0) {
                                        valueDelimiterMatchLen = valueDelimMatcher.isMatch(varNameExprChars, i, 0,
                                            varNameExprChars.length);
                                        varName = varNameExpr.substring(0, i);
                                        varDefaultValue = varNameExpr.substring(i + valueDelimiterMatchLen);
                                        break;
                                    }
                                }
                            }

                            // on the first call initialize priorVariables
                            if (priorVariables == null) {
                                priorVariables = new ArrayList<>();
                                priorVariables.add( /*builder.midString(offset, length)*/ substr(builder, offset, length));
                            }

                            // handle cyclic substitution
                            checkCyclicSubstitution(varName, priorVariables);
                            priorVariables.add(varName);

                            // resolve the variable
                            String varValue = resolveVariable(varName/*, builder, startPos, endPos*/);
                            if (varValue == null) {
                                varValue = varDefaultValue;
                            }
                            if (varValue != null) {
                                final int varLen = varValue.length();
                                builder.replace(startPos, endPos, varValue);
                                altered = true;
                                int change = 0;
                                if (!substitutionInValuesDisabled) { // recursive replace
                                    change = substitute(builder, startPos, varLen, priorVariables).lengthChange;
                                }
                                change = change + varLen - (endPos - startPos);
                                pos += change;
                                bufEnd += change;
                                lengthChange += change;
                            } else if (undefinedVariableException) {
                                throw new IllegalArgumentException(
                                    String.format("Cannot resolve variable '%s' (enableSubstitutionInVariables=%s).",
                                        varName, substitutionInVariablesEnabled));
                            }

                            // remove variable from the cyclic stack
                            priorVariables.remove(priorVariables.size() - 1);
                            break;
                        }
                        nestedVarCount--;
                        pos += endMatchLen;
                    }
                }
            }
        }
        return new Result(altered, lengthChange);
    }
    
    
    private void checkCyclicSubstitution(final String varName, final List<String> priorVariables) {
        if (!priorVariables.contains(varName)) {
            return;
        }
        final StringBuilder buf = new StringBuilder(256);
        buf.append("Infinite loop in property interpolation of ");
        buf.append(priorVariables.remove(0));
        buf.append(": ");
        buf.append(String.join("->", priorVariables));
        throw new IllegalStateException(buf.toString());
    }
    
    
    /**
     * Internal method that resolves the value of a variable.
     * <p>
     * Most users of this class do not need to call this method. This method is called automatically by the substitution
     * process.
     * </p>
     * <p>
     * Writers of subclasses can override this method if they need to alter how each substitution occurs. The method is
     * passed the variable's name and must return the corresponding value. This implementation uses the
     * {@link #getStringLookup()} with the variable's name as the key.
     * </p>
     *
     * @param variableName the name of the variable, not null
     * @param buf the buffer where the substitution is occurring, not null
     * @param startPos the start position of the variable including the prefix, valid
     * @param endPos the end position of the variable including the suffix, valid
     * @return The variable's value or <b>null</b> if the variable is unknown
     */
    private String resolveVariable(final String variableName) {        
        return variableResolver.lookup(variableName);
    }
    
    /**
     * Replaces all the occurrences of variables in the given source object with their matching values from the
     * resolver. The input source object is converted to a string using {@code toString} and is not altered.
     *
     * @param source the source to replace in, null returns null
     * @return The result of the replace operation
     * @throws IllegalArgumentException if a variable is not found and enableUndefinedVariableException is true
     */
    private String replace(final String source) {
        if (source == null) {
            return null;
        }
        final /*Text*/StringBuilder buf = new /*Text*/StringBuilder().append(source);
        substitute(buf, 0, buf.length());
        return buf.toString();
    }
    
    /**
     * replacement for TextStringBuilder.midString(int index, int length).
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
    
    /**
     * The low-level result of a substitution.
     *     
     */
    private static final class Result {

        /**
         * Whether the buffer is altered.
         */
        public final boolean altered;

        /**
         * The length of change.
         */
        public final int lengthChange;

        private Result(final boolean altered, final int lengthChange) {
            super();
            this.altered = altered;
            this.lengthChange = lengthChange;
        }

        @Override
        public String toString() {
            return "Result [altered=" + altered + ", lengthChange=" + lengthChange + "]";
        }
    }
    
    
    
}
