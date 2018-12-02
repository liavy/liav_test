package com.sap.engine.services.webservices.espbase.sr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.sr.metadata.ClassificationsMetaData;
import com.sap.engine.services.webservices.jaxrpc.encoding.XMLMarshaller;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;

/**
 * @author Viktoriya Ivanova
 */

public class MetadataXMLLoader {

  
  protected static XMLMarshaller marshaller;
  protected static QName elementName;
  
  static {
    try {
      marshaller = new XMLMarshaller();
      marshaller.setStringTrim(true);
      InputStream config = MetadataXMLLoader.class.getResourceAsStream("/com/sap/engine/services/webservices/espbase/sr/metadata/frm/types.xml"); 
      marshaller.init(config, MetadataXMLLoader.class.getClassLoader());
    } catch (Exception mEx) {
      throw new ExceptionInInitializerError(mEx);
    }
    elementName = new QName("http://www.sap.com/webas/2006/11/sr/metadata", "ClassificationsMetaData");
  }
  
  
  public static synchronized ClassificationsMetaData load(InputStream input) throws TypeMappingException, IOException {
    try {   
      return (ClassificationsMetaData)marshaller.unmarshal(ClassificationsMetaData.class, elementName, input);
    } finally {
      input.close();
    }
  }
  
  
  public static synchronized ClassificationsMetaData load(File f) throws TypeMappingException, IOException {
    return load(new FileInputStream(f));
  }
  
  
  public static synchronized void store(ClassificationsMetaData item, OutputStream output) throws TypeMappingException, IOException {
    try {   
      marshaller.marshal(item, elementName, output);           
    } finally {
      output.close();
    }
  }
  
  
  public static void store(ClassificationsMetaData item, String fileName) throws TypeMappingException, IOException {
    store(item, fileName);
  }
  
}
