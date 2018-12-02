package com.sap.engine.services.webservices.espbase.client.transport.local;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

import com.sap.engine.services.webservices.tools.ReferenceByteArrayOutputStream;

/**
 * @version 1.0
 * @author Ivaylo Zlatanov, ivaylo.zlatanov@sap.com
 *
 */

public class LocalServletOutputStream extends ServletOutputStream {
  
  private ByteArrayOutputStream body;
  
  public LocalServletOutputStream(ReferenceByteArrayOutputStream rbaos){
   body = rbaos; 
  }
  
  @Override
  public void write(int b) throws IOException {
    body.write(b);
  }
  
  @Override
  public void write(byte b[]) throws IOException {
    body.write(b);
  }

  @Override
  public void write(byte b[], int off, int len) throws IOException {
    body.write(b, off, len);
  }
  
  @Override
  public void flush() throws IOException {
    body.flush();
  }
  
  @Override
  public void close() throws IOException {
    body.close();
  }
}
