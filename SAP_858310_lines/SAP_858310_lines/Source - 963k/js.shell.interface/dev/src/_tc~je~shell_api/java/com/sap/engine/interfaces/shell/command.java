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
package com.sap.engine.interfaces.shell;

import java.io.*;

/**
 * An interface representing a command, which will be executed in an implementation of
 * the Environment interface.
 *
 * @author Ventcislav Dimitrov
 * @version 4.0.0
 */
public interface Command {

  /**
   * A method that executes the command .
   *
   * @param   environment  An implementation of Environment.
   * @param   input  The InputStream , used by the command.
   * @param   output  The OutputStream , used by the command.
   * @param   params  Parameters of the command.
   *
   */
  public void exec(Environment environment, InputStream input, OutputStream output, String[] params);


  /**
   * Gets the name of the command
   *
   * @return   The name of the command.
   */
  public String getName();


  /**
   * Returns the name of the group the command belongs to
   *
   * @return   The name of the group of commands, in which this command belongs.
   */
  public String getGroup();


  /**
   * Gives the name of the supported shell providers
   *
   * @return   The Shell providers' names who supports this command.
   */
  public String[] getSupportedShellProviderNames();


  /**
   * Gives a short help message about the command
   *
   * @return   A help message for this command.
   */
  public String getHelpMessage();

}

