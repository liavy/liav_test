/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.lib;

import java.util.Enumeration;
import java.util.Locale;
import java.util.NoSuchElementException;

import com.sap.engine.services.servlets_jsp.server.LogContext;

public class DefaultLocaleEnumeration implements Enumeration {
  private boolean hasMore = true;

  public boolean hasMoreElements() {
    return hasMore;
  }

  public Object nextElement() {
    if (hasMore) {
      hasMore = false;
      return Locale.getDefault();
    }
    try { //temporary try-catch
			if (LogContext.getLocationServletRequest().beWarning()) { 
				LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST).traceWarning("ASJ.web.000246",
						"DefaultLocaleEnumeration is incorrectly accessed when it has no more elements. "
							+ "This may cause the ServletRequest's method getLocale incorrectly to return null and the client request to fail.",
						new NoSuchElementException("Trying to retrieve locale from DefaultLocaleEnumeration when there are no more elements in the enumeration."), null, null);
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
    return null;
  }
}
