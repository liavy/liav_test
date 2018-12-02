/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.wsdl.wsdl11;  

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.sap.engine.lib.xml.Symbols;
import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.lib.xml.parser.URLLoader;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.espbase.wsdl.AbstractOperation;
import com.sap.engine.services.webservices.espbase.wsdl.AttachmentsContainer;
import com.sap.engine.services.webservices.espbase.wsdl.Base;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Endpoint;
import com.sap.engine.services.webservices.espbase.wsdl.ExtensionContext;
import com.sap.engine.services.webservices.espbase.wsdl.ExtensionElement;
import com.sap.engine.services.webservices.espbase.wsdl.HTTPBinding;
import com.sap.engine.services.webservices.espbase.wsdl.HTTPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.Interface;
import com.sap.engine.services.webservices.espbase.wsdl.NSURIResolver;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Operation;
import com.sap.engine.services.webservices.espbase.wsdl.Parameter;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBinding;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.Service;
import com.sap.engine.services.webservices.espbase.wsdl.XSDRef;
import com.sap.engine.services.webservices.espbase.wsdl.XSDTypeContainer;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLUnmarshalException;
import com.sap.engine.services.webservices.tools.SharedDocumentBuilders;
import com.sap.engine.services.webservices.tools.SharedTransformers;


//import static java.lang.System.out;
/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-19
 */
public class WSDL11Loader {

  /**
   * These constants represent constants for main elements in WSDL Document.
   */
  //private static final int DOCUMENT_STATE = -1;
  private static final int EXTENSION_STATE = 0;
  private static final int DEFINITIONS_STATE = 1;
  private static final int TYPES_STATE = 2;
  private static final int SCHEMA_STATE = 3;
  private static final int MESSAGE_STATE = 4;
  private static final int PART_STATE = 5;
  private static final int PORTTYPE_STATE = 6;
  private static final int OPERATION_STATE = 7;
  private static final int INPUT_STATE = 8;
  private static final int OUTPUT_STATE = 9;
  private static final int FAULT_STATE = 10;
  private static final int BINDING_STATE = 11;
  //private static final int BOPERATION_STATE = 12;
  //private static final int BINPUT_STATE = 13;
  //private static final int BOUTPUT_STATE = 14;
  //private static final int BFAULT_STATE = 15;
  private static final int SERVICE_STATE = 16;
  private static final int PORT_STATE = 17;
  private static final int DOCUMENTATION_STATE = 18;
  private static final int IMPORT_STATE = 19;
  //private static final int SAP_FEATURE_STATE = 20;
  //private static final int SAP_PROPERTY_STATE = 21;
  //private static final int SAP_OPTION_STATE = 22;
  //private static final int SAP_USE_FEATURE_STATE = 23;

  private static final Map EMPTY_MAP = new HashMap();
//private HashStringtoInt elementMapping; // Mapping of all wsdl names to internal id-s
//private HashStringtoInt elementMapping2; // Mapping of all sap wsdl names to internal id-s
  private Hashtable systemIdMapping; // Maps remote SystemId
  private Hashtable reverseMapping;  // Maps local locations to Remote Locations
  private String documentId;
  private String proxyHost = null;
  private String proxyPort = null;
  private EntityResolver wsdlResolver;
  private URIResolver uriResolver;

  private static final String SYSTEM_ID  =  "systemId";
  //private static final String HTTP_HOST  =  "http.proxyHost";
  //private static final String HTTP_PORT  =  "http.proxyPort";

  private Hashtable messMappings = new Hashtable(); //key msg QName, value message Element
  private Definitions definitions = new Definitions(); //this is the result Definitions object
  private final Map filter = new Hashtable(); //used for extension elements and attributes filter
  //private Set aboutToLoadedWDSLs = new HashSet(); //set, containing the targetNamespace of the wsdls that are about to be loaded. Used to preserve from clycling imports...
 
  /**
   * Default constructor.
   */
  public WSDL11Loader() throws WSDLException {
    documentId = null;
    this.systemIdMapping = null;
    this.reverseMapping = new Hashtable();
    definitions.setProperty(Definitions.VERSION, Definitions.WSDL11); //denote that this is WSDL11 definition
    definitions.getXSDTypeContainer().setURIResolver(new SchemaURIResolver()); 
  }

  /**
   * Sets Htpp proxy for resolving url based wsdl-z
   * @param proxyHost
   * @param proxyPort
   */
  public void setHttpProxy(String proxyHost,String proxyPort) {
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
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
    if (uri.equals(WSDL11Constants.WSDL_NS)) {
      if ("definitions".equals(localName)) return DEFINITIONS_STATE;
      if ("types".equals(localName)) return TYPES_STATE;
      if ("message".equals(localName)) return MESSAGE_STATE;
      if ("part".equals(localName)) return PART_STATE;
      if ("portType".equals(localName)) return PORTTYPE_STATE;
      if ("operation".equals(localName)) return OPERATION_STATE;
      if ("input".equals(localName)) return INPUT_STATE;
      if ("output".equals(localName)) return OUTPUT_STATE;
      if ("fault".equals(localName)) return FAULT_STATE;
      if ("binding".equals(localName)) return BINDING_STATE;
      if ("service".equals(localName)) return SERVICE_STATE;
      if ("port".equals(localName)) return PORT_STATE;
      if ("documentation".equals(localName)) return DOCUMENTATION_STATE;
      if ("import".equals(localName)) return IMPORT_STATE;
    }

    if (uri.equals(WSDL11Constants.SCHEMA_NS)) {
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
        throw new WSDLException(e);
      }
    } else {
      try {
        URL rootURL = URLLoader.fileOrURLToURL(null, baseUri);
        URL locationURL = URLLoader.fileOrURLToURL(rootURL, relativeUri);
        return locationURL;
      } catch (IOException i) {
        throw new WSDLException(i);
      }
    }
  }

  public void setURIResolver(URIResolver uriResolver) {
    this.uriResolver = uriResolver;
  }

  public void setWSDLResolver(EntityResolver wsdlResolver) {
    this.wsdlResolver = wsdlResolver;
  }

  /**
   * Loads import statement.
   */
  
  private void checkImportAttributes(Element importNode, String location, String namespace) throws WSDLUnmarshalException{
    if (location == null || location.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{transform(importNode), WSDL11Constants.LOCATION_ATTR});
    }

    if (namespace == null || namespace.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{transform(importNode), WSDL11Constants.NAMESPACE_ATTR});
    }       
  }

  private void getAllImportedWsdlsAndLoadSchemas(DOMSource startDOM, Map<String, DOMSource> output) throws WSDLException{
    Element curElement, rootElement;
    Node node;
    DOMSource newDocument;
    String namespace, location;
    int elementCode, length;
    Object o;
    String rootId = startDOM.getSystemId();
    Element defElement = (Element)startDOM.getNode();
    NodeList defChildren = defElement.getChildNodes();
    
    String tns = defElement.getAttribute(Definitions.TARGET_NS);
    String key = startDOM.getSystemId() != null && this.systemIdMapping == null ? tns+"{"+startDOM.getSystemId()+"}"  : tns;
    /*if (aboutToLoadedWDSLs.contains(setKeyTns)) {
      return;
    }
    aboutToLoadedWDSLs.add(setKeyTns);*/
    if (output.get(key) != null){
      return;
    }
    output.put(key, startDOM);

    // look for import elements
    length = defChildren.getLength();
    for (int i = 0; i < length; i++) {
      node =  defChildren.item(i);
      if (node.getNodeType() != Node.ELEMENT_NODE) {
        continue;
      }
      curElement = (Element)node;
      elementCode = getElementCode(curElement.getNamespaceURI(), curElement.getLocalName());
      if (elementCode == TYPES_STATE){
        loadTypes(definitions.getXSDTypeContainer(), curElement, rootId);      
        continue;
      }
      if (elementCode != IMPORT_STATE) {
        continue;
      }        
      // import element found, it should be wsdl or xsd
      location = curElement.getAttribute(WSDL11Constants.LOCATION_ATTR);
      namespace = curElement.getAttribute(WSDL11Constants.NAMESPACE_ATTR);
      checkImportAttributes(curElement, location, namespace);
      

      //  Resolves external location
      o = resolveImport(rootId, location, namespace);    
      if (o instanceof DOMSource) {
        newDocument = (DOMSource) o;
      } else if (o instanceof InputSource) {
        InputSource in = (InputSource) o;
        newDocument = SharedDocumentBuilders.loadDOMDocument(in.getByteStream(), in.getSystemId());
      } else {
        newDocument = SharedDocumentBuilders.loadDOMDocument((String) o,proxyHost,proxyPort,wsdlResolver);
      }

      // check whether this import has already been processed
      //tns = curElement.getAttribute(WSDL11Constants.NAMESPACE_ATTR);
      //setKeyTns = newDocument.getSystemId() != null && this.systemIdMapping == null ? tns+"{"+newDocument.getSystemId()+"}"  : tns;
      /*if (aboutToLoadedWDSLs.contains(setKeyTns)) {
        return;
      }
      aboutToLoadedWDSLs.add(setKeyTns);*/
      
      
      //String definitionsTargetNS = defElement.getAttribute(Definitions.TARGET_NS);
      rootElement = (Element) newDocument.getNode();
      elementCode = getElementCode(rootElement.getNamespaceURI(), rootElement.getLocalName());

      if (elementCode == DEFINITIONS_STATE) {
        //loadDefinitions(newDocument);
        //Object previous = output.put(newDocument.getSystemId(), newDocument);
        getAllImportedWsdlsAndLoadSchemas(newDocument, output);
      } else if (elementCode == SCHEMA_STATE) {
        //out.println("loading schema from import");
        resolveAndLoadSchema(definitions.getXSDTypeContainer(), newDocument, rootId);      
      } else {
        throw new WSDLUnmarshalException(WSDLUnmarshalException.INVALID_DEFINITIONS_ELEMENT, new Object[]{Definitions.WSDL11, transform(rootElement)});
      }
    }
  }

  /**
   * Loads WSDL definitions contents and returns the new instance.
   */
  private Definitions loadDefinitions(DOMSource source) throws WSDLException {
    Node node = source.getNode();

    if (node.getNodeType() != Node.ELEMENT_NODE) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.INVALID_DEFINITIONS_ELEMENT, new Object[]{Definitions.WSDL11, transform(node)});
    }

    Element defElement = (Element) node;
    int nodeCode = getElementCode(defElement.getNamespaceURI(), defElement.getLocalName());

    if (nodeCode != DEFINITIONS_STATE) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.INVALID_DEFINITIONS_ELEMENT, new Object[]{Definitions.WSDL11, transform(defElement)});
    }

    if (source.getSystemId() != null) {
      try {
        String id = source.getSystemId();
        if (wsdlResolver == null) {
          id = URLLoader.fileOrURLToURL(null, id).toString();
        }
        source.setSystemId(id);
      } catch (IOException e) {
        throw new WSDLException(e);
      }
    }

    //register the tns into tns-set.
    String rootDefinitionsTargetNS = defElement.getAttribute(Definitions.TARGET_NS);
    String currentDomTargetNS;
    String systemId = source.getSystemId();
    
    Map<String, DOMSource> domSourcesHash = new HashMap<String, DOMSource>();
    //domSourcesHash.put(systemId, source); // !! starting source must be in the map before adding imports
    getAllImportedWsdlsAndLoadSchemas(source, domSourcesHash);
    
    if(definitions.getProperty(Definitions.TARGET_NS) == null) {
      definitions.setProperty(Definitions.TARGET_NS, rootDefinitionsTargetNS);
    }

    NodeList defChildren; 
    Element curElement;
    Collection<DOMSource> domSources = domSourcesHash.values();
    
    //load messages
    for (DOMSource dom : domSources){
      defElement = (Element)dom.getNode();
      currentDomTargetNS = defElement.getAttribute(Definitions.TARGET_NS);
      defChildren = defElement.getChildNodes();
      //out.println("looking into dom source [" + dom.getSystemId() + "]");
      for (int i = 0; i < defChildren.getLength(); i++) {
        if (defChildren.item(i).getNodeType() == Node.ELEMENT_NODE) {
          curElement = (Element) defChildren.item(i);
          int elementCode = getElementCode(curElement.getNamespaceURI(), curElement.getLocalName());
          if (elementCode == MESSAGE_STATE) {
            loadMessage(curElement, currentDomTargetNS);
          }        
        }
      }
    }
  


    //load portTypes
    for (DOMSource dom : domSources){
      defElement = (Element)dom.getNode();
      currentDomTargetNS = defElement.getAttribute(Definitions.TARGET_NS);
      defChildren = defElement.getChildNodes();
      //out.println("looking into dom source [" + dom.getSystemId() + "]");
      for (int i = 0; i < defChildren.getLength(); i++) {
        if (defChildren.item(i).getNodeType() == Node.ELEMENT_NODE) {
          curElement = (Element) defChildren.item(i);
          int elementCode = getElementCode(curElement.getNamespaceURI(), curElement.getLocalName());
          if (elementCode == PORTTYPE_STATE) {
            loadPortType(definitions, curElement, currentDomTargetNS, systemId);
          }        
        } 
      }
    }
    
    //load bindings
    for (DOMSource dom : domSources){
      defElement = (Element)dom.getNode();
      currentDomTargetNS = defElement.getAttribute(Definitions.TARGET_NS);
      defChildren = defElement.getChildNodes();
      for (int i = 0; i < defChildren.getLength(); i++) {
        if (defChildren.item(i).getNodeType() == Node.ELEMENT_NODE) {
          curElement = (Element) defChildren.item(i);
          int elementCode = getElementCode(curElement.getNamespaceURI(), curElement.getLocalName());
          if (elementCode == BINDING_STATE) {
            loadBinding(definitions, curElement, currentDomTargetNS);
          }        
        }
      }
    }
    //load services + extension elements
    for (DOMSource dom : domSources){
      defElement = (Element)dom.getNode();
      currentDomTargetNS = defElement.getAttribute(Definitions.TARGET_NS);
      defChildren = defElement.getChildNodes();
      for (int i = 0; i < defChildren.getLength(); i++) {
        if (defChildren.item(i).getNodeType() == Node.ELEMENT_NODE) {
          curElement = (Element) defChildren.item(i);
          int elementCode = getElementCode(curElement.getNamespaceURI(), curElement.getLocalName());
          if (elementCode == SERVICE_STATE) {
            loadService(definitions, curElement, currentDomTargetNS);
          }        
        }
      }
      
      filter.clear();
      filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.SERVICE_ELEMENT), "");
      filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.BINDING_ELEMENT), "");
      filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.PORTTYPE_ELEMENT), "");
      filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.TYPES_ELEMENT), "");
      filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.IMPORT_ELEMENT), "");        
      filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.MESSAGE_ELEMENT), "");        
      loadExtensionElements(definitions, defElement, filter, -1, null, currentDomTargetNS);
    }
    definitions.setProperty(SYSTEM_ID, systemId);

    return this.definitions;
  }

  List<Object[]> potentialExc = new ArrayList();

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
  /**
   * Loads wsdl definitions from InputStream and systemId.
   * @param input
   * @return
   * @throws WSDLException
   */
  public Definitions loadWSDLDocument(DOMSource source) throws WSDLException {
    Definitions result = loadDefinitions(source);
    //#Copy Upper level namespaces to XSD Schemas (fix for internal message 4288676 2008) by Chavdar Baikov (i024072)
    copySchemaNamespaces(result);
    return result;
  }

  /**
   * Copy WSDL Definitions namespaces to XSDs inside the WSDL types section.
   * @param definitions
   */
  private void copySchemaNamespaces(Definitions definitions) {   
    List schemas = definitions.getXSDTypeContainer().getSchemas();
    for (int i = 0; i < schemas.size(); i++) {
      DOMSource domSource = (DOMSource) schemas.get(i);
      Element elementRoot = (Element) domSource.getNode();
      Node parent = elementRoot.getParentNode();
      if (parent != null && parent.getNodeType() != Node.DOCUMENT_NODE) {
        // Get all valid namespace mappings in scope
        Hashtable hash = DOM.getNamespaceMappingsInScope(elementRoot);
        Enumeration enumeration = hash.keys();
        while (enumeration.hasMoreElements()) {
          String key = (String) enumeration.nextElement();
          String value = (String) hash.get(key);
          // Optimization which skips some namespaces which are rarely referenced by XML Schemas.
          if (WSDL11Constants.WSDL_NS.equals(value)) {
            // Usually the xsd does not reference the WSDL namespace 
            continue;
          }
          if (WSDL11Constants.SOAP_NS.equals(value)) {
            // Usually the xsd does not reference the SOAP 1.1 namespace
            continue;
          }
          if ("http://java.sun.com/xml/ns/jaxws".equals(value)) {
            // JAXWS namespace is not used inside the XML Schemas
            continue;
          }
          if (key == null || key.length() == 0) {
            elementRoot.setAttributeNS(NS.XMLNS,"xmlns", value);
          } else {
            elementRoot.setAttributeNS(NS.XMLNS, "xmlns:" + key, value);
          }
        }
      }
    }    
  }

  /**
   * Loads WSDL Definitions from mirror location (cache).
   * Must provide a hashtable with mapping with the following
   * "" -> Original WSDL Location
   * "All imported absolute locations" -> local relative locations
   * This map is returned from WSDL Import tool
   */
  public Definitions loadMirrorWSDLDocument(DOMSource source, Hashtable systemIdMap) throws WSDLException {
    this.systemIdMapping = systemIdMap;
    this.reverseMapping.clear();
    String systemId = source.getSystemId();
    try {
      URL location = URLLoader.fileOrURLToURL(null, systemId);

      this.documentId = location.toString(); 
    } catch (IOException e) {
      throw new WSDLException(e);
    }
    return loadDefinitions(source);
  }

//================================ utility methods ===============================  
  /**
   * The keys in the map are QName values, of attributes that shouldn't be put as extension attributes.
   * @param target the entity to which extension elements will be added
   * @param element the element which contains(wraps) the extension elements
   * @param filter contains QName objects which represents the qname of extension attributes that must be filtered. 
   * @param channelType this parameter is relevant for operation's input, output and fault channels. If it is relevant its value must be from the set {INPUT_STATE, OUTPUT_STATE, FAULT_STATE}.
   * @param faultName this parameter is relevant only when the <code>channelType</code> parameter has value FAULT_STATE. This parameter represents the name of the fault
   *        as it is determined by the <operaration>/<fault>@name attribute value.
   */
  private void loadExtensionAttributes(Base target, Element source, Map filter, int channelType, String faultName) throws WSDLException {
    if (filter == null) {
      filter = EMPTY_MAP; 
    }
    NamedNodeMap attrs = source.getAttributes();
    for (int i = 0; i < attrs.getLength(); i++) {
      Node n = attrs.item(i);
      if (! NS.XMLNS.equals(n.getNamespaceURI())) {//do not add prefix declarations
        QName qname = new QName(n.getNamespaceURI(), n.getLocalName());
        if (filter.get(qname) == null) { 
          if (channelType == INPUT_STATE) {
            ((AbstractOperation) target).appendInputChannelExtensionAttribute(qname, n.getNodeValue());
          } else if (channelType == OUTPUT_STATE) {
            ((AbstractOperation) target).appendOutputChannelExtensionAttribute(qname, n.getNodeValue());
          } else if (channelType == FAULT_STATE) {
            ((AbstractOperation) target).appendFaultExtensionAttribute(faultName, qname, n.getNodeValue());          
          } else {
            target.setExtensionAttr(qname, n.getNodeValue());
          }
        }
      }
    }  
  }  


  /**
   * The map contains as keys QName values of the elements, that shouldn't be
   * put as extension elements.
   * @param base the entity to which extension elements will be added
   * @param element the element which contains(wraps) the extension elements
   * @param filter contains QName objects which represents the qname of extension elements that must be filtered. 
   * @param channelType this parameter is relevant for operation's input, output and fault channels. If it is relevant its value must be from the set {INPUT_STATE, OUTPUT_STATE, FAULT_STATE}.
   * @param faultName this parameter is relevant only when the <code>channelType</code> parameter has value FAULT_STATE. This parameter represents the name of the fault
   *        as it is determined by the <operaration>/<fault>@name attribute value.
   */
  private void loadExtensionElements(Base base, Element element, Map filter, int channelType, String faultName, String definitionsTargetNS) throws WSDLException {
    if (filter == null) {
      filter = EMPTY_MAP; 
    }
    NodeList list = element.getChildNodes();
    Node cur;
    for (int i = 0; i < list.getLength(); i++) {
      cur = list.item(i);
      if (cur.getNodeType() == Node.ELEMENT_NODE) {
        QName qname = new QName(cur.getNamespaceURI(), cur.getLocalName());
        if (filter.get(qname) == null) {//this element should be processed
          ExtensionElement extElement = new ExtensionElement((Element) cur, definitionsTargetNS);
          if (channelType == INPUT_STATE) {
            ((AbstractOperation) base).appendInputChannelExtensionElement(extElement);
          } else if (channelType == OUTPUT_STATE) {
            ((AbstractOperation) base).appendOutputChannelExtensionElement(extElement);            
          } else if (channelType == FAULT_STATE) {
            ((AbstractOperation) base).appendFaultExtensionElement(faultName, extElement);            
          } else { //by default append to the base entity itsefl.
            base.appendChild(extElement);
          }
        }
      }
    }
  }  


//====================================== Types and Messages processing ============================== 
  /**
   * Loads Types contents.
   */
  private void loadTypes(XSDTypeContainer typeContainer, Element element, String systemId) throws WSDLException {
    Element currentElement = getFirstChildElement(element);
//  String systemId = typeContainer.getParent().getProperty(SYSTEM_ID);
    //String systemId = (String) importStack.peek();
    while (currentElement != null) {
      int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());
      if (elementCode == SCHEMA_STATE) { // This is XML schema
        resolveAndLoadSchema(typeContainer, new DOMSource(currentElement, systemId), systemId);
      }
      currentElement = getNextElement(currentElement);
    }
  }

  /**
   * Loads WSDL part contents.
   */
  private Parameter loadPart(Element element, String namespace) throws WSDLException {
    String value = element.getAttribute(WSDL11Constants.NAME_ATTR);
    if (value.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{WSDL11Constants.PART_ELEMENT, WSDL11Constants.NAME_ATTR});
    } 
    checkNCNameAndThrowException(value, "definitions/message/part@name");

    String typeValue = element.getAttribute(WSDL11Constants.TYPE_ATTR);
    String elementValue = element.getAttribute(WSDL11Constants.ELEMENT_ATTR);   
    if (elementValue.length() > 0 && typeValue.length() > 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.TYPE_AND_ELEMENT_ATTRIBUTES_AVAILABLE);      
    }
    if (elementValue.length() == 0 && typeValue.length() == 0) {
      String param = WSDL11Constants.NAME_ATTR + "/" + WSDL11Constants.TYPE_ATTR;
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{WSDL11Constants.PART_ELEMENT, param});        
    }

    Parameter p = new Parameter(value, Parameter.UNDEFINED);
    QName qname;
    if (typeValue.length() > 0) {
      qname = createQNameFromString(typeValue, element);
      p.appendXSDTypeRef(qname, XSDRef.TYPE);  
    } else {
      qname = createQNameFromString(elementValue, element);
      p.appendXSDTypeRef(qname, XSDRef.ELEMENT);
    }

    loadExtensionElements(p, element, null, -1, null, namespace);
    Map filter = new HashMap();
    filter.put(new QName("", WSDL11Constants.NAME_ATTR), "");
    filter.put(new QName("", WSDL11Constants.TYPE_ATTR), "");
    filter.put(new QName("", WSDL11Constants.ELEMENT_ATTR), "");
    loadExtensionAttributes(p, element, filter, -1, null);
    return p;
  }
  /**
   * Loads WSDL message contents and returns the new instance.
   */
  private void loadMessage(Element element, String definitionsTargetNS) throws WSDLException {
    String value = element.getAttribute(WSDL11Constants.NAME_ATTR);
    if (value.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{WSDL11Constants.MESSAGE_ELEMENT, WSDL11Constants.NAME_ATTR});
    } 
    checkNCNameAndThrowException(value, "definitions/message@name");

    List parts = new ArrayList();
    QName msgQName = new QName(definitionsTargetNS, value);

    //out.println("message added: [" + msgQName + "]");
    if (messMappings.put(msgQName, element) != null) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MORE_THAN_ONE_MESSAGES, new Object[]{msgQName});
    }
    //create, initialized and add extension context
    ExtensionContext extCtx = new ExtensionContext("message-ext-context:" + msgQName);
    //exclude 'part's for extension elements
    Map filter = new HashMap();
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.PART_ELEMENT), "");
    loadExtensionElements(extCtx, element, filter, -1, null, definitionsTargetNS);
    //exclude @name for extension attributes
    filter.clear();
    filter.put(new QName("", WSDL11Constants.NAME_ATTR), "");
    loadExtensionAttributes(extCtx, element, filter, -1, null);
    definitions.appendChild(extCtx);
  }

  private List loadParameterFromMessage(QName msgQName, String namespace) throws WSDLException {
    Element element = (Element) messMappings.get(msgQName);
    if (element == null) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_MESSAGE, new Object[] {msgQName});
    }
    List parts = new ArrayList();    
    NodeList children = element.getChildNodes();
    Node cur;
    Element curElem;
    for (int i = 0; i < children.getLength(); i++) {
      cur = children.item(i);
      if (cur.getNodeType() == Node.ELEMENT_NODE) {
        curElem = (Element) cur;
        int elementCode = getElementCode(curElem.getNamespaceURI(), curElem.getLocalName());
        if (elementCode == PART_STATE) {
          Parameter p = loadPart(curElem, namespace);
          p.setProperty(WSDL11Constants.MESSAGE_QNAME, msgQName.toString()); 
          parts.add(p);
        }
      }
    }

    return parts;
  }

//============================ PortTyte processing ======================================  
//private String getDefinitionsTargetNS(Element element) {
//Element parent = element;
//String value;
//while (parent != null) {
//if (parent.getNamespaceURI().equals(WSDL11Constants.WSDL_NS) && parent.getLocalName().equals(WSDL11Constants.DEFINITIONS_ELEMENT)) {
//return parent.getAttribute(WSDL11Constants.TARGETNAMESPACE_ATTR);
//}
//parent = (Element) parent.getParentNode();
//}

//return "";
//}

  /**
   * Loads portType contents.
   */
  private Interface loadPortType(Definitions parent, Element element, String definitionsTargetNS, String systemId) throws WSDLException {
    String value = element.getAttribute(WSDL11Constants.NAME_ATTR);
    if (value.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{WSDL11Constants.PORTTYPE_ELEMENT, WSDL11Constants.NAME_ATTR});
    }
    checkNCNameAndThrowException(value, "definitions/portType@name");

    QName qname = new QName(definitionsTargetNS, value);     
    Interface result = parent.appendInterface(qname);
    //out.println("loaded port type [" + qname + "]");

    NodeList children = element.getChildNodes();
    Node cur;
    Element curElem;
    for (int i = 0; i < children.getLength(); i++) {
      cur = children.item(i);
      if (cur.getNodeType() == Node.ELEMENT_NODE) {
        curElem = (Element) cur;
        int elementCode = getElementCode(curElem.getNamespaceURI(), curElem.getLocalName());
        if (elementCode == OPERATION_STATE) {
          loadOperation(result, curElem, definitionsTargetNS);
        }
      }
    }
    result.setDomElement(element);
    
    if (systemId != null) {
      result.setProperty(Base.SYSTEM_ID, systemId);
    }
    
    //load extensions
    filter.clear();
    filter.put(new QName("", WSDL11Constants.NAME_ATTR), "");
    loadExtensionAttributes(result, element, filter, -1, null);
    filter.clear();
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT), "");
    loadExtensionElements(result, element, filter, -1, null, definitionsTargetNS);
    return result;
  }

  /**
   * Loads operation contents.
   */
  private void loadOperation(Interface parent, Element element, String definitionsTargetNS) throws WSDLException {
    String value = element.getAttribute(WSDL11Constants.NAME_ATTR);
    if (value.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{WSDL11Constants.OPERATION_ELEMENT, WSDL11Constants.NAME_ATTR});
    }
    /**
    checkNCNameAndThrowException(value, "definitions/portType/operation@name");
     */

    Operation result = parent.appendOperation(value);
    Element currentElement = getFirstChildElement(element);

    String exParam = "portType[" + parent.getName() + "]/operation[" + result.getName() + "]"; 

    boolean foundIn = false, foundOut = false;

    NodeList children = element.getChildNodes(); 
    Node cur;
    Element curElem;
    for (int i = 0; i < children.getLength(); i++) {
      cur = children.item(i);
      if (cur.getNodeType() == Node.ELEMENT_NODE) {
        curElem = (Element) cur;
        int elementCode = getElementCode(curElem.getNamespaceURI(), curElem.getLocalName());

        if (elementCode == INPUT_STATE) {
          if (foundIn) {
            throw new WSDLUnmarshalException(WSDLUnmarshalException.MORE_THAN_ONE_CHILD_ELEMENTS_FOUND, new Object[]{"input", exParam});
          }
          foundIn = true;
          loadChannel(result, curElem, INPUT_STATE, definitionsTargetNS);    
        } else if (elementCode == OUTPUT_STATE) {
          if (foundOut) {
            throw new WSDLUnmarshalException(WSDLUnmarshalException.MORE_THAN_ONE_CHILD_ELEMENTS_FOUND, new Object[]{"output", exParam});
          }
          foundOut = true;
          loadChannel(result, curElem, OUTPUT_STATE, definitionsTargetNS);    
        } else if (elementCode == FAULT_STATE) {
          loadChannel(result, curElem, FAULT_STATE, definitionsTargetNS);

        }
      }
    }
    //set the MEP for this operation
    if (foundIn && foundOut) {
      result.setProperty(Operation.MEP, Operation.INOUT_MEP);
    } else if (foundIn && ! foundOut) {
      result.setProperty(Operation.MEP, Operation.IN_MEP);      
    } else {
      String mep = "input=" + foundIn + "; output=" + foundOut;
      throw new WSDLUnmarshalException(WSDLUnmarshalException.UNKNOW_OPERATION_MEP, new Object[]{result.getName(), parent.getName(), mep});
    }

    //order: first IN params then OUT, at the end FAULT
    ObjectList tmp = result.getParameters(Parameter.IN | Parameter.OUT | Parameter.FAULT);
    for (int i = 0; i < tmp.getLength(); i++) { //remove all params
      result.removeChild(tmp.item(i));
    }
    for (int i = 0; i < tmp.getLength(); i++) { //first add IN params
      if (((Parameter) tmp.item(i)).getParamType() == Parameter.IN) {
        result.appendChild(tmp.item(i));
      }
    }
    for (int i = 0; i < tmp.getLength(); i++) { //second add OUT params
      if (((Parameter) tmp.item(i)).getParamType() == Parameter.OUT) {
        result.appendChild(tmp.item(i));
      }
    }
    for (int i = 0; i < tmp.getLength(); i++) { //last add FAULT params
      if (((Parameter) tmp.item(i)).getParamType() == Parameter.FAULT) {
        result.appendChild(tmp.item(i));
      }
    }

    ObjectList outParams = result.getParameters(Parameter.OUT);
    ObjectList inParams = result.getParameters(Parameter.IN);
    Parameter in, out;
    //check for inout params
    for (int i = 0; i < outParams.getLength(); i++) {
      for (int j = 0; j < inParams.getLength(); j++) {
        in = (Parameter) inParams.item(j);
        out = (Parameter) outParams.item(i);
        //in case inout, mark the in param as INOUT and remove the out param
        if (in.getName().equals(out.getName())) {
          if (in.getXSDTypeRef().getXSDType() == out.getXSDTypeRef().getXSDType() && 
              in.getXSDTypeRef().getQName().equals(out.getXSDTypeRef().getQName())) {
            in.setParamType(Parameter.INOUT);
            result.removeChild(out);
          }
        }
      }
    }
    //process parameterOrder attribute, if available
    String paramOrderAttr =  element.getAttribute(WSDL11Constants.PARAMETER_ORDER_ATTR);
    if (paramOrderAttr.length() > 0) {
      ObjectList inout = result.getParameters(Parameter.IN | Parameter.INOUT);
      StringTokenizer t = new StringTokenizer(paramOrderAttr, " ");
      if (inout.getLength() > t.countTokens()) {
        throw new WSDLUnmarshalException(WSDLUnmarshalException.PARAMETERORDER_INCONSISTENCY, new Object[]{new Integer(inout.getLength()), 
            new Integer(t.countTokens()), exParam});
      }
      ObjectList allParams = result.getParameters(Parameter.IN | Parameter.INOUT | Parameter.OUT);
      String token;
      List newInOutOrder = new ArrayList();
      while (t.hasMoreTokens()) {
        token = t.nextToken();
        boolean found = false;
        for (int i = 0; i < allParams.getLength() && (! found); i++) {
          if (((Parameter) allParams.item(i)).getName().equals(token)) {
            newInOutOrder.add(allParams.item(i));
            result.removeChild(allParams.item(i));
            found = true;
          }
        } 
        if (! found) {
          throw new WSDLUnmarshalException(WSDLUnmarshalException.PARAMETERORDER_MISSING_PART, new Object[]{token, exParam});            
        }
      }
      //remove all remaining children of result and append them in the same order at the end of newInOutOrder
      allParams = result.getParameters(Parameter.IN | Parameter.INOUT | Parameter.OUT);
      for (int i = 0; i < allParams.getLength(); i++) {
        newInOutOrder.add(allParams.item(i));
        result.removeChild(allParams.item(i));
      }
      //attach in correct order
      for (int i = 0; i < newInOutOrder.size(); i++) {
        result.appendChild((Base) newInOutOrder.get(i));
      }
    }
    //if there is only one out parameter, this is the return parameter. JAX-RPC1.1, section 4.3.4.
    ObjectList outPs = result.getParameters(Parameter.OUT);
    /* // edited by chavdar - it is not mentioned in the specification that there should be only one output part
    if (outPs.getLength() == 1) { 
      StringTokenizer t = new StringTokenizer(paramOrderAttr, " ");
      boolean isListed = false;
      while (t.hasMoreTokens()) {
        if (((Parameter) outPs.item(0)).getName().equals(t.nextToken())) {
          isListed = true;
          break;
        }
      }
      if (! isListed) {//if this only part is not listed in param order, it is RETURN
        ((Parameter) outPs.item(0)).setParamType(Parameter.RETURN);        
      }
    }*/

    if (outPs.getLength() >=  1) {
      int unlistedIndex = -1;
      for (int i=0; i<outPs.getLength(); i++) {
        if (!isListed((Parameter) outPs.item(i),paramOrderAttr)) {
          if (unlistedIndex == -1) {
            unlistedIndex = i;
          } else {
            unlistedIndex = -1;
            break;
          }
        }
      }
      if (unlistedIndex > -1) {//if this only part is not listed in param order, it is RETURN
        ((Parameter) outPs.item(unlistedIndex)).setParamType(Parameter.RETURN);                
      }
    }    

    //load extensions
    filter.clear();
    filter.put(new QName("", WSDL11Constants.NAME_ATTR), "");
    loadExtensionAttributes(result, element, filter, -1, null);
    filter.clear();
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.INPUT_ELEMENT), "");
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.OUTPUT_ELEMENT), "");
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.FAULT_ELEMENT), "");
    loadExtensionElements(result, element, filter, -1, null, definitionsTargetNS);
  }

  /**
   * Checks if some parameter is listed.
   */
  private boolean isListed(Parameter param, String paramOrder) {
    StringTokenizer t = new StringTokenizer(paramOrder, " ");
    while (t.hasMoreTokens()) {
      if (param.getName().equals(t.nextToken())) {
        return true;
      }
    }      
    return false;
  }


  /**
   * Loads fault, input, output contents.
   */
  private void loadChannel(Operation parent, Element element, int type, String definitionsTargetNS) throws WSDLException {
    String message = element.getAttribute(WSDL11Constants.MESSAGE_ELEMENT);
    if (message.length() == 0) {
      String elName = element.getLocalName();
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{"portType/operation/" + elName, WSDL11Constants.MESSAGE_ELEMENT});
    }
    QName msgQName = createQNameFromString(message, element);
    List parts = loadParameterFromMessage(msgQName, definitionsTargetNS);
    String faultName = null;
    if (type == INPUT_STATE) {
      initParameters(parent, parts, Parameter.IN); 
      parent.setProperty(WSDL11Constants.OPERATION_IN_MESSAGE_QNAME, msgQName.toString());
      //set the message property also in the input context
      parent.getInputExtensionContext().setProperty(WSDL11Constants.OPERATION_IN_MESSAGE_QNAME, msgQName.toString());
    } else if (type == OUTPUT_STATE) { 
      initParameters(parent, parts, Parameter.OUT); 
      parent.setProperty(WSDL11Constants.OPERATION_OUT_MESSAGE_QNAME, msgQName.toString());
      //set the message property also in the output context
      parent.getOutputExtensionContext().setProperty(WSDL11Constants.OPERATION_OUT_MESSAGE_QNAME, msgQName.toString());
    } else if (type == FAULT_STATE) {
      //the fault message must be with exactly one part
      if (parts.size() != 1) { 
        throw new WSDLUnmarshalException(WSDLUnmarshalException.INCORRECT_WSDL_FAULT_MESSAGE, new Object[]{msgQName});
      }
      String nameAttr = element.getAttribute(WSDL11Constants.NAME_ATTR);
      if (nameAttr.length() == 0) {
        String qname = new QName(element.getNamespaceURI(), element.getLocalName()).toString();
        throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{qname, WSDL11Constants.NAME_ATTR});
      }       
      checkNCNameAndThrowException(nameAttr, "definitions/portType/operation/fault@name");
      faultName = nameAttr;
      ((Parameter) parts.get(0)).setProperty(WSDL11Constants.FAULT_NAME, nameAttr);
      initParameters(parent, parts, Parameter.FAULT); 
      parent.getFaultExtensionContext(faultName).setProperty(WSDL11Constants.FAULT_MESSAGE_QNAME, msgQName.toString());
      parent.getFaultExtensionContext(faultName).setProperty(WSDL11Constants.FAULT_NAME, nameAttr);
    }
    filter.clear();
    filter.put(new QName("", WSDL11Constants.MESSAGE_ELEMENT), "");      
    if (faultName != null) { //this is fault channel - do not load the @name attribute also
      filter.put(new QName("", WSDL11Constants.NAME_ATTR), "");
    }
    loadExtensionAttributes(parent, element, filter, type, faultName);
    filter.clear();
    loadExtensionElements(parent, element, filter, type, faultName, definitionsTargetNS);
  }

  private void initParameters(Base operation, List params, int paramType) throws WSDLException {
    Parameter p;
    for (int i = 0; i < params.size(); i++) {
      p = (Parameter) params.get(i);
      p.setParamType(paramType);
      operation.appendChild(p);
    }
  }  

//========================= Binding processing ===================================
  /**
   * Examines the kind of the binding, and invokes the correct method
   * to load it.
   */
  private Binding loadBinding(Definitions parent, Element element, String definitionsTargetNS) throws WSDLException {
    Element currentElement;
    //check for SOAPBinding, with SOAP1.1
    List list = DOM.getChildElementsByTagNameNS(element, WSDL11Constants.SOAP_NS, WSDL11Constants.BINDING_ELEMENT);
    if (list.size() == 1) {
      SOAPBinding b = loadSOAPBinding(parent, element, WSDL11Constants.SOAP_NS, definitionsTargetNS);
      b.setProperty(SOAPBinding.SOAP_VERSION, SOAPBinding.SOAP_VERSION_11);
      b.setDomElement(element); 
      return b;
    }
    //check for SOAPBinding, with SOAP1.2
    list = DOM.getChildElementsByTagNameNS(element, WSDL11Constants.SOAP12_NS, WSDL11Constants.BINDING_ELEMENT);
    if (list.size() == 1) {
      SOAPBinding b = loadSOAPBinding(parent, element, WSDL11Constants.SOAP12_NS, definitionsTargetNS);
      b.setProperty(SOAPBinding.SOAP_VERSION, SOAPBinding.SOAP_VERSION_12);
      b.setDomElement(element); 
      return b;
    }
    //check for HTTPBinding
    list = DOM.getChildElementsByTagNameNS(element, WSDL11Constants.HTTP_NS, WSDL11Constants.BINDING_ELEMENT);
    if (list.size() == 1) {
      Binding b = loadHTTPBinding(parent, element, definitionsTargetNS);
      b.setDomElement(element); 
      return b;
    }

    //if not recognized the binding type, through exception
    throw new WSDLUnmarshalException(WSDLUnmarshalException.NOTESUPPORTED_BIDING_ELEMENT, new Object[]{transform(element)});
  }

  private HTTPBinding loadHTTPBinding(Definitions defn, Element element, String definitionsTargetNS) throws WSDLException {
    String nameAttr = element.getAttribute(WSDL11Constants.NAME_ATTR);
    if (nameAttr.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{WSDL11Constants.BINDING_ELEMENT, WSDL11Constants.NAME_ATTR});
    }
    checkNCNameAndThrowException(nameAttr, "definitions/binding@name");

    HTTPBinding httpBinding = new HTTPBinding(new QName(definitionsTargetNS, nameAttr));
    defn.appendChild(httpBinding);
    //portType resolution
    String typeValue = element.getAttribute(WSDL11Constants.TYPE_ATTR);
    if (typeValue.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{WSDL11Constants.BINDING_ELEMENT, WSDL11Constants.TYPE_ATTR});
    }
    QName qname = createQNameFromString(typeValue, element);
    //check for available Inteface
    if (defn.getInterface(qname) == null) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_WSDL_ENTITY, new Object[]{WSDL11Constants.PORTTYPE_ELEMENT, qname});
      //addForDelayedCheck(WSDL11Constants.PORTTYPE_ELEMENT, qname);
    }
    httpBinding.setInterface(qname);
    //process 'verb' attribute
    List list = DOM.getChildElementsByTagNameNS(element, WSDL11Constants.HTTP_NS, WSDL11Constants.BINDING_ELEMENT);                           
    Element currentElement = (Element) list.get(0);
    String httpMethod = currentElement.getAttribute(WSDL11Constants.VERB_ATTR);
    if (httpMethod.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{WSDL11Constants.BINDING_ELEMENT, WSDL11Constants.VERB_ATTR});
    }
    if (! HTTPBinding.HTTP_GET_METHOD.equals(httpMethod) && ! HTTPBinding.HTTP_POST_METHOD.equals(httpMethod)) {
      String validValues = HTTPBinding.HTTP_GET_METHOD + ", " + HTTPBinding.HTTP_POST_METHOD;
      throw new WSDLUnmarshalException(WSDLUnmarshalException.INVALID_ATTRIB_VALUE, new Object[]{WSDL11Constants.VERB_ATTR, httpMethod, validValues});      
    }
    httpBinding.setProperty(HTTPBinding.HTTP_METHOD, httpMethod);

    //process operations
    NodeList children = element.getChildNodes();
    Node cur;
    Element curElem;
    for (int i = 0; i < children.getLength(); i++) {
      cur = children.item(i);
      if (cur.getNodeType() == Node.ELEMENT_NODE) {
        curElem = (Element) cur;
        int elementCode = getElementCode(curElem.getNamespaceURI(), curElem.getLocalName());

        if (elementCode == OPERATION_STATE) {
          loadHTTPBindingOperation(httpBinding, curElem, definitionsTargetNS);
        }
      }    
    }
    //load extension attribs
    filter.clear();
    filter.put(new QName("", WSDL11Constants.NAME_ATTR), "");
    filter.put(new QName("", WSDL11Constants.TYPE_ATTR), "");
    loadExtensionAttributes(httpBinding, element, filter, -1, null);
    //load extended elements
    filter.clear();
    filter.put(new QName(WSDL11Constants.HTTP_NS, WSDL11Constants.BINDING_ELEMENT), "");
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT), "");
    loadExtensionElements(httpBinding, element, filter, -1, null, definitionsTargetNS);

    return httpBinding;
  }

  private HTTPBindingOperation loadHTTPBindingOperation(HTTPBinding binding, Element element, String definitionsTargetNS) throws WSDLException {
    String nameAttr = element.getAttribute(WSDL11Constants.NAME_ATTR);
    if (nameAttr.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{"/binding/operation", WSDL11Constants.NAME_ATTR});
    }
    checkNCNameAndThrowException(nameAttr, "definitions/binding/operation@name");

    HTTPBindingOperation operation = new HTTPBindingOperation(nameAttr);
    binding.appendChild(operation);

    String locationAttr = "";
    List list = DOM.getChildElementsByTagNameNS(element, WSDL11Constants.HTTP_NS, WSDL11Constants.OPERATION_ELEMENT);
    if (list.size() == 1) {
      Element currentElement = (Element) list.get(0);
      locationAttr = currentElement.getAttribute(WSDL11Constants.LOCATION_ATTR);
    }
    if (locationAttr.length() == 0) {
//    throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{"/binding/operation/http:operation", WSDL11Constants.LOCATION_ATTR});
    }    
    operation.setProperty(HTTPBindingOperation.LOCATION, locationAttr);
    //set operaiton method
    String httpMethod = binding.getProperty(HTTPBinding.HTTP_METHOD);
    operation.setProperty(HTTPBindingOperation.HTTP_METHOD, httpMethod);

    boolean foundIn = false, foundOut = false; 
    NodeList children = element.getChildNodes();
    Node cur;
    Element curElem;
    for (int i = 0; i < children.getLength(); i++) {
      cur = children.item(i);
      if (cur.getNodeType() == Node.ELEMENT_NODE) {
        curElem = (Element) cur;
        int elementCode = getElementCode(curElem.getNamespaceURI(), curElem.getLocalName());

        if (elementCode == INPUT_STATE || elementCode == OUTPUT_STATE) {
          //load http channel content
          String serializationType = null;
          //check for http:urlEncoded element.
          List crn = DOM.getChildElementsByTagNameNS(curElem, WSDL11Constants.HTTP_NS, WSDL11Constants.URLENCODED_ELEMENT);
          if (crn.size() > 0) {
            serializationType = HTTPBindingOperation.URLENCODED_SERIALIZATION;
          }
          //check for <mime:content type='application/x-www-form-urlencoded'/>
          crn = DOM.getChildElementsByTagNameNS(curElem, WSDL11Constants.MIME_NS, WSDL11Constants.CONTENT_ELEMENT);
          if (crn.size() == 1) {
            Element cntEl = (Element) crn.get(0);
            String typeAttr = cntEl.getAttribute(WSDL11Constants.TYPE_ATTR);
            if (typeAttr.equals(HTTPBindingOperation.URLENCODED_SERIALIZATION)) {
              serializationType = HTTPBindingOperation.URLENCODED_SERIALIZATION; 
            }
          }
          //check for <mime:mimeXml/>
          crn = DOM.getChildElementsByTagNameNS(curElem, WSDL11Constants.MIME_NS, WSDL11Constants.MIMEXML_ELEMENT);
          if (crn.size() == 1) {
            serializationType = HTTPBindingOperation.MIMEXML_SERIALIZATION;
          }
          if (serializationType == null) {
            serializationType = ""; //do not throw exception in case unrecorgnized type is encountered.
//          throw new WSDLUnmarshalException(WSDLUnmarshalException.UNRECORGNIZED_ELEMENT_CONTENT, new Object[]{curElem});
          }
          //setup extension element filter
          filter.clear();
          filter.put(new QName(WSDL11Constants.HTTP_NS, WSDL11Constants.URLENCODED_ELEMENT), "");
          filter.put(new QName(WSDL11Constants.MIME_NS, WSDL11Constants.CONTENT_ELEMENT), "");
          filter.put(new QName(WSDL11Constants.MIME_NS, WSDL11Constants.MIMEXML_ELEMENT), "");

          if (elementCode == INPUT_STATE) {
            if (foundIn) {
              throw new WSDLUnmarshalException(WSDLUnmarshalException.MORE_THAN_ONE_CHILD_ELEMENTS_FOUND, new Object[]{"input", "binding/operation"});
            }
            foundIn = true;
            operation.setProperty(HTTPBindingOperation.INPUT_SERIALIZATION, serializationType);
            //load extension element
            loadExtensionElements(operation, curElem, filter, elementCode, null, definitionsTargetNS);
            //load extension attributes
            loadExtensionAttributes(operation, curElem, null, elementCode, null);
          } else {
            if (foundOut) {
              throw new WSDLUnmarshalException(WSDLUnmarshalException.MORE_THAN_ONE_CHILD_ELEMENTS_FOUND, new Object[]{"output", "binding/operation"});
            }
            foundOut = true;
            operation.setProperty(HTTPBindingOperation.OUTPUT_SERIALIZATION, serializationType);
          }
        }
//      case FAULT_STATE: {
////    loadChannel(result, curElem, FAULT_STATE);    
////    break;
//      }
//      }
      }
    }
    //load extension    
    filter.clear();
    filter.put(new QName("", WSDL11Constants.NAME_ATTR), "");
    loadExtensionAttributes(operation, element, filter, -1, null);
    filter.clear();
    filter.put(new QName(WSDL11Constants.HTTP_NS, WSDL11Constants.OPERATION_ELEMENT), "");
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.INPUT_ELEMENT), "");
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.OUTPUT_ELEMENT), "");
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.FAULT_ELEMENT), "");
    loadExtensionElements(operation, element, filter, -1, null, definitionsTargetNS);   

    return operation;      
  }

  private SOAPBinding loadSOAPBinding(Definitions defn, Element element, String soapNS, String definitionsTargetNS) throws WSDLException {
    String nameAttr = element.getAttribute(WSDL11Constants.NAME_ATTR);
    if (nameAttr.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{WSDL11Constants.BINDING_ELEMENT, WSDL11Constants.NAME_ATTR});
    }
    checkNCNameAndThrowException(nameAttr, "definitions/binding@name");
    SOAPBinding soapBinding = new SOAPBinding(new QName(definitionsTargetNS, nameAttr));
    //out.println("added binding [" + soapBinding.getName() + "]");
    defn.appendChild(soapBinding);
    //portType resolution
    String typeValue = element.getAttribute(WSDL11Constants.TYPE_ATTR);
    if (typeValue.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{WSDL11Constants.BINDING_ELEMENT, WSDL11Constants.TYPE_ATTR});
    }
    QName qname = createQNameFromString(typeValue, element);
    //check for available Inteface
    if (defn.getInterface(qname) == null) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_WSDL_ENTITY, new Object[]{WSDL11Constants.PORTTYPE_ELEMENT, qname});
      //addForDelayedCheck(WSDL11Constants.PORTTYPE_ELEMENT, qname);
    }
    soapBinding.setInterface(qname);

    List list = DOM.getChildElementsByTagNameNS(element, soapNS, WSDL11Constants.BINDING_ELEMENT);                           
    Element currentElement = (Element) list.get(0);

    String transport = currentElement.getAttribute(WSDL11Constants.TRANSPORT_ATTR);
    String style = currentElement.getAttribute(WSDL11Constants.STYLE_ATTR);
    //if no style attib is available, place the default value;
    if (style.length() == 0) {
      style = SOAPBinding.DOC_STYLE_VALUE;
    }
    if (! SOAPBinding.DOC_STYLE_VALUE.equals(style) && ! SOAPBinding.RPC_STYLE_VALUE.equals(style)) {
      String validValues = SOAPBinding.DOC_STYLE_VALUE + " " + SOAPBinding.RPC_STYLE_VALUE;
      throw new WSDLUnmarshalException(WSDLUnmarshalException.INVALID_ATTRIB_VALUE, new Object[]{WSDL11Constants.STYLE_ATTR, style, validValues});
    }
    //the transport must not be missing
    if (transport.length() == 0) {
      QName bExtQName = new QName(currentElement.getNamespaceURI(), currentElement.getLocalName());
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{bExtQName, WSDL11Constants.TRANSPORT_ATTR});
    }
    soapBinding.setProperty(SOAPBinding.TRANSPORT, transport);
    soapBinding.setProperty(SOAPBinding.STYLE, style);

    NodeList children = element.getChildNodes();
    Node cur;
    Element curElem;
    for (int i = 0; i < children.getLength(); i++) {
      cur = children.item(i);
      if (cur.getNodeType() == Node.ELEMENT_NODE) {
        curElem = (Element) cur;
        int elementCode = getElementCode(curElem.getNamespaceURI(), curElem.getLocalName());

        if (elementCode == OPERATION_STATE) {
          loadSOAPBindingOperation(soapBinding, curElem, soapNS, definitionsTargetNS);
        }
      }    
    }
    //load extension attribs
    filter.clear();
    filter.put(new QName("", WSDL11Constants.NAME_ATTR), "");
    filter.put(new QName("", WSDL11Constants.TYPE_ATTR), "");
    loadExtensionAttributes(soapBinding, element, filter, -1, null);
    //load extended elements
    filter.clear();
    filter.put(new QName(soapNS, WSDL11Constants.BINDING_ELEMENT), "");
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.OPERATION_ELEMENT), "");
    loadExtensionElements(soapBinding, element, filter, -1, null, definitionsTargetNS);

    return soapBinding;
  }

  private void loadSOAPBindingOperation(SOAPBinding binding, Element element, String soapNS, String definitionsTargetNS) throws WSDLException {    
    String nameAttr = element.getAttribute(WSDL11Constants.NAME_ATTR);
    if (nameAttr.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{"/binding/operation", WSDL11Constants.NAME_ATTR});
    }
    /*
    checkNCNameAndThrowException(nameAttr, "definitions/binding/operation@name");
     */
    SOAPBindingOperation operation = new SOAPBindingOperation(nameAttr);
    binding.appendChild(operation);

    String styleAttr = "";
    String soapActionAttr = "";
    List list = DOM.getChildElementsByTagNameNS(element, soapNS, WSDL11Constants.OPERATION_ELEMENT);
    if (list.size() == 1) {
      Element currentElement = (Element) list.get(0);
      styleAttr = currentElement.getAttribute(WSDL11Constants.STYLE_ATTR);
      soapActionAttr = currentElement.getAttribute(WSDL11Constants.SOAPACTION_ATTR);
    }

    //in case no 'style' attr is available, use the style from the binding.
    if (styleAttr.length() == 0) {
      styleAttr = binding.getProperty(SOAPBinding.STYLE);
    }
    operation.setProperty(SOAPBindingOperation.STYLE, styleAttr);
    //setting soapAction attr
    operation.setProperty(SOAPBindingOperation.SOAPACTION, soapActionAttr);

    boolean foundIn = false, foundOut = false; 
    NodeList children = element.getChildNodes();
    Node cur;
    Element curElem;
    for (int i = 0; i < children.getLength(); i++) {
      cur = children.item(i);
      if (cur.getNodeType() == Node.ELEMENT_NODE) {
        curElem = (Element) cur;
        int elementCode = getElementCode(curElem.getNamespaceURI(), curElem.getLocalName());

        if (elementCode == INPUT_STATE) {
          if (foundIn) {
            throw new WSDLUnmarshalException(WSDLUnmarshalException.MORE_THAN_ONE_CHILD_ELEMENTS_FOUND, new Object[]{"input", "binding/operation"});
          }
          foundIn = true;
          loadSOAPBindingChannel(operation, curElem, INPUT_STATE, soapNS, definitionsTargetNS);    
        } else if (elementCode == OUTPUT_STATE) {
          if (foundOut) {
            throw new WSDLUnmarshalException(WSDLUnmarshalException.MORE_THAN_ONE_CHILD_ELEMENTS_FOUND, new Object[]{"output", "binding/operation"});
          }
          foundOut = true;
          loadSOAPBindingChannel(operation, curElem, OUTPUT_STATE, soapNS, definitionsTargetNS);    
        } else if (elementCode == FAULT_STATE) {
          loadSOAPBindingChannel(operation, curElem, FAULT_STATE, soapNS, definitionsTargetNS);    
        }
      }
    }
    //load extension    
    filter.clear();
    filter.put(new QName("", WSDL11Constants.NAME_ATTR), "");
    loadExtensionAttributes(operation, element, filter, -1, null);
    filter.clear();
    filter.put(new QName(soapNS, WSDL11Constants.OPERATION_ELEMENT), "");
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.INPUT_ELEMENT), "");
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.OUTPUT_ELEMENT), "");
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.FAULT_ELEMENT), "");
    loadExtensionElements(operation, element, filter, -1, null, definitionsTargetNS);     
  }

  private void loadSOAPBindingChannel(SOAPBindingOperation operation, Element element, int type, String soapNS, String definitionsTargetNS) throws WSDLException {
    //check whether this is not multipartRelated construct.
    List mPR = DOM.getChildElementsByTagNameNS(element, WSDL11Constants.MIME_NS, WSDL11Constants.MULTIPART_RELATED_ELEMENT);
    boolean isMMR = false;
    if (mPR.size() == 1) { //this is multipartRelated element
      isMMR = true;
      Element mprEl = (Element) mPR.get(0);
      //check whether this is <fault> channel. 
      if (type == FAULT_STATE) {
        throw new WSDLUnmarshalException(WSDLUnmarshalException.ELEMENT_CANNOT_BE_ATTACHED, new Object[]{mprEl, "binding/operation/fault", "AttachmentsProfile1.0(R2930)"});
      }
      Element soapBodyMimePart = null;
      List mimeParts = DOM.getChildElementsByTagNameNS(mprEl, WSDL11Constants.MIME_NS, WSDL11Constants.PART_ELEMENT);
      List tmpList;
      Element mimePart;
      //append AttachmentsContainer
      AttachmentsContainer attContainer;
      if (type == INPUT_STATE) { //this is input channel
        attContainer = operation.appendInputAttachmentsContainer();
      } else { //this is output channel
        attContainer = operation.appendOutputAttachmentsContainer();
      }
      //process the parts
      for (int i = 0; i < mimeParts.size(); i++) {
        mimePart = (Element) mimeParts.get(i);
        //check for <soap:body> element
        tmpList = DOM.getChildElementsByTagNameNS(mimePart, soapNS, WSDL11Constants.SOAPBODY_ELEMENT);
        if (tmpList.size() == 1) {
          //check whether more then once <soap:body> appears. This is restructed by AP1.0.
          if (soapBodyMimePart != null) {
            String qname = new QName(soapNS, WSDL11Constants.SOAPBODY_ELEMENT).toString();
            throw new WSDLUnmarshalException(WSDLUnmarshalException.ELEMENT_MUST_APPEAR_EXACTLY_ONCE, new Object[]{qname, mprEl, "AttachmentsProfile1.0(R2911)"});
          }
          soapBodyMimePart = mimePart;
        } else if (tmpList.size() > 1) {
          String qname = new QName(soapNS, WSDL11Constants.SOAPBODY_ELEMENT).toString();
          throw new WSDLUnmarshalException(WSDLUnmarshalException.ELEMENT_MUST_APPEAR_EXACTLY_ONCE, new Object[]{qname, mprEl, "AttachmentsProfile1.0(R2911)"});
        } else { //check for mime:content elements
          tmpList = DOM.getChildElementsByTagNameNS(mimePart, WSDL11Constants.MIME_NS, WSDL11Constants.CONTENT_ELEMENT);
          if (tmpList.size() > 0) {
            String partName = null;
            String tmpPName, tmpType;
            Element content;
            ArrayList types = new ArrayList();
            for (int p = 0; p < tmpList.size(); p++) {
              content = (Element) tmpList.get(p);
              tmpPName = content.getAttribute(WSDL11Constants.PART_ELEMENT);
              if (tmpPName.length() == 0) {
                String qname = new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.CONTENT_ELEMENT).toString();
                throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{qname, WSDL11Constants.PART_ELEMENT});
              }
              tmpType = content.getAttribute(WSDL11Constants.TYPE_ATTR);
              if (tmpType.length() == 0) {
                String qname = new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.CONTENT_ELEMENT).toString();
                throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{qname, WSDL11Constants.TYPE_ATTR});
              }
              types.add(tmpType);

              if (partName == null) {
                partName = tmpPName;
              }
              if (! partName.equals(tmpPName)) {
                throw new WSDLUnmarshalException(WSDLUnmarshalException.ALL_CONTENT_ELEMENTS_MUST_HAVE_EQUAL_PART_VALUES, new Object[]{mimePart, "AttachmentsProfile1.0(R2909)"});
              }   
            }
            //create MIMEPart
            attContainer.appendMIMEPart(partName, types);
          }
        }
      }
      //load extension attributes and elements
      filter.clear();
      loadExtensionAttributes(operation, element, filter, type, null); //it is not valid 'fault' channel with 'multiPartRelated' construct. 
      filter.put(new QName(WSDL11Constants.MIME_NS, WSDL11Constants.MULTIPART_RELATED_ELEMENT), "");
      loadExtensionElements(operation, element, filter, type, null, definitionsTargetNS);
      //preset the 'element' parameter to reference to the mime:part which contains the <soap:body>
      if (soapBodyMimePart == null) {
        String qname = new QName(soapNS, WSDL11Constants.SOAPBODY_ELEMENT).toString();
        throw new WSDLUnmarshalException(WSDLUnmarshalException.ELEMENT_MUST_APPEAR_EXACTLY_ONCE, new Object[]{qname, mprEl, "AttachmentsProfile1.0(R2911)"});
      }
      element = soapBodyMimePart;
    }
    //process the soapbody content
    if (type == INPUT_STATE || type == OUTPUT_STATE) {
      //soap:body element processing
      //find soap:body element
      List bodies = DOM.getChildElementsByTagNameNS(element, soapNS, WSDL11Constants.SOAPBODY_ELEMENT);
      if (bodies.size() != 1) {
        throw new WSDLUnmarshalException(WSDLUnmarshalException.SINGLE_WSDL_ELEMENT, new Object[]{new QName(soapNS, WSDL11Constants.SOAPBODY_ELEMENT), element});
      }
      Element soapBody = (Element) bodies.get(0);
      QName soapBodyQN = new QName(soapNS, WSDL11Constants.SOAPBODY_ELEMENT);
      String useAttr = soapBody.getAttribute(WSDL11Constants.USE_ATTR);
      //use is required 
      if (useAttr.length() == 0) {
        useAttr = SOAPBindingOperation.USE_LITERAL; //set default value according to BP1.1
      }

      if (! SOAPBindingOperation.USE_ENCODED.equals(useAttr) && ! SOAPBindingOperation.USE_LITERAL.equals(useAttr)) {
        String validValues = SOAPBindingOperation.USE_ENCODED + " " + SOAPBindingOperation.USE_LITERAL;
        throw new WSDLUnmarshalException(WSDLUnmarshalException.INVALID_ATTRIB_VALUE, new Object[]{WSDL11Constants.USE_ATTR, useAttr, validValues}); 
      }
      //invalid operation - document/encoded
      if (SOAPBindingOperation.USE_ENCODED.equals(useAttr) && SOAPBinding.DOC_STYLE_VALUE.equals(operation.getProperty(SOAPBindingOperation.STYLE))) {
        throw new WSDLUnmarshalException(WSDLUnmarshalException.INVALID_DOCUMENT_ENCODED_OPERATION, new Object[]{element});
      }

      String encStyle = soapBody.getAttribute(WSDL11Constants.ENCODEDSTYLE_ATTR);
      if (encStyle.length() == 0 && SOAPBindingOperation.USE_ENCODED.equals(useAttr)) {
        throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{soapBodyQN, WSDL11Constants.ENCODEDSTYLE_ATTR});
      }

      String namespaceAttr = soapBody.getAttribute(WSDL11Constants.NAMESPACE_ATTR);
      String partsAttr = soapBody.getAttribute(WSDL11Constants.PARTS_ATTR);
      //setting the properties
      operation.setProperty(SOAPBindingOperation.USE, useAttr);
      if (type == INPUT_STATE) {
        if (encStyle.length() > 0) {
          operation.setProperty(SOAPBindingOperation.INPUT_ENCODINGSTYLE, encStyle);
        }
        if (namespaceAttr.length() > 0) {
          operation.setProperty(SOAPBindingOperation.INPUT_NAMESPACE, namespaceAttr);
        }
        if (partsAttr.length() > 0) {
          operation.setProperty(SOAPBindingOperation.INPUT_PARTS, partsAttr);
        }
      } else {
        if (encStyle.length() > 0) {
          operation.setProperty(SOAPBindingOperation.OUTPUT_ENCODINGSTYLE, encStyle);
        }
        if (namespaceAttr.length() > 0) {
          operation.setProperty(SOAPBindingOperation.OUTPUT_NAMESPACE, namespaceAttr);
        }
        if (partsAttr.length() > 0) {
          operation.setProperty(SOAPBindingOperation.OUTPUT_PARTS, partsAttr);
        }
      } 

      QName msgQName = null;
      String msgQNameStr = null;
      Operation op = operation.getReferencedOperation();
      if (type == INPUT_STATE) {
        msgQNameStr = op.getProperty(WSDL11Constants.OPERATION_IN_MESSAGE_QNAME);
      } else {
        msgQNameStr = op.getProperty(WSDL11Constants.OPERATION_OUT_MESSAGE_QNAME);
      }     
      if (msgQNameStr != null) {
        msgQName = QName.valueOf(msgQNameStr);
      }
      //header elements processing
      //find soap:header elements
      List headers = DOM.getChildElementsByTagNameNS(element, soapNS, WSDL11Constants.SOAPHEADER_ELEMENT);
      String messageStr, partStr;
      StringBuffer headerParts = new StringBuffer();
      Element curHeader;
      for (int i = 0; i < headers.size(); i++) {
        curHeader = (Element) headers.get(i);
        messageStr = curHeader.getAttribute(WSDL11Constants.MESSAGE_ELEMENT);
        if (messageStr.length() == 0) {
          QName soapHeader = new QName(soapNS, WSDL11Constants.SOAPHEADER_ELEMENT);
          throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{soapHeader, WSDL11Constants.MESSAGE_ELEMENT});
        }
        partStr = curHeader.getAttribute(WSDL11Constants.PART_ELEMENT);
        if (partStr.length() == 0) {
          QName soapHeader = new QName(soapNS, WSDL11Constants.SOAPHEADER_ELEMENT);
          throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{soapHeader, WSDL11Constants.PART_ELEMENT});
        }
        QName curMQName = createQNameFromString(messageStr, curHeader);
        Parameter p = getParameterFromMessage(curMQName, partStr, definitionsTargetNS);
        //only references to xsd element are supported as headers
        if (p.getXSDTypeRef().getXSDType() != XSDRef.ELEMENT) {
          throw new WSDLUnmarshalException(WSDLUnmarshalException.INVALID_SOAP_HEADER_REFERENCE, new Object[]{curMQName, partStr});
        }
        //check whether the header is from the portType in/out message
        if (curMQName.equals(msgQName)) {
          //append to the header part list.
          headerParts.append(partStr + " ");
        } else { //create Parameter object for that header
          if (type == INPUT_STATE) {
            operation.appendInAdditionalHeader(p);
          } else {
            operation.appendOutAdditionalHeader(p);
          }
        }        
      }
      if (type == INPUT_STATE) {
        operation.setProperty(SOAPBindingOperation.IN_HEADERS, headerParts.toString());
      } else {
        operation.setProperty(SOAPBindingOperation.OUT_HEADERS, headerParts.toString());
      }
      //load extensions
      if (! isMMR) { //this is 'pure' soap
        filter.clear();
        loadExtensionAttributes(operation, element, filter, type, null);
        filter.clear();
        filter.put(new QName(soapNS, WSDL11Constants.SOAPBODY_ELEMENT), "");
        filter.put(new QName(soapNS, WSDL11Constants.SOAPHEADER_ELEMENT), "");
        loadExtensionElements(operation, element, filter, type, null, definitionsTargetNS);
      }
    } else { //fault processing
      //find soap:fault element
      List faults = DOM.getChildElementsByTagNameNS(element, soapNS, WSDL11Constants.FAULT_ELEMENT);
      if (faults.size() != 1) {
        throw new WSDLUnmarshalException(WSDLUnmarshalException.SINGLE_WSDL_ELEMENT, new Object[]{new QName(soapNS, WSDL11Constants.FAULT_ELEMENT), element});
      }
      //take 'name' attrib
      String nameAttr = element.getAttribute(WSDL11Constants.NAME_ATTR);
      if (nameAttr.length() == 0) {
        String soapFaultQN = new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.FAULT_ELEMENT).toString();
        throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{soapFaultQN, WSDL11Constants.NAME_ATTR});
      }
      checkNCNameAndThrowException(nameAttr, "definitions/binding/operation/fault@name");

      if (SOAPBindingOperation.USE_ENCODED.equals(operation.getProperty(SOAPBindingOperation.USE))) { //only for rpc/encoded operations fault data should be processed
        //take 'namespace' attrib
        Element soapFault = (Element) faults.get(0);
        String nsAttr = soapFault.getAttribute(WSDL11Constants.NAMESPACE_ATTR);
        operation.setEncodedFaultNS(nameAttr, nsAttr);
      }
      filter.clear();
      filter.put(new QName("", WSDL11Constants.NAME_ATTR), "");
      loadExtensionAttributes(operation, element, filter, type, nameAttr);
      filter.clear();
      filter.put(new QName(soapNS, WSDL11Constants.FAULT_ELEMENT), "");
      loadExtensionElements(operation, element, filter, type, nameAttr, definitionsTargetNS);      
    }
  }

  private Parameter getParameterFromMessage(QName qname, String partName, String definitionsTargetNS) throws WSDLException {
    List parts = loadParameterFromMessage(qname, definitionsTargetNS); 
    for (int i = 0; i < parts.size(); i++) {
      if (((Parameter) parts.get(i)).getName().equals(partName)) {
        return (Parameter) parts.get(i);
      }
    }
    throw new WSDLUnmarshalException(WSDLUnmarshalException.PART_NOT_FOUND, new Object[] {partName, qname});
  }

  private QName createQNameFromString(String qName, Element element) {
    String uri = DOM.qnameToURI(qName, element);
    String localName = DOM.qnameToLocalName(qName);
    return new QName(uri, localName);
  }

//================================== Service and Endpoint loading ==========================================

  /**
   * Loads service contents.
   */
  private Service loadService(Definitions defn, Element element, String definitionsTargetNS) throws WSDLException {
    String nameAttr = element.getAttribute(WSDL11Constants.NAME_ATTR);
    if (nameAttr.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{WSDL11Constants.SERVICE_ELEMENT, WSDL11Constants.NAME_ATTR});
    }
    checkNCNameAndThrowException(nameAttr, "definitions/service@name");

    QName qname = new QName(definitionsTargetNS, nameAttr);
    Service service = defn.appendService(qname);     
    //Service service = defn.appendService();
    Element currentElement = getFirstChildElement(element);

    NodeList children = element.getChildNodes();
    Node cur;
    Element curElem;
    for (int i = 0; i < children.getLength(); i++) {
      cur = children.item(i);
      if (cur.getNodeType() == Node.ELEMENT_NODE) {
        curElem = (Element) cur;
        int elementCode = getElementCode(curElem.getNamespaceURI(), curElem.getLocalName());

        if (elementCode == PORT_STATE) {
          loadPort(service, curElem, definitionsTargetNS);
        }
      }
    }

    service.setDomElement(element);

    //loadExtensions
    filter.clear();
    filter.put(new QName("", WSDL11Constants.NAME_ATTR), "");
    loadExtensionAttributes(service, element, filter, -1, null);
    filter.clear();
    filter.put(new QName(WSDL11Constants.WSDL_NS, WSDL11Constants.PORT_ELEMENT), "");
    loadExtensionElements(service, element, filter, -1, null, definitionsTargetNS);
    return service;
  }

  /**
   * Loads WSDL port contents.
   */
  private Endpoint loadPort(Service service, Element element, String definitionsTargetNS) throws WSDLException {
    String nameAttr = element.getAttribute(WSDL11Constants.NAME_ATTR);
    if (nameAttr.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{"service/port", WSDL11Constants.NAME_ATTR});
    }
    checkNCNameAndThrowException(nameAttr, "definitions/service/port@name");

    String bindingRef = element.getAttribute(WSDL11Constants.BINDING_ELEMENT);
    if (bindingRef.length() == 0) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{"service/port", WSDL11Constants.BINDING_ELEMENT});
    }
    QName qname = createQNameFromString(bindingRef, element);
    Definitions def = (Definitions) service.getRoot();
    //no binding for that port
    if (def.getBinding(qname) == null) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_ENTITY, new Object[]{WSDL11Constants.BINDING_ELEMENT, qname});
      //addForDelayedCheck(WSDL11Constants.BINDING_ELEMENT, qname);
    }

    Endpoint endpoint = service.appendEndpoint(nameAttr);
    endpoint.setBinding(qname);

    //base on binding type, determine what should be the 'address' namespace (SOAP1.1, SOAP1.2, HTTP)
    String extNS;
    Binding b = definitions.getBinding(qname);
    if (b instanceof SOAPBinding) {
      String bSoapVer = ((SOAPBinding) b).getSOAPVersion();
      if (SOAPBinding.SOAP_VERSION_11.equals(bSoapVer)) {
        extNS = WSDL11Constants.SOAP_NS;
      } else {
        extNS = WSDL11Constants.SOAP12_NS;
      }
    } else {
      extNS = WSDL11Constants.HTTP_NS;
    }

    //check port for extension element     
    List list = DOM.getChildElementsByTagNameNS(element, extNS, WSDL11Constants.ADDRESS_ELEMENT);
    if (list.size() == 1) {
      Element currentElement = (Element) list.get(0);    
      String locationAttr = currentElement.getAttribute(WSDL11Constants.LOCATION_ATTR);
//    if (locationAttr.length() == 0) {
//    QName addQName = new QName(currentElement.getNamespaceURI(), currentElement.getLocalName());
//    throw new WSDLUnmarshalException(WSDLUnmarshalException.MISSING_RQUIRED_ATTRIBUTE, new Object[]{addQName, WSDL11Constants.LOCATION_ATTR});             
//    }
      endpoint.setProperty(Endpoint.URL, locationAttr);      
    }    

    //loadExtensions
    filter.clear();
    filter.put(new QName("", WSDL11Constants.NAME_ATTR), "");
    filter.put(new QName("", WSDL11Constants.TYPE_ATTR), "");
    loadExtensionAttributes(endpoint, element, filter, -1, null);
    filter.clear();
    filter.put(new QName(extNS, WSDL11Constants.ADDRESS_ELEMENT), "");
    loadExtensionElements(endpoint, element, filter, -1, null, definitionsTargetNS);

    return endpoint;
  }
  /**
   * 
   * Resolves and loads 'import' and 'include' statements if any inside, <code>schemaDomSource</code>.
   * Adds <code>schemaDoSource</code> to <code>xsdContainer</code>. 
   */
  private void resolveAndLoadSchema(XSDTypeContainer xsdContainer, DOMSource schemaDomSource, String systemId) throws WSDLException {
    //add schema to xsdContainer
    //out.println("adding schema [" + schemaDomSource.getSystemId() + "]");
    xsdContainer.addSchema(schemaDomSource);
    SchemaURIResolver schemaURIResolver = (SchemaURIResolver) xsdContainer.getURIResolver();
    //resolve imports
    Element tmpImport;
    Element schema = (Element) schemaDomSource.getNode();
    List imports = DOM.getChildElementsByTagNameNS(schema, WSDL11Constants.SCHEMA_NS, WSDL11Constants.IMPORT_ELEMENT);
    List includes = DOM.getChildElementsByTagNameNS(schema, WSDL11Constants.SCHEMA_NS, WSDL11Constants.SCHEMA_INCLUDE_ELEM);
    List redefine = DOM.getChildElementsByTagNameNS(schema, WSDL11Constants.SCHEMA_NS, WSDL11Constants.REDEFINE_ELEMENT);    
    //join includes to imports.
    imports.addAll(includes);
    imports.addAll(redefine);
    String xsdLoc;

    try {
      for (int i = 0; i < imports.size(); i++) {
        tmpImport = (Element) imports.get(i);
        xsdLoc = tmpImport.getAttribute(WSDL11Constants.SCHEMA_LOCATION_ATTR);   
        if (xsdLoc != null && xsdLoc.length() > 0) { //there is @schemaLocation with specific value, load it
          String newSchemaNS = tmpImport.getAttribute(WSDL11Constants.NAMESPACE_ATTR);
          if (newSchemaNS != null && xsdContainer.containsSchema(newSchemaNS)) {
            continue; //do not load the schema since it is already loaded.
          }
          if (schemaURIResolver.resolve(xsdLoc, schemaDomSource.getSystemId()) != null) {
            continue;//do not load the schema since it is already loaded.
          }

          Object newSchemaSource = resolveImport(schemaDomSource.getSystemId(), xsdLoc, newSchemaNS);
          DOMSource newSchema;
          if (newSchemaSource instanceof DOMSource) {
            newSchema = (DOMSource) newSchemaSource;
          } else if (newSchemaSource instanceof InputSource) {
            InputSource inpSource = (InputSource) newSchemaSource;
            newSchema = SharedDocumentBuilders.loadDOMDocument(inpSource.getByteStream(), inpSource.getSystemId()); 
          } else {
            newSchema = SharedDocumentBuilders.loadDOMDocument((String) newSchemaSource,this.proxyHost,this.proxyPort,this.wsdlResolver);
          }
          schemaURIResolver.registerSchema(xsdLoc, schemaDomSource.getSystemId(), newSchema);
          //        Element newSchemaElem = (Element) newSchema.getNode();
          //        newSchemaNS = newSchemaElem.getAttributeNode(WSDL11Constants.TARGETNAMESPACE_ATTR);
          //        if (tmpImport.getLocalName().equals(WSDL11Constants.SCHEMA_INCLUDE_ELEM)) { //always load the include schemas
          resolveAndLoadSchema(xsdContainer, newSchema, systemId); //try to resolve the newly imported schema.
          //        } else if (newSchemaNS != null && (! xsdContainer.containsSchema(newSchemaNS.getValue()))) {
          //          resolveAndLoadSchema(xsdContainer, newSchema); //try to resolve the newly imported schema.
          //        }
        }
      }
    } catch (Exception e) {
      throw new WSDLException(e);
    }
  }   
  /**
   * Resolves import using base url <code>base</code></b>, and relative <code>relative</code>.
   * @return in case EntityResolver is set and it is used to resolve the entity - org.xml.sax.InputSource is returned, 
   *         otherwise location value in form of java.lang.String is returned.
   */
  private Object resolveImport(String base, String relative, String namespace) throws WSDLException {
    // Resolves external location
    if (this.uriResolver != null) {
      try {
        DOMSource resolved = (DOMSource) this.uriResolver.resolve(relative,base);

        if ((resolved == null) && (uriResolver instanceof NSURIResolver)){
          // If the schema can not be resolved the normal way and the passed resolver is NSResolver -
          // resolve the schema by its ns.
          NSURIResolver nsUriResolver = (NSURIResolver)uriResolver;          
          resolved = (DOMSource) nsUriResolver.resolveByNS(namespace);
        }                       
        return resolved;
      } catch (TransformerException t) {
        throw new WSDLException(t);
      }      
    }
    InputSource in = null;
    if (wsdlResolver != null) {
      try {
        URL importURL = resolveUri(base, relative);
        in = wsdlResolver.resolveEntity(null, importURL.toExternalForm());
      } catch (Exception e) {
        throw new WSDLException(e);
      }
    }
    URL locationURL = null;
    if (in == null) {
      locationURL = resolveUri(base, relative);
      return locationURL.toExternalForm();
    } else {
      return in;
    }    
  }   

  /**
   * Checks <code>ncNameForCheck</code> whether is a valid NCName.If it is not, an exception is thrown, which exception
   * describes the wsdl origin of the check NCName, based on <code>ncNameWSDLPath</code>. 
   * @throws WSDLUnmarshalException if <code>ncNameForCheck</code> is not a valid NCName.
   */
  private void checkNCNameAndThrowException(String ncNameForCheck, String ncNameWSDLPath) throws WSDLUnmarshalException {
    boolean b = Symbols.isNCName(ncNameForCheck);
    if (! b) {
      throw new WSDLUnmarshalException(WSDLUnmarshalException.INVALID_NCNAME_ATTRIBUTE_VALUE, new Object[]{ncNameWSDLPath, ncNameForCheck});
    }
  }

  /**
   * Transforms <code>node</code> to String using SharedTransformer
   * 
   * @param node Node
   * @return String
   */

  private String transform(Node node) {
    String elementString;
    try {
      elementString = SharedTransformers.transform(SharedTransformers.DEFAULT_TRANSFORMER, node);
    } catch (TransformerException e) {
      // gives a clue with node name if exception occurs during transformation
      elementString = "<" + node.getNodeName() + "...";
    }
    return elementString;
  }

//================================================== OLD methods ================================

///**
//* Loads WSDL Extension contents.
//*/
//private WSDLExtension loadExtension(WSDLNode parent, Element element) throws WSDLException {
//WSDLExtension result = new WSDLExtension(parent);
//result.setLocalName(element.getLocalName());
//result.setURI(element.getNamespaceURI());
//result.loadAttributes(element);
//Element currentElement = getFirstChildElement(element);

//while (currentElement != null) {
//int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

//if (elementCode == EXTENSION_STATE) {
//WSDLExtension extension = loadExtension(result, currentElement);
//result.addChild(extension);
//} else if (elementCode != DOCUMENTATION_STATE) {
//throw new WSDLException(" Incorrect WSDL file ! Please do not mix valid WSDL Element's in wsdl extension elements !");
//}

//currentElement = getNextElement(currentElement);
//}

//return result;
//}

///**
//* Loads SAP UseFeature Element.
//*/ 
//private SAPUseFeature loadUseFeatureState( WSDLNode parent, Element element) throws WSDLException {
//SAPUseFeature result = new SAPUseFeature(parent);
//result.loadAttributes(element);
//return result;
//}

///**
//* Loads SAP Option Element.
//*/ 
//private SAPOption loadOptionState( WSDLNode parent, Element element) throws WSDLException {
//SAPOption result = new SAPOption(parent);
//result.loadAttributes(element);
//return result;
//}

///**
//* Loads SAP Property Element.
//*/ 
//private SAPProperty loadPropertyState( WSDLNode parent, Element element) throws WSDLException {
//SAPProperty result = new SAPProperty(parent);
//result.loadAttributes(element);
//Element currentElement = getFirstChildElement(element);
//while (currentElement != null) {
//int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());
//switch (elementCode) {
//case SAP_OPTION_STATE: {
//SAPOption option = loadOptionState(result, currentElement);
//result.addOption(option);
//}
//}
//currentElement = getNextElement(currentElement);      
//}
//return result;
//}
///**
//* Loads SAP Feature element.
//*/ 
//private SAPFeature loadFeatureState( WSDLNode parent, Element element)  throws WSDLException {
//SAPFeature result = new SAPFeature(parent);
//result.loadAttributes(element);
//Element currentElement = getFirstChildElement(element);
//while (currentElement != null) {
//int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());      
//switch (elementCode) {
//case SAP_PROPERTY_STATE: {
//SAPProperty property = loadPropertyState(result, currentElement);
//result.addProperty(property);
//}
//}
//currentElement = getNextElement(currentElement);
//}
//return result;
//}

///**
//* Loads the documentation tag
//* @param parent
//* @param element
//* @return
//* @throws WSDLException
//*/
//private WSDLDocumentation loadDocumentation(WSDLNode parent, Element element) throws WSDLException {
//element.normalize();
//WSDLDocumentation result = new WSDLDocumentation(parent);
//Element elementContent = getFirstChildElement(element);
//if (elementContent != null) {
//result.setElementContent(elementContent);
//} else {
//Node node = element.getFirstChild();
//if (node != null) {
//StringBuffer content = new StringBuffer();
//while (node != null) {
//boolean flag = true;
//if (node.getNodeType() == Node.TEXT_NODE) {
//content.append(((Text) node).getData());
//flag = false;
//}
//if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
//content.append(((CDATASection) node).getData());
//flag = false;
//}
//if (node.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
//content.append(node.getNodeValue());
//flag = false;
//}
//if (flag) {
//break;
//}
//node = node.getNextSibling();
//}
//result.setContent(new CharArray(content.toString().trim()));
//}
//}
//return result;
//}
///**
//* Loads binding fault contents.
//*/
//private WSDLBindingFault loadBindingFault(WSDLNode parent, Element element) throws WSDLException {
//WSDLBindingFault result = new WSDLBindingFault(parent);
//result.loadAttributes(element);
//Element currentElement = getFirstChildElement(element);

//while (currentElement != null) {
//int elementCode = getElementCode(currentElement.getNamespaceURI(), currentElement.getLocalName());

//if (elementCode == EXTENSION_STATE) {
//WSDLExtension extension = loadExtension(result, currentElement);
//result.addExtension(extension);
//} else {
//if (elementCode != DOCUMENTATION_STATE) {
//invalidMemberException("bindingFaultChannel", currentElement);
//}
//}

//currentElement = getNextElement(currentElement);
//}

//return result;
//}

///**
//* Joins the input wsdl to this one / without types definition
//*/
//public void joinWSDL(WSDLDefinitions definitions, WSDLDefinitions new_wsdl) throws WSDLException {
//int i;

//for (i = 0; i < new_wsdl.bindings.size(); i++) {
//definitions.addBinding((WSDLBinding) new_wsdl.bindings.get(i));
//} 

//for (i = 0; i < new_wsdl.messages.size(); i++) {
//definitions.addMessage((WSDLMessage) new_wsdl.messages.get(i));
//} 

//for (i = 0; i < new_wsdl.portTypes.size(); i++) {
//definitions.addPortType((WSDLPortType) new_wsdl.portTypes.get(i));
//} 

//for (i = 0; i < new_wsdl.services.size(); i++) {
//definitions.addService((WSDLService) new_wsdl.services.get(i));
//} 

//ArrayList schemaDefinitions = new_wsdl.getSchemaDefinitions();

//for (i = 0; i < schemaDefinitions.size(); i++) {
//definitions.addSchema((DOMSource) schemaDefinitions.get(i), new_wsdl.getTargetNamespace());
//}

//ArrayList features = new_wsdl.getFeatures();

//for (i=0; i < features.size(); i++) {
//definitions.addFeature((SAPFeature) features.get(i));
//}

//if (this.systemIdMapping != null) {
//Hashtable table = new_wsdl.getMirrorLocations();
//Enumeration keys = table.keys();
//while (keys.hasMoreElements()) {
//String key = (String) keys.nextElement();
//String value = (String) table.get(key);
//definitions.setMirrorLocation(key,value);
//}
//}

//if (new_wsdl.types != null) {
//if (definitions.types == null) {
//definitions.types = new_wsdl.types;
//} else {
//// do not thorow exception not fatal error.
////throw new WSDLException(" WSDL 'types' can appear only onse in wsdl document including the imported documents !");
//}
//}
//}

//======================================================== Public methods =============================================

///**
//* Loads wsdl definitions from InputStream and systemId.
//* @param input
//* @return
//* @throws WSDLException
//*/
//public Definitions loadWSDLDocument(InputStream input) throws WSDLException {
////SystemProperties.setProperty(DocumentBuilderFactory.class.getName(),com.sap.engine.lib.jaxp.DocumentBuilderFactoryImpl.class.getName());
//systemIdMapping = null;
//DOMSource source  = loadDOMDocument(input, "");
//return loadDefinitions(source);
//}

///**
//* Loads wsdl definitions from InputStream and systemId.
//* @param input
//* @param systemId
//* @return
//* @throws WSDLException
//*/
//public Definitions loadWSDLDocument(InputStream input, String systemId) throws WSDLException {
////SystemProperties.setProperty(DocumentBuilderFactory.class.getName(),com.sap.engine.lib.jaxp.DocumentBuilderFactoryImpl.class.getName());
//systemIdMapping = null;
//DOMSource source  = loadDOMDocument(input, systemId);
//return loadDefinitions(source);
//}


///**
//* Loads WSDL Definitions structure from file.
//* @param wsdlFileName
//* @return Returns built wsdl definitiond.
//* @throws WSDLException
//*/
//public Definitions loadWSDLDocument(String wsdlFileName) throws WSDLException {
//systemIdMapping = null;
//DOMSource source = null;
//source = loadDOMDocument(wsdlFileName);
//return loadDefinitions(source);
//}

///**
//* Loads WSDL Definitions from mirror location (cache).
//* Must provide a hashtable with mapping with the following
//* "" -> Original WSDL Location
//* "All imported absolute locations" -> local relative locations
//* This map is returned from WSDL Import tool
//*/
//public Definitions loadMirrorWSDLDocument(String wsdlFileName, Hashtable systemIdMap) throws WSDLException {
//this.systemIdMapping = systemIdMap;
//this.reverseMapping.clear();
//try {
//URL rooturl = new File(".").toURL();
//URL location = URLLoader.fileOrURLToURL(rooturl, wsdlFileName);

//this.documentId = location.toString(); //URLLoader.fileOrURLToURL(null,wsdlFileName).toString();
//} catch (IOException e) {
//throw new WSDLException("Unable to convert path "+wsdlFileName+" to url !",e);
//}
//DOMSource source = null;
////File inputFile = getFile(wsdlFileName);
////this.documentId = inputFile.getAbsolutePath();
//source = loadDOMDocument(wsdlFileName);
//return loadDefinitions(source);
//}

///**
//* Loads WSDL Definitions from mirror location (cache).
//* @param input
//* @param systemId
//* @param systemIdMap
//* @return
//* @throws WSDLException
//*/
//public Definitions loadMirrorWSDLDocument(InputStream input, String systemId, Hashtable systemIdMap) throws WSDLException {
//this.systemIdMapping = systemIdMap;
//this.reverseMapping.clear();
//try {
//this.documentId = URLLoader.fileOrURLToURL(null,systemId).toString();
//} catch (IOException e) {
//throw new WSDLException("Unable to convert path "+systemId+" to url !",e);
//}
//DOMSource source = null;
//source = loadDOMDocument(input,systemId);
//return loadDefinitions(source);
//}  
 }