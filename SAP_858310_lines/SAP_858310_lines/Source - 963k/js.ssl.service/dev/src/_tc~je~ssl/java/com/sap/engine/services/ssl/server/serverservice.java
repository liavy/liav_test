/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf.. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.ssl.server;

import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.ApplicationServiceFrame;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.ServiceRuntimeException;
import com.sap.engine.services.ssl.dispatcher.RuntimeInterfaceImpl;
import com.sap.engine.services.ssl.dispatcher.RuntimeInterface;
import com.sap.engine.services.ssl.exception.SSLResourceAccessor;
import com.sap.engine.services.ssl.keystore.KeyStoreConnector;
import com.sap.tc.logging.Severity;
import com.sap.bc.proj.jstartup.JStartupFramework;

import java.io.*;

/**
 *  The server part of the SSL transport service is used for gateway to dispatcher
 * cluster elements.
 *
 * @author  Stephan Zlatarev
 * @version 6.30
 */
public class ServerService implements ApplicationServiceFrame {
  ApplicationServiceContext serviceContext = null;
  MessageListenerImpl icm_comunicator = null;


  public void start(ApplicationServiceContext serviceContext) throws ServiceException {
    SSLResourceAccessor.getResourceAccessor();

    try {
      File f = new File("ssl.txt");
      if (f.exists()) {
        f.delete();
      }
      printer = new PrintWriter(new FileOutputStream(f));
    } catch (Exception e) {
      SSLResourceAccessor.trace(Severity.ERROR, e.toString());
    }

    this.serviceContext = serviceContext;

    SAPStartupAccessor.setInstanceNumber(serviceContext.getCoreContext().getConfigurationHandlerFactory().getSystemProfile().getProperty("INSTANCE_NUMBER"));

    ClusterEventListenerImpl.setServiceState(serviceContext.getServiceState());
    int currentGroupID = ClusterEventListenerImpl.setClusterMonitor(serviceContext.getClusterContext().getClusterMonitor());
    int currentNodeID = serviceContext.getClusterContext().getClusterMonitor().getCurrentParticipant().getClusterId();

    dump("  current group: " + currentGroupID);
    dump("  current node : " + currentNodeID);

    LockManager.setCurrentGroupID(currentGroupID);
    LockManager.setLockContext(serviceContext.getCoreContext().getLockingContext());


    MessageListenerImpl.setCurrentGroupAndNodeID(currentGroupID, currentNodeID);
    MessageListenerImpl.setMessageContext(serviceContext.getClusterContext().getMessageContext());


    KeyStoreConnector.setCurrentGroupID(currentGroupID);

    boolean autoSynchPSE = true;

    /**
     *  ABAP or Java managed PSEs?
     *
     */
    String flag = JStartupFramework.getParam("ssl/pse_provider");
    dump("profile property \"ssl/pse_provider\" = [" + flag + "]");

    if (flag == null || flag.trim().equals("")) {
      flag = serviceContext.getServiceState().getProperty("AUTOMATIC_PSE_SYNCHRONIZATION");
      dump("Synch PSE flag[service.props]: [" + flag + "]");
      autoSynchPSE = (flag == null) || flag.equalsIgnoreCase("true");
    } else {
      autoSynchPSE = flag.equalsIgnoreCase("JAVA");
    }

    dump("autoSynchPSE = " + autoSynchPSE);

    try {
      KeyStoreConnector.start(autoSynchPSE);
    } catch (Exception e) {
     //$JL-EXC$
      dump(" KeyStoreConnector.start()",e);
    }

    RuntimeInterface runtimeAPI = new RuntimeInterfaceImpl();
    serviceContext.getServiceState().registerManagementInterface(runtimeAPI);
  }

  public void stop() throws ServiceRuntimeException {
    serviceContext.getServiceState().unregisterManagementInterface();
    KeyStoreConnector.stop();
  }


  static PrintWriter printer = null;
  public static final void dump(Object msg) {
    SSLResourceAccessor.trace(Severity.DEBUG , msg + "");
    if (printer == null) {
      return;
    }
    try {
      printer.write("\r\n: " + msg);
      printer.flush();

    } catch (Exception e) {
      //$JL-EXC$
    }
  }

  public static final void dump(Object msg, Throwable t) {
    SSLResourceAccessor.traceThrowable(Severity.DEBUG , msg + "", t);
    if (printer == null) {
      return;
    }
    try {
      printer.write("\r\n: " + msg + "\r\n");
      t.printStackTrace(printer);
      printer.flush();

    } catch (Exception e) {
      //$JL-EXC$
    }
  }
}