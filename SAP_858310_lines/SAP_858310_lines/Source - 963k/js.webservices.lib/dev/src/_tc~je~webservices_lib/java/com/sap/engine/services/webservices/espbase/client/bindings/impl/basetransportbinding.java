package com.sap.engine.services.webservices.espbase.client.bindings.impl;

import java.rmi.RemoteException;

import javax.xml.rpc.holders.Holder;

import com.sap.engine.interfaces.webservices.runtime.HTTPProxy;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ParameterObject;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.TransportBinding;
import com.sap.engine.services.webservices.espbase.client.bindings.exceptions.TransportBindingException;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;
import com.sap.engine.services.webservices.jaxm.soap.HTTPSocket;
import com.sap.engine.services.webservices.jaxrpc.exceptions.TypeMappingException;

public abstract class BaseTransportBinding implements TransportBinding {

  public static final String CONTENT_TYPE_HEADER = "Content-Type";
  public static final String CONTENT_LENGTH_HEADER = "Content-Length";
  
  public abstract void call(ClientConfigurationContext context) throws java.rmi.RemoteException;   
  
  public abstract void sendMessage(ClientConfigurationContext context) throws RemoteException;
  
  protected OperationMapping getOperationMapping(ClientConfigurationContext context) {
    String operationName = context.getOperationName();
    InterfaceMapping interfaceMapping = context.getStaticContext().getInterfaceData();
    OperationMapping operationMapping = interfaceMapping.getOperationByJavaName(operationName);
    return(operationMapping);
  }
  
  /**
   * Checks if the parameters passed to transport binding call method are correct.
   * @throws TypeMappingException
   */
  protected void initialParametersCheck(ParameterObject[] parameters, OperationMapping opMapping, ClientConfigurationContext clientCfgCtx) throws TransportBindingException {
    ParameterMapping[] pMappings = opMapping.getParameter();
    if (pMappings.length != parameters.length) {
      throw new TransportBindingException(TransportBindingException.INVALID_OPERATION_PARAMS,opMapping.getJavaMethodName());
    }
    for (int i = 0; i < parameters.length; i++) {
      if (parameters[i].parameterType == null) {
        throw new TransportBindingException(TransportBindingException.INVALID_OPERATION_PARAMS,opMapping.getJavaMethodName());
      }
      if (pMappings[i].getParameterType() == ParameterMapping.OUT_TYPE || pMappings[i].getParameterType() == ParameterMapping.IN_OUT_TYPE) {
        if (parameters[i].parameterValue == null) {
          throw new TransportBindingException(TransportBindingException.NULL_INOUT_PARAMS,pMappings[i].getHolderName(),opMapping.getJavaMethodName());
        } 
        if ((clientCfgCtx.getJAXBContext() == null && !(parameters[i].parameterValue instanceof Holder))
            || (clientCfgCtx.getJAXBContext() != null && !(parameters[i].parameterValue instanceof javax.xml.ws.Holder))) {
          throw new TransportBindingException(TransportBindingException.INVALID_OPERATION_PARAMS,opMapping.getJavaMethodName());
        }          
      }
    }
  }
  
  /**
   * Get log proxy info whether the called host uses a proxy.
   * @param context
   * @return
   */
  protected String getLogHttpProxyInfo(ClientConfigurationContext context){
    
    String httpProxy = getProxyForHost(context);
    
    String logInfo = null;
    
    if (httpProxy != null){
     logInfo = " Http proxy " + httpProxy + " used for host " + PublicProperties.getEndpointURL(context);; 
    }else{
     logInfo = " none";
    }
    
    return logInfo;
  }
  
        
  /**
   * Returns the host and port of the proxy if the called endpoint uses one.
   * @param host
   * @return
   */
  protected String getProxyForHost(ClientConfigurationContext context){
    try {
      String proxyLogInfo = null;
         
      // The proxy resolver is registered by the WSContainer class. This covers the nwa scenario.
      if (HTTPSocket.PROXY_RESOLVER != null) {
        String host = PublicProperties.getEndpointURL(context);
        
        HTTPProxy httpProxy = HTTPSocket.PROXY_RESOLVER.getHTTPProxyForHost(host);

        if (httpProxy != null) {
          StringBuilder proxyDescription = new StringBuilder();

          proxyDescription.append(httpProxy.getProxyHost());

          proxyDescription.append(":");

          proxyDescription.append(httpProxy.getProxyHost());

          proxyLogInfo = proxyDescription.toString();
        }
      }else{
        //this should cover the standalone case.
        String httpProxy = PublicProperties.getProxyHost(context);
        String httpProxyPort = PublicProperties.getProxyPort(context); 
        
        if ((httpProxy != null) && (httpProxyPort != null)){
         proxyLogInfo = httpProxy + ":" + httpProxyPort; 
        }
      }
      
      return proxyLogInfo;      
    } catch (Exception e) {           
      return null;
    }
    
  }
              
}
