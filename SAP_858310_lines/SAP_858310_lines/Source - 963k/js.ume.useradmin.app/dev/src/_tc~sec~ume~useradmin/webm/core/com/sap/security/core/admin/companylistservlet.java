package com.sap.security.core.admin;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.security.api.UMFactory;
import com.sap.security.api.util.IUMParameters;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;

public class CompanyListServlet extends HttpServlet {
   public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/CompanyListServlet.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
   private static  IUMTrace trace = null;

    static {
        trace = InternalUMFactory.getTrace(VERSIONSTRING);
    } // static

    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doPost(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            IAccessToLogic accessor = new ServletAccessToLogic (req, resp);
            CompanyListLogic logic = new CompanyListLogic(accessor);
            logic.executeRequest();
        } catch (Exception e) {
            trace.errorT("doPost", e);
        }
    }
}