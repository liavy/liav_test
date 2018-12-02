package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceParam;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Map;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-6-7
 * Time: 16:40:51
 * To change this template use Options | File Templates.
 */
public class OperationParametersConfiguration {

  private String[] orderedParameters;
  private Vector inputParameterWrappersCollector;
  private ParameterWrapper returnParameterWrapper;

  public OperationParametersConfiguration() {
    inputParameterWrappersCollector = new Vector();
  }

  public OperationParametersConfiguration(String parameterOrder) {
    this();
    if(parameterOrder != null) {
      StringTokenizer tokenizer = new StringTokenizer(parameterOrder);
      orderedParameters = new String[tokenizer.countTokens()];
      int index = 0;
      while(tokenizer.hasMoreTokens()) {
        orderedParameters[index++] = tokenizer.nextToken();
      }
    }
  }

  protected void processInParameter(ServiceParam serviceParam) {
    inputParameterWrappersCollector.add(determineParameterIndex(serviceParam.name), new ParameterWrapper(serviceParam, ParameterMode.IN, false));
  }

  protected void processOutParameter(ServiceParam serviceParam, boolean isSingle) {
    for(int i = 0; i < inputParameterWrappersCollector.size(); i++) {
      ParameterWrapper parameterWrapper = (ParameterWrapper)(inputParameterWrappersCollector.get(i));
      if(parameterWrapper.getParameterMode().equals(ParameterMode.IN) && parameterWrapper.getServiceParam().name.equals(serviceParam.name)) {
        parameterWrapper.setIsHolder(true);
        return;
      }
    }
    if(!isSingle) {
      inputParameterWrappersCollector.add(determineParameterIndex(serviceParam.name), new ParameterWrapper(serviceParam, ParameterMode.OUT, true));
    } else {
      returnParameterWrapper = new ParameterWrapper(serviceParam, ParameterMode.OUT, false);
    }
  }

  protected void processInoutParameter(ServiceParam serviceParam) {
    inputParameterWrappersCollector.add(determineParameterIndex(serviceParam.name), new ParameterWrapper(serviceParam, ParameterMode.OUT, true));
  }

  protected Vector getInputParameterWrappers() {
    return(inputParameterWrappersCollector);
  }

  protected ParameterWrapper getReturnParameterWrapper() {
    return(returnParameterWrapper);
  }

  protected void setReturnParameter(ServiceParam serviceParam) {
    this.returnParameterWrapper = new ParameterWrapper(serviceParam, ParameterMode.OUT, false);
  }

  private int determineParameterIndex(String partName) {
    if(orderedParameters == null) {
      return(inputParameterWrappersCollector.size());
    }
    int index = 0;
    while(!orderedParameters[index].equals(partName) && ++index != orderedParameters.length);
    return(index == orderedParameters.length ? inputParameterWrappersCollector.size() : index);
  }

  public QName getParameterType(String parameterName) {
    for(int i = 0; i < inputParameterWrappersCollector.size(); i++) {
      ParameterWrapper paramWrapper = (ParameterWrapper)(inputParameterWrappersCollector.get(i));
      if(paramWrapper.getServiceParam().name.equals(parameterName)) {
        return(paramWrapper.getServiceParam().schemaName);
      }
    }
    return(null);
  }

  public QName getReturnType() {
    if(returnParameterWrapper != null) {
      return(returnParameterWrapper.getServiceParam().schemaName);
    }
    return(null);
  }

  public void removeAllParameters() {
    inputParameterWrappersCollector.clear();
    returnParameterWrapper = null;
  }
}