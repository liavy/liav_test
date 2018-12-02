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
package com.sap.engine.services.webservices.wspolicy;
 
import com.sap.exception.BaseException;
import com.sap.tc.logging.Location;

/**
 * Add javadoc...
 *  
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-10
 */
public class PolicyException extends BaseException {
  
  public static final String NOT_NORMALIZE_EXPRESSION  =  "webservices_2100"; 
	public static final String NO_POLICY_DESCRIPTION  =  "webservices_2101"; 
	public static final String POLICYREFERENCE_NOT_SUPPORTED  =  "webservices_2102"; 
	public static final String UNKNOWN_POLICY_ELEMENT  =  "webservices_2103"; 
  public static final String POLICY_NOT_FOUND  =  "webservices_2104"; 
  public static final String MISSING_POLICY_REFERENCES  =  "webservices_2105"; 
  public static final String POLICYREFERENCE_EXPECTED=  "webservices_2106"; 

  private static final Location LOC = Location.getLocation(PolicyException.class);
  
	public PolicyException(Throwable arg0) {
		super(LOC, arg0);
	}

	public PolicyException(String arg1) {
		super(LOC, PolicyResourceAccessor.getResourceAccessor(), arg1);
	}

	public PolicyException(String arg1, Throwable arg2) {
		super(LOC, PolicyResourceAccessor.getResourceAccessor(), arg1, arg2);
	}


	public PolicyException(String arg1, Object[] arg2) {
		super(LOC, PolicyResourceAccessor.getResourceAccessor(), arg1, arg2);
	}

	public PolicyException(String arg1, Object[] arg2,	Throwable arg3) {
		super(LOC, PolicyResourceAccessor.getResourceAccessor(), arg1, arg2, arg3);
	}

}
