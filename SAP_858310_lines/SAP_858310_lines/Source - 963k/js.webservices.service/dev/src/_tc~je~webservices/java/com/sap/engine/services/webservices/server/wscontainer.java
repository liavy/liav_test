package com.sap.engine.services.webservices.server;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.management.MBeanServer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.engine.admin.model.ManagementModelManager;
import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.cluster.ClusterElement;
import com.sap.engine.interfaces.webservices.client.WSConnectionFactory;
import com.sap.engine.interfaces.webservices.runtime.HTTPProxyResolver;
import com.sap.engine.services.log_configurator.admin.LogConfiguratorManagementInterface;
import com.sap.engine.services.webservices.additions.soaphttp.HTTPFactory;
import com.sap.engine.services.webservices.additions.soaphttp.MIMEFactory;
import com.sap.engine.services.webservices.additions.soaphttp.SOAPHTTPFactroy;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationMarshallerFactory;
import com.sap.engine.services.webservices.espbase.configuration.IConfigurationMarshaller;
import com.sap.engine.services.webservices.espbase.configuration.marshallers.MarshallerRegistry;
import com.sap.engine.services.webservices.espbase.server.runtime.RuntimeProcessingEnvironment;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.jaxm.soap.HTTPSocket;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.SLDConnection;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.builtin.MessageIdProtocol;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.builtin.SessionProtocol;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.builtin.SoapHeadersProtocol;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.httpgetpost.ClientHTTPGetPostFactory;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.httpgetpost.HttpGetPostBinding;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.ClientSOAPHTTPFactory;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.MimeHttpBinding;
import com.sap.engine.services.webservices.runtime.component.ClientProtocolFactoryRegistry;
import com.sap.engine.services.webservices.runtime.component.ClientTransportBindingFactoryRegistry;
import com.sap.engine.services.webservices.runtime.component.ComponentFactoryImpl;
import com.sap.engine.services.webservices.runtime.component.ImplementationContainerManager;
import com.sap.engine.services.webservices.runtime.component.TransportBindingFactoryRegistry;
import com.sap.engine.services.webservices.runtime.registry.RuntimeRegistry;
import com.sap.engine.services.webservices.runtime.registry.WebServiceRegistry;
import com.sap.engine.services.webservices.runtime.registry.wsclient.WSClientRegistry;
import com.sap.engine.services.webservices.uddi.UDDIServerAdminImpl;
import com.sap.engine.services.webservices.webservices630.server.deploy.WSDeployer;
import com.sap.engine.services.webservices.webservices630.server.deploy.event.system.EventContextImpl;
import com.sap.engine.services.webservices.webservices630.server.deploy.event.system.EventFactoryRegistry;
import com.sap.engine.services.webservices.webservices630.server.deploy.event.system.EventListenerRegistry;
import com.sap.engine.services.webservices.webservices630.server.deploy.event.uddipublication.UDDIPublicationEvent;
import com.sap.engine.services.webservices.webservices630.server.deploy.event.uddipublication.UDDIPublicationFactory;
import com.sap.engine.services.webservices.webservices630.server.deploy.event.uddipublication.UDDIPublicationListener;
import com.sap.tc.logging.Location;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSContainer {

  public static String NAME    = "webservices";
  public static String SLD_KEY = "sld";

  private static WebServiceRegistry wsRegistry = null;
  private static WSClientRegistry  wsClientRegistry = null;
  private static RuntimeRegistry runtimeRegistry = null;

  private static TransportBindingFactoryRegistry transportBindingFactoryRegistry = null;
//  private static ProtocolFactoryRegistry protocolFactoryRegistry = null;
  private static ClientProtocolFactoryRegistry clientProtocolFactoryRegistry = null;
  private static ClientTransportBindingFactoryRegistry clientTransportBindingFactoryRegistry = null;
  private static ImplementationContainerManager implementationContainerManager = null;
  private static ComponentFactoryImpl componentFactory = null;

  private static EventContextImpl eventContext = null;
  private static InterfaceContext interfaceContext = null;

  private static HTTPProxyResolver proxyResolver = null;
  private static WSDeployer wsDeployer = null;

  private static ApplicationServiceContext wsServiceContext = null;

  private static LogConfiguratorManagementInterface  logConfiguratorInterface = null;

  private static UDDIServerAdminImpl uddiServerAdmin = null;

  private static Object sldInterface = null;

  
  private static SLDConnection sldConnection = null;
  
  private static boolean monitorServiceStarted;
  private static ArrayList wsAdminPlugIns = new ArrayList();
  
  private static RuntimeProcessingEnvironment processingEnv;
  
  public static void init(ApplicationServiceContext serviceContext) {
    wsServiceContext = serviceContext;

    wsRegistry = new WebServiceRegistry();
    wsClientRegistry = new WSClientRegistry();
    runtimeRegistry = new RuntimeRegistry();

    transportBindingFactoryRegistry = new TransportBindingFactoryRegistry();
//    protocolFactoryRegistry = new ProtocolFactoryRegistry();
    clientProtocolFactoryRegistry = new ClientProtocolFactoryRegistry();
    clientTransportBindingFactoryRegistry = new ClientTransportBindingFactoryRegistry();
    implementationContainerManager = new ImplementationContainerManager(wsServiceContext);

    initEventContext();

    interfaceContext = new InterfaceContext();

    componentFactory = new ComponentFactoryImpl();

//    wsConnectionFactory = new WSConnectionFactoryImpl(serviceContext);
//    wsConnectionRegistry = new WSConnectionRegistry();

    uddiServerAdmin = new UDDIServerAdminImpl();

    proxyResolver = new HTTPProxyResolverImpl();
    HTTPSocket.PROXY_RESOLVER = proxyResolver;

    sldConnection = new SLDConnectionImpl();
    initAdditions();
  }

  public static ApplicationServiceContext getServiceContext() {
    return wsServiceContext;
  }

  public static WebServiceRegistry getWSRegistry() {
    return wsRegistry;
  }

  public static WSClientRegistry getWsClientRegistry() {
    return wsClientRegistry;
  }

  public static RuntimeRegistry getRuntimeRegistry() {
    return runtimeRegistry;
  }

  public static TransportBindingFactoryRegistry getTransportBindingFactoryRegistry() {
    return transportBindingFactoryRegistry;
  }

//  public static ProtocolFactoryRegistry getProtocolFactoryRegistry() {
//    return protocolFactoryRegistry;
//  }

  public static ClientProtocolFactoryRegistry getClientProtocolFactoryRegistry() {
    return clientProtocolFactoryRegistry;
  }

  public static ClientTransportBindingFactoryRegistry getClientTransportBindingFactoryRegistry() {
    return clientTransportBindingFactoryRegistry;
  }

  public static EventContextImpl getEventContext() {
    return eventContext;
  }

  public static InterfaceContext getInterfaceContext() {
    return interfaceContext;
  }

  public static ImplementationContainerManager getImplementationContainerManager() {
    return implementationContainerManager;
  }

  public static ComponentFactoryImpl getComponentFactory() {
    return componentFactory;
  }

//  public static WSConnectionFactory getWsConnectionFactory() {
//    return wsConnectionFactory;
//  }
//
//  public static WSConnectionManipulator getWsConnectionManipulator() {
//    return wsConnectionFactory;
//  }
//
//  public static WSConnectionRegistry getWsConnectionRegistry() {
//    return wsConnectionRegistry;
//  }

  public static void setWSDeployer(WSDeployer wsDeployer) {
    WSContainer.wsDeployer = wsDeployer;
  }

  public static WSDeployer getWSDeployer() {
    return wsDeployer;
  }

  private static void initEventContext() {
    eventContext = new EventContextImpl();
    EventFactoryRegistry eventFactoryRegistry = eventContext.getEventFactoryRegistry();
    EventListenerRegistry eventListenerRegistry = eventContext.getEventListenerRegistry();
    eventFactoryRegistry.registerFactory(UDDIPublicationEvent.EVENT_ID, new UDDIPublicationFactory());
    eventListenerRegistry.registerListener(UDDIPublicationEvent.EVENT_ID, new UDDIPublicationListener());
  }

  private static void initAdditions() {
    try {
      transportBindingFactoryRegistry.registerTransportBindingFactory("SOAPHTTP_TransportBinding", new SOAPHTTPFactroy());
      transportBindingFactoryRegistry.registerTransportBindingFactory("MIME_TransportBinding", new MIMEFactory());
      transportBindingFactoryRegistry.registerTransportBindingFactory("HTTP_TransportBinding", new HTTPFactory());

      clientTransportBindingFactoryRegistry.registerClientTransportBindingFactory(MimeHttpBinding.NAME, new ClientSOAPHTTPFactory());
      clientTransportBindingFactoryRegistry.registerClientTransportBindingFactory(HttpGetPostBinding.HTTP_GET_POST_BINDING, new ClientHTTPGetPostFactory());

//      protocolFactoryRegistry.registerProtocolFactory(HTTPStatefulProtocol.PROTOCOL_NAME, new HTTPStatefulProtocol());
//      protocolFactoryRegistry.registerProtocolFactory(MessageIDProtocolImpl.MESSAGEID_PROTOCOLNAME, new MessageIDProtocolImpl());

      clientProtocolFactoryRegistry.registerClientProtocolFactory(SessionProtocol.NAME,new SessionProtocol());
      clientProtocolFactoryRegistry.registerClientProtocolFactory(SoapHeadersProtocol.NAME, new SoapHeadersProtocol());
      clientProtocolFactoryRegistry.registerClientProtocolFactory(MessageIdProtocol.NAME, new MessageIdProtocol());
    } catch (Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      wsLocation.catching(e);
    }
  }

  public static int[] getDispatcherIDs() {
    ClusterElement[] clusterParticipants = getServiceContext().getClusterContext().getClusterMonitor().getParticipants();
    int clusterParticipantsSize = clusterParticipants.length;
    int[] ids = new int[clusterParticipantsSize];
    int pos = 0;
    for (int i = 0; i < clusterParticipantsSize; i++) {
      ClusterElement currentClusterElement = clusterParticipants[i];
      if (currentClusterElement.getType() == ClusterElement.DISPATCHER && currentClusterElement.getState() == ClusterElement.RUNNING) {
        ids[pos++] = currentClusterElement.getClusterId();
      }
    }
    int[] realIDs = new int[pos];
    System.arraycopy(ids, 0, realIDs, 0, pos);
    return realIDs;
  }

  public static LogConfiguratorManagementInterface getLogConfiguratorInterface() {
    return logConfiguratorInterface;
  }

  public static void setLogConfiguratorInterface(LogConfiguratorManagementInterface logConfiguratorInterface) {
    WSContainer.logConfiguratorInterface = logConfiguratorInterface;
  }

  public static UDDIServerAdminImpl getUddiServerAdmin() {
    return uddiServerAdmin;
  }

  public static HTTPProxyResolver getHTTPProxyResolver() {
    return proxyResolver;
  }

  public static Object getSLDInterface() {
    Hashtable env = new Hashtable();
    env.put("domain", "true");
    try {
      Context ctx = new InitialContext(env);
      return ctx.lookup("applsld");
    } catch (NamingException ne) {
      Location wsLocation = Location.getLocation(WSLogging.SERVER_LOCATION);
      wsLocation.errorT("Cannot get SLD Service Interface: " + ne);
      wsLocation.catching(ne);
      return null;
    }
  }

  public static void setSLDInterface(Object sldInterface) {
    WSContainer.sldInterface = sldInterface;
  }

  public static boolean isMonitorServiceStarted() {
    return monitorServiceStarted;
  }

  public static void setMonitorServiceStarted(boolean monitorServiceStarted) {
    WSContainer.monitorServiceStarted = monitorServiceStarted;
  }

  public static void setRuntimeProcessingEnv(RuntimeProcessingEnvironment env) {
    processingEnv = env;
  }
  
  public static RuntimeProcessingEnvironment getRuntimeProcessingEnv() {
    return processingEnv;
  }
  
  public static SLDConnection getSLDConnection() {
     return sldConnection;
  }
   
  public static void setSLDConnection(SLDConnection sldConnection) {
     WSContainer.sldConnection = sldConnection;
  }  
  
  public static ConfigurationMarshallerFactory createInitializedServerCFGFactory() throws Exception {
    ConfigurationMarshallerFactory cf = MarshallerRegistry.getInitializedFactory();
    //instantiate SecurityConfigurationMarshaller throughout reflection
    Class s_cfg_class = WSContainer.class.getClassLoader().loadClass("com.sap.engine.services.wssec.srt.features.configuration.SecurityConfigurationMarshaller");
    IConfigurationMarshaller s_cfg = (IConfigurationMarshaller) s_cfg_class.newInstance();
    cf.registerMarshaller(s_cfg);
    return cf;
  }

}


