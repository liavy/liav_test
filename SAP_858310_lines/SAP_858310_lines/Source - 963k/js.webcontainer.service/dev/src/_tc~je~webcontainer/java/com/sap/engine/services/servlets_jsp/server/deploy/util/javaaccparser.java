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
package com.sap.engine.services.servlets_jsp.server.deploy.util;

import com.sap.engine.lib.descriptors5.javaee.RoleNameType;
import com.sap.engine.lib.descriptors5.javaee.SecurityRoleRefType;
import com.sap.engine.lib.descriptors5.javaee.SecurityRoleType;
import com.sap.engine.lib.descriptors5.web.ServletNameType;
import com.sap.engine.lib.descriptors5.web.ServletType;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.tc.logging.Location;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebRoleRefPermission;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;


/**
 * Utilities for parsing security related parts from descriptors and creates needed JavaACC configuration statements.
 *
 * @author Svilen Dikov
 */
public class JavaACCParser {
  final static Location traceLocation = LogContext.getLocationSecurity();

  private String applicationName;

  private String aliasName;
  /**
   * PolicyConfiguration ID - unique with every other combination of application and module
   */
  private String policyConfigID;

  //private JavaACCUniversalMapping universalMappingContainer;

  /**
   * PolicyConfiguration object used in all JavaACC-related calls
   */
  private PolicyConfiguration policyConfig = null;

  final static String EMPTY_STR = "";
  /**
   * Security constraint type identifier. Depends on existence of auth-constraint tag. Constant for unset value.
   */
  final static int CONSTRAINT_TYPE_IS_UNDEFINED = -1;
  /**
   * Security constraint type identifier. Depends on existence of auth-constraint tag. Exists but empty - restricted for all.
   */
  final static int CONSTRAINT_TYPE_IS_EXCLUDED = 0;
  /**
   * Security constraint type identifier. Depends on existence of auth-constraint tag. No existence - allow for all;
   */
  final static int CONSTRAINT_TYPE_IS_UNCHECKED = 1;
  /**
   * Security constraint type identifier. Depends on existence of auth-constraint type. Some roles inside - allowed just
   * for those roles.
   */
  final static int CONSTRAINT_TYPE_IS_UNCHECKED_PER_ROLE = 2;
  private SecurityRoleType[] secRolesArr;
  private ServletType[] servletDescArr;

  /**
   * @param applicationName
   * @param aliasName
   * @param policyConfigID
   */
  public JavaACCParser(String applicationName, String aliasName, String policyConfigID,
                       SecurityRoleType[] secRolesArr,
                       ServletType[] servletDescArr) {

    this.applicationName = applicationName;
    this.aliasName = aliasName;
    this.policyConfigID = policyConfigID;
    this.secRolesArr = secRolesArr;
    this.servletDescArr = servletDescArr;
  } // JavaACCParser()

  /**
   * Main method to create JavaACC permissions.
   *
   * @return Vector with deployment warnings;
   */
  public Vector createSecurityRoleRefs(boolean removePrevPolicyConfig, PolicyConfiguration policyConfig) throws DeploymentException {
    // roleMap (webapp-role:server-role)

    this.policyConfig = policyConfig;

    boolean processSecurityRoleRefs = false;
    try {
      processSecurityRoleRefs = processSecurityRoleRefElements();
    } catch (PolicyContextException e) {
      throw new WebDeploymentException(WebDeploymentException.ERROR_PROCESSING_SECURITY_ROLE_REF, new String[]{aliasName, applicationName}, e);
    }
    if (traceLocation.beDebug()) {
      traceLocation.debugT("SecurityRoleRefs processed: " + processSecurityRoleRefs);
    }

    // TODO - Policy.refresh
    return null; // warningsVec;
  } // createSecurityResourcesJACC()


  /**
   * Process all servlet -> security-role-ref elements on every servlet and security-role tag
   * as per JavaACC 1.0, Chapter 3.1.3.2
   *
   * @return
   */
  private boolean processSecurityRoleRefElements() throws PolicyContextException {
    boolean beDebug = traceLocation.beDebug();
  	if (beDebug) {
      traceLocation.debugT("-->processSecurityRoleRefElements():");
    }

    // processing security-role descriptor part
    if (beDebug) {
    	traceLocation.debugT("security-role tag processing");
    }
    SecurityRoleType[] securityRoleTypeArr = secRolesArr;
    TreeSet securityRoleNamesStrArr;
    if (securityRoleTypeArr != null && securityRoleTypeArr.length > 0) {
      SecurityRoleType securityRoleType;
      int securityRoleTypeArrLen = securityRoleTypeArr.length;
      securityRoleNamesStrArr = new TreeSet();//String[securityRoleTypeArrLen];
      String roleNameStr;
      for (int i = 0; i < securityRoleTypeArrLen; i++) {
        securityRoleType = securityRoleTypeArr[i];
        RoleNameType roleNameType = securityRoleType.getRoleName();
        roleNameStr = roleNameType.get_value();
        if (roleNameStr.length() > 0) {
          securityRoleNamesStrArr.add(roleNameStr);
          if (beDebug) {
          	traceLocation.debugT("policyConfig.addToRole(" + roleNameStr +", new WebRoleRefPermission(\"\", " + roleNameStr +"));");
          }
          // for unnamed servlets - JSP files; JavaACC 1.0, Chapter 3.1.3.2, Errata p.24 
          policyConfig.addToRole(roleNameStr, new WebRoleRefPermission("", roleNameStr));
        }
      }
    } else {
      securityRoleNamesStrArr = new TreeSet();
    }

    // processing security-role-ref elements
    ServletType[] servletTypeArr = servletDescArr;
    if (servletTypeArr == null) {
      return true;
    }
    ServletType servletType;
    SecurityRoleRefType[] securityRoleRefTypeArr;
    SecurityRoleRefType securityRoleRefType;
    RoleNameType roleNameType;
    String servletName;
    ServletNameType servletNameType;
    for (int i = 0; i < servletTypeArr.length; i++) {
      servletType = servletTypeArr[i];
      servletNameType =  servletType.getServletName();
      if (servletNameType != null) {
        servletName = servletNameType.get_value();
      } else {
        servletName = null;
      }
      securityRoleRefTypeArr = servletType.getSecurityRoleRef();
      TreeSet rolesNotInSecurityRoleRef;
      rolesNotInSecurityRoleRef = (TreeSet) securityRoleNamesStrArr.clone();
      if (securityRoleRefTypeArr != null) {
        // JavaACC 1.0 Ch. 3, p. 23
        String roleName;
        String roleLinkName;
        RoleNameType roleLinkNameType;
        for (int j = 0; j < securityRoleRefTypeArr.length; j++) {
          securityRoleRefType = securityRoleRefTypeArr[j];
          roleNameType = securityRoleRefType.getRoleName();
          roleName = roleNameType.get_value();
          roleLinkNameType = securityRoleRefType.getRoleLink();
          if (roleLinkNameType != null) {
            roleLinkName = roleLinkNameType.get_value();
          } else {
            roleLinkName = null;
          }
          rolesNotInSecurityRoleRef.remove(roleName);
          if (roleLinkName != null) {
            if (beDebug) {
            	traceLocation.debugT("policyConfig.addToRole(" + roleLinkName + ", "
                    + "new WebRoleRefPermission(" + servletName + "," + roleName + "));");
            }
            WebRoleRefPermission webRoleRefPermission = new WebRoleRefPermission(servletName, roleName);
            policyConfig.addToRole(roleLinkName, webRoleRefPermission);
          }
        }
        // iterate on non-occurring roles in security-role-ref
        Iterator iter = rolesNotInSecurityRoleRef.iterator();
        if (beDebug) {
        	traceLocation.debugT("Roles not referenced in servlet " + servletName);
        }
        while (iter.hasNext()) {
          roleName = (String) iter.next();
          if (beDebug) {
          	traceLocation.debugT("policyConfig.addToRole(" + roleName + ", "
                    + "new WebRoleRefPermission(" + servletName + "," + roleName + "));");
            }
            WebRoleRefPermission webRoleRefPermission = new WebRoleRefPermission(servletName, roleName);
            policyConfig.addToRole(roleName, webRoleRefPermission);
        }

      }
    } // for i

    return true;
  } // processSecurityRoleRefElements()

  static String arrayToString(Object[] objArr) {
    if (objArr == null) {
      return "[] null";
    }
    StringBuffer sb = new StringBuffer();
    sb.append("[");
    Object obj;
    int arrLen = objArr.length;
    for (int i = 0; i < arrLen; i++) {
      obj = objArr[i];
      if (i > 0) {
        sb.append(",");
      }
      sb.append(obj);
    }
    sb.append("]");
    return sb.toString();
  } // arrayToString()

  static String collectionToString(Collection c) {
    if (c == null) {
      return null;
    }
    int collectionLen = c.size();
    if (collectionLen == 0) {
      return "[]";
    }
    StringBuffer sb = new StringBuffer(collectionLen*10);
    Iterator iter = c.iterator();
    sb.append(iter.next());
    while (iter.hasNext()) {
      sb.append(",");
      sb.append(iter.next());
    }
    return sb.toString();
  } // colleactionToString()

  static String[] collectionToStringArr(Collection col) {
    if (col == null) {
      return null;
    }
    String[] strArr = new String[col.size()];
    Iterator iter = col.iterator();
    int i = 0;
    Object obj;
    while (iter.hasNext()) {
      obj = iter.next();
      strArr[i++] = obj == null ? null : obj.toString();
    }
    return strArr;
  } // collectionToStringArr()
}
