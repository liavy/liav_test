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

import java.util.ArrayList;

import com.sap.engine.services.ts.exceptions.BaseIllegalStateException;
import com.sap.engine.services.ts.exceptions.ExceptionConstants;
import com.sap.engine.services.ts.jta.impl2.SAPXidImpl;
import com.sap.tc.logging.Location;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * RMMap comes from Resource Managers Map.
 * This is an implementation of simple data structure that holds all XAResources that are enlisted into associated transaction.
 *
 * This structure is used by JTA Transaction and OTSTransaction objects. JTA Transaction holds
 * javax.transaction.xa.XAResource OTSTransaction holds org.omg.CosTransactions.Resource
 * each of them casts the Objects in the structure to the respective interface
 *
 * @author : Nikolai Tankov, nikolai.tankov@sap.com
 
 */
public class RMMap {

  private static final Location LOCATION = Location.getLocation(RMMap.class);	
  /*
   * List with all XAResources that are asociated with current transaction.
   */	
  private ArrayList<XAResourceWrapper> xaResources = null;
  /*
   * BranchQualifier counter for associated transaction. Initial value is 0.
   */
  private int branchQualifier = 0;
  /*
   * Transaction Id for associated transaction. Value is used and increased for non-shareable connections. 
   */
  private byte[] globalTransactionId;
//  /*
//   * Value is used and increased for non-shareable connections. For each not shared connection we have a separate transaction into DB. 
//   */
//  private int globalTransactionIdCounter = 0;
  
  /**
   * Constructor of this class
   */
  public RMMap(byte[]globalTransactionId ) {
    xaResources = new ArrayList<XAResourceWrapper>();
    this.globalTransactionId = globalTransactionId; 
  }

  /**
   * Returns the XAResources associated to the Resource Manager with index rmIndex passed as an argument
   */
  public XAResourceWrapper getResources(int rmIndex) {
    return xaResources.get(rmIndex);
  }
  
  /**
   * Returns the count of the resource managers
   */
  public int getXAResourceCount() {
    return xaResources.size();
  }

  public boolean enlistResource(XAResource newXARes) throws XAException{
	  
	  for(XAResourceWrapper xaResWrap : xaResources ){		  
		  if(xaResWrap.isSameXAResource(newXARes)){// these cases must not happened
			  if(xaResWrap.isDelisted()){
				 xaResWrap.joinAgain();
				 return true;
			  } else {				  
				 throw new XAException("XAResource :" + newXARes + " is already enlisted");  
			  }
		  }
		  if(xaResWrap.isSameXAResource(newXARes)){  // unshared connection. will start another transaction into RM
			    // XAResource into one transaction are less then 100. 			  
			  branchQualifier++;
			  byte[] globalTransactionID_copy = new byte[globalTransactionId.length];		
			  System.arraycopy(globalTransactionId, 0, globalTransactionID_copy, 0, globalTransactionId.length);
			  globalTransactionID_copy[globalTransactionID_copy.length-1] = (byte)(globalTransactionId[globalTransactionId.length-1] + branchQualifier);
			  Xid newXid = SAPXidImpl.createNewSimpleXid(globalTransactionID_copy, branchQualifier);
			  xaResources.add(new XAResourceWrapper(newXid, newXARes));
			  return true;			  
		  }
	  }
	  // new XAResource from unknown RM
	  branchQualifier++;	
	  byte[] globalTransactionID_copy = new byte[globalTransactionId.length];		
	  System.arraycopy(globalTransactionId, 0, globalTransactionID_copy, 0, globalTransactionId.length);	  
	  Xid newXid = SAPXidImpl.createNewSimpleXid(globalTransactionID_copy, branchQualifier);
	  xaResources.add(new XAResourceWrapper(newXid, newXARes));	  
	  return true;
  }
  
  public boolean delistResource(XAResource existingResource) throws XAException, IllegalStateException{
	  
	  for(XAResourceWrapper xaResWrap : xaResources ){	
		  if(xaResWrap.isSameXAResource(existingResource)){
			  xaResWrap.delistResource();
			  return true;
		  }
	  }
	  throw new BaseIllegalStateException(ExceptionConstants.XAResource_manager_not_found);
  }
    
}