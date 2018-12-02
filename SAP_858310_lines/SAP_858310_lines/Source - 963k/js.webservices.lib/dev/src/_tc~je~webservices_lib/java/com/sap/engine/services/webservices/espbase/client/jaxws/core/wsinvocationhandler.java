package com.sap.engine.services.webservices.espbase.client.jaxws.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;

import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.ParameterObject;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.client.jaxws.metadata.InterfaceMetadata;
import com.sap.engine.services.webservices.espbase.client.jaxws.metadata.OperationMetadata;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WebserviceClientException;


public class WSInvocationHandler implements InvocationHandler, JAXWSProxy, BindingProvider {
  
  private ClientConfigurationContextImpl clientConfigurationCtx;
  private InterfaceMetadata interfaceMetadata;
  private RequestContext requestContext; 
  private Map<String,Object> responseContext;
  private BindingImpl binding;
  
  /**
   * Default constructor.
   * @param clientConfigurationCtx
   * @param interfaceMetadata
   */
  public WSInvocationHandler(ClientConfigurationContextImpl clientConfigurationCtx, InterfaceMetadata interfaceMetadata) {
    this.clientConfigurationCtx = clientConfigurationCtx;
    this.interfaceMetadata = interfaceMetadata;    
    requestContext = new RequestContext(clientConfigurationCtx);
    responseContext = new Hashtable<String,Object>();
    binding = createBinding();
  }
  
  private BindingImpl createBinding() {
    InterfaceMapping interfaceMapping = clientConfigurationCtx.getStaticContext().getInterfaceData();
    String bindingType = interfaceMapping.getProperty(InterfaceMapping.BINDING_TYPE);
    BindingImpl binding = bindingType.equals(InterfaceMapping.HTTPGETBINDING) || bindingType.equals(InterfaceMapping.HTTPPOSTBINDING) ? new HTTPBindingImpl() : new SOAPBindingImpl(clientConfigurationCtx);   
    binding.setResponseContext(responseContext);
    binding.setRequestContext(requestContext);
    return(binding);
  }
  
  /**
   * InvokationHandler.invoke() method implementation.
   */
  public Object invoke(Object proxy, Method method, Object[] paramValues) throws Throwable {    
    OperationMetadata operationMetadata = interfaceMetadata.getOperationMetadata(method);
    if (operationMetadata != null) {
      return(invokeSEIMethod(proxy, method, paramValues, operationMetadata));
    } else {
      return(method.invoke(this, paramValues));
    }
  }
  
  /**
   * Invokes the client runtime.
   * @param operationMetadata
   * @param paramValues
   * @param operationName
   * @return
   * @throws Throwable
   */
  private Object invokeSEIMethod(Object proxy, Method method, Object[] paramValues, OperationMetadata operationMetadata) throws Throwable {
    if (paramValues == null) {
      paramValues = new Object[0];
    }
    Method syncMethod = operationMetadata.getSyncMethod();
    return(syncMethod != null ? invokeSEIAsyncMethod(proxy, syncMethod, paramValues) : invokeSEISyncMethod(operationMetadata, method, paramValues));
  }
  
  private Object invokeSEIAsyncMethod(Object proxy, Method method, Object[] paramValues) throws Throwable {
    ResponseImpl response = createResponse(proxy, method, paramValues);
    clientConfigurationCtx.getServiceContext().getExecutor().execute(response);
    return(response);
  }
  
  private ResponseImpl createResponse(Object proxy, Method method, Object[] asyncParamValues) throws WebserviceClientException {
    ClientConfigurationContextImpl asyncClientConfigurationCtx = clientConfigurationCtx.copy();
    WSInvocationHandler asyncInvokationHandler = new WSInvocationHandler(asyncClientConfigurationCtx, interfaceMetadata);
    AsyncHandler asyncHandler = null;
    Object[] syncParamValues = asyncParamValues;
    if(asyncParamValues.length != 0 && asyncParamValues[asyncParamValues.length - 1] instanceof AsyncHandler) {
      asyncHandler = (AsyncHandler)asyncParamValues[asyncParamValues.length - 1];
      syncParamValues = createSyncParanmeterValuesArray(asyncParamValues);
    }
    return(new ResponseImpl(asyncInvokationHandler, proxy, method, syncParamValues, asyncHandler));
  }
  
  private Object[] createSyncParanmeterValuesArray(Object[] asyncParamValues) {
    Object[] syncParamValues = new Object[asyncParamValues.length - 1];
    System.arraycopy(asyncParamValues, 0, syncParamValues, 0, asyncParamValues.length - 1);
    return(syncParamValues);
  }
  
  private Object invokeSEISyncMethod(OperationMetadata operationMetadata, Method method, Object[] paramValues) throws Throwable {
    responseContext.clear();
    ParameterObject[] allParameters = OperationMetadata.createOperationParameterObjects(operationMetadata);
    // Input parameter count
    int parameterCount = operationMetadata.getOperationParameters().size(); 
    for(int i = 0; i < parameterCount; i++) {
      allParameters[i].parameterValue = paramValues[i]; 
    }    
    clientConfigurationCtx.setInvokedOperation(method.getName(), allParameters);
    setProtocolName();
    processTransportBindingCall();
    int faultCount = operationMetadata.getOperationFaultParameters().size();
    int lastParamIndex = allParameters.length -1;
    while (faultCount > 0) {      
      Throwable faultValue = (Throwable)(allParameters[lastParamIndex].parameterValue);
      if(faultValue != null) {
        throw faultValue;
      }
      faultCount--;
      lastParamIndex--;
    }
    if (lastParamIndex == parameterCount) { // There is return parameter
      return allParameters[lastParamIndex].parameterValue;  
    } else {
      return null;
    }
  }
  
  private void setProtocolName() {
    if (!binding.isHandlerChainEmpty()) {
      clientConfigurationCtx.getDynamicContext().setProperty(BindingImpl.PROTOCOL_NAME, binding);
    } else {
      clientConfigurationCtx.getDynamicContext().removeProperty(BindingImpl.PROTOCOL_NAME);
    }
  }
  
  /**
   * Reads the Fault parameters.
   * @param faultParamObjects
   * @throws Throwable
   */
  private void processFaultParameters(Vector<ParameterObject> faultParamObjects) throws Throwable {
    for(int i = 0; i < faultParamObjects.size(); i++) {
      ParameterObject faultParamObject = faultParamObjects.get(i);
      Throwable faultValue = (Throwable)(faultParamObject.parameterValue);
      if(faultValue != null) {
        throw faultValue;
      }
    }
  }
  
  /**
   * Invokes the transport binding call() method.
   * @throws Throwable
   */
  private void processTransportBindingCall() throws Throwable {
    try {
      clientConfigurationCtx.getTransportBinding().call(clientConfigurationCtx);
    } catch(javax.xml.ws.soap.SOAPFaultException soapFaultExc) {
      throw soapFaultExc;
    } catch (WebServiceException e) {
      throw e;
    } catch(Exception exc) {
      WebServiceException wsException = new WebServiceException(exc.getLocalizedMessage(), exc);
      throw wsException;
    }
  }
  
  /**
   * Sets the operation parameters as binding input.
   * @param paramValues
   * @param operationParams
   */
  private void setOperationSignatureParameterValues(Object[] paramValues, Vector<ParameterObject> operationParams) {
    for(int i = 0; i < paramValues.length; i++) {
      operationParams.get(i).parameterValue = paramValues[i]; 
    }
  }

  /**
   * Method that returns the proxy client configuration context.
   */
  public ClientConfigurationContext _getConfigurationContext() {
    return(clientConfigurationCtx);  
  }

  public Binding getBinding() {
    return this.binding;
  }

  public Map<String, Object> getRequestContext() {    
    return this.requestContext;
  }

  public Map<String, Object> getResponseContext() {
    return this.responseContext;
  }

  public EndpointReference getEndpointReference() {
//    throw new RuntimeException("Method not supported");
    return null;
  }

  public <T extends EndpointReference> T getEndpointReference(Class<T> clazz) {
//    throw new RuntimeException("Method not supported");
    return null;
  }
  
}
