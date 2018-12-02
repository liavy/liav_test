/*
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.ts.jts;

import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.XAException;

/**
 * This is wrapper of each enlisted XAResource. This class encapsulates XAResource and xid for this resource.
 *
 * @author : Nikolai Tankov, nikolai.tankov@sap.com
 */
public class XAResourceWrapper {


  /* Xid for the XAResource*/
  private Xid xid = null;

  /* The XAResource */
  private XAResource theXAResource = null;

  /* Shows if XAResource is prepared with status XA_OK */
  private int prepareStatus = XAResource.XA_OK;

  /* Shows if XAResource is delisted */
  private boolean isDelisted = false;
  /**
   * Constructor for this class
 * @throws XAException 
   */
  public XAResourceWrapper(Xid xid, XAResource xaResource) throws XAException {
    this.xid = xid;
    this.theXAResource = xaResource;
    xaResource.start(xid,XAResource.TMNOFLAGS);
  }
 
  public boolean isSameXAResource(XAResource xaResource){    
	return xaResource == theXAResource; 	 
  }
  
  public boolean isSameResourceManager(XAResource xaResource) throws XAException{
	  return xaResource.isSameRM(theXAResource);
  }

  public int prepare() throws XAException {
  	try {  	
      prepareStatus = theXAResource.prepare(xid);
      if(prepareStatus != XAResource.XA_OK && prepareStatus != XAResource.XA_RDONLY){
//    	  Location.getLocation(XAResourceWrapper.class).traceThrowableT(Severity.ERROR, "XAResource"+ theXAResource+ " cannot prepare it's transaction branch.", new Exception("One XAResource cannot prepare it's transaction branch. This is just calling trace."));
    	  SimpleLogger.traceThrowable(Severity.ERROR, Location.getLocation(XAResourceWrapper.class), new Exception("One XAResource cannot prepare it's transaction branch. This is just calling trace."), "ASJ.trans.000011","XAResource {0} cannot prepare it's transaction branch.", new Object[] {theXAResource});
      }
      return prepareStatus;
  	} catch (XAException xae){
  	  prepareStatus = XAResource.TMFAIL;
  	  throw xae;
  	} catch (RuntimeException npe){
	  prepareStatus = XAResource.TMFAIL;
	  throw npe;  		
  	}
  }
  
  public void commitTwoPhase() throws XAException {
    if (prepareStatus == XAResource.XA_OK) {
      theXAResource.commit(xid, false);
    }
  }

  public void commitOnePhase() throws XAException {
    theXAResource.commit(xid, true);
  }

  public void rollback() throws XAException {
    try{
      theXAResource.rollback(xid);
    } catch (XAException xaex){
      // TO DO trace  	
	  if (prepareStatus == XAResource.XA_OK) {
	  	throw xaex;     
	  }	
    } catch (RuntimeException re){
	  if (prepareStatus == XAResource.XA_OK) {
	    throw re;     
	  }	
    }
  }

  void joinAgain() throws XAException{
	  theXAResource.start(xid,XAResource.TMJOIN);	  
  }
  
  void delistResource() throws XAException{
	  isDelisted = true;
	  theXAResource.end(xid,XAResource.TMSUCCESS); // always TMSuccess because of compatibility with DB2-4-6	  
  }

  public void forget() throws XAException{
	  theXAResource.forget(xid);	  
  }
  boolean isDelisted(){
	  return isDelisted;
  }

//  /**
//   * Checks if there isn't any XAResources
//   *
//   * @return true if all XAresources are delisted
//   */
//  public boolean isMarked() {
//    return marked;
//  }

 



}

//
///*
// * Copyright (c) 2002 by SAP AG, Walldorf.,
// * url: http://www.sap.com
// * All rights reserved.
// *
// * This software is the confidential and proprietary information
// * of SAP AG, Walldorf. You shall not disclose such Confidential
// * Information and shall use it only in accordance with the terms
// * of the license agreement you entered into with SAP.
// */
//
//package com.sap.engine.services.ts.jta.impl;
//
//import com.sap.engine.lib.util.LinkedList;
//import com.sap.tc.logging.Location;
//import com.sap.tc.logging.Severity;
//
//import javax.transaction.xa.Xid;
//import javax.transaction.xa.XAResource;
//import javax.transaction.xa.XAException;
//
///**
// * This is representation of the Resources in the Resource Managers Map
// *
// * @author : Iliyan Nenov, ilian.nenov@sap.com
// * @version 1.0
// */
//public class ResourceList {
//
//  /* This is Xid for each XAResource in this resource manager */
//  private Xid xid = null;
//
//  /* Storage for XAResources */
//  private LinkedList resourceList = null;
//
//  /* Flag which shows if there is only one Resource in this ResourceList */
//  private boolean marked = false;
//
//  /* Shows if XAResource is prepared with status XA_OK */
//  private int prepareStatus = XAResource.XA_OK;
//
//  /**
//   * Constructor for this class
//   */
//  public ResourceList(Xid xid) {
//    this.resourceList = new LinkedList();
//    this.xid = xid;
//  }
//
//  /**
//   * Adds a new XAResource to the XAList
//   */
//  public void add(Object xaResource) {
//    if (marked) {
//      marked = false;
//      resourceList.removeFirst();
//    }
//    resourceList.add(xaResource);
//  }
//
//  /**
//   * Returns a XAResource
//   */
//  public Object getResource(int xaIndex) {
//    return resourceList.get(xaIndex);
//  }
//
//  public boolean isSameXAResource(XAResource xaResource){
//    
//	return xaResource == resourceList.getFirst(); 
//	 
//  }
//  
//  public boolean isSameResourceManager(XAResource xaResource){
//	  return xaResource.isSameRM(resourceList)
//  }
//
//  public int prepare() throws XAException {
//  	try {  	
//      prepareStatus = ((XAResource)resourceList.getFirst()).prepare(xid);
//      if(prepareStatus != XAResource.XA_OK && prepareStatus != XAResource.XA_RDONLY){
//    	  Location.getLocation(ResourceList.class).traceThrowableT(Severity.ERROR, "XAResource"+ resourceList.getFirst()+ " cannot prepare it's transaction branch.", new Exception("One XAResource cannot prepare it's transaction branch. This is just calling trace."));
//      }
//      return prepareStatus;
//  	} catch (XAException xae){
//  	  prepareStatus = XAResource.TMFAIL;
//  	  throw xae;
//  	} catch (RuntimeException npe){
//	  prepareStatus = XAResource.TMFAIL;
//	  throw npe;  		
//  	}
//  }
//
//  public void forget() throws XAException {
//    ((XAResource)resourceList.getFirst()).forget(xid);
//  }
//
//  public void commitTwoPhase() throws XAException {
//    if (prepareStatus == XAResource.XA_OK) {
//      ((XAResource)resourceList.getFirst()).commit(xid, false);
//    }
//  }
//
//  public void commitOnePhase() throws XAException {
//    ((XAResource)resourceList.getFirst()).commit(xid, true);
//  }
//
//  public void rollback() throws XAException {
//    try{
//      ((XAResource)resourceList.getFirst()).rollback(xid);
//    } catch (XAException xaex){
//	  if (prepareStatus == XAResource.XA_OK) {
//	  	throw xaex;     
//	  }	
//    } catch (RuntimeException re){
//	  if (prepareStatus == XAResource.XA_OK) {
//	    throw re;     
//	  }	
//    }
//  }
//
//  /**
//   * Returns the count of the XAResource
//   */
//  public int xaCount() {
//    return resourceList.size();
//  }
//
//  /**
//   * Delists XAResource
//   *
//   * @param xaIndex the index of the resource managed in the resource map
//   */
//  public void deleteResource(int xaIndex) {
//    if (resourceList.size() == 1) {
//      marked = true;
//    } else {
//      resourceList.remove(xaIndex);
//    }
//  }
//
//  /**
//   * Checks if there isn't any XAResources
//   *
//   * @return true if all XAresources are delisted
//   */
//  public boolean isMarked() {
//    return marked;
//  }
//
//  /**
//   * Returns the prepare status
//   */
//  public int getPrepareStatus() {
//    return prepareStatus;
//  }
//
//  public void setPrepareStatus(int prepareStatus) {
//    this.prepareStatus = prepareStatus;
//  }
//
//  /**
//   * Returns the xid of this ResourceList
//   */
//  public Xid getXid() {
//    return xid;
//  }
//
//
//}