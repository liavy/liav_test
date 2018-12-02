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
import java.util.ListIterator;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.sap.engine.services.webservices.espbase.client.jaxws.cts.ExtBindingCustomization;
import com.sap.engine.services.webservices.espbase.wsdl.HTTPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.ObjectList;
import com.sap.engine.services.webservices.espbase.wsdl.Parameter;
import com.sap.engine.services.webservices.espbase.wsdl.SOAPBindingOperation;
import com.sap.engine.services.webservices.espbase.wsdl.exceptions.WSDLException;
import com.sap.engine.services.webservices.jaxrpc.exceptions.JaxWsMappingException;

/**
 * @author Dimitar Velichkov dimitar.velichkov@sap.com
 * 
 * 
 */
public class ExtendedBindingOperation extends ExtendedElement {

	public static final int		HTTP_OP						= 1;
	public static final int		SOAP_OP						= 2;

	public static final QName	EXCEPTION					= new QName(ExtBindingCustomization.JAXWS_NS, "exception");

	private String						mimeContent				= null;
	private String						soapHeaderMapping	= null;
	private int								type							= -1;
	private ObjectList				params						= null;
  private ArrayList         paramMappings     = null;

	public ExtendedBindingOperation(SOAPBindingOperation soapOp, ExtendedElement parent) throws WSDLException {
		super(soapOp, parent, false);
		type = SOAP_OP;

		params = soapOp.getReferencedOperation().getParameters(Parameter.IN | Parameter.OUT | Parameter.FAULT | Parameter.INOUT | Parameter.RETURN);
    paramMappings = new ArrayList();		

	}

	public ExtendedBindingOperation(HTTPBindingOperation httpOp, ExtendedElement parent) {
		super(httpOp, parent, false);
		type = HTTP_OP;

		// ?? parameters ??
		

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see bindext.ExtendedElement#applyExtensions()
	 */
	public void applyExtensions() throws JaxWsMappingException {

		super.applyExtensions();

		for (ListIterator it = extensions.listIterator(); it.hasNext();) {
			Element elem = (Element) it.next();
			QName ex = new QName(elem.getNamespaceURI(), elem.getLocalName());
      

			if (ex.equals(ExtendedElement.MIME_CONTENT)) {
				if (elem.getFirstChild() == null) {
					throw new JaxWsMappingException(JaxWsMappingException.MISSING_CHILD_ELEM);
				}
				mimeContent = elem.getFirstChild().getNodeValue();
			}

			else if (ex.equals(ExtendedElement.SOAP_HEADER_MAPPING)) {
				if (elem.getFirstChild() == null) {
					throw new JaxWsMappingException(JaxWsMappingException.BINDING_ELEM_MISSING_CHILD);
				}

				soapHeaderMapping = elem.getFirstChild().getNodeValue();
			}

			// nothing to do for HTTP op.....
			else if (ex.equals(ExtendedPorttypeOperation.PARAMETER) && type == SOAP_OP) {

				for (int i = 0; i < params.getLength(); ++i) {
					Parameter toMod = (Parameter) params.item(i);
          ExtendedOperationMod extOpMod = generateExtendedOpMod(elem, toMod);
					if (extOpMod != null) {
            paramMappings.add(extOpMod);            
            //wsdlChildToExtChild.put(new QName(toMod.getName()), extOpMod);
          }
          
				}

			}

        //NOT APPLICABLE SINCE SEPT DRAFT
//			else if (ex.getQName().equals(EXCEPTION) && type == SOAP_OP) {
//
//				Parameter toMod = null;
//				ExtendedOperationMod opMod = null;
//				for (int i = 0; i < params.getLength(); ++i) {
//					toMod = (Parameter) params.item(i);					
//					if (toMod.getName().equals(ex.getContent().getAttribute("sapPart"))) {
//
//						Element clsName = getSingleChild(ex.getContent(), "class", false);
//						opMod = new ExtendedOperationMod(toMod, clsName.getAttribute("name"));
//
//						Element javaDoc = getSingleChild(clsName, "javadoc", true);
//						if (javaDoc != null) {
//							opMod.setJavaDoc(javaDoc.getFirstChild().getNodeValue());
//
//						}
//
//						wsdlChildToExtChild.put(new QName(toMod.getName()), opMod);
//					}
//
//				}
//
//			}

		}

	}
  
  public ExtendedOperationMod getParameterCust(String parameterName, QName childName) {
    for (int i=0; i<paramMappings.size(); i++) {
      ExtendedOperationMod paramMod = (ExtendedOperationMod) paramMappings.get(i);
      String paramModName = paramMod.getParameterName();
      QName childModName = paramMod.getChildName();
      if (parameterName.equals(paramModName)) {
        if (childName == null && childModName == null) {
          return paramMod;
        }
        if (childName != null && childName.equals(childModName)) {
          return paramMod;
        }
      }
    }
    return null;
  }

	/*
	 * (non-Javadoc)
	 * 
	 * @see bindext.ExtendedElement#checkParentExtensions()
	 */
	protected void checkParentExtensions() {

		mimeContent = getPropagatingExtensionValue(ExtendedElement.MIME_CONTENT);
		soapHeaderMapping = getPropagatingExtensionValue(ExtendedElement.SOAP_HEADER_MAPPING);

	}

	/**
	 * @return Returns the mimeContent.
	 */
	public String getMimeContent() {
		return mimeContent;
	}

	/**
	 * @return Returns the soapHeaderMapping.
	 */
	public String getSoapHeaderMapping() {
		return soapHeaderMapping;
	}

}
