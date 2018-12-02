/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.sca.plugins.ws.tools.sdo.das;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Converts aribitrary strings into Java identifiers.
 */
public class NameConverter
{
    public static final NameConverter CONVERTER = new NameConverter();
    
    static private enum CharType {
        UPPER_LETTER {
            boolean isLetter() {
                return true;
            }
        },
        LOWER_LETTER {
            boolean isLetter() {
                return true;
            }
        },
        OTHER_LETTER {
            boolean isLetter() {
                return true;
            }
        },
        DIGIT {
            boolean isLetter() {
                return false;
            }
        },
        OTHER{
            boolean isLetter() {
                return false;
            }
        };
        abstract boolean isLetter();
    };

    // action constants. see nextBreak for the meaning
    static private enum Action {CHECK_PUNCT, CHECK_C2, BREAK, NOBREAK};
    
    /**
     * Look up table for actions.
     * type0*5+type1 would yield the action to be taken.
     */
    private static final Action[] ACTION_TABLE = new Action[5*5];

    static {
        // initialize the action table
        for (CharType t0 : CharType.values()) {
            for (CharType t1 : CharType.values()) {
                ACTION_TABLE[t0.ordinal()*5+t1.ordinal()] = decideAction(t0,t1);
            }
        }
    }

    /**
     * Decide the action to be taken given
     * the classification of the preceding character 't0' and
     * the classification of the next character 't1'.
     */
    private static Action decideAction( CharType t0, CharType t1 ) {
        if(t0==CharType.OTHER && t1==CharType.OTHER) {
            return Action.CHECK_PUNCT;
        }
        if(!xor(t0==CharType.DIGIT,t1==CharType.DIGIT)) {
            return Action.BREAK;
        }
        if(t0==CharType.LOWER_LETTER && t1!=CharType.LOWER_LETTER) {
            return Action.BREAK;
        }
        if(!xor(t0.isLetter(),t1.isLetter())) {
            return Action.BREAK;
        }
        if(!xor(t0==CharType.OTHER_LETTER,t1==CharType.OTHER_LETTER)) {
            return Action.BREAK;
        }
        if(t0==CharType.UPPER_LETTER && t1==CharType.UPPER_LETTER) {
            return Action.CHECK_C2;
        }

        return Action.NOBREAK;
    }

    private static boolean xor(boolean x,boolean y) {
        return (x&&y) || (!x&&!y);
    }

    /**
     * Classify a character into 5 categories that determine the word break.
     */
    private static CharType classify(char c0) {
        switch(Character.getType(c0)) {
        case Character.UPPERCASE_LETTER:        return CharType.UPPER_LETTER;
        case Character.LOWERCASE_LETTER:        return CharType.LOWER_LETTER;
        case Character.TITLECASE_LETTER:
        case Character.MODIFIER_LETTER:
        case Character.OTHER_LETTER:            return CharType.OTHER_LETTER;
        case Character.DECIMAL_DIGIT_NUMBER:    return CharType.DIGIT;
        default:                                return CharType.OTHER;
        }
    }

    private boolean isPunct(char c) {
        return c == '-' || c == '.' || c == ':' || c == '_' || c == '\u00b7' || c == '\u0387' || c == '\u06dd' || c == '\u06de';
    }

    private boolean isLower(char c) {
        return c >= 'a' && c <= 'z' || Character.isLowerCase(c);
    }

    /**
     * Capitalizes the first character of the specified string,
     * and de-capitalize the rest of characters.
     */
    private String capitalize(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        sb.append(Character.toUpperCase(s.charAt(0)));
        sb.append(s.substring(1).toLowerCase(Locale.ENGLISH));
        return sb.toString();
    }

    // Precondition: s[start] is not punctuation
    private int nextBreak(String s, int start) {
        int n = s.length();

        char c1 = s.charAt(start);
        CharType t1 = classify(c1);

        for (int i=start+1; i<n; i++) {
            // shift (c1,t1) into (c0,t0)
            // char c0 = c1;  --- conceptually, but c0 won't be used
            CharType t0 = t1;

            c1 = s.charAt(i);
            t1 = classify(c1);

            switch(ACTION_TABLE[t0.ordinal()*5+t1.ordinal()]) {
                case CHECK_PUNCT:
                    if(isPunct(c1)) {
                        return i;
                    }
                    break;
                case CHECK_C2:
                    if (i < n-1) {
                        char c2 = s.charAt(i+1);
                        if (isLower(c2)) {
                            return i;
                        }
                    }
                    break;
                case BREAK:
                    return i;
                default:
                    break;
            }
        }
        return -1;
    }

    /**
     * Tokenizes a string into words and capitalizes the first
     * character of each word.
     *
     * <p>
     * This method uses a change in character type as a splitter
     * of two words. For example, "abc100ghi" will be splitted into
     * {"Abc", "100","Ghi"}.
     */
    private List<String> toWordList(String s, boolean capitalize) {
        ArrayList<String> ss = new ArrayList<String>();
        int n = s.length();
        for (int i = 0; i < n;) {

            // Skip punctuation
            while (i < n) {
                if (!isPunct(s.charAt(i))) {
                    break;
                }
                i++;
            }
            if (i >= n) {
                break;
            }

            // Find next break and collect word
            int b = nextBreak(s, i);
            String w = (b == -1) ? s.substring(i).trim() : s.substring(i, b).trim();
            if (w.length() > 0) {
                ss.add(escape(capitalize ? capitalize(w) : w));
            }
            if (b == -1) {
                break;
            }
            i = b;
        }

        return ss;
    }

    private String toMixedCaseName(String s, boolean startUpper, boolean capitalize) {
        List<String> ss = toWordList(s.trim(), capitalize);
        StringBuilder sb = new StringBuilder();
        if(!ss.isEmpty()) {
            sb.append(startUpper ? ss.get(0) : ss.get(0).toLowerCase(Locale.ENGLISH));
            for (int i = 1; i < ss.size(); i++) {
                sb.append(ss.get(i));
            }
        }
        return sb.toString();
    }

    /**
     * Normalize internal representation of arrays.
     * Remove full qualification from class names where possible.
     * 
     * @param typeName
     * @return
     */
    public String normalizeClassname(String typeName) {
        // special handling for arrays
        if (typeName.charAt(0)=='[') {
            int idx = typeName.indexOf('L');
            if (idx > 0) {
                StringBuffer buf = new StringBuffer();
                // append classname without closing ';'
                buf.append(typeName.substring(idx + 1, typeName.length() - 1));
                for (int i = 0; i < idx; ++i) {
                    buf.append("[]");
                }
                typeName = buf.toString();
            } else if (typeName.charAt(1)=='B') {
                typeName = "byte[]";
            }
        }
        if (typeName.startsWith("java.lang.")) {
            typeName = typeName.substring(10);
        }
        return typeName;
    }

    /**
     * Escapes characters is the given string so that they can be
     * printed by only using US-ASCII characters.
     *
     * The escaped characters will be appended to the given
     * StringBuffer.
     *
     * @param sb
     *      StringBuffer that receives escaped string.
     * @param s
     *      String to be escaped. <code>s.substring(start)</code>
     *      will be escaped and copied to the string buffer.
     */
    private void escape(StringBuilder sb, String s, int start) {
        int n = s.length();
        for (int i = start; i < n; i++) {
            char c = s.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
    }

    /**
     * Escapes characters that are unusable as Java identifiers
     * by replacing unsafe characters with safe characters.
     */
    private String escape(String s) {
        int n = s.length();
        for (int i = 0; i < n; i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                StringBuilder sb = new StringBuilder(s.substring(0, i));
                escape(sb, s, i);
                return sb.toString();
            }
        }
        return s;
    }

    /**
     * converts a string into an identifier suitable for classes.
     *
     * In general, this operation should generate "NamesLikeThis".
     */
    public String toClassName(String s) {
        String className = toMixedCaseName(s, true, true);
        if (!Character.isLetter(className.charAt(0))) {
            className = 'I' + className;
        }
        return className;
    }

    /**
     * converts a string into an identifier suitable for properties.
     *
     * In general, this operation should generate "NamesLikeThis",
     * which will be used with known prefixes like "get" or "set".
     */
    public String toPropertyName(String s) {
        String prop = toMixedCaseName(s, true, true);
        // property name "Class" with collide with Object.getClass,
        // so escape this.
        if (prop.equals("Class")) {
            prop = "Clazz";
        }
        return prop;
    }

    /**
     * Converts a string into an identifier suitable for variables.
     *
     * In general it should generate "namesLikeThis".
     */
    public String toVariableName(String s) {
        String variableName = toMixedCaseName(s, false, true);
        if (Character.isDigit(variableName.charAt(0))) {
            variableName = '_' + variableName;
        }
        return variableName;
    }

    /**
     * Converts a string into a package name.
     * This method should expect input like "org", "ACME", or "Foo"
     * and return something like "org", "acme", or "foo" respectively
     * (assuming that it follows the standard Java convention.)
     */
    public String toPackageName( String s ) {
        String packageName = toMixedCaseName(s, false, false);
        if (!Character.isLetter(packageName.charAt(0))) {
            packageName = 'p' + packageName;
        }
        return packageName;
    }
}
