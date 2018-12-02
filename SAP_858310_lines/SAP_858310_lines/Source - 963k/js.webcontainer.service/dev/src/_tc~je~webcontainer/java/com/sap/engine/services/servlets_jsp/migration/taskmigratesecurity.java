/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.migration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.interfaces.security.AuthorizationContext;
import com.sap.engine.interfaces.security.JACCSecurityRoleMappingContext;
import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.interfaces.security.SecurityRole;
import com.sap.engine.interfaces.security.SecurityRoleContext;
import com.sap.engine.interfaces.security.ModificationContext;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.WebDeploymentDescriptor;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.deploy.util.JavaACCParser;
import com.sap.engine.services.servlets_jsp.server.deploy.util.SecurityUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.XmlUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.jacc.SecurityConstraintParser;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.lib.descriptors5.javaee.SecurityRoleType;
import com.sap.engine.lib.descriptors5.web.ServletType;
import com.sap.engine.lib.descriptors5.web.SecurityConstraintType;
import com.sap.tc.logging.LoggingUtilities;

/**
 * @author Violeta Georgieva
 * @version 7.0
 */
public class TaskMigrateSecurity {

  public static void createSecurityResources(Configuration servlet_jspConfig,
                                             JACCSecurityRoleMappingContext jaccSecurityRoleMappingContext,
                                             String rootDirectory, String aliasDir, String policyConfigurationName,
                                             String applicationName, String alias, int currentVersion) throws Exception {
    WebDeploymentDescriptor globalWebDesc = ServiceContext.getServiceContext().getDeployContext().getGlobalDD();

    Thread currentThread = Thread.currentThread();
    ClassLoader currentThreadLoader = currentThread.getContextClassLoader();
    currentThread.setContextClassLoader(ServiceContext.getServiceContext().getServiceLoader());

    WebDeploymentDescriptor webDesc = null;
    InputStream altDD = null;
    try {
      if (currentVersion < 2) {
        String xmlFile = rootDirectory + "WEB-INF" + File.separator + "web.xml";
        if (!(new File(xmlFile)).exists()) {
          throw new WebDeploymentException(WebDeploymentException.THE_ARCHIVE_FOR_WEB_APPLICATION_DOES_NOT_CONTAIN_WEBINF_WEBXML_FILE_OR_IT_IS_SPELT_INCORRECTLY,
            new Object[]{aliasDir});
        }

        altDD = ConfigurationUtils.getFile(servlet_jspConfig, Constants.ALT_DD + aliasDir, aliasDir);

        if (altDD == null) {
          FileInputStream fis = new FileInputStream(xmlFile);
          try {
            byte[] tempStream = ServiceContext.getServiceContext().getDeployContext().getWebConverter().convertXML(fis, "web", "extended");

            webDesc = XmlUtils.parseXml(new ByteArrayInputStream(tempStream), null, aliasDir, xmlFile, "", true);
          } finally {
            fis.close();
          }
        } else {
          byte[] tempStream = ServiceContext.getServiceContext().getDeployContext().getWebConverter().convertXML(altDD, "web", "extended");

          webDesc = XmlUtils.parseXml(new ByteArrayInputStream(tempStream), null, aliasDir, " alternative descriptor ", "", true);
        }
      } else {
        webDesc = getWebDeploymentDescriptor(rootDirectory, aliasDir);
      }
    } finally {
      currentThread.setContextClassLoader(currentThreadLoader);

      if (currentVersion < 2) {
        if (altDD != null) {
          altDD.close();
        }
      }
    }

    // Init security roles and security role maps
    Vector secRolesVec = new Vector();
    HashMap secRolesMap = new HashMap();
    // Init security resources from global-web.xml
    SecurityUtils.initSecurityRoles(globalWebDesc, secRolesVec, secRolesMap);
    // Init security resources from local web.xml
    SecurityUtils.initSecurityRoles(webDesc, secRolesVec, secRolesMap);
    SecurityRoleType[] secRoles = (SecurityRoleType[]) secRolesVec.toArray(new SecurityRoleType[secRolesVec.size()]);

    // Init servlets in order to be process run as and security role references.
    Vector servletDescVec = new Vector();
    SecurityUtils.initServlets(globalWebDesc, servletDescVec);
    SecurityUtils.initServlets(webDesc, servletDescVec);
    ServletType[] servletDesc = (ServletType[]) servletDescVec.toArray(new ServletType[servletDescVec.size()]);

    // Init security constraints
    Vector securityConstraintsVec = new Vector();
    SecurityUtils.initSecurityConstraints(globalWebDesc, securityConstraintsVec);
    SecurityUtils.initSecurityConstraints(webDesc, securityConstraintsVec);
    SecurityConstraintType[] securityConstraints = (SecurityConstraintType[]) securityConstraintsVec.toArray(new SecurityConstraintType[securityConstraintsVec.size()]);

    String policyConfigID = SecurityUtils.getPolicyConfigurationID(applicationName, alias);
    PolicyConfiguration policyConfig = SecurityUtils.getPolicyConfiguration(policyConfigID, 
        true /* removePrevPolicyConfig */, applicationName);
    if (policyConfig == null) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_POLICY_CONFIGURATION, new String[]{alias, applicationName, policyConfigID});
    }
    if (LogContext.getLocationSecurity().beDebug()) {
      LogContext.getLocation(LogContext.LOCATION_SECURITY).traceDebug("JavaACCParser(): policyConfiguration initialized", alias);
    }
    JavaACCParser javaACCParser = new JavaACCParser(applicationName, alias, policyConfigID, secRoles, servletDesc);
    javaACCParser.createSecurityRoleRefs(true, policyConfig);
    
    // Switch back usage to SecurityConstraintParser after enabling by default JACC 1.1
    SecurityConstraintParser securityConstraintParser = new SecurityConstraintParser(applicationName, alias, policyConfig, securityConstraints, secRoles);
    securityConstraintParser.createSecurityResourcesJACC();
    
    try {
      policyConfig.commit();
    } catch (PolicyContextException e) {
    	//TODO:Polly type:ok content - more info. during application migration could be added. 
      LogContext.getCategory(LogContext.CATEGORY_SECURITY).logError(LogContext.getLocationSecurity(), "ASJ.web.000267", 
        "Error committing security changes for application [{0}] with [{1}] alias." , new Object[]{applicationName, alias}, e, null, null);
      throw new WebDeploymentException( WebDeploymentException.DEPLOY_JACC_ERROR,
            new Object[] { applicationName, alias, "JACC commit error."}, e);
    }
    
    SecurityContext oldAppSecurityContext = ServiceContext.getServiceContext().getSecurityContext().getPolicyConfigurationContext(policyConfigurationName);
    if (oldAppSecurityContext == null) {
      String dcName = LoggingUtilities.getDcNameByClassLoader(ServiceContext.getServiceContext().getSecurityContext().getClass().getClassLoader());
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_APPLICATION_SECURITY_CONTEXT_FOR_WEB_APPLICATION,
        new Object[]{dcName, LoggingUtilities.getCsnComponentByDCName(dcName), policyConfigurationName.substring(policyConfigurationName.indexOf("*") + 1)});
    }

    AuthorizationContext autorizationContext = oldAppSecurityContext.getAuthorizationContext();

    SecurityRoleContext securityRoleContext = autorizationContext.getSecurityRoleContext();

    SecurityRole[] securityRoles = securityRoleContext.listSecurityRoles();

    policyConfig = SecurityUtils.getPolicyConfiguration(policyConfigurationName, false, applicationName);
    if (policyConfig == null) {
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_POLICY_CONFIGURATION, new String[]{alias, applicationName});
    }
    SecurityUtils.processDBRolesNotInAppDesctriptors(globalWebDesc, webDesc, securityRoles, policyConfig);

    for (int i = 0; i < securityRoles.length; i++) {
      SecurityRole currentSecurityRole = securityRoles[i];

      String roleName = currentSecurityRole.getName();

      String[] roleReference = currentSecurityRole.getReference();
      if (roleReference != null && roleReference.length > 1 && roleReference[0] != null && roleReference[0].equals(SecurityContext.ROOT_POLICY_CONFIGURATION)) {
        jaccSecurityRoleMappingContext.addUMERoleToJACCRole(roleName, policyConfigurationName, roleReference[1]);
      } else {
        String[] users = currentSecurityRole.getUsers();
        String[] groups = currentSecurityRole.getGroups();
        String umeRole = jaccSecurityRoleMappingContext.addUsersAndGroupsToJACCRole(roleName, policyConfigurationName, users, groups);
        jaccSecurityRoleMappingContext.addUMERoleToJACCRole(roleName, policyConfigurationName, umeRole);
      }
    }
  }//end of createSecurityResources(Configuration servlet_jspConfig, JACCSecurityRoleMappingContext jaccSecurityRoleMappingContext, String rootDirectory,
  //                                String aliasDir, String policyConfigurationName, String applicationName, String alias, int currentVersion)

  public static void addSecurityProperties(Configuration securityConfig, String policyConfig, String alias, String rootDir, int currentVersion) throws Exception {
    ModificationContext securityModificationContext = ServiceContext.getServiceContext().getSecurityContext().getModificationContext();
    SecurityContext mySecContext = securityModificationContext.beginModifications(securityConfig);
    SecurityContext appSecurityContext = mySecContext.getPolicyConfigurationContext(policyConfig);
    if (appSecurityContext == null) {
      String dcName = LoggingUtilities.getDcNameByClassLoader(mySecContext.getClass().getClassLoader());
      throw new WebDeploymentException(WebDeploymentException.CANNOT_GET_APPLICATION_SECURITY_CONTEXT_FOR_WEB_APPLICATION, 
        new Object[]{dcName, LoggingUtilities.getCsnComponentByDCName(dcName), alias});
    }

    Thread currentThread = Thread.currentThread();
    ClassLoader currentThreadLoader = currentThread.getContextClassLoader();
    currentThread.setContextClassLoader(ServiceContext.getServiceContext().getServiceLoader());
    try {
      WebDeploymentDescriptor webDesc = getWebDeploymentDescriptor(rootDir, alias);

      if (currentVersion < 4) {
        // Init security configuration from global-web.xml
        WebDeploymentDescriptor globalWebDesc = ServiceContext.getServiceContext().getDeployContext().getGlobalDD();
        initSecurityConfiguration(appSecurityContext, globalWebDesc, currentVersion);
        // Init security configuration from local web.xml
        initSecurityConfiguration(appSecurityContext, webDesc, currentVersion);
      } else {
        removeGlobalAuthMethod(appSecurityContext, webDesc);
      }
    } finally {
      currentThread.setContextClassLoader(currentThreadLoader);
    }
  }//end of addSecurityProperties(Configuration securityConfig, String policyConfig, String alias, String rootDir, int currentVersion)

  private static void initSecurityConfiguration(SecurityContext appSecurityContext, WebDeploymentDescriptor webDesc, int currentVersion) {
    if (webDesc.getWebJ2EEEngine().getLoginModuleConfiguration() != null
      && webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getPasswordChangeConfig() != null) {
      // Set password config to the security context
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

    if (webDesc.getLoginConfig() != null) {
      // Set auth method to the security context
      if (currentVersion == 3) {
        if (webDesc.getLoginConfig().getAuthMethod() != null) {
          appSecurityContext.getAuthenticationContext().setProperty("auth_method", webDesc.getLoginConfig().getAuthMethod().get_value().toLowerCase());
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
    }
  }// end of initSecurityConfiguration(SecurityContext appSecurityContext, WebDeploymentDescriptor webDesc, int currentVersion)

  public static WebDeploymentDescriptor getWebDeploymentDescriptor(String rootDir, String alias) throws DeploymentException{
    String xmlFile = rootDir + "WEB-INF" + File.separator + "web.xml";
    if (!(new File(xmlFile)).exists()) {
      throw new WebDeploymentException(WebDeploymentException.THE_ARCHIVE_FOR_WEB_APPLICATION_DOES_NOT_CONTAIN_WEBINF_WEBXML_FILE_OR_IT_IS_SPELT_INCORRECTLY,
        new Object[]{alias});
    }

    String additonalXmlFile = rootDir + "WEB-INF" + File.separator + "web-j2ee-engine.xml";
    if (!new File(additonalXmlFile).exists()) {
      additonalXmlFile = null;
    }

    WebDeploymentDescriptor webDesc = null;
    try {
      FileInputStream additonalXmlInputFile = null;
      if (additonalXmlFile != null && (new File(additonalXmlFile)).exists()) {
        additonalXmlInputFile = new FileInputStream(additonalXmlFile);
      }
      webDesc = XmlUtils.parseXml(new FileInputStream(xmlFile), additonalXmlInputFile, alias, xmlFile, additonalXmlFile, true);
    } catch (IOException io) {
      throw new WebDeploymentException(WebDeploymentException.THE_ARCHIVE_FOR_WEB_APPLICATION_DOES_NOT_CONTAIN_WEBINF_WEBXML_FILE_OR_IT_IS_SPELT_INCORRECTLY,
        new Object[]{alias}, io);
    }

    return webDesc;
  }//end of getWebDeploymentDescriptor(String rootDir, String alias)

  private static void removeGlobalAuthMethod(SecurityContext appSecurityContext, WebDeploymentDescriptor webDesc) {
    if (webDesc.getLoginConfig() == null || webDesc.getLoginConfig().getAuthMethod() == null) {
      // Delete auth method in the security context
      String authMethodInSecurity = appSecurityContext.getAuthenticationContext().getProperty("auth_method");
      if (authMethodInSecurity != null && authMethodInSecurity.equals("form")) {
        appSecurityContext.getAuthenticationContext().setProperty("auth_method", null);
      }

      // Delete auth template in the security context.
      if (webDesc.getWebJ2EEEngine().getLoginModuleConfiguration() == null ||
        webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getLoginModuleStack() == null ||
        webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getLoginModuleStack().getLoginModule() == null ||
        webDesc.getWebJ2EEEngine().getLoginModuleConfiguration().getLoginModuleStack().getLoginModule().length == 0) {
        String authTemplateInSecurity = appSecurityContext.getAuthenticationContext().getTemplate();
        if (authTemplateInSecurity != null && (authTemplateInSecurity.equals("form") || authTemplateInSecurity.equals("basic"))) {
          appSecurityContext.getAuthenticationContext().setProperty("template", null);
        }
      }
    }
  }
}//end of class
