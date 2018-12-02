/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.lib;

import java.util.StringTokenizer;

import com.sap.engine.services.servlets_jsp.lib.eclipse.JspLineMapper;
import com.sap.engine.services.servlets_jsp.server.LogContext;

public class StringUtils {
  public static String escapeJavaCharacters(String s) {
    String s1;
    char c;
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < s.length(); i++) {
      switch (c = s.charAt(i)) {
        case'\b': {
          sb.append("\\b");
          break;
        }
        case'\t': {
          sb.append("\\t");
          break;
        }
        case'\r': {
          sb.append("\\r");
          break;
        }
        case'\n': {
          sb.append("\\n");
          break;
        }
        case'\f': {
          sb.append("\\f");
          break;
        }
        case'\'': {
          sb.append("\\\'");
          break;
        }
        case'\"': {
          sb.append("\\\"");
          break;
        }
        case'\\': {
          sb.append("\\\\");
          break;
        }
        default: {
          if (c > 0xFF) {
            s1 = "00" + Integer.toHexString(c);
            sb.append("\\u").append(s1.substring(s1.length() - 4));
          } else if (c < ' ' || c >= 0x7F) {
            s1 = "00" + Integer.toOctalString(c);
            sb.append('\\').append(s1.substring(s1.length() - 3));
          } else {
            sb.append(c);
          }
          break;
        }
      }
    }
    return sb.toString();
  }

  public static String unquote(byte[] quotedString, int off, int len) {
    int cutQuotes = 0;
    int middle = len / 2;
    for (; cutQuotes < middle; cutQuotes++) {
      if (quotedString[off + cutQuotes] != '\"' || quotedString[off + len - 1 - cutQuotes] != '\"') {
        break;
      }
    }
    return new String(quotedString, off + cutQuotes, len - cutQuotes * 2);
  }

  /**
   * Replaces all occurrences of CR and LF with spaces
   * Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
   * Status-Code = 3DIGIT
   * Reason-Phrase = *<TEXT, excluding CR, LF>
   * HTTP-Version = "HTTP" "/" 1*DIGIT "." 1*DIGIT
   *
   * @param value
   * @return
   */
  public static String replaceCRLF(String value) {
    if (value != null) {
      value = value.replace((char) 10, (char) 32);
      value = value.replace((char) 13, (char) 32);
    }
    return value;
  }

  /**
   * Used for Header names. According HTTP spec. 1.1:
   * Header names are tokens.
   * token = 1*<any CHAR except CTLs or separators>
   * separators = "(" | ")" | "<" | ">" | "@"
   * | "," | ";" | ":" | "\" | <">
   * | "/" | "[" | "]" | "?" | "="
   * | "{" | "}" | SP | HT
   * CHAR = <any US-ASCII character (octets 0 - 127)>
   *
   * @param value
   * @return
   */
  public static byte[] filterHeaderName(byte[] value) {
    if (value == null) {
      return null;
    }

    byte[] result = new byte[value.length];
    int bytesCopied = 0;
    for (int i = 0; i < value.length; i++) {
      if (value[i] <= 32) {
        continue;
      }
      switch (value[i]) {
        case'\r':
        case'\n':
        case' ':
        case'(':
        case')':
        case'<':
        case'>':
        case'@':
        case'/':
        case',':
        case';':
        case':':
        case'\\':
        case'[':
        case']':
        case'?':
        case'=':
        case'{':
        case'}':
        case 127: // DEL
          continue;
        default:
          result[bytesCopied] = value[i];
          bytesCopied++;
      }
    }
    if (value.length != bytesCopied) {
      if (LogContext.getLocationServletResponse().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000248",
					"CR or LF found in header for name[{0}].", new Object[]{new String(value)}, null, null);
			}
    }
    value = new byte[bytesCopied];
    System.arraycopy(result, 0, value, 0, bytesCopied);
    return value;
  }

  /**
   * Used for Header names. According HTTP spec. 1.1:
   * Header names are tokens.
   * token = 1*<any CHAR except CTLs or separators>
   * separators = "(" | ")" | "<" | ">" | "@"
   * | "," | ";" | ":" | "\" | <">
   * | "/" | "[" | "]" | "?" | "="
   * | "{" | "}" | SP | HT
   * CHAR = <any US-ASCII character (octets 0 - 127)>
   *
   * @param value
   * @return
   */
  public static byte[] filterHeaderName(String value) {
    if (value == null) {
      return null;
    }
    return filterHeaderName(value.getBytes());
  }

  /**
   * Used for Header values. Replaces '\r','\n' with space except for LWS.
   *
   * @param value
   * @return
   */
  public static byte[] filterHeaderValue(String value) {
    if (value == null) {
      return null;
    }
    return filterHeaderValue(value.getBytes());
  }

  /**
   * Replaces all occurrences of CR AND LF with spaces.
   * If combination of bytes forms "linear white space" it is not touched.
   * This method should be used only for header values.
   * HTTP 1.1:
   * CRLF = CR LF
   * LWS = [CRLF] 1*( SP | HT )
   */
  public static byte[] filterHeaderValue(byte[] bytes) {
    boolean found = false;
    for (int i = 0; i < bytes.length; i++) {
      byte currByte = bytes[i];
      switch (currByte) {
        case 13: //CR
          if ((i + 2 < bytes.length) &&                       // if we have at least 2 chars more
            (bytes[i + 1] == 10) &&                          //and next char is LF
            (bytes[i + 2] == 32 || bytes[i + 2] == 9)          //and next char is space or tab
            ) {                          // then this is linear white space
            continue;
          } else {
            bytes[i] = 32;              //replace with space
            found = true;
          }
          break;
        case 10: //LF
          if ((i != 0) &&                                     // if we have some characters before
            (i + 1 < bytes.length && i - 1 >= 0) &&          // and at least 1 more
            (bytes[i - 1] == 13) &&                          // and the previous one was CR
            (bytes[i + 1] == 32 || bytes[i + 1] == 9)        // and next is space or tab
            ) {                            // then this is linear white space
            continue;
          } else {
            bytes[i] = 32;              //replace with space
            found = true;
          }
          break;
        default:
          continue;
      }
    }

    if (found) {
      if (LogContext.getLocationServletResponse().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000515",
					"CR or LF found in header for value[{0}].", new Object[]{new String(bytes)}, null, null);
			}
    }
    return bytes;
  }

  /**
   * Returns true if the  string is greater that the double number.
   *
   * @param s1
   * @param s2
   * @return
   */
  public static boolean greaterThan(String s1, double s2) {
    Double jspVersionDouble = Double.valueOf(s1);
    return Double.compare(jspVersionDouble, s2) > 0;
  }

  /**
   * Returns true if the  string is lesser that the double number.
   *
   * @param s1
   * @param s2
   * @return
   */
  public static boolean lessThan(String s1, double s2) {
    Double jspVersionDouble = Double.valueOf(s1);
    return Double.compare(jspVersionDouble, s2) < 0;
  }

  /**
   * Invoked for scriptlets to escape the debug info final comment "// end" if found in this scriptlet
   * and a new line is starting with it.
   *
   * @param origin - The String to be escaped.
   * @return - if found escaped String otherwise the origin String.
   */
  public static String escapeEndComment(String origin) {
    if (origin == null) {
      return origin;
    }
    String result = null;
    int indexOfEnd = origin.indexOf(JspLineMapper.END_OF_COMMENT_TAG);
    if (indexOfEnd >= 0) {
      StringBuilder builder = new StringBuilder(origin.length() + 1);
      StringTokenizer stringTokenizer = new StringTokenizer(origin, "\n", true);
      while (stringTokenizer.hasMoreTokens()) {
        String line = stringTokenizer.nextToken();
        if (line.trim().startsWith(JspLineMapper.END_OF_COMMENT_TAG)) {
          builder.append(line.replaceFirst(JspLineMapper.END_OF_COMMENT_TAG, "/" + JspLineMapper.END_OF_COMMENT_TAG));
        } else {
          builder.append(line);
        }
      }
      result = builder.toString();
    } else {
      result = origin;
    }
      return result;
    }

  /**
   * Determines if the specified string is permissible as Java identifier.
   * @param str Java identifier
   * @return  false if contains invalid Java identifier part, true if all characters are valid
   */
  public static boolean isValidJavaIdentifier(String str) {
    if (str == null || str.length() == 0 || !Character.isJavaIdentifierStart(str.charAt(0))) {
      return false;
    }
    for (int i = 1; i < str.length(); i++) {
      if (!Character.isJavaIdentifierPart(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Replaces all non-ISO-8859-1 characters with their numeric character
   * references
   * <p>
   * Ensures the correct output when the encoding of an HTML page where this
   * message should be displayed is unknown
   * </p>
   *
   * @param message
   * the message to be encoded
   *
   * @return
   * the original message, but with all non-ISO-8859-1 characters replaced
   * with their numeric character references
   */
  public static String encodeToEntities(String message) {
    if (message == null) {
      return null;
    }
    int i = 0, j = 0;
    StringBuffer result = null;
    int length = message.length();
    for (; i < length; i++) {
      int shar = message.charAt(i);
      if (shar < 127) {
        continue ;
      }
      // Creates string buffer on demand
      if (result == null) {
        result = new StringBuffer(length*2);
      }
      result.append(message.substring(j, i));
      // Numeric character reference starts with "&#" fallowed by
      // Unicode character code as decimal value and ends with ";"
      result.append("&#");
      result.append(shar);
      result.append(";");
      j = i + 1;
    }
    if (result == null) {
      return message;
    }
    result.append(message.substring(j, i));
    return result.toString();
  }

}
