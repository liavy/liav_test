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

import static com.sap.engine.services.servlets_jsp.server.deploy.WebContainer.*;

import com.sap.engine.frame.cluster.ClusterElement;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.deploy.DeployEvent;
import com.sap.engine.services.deploy.container.ContainerInfo;
import com.sap.engine.services.deploy.container.DeployCommunicator;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.op.util.StartUp;
import com.sap.engine.services.httpserver.interfaces.HttpProvider;
import com.sap.engine.services.httpserver.interfaces.exceptions.HttpShmException;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.WebContainerInterface;
import com.sap.engine.services.servlets_jsp.admin.WebContainerLazyMBeanProvider;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.IWebContainer;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WCEAppThreadInitializer;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerProvider;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.module.WebModule;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;

import java.io.File;
import java.rmi.RemoteException;

/**
 * @author Violeta Georgieva
 * @version 7.10
 */
public class DeployCallbackAction extends ActionBase {
  private static Location currentLocation = Location.getLocation(DeployCallbackAction.class);

  /**
   * @param iWebContainer
   * @param containerInfo
   * @param servletsAndJsp
   * @param runtimeInterface
   */
  public DeployCallbackAction(IWebContainer iWebContainer, ContainerInfo containerInfo,
    WebContainerHelper servletsAndJsp, WebContainerInterface runtimeInterface) {
    super(iWebContainer, containerInfo, servletsAndJsp, runtimeInterface);
  } // end of constructor

  /**
   * @param event
   * @param lazyMBeanProvider
   */
  public void processApplicationEvent(DeployEvent event, WebContainerLazyMBeanProvider lazyMBeanProvider) {
    if (event.getAction() == DeployEvent.LOCAL_ACTION_FINISH && event.getActionType() == DeployEvent.START_APP && "servlet_jsp".equals(event.whoCausedGroupOperation()) && 
        ServiceContext.getServiceContext().getClusterContext().getClusterElement(ServiceContext.getServiceContext().getServerId()).getRealState() == ClusterElement.RUNNING) {
      if (ServiceContext.getServiceContext().getWebContainer().allApplicationsStartedCounter.compareAndSet(1, 0)) {
        //CSN message 1554484 2008 (KM had didn't came up after restart.)
        if (ServiceContext.getServiceContext().getWebContainer().allApplicationsStarted) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000115", 
            "The event \"All Applications Started\" will be send for a second time. " + 
            "There might have problems because of these two events.", null, null);
          return;
        }
        
        ServiceContext.getServiceContext().getWebContainer().allApplicationsStarted = true;
            
        WCEAppThreadInitializer wceAppThreadInitializer = new WCEAppThreadInitializer(iWebContainer);
        
        String accountingTag = "Start WCEAppThreadInitializer for all applications started event.";
        try {
          ThreadWrapper.pushSubtask("All applications started event.", ThreadWrapper.TS_PROCESSING);
          
          if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
            Accounting.beginMeasure(accountingTag, DeployCommunicator.class);
          }//ACCOUNTING.start - END
          ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().execute(wceAppThreadInitializer);
        } catch (InterruptedException e) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000031", 
            "Thread interrupted while distributing [all applications started] event to web container extensions.", e, null, null);
        } finally {
          ThreadWrapper.popSubtask();
          if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
            Accounting.endMeasure(accountingTag);
          }//ACCOUNTING.end - END
        }
        
        if (LogContext.getLocationDeploy().beDebug() || LogContext.getLocationStatistics().beDebug()) {
          makeTimeStatistics();
        }
      } else {
        ServiceContext.getServiceContext().getWebContainer().allApplicationsStartedCounter.decrementAndGet();
      }
    } else if (event.getAction() == DeployEvent.LOCAL_ACTION_FINISH && event.getActionType() == DeployEvent.INITIAL_START_APPLICATIONS) {
      //CSN message 1554484 2008 (KM had didn't came up after restart.)
      if (ServiceContext.getServiceContext().getWebContainer().allApplicationsStarted) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000638", 
          "The event \"All Applications Started\" will be send for a second time. " + 
          "There might have problems because of these two events.", null, null);
        return;
      }
      
      ServiceContext.getServiceContext().getWebContainer().allApplicationsStarted = true;
      
      WCEAppThreadInitializer wceAppThreadInitializer = new WCEAppThreadInitializer(iWebContainer);
      
      String accountingTag = "Start WCEAppThreadInitializer for all applications started event.";
      try {
        ThreadWrapper.pushSubtask("All applications started event.", ThreadWrapper.TS_PROCESSING);
        
        if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
          Accounting.beginMeasure(accountingTag, DeployCommunicator.class);
        }//ACCOUNTING.start - END
        ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().execute(wceAppThreadInitializer);
      } catch (InterruptedException e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000639", 
          "Thread interrupted while distributing [all applications started] event to web container extensions.", e, null, null);
      } finally {
        ThreadWrapper.popSubtask();
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.endMeasure(accountingTag);
        }//ACCOUNTING.end - END
      }
      
      if (LogContext.getLocationDeploy().beDebug() || LogContext.getLocationStatistics().beDebug()) {
        makeTimeStatistics();
      }
    }

    // WEB-1923 TODO da se oprawi tuka ne se hwyrlq ediniq event - toni trqbwa da kaje
    if (event.getAction() == DeployEvent.LOCAL_ACTION_START &&
         (event.getActionType() == DeployEvent.INITIAL_START_APPLICATIONS || 
           (event.getActionType() == DeployEvent.START_APP && 
             ServiceContext.getServiceContext().getClusterContext().getClusterElement(ServiceContext.getServiceContext().getServerId()).getRealState() == ClusterElement.RUNNING))) {
      if (event.getComponentName() == null) {
        String[] allMyApplications = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getMyApplications();

        long newtime = System.currentTimeMillis();
        long newtime1 = newtime;
        if (allMyApplications != null && allMyApplications.length > 0) {
          for (int i = 0; i < allMyApplications.length; i++) {
            String applicationName = allMyApplications[i];

            String[] aliasesCanonicalized = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalized(applicationName);
            if (aliasesCanonicalized != null && aliasesCanonicalized.length > 0) {
              registerAliases(applicationName, aliasesCanonicalized, urlSessionTrackingPerModulePerApplication.remove(applicationName));
            }

            Location traceLocation = LogContext.getLocationDeploy();
            boolean beDebug = traceLocation.beDebug();
            // TODO This is only a workaround and MUST be think about correct way to register 'lazy' applications in HTTP provider and ICM.
            try {
              if (ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStartUpMode(applicationName) == StartUp.LAZY.getId().byteValue()) {
                HttpProvider httpProvider = ServiceContext.getServiceContext().getHttpProvider();
                if (httpProvider != null) {
                  String[] aliases = ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName);
                  if (aliases != null && aliases.length > 0) {
                    for (int j = 0; j < aliases.length; j++) {
                      String aliasCanonicalized = ParseUtils.convertAlias(aliases[j]);
                      String aliasDir = WebContainerHelper.getAliasDirName(aliasCanonicalized);

                      String webApplWorkDirName = WebContainerHelper.getDirName(new String[]{WebContainerHelper.getDeployTempDir(applicationName), aliasDir});
                      String rootDir = webApplWorkDirName + "root" + File.separator;

                      if (beDebug) {
                        newtime = System.currentTimeMillis();
                      }                      
                      if (aliasCanonicalized.equals("/")) {
                        WebContainerProvider webContainerProvider = (WebContainerProvider) ServiceContext.getServiceContext().getWebContainer().getIWebContainerProvider();                        
                        WebModule webModule = (WebModule) webContainerProvider.getDeployedAppl(aliasCanonicalized);
                        if (webModule == null) {         
                          // there is no default applications; this should not happen
                          httpProvider.startApplicationAlias(aliasCanonicalized, rootDir);
                          if (LogContext.getLocationDeploy().beDebug()) {
                            traceLocation.debugT("Starting default alias within application [" + applicationName + "] when there is no default application");
                          }
                        } else if (webContainerProvider.getDeployedAppls(aliasCanonicalized).size() == 1 || 
                            !applicationName.equalsIgnoreCase(WebContainer.sapDefaultApplName)) {
                          // If there is only one default application then register its root directory
                          // If there are more than 1 default applications then register the custom's default application root directory
                          httpProvider.startApplicationAlias(aliasCanonicalized, rootDir);
                          if (LogContext.getLocationDeploy().beDebug()) {
                            traceLocation.debugT("Starting default alias within application [" + applicationName + "].");
                          }
                        }
                      } else {
                        httpProvider.startApplicationAlias(aliasCanonicalized, rootDir);
                      }                                                                  
                      if (beDebug) {
                        newtime1 = System.currentTimeMillis();
                        traceLocation.debugT("prepareAppMetaData/startApplicationAlias(" + aliasCanonicalized + ") >>> " + (newtime1 - newtime));
                      }
                    }

                    try {
                      if (beDebug) {
                        newtime = System.currentTimeMillis();
                      }
                      httpProvider.changeLoadBalance(applicationName, aliases, true);
                      if (beDebug) {
                        newtime1 = System.currentTimeMillis();
                        traceLocation.debugT("prepareAppMetaData/changeLoadBalance(" + applicationName + ") >>> " + (newtime1 - newtime));
                      }
                    } catch (HttpShmException she) {
                      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000149", 
                        "Cannot start load balancing for 'Lazy' application [{0}].", new Object[]{applicationName}, she, null, null);
                    }
                    ServiceContext.getServiceContext().getWebContainer().startedApplications.add(applicationName);
                  } else {
                    LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000150", 
                      "Cannot start load balancing for 'Lazy' application [{0}].", new Object[]{applicationName}, null, null);
                  }
                }
              }
            } catch (RemoteException e) {
              LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000151", 
                "Cannot notify HTTP Provider for 'Lazy' application [{0}].", new Object[]{applicationName}, e, null, null);
            }
          }
        }
      }
    }

    if (event.getAction() == DeployEvent.LOCAL_ACTION_START && event.getActionType() == DeployEvent.STOP_APP && event.getComponentName() == null) {
      String[] allMyApplications = ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getMyApplications();
      if (allMyApplications != null && allMyApplications.length > 0) {
        for (int i = 0; i < allMyApplications.length; i++) {
          String applicationName = allMyApplications[i];

          // TODO This is only a workaround and MUST be think about correct way to unregister 'lazy' applications from HTTP provider and ICM.
          try {
            if (ServiceContext.getServiceContext().getDeployContext().getDeployCommunicator().getStartUpMode(applicationName) == StartUp.LAZY.getId().byteValue()) {
              HttpProvider httpProvider = ServiceContext.getServiceContext().getHttpProvider();
              if (httpProvider != null) {
                String[] aliases = ServiceContext.getServiceContext().getDeployContext().getAliases(applicationName);
                if (aliases != null && aliases.length > 0) {
                  try {
                    httpProvider.changeLoadBalance(applicationName, aliases, false);
                  } catch (HttpShmException she) {
                    LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000090", 
                      "Cannot stop load balancing for 'Lazy' application [{0}].", new Object[]{applicationName}, she, null, null);
                  }
                  ServiceContext.getServiceContext().getWebContainer().startedApplications.remove(applicationName);
                } else {
                  LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000091", 
                    "Cannot stop load balancing for 'Lazy' application [{0}].", new Object[]{applicationName}, null, null);
                }
              }
            }
          } catch (RemoteException e) {
            LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000092", 
              "Cannot notify HTTP Provider for 'Lazy' application [{0}].", new Object[]{applicationName}, e, null, null);
          }
        }
      }
    }

    if (lazyMBeanProvider != null) {
      processDeployEventForMBeans(event, lazyMBeanProvider);
    }

  }// end of processApplicationEvent(DeployEvent event)

  public void processDeployEventForMBeans(DeployEvent event, WebContainerLazyMBeanProvider lazyMBeanProvider) {
    Location traceLocation = LogContext.getLocationWebadmin();
    String applicationName = event.getComponentName();
    if (applicationName == null) {
      if (traceLocation.beDebug()) {
        traceLocation.debugT("Deploy event " + event + " does not contain application name.");
      }
      return;
    } else {
      try {
        String[] aliases = ServiceContext.getServiceContext().getDeployContext().getAliasesCanonicalizedIfExists(applicationName);
        if (aliases == null || aliases.length == 0) {
          if (traceLocation.beDebug()) {
            traceLocation.debugT("The application [" + applicationName + "] does not contain web modules.");
          }
          return;
        }
      } catch (DeploymentException e) {
        if (traceLocation.beDebug()) {
          traceLocation.debugT("Problems during retrieving web module names for the application [" + applicationName + "]. " +
          	"Error is: " + LogContext.getExceptionStackTrace(e));
        }        
        return;
      }
    }
    
    if (event.getAction() % 2 == 1) { // ACTION_FINISH or LOCAL_ACTION_FINISH
      if (event.getActionType() == DeployEvent.DEPLOY_APP) {
        if (event.getErrors() != null && event.getErrors().length != 0) {
          LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000514", 
            "Deploy event [{0}] contains errors and will be ignored.", new Object[]{event}, null, null);
          return;
        }

        if (traceLocation.beInfo()) {
          traceLocation.infoT("Received action of deployed application [" + applicationName + "].");
        }
        
        lazyMBeanProvider.registerInNamesCache(applicationName);
      }
      if (event.getActionType() == DeployEvent.UPDATE_APP) {
        if (event.getErrors() != null && event.getErrors().length != 0) {
          LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000182", 
            "Deploy event [{0}] contains errors and will be ignored.", new Object[]{event}, null, null);
          return;
        }

        if (traceLocation.beInfo()) {
          traceLocation.infoT("Received action of updated application [" + applicationName + "].");
        }

        lazyMBeanProvider.invalidateLazyMBeans(applicationName);
        lazyMBeanProvider.registerInNamesCache(applicationName);
      }
      if (event.getActionType() == DeployEvent.RUNTIME_CHANGES) {
        if (event.getErrors() != null && event.getErrors().length != 0) {
          LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000122", 
            "Deploy event [{0}] contains errors and will be ignored.", new Object[]{event}, null, null);
          return;
        }

        if (traceLocation.beInfo()) {
          traceLocation.infoT("Received action of changed application [" + applicationName + "].");
        }

        lazyMBeanProvider.invalidateLazyMBeans(applicationName);
        lazyMBeanProvider.registerInNamesCache(applicationName);
      } 
    } else { // ACTION_START or LOCAL_ACTION_START
      if (event.getActionType() == DeployEvent.REMOVE_APP) {
        if (traceLocation.beInfo()) {
          traceLocation.infoT("Received action of removed application [" + applicationName + "].");
        }
 
        lazyMBeanProvider.invalidateLazyMBeans(applicationName);
      }
    }
  }// end of processDeployEventForMBeans(DeployEvent event, WebContainerLazyMBeanProvider lazyMBeanProvider)
 
  private void makeTimeStatistics() {
    long wholeTime = applicationsStartupTime.get();
    long init = initStartupTime1.get() + initStartupTime2.get();
    long security = getSubjectStartupTime.get() + (doAsStartupTime.get() - initStartupTime2.get());
    long webContainerOnly = wholeTime - wceStartupTime.get() - init - security - parseTLD.get() - 
      loadTLDListenerFromClassLoaderRsourceTime.get() - loadClassTime.get();

    StringBuilder builder = new StringBuilder();

    char[] emptyString = new char[20];
    for (int i = 0; i < 20; i++) {
      emptyString[i] = ' ';
    } 
 
    builder.append("\r\n+------------------------------------------------------------------------------------------+--------------------+------+\r\n");
    builder.append("| Time for download applications files in web container                                    |");
    builder.append(emptyString, 0, 16 - Long.toString(downloadAppFilesTime.get()).length()).append(downloadAppFilesTime).append(" ms | 100% |\r\n");
    builder.append("+------------------------------------------------------------------------------------------+--------------------+------+\r\n");
    builder.append("| Time for starting applications in web container                                          |");
    builder.append(emptyString, 0, 16 - Long.toString(wholeTime).length()).append(wholeTime).append(" ms | 100% |\r\n");
    builder.append("+------------------------------------------------------------------------------------------+--------------------+------+\r\n");
    builder.append("|     Time in Web Container                                                                |");
    builder.append(emptyString, 0, 16 - Long.toString(webContainerOnly).length()).append(webContainerOnly).append(" ms | ");
    long percent = (wholeTime != 0) ? (webContainerOnly * 100) / wholeTime : 100;
    builder.append(emptyString, 0, 3 - Long.toString(percent).length()).append(percent).append("% |\r\n");
    builder.append("+------------------------------------------------------------------------------------------+--------------------+------+\r\n");
    builder.append("|     Time in WCE providers code (portal, wdp, etc.); method - onStart()                   |");
    builder.append(emptyString, 0, 16 - Long.toString(wceStartupTime.get()).length()).append(wceStartupTime).append(" ms | ");
    percent = (wholeTime != 0) ? (wceStartupTime.get() * 100) / wholeTime : 100;
    builder.append(emptyString, 0, 3 - Long.toString(percent).length()).append(percent).append("% |\r\n");
    builder.append("+------------------------------------------------------------------------------------------+--------------------+------+\r\n");
    builder.append("|     Time in applications code; method - init()                                           |");
    builder.append(emptyString, 0, 16 - Long.toString(init).length()).append(init).append(" ms | ");
    percent = (wholeTime != 0) ? (init * 100) / wholeTime : 100;
    builder.append(emptyString, 0, 3 - Long.toString(percent).length()).append(percent).append("% |\r\n");
    builder.append("+------------------------------------------------------------------------------------------+--------------------+------+\r\n");
    builder.append("|     Time in security infrastructure code; methods - getSubject() and doAs()              |");
    builder.append(emptyString, 0, 16 - Long.toString(security).length()).append(security).append(" ms | ");
    percent = (wholeTime != 0) ? (security * 100) / wholeTime : 100;
    builder.append(emptyString, 0, 3 - Long.toString(percent).length()).append(percent).append("% |\r\n");
    builder.append("+------------------------------------------------------------------------------------------+--------------------+------+\r\n");
    builder.append("|     Time in J2EE descriptors code; method - SchemaProcessor.parse(stream)                |");
    builder.append(emptyString, 0, 16 - Long.toString(parseTLD.get()).length()).append(parseTLD).append(" ms | ");
    percent = (wholeTime != 0) ? (parseTLD.get() * 100) / wholeTime : 100;
    builder.append(emptyString, 0, 3 - Long.toString(percent).length()).append(percent).append("% |\r\n");
    builder.append("+------------------------------------------------------------------------------------------+--------------------+------+\r\n");
    builder.append("|     Time in loading portlet TLD; method - loadTLDListenerFromClassLoaderRsource()        |");
    builder.append(emptyString, 0, 16 - Long.toString(loadTLDListenerFromClassLoaderRsourceTime.get()).length()).append(loadTLDListenerFromClassLoaderRsourceTime).append(" ms | ");
    percent = (wholeTime != 0) ? (loadTLDListenerFromClassLoaderRsourceTime.get() * 100) / wholeTime : 100;
    builder.append(emptyString, 0, 3 - Long.toString(percent).length()).append(percent).append("% |\r\n");
    builder.append("+------------------------------------------------------------------------------------------+--------------------+------+\r\n");
    builder.append("|     Time in loadClass()                                                                  |");
    builder.append(emptyString, 0, 16 - Long.toString(loadClassTime.get()).length()).append(loadClassTime).append(" ms | ");
    percent = (wholeTime != 0) ? (loadClassTime.get() * 100) / wholeTime : 100;
    builder.append(emptyString, 0, 3 - Long.toString(percent).length()).append(percent).append("% |\r\n");
    builder.append("+------------------------------------------------------------------------------------------+--------------------+------+\r\n");
    builder.append("| Time in WCE providers code (portal, wdp, etc.); method - allApplicationsStarted()        |");
    builder.append(emptyString, 0, 16 - Long.toString(allApplicationsStartedTime.get()).length()).append(allApplicationsStartedTime).append(" ms | 100% |\r\n");
    builder.append("+------------------------------------------------------------------------------------------+--------------------+------+\r\n");

    if (LogContext.getLocationDeploy().beDebug()) {
      LogContext.getLocationDeploy().debugT(builder.toString());
    } else if (LogContext.getLocationStatistics().beDebug()) {
      LogContext.getLocationStatistics().debugT(builder.toString());
    }
  }//makeTimeStatistics(long startupTime)
   
}//end of class
