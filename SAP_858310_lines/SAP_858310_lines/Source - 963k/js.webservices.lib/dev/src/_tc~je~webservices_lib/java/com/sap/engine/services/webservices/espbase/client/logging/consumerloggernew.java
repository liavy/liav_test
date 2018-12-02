package com.sap.engine.services.webservices.espbase.client.logging;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sap.engine.interfaces.webservices.runtime.ProtocolExceptionExt;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientServiceContext;
import com.sap.engine.services.webservices.espbase.client.bindings.StaticConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientServiceContextImpl;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.tc.logging.Location;

public class ConsumerLoggerNew extends ConsumerLogger {  

  
  public static void logRedirectResponse(ClientConfigurationContext context, String endpoint, Location location) {
    Map<String,String> contextInfo = context2Map(context);
    
    logRedirectResponse(contextInfo, endpoint, location);
  }

  public static void log404Response(ClientConfigurationContext context, String endpoint, Location location) {
    Map<String,String> contextInfo = context2Map(context);
    
    log404Response(contextInfo, endpoint,location);
  }

  public static void log500Reponse(ClientConfigurationContext context, Location location) {
    Map<String,String> contextInfo = context2Map(context);

    log500Reponse(contextInfo, location);
  }
  
  public static void log502Response(ClientConfigurationContext context, String endpoint, Location location) {
    Map<String,String> contextInfo = context2Map(context);
    
    log502Response(contextInfo, endpoint, location);
  }
  

  public static void log503Response(ClientConfigurationContext context, Location location) {
    Map<String,String> contextInfo = context2Map(context);

    log503Response(contextInfo, location);
  }

  public static void logUnableToConnect(IOException e, ClientConfigurationContext context, String endpoint, Location location) {
    Map<String,String> contextInfo = context2Map(context);

    logUnableToConnect(e, contextInfo, endpoint,location);
  }

  public static void logNoHTTPSPeer(IOException e, ClientConfigurationContext context, String endpoint, Location location) {
    Map<String,String> contextInfo = context2Map(context);
    
    logNoHTTPSPeer(e, contextInfo, endpoint, location);
  }

  public static void logSocketTimeout(SocketTimeoutException e, ClientConfigurationContext context, String endpoint,Location location) {
    Map<String,String> contextInfo = context2Map(context);
    
    logSocketTimeout(e, contextInfo, endpoint,location);
  }
  
  public static void logSecurityFailure(ProtocolExceptionExt excception, Location loc, ClientConfigurationContext context){
    Map<String,String> contextInfo = context2Map(context);
    
    logSecurityFailure(excception, contextInfo, loc);
  }
    
  private static Map<String, String> context2Map(ClientConfigurationContext context){   
    Map<String, String>  logData = new HashMap<String, String>();
                                                    
    ClientServiceContext serviceContext = context.getServiceContext();    
    
    String clientType = serviceContext.getClientType();
    
    if (ClientServiceContext.JEE5.equals(clientType)) {      
      logData.put(PROXY_TYPE, JAX_WS_TYPE);
      
      logData.put(APPLICATION_NAME, getApplicationName(context));
      logData.put(PORT_QNAME, getPortQName(context));
      logData.put(INTERFACE_QNAME, getInterfaceName(context));
    } else if (ClientServiceContext.JAXRPC.equals(clientType)) {      
      logData.put(PROXY_TYPE, JAX_RPC_TYPE);
      
      logData.put(APPLICATION_NAME, getApplicationName(context));
      logData.put(PORT_QNAME, getPortQName(context));
      logData.put(INTERFACE_QNAME, getInterfaceName(context));
    } else if (ClientServiceContext.DYNAMIC.equals(clientType)) {
      if (serviceContext.getProperty(ClientServiceContextImpl.SERVICE_REF_ID) != null) {
        // This client is created by mass configuration
        logData.put(PROXY_TYPE, DYNAMIC_MASS_CONFIG_TYPE);
        
        logData.put(APPLICATION_NAME, context.getServiceContext().getApplicationName());
        logData.put(SERVICE_REF_ID, (String) serviceContext.getProperty(ClientServiceContextImpl.SERVICE_REF_ID));
      } else if (serviceContext.getProperty(ClientServiceContextImpl.LMT_NAME) != null) {
        // This client is created by destination
        logData.put(PROXY_TYPE, DYNAMIC_DESTINATION_TYPE);
        
        logData.put(LMT_NAME, (String) serviceContext.getProperty(ClientServiceContextImpl.LMT_NAME));
//      logData.put(IF_NAME, (String) serviceContext.getProperty(ClientServiceContextImpl.IF_NAME));
      } else {
        // This client is created by WSDL URL.
        logData.put(PROXY_TYPE, DYNAMIC_WSDL_TYPE);
        
        logData.put(WSDL_URL, (String) serviceContext.getProperty(ClientServiceContextImpl.WSDL_URL));       
      }       
    }
               
    return logData; 
  }
  
  private static String getApplicationName(ClientConfigurationContext context){    
    return context.getServiceContext().getApplicationName();       
  }
  
  private static String getPortQName(ClientConfigurationContext context){
    ClientServiceContext serviceContext = context.getServiceContext();    
    StaticConfigurationContext staticContext = context.getStaticContext();        
    BindingData bdata = staticContext.getRTConfig();    
    
    QName serviceName = serviceContext.getServiceName();
    QName portQName = new  QName(serviceName.getNamespaceURI(), bdata.getName());
   
    return portQName.toString();
  }
  
  private static String getInterfaceName(ClientConfigurationContext context) {    
    StaticConfigurationContext staticContext = context.getStaticContext();
    QName interfaceQName = new QName(staticContext.getDTConfig().getNamespace(), staticContext.getDTConfig().getName());

    return interfaceQName.toString();
  }

}
