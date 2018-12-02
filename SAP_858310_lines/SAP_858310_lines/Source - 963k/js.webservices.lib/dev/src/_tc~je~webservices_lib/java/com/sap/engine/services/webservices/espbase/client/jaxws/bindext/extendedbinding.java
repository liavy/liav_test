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

import com.sap.engine.services.webservices.espbase.wsdl.Binding;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBinding;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.JaxWsMappingException;

/**
 * @author Dimitar Velichkov dimitar.velichkov@sap.com
 * 
 * 
 */
public class ExtendedBinding extends ExtendedElement {

	private String	mimeContent				= null;
	private String	soapHeaderContent	= null;
	
	private ExtendedBindingOperation[] bindingOps = null;
	
	public ExtendedBinding(Binding base, ExtendedElement parent, boolean recursive) throws JaxWsMappingException{
		super(base, parent, recursive);
	
		if (recursive) {

      if (base.getClass().equals(SOAPBinding.class)) {
        ObjectList allOps = ((SOAPBinding) base).getOperations();
        bindingOps = new ExtendedBindingOperation[allOps.getLength()];
        for (int i = 0; i < bindingOps.length; ++i) {

          try {
            bindingOps[i] = new ExtendedBindingOperation((SOAPBindingOperation) allOps.item(i), this);
          } catch (WSDLException e) {
            throw new JaxWsMappingException(JaxWsMappingException.EXTWSDL_BINDING_ERROR, e);
          }
          wsdlChildToExtChild.put(new QName(((SOAPBindingOperation) allOps.item(i)).getName()), bindingOps[i]);

        }

      }

      else {
        bindingOps = new ExtendedBindingOperation[0];
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

		for (ListIterator it = extensions.listIterator(); it.hasNext();) {
			Element el = (Element) it.next();
			QName ex = new QName(el.getNamespaceURI(), el.getLocalName());
      
			if (el.getFirstChild() == null) {
				throw new JaxWsMappingException(JaxWsMappingException.BINDING_ELEM_MISSING_CHILD);
			}

			if (ex.equals(ExtendedElement.MIME_CONTENT)) {

				mimeContent = el.getFirstChild().getNodeValue();
			}

      //TODO: missing?
			else if (ex.equals(ExtendedElement.SOAP_HEADER_MAPPING)) {
				soapHeaderContent = el.getFirstChild().getNodeValue();
			}

		}
		
		if(recursive){
			for(int i = 0; i < bindingOps.length; ++i){
				bindingOps[i].applyExtensions();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bindext.ExtendedElement#checkParentExtensions()
	 */
	protected void checkParentExtensions() {

		mimeContent = getPropagatingExtensionValue(ExtendedElement.MIME_CONTENT);
		soapHeaderContent = getPropagatingExtensionValue(ExtendedElement.SOAP_HEADER_MAPPING);

	}

	/**
	 * @return Returns the mimeContent.
	 */
	public String getMimeContent() {
		return mimeContent;
	}

	/**
	 * @return Returns the soapHeaderContent.
	 */
	public String getSoapHeaderContent() {
		return soapHeaderContent;
	}
	
}
