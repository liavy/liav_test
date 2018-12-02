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
 
package com.sap.engine.services.webservices.server.container.configuration;

import java.util.Enumeration;
import java.util.Hashtable;

import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.exceptions.PatternKeys;
import com.sap.engine.services.webservices.exceptions.RegistryException;

/**
 * Title: BindingDataRegistry 
 * Description: BindingDataRegistry is a registry for binding datas
 * 
 * @author Dimitrina Stoyanova
 * @version
 */
public class BindingDataRegistry {
  
  private Hashtable<String, BindingData> bindingDatas;
  
  public BindingDataRegistry() {    
    this.bindingDatas = new Hashtable<String, BindingData>();
  }     

  /**
   * @return - a hashtable of binding datas 
   */
  public Hashtable getBindingDatas() {
    if(bindingDatas == null) {
      bindingDatas = new Hashtable();
    }
    return bindingDatas;
  }
  
  public boolean containsBindingDataID(String id) {
    return getBindingDatas().containsKey(id); 
  }
  
  public boolean containsBindingData(BindingData bindingData) {
    return getBindingDatas().contains(bindingData); 
  }
  
  public void putBindingData(String id, BindingData bindingData) throws Exception {
    if(containsBindingDataID(id)) {
      //throw new RegistryException(PatternKeys.WS_DUBLICATE_ELEMENT, new Object[]{id});
    }
    getBindingDatas().put(id, bindingData);
  }
  
  public BindingData getBindingData(String id) {
    return (BindingData)getBindingDatas().get(id);
  }
  
  public BindingData removeBindingData(String id) {
    return (BindingData)getBindingDatas().remove(id);
  }  
  
  public String toString() {
    String resultStr = ""; 
    String nl = System.getProperty("line.separator");    
    
    Hashtable bindingDatas = getBindingDatas();
    
    if(bindingDatas.size() == 0) {    
      return "EMPTY";
    }
    
    Enumeration enum1 = bindingDatas.keys();
    int i = 0; 
    while(enum1.hasMoreElements()) {
      resultStr += "Binding Data[" + i++ + "]: " +enum1.nextElement() + nl;   
    }            
    
    return resultStr;
  }

}
