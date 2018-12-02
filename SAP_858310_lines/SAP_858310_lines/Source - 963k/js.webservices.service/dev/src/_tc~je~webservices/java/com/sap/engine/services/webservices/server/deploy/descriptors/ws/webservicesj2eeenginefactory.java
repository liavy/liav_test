/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
 
package com.sap.engine.services.webservices.server.deploy.descriptors.ws;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.jaxrpc.encoding.XMLMarshaller;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;

/**
 * Title: WebServicesJ2EEEngineFactory 
 * Description: WebServicesJ2EEEngineFactory
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class WebServicesJ2EEEngineFactory {
  
  public WebServicesJ2EEEngineFactory() {
  }
    
  private static XMLMarshaller marshaller = null;
  private static QName elementName = new QName("http://www.sap.com/webas/710/ws/webservices-j2ee-engine-descriptor", "webservices");  
  
  private static XMLMarshaller getMarshaller() throws TypeMappingException, IOException {    
    if (marshaller == null) {
      InputStream configIn = null; 
      try  {              
        marshaller = new XMLMarshaller();
        configIn = WebservicesType.class.getResourceAsStream("/com/sap/engine/services/webservices/server/deploy/descriptors/ws/frm/types.xml");  
        marshaller.init(configIn, WebservicesType.class.getClassLoader());      
      } finally {
        if(configIn != null) {
          try {
            configIn.close(); 
          } catch(IOException e) {
            // $JL-EXC$
          }
        }
      }
    }
    return marshaller;
  }

  public static WebservicesType load(String fileName) throws TypeMappingException, IOException {
    FileInputStream input = null; 
    
    try {
      input = new FileInputStream(fileName);
      return load(input);
    } finally {      
      if(input != null) {
        try {          
         input.close();         
        } catch(IOException e) {
          // $JL-EXC$
        }       
      }     
    } 
  }
  
  public static synchronized WebservicesType load(InputStream input) throws TypeMappingException, IOException {
    WebservicesType result = (WebservicesType)getMarshaller().unmarshal(WebservicesType.class, elementName, input);
    return result;      
  }

  public static void save(WebservicesType item, String fileName) throws TypeMappingException, IOException {
    FileOutputStream output = null; 
    try {
      output = new FileOutputStream(fileName);
      save(item, output);
    } finally {         
      if(output != null) {
        try {      
          output.close();          
        } catch(IOException e) {    
         // $JL-EXC$   
        } 
      }          
    }
  }
  
  public static synchronized void save(WebservicesType item, OutputStream output) throws TypeMappingException, IOException {
    getMarshaller().marshal(item, elementName, output);            
  }
  
}
