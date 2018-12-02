package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ClientTransportBinding;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.PropertyContext;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProtocolList;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceParam;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.MimeHttpBinding;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.rpc.holders.Holder;
import java.util.*;
import java.rmi.RemoteException;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-7-23
 * Time: 9:08:22
 * To change this template use Options | File Templates.
 */
public abstract class AbstractOperationInvoker implements Constants {

  protected Vector inputServiceParamsCollector;
  protected Vector outputServiceParamsCollector;
  protected Vector faultsServiceParamsCollector;
  protected ClientTransportBinding clientTransportBinding;
  protected PropertyContext operationConfiguration;
  protected PropertyContext bindingConfiguration;
  protected PropertyContext featureConfiguration;
  protected QName operationName;
  protected OperationParametersConfiguration operationParametersConfig;
  protected TypeMappingRegistryImpl typeMappingRegistry;

  protected AbstractOperationInvoker(ClientTransportBinding clientTransportBinding, TypeMappingRegistryImpl typeMappingRegistry) {
    this.typeMappingRegistry = typeMappingRegistry;
    this.clientTransportBinding = clientTransportBinding;
    inputServiceParamsCollector = new Vector();
    outputServiceParamsCollector = new Vector();
    faultsServiceParamsCollector = new Vector();
    operationConfiguration = new PropertyContext();
    bindingConfiguration = new PropertyContext();
    featureConfiguration = new PropertyContext();

    operationConfiguration.setProperty(ClientTransportBinding.APP_CLASSLOADER,this.getClass().getClassLoader());
    operationConfiguration.setSubContext(ClientTransportBinding.BINDING_CONFIG, bindingConfiguration);
    operationConfiguration.setSubContext(ClientTransportBinding.FEATUTE_CONFIG, featureConfiguration);

    featureConfiguration.setProperty("typeMapping", typeMappingRegistry.getDefaultTypeMapping());
    clientTransportBinding.setTypeMappingRegistry(typeMappingRegistry);
  }

  protected Object invoke(Object[] inputParams) throws RemoteException {
    try {
      Vector inputParameterWrappersCollector = operationParametersConfig.getInputParameterWrappers();
      if(inputParams.length != inputParameterWrappersCollector.size()) {
        throw new JAXRPCException(createErrorInfo() + " ERROR : The invokation of the method '{" + operationName.getNamespaceURI() + "} : " + operationName.getLocalPart() + "' is not correct. The count of the input parameters, determined by the invokation ot the method, does not match to the count of the defined input parameters.");
      }
      for(int i = 0; i < inputParameterWrappersCollector.size(); i++) {
        ParameterWrapper inputParameterWrapper = (ParameterWrapper)(inputParameterWrappersCollector.get(i));
        Object inputParameter = inputParams[i];
        Object inputParameterValue = null;
        if(inputParameter != null) {
          if(inputParameterWrapper.isHolder()) {
            if(!(inputParameter instanceof Holder)) {
              throw new JAXRPCException(createErrorInfo() + " ERROR : The invokation of the method '{" + operationName.getNamespaceURI() + "} : " + operationName.getLocalPart() + "' is not correct. The input param with index '" + i + "' has to impement javax.xml.rpc.holders.Holder.");
            }
            inputParameterValue = inputParameter.getClass().getField(HOLDER_VALUE_FIELD_NAME).get(inputParameter);
          } else {
            inputParameterValue = inputParameter;
          }
          if(inputParameterWrapper.getServiceParam().contentClass == null) {
            throw new JAXRPCException("Type : " + inputParameterWrapper.getServiceParam().schemaName + " is not mapped in the type mapping.");
          }
          if(!inputParameterValue.getClass().isAssignableFrom(inputParameterWrapper.getServiceParam().contentClass)) {
            throw new JAXRPCException(createErrorInfo() + " ERROR : The invokation of the method '{" + operationName.getNamespaceURI() + "} : " + operationName.getLocalPart() + "' is not correct. The class '" + inputParameterValue.getClass().getName() + "' is not assignable from the defined class '" + inputParameterWrapper.getServiceParam().contentClassName + "'.");
          }
        }
        inputParameterWrapper.getServiceParam().content = inputParameterValue;
      }
      clientTransportBinding.startOperation(createServiceParamsArrayFromVector(inputServiceParamsCollector), createServiceParamsArrayFromVector(outputServiceParamsCollector), createServiceParamsArrayFromVector(faultsServiceParamsCollector));
      clientTransportBinding.call(operationConfiguration, new ProtocolList(), new ProtocolList());
      for(int i = 0; i < faultsServiceParamsCollector.size(); i++) {
        ServiceParam faultServiceParam = (ServiceParam)(faultsServiceParamsCollector.get(i));
        if(faultServiceParam.content != null) {
          throw (RemoteException)faultServiceParam.content;
        }
      }
      return(operationParametersConfig.getReturnParameterWrapper() == null ? null : operationParametersConfig.getReturnParameterWrapper().getServiceParam().content);
    } catch(Exception exc) {
      if(exc instanceof JAXRPCException) {
        throw (JAXRPCException)exc;
      } else  if(exc instanceof SOAPFaultException) {
        throw new RemoteException(exc.getMessage());
      } else {
        throw new JAXRPCException(exc);
      }
    }
  }

  private ServiceParam[] createServiceParamsArrayFromVector(Vector collector) {
    ServiceParam[] serviceParamsArray = new ServiceParam[collector.size()];
    collector.copyInto(serviceParamsArray);
    return(serviceParamsArray);
  }

  protected abstract String createErrorInfo();

  protected ServiceParam[] getOutputServiceParams() {
    return(createServiceParamsArrayFromVector(outputServiceParamsCollector));
  }

  protected ServiceParam[] getInputServiceParams() {
    return(createServiceParamsArrayFromVector(inputServiceParamsCollector));
  }

  protected ServiceParam[] getFaultServiceParams() {
    return(createServiceParamsArrayFromVector(faultsServiceParamsCollector));
  }

  protected Vector getOutputServiceParamsVector() {
    return(new Vector(outputServiceParamsCollector));
  }

  protected Vector getInputServiceParamsVector() {
    return(new Vector(inputServiceParamsCollector));
  }

  protected Vector getFaultServiceParamsVector() {
    return(new Vector(faultsServiceParamsCollector));
  }

  protected QName getParameterType(String parameterName) {
    return(operationParametersConfig.getParameterType(parameterName));
  }

  protected QName getReturnType() {
    return(operationParametersConfig.getReturnType());
  }

  protected abstract Map getOutputParams();

  protected abstract List getOutputValues();

  protected void setTargetEndpointAddress(String endpointAdress) {
    bindingConfiguration.setProperty(MimeHttpBinding.ENDPOINT, endpointAdress);
  }

  protected String getTargetEndpointAddress() {
    return((String)(bindingConfiguration.getProperty(MimeHttpBinding.ENDPOINT)));
  }

  protected QName getOperationName() {
    return(operationName);
  }

  protected OperationParametersConfiguration getParametersConfiguration() {
    return(operationParametersConfig);
  }
}
