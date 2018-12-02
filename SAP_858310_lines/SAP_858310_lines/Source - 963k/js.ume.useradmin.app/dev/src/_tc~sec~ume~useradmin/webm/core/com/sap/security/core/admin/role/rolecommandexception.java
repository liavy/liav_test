package com.sap.security.core.admin.role;

/**
 *  Description of the Class
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class RoleCommandException extends Exception
{

	/**
	 *  Constructor for the RoleCommandException object
	 */
	public RoleCommandException() {
		super();
	}


	/**
	 *  Constructor for the RoleCommandException object
	 *
	 *@param  msg  Description of Parameter
	 */
	public RoleCommandException(String msg) {
		super(msg);
	}
}