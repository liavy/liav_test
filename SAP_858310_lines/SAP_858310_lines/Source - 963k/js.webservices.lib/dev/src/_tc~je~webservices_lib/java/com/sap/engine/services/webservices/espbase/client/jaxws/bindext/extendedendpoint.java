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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.sap.engine.services.webservices.espbase.client.jaxws.cts.ExtBindingCustomization;
import com.sap.engine.services.webservices.espbase.wsdl.Endpoint;
import com.sap.engine.services.webservices.jaxrpc.exceptions.JaxWsMappingException;

/**
 * @author Dimitar Velichkov  dimitar.velichkov@sap.com
 * 
 *
 */
public class ExtendedEndpoint extends ExtendedElement {

	private String provider = null;
	
	private String methodName = null;
	
	public static final QName PORT_METHOD_NAME = new QName(ExtBindingCustomization.JAXWS_NS, "method");
	public static final QName PORT_PROVIDER= new QName(ExtBindingCustomization.JAXWS_NS, "provider");
	
	public ExtendedEndpoint(Endpoint endpoint, ExtendedService parent){
		super(endpoint, parent, false);
	
		
	}
	
	public void applyExtensions() throws JaxWsMappingException {
		super.applyExtensions();
		
		for(int i = 0; i < extensions.size(); ++i){
			
			Element el = (Element)extensions.get(i);		
			QName ex = new QName(el.getNamespaceURI(), el.getLocalName());
      
			if(ex.equals(PORT_METHOD_NAME)){
				
				methodName = el.getAttribute("name");
				if(methodName == null || methodName.length() == 0){	
					throw new JaxWsMappingException(JaxWsMappingException.MISSING_NAME_ATTR, ex.toString());
				}
				
				javaDoc = getJavaDocText(el);
				
			}
			
			else if(ex.equals(PORT_PROVIDER)){
				provider = el.getFirstChild().getNodeValue();
			}
			
			
		}
		
		
		
		
	}
	
	
	/* (non-Javadoc)
	 * @see bindext.ExtendedElement#checkParentExtensions()
	 */
	protected void checkParentExtensions() {
		// do nothing 

	}

	/**
	 * @return Returns the javaDoc.
	 */
	public String getJavaDoc() {
		return javaDoc;
	}

	/**
	 * @return Returns the methodName.
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return Returns the provider.
	 */
	public String getProvider() {
		return provider;
	}

}
