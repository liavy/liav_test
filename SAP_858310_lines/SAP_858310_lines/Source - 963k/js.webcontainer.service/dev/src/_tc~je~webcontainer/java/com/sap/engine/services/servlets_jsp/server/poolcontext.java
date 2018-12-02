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
package com.sap.engine.services.servlets_jsp.server;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import com.sap.engine.lib.lang.ObjectPool;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacadeWrapper;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletResponseFacadeWrapper;
import com.sap.tc.logging.Location;

public class PoolContext {
  private ObjectPool requestsPool = null;
  private ObjectPool responsesPool = null;
  
  public PoolContext(int minSize, int maxSize, int decreaseCapacity) {
	requestsPool = new ObjectPool(minSize, maxSize, decreaseCapacity, HttpServletRequestFacadeWrapper.class);
    responsesPool = new ObjectPool(minSize, maxSize, decreaseCapacity, HttpServletResponseFacadeWrapper.class);
  }

  public HttpServletRequestFacadeWrapper getRequest() {
	    return (HttpServletRequestFacadeWrapper)requestsPool.getObject();
  }

  public void releaseRequest(HttpServletRequestFacadeWrapper request) {
  	if (LogContext.getLocationServletRequest().beWarning()) {
  		String checkReset = request.checkReset();
  		if (checkReset != null && !"".equals(checkReset)) {
  			LogContext.getLocation(LogContext.LOCATION_SERVLET_REQUEST)
				.traceWarning("ASJ.web.000567", "The servlet request object is not reset properly at the end " +
						"of the request processing. It will be returned to the pool unreset. This may cause " +
						"next requests to be incorrectly initialized.\r\n" +
						"Details: {0}" , new Object[]{checkReset}, null, null);
  		}
  	}
  	
    requestsPool.returnInPool(request);
  }
  
  public HttpServletResponseFacadeWrapper getResponse() {
    return (HttpServletResponseFacadeWrapper)responsesPool.getObject();
  }

  public void releaseResponse(HttpServletResponseFacadeWrapper response) {  	
  	if (LogContext.getLocationServletResponse().beWarning()) {
  		String checkReset = response.checkReset();
  		if (checkReset != null && ! "".equals(checkReset)) {
	  		LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE)
					.traceWarning("ASJ.web.000568", "The servlet response object is not reset properly at the end " +
							"of the request. It will be returned to the pool unreset. This may cause " +
							"next requests to be incorrectly initialized.\r\n" +
							"Details: {0}", new Object[]{checkReset}, null, null);
  		}
  	}
    responsesPool.returnInPool(response);
  }
  
  public void returnNewResponse() {  	
	    try{
    		responsesPool.returnInPool(new HttpServletResponseFacadeWrapper());
		} catch (IOException e) {
			LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE)
			.traceWarning("ASJ.web.000640", "A new ServletResponse  can not be added to the response pool\r\n" +
					"Details: {0}", new Object[]{e.getMessage()}, null, null);
		}
  }
  
  public void returnNewRequest() {  
	   requestsPool.returnInPool(new HttpServletRequestFacadeWrapper());
  }
  
}
