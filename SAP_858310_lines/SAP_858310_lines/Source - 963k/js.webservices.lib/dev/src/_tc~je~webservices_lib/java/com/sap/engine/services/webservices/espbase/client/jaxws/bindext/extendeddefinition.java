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

import java.util.HashMap;
import java.util.ListIterator;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.sap.engine.services.webservices.espbase.client.jaxws.cts.ExtBindingCustomization;
import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.espbase.wsdl.Interface;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Service;
import com.sap.engine.services.webservices.jaxrpc.exceptions.JaxWsMappingException;

/**
 * @author Dimitar Velichkov dimitar.velichkov@sap.com
 * 
 * 
 */
public class ExtendedDefinition extends ExtendedElement {

	private String							extPkgName		= null;
	private Boolean							asyncMapping	= null;
	private String							mimeContent		= null;
	private String							soapHeaders		= null;
	private Boolean							wrapperStyle	= null;

	private ExtendedService[]		extServices		= null;
	private ExtendedPortType[]	extPortTypes	= null;
	private ExtendedBinding[]		extBindings		= null;

	public static final QName		PACKAGE_EXT								= new QName(ExtBindingCustomization.JAXWS_NS, "package");

	private HashMap							wsdlBindingToExtBinding		= new HashMap();
	private HashMap							wsdlPortTypeToExtPortType	= new HashMap();
	private HashMap							wsdlServiceToExtService		= new HashMap();
	
	/**
	 * Create an extended "Definitions" element
	 * 
	 * @param current
	 *          the definition element to use
	 * @param recursive
	 *          if set to true, apply extensions to all children elements
	 * @throws JaxWsMappingException 
	 * @throws BindingFormatException 
	 */
	public ExtendedDefinition(Definitions current, boolean recursive) throws JaxWsMappingException {
		super(current, null, recursive);

		if (recursive) {

			ObjectList allBindings = current.getBindings();
			extBindings = new ExtendedBinding[allBindings.getLength()];
			for (int i = 0; i < extBindings.length; ++i) {
				extBindings[i] = new ExtendedBinding((Binding) allBindings.item(i), this, true);
				wsdlBindingToExtBinding.put(((Binding)allBindings.item(i)).getName(), extBindings[i]);
			}

			ObjectList allPorttypes = current.getInterfaces();
			extPortTypes = new ExtendedPortType[allPorttypes.getLength()];
			for (int i = 0; i < extPortTypes.length; ++i) {
				extPortTypes[i] = new ExtendedPortType((Interface) allPorttypes.item(i), this, true);
				wsdlPortTypeToExtPortType.put(((Interface)allPorttypes.item(i)).getName(), extPortTypes[i]);
			}

			ObjectList allServices = current.getServices();
			extServices = new ExtendedService[allServices.getLength()];
			for (int i = 0; i < extServices.length; ++i) {
				extServices[i] = new ExtendedService((Service) allServices.item(i), this, true);
				wsdlServiceToExtService.put(((Service)allServices.item(i)).getName(), extServices[i]);
			}

		}

	}

	
	public ExtendedService getExtendedService(QName baseService){
		return (ExtendedService)wsdlServiceToExtService.get(baseService);
	}
	
	public ExtendedPortType getExtendedPortType(QName basePortType){
		return (ExtendedPortType)wsdlPortTypeToExtPortType.get(basePortType);
	}
	
	public ExtendedBinding getExtendedBinding(QName baseBinding){
		return (ExtendedBinding)wsdlBindingToExtBinding.get(baseBinding);
	}
	
	public ExtendedElement getExtendedChild(QName base){
		return null;
	}
	
	
	private void applyChildren(ExtendedElement[] objs) throws JaxWsMappingException {

		for (int i = 0; i < objs.length; ++i) {
			objs[i].applyExtensions();
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

			String value = null;
			if (el.getFirstChild() != null) {
				value = el.getFirstChild().getNodeValue();
			}

			if (ex.equals(PACKAGE_EXT)) {

				String pkgName = el.getAttribute("name");
				if (pkgName == null) {
					throw new JaxWsMappingException(JaxWsMappingException.PACKAGE_EXT_MISSING_NAME);
				}
				extPkgName = pkgName;
        javaDoc = getJavaDocText(el);

			}

			// these are also propagating extensions
			else if (ex.equals(ExtendedElement.ASYNC_MAPPING)) {
        String asyncString = value;
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

			else if (ex.equals(ExtendedElement.MIME_CONTENT)) {
				mimeContent = value;
				propagatingExtensions.put(ExtendedElement.MIME_CONTENT, value);
			}

			else if (ex.equals(ExtendedElement.SOAP_HEADER_MAPPING)) {
				soapHeaders = value;
				propagatingExtensions.put(ExtendedElement.SOAP_HEADER_MAPPING, value);
			}

			else if (ex.equals(ExtendedElement.WRAPPER_STYLE)) {
        String wrapperString = value;
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
				propagatingExtensions.put(ExtendedElement.WRAPPER_STYLE, wrapperString);
			}

			// not allowed here!
			else {
				throw new JaxWsMappingException(JaxWsMappingException.UNEXPECTED_EXT_ELEMENT, ex.toString());
			}

		}

		// go through all children and apply extensions

		if (recursive) {
			applyChildren(extServices);
			applyChildren(extPortTypes);
			applyChildren(extBindings);

		}

	}

	/**
	 * @return Returns the asyncMapping.
	 */
	public Boolean getAsyncMapping() {
		return asyncMapping;
	}

	/**
	 * @return Returns the extPkgName.
	 */
	public String getExtPkgName() {
		return extPkgName;
	}

	/**
	 * @return Returns the mimeContent.
	 */
	public String getMimeContent() {
		return mimeContent;
	}

	/**
	 * @return Returns the soapHeaders.
	 */
	public String getSoapHeaders() {
		return soapHeaders;
	}

	/**
	 * @return Returns the wrapperStyle.
	 */
	public Boolean getWrapperStyle() {
		return wrapperStyle;
	}

	/**
	 * This method here does nothing - Definition has no parent elements
	 */
	protected void checkParentExtensions() {
	}
}
