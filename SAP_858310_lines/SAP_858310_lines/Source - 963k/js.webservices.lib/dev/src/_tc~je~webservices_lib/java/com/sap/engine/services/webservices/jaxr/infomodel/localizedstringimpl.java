package com.sap.engine.services.webservices.jaxr.infomodel;

import java.util.Locale;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.LocalizedString;

public class LocalizedStringImpl implements LocalizedString {
  private Locale locale;
  private String value;
  private String charset;
  
  public Locale getLocale() throws JAXRException {
    return locale;
  }
  
  public String getValue() throws JAXRException {
    return value;
  }
  
  public void setLocale(Locale l) throws JAXRException {
    locale = l;
  }
  
  public void setValue(String s) throws JAXRException {
    value = s;
  }
  
  public void setCharsetName(String charset) throws JAXRException {
    this.charset = charset;
  }
  
  public String getCharsetName() throws JAXRException {
    if (charset != null) {
      return charset;
    } else {
      return LocalizedString.DEFAULT_CHARSET_NAME;
    }    
  }
}