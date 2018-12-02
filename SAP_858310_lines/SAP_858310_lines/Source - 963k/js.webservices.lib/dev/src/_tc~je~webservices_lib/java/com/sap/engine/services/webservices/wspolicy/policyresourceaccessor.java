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

import com.sap.localization.ResourceAccessor;

/**
 * Add javadoc...
 *  
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-10
 */
public class PolicyResourceAccessor extends ResourceAccessor {
	
	private static final String BUNDLE_NAME = "com.sap.engine.services.webservices.wspolicy.PolicyResourceBundle";

	private static PolicyResourceAccessor resourceAccessor;

	static {
		resourceAccessor = new PolicyResourceAccessor();
	}

	public PolicyResourceAccessor() {
		super(BUNDLE_NAME);
	}

	public static PolicyResourceAccessor getResourceAccessor() {
		return resourceAccessor;
	}

}
