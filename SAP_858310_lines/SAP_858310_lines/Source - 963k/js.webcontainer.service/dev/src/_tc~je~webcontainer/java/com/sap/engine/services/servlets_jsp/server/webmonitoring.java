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

import com.sap.engine.services.servlets_jsp.Monitoring;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.timeout.TimeoutListener;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;
import com.sap.engine.services.httpserver.server.errorreport.CategorizationEntry_ISE_500;

public class WebMonitoring implements Monitoring, TimeoutListener {
  private DeployContext deployContext = null;
  private boolean isMonitoringStarted = false;
  
  //ISE 500 monitors clean up related flags 
  private long timeoutSeconds = ServiceContext.getServiceContext().getWebContainerProperties().getError500MonitorsCleanupPeriod();
  private boolean isTimeoutListenerRegistered = false;
  private boolean isTimeoutDisabledByUser = false;

  private AtomicLong httpSessionsInvalidatedByApplication = new AtomicLong(0);
  private AtomicLong timedOutHttpSessions = new AtomicLong(0);
  private AtomicLong securitySessionsInvalidatedByApplication = new AtomicLong(0);
  private AtomicLong timedOutSecuritySessions = new AtomicLong(0);
  private AtomicLong allRequestsCount = new AtomicLong(0);
  private AtomicLong allResponsesCount = new AtomicLong(0);
  private AtomicLong totalResponseTime = new AtomicLong(0);
  private AtomicLong error500Count = new AtomicLong(0);
  private ConcurrentHashMap<Integer, CategorizationEntry_ISE_500 > allError500Categorizations = new ConcurrentHashMap<Integer, CategorizationEntry_ISE_500>();
  
  public WebMonitoring(DeployContext deployContext) {
    this.deployContext = deployContext;
  }

  public void monitoringStarted(boolean isStarted) {
    isMonitoringStarted = isStarted;
  }

  public boolean isMonitoringStarted() {
    return isMonitoringStarted;
  }

  public int getCurrentHttpSessions() {
    int sessionsCount = 0;
    Enumeration en = deployContext.getStartedWebApplications();
    while (en.hasMoreElements()) {
      ApplicationContext application = (ApplicationContext)en.nextElement();
      sessionsCount += application.getSessionServletContext().getSession().size();
    }
    return sessionsCount;
  }

  public long getHttpSessionsInvalidatedByApplication() {
    return httpSessionsInvalidatedByApplication.get();
  }

  public long getTimedOutHttpSessions() {
    return timedOutHttpSessions.get();
  }

  public int getCurrentSecuritySessions() {
    int sessionsCount = 0;
    Enumeration en = deployContext.getStartedWebApplications();
    while (en.hasMoreElements()) {
      ApplicationContext application = (ApplicationContext)en.nextElement();
      sessionsCount += application.getSessionServletContext().getPolicyDomain().getSecuritySessions().size();
    }
    return sessionsCount;
  }

  public long getSecuritySessionsInvalidatedByApplication() {
    return securitySessionsInvalidatedByApplication.get();
  }

  public long getTimedOutSecuritySessions() {
    return timedOutSecuritySessions.get();
  }

  public long getAllRequestsCount() {
    return allRequestsCount.get();
  }

  public long getAllResponsesCount() {
    return allResponsesCount.get();
  }

  public long getTotalResponseTime() {
    return totalResponseTime.get();
  }

  public long getError500Count(){
	  return error500Count.get();
  }
  
  public Serializable[][] getError500CategorizationEntries(){
	  //this is a workaround because the monitor service cannot interpret empty Serializable matrix correctly
	  Serializable [][] catEntriesResult=new Serializable[1][3];
	  catEntriesResult[0][0] = "";
	  catEntriesResult[0][1] = "";
	  catEntriesResult[0][2] = "";
	  //end of workaround 
	  if (!allError500Categorizations.isEmpty()){
		  catEntriesResult= new Serializable[allError500Categorizations.size()][];
		  ArrayList<CategorizationEntry_ISE_500> catEntriesArray = new ArrayList<CategorizationEntry_ISE_500>(allError500Categorizations.values());
		  Comparator<CategorizationEntry_ISE_500> descendingOrderComparator = Collections.reverseOrder();
		  Collections.sort(catEntriesArray, descendingOrderComparator);
		  int i =0;
		  for(CategorizationEntry_ISE_500 catEntry: catEntriesArray){
			  if (i==100) {break;}
			  catEntriesResult[i] = new Serializable []{catEntry.getCategorizationId(), catEntry.getOcurrencesNumber(), catEntry.getErrorReportFiles()};
			  i++;
		  }
		  
 	  }
	  return catEntriesResult;
  }
  
  // ------------------------------ INTERNAL ------------------------------

  public void sessionInvalidated(boolean byApplication) {
    if (byApplication) {
      httpSessionsInvalidatedByApplication.incrementAndGet();
    } else {
      timedOutHttpSessions.incrementAndGet();
    }
  }

  public void addErrorReportToCategorizationID(int categorizationId, String fileName){
	  CategorizationEntry_ISE_500 catEntry = allError500Categorizations.get(categorizationId);
	  if (catEntry!=null){
		  catEntry.addErrorReportFile(fileName);	
	  }
  }
  
  public void addCategorizationID(int categorizationId){
	  if (!isTimeoutListenerRegistered && !isTimeoutDisabledByUser){
		  registerAsTimeoutListener();
	  }
	  error500Count.incrementAndGet();	 
	  CategorizationEntry_ISE_500 catEntry = allError500Categorizations.get(categorizationId);
	  if (catEntry!=null){
		  catEntry.addOccurrence();		  
	  }else{
		  allError500Categorizations.put(categorizationId, new CategorizationEntry_ISE_500(categorizationId));
	  }
  }
  
 
  public void securitySessionInvalidated(boolean byApplication) {
    if (byApplication) {
      securitySessionsInvalidatedByApplication.incrementAndGet();
    } else {
      timedOutSecuritySessions.incrementAndGet();
    }
  }

  public void newRequest() {
    allRequestsCount.incrementAndGet();
  }

  public void newResponse(long responseTime) {
    allResponsesCount.incrementAndGet();
    totalResponseTime.addAndGet(responseTime);
  }

  
      
  /**
  * If returns true <code>timeout()</code> will be called
  */
  public boolean check() {
	return !allError500Categorizations.isEmpty();
  }

  /**
  * After the TimeoutListener is registered this method is
  * invoked in dependence of the repeat time and delay time.
  */
  public void timeout() {
		dumpAndClearISE500Monitors();
  }

  public void dumpAndClearISE500Monitors(){
	if (LogContext.getLocationService().beDebug()) {
    	LogContext.getLocationService().debugT("Tracing Error 500 related Web Container Monitors before being cleared. \r\n Monitor: error500Count, value: " + error500Count + " ;\r\n" +
    										    "Monitor: error500Categorizationentries, value: " + getCategorizationEntriesAsString());
    }
	error500Count = new AtomicLong(0);
	allError500Categorizations = new ConcurrentHashMap<Integer, CategorizationEntry_ISE_500>();
  }

  private StringBuffer getCategorizationEntriesAsString(){
	StringBuffer result = new StringBuffer("");
	ArrayList<CategorizationEntry_ISE_500> catEntriesArray = new ArrayList<CategorizationEntry_ISE_500>(allError500Categorizations.values());
	for (CategorizationEntry_ISE_500 catEntry:catEntriesArray){
		  result.append("\r\n").append(catEntry.getCategorizationId()).append( "	").append(catEntry.getOcurrencesNumber()).append("	").append(catEntry.getErrorReportFiles());
	}
	return result;
  }

  /**
   * Enables a new Error500Monitors cleanup period. Before applying the new period,
   * the current error 500 monitors are traced and cleared. 
   * @param newTimeoutSeconds - the new time interval between two regular monitor cleanups; specified in seconds
   */
  public void changeEror500CleanupPeriod(Long newTimeoutSeconds){
	if (isTimeoutListenerRegistered){
		if (newTimeoutSeconds <= 0){
			//disable the timeout service - simply unregister the timeout listener
			ServiceContext.getServiceContext().getTimeoutManager().unregisterTimeoutListener(this);
			timeoutSeconds = newTimeoutSeconds;
			isTimeoutDisabledByUser = true;
			isTimeoutListenerRegistered = false;
		}else { //renew the registration with the new timeout
			ServiceContext.getServiceContext().getTimeoutManager().unregisterTimeoutListener(this);
			isTimeoutListenerRegistered = false;
			timeoutSeconds = newTimeoutSeconds;
			registerAsTimeoutListener();
		}
	}else{ // register the timeout listener with the specified timeout
		timeoutSeconds = newTimeoutSeconds;
		registerAsTimeoutListener();
	}
  }

  private void registerAsTimeoutListener(){
	if (timeoutSeconds <= 0 ){
		isTimeoutDisabledByUser = true;
	}else{
		ServiceContext.getServiceContext().getTimeoutManager().registerTimeoutListener(this, timeoutSeconds*1000, timeoutSeconds*1000);
		isTimeoutListenerRegistered = true;
	}
  }
}
