package com.sap.sl.util.jarsl.impl;

import java.io.IOException;
import java.util.jar.JarOutputStream;

import com.sap.sl.util.jarsl.api.JarSLOutputStreamIF;

public class JarSLOutputStream implements JarSLOutputStreamIF {
  private JarOutputStream jos=null;
  private boolean open=false;
  JarSLOutputStream(JarOutputStream jos) {
    this.jos=jos;
    open=true;
  }
  public void write(byte[] b, int off, int len) throws IOException {
    if (!open) {
      throw new IOException("Cannot write data, because the JarSLOutput stream was closed.");
    }
    if (jos==null) {
      throw new IOException("Cannot write data, because the JarSLFileStream handler was closed.");
    }
    jos.write(b,off,len);
  }
  public void close() throws IOException {
    open=false;
  }
  boolean isOpen() {
    return open;
  }
}
