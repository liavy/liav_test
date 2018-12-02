package com.sap.engine.services.webservices.espbase.configuration.idempotency;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.engine.lib.schema.Constants;
import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.services.webservices.espbase.wsdl.XSDTypeContainer;

public class XSDBuilder {

  private static final String MESSAGE_HEADER_ELEMENT_NAME = "MessageHeader";
  private static final String UUID_ELEMENT_NAME = "UUID";
  private static final String ID_ELEMENT_NAME = "ID";
  
  private Hashtable<String, XSDComponent> xsdComponents;
  private XSDTypeContainer xsdTypeContainer;
  
  XSDBuilder(XSDTypeContainer xsdTypeContainer) {
    this.xsdTypeContainer = xsdTypeContainer;
    xsdComponents = new Hashtable();
  }
  
  XSDComponent getXSDTopLevelType(QName typeName) {
    XSDComponent type = getRegisteredXSDComponent(Constants.NODE_COMPLEX_TYPE_NAME, typeName);
    if(type != null) {
      return(type);
    }
    if(Constants.SCHEMA_COMPONENTS_NS.equals(typeName.getNamespaceURI()) && Constants.TYPE_ANY_TYPE_NAME.equals(typeName.getLocalPart())) {
      return(XSDComponent.EMPTY);
    }
    Element xsdTypeDOMElement = getXSDTopLevelComponentDOMElement(typeName, Constants.NODE_COMPLEX_TYPE_NAME);
    type = xsdTypeDOMElement != null ? getXSDType(xsdTypeDOMElement) : XSDComponent.UNIMPORTANT;
    registerXSDComponent(Constants.NODE_COMPLEX_TYPE_NAME, typeName, type);
    return(type);
  }
  
  XSDComponent getXSDTopLevelElement(QName elementName) {
    XSDComponent element = getRegisteredXSDComponent(Constants.NODE_ELEMENT_NAME, elementName);
    if(element != null) {
      return(element);
    }
    Element xsdElementDOMElement = getXSDTopLevelComponentDOMElement(elementName, Constants.NODE_ELEMENT_NAME);
    element = xsdElementDOMElement != null ? getXSDElement(xsdElementDOMElement) : XSDComponent.UNIMPORTANT;
    registerXSDComponent(Constants.NODE_ELEMENT_NAME, elementName, element);
    return(element);
  }
  
  private XSDComponent getXSDElement(Element xsdElementDOMElement) {
    Attr nameAttr = xsdElementDOMElement.getAttributeNode(Constants.NODE_NAME_NAME);
    String xsdElementName = nameAttr.getValue();
    QName xsdTopLevelTypeName = getXSDTopLevelComponentName(xsdElementDOMElement, Constants.NODE_TYPE_NAME);
    XSDComponent type = XSDComponent.UNIMPORTANT; 
    if(xsdTopLevelTypeName != null) {
      type = getXSDTopLevelType(xsdTopLevelTypeName);
    } else {
      Element xsdAnonymousComplexTypeElement = getXSDAnonymousComplexTypeElement(xsdElementDOMElement);
      if(xsdAnonymousComplexTypeElement != null) {
        type = getXSDType(xsdAnonymousComplexTypeElement);
      }
    }
    if(type == XSDComponent.UNIMPORTANT || type == XSDComponent.EMPTY) {
      if(UUID_ELEMENT_NAME.equalsIgnoreCase(xsdElementName)) {
        return(XSDComponent.UUID);
      }
      if(ID_ELEMENT_NAME.equalsIgnoreCase(xsdElementName)) {
        return(XSDComponent.ID);
      }
    }
    if(type == XSDComponent.MSG_HEADER && MESSAGE_HEADER_ELEMENT_NAME.equals(xsdElementName)) {
      return(XSDComponent.MSG_HEADER);
    }
    if(type == XSDComponent.IDEMPOTENT) {
      return(XSDComponent.IDEMPOTENT);
    }
    return(XSDComponent.UNIMPORTANT);
  }
  
  private void getXSDTopLevelGroupContent(QName groupName, Vector<XSDComponent> content) {
    Element xsdGroupDOMElement = getXSDTopLevelComponentDOMElement(groupName, Constants.NODE_GROUP_NAME);
    if(xsdGroupDOMElement != null) {
      getXSDGroupContent(xsdGroupDOMElement, content);
    } else {
      content.add(XSDComponent.UNIMPORTANT);
    }
  }
  
  private static QName getXSDTopLevelComponentName(Element xsdComponentDOMElement, String attrName) {
    Attr xsdTopLevelComponentNameAttr = xsdComponentDOMElement.getAttributeNode(attrName);
    if(xsdTopLevelComponentNameAttr != null) {
      String xsdTopLevelComponentName = xsdTopLevelComponentNameAttr.getValue();
      String[] prefixAndLocalName = getPrefixAngLocalName(xsdTopLevelComponentName);
      Hashtable<String, String> nsMapping = DOM.getNamespaceMappingsInScope(xsdComponentDOMElement);
      return(new QName(nsMapping.get(prefixAndLocalName[0]), prefixAndLocalName[1]));
    }
    return(null);
  }
  
  private static Element getXSDAnonymousComplexTypeElement(Element xsdElementDOMElement) {
    NodeList childNodes = xsdElementDOMElement.getChildNodes();
    for(int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      if(childNode.getNodeType() == Node.ELEMENT_NODE) {
        Element childElement = (Element)childNode;
        if(Constants.SCHEMA_COMPONENTS_NS.equals(childElement.getNamespaceURI()) && Constants.NODE_COMPLEX_TYPE_NAME.equals(childElement.getLocalName())) {
          return(childElement);
        }
      }
    }
    return(null);
  }
  
  private static String[] getPrefixAngLocalName(String name) {
    int prefixFromNameDelimiterIndex = name.indexOf(':');
    return(prefixFromNameDelimiterIndex > 0 ? new String[]{name.substring(0, prefixFromNameDelimiterIndex), name.substring(prefixFromNameDelimiterIndex + 1)} : new String[]{"", name});
  }
  
  private XSDComponent getXSDType(Element xsdTypeDOMElement) {
    Vector<XSDComponent> content = getXSDTypeContent(xsdTypeDOMElement);
    if(content.size() == 0) {
      return(XSDComponent.EMPTY);
    }
    XSDComponent firstXSDComponent = content.get(0); 
    if(firstXSDComponent == XSDComponent.MSG_HEADER) {
      return(XSDComponent.IDEMPOTENT);
    }
    if(firstXSDComponent == XSDComponent.ID) {
      if(content.size() > 1) {
        if(content.get(1) == XSDComponent.UUID) {
          return(XSDComponent.MSG_HEADER);
        }
      } else {
        return(XSDComponent.ID);
      }
    }
    return(XSDComponent.UNIMPORTANT);
  }
  
  private Vector<XSDComponent> getXSDTypeContent(Element xsdTypeDOMElement) {
    Vector<XSDComponent> content = new Vector();
    NodeList childNodes = xsdTypeDOMElement.getChildNodes();
    for(int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      if(childNode.getNodeType() == Node.ELEMENT_NODE) {
        Element childElement = (Element)childNode;
        String childElementNs = childElement.getNamespaceURI();
        String childElementName = childElement.getLocalName(); 
        if(Constants.SCHEMA_COMPONENTS_NS.equals(childElementNs)) {
          if(Constants.NODE_SEQUENCE_NAME.equals(childElementName)) {
            getXSDSequenceGroupContent(childElement, content);
          } else if(Constants.NODE_COMPLEX_CONTENT_NAME.equals(childElementName)) {
            getXSDTypeComplexContent(childElement, content);
          } else if(Constants.NODE_GROUP_NAME.equals(childElementName)) {
            getXSDAnonymousGroupContent(childElement, content);
          }
        }
      }
    }
    return(content);
  }
  
  private void getXSDTypeComplexContent(Element xsdComplContentDOMElement, Vector<XSDComponent> content) {
    NodeList childNodes = xsdComplContentDOMElement.getChildNodes();
    for(int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      if(childNode.getNodeType() == Node.ELEMENT_NODE) {
        Element childElement = (Element)childNode;
        String childElementNs = childElement.getNamespaceURI(); 
        String childElementName = childElement.getLocalName();
        if(Constants.SCHEMA_COMPONENTS_NS.equals(childElementNs)) {
          if(Constants.NODE_RESTRICTION_NAME.equals(childElementName)) {
            getXSDTypeInheritanceContent(childElement, content);
          } if(Constants.NODE_EXTENSION_NAME.equals(childElementName)) {
            getXSDTypeExtendedContent(childElement, content);
          }
        }
      }
    }
  }

  private void getXSDTypeExtendedContent(Element xsdExtensionDOMElement, Vector<XSDComponent> content) {
    XSDComponent type = getXSDTopLevelType(getXSDTopLevelComponentName(xsdExtensionDOMElement, Constants.NODE_BASE_NAME));
    if(type == XSDComponent.IDEMPOTENT) {
      content.add(XSDComponent.MSG_HEADER);
      return;
    }
    if(type == XSDComponent.MSG_HEADER) {
      content.add(XSDComponent.ID);
      content.add(XSDComponent.UUID);
      return;
    }
    if(type == XSDComponent.UNIMPORTANT) {
      content.add(XSDComponent.UNIMPORTANT);
      return;
    }
    if(type == XSDComponent.ID) {
      content.add(XSDComponent.ID);
    }
    getXSDTypeInheritanceContent(xsdExtensionDOMElement, content);
  }
  
  private void getXSDTypeInheritanceContent(Element xsdInheritanceDOMElement, Vector<XSDComponent> content) {
    NodeList childNodes = xsdInheritanceDOMElement.getChildNodes();
    for(int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      if(childNode.getNodeType() == Node.ELEMENT_NODE) {
        Element childElement = (Element)childNode;
        String childElementNs = childElement.getNamespaceURI(); 
        String childElementName = childElement.getLocalName();
        if(Constants.SCHEMA_COMPONENTS_NS.equals(childElementNs)) {
          if(Constants.NODE_SEQUENCE_NAME.equals(childElementName)) {
            getXSDSequenceGroupContent(childElement, content);
          } else if(Constants.NODE_GROUP_NAME.equals(childElementName)) {
            getXSDAnonymousGroupContent(childElement, content);
          }
        }
      }
    }
  }

  private void getXSDAnonymousGroupContent(Element xsdGroupDOMElement, Vector<XSDComponent> content) {
    QName xsdTopLevelGroupName = getXSDTopLevelComponentName(xsdGroupDOMElement, Constants.NODE_REF_NAME);
    if(xsdTopLevelGroupName != null) {
      getXSDTopLevelGroupContent(xsdTopLevelGroupName, content);
    } else {
      getXSDGroupContent(xsdGroupDOMElement, content);
    }
  }
  
  private void getXSDGroupContent(Element xsdGroupDOMElement, Vector<XSDComponent> content) {
    NodeList childNodes = xsdGroupDOMElement.getChildNodes();
    for(int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      if(childNode.getNodeType() == Node.ELEMENT_NODE) {
        Element childElement = (Element)childNode;
        if(Constants.SCHEMA_COMPONENTS_NS.equals(childElement.getNamespaceURI()) && Constants.NODE_SEQUENCE_NAME.equals(childElement.getLocalName())) {
          getXSDSequenceGroupContent(childElement, content);
        }
      }
    }
  }
  
  private void getXSDSequenceGroupContent(Element xsdSequenceGroupDOMElement, Vector<XSDComponent> content) {
    NodeList childNodes = xsdSequenceGroupDOMElement.getChildNodes();
    for(int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      if(childNode.getNodeType() == Node.ELEMENT_NODE) {
        Element childElement = (Element)childNode;
        if(Constants.SCHEMA_COMPONENTS_NS.equals(childElement.getNamespaceURI())) {
          String childElementName = childElement.getLocalName();
          if(Constants.NODE_GROUP_NAME.equals(childElementName)) {
            getXSDAnonymousGroupContent(childElement, content);
          } else if(Constants.NODE_SEQUENCE_NAME.equals(childElementName)) {
            getXSDSequenceGroupContent(childElement, content);
          } else if(Constants.NODE_ELEMENT_NAME.equals(childElementName)) {
            content.add(getXSDAnonymousElement(childElement));
          } else {
            content.add(XSDComponent.UNIMPORTANT);
          }
          if(content.size() == 1) {
            XSDComponent firstXSDComponent = content.get(0); 
            if(firstXSDComponent == XSDComponent.UNIMPORTANT || firstXSDComponent == XSDComponent.MSG_HEADER) {
              return;
            } 
            if(firstXSDComponent != XSDComponent.ID) {
              clearContent(content);
              return;
            }
          } else if(content.size() > 1) {
            if(content.get(1) != XSDComponent.UUID) {
              clearContent(content);
            }
            return;
          }
        }
      }
    }
  }
  
  private void clearContent(Vector<XSDComponent> content) {
    content.clear();
    content.add(XSDComponent.UNIMPORTANT);
  }
  
  private XSDComponent getXSDAnonymousElement(Element xsdElementDOMElement) {
    QName xsdTopLevelElementName = getXSDTopLevelComponentName(xsdElementDOMElement, Constants.NODE_REF_NAME);
    if(xsdTopLevelElementName != null) {
      return(getXSDTopLevelElement(xsdTopLevelElementName));
    }
    return(getXSDElement(xsdElementDOMElement));
  }
  
  private Element getXSDTopLevelComponentDOMElement(QName xsdComponentName, String xmlComponentName) {
    List xsdDOMSourcesList = xsdTypeContainer.getSchemas();
    Iterator<DOMSource> xsdDOMSources = xsdDOMSourcesList.iterator();
    while(xsdDOMSources.hasNext()) {
      Element xsdTopLevelComponentElement = getXSDTopLevelComponentDOMElement(xsdComponentName, xmlComponentName, xsdDOMSources.next()); 
      if(xsdTopLevelComponentElement != null) {
        return(xsdTopLevelComponentElement);
      }
    }
    return(null);
  }
  
  private Element getXSDTopLevelComponentDOMElement(QName xsdComponentName, String xmlComponentName, DOMSource xsdDOMSource) {
    Element xsdElement = getXSDRootDOMElement(xsdDOMSource);
    String targetNamespace = getXSDTargetNamespace(xsdElement);
    if(targetNamespace.equals(xsdComponentName.getNamespaceURI())) {
      NodeList childNodes = xsdElement.getChildNodes();
      for(int i = 0; i < childNodes.getLength(); i++) {
        Node xsdNode = childNodes.item(i);
        if(xsdNode.getNodeType() == Node.ELEMENT_NODE) {
          Element childElement = (Element)xsdNode;
          if(Constants.SCHEMA_COMPONENTS_NS.equals(childElement.getNamespaceURI()) && xmlComponentName.equals(childElement.getLocalName())) {
            Attr nameAttr = childElement.getAttributeNode(Constants.NODE_NAME_NAME);
            if(nameAttr != null && nameAttr.getValue().equals(xsdComponentName.getLocalPart())) {
              return(childElement);
            }
          }
        }
      }
    }
    return(null);
  }
  
  private String getXSDTargetNamespace(Element xsdElement) {
    Attr targetNSAttr = xsdElement.getAttributeNode(Constants.NODE_TARGET_NAMESPACE_NAME);
    return(targetNSAttr == null ? "" : targetNSAttr.getValue());
  }
  
  private Element getXSDRootDOMElement(DOMSource xsdDOMSource) {
    Node xsdNode = xsdDOMSource.getNode();
    if(xsdNode != null) {
      switch(xsdNode.getNodeType()) {
        case(Node.ELEMENT_NODE) : {
          return((Element)xsdNode);
        }
        case(Node.DOCUMENT_NODE) : {
          return(((Document)xsdNode).getDocumentElement());
        }
      }
    }
    throw new IllegalArgumentException("XSD DOM source object should contain Element node or Document node!");
  }
  
  private XSDComponent getRegisteredXSDComponent(String xmlComponentName, QName xsdComponentName) {
    return(xsdComponents.get(createKey(xmlComponentName, xsdComponentName)));
  }
  
  private void registerXSDComponent(String xmlComponentName, QName xsdComponentName, XSDComponent component) {
    xsdComponents.put(createKey(xmlComponentName, xsdComponentName), component);
  }
  
  private String createKey(String xmlComponentName, QName xsdComponentName) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(xmlComponentName);
    buffer.append(xsdComponentName);
    return(buffer.toString());
  }
}
