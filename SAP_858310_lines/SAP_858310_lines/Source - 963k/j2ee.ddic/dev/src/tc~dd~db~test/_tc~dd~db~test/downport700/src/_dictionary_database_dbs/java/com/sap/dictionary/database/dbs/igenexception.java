package com.sap.dictionary.database.dbs;

/**
 * @author d019347
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public interface IGenException {
	
	public GenExceptionInfo getInfo();
	
	public String getStackTraceString();
	
	public ExType getExType();

}
