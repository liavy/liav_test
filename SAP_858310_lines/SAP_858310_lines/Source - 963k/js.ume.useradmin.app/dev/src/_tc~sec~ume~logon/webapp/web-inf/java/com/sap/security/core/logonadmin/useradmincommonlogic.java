package com.sap.security.core.logonadmin;

import com.sap.security.core.util.ResourceBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.*;
import com.sap.security.core.logon.imp.SecurityPolicy;

public abstract class UserAdminCommonLogic {
  public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~logon/webapp/WEB-INF/java/com/sap/security/core/logonadmin/UserAdminCommonLogic.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
  public final static String setLanguageAction = "locale";
  public final static String [] LOGON_FAILURE_CODES = {
      SecurityPolicy.USER_AUTH_FAILED /* [0] */,
      SecurityPolicy.PASSWORD_EXPIRED /* [1] */,
      SecurityPolicy.USER_IS_CURRENTLY_NOT_VALID       /* [2] */,
      SecurityPolicy.USER_AUTH_FAILED /* [3] SecurityPolicy.ACCOUNT_LOCKED_ADMIN */,
      SecurityPolicy.CERT_AUTH_FAILED /* [4] */,
      SecurityPolicy.CERT_AUTH_FAILED /* [5] */,
      null /* Meaning: no message [6] */,
      null /* Meaning: no message [7] */,
      null /* Meaning: no message [8] */,
      SecurityPolicy.USER_AUTH_FAILED /* [9] SecurityPolicy.SAPSTAR_ACTIVATED */
  };

  private static IUMTrace trace = null;
  private static boolean servlet23;

  static {
    Class _class = javax.servlet.http.HttpServletRequest.class;
    Class[] args = {String.class};
    trace = InternalUMFactory.getTrace(VERSIONSTRING);

    try {
      _class.getMethod("setCharacterEncoding", args);
      servlet23 = true;
    } catch (NoSuchMethodException nsme) {
      servlet23 = false;
      trace.warningT("doPost", "Servlet 2.3 not available, character encoding could not be set!");
    }
  } // static

  public abstract void executeRequest() throws Exception;
  public abstract ResourceBean getLocaleBean();

  public static void setUnicodeEnabled(IAccessToLogic proxy) {
    if (servlet23) {
      try {
        proxy.setResponseContentType("text/html; charset=utf-8");
        proxy.setRequestCharacterEncoding("UTF8");
      } catch (java.io.UnsupportedEncodingException uee) {
        trace.errorT("setUnicodeEnabled", uee);
      }
    }
  } // setUnicodeEnabled

}