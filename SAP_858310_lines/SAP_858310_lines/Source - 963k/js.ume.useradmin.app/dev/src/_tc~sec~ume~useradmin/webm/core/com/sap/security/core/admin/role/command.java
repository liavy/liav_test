package com.sap.security.core.admin.role;

import com.sap.security.core.admin.IAccessToLogic;

/**
 *  Description of the Interface
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public interface Command
{
	/**
	 *  Description of the Method
	 *
	 *@param  proxy          Description of Parameter
	 *@return                Description of the Returned Value
	 *@exception  Exception  Description of Exception
	 */
	public String execute( IAccessToLogic proxy ) throws Exception;

}
