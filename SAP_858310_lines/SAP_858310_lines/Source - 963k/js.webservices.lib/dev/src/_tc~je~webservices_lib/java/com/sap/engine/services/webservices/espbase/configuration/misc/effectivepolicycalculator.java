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
package com.sap.engine.services.webservices.espbase.configuration.misc;

import java.io.File;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationBuilder;
import com.sap.engine.services.webservices.espbase.wsdl.AbstractOperation;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Endpoint;
import com.sap.engine.services.webservices.espbase.wsdl.ExtensionContext;
import com.sap.engine.services.webservices.espbase.wsdl.Interface;
import com.sap.engine.services.webservices.espbase.wsdl.Operation;
import com.sap.engine.services.webservices.espbase.wsdl.Service;
import com.sap.engine.services.webservices.espbase.wsdl.WSDLLoader;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.espbase.wsdl.wsdl11.WSDL11Constants;
import com.sap.engine.services.webservices.wspolicy.Policy;
import com.sap.engine.services.webservices.wspolicy.PolicyException;

/**
 * Provides methods for calculation effective policy in wsdl. 
 * 
 * Copyright (c) 2006, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2006-4-5
 */
public class EffectivePolicyCalculator {
  /**
   * Calculates the service effective policy of service with qname <code>serviceQName</code> from
   * wsdl definitions <code>wsdl</code>.
   * @param wsdl
   * @param serviceQName
   * @return the effective policy
   * @throws PolicyException
   */  
  public static Policy getServiceEffectivePolicy(Definitions wsdl, QName serviceQName) throws PolicyException {
    Service srv = wsdl.getService(serviceQName);
    if (srv == null) {
      throw new IllegalArgumentException("Cannot find service with qname " + serviceQName);
    }
    
    Element srvEl = srv.getDomElement();
    Element defsEl = (Element) srvEl.getParentNode();
    
    Policy res = ConfigurationBuilder.loadPolicies(srv, defsEl);
    return res;
  }
  /**
   * Calculates the endpoint effective policy of port with name <code>portName</code> inside service <code>serviceQname</code>.
   * @param wsdl
   * @param serviceQName
   * @param portName
   * @return
   * @throws PolicyException
   */
  public static Policy getEndpointEffectivePolicy(Definitions wsdl, QName serviceQName, String portName) throws PolicyException {
    Service srv = wsdl.getService(serviceQName);
    if (srv == null) {
      throw new IllegalArgumentException("Cannot find service with qname " + serviceQName);
    }
    //obtain port
    Endpoint port = srv.getEndpoint(portName);
    if (port == null) {
      throw new IllegalArgumentException("Cannot find port with name '" + portName + "' in service with qname " + serviceQName);
    }
    //obtain binding
    QName bQName = port.getBinding();
    Binding binding = wsdl.getBinding(bQName);
    //obtain portType
    QName intQName = binding.getInterface();
    Interface portType = wsdl.getInterface(intQName);
    
    Element wsdlEl = (Element) binding.getDomElement().getParentNode();
    
    Policy portPolicy = ConfigurationBuilder.loadPolicies(port, wsdlEl);
    Policy bindingPolicy = ConfigurationBuilder.loadPolicies(binding, wsdlEl);
    Policy portTypePolicy = ConfigurationBuilder.loadPolicies(portType, wsdlEl);
    
    Policy res = Policy.mergePolicies(new Policy[]{portPolicy, bindingPolicy, portTypePolicy});
    return res;
  }
  /**
   * Calculates the operation effective policy of operation <code>operationName</code> from service <code>service</code>
   * and port <code>portName</code>.
   * It starts from port since WS-PolicyAttachment spec says the operation effective policy is related to port.  
   * @param wsdl
   * @param service
   * @param portName
   * @param operationName
   * @return
   * @throws PolicyException
   */
  public static Policy getOperationEffectivePolicy(Definitions wsdl, QName service, String portName, String operationName) throws PolicyException {
    Service srv = wsdl.getService(service);
    if (srv == null) {
      throw new IllegalArgumentException("Cannot find service with qname " + service);
    }
    //obtain port
    Endpoint port = srv.getEndpoint(portName);
    if (port == null) {
      throw new IllegalArgumentException("Cannot find port with name '" + portName + "' in service with qname " + service);
    }
    //obtain binding
    QName bQName = port.getBinding();
    Binding binding = wsdl.getBinding(bQName);
    AbstractOperation bOp = binding.getOperationByName(operationName);
    if (bOp == null) {
      throw new IllegalArgumentException("Cannot find binding operation with name '" + operationName + "' in binding " + bQName);
    }
    
    QName intQName = binding.getInterface();
    Interface portType = wsdl.getInterface(intQName);
    Operation pOp = portType.getOperation(operationName);
    if (pOp == null) {
      throw new IllegalArgumentException("Cannot find portType operation with name '" + operationName + "' in portType " + intQName);
    }
    
    Element wsdlEl = (Element) binding.getDomElement().getParentNode();

    Policy pOpPolicy = ConfigurationBuilder.loadPolicies(pOp, wsdlEl);
    Policy bOpPolicy = ConfigurationBuilder.loadPolicies(bOp, wsdlEl);
    
    Policy res = Policy.mergePolicies(new Policy[]{pOpPolicy, bOpPolicy});
    return res;
  }
  /**
   * Calculates input effective policy for given operation <code>operationName</code> from port <code>portName</code>
   * from service <code>service</code>.
   * @param wsdl
   * @param service
   * @param portName
   * @param operationName
   * @return
   * @throws PolicyException
   */
  public static Policy getInputMessageEffectivePolicy(Definitions wsdl, QName service, String portName, String operationName) throws PolicyException {
    return getInputOutputMessageEffectivePolicy(wsdl, service, portName, operationName, true);
  }
  /**
   * Calculates output effective policy for given operation <code>operationName</code> from port <code>portName</code>
   * from service <code>service</code>.
   * @param wsdl
   * @param service
   * @param portName
   * @param operationName
   * @return
   * @throws PolicyException
   */
  public static Policy getOutputMessageEffectivePolicy(Definitions wsdl, QName service, String portName, String operationName) throws PolicyException {
    return getInputOutputMessageEffectivePolicy(wsdl, service, portName, operationName, false);
  }
  /**
   * Calculates input effective policy for given operation <code>operationName</code> from port <code>portName</code>
   * from service <code>service</code>.
   * @param wsdl
   * @param service
   * @param portName
   * @param operationName
   * @param isInput if true 'input' is generated otherwise 'output' is generated
   * @return
   * @throws PolicyException
   */
  private static Policy getInputOutputMessageEffectivePolicy(Definitions wsdl, QName service, String portName, String operationName, boolean isInput) throws PolicyException {
    Service srv = wsdl.getService(service);
    if (srv == null) {
      throw new IllegalArgumentException("Cannot find service with qname " + service);
    }
    //obtain port
    Endpoint port = srv.getEndpoint(portName);
    if (port == null) {
      throw new IllegalArgumentException("Cannot find port with name '" + portName + "' in service with qname " + service);
    }
    //obtain binding
    QName bQName = port.getBinding();
    Binding binding = wsdl.getBinding(bQName);
    AbstractOperation bOp = binding.getOperationByName(operationName);
    if (bOp == null) {
      throw new IllegalArgumentException("Cannot find binding operation with name '" + operationName + "' in binding " + bQName);
    }
    
    QName intQName = binding.getInterface();
    Interface portType = wsdl.getInterface(intQName);
    Operation pOp = portType.getOperation(operationName);
    if (pOp == null) {
      throw new IllegalArgumentException("Cannot find portType operation with name '" + operationName + "' in portType " + intQName);
    }
    
    Element wsdlEl = (Element) binding.getDomElement().getParentNode();

    try {
      ExtensionContext bOpExtCtx = null;
      ExtensionContext pOpExtCtx = null;
      ExtensionContext msgExtCtx = null;
      if (isInput) {
        bOpExtCtx = bOp.getInputExtensionContext();
        pOpExtCtx = pOp.getInputExtensionContext();
        //take msg extension context
        QName msgQName = QName.valueOf(pOpExtCtx.getProperty(WSDL11Constants.OPERATION_IN_MESSAGE_QNAME));
        msgExtCtx = wsdl.getMessageContext(msgQName);
      } else {
        bOpExtCtx = bOp.getOutputExtensionContext();
        pOpExtCtx = pOp.getOutputExtensionContext();
        //take msg extension context
        QName msgQName = QName.valueOf(pOpExtCtx.getProperty(WSDL11Constants.OPERATION_OUT_MESSAGE_QNAME));
        msgExtCtx = wsdl.getMessageContext(msgQName);
      }
      Policy bOpPolicy = ConfigurationBuilder.loadPolicies(bOpExtCtx, wsdlEl);
      Policy pOpPolicy = ConfigurationBuilder.loadPolicies(pOpExtCtx, wsdlEl);
      Policy msgPolicy = ConfigurationBuilder.loadPolicies(msgExtCtx, wsdlEl);
      
      Policy res = Policy.mergePolicies(new Policy[]{bOpPolicy, pOpPolicy, msgPolicy});
      return res;
    } catch (WSDLException wsE) {
      throw new PolicyException(wsE);
    }
  }

  /**
   * Calculates fault effective policy for fault with name <code>faultName</code> from operation <code>operationName</code> from port <code>portName</code>
   * from service <code>service</code>.
   * @param wsdl
   * @param service
   * @param portName
   * @param operationName
   * @param faultName
   * @return
   * @throws PolicyException
   */
  public static Policy getFaultMessageEffectivePolicy(Definitions wsdl, QName service, String portName, String operationName, String faultName) throws PolicyException {
    Service srv = wsdl.getService(service);
    if (srv == null) {
      throw new IllegalArgumentException("Cannot find service with qname " + service);
    }
    //obtain port
    Endpoint port = srv.getEndpoint(portName);
    if (port == null) {
      throw new IllegalArgumentException("Cannot find port with name '" + portName + "' in service with qname " + service);
    }
    //obtain binding
    QName bQName = port.getBinding();
    Binding binding = wsdl.getBinding(bQName);
    AbstractOperation bOp = binding.getOperationByName(operationName);
    if (bOp == null) {
      throw new IllegalArgumentException("Cannot find binding operation with name '" + operationName + "' in binding " + bQName);
    }
    
    QName intQName = binding.getInterface();
    Interface portType = wsdl.getInterface(intQName);
    Operation pOp = portType.getOperation(operationName);
    if (pOp == null) {
      throw new IllegalArgumentException("Cannot find portType operation with name '" + operationName + "' in portType " + intQName);
    }
    
    Element wsdlEl = (Element) binding.getDomElement().getParentNode();

    try {
      ExtensionContext bOpExtCtx = bOp.getFaultExtensionContext(faultName);
      ExtensionContext pOpExtCtx = pOp.getFaultExtensionContext(faultName);
      QName msgQName = QName.valueOf(pOp.getFaultExtensionContext(faultName).getProperty(WSDL11Constants.FAULT_MESSAGE_QNAME));
      ExtensionContext msgExtCtx = wsdl.getMessageContext(msgQName); 

      Policy bOpPolicy = ConfigurationBuilder.loadPolicies(bOpExtCtx, wsdlEl);
      Policy pOpPolicy = ConfigurationBuilder.loadPolicies(pOpExtCtx, wsdlEl);
      Policy msgPolicy = ConfigurationBuilder.loadPolicies(msgExtCtx, wsdlEl);
      
      Policy res = Policy.mergePolicies(new Policy[]{bOpPolicy, pOpPolicy, msgPolicy});
      return res;
    } catch (WSDLException wsE) {
      throw new PolicyException(wsE);
    }
  }
  
  public static void main(String[] args) throws Exception {
    WSDLLoader loader = new WSDLLoader();
    Definitions def = loader.load("F:/docs/java/webservices_specs/WS-Policy/2006_01_11_updated_version/interop_workshop/PolicyAttachments.wsdl");
    QName service = new QName("http://ws-policy.tests", "MyService");
    //Policy p = EffectivePolicyCalculator.getEndpointEffectivePolicy(def, service, "MyPort");
    //Policy p = EffectivePolicyCalculator.getOperationEffectivePolicy(def, service, "MyPort", "MyOperation");
    //Policy p = EffectivePolicyCalculator.getInputMessageEffectivePolicy(def, service, "MyPort", "MyOperation");
    //Policy p = EffectivePolicyCalculator.getOutputMessageEffectivePolicy(def, service, "MyPort", "MyOperation");
    Policy p = EffectivePolicyCalculator.getFaultMessageEffectivePolicy(def, service, "MyPort", "MyOperation", "MyFault");
    Element pEl = p.getPolicyAsDom();
//    TransformerFactoryImpl tf = new TransformerFactoryImpl();
    TransformerFactory tf = TransformerFactory.newInstance();
    tf.newTransformer().transform(new DOMSource(pEl), new StreamResult(new File("F:/docs/java/webservices_specs/WS-Policy/2006_01_11_updated_version/interop_workshop/EffectivePolicy/Policy-for-Fault_sap.xml")));
  }
}
