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
package com.sap.engine.services.webservices.espbase.configuration.p_set;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.jaxrpc.encoding.XMLMarshaller;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Dec 4, 2006
 */
public class PropertySetFactory {
  private static XMLMarshaller marshaller = null;
  private static QName elementName = new QName("http://xml.sap.com/2006/11/esi/conf/feat/", "PropertySet");
  
  private static XMLMarshaller getMarshaller() throws TypeMappingException, IOException {
    if (marshaller == null) { 
      marshaller = new XMLMarshaller();
      InputStream config = ConfigurationFactory.class.getResourceAsStream("/com/sap/engine/services/webservices/espbase/configuration/p_set/frm/types.xml");  
      marshaller.init(config, ConfigurationFactory.class.getClassLoader());
    }
    return marshaller;
  }
  /**
   * Loads configuration from InputStream and closes the input stream.
   * @param input
   * @return
   */
  public static synchronized PropertySetType load(InputStream input) throws TypeMappingException, IOException {
    PropertySetType result = (PropertySetType) getMarshaller().unmarshal(PropertySetType.class,elementName,input);
    return result;      
  }  
  
  public static synchronized void save(PropertySetType item, OutputStream output) throws TypeMappingException, IOException {
    getMarshaller().marshal(item,elementName,output);
  }
}
