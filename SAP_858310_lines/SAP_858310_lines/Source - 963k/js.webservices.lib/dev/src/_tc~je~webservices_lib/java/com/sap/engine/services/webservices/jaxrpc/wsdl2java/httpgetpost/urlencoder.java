package com.sap.engine.services.webservices.jaxrpc.wsdl2java.httpgetpost;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class URLEncoder {

  private static final char[] RESERVED_UNSAFED  =  {' ', '<', '>', '"', '#', '%', '{', '}', '|', '\\', '^', '~', '[', ']', '\'', ';', '/', '?', ':', '@', '=', '&', '$', '-', '+', ','};
  private static final int RESERVED_LENGTH  =  RESERVED_UNSAFED.length;
  private static String[] CODES;

  static {
    CODES = new String[RESERVED_UNSAFED.length];
    CODES[0] = "+";//code for interval

    for (int i = 1; i < RESERVED_UNSAFED.length; i++) {
      CODES[i] = doEncode(RESERVED_UNSAFED[i]);
    }
  }

  /**
   * Encodes the string according to rfc 1738.
   *
   * @throws  NillPointerException if the string is null
   * @throws  java.lang.IllegalArgumentException if in the string there are
   *          no US-ASCII characters(code > 0xFF).
   */
  public static String encode(String str) {
    StringBuffer enStr = new StringBuffer(str.length());
    char curr;
    int i, j;
    boolean found;

    for (i = 0; i < str.length(); i++) {
      curr = str.charAt(i);
      if (curr > 0xff) {
        throw new IllegalArgumentException("Char at position '" + i + "' is not valud US-ASCII character. Code 0x" + Integer.toHexString((int) curr) + "");
      }

      if ((curr <= 0x1f) || (curr >= 0x7f && curr <= 0xff)) {
        enStr.append(doEncode(curr));
        continue;
      }

      //test if is reserve
      found = false;
      for (j = 0; j < RESERVED_LENGTH; j++) {
        if (curr == RESERVED_UNSAFED[j]) {
          enStr.append(CODES[j]);
          found = true;
          break;
        }
      }
      //normal
      if (! found) {
        enStr.append(curr);
      }
    }

    return enStr.toString();
  }

  private static String doEncode(char c) {
    return "%" + Integer.toHexString(c);
  }

//  public static void main(String[] args) throws Exception {
//    String s = "aa%< \uf07b ";
//    System.out.println("Res '" + URLEncoder.encode(s) + "'");
//  }

}