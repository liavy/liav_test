package com.sap.security.core.admin.role;

import com.sap.security.core.admin.IAccessToLogic;

/**
 *  Description of the Class
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class NullCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/NullCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";

	/**
	 *  Constructor for the NullCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public NullCommand(String next) {
		this.next = next;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  proxy                     Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute(IAccessToLogic proxy ) throws RoleCommandException 
    {
		return next;
	}
}