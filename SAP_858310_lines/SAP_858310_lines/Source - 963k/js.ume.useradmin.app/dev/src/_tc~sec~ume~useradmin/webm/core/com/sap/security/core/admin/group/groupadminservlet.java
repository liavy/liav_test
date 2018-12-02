/*
 *  Copyright 2001
 *
 *  SAPMarkets, Inc.
 *  All rights reserved
 *  Palo Alto, California, 94304, U.S.A.
 *
 */
package com.sap.security.core.admin.group;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;

import com.sap.security.core.admin.ServletAccessToLogic;
import com.sap.security.core.admin.IAccessToLogic;

/**
 *  This servlet is starting point for the Administraton
 *
 *@author     Fereidoon Samii
 *@created    May 16, 2001
 *@version    1.0 01.17.2001
 */

public class GroupAdminServlet extends HttpServlet
{
    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/group/GroupAdminServlet.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
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
            GroupAdminLogic logic = new GroupAdminLogic(accessor);
            logic.doGet();
        } catch (Exception e) {
            trace.errorT("doPost", e);
        }
    } // doPost
}