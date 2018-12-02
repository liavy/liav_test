package com.sap.security.core.admin.batch;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.security.api.UMFactory;
import com.sap.security.api.util.IUMParameters;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;

import com.sap.security.core.admin.ServletAccessToLogic;
import com.sap.security.core.admin.IAccessToLogic;

public class BatchServlet extends HttpServlet {
    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/batch/BatchServlet.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private static  IUMTrace trace = null;

    static {
        trace = InternalUMFactory.getTrace(VERSIONSTRING);
    } // static

    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doPost(req, resp);
    } // doGet

    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            IAccessToLogic accessor = new ServletAccessToLogic(req, resp);
            BatchLogic logic = new BatchLogic(accessor);
            logic.executeRequest();
        } catch (Exception e) {
            trace.errorT("doPost", e);
        }
    } // doPost
}