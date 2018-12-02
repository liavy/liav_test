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
package com.sap.engine.services.webservices.espbase.client.jaxws.bindext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.engine.lib.xml.dom.DOM;
import com.sap.engine.services.webservices.espbase.client.JAXWSFileParser;
import com.sap.engine.services.webservices.espbase.client.NamespaceContextResolver;
import com.sap.engine.services.webservices.espbase.client.jaxws.cts.ExtBindingCustomization;
import com.sap.engine.services.webservices.espbase.wsdl.Base;
import com.sap.engine.services.webservices.espbase.wsdl.ExtensionElement;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Parameter;
import com.sap.engine.services.webservices.espbase.wsdl.XSDRef;
import com.sap.engine.services.webservices.jaxrpc.exceptions.JaxWsMappingException;
import com.sap.engine.services.webservices.wsdl.WSDLException;

/**
 * @author Dimitar Velichkov dimitar.velichkov@sap.com
 * 
 */
public abstract class ExtendedElement {

	protected Base						current								= null;
	protected ExtendedElement	parent								= null;
	protected LinkedList			extensions						= null;
	protected HashMap					propagatingExtensions	= null;
	protected boolean					recursive							= false;
  protected String          javaDoc               = null;
  
	public static final QName	ASYNC_MAPPING					= new QName(ExtBindingCustomization.JAXWS_NS, "enableAsyncMapping");
	public static final QName	WRAPPER_STYLE					= new QName(ExtBindingCustomization.JAXWS_NS, "enableWrapperStyle");
	public static final QName	MIME_CONTENT					= new QName(ExtBindingCustomization.JAXWS_NS, "enableMIMEContent");
	public static final QName	SOAP_HEADER_MAPPING		= new QName(ExtBindingCustomization.JAXWS_NS, "enableAdditionalSOAPHeaderMapping");
	protected HashMap					wsdlChildToExtChild		= null;	

	protected ExtendedElement(Base current, ExtendedElement parent, boolean recursive) {

		// parent can be null, but not the current elem
		if (current == null) {
			throw new NullPointerException("Cannot modify null element with binding extensions!");
		}

		this.current = current;
		this.parent = parent;
		this.recursive = recursive;
		extensions = new LinkedList();
		propagatingExtensions = new HashMap();
		wsdlChildToExtChild = new HashMap();
	}

	/**
	 * gather all extension elements
	 * @throws JaxWsMappingException 
	 * 
	 */
	protected void getExtensionElements() throws JaxWsMappingException {

		extensions.clear();

		ObjectList children = current.getChildren();

		int childrenLength = children.getLength();
		for (int i = 0; i < childrenLength; ++i) {

			Base child = children.item(i);
			if (child instanceof ExtensionElement) {
			  
        Element[] theExts = getExtensionContent((ExtensionElement)child);
        for(int extIx = 0; extIx < theExts.length; ++extIx){
          extensions.add(theExts[extIx]);  
        }
        
      }
    }

	}

  protected Element[] getExtensionContent(ExtensionElement parent) throws JaxWsMappingException{
    
    ArrayList els = new ArrayList();
    
    if(parent.getContent().getNamespaceURI().equals(ExtBindingCustomization.JAXWS_NS)){
      Element content = parent.getContent();
      if(!content.getLocalName().equals("bindings")){
        throw new JaxWsMappingException(JaxWsMappingException.ILLEGAL_JAXWS_DECL, parent.getContent().toString());
      }
      
      NodeList childNodes = content.getChildNodes();
      for (int chIx = 0; chIx < childNodes.getLength(); ++chIx) {
        Node possibleEx = childNodes.item(chIx);
        if (possibleEx.getNodeType() == Node.ELEMENT_NODE) {
          Element el = ((Element) possibleEx);
          if (el.getNamespaceURI().equals(ExtBindingCustomization.JAXWS_NS)) {
            els.add(el);
          }

        }

      }

    }
    
    Element[] allElems = new Element[els.size()];
    allElems = (Element[])els.toArray(allElems);
    els = null;
    
    return allElems;
  }
  
	/**
   * Apply JAX-WS extensions to this WSDL element
   * 
   * @throws BindingFormatException
   *           thrown if structure of binding extensions is incorrect
   * 
   */
	public void applyExtensions() throws JaxWsMappingException {
		getExtensionElements();
		checkParentExtensions();
	}

	/**
	 * Must go through parent's propagating extension elements
	 * 
	 */
	protected abstract void checkParentExtensions();

	/**
	 * Some extension element values can propagate to the element's children -
	 * setting the value in the parent makes it apply to the children unless they
	 * override it
	 * 
	 * @param extension
	 *          the propagating extension to check for
	 * @return the extension element's value if set, null if not set
	 */
	protected String getPropagatingExtensionValue(QName extension) {
		if (!extension.equals(ASYNC_MAPPING) && !extension.equals(MIME_CONTENT) && !extension.equals(SOAP_HEADER_MAPPING)
				&& !extension.equals(WRAPPER_STYLE)) {
			throw new IllegalArgumentException("extension being tested must be a QName constant defined in ExtendedElement");
		}

		if (propagatingExtensions.containsKey(extension)) {
			return (String) propagatingExtensions.get(extension);
		}
		else if (parent != null) {
			return parent.getPropagatingExtensionValue(extension);
		}

		else {
			return null;
		}
	}

  protected String getJavaDocText(Element parent){
    
    NodeList ch = parent.getElementsByTagNameNS(ExtBindingCustomization.JAXWS_NS, "javadoc");
    String jDoc = null;

    if(ch.getLength() > 0){
      NodeList texts = ch.item(0).getChildNodes();
      
      for(int j = 0; j < texts.getLength(); ++j){
        if(texts.item(j).getNodeType() == Node.TEXT_NODE){
                   
          StringBuffer clean = new StringBuffer();
          String dirty = texts.item(j).getNodeValue();
          for(int ix = 0; ix < dirty.length(); ++ix){
            if(!Character.isISOControl(dirty.charAt(ix))){
              clean.append(dirty.charAt(ix));
            }
          }
          jDoc = clean.toString();
          break;
        }
      }
    }
    
    if(jDoc != null && jDoc.indexOf("/**") < 0){
      String nl = System.getProperty("line.separator");
      jDoc = "/** " + nl + " * " + jDoc + nl + " */"; 
    }
    return jDoc;  
  }

	/**
	 * Get the extended element that corresponds to the requested wsdlElement (the
	 * extended element to search for must be a child of the current extended
	 * element)
	 * 
	 * @param the
	 *          requested WSDL Element QName
	 * @return the extended element (null if not found)
	 */
	public ExtendedElement getExtendedChild(QName wsdlElem) {
		return (ExtendedElement) wsdlChildToExtChild.get(wsdlElem);
	}

	/**
	 * @return Returns the Base element
	 */
	public Base getBaseElement() {
		return current;
	}

	
  protected ExtendedOperationMod generateExtendedOpMod(Element el, Parameter toMod) throws JaxWsMappingException{
         
    String xpath = el.getAttribute("part");
    if(xpath == null || xpath.length() == 0){
      throw new JaxWsMappingException(JaxWsMappingException.ILLEGAL_JAXWS_DECL, "jaxws:parameter extension should have a 'part' attribute!");
    }
    XPathFactory xpathFactory = XPathFactory.newInstance();  
    XPath xpathAPI = xpathFactory.newXPath();     
    NamespaceContextResolver resolver = new NamespaceContextResolver();
    resolver.setDefaultPrefix("wsdl",JAXWSFileParser.WSDL_NAMESPACE);
    resolver.setScope(el);
    xpathAPI.setNamespaceContext(resolver);
    //Element found = (Element) DOM.toNode(xpath, el);    
    Element found = null;
    if (xpath.startsWith("wsdl:definitions")) {
      xpath = "/"+xpath;
    }
    try {        
      //found = DOM.toNode(xpath, wsdlDocument);        
      found = (Element) xpathAPI.evaluate(xpath,el.getOwnerDocument().getDocumentElement(),XPathConstants.NODE);
    } catch (XPathExpressionException e) {
      throw new JaxWsMappingException(JaxWsMappingException.NODE_NOT_FOUND,e, el.getAttribute("part"), el.getParentNode().toString());        
    }
    
    if (found == null) {
      throw new JaxWsMappingException(JaxWsMappingException.NODE_NOT_FOUND, el.getAttribute("part"), el.getNodeName());
    }
    
    ExtendedOperationMod theMod = null;
    int xsdType = toMod.getXSDTypeRef().getXSDType();
    QName xsdNodeName = toMod.getXSDTypeRef().getQName();
    String elementRef = found.getAttribute("element");
    String typeRef = found.getAttribute("type");
    if (xsdType == XSDRef.ELEMENT) {
      if (elementRef.length() == 0) {
        return null;
      } else {
        if (!elementRef.endsWith(xsdNodeName.getLocalPart())) {
          return null;
        }        
      }
    }
    if (xsdType == XSDRef.TYPE) {
      if (typeRef.length() == 0) {
        return null;
      } else {
        if (!typeRef.endsWith(xsdNodeName.getLocalPart())) {
          return null;
        }
      }
    }
    if (toMod.getName().equals(found.getAttribute("name"))) {
      // The Parameter matches the customisation reference
      String childElementName = el.getAttribute("childElementName");
      QName childQName = null;
      if (childElementName.length() > 0) {
        String uri = DOM.qnameToURI(childElementName,el);
        if (uri == null) {
          throw new JaxWsMappingException(JaxWsMappingException.CHILD_ATTR_MISSING_NOT_QNAME, childElementName);
        }
        String localName = DOM.qnameToLocalName(childElementName);
        childQName = new QName(uri,localName);
      }
      /*
      String uri = getUriForPrefix(el);
      String locName = el.getAttribute("childElementName").substring(el.getAttribute("childElementName").indexOf(":") + 1);
      */
      theMod = new ExtendedOperationMod(toMod, el.getAttribute("name"), childQName);
    }

    return theMod;
  }

  /**
   * Get the Namespace URI for a given prefix (null if not found). Used only for the "childElementName" jaxws attribute
   * @param el
   * @param start
   * @return
   * @throws JaxWsMappingException
   */
  /*
  private String getUriForPrefix(Element el) throws JaxWsMappingException {
    String elName = el.getAttribute("childElementName");
    if (elName == null) {
      throw new JaxWsMappingException(JaxWsMappingException.CHILD_ATTR_MISSING_NOT_QNAME, el.toString());
    }
    String pfx = elName.substring(0, elName.indexOf(":"));
    Element prefixDecl = DOM.getPrefixDeclaringElement(el, pfx);

    String uri = null;
    if (prefixDecl != null) {
      uri = prefixDecl.getAttribute("xmlns:" + pfx);
    }
    return uri;
  }*/

  /**
   * @return Returns the javaDoc string if present (null if not set).
   */
  public String getJavaDoc() {
    return javaDoc;
  }

}
