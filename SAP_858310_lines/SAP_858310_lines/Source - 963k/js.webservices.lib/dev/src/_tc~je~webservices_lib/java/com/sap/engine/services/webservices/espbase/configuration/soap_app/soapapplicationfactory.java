/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.configuration.soap_app;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.p_set.PropertySetType;
import com.sap.engine.services.webservices.jaxrpc.encoding.XMLMarshaller;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Dec 4, 2006
 */
public class SoapApplicationFactory {
  private static XMLMarshaller marshaller = null;
  private static QName elementName = new QName("http://xml.sap.com/2006/11/esi/conf/soapapplication", "SoapApplication");
  
  private static XMLMarshaller getMarshaller() throws TypeMappingException, IOException {
    if (marshaller == null) { 
      marshaller = new XMLMarshaller();
      InputStream config = ConfigurationFactory.class.getResourceAsStream("/com/sap/engine/services/webservices/espbase/configuration/soap_app/frm/types.xml");  
      marshaller.init(config, ConfigurationFactory.class.getClassLoader());
    }
    return marshaller;
  }
  /**
   * Loads configuration from InputStream and closes the input stream.
   * @param input
   * @return
   */
  public static synchronized SoapApplicationType load(InputStream input) throws TypeMappingException, IOException {
    SoapApplicationType result = (SoapApplicationType) getMarshaller().unmarshal(SoapApplicationType.class,elementName,input);
    return result;      
  }  

}
