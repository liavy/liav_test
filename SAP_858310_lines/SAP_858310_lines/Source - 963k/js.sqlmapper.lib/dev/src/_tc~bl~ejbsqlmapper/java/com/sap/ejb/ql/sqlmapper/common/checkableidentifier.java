package com.sap.ejb.ql.sqlmapper.common;

/**
 * Realise an identifier which can be checked whether it is a native one or not.
 * </p><p>
 * Copyright (c) 2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public interface CheckableIdentifier {

	/**
	 * Gets the name of the identifier.
	 * @return
	 * the identifier's name.
	 */
	public String getName(boolean nativeMode);
	
	/**
	 * Indicates whether a <code>CheckableIdentifer</code> instance is 
	 * native or not. 
	 * @return
	 * true if the identifier is not an Open SQL identifier; false elsewise.
	 */
	public boolean isNative();
}
	
