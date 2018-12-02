/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.runtime.client;

import java.util.Hashtable;

import com.sap.engine.services.httpserver.interfaces.ErrorData;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.RequestPathMappings;
import com.sap.engine.services.httpserver.interfaces.client.Request;
import com.sap.engine.services.httpserver.interfaces.client.Response;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.session.runtime.http.HttpSessionRequest;
import com.sap.engine.lib.util.HashMapObjectLong;

public class HttpParametersWrapper implements HttpParameters {

  private HttpParameters httpParameters = null;
  private HttpSessionRequest sessionRequest = null;

  public HttpParametersWrapper(HttpParameters httpParameters, HttpSessionRequest sessionRequest) {
    this.httpParameters = httpParameters;
    this.sessionRequest = sessionRequest;
  }

  public Object getApplicationSession() {
    Object appSession = null;
    try {
      if (this.sessionRequest.isRequested()) {
        appSession = this.sessionRequest.getSession(false);
      }
    } catch (IllegalStateException ilSt) {
      appSession = null;
    }
    return appSession;
  }

  public ErrorData getErrorData() {
    return httpParameters.getErrorData();
  }

  public String getHostName() {
    return httpParameters.getHostName();
  }

  public Request getRequest() {
    return httpParameters.getRequest();
  }

  public Hashtable getRequestAttributes() {
    return httpParameters.getRequestAttributes();
  }

  public MessageBytes getRequestParametersBody() {
    return httpParameters.getRequestParametersBody();
  }

  public RequestPathMappings getRequestPathMappings() {
    return httpParameters.getRequestPathMappings();
  }

  public Response getResponse() {
    return httpParameters.getResponse();
  }

  public String getServerURL(String arg0) {
    return httpParameters.getServerURL(arg0);
  }

  public HttpSessionRequest getSessionRequest() {
    return sessionRequest;
  }

  public void setSessionRequest(HttpSessionRequest sessionRequest) {
    this.sessionRequest = sessionRequest;
  }

  public boolean isDebugRequest() {
    return httpParameters.isDebugRequest();
  }

  public boolean isProtected() {
    return httpParameters.isProtected();
  }

  public boolean isSetApplicationCookie() {
    return httpParameters.isSetApplicationCookie();
  }

  public boolean isSetSessionCookie() {
    return httpParameters.isSetSessionCookie();
  }

  public void redirect(byte[] arg0) {
    httpParameters.redirect(arg0);
  }

  public MessageBytes replaceAliases(MessageBytes arg0) {
    return httpParameters.replaceAliases(arg0);
  }

  public void setApplicationCookie(boolean arg0) {
    httpParameters.setApplicationCookie(arg0);
  }

  public void setApplicationSession(Object arg0) {
    httpParameters.setApplicationSession(arg0);
  }

  public void setDebugRequest(boolean arg0) {
    httpParameters.setDebugRequest(arg0);
  }

  public void setErrorData(ErrorData arg0) {
    httpParameters.setErrorData(arg0);
  }

  public void setProtected(boolean arg0) {
    httpParameters.setProtected(arg0);
  }

  public void setRequestAttribute(String arg0, Object arg1) {
    httpParameters.setRequestAttribute(arg0, arg1);
  }

  public void setResponseLength(int arg0) {
    httpParameters.setResponseLength(arg0);
  }

  public void setSessionCookie(boolean arg0) {
    httpParameters.setSessionCookie(arg0);
  }

  public Object clone() {
    return httpParameters.clone();
  }

  public HashMapObjectLong getTimeStatisticsMap() {
    return httpParameters.getTimeStatisticsMap();
  }//end of getTimeStatisticsMap()

  public int getTraceResponseTimeAbove() {
    return httpParameters.getTraceResponseTimeAbove();
  }// end of getTraceResponseTimeAbove()

  public boolean isMemoryTrace() {
    return httpParameters.isMemoryTrace();
  }

  public void setMemoryTrace(boolean arg0) {
    httpParameters.setMemoryTrace(arg0);
  }

  
  public boolean isPreserved() {
	return httpParameters.isPreserved();
  }

  public void preserveWithoutFinalizer() {
	httpParameters.preserveWithoutFinalizer();
  }
  
  public void preserveWithFinalizer() {
		httpParameters.preserveWithFinalizer();
  }

public void justNew() {
	httpParameters.justNew();
	
}

public void recycleNew() {
	httpParameters.recycleNew();
	
}

public void recycleReturn() {
	httpParameters.recycleReturn();
	
}

  
}
