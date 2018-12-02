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

package com.sap.engine.services.webservices.server;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.ApplicationServiceFrame;
import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.ServiceRuntimeException;
import com.sap.engine.frame.container.event.ContainerEventListener;
import com.sap.engine.frame.container.monitor.ServiceMonitor;
import com.sap.engine.frame.container.runtime.RuntimeConfiguration;
import com.sap.engine.frame.core.configuration.ConfigurationException;
import com.sap.engine.frame.core.configuration.ConfigurationHandler;
import com.sap.engine.frame.core.configuration.ConfigurationHandlerFactory;
import com.sap.engine.frame.core.thread.ThreadSystem;
import com.sap.engine.interfaces.connector.ComponentExecutionContext;
import com.sap.engine.interfaces.ejb.monitor.EJBManager;
import com.sap.engine.interfaces.sca.SCAEnvironment;
import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.interfaces.security.userstore.UserStore;
import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.interfaces.shell.ShellInterface;
import com.sap.engine.interfaces.webservices.dynamic.DynamicServiceFactory;
import com.sap.engine.interfaces.webservices.esp.ProviderProtocol;
import com.sap.engine.interfaces.webservices.runtime.ServletDispatcher;
import com.sap.engine.interfaces.webservices.runtime.ServletsHelper;
import com.sap.engine.interfaces.webservices.runtime.component.BaseRegistryException;
import com.sap.engine.interfaces.webservices.server.WSContainerInterface;
import com.sap.engine.interfaces.webservices.server.WebServiceInterface;
import com.sap.engine.interfaces.webservices.uddi.UDDIServerAdmin;
import com.sap.engine.services.deploy.container.ContainerManagement;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.licensing.LicensingRuntimeInterface;
import com.sap.engine.services.log_configurator.admin.LogConfiguratorManagementInterface;
import com.sap.engine.services.sca.plugins.ws.WebServiceImplementationContainer;
import com.sap.engine.services.sca.plugins.ws.WebServicePluginConstants;
import com.sap.engine.services.sca.plugins.ws.WebServicePluginFrame;
import com.sap.engine.services.servlets_jsp.webcontainer_api.IWebContainerProvider;
import com.sap.engine.services.servlets_jsp.webcontainer_api.exceptions.WebContainerExtensionDeploymentException;
import com.sap.engine.services.timeout.TimeoutManager;
import com.sap.engine.services.webservices.additions.client.dsr.DSRConsumerProtocol;
import com.sap.engine.services.webservices.additions.client.metering.MeteringProtocol;
import com.sap.engine.services.webservices.additions.client.metering.MeteringTimeoutListener;
import com.sap.engine.services.webservices.additions.server.dsr.DSRProviderProtocol;
import com.sap.engine.services.webservices.espbase.WSLogTrace;
import com.sap.engine.services.webservices.espbase.client.bindings.ConsumerProtocolFactory;
import com.sap.engine.services.webservices.espbase.client.dynamic.GenericServiceFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationBuilder;
import com.sap.engine.services.webservices.espbase.server.ContainerEnvironmentHolder;
import com.sap.engine.services.webservices.espbase.server.runtime.ApplicationWebServiceContextImpl;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.espbase.server.runtime.metering.ServiceMeter;
import com.sap.engine.services.webservices.espbase.server.runtime.sec.WSDLSecurityProcessor;
import com.sap.engine.services.webservices.espbase.xi.XIFrameworkConstants;
import com.sap.engine.services.webservices.espbase.xi.impl.ESPXIMessageProcessorImpl;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.jaxm.soap.HTTPSocket;
import com.sap.engine.services.webservices.logtrace.ClusterCommunicationHelper;
import com.sap.engine.services.webservices.runtime.monitor.WSMonitor;
import com.sap.engine.services.webservices.runtime.servlet.ServletDispatcherImpl;
import com.sap.engine.services.webservices.runtime.servlet.ServletsHelperImpl;
import com.sap.engine.services.webservices.server.command.ClearWSDLCacheCommand;
import com.sap.engine.services.webservices.server.command.ListWebServicesCommand;
import com.sap.engine.services.webservices.server.deploy.WebServicesDeployManager;
import com.sap.engine.services.webservices.server.deploy.migration.ws.WebServicesMigrator;
import com.sap.engine.services.webservices.server.dynamic.DynamicServiceFactoryImpl;
import com.sap.engine.services.webservices.server.dynamic.J2EEEngineHelperImpl;
import com.sap.engine.services.webservices.server.wcextension.WebContainerExtensionImpl;
import com.sap.engine.services.webservices.tools.SAPTransportImpl;
import com.sap.engine.services.webservices.uddi.UDDIUserStoreConfig;
import com.sap.engine.services.webservices.webservices630.server.command.ListAdditionalComponentsCommand;
import com.sap.engine.services.webservices.webservices630.server.command.ListTransportAddressesCommand;
import com.sap.engine.services.webservices.webservices630.server.command.ListWSClientsCommand;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WebServicesConstants;
import com.sap.engine.services.webservices.webservices630.server.deploy.wsclient.WSClientsConstants;
import com.sap.tc.logging.Location;

/**
 * Title: WebServicesFrame
 * Description: WebServicesFrame
 * 
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 */

public class WebServicesFrame implements ApplicationServiceFrame, ContainerEventListener {

  public static final Location LOCATION = Location.getLocation(WSLogging.SERVER_LOCATION);
 
  public static final String WS_CONTEXT_NAME = "wsContext";
//  public static final String JMX_INTERFACE = "jmx";

  private ApplicationServiceContext serviceContext; 
  public static WebServicesDeployManager webServicesDeployManager;    
  private ServletDispatcherImpl servletDispatcher;
  private ApplicationWebServiceContextImpl applicationWSContextImpl;
  private MeteringTimeoutListener meteringTimeoutListener;
  
  private int wsCommandId = 0;
  private ClusterCommunicationHelper helper;
  private WebServicePluginFrame scaPlugin;

  // WCE
  private static WebContainerExtensionImpl webContainerExtension = null;
  private static IWebContainerProvider webContainerProvider = null;
  
  private static final String WEB_CONTAINER_API = "tc~je~webcontainer~api";
  private static final String WCE_PROVIDER_DESCRIPTOR_NAME = "web.xml";
  private static final String WCE_PROVIDER_NAME = "webservices_wce";

  
  public static WebContainerExtensionImpl getWebContainerExtension(){
    return webContainerExtension;
  }
  
  public void start(ApplicationServiceContext serviceContext) throws ServiceException {
    setServiceProperty("wsclients.dispatcher", serviceContext.getServiceState().getProperty("wsclients.dispatcher", "false"));
    this.serviceContext = serviceContext;

    initWSContainer();

    initWSJndiContexts();
    
    ContainerEnvironmentHolder.setContainerEnvironment(WebServicesContainer.getServiceContext());
    
    serviceContext.getContainerContext().getObjectRegistry().registerInterfaceProvider(WebServiceInterface.NAME, new WebServiceInterfaceImpl());
    registerRuntimeConfiguration();
    
    GenericServiceFactory.engineHelper = new J2EEEngineHelperImpl();

    //helper = new ClusterCommunicationHelper(serviceContext.getClusterContext().getMessageContext());
      registerContainerEventListener();
    
    // WCE
    webContainerExtension = new WebContainerExtensionImpl();
    webContainerProvider = (IWebContainerProvider) serviceContext.getContainerContext().getObjectRegistry().getProvidedInterface(WEB_CONTAINER_API);
    if (webContainerProvider != null) {
      try {
        webContainerProvider.registerWebContainerExtension(webContainerExtension, WCE_PROVIDER_NAME, WCE_PROVIDER_DESCRIPTOR_NAME);
      } catch (WebContainerExtensionDeploymentException e) {
        throw new ServiceException(e);
      }
    } else {
      throw new ServiceException("Cannot obtain Web Container API.");
    }
    
  }

  public void stop() throws ServiceRuntimeException {
    HTTPSocket.onServer = false;


    stopMyApplications();

    WSMonitor.clear();

    destroyWSJndiContexts();

    if (serviceContext.getContainerContext().getSystemMonitor().getInterface("container").getStatus() == ServiceMonitor.STATUS_ACTIVE) {
      ContainerManagement containerManagement = (ContainerManagement)serviceContext.getContainerContext().getObjectRegistry().getProvidedInterface("container");
      containerManagement.unregisterContainer(WebServicesConstants.WS_CONTAINER_NAME);
    }
    
    try {
	    if (scaPlugin != null) {
	      scaPlugin.stop();
	      scaPlugin = null;
	      LOCATION.debugT("Finished stopping the SCA Plug-in.");
	    }
	    
	    // Unregister SCA WebServiceImplementationConteiner.
	    ((WebServiceInterfaceImpl) serviceContext.getContainerContext().getObjectRegistry().getProvidedInterface(WebServiceInterface.NAME)).unregisterImplementationContainerInterface(WebServicePluginConstants.SCA_IMPL_CONTAINER_NAME);    
    } catch (Exception e) {
    	// $JL-EXC$
	}

    serviceContext.getContainerContext().getObjectRegistry().unregisterInterfaceProvider(WebServiceInterface.NAME);
    serviceContext.getServiceState().unregisterManagementInterface();
    serviceContext.getServiceState().unregisterContainerEventListener();
    serviceContext.getServiceState().unregisterRuntimeConfiguration();

    if (serviceContext.getContainerContext().getSystemMonitor().getInterface("shell").getStatus() == ServiceMonitor.STARTUP_STATE_STARTED) {
      ShellInterface shellInterface = (ShellInterface)serviceContext.getContainerContext().getObjectRegistry().getProvidedInterface("shell");
      shellInterface.unregisterCommands(wsCommandId);
    }
    
    if (helper != null) {
      helper.invalidate();
      helper = null;
    }
    MeteringProtocol.getInstance().unregisterProtocol();
    if (meteringTimeoutListener != null){
      meteringTimeoutListener.unregister();
    }
   
    // WCE
    if (webContainerProvider != null) {
      webContainerProvider.unregisterWebContainerExtension(WCE_PROVIDER_NAME);
      webContainerProvider = null;
    }
    if (webContainerExtension != null) {
      webContainerExtension = null;
    }
  }

  public void containerStarted() {
  
  }

  public void beginContainerStop() {

  }

  public void serviceStarted(String serviceName) {
  
  }

  public void serviceNotStarted(String serviceName) {

  }

  public void beginServiceStop(String serviceName) {

  }

  public void serviceStopped(String serviceName) {
    if (serviceName.equals("monitor")) {
      WSContainer.setMonitorServiceStarted(false);
      WSMonitor.clear();
    }else if ("licensing".equals(serviceName)) {
      LOCATION.pathT("Received service stopped event for licensing service");
      MeteringProtocol.getInstance().removeLicensingInterface();
    }
  }

  public void interfaceAvailable(String interfaceName, Object interfaceImpl) {    
    if (interfaceName.equals("container")) {
      containerInterfaceAvailable((ContainerManagement)interfaceImpl);
    } else if (interfaceName.equals("ejbmonitor")) {
      WSContainer.getInterfaceContext().setEjbManager((EJBManager)interfaceImpl);
    } else if (interfaceName.equals("shell")) {
      ShellInterface shellInterface = (ShellInterface)interfaceImpl;
      wsCommandId = shellInterface.registerCommands(getWSCommands());
    } else if (interfaceName.equals("security")) {
      securityInterfaceAvailable((SecurityContext)interfaceImpl);
    }else if (interfaceName.equals("appcontext")){
      LOCATION.pathT("Received interface available event for appcontext interface name");
      ComponentExecutionContext cec = (ComponentExecutionContext) interfaceImpl;
      MeteringProtocol.getInstance().setAppContextInterface(cec);
    }else if (interfaceName.equals(WebServicePluginConstants.SCA_SPI_NAME)){
      LOCATION.pathT("Received interface available event for tc~je~sca~spi interface name");
      //get ESB tracer impl and give it to webservices lib
      SCAEnvironment scaEnvironment = (SCAEnvironment) interfaceImpl;
      WSLogTrace.setESBTracer(scaEnvironment.getESBTracer());
      //start ws plugin
      scaPlugin = new WebServicePluginFrame();
      try {
        scaPlugin.start(this.serviceContext);
      } catch (ServiceException e) {
        LOCATION.debugT("Error starting the sca plugin");
      }
    }else if (interfaceName.equals(WebServicePluginConstants.WEBSERVICE_CONTAINER_NAME)){
      try {
        if (interfaceName.equals(WebServicePluginConstants.WEBSERVICE_CONTAINER_NAME)) {
          if (interfaceImpl instanceof WebServiceInterfaceImpl)
            ((WebServiceInterfaceImpl) interfaceImpl).registerImplementationContainerInterface(new WebServiceImplementationContainer());
        }
      } catch (BaseRegistryException bre) {
        throw new RuntimeException("Exception in WebServiceServiceFrame.interfaceAvailable method", bre);
      }    
    }
  }

  public void interfaceNotAvailable(String interfaceName) {
    if (interfaceName.equals("appcontext")){
      LOCATION.pathT("Received interface not available event for appcontext interface name");
      MeteringProtocol.getInstance().removeAppContextInterface();
    }
  }

  public void serviceInterfaceRegistered(String serviceName, Object serviceInterface) {
  
  }

  public void markForShutdown(long timeout) {

  }

  public void serviceInterfaceUnregistered(String serviceName) {
  
  }

  // ContainerEventListener.setServiceProperty is deprecated, using the recommended API  
  private void registerRuntimeConfiguration() {
	serviceContext.getServiceState().unregisterRuntimeConfiguration();
    serviceContext.getServiceState().registerRuntimeConfiguration(new RuntimeConfiguration() {
      @Override
      public void updateProperties(Properties properties) throws ServiceException {
        WebServicesFrame.this.updateProperties(properties);
      }
    });
    updateProperties(serviceContext.getServiceState().getProperties());
  }
  
  public synchronized void updateProperties(Properties properties){      
    for (Map.Entry<Object, Object> e: properties.entrySet()) {
      String key = (String)e.getKey();
      // Update the wsdl security if the property exists.
      if (WSDLSecurityProcessor.WSDL_SECURITY_PROPERTY.equals(key)){
        WSDLSecurityProcessor.setSecureWsdl((String)e.getValue());        
      }
    }
  }
    
  public boolean setServiceProperty(String key, String value) {
    if ("wsclients.dispatcher".equals(key)) {
      if (value.equalsIgnoreCase("false")) {
        HTTPSocket.onServer = false;
      } else {
        HTTPSocket.onServer = true;
      }
    }
    return false;
  }

  public boolean setServiceProperties(Properties serviceProperties) {
    Enumeration keys = serviceProperties.keys();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement().toString();
      String value = serviceProperties.getProperty(key);
      setServiceProperty(key, value);
    }
    return false;
  }

  public void serviceStarted(String serviceName, Object serviceInterface) {
    if (serviceName.equals("log_configurator")) {
      WSContainer.setLogConfiguratorInterface((LogConfiguratorManagementInterface)serviceContext.getContainerContext().getObjectRegistry().getServiceInterface(serviceName));
    }else if (serviceName.equals("timeout")) {
      TimeoutManager timeMan = (TimeoutManager)serviceInterface;
      //WSContainer.getImplementationContainerManager().registerTimeoutListeners((TimeoutManager) serviceInterface);
      //ServiceMeter.getInstance().setTimeoutManager((TimeoutManager)serviceInterface);

      if (meteringTimeoutListener == null){
        meteringTimeoutListener = new MeteringTimeoutListener(ServiceMeter.getInstance(), timeMan);
      }
      meteringTimeoutListener.register();
    
    } else if (serviceName.equals("monitor")) {
      WSContainer.setMonitorServiceStarted(true);
    } else if ("licensing".equals(serviceName)) {
      LOCATION.pathT("Received service started event for licensing service");
      LicensingRuntimeInterface lri = (LicensingRuntimeInterface)serviceInterface;
      MeteringProtocol.getInstance().setLicensingInterface(lri);
      ServiceMeter.getInstance().setInstalationNumber(lri.getInstNo());
      ServiceMeter.getInstance().setSystemID(lri.getSystemId());
    }
  }
  
  private void initWSContainer() {
    WSContainer.init(serviceContext);    
  }

  private void initWSJndiContexts() throws ServiceException {
    try {
      Context ctx = new InitialContext();     
      try {
    	Context wsContext = null;
    	try {
    	  wsContext = (Context)ctx.lookup(WS_CONTEXT_NAME); 	
    	} catch(NameNotFoundException e) {
          // $JL-EXC$	
    	}
    	
    	if(wsContext != null) {
          WSUtil.destroyContext(ctx, WS_CONTEXT_NAME);
    	}
      } catch(Exception e) {
        // $JL-EXC$  	  
      }                       
      
      try {
        Context wsClientsContext = null;
        try {
       	  wsClientsContext = (Context)ctx.lookup(WSClientsConstants.WS_CLIENTS_CONTEXT); 	
       	} catch(NameNotFoundException e) {
          // $JL-EXC$	
       	}
       	
       	if(wsClientsContext != null) {
          WSUtil.destroyContext(ctx, WSClientsConstants.WS_CLIENTS_CONTEXT);
       	}
      } catch(Exception e) {
        // $JL-EXC$  	  
      }
      
      Context wsClientsContext = ctx.createSubcontext(WSClientsConstants.WS_CLIENTS_CONTEXT);
      bindGenericServiceFactoryWithCache(wsClientsContext);
      wsClientsContext.createSubcontext(WSClientsConstants.WS_CLIENTS_PROXY_REL_CONTEXT);
      wsClientsContext.rebind("/WSClientsManipulator", WebServicesContainer.getWSClientsContainerManipulator());
      
      initXIFrameworkContexts(ctx);      
      
      Context wsContext = ctx.createSubcontext(WS_CONTEXT_NAME);
      UDDIServerAdmin uddiServerAdmin = WSContainer.getUddiServerAdmin();
      SAPTransportImpl sapTransport = new SAPTransportImpl(WSContainer.getHTTPProxyResolver(), uddiServerAdmin, new DispatcherPortsGetterImpl());
      wsContext.rebind("/" + SAPTransportImpl.NAME, sapTransport);
      wsContext.rebind("/" + ServletsHelper.NAME, new ServletsHelperImpl());
      wsContext.rebind("/" + DynamicServiceFactory.NAME, new DynamicServiceFactoryImpl());
      wsContext.rebind("/" + UDDIServerAdmin.JNDI_NAME, uddiServerAdmin);
      wsContext.rebind("/" + WSContainerInterface.NAME, new WSContainerInterfaceImpl());
      wsContext.rebind("/WSContainerManipulator", WebServicesContainer.getWSContainerManipulator()); 
      ThreadSystem threadSystem = serviceContext.getCoreContext().getThreadSystem();
      applicationWSContextImpl = ApplicationWebServiceContextImpl.initializeApplicationContext(threadSystem);
      wsContext.rebind("/" + ApplicationWebServiceContextImpl.APPLICATION_WSCONTEXT, applicationWSContextImpl);

      ServiceMeter.initState(serviceContext);
      ServiceMeter sMeter = ServiceMeter.getInstance();
      RuntimeProcessingEnvironment runtimeProcessingEnvironment = new RuntimeProcessingEnvironment(serviceContext, applicationWSContextImpl, WSContainer.getImplementationContainerManager(), WebServicesContainer.getServiceContext(), sMeter);
      initializeCFGBuilder(runtimeProcessingEnvironment.getWSDLVisualizer().getInternalCFGBuilder());
      wsContext.rebind("/" + RuntimeProcessingEnvironment.JNDI_NAME_OLD, runtimeProcessingEnvironment); //for backwards compatibility
      wsContext.rebind("/" + RuntimeProcessingEnvironment.JNDI_NAME, runtimeProcessingEnvironment);
      WSContainer.setRuntimeProcessingEnv(runtimeProcessingEnvironment);
      
      servletDispatcher = new ServletDispatcherImpl(runtimeProcessingEnvironment);
      wsContext.rebind("/" + ServletDispatcher.NAME, servletDispatcher);
      
      helper = new ClusterCommunicationHelper(serviceContext.getClusterContext().getMessageContext(), sMeter);
      wsContext.rebind("/" + sMeter.JNDI_NAME, sMeter);
      
      runtimeProcessingEnvironment.registerProviderProtocol(new DSRProviderProtocol());
      ConsumerProtocolFactory.protocolFactory.registerProtocol(DSRConsumerProtocol.PROTOCOL, 
				new DSRConsumerProtocol());
		
      
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      wsLocation.catching(e);

      throw new ServiceException(e);
    }
  }
  
  private void bindGenericServiceFactoryWithCache(Context ctx) throws NamingException {
    GenericServiceFactory factory = GenericServiceFactory.newInstance(true);    
    ctx.rebind(GenericServiceFactory.JNDI_NAME,factory);
  }

  private void initXIFrameworkContexts(Context rootContext) {
    try {
      Context xiFrameworkRootContext = determineContext(rootContext, XIFrameworkConstants.JNDI_ROOT_CONTEXT_NAME);
      Context xi2espContext = determineContext(xiFrameworkRootContext, XIFrameworkConstants.JNDI_XI2ESP_CONTEXT_NAME);
      xi2espContext.rebind(XIFrameworkConstants.JNDI_XI_MSG_PROC_NAME, new ESPXIMessageProcessorImpl());
    } catch(Throwable thr) {
      LOCATION.catching(thr);
    }
  }
  
  private Context determineContext(Context parentCtx, String ctxName) throws NamingException {
    try {
      return((Context)(parentCtx.lookup(ctxName)));
    } catch(NameNotFoundException nameNFExc) {
      return(parentCtx.createSubcontext(ctxName));
    }
  }

  private void destroyWSJndiContexts() {
    try {
      Context ctx = new InitialContext();

      destroyXIFrameworkContexts(ctx);
      
      WSContainerInterfaceImpl wsContainerInterfaceImpl = (WSContainerInterfaceImpl)ctx.lookup(WS_CONTEXT_NAME  + "/" + WSContainerInterface.NAME);
      wsContainerInterfaceImpl.stop();

      ApplicationWebServiceContextImpl applicationWSContextImpl = (ApplicationWebServiceContextImpl)ctx.lookup(WS_CONTEXT_NAME + "/" + ApplicationWebServiceContextImpl.APPLICATION_WSCONTEXT);
      applicationWSContextImpl.destroy();
     
      try {
        WSUtil.destroyContext(ctx, WS_CONTEXT_NAME);
      } catch(Exception e) {
    	// $JL-EXC$
        //TODO      	  
      }
      
      try {
        WSUtil.destroyContext(ctx, WSClientsConstants.WS_CLIENTS_CONTEXT);	  
      } catch(Exception e) {
        // $JL-EXC$
    	//TODO    	  
      }
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      wsLocation.catching(e);

      throw new ServiceRuntimeException(e);
    }
  }

  private void destroyXIFrameworkContexts(Context rootContext) throws NamingException {
    rootContext.unbind(XIFrameworkConstants.JNDI_XI2ESP_MSG_PROC_NAME);
    Context xiFrameworkRootContext = (Context)(rootContext.lookup(XIFrameworkConstants.JNDI_ROOT_CONTEXT_NAME));
    xiFrameworkRootContext.destroySubcontext(XIFrameworkConstants.JNDI_XI2ESP_CONTEXT_NAME);
  }

  private void registerContainerEventListener() {
    int mask = ContainerEventListener.MASK_INTERFACE_AVAILABLE
      |ContainerEventListener.MASK_INTERFACE_NOT_AVAILABLE
      |ContainerEventListener.MASK_SERVICE_STARTED
      |ContainerEventListener.MASK_SERVICE_STOPPED
      |ContainerEventListener.MASK_CONTAINER_STARTED;
    Set names = new HashSet(8);
    names.add("container");
    names.add("ejbmonitor");
    names.add("log_configurator");
    names.add("shell");
    names.add("security");
    names.add("monitor");
    names.add("licensing");
    names.add("appcontext");
    names.add("timeout");
    names.add(WebServicePluginConstants.SCA_SPI_NAME);
    names.add(WebServicePluginConstants.WEBSERVICE_CONTAINER_NAME);
//    names.add(BASICADMIN_SERVICE);
//    names.add(JMX_INTERFACE);
    
    serviceContext.getServiceState().registerContainerEventListener(mask, names, this);
  }

  private void stopMyApplications() throws ServiceRuntimeException {
    try {      
       webServicesDeployManager.getDeployCommunicator().stopMyApplications(webServicesDeployManager.getDeployCommunicator().getMyApplications());
    } catch(RemoteException e) {
      Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      wsLocation.catching(e);

      throw new ServiceRuntimeException(e);
    }
  }

  private void containerInterfaceAvailable(ContainerManagement container) {    
    webServicesDeployManager = new WebServicesDeployManager();
    DeployCommunicator deployCommunicator = container.registerContainer(WebServicesDeployManager.WEBSERVICES_CONTAINER_NAME, webServicesDeployManager);  
    webServicesDeployManager.init(serviceContext, WSContainer.getRuntimeProcessingEnv(), deployCommunicator);
    try {    
      deployCommunicator.setMigrator(new WebServicesMigrator(serviceContext.getServiceState().getWorkingDirectoryName(), deployCommunicator));
    } catch (Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      wsLocation.catching(e);
    }
           
    try {
      deployCommunicator.startMyApplications(deployCommunicator.getMyApplications());
    } catch (RemoteException e) {
      Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      wsLocation.catching(e);
    }    
    WSContainer.getEventContext().getEventHandler().init(deployCommunicator);
  }

  private void securityInterfaceAvailable(SecurityContext secCtx) {
    ConfigurationHandlerFactory factory = WSContainer.getServiceContext().getCoreContext().getConfigurationHandlerFactory();
      ConfigurationHandler handler = null;
      try {
        handler = factory.getConfigurationHandler();
        String[] names = handler.getAllRootNames();
        boolean rootExists = false;
        for (int i = 0; i < names.length; i++) {
          if (names[i].equals(UDDIUserStoreConfig.UDDI_CONFIG_NAME)) {
            rootExists = true;
          }
        }
        if (!rootExists) {
          handler.createRootConfiguration(UDDIUserStoreConfig.UDDI_CONFIG_NAME);
          handler.commit();
        }
      } catch (Exception e) {
        Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
        wsLocation.catching(e);
      } finally {
        if (handler != null) {
          try {
            handler.closeAllConfigurations();
          } catch (ConfigurationException ce) {
            Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
            wsLocation.catching(ce);
          }
        }
      }
      UserStore store = secCtx.getUserStoreContext().getUserStore(UDDIUserStoreConfig.USER_STORE_NAME);
      if (store == null) {
        secCtx.getUserStoreContext().registerUserStore(new UDDIUserStoreConfig(), this.getClass().getClassLoader());
        store = secCtx.getUserStoreContext().getUserStore(UDDIUserStoreConfig.USER_STORE_NAME);
      }
    WSContainer.getUddiServerAdmin().setUserStore(store);
  }

  private Command[] getWSCommands() {
    Command[] wsCommands = new Command[6];
  //wsCommands[0] = new ListWebServicesCommand();
    wsCommands[0] = new ListWebServicesCommand();
    wsCommands[1] = new ListWSClientsCommand();// 630  wsclients command interface
    wsCommands[2] = new com.sap.engine.services.webservices.server.command.ListWSClientsCommand(); 
    wsCommands[3] = new ListAdditionalComponentsCommand();
    wsCommands[4] = new ListTransportAddressesCommand();
    wsCommands[5] = new ClearWSDLCacheCommand();
    return wsCommands;
  }

  private void initializeCFGBuilder(ConfigurationBuilder b) throws Exception {
    b.reInitialize(WSContainer.createInitializedServerCFGFactory());   
  }
}
