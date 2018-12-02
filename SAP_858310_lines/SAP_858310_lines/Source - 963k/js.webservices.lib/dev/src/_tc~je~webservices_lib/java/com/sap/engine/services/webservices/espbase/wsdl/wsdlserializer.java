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
package com.sap.engine.services.webservices.espbase.wsdl;

import java.io.File;
import java.util.List;
import java.util.jar.JarOutputStream;

import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLMarshalException;
import com.sap.engine.services.webservices.espbase.wsdl.wsdl11.WSDL11Serializer;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-12-9
 */
public class WSDLSerializer {
  
  private WSDL11Serializer serializer;
  
  public WSDLSerializer() throws WSDLMarshalException {
    serializer = new WSDL11Serializer();    
  }
  
  /**
   * Returns a list of WSDLDescriptor objects.
   */
  public List serialize(Definitions def) throws WSDLMarshalException {
    return serialize(def, 0xFFFF);
  }

  /**
   * Returns a list of WSDLDescriptor objects
   * @param def definitions which is to be serialized
   * @param mask denotes which components of the definitions to be serialized - service(s), interface(s), binding(s). 
   *        For example if only Service is needed to be serialized the mask value should be Service.SERVICE_ID.
   */
  public List serialize(Definitions def, int mask) throws WSDLMarshalException {
    if (Definitions.WSDL11.equals(def.getProperty(Definitions.VERSION))) {
      return serializer.serialize(def, mask);
    } else {
      throw new WSDLMarshalException(WSDLMarshalException.DEFINITIONS_VERSION_NOT_SUPPORTED, new Object[]{def.getProperty(Definitions.VERSION)});
    } 
  }
  /**
   * @deprecated use the other save method.
   * @param def
   * @param dir
   * @param baseFName
   * @throws WSDLMarshalException
   */
  public void save(Definitions def, String dir, String baseFName) throws WSDLMarshalException {
    List descrs = serialize(def);
    for (int i = 0; i < descrs.size(); i++) {
      ((WSDLDescriptor) descrs.get(i)).setFileName(baseFName + i);
    }
    save(descrs, dir);  
  }
  /**
   * 
   * @param def the definitions to be saved.
   * @param dir the directory into which the files to be saved
   * @param rootWSDLName the name of the wsdl file that will contains the wsdl service(s). 
   * If wsdl service(s) need to be saved in more then one wsdl files exception is thrown.
   * imports, from which 
   * @throws WSDLMarshalException
   */
  public void saveWsdl(Definitions def, String dir, String rootWSDLName) throws WSDLMarshalException {
    List descrs = serialize(def);
    WSDLDescriptor rootWsdl = null;
    for (int i = 0; i < descrs.size(); i++) {
      WSDLDescriptor wd = (WSDLDescriptor) descrs.get(i);
      if (wd.getServices().size() > 0) {
        if (rootWsdl != null) {
          throw new WSDLMarshalException("More then one file will contain wsdl:service entities. This method does not handle this case.");
        }
        rootWsdl = wd;
        rootWsdl.setFileName(rootWSDLName);
      } else {
        int dotInd = rootWSDLName.indexOf("."); //search for extension
        if (dotInd > 0 && dotInd < rootWSDLName.length() - 1) {
          String ext = rootWSDLName.substring(dotInd + 1, rootWSDLName.length());
          String fName = rootWSDLName.substring(0, dotInd);
          wd.setFileName(fName + "_" + i + "." + ext);
        } else {
          wd.setFileName(rootWSDLName + "_" + i);
        }
      }
    }
    save(descrs, dir);  
  }
  /**
   * Saves wsdls into given directory. The file names are taken from the WSDLDescriptor.getFileName().
   * By default the .wsdl extension is added to the file name.
   * Correct relative import locations are generated.
   */
  public void save(List wsDescriptors, String dir) throws WSDLMarshalException {
    save(wsDescriptors, dir, null); 	  
  }
  
  public void save(List wsDescriptors, JarOutputStream jarOut) throws WSDLMarshalException {
    save(wsDescriptors, null, jarOut); 	  
  }
  
  private void save(List wsDescriptors, String dir, JarOutputStream jarOut) throws WSDLMarshalException {
    String version = null;
    String cur;
    //check the version
    for (int i = 0; i < wsDescriptors.size(); i++) {
      cur = ((WSDLDescriptor) wsDescriptors.get(i)).getWsdlVersion();
      if (version == null) {
        version = cur;
      }
      if (version == null || (! version.equals(cur))) {
        throw new WSDLMarshalException("WSDL documents with different versions found " + version + " " + cur);
      }
    }
    
    if(dir != null) {
      File fDir = new File(dir);
      if (! fDir.exists()) {
        fDir.mkdirs();
      }
    }
    
    if (Definitions.WSDL11.equals(version)) {
      serializer.save(wsDescriptors, dir, jarOut);    
    } else {
      throw new WSDLMarshalException(WSDLMarshalException.DEFINITIONS_VERSION_NOT_SUPPORTED, new Object[]{version});      
    }
      
  } 
    
  /**
   * Saves wsdls into given directory. The file names are generated using the baseFName, followed
   * by suffixed unique number (0, 1, ...). By default the .wsdl extension is added to the file name.
   * Correct relative import locations are generated.
   * The wsDescriptors data is modified by the method
   */
  public void saveGeneric(List wsDescriptors, String dir, String baseFName) throws WSDLException {
    WSDLDescriptor cur;
    for (int i = 0; i < wsDescriptors.size(); i++) {
      cur = (WSDLDescriptor) wsDescriptors.get(i);
      cur.setFileName(baseFName + Integer.toString(i));
    }
    save(wsDescriptors, dir);
  }
  
}
