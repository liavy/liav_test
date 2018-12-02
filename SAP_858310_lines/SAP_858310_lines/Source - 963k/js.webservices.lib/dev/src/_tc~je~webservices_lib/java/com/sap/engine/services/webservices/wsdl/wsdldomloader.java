/*
 * Copyright (c) 2002 by SAP AG.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP AG.
 * Created : 2002-4-12
 */
package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.parser.URLLoader;
import com.sap.engine.lib.xml.parser.helpers.CharArray;
import com.sap.engine.services.webservices.jaxrpc.schema2java.*;
import com.sap.engine.services.webservices.jaxrpc.util.PackageBuilder;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Builds WSDL definitions from dom tree.
 * @author Chavdar Baykov, chavdar.baikov@sap.com
 * @version 1.0
 */
public class WSDLDOMLoader {

  public static String WSDL_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
  public static String WSDL_SAP_NAMESPACE = "http://www.sap.com/webas/630/wsdl/features";
  public static String SCHEMA_NAMESPACE1 = "http://www.w3.org/2000/10/XMLSchema";
  public static String SCHEMA_NAMESPACE2 = "http://www.w3.org/2001/XMLSchema";
  public static String SCHEMA_NAMESPACE3 = "http://www.w3.org/1999/XMLSchema";
  /**
   * These constants represent constants for main elements in WSDL Document.
   */
  public static final int DOCUMENT_STATE = -1;
  public static final int EXTENSION_STATE = 0;
  public static final int DEFINITIONS_STATE = 1;
  public static final int TYPES_STATE = 2;
  public static final int SCHEMA_STATE = 3;
  public static final int MESSAGE_STATE = 4;
  public static final int PART_STATE = 5;
  public static final int PORTTYPE_STATE = 6;
  public static final int OPERATION_STATE = 7;
  public static final int INPUT_STATE = 8;
  public static final int OUTPUT_STATE = 9;
  public static final int FAULT_STATE = 10;
  public static final int BINDING_STATE = 11;
  public static final int BOPERATION_STATE = 12;
  public static final int BINPUT_STATE = 13;
  public static final int BOUTPUT_STATE = 14;
  public static final int BFAULT_STATE = 15;
  public static final int SERVICE_STATE = 16;
  public static final int PORT_STATE = 17;
  public static final int DOCUMENTATION_STATE = 18;
  public static final int IMPORT_STATE = 19;
  public static final int SAP_FEATURE_STATE = 20;
  public static final int SAP_PROPERTY_STATE = 21;
  public static final int SAP_OPTION_STATE = 22;
  public static final int SAP_USE_FEATURE_STATE = 23;
  private HashStringtoInt elementMapping; // Mapping of all wsdl names to internal id-s
  private HashStringtoInt elementMapping2; // Mapping of all sap wsdl names to internal id-s
  private Hashtable systemIdMapping; // Maps remote SystemId
  private Hashtable reverseMapping;  // Maps local locations to Remote Locations
  private String documentId;
  private String proxyHost = null;
  private String proxyPort = null;
  private EntityResolver wsdlResolver;

  /**
   * Default constructor.
   */
  public WSDLDOMLoader() {
    documentId = null;
    this.systemIdMapping = null;
    this.reverseMapping = new Hashtable();
    initMapping();
  }

  /**
   * Set's Htpp proxy for resolving url based wsdl-z
   * @param proxyHost
   * @param proxyPort
   */
  public void setHttpProxy(String proxyHost,String proxyPort) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
  }
  /**
   * Initializes mapping from element names to constants.
   */
  private void initMapping() {
    elementMapping = new HashStringtoInt();
    elementMapping.put("definitions", DEFINITIONS_STATE);
    elementMapping.put("types", TYPES_STATE);
    elementMapping.put("schema", SCHEMA_STATE);
    elementMapping.put("message", MESSAGE_STATE);
    elementMapping.put("part", PART_STATE);
    elementMapping.put("portType", PORTTYPE_STATE);
    elementMapping.put("operation", OPERATION_STATE);
    elementMapping.put("input", INPUT_STATE);
    elementMapping.put("output", OUTPUT_STATE);
    elementMapping.put("fault", FAULT_STATE);
    elementMapping.put("binding", BINDING_STATE);
    elementMapping.put("service", SERVICE_STATE);
    elementMapping.put("port", PORT_STATE);
    elementMapping.put("documentation", DOCUMENTATION_STATE);
    elementMapping.put("import", IMPORT_STATE);
    elementMapping2 = new HashStringtoInt();
    elementMapping2.put("Feature",SAP_FEATURE_STATE);
    elementMapping2.put("Property",SAP_PROPERTY_STATE);
    elementMapping2.put("Option",SAP_OPTION_STATE);
    elementMapping2.put("useFeature",SAP_USE_FEATURE_STATE);    
  }

  /**
   * Returns tag code from element local name and namespace. This code is
   * @param uri
   * @param localName
   * @return internal wsdl element id.
   */
  private int getElementCode(String uri, String localName) {
    if (uri == null) {
      return EXTENSION_STATE;
    }
    if (uri.equals(WSDL_NAMESPACE)) {
      return elementMapping.get(localName);
    }
    if (uri.equals(WSDL_SAP_NAMESPACE)) {
      return elementMapping2.get(localName);
    }
    if ((uri.equals(SCHEMA_NAMESPACE1) || uri.equals(SCHEMA_NAMESPACE2) || uri.equals(SCHEMA_NAMESPACE3)) && localName.equals("schema")) {
      return SCHEMA_STATE;
    }
    return EXTENSION_STATE;
  }

  /**
   * Resolves base-to-relative uri mapping
   * @param baseUri
   * @param relativeUri
   * @return resolved uri.
   */
  private URL resolveUri(String baseUri, String relativeUri) throws WSDLException {
    if (systemIdMapping != null) { // working with mirror image
      try {
        // gets remote base uri
        String realBaseUri;
        if (baseUri.equals(documentId)) {
          realBaseUri = (String) systemIdMapping.get("");
        } else {
          realBaseUri = (String) reverseMapping.get(baseUri);
        }
        String realNewUri = "";
        if (systemIdMapping.get("Version")!=null) {
          realNewUri = realBaseUri+"|"+relativeUri;
        } else {
          URL base = URLLoader.fileOrURLToURL(null,realBaseUri);
          realNewUri = URLLoader.fileOrURLToURL(base,relativeUri).toString();
        }
        URL realLocalUri = URLLoader.fileOrURLToURL(null,baseUri);
        URL newLocalUri = URLLoader.fileOrURLToURL(realLocalUri,(String) systemIdMapping.get(realNewUri));
        reverseMapping.put(newLocalUri.toString(),realNewUri.toString());
        return newLocalUri;
      } catch (IOException e) {
        throw new WSDLException("Unable to resolve {"+relativeUri+"} from base uri {"+baseUri+"} using mirror image. Check if you pass correct import mapping to the WSDLDomLoader.",e);
      }
    } else {
      try {
        URL rootURL = URLLoader.fileOrURLToURL(null, baseUri);
        URL locationURL = URLLoader.fileOrURLToURL(rootURL, relativeUri);
        return locationURL;
      } catch (IOException i) {
        throw new WSDLException("Unable to resolve {"+relativeUri+"} from base uri {"+baseUri+"} using mirror image. Check if you pass correct import mapping to the WSDLDomLoader.",i);
      }
    }
  }

  public void setWSDLResolver(EntityResolver wsdlResolver) {
    this.wsdlResolver = wsdlResolver;
  }

  /**
   * Loads binding channel contents.
   * @param parent
   * @param element
   * @return
   * @throws WSDLException
   */
  private WSDLBindingChannel loadBindingChannel(WSDLNode parent, Element element) throws WSDLException {
    WSDLBindingChannel result = new WSDLBindingChannel(parent);
    result.loadAttributes(element);
    Element currentElement = getFirstChildElement(element);

    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

      if (elementCode == EXTENSION_STATE) {
        WSDLExtension extension = loadExtension(result, currentElement);
        result.addExtension(extension);
      }/* else if (elementCode != DOCUMENTATION_STATE) {
        invalidMemberException("bindingChannel", currentElement);
      }*/

      currentElement = getNextElement(currentElement);
    }

    return result;
  }

  /**
   * Loads binding fault contents.
   */
  private WSDLBindingFault loadBindingFault(WSDLNode parent, Element element) throws WSDLException {
    WSDLBindingFault result = new WSDLBindingFault(parent);
    result.loadAttributes(element);
    Element currentElement = getFirstChildElement(element);

    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

      if (elementCode == EXTENSION_STATE) {
        WSDLExtension extension = loadExtension(result, currentElement);
        result.addExtension(extension);
      } /*else {
        if (elementCode != DOCUMENTATION_STATE) {
          invalidMemberException("bindingFaultChannel", currentElement);
        }
      }*/

      currentElement = getNextElement(currentElement);
    }

    return result;
  }

  private WSDLBindingOperation loadBOperation(WSDLNode parent, Element element) throws WSDLException {
    WSDLBindingOperation result = new WSDLBindingOperation(parent);
    result.loadAttributes(element);
    Element currentElement = getFirstChildElement(element);

    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

      switch (elementCode) {
        case INPUT_STATE: {
          if (result.getInput() != null) {
            throw new WSDLException(" WSDL binding operation can not have multiple 'input' elements !");
          }

          WSDLBindingChannel input = loadBindingChannel(result, currentElement);
          result.setInput(input);
          break;
        }
        case OUTPUT_STATE: {
          if (result.getOutput() != null) {
            throw new WSDLException(" WSDL binding operation can not have multiple 'output' elements !");
          }

          WSDLBindingChannel output = loadBindingChannel(result, currentElement);
          result.setOutput(output);
          break;
        }
        case FAULT_STATE: {
          WSDLBindingFault fault = loadBindingFault(result, currentElement);
          result.addFault(fault);
          break;
        }
        case EXTENSION_STATE: {
          WSDLExtension extension = loadExtension(result, currentElement);
          result.addExtension(extension);
          break;
        }
        case DOCUMENTATION_STATE: {
          break; // Do nothing
        }
        case SAP_USE_FEATURE_STATE: {
          SAPUseFeature useFeature = loadUseFeatureState(result, currentElement);
          result.addUseFeatire(useFeature);
          break;
        }
        /*default: {
          invalidMemberException("bindingOperation", currentElement);
        }*/
      }

      currentElement = getNextElement(currentElement);
    }

    return result;
  }

  /**
   * Loades WSDL Binding contents.
   */
  private WSDLBinding loadBinding(WSDLNode parent, Element element) throws WSDLException {
    WSDLBinding result = new WSDLBinding(parent);
    result.loadAttributes(element);
    Element currentElement = getFirstChildElement(element);

    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

      switch (elementCode) {
        case OPERATION_STATE: {
          WSDLBindingOperation operation = loadBOperation(result, currentElement);
          result.addOperation(operation);
          break;
        }
        case EXTENSION_STATE: {
          WSDLExtension extension = loadExtension(result, currentElement);
          result.addExtension(extension);
          break;
        }        
        case SAP_USE_FEATURE_STATE: {
          SAPUseFeature useFeature = loadUseFeatureState(result, currentElement);
          result.addUseFeature(useFeature);
          break;
        }
        case DOCUMENTATION_STATE: {
          WSDLDocumentation documentation = loadDocumentation(result,currentElement);
          result.setDocumentation(documentation);
          break;
        }
      }

      currentElement = getNextElement(currentElement);
    }

    return result;
  }

  /**
   * Loads WSDL port contents.
   */
  private WSDLPort loadPort(WSDLNode parent, Element element) throws WSDLException {
    WSDLPort result = new WSDLPort(parent);
    result.loadAttributes(element);
    Element currentElement = getFirstChildElement(element);

    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

      if (elementCode == EXTENSION_STATE) {
        if (result.getExtension() != null) {
          throw new WSDLException(" WSDL 'port' can not have more than one extension elements ! Check port '"+result.getName()+"' contents !");
        }

        WSDLExtension extension = loadExtension(result, currentElement);
        result.setExtension(extension);
      } else if (elementCode == DOCUMENTATION_STATE) {
        WSDLDocumentation documentation = loadDocumentation(result,currentElement);
        result.setDocumentation(documentation);
      } else {
        throw new WSDLException(" Ivalid WSDL file !"); // This should not happen
      }

      currentElement = getNextElement(currentElement);
    }

    return result;
  }

  /**
   * Loads WSDL Extension contents.
   */
  private WSDLExtension loadExtension(WSDLNode parent, Element element) throws WSDLException {
    WSDLExtension result = new WSDLExtension(parent);
    result.setLocalName(element.getLocalName());
    result.setURI(element.getNamespaceURI());
    result.loadAttributes(element);
    Element currentElement = getFirstChildElement(element);

    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

      if (elementCode == EXTENSION_STATE) {
        WSDLExtension extension = loadExtension(result, currentElement);
        result.addChild(extension);
      } else if (elementCode != DOCUMENTATION_STATE) {
        throw new WSDLException(" Incorrect WSDL file ! Please do not mix valid WSDL Element's in wsdl extension elements !");
      }

      currentElement = getNextElement(currentElement);
    }

    return result;
  }

  /**
   * Loads SAP UseFeature Element.
   */ 
  private SAPUseFeature loadUseFeatureState( WSDLNode parent, Element element) throws WSDLException {
    SAPUseFeature result = new SAPUseFeature(parent);
    result.loadAttributes(element);
    return result;
  }
  
  /**
   * Loads SAP Option Element.
   */ 
  private SAPOption loadOptionState( WSDLNode parent, Element element) throws WSDLException {
    SAPOption result = new SAPOption(parent);
    result.loadAttributes(element);
    return result;
  }

  /**
   * Loads SAP Property Element.
   */ 
  private SAPProperty loadPropertyState( WSDLNode parent, Element element) throws WSDLException {
    SAPProperty result = new SAPProperty(parent);
    result.loadAttributes(element);
    Element currentElement = getFirstChildElement(element);
    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());
      switch (elementCode) {
        case SAP_OPTION_STATE: {
          SAPOption option = loadOptionState(result, currentElement);
          result.addOption(option);
        }
      }
      currentElement = getNextElement(currentElement);      
    }
    return result;
  }
  /**
   * Loads SAP Feature element.
   */ 
  private SAPFeature loadFeatureState( WSDLNode parent, Element element)  throws WSDLException {
    SAPFeature result = new SAPFeature(parent);
    result.loadAttributes(element);
    Element currentElement = getFirstChildElement(element);
    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());      
      switch (elementCode) {
        case SAP_PROPERTY_STATE: {
          SAPProperty property = loadPropertyState(result, currentElement);
          result.addProperty(property);
        }
      }
      currentElement = getNextElement(currentElement);
    }
    return result;
  }

  /**
   * Loads the documentation tag
   * @param parent
   * @param element
   * @return
   * @throws WSDLException
   */
  private WSDLDocumentation loadDocumentation(WSDLNode parent, Element element) throws WSDLException {
    element.normalize();
    WSDLDocumentation result = new WSDLDocumentation(parent);
    Element elementContent = getFirstChildElement(element);
    if (elementContent != null) {
      result.setElementContent(elementContent);
    } else {
      Node node = element.getFirstChild();
      if (node != null) {
        StringBuffer content = new StringBuffer();
        while (node != null) {
          boolean flag = true;
          if (node.getNodeType() == Node.TEXT_NODE) {
            content.append(((Text) node).getData());
            flag = false;
          }
          if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
            content.append(((CDATASection) node).getData());
            flag = false;
          }
          if (node.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
            content.append(node.getNodeValue());
            flag = false;
          }
          if (flag) {
            break;
          }
          node = node.getNextSibling();
        }
        result.setContent(new CharArray(content.toString().trim()));
      }
    }
    return result;
  }

  /**
   * Loads service contents.
   */
  private WSDLService loadService(WSDLNode parent, Element element) throws WSDLException {
    WSDLService result = new WSDLService(parent);
    result.loadAttributes(element);
    Element currentElement = getFirstChildElement(element);

    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

      switch (elementCode) {
        case PORT_STATE: {
          WSDLPort port = loadPort(result, currentElement);
          result.addPort(port);
          break;
        }
        case EXTENSION_STATE: {
          WSDLExtension extension = loadExtension(result, currentElement);
          result.setExtension(extension);
          break;
        }
        case DOCUMENTATION_STATE: {
          WSDLDocumentation documentation = loadDocumentation(result,currentElement);
          result.setDocumentation(documentation);
          break;
        }
        /*default: {
          invalidMemberException("service", currentElement);
        }*/
      }

      currentElement = getNextElement(currentElement);
    }

    return result;
  }

  /**
   * Loads Types contents.
   */
  private WSDLTypes loadTypes(WSDLDefinitions parent, Element element) throws WSDLException {
    Element currentElement = getFirstChildElement(element);

    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

      if (elementCode == SCHEMA_STATE) { // This is XML schema
        parent.addSchema(new DOMSource(currentElement, parent.getSystemId()),parent.getTargetNamespace());
      } /*else if (elementCode != DOCUMENTATION_STATE) {
        invalidMemberException("types", currentElement);
      }*/

      currentElement = getNextElement(currentElement);
    }

    return new WSDLTypes(parent);
  }

  /**
   * Loads WSDL fault contents.
   */
  private WSDLChannel loadChannel(WSDLNode parent, Element element) throws WSDLException {
    WSDLChannel result = new WSDLChannel(parent);
    result.loadAttributes(element);

    if (getFirstChildElement(element) != null) {
      throw new WSDLException(" WSDL 'operation' element can not have child elements !");
    }

    return result;
  }

  /**
   * Loads WSDL fault contents.
   */
  private WSDLFault loadFault(WSDLNode parent, Element element) throws WSDLException {
    WSDLFault result = new WSDLFault(parent);
    result.loadAttributes(element);

    if (getFirstChildElement(element) != null) {
      throw new WSDLException(" WSDL 'fault' element can not have child elements !");
    }

    return result;
  }

  /**
   * Loads operation contents.
   */
  private WSDLOperation loadOperation(WSDLNode parent, Element element) throws WSDLException {
    WSDLOperation result = new WSDLOperation(parent);
    result.loadAttributes(element);
    Element currentElement = getFirstChildElement(element);

    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

      switch (elementCode) {
        case INPUT_STATE: {
          if (result.getInput() != null) {
            throw new WSDLException(" More than one WSDL 'input' can not contain in WSDL 'operation' !");
          }

          WSDLChannel input = loadChannel(result, currentElement);
          result.setInput(input);
          break;
        }
        case OUTPUT_STATE: {
          if (result.getOutput() != null) {
            throw new WSDLException(" More than one WSDL 'output' can not contain in WSDL 'operation' !");
          }

          WSDLChannel output = loadChannel(result, currentElement);
          result.setOutput(output);
          break;
        }
        case FAULT_STATE: {
          WSDLFault fault = loadFault(result, currentElement);
          result.addFault(fault);
          break;
        }
        case SAP_USE_FEATURE_STATE: {
          SAPUseFeature useFeature = loadUseFeatureState(result, currentElement);
          result.addUseFeature(useFeature);
          break;
        }
        case DOCUMENTATION_STATE: {
          WSDLDocumentation documentation = loadDocumentation(result, currentElement);
          result.setDocumentation(documentation);
          break;
        }
        /*default: {
          MemberException("option", currentElement);
        }*/
      }

      currentElement = getNextElement(currentElement);
    }

    return result;
  }

  /**
   * Loads portType contents.
   */
  private WSDLPortType loadPortType(WSDLNode parent, Element element) throws WSDLException {
    WSDLPortType result = new WSDLPortType(parent);
    result.loadAttributes(element);
    Element currentElement = getFirstChildElement(element);

    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

      if (elementCode == OPERATION_STATE) {
        WSDLOperation operation = loadOperation(result, currentElement);
        result.addOperation(operation);
      } else if (elementCode == SAP_USE_FEATURE_STATE) {
        SAPUseFeature useFeature = loadUseFeatureState(result, currentElement);
        result.addUseFeature(useFeature);
      } else if (elementCode == DOCUMENTATION_STATE) {
        WSDLDocumentation documentation = loadDocumentation(result,currentElement);
        result.setDocumentation(documentation);
      } /*else {
        invalidMemberException("portType", currentElement);
      }*/

      currentElement = getNextElement(currentElement);
    }

    return result;
  }

  /**
   * Loads WSDL part contents.
   */
  private WSDLPart loadPart(WSDLNode parent, Element element) throws WSDLException {
    WSDLPart result = new WSDLPart(parent);
    result.loadAttributes(element);

    Element currentElement = getFirstChildElement(element);
    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());
      if (elementCode != DOCUMENTATION_STATE) {
        throw new WSDLException(" WSDL 'part' can only have 'documentation' children !");
      }
      currentElement = getNextElement(currentElement);
    }
    return result;
  }

  /**
   * Loads WSDL message contents and returns the new instance.
   */
  private WSDLMessage loadMessage(WSDLNode parent, Element element) throws WSDLException {
    WSDLMessage result = new WSDLMessage(parent);
    result.loadAttributes(element);
    Element currentElement = getFirstChildElement(element);

    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

      if (elementCode == PART_STATE) {
        WSDLPart part = loadPart(result, currentElement);
        result.addPart(part);
      } else if (elementCode == DOCUMENTATION_STATE) {
        WSDLDocumentation documentation = loadDocumentation(result, currentElement);
        result.setDocumentation(documentation);
      }/* else {
        invalidMemberException("message", currentElement);
      }*/

      currentElement = getNextElement(currentElement);
    }

    return result;
  }

  /**
   * Loads import statement.
   */
  private void loadImportState(WSDLDefinitions definitions, Element importNode) throws WSDLException {
    String rootId = definitions.getSystemId();
    String location = importNode.getAttribute("location");

    if (location.length() == 0) {
      throw new WSDLException(" Error in WSDL 'import' no or empty location attribute !");
    }
    // Resolves external location
    DOMSource newDocument;
//    InputSource in = null;
//    if (wsdlResolver != null) {
//      try {
//        in = wsdlResolver.resolveEntity(rootId, location);
//      } catch (SAXException ioe) {
//      } catch (IOException ioe) {
//        throw new WSDLException("Cannot load wsdl: base=" + rootId + ", relative=" + location, ioe);
//      }
//    }
    URL locationURL = null;
//    if (in == null) {
      locationURL = resolveUri(rootId, location);
      newDocument = loadDOMDocument(locationURL.toExternalForm());
//    } else {
//      newDocument = loadDOMDocument(in.getByteStream(), in.getSystemId());
//    }
    Element rootElement = (Element) newDocument.getNode();
    int elementCode = getElementCode(rootElement.getNamespaceURI(), rootElement.getLocalName());

    switch (elementCode) {
      case DEFINITIONS_STATE: { // This must be WSDL document
        if (systemIdMapping != null) {
          definitions.setMirrorLocation(locationURL.toString(),(String) reverseMapping.get(locationURL.toString()));
        }
        WSDLDefinitions importedDefinitions = loadDefinitions(newDocument);
        joinWSDL(definitions, importedDefinitions);
        break;
      }
      case SCHEMA_STATE: { // This must be Schema document
        if (systemIdMapping != null) {
          definitions.setMirrorLocation(locationURL.toString(),(String) reverseMapping.get(locationURL.toString()));
        }
        definitions.addSchema(new DOMSource(rootElement, locationURL.toExternalForm()));
        break;
      }
      default: {
        throw new WSDLException(" Invalid document imported. It must be valid WSDL File or Schema document !");
      }
    }
    WSDLImport importStatement = new WSDLImport(definitions);
    importStatement.loadAttributes(importNode);
    definitions.getImportDeclaratuions().add(importStatement);
  }

  /**
   * Loads WSDL definitions contents and returns the new instance.
   */
  public WSDLDefinitions loadDefinitions(DOMSource source) throws WSDLException {
    Node node = source.getNode();

    if (node.getNodeType() != Node.ELEMENT_NODE) {
      throw new WSDLException(" DOM Source passed can not be used to load WSDL Definitions !");
    }

    Element element = (Element) node;
    int nodeCode = getElementCode(element.getNamespaceURI(), element.getLocalName());

    if (nodeCode != DEFINITIONS_STATE) {
      throw new WSDLException(" Element passed is not WSDLD Document !");
    }

    WSDLDefinitions result = new WSDLDefinitions();
    if (source.getSystemId() != null) {
      try {
        String id = source.getSystemId();
        if (wsdlResolver == null) {
          id = URLLoader.fileOrURLToURL(null, id).toString();
        }
        source.setSystemId(id);
      } catch (IOException e) {
        throw new WSDLException("System id "+source.getSystemId()+" can not be converted to url resource !",e);
      }
    }
    result.setSystemId(source.getSystemId());
    result.loadAttributes(element);
    if (this.systemIdMapping != null) {
      result.setMirrorMapping(systemIdMapping);
      if (result.getSystemId() != null && systemIdMapping.get("") != null && result.getSystemId().equals(this.documentId)) {
        result.setMirrorLocation(result.getSystemId(),(String) systemIdMapping.get(""));
      }
    }
    Element currentElement = getFirstChildElement(element);

    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

      switch (elementCode) {
        case TYPES_STATE: { // Loads types element
//          if (result.types != null) {
//            throw new WSDLException(" WSDL 'types' element not allowed more than once in WSDL !");
//          }

          WSDLTypes types = loadTypes(result, currentElement);
          result.types = types;
          break;
        }
        case MESSAGE_STATE: { // Loads WSDL message
          WSDLMessage message = loadMessage(result, currentElement);
          result.addMessage(message);
          break;
        }
        case PORTTYPE_STATE: { // Loads PortType
          WSDLPortType porttype = loadPortType(result, currentElement);
          result.addPortType(porttype);
          break;
        }
        case BINDING_STATE: { // Loads Binding
          WSDLBinding binding = loadBinding(result, currentElement);
          result.addBinding(binding);
          break;
        }
        case SERVICE_STATE: { // Loads Service
          WSDLService service = loadService(result, currentElement);
          result.addService(service);
          break;
        }
        case IMPORT_STATE: { // Loads Import
          loadImportState(result, currentElement);
          break;
        }
        case DOCUMENTATION_STATE: {
          WSDLDocumentation documentation = loadDocumentation(result,currentElement);
          result.setDocumentation(documentation);
          break;
        }
        case SAP_FEATURE_STATE: { // SAP Feature
          SAPFeature feature = loadFeatureState(result, currentElement);
          result.addFeature(feature);
          break;
        }
        case EXTENSION_STATE: {
          WSDLExtension extension = loadExtension(result, currentElement);
          result.addExtension(extension);
          break;
        }
        /*default: {
          invalidMemberException("definitions", currentElement);
        }*/
      }

      currentElement = getNextElement(currentElement);
    }

    return result;
  }

  private Element getFirstChildElement(Element element) {
    Node child = element.getFirstChild();

    while (child != null) {
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        return (Element) child;
      }

      child = child.getNextSibling();
    }

    return null;
  }

  private Element getNextElement(Element element) {
    Node next = element.getNextSibling();

    while (next != null) {
      if (next.getNodeType() == Node.ELEMENT_NODE) {
        return (Element) next;
      }

      next = next.getNextSibling();
    }

    return null;
  }

  private void invalidMemberException(String partName, Element element) throws WSDLException {
    throw new WSDLException(" WSDL '" + partName + "' can not have child '" + element.getNodeName() + "' of namespace '" + element.getNamespaceURI() + "' !");
  }

  /**
   * Joins the input wsdl to this one / without types definition
   */
  public void joinWSDL(WSDLDefinitions definitions, WSDLDefinitions new_wsdl) throws WSDLException {
    int i;

    for (i = 0; i < new_wsdl.bindings.size(); i++) {
      definitions.addBinding((WSDLBinding) new_wsdl.bindings.get(i));
    } 

    for (i = 0; i < new_wsdl.messages.size(); i++) {
      definitions.addMessage((WSDLMessage) new_wsdl.messages.get(i));
    } 

    for (i = 0; i < new_wsdl.portTypes.size(); i++) {
      definitions.addPortType((WSDLPortType) new_wsdl.portTypes.get(i));
    } 

    for (i = 0; i < new_wsdl.services.size(); i++) {
      definitions.addService((WSDLService) new_wsdl.services.get(i));
    } 

    ArrayList schemaDefinitions = new_wsdl.getSchemaDefinitions();

    for (i = 0; i < schemaDefinitions.size(); i++) {
      definitions.addSchema((DOMSource) schemaDefinitions.get(i), new_wsdl.getTargetNamespace());
    }
    
    ArrayList features = new_wsdl.getFeatures();
    
    for (i=0; i < features.size(); i++) {
      definitions.addFeature((SAPFeature) features.get(i));
    }

    if (this.systemIdMapping != null) {
      Hashtable table = new_wsdl.getMirrorLocations();
      Enumeration keys = table.keys();
      while (keys.hasMoreElements()) {
        String key = (String) keys.nextElement();
        String value = (String) table.get(key);
        definitions.setMirrorLocation(key,value);
      }
    }

    if (new_wsdl.types != null) {
      if (definitions.types == null) {
        definitions.types = new_wsdl.types;
      } else {
        // do not thorow exception not fatal error.
        //throw new WSDLException(" WSDL 'types' can appear only onse in wsdl document including the imported documents !");
      }
    }
  }

  /**
   * Creates File pointer to file from path and throws appropriate exception.
   * @param fileName
   * @return
   * @throws WSDLException if file does not exists ot path given is directory.
   */
//  private File getFile(String fileName) throws WSDLException {
//    File inputFile = new File(fileName);
//    if (!inputFile.exists()) {
//      throw new WSDLException("File named ["+inputFile.getAbsolutePath()+"] does not exists !");
//    }
//    if (inputFile.isDirectory()) {
//      throw new WSDLException("Path ["+inputFile.getAbsolutePath()+"] points to directory not to a file !");
//    }
//    return inputFile;
//  }

  private DOMSource loadDOMDocument(String wsdlLocation) throws WSDLException {
    String oldHost="";
    String oldPort="";
    try {
      //DocumentBuilderFactory factory = new DocumentBuilderFactoryImpl();
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      if (this.proxyHost != null) {
        //TODO: Set this proxy like it should be finally
        //factory.setAttribute("xmlparser.use-proxy","yes");
        //factory.setAttribute("xmlparser.proxy-host",proxyHost);
        //factory.setAttribute("xmlparser.proxy-port",proxyPort);
        oldHost = System.getProperty("http.proxyHost");
        oldPort = System.getProperty("http.proxyPort");
        System.setProperty("http.proxyHost",proxyHost);
        System.setProperty("http.proxyPort",proxyPort);
      }
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document;
      if (wsdlResolver != null) {
        builder.setEntityResolver(wsdlResolver);
        document = builder.parse(wsdlResolver.resolveEntity(null, wsdlLocation));
      } else {
        document = builder.parse(wsdlLocation);
      }
      Element root = document.getDocumentElement();
      URL rooturl = new File(".").toURL();
      URL location = URLLoader.fileOrURLToURL(rooturl,wsdlLocation);
      DOMSource source = new DOMSource(root, location.toString());
      return source;
    } catch (SAXException e) {
      throw new WSDLException("Parser exception occurred:"+e.getMessage(), e);
    } catch (FactoryConfigurationError e) {
      throw new WSDLException("Factory configuration error occurred:"+e.getMessage());
    } catch (ParserConfigurationException e) {
      throw new WSDLException("Parser configuration exception occurred:"+e.getMessage(), e);
    } catch (IOException e) {
      throw new WSDLException("IO Exception occurred while parsing file:"+e.getMessage(), e);
    } finally {
      if (this.proxyHost != null) { // Restore the system property
        //TODO: Set this proxy like it should be finally
        if (oldHost == null || oldPort == null) {
          System.getProperties().remove("http.proxyHost");
          System.getProperties().remove("http.proxyPort");
        } else {
          System.setProperty("http.proxyHost",oldHost);
          System.setProperty("http.proxyPort",oldPort);
        }
      }

    }
  }
  /**
   * Loads DOMSource from input stream and System id and handles exceptions correctly.
   * @param input
   * @param systemId
   * @return
   * @throws WSDLException
   */
  private DOMSource loadDOMDocument(InputStream input, String systemId) throws WSDLException {
    try {
      //DocumentBuilderFactory factory = new DocumentBuilderFactoryImpl();
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(input);
      Element root = document.getDocumentElement();
      DOMSource source = new DOMSource(root, systemId);
      return source;
    } catch (SAXException e) {
      throw new WSDLException("Parser exception occurred:"+e.getMessage(), e);
    } catch (FactoryConfigurationError e) {
      throw new WSDLException("Factory configuration error occurred:"+e.getMessage());
    } catch (ParserConfigurationException e) {
      throw new WSDLException("Parser configuration exception occurred:"+e.getMessage(), e);
    } catch (IOException e) {
      throw new WSDLException("IO Exception occurred while parsing file:"+e.getMessage(), e);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException x) {
          throw new WSDLException("Unable to close input stream",x);
        }
      }
    }
  }

  /**
   * Loads wsdl definitions from InputStream and systemId.
   * @param input
   * @return
   * @throws WSDLException
   */
  public WSDLDefinitions loadWSDLDocument(InputStream input) throws WSDLException {
    //SystemProperties.setProperty(DocumentBuilderFactory.class.getName(),com.sap.engine.lib.jaxp.DocumentBuilderFactoryImpl.class.getName());
    systemIdMapping = null;
    DOMSource source  = loadDOMDocument(input, "");
    return loadDefinitions(source);
  }

  /**
   * Loads wsdl definitions from InputStream and systemId.
   * @param input
   * @param systemId
   * @return
   * @throws WSDLException
   */
  public WSDLDefinitions loadWSDLDocument(InputStream input, String systemId) throws WSDLException {
    //SystemProperties.setProperty(DocumentBuilderFactory.class.getName(),com.sap.engine.lib.jaxp.DocumentBuilderFactoryImpl.class.getName());
    systemIdMapping = null;
    DOMSource source  = loadDOMDocument(input, systemId);
    return loadDefinitions(source);
  }


  /**
   * Loads WSDL Definitions structure from file.
   * @param wsdlFileName
   * @return Returns built wsdl definitiond.
   * @throws WSDLException
   */
  public WSDLDefinitions loadWSDLDocument(String wsdlFileName) throws WSDLException {
    systemIdMapping = null;
    DOMSource source = null;
    source = loadDOMDocument(wsdlFileName);
    return loadDefinitions(source);
  }

  /**
   * Loads WSDL Definitions from mirror location (cache).
   * Must provide a hashtable with mapping with the following
   * "" -> Original WSDL Location
   * "All imported absolute locations" -> local relative locations
   * This map is returned from WSDL Import tool
   */
  public WSDLDefinitions loadMirrorWSDLDocument(String wsdlFileName, Hashtable systemIdMap) throws WSDLException {
    this.systemIdMapping = systemIdMap;
    this.reverseMapping.clear();
    try {
      URL rooturl = new File(".").toURL();
      URL location = URLLoader.fileOrURLToURL(rooturl, wsdlFileName);

      this.documentId = location.toString(); //URLLoader.fileOrURLToURL(null,wsdlFileName).toString();
    } catch (IOException e) {
      throw new WSDLException("Unable to convert path "+wsdlFileName+" to url !",e);
    }
    DOMSource source = null;
    //File inputFile = getFile(wsdlFileName);
    //this.documentId = inputFile.getAbsolutePath();
    source = loadDOMDocument(wsdlFileName);
    return loadDefinitions(source);
  }

  /**
   * Loads WSDL Definitions from mirror location (cache).
   * @param input
   * @param systemId
   * @param systemIdMap
   * @return
   * @throws WSDLException
   */
  public WSDLDefinitions loadMirrorWSDLDocument(InputStream input, String systemId, Hashtable systemIdMap) throws WSDLException {
    this.systemIdMapping = systemIdMap;
    this.reverseMapping.clear();
    try {
      this.documentId = URLLoader.fileOrURLToURL(null,systemId).toString();
    } catch (IOException e) {
      throw new WSDLException("Unable to convert path "+systemId+" to url !",e);
    }
    DOMSource source = null;
    source = loadDOMDocument(input,systemId);
    return loadDefinitions(source);
  }

  private static void generateWSDLTypes(String wsdlPath, String outPath, String outpackage) throws WSDLException {
    File wsdlFile = new File(wsdlPath);

    if (wsdlFile.isFile() == false) {
      throw new WSDLException("{" + wsdlPath + "} is not a file !");
    }

    File outDir = new File(outPath);

    if (outDir.isDirectory() == false) {
      throw new WSDLException("{" + outPath + "} is not a directory !");
    }

    //SystemProperties.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sap.engine.lib.jaxp.DocumentBuilderFactoryImpl");
    //SystemProperties.setProperty("javax.xml.parsers.TransformerFactory", "com.sap.engine.lib.jaxp.TransformerFactoryImpl");
    WSDLDOMLoader loader = new WSDLDOMLoader();
    WSDLDefinitions definitions = loader.loadWSDLDocument(wsdlPath);
    definitions.loadSchemaInfo();
    SchemaToJavaGenerator schema = definitions.getSchemaInfo();
    try {
    if (schema != null) {
      //ArrayList schemaContent = definitions.getSchemaDefinitions();
      schema.generateAll(outDir, outpackage);
      PackageBuilder builder = new PackageBuilder();
      builder.setPackageRoot(outDir);
      builder.setPackageName(outpackage);
      builder.compilePackage();
    }
    } catch (SchemaToJavaGeneratorException e) {
      throw new WSDLException("Schema to java Generator problem when generating files. See nested Exception:",e);
    } catch (Exception e) {
      throw new WSDLException("Problem in compiling generated files. See nested exception:",e);
    }
  }
  /*
  public static void testWSDL(String inputPath, String outPath) throws Exception {
    File wsdlFile = new File(inputPath);
    File outDir = new File(outPath);

    if (outDir.exists() == false) {
      outDir.mkdirs();
    }

    if (outDir.isDirectory() == false) {
      throw new Exception("{" + outPath + "} is not a directory !");
    }

    if (wsdlFile.isFile()) {
      System.out.println("Parsing : " + wsdlFile.getName() + "   {" + wsdlFile.getAbsolutePath() + "}");//$JL-SYS_OUT_ERR$
      try {
        generateWSDLTypes(wsdlFile.getAbsolutePath(), outDir.getCanonicalPath(), wsdlFile.getName().substring(0, wsdlFile.getName().lastIndexOf(".")));
        System.out.println("Success ! ");//$JL-SYS_OUT_ERR$
      } catch (Exception e) {
//      $JL-EXC$
        System.out.println("Failed ! " + e.getMessage());//$JL-SYS_OUT_ERR$
        e.printStackTrace();
      }
    } else {
      File files[] = wsdlFile.listFiles();
      int fileFailed = 0;
      int succesCount = 0;

      if (files == null) {
        System.out.println(" File not Found !");//$JL-SYS_OUT_ERR$
        return;
      }

      for (int i = 0; i < files.length; i++) {
        if (files[i].isFile() && files[i].getName().endsWith(".wsdl")) {
          System.out.println("Parsing : " + wsdlFile.getName() + "   {" + files[i].getAbsolutePath() + "}");//$JL-SYS_OUT_ERR$
          try {
            generateWSDLTypes(files[i].getAbsolutePath(), outDir.getAbsolutePath(), files[i].getName().substring(0, files[i].getName().lastIndexOf(".")));
            succesCount++;
            System.out.println("Success ! ");//$JL-SYS_OUT_ERR$
          } catch (Exception e) {
//          $JL-EXC$
            System.out.println("Failed ! " + e.getMessage());//$JL-SYS_OUT_ERR$
            e.printStackTrace();
            fileFailed++;
          }
        }
      } 

      System.out.println("----------------------------");//$JL-SYS_OUT_ERR$
      System.out.println("Parsed : " + (succesCount + fileFailed));//$JL-SYS_OUT_ERR$
      System.out.println("OK     : " + succesCount);//$JL-SYS_OUT_ERR$
      System.out.println("Failed : " + fileFailed);//$JL-SYS_OUT_ERR$
      System.out.println();//$JL-SYS_OUT_ERR$
    }
  }
*/
}

