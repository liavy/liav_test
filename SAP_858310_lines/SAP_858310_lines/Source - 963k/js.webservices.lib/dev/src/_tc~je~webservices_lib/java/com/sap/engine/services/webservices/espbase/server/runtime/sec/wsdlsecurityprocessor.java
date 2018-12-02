package com.sap.engine.services.webservices.espbase.server.runtime.sec;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.servlet.http.HttpServletRequest;
import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.core.thread.ThreadContext;
import com.sap.engine.frame.core.thread.ThreadSystem;
import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.interfaces.security.SecurityContextObject;
import com.sap.engine.interfaces.security.SecuritySession;
import com.sap.tc.logging.Location;

/**
 * WSDLSecurityUtil encapsulates the logic for creation a JAAS module login stack which will authenticate the 
 * http wsdl request. It also authenticates a given http request against these modules.
 * @author I056242
 *
 */
public class WSDLSecurityProcessor {
  
  public static final String WSDL_SECURITY_PROPERTY = "wsdl.security";
        
  private static final String WSS_LOGIN_STACK_POLICY = "BASIC*SSO2*X509*SAML*ws";
       
  private static Location log = Location.getLocation(WSDLSecurityProcessor.class);  
      
  private static boolean secureWSDL = false;
    
  
  /**
   * Set if the wsdl security is enabled.
   * @param wsdlSecurity
   */
  public static void setSecureWsdl(String wsdlSecurity) {    
    try{
      secureWSDL = Boolean.parseBoolean(wsdlSecurity);        
    }catch (Exception e) {
      log.debugT("The value of " + WSDL_SECURITY_PROPERTY + " property: " + wsdlSecurity
          + " can not be parsed to boolean value.");
      secureWSDL = false;
    }
  }

  /**
   * Check if the wsdl security is enables.
   * @return
   */
  public static boolean isSecureWsdl() {
    return secureWSDL;
  }

 
  /**
   * Authenticate the given http request against the wsdl JAAS module stack (basic auth, sso, x509).
   * 
   * @param security
   * @param policyName
   * @return
   * @throws Exception 
   */
  public static Subject authenticate(HttpServletRequest request, ApplicationServiceContext serviceContext)
      throws Exception {
            
    SecurityContext securityContext = (SecurityContext) serviceContext.getContainerContext().getObjectRegistry().getProvidedInterface("security");
    
    String policyName = WSS_LOGIN_STACK_POLICY;

    // Check if a security session already exists.
    String authenticationConfiguration = getSecuritySession(serviceContext).getAuthenticationConfiguration();

    if (authenticationConfiguration == null) {
      /**
       * login is needed
       */

      LoginContext login = null;

      SecurityContext policyContext = securityContext.getPolicyConfigurationContext(policyName);

      //Create the callback which will parse the http request and give the modules the neede authentication information.
      CallbackHandler handler = new WSDLCallbackHandler(request, serviceContext, policyContext);

      if (policyContext == null) {
        throw new Exception("Policy " + policyName + " can no be obtained."); 
      }

      login = policyContext.getAuthenticationContext().getLoginContext(null, handler);
      if (login == null) {
        throw new Exception("Login can not be created for policy: " + policyName);
      }
      
      // Authenticate.
      login.login();

      return login.getSubject();
    } else {
      return getSecuritySession(serviceContext).getSubject();
    }
  }

  /**
   * Get security session if it exists.
   * @param serviceContext
   * @return
   */
  private static SecuritySession getSecuritySession(ApplicationServiceContext serviceContext) {
    ThreadSystem threadSystem = serviceContext.getCoreContext().getThreadSystem();
    ThreadContext threadContext = threadSystem.getThreadContext();
    return ((SecurityContextObject) threadContext.getContextObject(threadSystem.getContextObjectId("security"))).getSession();
  }

}
