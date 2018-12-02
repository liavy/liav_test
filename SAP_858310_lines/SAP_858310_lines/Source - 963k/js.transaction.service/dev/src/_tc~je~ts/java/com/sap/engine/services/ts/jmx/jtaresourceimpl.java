/**
*
* Copyright (c) 2002 by SAP Labs Bulgaria AG.,
* url: http://www.saplabs.bg
* All rights reserved.
*
* This software is the confidential and proprietary information
* of SAP AG. You shall not disclose such Confidential
* Information and shall use it only in accordance with the terms
* of the license agreement you entered into with SAP Labs Bulgaria AG.
*
* Created on 2004-11-30 
* Created by ralitsa-v (e-mail: ralitsa.vassileva@sap.com)
*/
package com.sap.engine.services.ts.jmx;

import com.sap.engine.admin.model.jsr77.J2EEManagedObjectAdapterTreeNode;
import com.sap.engine.admin.model.jsr77.JTAResource;


/**
* @author Ralitsa Vassileva
* @version 7.0
*/
public class JTAResourceImpl extends J2EEManagedObjectAdapterTreeNode implements JTAResource {

 public static final String TRANSACTION_MANAGER = "TransactionManager";
 
 public JTAResourceImpl(String internalName, String caption) {
   super(internalName, caption);
 }
}

