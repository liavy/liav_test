/*
 * Copyright (c) 2004-2009 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.deploy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.deploy.container.ContainerInfo;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.deploy.container.op.util.StartUp;
import com.sap.engine.services.httpserver.interfaces.HttpProvider;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.WebContainerInterface;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebResourceAccessor;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebWarningException;
import com.sap.engine.session.SessionDomain;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public class RemoveAction extends ActionBase {
  private static Location currentLocation = Location.getLocation(RemoveAction.class);

  /**
   * @param iWebContainer
   * @param containerInfo
   * @param servletsAndJsp
   * @param runtimeInterface
   */
  public RemoveAction(IWebContainer iWebContainer, ContainerInfo containerInfo, WebContainerHelper servletsAndJsp, WebContainerInterface runtimeInterface) {
    super(iWebContainer, containerInfo, servletsAndJsp, runtimeInterface);
  }//end of constructor

  /**
   * @param applicationName
   * @throws DeploymentException
   * @throws WarningException
   */
  public void remove(String applicationName, Configuration appConfiguration) throws DeploymentException, WarningException {
    remove(applicationName, false, appConfiguration);
  }//end of remove(String applicationName)

  public void notifyRemove(String applicationName, Properties properties, boolean isUpdate) throws WarningException {
    if (applicationName == null) {
      return;
    }
    String debugInfo = (!isUpdate) ? "notifyRemove" : "notifyUpdatedComponents";
    ArrayList<LocalizableTextFormatter> warnings = new ArrayList<LocalizableTextFormatter>();
    String tagName = null;

    //These deleted aliases are due to update operation.
    Vector<String> allAliases = new Vector<String>();
    if (properties != null) {
      String deletedAliasesStr = properties.getProperty("deleted_web_aliases");
      if (deletedAliasesStr != null) {
        Pattern p = Pattern.compile("::");
        allAliases.addAll(Arrays.asList(p.split(deletedAliasesStr)));
      }
    }

    String[] aliases = ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName);
    if (aliases != null) {
      allAliases.addAll(Arrays.asList(aliases));
    }

    aliases = (String[]) allAliases.toArray(new String[allAliases.size()]);
    if (aliases != null) {
      String[] aliasesCanonicalized = new String[aliases.length];
      for (int i = 0; i < aliases.length; i++) {
        aliasesCanonicalized[i] = ParseUtils.convertAlias(aliases[i]);
      }

      try { //ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          tagName = debugInfo + "/iWebContainer.notifyRemove (" + aliasesCanonicalized + ")";
          Accounting.beginMeasure(tagName, iWebContainer.getClass());
        } //ACCOUNTING.start - END
        iWebContainer.notifyRemove(applicationName, aliasesCanonicalized);
      } finally { //ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      } //ACCOUNTING.end - END

      for (int i = 0; i < aliases.length; i++) {
        String aliasCanonicalized = aliasesCanonicalized[i];

        String[] deployedApplications = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getMyApplications();

        boolean removeAlias = true;
        boolean aliasFoundInDeployedApps = false;
        for (int j = 0; deployedApplications != null && j < deployedApplications.length; j++) {
          //get canonicalized aliases
          String[] deployedAliases = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(deployedApplications[j]);

          for (int k = 0; deployedAliases != null && k < deployedAliases.length; k++) {
            if (aliasCanonicalized.equals(deployedAliases[k])) {
              aliasFoundInDeployedApps = true;
              if (applicationName.equals(deployedApplications[j])) {
                //remove_app!! (the same alias and application name)
                removeAlias = true;
                try {
                  if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
                    tagName = debugInfo + "/sessionDomain.destroy (" + aliasCanonicalized + ")";
                    Accounting.beginMeasure(tagName, SessionDomain.class);
                  } //ACCOUNTING.start - END
                  
                  SessionDomain sessionDomain = webContainerHelper.getSessionContext().findSessionDomain("/" + aliasCanonicalized);
                  if (sessionDomain != null) {
                    sessionDomain.destroy();
                  }
                } catch (OutOfMemoryError e) {
                  throw e;
                } catch (ThreadDeath e) {
                  throw e;
                } catch (Throwable e) {
                	warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
                	WebWarningException.ERROR_IN_DESTROING_SESSION_DOMAIN,
                	new Object[]{aliasCanonicalized, e.toString()}));
                } finally { //ACCOUNTING.end - BEGIN
                  if (Accounting.isEnabled()) {
                    Accounting.endMeasure(tagName);
                  }
                } //ACCOUNTING.end - END
              } else {
                removeAlias = false;
                //roll-back due to already used alias!! (the same alias but different application name)
              }
              break;
            }
          }
        }

        if (removeAlias || !aliasFoundInDeployedApps) {
          //!aliasFoundInDeployedApps => rollback but not due to the same alias name
          try {
            if (Accounting.isEnabled()) { //ACCOUNTING.end - BEGIN
              tagName = debugInfo + "/removeApplicationAlias (" + aliasCanonicalized + ")";
              Accounting.beginMeasure(tagName, HttpProvider.class);
            } //ACCOUNTING.start - END
            ServiceContext.getServiceContext().getHttpProvider().removeApplicationAlias(aliasCanonicalized, false);
          } catch (OutOfMemoryError e) {
            throw e;
          } catch (ThreadDeath e) {
            throw e;
          } catch (Throwable e) {
          	warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
                	WebWarningException.ERROR_IN_REMOVING_APPLICATION_ALIAS,
                	new Object[]{aliasCanonicalized, e.toString()}));
          } finally { //ACCOUNTING.end - BEGIN
            if (Accounting.isEnabled()) {
              Accounting.endMeasure(tagName);
            }
          } //ACCOUNTING.end - END
        }
      }
      
      try {
        if (Accounting.isEnabled()) { //ACCOUNTING.end - BEGIN
          tagName = debugInfo + "/changeLoadBalance";
          Accounting.beginMeasure(tagName, HttpProvider.class);
        } //ACCOUNTING.start - END
        //TODO This is only a workaround and MUST be think about correct way to unregister 'lazy' applications from HTTP provider and ICM.
        if (ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStartUpMode(applicationName) == StartUp.LAZY.getId().byteValue()) {
          ServiceContext.getServiceContext().getHttpProvider().changeLoadBalance(applicationName, aliases, false);
          ServiceContext.getServiceContext().getWebContainer().startedApplications.remove(applicationName);
        }
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000093",
          "Cannot stop load balancing for [{0}] application.", new Object[]{applicationName}, e, null, null);
      } finally { //ACCOUNTING.end - BEGIN
        if (Accounting.isEnabled()) {
          Accounting.endMeasure(tagName);
        }
      } //ACCOUNTING.end - END
    } else {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000094",
        "Cannot stop load balancing for [{0}] application.", new Object[]{applicationName}, null, null);
    }

    try {
      runtimeInterface.removeApp(applicationName);
    } catch (RemoteException e) {
    	warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            	WebWarningException.CANNOT_NOTIFY_FOR_REMOVING_APPLICATION,
    	        new Object[]{applicationName, e.toString()}));
    } catch (RuntimeException e) {
    	warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            	WebWarningException.CANNOT_NOTIFY_FOR_REMOVING_APPLICATION,
    	        new Object[]{applicationName, e.toString()}));
    }
    makeWarningException(warnings);
  }//end of notifyRemove(String applicationName)

}//end of class
