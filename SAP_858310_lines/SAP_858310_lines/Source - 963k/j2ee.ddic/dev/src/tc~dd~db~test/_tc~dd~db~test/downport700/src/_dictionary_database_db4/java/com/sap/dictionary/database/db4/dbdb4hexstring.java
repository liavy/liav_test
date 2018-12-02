package com.sap.dictionary.database.db4;

import java.util.Hashtable;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * This class represents a Java string object and its hexadecimal string 
 * representation (UTF-2).
 * <p>
 * As a conversion tool it can either be used to generate the hexadecimal 
 * representation from an arbitrary Java string object, or to re-engineer the 
 * original Java string from its hex string representation.
 * <p>
 * <b>Example:</b> 
 * <p>
 * <code>System.out.println(DbDb4HexString("ABC\uffff", null).getHexString());</code>
 * <p>
 * results in "<code>004100420043FFFF</code>" whereas 
 * <p>
 * <code>System.out.println(DbDb4HexString(null, "004100420043").getJavaString());</code>
 * <p>
 * returns "<code>ABC</code>". In this case, the input string must not contain other
 * characters than such in [A-F][a-f][0-9]. At least one input parameter must be 
 * <code>null</code>.
 * 
 * Copyright:    Copyright (c) 2003
 * Company:      SAP AG
 * @author       Dorothea Rink
 */


public class DbDb4HexString {

    private static final Location loc = Logger.getLocation("db4.DbDb4HexString");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,
                                                             Logger.CATEGORY_NAME);

    private static final Hashtable convTab = new Hashtable(32); 
    static {
        convTab.put(new Character('0'), new Integer(0));
        convTab.put(new Character('1'), new Integer(1));
        convTab.put(new Character('2'), new Integer(2));
        convTab.put(new Character('3'), new Integer(3));
        convTab.put(new Character('4'), new Integer(4));
        convTab.put(new Character('5'), new Integer(5));
        convTab.put(new Character('6'), new Integer(6));
        convTab.put(new Character('7'), new Integer(7));
        convTab.put(new Character('8'), new Integer(8));
        convTab.put(new Character('9'), new Integer(9));
        convTab.put(new Character('A'), new Integer(10));
        convTab.put(new Character('a'), new Integer(10));
        convTab.put(new Character('B'), new Integer(11));
        convTab.put(new Character('b'), new Integer(11));
        convTab.put(new Character('C'), new Integer(12));
        convTab.put(new Character('c'), new Integer(12));
        convTab.put(new Character('D'), new Integer(13));
        convTab.put(new Character('d'), new Integer(13));
        convTab.put(new Character('E'), new Integer(14));
        convTab.put(new Character('e'), new Integer(14));
        convTab.put(new Character('F'), new Integer(15));
        convTab.put(new Character('f'), new Integer(15));
    }


    //--------------
    //  attributes  ----------------------------------
    //--------------

    /**
     * A Java String object.
     */
    private String javaString;
    
    /**
     * The hexadecimal string representation of <code>javaString</code>.
     */
    private String hexString;
    

    //----------------
    //  constructors  ----------------------------------
    //----------------
    
    /**
     * Creates the hex string representation to any given <code>String</code> 
     * object, or vice versa.
     * <p>
     * @param <code>javaString</code> 
     *             - any <code>String</code> object.
     * @param <code>hexString</code> 
     *             - any <code>String</code> object that conforms to the rules of an
     *               UTF-2 string's hex representation.
     * @throws Exception if hexString isn't a valid hex representation of a UTF-2 string,
     * or if more than one input parameter is not <code>null</code>.
     */
    public DbDb4HexString(String javaString, String hexString) throws JddException {
        if ((javaString != null) && (hexString != null)) {
            throw new JddException(ExType.OTHER, "DbDb4HexString: " +
                        "At least one parameter must be null. (" +
                        javaString + ", " + hexString + ")");
        }
        if (javaString != null) {
            this.javaString = javaString;
            this.hexString = convertToHex(javaString);
        } else {
            this.javaString = convertToString(hexString);
            this.hexString = hexString;
        }
    }

    //------------------
    //  public methods  ----------------------------------
    //------------------

    public String getJavaString () {
        return javaString;
    }
    
    public String getHexString () {
        return hexString;
    }

    
    //-------------------
    //  private methods  ----------------------------------
    //-------------------

    private static String convertToHex(String javaString) {
        // DR: (perf: ~ 1 mio chars per sec)
        final int[] base16Vals = {1, 16, 256, 4096};
        final char[] hexChars = {'0','1','2','3','4', '5','6','7','8','9',
                                    'A','B','C','D','E','F'};
        char[] hexCharArray = new char[4*javaString.length()];
        int codepoint = 0;
        int digit = 0;
        if (javaString != null) {
            int pos = 0;
            for (int i=0; i<javaString.length(); i++) {
                codepoint = (int) javaString.charAt(i);
                
                // Do it the safe way to avoid Endian problems...
                for (int j=3; j>=0; j--) {
                    digit = codepoint / base16Vals[j];
                    hexCharArray[pos] = hexChars[digit];
                    codepoint %= base16Vals[j];
                    pos ++;
                }
            }
        }
        return new String(hexCharArray);
    }

    private static String convertToString(String hexString) {
        // DR: (Hashtable experiment. Perf: ~ 400,000 unicode chars per sec 
        // crazy slow like expected ...) To be replaced by low level implementation.
        char[] charArray = new char[hexString.length()/4];
        final int[] base16Vals = {1, 16, 256, 4096};
        String unicodeString = null;
        int pos = 0;
        int codepoint = 0;
        if (hexString != null) {
            for (int i=0; i<hexString.length()/4; i++) {
                codepoint = 0;
                for (int j=3; j>=0; j--) {
                    codepoint += ((Integer) convTab.get(new Character(hexString.charAt(pos)))).intValue() 
                                    * base16Vals[j]; 
                    pos++;
                }
                charArray[i] = ((char) codepoint);
            }
        }
        return new String(charArray);
    }

}