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
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Service;
import com.sap.engine.services.webservices.jaxrpc.exceptions.JaxWsMappingException;

/**
 * @author Dimitar Velichkov  dimitar.velichkov@sap.com
 * 
 *
 */
public class ExtendedService extends ExtendedElement {

	public static final QName SRV_CLASS = new QName(ExtBindingCustomization.JAXWS_NS, "class");
	
	private String newName = null;
	
	private ExtendedEndpoint[] extEndPoints = null;
	
	public ExtendedService(Service current, ExtendedElement parent, boolean recursive){
		super(current, parent, recursive);
		
		if(recursive){
			
			ObjectList allEPs = current.getEndpoints();
			
			extEndPoints = new ExtendedEndpoint[allEPs.getLength()];
			for(int i = 0; i < extEndPoints.length; ++i){
				extEndPoints[i] = new ExtendedEndpoint((Endpoint)allEPs.item(i), this);
				wsdlChildToExtChild.put(new QName(((Endpoint)allEPs.item(i)).getName()), extEndPoints[i]);
			}
			
		}
		
	}
	
	public void applyExtensions() throws JaxWsMappingException {
		super.applyExtensions();
		
		for (int i = 0; i < extensions.size(); ++i) {

      Element el = (Element) extensions.get(i);
      QName ex = new QName(el.getNamespaceURI(), el.getLocalName());
      

			if (ex.equals(SRV_CLASS)) {
				newName = el.getAttribute("name");
				if (newName == null || newName.length() == 0) {
					throw new JaxWsMappingException(JaxWsMappingException.MISSING_NAME_ATTR,  el.toString());
				}

        javaDoc = getJavaDocText(el);

			}

		}
		
		if(recursive){
			for(int i = 0; i < extEndPoints.length; ++i){
				extEndPoints[i].applyExtensions();
			}
		}
		
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see bindext.ExtendedElement#checkParentExtensions()
	 */
	protected void checkParentExtensions() {
		// No parent extensions to check for!

	}

	/**
	 * @return Returns the javaDoc.
	 */
	public String getJavaDoc() {
		return javaDoc;
	}

	/**
	 * @return Returns the newName.
	 */
	public String getNewName() {
		return newName;
	}

}
