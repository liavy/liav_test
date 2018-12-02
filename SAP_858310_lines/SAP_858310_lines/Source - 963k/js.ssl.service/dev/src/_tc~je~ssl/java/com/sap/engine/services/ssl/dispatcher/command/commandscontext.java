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
import com.sap.engine.services.ssl.dispatcher.RuntimeInterfaceImpl;

/**
 *  This class registers available commands with the shell.
 *
 * @author  Svetlana Stancheva
 * @version 4.3
 */
public class CommandsContext {

  private Command[] commands = null;
  private RuntimeInterfaceImpl runtime = null;

  /**
   *  Default constructor.
   */
  public CommandsContext(RuntimeInterfaceImpl runtime) {
    this.runtime = runtime;
  }

  private void createCommandsArray() {
    commands = new Command[] {new AddTrustedCertificateCommand(runtime), new RemoveTrustedCertificateCommand(runtime), new ListCommand(runtime), new SessionCommand(), new InvalidateSessionCommand()};
  }

  /**
   *  Returns the available commands for the ssl service.
   *
   * @return  array of commands provided by ssl service.
   */
  public Command[] getAvailableCommands() {
    if (commands == null) {
      createCommandsArray();
    }

    return commands;
  }

}

