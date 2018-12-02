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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

import com.sap.engine.frame.core.thread.ContextObject;
import com.sap.engine.frame.core.thread.ThreadContext;

import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.RequestContextBaseRuntimeException;
import com.sap.engine.services.servlets_jsp.server.runtime.client.preservation.ObservableHashSet;
import com.sap.tc.logging.Location;

/**
 * @author diyan-y
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RequestContextObject implements ContextObject {

  public final static String NAME = "REQUEST_CONTEXT_OBJECT";
  private static Location location = Location.getLocation(LogContext.getLocationRequestPreservation(),"ThreadAccounting");
  
  /**
   * the id, with which this object (RequestContextObject) is registered in thread context;
   * it should be unique in every service/server run
   */
  private static int CONTEXT_OBJECT_ID = -1;
  
  private Stack appContextRequestStack = null;
  
  private Long requestContextId = null;
  
  
  private ObservableHashSet<RequestPreservationContext> preservationSet = new ObservableHashSet<RequestPreservationContext>();
  private RequestPreservationContext preservationContext = null;
  //no timeout is set
  private long timeout = 0;
  private Boolean dispatched = false;
  
  public RequestContextObject(Long id) {
    requestContextId = id;
    appContextRequestStack = new Stack();
  }
  
  /**
   * Through this method you can very fast fill internal datastructure of the
   * object. This method is called from the system to copy context object of
   * the parent thread to this one associated with the new thread.
   */
  public ContextObject childValue(ContextObject parent, ContextObject child) {
    Long parentId = null;
    if (parent != null) {
      parentId = ((RequestContextObject)parent).requestContextId; 
    }
    if (child == null) {
      child = new RequestContextObject(parentId);
    } else {
      ((RequestContextObject)child).requestContextId = parentId;
      ((RequestContextObject)child).appContextRequestStack = new Stack();
    }
    //moveInheritedDataFromParentToChild(parent, child);
    if (location.beDebug()){
    	String tid = "id-"+Thread.currentThread().getId();
    	location.debugT("@@@@@@-childValue-"+tid);
    }
    if (parent != null){
    	
    	if (((RequestContextObject)child).preservationSet != null && ((RequestContextObject)child).preservationSet.size() > 0 ){
    		String tid = "id-"+Thread.currentThread().getId();
        	location.debugT("@@@@@@-childValue-warning-"+tid+" has preservation set: "+((RequestContextObject)child).preservationSet);
    	}
    	
      ((RequestContextObject)child).dispatched = ((RequestContextObject)parent).dispatched; 	
      ((RequestContextObject)child).preservationSet = ((RequestContextObject)parent).preservationSet;
  	  ((RequestContextObject)child).preservationContext = new RequestPreservationContext();
  	  ((RequestContextObject)child).preservationSet.add(((RequestContextObject)child).preservationContext);
  	  
  	  
  	if (((RequestContextObject)parent).preservationContext != null){
  		if (((RequestContextObject)parent).preservationContext.isDispatched()){
  			((RequestContextObject)child).preservationContext.setDispatched(true);
  		}
  		
  		if (((RequestContextObject)parent).preservationContext.isInvoked()){
  			long timeout = ((RequestContextObject)parent).preservationContext.getTimeout();
  			((RequestContextObject)child).preservationContext.setInvoked(true, timeout);
  		}
  	}    
  	  
  	  
  	  
  	  
  	if (location.beDebug()){
  		  String tid = "id-"+Thread.currentThread().getId();
	  	  location.debugT("@@@@@@-childValue-"+tid+" preservation set: "+((RequestContextObject)child).preservationSet.toString());
	  }
  	  
    } 
    
    return child;
  }

  /**
   * @param parent
   * @param child
   */
  private void moveInheritedDataFromParentToChild(ContextObject parent, ContextObject child) {

  }

  /* (non-Javadoc)
   * @see com.sap.engine.frame.core.thread.ContextObject#getInitialValue()
   */
  public ContextObject getInitialValue() {
	RequestContextObject rco = new RequestContextObject(null);
	
    return rco;
  }
  
  public static ObservableHashSet<RequestPreservationContext> getPreservationSet(){
	  initContextObjectID();
	  ThreadContext threadContext = ServiceContext.getServiceContext().getThreadSystem().getThreadContext();
	  if (threadContext == null) {
	      //TODO throw new BaseRuntimeException();
	      throw new RequestContextBaseRuntimeException(RequestContextBaseRuntimeException.PROBLEMS_WHEN_GETTING_ID, null);
	  }
	  RequestContextObject contextObject = (RequestContextObject) threadContext.getContextObject(CONTEXT_OBJECT_ID);
	  if (contextObject != null){
		  return contextObject.preservationSet;
	  }
	  return null;
  }
  
  
  
  public static void mark(long id){
	  initContextObjectID();
	  ThreadContext threadContext = ServiceContext.getServiceContext().getThreadSystem().getThreadContext();
	  if (threadContext == null) {
	      //TODO throw new BaseRuntimeException();
	      throw new RequestContextBaseRuntimeException(RequestContextBaseRuntimeException.PROBLEMS_WHEN_GETTING_ID, null);
	  }
	  RequestContextObject contextObject = (RequestContextObject) threadContext.getContextObject(CONTEXT_OBJECT_ID);
	  if (contextObject != null) {
		  if (contextObject.preservationContext == null){
			  contextObject.preservationContext = new RequestPreservationContext();
			  contextObject.preservationSet.add(contextObject.preservationContext);
		  }
		  
		  contextObject.dispatched = Boolean.TRUE;
		  contextObject.preservationContext.setDispatched(true);
		  if (location.beDebug()){
			  String tid = "id-"+Thread.currentThread().getId();
			  location.debugT("@@@@@@-mark-"+tid+" parent thread: "+contextObject.requestContextId);
		  	  location.debugT("@@@@@@-mark-"+tid+" preservation set: "+contextObject.preservationSet.toString());
		  	
		  }
	  }
  }
  
  
  public static void setAutoComplete(long id, long timeout){
	  initContextObjectID();
	  ThreadContext threadContext = ServiceContext.getServiceContext().getThreadSystem().getThreadContext();
	  if (threadContext == null) {
	      //TODO throw new BaseRuntimeException();
	      throw new RequestContextBaseRuntimeException(RequestContextBaseRuntimeException.PROBLEMS_WHEN_GETTING_ID, null);
	  }
	  RequestContextObject contextObject = (RequestContextObject) threadContext.getContextObject(CONTEXT_OBJECT_ID);
	  if (contextObject != null) {
		  contextObject.timeout = timeout;
		  if (contextObject.preservationContext == null){
			  contextObject.preservationContext = new RequestPreservationContext();
			  contextObject.preservationSet.add(contextObject.preservationContext);
		  }
			  //it is invoked from a child of the http worker thread
		  contextObject.preservationContext.setInvoked(true,timeout);
		  if (location.beDebug()){
			  String tid = "id-"+Thread.currentThread().getId();
			  location.debugT("@@@@@@-["+timeout+"]-setAutoComplete-"+tid+" parent thread: "+contextObject.requestContextId);
		  	  location.debugT("@@@@@@-["+timeout+"]-setAutoComplete-"+tid+" preservation set: "+contextObject.preservationSet.toString());
		  }
	  }
  }
  
  
  public static long getTimeout(){
	  initContextObjectID();
	  ThreadContext threadContext = ServiceContext.getServiceContext().getThreadSystem().getThreadContext();
	  if (threadContext == null) {
	      //TODO throw new BaseRuntimeException();
	      throw new RequestContextBaseRuntimeException(RequestContextBaseRuntimeException.PROBLEMS_WHEN_GETTING_ID, null);
	  }
	  RequestContextObject contextObject = (RequestContextObject) threadContext.getContextObject(CONTEXT_OBJECT_ID);
	  if (contextObject != null) {
		  return contextObject.timeout;
	  }
	  return 0;
  }
  
  public static boolean isDispatched(){
	  initContextObjectID();
	  ThreadContext threadContext = ServiceContext.getServiceContext().getThreadSystem().getThreadContext();
	  if (threadContext == null) {
	      //TODO throw new BaseRuntimeException();
	      throw new RequestContextBaseRuntimeException(RequestContextBaseRuntimeException.PROBLEMS_WHEN_GETTING_ID, null);
	  }
	  RequestContextObject contextObject = (RequestContextObject) threadContext.getContextObject(CONTEXT_OBJECT_ID);
	  if (contextObject != null) {
		  return contextObject.dispatched.booleanValue();
	  }
	  return false;
  }
  
  
  
  /* (non-Javadoc)
   * @see com.sap.engine.frame.core.thread.ContextObject#empty()
   */
  public void empty() {
	  if (location.beDebug()){
		    String tid = "id-"+Thread.currentThread().getId();
			location.debugT("@@@@@@-empty-"+tid);
			if (preservationContext != null){
				location.debugT("@@@@@@-empty-"+tid+" about to remove preservation context: "+preservationContext);
			}
	  }	 
	  
	  
	 if (preservationSet != null && preservationContext != null && preservationSet.contains(preservationContext)){
		 
		preservationSet.remove(preservationContext);
		if (location.beDebug()){
			String tid = "id-"+Thread.currentThread().getId();
			location.debugT("@@@@@@-empty-"+tid+" remove preservation context: "+preservationContext+" parent thread: "+requestContextId);
			location.debugT("@@@@@@-empty-"+tid+" preservation set after remove: "+preservationSet);
	    }
	 }	
	
	preservationContext = null;
	preservationSet = null;
	timeout = 0;
	dispatched = null;
	
    requestContextId = null;
    appContextRequestStack.clear();    
    
  }
  
  public static Long getCurrentRequestContextId() {
    initContextObjectID();
    ThreadContext threadContext = ServiceContext.getServiceContext().getThreadSystem().getThreadContext();
    if (threadContext == null) {
      //TODO throw new BaseRuntimeException();
      throw new RequestContextBaseRuntimeException(RequestContextBaseRuntimeException.PROBLEMS_WHEN_GETTING_ID, null);
    }
    RequestContextObject contextObject = (RequestContextObject) threadContext.getContextObject(CONTEXT_OBJECT_ID);
    if (contextObject == null) {
      return null;
    }
    return contextObject.requestContextId;
  }
  
  public static void setCurrentRequestContextId(Long requestContextId) {
    //TODO
    initContextObjectID();
    ThreadContext threadContext = ServiceContext.getServiceContext().getThreadSystem().getThreadContext();
    if (threadContext == null) {
      //TODO throw new Runtime exception
      throw new RequestContextBaseRuntimeException(RequestContextBaseRuntimeException.PROBLEMS_WHEN_SETTING_ID, null);
    }
    RequestContextObject contextObject = (RequestContextObject) threadContext.getContextObject(CONTEXT_OBJECT_ID);
    contextObject.requestContextId = requestContextId;
  }

  public static void pushRequestContextId(Long requestContextId) {
    initContextObjectID();
    ThreadContext threadContext = ServiceContext.getServiceContext().getThreadSystem().getThreadContext();
    if (threadContext == null) {
      //TODO throw new Runtime exception
      throw new RequestContextBaseRuntimeException(RequestContextBaseRuntimeException.PROBLEMS_WHEN_PUSHING_ID, null);
    }
    RequestContextObject contextObject = (RequestContextObject) threadContext.getContextObject(CONTEXT_OBJECT_ID);
    contextObject.appContextRequestStack.push(requestContextId);
  }
  
  public static Long popRequestContextId() {
    initContextObjectID();
    ThreadContext threadContext = ServiceContext.getServiceContext().getThreadSystem().getThreadContext();
    if (threadContext == null) {
      //TODO throw new Runtime exception
      throw new RequestContextBaseRuntimeException(RequestContextBaseRuntimeException.PROBLEMS_WHEN_POPPING_ID, null);
    }
    RequestContextObject contextObject = (RequestContextObject) threadContext.getContextObject(CONTEXT_OBJECT_ID);
    return (Long)contextObject.appContextRequestStack.pop();
  }
  
  private final static void initContextObjectID() {
	ThreadContext tc = ServiceContext.getServiceContext().getThreadSystem().getThreadContext();
	if (CONTEXT_OBJECT_ID == -1) {
       CONTEXT_OBJECT_ID = tc.getContextObjectId(NAME);
    }
	RequestContextObject contextObject = (RequestContextObject) tc.getContextObject(CONTEXT_OBJECT_ID);
    if (contextObject.preservationSet == null){
    	contextObject.preservationSet = new ObservableHashSet<RequestPreservationContext>();
    }
  }
  
  public static int getContextObjectId() {
    return CONTEXT_OBJECT_ID;
  }
  
  /**
   * reset context object for next usage
   */
  public static void reset() {
    CONTEXT_OBJECT_ID = -1;
  }
}
