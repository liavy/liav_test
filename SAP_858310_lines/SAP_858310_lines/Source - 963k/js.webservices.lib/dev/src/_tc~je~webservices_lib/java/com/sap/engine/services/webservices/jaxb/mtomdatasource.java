package com.sap.engine.services.webservices.jaxb;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public class MTOMDataSource implements DataSource {
  
  private String mimeType;
  private byte[] bytes;
  int offset,length;
  
  protected MTOMDataSource(byte[] bytes, int offset, int length, String mimeType) {
    this.mimeType = mimeType;
    this.bytes = bytes;
    this.offset = offset;
    this.length = length;
  }
  
  public String getContentType() {
    return(mimeType);
  }
  
  public InputStream getInputStream() {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes, offset, length);
    return (inputStream);
  }
  
  public String getName() {
    return(null);
  }
  
  public OutputStream getOutputStream() {
    return(null);
  }
}
