/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServer;
import javax.transaction.TransactionManager;

import com.sap.engine.frame.ServiceException;
import com.sap.engine.frame.container.event.ContainerEventListener;
import com.sap.engine.frame.container.monitor.ComponentMonitor;
import com.sap.engine.interfaces.security.SecurityContext;
import com.sap.engine.interfaces.shell.Command;
import com.sap.engine.interfaces.shell.ShellInterface;
import com.sap.engine.interfaces.webservices.server.management.WSManager;
import com.sap.engine.services.deploy.container.ContainerManagement;
import com.sap.engine.services.httpserver.interfaces.HttpProvider;
import com.sap.engine.services.servlets_jsp.admin.WebContainerLazyMBeanProvider;
import com.sap.engine.services.servlets_jsp.migration.MigrationManager;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.management.ServletJSPMBeanManager;
import com.sap.engine.services.servlets_jsp.server.shellcommands.CleanupError500Monitors;
import com.sap.engine.services.servlets_jsp.server.shellcommands.ListSessions;
import com.sap.engine.services.servlets_jsp.server.shellcommands.ListWCEProviders;
import com.sap.engine.services.servlets_jsp.server.shellcommands.UserSessionTracing;
import com.sap.engine.services.timeout.TimeoutManager;
import com.sap.tc.logging.Location;

public class WebContainerContainerEventListener implements ContainerEventListener {
  private static Location currentLocation = Location.getLocation(WebContainerContainerEventListener.class);
  private static Location traceLocation = LogContext.getLocationService();

  private ServiceContext serviceContext = null;

  private ShellInterface shell = null;
  private int commandId = -1;
  private HttpProvider httpProvider = null;
  private ContainerManagement deployService = null;

  Set initServices;

  public WebContainerContainerEventListener(ServiceContext serviceContext) {
    this.serviceContext = serviceContext;
  }

  public void register() {
  	initServices = Collections.synchronizedSet(new HashSet(7));
  	initIfAlreadyStarted(J2EEComponents.SERVICE_DEPLOY);
    initIfAlreadyStarted(J2EEComponents.SERVICE_HTTP);
    initIfAlreadyStarted(J2EEComponents.SERVICE_MONITOR);
    initIfAlreadyStarted(J2EEComponents.SERVICE_JMX);
    initIfAlreadyStarted(J2EEComponents.SERVICE_TS);
    initIfAlreadyStarted(J2EEComponents.SERVICE_BASICADMIN);
    initIfAlreadyStarted(J2EEComponents.SERVICE_TIMEOUT);

    int mask = ContainerEventListener.MASK_INTERFACE_AVAILABLE |
               ContainerEventListener.MASK_INTERFACE_NOT_AVAILABLE |
               ContainerEventListener.MASK_SERVICE_STARTED |
               ContainerEventListener.MASK_BEGIN_SERVICE_STOP;
    Set names = new HashSet(11, 1);
    names.add(J2EEComponents.INTERFACE_WEBSERVICES);
    names.add(J2EEComponents.INTERFACE_RESOURCECONTEXT);
    names.add(J2EEComponents.INTERFACE_SHELL);
    names.add(J2EEComponents.SERVICE_SECURITY);
    names.add(J2EEComponents.SERVICE_TS);
    names.add(J2EEComponents.SERVICE_HTTP);
    names.add(J2EEComponents.SERVICE_DEPLOY);
    names.add(J2EEComponents.SERVICE_MONITOR);
    names.add(J2EEComponents.SERVICE_JMX);
    names.add(J2EEComponents.SERVICE_BASICADMIN);
    names.add(J2EEComponents.SERVICE_TIMEOUT);
    serviceContext.getApplicationServiceContext().getServiceState().registerContainerEventListener(mask, names, this);
  }

  public void unregister() {
    try {
      interfaceNotAvailable(J2EEComponents.INTERFACE_SHELL);
    } catch (Exception e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000152",
              "Cannot unregister container event listener.", e, null, null);
    }
    try {
      interfaceNotAvailable(J2EEComponents.INTERFACE_WEBSERVICES);
    } catch (Exception e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000153",
              "Cannot unregister container event listener.", e, null, null);
    }
    try {
      interfaceNotAvailable(J2EEComponents.INTERFACE_RESOURCECONTEXT);
    } catch (Exception e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000154",
              "Cannot unregister container event listener.", e, null, null);
    }
    try {
      interfaceNotAvailable(J2EEComponents.SERVICE_SECURITY);
    } catch (Exception e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000155",
              "Cannot unregister container event listener.", e, null, null);
    }
    try {
      beginServiceStop(J2EEComponents.SERVICE_TS);
    } catch (Exception e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000156",
              "Cannot unregister container event listener.", e, null, null);
    }
    try {
      beginServiceStop(J2EEComponents.SERVICE_HTTP);
    } catch (Exception e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000157",
              "Cannot unregister container event listener.", e, null, null);
    }
    try {
      beginServiceStop(J2EEComponents.SERVICE_MONITOR);
    } catch (Exception e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000158",
              "Cannot unregister container event listener.", e, null, null);
    }
    try {
      beginServiceStop(J2EEComponents.SERVICE_DEPLOY);
    } catch (Exception e) {
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000159",
              "Cannot unregister container event listener.", e, null, null);
    }
    try {
        beginServiceStop(J2EEComponents.SERVICE_JMX);
      } catch (Exception e) {
        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000160",
                "Cannot unregister container event listener.", e, null, null);
      }
      try {
          beginServiceStop(J2EEComponents.SERVICE_BASICADMIN);
        } catch (Exception e) {
          LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000595",
                  "Cannot unregister container event listener.", e, null, null);
        }
     try {
            beginServiceStop(J2EEComponents.SERVICE_TIMEOUT);
          } catch (Exception e) {
            LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000688",
                    "Cannot unregister container event listener.", e, null, null);
          }
  }

  public void markForShutdown(long timeout) {
    serviceContext.setShutdownTime(System.currentTimeMillis() + timeout);
  }

  /**
   *
   * @deprecated - use runtime configuration: WebContainerProperties.updateProperties()
   */
  public boolean setServiceProperties(Properties sp) {
  	LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000161", "Changing service properties " +
  			"via deprecated method. Please, use runtime configuration", null, null);
 		//  return false - to restart the server
  	return !serviceContext.getWebContainerProperties().setServiceProperties(sp);
  }

  /**
   *
   * @deprecated - use runtime configuration
   */
  public boolean setServiceProperty(String name, String value) {
  	LogContext.getCategory(LogContext.CATEGORY_SERVICE).logWarning(currentLocation, "ASJ.web.000114", "An attempt to " +
  			"change service properties via deprecated method. Please, use runtime configuration.", null, null);
    // return false - to restart the server
    return !serviceContext.getWebContainerProperties().setServiceProperty(name, value);
  }

  public void beginContainerStop() {

  }

  public void containerStarted() {

  }

  /**
   * Invoked after the service with specified name had been started
   *
   * @param   serviceName  the service which is stopped
   * @param   serviceInterface  if true the service is start successful
   */
  public void serviceStarted(String serviceName, Object serviceInterface) {
  	if (initServices.contains(serviceName)) {
  		return;
  	}
    if (serviceName.equals(J2EEComponents.SERVICE_HTTP)) {
      httpProvider = (HttpProvider) serviceInterface;
      serviceContext.setHttpProvider(httpProvider);
      httpProvider.registerHttpHandler(serviceContext.getHttpHandler());
      initServices.add(serviceName);
    } else if (serviceName.equals(J2EEComponents.SERVICE_DEPLOY)) {
			deployService = (ContainerManagement) (serviceContext.getApplicationServiceContext().getContainerContext().getObjectRegistry().getProvidedInterface("container"));
  		try {
  			serviceContext.getDeployContext().setDeployCommunicator(deployService.registerContainer(serviceContext.getWebContainer().getContainerInfo().getName(), serviceContext.getWebContainer()));
  			try {
  				new MigrationManager(serviceContext.getApplicationServiceContext()).initializeMigration();
  			} catch (ServiceException se) {
  				LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000162", 
  				  "Problems during migration.", se, null, null);
  				throw new ServiceException(se);
  			}
  		} catch (OutOfMemoryError e) {
  			throw e;
  		} catch (ThreadDeath e) {
  			throw e;
  		} catch (Throwable rexc) {
  			rexc.printStackTrace();
  			LogContext.getCategory(LogContext.CATEGORY_SERVICE).logFatal(currentLocation, "ASJ.web.000320", 
  			  "Cannot register the web container in deploy service.", rexc, null, null);
  		}
    	initServices.add(serviceName);
    } else if (serviceName.equals(J2EEComponents.SERVICE_MONITOR)) {
    	serviceContext.getWebMonitoring().monitoringStarted(true);
	    initServices.add(serviceName);
    }else if (serviceName.equals(J2EEComponents.SERVICE_JMX)) {
    	Object jmxi = serviceContext.getApplicationServiceContext().getContainerContext().getObjectRegistry().getServiceInterface("jmx");
    	if (jmxi!= null && jmxi instanceof MBeanServer){
    		ServletJSPMBeanManager mngr = ServletJSPMBeanManager.initManager((MBeanServer)jmxi,serviceContext);
        	mngr.startAll();
    	}
    	initServices.add(serviceName);
    } else if (serviceName.equals(J2EEComponents.SERVICE_BASICADMIN)) {
    	Object jmxi = serviceContext.getApplicationServiceContext().getContainerContext().getObjectRegistry().getServiceInterface("jmx");
    	if (jmxi!= null && jmxi instanceof MBeanServer){
        	WebContainerLazyMBeanProvider lazyProvider = new WebContainerLazyMBeanProvider((MBeanServer)jmxi);
        	serviceContext.getWebContainer().setWebContainerLazyMBeanProvider(lazyProvider);
	       	lazyProvider.registerProvider();
	}
    	initServices.add(serviceName);
    }else if (serviceName.equals(J2EEComponents.SERVICE_TS)) {
      serviceContext.setTransactionManager((TransactionManager)serviceInterface);
      initServices.add(serviceName);
    }else if (serviceName.equals(J2EEComponents.SERVICE_TIMEOUT)){
    	serviceContext.setTimeoutManager((TimeoutManager)serviceInterface);
    	initServices.add(serviceName);
    }
  }

	public void serviceNotStarted(String serviceName) {

  }

  //stop
  public void beginServiceStop(String serviceName) {
  	if (!initServices.remove(serviceName)) {
  		return;
  	}
    if (serviceName.equals(J2EEComponents.SERVICE_HTTP)) {
      httpProvider.registerHttpHandler(null);
      serviceContext.setHttpProvider(null);
      httpProvider = null;
    } else if (serviceName.equals(J2EEComponents.SERVICE_DEPLOY)) {
      try {
        deployService.unregisterContainer(J2EEComponents.SERVICE_SERVLET_JSP);
      } catch (Exception rexc) {
    	  //TODO:Polly ok
        LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000163",
            "Cannot unregister the web container from deploy service.", rexc, null, null);
      }
      serviceContext.getDeployContext().setDeployCommunicator(null);
      deployService = null;
    } else if (serviceName.equals(J2EEComponents.SERVICE_MONITOR)) {
      serviceContext.getWebMonitoring().monitoringStarted(false);
    }else if (serviceName.equals(J2EEComponents.SERVICE_JMX)) {
    	Object jmxi = serviceContext.getApplicationServiceContext().getContainerContext().getObjectRegistry().getServiceInterface("jmx");
    	if (jmxi!= null && jmxi instanceof MBeanServer){
    		ServletJSPMBeanManager mngr = ServletJSPMBeanManager.initManager((MBeanServer)jmxi,serviceContext);
        	mngr.stopAll();
    	}
    } else if (serviceName.equals(J2EEComponents.SERVICE_BASICADMIN)) {
       	WebContainerLazyMBeanProvider lazyProvider = serviceContext.getWebContainer().getWebContainerLazyMBeanProvider();
       	lazyProvider.unregisterProvider();
      	serviceContext.getWebContainer().setWebContainerLazyMBeanProvider(null);
    } else if (serviceName.equals(J2EEComponents.SERVICE_TS)) {
      serviceContext.setTransactionManager(null);
    } else if (serviceName.equals(J2EEComponents.SERVICE_TIMEOUT)) {
      serviceContext.setTimeoutManager(null);
    }
    
  }

  //stop
  public void serviceStopped(String serviceName) {
    beginServiceStop(serviceName);
  }

  public void interfaceAvailable(String interfaceName, Object interfaceImpl) {
    if (interfaceName.equals(J2EEComponents.INTERFACE_WEBSERVICES)) {
      serviceContext.setWSManager((WSManager)interfaceImpl);
    } else if (interfaceName.equals(J2EEComponents.INTERFACE_SHELL)) {
      if (traceLocation.beInfo()) {
				traceLocation.infoT("interfaceAvailable [shell]");
			}
			shell = (ShellInterface)interfaceImpl;
      Command cmds[] = {new ListSessions(serviceContext.getDeployContext()), 
          new ListWCEProviders(),
          new UserSessionTracing(),
          new CleanupError500Monitors()};
      commandId = shell.registerCommands(cmds);
      if (traceLocation.beInfo()) {
				traceLocation.infoT("interfaceAvailable [shell] successfully processed");
			}
    } else if (interfaceName.equals(J2EEComponents.INTERFACE_RESOURCECONTEXT)) {
      ApplicationContext.setResourceContextFactory(interfaceImpl);
    } else if (interfaceName.equals(J2EEComponents.SERVICE_SECURITY)) {
      serviceContext.setSecurityContext((SecurityContext)interfaceImpl);
    }
  }

  public void interfaceNotAvailable(String interfaceName) {
    if (interfaceName.equals(J2EEComponents.INTERFACE_SHELL)) {
      if (traceLocation.beInfo()) {
				traceLocation.infoT("interfaceNotAvailable [shell]");
			}
			if (shell != null) {
        shell.unregisterCommands(commandId);
        shell = null;
      }
      if (traceLocation.beInfo()) {
				traceLocation.infoT("interfaceNotAvailable [shell] successfully processed");
			}
    } else if (interfaceName.equals(J2EEComponents.INTERFACE_RESOURCECONTEXT)) {
      ApplicationContext.setResourceContextFactory(null);
    } else if (interfaceName.equals(J2EEComponents.INTERFACE_WEBSERVICES)) {
      serviceContext.setWSManager(null);
    } else if (interfaceName.equals(J2EEComponents.SERVICE_SECURITY)) {
      serviceContext.setSecurityContext(null);
    }
  }

  private void initIfAlreadyStarted(String serviceName) {
    if (serviceContext.getApplicationServiceContext().getContainerContext().getSystemMonitor().getService(serviceName) != null) {
      if (serviceContext.getApplicationServiceContext().getContainerContext().getSystemMonitor().getService(serviceName).getStatus() == ComponentMonitor.STATUS_ACTIVE) {
      	serviceStarted(serviceName, serviceContext.getApplicationServiceContext().getContainerContext().getObjectRegistry().getServiceInterface(serviceName));
      }
    }
  }
}
