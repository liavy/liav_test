/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.interfaces.webservices.client;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * This class represents a connection to an external server.
 *
 * @author Alexander Zubev
 */
public interface WSConnection {

  /**
   * A method for getting the input stream of the connection
   *
   * @return InputStream of the connection
   */
  public InputStream getInputStream();

  /**
   * A method for getting the output stream of the connection.
   *
   * @return OutputStream of the connection
   */
  public OutputStream getOutputStream();

  /**
   * A method for closing the real connection
   *
   * @throws IOException in case of I/O errors
   */
  public void close() throws IOException;

}
