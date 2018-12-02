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
package com.sap.engine.interfaces.webservices.admin;

import java.rmi.RemoteException;

import com.sap.engine.frame.state.ManagementInterface;
import com.sap.engine.interfaces.webservices.runtime.definition.IWSClient;
import com.sap.engine.interfaces.webservices.runtime.definition.IWebService;
import com.sap.engine.interfaces.webservices.uddi.UserAccount;
import com.sap.engine.interfaces.webservices.uddi4j.UDDIRegistry;

/**
 * Copyright (c) 2003, SAP-AG
 * @author Alexander Zubev (alexander.zubev@sap.com)
 * @version 1.1, 2004-2-5
 */
public interface AdminManager {
  public static final String NAME = "AdminManager"; 
  
  public IWebService[] getWebServices();
  public IWSClient[] getWSClients();
  public UDDIRegistry[] getUDDIRegistries() throws RemoteException;
  public UserAccount[] getUDDIUsers() throws Exception;
  public void removeUDDIUser(String name) throws Exception;
  public void updateUDDIUser(UserAccount user, char[] password) throws Exception;
  public void addUDDIRegistry(UDDIRegistry registry) throws Exception;
  public void removeUDDIRegistry(UDDIRegistry registry) throws Exception;
  public ManagementInterface getManagementInterface(String service) throws Exception;
  
  
  //public PolicyListener[] getPolicyListeners();
}
