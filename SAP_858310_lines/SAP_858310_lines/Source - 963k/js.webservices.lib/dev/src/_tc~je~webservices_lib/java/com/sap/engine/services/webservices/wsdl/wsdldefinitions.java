/**
 * Title:        xml2000
 * Description:  Holds PortType operation Input/Output/And Fault Channels
 *
 * Copyright:    Copyright (c) 2001
 * Company:      InQMy
 * @author       Chavdar Baykov, Chavdarb@abv.bg
 * @version      July 2001
 */
package com.sap.engine.services.webservices.wsdl;

import com.sap.engine.lib.xml.parser.handlers.SimpleAttr;
import com.sap.engine.lib.xml.util.QName;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaGenerator;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaGeneratorException;
import com.sap.engine.services.webservices.jaxrpc.util.PackageBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

public class WSDLDefinitions extends WSDLNamedNode {

  public String targetNamespace;
  public WSDLTypes types;
  public ArrayList messages;
  public ArrayList portTypes;
  public ArrayList bindings;
  public ArrayList services;
  public ArrayList features;
  public ArrayList extensions;

  /**
   * Returns true if this wsdl is SAP-WSDL by inspecting definitions documentation tag.
   * @return
   */
  public boolean isSapWsdl() {
    WSDLDocumentation documentation = this.getDocumentation();
    if (documentation != null) {
      Element elementContent = documentation.getElementContent();
      if (elementContent != null) {
        if (elementContent.getLocalName().equals("SAP_WSDL") && "http://www.sap.com/webas".equals(elementContent.getNamespaceURI())) {
          return true;
        }
      }
//      String content = documentation.getContent().toString();
//      if (content.length() != 0) {
//        if ("SAP_WSDL".equals(content)) {
//          return true;
//        }
//        if ("<sap:SAP_WSDL xmlns:sap=\"http://www.sap.com/webas\" />".equals(content)) {
//          return true;
//        }
//      }
    }
    return false;
  }

  /**
   * This returns true is this WSDL Definitions are produced by SAP but this is not SAP-WSDL with features.
   * @return
   */
  public boolean isSapOrigin() {
    WSDLDocumentation documentation = this.getDocumentation();
    if (documentation != null) {
      Element contentElement = documentation.getElementContent();
      if (contentElement != null && WSDLDOMLoader.WSDL_SAP_NAMESPACE.equals(contentElement.getNamespaceURI()) && "SAP_WS".equals(contentElement.getLocalName())) {
        return true;
      }
    }
    return false;
  }


  public ArrayList getImportDeclaratuions() {
    return importDeclaratuions;
  }

  public void setImportDeclaratuions(ArrayList importDeclaratuions) {
    this.importDeclaratuions = importDeclaratuions;
  }

  public ArrayList importDeclaratuions;
  public String xsdNamespace = null;
  private String systemId;
  private ArrayList schemaDefinitions;
  private Hashtable mirrorMapping; // A map from remote file locations to local file locations
  private Hashtable mirrorLocations; // A map from local file locations to remote file locations.
  private SchemaToJavaGenerator schemaInfo;//$JL-SER$
  private Hashtable additionalAttrs = new Hashtable(); //added by Misho

  public WSDLDefinitions() {
    super();
    targetNamespace = null;
    types = null;
    messages = new ArrayList();
    portTypes = new ArrayList();
    bindings = new ArrayList();
    services = new ArrayList();
    schemaDefinitions = new ArrayList();
    importDeclaratuions = new ArrayList();
    features = new ArrayList();
    mirrorMapping = null;
    mirrorLocations = new Hashtable();
    extensions = new ArrayList();
  }

  public void addExtension(WSDLExtension extension) {
    extensions.add(extension);
  }

  public ArrayList getExtensions() {
    return extensions;
  }


  public WSDLDefinitions(WSDLNode parent) {
    super(parent);
    types = null;
    messages = new ArrayList();
    portTypes = new ArrayList();
    bindings = new ArrayList();
    services = new ArrayList();
    schemaDefinitions = new ArrayList();
    importDeclaratuions = new ArrayList();
    features = new ArrayList();
    this.mirrorMapping = null;
    mirrorLocations = new Hashtable();
    extensions = new ArrayList();
  }

  /**
   * Set's mirror mapping map. This indicates that mirror wsdl image will be used.
   * @param mirrorMapping
   */
  public void setMirrorMapping(Hashtable mirrorMapping) {
    this.mirrorMapping = mirrorMapping;
  }

  /**
   * Sets mapping from local path to remote path.
   */
  public void setMirrorLocation(String localPath, String remoteLocation) {
    this.mirrorLocations.put(localPath,remoteLocation);
  }

  public Hashtable getMirrorLocations() {
    return this.mirrorLocations;
  }

  public Hashtable getMirrorMapping() {
    return this.mirrorMapping;
  }

  private String getSchemaNamespace(Node node) {
    String result = "";
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      Element element = (Element) node;
      if (NS.XS.equals(element.getNamespaceURI()) && "schema".equals(element.getLocalName())) {
        result = element.getAttribute("targetNamespace");
      }
    }
    return result;
  }

  public void addSchema(DOMSource schemaSource, String namespace) {
    String currentns = getSchemaNamespace(schemaSource.getNode());
    if (currentns.length() == 0) {
      currentns = namespace;
    }
    for (int i=0; i<schemaDefinitions.size(); i++) {
      DOMSource source = (DOMSource) schemaDefinitions.get(i);
      String onamespace = getSchemaNamespace(source.getNode());
      if (onamespace.equals(currentns)) {
        return;
      }
    }
    schemaDefinitions.add(schemaSource);
  }

  public void addSchema(DOMSource schemaSource) {
    String currentns = getSchemaNamespace(schemaSource.getNode());
    for (int i=0; i<schemaDefinitions.size(); i++) {
      DOMSource source = (DOMSource) schemaDefinitions.get(i);
      String namespace = getSchemaNamespace(source.getNode());
      if (namespace.equals(currentns)) {
        return;
      }
    }
    schemaDefinitions.add(schemaSource);
  }

  public ArrayList getSchemaDefinitions() {
    return schemaDefinitions;
  }

  public void loadAttributes(SimpleAttr[] attr, int attrCount) throws WSDLException {
    String name = SimpleAttr.getAttribute("name", attr, attrCount);
    if (name != null) {
      this.name = name;
    }
    targetNamespace = SimpleAttr.getAttribute("targetNamespace", attr, attrCount);
  }

  public void loadAttributes(Element element) throws WSDLException {
    NamedNodeMap nodeMap = element.getAttributes();
    int length = nodeMap.getLength();

    Attr attribute;
    for (int i = 0; i < length; i++) {
      attribute = (Attr) nodeMap.item(i);
      if (attribute.getName().equals("name")) {
        super.setName(attribute.getValue());
      } else if (attribute.getName().equals("targetNamespace")) {
        targetNamespace = attribute.getValue();
      } else {
        this.addAdditionalAttribute(attribute.getName(), attribute.getValue());
      }
    }

    if (targetNamespace == null) {
      throw new WSDLException(" WSDL 'definitions' must have 'targetNamespace' attribute !");
    }

  }

  public WSDLPortType getPortType(String portTypeName, String namespace) {
    WSDLPortType perm;

    for (int i = 0; i < portTypes.size(); i++) {
      perm = (WSDLPortType) portTypes.get(i);
      if (perm.getName().equals(portTypeName)) {
        if (namespace == null) {
          if (perm.getNamespace() == null) {
            return perm;
          }
        } else {
          if (namespace.equals(perm.getNamespace())) {
            return perm;
          }
        }

      }
    }

    return null;
  }

  /**
   * Returns WSDL message by it's name.
   * @param messageName
   * @param namespace
   * @return
   */
  public WSDLMessage getMessage(String messageName, String namespace) {
    WSDLMessage perm;

    for (int i = 0; i < messages.size(); i++) {
      perm = (WSDLMessage) messages.get(i);

      if (perm.getName().equals(messageName)) {
        if (namespace == null) {
          if (perm.getNamespace() == null) {
            return perm;
          }
        } else {
          if (namespace.equals(perm.getNamespace())) {
            return perm;
          }
        }

      }
    }
    return null;
  }

  /**
   * Returns WSDL message by it's qname.
   */
  public WSDLMessage getMessage(QName qname) {
    WSDLMessage perm;

    for (int i = 0; i < messages.size(); i++) {
      perm = (WSDLMessage) messages.get(i);

      if (perm.getName().equals(qname.getLocalName())) {
        if (qname.getURI() == null) {
          if (perm.getNamespace() == null) {
            return perm;
          }
        } else {
          if (qname.getURI().equals(perm.getNamespace())) {
            return perm;
          }
        }

      }
    }
    return null;
  }

  /**
   * Returns the parent of all nodes
   */
  public WSDLNode getDocument() {
    return this;
  }

  public void addMessage(WSDLMessage mess) throws WSDLException {
    for (int i = 0; i < messages.size(); i++) {
      WSDLMessage curMessage = (WSDLMessage) messages.get(i);
      if (curMessage.getQName().equals(mess.getQName())) {
        return;
        // Do nothing do not override messages.
        //throw new WSDLException("Many messages with same name not allowed in WSDL document ! Problematic name: {"+curMessage.getNamespace()+"}" + curMessage.getName() + "");
      }
    }
    messages.add(mess);
  }

  public void addPortType(WSDLPortType portType) throws WSDLException {
    for (int i = 0; i < portTypes.size(); i++) {
      WSDLPortType curPortType = (WSDLPortType) portTypes.get(i);

      if (curPortType.getQName().equals(portType.getQName())) {
        return;
        // Do nothing
        //throw new WSDLException("Many portTypes with same name not allowed in WSDL document !");
      }
    } 

    portTypes.add(portType);
  }

  public void addBinding(WSDLBinding binding) throws WSDLException {
    for (int i = 0; i < bindings.size(); i++) {
      if (((WSDLBinding) bindings.get(i)).getQName().equals(binding.getQName())) {
        return;
      }
    }
    bindings.add(binding);
  }

  public void addService(WSDLService service) {
    services.add(service);
  }

  public ArrayList getMessages() {
    return messages;
  }

  public ArrayList getPortTypes() {
    return portTypes;
  }

  public ArrayList getBindings() {
    return bindings;
  }

  public ArrayList getServices() {
    return services;
  }

  public void addAdditionalAttribute(String name, String value) {
    additionalAttrs.put(name, value);
  }

  public Hashtable getAdditionalAttributes() {
    return additionalAttrs;
  }

  public String getAdditionalAttrValue(String name) {
    return (String) additionalAttrs.get(name);
  }

  /**
   * Returns WSDL targetNamespace
   */
  public String getTargetNamespace() {
    return targetNamespace;
  }

  /**
   * Sets System id of WSDL
   */
  public void setSystemId(String id) {
    this.systemId = id;
  }

  /**
   * Returns SystemId of WSDL file.
   */
  public String getSystemId() {
    return this.systemId;
  }

  /**
   * Call this to load all schema-s into Schema-to-Java generator.
   * @throws WSDLException
   */
  public void loadSchemaInfo() throws WSDLException {
    if (schemaDefinitions.size() == 0) {
      schemaInfo = null;
      return;
    }
    try {
    schemaInfo = new SchemaToJavaGenerator();
    schemaInfo.setPackageBuilder(new PackageBuilder());
    for (int i = 0; i < schemaDefinitions.size(); i++) {
      schemaInfo.addSchemaSource((DOMSource) schemaDefinitions.get(i));
    }
    } catch (SchemaToJavaGeneratorException e) {
      throw new WSDLException("Unable to load Schema information. See nested Exception :",e);
    }
  }

  public WSDLBinding getBinding(String bindingName, String namespace) {
    for (int i=0; i<bindings.size(); i++) {
      WSDLBinding binding = (WSDLBinding) bindings.get(i);
      if (binding.getName().equals(bindingName)) {
        if (namespace == null) {
          if (binding.getNamespace() == null) {
            return binding;
          }
        } else {
          if (namespace.equals(binding.getNamespace())) {
            return binding;
          }
        }

      }
    }
    return null;
  }

  /**
   * Returns binding node with given qname.
   * @param name
   * @return
   */
  public WSDLBinding getBinding(QName name) {
    for (int i=0; i<bindings.size(); i++) {
      WSDLBinding binding = (WSDLBinding) bindings.get(i);
      if (binding.getName().equals(name.getLocalName())) {
        if (name.getURI() == null) {
          if (binding.getNamespace() == null) {
            return binding;
          }
        } else {
          if (name.getURI().equals(binding.getNamespace())) {
            return binding;
          }
        }

      }
    }
    return null;
  }

  public SchemaToJavaGenerator getSchemaInfo() {
    return this.schemaInfo;
  }
  
  public ArrayList getFeatures() {
    return this.features;    
  }
  
  public void addFeature(SAPFeature feature) {
    this.features.add(feature);
  }

  private boolean qnamesEqual(QName qname, WSDLNamedNode node) {
    boolean flag = false;
    if (qname.getURI() == null && node.getNamespace() == null) {
      flag = true;
    }
    if (qname.getURI() != null && qname.getURI().equals(node.getNamespace())) {
      flag = true;
    }
    if (flag) {
      if (qname.getLocalName() == null && node.getName() == null) {
        return true;
      }
      if (qname.getLocalName() != null && qname.getLocalName().equals(node.getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns feature by it's name.
   */ 
  public SAPFeature getFeatureByName(QName name) {
    for (int i=0; i<features.size(); i++) {
      SAPFeature feature = (SAPFeature) features.get(i);
      if (qnamesEqual(name,feature)) {
        return feature;
      }
    }
    return null;
  }

  /**
   * Returns feature by it's name.
   */
  public SAPFeature getFeatureByName(String name) {
    if (name == null) {
      return null;
    }
    for (int i=0; i<features.size(); i++) {
      SAPFeature feature = (SAPFeature) features.get(i);
      if (name.equals(feature.getName())) {
        return feature;
      }
    }
    return null;
  }


  /**
   * Returns Feature bu his uri.
   */ 
  public SAPFeature getFeatureByUri(String uri) {
    for (int i=0; i<features.size(); i++) {
      SAPFeature feature = (SAPFeature) features.get(i);
      if (feature.getUri().equals(uri)) {
        return feature;
      }
    }
    return null;
  }

  public void writeTo(OutputStream out) throws IOException, WSDLException {
     WSDLDefinitionsParser parser = new WSDLDefinitionsParser();
     parser.writeDefintionsToStream(this, out);
  }

}

