/*
 * Created on 2005-9-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.dynamic.impl;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Vector;

import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.DynamicStubImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.DInterface;
import com.sap.engine.services.webservices.espbase.client.dynamic.DInterfaceInvoker;
import com.sap.engine.services.webservices.espbase.client.dynamic.DOperation;
import com.sap.engine.services.webservices.espbase.client.dynamic.DParameter;
import com.sap.engine.services.webservices.espbase.client.dynamic.ParametersConfiguration;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.ObjectFactory;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.BaseGeneratedStub;
import javax.xml.rpc.soap.SOAPFaultException;

/**
 * @author ivan-m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DInterfaceInvokerImpl implements DInterfaceInvoker {
  
  private DInterface dInterface;
  private ClientConfigurationContextImpl clientConfigContext;

  protected DInterfaceInvokerImpl(DInterface dInterface, ClientConfigurationContextImpl clientConfigContext) {
    this.dInterface = dInterface;
    this.clientConfigContext = clientConfigContext;
  }
  
  public ClientConfigurationContext _getConfigurationContext() {
    return this.clientConfigContext;
  }

  public BaseGeneratedStub getStubInstance() {
    return(null);
  }

  public ParametersConfiguration getParametersConfiguration(String operationName) {
    DOperation dOperation = getDOperation(operationName);
    ParametersConfiguration paramsConfiguration = new ParametersConfigurationImpl((DOperationImpl)dOperation);
    return(paramsConfiguration);
  }
  
  private DOperation getDOperation(String operationName) {
    DOperation dOperation = dInterface.getOperation(operationName);
    if(dOperation == null) {
      throw new IllegalArgumentException("Operation with name '" + operationName + "' is not defined.");
    }
    return(dOperation);
  }

  public synchronized void invokeOperation(String operationName, ParametersConfiguration parametersConfiguration, ObjectFactory factory) throws RemoteException, InvocationTargetException {
    DOperationImpl dOperation = (DOperationImpl)(dInterface.getOperation(operationName));
    if(dOperation == null) {
      throw new IllegalArgumentException("Operation with name '" + operationName + "' is not defined.");
    }
    clientConfigContext.setInvokedOperation(((DOperationImpl)dOperation).getJavaMethodName(), ((ParametersConfigurationImpl)parametersConfiguration).getParameterObjects());
    clientConfigContext.setObjectFactory(factory);
    Vector faultDParameters = dOperation.getFaultParametersCollector();
    resetNonNullFaultParametersBeforeInvokation(faultDParameters, (ParametersConfigurationImpl)parametersConfiguration);
    try {
      clientConfigContext.getTransportBinding().call(clientConfigContext);
      processFaultParametersAfterTheInvokation(faultDParameters, parametersConfiguration);
    } catch(SOAPFaultException soapFaultExc) {
      throw new InvocationTargetException(soapFaultExc, soapFaultExc.getFaultString());
    }
  }
  
  private void processFaultParametersAfterTheInvokation(Vector faultDParameters, ParametersConfiguration parametersConfiguration) throws InvocationTargetException {
    for(int i = 0; i < faultDParameters.size(); i++) {
      DParameter faultDParameter = (DParameter)(faultDParameters.get(i));
      String faultParameterName = faultDParameter.getName();
      Throwable faultValue = parametersConfiguration.getFaultParameterValue(faultParameterName);
      if(faultValue != null) {
        throw new InvocationTargetException(faultValue, faultValue.getMessage());
      }
    }
  }
  
  private void resetNonNullFaultParametersBeforeInvokation(Vector faultDParameters, ParametersConfigurationImpl parametersConfiguration) {
    for(int i = 0; i < faultDParameters.size(); i++) {
      DParameter faultDParameter = (DParameter)(faultDParameters.get(i));
      String faultParameterName = faultDParameter.getName();
      Throwable faultValue = parametersConfiguration.getFaultParameterValue(faultParameterName);
      if(faultValue != null) {
        ((ParametersConfigurationImpl)parametersConfiguration).resetFaultParameterValue(faultParameterName);
      }
    }
  }
  
  public Object getProperty(String key) {
    return(DynamicStubImpl._getProperty(key, clientConfigContext));
  }
  
  public void setProperty(String key, Object value) {
    DynamicStubImpl._setProperty(key, value, clientConfigContext);
  }

  public void startLogging(OutputStream requestLog, OutputStream responseLog) {
    clientConfigContext.getDynamicContext().setProperty(PublicProperties.P_REQUEST_LOG_STREAM, requestLog);
    clientConfigContext.getDynamicContext().setProperty(PublicProperties.P_RESPONSE_LOG_STREAM, responseLog);
  }

  public void stopLogging() {
    clientConfigContext.getDynamicContext().removeProperty(PublicProperties.P_REQUEST_LOG_STREAM);
    clientConfigContext.getDynamicContext().removeProperty(PublicProperties.P_RESPONSE_LOG_STREAM);
  }
}
