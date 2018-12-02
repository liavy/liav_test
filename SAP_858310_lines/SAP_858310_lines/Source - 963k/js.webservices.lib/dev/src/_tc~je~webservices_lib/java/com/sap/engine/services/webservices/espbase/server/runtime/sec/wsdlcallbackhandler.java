package com.sap.engine.services.webservices.espbase.server.runtime.sec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.LanguageCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.interfaces.security.auth.AuthStateCallback;
import com.sap.engine.lib.security.PasswordChangeCallback;
import com.sap.engine.lib.security.http.HttpCallback;
import com.sap.engine.lib.security.http.HttpGetterCallback;
import com.sap.engine.lib.xml.util.BASE64Decoder;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;



/**
 * WSDLCallbackHandler passes the needed security information to be validated against
 * a stack of jaas modules(http basic auth module, sso module, x509 module).
 * It extracts the security information from HttpServletRequest and passes it to the correct handlers. 
 * @author I056242
 *
 */
public class WSDLCallbackHandler implements CallbackHandler {
  
    
    private static final String BASIC_AUTH_METHOD = "Basic";

    private static final String SAP_LANGUAGE = "sap-language";

    private static Locale loc = null;
    
    private Map<String, Cookie> cookies;

    private char[] basicAuthenticationPassword = null;

    private String basicAuthenticationUsername = null;

    private HttpServletRequest request;

    private ApplicationServiceContext serviceContext;
          
    /** Logger instance */
    private Location log = Location.getLocation(WSDLCallbackHandler.class);

    
    
    public WSDLCallbackHandler(HttpServletRequest request, ApplicationServiceContext serviceContext, SecurityContext policyContext){      
      this.request = request;
      this.serviceContext = serviceContext;                  
    }
    

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
      
    for (Callback currentCallback : callbacks) {
      if (currentCallback instanceof HttpGetterCallback) {
        handleHttpGetterCallback(currentCallback);
      } else if (currentCallback instanceof AuthStateCallback){
        handleAuthStateCallback(currentCallback);
      } else if (currentCallback instanceof NameCallback) {
        handleNameCallback(currentCallback);
      } else if (currentCallback instanceof PasswordCallback && !(currentCallback instanceof PasswordChangeCallback)) {
        handlePasswordCallback(currentCallback);
      } else if (currentCallback instanceof LanguageCallback) {
        handleLanguageCallback(currentCallback);
      } else {
        UnsupportedCallbackException unsupportedCallbackException = new UnsupportedCallbackException(currentCallback);
        throw unsupportedCallbackException;
      }
    }// for
  }
    
    
    
    /**
     * Will be used later for authentication purposes. 
     * @param currentCallback
     */
    protected void handleAuthStateCallback(Callback currentCallback) {
      // AuthStateCallback authCallBack = (AuthStateCallback) currentCallback;
      /**
      * FIXME: For now the method implementation is pending. Stays here and in the handle() method
      * to avoid UnsupportedPassportCallbackException.
      */     
    }

    
    
    
    
    /**
     * Standart callback to determine the locale settings.
     * @param currentCallback
     */
    protected void handleLanguageCallback(Callback currentCallback) {
      
      if (loc == null) {
        // ISO-639-1 2 letter code, by default "en"
        String code = serviceContext.getServiceState().getProperty("authentication.language", "en");
        try {
          loc = new Locale(code);
        } catch (Exception ex) {
          log.traceThrowableT(Severity.DEBUG, "Can not create java.util.locale.Locale with code " + code, ex);
          loc = Locale.ENGLISH;
        }
      }
      
      Locale toRet = loc;
      String str = request.getHeader(SAP_LANGUAGE);
      try {
        if (str != null) {
          toRet = new Locale(str);
        }
      } catch (Exception e) {
        log.traceThrowableT(Severity.DEBUG, "Can not create java.util.locale.Locale with code " + str, e);
      }
      ((LanguageCallback) currentCallback).setLocale(toRet);
    }
    


    /**
     * Handles the X509 tokens and SSO cookies here.
     */
    protected void handleHttpGetterCallback(Callback currentCallback) {
    HttpGetterCallback httpGetterCallback = (HttpGetterCallback) currentCallback;

    /**
     * Authentication over SSL
     */
    if (request.isSecure()) {
      Object cert = request.getAttribute("javax.servlet.request.X509Certificate");
      if (cert != null) {
        httpGetterCallback.setValue(cert);
      }
    }

    /**
     * Read data from cookie
     */
    if (httpGetterCallback.getType() == HttpCallback.COOKIE) {
      String name = httpGetterCallback.getName();
      Cookie cookie = (Cookie) getCookies().get(name);
      if (cookie != null) {
        httpGetterCallback.setValue(cookie.getValue());
      }
    }
    
    /**
     * Read data from http header
     */
    if (httpGetterCallback.getType() == HttpCallback.HEADER) {
      String name = httpGetterCallback.getName();
      if (request.getHeader(name) != null) {
        httpGetterCallback.setValue(request.getHeader(name));
      }
    }
  }//handleHttpGetterCallback

  
    /**
     * Gives the basic auth password here if it is present.
     * @param currentCallback
     */
    protected void handlePasswordCallback(Callback currentCallback) {
    PasswordCallback passwordCallback = (PasswordCallback) currentCallback;

    /**
     * basic authentication
     */
    if (hasBasicAuthenticationData()) {
      passwordCallback.setPassword(basicAuthenticationPassword);
    }
  }//handlePasswordCallback

    
    protected void handleNameCallback(Callback currentCallback) {
    NameCallback nameCallback = (NameCallback) currentCallback;
    if (nameCallback.getName() == null || "".equals(nameCallback.getName())) {
      /**
       * basic authentication
       */
      if (hasBasicAuthenticationData()) {
        ((NameCallback) currentCallback).setName(basicAuthenticationUsername);
      }
    }//if callback is empty
  }//handleNameCallback


    /**
     * Check for basic authentication data
     * 
     * @param request
     * @return
     */
    private boolean hasBasicAuthenticationData() {
      if (basicAuthenticationUsername == null)
        initBasicCredentials();
      return (basicAuthenticationUsername != null);
    }

    /**
     * Read the basic authorization data and store it
     * 
     * @param request
     */
    private void initBasicCredentials() {
      String[] authHeader = parseAuthHeader(request.getHeader("Authorization"));

      if (authHeader != null) {
        this.basicAuthenticationUsername = authHeader[0];
                
        if (this.basicAuthenticationPassword == null) {
          this.basicAuthenticationPassword = authHeader[1].toCharArray();        
        }
      }
    }

    /**
     * Parse the basic authorization header
     * 
     * @param authorization
     * @return
     */
    private String[] parseAuthHeader(String authorization) {

      if (authorization == null) {
        return null;
      }

      // assuming authorization header like this:
      // Authorization: Method<space>data
      int startAuthSub = authorization.indexOf(' ');

      // no method found
      if (startAuthSub <= 0) {
        return null;
      }

      String method = authorization.substring(0, startAuthSub);

      if (BASIC_AUTH_METHOD.equals(method)) {
        String credentialsString = authorization.substring(authorization.indexOf(' ') + 1);

        try {
          credentialsString = new String(BASE64Decoder.decode(credentialsString.getBytes())); //$JL-I18N$
        } catch (Exception e) {
          log.traceThrowableT(Severity.DEBUG, "Can not decode basic auth header string.", e);
          return null;
        }

        String[] credentials = new String[2];
        credentials[0] = credentialsString.substring(0, credentialsString.indexOf(':'));
        credentials[1] = credentialsString.substring(credentialsString.indexOf(':') + 1);

        return credentials;
      }

      return null;
    }

    /**
     * Parses cookies fromt the request and puts them into a map.
     * @return
     */      
    private Map<String, Cookie> getCookies() {
      if (this.cookies == null) {
        this.cookies = new HashMap<String, Cookie>();
        Cookie[] requestCookies = request.getCookies();

        if (requestCookies != null) {
          for (int i = 0; i < requestCookies.length; i++) {
            this.cookies.put(requestCookies[i].getName(), requestCookies[i]);
          }
        }
        return cookies;
      } else
        return cookies;
    }

}



