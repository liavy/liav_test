package com.sap.engine.services.webservices.espbase.client.transport.local;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletInputStream;

/**
 * @version 1.0
 * @author Ivaylo Zlatanov, ivaylo.zlatanov@sap.com
 *
 */

public class LocalServletInputStream extends ServletInputStream {

  private ByteArrayInputStream body;
  
  public LocalServletInputStream(ByteArrayInputStream bais){
    body = bais;
  }
  
  public LocalServletInputStream(byte[] bytes){
    body = new ByteArrayInputStream(bytes);
  }
  
  @Override
  public int read() throws IOException {
    return body.read();
  }
  
  @Override
  public int readLine(byte[] b, int i, int j) throws IOException{
    throw new UnsupportedOperationException();
  }
  
  @Override
  public int read(byte b[]) throws IOException {
    return body.read(b);
  }
  
  @Override
  public int read(byte b[], int off, int len) throws IOException {
    return body.read(b, off, len);
  }
  
  @Override
  public long skip(long n) throws IOException {
    return body.skip(n);
  }
  
  @Override
  public int available() throws IOException {
    return body.available();
  }
  
  @Override
  public void close() throws IOException{
    body.close();
  }
  
  @Override
  public boolean markSupported() {
   return body.markSupported(); 
  }
  
  @Override
  public synchronized void mark(int readlimit){
   body.mark(readlimit);
  }
  
  @Override
  public synchronized void reset() throws IOException {
   body.reset(); 
  }
}
