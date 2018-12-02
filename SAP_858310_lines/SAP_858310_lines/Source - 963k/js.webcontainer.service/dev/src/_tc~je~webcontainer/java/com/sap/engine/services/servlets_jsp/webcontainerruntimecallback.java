/*
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp;

import com.sap.engine.services.servlets_jsp.WebContainerInterface;

import java.rmi.RemoteException;
import java.rmi.Remote;

//import com.inqmy.frame.RuntimeInterface;
//import com.sap.engine.services.servlets_jsp.descriptor.WebDeploymentDescriptor;
public interface WebContainerRuntimeCallback extends Remote {

  public void update(String appName, WebContainerInterface ri) throws RemoteException;


  public void removeApp(String appName, WebContainerInterface ri) throws RemoteException;


  public void appStarted(String appName, WebContainerInterface ri) throws RemoteException;


  public void appStopped(String appName, WebContainerInterface ri) throws RemoteException;

}

