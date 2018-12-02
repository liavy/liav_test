/*
 * Copyright (c) 2003 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.mappings;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.jaxrpc.encoding.XMLMarshaller;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;

/**
 * @author Chavdar-b
 *
 * Factory class for saving and loading of mapping rules.
 */
public class MappingFactory {
  
  private static XMLMarshaller marshaller = null;
  private static QName elementName = new QName("http://www.sap.com/webas/710/ws/ws-mapping-descriptor","mappings"); 
  
  private static XMLMarshaller getMarshaller() throws TypeMappingException, IOException {
    if (marshaller == null) {
      marshaller = new XMLMarshaller();
      InputStream config = MappingFactory.class.getResourceAsStream("/com/sap/engine/services/webservices/espbase/mappings/frm/types.xml"); 
      marshaller.init(config, MappingFactory.class.getClassLoader());
    }
    
    return marshaller;
    
  }
	
	/**
	 * Loads mapping rules from InputStream and closes the input stream.
	 * @param input
	 * @return
	 */
	public static synchronized MappingRules load(InputStream input) throws TypeMappingException, IOException {
	  try {  	
//		  XMLMarshaller marshaller = new XMLMarshaller();
//		  InputStream config = MappingFactory.class.getResourceAsStream("/com/sap/engine/services/webservices/espbase/mappings/frm/types.xml");	
//		  marshaller.init(config);
//		  QName elementName = new QName("http://www.sap.com/webas/710/ws/ws-mapping-descriptor","mappings");	
		  MappingRules result = (MappingRules) getMarshaller().unmarshal(MappingRules.class,elementName,input);
		  return result;  		
	  } finally {
		  input.close();
	  }
	}
  
	/**
	 * Loads mapping rules from input file.
	 * @param fileName
	 * @return
	 * @throws TypeMappingException
	 * @throws IOException
	 */
	public static MappingRules load(String fileName) throws TypeMappingException, IOException {
	  FileInputStream input = new FileInputStream(fileName);
	  return load(input);	
	}
  
	/**
	 * Saves mapping rules to OutputStream and closes the stream.
	 * @param item
	 * @param output
	 * @throws TypeMappingException
	 * @throws IOException
	 */
	public static synchronized void save(MappingRules item, OutputStream output) throws TypeMappingException, IOException {
	  try {  	
//		  XMLMarshaller marshaller = new XMLMarshaller();
//		  InputStream config = MappingFactory.class.getResourceAsStream("/com/sap/engine/services/webservices/espbase/mappings/frm/types.xml");	
//		  marshaller.init(config);
//      QName elementName = new QName("http://www.sap.com/webas/710/ws/ws-mapping-descriptor","mappings");
		  getMarshaller().marshal(item,elementName,output);  					
	  } finally {
		  output.close();
	  }  	
	}
	
	/**
	 * Saves mapping rules to OutputStream and closes the stream.
	 * @param item
	 * @param output
	 * @throws TypeMappingException
	 * @throws IOException
	 */
	public static synchronized void save0(MappingRules item, OutputStream output) throws TypeMappingException, IOException {
	  getMarshaller().marshal(item,elementName,output);  					
	}
  
	/**
	 * Saves mapping rules to output file and closes the file.
	 * @param item
	 * @param fileName
	 * @throws TypeMappingException
	 * @throws IOException
	 */
	public static void save(MappingRules item, String fileName) throws TypeMappingException, IOException {
	  FileOutputStream output = new FileOutputStream(fileName);
	  save(item, output);
	}
	/*
  public static void main(String[] params) throws TypeMappingException, IOException {
    MappingRules rules = load("E:/DTR/0/DCs/sap.com/tc/je/webservices_lib/_comp/src/packages/com/sap/engine/services/webservices/espbase/mappings/sample1.xml");
    System.out.println("Mappings loaded.");
    save(rules,"E:/DTR/0/DCs/sap.com/tc/je/webservices_lib/_comp/src/packages/com/sap/engine/services/webservices/espbase/mappings/sample1out.xml");
    System.out.println("Mappings saved.");
  }
  */
	
}
