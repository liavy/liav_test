package com.sap.engine.services.servlets_jsp.server.deploy.util.jacc;

import com.sap.engine.lib.descriptors5.javaee.RoleNameType;
import com.sap.engine.lib.descriptors5.javaee.SecurityRoleType;
import com.sap.engine.lib.descriptors5.javaee.UrlPatternType;
import com.sap.engine.lib.descriptors5.web.AuthConstraintType;
import com.sap.engine.lib.descriptors5.web.SecurityConstraintType;
import com.sap.engine.lib.descriptors5.web.WebResourceCollectionType;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.tc.logging.Location;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class SecurityConstraintParser {
  private static Location currentLocation = Location.getLocation(SecurityConstraintParser.class);

  private String applicationName = null;
  private String aliasName = null;
  private PolicyConfiguration policyConfig = null;
  //private PolicyConfiguration additionalPolicyConfig = null;
  
  private SecurityConstraintType[] securityConstraintsArr = null;
  private SecurityRoleType[] secRolesType = null;

  private Hashtable allUrlPatternConstraints = new Hashtable();

  public SecurityConstraintParser(String applicationName, String aliasName, PolicyConfiguration policyConfig,
                       SecurityConstraintType[] securityConstraintsArr, SecurityRoleType[] secRolesType)
          throws WebDeploymentException {

    this.applicationName = applicationName;
    this.aliasName = aliasName;
    this.policyConfig = policyConfig;
    if (policyConfig == null) {
      throw new WebDeploymentException("Deploy error in web container while setting application security. Could not "
          + "get PolicyConfiguration for application {0} in web module {1}", new String[] { applicationName, aliasName });
    }
    
    this.securityConstraintsArr = securityConstraintsArr;
    this.secRolesType = secRolesType;
  }

  /**
   * Main method to create JavaACC permissions.
   *
   * @return Vector with deployment warnings;
   */
  public Vector createSecurityResourcesJACC() throws DeploymentException {
    try {
      processAllSecurityConstraints();
    } catch (PolicyContextException e) {
      throw new DeploymentException("Error setting security constraints for " + applicationName + " - " + aliasName, e);
    }

    // TODO - Policy.refresh
    return null; // warningsVec;
  }

  private void processAllSecurityConstraints() throws PolicyContextException {
    allUrlPatternConstraints.put(UrlPattern.SLASH, new UrlPatternConstraint(UrlPattern.SLASH));

    //iterate over security-constraint elements
    for (int i = 0; i < securityConstraintsArr.length; i++) {
      SecurityConstraintType secConstraint = securityConstraintsArr[i];
      AuthConstraintType authConstraintType = secConstraint.getAuthConstraint();
      WebResourceCollectionType[] webResourceCollectionTypeArr = secConstraint.getWebResourceCollection();

      String transportGuarantee = null;
      if (secConstraint.getUserDataConstraint() != null) {
        transportGuarantee = secConstraint.getUserDataConstraint().getTransportGuarantee().get_value();
      }

      String[] securityRoles = null;
      boolean urlPatternDenied = false; // is url-pattern denied, i.e. has existing but empty auth-constraint tag. This means - denied for all
      if (authConstraintType != null) {
        RoleNameType[] roleNameArr = authConstraintType.getRoleName();
        if (roleNameArr.length == 0) {
          urlPatternDenied = true; // has existing but empty auth-constraint tag. This means - denied for all
        } else  {
          int roleNameArrSize = roleNameArr.length;
          securityRoles = new String[roleNameArrSize];
          for (int j = 0; j < roleNameArrSize; j++) {
            securityRoles[j] = roleNameArr[j].get_value();
          }
        }
      }

      // web-resource-collection elements
      for (int webResCollIdx = 0; webResourceCollectionTypeArr != null && webResCollIdx < webResourceCollectionTypeArr.length; webResCollIdx++) {
        WebResourceCollectionType webResourceCollectionType = webResourceCollectionTypeArr[webResCollIdx];
        String[] httpMethods = webResourceCollectionType.getHttpMethod();
        if (httpMethods == null || httpMethods.length == 0) {
          httpMethods = null;
        }

        UrlPatternType[] urlPatternTypeArr = webResourceCollectionType.getUrlPattern();
        // url-pattern elements & http-method
        for (int urlIdx = 0; urlIdx < urlPatternTypeArr.length; urlIdx++) {
          UrlPatternType urlPattern = urlPatternTypeArr[urlIdx];
          String urlPatternValue = urlPattern.get_value();
          // Workaround for URLs that contain ":". They are not accepted by JACC API permission 
          // constructors but are allowed according to HTTP and RFCs
          urlPatternValue = urlPatternValue.replaceAll(":", "%3a"); 
          // allUrlPatternConstraints - hashtable container for mappings URLPatternStr:UrlPatternConstraintObj
          UrlPatternConstraint urlPatternConstraint = (UrlPatternConstraint)allUrlPatternConstraints.get(urlPatternValue);
          if (urlPatternConstraint == null) {
            urlPatternConstraint = new UrlPatternConstraint(urlPatternValue);
            allUrlPatternConstraints.put(urlPatternConstraint.getUrlPattern().getPattern(), urlPatternConstraint);
          }
          if (httpMethods == null) {
            urlPatternConstraint.setTransportGuarantee(HttpMethod.HTTP_METHODS_ALL, transportGuarantee);
            urlPatternConstraint.addHttpMethod(HttpMethod.HTTP_METHODS_ALL, urlPatternDenied);
            for (int k = 0; securityRoles != null && k < securityRoles.length; k++) {
              urlPatternConstraint.addHttpMethodToRoleMapping(HttpMethod.HTTP_METHODS_ALL, securityRoles[k]);
            }
          } else {
            for (int j = 0; j < httpMethods.length; j++) {
              urlPatternConstraint.setTransportGuarantee(httpMethods[j], transportGuarantee);
              urlPatternConstraint.addHttpMethod(httpMethods[j], urlPatternDenied);
              for (int k = 0; securityRoles != null && k < securityRoles.length; k++) {
                urlPatternConstraint.addHttpMethodToRoleMapping(httpMethods[j], securityRoles[k]);
              }
            }
          }
        } // url-pattern elements
      } // web-resource-collection
    } // security-contraints

    processAllWebResourcesWithoutHttpMethods();

    String[] appSecurityRoles = getAppSecurityRoles();
    new JACCRulesGenerator(policyConfig).generateAndAddPermissionsIntoPolicyConfiguration(allUrlPatternConstraints, appSecurityRoles);
  } // processAllSecurityConstraints()

  /**
   * Apply restrictions {@link HttpMethod#HTTP_METHODS_ALL} to all other methods for each UrlPatternConstraint
   * These restrictions take precedence over other existing methods.
   */
  private void processAllWebResourcesWithoutHttpMethods() {
    Enumeration en = allUrlPatternConstraints.elements();
    while (en.hasMoreElements()) {
      UrlPatternConstraint nextUrlPatternConstraint = (UrlPatternConstraint) en.nextElement();
      HttpMethod http_methods_all = nextUrlPatternConstraint.getHttpMethod(HttpMethod.HTTP_METHODS_ALL);
      if (http_methods_all != null) {
        for (int i = 0; i < HttpMethod.STANDARD_HTTP_METHODS.length; i++) {
          nextUrlPatternConstraint.setTransportGuarantee(HttpMethod.STANDARD_HTTP_METHODS[i], http_methods_all.getTransportGuarantee());
          nextUrlPatternConstraint.addHttpMethod(HttpMethod.STANDARD_HTTP_METHODS[i], http_methods_all.isMethodDenied());
          for (int k = 0; http_methods_all.getAllRoles() != null && k < http_methods_all.getAllRoles().length; k++) {
            nextUrlPatternConstraint.addHttpMethodToRoleMapping(HttpMethod.STANDARD_HTTP_METHODS[i], http_methods_all.getAllRoles()[k]);
          }
        }
        HttpMethod[] nonStandardHttpMethods = nextUrlPatternConstraint.getAllNonStarndardMethods();
        for (int i = 0; nonStandardHttpMethods != null && i < nonStandardHttpMethods.length; i++) {
          if (!http_methods_all.equals(nonStandardHttpMethods[i])) {
            nextUrlPatternConstraint.setTransportGuarantee(nonStandardHttpMethods[i].getMethodName(), http_methods_all.getTransportGuarantee());
            nextUrlPatternConstraint.addHttpMethod(nonStandardHttpMethods[i].getMethodName(), http_methods_all.isMethodDenied());
            for (int k = 0; http_methods_all.getAllRoles() != null && k < http_methods_all.getAllRoles().length; k++) {
              nextUrlPatternConstraint.addHttpMethodToRoleMapping(nonStandardHttpMethods[i].getMethodName(), http_methods_all.getAllRoles()[k]);
            }
          }
        }
      }
    }
  }

  private String[] getAppSecurityRoles() {
    if (secRolesType == null) {
      return null;
    }
    String[] securityRoles = new String[secRolesType.length];
    for (int i = 0; i < secRolesType.length; i++) {
      securityRoles[i] = secRolesType[i].getRoleName().get_value();
    }
    return securityRoles;
  }
}
