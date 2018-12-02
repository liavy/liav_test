/*
 * Copyright (c) 2004-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebRoleRefPermission;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.interfaces.security.JACCSecurityRoleMappingContext;
import com.sap.engine.interfaces.security.JACCUndeployContext;
import com.sap.engine.interfaces.security.JACCUpdateContext;
import com.sap.engine.interfaces.security.ModificationContext;
import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.interfaces.security.SecurityRole;
import com.sap.engine.interfaces.security.UpdateSecurityContext;
import com.sap.engine.lib.descriptors.webj2eeengine.SecurityRoleMapType;
import com.sap.engine.lib.descriptors5.javaee.SecurityRoleType;
import com.sap.engine.lib.descriptors5.web.LoginConfigType;
import com.sap.engine.lib.descriptors5.web.SecurityConstraintType;
import com.sap.engine.lib.descriptors5.web.ServletType;
import com.sap.engine.services.deploy.container.AppConfigurationHandler;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.ear.common.EqualUtils;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.ActionBase;
import com.sap.engine.services.servlets_jsp.server.deploy.UpdateAction;
import com.sap.engine.services.servlets_jsp.server.deploy.WebContainerHelper;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.util.jacc.SecurityConstraintParser;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebResourceAccessor;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebWarningException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public class SecurityUtils {
  private final static Location currentLocation = Location.getLocation(SecurityUtils.class);
  private final static Location traceLocation = LogContext.getLocationSecurity();
  
  private boolean isDebugTracing = traceLocation.beDebug(); // updated on each createSecurityResources
  private static PolicyConfigurationFactory umeFactory = null;

  /**
   */
  public SecurityUtils() {
  }// end of constructor

  /**
   * @param applicationName
   * @param aliasCanonicalized canonicalized alias
   * @param appConfig
   * @param globalWebDesc
   * @param webDesc
   * @param warnings
   * @throws DeploymentException
   */
  public void createSecurityResources(String applicationName, String aliasCanonicalized, Configuration appConfig, AppConfigurationHandler appConfigurationHandler,
                                      WebDeploymentDescriptor globalWebDesc, WebDeploymentDescriptor webDesc, Vector warnings, boolean newAlias) throws DeploymentException {
    String aliasDirName = WebContainerHelper.getAliasDirName(aliasCanonicalized);

    if (globalWebDesc == null || webDesc == null) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_LOAD_DESCRIPTOR_FOR_WEBXML_FOR_WEB_APPLICATION, new Object[]{aliasDirName});
    }

    // Init security roles and security role maps
    Vector secRolesVec = new Vector();
    HashMap secRolesMap = new HashMap();
    // Init security resources from global-web.xml
    initSecurityRoles(globalWebDesc, secRolesVec, secRolesMap);
    // Init security resources from local web.xml
    initSecurityRoles(webDesc, secRolesVec, secRolesMap);
    SecurityRoleType[] secRoles = (SecurityRoleType[]) secRolesVec.toArray(new SecurityRoleType[secRolesVec.size()]);

    // Init servlets in order to be processed run as and security role refs.
    Vector servletDescVec = new Vector();
    initServlets(globalWebDesc, servletDescVec);
    initServlets(webDesc, servletDescVec);
    ServletType[] servletDesc = (ServletType[]) servletDescVec.toArray(new ServletType[servletDescVec.size()]);

    // Init security constraints
    Vector securityConstraintsVec = new Vector();
    initSecurityConstraints(globalWebDesc, securityConstraintsVec);
    initSecurityConstraints(webDesc, securityConstraintsVec);
    SecurityConstraintType[] securityConstraints = (SecurityConstraintType[]) securityConstraintsVec.toArray(new SecurityConstraintType[securityConstraintsVec.size()]);

    // begin JACC part
    String policyConfigID = getPolicyConfigurationID(applicationName, aliasDirName);
    PolicyConfiguration policyConfig = SecurityUtils.getPolicyConfiguration(policyConfigID, 
        true /* removePrevPolicyConfig */, applicationName);
    if (policyConfig == null) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_POLICY_CONFIGURATION, new String[]{aliasCanonicalized, applicationName, policyConfigID});
    }
    isDebugTracing = traceLocation.beDebug(); 
    if (isDebugTracing) {
    	traceLocation.debugT("PolicyConfiguration for canon. alias [" + aliasCanonicalized + "] initialized.");
    }
    
    JavaACCParser javaACCParser = new JavaACCParser(applicationName, aliasDirName, policyConfigID, secRoles, servletDesc);
    javaACCParser.createSecurityRoleRefs(true, policyConfig);

    SecurityConstraintParser securityConstraintParser = new SecurityConstraintParser(applicationName, aliasDirName, policyConfig, securityConstraints, secRoles);
    securityConstraintParser.createSecurityResourcesJACC();

    try {
      policyConfig.commit();
    } catch (PolicyContextException e) {
      throw new WebDeploymentException( WebDeploymentException.DEPLOY_JACC_ERROR,
            new Object[] { applicationName, aliasCanonicalized, "JACC commit error."}, e);
    }
    // end of JACC part
    
    // Get security configuration - created new if needed 
    SecurityContext appSecurityContext = createSecurityConfiguration(applicationName, aliasDirName, appConfig, appConfigurationHandler);

    if (newAlias) {
      // SET NEW authentication stack and related settings directly
      
      // Initialize security configuration from global-web.xml
      initSecurityConfiguration(appSecurityContext, aliasDirName, globalWebDesc);
      // Initialize security configuration from local web.xml
      initSecurityConfiguration(appSecurityContext, aliasDirName, webDesc);

      JACCSecurityRoleMappingContext jACCSecurityRoleMappingContext = appSecurityContext.getJACCSecurityRoleMappingContext();

      try {//ACCOUNTING.start - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.beginMeasure("SecurityUtils.createSecurityResources/JACCSecurityRoleMappingContext.addUMERoleToJACCRole", JACCSecurityRoleMappingContext.class);
        }//ACCOUNTING.start - END
        
        for (int i = 0; secRoles != null && i < secRoles.length; i++) {
          String roleName = secRoles[i].getRoleName().get_value();
          SecurityRoleMapType temp = (SecurityRoleMapType) secRolesMap.get(roleName);
          if (temp != null && temp.getServerRoleName() != null) {
            String[] serverRoleNames = temp.getServerRoleName();
            for (int j = 0; j < serverRoleNames.length; j++) {
              try {
                jACCSecurityRoleMappingContext.addUMERoleToJACCRole(roleName, policyConfigID, serverRoleNames[j]);
                //jACCSecurityRoleMappingContext.addUMERoleToJACCRole(roleName, policyConfigID + "-EXTERNAL", serverRoleNames[j]);
              } catch (OutOfMemoryError e) {
                throw e;
              } catch (ThreadDeath e) {
                throw e;
              } catch (Throwable e) {
                warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
                  WebWarningException.ERROR_IN_SECURITY_MAPPINGS_IN_DESCRIPTOR_IN_WEB_APPLICATION_TO_DEFAULT_SERVER_SECURITY_MAPPINGS_ERROR_IS,
                  new Object[]{aliasDirName, e.toString()}));
              }
            }
          }
        }
      
      } finally {//ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure("SecurityUtils.createSecurityResources/JACCSecurityRoleMappingContext.addUMERoleToJACCRole");        
        }
      }//ACCOUNTING.end - END
      
    } else {
      // UPDATE authentication stack, roles and related settings - there are previous(source) and new (target) settings
      
      // get and remove descriptor from Hashtable in order to free memory usage
      final WebDeploymentDescriptor oldWebDescr = UpdateAction.aliasToOldDescrMap.remove(aliasCanonicalized);
      if (isDebugTracing) {
        traceLocation.debugT("Starting update of existing alias. oldWebDesc: [" + oldWebDescr +"].");
      }
      UpdateSecurityContext updateContext = appSecurityContext.getUpdateSecurityContext();
      if (isDebugTracing) {
        traceLocation.debugT("Update security context got.");
      }
      updateContext.setSecurityContext(appSecurityContext);
      
      // assuming for global web descriptor that it is the same for old and updated app. No way to track possible changes
      updateAuthenticationConfiguration(applicationName, aliasCanonicalized, updateContext, 
          globalWebDesc, oldWebDescr, webDesc);
      
      //update security role mappings for updated aliases
      HashMap currentSecRolesMap = secRolesMap;
      JACCUpdateContext jaccUpdateContext = ServiceContext.getServiceContext().getSecurityContext().getJACCContext(policyConfigID).getUpdateContext();
      HashMap previousSecRolesMap = (HashMap) ActionBase.mapAliasSecurityRes.remove(aliasCanonicalized);
      
      Iterator prevIter = previousSecRolesMap.keySet().iterator();
      while (prevIter.hasNext()) {
        try {
          String role = (String) prevIter.next();
          SecurityRoleMapType roleMap = (SecurityRoleMapType) previousSecRolesMap.get(role);

          if (currentSecRolesMap.containsKey(role)) {
            SecurityRoleMapType tempRoleMap = (SecurityRoleMapType) currentSecRolesMap.get(role);
            if (roleMap.getServerRoleName() != null) {
              if (tempRoleMap.getServerRoleName() != null) {
                if (!EqualUtils.equalUnOrderedArrays(roleMap.getServerRoleName(), tempRoleMap.getServerRoleName())) {
                  //mapping was changed
                  jaccUpdateContext.jaccRoleMappingsChanged(role, tempRoleMap.getServerRoleName());
                }
              } else {
                //mapping is deleted
                jaccUpdateContext.jaccRoleRemoved(role);
              }
            } else {
              //mapping is added
              if (tempRoleMap.getServerRoleName() != null) {
                jaccUpdateContext.jaccRoleAdded(role, tempRoleMap.getServerRoleName());
              }
            }
          } else {
            //the role is deleted
            jaccUpdateContext.jaccRoleRemoved(role);
          }

          currentSecRolesMap.remove(role);
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
          warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            WebWarningException.ERROR_IN_SECURITY_MAPPINGS_IN_DESCRIPTOR_IN_WEB_APPLICATION_TO_DEFAULT_SERVER_SECURITY_MAPPINGS_ERROR_IS,
            new Object[]{aliasDirName, e.toString()}));
        }
      }

      //what is left of security roles is added security roles
      Iterator currIter = currentSecRolesMap.keySet().iterator();
      while (currIter.hasNext()) {
        try {
          String role = (String) currIter.next();
          SecurityRoleMapType roleMap = (SecurityRoleMapType) currentSecRolesMap.get(role);
          if (roleMap.getServerRoleName() != null) {
            jaccUpdateContext.jaccRoleAdded(role, roleMap.getServerRoleName());
          }
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
          warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            WebWarningException.ERROR_IN_SECURITY_MAPPINGS_IN_DESCRIPTOR_IN_WEB_APPLICATION_TO_DEFAULT_SERVER_SECURITY_MAPPINGS_ERROR_IS,
            new Object[]{aliasDirName, e.toString()}));
        }
      }
    } 
  }//end of createSecurityResources(String applicationName, String aliasCanonicalized, Configuration appConfig, AppConfigurationHandler appConfigurationHandler,
   //                               WebDeploymentDescriptor globalWebDesc, WebDeploymentDescriptor webDesc, Vector warnings)

  /**
   * Used to notify {@link UpdateSecurityContext} for previous and new descriptors in order to identify 
   * custom authentication changes
   * @param globalDescr global descriptor
   */
  private void updateAuthenticationConfiguration(String applicationName, String aliasCanonicalized,
      UpdateSecurityContext updateContext, WebDeploymentDescriptor globalDescr, 
      WebDeploymentDescriptor oldDescr, WebDeploymentDescriptor newDescr) {
    if (traceLocation.bePath()) {
      traceLocation.pathT(" starting updateAuthnticationConfiguration([" + applicationName + "], [" + aliasCanonicalized + "], " 
          + updateContext + ")");
    }
    boolean updateAuthenticationPassed = false; // final status
    try {
      // previous version - OLD descriptor
      // authentication stack - login modules configuration
      AppConfigurationEntry[] appCfgEntries = oldDescr.getLoginModules(); 
      if (appCfgEntries != null) {
        if (isDebugTracing) {
          traceLocation.debugT("Will set previous auth stack of " +  appCfgEntries.length + " elements.");
          traceLocation.debugT("  First element " +  appCfgEntries[0].getLoginModuleName());
        }
        updateContext.setDefaultSourceAuthenticationStack(appCfgEntries);
      } else { 
        // oldDescr.loginModules == null
        if (isDebugTracing) {
          traceLocation.debugT("Previous local descriptor auth stack is not set.");
        }
        // check for local auth_method
        if ( oldDescr.getLoginConfig() != null && oldDescr.getLoginConfig().getAuthMethod() != null) {
          // tag exists but may be null
          String value = oldDescr.getLoginConfig().getAuthMethod().get_value();
          if (value != null) {
            value = value.toLowerCase();
          }
          if (isDebugTracing) {
            traceLocation.debugT("Will set previous local auth method [" +  value + "].");
          }
          updateContext.setDefaultSourceAuthenticationTemplate(value);
        } else { 
          if (isDebugTracing) {
            traceLocation.debugT("Previous local auth method is not set. Initiate checking for global values.");
          }
          // oldDescr.getLoginModules() == null && oldDescr.getLoginConfig() == null
          AppConfigurationEntry[] appConfigurationEntries = globalDescr.getLoginModules();
          if (appConfigurationEntries != null) {
            if (isDebugTracing) {
              traceLocation.debugT("Will set source auth stack of " +  appConfigurationEntries.length + " elements taken from global descriptor.");
              traceLocation.debugT("  First element " +  appConfigurationEntries[0].getLoginModuleName());
            }
            updateContext.setDefaultSourceAuthenticationStack(appConfigurationEntries);
          } else {
            if (isDebugTracing) {
              traceLocation.debugT("Global descriptor auth stack is not set null.");
            }
            LoginConfigType globalLoginConfig = globalDescr.getLoginConfig(); 
            if (globalLoginConfig != null && globalLoginConfig.getAuthMethod() != null) {
              // tag exists but may be null
              String value = oldDescr.getLoginConfig().getAuthMethod().get_value();
              if (value != null) {
                value = value.toLowerCase();
              }
              if (isDebugTracing) { 
                traceLocation.debugT("Will set previous global auth method [" +  value + "] .");
              }
              updateContext.setDefaultSourceAuthenticationTemplate(value);
            } else { // not expected as general
              traceLocation.infoT("Even global auth. method not found. No source auth. info set.");
            }
          }
        }
      } // // oldDescr.loginModules == null
      // initially source properties are ignored
      if (isDebugTracing) {
        traceLocation.debugT("Processing source properties from global descriptor.");
      }
      updateAuthProperties(updateContext, globalDescr, true /*isSource */, aliasCanonicalized );
      if (isDebugTracing) {
        traceLocation.debugT("Processing source properties from local descriptor");
      }
      updateAuthProperties(updateContext, oldDescr, true /*isSource */, aliasCanonicalized );
      
      // NEW descriptor version - target authentication settings
      appCfgEntries = newDescr.getLoginModules(); 
      if (appCfgEntries != null) {
        if (isDebugTracing) {
          traceLocation.debugT("Will set target auth stack of " +  appCfgEntries.length + " elements.");
          traceLocation.debugT("  First element " +  appCfgEntries[0].getLoginModuleName());
        }
        updateContext.setDefaultTargetAuthenticationStack(appCfgEntries);
      } else { 
        // newDescr.loginModules == null
        if (isDebugTracing) {
          traceLocation.debugT("Target local descriptor authentication stack is not set.");
        }
          
        // check for local auth_method
        if (newDescr.getLoginConfig() != null && newDescr.getLoginConfig().getAuthMethod() != null ) {
          // tag exists but may be null
          String value = newDescr.getLoginConfig().getAuthMethod().get_value();
          if (value != null) {
            value = value.toLowerCase();
          }
          if (isDebugTracing) {
            traceLocation.debugT("Will set target local auth method [" +  value + "].");
          }
          updateContext.setDefaultTargetAuthenticationTemplate(value);
        } else { 
          if (isDebugTracing) {
            traceLocation.debugT("Target local auth method is not set. Initiate checking for global values.");
          }
          AppConfigurationEntry[] appConfigurationEntries = globalDescr.getLoginModules();
          if (appConfigurationEntries != null) {
            if (isDebugTracing) {
              traceLocation.debugT("Will set target auth stack of " +  appConfigurationEntries.length + " elements taken from global descriptor.");
              traceLocation.debugT("  First element " +  appConfigurationEntries[0].getLoginModuleName());
            }
            updateContext.setDefaultTargetAuthenticationStack(appConfigurationEntries);
          } else {
            traceLocation.debugT("Global descriptor auth stack is not set.");
            LoginConfigType globalLoginConfig = globalDescr.getLoginConfig(); 
            if (globalLoginConfig != null && globalLoginConfig.getAuthMethod() != null) {
              // tag exists but may be null
              String value = newDescr.getLoginConfig().getAuthMethod().get_value();
              if (value != null) {
                value = value.toLowerCase();
              }
              if (isDebugTracing) {
                traceLocation.debugT("Will set previous global auth method [" +  value + "].");
              }
              updateContext.setDefaultTargetAuthenticationTemplate(value);
            } else {
              traceLocation.infoT("Even global auth. method not found. No source auth. info set.");
            }
          }
        }
      } // // newDescr.loginModules == null
      
      if (isDebugTracing) {
        traceLocation.debugT("Processing target properties from global descriptor.");
      }
      updateAuthProperties(updateContext, globalDescr, false /*isSource */, aliasCanonicalized );
      if (isDebugTracing) {
        traceLocation.debugT("Processing target properties from local descriptor.");
      }
      updateAuthProperties(updateContext, newDescr, false /*isSource */, aliasCanonicalized);
      
      if (isDebugTracing) {
        traceLocation.debugT("About to start commit configuration passed.");
      }
      updateContext.updateAuthentication();
      updateAuthenticationPassed = true;
      if (isDebugTracing) {
        traceLocation.pathT("Update security context: updateAuthentication() passed.");
      }
    } finally {
      if (isDebugTracing) {
        traceLocation.pathT("end of updateAuthnticationConfiguration([" + applicationName 
            + "], [" + aliasCanonicalized + "], " + updateContext + ") Update OK: " 
            + updateAuthenticationPassed);
      }
    }
  } // updateAuthenticationConfiguration()

  
  /**
   * Sets properties for security configuration update
   * @param updateContext
   * @param descriptor source(old version) or target(new one); local or global descriptor
   * @param isSorceDescriptor is previous or new version of descriptor
   * @param aliasCanonicalized
   */
  private void updateAuthProperties(UpdateSecurityContext updateContext, WebDeploymentDescriptor descriptor, 
      boolean isSorceDescriptor, String aliasCanonicalized) {
    String sourceOrTargetCfgStr = isSorceDescriptor ? "source" : "target";
    String policyDomain = null;
    if (descriptor.getWebJ2EEEngine().getLoginModuleConfiguration() != null) {
      // Set password change configuration
      if (descriptor.getWebJ2EEEngine().getLoginModuleConfiguration().getPasswordChangeConfig() != null) {
        String loginPage = descriptor.getWebJ2EEEngine().getLoginModuleConfiguration().getPasswordChangeConfig().getLoginPage();
        if (loginPage != null) {
          loginPage = ParseUtils.canonicalize(loginPage).replace('\\', ParseUtils.separatorChar);
          if (!loginPage.startsWith(ParseUtils.separator)) {
            loginPage = ParseUtils.separatorChar + loginPage;
          }
          if (isDebugTracing) {
            traceLocation.debugT("Seting " + sourceOrTargetCfgStr + " password change page [" + loginPage + "].");
          }
          if (isSorceDescriptor) {
            updateContext.setDefaultSourceAuthenticationProperty("password_change_login_page", loginPage);
          } else {
            updateContext.setDefaultTargetAuthenticationProperty("password_change_login_page", loginPage);
          }
        }

        String errorPage = descriptor.getWebJ2EEEngine().getLoginModuleConfiguration().getPasswordChangeConfig().getErrorPage();
        if (errorPage != null) {
          errorPage = ParseUtils.canonicalize(errorPage).replace('\\', ParseUtils.separatorChar);
          if (!errorPage.startsWith(ParseUtils.separator)) {
            errorPage = ParseUtils.separatorChar + errorPage;
          }
          if (isDebugTracing) {
            traceLocation.debugT("Seting " + sourceOrTargetCfgStr + " password change error page [" + errorPage + "].");
          }
          if (isSorceDescriptor) {
            updateContext.setDefaultSourceAuthenticationProperty("password_change_error_page", errorPage);
          } else {
            updateContext.setDefaultTargetAuthenticationProperty("password_change_error_page", errorPage);
          }
        }
      }
      policyDomain = descriptor.getWebJ2EEEngine().getLoginModuleConfiguration().getSecurityPolicyDomain();
    }

    // Set policy domain to the security context
    if (policyDomain == null || policyDomain.length() == 0) {
      policyDomain = "/" + aliasCanonicalized;
    }
    if (isDebugTracing) {
      traceLocation.debugT("Seting " + sourceOrTargetCfgStr + " policy_domain property to [" + policyDomain + "].");
    } 
    if (isSorceDescriptor) {
      updateContext.setDefaultSourceAuthenticationProperty("policy_domain", policyDomain);
    } else {
      updateContext.setDefaultTargetAuthenticationProperty("policy_domain", policyDomain);
    }

    if (descriptor.getLoginConfig() != null) {
      // Set authentication method to the security context
      if (descriptor.getLoginConfig().getAuthMethod() != null) {
        String value = descriptor.getLoginConfig().getAuthMethod().get_value();
        if (value != null) {
          value = value.toLowerCase();
        }
        if (isDebugTracing) {
          traceLocation.debugT("Seting " + sourceOrTargetCfgStr + " auth_method property to [" + value + "].");
        }
        if (isSorceDescriptor) {
          updateContext.setDefaultSourceAuthenticationProperty("auth_method", value);
        } else {
          updateContext.setDefaultTargetAuthenticationProperty("auth_method", value);
        }
      }

      // Set login configuration to the security context
      if (descriptor.getLoginConfig().getFormLoginConfig() != null) {
        if (descriptor.getLoginConfig().getFormLoginConfig().getFormLoginPage() != null) {
          String formLoginPage = descriptor.getLoginConfig().getFormLoginConfig().getFormLoginPage().get_value();
          formLoginPage = ParseUtils.canonicalize(formLoginPage).replace('\\', ParseUtils.separatorChar);
          if (!formLoginPage.startsWith(ParseUtils.separator)) {
            formLoginPage = ParseUtils.separatorChar + formLoginPage;
          }
          if (isDebugTracing) {
            traceLocation.debugT("Seting " + sourceOrTargetCfgStr + " form_login_page property to [" + formLoginPage + "].");
          }
          if (isSorceDescriptor) {
            updateContext.setDefaultSourceAuthenticationProperty("form_login_page", formLoginPage);
          } else {
            updateContext.setDefaultTargetAuthenticationProperty("form_login_page", formLoginPage);
          }
        }

        if (descriptor.getLoginConfig().getFormLoginConfig().getFormErrorPage() != null) {
          String formErrorPage = descriptor.getLoginConfig().getFormLoginConfig().getFormErrorPage().get_value();
          formErrorPage = ParseUtils.canonicalize(formErrorPage).replace('\\', ParseUtils.separatorChar);
          if (!formErrorPage.startsWith(ParseUtils.separator)) {
            formErrorPage = ParseUtils.separatorChar + formErrorPage;
          }
          if (isDebugTracing) {
            traceLocation.debugT("Seting " + sourceOrTargetCfgStr + " form_error_page property to [" + formErrorPage + "].");
          }
          if (isSorceDescriptor) {
            updateContext.setDefaultSourceAuthenticationProperty("form_error_page", formErrorPage);
          } else {
            updateContext.setDefaultTargetAuthenticationProperty("form_error_page", formErrorPage);
          }
        }
      }

      // Set realm name to the security context
      if (descriptor.getLoginConfig().getRealmName() != null) {
        String value = descriptor.getLoginConfig().getRealmName().get_value();
        if (isDebugTracing) {
          traceLocation.debugT("Seting " + sourceOrTargetCfgStr + " realm_name property to [" + value + "].");
        }
        if (isSorceDescriptor) {
          updateContext.setDefaultSourceAuthenticationProperty("realm_name", value);
        } else {
          updateContext.setDefaultTargetAuthenticationProperty("realm_name", value);
        }
      }
    }
  } // updateAuthProperties()

  public static void initSecurityRoles(WebDeploymentDescriptor webDesc, Vector secRoles, HashMap secRolesMap) {
    // Set roles from descriptors into roleMap (webapp-role:server-role)
    SecurityRoleType[] secRolesTemp = webDesc.getSecurityRoles();
    if (secRolesTemp != null) {
      for (int i = 0; i < secRolesTemp.length; i++) {
        SecurityRoleMapType temp = webDesc.getSecurityRoleFromAdditional(secRolesTemp[i]);
        if (temp != null) {
          secRolesMap.put(secRolesTemp[i].getRoleName().get_value(), temp);
        }
      }

      if (secRoles == null || secRoles.size() == 0) {
        // if there are not already set security roles
        // from previous invocation of the method (for global descriptor)
        secRoles.addAll(Arrays.asList(secRolesTemp));
      } else {
        // there are already set roles from previous invocation of this
        // method (global xml), so have to add and the new ones
        HashMap<String,SecurityRoleType> secRolesAll = new HashMap<String,SecurityRoleType>();
        for (int i = 0; i < secRoles.size(); i++) {
          secRolesAll.put(((SecurityRoleType) secRoles.get(i)).getRoleName().get_value(),(SecurityRoleType) secRoles.get(i));
        }

        for (int i = 0; i < secRolesTemp.length; i++) {
          secRolesAll.put(secRolesTemp[i].getRoleName().get_value(), secRolesTemp[i]);
        }

        secRoles.removeAllElements();
        secRoles.addAll(secRolesAll.values());
      }
    }
  }// end of initSecurityRoles(WebDeploymentDescriptor webDesc, SecurityRoleType[] secRoles, HashMap secRolesMap)

  public static void initServlets(WebDeploymentDescriptor webDesc, Vector servletDesc) {
    ServletType[] servletTypes = webDesc.getServlets();
    if (servletTypes != null) {
      if (servletDesc == null || servletDesc.size() == 0) {
        // global-web.xml case
        servletDesc.addAll(Arrays.asList(servletTypes));
      } else {
        // local web.xml case
        HashMap servletsAll = new HashMap();
        for (int i = 0; i < servletDesc.size(); i++) {
          servletsAll.put(((ServletType) servletDesc.get(i)).getServletName().get_value(), servletDesc.get(i));
        }

        for (int i = 0; i < servletTypes.length; i++) {
          servletsAll.put(servletTypes[i].getServletName().get_value(), servletTypes[i]);
        }

        servletDesc.removeAllElements();
        servletDesc.addAll(servletsAll.values());
      }
    }
  }// end of initServlets(WebDeploymentDescriptor webDesc, ServletType[] servletDesc)

  public static void initSecurityConstraints(WebDeploymentDescriptor webDesc, Vector securityConstraints) {
    SecurityConstraintType[] securityConstraintTypes = webDesc.getSecConstraints();
    if (securityConstraintTypes != null) {
      securityConstraints.addAll(Arrays.asList(securityConstraintTypes));
    }
  }// end of initSecurityConstraints(WebDeploymentDescriptor webDesc, SecurityConstraintType[] securityConstraints)

  /**
   * Gets SecurityContext for given application. Appropriate configurations are created if needed.  
   * @param applicationName
   * @param alias
   * @param appConfig
   * @param appConfigurationHandler
   * @return
   * @throws DeploymentException
   */
  private SecurityContext createSecurityConfiguration(String applicationName, String alias, Configuration appConfig, 
      AppConfigurationHandler appConfigurationHandler) throws DeploymentException {
    String aliasForSecurity = alias.replace('/', '_');
    aliasForSecurity = aliasForSecurity.replace('\\', '_');

    Configuration subConfig = null;
    try {
      subConfig = appConfig.getSubConfiguration(aliasForSecurity.replace(ParseUtils.separatorChar, File.separatorChar));
      if (subConfig == null) {
        subConfig = appConfig.createSubConfiguration(aliasForSecurity.replace(ParseUtils.separatorChar, File.separatorChar));
      }
    } catch (OutOfMemoryError ex) {
      throw ex;
    } catch (ThreadDeath ex) {
      throw ex;
    } catch (Throwable ex) {
      try {
        subConfig = appConfig.createSubConfiguration(aliasForSecurity.replace(ParseUtils.separatorChar, File.separatorChar));
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_CREATE_SECURITY_SUBCONFIGURATION_FOR_ALIAS_OF_APPLICATION,
          new Object[]{aliasForSecurity, applicationName}, e);
      }
    }

    ModificationContext securityModificationContext = ServiceContext.getServiceContext().getSecurityContext().getModificationContext();
    SecurityContext mySecContext = securityModificationContext.beginModifications(appConfigurationHandler, subConfig);
    SecurityContext appSecurityContext = mySecContext.getPolicyConfigurationContext(applicationName + "*" + aliasForSecurity);
    if (appSecurityContext == null) {
      try {
        mySecContext.registerPolicyConfiguration(applicationName + "*" + aliasForSecurity, SecurityContext.TYPE_WEB_COMPONENT);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        throw new WebDeploymentException(WebDeploymentException.CANNOT_REGISTER_POLICY_CONFIG,
          new Object[]{applicationName + "*" + aliasForSecurity}, e);
      }
      appSecurityContext = mySecContext.getPolicyConfigurationContext(applicationName + "*" + aliasForSecurity);
    }

    if (appSecurityContext == null) {
      String dcName = LoggingUtilities.getDcNameByClassLoader(mySecContext.getClass().getClassLoader());
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_APPLICATION_SECURITY_CONTEXT_FOR_WEB_APPLICATION, 
        new Object[]{dcName, LoggingUtilities.getCsnComponentByDCName(dcName), alias});
    } else {
      return appSecurityContext;
    }
  }// end of createSecurityConfiguration(String applicationName, String alias, Configuration appConfig, AppConfigurationHandler appConfigurationHandler)

  /**
   * Authentication/login modules related settings.
   * @param appSecurityContext
   * @param alias
   * @param webDesc
   */
  private void initSecurityConfiguration(SecurityContext appSecurityContext, String alias, WebDeploymentDescriptor webDesc) {
    String policyDomain = null;
    AppConfigurationEntry[] entries = null;
    
    if (webDesc.getWebJ2EEEngine().getLoginModuleConfiguration() != null) {
      // Set login modules from the additional deployment descriptor to the security context
      if (webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getLoginModuleStack() != null
        && webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getLoginModuleStack().getLoginModule() != null
        && webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getLoginModuleStack().getLoginModule().length > 0) {
        entries = webDesc.getLoginModules();
        appSecurityContext.getAuthenticationContext().setLoginModules(entries);
      }

      // Set password config to the security context
      if (webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getPasswordChangeConfig() != null) {
        String loginPage = webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getPasswordChangeConfig().getLoginPage();
        if (loginPage != null) {
          loginPage = ParseUtils.canonicalize(loginPage).replace('\\', ParseUtils.separatorChar);
          if (!loginPage.startsWith(ParseUtils.separator)) {
            loginPage = ParseUtils.separatorChar + loginPage;
          }
          appSecurityContext.getAuthenticationContext().setProperty("password_change_login_page", loginPage);
        }

        String errorPage = webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getPasswordChangeConfig().getErrorPage();
        if (errorPage != null) {
          errorPage = ParseUtils.canonicalize(errorPage).replace('\\', ParseUtils.separatorChar);
          if (!errorPage.startsWith(ParseUtils.separator)) {
            errorPage = ParseUtils.separatorChar + errorPage;
          }
          appSecurityContext.getAuthenticationContext().setProperty("password_change_error_page", errorPage);
        }
      }

      // Set policy domain to the security context
      policyDomain = webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getSecurityPolicyDomain();
    }

    if (policyDomain == null || policyDomain.length() == 0) {
      policyDomain = "/" + alias;
    }
    appSecurityContext.getAuthenticationContext().setProperty("policy_domain", policyDomain);

    if (webDesc.getLoginConfig() != null) {
      // Set auth method to the security context
      if (webDesc.getLoginConfig().getAuthMethod() != null) {
        appSecurityContext.getAuthenticationContext().setProperty("auth_method", webDesc.getLoginConfig().getAuthMethod().get_value().toLowerCase());
        if (entries == null || entries.length == 0) {
          appSecurityContext.getAuthenticationContext().setLoginModules(webDesc.getLoginConfig().getAuthMethod().get_value().toLowerCase());
        }
      }

      // Set login config to the security context
      if (webDesc.getLoginConfig().getFormLoginConfig() != null) {
        if (webDesc.getLoginConfig().getFormLoginConfig().getFormLoginPage() != null) {
          String formLoginPage = webDesc.getLoginConfig().getFormLoginConfig().getFormLoginPage().get_value();
          formLoginPage = ParseUtils.canonicalize(formLoginPage).replace('\\', ParseUtils.separatorChar);
          if (!formLoginPage.startsWith(ParseUtils.separator)) {
            formLoginPage = ParseUtils.separatorChar + formLoginPage;
          }
          appSecurityContext.getAuthenticationContext().setProperty("form_login_page", formLoginPage);
        }

        if (webDesc.getLoginConfig().getFormLoginConfig().getFormErrorPage() != null) {
          String formErrorPage = webDesc.getLoginConfig().getFormLoginConfig().getFormErrorPage().get_value();
          formErrorPage = ParseUtils.canonicalize(formErrorPage).replace('\\', ParseUtils.separatorChar);
          if (!formErrorPage.startsWith(ParseUtils.separator)) {
            formErrorPage = ParseUtils.separatorChar + formErrorPage;
          }
          appSecurityContext.getAuthenticationContext().setProperty("form_error_page", formErrorPage);
        }
      }

      // Set realm name to the security context
      if (webDesc.getLoginConfig().getRealmName() != null) {
        appSecurityContext.getAuthenticationContext().setProperty("realm_name", webDesc.getLoginConfig().getRealmName().get_value());
      }
    }
  }// end of initSecurityConfiguration(SecurityContext appSecurityContext, String alias, WebDeploymentDescriptor webDesc)

  /**
   * @param applicationName
   * @param aliasCanonicalized
   */
  public void removeSecurityResources(String applicationName, String aliasCanonicalized) {
    String policyConfigID = getPolicyConfigurationID(applicationName, aliasCanonicalized);

    ServiceContext.getServiceContext().getSecurityContext().getJACCContext(policyConfigID).getUndeployContext().undeployPolicyConfiguration();

    PolicyConfiguration policyConfig = SecurityUtils.getPolicyConfiguration(policyConfigID,
      true /* remove policy configuration */, applicationName);

    if (policyConfig != null) {
      try {
        policyConfig.commit();
      } catch (PolicyContextException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000019",
          "Attempt to commit removed security resources.", e, null, null);
      }
    }

    getUMEUndeployContext(policyConfigID).undeployPolicyConfiguration();
  }// end of removeSecurityResources(String applicationName, String alias)

  /**
   * <p/>
   * Generates the unique PolicyConfiguration ID of this application module i.e. (WAR with specific alias (context)
   * name).
   * </p>
   * <p/>
   * The default implementation is appName*escapedAliasName.
   *
   * @param applicationName
   * @param alias
   * @return the generated unique application module ID
   */
  public static String getPolicyConfigurationID(String applicationName, String alias) {
    // takes the root alias into consideration
    String aliasName = WebContainerHelper.getAliasDirName(ParseUtils.convertAlias(alias));
    // replaces path separator chars with underscores
    String aliasForSecurity = aliasName.replace(ParseUtils.separatorChar, '_');
    return applicationName + "*" + aliasForSecurity;
  } // end of getPolicyConfigurationID(String applicationName, String alias)


  /**
   * Merges and gets security-related info from global-web.xml and web.xml descriptors
   *
   * @param globalWebDesc
   * @param webDesc
   * @param mergedRoleNamesSet
   * @return
   */
  static boolean mergeDescriptorsSecurity(WebDeploymentDescriptor globalWebDesc, WebDeploymentDescriptor webDesc, Set mergedRoleNamesSet) {

    SecurityRoleType[] secRolesTemp = globalWebDesc.getSecurityRoles();
    if (secRolesTemp != null) {
      for (int i = 0; i < secRolesTemp.length; i++) {
        mergedRoleNamesSet.add(secRolesTemp[i].getRoleName().get_value());
      }
    }
    if (traceLocation.beDebug()) {
    	traceLocation.debugT("Roles from global descriptor: " + JavaACCParser.collectionToString(mergedRoleNamesSet));
    }
    secRolesTemp = webDesc.getSecurityRoles();
    if (secRolesTemp != null) {
      for (int i = 0; i < secRolesTemp.length; i++) {
        mergedRoleNamesSet.add(secRolesTemp[i].getRoleName().get_value());
      }
    }
    if (traceLocation.beDebug()) {
    	traceLocation.debugT("Roles from global and web descriptor: " + JavaACCParser.collectionToStringArr(mergedRoleNamesSet));
    }
    return true;
  } // mergeDescriptorsSecurity(WebDeploymentDescriptor globalWebDesc, WebDeploymentDescriptor webDesc, HashMap mergedRolesMap) {

  /**
   * Add needed JavaACC permissions for migration of roles that are in Configuration and are not in app descriptors.
   *
   * @param globalWebDesc
   * @param webDesc
   * @param securityRolesInDBArr
   * @return true if successfully processed
   */
  public static boolean processDBRolesNotInAppDesctriptors(WebDeploymentDescriptor globalWebDesc, WebDeploymentDescriptor webDesc, SecurityRole[] securityRolesInDBArr, PolicyConfiguration policyConfiguration) throws PolicyContextException {
    if (securityRolesInDBArr == null || securityRolesInDBArr.length == 0) {
      // nothing to do
      return true;
    }

    Set mergedRoleNamesInDescriptorsSet = new HashSet(15);
    boolean reaultOk = mergeDescriptorsSecurity(globalWebDesc, webDesc, mergedRoleNamesInDescriptorsSet);
    if (!reaultOk) {
      return false;
    }

    // get DB role names as Set
    SecurityRole securityRole;
    Set dbRoleNamesSet = new HashSet(10);
    for (int i = 0; i < securityRolesInDBArr.length; i++) {
      securityRole = securityRolesInDBArr[i];
      dbRoleNamesSet.add(securityRole.getName());
    }
    if (traceLocation.beDebug()) {
    	traceLocation.debugT("Roles from DB: " + JavaACCParser.collectionToStringArr(dbRoleNamesSet));
    }

    dbRoleNamesSet.removeAll(mergedRoleNamesInDescriptorsSet);
    if (traceLocation.beDebug()) {
    	traceLocation.debugT("Roles not in descriptors: " + dbRoleNamesSet);
    }

    Iterator roleNamesIter = dbRoleNamesSet.iterator();
    String roleName;
    WebRoleRefPermission webRoleRefPermission;
    while (roleNamesIter.hasNext()) {
      roleName = (String) roleNamesIter.next();
      webRoleRefPermission = new WebRoleRefPermission("", roleName);
      if (traceLocation.beDebug()) {
      	traceLocation.debugT("performing policyConfiguration.addToRole(" + roleName + ", " + "new WebRoleRefPermission(\"\", roleName);");
      }
      policyConfiguration.addToRole(roleName, webRoleRefPermission);
    }
    return true;
  } // processDBRolesNotInAppDesctriptors()

  public static PolicyConfiguration getPolicyConfiguration(String policyConfigID, boolean overwriteAlreadyExistingInstance, String applicationName) {
    PolicyConfiguration policyConfig = null;
    try {
      policyConfig = PolicyConfigurationFactory.getPolicyConfigurationFactory().getPolicyConfiguration(policyConfigID,
        overwriteAlreadyExistingInstance);
      // linking configuration as required in JACC 1.1, ch. 3.1 What a Java EE Platform's Deployment Tools Must Do
      policyConfig.linkConfiguration(PolicyConfigurationFactory.getPolicyConfigurationFactory().getPolicyConfiguration(applicationName, false));
    } catch (PolicyContextException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000020",
        "Error initializing security constraints. Getting JavaACC PolicyConfiguration.", e, null, null);
      return null;
    } catch (ClassNotFoundException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000021",
        "ClassNotFoundException is thrown. Maybe the JavaACC PolicyProvider is not configured.", e, null, null);
      return null;
    }
    return policyConfig;
  } // getPolicyConfiguration()

  /*
  public static PolicyConfiguration getAdditionalPolicyConfiguration(String policyConfigID, boolean overwriteAlreadyExistingInstance, String alias) {
    PolicyConfiguration policyConfig = null;
    try {
      if (umeFactory == null) {
        umeFactory = (PolicyConfigurationFactory) Class.forName("com.sap.security.core.role.jacc.UmePolicyConfigurationFactory").newInstance();
      }
      policyConfig = umeFactory.getPolicyConfiguration(policyConfigID + "-EXTERNAL", overwriteAlreadyExistingInstance);
    } catch (InstantiationException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation,
        "Error initializing security constraints. Getting UME JavaACC PolicyConfiguration.", e, alias);
      return null;
    } catch (IllegalAccessException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation,
        "Error initializing security constraints. Getting UME JavaACC PolicyConfiguration.", e, alias);
      return null;
    } catch (PolicyContextException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation,
        "Error initializing security constraints. Getting UME JavaACC PolicyConfiguration.", e, alias);
      return null;
    } catch (ClassNotFoundException e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation,
        "ClassNotFoundException is thrown. Maybe the UME JavaACC PolicyProvider is not configured.", e, alias);
      return null;
    }
    return policyConfig;
  }
*/
  private JACCUpdateContext getUMEUpdateContext(String policyConfiguration) {
    try {
      Class updateClass = Class.forName("com.sap.security.core.server.ume.service.jacc.JACCUpdateContextImpl");
      return (JACCUpdateContext) updateClass.getConstructor(new Class[]{java.lang.String.class}).newInstance(new Object[]{policyConfiguration});
    } catch (Exception cnf) {
      // log
      throw new SecurityException(cnf.getMessage());
    }
  }

  private JACCUndeployContext getUMEUndeployContext(String policyConfiguration) {
    try {
      Class updateClass = Class.forName("com.sap.security.core.server.ume.service.jacc.JACCUndeployContextImpl");
      return (JACCUndeployContext) updateClass.getConstructor(new Class[]{java.lang.String.class}).newInstance(new Object[]{policyConfiguration});
    } catch (Exception cnf) {
      // log
      throw new SecurityException(cnf.getMessage());
    }
  }
}// end of class
