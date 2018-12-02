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

package com.sap.engine.services.webservices.server.container.metadata;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Title: InterfaceDefMetaDataRegistry 
 * Description: InterfaceDefMetaDataRegistry 
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class InterfaceDefMetaDataRegistry {
  
  private Hashtable<String, InterfaceDefMetaData> interfaceDefMetaDatas; 
  
  public InterfaceDefMetaDataRegistry() {
    this.interfaceDefMetaDatas = new Hashtable<String, InterfaceDefMetaData>(); 
  }  
  
  /**
   * @return - a hashtable of interface definition data meta data
   */
  public Hashtable getInterfaceDefMetaDatas() {
    if(interfaceDefMetaDatas == null) {
      interfaceDefMetaDatas = new Hashtable();
    }
    return interfaceDefMetaDatas;
  }
  
  public boolean containsInterfaceDefMetaDataID(String id) {    
    return getInterfaceDefMetaDatas().containsKey(id);    
  }
  
  public boolean containsInterfaceDefMetaData(InterfaceDefMetaData bindingDataMetaData) {
    return getInterfaceDefMetaDatas().contains(bindingDataMetaData);    
  }
  
  public void putInterfaceDefMetaData(String id, InterfaceDefMetaData bindingDataMetaData) {
    getInterfaceDefMetaDatas().put(id, bindingDataMetaData);    
  }
  
  public InterfaceDefMetaData getInterfaceDefMetaData(String id) {
    return (InterfaceDefMetaData)getInterfaceDefMetaDatas().get(id);    
  }
  
  public InterfaceDefMetaData removeInterfaceDefMetaData(String id) {
    return (InterfaceDefMetaData)getInterfaceDefMetaDatas().remove(id);   
  }
  
  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");    

    Hashtable interfaceDefMetaDatas = getInterfaceDefMetaDatas();

    if(interfaceDefMetaDatas.size() == 0) {
      return "EMPTY";
    }

    Enumeration enum1 = interfaceDefMetaDatas.keys();
    int i = 0;     
    while(enum1.hasMoreElements()) {
      String bindingDataUrl = (String)enum1.nextElement();      
      InterfaceDefMetaData interfaceDefMetaData = (InterfaceDefMetaData)interfaceDefMetaDatas.get(bindingDataUrl);
      resultStr += "InterfaceDefMetaData[" + i + "]: " + bindingDataUrl + nl; 
      resultStr += interfaceDefMetaData.toString() + nl;      
    }            

    return resultStr;
  }

}
