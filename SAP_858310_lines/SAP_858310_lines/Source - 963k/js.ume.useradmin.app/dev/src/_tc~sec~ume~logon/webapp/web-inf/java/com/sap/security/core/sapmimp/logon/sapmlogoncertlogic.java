package com.sap.security.core.sapmimp.logon;

import com.sap.security.core.logonadmin.UserAdminCommonLogic;
import com.sap.security.core.logonadmin.IAccessToLogic;
import com.sap.security.core.logonadmin.ServletAccessToLogic;

import java.io.IOException;
import java.util.Locale;
import com.sap.security.api.*;
import com.sap.security.api.logon.ILoginConstants;
import com.sap.security.core.logon.imp.SecurityPolicy;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.ErrorBean;
import com.sap.security.core.util.Message;
import com.sap.security.core.util.ResourceBean;
import com.sap.security.core.util.IUMTrace;
/**
 * Title: UM3 Description: Copyright: Copyright (c) 2001 Company: SAPMarkets,
 * Inc
 *
 * @author     William Li
 * @created    July 12, 2001
 * @version    1.0
 */

public class SAPMLogonCertLogic extends UserAdminCommonLogic {
  private IAccessToLogic proxy;

  public final static String VERSIONSTRING = "$Id: //engine/j2ee.ume/dev/src/com.sap.security.core.admin/_tc~sec~ume~logon/webapp/WEB-INF/java/com/sap/security/core/sapmimp/logon/SAPMLogonCertLogic.java#3 $ from $DateTime: 2005/06/03 13:14:11 $ ($Change: 18458 $)";         
  private static IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

  public final static String servlet_alias = "/logon/logonCertServlet";
  public final static String component_alias = "/logonCert.properties";

  //actions
  public final static String uidPasswordLogonAction = "uidPasswordLogon";
  public final static String changePasswordAction = "changePassword";
  public final static String performChangePasswordAction = "performChangePassword";
  public final static String uidPasswordClearAction = "uidPasswordClear";
  public final static String showUidPasswordLogonPage = "showUidPasswordLogonPage";

//  private final static String labelBaseName = "logonLabels";
//  private final static String pageBaseName = "logonPages";

  //logon jsp pages
  private final static String umLogonCertPage = "umLogonCertPage";
  private final static String umLogonPage = "umLogonPage";
  private final static String changePasswordPage = "changePasswordPage";
//  private final static String errorPage = "errorPage";

  public SAPMLogonCertLogic(IAccessToLogic proxy) {
    this.proxy = proxy;
  }

  public static String alias(IAccessToLogic proxy) {
    if (proxy.getEnvironment().equals(IAccessToLogic.ENV_LOGONSERVLET)) {
      if (trace.beDebug()) {
        trace.debugT("alias", "logonservlet environment, alias is " + ServletAccessToLogic.logon_certservlet_alias);
      }
      return ServletAccessToLogic.logon_certservlet_alias;
    }
    
    String alias = proxy.getAlias(null);
    
    if (trace.beDebug()) {
      trace.debugT("alias", "alias is " + alias);
    }
    
    return alias;
  }

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.UserAdminCommonLogic#getLocaleBean()
   */
  public ResourceBean getLocaleBean() {
    if (trace.bePath()) {
      trace.entering("getLocaleBean");
    }

    ResourceBean localeBean = 
        (ResourceBean) proxy.getSessionAttribute(LogonLocaleBean.beanId);
    
    if (trace.beDebug()) {
      trace.debugT("getLocaleBean", "locale retrieved from the proxy is: ",
                   new Object[] {localeBean == null 
                                 ? "null" 
                                 : localeBean.getLocale().getDisplayName()} );
    }
    
    if (trace.bePath()) {
      trace.exiting("getLocaleBean");
    }
    
    return localeBean;
  } // getLocaleBean

  /* (non-Javadoc)
   * @see com.sap.security.core.logonadmin.UserAdminCommonLogic#executeRequest()
   */
  public void executeRequest() {
    final String methodname = "executeRequest";
    
    if (trace.bePath()) {
      trace.entering(methodname);
    }
    
    try {
      initBeans();

      String errmsg = (String) proxy.getSessionAttribute(SAPMLogonLogic.FATAL_MESSAGE);
      
      if (trace.beDebug()) {
        trace.debugT(methodname, "Error message: " + errmsg);
      }
            
      if (!(errmsg == null || errmsg.equals(""))) {
        proxy.setSessionAttribute(ErrorBean.beanId, new ErrorBean(new Message(errmsg)));
        proxy.removeSessionAttribute(SAPMLogonLogic.FATAL_MESSAGE);
        
        if (trace.beDebug()) {
          trace.debugT(methodname, "Error bean set as session attribute");
        }
      }

      if (proxy.isAction(changePasswordAction)) {
        if (trace.beDebug()) {
          trace.debugT(methodname, "Action: change password");
        }
        changePassword();
        
      } else if (proxy.isAction(showUidPasswordLogonPage)) {
        if (trace.beDebug()) {
          trace.debugT(methodname, "Action: show uid password logon page");
        }
        proxy.gotoPage(umLogonCertPage);
        
      } else if (proxy.isAction(performChangePasswordAction)) {
        if (trace.beDebug()) {
          trace.debugT(methodname, "Action: perform change password");
        }
        performChangePassword();
        
      } else if (proxy.isAction(uidPasswordLogonAction)) {
        if (trace.beDebug()) {
          trace.debugT(methodname, "Action: uid password logon");
        }
        // check longUid/pswd from logon page
        uidPasswordLogon();
        
      } else if (proxy.isAction(uidPasswordClearAction)) {
        if (trace.beDebug()) {
          trace.debugT(methodname, "Action: clear password");
        }
        proxy.gotoPage(umLogonCertPage);
        
      } else {
        certLogon();
      }
    } catch (Exception e) {
      trace.errorT("doPost", "Fatal logon error", e);
    } finally {
      if (trace.bePath()) {
        trace.exiting(methodname);
      }
    }
  }

  /**
   * Description of the Method
   *
   * @exception  Exception  Description of Exception
   */
  private void initBeans() throws Exception {
    Locale locale = proxy.getRequestLocale();
    trace.infoT("initBeans", "locale = " + locale + "; session = " + proxy.getSessionId());

    ResourceBean localeBean = 
        (ResourceBean) proxy.getSessionAttribute(LogonLocaleBean.beanId);
    
    if (null == localeBean || !locale.equals(localeBean.getLocale())) {
      proxy.setSessionAttribute(LogonLocaleBean.beanId, new LogonLocaleBean(locale));
      proxy.setSessionAttribute(LogonMessageBean.beanId, new LogonMessageBean(locale));
      trace.infoT("initBeans", "LogonLocaleBean and LogonMessageBean created");
    }
  }

  /**
   *  Description of the Method
   *
   * @exception  IOException              Description of Exception
   * @exception  UMException  Description of Exception
   */
  private void changePassword() throws IOException, UMException {
    trace.infoT("changePassword", "changePassword request");
    proxy.gotoPage(changePasswordPage);
  }

  /**
   *  Description of the Method
   *
   * @exception  IOException                   Description of Exception
   * @exception  UMException       Description of Exception
   */
  private void performChangePassword() throws IOException, UMException {
    final String methodName = "performChangePassword";
    if (trace.bePath()) {
      trace.entering(methodName);
    }

    try {
      // todo where should the form fields be checked if they are empty and if new password equals confirm new password. If they are checked here, this will be more quickly. If they are checked in security service, then the exception message will be unified.
      String newPassword = proxy.getRequestParameter(ILoginConstants.NEW_PASSWORD);
      String confirmNewPassword = proxy.getRequestParameter(ILoginConstants.CONFIRM_PASSWORD);

      if (newPassword == null) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "New password is null");
        }
        throw new InvalidPasswordException(SecurityPolicy.NEW_PASSWORD_INVALID);
      }
      
      if (!newPassword.equals(confirmNewPassword)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "New password and confirmation password does not match");
        }
        throw new InvalidPasswordException(SecurityPolicy.NEW_PASSWORDS_UNMATCHED);
      }

      proxy.setRequestAttribute("logon.cert.password.changed", "true");
    } catch (InvalidPasswordException ex) {
      //this can be improved, this exception is thrown only when new password is null
      //or the confirmation password is different from it.
      if (trace.beDebug()) {
        trace.debugT(methodName, "Exception occurred: "+ ex.getMessage(), ex);
      }

      if (SecurityPolicy.NEW_PASSWORDS_UNMATCHED.equalsIgnoreCase(ex.getMessage())) {
        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(SecurityPolicy.NEW_PASSWORDS_UNMATCHED)));
      
      } else if (SecurityPolicy.NEW_PASSWORD_INVALID.equalsIgnoreCase(ex.getMessage())) {
        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(SecurityPolicy.NEW_PASSWORD_INVALID)));
      
      } else if (SecurityPolicy.WRONG_OLD_PASSWORD.equalsIgnoreCase(ex.getMessage())) {
        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(SecurityPolicy.WRONG_OLD_PASSWORD)));
      
      } else if (SecurityPolicy.CHANGE_PASSWORD_NOT_ALLOWED.equalsIgnoreCase(ex.getMessage())) {
        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(SecurityPolicy.CHANGE_PASSWORD_NOT_ALLOWED)));
      
      } else if (SecurityPolicy.USERID_CONTAINED_IN_PASSWORD.equalsIgnoreCase(ex.getMessage())) {
        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(SecurityPolicy.USERID_CONTAINED_IN_PASSWORD)));
      
      } else if (SecurityPolicy.PASSWORD_TOO_LONG.equalsIgnoreCase(ex.getMessage())) {
        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(SecurityPolicy.PASSWORD_TOO_LONG)));
      
      } else if (SecurityPolicy.PASSWORD_TOO_SHORT.equalsIgnoreCase(ex.getMessage())) {
        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(SecurityPolicy.PASSWORD_TOO_SHORT)));
      
      } else if (SecurityPolicy.MIXED_CASE_REQUIRED_FOR_PSWD.equalsIgnoreCase(ex.getMessage())) {
        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(SecurityPolicy.MIXED_CASE_REQUIRED_FOR_PSWD)));
      
      } else if (SecurityPolicy.ALPHANUM_REQUIRED_FOR_PSWD.equalsIgnoreCase(ex.getMessage())) {
        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(SecurityPolicy.ALPHANUM_REQUIRED_FOR_PSWD)));
      
      } else if (SecurityPolicy.SPEC_CHARS_REQUIRED_FOR_PSWD.equalsIgnoreCase(ex.getMessage())) {
        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(SecurityPolicy.SPEC_CHARS_REQUIRED_FOR_PSWD)));
      }

      proxy.gotoPage(changePasswordPage);
    } finally {
      if (trace.bePath()) {
        trace.exiting(methodName);
      }
    }
  }


  /**
   * Description of the Method
   *
   * @exception  IOException                   Description of Exception
   * @exception  UMException       Description of Exception
   * @exception  FeatureNotAvailableException  Description of Exception
   */
  private void uidPasswordLogon() throws IOException, UMException, FeatureNotAvailableException {
    final String methodname = "uidPasswordLogon";
    
    if (trace.bePath()) {
      trace.entering(methodname);
    }

    try {
      String longUid = proxy.getRequestParameter(ILoginConstants.LOGON_USER_ID);

      if (longUid != null) {
        longUid = longUid.trim();
      }
      
      if (trace.beDebug()) {
        trace.debugT(methodname, "longUid = " + longUid);
      }

      if (longUid == null || longUid.equals("")) {
        if (trace.beDebug()) {
          trace.debugT(methodname, "Error: " + SecurityPolicy.MISSING_UID);
        }
        throw new InvalidIDException(SecurityPolicy.MISSING_UID);
      }

      String password = proxy.getRequestParameter(ILoginConstants.LOGON_PWD_ALIAS);
      
      if (password == null || password.equals("")) {
        if (trace.beDebug()) {
          trace.debugT(methodname, "Error: "+ SecurityPolicy.MISSING_PASSWORD);
        }
        throw new InvalidPasswordException(SecurityPolicy.MISSING_PASSWORD);
      }
      
      String redirectURL = proxy.getRequestParameter(ILoginConstants.REDIRECT_PARAMETER);
      trace.infoT(methodname, "the redirectURL is: " + redirectURL);
      //saveCertificate(longUid, password);
      doUidPwLogon(redirectURL);
    
    } catch (Exception ex) {
      proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(ex.getMessage())));
      trace.errorT(methodname, "uidPasswordLogon - Exception occurred in SAPMLogonCertServlet", ex);
      proxy.gotoPage(umLogonCertPage);
      
    } finally {
      if (trace.bePath()) {
        trace.exiting(methodname);
      }
    }
  }

  /**
   *  Description of the Method
   *
   * @exception  IOException                   Description of Exception
   * @exception  UMException       Description of Exception
   */
  private void certLogon() throws IOException, UMException {
    final String methodname = "certLogon";
    
    if (trace.bePath()) {
      trace.entering(methodname);
    }
    
    try {
      String redirectURL = proxy.getRequestParameter(ILoginConstants.REDIRECT_PARAMETER);
      trace.infoT(methodname, "the redirectURL is: " + redirectURL);
      doCertLogon(redirectURL);
    } catch (Exception ex) {
      trace.errorT(methodname, "Exception occurred in SAPMLogonCertServlet", ex);
      proxy.gotoPage(umLogonCertPage);
    } finally {
      if (trace.bePath()) {
        trace.exiting(methodname);
      }
    }
  }


  private void doCertLogon(String redirectURL) throws IOException, UMException {
    if (trace.beDebug()) {
      trace.debugT("doCertLogon", "The redirectURL is " + redirectURL);
    }
    doLogon(redirectURL, true);
  }

  private void doUidPwLogon(String redirectURL) throws IOException, UMException {
    if (trace.beDebug()) {
      trace.debugT("doUidPwLogon", "The redirectURL is " + redirectURL);
    }
    
    doLogon(redirectURL, false);
  }

  /**
   *  Description of the Method
   *
   * @param  redirectURL  Description of Parameter
   * @param  useScheme    Description of Parameter
   */
  private void doLogon(String redirectURL, boolean useScheme) throws IOException, UMException {
    final String methodname = "doLogon";
    if (trace.bePath()) {
      trace.entering(methodname, new Object[]{redirectURL, Boolean.valueOf(useScheme)});
    }

    try {
      String schemeName = null;

      if (useScheme) {
        schemeName = proxy.getRequiredAuthScheme();
      }
       
      if (trace.beDebug()) {
        trace.debugT(methodname, "Scheme name is " + schemeName);
      }
      
      proxy.logon(schemeName);
      
      if (trace.beDebug()) {
        trace.debugT(methodname, "Logon passed");
      }
      
      proxy.sendRedirect(redirectURL);
    } catch (Exception ume) {
      String msg = ume.getMessage();
      boolean isR3 = false;

      // check if error message came from R/3
      if (msg.startsWith("R3-")) {
        isR3 = true;
        msg = msg.substring(3);
      }
      
      proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(msg)));
      trace.errorT(methodname, "doLogon failed", ume);
      
      if (SAPMLogonLogic.isPasswordExpiredException(ume, proxy)) {
        this.proxy.setRequestAttribute(ErrorBean.beanId, SAPMLogonLogic.createPwdChangeErrorBean(ume));

        if (trace.beDebug()) {
          trace.debugT(methodname, "Password expired");
        }
        
        proxy.gotoPage(changePasswordPage);
      } else {
        this.proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(msg)));
        
        if (trace.beDebug()) {
          trace.debugT(methodname, "isR3 message: " + isR3);
        }
        
        // if error message from R/3, go back to normal logon page
        if (isR3) {
          proxy.gotoPage(umLogonPage);
        } else {
          proxy.gotoPage(umLogonCertPage);
        }
      }
    } finally {
      if (trace.bePath()) {
        trace.exiting(methodname);
      }
    }
  }
}
