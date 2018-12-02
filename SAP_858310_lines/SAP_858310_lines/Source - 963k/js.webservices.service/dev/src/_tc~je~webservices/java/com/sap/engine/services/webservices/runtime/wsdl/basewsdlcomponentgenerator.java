package com.sap.engine.services.webservices.runtime.wsdl;

import com.sap.engine.lib.xml.parser.handlers.SimpleAttr;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.lib.xml.util.QName;
import com.sap.engine.services.webservices.wsdl.*;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */
public class BaseWSDLComponentGenerator {

  public static final String DEFAULT_PART_NAME = "response";
  public static final String SCHEMA_ELEMENT = "element";
  public static final String SCHEMA_NAME_ATTR = "name";
  public static final String SCHEMA_COMPLEXTYPE = "complexType";
  public static final String SCHEMA_SEQUENCE = "sequence";
  public static final String SCHEMA_TYPE = "type";
  public static final String BASE_PREFIX = "s";
  public static final String SOAP_BODY = "body";
  public static final String SOAP_NS_ATTR = "namespace";
  public static final String SOAP_USE = "use";
  public static final String SOAP_ENCODED = "encoded";
  public static final String SOAP_LITERAL = "literal";
  public static final String SOAP_ENCSTL_ATTR = "encodingStyle";
  public static final String SOAPHEADER  =  "header";
  private static final String SOAP_URI = "http://schemas.xmlsoap.org/wsdl/soap/";
  private static final String FAULT_MESSAGE_PART_NAME  =  "errorPart";

  public static WSDLMessage generateRPCEncodedMessage(String messageName, com.sap.engine.interfaces.webservices.runtime.ParameterNode[] nodes, com.sap.engine.interfaces.webservices.runtime.JavaToQNameMappings mapping, PrefixFactory prefixFactory, boolean isLiteral) throws WSDLException {
    WSDLMessage message = new WSDLMessage();
    message.setName(messageName);

    WSDLPart tempPart;
    QName tempQName;
    javax.xml.namespace.QName source;

//    System.out.println("BaseWSDLComponentGenerator message: " + messageName);
    for (int i = 0; i < nodes.length; i++) {
      if (! nodes[i].isExposed()) { //missing wsdl generation for not exposed parameters.
        continue;
      }

      if (nodes[i].isHeader() && isLiteral) {
        continue;
      }

//      System.out.println("BaseWSDLComponentGenerator adding part: " + nodes[i].getParameterName());

      source = mapping.getMappedQName(nodes[i].getJavaClassName());

      if (source == null) {
        throw new WSDLException("No qname for class '" + nodes[i].getJavaClassName() + "' is found.");
      }

      tempQName = new QName(prefixFactory.getPrefix(source.getNamespaceURI()), source.getLocalPart(), source.getNamespaceURI());
      tempPart = new WSDLPart();
      tempPart.setName(nodes[i].getParameterName());
      tempPart.setType(WSDLPart.SIMPLE_TYPE, tempQName);
      message.addPart(tempPart);
    }

    return message;
  }

  public static WSDLMessage generateDocumentLiteralMessage(String messageName, String partName, javax.xml.namespace.QName elementName, String prefix) throws WSDLException {
    WSDLMessage message = new WSDLMessage();
    message.setName(messageName);
    WSDLPart part = new WSDLPart();

    part.setName(partName);

    part.setType(WSDLPart.STRUCTURED_TYPE, new QName(prefix, elementName.getLocalPart(), elementName.getNamespaceURI()));
    message.addPart(part);
    return message;
  }

  public static WSDLOperation generatePortTypeOperation(String operationName, String inputMessageName, String outputMessageName, String messageUri, String prefix) {
    WSDLOperation operation = new WSDLOperation();
    operation.setName(operationName);

    if (inputMessageName != null) {
      WSDLChannel input = new WSDLChannel();
      input.setMessage(new QName(prefix, inputMessageName, messageUri));
      operation.setInput(input);
    }

    //in case of one-way operation
    if (outputMessageName != null) {
      WSDLChannel output = new WSDLChannel();
      output.setMessage(new QName(prefix, outputMessageName, messageUri));
      operation.setOutput(output);
    }

    return operation;
  }

  public static WSDLOperation generatePortTypeOperation(com.sap.engine.interfaces.webservices.runtime.OperationDefinition virtualOperation, String inputMessageName, String outputMessageName, String[] faultMessages, String messageUri, String prefix) throws WSDLException {
    WSDLOperation operation = new WSDLOperation();
    operation.setName(virtualOperation.getOperationName());
    WSDLChannel input = new WSDLChannel();
    input.setMessage(new QName(prefix, inputMessageName, messageUri));
    operation.setInput(input);
    WSDLChannel output = new WSDLChannel();
    output.setMessage(new QName(prefix, outputMessageName, messageUri));
    operation.setOutput(output);
    com.sap.engine.interfaces.webservices.runtime.Fault[] faults = virtualOperation.getFaults();

    if ((faults != null) && (faults.length > 0)) {
      if ((faultMessages != null) && (faultMessages.length > 0)) {
        if (faults.length == faultMessages.length) {
          for (int i = 0; i < faults.length; i++) {
            WSDLFault fault = new WSDLFault();
            fault.setName(faults[i].getFaultName());
            fault.setMessage(new QName(prefix, faultMessages[i], messageUri));
            operation.addFault(fault);
          }
        } else {
          throw new WSDLException(" OperationDefinitions faults number(" + faults.length + ") must equal those of faultMessages(" + faultMessages.length + ")");
        }
      } else {
        throw new WSDLException(" If virtualOperation faults number is not null the faultMessages must not be null too");
      }
    }

    return operation;
  }

  public static WSDLBindingChannel generateSOAPEncodedChannel(String namespace, String encodingUri) {
    WSDLBindingChannel channel = new WSDLBindingChannel();
    WSDLExtension extension = new WSDLExtension();
    extension.setLocalName(SOAP_BODY);
    extension.setURI(SOAP_URI);
    extension.setAttribute(new SimpleAttr(SOAP_USE, SOAP_ENCODED, SOAP_URI));
    extension.setAttribute(new SimpleAttr(SOAP_ENCSTL_ATTR, encodingUri, SOAP_URI));

    if (namespace != null) {
      extension.setAttribute(new SimpleAttr(SOAP_NS_ATTR, namespace, SOAP_URI));
    }

    channel.addExtension(extension);
    return channel;
  }

  public static WSDLBindingChannel generateSOAPLiteralChannel(String partName) {
    WSDLBindingChannel channel = new WSDLBindingChannel();
    WSDLExtension extension = new WSDLExtension();
    extension.setLocalName(SOAP_BODY);
    extension.setURI(SOAP_URI);
    extension.setAttribute(new SimpleAttr(SOAP_USE, SOAP_LITERAL, SOAP_URI));

    if (partName != null) {
      extension.setAttribute("parts", partName, SOAP_URI);
    }

    channel.addExtension(extension);
    return channel;
  }

  public static WSDLBindingChannel generateSOAPLiteralChannel(String namespace, com.sap.engine.interfaces.webservices.runtime.ParameterNode[] parameters) {
    WSDLBindingChannel channel = new WSDLBindingChannel();
    WSDLExtension extension = new WSDLExtension();
    extension.setLocalName(SOAP_BODY);
    extension.setURI(SOAP_URI);
    extension.setAttribute(new SimpleAttr(SOAP_USE, SOAP_LITERAL, SOAP_URI));

    if (namespace != null) {
      extension.setAttribute(SOAP_NS_ATTR, namespace, SOAP_URI);
    }

    String partsS = "";
    if (parameters != null) {
      for (int i = 0; i < parameters.length; i++) {
        //in case of soapheader miss it
        if (parameters[i].isHeader()) {
          continue;
        }

        //in case the parameter is hidden
        if (! parameters[i].isExposed()) {
          continue;
        }

        partsS += parameters[i].getParameterName() + " ";
      }
    }

    if (partsS.length() != 0) {
      extension.setAttribute("parts", partsS.trim(), SOAP_URI); //call .trim() in order to remove the trailing ' ' ramained from the loop above.
    }

    channel.addExtension(extension);
    return channel;
  }

  public static WSDLBindingFault generateSOAPEncodedFaultChannel(String name, String namespace, String encodingStyle) {
    WSDLBindingFault fault = new WSDLBindingFault();
    WSDLExtension extension = new WSDLExtension();
    extension.setLocalName(SOAP_BODY);
    extension.setURI(SOAP_URI);
    extension.setAttribute(new SimpleAttr(SOAP_USE, SOAP_ENCODED, SOAP_URI));
    extension.setAttribute(new SimpleAttr(SOAP_NS_ATTR, namespace, SOAP_URI));
    extension.setAttribute(new SimpleAttr(SOAP_ENCSTL_ATTR, encodingStyle, SOAP_URI));
    extension.setAttribute(new SimpleAttr(SCHEMA_NAME_ATTR, name, SOAP_URI));
    fault.addExtension(extension);
    return fault;
  }

  public static WSDLBindingFault generateSOAPLiteralFaultChannel(String name) {
    WSDLBindingFault fault = new WSDLBindingFault();
    WSDLExtension extension = new WSDLExtension();
    extension.setLocalName(SOAP_BODY);
    extension.setURI(SOAP_URI);
    extension.setAttribute(new SimpleAttr(SOAP_USE, SOAP_LITERAL, SOAP_URI));
    extension.setAttribute(new SimpleAttr(SCHEMA_NAME_ATTR, name, SOAP_URI));
    fault.addExtension(extension);
    return fault;
  }

  public static WSDLPort generateSOAPPort(String portName, String bindingUri, String bindingQName, String location) {
    WSDLPort port = new WSDLPort();
    port.setName(portName);
    port.setBinding(new QName(bindingQName, bindingUri));

    WSDLExtension extension = new WSDLExtension();
    extension.setLocalName("address");
    extension.setURI(NS.WSDL_SOAP_EXTENSION);
    extension.addAttribute(new SimpleAttr("location", location, ""));

    port.setExtension(extension);
    return port;
  }

  public static WSDLExtension generateSOAPHeaderLiteralBindingExtension(String messageQName, String messagePartName) {
    WSDLExtension extension = new WSDLExtension();
    extension.setLocalName(SOAPHEADER);
    extension.setURI(SOAP_URI);
    extension.setAttribute("message", messageQName, "");
    extension.setAttribute("part", messagePartName, "");
    extension.setAttribute(SOAP_USE, SOAP_LITERAL, "");

    return extension;
  }

  public static WSDLMessage generateEncodedFaultMessage(String messageName, com.sap.engine.interfaces.webservices.runtime.JavaToQNameMappings encodedMappings, PrefixFactory prefixFactory, com.sap.engine.interfaces.webservices.runtime.Fault fault, String partName) throws WSDLException {
    WSDLMessage message = new WSDLMessage();
    message.setName(messageName);

    WSDLPart tPart = new WSDLPart();
    tPart.setName(partName);
    javax.xml.namespace.QName qname =  encodedMappings.getMappedQName(fault.getJavaClassName());
    if (qname == null) {
      throw new WSDLException("No mapping for javaClass: '" + fault.getJavaClassName() + "' in encodedMapping set: '" + encodedMappings + "'");
    }
    String prf = prefixFactory.getPrefix(qname.getNamespaceURI());
    tPart.setType(WSDLPart.SIMPLE_TYPE, new QName(prf, qname.getLocalPart(), qname.getNamespaceURI()));
    message.addPart(tPart);

    return message;
  }

  public static WSDLMessage generateLiteralFaultMessage(String messageName, String prefix, String elementName) throws WSDLException {
    WSDLMessage message = new WSDLMessage();
    message.setName(messageName);

    WSDLPart part = new WSDLPart();
    part.setName(FAULT_MESSAGE_PART_NAME);
    part.setType(WSDLPart.STRUCTURED_TYPE, new QName(prefix, elementName, ""));
    message.addPart(part);

    return message;
  }

  public static WSDLFault generateFaultChannel(String faultName, String prefix, String messageLocalName) {
    WSDLFault fault = new WSDLFault();
    fault.setName(faultName);
    fault.setMessage(new QName(prefix, messageLocalName, ""));
    return fault;
  }

  public static WSDLBindingFault generateBindingFault(String faultName, String use, String encodingStyle) throws WSDLException {
    WSDLBindingFault fault = new WSDLBindingFault();
    fault.setName(faultName);
    WSDLExtension extension = new WSDLExtension();
    extension.setLocalName("fault");
    extension.setURI(NS.WSDL_SOAP_EXTENSION);

    extension.setAttribute("use", use, "");
    extension.setAttribute("name", faultName, "");
    if (use.equals("encoded")) {
      extension.setAttribute("encodingStyle", encodingStyle, "");
    } else if (! use.equals("literal")) {
      throw new WSDLException("Not supported 'use' value found '" + use + "'");
    }

    fault.addExtension(extension);
    return fault;
  }

}

