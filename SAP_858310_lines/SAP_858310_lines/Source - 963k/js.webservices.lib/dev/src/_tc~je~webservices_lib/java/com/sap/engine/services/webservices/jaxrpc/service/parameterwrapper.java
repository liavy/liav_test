package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceParam;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-6-7
 * Time: 16:41:22
 * To change this template use Options | File Templates.
 */
public class ParameterWrapper {

  private ParameterMode parameterMode;
  private boolean isHolder;
  private ServiceParam serviceParam;

  protected ParameterWrapper(ServiceParam serviceParam, ParameterMode parameterMode, boolean isHolder) {
    this.parameterMode = parameterMode;
    this.isHolder = isHolder;
    this.serviceParam = serviceParam;
  }

  protected ServiceParam getServiceParam() {
    return(serviceParam);
  }

  protected boolean isHolder() {
    return(isHolder);
  }

  protected void setIsHolder(boolean isHolder) {
    this.isHolder = isHolder;
  }

  protected ParameterMode getParameterMode() {
    return(parameterMode);
  }
}
