package com.sap.security.core.admin.role;

import java.io.*;
import java.security.AccessControlException;
import javax.servlet.*;
import javax.servlet.http.*;

import com.sap.security.api.*;
import com.sap.security.core.*;
import com.sap.security.core.admin.*;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.util.*;
import com.sap.security.core.role.imp.xml.*;

/**
 *  This servlet handles the refresh of role XML files.
 */
public class UpdateXMLFiles extends HttpServlet
{
    /**
     *  Description of the Field
     */
    public String statusPage = "statusPage.jsp";

    private String dir = "/role/";

    public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/role/UpdateXMLFiles.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private final static IUMTrace trace = InternalUMFactory.getTrace( VERSIONSTRING );

    /**
     *  Gets the ServletInfo attribute
     *
     *@return    The ServletInfo value
     */
    public String getServletInfo()
    {
        return "Update actions and permissions from XML files";
    }


    /**
     *  Description of the Method
     *
     *@param  req                   Description of Parameter
     *@param  res                   Description of Parameter
     *@exception  ServletException  Description of Exception
     *@exception  IOException       Description of Exception
     */
    public void service( HttpServletRequest req, HttpServletResponse res )
        throws ServletException, IOException
    {
        String methodName = "service(req,res)";
        trace.entering( methodName );

        IAccessToLogic proxy = new ServletAccessToLogic(req, res);
        try {
            proxy.setResponseContentType("text/html; charset=utf-8");
            proxy.setRequestCharacterEncoding("UTF8");
            //UserAdminLogic.setUnicodeEnabled(proxy);
            IUser performer = proxy.getActiveUser();
            if (performer == null) 
            {
                trace.exiting( methodName, "No user was not logged in");
                return;
            }
            performer.checkPermission( new UMAdminPermissions(UserAdminHelper.MANAGE_ALL_COMPANIES));

            boolean status = XMLServiceRepository.loadXMLFiles();

            String message = null;
            if ( status ) 
            {
                message = "Refresh of XML files was successfully";
                trace.infoT(methodName, message);
                proxy.setRequestAttribute("refresh_msg", message);
            }
            else {
                message = "Refresh of XML files failed!";
                trace.infoT(methodName, message);
                proxy.setRequestAttribute("refresh_msg", message);
            }
        }
        catch ( AccessControlException e ) 
        {
          trace.errorT(methodName, "Authorization Failed for logged in user!" );
          proxy.setRequestAttribute("refresh_msg", "Failed.  You are not authorized to refresh the role XML files!");
        }

        trace.infoT(methodName, "Forwarding to jsp file:" + dir + statusPage );
        RequestDispatcher rd = getServletContext().getRequestDispatcher( dir + statusPage );
        rd.forward( req, res );
    }
    
}
