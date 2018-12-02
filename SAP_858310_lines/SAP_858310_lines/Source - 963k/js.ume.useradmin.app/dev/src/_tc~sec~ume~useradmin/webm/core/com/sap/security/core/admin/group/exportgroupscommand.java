package com.sap.security.core.admin.group;

import java.io.BufferedWriter;

import com.sap.security.api.*;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.*;
import com.sap.security.core.admin.role.*;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.util.*;
import com.sap.security.core.util.batch.Batch;

/**
 *  This class is called when a user wants to export groups.
 *
 *@author     Markus Liepold
 */
public class ExportGroupsCommand implements Command
{
	public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/ExportGroupsCommand.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);


	/**
	 *  This method expects groupname(s) to be exports. It then exports the group(s).
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
        performer.checkPermission( new UMAdminPermissions(UserAdminHelper.VIEW_GROUPS) );

		String groupIDs[] = util.getUniqueIDs( proxy, "groupname" );
        // get number if group ID's
        int count = (groupIDs==null) ? 0 : groupIDs.length;

		if ( (count == 0) || ((count == 1) && ("".equals( groupIDs[0] ))) ) 
		{
            trace.infoT(methodName, "Could not export a group -> groupID(s) were empty");
			proxy.setRequestAttribute( ErrorBean.beanId, new ErrorBean( new Message( RoleAdminMessagesBean.MISSING_GROUP ) ) );
            return GroupAdminLogic.groupManagementPage;
		}
		else {
            try {
				proxy.setResponseContentType("text/html; charset=utf-8");
                BufferedWriter bw = new BufferedWriter( proxy.getResponseWriter() );
                boolean inPortal = (proxy instanceof com.sap.security.core.admin.ServletAccessToLogic)?false:true;
                Batch batch = new Batch( performer, bw );
                batch.downloadGroups( groupIDs, true );
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
