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
package com.sap.engine.services.webservices.espbase.wsdl.exceptions;

/**
 * Javadoc goes here...
 * 
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-11-21
 */
public class WSDLUnmarshalException extends WSDLException {
  public static final String MISSING_RQUIRED_ATTRIBUTE  =  "webservices_2000";
  public static final String TYPE_AND_ELEMENT_ATTRIBUTES_AVAILABLE  =  "webservices_2001";
  public static final String MISSING_MESSAGE  =  "webservices_2002";
  public static final String MORE_THAN_ONE_CHILD_ELEMENTS_FOUND  =  "webservices_2003";
  public static final String NOTESUPPORTED_BIDING_ELEMENT  =  "webservices_2004";
  public static final String INVALID_ATTRIB_VALUE  =  "webservices_2005";
  public static final String MISSING_WSDL_ENTITY  =  "webservices_2006";
  public static final String INCORRECT_WSDL_FAULT_MESSAGE  =  "webservices_2007";
  public static final String SINGLE_WSDL_ELEMENT  =  "webservices_2008";
  public static final String INVALID_DOCUMENT_ENCODED_OPERATION  =  "webservices_2009";
  public static final String PART_NOT_FOUND  =  "webservices_2010";
  public static final String INVALID_SOAP_HEADER_REFERENCE  =  "webservices_2011";
  public static final String PARAMETERORDER_INCONSISTENCY  =  "webservices_2014";
  public static final String PARAMETERORDER_MISSING_PART  =  "webservices_2015";
  public static final String INVALID_DEFINITIONS_ELEMENT  =  "webservices_2016";
  public static final String MORE_THAN_ONE_MESSAGES  =  "webservices_2017";
  public static final String UNRECORGNIZED_ELEMENT_CONTENT  =  "webservices_2023";
  public static final String ELEMENT_CANNOT_BE_ATTACHED  =  "webservices_2024";
  public static final String ELEMENT_MUST_APPEAR_EXACTLY_ONCE  =  "webservices_2025";
  public static final String ALL_CONTENT_ELEMENTS_MUST_HAVE_EQUAL_PART_VALUES  =  "webservices_2026";
  public static final String UNKNOW_OPERATION_MEP  =  "webservices_2027";
  public static final String INVALID_NCNAME_ATTRIBUTE_VALUE =  "webservices_2031";
   
	public WSDLUnmarshalException(Throwable arg0) {
		super(arg0);
	}

	public WSDLUnmarshalException(String pattern) {
		super(pattern);
  }

	public WSDLUnmarshalException(String pattern, Throwable cause) {
    super(pattern, cause);
	}

	public WSDLUnmarshalException(String pattern, Object[] params) {
		super(pattern, params);
	}

	public WSDLUnmarshalException(String pattern, Object[] params, Throwable cause) {
		super(pattern, params, cause);
	}
}
