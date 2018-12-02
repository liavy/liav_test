/*
* Copyright (c) 2006-2009 by SAP AG, Walldorf.,
* http://www.sap.com
* All rights reserved.
*
* This software is the confidential and proprietary information
* of SAP AG, Walldorf. You shall not disclose such Confidential
* Information and shall use it only in accordance with the terms
* of the license agreement you entered into with SAP.
*/
package com.sap.engine.services.servlets_jsp.server.deploy.impl;

import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;

import javax.servlet.ServletContext;

/**
 * Initialize WCE components in an application thread.
 *
 * @author Violeta Georgieva
 * @version 7.10
 */
public class WCEAppThreadInitializer implements Runnable {
  private static Location currentLocation = Location.getLocation(WCEAppThreadInitializer.class);

  private IWebContainer iWebContainer = null;
  private String applicationName = null;
  private String[] aliases = null;
  private ServletContext[] servletContexts = null;
  private ClassLoader publicClassloader = null;
  private boolean allApplicationsStarted = false;

  private Throwable exceptionsDuringInit = null;

  /**
   * Invoked on application start
   * @param countDown
   * @param iWebContainer
   * @param applicationName
   * @param aliases
   * @param servletContexts
   * @param publicClassloader
   */
  public WCEAppThreadInitializer(IWebContainer iWebContainer, String applicationName, String[] aliases,
                                 ServletContext[] servletContexts, ClassLoader publicClassloader) {
    this.iWebContainer = iWebContainer;
    this.applicationName = applicationName;
    this.aliases = aliases;
    this.servletContexts = servletContexts;
    this.publicClassloader = publicClassloader;
  }//end of constructor

  /**
   * Invoked on engine startup when all applications are started
   * @param countDown
   * @param iWebContainer
   */
  public WCEAppThreadInitializer(IWebContainer iWebContainer) {
    this.iWebContainer = iWebContainer;
    this.allApplicationsStarted = true;
  }//end of constructor

  public void run() {
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WCEAppThreadInitializer", WCEAppThreadInitializer.class);
      }//ACCOUNTING.start - END

      ThreadWrapper.pushTask("Initializing WCE components for application [" 
      		+ applicationName + "] in WebContainer.", ThreadWrapper.TS_PROCESSING);
      if (!allApplicationsStarted) {
        iWebContainer.start(applicationName, aliases, servletContexts, publicClassloader);
      } else {
        iWebContainer.allApplicationsStarted();
      }
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (WarningException e) {
      if (!allApplicationsStarted) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000004",
          "Error occurred when invoking start methods of web container extensions for [{0}] application.", 
          new Object[]{applicationName}, e, null, null);
      } else {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation,"ASJ.web.000005",
          "Error occurred when distributing [all applications started] event to web container extensions.", e, null, null);
      }
    } catch (Throwable e) {
      if (!allApplicationsStarted) {
        exceptionsDuringInit = new WebDeploymentException(WebDeploymentException.ERROR_OCCURRED_STARTING_WCE_COMPONENTS, new Object[]{applicationName}, e);
      } else {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000006",
          "Error occurred when distributing [all applications started] event to web container extensions.", e, null, null);
      }
    } finally {
      ThreadWrapper.popTask();
      
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WCEAppThreadInitializer");
      }//ACCOUNTING.end - END
    }
  }//end of run()

  public Throwable getExceptionDuringInit() {
    return exceptionsDuringInit;
  }//end of getExceptionDuringInit()

}//end of class
