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

import java.io.*;
import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.interfaces.shell.Environment;
import com.sap.engine.services.ssl.dispatcher.RuntimeInterfaceImpl;

public class AddTrustedCertificateCommand implements Command {

  private RuntimeInterfaceImpl runtime = null;

  public AddTrustedCertificateCommand(RuntimeInterfaceImpl runtime) {
    this.runtime = runtime;
  }

  /**
   *  Executes the command.
   *
   * @param  env  the environment of the corresponding process ,which executes the command
   * @param  is   an input stream for this command
   * @param  os   an output stream for the resusts of this command
   * @param  params  parameters of the command - expiration period in seconds
   */
  public void exec(Environment env, InputStream is, OutputStream os, String[] params) {
    PrintWriter out = new PrintWriter(os, true);

     if ((params.length > 0) && (params[0].equals("-?") || params[0].equalsIgnoreCase("-h") || params[0].equalsIgnoreCase("-help"))) {
      out.println(getHelpMessage());
      return;
    }

    try {
//      if (params.length == 1) {
//        runtime.addTrustedCertificates(RuntimeInterfaceImpl.NEW_SERVER_SOCKETS, 0, params[0]);
//      } else if (params.length == 3) {
//        runtime.addTrustedCertificates(params[0], Integer.parseInt(params[1]), params[2]);
//      } else {
//        out.println(getHelpMessage());
//      }
    } catch (Exception e) {
      new PrintWriter(env.getErrorStream(), true).println(getHelpMessage());
    }
  }

  /**
   *  Returns the name of the group of this command.
   *
   * @return  the name of the group of this command.
   */
  public String getGroup() {
    return "ssl";
  }

  /**
   *  Returns a printable explanation of how the command is used.
   *
   * @return  explanatory printable string.
   */
  public String getHelpMessage() {
    return "Adds trusted certificate to an existing socket or to the configuration that will be used for the new sockets.\nUsage: " + getName() + " [<host> <port>] <alias>\nParameters:\n\t" +
            "[<host> <port>] - The host and port of the SSL socket.\n\t" +
            "<alias> - the alias of the certificate in 'TrustedCAs' keystore view";
  }

  /**
   * Gets a name for this command.
   *
   * @return     the message
   */
  public String getName() {
    return "ADD_TRUSTED_CERTIFICATE";
  }

  public String[] getSupportedShellProviderNames() {
    return null;
  }

}

