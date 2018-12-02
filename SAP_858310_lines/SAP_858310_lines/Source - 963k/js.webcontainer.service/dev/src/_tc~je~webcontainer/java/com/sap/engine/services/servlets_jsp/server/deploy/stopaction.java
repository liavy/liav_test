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

import static com.sap.engine.services.servlets_jsp.server.LogContext.CATEGORY_DEPLOY;
import static com.sap.engine.services.servlets_jsp.server.ServiceContext.getServiceContext;
import static com.sap.engine.services.servlets_jsp.server.exceptions.WebWarningException.*;

import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.deploy.container.ContainerInfo;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.httpserver.interfaces.HttpProvider;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.servlets_jsp.WebContainerInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.WebResourceAccessor;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.utils.NamingUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WCEAppThreadDestroyer;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerProvider;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.module.WebModule;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.system.ThreadWrapper;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public class StopAction extends ActionBase {
  private static Location currentLocation = Location.getLocation(StopAction.class);

  /**
   * @param iWebContainer
   * @param containerInfo
   * @param servletsAndJsp
   * @param runtimeInterface
   */
  public StopAction(IWebContainer iWebContainer, ContainerInfo containerInfo, WebContainerHelper servletsAndJsp,
                    WebContainerInterface runtimeInterface) {
    super(iWebContainer, containerInfo, servletsAndJsp, runtimeInterface);
  }//end of constructor

  /**
   * @param applicationName
   * @throws WarningException
   */
  public void commitStop(String applicationName) throws WarningException {
    ArrayList<LocalizableTextFormatter> warnings = new ArrayList<LocalizableTextFormatter>();

    String[] aliasesCannonicalized = getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);

    if (aliasesCannonicalized != null && aliasesCannonicalized.length > 0) {
      //Check whether the web application is pure web application or not
      //If it is pure web application we will not sent events to the WCE providers
      //This is an optimization - do not start application thread
      if (!isPureWebApplication(aliasesCannonicalized, applicationName)) {
        String accountingTag0 = "commitStop/onStop(" + applicationName + ")";
        if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
          Accounting.beginMeasure(accountingTag0, DeployCommunicator.class);
        }//ACCOUNTING.start - END

        destroyWCEComponents(iWebContainer, applicationName, aliasesCannonicalized); //does not remove the alias from runtime structure

        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.endMeasure(accountingTag0);
        }//ACCOUNTING.end - END
      }

      for (int i = 0; i < aliasesCannonicalized.length; i++) {
        String aliasURL = aliasesCannonicalized[i];

        ApplicationContext scf = getServiceContext().getDeployContext().getStartedWebApplicationContext(new MessageBytes(aliasURL.getBytes()));
        if (scf == null) {
        	warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        	  CANNOT_STOP_WEB_APPLICATION,new Object[]{applicationName}));
        	continue;
        } else {
          // in case of more than one scf for one alias check if it is invoked destroy
          // of the correct context. Case for more than 2 contexts per alias could be
          // default alias - SAP and customer default alias
          if (!scf.getApplicationName().equalsIgnoreCase(applicationName)) {
            // destroy the scf of the dest
            continue;
          }
        }

        String accountingTag1 = "commitStop/destroyWebAppComponents(" + aliasURL + ")";
        if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
          Accounting.beginMeasure(accountingTag1, StopAction.class);
        }//ACCOUNTING.start - END

        destroyWebAppComponents(scf);

        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.endMeasure(accountingTag1);
        }//ACCOUNTING.end - END

        //after that it is no more possible to get web module contexts for this stopping application:
        iWebContainer.removeWebModuleContextFromCache(aliasesCannonicalized[i]);

        scf.destroy();

        webContainerHelper.removeStartedApplication(new MessageBytes(aliasURL.getBytes()), applicationName);

        HttpProvider httpProvider = getServiceContext().getHttpProvider();
        if (httpProvider != null) {
          String accountingTag2 = "commitStop/HTTP Service.clearCache(" + aliasesCannonicalized[i] + ")";
          if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
            Accounting.beginMeasure(accountingTag2, HttpProvider.class);
          }//ACCOUNTING.start - END

          httpProvider.clearCacheByAlias(aliasesCannonicalized[i]);

          if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
            Accounting.endMeasure(accountingTag2);
          }//ACCOUNTING.end - END
        } else {
        	warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
        	  HHTP_SERVICE_IS_NOT_STOPPED_AND_CANNOT_CLEAR_HTTP_CACHE, new Object[]{applicationName}));
        }
      }
    }

    String accountingTag3 = "commitStop/Unregister private class loader (" + applicationName + ")";
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag3, getServiceContext().getLoadContext().getClass());
      }//ACCOUNTING.start - END

      ClassLoader privateClassLoader = getServiceContext().getLoadContext().getClassLoader(Constants.PRIVATE_CL_NAME + applicationName);
      if (privateClassLoader != null) {
        getServiceContext().getLoadContext().unregister(privateClassLoader);
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000097",
        "Error while unregistering private class loader for [{0}] application.", new Object[]{applicationName}, e, null, null);
    } finally {
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag3);
      }//ACCOUNTING.end - END
    }

    String accountingTag4 = "commitStop/unBindApp(" + applicationName + ")";
    if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
      Accounting.beginMeasure(accountingTag4, StopAction.class);
    }//ACCOUNTING.start - END

    NamingUtils.unBindApp(applicationName);

    try {
      runtimeInterface.appStopped(applicationName);
    } catch (RemoteException e) {
    	warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
    	  CANNOT_NOTIFY_RUNTIME_INTERFACE_FOR_CHANGING_STATUS_OF_APPLICATION_ERROR_IS,
        new Object[]{applicationName, e.toString()}));
    } catch (RuntimeException e) {
    	warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
    	  CANNOT_NOTIFY_RUNTIME_INTERFACE_FOR_CHANGING_STATUS_OF_APPLICATION_ERROR_IS,
        new Object[]{applicationName, e.toString()}));
    }

    if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
      Accounting.endMeasure(accountingTag4);
    }//ACCOUNTING.end - END

    makeWarningException(warnings);
  }//end of commitStop(String applicationName)

  private void destroyWCEComponents(IWebContainer iWebContainer, String applicationName, String[] aliases) {
    WCEAppThreadDestroyer wceAppThreadDestroyer = new WCEAppThreadDestroyer(iWebContainer, applicationName, aliases);

    try {
      ThreadWrapper.pushSubtask("Destroying application [" + applicationName + "]", ThreadWrapper.TS_PROCESSING);

      getServiceContext().getDeployContext().getDeployCommunicator().execute(wceAppThreadDestroyer);
    } catch (InterruptedException e) {
      LogContext.getCategory(CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000035",
        "Thread interrupted while invoking stop methods of web container extensions for [{0}] application.",
        new Object[]{applicationName}, e, null, null);
    } finally {
      ThreadWrapper.popSubtask();
    }
  }//end of destroyWCEComponents(IWebContainer iWebContainer, String applicationName, String[] aliases)

  private boolean isPureWebApplication(String[] aliasesCanonicalized, String applicationName) {
    WebModule webModule;
    for (String alias : aliasesCanonicalized) {
      webModule = (WebModule) (((WebContainerProvider) iWebContainer).getDeployedAppl(alias, applicationName));
      if (webModule != null && webModule.getWceInDeploy().size() > 0) {
        return false;
      }
    }
    return true;
  }//end of isPureWebApplication(String[] aliasesCanonicalized)

}//end of class
