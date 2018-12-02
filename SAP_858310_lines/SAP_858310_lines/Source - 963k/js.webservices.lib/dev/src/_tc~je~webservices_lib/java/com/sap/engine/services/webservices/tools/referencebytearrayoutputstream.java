/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 */
package com.sap.engine.services.webservices.tools;

/**
 *
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class ReferenceByteArrayOutputStream extends java.io.ByteArrayOutputStream {

  public ReferenceByteArrayOutputStream() {
  }

  public ReferenceByteArrayOutputStream(int size) {
    super(size);
  }

  public byte[] getContentReference() {
    return super.buf;
  }
  /**
   * Sets the internal byte array reference to point to <code>arr</code>
   * and sets the internal counter to <code>arr</code> size.
   */
  public void presetContent(byte[] arr) {
    super.buf = arr;
    super.count = arr.length;
  }
}
