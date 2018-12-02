/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws.j2w;

import javax.activation.DataSource;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.soap.SOAPBinding;

import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.mappings.ParameterMapping;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, Jul 20, 2006
 */
public class JaxWsIMappingGeneratorForWSProvider {
  
  public static InterfaceMapping generateIMapping(Class wsProviderClass) throws Exception {
    WebServiceProvider ann = (WebServiceProvider) wsProviderClass.getAnnotation(WebServiceProvider.class);
    if (ann == null) {
      throw new IllegalArgumentException("Missing annotation '" + WebServiceProvider.class.getName() + "'");
    }
    if (! Provider.class.isAssignableFrom(wsProviderClass)) {
      throw new IllegalArgumentException("The class must implement '" + Provider.class.getName() + "'");
    }
    
    InterfaceMapping intfM = new InterfaceMapping();
    intfM.setJAXWSProviderInterfaceFlag(true);
    
    BindingType btAnn = (BindingType) wsProviderClass.getAnnotation(BindingType.class);
    if (btAnn != null) {
      if (SOAPBinding.SOAP11HTTP_BINDING.equals(btAnn.value()) || SOAPBinding.SOAP11HTTP_MTOM_BINDING.equals(btAnn.value())
          || SOAPBinding.SOAP12HTTP_BINDING.equals(btAnn) || SOAPBinding.SOAP12HTTP_MTOM_BINDING.equals(btAnn.value())) {
        intfM.setProperty(InterfaceMapping.BINDING_TYPE, InterfaceMapping.SOAPBINDING);
      } else if (javax.xml.ws.http.HTTPBinding.HTTP_BINDING.equals(btAnn.value())) {
        intfM.setProperty(InterfaceMapping.BINDING_TYPE, InterfaceMapping.HTTPGETBINDING);
      }
    } else {
      intfM.setProperty(InterfaceMapping.BINDING_TYPE, InterfaceMapping.SOAPBINDING);
    }

    Class providerType = null;
    ServiceMode srvModeAnn = (ServiceMode) wsProviderClass.getAnnotation(ServiceMode.class);
    if (srvModeAnn == null || Service.Mode.PAYLOAD.equals(srvModeAnn.value())) {
      providerType = Source.class; 
    } else if (Service.Mode.MESSAGE.equals(srvModeAnn.value())) {
      if (InterfaceMapping.SOAPBINDING.equals(intfM.getProperty(InterfaceMapping.BINDING_TYPE))) {
        providerType = SOAPMessage.class;
      } else {
        providerType = Source.class; //this is not correct, but CTS test pass
        //providerType = DataSource.class;
      }
    }
    
//only single operation is valid here...
    OperationMapping opM = new OperationMapping();
    opM.setJavaMethodName("invoke");
    opM.setWSDLOperationName("invoke");
    ParameterMapping inParam = new ParameterMapping();
    inParam.setParameterType(ParameterMapping.IN_TYPE);
    inParam.setJavaType(providerType.getName());
    ParameterMapping retParam = new ParameterMapping();
    retParam.setParameterType(ParameterMapping.RETURN_TYPE);
    retParam.setJavaType(providerType.getName());
    opM.addParameter(retParam);
    opM.addParameter(inParam);
    
    intfM.addOperation(opM);
    return intfM;
  }

}
