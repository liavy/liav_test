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

import java.util.ListIterator;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.sap.engine.services.webservices.espbase.client.jaxws.cts.ExtBindingCustomization;
import com.sap.engine.services.webservices.espbase.wsdl.Interface;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Operation;
import com.sap.engine.services.webservices.jaxrpc.exceptions.JaxWsMappingException;

/**
 * @author Dimitar Velichkov dimitar.velichkov@sap.com
 * 
 * PortType modified by JAX-WS binding declarations
 * 
 */
public class ExtendedPortType extends ExtendedElement {

	private String						className			= null;
	private Boolean						asyncMapping	= null;
	private Boolean						wrapperStyle	= null;

	private ExtendedPorttypeOperation[] extOps = null; 
	
	public static final QName	SEI_NAME			= new QName(ExtBindingCustomization.JAXWS_NS, "class");

	
	
	/**
	 * 
	 * @param current
	 *          WSDL Interface with extension elements
	 * @param parent
	 *          parent extended definition
	 */
	public ExtendedPortType(Interface current, ExtendedDefinition parent, boolean recursive) {
		super(current, parent, recursive);
		
		if(recursive){
			
			ObjectList allOps = current.getOperations();
			extOps = new ExtendedPorttypeOperation[allOps.getLength()];
			
			for(int i = 0; i < extOps.length; ++i){
				extOps[i] = new ExtendedPorttypeOperation((Operation)allOps.item(i), this);
				wsdlChildToExtChild.put(new QName(((Operation)allOps.item(i)).getName()), extOps[i]);
			}
			
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bindext.ExtendedElement#applyExtensions()
	 */
	public void applyExtensions() throws JaxWsMappingException {

		super.applyExtensions();
		// go through each allowed extension for a definition element
		for (ListIterator it = extensions.listIterator(); it.hasNext();) {
			Element el = (Element) it.next();
			QName ex = new QName(el.getNamespaceURI(), el.getLocalName());
      
			if (ex.equals(SEI_NAME)) {
        
				String newSEIName = el.getAttribute("name");
				className = newSEIName;
        javaDoc = getJavaDocText(el);
        
             
			}
			
			else if(ex.equals(ExtendedElement.WRAPPER_STYLE)){
        String wrapperString = el.getFirstChild().getNodeValue();
        if (wrapperString == null) {
          wrapperString = "true";
        } else {
          wrapperString = wrapperString.trim();
        }
        if ("true".equalsIgnoreCase(wrapperString)) {
          wrapperStyle = Boolean.TRUE;
        } else {
          wrapperStyle = Boolean.FALSE;
        }      
        propagatingExtensions.put(ExtendedElement.WRAPPER_STYLE,wrapperString);
			}
			
			else if(ex.equals(ExtendedElement.ASYNC_MAPPING)){
        String asyncString = el.getFirstChild().getNodeValue();
        if (asyncString == null) {
          asyncString = "false";
        } else {
          asyncString = asyncString.trim();
        }
        if ("true".equalsIgnoreCase(asyncString)) {
          asyncMapping = Boolean.TRUE;
        } else {
          asyncMapping = Boolean.FALSE;
        }	
        propagatingExtensions.put(ExtendedElement.ASYNC_MAPPING, asyncString);
			}

		}

		if(recursive){
			for(int i = 0; i < extOps.length; ++i){
				extOps[i].applyExtensions();				
			}
		}
		
	}

	protected void checkParentExtensions() {
    String wrapperString = getPropagatingExtensionValue(ExtendedElement.WRAPPER_STYLE);
    if (wrapperString != null) {
      if ("true".equalsIgnoreCase(wrapperString)) {
        wrapperStyle = Boolean.TRUE;
      } else {
        wrapperStyle = Boolean.FALSE;
      }      
    }
    String asyncString = getPropagatingExtensionValue(ExtendedElement.ASYNC_MAPPING);
    if (asyncString != null) {
      if ("true".equalsIgnoreCase(asyncString)) {
        asyncMapping = Boolean.TRUE;
      } else {
        asyncMapping = Boolean.FALSE;
      }      
    }    		 

	}

	/**
	 * @return Returns the asyncMapping.
	 */
	public Boolean getAsyncMapping() {
    if (asyncMapping == null) {
      return Boolean.FALSE;
    }
		return asyncMapping;
	}

	/**
	 * @return Returns the className.
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return Returns the wrapperStyle.
	 */
	public Boolean getWrapperStyle() {
    if (wrapperStyle == null) {
      return Boolean.TRUE;
    }
		return wrapperStyle;
	}

}
