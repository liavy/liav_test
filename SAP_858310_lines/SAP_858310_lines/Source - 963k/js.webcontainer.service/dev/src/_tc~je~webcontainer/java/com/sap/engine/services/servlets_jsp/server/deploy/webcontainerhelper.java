/*
 * Copyright (c) 2004-2008 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy;

import static com.sap.engine.services.httpserver.lib.ParseUtils.separator;
import static com.sap.engine.services.servlets_jsp.server.ServiceContext.getServiceContext;
import static com.sap.engine.services.servlets_jsp.server.deploy.util.Constants.defaultAliasDir;
import static com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException.EMPTY_CONTEXT_ROOT_FOR_ARCHIVE_IN_APPLICATION;

import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.session.SessionContext;
import com.sap.tc.logging.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * Different utilities used in deploy operations.
 *
 * @author Violeta Georgieva
 * @version 7.10
 */
public class WebContainerHelper {
  private static final String web = "web:";
  private static final String semicolon = ";"; 
  private static final String webinf = "WEB-INF";

  private static Location currentLocation = Location.getLocation(WebContainerHelper.class);

  private SessionContext sessionContext = null;

  /**
   *
   */
  public WebContainerHelper() {
    sessionContext = SessionContext.obtainSessionContext("HTTP_Session_Context");
  }//end of constructor

  public SessionContext getSessionContext() {
    return sessionContext;
  }//end of getSessionContext()

  /**
   * @param aliasMB
   */
  public void removeStartedApplication(MessageBytes aliasMB, String applicationName) {
    if (aliasMB.equals(Constants.defaultAliasMB)) {     
      // if it is default alias check not to remove the context from the other application
      // get the application context of the started application 
      ApplicationContext appContext = getServiceContext().getDeployContext().getStartedWebApplicationContext(aliasMB);
      if (appContext != null && appContext.getApplicationName().equalsIgnoreCase(applicationName)) {
        // remove from the hashtable only if the appplication names of the started and starting applications are the same
        // otherwise skip this step
        getServiceContext().getDeployContext().removeStartedApplication(Constants.defaultAliasMB);
      }
    } else {
      getServiceContext().getDeployContext().removeStartedApplication(aliasMB);
    }    
  }//end of removeStartedApplication(String applicationName)

  /**
   * Invoked during deploy and update.
   * @param applicationName
   * @param props contains the new application aliases, the old ones can be obtained from DeployCommunicator.
   * @return aliases for the application, or empty String []
   * @throws DeploymentException
   */
  public static String[] getAliases(String applicationName, Properties props) throws DeploymentException {
    ArrayList<String> allAliases = new ArrayList<String>();
    Enumeration en = props.keys();
    while (en.hasMoreElements()) {
      String element = (String) en.nextElement();
      if (element.startsWith(web)) {
        String alias = element.substring(web.length());
        if (alias == null || alias.trim().equals("")) {
          throw new WebDeploymentException(EMPTY_CONTEXT_ROOT_FOR_ARCHIVE_IN_APPLICATION,
            new Object[]{element, applicationName});
        }

        if (alias.startsWith(separator) && !alias.equals(separator)) {
          alias = alias.substring(1);
        }

        allAliases.add(alias);
      }
    }
    return (String[]) allAliases.toArray(new String[allAliases.size()]);
  }//end of getAliases(String applicationName, Properties props)

  /**
   * Call ParseUtils.convertAlias(String alias) before using this method!!!
   *
   * @param aliasCanonicalized canonicalized alias
   * @return
   */
  public static String getAliasDirName(String aliasCanonicalized) {
    //Call ParseUtils.convertAlias(String alias) before using this method!!!
    if (aliasCanonicalized.equals(separator)) {
      return defaultAliasDir;
    } else {
      return aliasCanonicalized;
    }
  }//end of getAliasDirName(String aliasDirName)

  /**
   * Returns application's temp dir valid for our container.
   * @param applicationName
   * @return
   */
  public static String getDeployTempDir(String applicationName) {
    String tempDir = null;
    try {
      tempDir = getServiceContext().getDeployContext().getDeployCommunicator().getMyWorkDirectory(applicationName);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000107",
        "Cannot get working directory.", e, null, null);
      tempDir = getServiceContext().getWorkDirectory();
    }
    return tempDir;
  }//end of getDeployTempDir(String applicationName)

  /**
   * Iterates over the elements from the array and constructs one string with File.separator between all elements.
   * The resulted string ends with File.separator.
   *
   * @param names
   * @return
   */
  public static String getDirName(String[] names) {
    if (names == null) {
      return null;
    }
    StringBuilder builder = new StringBuilder(names.length);
    for (int i = 0; i < names.length; i++) {
      if (names[i] != null) {
        builder.append(names[i]);
        builder.append(File.separator);
      }
    }
    return builder.toString();
  }// end of getDirName(String[] names)

  /**
   * Transforms Vector into String with ";" as a delimiter between elements.
   *
   * @param vec
   * @return a string representation of the vector elements.
   */
  public static String vectorToString(Vector vec) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < vec.size(); i++) {
      sb.append((String)vec.elementAt(i)).append(semicolon);
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }//end of vectorToString(Vector vec)

  /**
   * Checks whether a specific deployment descriptor exists in WEB-INF directory.
   * @param descriptorNames
   * @param rootDirectory
   * @return true if one of the descriptor names exist in "WEB-INF" directory, otherwise - false
   */
  public static boolean isDescriptorExist(Vector descriptorNames, String rootDirectory) {
    for (int i = 0; i < descriptorNames.size(); i++) {
      if ((new File(rootDirectory + File.separator + webinf + File.separator + descriptorNames.elementAt(i))).exists()) {
        return true;
      }
    }

    return false;
  }//end of isDescriptorExist(String descriptorNames, String rootDirectory)

}//end of class
