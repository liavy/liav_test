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
package com.sap.engine.services.ssl.dispatcher;

import com.sap.engine.services.ssl.dispatcher.command.CommandsContext;
import com.sap.engine.services.ssl.factory.SSLTransportFactory;
import com.sap.engine.services.ssl.exception.SSLResourceAccessor;
import com.sap.engine.services.timeout.TimeoutManager;
import com.sap.engine.frame.CommunicationServiceFrame;
import com.sap.engine.frame.CommunicationServiceContext;
import com.sap.engine.frame.ServiceRuntimeException;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.container.event.ContainerEventListener;
import com.sap.engine.frame.state.PersistentContainer;
import com.sap.engine.frame.cluster.transport.TransportContext;
import com.sap.engine.frame.cluster.CommunicationClusterContext;
import com.sap.tc.logging.Severity;


import java.util.HashSet;
import javax.crypto.Cipher;

/**
 *  The dispatcher service for SSL transport service.
 *
 * @author  Stephan Zlatarev
 * @version 4.0.2
 */
public class DispatcherService implements CommunicationServiceFrame {

  private static CommunicationServiceContext serviceContext = null;
  private CommunicationClusterContext clusterContext = null;
  private TransportContext transportContext = null;
  private RuntimeInterfaceImpl runtimeInterface = null;
  private KeystoreListenerImpl keystoreListener = null;
  private DispatcherContainerEventListener containerEventListener = null;
  private TimeoutManager timeout = null;

  public static int RESUME_SESSION_PERIOD = 2 * 60 * 60;
  public static int SESSION_CACHE_SIZE    = 1000;

  public static String RESUME_SESSION_PROPERTY = "RESUME_SESSION_PERIOD";
  public static String SESSION_CACHE_PROPERTY  = "SESSION_CACHE_SIZE";

  private static final String NO_FULL_IAIK_JCE="ssl_disp_no_full_iaik_jce";
  private static final String NO_KEYSTORE="ssl_disp_no_running_keystore";
  private static final String UNEXPECTED_SERVICE_EXCEPTION="ssl_disp_unexpected_service_exception";


  public void start(CommunicationServiceContext sdc) throws ServiceException {
    try {
      SSLResourceAccessor.getResourceAccessor();

      try {
        Cipher.getInstance("RSA");
      } catch (Exception e) {
        SSLResourceAccessor.log(Severity.FATAL, e, NO_FULL_IAIK_JCE);
        SSLResourceAccessor.traceThrowable(Severity.FATAL, SSLResourceAccessor.getDescription(NO_FULL_IAIK_JCE, null), e);
        throw new ServiceException(SSLResourceAccessor.getLocalizableMessage(NO_FULL_IAIK_JCE, null));
      }

      serviceContext = sdc;
      RESUME_SESSION_PERIOD = Integer.parseInt(sdc.getServiceState().getProperty(RESUME_SESSION_PROPERTY, new Integer(2 * 60 * 60).toString()));
      SESSION_CACHE_SIZE    = Integer.parseInt(sdc.getServiceState().getProperty(SESSION_CACHE_PROPERTY, new Integer(1000).toString()));
      timeout = (TimeoutManager) serviceContext.getContainerContext().getObjectRegistry().getServiceInterface("timeout");
      timeout.registerTimeoutListener(com.sap.engine.services.ssl.factory.session.JSSELimitedCache.initialize(SESSION_CACHE_SIZE, RESUME_SESSION_PERIOD), (long) 5 * 60 * 1000, (long) 5 * 60 * 1000);

      clusterContext = sdc.getClusterContext();
      transportContext = clusterContext.getTransportContext();

      if (sdc.getContainerContext().getObjectRegistry().getServiceInterface("keystore") == null) {
        SSLResourceAccessor.log(Severity.FATAL, "", NO_KEYSTORE);
        throw new ServiceException(SSLResourceAccessor.getLocalizableMessage(NO_KEYSTORE, null));
      }


      runtimeInterface = new RuntimeInterfaceImpl();

      containerEventListener = new DispatcherContainerEventListener(new CommandsContext(runtimeInterface).getAvailableCommands(), sdc.getContainerContext().getObjectRegistry());

      int mask = ContainerEventListener.MASK_INTERFACE_AVAILABLE | ContainerEventListener.MASK_INTERFACE_NOT_AVAILABLE;
      HashSet names = new HashSet(1);
      names.add("shell");
      sdc.getServiceState().registerContainerEventListener(mask, names, containerEventListener);

      serviceContext.getServiceState().registerManagementInterface(runtimeInterface);
      keystoreListener = new KeystoreListenerImpl(transportContext, sdc.getCoreContext().getThreadSystem());
      keystoreListener.keystoreIsAvailable();
    } catch (ServiceException se) {
      throw se;
    } catch (Exception e) {
      SSLResourceAccessor.log(Severity.FATAL, e,UNEXPECTED_SERVICE_EXCEPTION);
      SSLResourceAccessor.traceThrowable(Severity.FATAL, SSLResourceAccessor.getMessage(UNEXPECTED_SERVICE_EXCEPTION), e);
      stop();
      throw new ServiceException(UNEXPECTED_SERVICE_EXCEPTION, null, e);
    }
  }


  public void stop() throws ServiceRuntimeException {
    timeout.unregisterTimeoutListener(com.sap.engine.services.ssl.factory.session.JSSELimitedCache.getInstance());
    com.sap.engine.services.ssl.factory.session.JSSELimitedCache.stop();
    containerEventListener.stop();
    serviceContext.getServiceState().unregisterContainerEventListener();
    SSLTransportFactory.stop();
    keystoreListener.stop();
  }


  public static PersistentContainer getPersistentContainer() {
    return serviceContext.getServiceState().getPersistentContainer();
  }


  public static CommunicationServiceContext getServiceContext() {
    return serviceContext;
  }

}

