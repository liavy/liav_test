/*
 * Copyright (c) 2002 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */

package com.sap.engine.services.servlets_jsp.server;

import com.sap.engine.frame.ApplicationServiceContext;
import com.sap.engine.frame.core.locking.LockingContext;
import com.sap.engine.frame.core.locking.TechnicalLockException;
import com.sap.engine.frame.core.locking.LockException;
import com.sap.tc.logging.Location;

/*
 *
 *
 * @author Velin Doychinov
 * @version 6.30
 */
public class LockContext {
  private static String LOCK_AREA = "SERVLET_AND_JSP_LOCK_AREA";
  private static String HTTP_LOCK_AREA = "_HTTP_LOCK_AREA_";
  public static String HTTP_UPLOADED_FILES_LOCK = "_HTTP_UPLOADED_FILES_LOCK_";
  private static Location currentLocation = Location.getLocation(LockContext.class);

  private LockingContext lock = null;
  private String deployOwner = null;

  public LockContext(ApplicationServiceContext sc) {
    lock = sc.getCoreContext().getLockingContext();
    try {
      deployOwner = lock.getAdministrativeLocking().createUniqueOwner();
    } catch (TechnicalLockException tlex) {
      deployOwner = "SERVLET_AND_JSP_OWNER";
    }
  }

  public void lock(String name) throws LockException, TechnicalLockException {
    if (lock != null) {
      if (name.equals(HTTP_UPLOADED_FILES_LOCK)) {
        lock.getAdministrativeLocking().lock(deployOwner, HTTP_LOCK_AREA, name, LockingContext.MODE_EXCLUSIVE_NONCUMULATIVE);
      } else {
        lock.getAdministrativeLocking().lock(deployOwner, LOCK_AREA, name, LockingContext.MODE_EXCLUSIVE_NONCUMULATIVE);
      }
    } else {
    	//TODO:Polly ok ?
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000138",
        "Cannot get a cluster lock for Web Container service. " +
        "Locking Manager is not available or not initialized. Synchronization problems may occur.", null, null);
    }
  }

  public void unlock(String name) throws TechnicalLockException {
    if (lock != null) {
      if (name.equals(HTTP_UPLOADED_FILES_LOCK)) {
        lock.getAdministrativeLocking().unlock(deployOwner, HTTP_LOCK_AREA, name, LockingContext.MODE_EXCLUSIVE_NONCUMULATIVE, true);
      } else {
        lock.getAdministrativeLocking().unlock(deployOwner, LOCK_AREA, name, LockingContext.MODE_EXCLUSIVE_NONCUMULATIVE, true);
      }
    } else {
    	//TODO:Polly ok ?
      LogContext.getCategory(LogContext.CATEGORY_SERVICE).logError(currentLocation, "ASJ.web.000139", 
        "Cannot release a cluster lock for Web Container service. " +
        "Locking Manager is not available or not initialized. Synchronization problems may occur.", null, null);
    }
  }
}
