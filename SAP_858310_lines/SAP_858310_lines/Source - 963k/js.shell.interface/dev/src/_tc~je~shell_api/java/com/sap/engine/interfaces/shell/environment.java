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
 * This interface represents an environment for some kind of shell.
 *
 * @author Ventcislav Dimitrov
 * @version 4.0.0
 */
public interface Environment {

  /**
   * Gets the input stream
   *
   * @return   The input stream for this environment.
   */
  public InputStream getInputStream();


  /**
   * Gets the output stream
   *
   * @return   The output stream for this environment.
   */
  public OutputStream getOutputStream();


  /**
   * Returns the error stream
   *
   * @return   The error stream for this environment.
   */
  public OutputStream getErrorStream();


  /**
   * Gets the value of a variable with the specified name.
   *
   * @param   name  Name of the variable
   * @return   The requested variable
   */
  public String getVariable(String name);


  /**
   * Gets an object , which represents a context for this environment, i.e. it contains
   * some specific information needed for the shell or its environment.
   *
   * @return   The requested object
   */
  public Object getContext();


  /**
   * Gets a boolean variable ,presenting whether the administrating is by a remote
   * client .If it equals false ,the client uses this environment for managing
   * the system by a remote client, otherwise he is managing the
   * system locally.
   *
   * @return The requested value
   */
  public boolean isRemote();

}

