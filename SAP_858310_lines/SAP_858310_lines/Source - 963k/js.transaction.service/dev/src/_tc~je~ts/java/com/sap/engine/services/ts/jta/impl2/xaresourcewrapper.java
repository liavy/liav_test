package com.sap.engine.services.ts.jta.impl2;

import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.XAException;

/**
 * This is a wrapper of each XAResource which is enlisted into transaction.
 *
 * @author : Nikolai Tankov, nikolai.tankov@sap.com
 */
public class XAResourceWrapper {

  private static final Location LOCATION = Location.getLocation(XAResourceWrapper.class);

  /* Xid for the XAResource*/
  protected Xid xid = null;

  /* The XAResource */
  protected XAResource theXAResource = null;

  /* Shows if XAResource is prepared with status XA_OK */
  private int prepareStatus = XAResource.TMFAIL;

  /* Shows if XAResource is delisted */
  private boolean isDelisted = false;
  
  /* id of the resource manager from which this XAResource was created */
  protected int rmID = 0;
  
  private boolean rolledbackBecauseOfRBError = false;
  
 
  
  public XAResourceWrapper(XAResource xaResource, int rmID) {
    this.theXAResource = xaResource;
    this.rmID = rmID;
  }
 
  public void start(Xid xid)throws XAException{
	    this.xid = xid;
	    theXAResource.start(xid,XAResource.TMNOFLAGS);	  
  }

  public int prepare() throws XAException {
  	try {  	
      prepareStatus = theXAResource.prepare(xid);
      if(prepareStatus != XAResource.XA_OK && prepareStatus != XAResource.XA_RDONLY){
//    	  Location.getLocation(XAResourceWrapper.class).traceThrowableT(Severity.ERROR, "XAResource"+ theXAResource+ " cannot prepare it's transaction branch.", new Exception("One XAResource cannot prepare it's transaction branch. This is just calling trace."));
    	  SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,new Exception("One XAResource cannot prepare it's transaction branch. This is just calling trace."), "ASJ.trans.000114", "XAResource{0} cannot prepare it's transaction branch.",  new Object[] { theXAResource});
      }
      return prepareStatus;
    } catch (XAException xaE) {       
        if (LOCATION.beLogged(Severity.DEBUG)) {
//          LOCATION.traceThrowableT(Severity.DEBUG, "{0} exception xa prepare.", toObjectArray(), xaE);
        }
        int errorCode = xaE.errorCode;
        if (errorCode >= XAException.XA_RBBASE && errorCode <= XAException.XA_RBEND){
        	rolledbackBecauseOfRBError = true;
        } 
        throw xaE;
      }
  }
  
  public void commitTwoPhase() throws XAException {
    if (prepareStatus == XAResource.XA_OK && !rolledbackBecauseOfRBError) {
      theXAResource.commit(xid, false);
    }
  }

  public void commitOnePhase() throws XAException {
    theXAResource.commit(xid, true);
  }

  public void rollback() throws XAException {
    if(prepareStatus != XAResource.XA_RDONLY &&!rolledbackBecauseOfRBError){
    	theXAResource.rollback(xid);
    }    
  }
  
  public void markAsRolledback() {
	  rolledbackBecauseOfRBError = true;		
  }
  
  public void delistResource() throws XAException, IllegalStateException{
	  if(isDelisted){
		  throw new IllegalStateException("Second delistment of one and the same XAResource is not supported.");
	  }
	  isDelisted = true;
	  theXAResource.end(xid,XAResource.TMSUCCESS); // always TMSUCCESS because optimization with RMFAIL is not supported 	  
  }

  public boolean checkIfEligibleForRecovery(){
	  return prepareStatus == XAResource.XA_OK && rmID > 0;
  }
  public void forget(){
	  if(TransactionServiceFrame.callForgetAfterHeuristics){
		  try{
			  theXAResource.forget(xid);
		  } catch(Exception e) {
			  SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "TransactionManager tried to call forget() on "+theXAResource+" but Unexpected exception was thrown. This Exception is ignored.", e);
		  }
	  }
  }
  
  public void forceForget() throws XAException{
	  theXAResource.forget(xid);
  }
  
  public int hashCode(){
	  return System.identityHashCode(theXAResource);  
  }  
  /** 
   * Will be used for search operations into ArrayList wrappers of all XAResources that are enlisted into transaction.
   */
public boolean equals(Object obj) {
	  
	  if(obj instanceof XAResourceWrapper){
		  return theXAResource == ((XAResourceWrapper)obj).theXAResource;
	  }
	  
	  return false;
  }

 
}