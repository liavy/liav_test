package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceParam;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ClientTransportBinding;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.MimeHttpBinding;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.JAXRPCException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-7-23
 * Time: 9:10:55
 * To change this template use Options | File Templates.
 */
public class BasicOperationInvoker extends AbstractOperationInvoker {

  private String parts;
  private ServiceParam returnParam;

  protected BasicOperationInvoker(ClientTransportBinding clientTransportBinding, TypeMappingRegistryImpl typeMappingRegistry) {
    super(clientTransportBinding, typeMappingRegistry);
    parts = "";
    operationParametersConfig = new OperationParametersConfiguration();
    bindingConfiguration.setProperty("soapAction","");
    bindingConfiguration.setProperty("style","rpc");
    bindingConfiguration.setProperty("transport","http://schemas.xmlsoap.org/soap/http");
    bindingConfiguration.getSubContext("input").setProperty(MimeHttpBinding.SOAP_USE, "encoded");
    bindingConfiguration.getSubContext("input").setProperty(MimeHttpBinding.SOAP_ENCODING_STYLE, MimeHttpBinding.SOAP_ENCODING);
    bindingConfiguration.getSubContext("output").setProperty(MimeHttpBinding.SOAP_USE, "encoded");
    bindingConfiguration.getSubContext("output").setProperty(MimeHttpBinding.SOAP_ENCODING_STYLE, MimeHttpBinding.SOAP_ENCODING);
  }

  protected Object invoke(Object[] inputParams) throws RemoteException {
    bindingConfiguration.getSubContext("input").setProperty(MimeHttpBinding.SOAP_PARTS, parts.trim());
    return(super.invoke(inputParams));
  }

  protected void setOperationName(QName operationName) {
    if(this.operationName == null || !this.operationName.equals(operationName)) {
      if(this.operationName != null) {
        removeAllParameters();
      }
      this.operationName = operationName;
      bindingConfiguration.getSubContext("input").setProperty(MimeHttpBinding.OPERATION_NAME, operationName.getLocalPart());
      bindingConfiguration.getSubContext("input").setProperty(MimeHttpBinding.SOAP_OPERATION_NAMESPACE, operationName.getNamespaceURI());
      bindingConfiguration.getSubContext("output").setProperty(MimeHttpBinding.OPERATION_NAME, operationName.getLocalPart());
      bindingConfiguration.getSubContext("output").setProperty(MimeHttpBinding.SOAP_OPERATION_NAMESPACE, operationName.getNamespaceURI());
    }
  }

  protected String createErrorInfo() {
    return("");
  }

  protected void addParameter(String paramName, QName xmlType, Class javaType, ParameterMode parameterMode) {
    ServiceParam serviceParam = createServiceParam(paramName, xmlType, javaType);
    if(ParameterMode.IN.equals(parameterMode)) {
      inputServiceParamsCollector.add(serviceParam);
      processInParameter(serviceParam);
    } else {
      outputServiceParamsCollector.add(serviceParam);
      if(ParameterMode.OUT.equals(parameterMode)) {
        processInParameter(serviceParam);
      } else {
        operationParametersConfig.processInoutParameter(serviceParam);
      }
    }
  }

  private void processInParameter(ServiceParam serviceParam) {
    operationParametersConfig.processInParameter(serviceParam);
    parts += serviceParam.name + " ";
  }

  private ServiceParam createServiceParam(String paramName, QName xmlType, Class javaType) {
    ServiceParam serviceParam = new ServiceParam();
    serviceParam.isElement = false;
    serviceParam.name = paramName;
    serviceParam.namespace = null;
    serviceParam.wsdlPartName = paramName;
    serviceParam.schemaName = xmlType;
    serviceParam.contentClassName = javaType == null ? null : javaType.getName();
    serviceParam.contentClass = javaType;
    return(serviceParam);
  }

  protected void setReturnType(QName xmlType, Class javaType) {
    ServiceParam serviceParam = createServiceParam(MimeHttpBinding.ANY_NAME, xmlType, javaType);
    if(returnParam != null) {
      outputServiceParamsCollector.remove(returnParam);
    }
    returnParam = serviceParam;
    outputServiceParamsCollector.add(serviceParam);
    operationParametersConfig.setReturnParameter(serviceParam);
  }

  protected void removeAllParameters() {
    inputServiceParamsCollector.clear();
    outputServiceParamsCollector.clear();
    faultsServiceParamsCollector.clear();
    operationParametersConfig.removeAllParameters();
    returnParam = null;
    parts = "";
  }

  protected Map getOutputParams() {
    Hashtable map = new Hashtable();
    for(int i = 0; i < outputServiceParamsCollector.size(); i++) {
      ServiceParam outputServiceParam = (ServiceParam)(outputServiceParamsCollector.get(i));
      if(outputServiceParam.content == null) {
        throw new JAXRPCException(createErrorInfo() + " ERROR : The method getOutputParams() is invoked before any invoke method has been called.");
      }
      if(outputServiceParam != returnParam) {
        map.put(outputServiceParam.name, outputServiceParam.content);
      }
    }
    return(map);
  }

  protected List getOutputValues() {
    ArrayList list = new ArrayList();
    for(int i = 0; i < outputServiceParamsCollector.size(); i++) {
      ServiceParam outputServiceParam = (ServiceParam)(outputServiceParamsCollector.get(i));
      if(outputServiceParam.content == null) {
        throw new JAXRPCException(createErrorInfo() + " ERROR : The method getOutputValues() is invoked before any invoke method has been called.");
      }
      if(outputServiceParam != returnParam) {
        list.add(outputServiceParam.content);
      }
    }
    return(list);
  }
}
