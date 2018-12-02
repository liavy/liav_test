/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server;

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.directory.InitialDirContext;
import javax.servlet.jsp.JspFactory;

import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.ApplicationServiceFrame;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.cluster.ClusterElement;
import com.sap.engine.frame.cluster.event.ServiceEventListener;
import com.sap.engine.frame.cluster.message.ListenerAlreadyRegisteredException;
import com.sap.engine.frame.cluster.message.MessageAnswer;
import com.sap.engine.frame.cluster.message.MessageListener;
import com.sap.engine.services.deploy.DeployService;
import com.sap.engine.services.deploy.container.op.util.StartUp;
import com.sap.engine.services.httpserver.chain.ChainComposer;
import com.sap.engine.services.httpserver.interfaces.HttpProvider;
import com.sap.engine.services.httpserver.interfaces.exceptions.HttpShmException;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.WebContainerInterface;
import com.sap.engine.services.servlets_jsp.chain.impl.ChainComposerImpl;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParserFactory;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParserFactoryImpl;
import com.sap.engine.services.servlets_jsp.jspparser_api.WebContainerParameters;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParserInitializationException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.GenerateJavaHelper;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.JspFactoryImpl;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.utils.NamingUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.ActionBase;
import com.sap.engine.services.servlets_jsp.server.deploy.WebContainerHelper;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebContainerServiceException;
import com.sap.engine.services.servlets_jsp.server.jsp.JSPProcessor;
import com.sap.engine.services.servlets_jsp.server.runtime.client.RequestContextObject;
import com.sap.tc.logging.Location;

/**
 * This class accomplishes the interaction between the Web container (servlet_jsp service) and
 * server and resources on the server. Service is started and stopped and registered
 * from ServletContext through this class.
 */
public class ServletsAndJspServerFrame implements ApplicationServiceFrame, ServiceEventListener, MessageListener {

  private static final int JSP_PARSER_VERSION = 1;

  private static Location currentLocation = Location.getLocation(ServletsAndJspServerFrame.class);
  private static Location traceLocation = null;
  private ServiceContext serviceContext = null;
  private WebContainerInterface runtimeInterface = null;
  private DeployService deploy = null;
  private WebContainerContainerEventListener webContainerContainerEventListener = null;
  private ChainComposer composer;

  /**
   * This is the first method invoked on this service when the service is started. The service is
   * initialized with references to server's properties and to ApplicationServiceContext.
   *
   * @param sc a reference to ApplicationServiceContext that gives to service a references to all
   *           other services and resources on the server
   * @throws ServiceException
   */
  public void start(ApplicationServiceContext sc) throws ServiceException {
    long time = System.currentTimeMillis();
    long newtime = time;
    long newtime1 = time;
    boolean beDebug = false;

    try {
      initLogging();
      traceLocation = LogContext.getLocationService();
      beDebug = traceLocation.beDebug();
      if (beDebug) {
        newtime = System.currentTimeMillis();
				traceLocation.debugT("initLogging() >>> " + (newtime - time));
			}

      initServiceContext(sc);
      if (beDebug) {
        newtime1 = System.currentTimeMillis();
				traceLocation.debugT("initServiceContext() >>> " + (newtime1 - newtime));
			}

      try {
        initParser();
      } catch (Exception e) {
    	  //TODO:Polly type:ok ?
        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000140", 
          "Cannot initialize JSP parser.", e, null, null);
      }
      if (beDebug) {
      newtime = System.currentTimeMillis();
				traceLocation.debugT("initParser() >>> " + (newtime - newtime1));
			}

      initNaming();
      if (beDebug) {
      newtime1 = System.currentTimeMillis();
				traceLocation.debugT("initNaming() >>> " + (newtime1 - newtime));
			}

      registerDeployCallbackListener();
      if (beDebug) {
      newtime = System.currentTimeMillis();
				traceLocation.debugT("registerDeployCallbackListener() >>> " + (newtime - newtime1));
			}

      serviceContext.getWebContainer().initialize(getRuntimeInterface(), sc.getCoreContext().getConfigurationHandlerFactory());
      if (beDebug) {
      newtime1 = System.currentTimeMillis();
				traceLocation.debugT("serviceContext.getWebContainer().initialize() >>> " + (newtime1 - newtime));
			}

      try {
        sc.getServiceState().registerServiceEventListener(this);
      } catch (ListenerAlreadyRegisteredException e) {
        throw new ServiceException(e);
      }
      if (beDebug) {
      newtime = System.currentTimeMillis();
				traceLocation.debugT("sc.getServiceState().registerServiceEventListener() >>> " + (newtime - newtime1));
			}

      try {
        sc.getClusterContext().getMessageContext().registerListener(this);
      } catch (Exception e) {
    	  //TODO:polly ok
        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logFatal(currentLocation, "ASJ.web.000312",
          "Cannot register servlet_jsp service as message listener in the message context.", e, null, null);
      }
      if (beDebug) {
      newtime1 = System.currentTimeMillis();
				traceLocation.debugT("sc.getClusterContext().getMessageContext().registerListener() >>> " + (newtime1 - newtime));
			}

      sc.getServiceState().registerManagementInterface(getRuntimeInterface());
      if (beDebug) {
      newtime = System.currentTimeMillis();
				traceLocation.debugT("sc.getServiceState().registerManagementInterface() >>> " + (newtime - newtime1));
			}

      sc.getContainerContext().getObjectRegistry().registerInterfaceProvider(J2EEComponents.INTERFACE_WEBCONTAINER_API, serviceContext.getWebContainer().getIWebContainerProvider());
      if (beDebug) {
      newtime1 = System.currentTimeMillis();
				traceLocation.debugT("sc.getContainerContext().getObjectRegistry().registerInterfaceProvider() >>> " + (newtime1 - newtime));
			}

      webContainerContainerEventListener = new WebContainerContainerEventListener(serviceContext);
      if (beDebug) {
      newtime = System.currentTimeMillis();
				traceLocation.debugT("new WebContainerContainerEventListener() >>> " + (newtime - newtime1));
			}

      webContainerContainerEventListener.register();
      if (beDebug) {
      newtime1 = System.currentTimeMillis();
				traceLocation.debugT("webContainerContainerEventListener.register() >>> " + (newtime1 - newtime));
			}

      sc.getServiceState().registerRuntimeConfiguration(serviceContext.getWebContainerProperties());
      if (beDebug) {
      newtime = System.currentTimeMillis();
				traceLocation.debugT("registerRuntimeConfiguration() >>> " + (newtime - newtime1));
			}

      JspFactory.setDefaultFactory(new JspFactoryImpl(serviceContext.getWebContainerProperties().getMinPoolSize(),
        serviceContext.getWebContainerProperties().getMaxPoolSize(),
        serviceContext.getWebContainerProperties().getDecreaseCapacityPoolSize()));
      if (beDebug) {
      newtime1 = System.currentTimeMillis();
				traceLocation.debugT("JspFactory.setDefaultFactory() >>> " + (newtime1 - newtime));
			}

      if (serviceContext.getDeployContext().getDeployCommunicator() != null) {
        try {
          String[] allMyApplications = serviceContext.getDeployContext().getDeployCommunicator().getMyApplications();
          serviceContext.getWebContainer().allApplicationsStartedCounter.incrementAndGet();
					serviceContext.getDeployContext().getDeployCommunicator().startMyApplications(allMyApplications);
          if (beDebug) {
          newtime = System.currentTimeMillis();
						traceLocation.debugT("startMyApplications() >>> " + (newtime - newtime1));
					}
          
          prepareAppMetaData(allMyApplications);
          if (beDebug) {
          newtime1 = System.currentTimeMillis();
						traceLocation.debugT("prepareAppMetaData() >>> " + (newtime1 - newtime));
					}
        } catch (RemoteException rex) {
          LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000224",
            "Cannot start applications.", rex, null, null);
        }
      }
      
      if (beDebug) {
      newtime = System.currentTimeMillis();
			}
      if (serviceContext.getHttpProvider() != null) {
        serviceContext.getWebContainer().checkApplicationAliasesInHttp();
      }
      if (beDebug) {
      newtime1 = System.currentTimeMillis();
				traceLocation.debugT("serviceContext.getWebContainer().checkApplicationAliasesInHttp() >>> " + (newtime1 - newtime));
			}

      RequestContextObject requestContextObject = new RequestContextObject(null);
      serviceContext.getThreadSystem().registerContextObject(RequestContextObject.NAME, requestContextObject);
      if (beDebug) {
      newtime = System.currentTimeMillis();
				traceLocation.debugT("serviceContext.getThreadSystem().registerContextObject() >>> " + (newtime - newtime1));
			}

      serviceContext.setServiceStarting(false);
      
      // Chain provider construction and registration
      composer = new ChainComposerImpl();
      composer.register();
      if (beDebug) {
      newtime1 = System.currentTimeMillis();
				traceLocation.debugT("Chain provider registration >>> " + (newtime1 - newtime));
			}
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (ServiceException e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logFatal(currentLocation, "ASJ.web.000313", 
        "Service Web Container cannot start.", e, null, null);
      stop();
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logFatal(currentLocation, "ASJ.web.000314", 
        "Service Web Container cannot start.", e, null, null);
      stop();
      throw new ServiceException(e);
    }

    if (beDebug) {
			traceLocation.debugT("Whole time >>> " + (System.currentTimeMillis() - time));
		}
  }

  /**
   * This is the last method invoked on a service before stopping of the service.
   */
  public void stop() {
    try {
      if (serviceContext.getDeployContext().getDeployCommunicator() != null) {
				serviceContext.getDeployContext().getDeployCommunicator().stopMyApplications(serviceContext.getDeployContext().getAllMyApplications());
			}
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
    	//TODO:Polly type ok
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000141",
        "Cannot stop deployed applications while trying to stop servlet_jsp service.", e, null, null);
      destroyAll();
    }
    if (composer != null) {
      // Unregisters chain composer
      composer.unregister();
    }    
    serviceContext.getWebContainer().destroyWebContainerExtensions();
    if (webContainerContainerEventListener != null) {
      webContainerContainerEventListener.unregister();
    }
    unregisterDeployCallbackListener();
    serviceContext.getApplicationServiceContext().getClusterContext().getMessageContext().unregisterListener();
    serviceContext.getApplicationServiceContext().getServiceState().unregisterServiceEventListener();
    serviceContext.getApplicationServiceContext().getContainerContext().getObjectRegistry().unregisterInterfaceProvider(J2EEComponents.INTERFACE_WEBCONTAINER_API);
    serviceContext.getApplicationServiceContext().getServiceState().unregisterContainerEventListener();
    serviceContext.getApplicationServiceContext().getServiceState().unregisterManagementInterface();
    serviceContext.getApplicationServiceContext().getServiceState().unregisterRuntimeConfiguration();
    
    serviceContext.getThreadSystem().unregisterContextObject(RequestContextObject.NAME);
    RequestContextObject.reset();
    destroyNaming();
  }

  private WebContainerInterface getRuntimeInterface() {
    if (runtimeInterface == null) {
      try {
        runtimeInterface = new WebContainerInterfaceImpl();
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logFatal(currentLocation, "ASJ.web.000315",
          "Cannot get runtime interface of the service.", e, null, null);
      }
    }
    return runtimeInterface;
  }

  //////// ServiceEventListener interface methods

  /**
   * This method is invoked by the <code>MSConnection</code> in which this
   * listener is registered, when the monitored service on the specified
   * cluster node is started.
   *
   * @param newElement the node on which the monitored service is started.
   */
  public void serviceStarted(ClusterElement newElement) {
  }

  /**
   * This method is invoked by the <code>MSConnection</code> in which this
   * listener is registered, when the monitored service on the specified
   * cluster node is stopped.
   *
   * @param element the node on which the monitored service is stopped.
   */
  public void serviceStopped(ClusterElement element) {
  }

  // This is empty implementation. It is needed in order method service started to be called.
  public void receive(int clusterId, int messageType, byte[] body, int offset, int length) {
  }

  // This is empty implementation. It is needed in order method service started to be called.
  public MessageAnswer receiveWait(int clusterId, int messageType, byte[] body, int offset, int length) {
    try {
      switch (messageType) {
        case 1: {
          String msg = new String(body, offset, length);
          int internalOffset = msg.indexOf('/');
          if (internalOffset == -1) {
        	String message = new String(body, offset, length);  
            LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000142", 
              "Unknown message received from server [{0}], the message is " +
              "[{1}]. Missing symbol [/].", new Object[]{clusterId, message}, null, null);
            return new MessageAnswer(("Unknown message received form server [" + clusterId + "], the message is ["
              + new String(body, offset, length) + "]. Missing symbol [/].").getBytes());
          }
          MessageBytes alias = new MessageBytes(body, offset, internalOffset);
          String session = msg.substring(internalOffset + 1);
          ApplicationContext applicationContext = serviceContext.getDeployContext().getStartedWebApplicationContext(alias);
          if (applicationContext == null) {
            if (LogContext.getLocationRequestInfoServer().beInfo()) {
            	LogContext.getLocationRequestInfoServer().infoT(
									"Invalidation request received from server [" + clusterId + "] for session [" + 
									session + "] in application [" + alias + "]. The application is stopped.");
						}
            return new MessageAnswer(("Invalidation request received from server ["
              + clusterId + "] for session [" + session + "] in application [" + alias + "]. The application is stopped.").getBytes());
          }
          boolean result = applicationContext.invalidateSession(session);
          if (LogContext.getLocationRequestInfoServer().beInfo()) {
          	LogContext.getLocationRequestInfoServer().infoT(
								"Invalidation request received from server [" + clusterId + "] for session [" + 
								session + "] in application [" + alias + "]. Session invalidated: [" + result + "]");
					}
          return new MessageAnswer(("Invalidation request received from server ["
            + clusterId + "] for session [" + session + "] in application [" + alias + "]. Session invalidated: [" + result + "]").getBytes());
        }
        default: {
          String message =  new String(body, offset, length); 	
          LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000143",
            "Unknown message type received: [{0}] from server [{1}], the message is " +
            "[{2}].", new Object[]{messageType, clusterId, message}, null, null);
          return new MessageAnswer(("Unknown message type received: [" + messageType + "] form server [" + clusterId + "], the message is ["
            + new String(body, offset, length) + "].").getBytes());
        }
      }
    } catch (Exception e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000144",
        "Error in parsing message sent by server [{0}], message is " +
        "[{1}], offset is [{2}], length is [{3}].", new Object[]{clusterId, body, offset, length}, e, null, null);
      return new MessageAnswer(("Error in parsing message sent by server [" + clusterId + "], message is ["
        + body + "], offset is [" + offset + "], length is [" + length + "].").getBytes());
    }
  }

  /**
   * Invokes destroy method on all servlets in the container.
   */
  private void destroyAll() {
    Enumeration en = serviceContext.getDeployContext().getStartedWebApplications();
    if (en != null) {
      while (en.hasMoreElements()) {
        ActionBase.destroyWebAppComponents((ApplicationContext) en.nextElement());
      }
    }
  }

  private void initLogging() {
    LogContext.init();
    new com.sap.engine.services.servlets_jsp.server.exceptions.WebResourceAccessor().init(Location.getLocation("com.sap.engine.services.WebContainer"));
    new com.sap.engine.services.servlets_jsp.jspparser_api.exception.WebResourceAccessor().init(Location.getLocation("com.sap.engine.services.WebContainer"));
    new com.sap.engine.services.servlets_jsp.lib.jspruntime.exceptions.WebResourceAccessor().init(Location.getLocation("com.sap.engine.services.WebContainer"));
  }

  private void initNaming() {
    Properties p = new Properties();
    p.put(Context.INITIAL_CONTEXT_FACTORY, "com.sap.engine.services.jndi.InitialContextFactoryImpl");
    p.put("Replicate", "false");
    p.put("domain", "true");
    try {
      Context ctx = new InitialDirContext(p);
      ctx.createSubcontext("webContainer");
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logFatal(currentLocation, "ASJ.web.000316",
        "Cannot create a naming context for the Web Container.", e, null, null);
    }
  }
  
  private void destroyNaming() {
    Properties p = new Properties();
    p.put(Context.INITIAL_CONTEXT_FACTORY, "com.sap.engine.services.jndi.InitialContextFactoryImpl");
    p.put("Replicate", "false");
    p.put("domain", "true");
    try {
      Context ctx = new InitialDirContext(p);
      Context mainContext = (Context) ctx.lookup("webContainer");
      NamingUtils.destroyContext(mainContext);
      ctx.destroySubcontext("webContainer");
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000627",
        "Cannot destroy a naming context for the Web Container.", e, null, null);
    }    
  }//end of destroyNaming()

  private void registerDeployCallbackListener() {
    try {
      Context ctx = new InitialDirContext();
      deploy = (DeployService) ctx.lookup(J2EEComponents.SERVICE_DEPLOY);
    } catch (Exception e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000317",
        "Cannot lookup Deploy service.", e, null, null); 
    }
    if (deploy == null) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000145",
        "ERROR: Deploy service interface not found in naming.", null, null);
    } else {
      try {
        deploy.registerDeployCallback(serviceContext.getWebContainer(), new String[]{
          serviceContext.getApplicationServiceContext().getClusterContext().getClusterMonitor().getCurrentParticipant().getName()});
      } catch (Exception e) {
    	  //TODO:Polly ok
        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logFatal(currentLocation, "ASJ.web.000318",
          "Cannot register the deploy callback listener.", e, null, null);
      }
    }
  }

  private void unregisterDeployCallbackListener() {
    try {
      Context ctx = new InitialDirContext();
      deploy = (DeployService) ctx.lookup(J2EEComponents.SERVICE_DEPLOY);
    } catch (Exception e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000146",
        "Cannot lookup Deploy service.", e, null, null);
    }
    if (deploy == null) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000147",
        "ERROR: Deploy service interface not found in naming.", null, null);
    } else {
      try {
        deploy.unregisterDeployCallback(serviceContext.getWebContainer(), new String[]{
          serviceContext.getApplicationServiceContext().getClusterContext().getClusterMonitor().getCurrentParticipant().getName()});
      } catch (Exception e) {
        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000148",
          "Cannot unregister deploy callback listener.", e, null, null);
      }
    }
  }

  private void initServiceContext(ApplicationServiceContext applicationServiceContext) throws ServiceException {
    try {
      serviceContext = new ServiceContext(applicationServiceContext);
    } catch (IOException io) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logFatal(currentLocation, "ASJ.web.000319",
        "Cannot initialize Web Container service context. Service cannot start.", null, null);
      throw new WebContainerServiceException("Cannot init web container service context.", io);
    }
  }

  private void initParser() throws JspParserInitializationException {
    WebContainerParameters containerParameters = ((JspParserFactoryImpl) JspParserFactory.getInstance()).getContainerProperties();
    containerParameters.setJspDebugSupport(serviceContext.getWebContainerProperties().jspDebugSupport());
    containerParameters.setExternalCompiler(serviceContext.getWebContainerProperties().getExternalCompiler());
    containerParameters.setInternalCompiler(serviceContext.getWebContainerProperties().internalCompiler());
    containerParameters.setServerID(serviceContext.getServerId());
    containerParameters.setServiceClassLoader(ServiceContext.getServiceContext().getServiceLoader());
    containerParameters.setJavaEncoding(serviceContext.getWebContainerProperties().javaEncoding());
    containerParameters.setExtendedJspImports(serviceContext.getWebContainerProperties().isExtendedJspImports());
    containerParameters.setProductionMode(serviceContext.getWebContainerProperties().isProductionMode());

    // Elements[] is null because we use our own implementation of the handlers
    // Parseable is null, because default implementation will be used
    JspParserFactory.getInstance().registerParserInstance(JSPProcessor.PARSER_NAME, null, new GenerateJavaHelper());
  }

  private void prepareAppMetaData(String[] allMyApplications) {
    if (allMyApplications != null && allMyApplications.length > 0) {
      for (int i = 0; i < allMyApplications.length; i++) {
        String applicationName = allMyApplications[i];
       
       	long newtime = System.currentTimeMillis();
        ServiceContext.getServiceContext().getWebContainer().prepareApplicationInfo(applicationName, true);
        if (traceLocation.beDebug()) {
        	long newtime1 = System.currentTimeMillis();
					traceLocation.debugT("prepareAppMetaData/prepareApplicationInfo(" + applicationName + ") >>> " + (newtime1 - newtime));
				}
      }
    }
  }//end of prepareAppMetaData()

}
