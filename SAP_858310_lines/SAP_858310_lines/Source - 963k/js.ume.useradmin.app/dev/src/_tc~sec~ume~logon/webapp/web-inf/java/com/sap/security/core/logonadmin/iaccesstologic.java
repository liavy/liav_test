package com.sap.security.core.logonadmin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Enumeration;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import javax.servlet.http.Cookie;

import com.sap.security.api.IUser;
import com.sap.security.api.logon.IAuthScheme;

public interface IAccessToLogic {

  public static final String ENV_LOGONSERVLET         = "logonservlet";
  public static final String ENV_LOGONCERTSERVLET     = "logoncertservlet";
  public static final String ENV_LOGONCOMPONENT       = "logoncomponent";
  public static final String ENV_LOGONCERTCOMPONENT   = "logoncertcomponent";

  public Object getSessionAttribute(String attrName);
  public Object getRequestAttribute(String attrName);

  public void setSessionAttribute(String attrName, Object AttrValue);
  public void setRequestAttribute(String attrName, Object AttrValue);

  public boolean isSessionNew();

  public Object getSession();
  public Object getSession(boolean flag);

  public String getRequestParameter(String parName);

  public void gotoPage(String page) throws AccessToLogicException, IOException;

  public String getContextURI();

  public String getRequestURI();

  public String getSessionId();

  public void setResponseContentType(String s);

  public void setRequestCharacterEncoding(String s) throws UnsupportedEncodingException;

  public String getRequestHeader(String s);

  public void setResponseHeader(String s, String val);

  public PrintWriter getResponseWriter() throws IOException;

  public Enumeration getRequestParameterNames();

  public Enumeration getRequestAttributeNames();

  public Enumeration getSessionAttributeNames();

  public String[] getRequestParameterValues(String s);

  public String getRequestCharacterEncoding();

  public IUser getActiveUser();
  public Subject logon(String s) throws LoginException;
  public void logout();

  public void removeSessionAttribute(String att);

  public Locale getRequestLocale();

  public void sendRedirect(String url) throws IOException;

  public void sessionInvalidate();

  public String getQueryString();

  //MPW NEW
  public String getServerName();
  public String getAlias(String alias);
  public String getAlias(String context, String event);
  public String getAlias(String context, String event, String redirectURL);
  //public String getEventData( String event, String key );
  public String getRequiredAuthScheme();
  public IAuthScheme[] getAuthSchemes();
  public boolean isAction(String s);
  public String getEnvironment();
  
  //GWR new!
  public void setCookie(Cookie cookie);
}