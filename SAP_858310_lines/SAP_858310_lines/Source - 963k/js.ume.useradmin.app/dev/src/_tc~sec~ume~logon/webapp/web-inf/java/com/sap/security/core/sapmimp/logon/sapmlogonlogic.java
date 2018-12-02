package com.sap.security.core.sapmimp.logon;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.security.auth.login.LoginException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sap.engine.lib.security.LoginExceptionDetails;
import com.sap.security.api.FeatureNotAvailableException;
import com.sap.security.api.ISearchAttribute;
import com.sap.security.api.ISearchResult;
import com.sap.security.api.ISecurityPolicy;
import com.sap.security.api.IUser;
import com.sap.security.api.IUserAccount;
import com.sap.security.api.InvalidIDException;
import com.sap.security.api.InvalidPasswordException;
import com.sap.security.api.NoSuchUserAccountException;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.api.logon.ILoginConstants;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.admin.permissions.UMAdminPermissions;
import com.sap.security.core.imp.UserSearchFilter;
import com.sap.security.core.logon.imp.SecurityPolicy;
import com.sap.security.core.logonadmin.IAccessToLogic;
import com.sap.security.core.logonadmin.ServletAccessToLogic;
import com.sap.security.core.logonadmin.UserAdminCommonLogic;
import com.sap.security.core.logonadmin.AccessToLogicException;
import com.sap.security.core.util.ErrorBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.Message;
import com.sap.security.core.util.ResourceBean;
import com.sap.security.core.util.notification.SendMailAsynch;
import com.sapmarkets.tpd.TradingPartnerDirectoryCommon;
import com.sapmarkets.tpd.master.PartnerID;
import com.sapmarkets.tpd.master.TradingPartnerInterface;


/**
 *  Title: UM3 Description: Copyright: Copyright (c) 2001 Company: SAPMarkets,
 *  Inc
 *
 * @author     Tianwen Tim Feng
 * @created    April 20, 2001
 * @version    1.0
 */
public class SAPMLogonLogic extends UserAdminCommonLogic {
  public final static String VERSIONSTRING = "$Id: //engine/j2ee.ume/dev/src/com.sap.security.core.admin/_tc~sec~ume~logon/webapp/WEB-INF/java/com/sap/security/core/sapmimp/logon/SAPMLogonLogic.java#6 $ from $DateTime$ ($Change$)";
  
  private static IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);
  
  public final static String uidPasswordLogonAction = "uidPasswordLogon";
  public final static String setLanguageAction = "setLanguage";
  public final static String changePasswordAction = "changePassword";
  public final static String changePasswordErrorAction = "changePasswordError";
  public final static String performChangePasswordAction = "performChangePassword";

  /**
   * Added this field to fix a compiler bug in JDK 1.3.0.
   * Remove this field once everyone uses a new compiler version
   */
  public final static String TODOX = "TODOX";
  public final static String FATAL_MESSAGE = "FATAL_MSG";
  public final static String uidPasswordClearAction = "uidPasswordClear";
  public final static String gotoHelpPage = "gotoHelpPage";
  public final static String gotoResetPasswordPage = "gotoResetPasswordPage";
  public final static String helpActionPage = "helpActionPage";
  public final static String helpResetPasswordPage = "PASSWORD_RESET";
  public final static String resetPasswordAction = "resetPassword";
  public final static String helpLogonProblemPage = "LOGON_PROBLEM";
  public final static String logonProblemAction = "logonProblem";
  public final static String forgotPasswordAction = "forgotPassword";
  public final static String forgotPasswordCancelAction = "forgotPasswordCancel";
  public final static String logoffAction = "logout_submit";
  public final static String showUidPasswordLogonPage = "showUidPasswordLogonPage";
  public final static String showUidPasswordErrorPage = "showUidPasswordErrorPage";
  //private final static String labelBaseName = "logonLabels";
  //private final static String pageBaseName = "logonPages";

  //logon jsp pages
  private final static String umLogonPage = "umLogonPage";
  private final static String changePasswordPage = "changePasswordPage";
  //private final static String errorPage = "errorPage";
  //private final static String umForgotPasswordPage = "forgotPasswordPage";
  private final static String umHelpPage = "umHelpPage";
  private final static String umResetPasswordPage = "umResetPasswordPage";
  private final static String umLogonProblemPage = "umLogonProblemPage";
  private final static String logoffPage = "umLogoffPage";

  private IAccessToLogic proxy;

  public SAPMLogonLogic(IAccessToLogic proxy) {
    this.proxy = proxy;
  }

  public static String alias(IAccessToLogic proxy) {
    if (proxy.getEnvironment().equals(IAccessToLogic.ENV_LOGONCERTSERVLET)) {
      if (trace.beDebug()) {
        trace.debugT("alias", "logoncertservlet environment, alias is " + ServletAccessToLogic.logon_servlet_alias);
      }
      
      return ServletAccessToLogic.logon_servlet_alias;
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

    ResourceBean localeBean = (ResourceBean) proxy.getSessionAttribute(LogonLocaleBean.beanId);
    
    if (trace.beDebug()) {
      trace.debugT("getLocaleBean", 
                   "locale retrieved from the proxy is: ",
                   new Object[] {(localeBean == null) 
                                  ? "null"
                                  : localeBean.getLocale().getDisplayName()
      });
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
    final String methodName = "executeRequest";
    if (trace.bePath()) {
      trace.entering(methodName);
    }
    
    try {
      if (proxy.getRequestParameter(logoffAction) != null) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found logoff action in the request parameters");
        }
        performLogoff();
        
        return;
      }

      // TODO: when is this used?
      if (proxy.isAction(setLanguageAction)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: set language");
        }
        setLanguage();
      }

      if (proxy.isAction("ume.logon.locale")) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: ume.logon.locale");
        }
        setLanguage();
        setLanguageCookie();
      }

      initBeans();

      if (proxy.isAction(changePasswordAction)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: change password");
        }
        proxy.gotoPage(changePasswordPage);
        
      } else if (proxy.isAction(showUidPasswordLogonPage)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: show uid password logon page");
        }
        proxy.gotoPage(umLogonPage);
        
      } else if (proxy.isAction(showUidPasswordErrorPage)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: show uid password error page");
        }
        showUidPasswordErrorPage();
        
      } else if (proxy.isAction(changePasswordErrorAction)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: change password error");
        }
        changePasswordErrorAction();
        
      } else if (proxy.isAction(performChangePasswordAction)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: perform password change");
        }
        performChangePassword();
          
      } else if (proxy.isAction(uidPasswordLogonAction)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: uid password logon");
        }
        uidPasswordLogon();
        
      } else if (proxy.isAction(uidPasswordClearAction)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: clear uid password");
        }
        proxy.gotoPage(umLogonPage);
          
      } else if (proxy.isAction("RPWFS")) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: perform reset password");
        }
        // formsubmit on reset password page
        performResetPassword();
        
      } else if (proxy.isAction("OLPFS")) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: perform logon problem");
        }
        // formsubmit on other logon problem page
        performLogonProblem();
        
      } else if (proxy.isAction(gotoResetPasswordPage)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: goto reset password page");
        }
        proxy.gotoPage(umResetPasswordPage);
        
      } else if (proxy.isAction(gotoHelpPage)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: goto help page");
        }
        proxy.gotoPage(umHelpPage);
        
      } else if (proxy.isAction(helpResetPasswordPage)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: help reset password page");
        }
        proxy.gotoPage(umResetPasswordPage);
        
      } else if (proxy.isAction(helpLogonProblemPage)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: help logon problem page");
        }
        proxy.gotoPage(umLogonProblemPage);
      
      } else if (proxy.isAction(forgotPasswordAction)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: perform forgot password");
        }
        performForgotPassword();
        
      } else if (proxy.isAction(forgotPasswordCancelAction)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: cancel forgot password");
        }
        proxy.gotoPage(umLogonPage);
        
      } else if (proxy.isAction(logoffAction)) {
        if (trace.beDebug()) {
          trace.debugT(methodName, "Found action: logoff");
        }
        performLogoff();
      
      } else {
        boolean noex = false;
        
        String errmsg = proxy.getRequestParameter(FATAL_MESSAGE);
        byte exceptionCause = -1;
        
        // check also login exception in attributes...
        // TODO: clean this mess up, there's also exception handling in the doLogon method
        if (errmsg == null) {
          if (trace.beDebug()) {
            trace.debugT(methodName, "No error message found in request parameters.");
          }

          LoginException lex = (LoginException) proxy.getRequestAttribute(
              LoginException.class.getName());

          if (lex != null) {
            errmsg = lex.getMessage();
            exceptionCause = getExceptionCause(lex);
          }
        }

        if (errmsg == null) {
          if (trace.beDebug()) {
            trace.debugT(methodName, "No error message found in request attributes.");
          }

          /*
           *  ok, works like that:
           * on first logon, there is no error message, therefore we do
           * automatically logon for certs.
           * but we should check if there is a cert before auto logon
           */
          boolean bAutoLogon = false;

          if (proxy.getRequestHeader("Authorization") != null) {
            if (trace.beDebug()) {
              trace.debugT(methodName, "Found authorization header in request");
            }
            bAutoLogon = true;
          }

          try {
            X509Certificate[] certs = new X509Certificate[1];
            certs = (X509Certificate[]) proxy.getRequestAttribute(
                "javax.servlet.request.X509Certificate");

            if (certs != null) {
              if (trace.beDebug()) {
                trace.debugT(methodName, "Found certificate in request");
              }
              bAutoLogon = true;
            }
          } catch (Exception ex) {
            if (trace.beInfo()) {
              trace.infoT(methodName,
                          "Tried to find out if automatic logon is possible.",
                          ex);
            }
          }

          if (bAutoLogon) {
            if (trace.beDebug()) {
              trace.debugT(methodName, "Automatic logon is possible, will try to logon");
            }
			// we redirect directly to the redirectURL.
			String redirectURL = 
				proxy.getRequestParameter(ILoginConstants.REDIRECT_PARAMETER);
            
            noex = doLogon(redirectURL);
          }
        }

        if (!((errmsg == null) || errmsg.equals(""))) {
          proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(errmsg)));

          if (trace.beDebug()) {
            trace.debugT(methodName, "Error bean added to request attributes " + errmsg);
          }
        }

        if (isPasswordExpiredExceptionCause(exceptionCause, proxy)) {
          this.proxy.setRequestAttribute(ErrorBean.beanId, createPwdChangeErrorBean(exceptionCause, errmsg));

          if (trace.beDebug()) {
            trace.debugT(methodName, "Error message is for password expiration");
          }
          proxy.gotoPage(changePasswordPage);
        } else if (!(proxy instanceof ServletAccessToLogic) && proxy.getRequestParameter(performChangePasswordAction) != null) {
          /* used only in portal case: when a paasword change form has been submitted and the authentication is not successful,
            then we should redirect again to the password change page. */
          if (trace.beDebug()) {
            trace.debugT(methodName, "Password change form has been submitted.");
          }
          proxy.gotoPage(changePasswordPage);
        } else {
          if (trace.beInfo()) {
            trace.infoT(methodName, "No command found, forwarding to umLogonPage");
          }
                        
          if (!(proxy instanceof ServletAccessToLogic)) {
            proxy.gotoPage(umLogonPage);
            return ;
          }

          HttpServletRequest req   = null; 
          HttpServletResponse resp = null;
          
          try {
            req = ((ServletAccessToLogic) proxy).getRequest();
            resp= ((ServletAccessToLogic) proxy).getResponse();
          } catch (ClassCastException cce) {
            //this exception never occurs!!! there is a check for instance before these casts
            if (trace.beError()) {
              trace.errorT(methodName, "Method was called in wrong context.", cce);
            }
            
            proxy.gotoPage(umLogonPage);
            return ;
          }

          // !!
          if (req.getUserPrincipal() == null) {
            LoginException lex = (LoginException) this.proxy.getRequestAttribute(LoginException.class.getName());

            if (lex != null) {
              errmsg = lex.getMessage();
              exceptionCause = getExceptionCause(lex);
            }

            if (isPasswordExpiredExceptionCause(exceptionCause, proxy)) {
              this.proxy.setRequestAttribute(ErrorBean.beanId,
                  createPwdChangeErrorBean(exceptionCause, errmsg));
              this.proxy.gotoPage(changePasswordPage);
            } else {
            if (trace.beDebug()) {
              trace.debugT(methodName, "User is not authenticated, display logon page");
            }
              this.proxy.gotoPage(umLogonPage);
            }
          } else {
            if (trace.beDebug()) {
              trace.debugT(methodName, "User is authenticated.");
            }
            
            HttpSession session = req.getSession();
           
            if (session != null) {
              session.invalidate();
              if (trace.beDebug()) {
                trace.debugT(methodName, "Http session is invalidated");
              }
            }
             
            // we redirect directly to the redirectURL.
            String redirectURL = 
                proxy.getRequestParameter(ILoginConstants.REDIRECT_PARAMETER);

            if (redirectURL == null || redirectURL.length() == 0) {
              if (trace.beWarning()) {
                trace.warningT(methodName, 
                               "No redirectURL parameter in request. Displaying logon page.");
              }
              proxy.gotoPage(umLogonPage);
            } else {
              if (trace.beDebug()) {
                trace.debugT(methodName, "Redirect to " + redirectURL);
              }
              proxy.sendRedirect(redirectURL);
            }
          }
        }
      }
    } catch (Exception e) {
      trace.fatalT(methodName, "Fatal Logon error", e);
    } finally {
      if (trace.bePath()) {
        trace.exiting(methodName);
      }
    }
  }

  /**
   * Description of the Method
   *
   * @param  performer                    Description of Parameter
   * @param  action                       Description of Parameter
   * @exception  UMException  Description of Exception
   */
  public void checkAccess(IUser performer, String action) throws UMException {
    if (trace.beInfo()) {    
      // trace
      String traceString = "performer " + performer.getDisplayName() + " " 
                           + "performs " + action;
      trace.infoT("checkAccess", traceString);
    }

    if (!hasAccess(performer, action)) {
      throw new UMException("NO_ACCESS");
    }
  }
  
  /**
   * TODO: when is that used?
   */
  private void setLanguage() {
    final String methodName = "setLanguage";
    Locale locale = null; 
    String localeParam = proxy.getRequestParameter("ume.logon.locale");

    if (localeParam != null) {
      StringTokenizer st = new StringTokenizer(localeParam, "_");
      String [] params = {"", "", ""};
      int counter = 0;
      
      while (st.hasMoreTokens()) {
        params[counter++] = st.nextToken();
        
        if (counter >= 3) {
          break;
        }
      }
      
      locale = new Locale(params[0], params[1], params[2]);
      
      if (trace.beDebug()) {
        trace.debugT(methodName, "Locale is obtained from request parameter \"ume.logon.locale\"");
      }
    }

    if (null == locale) {
      locale = proxy.getRequestLocale();
      
      if (trace.beDebug()) {
        trace.debugT(methodName, "Locale is obtained from \"Accept-Language\" header");
      }
    }
    
    if (null != locale) {
      proxy.setSessionAttribute(setLanguageAction, locale);
      
      if (trace.beDebug()) {
        trace.debugT(methodName, "Set session attribute " + setLanguageAction + " to " + locale);
      }
    }
  }

  /**
   * sets the cookie "ume.logon.locale" with the chosen locale
   */
  private void setLanguageCookie() {
    String locale = proxy.getRequestParameter("ume.logon.locale");
    
    if (locale != null) {
      Cookie lc = new Cookie("ume.logon.locale", locale);
      // TODO: how setting cookie
      proxy.setCookie(lc);
      
      if (trace.beDebug()) {
        trace.debugT("setLanguageCookie", "sets the cookie \"ume.logon.locale\" with the chosen locale" + locale);
      }
    }
  }

  /**
   * Description of the Method
   *
   * @exception  Exception  Description of Exception
   */
  private void initBeans() throws Exception {
    final String methodName = "initBeans";
    Locale locale = (Locale) proxy.getSessionAttribute(setLanguageAction);

    if (locale == null) {
      locale = proxy.getRequestLocale();
      if (trace.beDebug()) {
        trace.debugT(methodName, "Get locale from request");
      }
    }
    
    if (trace.beDebug()) {
      trace.debugT(methodName, "Locale is " + locale);
    }
      
    ResourceBean localeBean = (ResourceBean) proxy.getSessionAttribute(LogonLocaleBean.beanId);

    if ((localeBean == null) || !locale.equals(localeBean.getLocale())) {
      proxy.setSessionAttribute(LogonLocaleBean.beanId, new LogonLocaleBean(locale));
      proxy.setSessionAttribute(LogonMessageBean.beanId, new LogonMessageBean(locale));
      proxy.setSessionAttribute(LogonBean.beanId, new LogonBean());
    
      if (trace.beInfo()) {
        trace.infoT(methodName, "LogonLocaleBean and LogonMessageBean created");
      }
    }
    
    proxy.setSessionAttribute(LanguagesBean.beanId, new LanguagesBean(locale));
    
    if (trace.beInfo()){
      trace.infoT(methodName, "LanguagesBean created");
    }
  }

  /**
   *  Description of the Method
   *
   * @exception  IOException                   Description of Exception
   * @exception  UMException       Description of Exception
   * @exception  FeatureNotAvailableException  Description of Exception
   */
  private void performChangePassword() throws IOException, UMException {
    final String methodname = "performChangePassword";
  
    if (trace.bePath()) {
      trace.entering(methodname);
    }

    String longUid = null;

    try {
      longUid = proxy.getRequestParameter(ILoginConstants.LOGON_USER_ID);

      if (longUid != null) {
        longUid = longUid.trim();
      }

      if (trace.beDebug()) {
        if ((longUid == null) || longUid.equals("")) {
          trace.debugT(methodname, "Error: " + SecurityPolicy.MISSING_UID);
        }
      }

      String redirectURL = proxy.getRequestParameter(ILoginConstants.REDIRECT_PARAMETER);
      String oldPassword = proxy.getRequestParameter(ILoginConstants.OLD_PASSWORD);

      if ((oldPassword == null) || oldPassword.equals("")) {
        if (trace.beDebug()) {
          trace.debugT(methodname, "Error: " + SecurityPolicy.MISSING_PASSWORD);
        }
        throw new InvalidPasswordException(SecurityPolicy.MISSING_PASSWORD);
      }

      String newPassword = proxy.getRequestParameter(ILoginConstants.NEW_PASSWORD);

      if ((newPassword == null) || newPassword.equals("")) {
        if (trace.beDebug()){
          trace.debugT(methodname, "Error: " + SecurityPolicy.MISSING_NEW_PASSWORD);
        }
        throw new InvalidPasswordException(SecurityPolicy.MISSING_NEW_PASSWORD);
      }

      String confirmNewPassword = proxy.getRequestParameter(ILoginConstants.CONFIRM_PASSWORD);

      if ((confirmNewPassword == null) || confirmNewPassword.equals("")) {
        if (trace.beDebug()){
          trace.debugT(methodname, "Error: " + SecurityPolicy.MISSING_PASSWORD_CONFIRM);
        }
        throw new InvalidPasswordException(SecurityPolicy.MISSING_PASSWORD_CONFIRM);
      }

      if (!newPassword.equals(confirmNewPassword)) {
        if (trace.beDebug()){
          trace.debugT(methodname, "Error: " + SecurityPolicy.NEW_PASSWORDS_UNMATCHED);
        }
        throw new InvalidPasswordException(SecurityPolicy.NEW_PASSWORDS_UNMATCHED);
      }

      doLogon(redirectURL);
      proxy.setRequestAttribute("logon.password.uid", longUid);
      proxy.setRequestAttribute("logon.password.newpassword", newPassword);
        
    } catch (InvalidPasswordException ex) {
      /**@todo check if iua is necessary...looks not
                  if (iua == null) {
                      iua = UMFactory.getUserAccountFactory().getUserAccountByLogonId(longUid);
                  }
      */
      trace.errorT(methodname, "Exception occurred: " + ex.getMessage(), ex);

      ISecurityPolicy isp = UMFactory.getSecurityPolicy();

      if (ex.getMessage().equalsIgnoreCase(SecurityPolicy.PASSWORD_TOO_SHORT)) {
        proxy.setRequestAttribute(ErrorBean.beanId,
            new ErrorBean(new Message(ex.getMessage(), new Integer(isp.getPasswordMinLength()))));
        
      } else if (ex.getMessage().equalsIgnoreCase(SecurityPolicy.PASSWORD_TOO_LONG)) {
        proxy.setRequestAttribute(ErrorBean.beanId,
            new ErrorBean(new Message(ex.getMessage(), new Integer(isp.getPasswordMaxLength()))));
        
      } else if (ex.getMessage().equalsIgnoreCase(SecurityPolicy.ALPHANUM_REQUIRED_FOR_PSWD)) {
        proxy.setRequestAttribute(ErrorBean.beanId,
            new ErrorBean(new Message(ex.getMessage(), new Integer(isp.getPasswordAlphaNumericRequired()))));
        
      } else if (ex.getMessage().equalsIgnoreCase(SecurityPolicy.MIXED_CASE_REQUIRED_FOR_PSWD)) {
        proxy.setRequestAttribute(ErrorBean.beanId,
            new ErrorBean(new Message(ex.getMessage(), new Integer(isp.getPasswordMixCaseRequired()))));
          
      } else if (ex.getMessage().equalsIgnoreCase(SecurityPolicy.SPEC_CHARS_REQUIRED_FOR_PSWD)) {
        proxy.setRequestAttribute(ErrorBean.beanId,
            new ErrorBean(new Message(ex.getMessage(), new Integer(isp.getPasswordSpecialCharRequired()))));
      } else {
        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(ex.getMessage())));
      }

      proxy.gotoPage(changePasswordPage);
 
    } catch (NoSuchUserAccountException nosuex) {
      if (trace.beDebug()){
        trace.debugT(methodname, "Change password error: " + nosuex.getMessage(), nosuex);
      }

      proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(SecurityPolicy.USER_AUTH_FAILED)));
      proxy.gotoPage(changePasswordPage);
      
    } catch (Exception ex) {
      // All other exceptions are unecpected exceptions!
      if (trace.beDebug()){
        trace.debugT(methodname, "Change password error: " + ex.getMessage(), ex);
      }

      proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message("UNKNOWN_CHANGE_PASSWORD_ERROR")));
      proxy.gotoPage(changePasswordPage);
    }
  }

  /**
   * Description of the Method
   *
   * @exception  IOException       Description of Exception
   * @exception  UMException       Description of Exception
   */
  private void uidPasswordLogon() throws IOException, UMException {
    final String methodname = "uidPasswordLogon";
    
    if (trace.bePath()) {
      trace.entering(methodname);
    }

    try {
      String longUid = proxy.getRequestParameter(ILoginConstants.LOGON_USER_ID);

      if (longUid != null) {
        longUid = longUid.trim();
      }

      if ((longUid == null) || longUid.equals("")) {
        if (trace.beDebug()){
          trace.debugT(methodname, "Error: " + SecurityPolicy.MISSING_UID);
        }
        throw new InvalidIDException(SecurityPolicy.MISSING_UID);
      }

      String password = proxy.getRequestParameter(ILoginConstants.LOGON_PWD_ALIAS);

      if ((password == null) || password.equals("")) {
        if (trace.beDebug()){
          trace.debugT(methodname, "Error: " + SecurityPolicy.MISSING_PASSWORD);
        }
        throw new InvalidPasswordException(SecurityPolicy.MISSING_PASSWORD);
      }

      String redirectURL = proxy.getRequestParameter(ILoginConstants.REDIRECT_PARAMETER);

      if (trace.beInfo()){
        trace.infoT(methodname, "the redirectURL is: " + redirectURL);
      }

      doLogon(redirectURL);
    } catch (Exception ex) {
      trace.errorT(methodname, "Exception occurred in SAPMLogonServlet: ", ex);
      proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(ex.getMessage())));

      //forwarding to logon page
      if (trace.beInfo()){
        trace.infoT(methodname, "uidPasswordLogon-forwarding to " + umLogonPage);
      }
      
      proxy.gotoPage(umLogonPage);
    } finally {
      if (trace.bePath()) {
        trace.exiting(methodname);
      }
    }
  }

  private void performForgotPassword() throws IOException, UMException, FeatureNotAvailableException {
    final String methodname = "performForgotPassword";
  
    if (trace.bePath()) {
      trace.entering(methodname);
    }

    try {
      String longUid = proxy.getRequestParameter(LogonBean.LONGUID);

      if (longUid != null) {
        longUid = longUid.trim();
      }

      // check for email id
      String email = proxy.getRequestParameter("email");
      String lastName = proxy.getRequestParameter("lastname");
      String firstName = proxy.getRequestParameter("firstname");
      String noteToAdmin = proxy.getRequestParameter("notetoadmin");
      IUser userFrom = UMFactory.getUserFactory().getUserByLogonID(longUid);

      if (email.equalsIgnoreCase(userFrom.getEmail()) 
            && firstName.equalsIgnoreCase(userFrom.getFirstName()) 
            && lastName.equalsIgnoreCase(userFrom.getLastName())) {
        // email matched, assign a new password and email to user
        String newPass = UMFactory.getSecurityPolicy().generatePassword();
        IUserAccount ua = UMFactory.getUserAccountFactory()
                                   .getMutableUserAccount(longUid);
        ua.setPassword(newPass);
        ua.save();
        ua.commit();

        SendMailAsynch.generateEmailOnUMEvent(userFrom, userFrom,
                    SendMailAsynch.USER_PASSWORD_RESET_PERFORMED, null, newPass);
    
        if (trace.beDebug()){
          trace.debugT(methodname,
              "Send reset pswd email to " + userFrom.getDisplayName() 
              + "/" + userFrom.getEmail() + "(" + newPass + ")");
        }
        
        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message("NEW_PSWD_ASSIGNED")));
        
        if (trace.beDebug()){
          trace.debugT(methodname, "New password assigned...email to user");
        }
      } else {
        // email did not match, send message to company administrator
        sendAdminEmail(userFrom, SendMailAsynch.USER_PASSWORD_RESET_REQUEST, noteToAdmin);
    
        if (trace.beDebug()) {
          trace.debugT(methodname,
              "Sent error email: from " + userFrom.getDisplayName() 
              + " to administrator of company: " + userFrom.getCompany());
        }
        
        proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message("ERROR_INFO_ENTERED")));
    
        if (trace.beDebug()) {
          trace.debugT(methodname, "Error info entered...email to administrator");
        }
      }
    } catch (Exception ex) {
      proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(ex.getMessage())));
      trace.errorT(methodname, "Error exception: ", ex);
    }

    proxy.gotoPage(umLogonPage);

    if (trace.bePath()) {
      trace.exiting(methodname);
    }
  }

  private void performLogonProblem() throws IOException, UMException, FeatureNotAvailableException {
    final String methodname = "performLogonProblem";
  
    if (trace.bePath()) {
      trace.entering(methodname);
    }

    String longUid = proxy.getRequestParameter(LogonBean.LONGUID);

    if (longUid != null) {
     longUid = longUid.trim();
    }

    String email = proxy.getRequestParameter("email");
    String lastName = proxy.getRequestParameter("lastname");
    String firstName = proxy.getRequestParameter("firstname");
    String noteToAdmin = proxy.getRequestParameter("notetoadmin");
    IUser userFrom = null;

    try {
      userFrom = UMFactory.getUserFactory().getUserByLogonID(longUid);

      if (trace.beDebug()){
        trace.debugT(methodname, "Sending email to administrator from: " + longUid);
      }

      if (sendAdminEmail(userFrom, SendMailAsynch.USER_LOGON_PROBLEM_REQUEST, 
                         noteToAdmin)) {
        proxy.setRequestAttribute(ErrorBean.beanId, 
            new ErrorBean(new Message("LOGON_PROBLEM_EMAIL_SENT")));
    
        if (trace.beDebug()) {
          trace.debugT(methodname, "Email sent to administrator");
        }
      }
    } catch (Exception ex) {
      if (trace.beDebug()) {
        trace.debugT(methodname, "Failed to send email to administrator", ex);
      }
      
      // search for user base on name and email
      UserSearchFilter userSearchFilter = new UserSearchFilter();
      userSearchFilter.setLastName(lastName, ISearchAttribute.EQUALS_OPERATOR, false);
      userSearchFilter.setFirstName(firstName, ISearchAttribute.EQUALS_OPERATOR, false);
      userSearchFilter.setEmail(email, ISearchAttribute.EQUALS_OPERATOR, false);

      ISearchResult foundUsers = UMFactory.getUserFactory().searchUsers(userSearchFilter);

      if (foundUsers.size() == 1) {
        userFrom = UMFactory.getUserFactory().getUser((String)foundUsers.next());
    
        if (trace.beDebug()){
          trace.debugT(methodname, 
              "Sending email to administrator from: " + userFrom.getDisplayName());
        }

        if (sendAdminEmail(userFrom,
                            SendMailAsynch.USER_LOGON_PROBLEM_REQUEST,
                            noteToAdmin)) {
          proxy.setRequestAttribute(ErrorBean.beanId, 
              new ErrorBean(new Message("LOGON_PROBLEM_EMAIL_SENT")));

          if (trace.beDebug()) {
            trace.debugT(methodname, "Email sent to administrator");
          }
        } else {
          proxy.setRequestAttribute(ErrorBean.beanId,
              new ErrorBean(new Message("LOGON_PROBLEM_INFO_ERROR")));
              
          if (trace.beDebug()) {
            trace.debugT(methodname, "Unable to send email to administrator");
          }
          
          proxy.gotoPage(umLogonProblemPage);
          
          if (trace.bePath()) {
            trace.exiting(methodname);
          }
      
          return;
        }
      } else {
        userFrom = null;

        if (trace.beDebug()){
          trace.debugT(methodname, "Sending email to administrator...from unknown user");
        }

        if (sendAdminEmail(userFrom, SendMailAsynch.USER_LOGON_PROBLEM_REQUEST,
                            noteToAdmin)) {
          proxy.setRequestAttribute(ErrorBean.beanId,
              new ErrorBean(new Message("LOGON_PROBLEM_EMAIL_SENT")));

          if (trace.beDebug()) {
            trace.debugT(methodname, "Email sent to administrator");
          }
        } else {
          proxy.setRequestAttribute(ErrorBean.beanId,
              new ErrorBean(new Message("LOGON_PROBLEM_INFO_ERROR")));

          if (trace.beDebug()){
            trace.debugT(methodname, "Unable to send email to administrator");
          }
          
          proxy.gotoPage(umLogonProblemPage);
      
          if (trace.bePath()){
            trace.exiting(methodname);
          }
          return;
        }
      }
    }

    proxy.gotoPage(umLogonPage);
    
    if (trace.bePath()) {
      trace.exiting(methodname);
    }
  }

  private boolean sendAdminEmail(IUser userFrom, String event, String message) {
    final String methodname = "sendEmail";
  
    try {
      TradingPartnerInterface company = null;
      
      if (userFrom != null && userFrom.getCompany() != null 
          && userFrom.getCompany().length() > 0) {
        company = TradingPartnerDirectoryCommon.getTPD().getPartner(
            PartnerID.instantiatePartnerID(userFrom.getCompany()));
      }
          
      if (company != null) {
        SendMailAsynch.generateEmailToAdminOnUMEvent(userFrom, company, event, message);
      } else {
        SendMailAsynch.generateEmailToAdminOnUMEvent(userFrom, event, message);
      }
                
      if (trace.beDebug()) {
        trace.debugT(methodname,
                     "Sent email: from " + userFrom.getDisplayName() 
                     + " to administrator of company: " + userFrom.getCompany());
      }
    } catch (Exception ex) {
      if (trace.beDebug()) {
        trace.debugT(methodname, "No email sent.", ex);
      }
            
      return false;
    }

    return true;
  }

  private void performResetPassword() throws IOException, UMException {
    final String methodname = "performResetPassword";
    
    if (trace.bePath()) {
      trace.entering(methodname);
    }

    try {
      String longUid = proxy.getRequestParameter(LogonBean.LONGUID);

      if (longUid != null) {
        longUid = longUid.trim();
      }

      // check for email id
      String email = proxy.getRequestParameter("email");
      String lastName = proxy.getRequestParameter("lastname");
      String firstName = proxy.getRequestParameter("firstname");
      IUser userFrom = UMFactory.getUserFactory().getUserByLogonID(longUid);

      if (email.equalsIgnoreCase(userFrom.getEmail()) 
            && firstName.equalsIgnoreCase(userFrom.getFirstName()) 
            && lastName.equalsIgnoreCase(userFrom.getLastName())) {
        // email matched, assign a new password and email to user
        //String newPass = PasswordGen.generate();
        String newPass = UMFactory.getSecurityPolicy().generatePassword();
        IUserAccount ua = 
            UMFactory.getUserAccountFactory().getUserAccountByLogonId(longUid);
        IUserAccount mua = 
            UMFactory.getUserAccountFactory().getMutableUserAccount(ua.getUniqueID());

        //                ua.prepare();
        mua.setPassword(newPass);
        mua.save();
        mua.commit();

        SendMailAsynch.generateEmailOnUMEvent(userFrom, userFrom,
            SendMailAsynch.USER_PASSWORD_RESET_PERFORMED, null, newPass);
        
        proxy.setRequestAttribute(ErrorBean.beanId, 
            new ErrorBean(new Message("NEW_PSWD_ASSIGNED")));
    
        if (trace.beDebug()){
          trace.debugT(methodname,
                       "Send reset pswd email to " + userFrom.getDisplayName() 
                       + "/" + userFrom.getEmail() + "(" + newPass + ")");
          trace.debugT(methodname, "New password assigned...email to user");
        }
      } else {
        // email did not match, send message to administrator(s)
        proxy.setRequestAttribute(ErrorBean.beanId,
            new ErrorBean(new Message("RESET_PASSWORD_INFO_ERROR")));
    
        if (trace.beDebug()) {
          trace.debugT(methodname, "Error info entered...email to administrator");
        }
      }
    } catch (Exception ex) {
      //ex.printStackTrace();
      proxy.setRequestAttribute(ErrorBean.beanId,
          new ErrorBean(new Message("RESET_PASSWORD_INFO_ERROR")));
      trace.errorT(methodname, "Error exception: ", ex);
    }

    proxy.gotoPage(umResetPasswordPage);
    
    if (trace.bePath()) {
      trace.exiting(methodname);
    }
  }

  /**
   * Description of the Method
   *
   * @param  redirectURL  Description of Parameter
   */
  private boolean doLogon(String redirectURL) throws IOException, UMException {
    final String methodname = "doLogon";
  
    if (trace.bePath()) {
      trace.entering(methodname, new Object[] { redirectURL });
    }

    try {
      proxy.logon(null);
      
      if (trace.beDebug()) {
        trace.debugT(methodname, "Logon passed, redirect to " + redirectURL);
      }

      proxy.sendRedirect(redirectURL);
    } catch (Exception ume) {
      String errmsg = ume.getMessage();

      if (errmsg == null) {
        errmsg = "";
      }

      proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(new Message(errmsg)));
      trace.errorT(methodname, "doLogon failed", ume);

      if (isPasswordExpiredException(ume, proxy)) {
        this.proxy.setRequestAttribute(ErrorBean.beanId,
            createPwdChangeErrorBean(ume));

        if (trace.beDebug()) {
          trace.debugT(methodname, "Password change is required, forward to " + changePasswordPage);
        }
        proxy.gotoPage(changePasswordPage);
      } else {
        this.proxy.setRequestAttribute(ErrorBean.beanId,
            new ErrorBean(new Message(errmsg)));
        
        if (trace.beDebug()) {
          trace.debugT(methodname, "Forward to logon page " + umLogonPage);
        }
        proxy.gotoPage(umLogonPage);
      }

      if (trace.bePath()) {
        trace.exiting(methodname, new Object[] {Boolean.FALSE});
      }
      
      return false;
    }

    if (trace.bePath()) {
      trace.exiting(methodname, new Object[] {Boolean.TRUE});
    }

    return true;
  }

  static ErrorBean createPwdChangeErrorBean(Exception ex) {
    byte reason = ((LoginExceptionDetails) ex).getExceptionCause();
    String msg = ex.getMessage();
    
    return createPwdChangeErrorBean(reason, msg);
  }
  
  private static ErrorBean createPwdChangeErrorBean(byte exceptionCause, String msg) {
    if (exceptionCause == LoginExceptionDetails.CHANGE_PASSWORD_TOO_SHORT) {
      return new ErrorBean(new Message(msg, new Integer(UMFactory.getSecurityPolicy().getPasswordMinLength())));
    } 
    
    if (exceptionCause == LoginExceptionDetails.CHANGE_PASSWORD_TOO_LONG) {
      return new ErrorBean(new Message(msg, new Integer(UMFactory.getSecurityPolicy().getPasswordMaxLength())));
    }
    
    if (exceptionCause == LoginExceptionDetails.CHANGE_PASSWORD_ALPHANUM_REQUIRED) {
      return new ErrorBean(new Message(msg,
          new Integer(UMFactory.getSecurityPolicy().getPasswordAlphaNumericRequired())));
    }
    
    if (exceptionCause == LoginExceptionDetails.CHANGE_PASSWORD_MIXED_CASE_REQUIRED) {
      return new ErrorBean(new Message(msg,
                new Integer(UMFactory.getSecurityPolicy().getPasswordMixCaseRequired())));
    }
    
    if (exceptionCause == LoginExceptionDetails.CHANGE_PASSWORD_SPEC_CHARS_REQUIRED) {
      return new ErrorBean(new Message(msg,
                new Integer(UMFactory.getSecurityPolicy().getPasswordSpecialCharRequired())));
    }
    
    if (exceptionCause == LoginExceptionDetails.WRONG_USERNAME_PASSWORD_COMBINATION) {
      return new ErrorBean(new Message("WRONG_OLD_PASSWORD"));
    }
    
    return new ErrorBean(new Message(msg));
  }

  /**
   * Description of the Method
   *
   * @param  performer  Description of Parameter
   * @param  action     Description of Parameter
   * @return            Description of the Returned Value
   */
  public static boolean hasAccess(IUser performer, String action) {
    if (trace.beInfo()) {
      // trace
      String traceString = "performer " + performer.getDisplayName() + " " 
                           + "performs " + action;
      trace.infoT("hasAccess", traceString);
    }

    // only for the purpose of menu bar or navigation bar
    // the URL links
    return performer.hasPermission(new UMAdminPermissions(action));
  }

  // hasAccess
  private void performLogoff() {
    final String methodname = "performLogoff";
  
    if (trace.beDebug()) {
      trace.debugT(methodname, "Logging off");
    }

    try {
      initBeans();

      LogonBean logonBean = (LogonBean) proxy.getSessionAttribute(LogonBean.beanId);
      logonBean.setLogoffRedirect(proxy);
      
      if (trace.beDebug()) {
        trace.debugT(methodname, "Forward to logoff page " + logoffPage);
      }
      
      proxy.gotoPage(logoffPage);
      
      if (trace.beDebug()) {
        trace.debugT(methodname, "Perform logoff.");
      }
      proxy.logout();
    } catch (Exception e) {
      if (trace.beDebug()){
        trace.debugT(methodname, e);
      }
    }
  }

  private void showUidPasswordErrorPage() throws AccessToLogicException, IOException {
    setErrorMessage();

    if (trace.beDebug()) {
      trace.debugT("showUidPasswordErrorPage", "Going to page " + umLogonPage);
    }
    
    proxy.gotoPage(umLogonPage);
  }

  private void changePasswordErrorAction() throws AccessToLogicException, IOException {
    setErrorMessage();
    
    if (trace.beDebug()) {
      trace.debugT("changePasswordErrorAction", "Going to page " + changePasswordPage);
    }
    
    proxy.gotoPage(changePasswordPage);
  }

  private void setErrorMessage() {
    String message = proxy.getRequestParameter("error_message");

    if (message != null) {
      this.proxy.setRequestAttribute(ErrorBean.beanId, new ErrorBean(message));
    }
  }
  
  static byte getExceptionCause(Exception e) {
    if (e instanceof LoginExceptionDetails) {
      return ((LoginExceptionDetails) e).getExceptionCause();
    }
    
    return -1;
  }
  
  static boolean isPasswordExpiredExceptionCause(byte cause, IAccessToLogic proxy) {
    return cause == LoginExceptionDetails.PASSWORD_EXPIRED
        || cause >= LoginExceptionDetails.CHANGE_PASSWORD_NO_PASSWORD
            && cause <= LoginExceptionDetails.UNKNOWN_CHANGE_PASSWORD_ERROR
        || cause == LoginExceptionDetails.WRONG_USERNAME_PASSWORD_COMBINATION
            && proxy.getRequestParameter(ILoginConstants.OLD_PASSWORD) != null;
  }
  
  static boolean isPasswordExpiredException(Exception e, IAccessToLogic proxy) {
    return isPasswordExpiredExceptionCause(getExceptionCause(e), proxy);
  }
}
