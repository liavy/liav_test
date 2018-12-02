package com.sap.security.core.admin.role;

import com.sap.security.core.util.*;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.*;

/**
 *  Description of the Class
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 */
public class AbortCommand implements Command
{
	private String next;

	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/AbortCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

	/**
	 *  Constructor for the AbortCommand object
	 *
	 *@param  next  Description of Parameter
	 */
	public AbortCommand(String next)
	{
		this.next = next;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  req                       Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute(IAccessToLogic proxy) throws RoleCommandException
	{
        trace.infoT( "execute(proxy)", "request parameters: " +HelperClass.reportRequestParameters(proxy) );
		proxy.setRequestAttribute(InfoBean.beanId, new InfoBean(new Message(RoleAdminMessagesBean.ABORT)));
		return next;
	}
}