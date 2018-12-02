/**
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.ssl.dispatcher.command;

import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.interfaces.shell.Environment;
import com.sap.engine.services.ssl.factory.session.JSSELimitedCache;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 * @author Jako Blagoev
 */
public class SessionCommand implements Command {

  public void exec(Environment environment, InputStream input, OutputStream output, String[] params) {
    try {
      if (params.length != 1) {
        output.write(getHelpMessage().getBytes());
        return;
      }
      JSSELimitedCache cache = JSSELimitedCache.getInstance();
      if (params[0].equals("-list")) {
        output.write(cache.toString().getBytes());
        return;
      }
      byte[] sessionKey = hexStringToByteArr(params[0]);
      String toString = cache.toString(sessionKey);
      if (toString == null) {
        output.write(("no ssl session with key " + params[0] + "\r\n").getBytes());
        return;
      }
      output.write(toString.getBytes());
    } catch (IOException ioe) {
      try {
        output.write(ioe.getMessage().getBytes());
      } catch (Exception e) {
//      $JL-EXC$
      }
    }
  }

  public String getName() {
    return "sslsession";
  }

  public String getGroup() {
    return "ssl";
  }

  public String[] getSupportedShellProviderNames() {
    return new String[0];
  }

  public String getHelpMessage() {
    return "Shows the ssl sessions in details.\nUsage: \n" +
            getName() + " -list\n Shows all the valid sessions.\n" +
            getName() + " [sessionID]\n Shows the specified ssl session in details.\n";
  }

  private static byte[] hexStringToByteArr(String s) {
    StringTokenizer tokenizer = new StringTokenizer(s,":",false);
    byte[] arr = new byte[tokenizer.countTokens()];
    int i = 0;
    String token = null;
    while (tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken();
      char c1 = token.charAt(0);
      char c2 = token.charAt(1);
      arr[i++] = (byte) (hexCharToByte(c1) << 4 | hexCharToByte(c2));
    }
    return arr;
  }

  private static byte hexCharToByte(char c) {
    for (byte i = 0 ; i < hexDigits.length; i++) {
      if(hexDigits[i] == c) {
        return i;
      }
    }
    return 0;
  }

  private final static char[] hexDigits = "0123456789ABCDEF".toCharArray();
}
