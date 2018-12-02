/*
 * Copyright (c) 2000-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.application;

import iaik.security.random.SecRandom;
import iaik.security.random.SeedGenerator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;
import java.security.Permission;
import java.security.Policy;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.security.SecureRandom;

import javax.security.auth.Subject;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.engine.frame.core.thread.ThreadContext;
import com.sap.engine.interfaces.security.AuthenticationContext;
import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.interfaces.security.SecurityContextObject;
import com.sap.engine.interfaces.security.userstore.context.UserContext;
import com.sap.engine.interfaces.security.userstore.context.UserInfo;
import com.sap.engine.lib.lang.Convert;
import com.sap.engine.lib.security.Base64;
import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.op.util.FailOver;
import com.sap.engine.services.httpserver.interfaces.ErrorData;
import com.sap.engine.services.httpserver.interfaces.HttpHandler;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.RequestPathMappings;
import com.sap.engine.services.httpserver.interfaces.SupportabilityData;
import com.sap.engine.services.httpserver.lib.CookieParser;
import com.sap.engine.services.httpserver.lib.CookieUtils;
import com.sap.engine.services.httpserver.lib.HttpCookie;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.ProtocolParser;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.httpserver.lib.util.Ascii;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.NewApplicationSessionException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;
import com.sap.engine.services.servlets_jsp.server.security.PrivilegedFillSubject;
import com.sap.engine.services.servlets_jsp.server.security.policy.PolicyDomain;
import com.sap.engine.services.servlets_jsp.server.servlet.AuthenticationFilter;
import com.sap.engine.session.SessionDomain;
import com.sap.engine.session.SessionException;
import com.sap.engine.session.failover.FailoverConfig;
import com.sap.engine.session.runtime.IpProtectionException;
import com.sap.engine.session.runtime.MarkIdProtectionException;
import com.sap.engine.session.runtime.http.HttpSessionRequest;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;

public class SessionServletContext {
  private static final int USER_DATA_CONSTRAINT_OK = 1;
  private static final int USER_DATA_CONSTRAINT_REDIRECT = 2;
  private static final int USER_DATA_CONSTRAINT_ERROR = 3;
  
  private static final String REQUEST_ATTRIBUTE_SESSION_PROLONGATION_REQUIRED = "sap.com/session_prolongation";
  
  private final static Location currentLocation = Location.getLocation(SessionServletContext.class);
  private final static Location securityTraceLocation = LogContext.getLocationSecurity();

  //"_SAP".getBytes("ISO-8859-1") - it is needed for generate session id
  private final static byte[] _SAP_BYTES = {95, 83, 65, 80};
  
  private String aliasName = null;
  private int sessionTimeout = -1;
  private SecurityContext appSecurityContext = null;
  private AuthenticationContext authenticationContext = null;
  private String realmName = null;
  private String authType = null;
  private String aplicationCookieName = null;
  private String formLoginErrorPage = null;
  private String formLoginLoginPage = null;
  private String changePasswordLoginPage = null;
  private String changePasswordErrorPage = null;
  private int intFailOver = FailOver.DISABLE.getId().byteValue();

  private PolicyDomain policyDomain = null;
  private ApplicationContext servletContextFacade = null;
  //  private Hashtable session = new Hashtable();
  private SessionDomain sessionDomain;

  private Policy umePolicy = null;

  private int securityObjectId = -1;

  private SecureRandom secRandom;
  private SeedGenerator seedGenerator;
  private byte[] nodeIDByteArr = new byte[4];

  public SessionServletContext(String aliasName, String contextNameInternalUse, String aliasForSecurity,
                               ApplicationContext servletContextFacade, SecurityContext securityContext,
                               WebApplicationConfig webApplicationConfig, SessionDomain sessionDomain, String aplicationCookieName) throws DeploymentException {
    this.sessionDomain = sessionDomain;
    String aliasId = servletContextFacade.getAliasName();
    aliasId = aliasId.startsWith("/") ? aliasId : "/" + aliasId;
    this.sessionDomain.setDomainAttribute(SessionDomain.SHARED_TABLE_ID, aliasId);

    this.intFailOver = webApplicationConfig.getIntFailOver();
    if (FailOver.DISABLE.getId().byteValue() != intFailOver) {
      this.sessionDomain.setConfiguration(FailoverConfig.FAILOVER_SCOPE, FailoverConfig.INSTANCE_LOCAL);
    }

    this.aliasName = aliasName;
    // TODO: Don't pass these parameter
    // this.contextNameInternalUse = contextNameInternalUse;
    this.servletContextFacade = servletContextFacade;
    this.sessionTimeout = webApplicationConfig.getSessionTimeout();
    // this.secRoles = webApplicationConfig.getSecurityRoles();
    this.aplicationCookieName = aplicationCookieName;

    if (webApplicationConfig.getRealmName() != null) {
      this.realmName = webApplicationConfig.getRealmName();
    } else {
      realmName = aliasName;
    }

    formLoginLoginPage = normalize(webApplicationConfig.getLoginPage());
    formLoginErrorPage = normalize(webApplicationConfig.getErrorPage());
    changePasswordLoginPage =
      normalize(webApplicationConfig.getChangePasswordLoginPage());
    changePasswordErrorPage =
      normalize(webApplicationConfig.getChangePasswordErrorPage());

    secRandom = SecRandom.getDefault(); // get default secure random
    seedGenerator =  SeedGenerator.getDefaultSeedGenerator(); // get the default seed generator
    updateSeed();
    initNodeIDBytes(ServiceContext.getServiceContext().getServerId());

    appSecurityContext = securityContext.getPolicyConfigurationContext(servletContextFacade.getApplicationName() + "*" + aliasForSecurity);
    if (appSecurityContext == null) {
      String dcName = LoggingUtilities.getDcNameByClassLoader(securityContext.getClass().getClassLoader());
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_APPLICATION_SECURITY_CONTEXT_FOR_WEB_APPLICATION,
        new Object[]{dcName, LoggingUtilities.getCsnComponentByDCName(dcName), getAliasName()});
    }
    authenticationContext = appSecurityContext.getAuthenticationContext();

    authType = "APPLICATION";
    if (authenticationContext != null) {
      String temp = authenticationContext.getProperty("auth_method");
      if (temp != null) {
        temp = temp.toUpperCase();
        if (temp.equals(HttpServletRequest.BASIC_AUTH)) {
          authType = HttpServletRequest.BASIC_AUTH;
        } else if (temp.equals(HttpServletRequest.FORM_AUTH)) {
          authType = HttpServletRequest.FORM_AUTH;
        } else if (temp.equals(HttpServletRequest.DIGEST_AUTH)) {
          authType = HttpServletRequest.DIGEST_AUTH;
        } else if (temp.equals("CLIENT_CERT") || temp.equals("CLIENT-CERT")) {
          authType = HttpServletRequest.CLIENT_CERT_AUTH;
        }
      }
    }
    policyDomain = ServiceContext.getServiceContext().getWebContainerPolicy().createDomain(webApplicationConfig.getDomainName(), aliasName);
  } // SessionServletContext()


  public String getAliasName() {
    return aliasName;
  }

  public int getSessionTimeout() {
    return sessionTimeout;
  }

  /**
   * Returns SessionDomain contains the all session-objects related to the given application.
   *
   * @return Session domain, which consists of a set of key-to-session
   *         object bindings.
   */
  public SessionDomain getSession() {
    return sessionDomain;
  }

  public AuthenticationContext getAuthenticationContext() {
    return authenticationContext;
  }

  public SecurityContext getAppSecurityContext() {
    return appSecurityContext;
  }

  public PolicyDomain getPolicyDomain() {
    return policyDomain;
  }

  public String getLoginRealmName() {
    return realmName;
  }

  public String getAuthType() {
    return authType;
  }

  public String getFormLoginLoginPage() {
    return formLoginLoginPage;
  }

  public String getFormLoginErrorPage() {
    return formLoginErrorPage;
  }

  public String getChangePasswordLoginPage() {
    return changePasswordLoginPage;
  }

  public String getChangePasswordErrorPage() {
    return changePasswordErrorPage;
  }

  public String getApplicationCookieEncoded(String zone, String zoneSeparator, int dispatcherId) {
    if ("sapj2ee_".equals(ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getLoadBalancingCookiePrefix())) {
      if (zone == null || zone.length() == 0) {
        return ";" + aplicationCookieName + ServiceContext.getServiceContext().getServerId();
      } else {
        return ";" + aplicationCookieName + zoneSeparator + zone + ServiceContext.getServiceContext().getServerId();
      }
    } else {
      String instanceName = ServiceContext.getServiceContext().getInstanceName();
      if (instanceName == null) {
        instanceName = "J2EE" + dispatcherId;
      }
      if (zone == null || zone.length() == 0) {
        return ";" + aplicationCookieName + "=(" + instanceName + ")" + ServiceContext.getServiceContext().getServerId();
      } else {
        return ";" + CookieParser.app_cookie_prefix + zone + "=(" + instanceName + ")" + ServiceContext.getServiceContext().getServerId();
      }
    }
  }

  public String getApplicationCookieName() {
    return aplicationCookieName;
  }

  /**
   * Checks security constraints and performs login if necessary
   *
   * @param httpParameters This request http parameters
   * @param requestURI     path relative to the context root without query string and path parameters
   * @return A {@link HttpHandler} flag, how http provider to continue with
   *         this request
   * @throws IOException
   */
  public byte checkUser(HttpParameters httpParameters, MessageBytes requestURI, MessageBytes filePath)
    throws IOException {
    boolean beDebug = securityTraceLocation.beDebug();
  	if (beDebug) {
    	securityTraceLocation.debugT("checkUser(" + httpParameters + ", req: " + requestURI.toString() + ");");
		}
		String httpMethod =
      new String(httpParameters.getRequest().getRequestLine().getMethod());

    // Enforces user-data constraint if available
    int userDataConstraintsCheckResult = redirectUserDataConstrJacc(
      httpParameters, requestURI);

    if (userDataConstraintsCheckResult == USER_DATA_CONSTRAINT_REDIRECT) {
      return HttpHandler.NOOP;
    } else if (userDataConstraintsCheckResult == USER_DATA_CONSTRAINT_ERROR) {
      // Every request to form-login, form-login error, change-password
      // and change-password error pages has to pass directly regardless
      // of security constraint is available or not
      if (!(requestURI.equals(formLoginLoginPage) ||
        requestURI.equals(formLoginErrorPage) ||
        requestURI.equals(changePasswordLoginPage) ||
        requestURI.equals(changePasswordErrorPage))) {
        return HttpHandler.ERROR;
      } else {
        httpParameters.setErrorData(null);
      }
    }

    // check if the resource is constrained at all - with anonymous user
    boolean isConstrained = !doCheckPermissionsJacc(httpMethod, httpParameters
      .getRequest().getRequestLine().isSecure(), requestURI);
    if (beDebug) {
    	securityTraceLocation.debugT("checkUser(isConstrained:" + isConstrained + ", httpMethod: " + httpMethod + ");");
    }
    if (isConstrained) {
      // Marks requests to security constrained
      // resources in order they not to be cached by ICM
      httpParameters.setProtected(true);

      // Fix problem with security check and trailing slash redirect. This prevents
      // 404 not found for /j_security_check after POST request.
      // A request URI of /foo will be redirected to a URI of /foo/.
      // A request URI of /catalog/products will be redirected to a URI of /catalog/products/.
      // Results in POST to /foo/j_security_check
      byte [] location = servletContextFacade.checkRedirectNeeded(httpParameters, requestURI, filePath);
      if (location != null) {
        httpParameters.redirect(location);
        return HttpHandler.RESPONSE_DONE;
      }
    }

    //boolean isLogged = doCrossApplicationLogin(httpParameters);

    // Binds actual security session
    try {
      httpParameters.getSessionRequest().applyUserContext();
    } catch (IpProtectionException e) {
      if (securityTraceLocation.beDebug()) {
        securityTraceLocation.debugT("checkUser " + e.getMessage());
      }
      HttpCookie cookie = setNewJsessionID(httpParameters);
      if (servletContextFacade.getWebApplicationConfiguration().isURLSessionTracking()) {
        sendRedirectWithNewJSESSIONID(httpParameters, cookie);
        return HttpHandler.RESPONSE_DONE;
      } else {
        return HttpHandler.NOOP;
      }
    } catch (MarkIdProtectionException e) {
      if (securityTraceLocation.beDebug()) {
        securityTraceLocation.debugT("checkUser " + e.getMessage());
      }
      setNewJsessionID(httpParameters);
      return enforceAuthentication(httpParameters);
    } 

    // Sets security policy domain in the security context so that the security service can get
    // access to it later. It is needed for both constrained and public resources.
    // TODO: Remove after application sessions handling is changed by Session Management team.
    getSecurityContext().setSecurityPolicyDomain(policyDomain.getName());

    // TODO: If it is better make this check only when login is needed
    // Every request to form-login, form-login error, change-password,
    // change-password error pages
    // has to be passed directly regardless of security constraints
    if (requestURI.equals(formLoginLoginPage)
        || requestURI.equals(formLoginErrorPage)
        || requestURI.equals(changePasswordLoginPage)
        || requestURI.equals(changePasswordErrorPage)) {
      return HttpHandler.NOOP;
    }

    // Checks if there has an already authenticated user for
    // the policy domain. True means that login is required but
    // only if requested resource is security constrained
    boolean isAuthenticated = authenticationContext.isAuthenticatedInPolicyDomain();
    if (beDebug) {
      securityTraceLocation.debugT("checkUser(isAuthenticatedInPolicyDomain:" + isAuthenticated + ", httpMethod: " + httpMethod + ");");
    }

    if (isAuthenticated) {

      // Check whether session prolongation is enabled by the SessionProlongation property
      boolean sessionProlongationEnabled = ServiceContext.getServiceContext().getWebContainerProperties().getSessionProlongation();
      if (sessionProlongationEnabled) {
        // If session prolongation is needed an authentication must be enforced
        int applicationSessionTimeout = servletContextFacade.getWebApplicationConfiguration().getSessionTimeout();
        boolean isSessionPrologationNeeded = authenticationContext.isSessionProlongationNeeded(applicationSessionTimeout);
        if (isSessionPrologationNeeded) {
          httpParameters.setRequestAttribute(REQUEST_ATTRIBUTE_SESSION_PROLONGATION_REQUIRED, isSessionPrologationNeeded);
          return enforceAuthentication(httpParameters);
        }
      }

      if (isConstrained) {
        // TODO: Check if here is the right place
        // If this application is in one policy domain with other
        // login will be omitted but http session is required
        createSession(httpParameters);
      }

    } else {
      // TODO: Check if this is correct in case of UME login
      // Removes security session when there aren't any authenticated
      // user and resource isn't security constrained
      httpParameters.getSessionRequest().removeUserContext();

      // Every request to paths reserved from the Servlet 2.4
      // specification for the form-login and from the SAP
      // for password-change has to enforce authentication
      if (requestURI.endsWith("/j_security_check")
          || requestURI.endsWith("/sap_j_security_check")
          || isConstrained) {
        return enforceAuthentication(httpParameters);
      }
    }

    return checkPermissionsAndReturnResponse(httpParameters, requestURI);
  }

  private byte checkPermissionsAndReturnResponse(HttpParameters httpParameters, MessageBytes requestURI) {
    String httpMethod = new String(httpParameters.getRequest().getRequestLine().getMethod());
    if (doCheckPermissionsJacc(httpMethod, httpParameters.getRequest().getRequestLine().isSecure(), requestURI)) {
      return HttpHandler.NOOP;
    } else {
      httpParameters.setErrorData(new ErrorData(HttpServletResponse.SC_FORBIDDEN,
        Responses.mess22, Responses.mess9, false, new SupportabilityData()));//here we do not need user action
      return HttpHandler.ERROR;
    }
  }

  public  HttpCookie addApplicationCookie(HttpParameters httpParameters) {
    HttpCookie result = null;
    String aplicationCookieNameZoneEncoded = aplicationCookieName;
    if (httpParameters.getRequestPathMappings().getZoneName() != null && httpParameters.getRequestPathMappings().getZoneName().length() != 0) {
      aplicationCookieNameZoneEncoded = CookieParser.app_cookie_prefix + httpParameters.getRequestPathMappings().getZoneName();
    }
    ArrayObject cooks = httpParameters.getRequest().getCookies(servletContextFacade.getWebApplicationConfiguration().isURLSessionTracking());
    boolean appCookieInRequest = false;
    for (int i = 0; cooks != null && i < cooks.size(); i++) {
      HttpCookie httpCookie = (HttpCookie) cooks.elementAt(i);
      if (httpCookie.getName().equals(aplicationCookieNameZoneEncoded)) {
        if (httpCookie.getValue().startsWith("(") && httpCookie.getValue().indexOf(')') > 0) {
          String instanceId = httpCookie.getValue().substring(1, httpCookie.getValue().indexOf(')'));
          if ("sapj2ee_".equals(ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getLoadBalancingCookiePrefix())) {
            if (ServiceContext.getServiceContext().getServerId() == Ascii.asciiArrToIntNoException(
              httpCookie.getValue().getBytes(), 0, httpCookie.getValue().length())) {
              appCookieInRequest = true;
              break;
            }
          } else {
            String currentInstanceId = ServiceContext.getServiceContext().getInstanceName();
            if (currentInstanceId == null) {
              currentInstanceId = "J2EE" + httpParameters.getRequest().getDispatcherId();
            }
            if (instanceId.equals(currentInstanceId)
              && ServiceContext.getServiceContext().getServerId() == Ascii.asciiArrToIntNoException(
                httpCookie.getValue().getBytes(),
                httpCookie.getValue().indexOf(')') + 1,
                httpCookie.getValue().length() - httpCookie.getValue().indexOf(')') - 1)) {
              appCookieInRequest = true;
              break;
            }
          }
        }
      }
    }
    if (!appCookieInRequest && !httpParameters.isSetApplicationCookie()) {
      String cookieValue = null;
      if ("sapj2ee_".equals(ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getLoadBalancingCookiePrefix())) {
        cookieValue = "" + ServiceContext.getServiceContext().getServerId();
      } else {
        String instanceId = ServiceContext.getServiceContext().getInstanceName();
        if (instanceId == null) {
          instanceId = "J2EE" + httpParameters.getRequest().getDispatcherId();
        }
        cookieValue = "(" + instanceId + ")" + ServiceContext.getServiceContext().getServerId();
      }
      result = CookieParser.createCookie(aplicationCookieNameZoneEncoded, cookieValue,
         httpParameters.getRequest().getHost(),
         servletContextFacade.getWebApplicationConfiguration().getApplicationCookieConfig());
      addCookie(result, httpParameters);
      httpParameters.setApplicationCookie(true);
    }
    return result;
  }

  public String encodeURL(String url, MessageBytes alias, HttpParameters httpParameters, ApplicationSession applicationSession) {
    if (!servletContextFacade.getWebApplicationConfiguration().isURLSessionTracking()) {
      ArrayObject cookies = httpParameters.getRequest().getCookies(servletContextFacade.getWebApplicationConfiguration().isURLSessionTracking());
      if (cookies != null && cookies.size() > 0) {
        return ProtocolParser.makeAbsolute(url, alias,
          httpParameters.getRequest().getScheme(),
          httpParameters.getRequest().getHost(),
          httpParameters.getRequest().getPort());
      }
    }
    int ind = url.indexOf('?');
    String params = "";
    if (ind > -1) {
      params = url.substring(ind);
      url = url.substring(0, ind);
    }
    ind = url.indexOf(';');
    if (ind == -1) {
      return ProtocolParser.makeAbsolute(
        url + CookieParser.jsessionid_url_sep + applicationSession.getIdInternal()
        + encodeApplicationCookies("",
          httpParameters.getRequestPathMappings().getZoneName(),
          httpParameters.getRequest().getDispatcherId()) + params,
        httpParameters.getRequestPathMappings().getAliasName(),
        httpParameters.getRequest().getScheme(),
        httpParameters.getRequest().getHost(),
        httpParameters.getRequest().getPort());
    }
    String encoded = url.substring(ind);
    url = url.substring(0, ind);
    if (!encoded.startsWith(CookieParser.jsessionid_url_sep)) {
      return ProtocolParser.makeAbsolute(
        url + CookieParser.jsessionid_url_sep + applicationSession.getIdInternal()
        + encodeApplicationCookies(encoded,
          httpParameters.getRequestPathMappings().getZoneName(),
          httpParameters.getRequest().getDispatcherId()) + params,
        httpParameters.getRequestPathMappings().getAliasName(),
        httpParameters.getRequest().getScheme(),
        httpParameters.getRequest().getHost(),
        httpParameters.getRequest().getPort());
    }
    encoded = encoded.substring(CookieParser.jsessionid_url_sep.length());
    ind = encoded.indexOf(';');
    if (ind == -1) {
      return ProtocolParser.makeAbsolute(
        url + CookieParser.jsessionid_url_sep + applicationSession.getIdInternal()
        + encodeApplicationCookies("",
          httpParameters.getRequestPathMappings().getZoneName(),
          httpParameters.getRequest().getDispatcherId()) + params,
        httpParameters.getRequestPathMappings().getAliasName(),
        httpParameters.getRequest().getScheme(),
        httpParameters.getRequest().getHost(),
        httpParameters.getRequest().getPort());
    }
    encoded = encoded.substring(ind);
    return ProtocolParser.makeAbsolute(
      url + CookieParser.jsessionid_url_sep + applicationSession.getIdInternal()
      + encodeApplicationCookies(encoded,
        httpParameters.getRequestPathMappings().getZoneName(),
        httpParameters.getRequest().getDispatcherId()) + params,
      httpParameters.getRequestPathMappings().getAliasName(),
      httpParameters.getRequest().getScheme(),
      httpParameters.getRequest().getHost(),
      httpParameters.getRequest().getPort());
  }

  private String encodeApplicationCookies(String encodedOld, String zone, int dispatcherId) {
    String thisAppCookieName = null;
    if (zone == null || zone.length() == 0) {
      thisAppCookieName = ";" + aplicationCookieName + "=";
    } else {
      thisAppCookieName = ";" + CookieParser.app_cookie_prefix + zone + "=";
    }
    if ("sapj2ee_".equals(ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getLoadBalancingCookiePrefix())) {
      if (encodedOld.indexOf(thisAppCookieName) > -1) {
        if (encodedOld.indexOf(thisAppCookieName + ServiceContext.getServiceContext().getServerId()) == -1) {
          int ind = encodedOld.indexOf(thisAppCookieName);
          int endInd = encodedOld.indexOf(';', ind + 1);
          if (endInd == -1) {
            encodedOld = encodedOld.substring(0, ind);
            encodedOld += thisAppCookieName + ServiceContext.getServiceContext().getServerId();
          } else {
            encodedOld = encodedOld.substring(0, ind) + thisAppCookieName + ServiceContext.getServiceContext().getServerId() + encodedOld.substring(endInd);
          }
        }
      } else {
        encodedOld = thisAppCookieName + ServiceContext.getServiceContext().getServerId() + encodedOld;
      }
    } else {
      String instanceName = ServiceContext.getServiceContext().getInstanceName();
      if (instanceName == null) {
        instanceName = "J2EE" + dispatcherId;
      }
      if (encodedOld.indexOf(thisAppCookieName) > -1) {
        if (encodedOld.indexOf(thisAppCookieName + "(" + instanceName + ")" + ServiceContext.getServiceContext().getServerId()) == -1) {
          int ind = encodedOld.indexOf(thisAppCookieName);
          int endInd = encodedOld.indexOf(';', ind + 1);
          if (endInd == -1) {
            encodedOld = encodedOld.substring(0, ind);
            encodedOld += thisAppCookieName + "(" + instanceName + ")" + ServiceContext.getServiceContext().getServerId();
          } else {
            encodedOld = encodedOld.substring(0, ind) + thisAppCookieName + "(" + instanceName + ")" + ServiceContext.getServiceContext().getServerId() + encodedOld.substring(endInd);
          }
        }
      } else {
        encodedOld = thisAppCookieName + "(" + instanceName + ")" + ServiceContext.getServiceContext().getServerId() + encodedOld;
      }
    }
    return encodedOld;
  }

  public Subject getSubject(String roleName) {
    Subject runAsSubject = new Subject();
    UserContext userCtx = appSecurityContext.getUserStoreContext().getActiveUserStore().getUserContext();
    ServiceContext.getServiceContext().getSecurityContext().getJACCSecurityRoleMappingContext().setRunAsAccountGenerationPolicy((byte)1,roleName, servletContextFacade.getPolicyConfigID());
    String userName = ServiceContext.getServiceContext().getSecurityContext().getJACCSecurityRoleMappingContext().getRunAsIdentity(roleName, servletContextFacade.getPolicyConfigID());

    if (userName != null) {
      UserInfo user = appSecurityContext.getUserStoreContext().getActiveUserStore().getUserContext().getUserInfo(userName);
      if (user == null) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000323",
          "Cannot get user for [{0}] security role.", new Object[]{roleName}, null, null);
        return null;
      }
      AccessController.doPrivileged(new PrivilegedFillSubject(userCtx, user, runAsSubject));
      return runAsSubject;
    }
    LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000324",
      "Cannot get user name for {0} security role.", new Object[]{roleName}, null, null);
    return null;
  }

  /**
   *
   * @return USER_DATA_CONSTRAINT_OK, USER_DATA_CONSTRAINT_REDIRECT, USER_DATA_CONSTRAINT_ERROR
   */
  private int redirectUserDataConstrJacc(HttpParameters httpParameters, MessageBytes contextRelativeURI) {
    boolean userDataConstraintAllowed = false;
    if (!httpParameters.getRequest().getRequestLine().isSecure()) {
      userDataConstraintAllowed = checkUserDataConstrJacc(httpParameters, contextRelativeURI, "");
    } else {
      userDataConstraintAllowed = checkUserDataConstrJacc(httpParameters, contextRelativeURI, ":CONFIDENTIAL");
      if (!userDataConstraintAllowed) {
        userDataConstraintAllowed = checkUserDataConstrJacc(httpParameters, contextRelativeURI, ":INTEGRAL");
      }
    }

    if (userDataConstraintAllowed) {
      if (securityTraceLocation.beDebug()) {
      	securityTraceLocation.debugT("checkUserDataConstr(" + contextRelativeURI + "): OK");
      }
      return USER_DATA_CONSTRAINT_OK;
    } else if (!httpParameters.getRequest().getRequestLine().isSecure()
            && !(checkUserDataConstrJacc(httpParameters, contextRelativeURI, ":CONFIDENTIAL") || checkUserDataConstrJacc(httpParameters, contextRelativeURI, ":INTEGRAL"))) {
      //check if CONFIDENTIAL or INTEGRAL is ok - otherwise redirect is meaningless
      httpParameters.setErrorData(new ErrorData(ResponseCodes.code_forbidden,
        Responses.mess22, Responses.mess23, false,
        new SupportabilityData()));//here we do not need user action
      return USER_DATA_CONSTRAINT_ERROR;
    } else {
      if (securityTraceLocation.beDebug()) {
      	securityTraceLocation.debugT("checkUserDataConstr(" + contextRelativeURI + "): FAILED");
      }
      // change scheme to try access through HTTPS. Before that check with WebResourcePermission
      httpParameters.getResponse().setSchemeHttps();
      return USER_DATA_CONSTRAINT_REDIRECT;
    }
  }

  private boolean checkUserDataConstrJacc(HttpParameters httpParameters, MessageBytes contextRelativeURI, String transportGuarantee) {
    String httpMethod = new String(httpParameters.getRequest().getRequestLine().getMethod());

    boolean beDebug = securityTraceLocation.beDebug();
    if (securityTraceLocation.bePath()) {
    	securityTraceLocation.pathT("entering checkUserDataConstrJacc() url = [" + contextRelativeURI
              + "], httpMethod = [" + httpMethod + "], transportGuarantee = [" + transportGuarantee +"]");
    }
    if (!servletContextFacade.getWebApplicationConfiguration().isWebAppWithSecurityConstraints()) {
      // optimization - return true if application has no security constraints
      if (beDebug) {
      	securityTraceLocation.debugT("checkUserDataConstrJacc(): return true (no sec. constraints in web.xml)");
      }
      return true;
    }

    if (httpParameters.getRequest().getRequestLine().isSecure()) {
      return true; //optimization;
    }

    String ctxRelURI = getContextRelativePathWoParameters(contextRelativeURI);
    if (ctxRelURI.equals("/")) {
      ctxRelURI = ""; //otherwise check permission always fails
    }
    ctxRelURI = ctxRelURI.replaceAll(":", "%3a");

    // TODO - may not be set since Web container is always the entry point
    String prevPolicyContext = servletContextFacade.setPolicyContextID(true);
    try {
      Permission userDataPermission = null;
      Policy policy = null;

      userDataPermission = new WebUserDataPermission(ctxRelURI, httpMethod + transportGuarantee);
      policy = Policy.getPolicy();
      //AccessController.checkPermission(userDataPermission); todo - use it after fixed in UME
      boolean result = policy.implies(new ProtectionDomain(null, null, null,
        new Principal[0]), userDataPermission);
      if (beDebug) {
      	securityTraceLocation.debugT("checkUserDataConstrJacc(): return " + result);
			}
			return result;
    } catch (SecurityException e) {
      if (beDebug) {
        LogContext.getLocation(LogContext.LOCATION_SECURITY).traceDebug(
            "checkUserDataConstrJacc(): Caught SecurityException. Return false: ", e, aliasName);
      }
      return false;
    } finally {
      // TODO - may not be set since Web container is always the entry point
      servletContextFacade.restorePrevPolicyContextID(prevPolicyContext);
    }
  } // checkUserDataConstr()

  public boolean doCheckPermissionsJacc(String httpMethod, boolean isSecure, MessageBytes ctxRelativeRequestURIMB) {
    boolean beDebug = securityTraceLocation.beDebug();
  	if (beDebug) {
    	securityTraceLocation.debugT("entering doCheckPermissions(): url = [" + ctxRelativeRequestURIMB
              + "], httpMethod = [" + httpMethod + "]");
    }
    // optimization
    if (!servletContextFacade.getWebApplicationConfiguration().isWebAppWithSecurityConstraints()) {
      // optimization - return true if application has no security constraints
      if (beDebug) {
      	securityTraceLocation.debugT(" doCheckPermissions(): Return true (no sec. constraints in web.xml)");
      }
      return true;
    }

    String ctxRelURI = getContextRelativePathWoParameters(ctxRelativeRequestURIMB);
    String permissionCheckURI = ctxRelURI;
    if (ctxRelURI.equals("/")) {
      permissionCheckURI = ctxRelURI = "";
    }
    permissionCheckURI = permissionCheckURI.replaceAll(":", "%3a");
    String prevPolicyContext;
    // TODO - maybe not needed since Web container is always the entry point and we check permissions only once (not on include/redirect)
    prevPolicyContext = servletContextFacade.setPolicyContextID(true); // force using standard policy context

    boolean result = false;
    try {
      Permission resourcePermission = null;
      Policy policy = null;
      resourcePermission = new WebResourcePermission(permissionCheckURI, httpMethod);
      policy = Policy.getPolicy();

      //AccessController.checkPermission(resourcePermission); todo - change when fixed in security

      if ((getSecurityContext().getSession() != null)
        && (getSecurityContext().getSession().getAuthenticationConfiguration() != null)) {
        if (beDebug) {
        	securityTraceLocation.debugT("checkResourcePermission session = [" + getSecurityContext().getSession()
                  + "], getAuthenticationConfiguration = [" + getSecurityContext().getSession().getAuthenticationConfiguration()
                  + "], getPrincipal" + getSecurityContext().getSession().getPrincipal() + "]");
        }
        result = policy.implies(
          new ProtectionDomain(null, null, null, new Principal[]{getSecurityContext().getSession().getPrincipal()}), resourcePermission);
      } else {
        if (beDebug) {
        	securityTraceLocation.debugT("checkResourcePermission - user not logged in!");
        }
        result = policy.implies(
          new ProtectionDomain(null, null, null, new java.security.Principal[]{}), resourcePermission);
      }
    } catch (SecurityException e) {
      if (securityTraceLocation.beError()) {
        LogContext.getLocation(LogContext.LOCATION_SECURITY).traceError("ASJ.web.000423",
          "checkResourcePermission", e, null, null);
      }
      result = false;
    } finally {
      // TODO - maybe not needed since Web container is always the entry point and we check permissions only once (not on include/redirect)
      servletContextFacade.restorePrevPolicyContextID(prevPolicyContext);
    }
    if (beDebug) {
    	securityTraceLocation.debugT("checkResourcePermission url = [" + permissionCheckURI
              + "], httpMethod = [" + httpMethod + "]: " + result);
    }
    return result;
  }

  public ApplicationSession createSession(HttpParameters httpParameters) {
    ApplicationSession applicationSession = null;
    if (isForbiddenNewApplicationSession()) {
      throw new NewApplicationSessionException(NewApplicationSessionException.CANNOT_CREATE_A_NEW_APPLICATION_SESSION_BECAUSE_MAX_NUMBER_OF_SESSIONS_HAS_BEEN_REACHED);
    }

    HttpCookie sCookie = httpParameters.getRequest().getSessionCookie(servletContextFacade.getWebApplicationConfiguration().isURLSessionTracking());
    if (sCookie != null) {
      HttpSessionRequest sRequest = httpParameters.getSessionRequest();
      sRequest.reDoSessionRequest(sessionDomain, sCookie.getValue(), SessionServletContext.getSessionIdFromJSession(sCookie.getValue()));
      //      SessionHolder sessionHolder = sessionDomain.getSessionHolder(sCookie.getValue());
      //      boolean createSession = false;
      //      synchronized (sessionIdSynchObject) {
      //        createSession = sessionIDlocks.get(sCookie.getValue()) == null;
      //        if (createSession) {
      //          if (getSession().get(sCookie.getValue()) != null) {
      //            createSession = false;
      //          } else {
      //            sessionIDlocks.put(sCookie.getValue(), new Object());
      //          }
      //        } else {
      //          boolean sleep = true;
      //          while (sleep) {
      //            try {
      //              sessionIdSynchObject.wait(Constants.WAIT_TIMEOUT);
      //            } catch (Exception e) {
      //              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation,
      //                  "Error in synchronizing the creation of http session " + sCookie.getValue() + ".", e, aliasName);
      //            }
      //            sleep = sessionIDlocks.contains(sCookie.getValue());
      //          }
      //        }
      //      }
      addApplicationCookie(httpParameters);
      SessionFactoryImpl factory = new SessionFactoryImpl(this, servletContextFacade, null, sCookie, httpParameters);
      try {
        applicationSession = (ApplicationSession) sRequest.getSession(factory);
      } catch (SessionException e) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000330",
          "Error in creating of HTTP session [{0}].", new Object[]{sCookie.getValue()}, e, null, null);
      }
      httpParameters.setApplicationSession(applicationSession);
      httpParameters.setDebugRequest(servletContextFacade.initializeDebugInfo(httpParameters, applicationSession));
    } else {
      HttpCookie cok = null;
      if (!httpParameters.isSetSessionCookie()) {
        //cok = CookieParser.createSessionCookie(generateSessionID(httpParameters) + generatePrivatePartJSessionId(),
        cok = CookieParser.createSessionCookie(generateJSessionIdValue(),  
          httpParameters.getRequest().getHost(), servletContextFacade.getWebApplicationConfiguration().getSessionCookieConfig());
        addCookie(cok, httpParameters);
        httpParameters.setSessionCookie(true);
      }
      addApplicationCookie(httpParameters);
      //      SessionHolder sessionHolder = sessionDomain.getSessionHolder(cok.getValue());
      HttpSessionRequest sRequest = httpParameters.getSessionRequest();
      sRequest.reDoSessionRequest(sessionDomain, cok.getValue(), SessionServletContext.getSessionIdFromJSession(cok.getValue()));
      SessionFactoryImpl factory = new SessionFactoryImpl(this, servletContextFacade, null, cok, httpParameters);
      try {
        applicationSession = (ApplicationSession) sRequest.getSession(factory);
      } catch (SessionException e) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000331",
          "Error in creating of http session [{0}].", new Object[]{cok.getValue()}, e, null, null);
      }
      httpParameters.setApplicationSession(applicationSession);
      //	  if (!httpParameters.getSessionRequest().isSessionInvolved()) {
      //		  httpParameters.getSessionRequest().involveSession(getSession(), applicationSession.getIdInternal());
      //	  } else {
      //		httpParameters.getSessionRequest().endRequest(0);
      //		try {
      //			httpParameters.getSessionRequest().activateNewSession(getSession(), applicationSession.getIdInternal());
      //		} catch (SessionExistException e1) {
      //			httpParameters.getSessionRequest().involveSession(getSession(), applicationSession.getIdInternal());
      //		}
      //	  }
      httpParameters.setDebugRequest(servletContextFacade.initializeDebugInfo(httpParameters, applicationSession));
    }
    return applicationSession;
  }

  public void removeSession(String sessionId) {
    sessionDomain.removeSession(sessionId);
  }
  //todo move in separated library - Ehp2
  /**
   * Generates MarkId cookie value
   * @return id len=10
   */
  public String generateMarkID() {
    //TODO remove this method or move to separate library (security team use similar method)
    return generateID(10, 4, 2, 0).toString();
  }  

  public static String getSessionIdFromJSession(String jSessionId) {
    if (jSessionId == null) {
      return null;
    }
    
    if (jSessionId.length() <= 44 || !jSessionId.contains("_SAP") ){
      return jSessionId;
    }
    
    return jSessionId.substring(0,44);
  }
  
  public static String getMarkIdFromJSession(String jSessionId) {
    if (jSessionId == null) {
      return null;
    }
    
    if (jSessionId.length() <= 44 || !jSessionId.contains("_SAP") ){
      return jSessionId;
    }
    
    return jSessionId.substring(44);
  }
  
  public String generateJSessionIdValue() {
    StringBuffer jsessionId = generateID(30, 20, 6, 4, 18);
    try {
      jsessionId = jsessionId.insert(40, "_SAP");
    }
    catch (StringIndexOutOfBoundsException e) {
      if (securityTraceLocation.beDebug()) {
        LogContext.getLocation(LogContext.LOCATION_SECURITY).traceDebug("Index out of bound exception. Should not be thrown", e, aliasName);
      }
    }
    
    return jsessionId.toString();
    
  }
  
  private StringBuffer generateID(int woSAPBytesLen, int SHA1BytesLen, int nanoTimeBytesLen, int nodeIDBytesLen, int additionalPrivateBytes) {
    byte[] forBase64EncByteArr = new byte[woSAPBytesLen + additionalPrivateBytes];

    // getSHA1 bits
    byte[] shaRandomBytesArr = new byte[SHA1BytesLen];
    secRandom.nextBytes( shaRandomBytesArr);
    System.arraycopy(shaRandomBytesArr, 0, forBase64EncByteArr, 0, SHA1BytesLen);

    if (woSAPBytesLen > SHA1BytesLen) {
      // Get milliseconds
      try {
        fillBytesFromNanos(forBase64EncByteArr, SHA1BytesLen);
      } catch (UnsupportedEncodingException e) {
        //TODO - change ASJ number according messageid file and add message to messageod file 
        LogContext.getLocation(LogContext.LOCATION_SECURITY).traceError("ASJ.web.000616", "session", e, null, null);
        return null;
      }
  
      // Get NodeID
      //fillNodeIDBytes(forBase64EncByteArr, base64SHA1BytesLen + base64nanoTimeBytesLen);
      System.arraycopy(nodeIDByteArr, 0, forBase64EncByteArr, SHA1BytesLen + nanoTimeBytesLen, nodeIDBytesLen);
    }
    
  //add private part
    byte[] shaRandomPrivatePartBytesArr = new byte[additionalPrivateBytes];
    secRandom.nextBytes(shaRandomPrivatePartBytesArr);
    System.arraycopy(shaRandomPrivatePartBytesArr, 0, forBase64EncByteArr, woSAPBytesLen, additionalPrivateBytes);
    
    // Base64
    byte[] base64EncByteArr = null;
    try {
      base64EncByteArr = Base64.encode(forBase64EncByteArr, Base64.VARIANT_URL);
    } catch (Exception e) {
      //TODO - change ASJ number according messageid file and add message to messageod file
      LogContext.getLocation(LogContext.LOCATION_SECURITY).traceError("ASJ.web.000617", "session", e, null, null);
      return null;
    }   
    
    StringBuffer sb = null;
    try {
      sb = new StringBuffer(new String(base64EncByteArr, "ISO-8859-1"));
    } catch (UnsupportedEncodingException e) {
      //TODO - change ASJ number according messageid file and add message to messageod file
      LogContext.getLocation(LogContext.LOCATION_SECURITY).traceError("ASJ.web.000618", "session", e, null, null);
      return null;
    }
    
    if (securityTraceLocation.beDebug()) {
      securityTraceLocation.debugT("ID: " + sb.toString());
    }
    return sb;
    
  } // generateSessionID()
  
  
  /**
   * @deprecated - use generateJSessionIdValue()
   * Generates value for JSESSIONID cookie
   * @param httpParameters - not used
   * @return session id len=40
   */
  public String generateSessionID(HttpParameters httpParameters) {
    //TODO remove this method
    /* old one
     String sessionId = "" + (int) (System.currentTimeMillis() & 0x7fffffff);
    long rand = random.nextLong();
    if (rand < 0) {
      rand = -rand;
    }
    sessionId += rand;
    return sessionId;*/

    //values for jsession ID
    final int woSAPBytesLen = 30;
    final int SHA1BytesLen = 20;
    final int nanoTimeBytesLen = 6;
    final int nodeIDBytesLen = 4;
    return generateID(woSAPBytesLen, SHA1BytesLen, nanoTimeBytesLen, nodeIDBytesLen).append("_SAP").toString();
  }

  //TODO - remove this method - it should not be used
  private StringBuffer generateID(int woSAPBytesLen, int SHA1BytesLen, int nanoTimeBytesLen, int nodeIDBytesLen) {
    /* old one
     String sessionId = "" + (int) (System.currentTimeMillis() & 0x7fffffff);
    long rand = random.nextLong();
    if (rand < 0) {
      rand = -rand;
    }
    sessionId += rand;
    return sessionId;*/

//    final int woSAPBytesLen = 30;
//    final int SHA1BytesLen = 20;
//    final int nanoTimeBytesLen = 6; // long has 8 bytes and we cut the 2 most highest bytes
//    final int nodeIDBytesLen = 4;
    byte[] forBase64EncByteArr = new byte[woSAPBytesLen];

    // getSHA1 bits
    byte[] shaRandomBytesArr = new byte[SHA1BytesLen];
    secRandom.nextBytes( shaRandomBytesArr);
    System.arraycopy(shaRandomBytesArr, 0, forBase64EncByteArr, 0, SHA1BytesLen);

    if (woSAPBytesLen > SHA1BytesLen) {
      // Get milliseconds
      try {
        fillBytesFromNanos(forBase64EncByteArr, SHA1BytesLen);
      } catch (UnsupportedEncodingException e) {
        LogContext.getLocation(LogContext.LOCATION_SECURITY).traceError("ASJ.web.000616", "session", e, null, null);
        return null;
      }
  
      // Get NodeID
      //fillNodeIDBytes(forBase64EncByteArr, base64SHA1BytesLen + base64nanoTimeBytesLen);
      System.arraycopy(nodeIDByteArr, 0, forBase64EncByteArr, SHA1BytesLen + nanoTimeBytesLen, nodeIDBytesLen);
    }
    // Base64
    byte[] base64EncByteArr = null;
    try {
      base64EncByteArr = Base64.encode(forBase64EncByteArr, Base64.VARIANT_URL);
    } catch (Exception e) {
      LogContext.getLocation(LogContext.LOCATION_SECURITY).traceError("ASJ.web.000617", "session", e, null, null);
      return null;
    }
    StringBuffer sb = null;
    try {
      sb = new StringBuffer(new String(base64EncByteArr, "ISO-8859-1"));
    } catch (UnsupportedEncodingException e) {
      LogContext.getLocation(LogContext.LOCATION_SECURITY).traceError("ASJ.web.000618", "session", e, null, null);
      return null;
    }
    
    if (securityTraceLocation.beDebug()) {
    	securityTraceLocation.debugT("ID: " + sb.toString());
    }
    return sb;
  } // generateSessionID()

  private void addCookie(HttpCookie cok, HttpParameters httpParameters) {
    if (servletContextFacade.getWebApplicationConfiguration().isURLSessionTracking()) {
      return;
    }
    if (securityTraceLocation.beDebug()) {
      securityTraceLocation.debugT("addCookie " + new String(CookieUtils.getCookieHeader(cok)));
    }
    MessageBytes cook = new MessageBytes(CookieUtils.getCookieHeader(cok));
    int index = cook.indexOf(':');

    if (cook.charAt(index + 1) == ' ' || cook.charAt(index + 1) == '\t') {
      httpParameters.getResponse().getHeaders().addHeader(cook.getBytes(0, index), cook.getBytes(index + 2));
    } else {
      httpParameters.getResponse().getHeaders().addHeader(cook.getBytes(0, index), cook.getBytes(index + 1));
    }
  }

  public boolean isForbiddenNewApplicationSession() {
    int max = -1;
    max = servletContextFacade.getWebApplicationConfiguration().getMaxSessions();
    if (max > 0 && getSession().size() >= max) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Gets context (alias)-relative path without any optional request parameters and URI session IDs
   *
   * @param requestURIMsgB context-relative path with request info
   * @return
   */
  private String getContextRelativePathWoParameters(MessageBytes requestURIMsgB) {
    String requestURI = requestURIMsgB.toStringUTF8();
    if (requestURI.indexOf("?") > -1) {
      requestURI = requestURI.substring(0, requestURI.indexOf("?"));
    }
    if (requestURI.indexOf(";") > -1) {
      requestURI = requestURI.substring(0, requestURI.indexOf(";"));
    }
    requestURI = ParseUtils.canonicalize(requestURI);
    return requestURI;
  } // getContextRelativePathWoParameters()


  /**
   * Web container enforces authentication in form of JAAS login by making the
   * request pass through the {@link AuthenticationFilter}
   *
   * @param params This request http parameters
   * @return An {@link HttpHandler} flag, how http provider to continue with
   *         this request
   */
  private byte enforceAuthentication(HttpParameters params) {
    String filterName = "AuthenticationFilter";
    RequestPathMappings mappings = params.getRequestPathMappings();
    String[] filters = mappings.getFilterChain();
    if (filters == null) {
      filters = new String[]{filterName};
      mappings.setFilterChain(filters);
    } else {
      // Adds the authentication filter in front
      String[] temp = new String[filters.length + 1];
      temp[0] = filterName;
      System.arraycopy(filters, 0, temp, 1, filters.length);
      mappings.setFilterChain(temp);
    }

    // Guarantees that request will go through the filter chain
    if (mappings.getServletName() == null) {
      mappings.setServletName(new MessageBytes("default".getBytes()));
    }

    // Doubles the guarantee that request will go through the filter chain
    return HttpHandler.START_SERVLET;
  }

  public SecurityContextObject getSecurityContext() {
    ThreadContext localTC = ServiceContext.getServiceContext().getThreadSystem().getThreadContext();
    if (securityObjectId == -1) {
      securityObjectId = localTC.getContextObjectId(SecurityContextObject.NAME);
    }
    return (SecurityContextObject) localTC.getContextObject(securityObjectId);
  }

  private String normalize(String path) {
    if (path == null) {
      return path;
    }

    // Removes query string if any
    int index = path.lastIndexOf("?");
    if (index > 0) {
      path = path.substring(0, index);
    }

    // Canonicalizes it
    path = ParseUtils.canonicalize(path)
      .replace('\\', ParseUtils.separatorChar);

    // Adds leading '/' if missing
    if (!path.startsWith(ParseUtils.separator)) {
      path = ParseUtils.separatorChar + path;
    }

    return path;
  }

  public Policy getUMEPolicy() {
    if (umePolicy != null) {
      return umePolicy;
    }

    synchronized (this) {
      if (umePolicy == null) {
        try {
          umePolicy = (Policy) Class.forName("com.sap.security.core.UmePolicy").newInstance();
        } catch (InstantiationException e) {
          throw new SecurityException(e.getMessage());
        } catch (IllegalAccessException e) {
          throw new SecurityException(e.getMessage());
        } catch (ClassNotFoundException e) {
          throw new SecurityException(e.getMessage());
        }
      }
    }
    return umePolicy;
  }


  private void initNodeIDBytes(int nodeIDInt) {
    nodeIDByteArr = new byte[4];
    Convert.writeIntToByteArr(nodeIDByteArr, 0, nodeIDInt);
  } // initNodeIDBytes

  private void updateSeed() {
    if (seedGenerator.seedAvailable()) { // new entropy
      secRandom.setSeed(seedGenerator.getSeed());
    }
  } // updateSeed()

  /**
   * Fills passed byte array with lowest 6 bytes from the nano time
   * @param forEncodingByteArr The byte array where to fill cut time
   * @param base64nanoTimeBytesLen Starting offset in the array
   * @throws UnsupportedEncodingException
   */
  private void fillBytesFromNanos(byte[] forEncodingByteArr, int startOffset) throws UnsupportedEncodingException {
    // TODO - initially milliseconds are get and when JDK 1.5 transition are made will be used System.nanoTime()
    long currentTimeNanos = System.currentTimeMillis();
    int lowerInt = (int) currentTimeNanos;
    short next2HigherBytes = (short)(currentTimeNanos >> 32);

    Convert.writeIntToByteArr(forEncodingByteArr, startOffset, lowerInt);
    Convert.writeShortToByteArr(forEncodingByteArr, startOffset + 4, next2HigherBytes);
  } // fillBytesFromNanos

  protected HttpCookie setNewJsessionID(HttpParameters httpParameters){
    //HttpCookie cok = CookieParser.createSessionCookie(generateSessionID(httpParameters) + generatePrivatePartJSessionId(),
    HttpCookie cok = CookieParser.createSessionCookie(generateJSessionIdValue(),
      httpParameters.getRequest().getHost(), servletContextFacade.getWebApplicationConfiguration().getSessionCookieConfig());
    //addCookieForced(cok, httpParameters);
    addCookie(cok, httpParameters);
    httpParameters.setSessionCookie(true);
    HttpCookie sesCookie = httpParameters.getRequest().getSessionCookie(servletContextFacade.getWebApplicationConfiguration().isURLSessionTracking());
    if (sesCookie != null) {
      sesCookie.setValue(cok.getValue());
    }

    //todo dali da se vika?
    //addApplicationCookie(httpParameters);
    //return HttpHandler.NOOP;
    if (securityTraceLocation.beDebug()) {
      LogContext.getLocation(LogContext.LOCATION_SECURITY).trace("setNewJsessionID " + cok.getValue(), aliasName);
    }
    return cok;
  }

  private void sendRedirectWithNewJSESSIONID(HttpParameters httpParameters, HttpCookie cookie) {
    byte[] idFromUrl = ";jsessionid=".getBytes(); // it should be without whitespaces
    byte[] location = null;
    byte[] jsessionid = (cookie.getValue()).getBytes();
    MessageBytes fullUrl = httpParameters.getRequest().getRequestLine().getFullUrl();
    int ind = fullUrl.indexOf(idFromUrl);

    if(ind > -1) {
      location = fullUrl.getBytes();
      System.arraycopy(jsessionid, 0, location, ind+idFromUrl.length, jsessionid.length);
    } else {
      location = new byte[fullUrl.length() + jsessionid.length];
      System.arraycopy(fullUrl.getBytes(), 0, location, 0, fullUrl.length());
      System.arraycopy(jsessionid, 0, location, fullUrl.length(), jsessionid.length);
    }

    httpParameters.getRequestPathMappings().setServletName(null);
    if (securityTraceLocation.beDebug()) {
      securityTraceLocation.debugT("sendRedirectWithNewJSESSIONID " + new String(location));
    }
    httpParameters.redirect(location);
  }
}
