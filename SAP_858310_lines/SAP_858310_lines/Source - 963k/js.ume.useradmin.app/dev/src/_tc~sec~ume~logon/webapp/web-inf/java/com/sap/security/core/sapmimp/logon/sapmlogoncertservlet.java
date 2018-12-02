package com.sap.security.core.sapmimp.logon;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.logonadmin.IAccessToLogic;
import com.sap.security.core.logonadmin.ServletAccessToLogic;
import com.sap.security.core.util.IUMTrace;
/**
 *  Title: UM3 Description: Copyright: Copyright (c) 2001 Company: SAPMarkets,
 *  Inc
 *
 * @author     William Li
 * @created    July 12, 2001
 * @version    1.0
 */

public class SAPMLogonCertServlet extends HttpServlet {

  public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~logon/webapp/WEB-INF/java/com/sap/security/core/sapmimp/logon/SAPMLogonCertServlet.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
  private static IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING); 

  public static boolean isServlet23() {
    try {
      HttpServletRequest.class.getMethod("setCharacterEncoding", new Class[]{String.class});
      return true;
    } catch (NoSuchMethodException nsme) {
      trace.warningT("doPost", "Servlet 2.3 not available, character encoding would not be set!");
      return false;
    }
  }

  /* (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (isServlet23()) {
      resp.setContentType("text/html; charset=utf-8");
      req.setCharacterEncoding("UTF8");
    }
    
    try {
      IAccessToLogic accessor = 
          new ServletAccessToLogic(req, resp, IAccessToLogic.ENV_LOGONCERTSERVLET);
      SAPMLogonCertLogic logic = new SAPMLogonCertLogic(accessor);
      logic.executeRequest();
    } catch (Exception e) {
      trace.errorT("doPost", "Fatal Logon error", e);
    }
  }

  /* (non-Javadoc)
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    doPost(request, response);
  }
}
