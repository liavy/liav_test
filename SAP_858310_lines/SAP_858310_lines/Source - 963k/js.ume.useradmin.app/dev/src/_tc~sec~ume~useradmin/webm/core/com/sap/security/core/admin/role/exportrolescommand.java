package com.sap.security.core.admin.role;

import java.io.BufferedWriter;

import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.*;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.util.*;
import com.sap.security.core.util.batch.Batch;

/**
 *  This class is called when a user wants to export roles.
 *
 *@author     Markus Liepold
 */
public class ExportRolesCommand implements Command
{
	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/ExportRolesCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  This method expects rolename(s) to be exported. It then exports the role(s).
	 *
	 *@param  proxy                     Description of Parameter
	 *@return                           Description of the Returned Value
	 *@exception  RoleCommandException  Description of Exception
	 */
	public String execute( IAccessToLogic proxy ) throws RoleCommandException
	{
        final String methodName = "execute(proxy)";
        if (trace.beDebug())
    		trace.debugT( methodName, "request parameters: " + HelperClass.reportRequestParameters(proxy) );

		// HelperClass.checkActiveUser throws UMRuntimeException if activeUser is null;
		IUser performer = HelperClass.checkActiveUser( proxy );
        // checkPermission throws java.security.AccessControlException if failed.
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.VIEW_ROLES) );

		String roleIDs[] = util.getUniqueIDs( proxy, "rolename" );
        // get number if role ID's
        int count = (roleIDs==null) ? 0 : roleIDs.length;

		if ( (count == 0) || ((count == 1) && ("".equals( roleIDs[0] ))) ) 
		{
            trace.infoT(methodName, "Could not export a role -> roleID(s) were empty");
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.MISSING_ROLE ) ) );
            return "role_management_main.jsp";
		}
		else {
            try {
				proxy.setResponseContentType("text/html; charset=utf-8");
                BufferedWriter bw = new BufferedWriter( proxy.getResponseWriter() );
                boolean inPortal = (proxy instanceof com.sap.security.core.admin.ServletAccessToLogic)?false:true;
                Batch batch = new Batch( performer, bw );
                batch.downloadRoles( roleIDs, true);
                bw.flush();
                bw.close();
            }
            catch (Exception exc)
            {
                trace.warningT(methodName, exc);
                proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.GENERAL_EXCEPTION ) ) );
                return UserAdminCommonLogic.errorPage;
            }
		}
        return null;
	}
}
