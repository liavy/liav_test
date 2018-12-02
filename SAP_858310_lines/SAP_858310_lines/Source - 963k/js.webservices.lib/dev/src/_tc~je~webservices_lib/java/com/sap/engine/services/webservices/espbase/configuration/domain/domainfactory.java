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
package com.sap.engine.services.webservices.espbase.configuration.domain;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReader;
import com.sap.engine.lib.xml.parser.tokenizer.XMLTokenReaderFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationFactory;
import com.sap.engine.services.webservices.jaxrpc.encoding.XMLMarshaller;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, May 31, 2006
 */
public class DomainFactory {
  private static XMLMarshaller marshaller = null;
  private static QName rootTypeName = new QName("http://www.sap.com/webas/710/ws/configuration-domain", "DomainType");

  public static synchronized DomainType load(InputStream input) throws Exception {
    XMLTokenReaderFactory factory = XMLTokenReaderFactory.getInstance();
    XMLTokenReader reader = factory.createReader();
    reader.init(input);
    reader.begin();
    reader.moveToNextElementStart();
    DomainType result = (DomainType) getMarshaller().unmarshal(DomainType.class, rootTypeName, reader);
    return result;      
  }
  
  private static XMLMarshaller getMarshaller() throws TypeMappingException, IOException {
    if (marshaller == null) { 
      marshaller = new XMLMarshaller();
      InputStream config = ConfigurationFactory.class.getResourceAsStream("/com/sap/engine/services/webservices/espbase/configuration/domain/frm/types.xml");  
      marshaller.init(config, ConfigurationFactory.class.getClassLoader());
      config.close();
    }
    return marshaller;
  }

}
