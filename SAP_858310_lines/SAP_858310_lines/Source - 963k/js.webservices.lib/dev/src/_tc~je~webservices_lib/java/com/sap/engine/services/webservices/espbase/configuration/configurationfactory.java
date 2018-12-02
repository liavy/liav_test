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
package com.sap.engine.services.webservices.espbase.configuration;

import java.io.File;
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
 * Factory class for saving and loading configuration files.
 */
public class ConfigurationFactory {
  
  private static XMLMarshaller marshaller = null;
  private static QName elementName = new QName("http://www.sap.com/webas/710/ws/configuration-descriptor","ConfigurationRoot");
  private static QName interfaceName = new QName(null,"InterfaceData");
  private static QName interfaceType = new QName("http://www.sap.com/webas/710/ws/configuration-descriptor","InterfaceData");
  private static QName configName = new QName(null,"BindingData");
  private static QName configType = new QName("http://www.sap.com/webas/710/ws/configuration-descriptor","BindingData");
  
  
  private static XMLMarshaller getMarshaller() throws TypeMappingException, IOException {
    if (marshaller == null) { 
      marshaller = new XMLMarshaller();
      InputStream config = ConfigurationFactory.class.getResourceAsStream("/com/sap/engine/services/webservices/espbase/configuration/frm/types.xml");  
      marshaller.init(config, ConfigurationFactory.class.getClassLoader());
    }
    return marshaller;
  }
  
  
  /**
   * Loads configuration from InputStream and closes the input stream.
   * @param input
   * @return
   */
  public static synchronized ConfigurationRoot load(InputStream input) throws TypeMappingException, IOException {
//		XMLMarshaller marshaller = new XMLMarshaller();
//		InputStream config = ConfigurationFactory.class.getResourceAsStream("/com/sap/engine/services/webservices/espbase/configuration/frm/types.xml");	
//		marshaller.init(config);
		ConfigurationRoot result = (ConfigurationRoot) getMarshaller().unmarshal(ConfigurationRoot.class,elementName,input);
		return result;  		
  }
  
  /**
   * Loads configuration from input file.
   * @param fileName
   * @return
   * @throws TypeMappingException
   * @throws IOException
   */
  public static ConfigurationRoot load(String fileName) throws TypeMappingException, IOException {
    FileInputStream input = new FileInputStream(fileName);
    try {
      return load(input);
    } finally {
      input.close();
    }	
  }
  
  /**
   * Saves configuration to OutputStream and closes the stream.
   * @param item
   * @param output
   * @throws TypeMappingException
   * @throws IOException
   */
  public static synchronized void save(ConfigurationRoot item, OutputStream output) throws TypeMappingException, IOException {
//		XMLMarshaller marshaller = new XMLMarshaller();
//		InputStream config = ConfigurationFactory.class.getResourceAsStream("/com/sap/engine/services/webservices/espbase/configuration/frm/types.xml");	
//		marshaller.init(config);
//		QName elementName = new QName("http://www.sap.com/webas/710/ws/configuration-descriptor","ConfigurationRoot");
		getMarshaller().marshal(item,elementName,output);  					
  }
  
  
  public static synchronized void saveDTConfig(InterfaceData iData, OutputStream output) throws TypeMappingException,IOException {
    getMarshaller().marshal(iData,interfaceName,interfaceType,output);    
  }
  
  public static synchronized void saveRTConfig(BindingData bData, OutputStream output) throws TypeMappingException,IOException {
    getMarshaller().marshal(bData,configName,configType,output);
  }  
  
  /**
   * Saves configuration to output file and closes the file.
   * @param item
   * @param fileName
   * @throws TypeMappingException
   * @throws IOException
   */
  public static void save(ConfigurationRoot item, String fileName) throws TypeMappingException, IOException {
    File f = new File(fileName);
    File dir = f.getParentFile(); //this should be the file parent directory
    dir.mkdirs();//create all necessary directories
  	FileOutputStream output = new FileOutputStream(f);
    try {
      save(item, output);
    } finally {
      output.close();
    }
  }
  
  /*
  public static void main(String[] args) throws TypeMappingException, IOException {
    ConfigurationRoot config = load("E:/DTR/0/DCs/sap.com/tc/je/webservices_lib/_comp/src/packages/com/sap/engine/services/webservices/espbase/configuration/sample1.xml");
    System.out.println("Configuration loaded.");
    save(config,"E:/DTR/0/DCs/sap.com/tc/je/webservices_lib/_comp/src/packages/com/sap/engine/services/webservices/espbase/configuration/sample1out.xml");
    System.out.println("Configuration saved.");
  }
  */
    
}
