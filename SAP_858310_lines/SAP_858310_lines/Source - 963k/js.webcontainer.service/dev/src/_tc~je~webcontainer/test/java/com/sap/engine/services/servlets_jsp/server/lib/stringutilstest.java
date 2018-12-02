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

package com.sap.engine.services.servlets_jsp.server.lib;

import com.sap.engine.services.servlets_jsp.server.LogContext;

import junit.framework.TestCase;

/**
 * @author Todor Mollov
 * webcontainer_junit
 * 2006-2-9
 * 
 */
public class StringUtilsTest extends TestCase {
  
  
  
  protected void setUp() throws Exception {
    //initialize the LogContext, otherwise NullPointer is thrown
    LogContext.init();
  }
  
  public void testEscapeJavaCharacters() {
   String parameter = "\b1\t2\r3\n4\f5\'6\"7\\";
   String expectedResult = "\\b1\\t2\\r3\\n4\\f5\\'6\\\"7\\\\";
   String returnedValue = StringUtils.escapeJavaCharacters(parameter);
   assertEquals(returnedValue, expectedResult);
  }
  
  /**
   * Tests unquote method. 
   * The purpose of the method is to remove the outer double quotes - ".
   * Apostrophe will be preserved.
   */
  public void testUnquote() {
    String requests[] = {"\"normal\"",
        									"\'normal\'",
        									"norm\"al",
        									"\"\"\"normal\"\"\""
        									};
    String results[] = {"normal",
        								"\'normal\'",
        								"norm\"al",
        								"normal"
        								};
    for(int i = 0 ; i < requests.length; i++){
      String returnValue = StringUtils.unquote(requests[i].getBytes(), 0, requests[i].length());
      assertEquals(returnValue, results[i]);
    }
    
  }

  /**
   * The method replaceCRLF() should replace ALL occurences of \r or \n with spaces.
   *
   */
  public void testReplaceCRLF() {
    String parameter = "\r1\n2\r\n3\n\r4";
    String expectedResult = " 1 2  3  4";
    String returnValue = StringUtils.replaceCRLF(parameter);
    assertEquals(returnValue, expectedResult);
  }

  /** 
   * Class under test for byte[] filterHeaderName(byte[])
   * 
	 * Used for Header names. According HTTP spec. 1.1:
	 * Header names are tokens. 
	 * token = 1*<any CHAR except CTLs or separators>
	 * separators = "(" | ")" | "<" | ">" | "@"
	 * | "," | ";" | ":" | "\" | <">
	 * | "/" | "[" | "]" | "?" | "="
	 * | "{" | "}" | SP | HT
	 * CHAR = <any US-ASCII character (octets 0 - 127)> 
	 * @param value
	 * @return
   */
  public void testFilterHeaderNamebyteArray() {
    String parameter = "\r1\n2 3(4)5<6>7@8/9,10;11:12\\13[14]15?16=17{18}";
    String expectedValue = "123456789101112131415161718";
    String returnedValue = new String(StringUtils.filterHeaderName(parameter.getBytes()));
    assertEquals(expectedValue, returnedValue);    
  }

  /*
   * Class under test for byte[] filterHeaderName(String)
   */
  public void testFilterHeaderNameString() {
    String parameter = "\r1\n2 3(4)5<6>7@8/9,10;11:12\\13[14]15?16=17{18}";
    String expectedValue = "123456789101112131415161718";
    String returnedValue = new String(StringUtils.filterHeaderName(parameter));
    assertEquals(expectedValue, returnedValue);  
  }

  /*
   * Class under test for byte[] filterHeaderValue(String)
   * Replaces all occurences of CR AND LF with spaces. 
   * If combination of bytes forms "linear white space" it is not touched.
   * This method should be used only for header values.
   * HTTP 1.1:
   *    CRLF = CR LF 
   *    LWS = [CRLF] 1*( SP | HT )
   */
  public void testFilterHeaderValueString() {
    String parameter = "\r1\n2\r\n3\r\n 4 5\n\r 6";
    String expectedValue = " 1 2  3\r\n 4 5   6";
    String returnedValue = new String(StringUtils.filterHeaderValue(parameter));
    assertEquals(expectedValue, returnedValue);  
  }

  /*
   * Class under test for byte[] filterHeaderValue(byte[])
   */
  public void testFilterHeaderValuebyteArray() {
    String parameter = "\r1\n2\r\n3\r\n 4 5\n\r 6";
    String expectedValue = " 1 2  3\r\n 4 5   6";
    String returnedValue = new String(StringUtils.filterHeaderValue(parameter.getBytes()));
    assertEquals(expectedValue, returnedValue); 
  }

}
