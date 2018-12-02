/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.jaxr;

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.xml.registry.JAXRException;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class JAXRNestedException extends JAXRException {
  public JAXRNestedException(String msg, Throwable thr) {
    super(msg, thr);
  }
  
  public JAXRNestedException(Throwable thr) {
    super(thr);
  }
  
  public void printStackTrace() {
    this.printStackTrace(System.err); //$JL-SYS_OUT_ERR$
  }

  public void printStackTrace(PrintStream stream) {
    this.printStackTrace(new PrintWriter(stream, true));
  }

  public void printStackTrace(PrintWriter s) {
    super.printStackTrace(s);
    s.write("--- caused by ---\r\n");
    getCause().printStackTrace(s);
  }

}
