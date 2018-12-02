package com.sap.engine.services.servlets_jsp.server.deploy.impl;

import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;

/**
 * Destroys WCE components in an application thread.
 *
 * @author Violeta Georgieva
 * @version 7.10
 */
public class WCEAppThreadDestroyer implements Runnable {
  private static Location currentLocation = Location.getLocation(WCEAppThreadDestroyer.class);

  private IWebContainer iWebContainer = null;
  private String applicationName = null;
  private String[] aliases = null;

  public WCEAppThreadDestroyer(IWebContainer iWebContainer, String applicationName, String[] aliases) {
    this.iWebContainer = iWebContainer;
    this.applicationName = applicationName;
    this.aliases = aliases;
  }//end of constructor

  public void run() {
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("WCEAppThreadDestroyer", WCEAppThreadDestroyer.class);
      }//ACCOUNTING.start - END

      ThreadWrapper.pushTask("Destroying web container extension's components for application ["
      		+ applicationName + "].", ThreadWrapper.TS_PROCESSING);
      iWebContainer.stop(applicationName, aliases);
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000003",
        "Error occurred when invoking stop methods of web container extensions for [{0}] application.",
        new Object[]{applicationName}, e, null, null);
    } finally {
      ThreadWrapper.popTask();
      
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure("WCEAppThreadDestroyer");
      }//ACCOUNTING.end - END
    }
  }//end of run()

}//end of class
