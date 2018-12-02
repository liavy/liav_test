/*
 * Created on Aug 21, 2003
 *
 * Copyright (c) 2003 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria AG.
 */
package com.sap.engine.interfaces.transaction;

/**
 * Interface indicating ability for local transaction provision. 
 * 
 * @author Svilen Dikov
 * @version 1.0
 */
public interface LocalTxProvider {

	/**
	 * Actually the returned object should be
	 * <code>javax.resource.spi.LocalTransaction</code> implementation
	 * 
	 * @throws javax.transaction.SystemException
	 * @return Object which is <code>javax.resource.spi.LocalTransaction</code> implementation 
	 */
	Object getLocalTransaction() throws javax.transaction.SystemException;
} 
