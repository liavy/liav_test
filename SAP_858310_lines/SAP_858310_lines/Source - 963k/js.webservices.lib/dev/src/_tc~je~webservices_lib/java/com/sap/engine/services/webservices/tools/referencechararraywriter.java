/*
 * Copyright (c) 2005 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.tools;

import java.io.CharArrayWriter;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2005-10-5
 */
public class ReferenceCharArrayWriter extends CharArrayWriter {
  
  
  public ReferenceCharArrayWriter() {
    super();
  }
  public ReferenceCharArrayWriter(int initialSize) {
    super(initialSize);
  }
  
  public char[] getContentReference() {
    return super.buf;
  }

}
