package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.parser.handlers.SimpleAttr;
import com.sap.engine.lib.xml.util.NS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import java.util.ArrayList;

public abstract class AbstractWSDLDefinitionsParser {

  protected final String SAPFEATURE_NS  =  "http://www.sap.com/webas/630/wsdl/features";
  protected final String WSDL_HTTP_NS  =  "http://schemas.xmlsoap.org/wsdl/http/";

  protected static DocumentBuilderFactory factory;
  protected static TransformerFactory transformerFactory;

  static {
   //factory = new DocumentBuilderFactoryImpl();
   factory = DocumentBuilderFactory.newInstance();  
   factory.setNamespaceAware(true);
   transformerFactory = TransformerFactory.newInstance();
  }

  Document doc;
  Element root;
  WSDLDefinitions def;

  protected void parseUseFeatures(Element parent, ArrayList useFeatures) {

    if ((useFeatures == null) || (useFeatures.size() == 0)) {
      return;
    }

    SAPUseFeature tmpFeature;
    Element useFElement;

    if (root.getAttribute("xmlns:sap").equals("")) {
      root.setAttributeNS(NS.XMLNS, "xmlns:sap", SAPFEATURE_NS);
    }

    for (int i = 0; i < useFeatures.size(); i++) {
      tmpFeature = (SAPUseFeature) useFeatures.get(i);
      useFElement = doc.createElementNS(SAPFEATURE_NS, "sap:useFeature");
      useFElement.setAttribute("feature", tmpFeature.getFeatureQName().getQName());
      parent.appendChild(useFElement);
    }
  }

  protected void parseDocumentation(Element wsdlElement, WSDLNode node) {

    if (node instanceof WSDLNamedNode) {
      WSDLNamedNode namedNode = (WSDLNamedNode) node;
      WSDLDocumentation wsDocum = namedNode.getDocumentation();

      if (wsDocum != null) {
        Element docElement = doc.createElementNS(NS.WSDL, "wsdl:documentation");
        try {
          Element res = factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(wsDocum.getContent().getBytes())).getDocumentElement();
          Node newNode = doc.importNode(res, true);
          docElement.appendChild(newNode);
          wsdlElement.appendChild(docElement);
        } catch (Exception e) { //in case of exception adding it as a string
//          e.printStackTrace();
          docElement.appendChild(doc.createTextNode(wsDocum.getContent().toString()));
          wsdlElement.appendChild(docElement);
        }
        return;
      }
    }
  }

  protected void parseBinding(WSDLBinding binding) throws WSDLException {
    int i;
    Element bindingEl = doc.createElementNS(NS.WSDL, "wsdl:binding");
    bindingEl.setAttribute("name", binding.getName());
    bindingEl.setAttribute("type", binding.getType().getQName());

    parseDocumentation(bindingEl, binding);

    parseUseFeatures(bindingEl, binding.getUseFeatures());

    ArrayList usedForAll = binding.getExtensions();
    for (i = 0; i < usedForAll.size(); i++) {
      parseExtensionElement((WSDLExtension) usedForAll.get(i), bindingEl);
    }

    usedForAll = binding.getOperations();
    for (i = 0; i < usedForAll.size(); i++) {
      parseBindingOperation((WSDLBindingOperation) usedForAll.get(i), bindingEl);
    }

    root.appendChild(bindingEl);
  }

  protected void parseExtensionElement(WSDLExtension extension, Element parent) throws WSDLException {
    int i;
    Element extensionEl;
    ArrayList attrChildren;
    SimpleAttr smpAttr;

    if ((extension.getURI() == null) || (extension.getLocalName() == null)) {
      throw new WSDLException ("Uri or localName element in WSDLException is null\n" + extension);
    }
    String temp = getURIMapping(extension.getURI());
    if (temp == null) {
      throw new WSDLException("There is not prefix for this URI:" + extension.getURI());
    } else {
      if (root.getAttribute("xmlns:" + temp).equals("")) {
        root.setAttributeNS(NS.XMLNS, "xmlns:" + temp, extension.getURI());
      }
      extensionEl = doc.createElementNS(extension.getURI(), temp + ":" + extension.getLocalName());
      attrChildren = extension.getAttributes();
      for (i = 0; i < attrChildren.size(); i++) {
        smpAttr = (SimpleAttr) attrChildren.get(i);
        extensionEl.setAttribute(smpAttr.localName, smpAttr.value);
      }
      attrChildren = extension.getChildren();
      for (i = 0; i < attrChildren.size(); i++) {
        parseExtensionElement((WSDLExtension) attrChildren.get(i), extensionEl);
      }
      parent.appendChild(extensionEl);
    }
  }

  private String getURIMapping(String uri) {
    if (uri.equals(NS.WSDL)) {
      return "wsdl";
    } else if (uri.equals(NS.WSDL_SOAP_EXTENSION)) {
      return "soap";
    } else if (uri.equals(NS.WSDL_MIME_EXTENSION)) {
      return "mime";
    } else if (uri.equals(WSDL_HTTP_NS)) {
      return "http";
    }
    return null;
  }

  private void parseBindingOperation(WSDLBindingOperation operation, Element parent) throws WSDLException {
    int i;
    Element operationEl = doc.createElementNS(NS.WSDL, "wsdl:operation");
    operationEl.setAttribute("name", operation.getName());

    parseDocumentation(operationEl, operation);

    parseUseFeatures(operationEl, operation.getUseFeatures());

    ArrayList universal = operation.getExtensions();
    for (i = 0; i < universal.size(); i++) {
      parseExtensionElement((WSDLExtension) universal.get(i), operationEl);
    }

    Element inoutChannelEl;
    WSDLBindingChannel inoutChannel = operation.getInput();

    if (inoutChannel != null) {
      inoutChannelEl = doc.createElementNS(NS.WSDL, "wsdl:input");
      if (inoutChannel.getName() != null) {
        inoutChannelEl.setAttribute("name", inoutChannel.getName());
      }
      parseDocumentation(inoutChannelEl, inoutChannel);

      universal = inoutChannel.getExtensions();
      for (i = 0; i < universal.size(); i++) {
        parseExtensionElement((WSDLExtension) universal.get(i), inoutChannelEl);
      }
      operationEl.appendChild(inoutChannelEl);
    }

    inoutChannel = operation.getOutput();

    if (inoutChannel != null) {
      inoutChannelEl = doc.createElementNS(NS.WSDL, "wsdl:output");
      if (inoutChannel.getName() != null) {
        inoutChannelEl.setAttribute("name", inoutChannel.getName());
      }

      parseDocumentation(inoutChannelEl, inoutChannel);

      universal = inoutChannel.getExtensions();
      for (i = 0; i < universal.size(); i++) {
        parseExtensionElement((WSDLExtension) universal.get(i), inoutChannelEl);
      }
      operationEl.appendChild(inoutChannelEl);
    }

    ArrayList faultList = operation.getFaults();
    WSDLBindingFault bFault;
    Element faultEl;

    for (i = 0; i < faultList.size(); i++) {
      bFault = (WSDLBindingFault) faultList.get(i);
      faultEl = doc.createElementNS(NS.WSDL, "wsdl:fault");
      if (bFault.getName() != null) {
        faultEl.setAttribute("name", bFault.getName());
      }
      parseDocumentation(faultEl, bFault);

      universal = bFault.getExtensions();
      for (int j = 0; j < universal.size(); j++) {
        parseExtensionElement((WSDLExtension) universal.get(j), faultEl);
      }
      operationEl.appendChild(faultEl);
    }
    parent.appendChild(operationEl);
  }
}




