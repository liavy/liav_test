package com.sap.engine.services.ts.tlog;

import java.util.ArrayList;
import java.util.Arrays;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.sap.engine.interfaces.transaction.RMProps;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.jta.impl2.SAPXidImpl;
import com.sap.engine.services.ts.utils.TLogVersion;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class RMPropsExtension extends RMProps{
	
	
	private static final Location LOCATION = Location.getLocation(RMPropsExtension.class);
	/**
	 * XAResource instance from resource manager which is represented with these RMProperties
	 */
	private transient XAResource xaResource= null;
	
	/**
	 * Resource manager's ID after its registration. This ID must be positive
	 */
	private transient int rmID = 0; 
	
	private transient ArrayList<Xid> xidsForRecover = null;

	public boolean recoveredSuccessfully = false;
	public boolean ignoredBecauseAnotherSameRM = false; 
	public boolean rmUnreachable = false;
	private int reachAttempts = 0;
	
	@Override
	public int hashCode() {
		return ((rmContainerName != null) ? rmContainerName.hashCode() : 0) +
				((keyName != null) ? keyName.hashCode() : 0);
	}	

	@Override
    public boolean equals(Object obj) {
    	
    	RMProps  otherRMProps = null;
    	
       if(obj == null){
    	   return false;
       }
       if(obj instanceof RMProps) {
		otherRMProps = (RMProps) obj;
	   } else {
		   return false;
	   }
       if(
    	 (rmContainerName != null && rmContainerName.equals(otherRMProps.getRmContainerName()))&& //rmContainer name must not be null
       	 (keyName != null && keyName .equals(otherRMProps.getKeyName())) && //keyName must not be null
    	 ((nonSecureProperties != null && nonSecureProperties.equals(otherRMProps.getNonSecureProperties())) || 
    			 (nonSecureProperties == null && otherRMProps.getNonSecureProperties() == null)) && 
    	 ((secureProperties != null && secureProperties.equals(otherRMProps.getSecureProperties())) || 
    			 (secureProperties == null && otherRMProps.getSecureProperties() == null ))       	 
         ){
    	   return true;
       }
       return false;
    }

	/**
	 * @return the xaResource
	 */
	public XAResource getXAResource() {
		return xaResource;
	}

	/**
	 * @param xaResource the xaResource to set
	 */
	public void setXAResource(XAResource xaResource) {
		if(xaResource == null){
			reachAttempts++;// no need to synchronize
			if(reachAttempts >= TransactionServiceFrame.retryAttemptsWhenRMisUnreachable){
				rmUnreachable = true;
			}
		}
		this.xaResource = xaResource;
	}

	/**
	 * @return the rmID
	 */
	public int getRmID() {
		return rmID;
	}

	/**
	 * @param rmID the rmID to set
	 */
	public void setRmID(int rmID) {
		this.rmID = rmID;
	}	
    
	/**
	 * Must be called only when XAResource is not null.
	 */
	public void recover(TLogVersion tlogVersion) throws XAException{
		if(recoveredSuccessfully || ignoredBecauseAnotherSameRM){
			return; 
		}
		if(rmUnreachable){
			SimpleLogger.trace(Severity.ERROR, LOCATION, "ResourceManager "+getKeyName()+" is not available. TransactionManager is not able to recover its transactions.");
			recoveredSuccessfully  = true; 
		}
		if(xaResource == null){
			XAException xae = new XAException("It is not possible to find transactions for recovery because resource manager " + getKeyName() + " is not available.");
			xae.errorCode = XAException.XAER_RMFAIL;
			throw xae;			
		}
		Xid[] initialXidSet = xaResource.recover(XAResource.TMSTARTRSCAN | XAResource.TMENDRSCAN);
		xidsForRecover = new ArrayList<Xid>();
		if(initialXidSet != null){// When initialXidSet is null this is equivalent to empty array.  
			for(Xid xid : initialXidSet){
				if(xid.getFormatId() == SAPXidImpl.sapFormatId && SAPXidImpl.compareTLogVersions(xid, tlogVersion)){
					xidsForRecover.add(xid);
				}
			}
		}
		recoveredSuccessfully  = true;  
	}
	
	public boolean commitTxForRecovery(byte[] globalTransactionID)throws XAException{
		
		if(rmUnreachable){
			SimpleLogger.trace(Severity.ERROR, LOCATION, "ResourceManager "+getKeyName()+" is not available. TransactionManager is not able to commit its pending transactions.");
			recoveredSuccessfully  = true; 
			return false;
		}
		
		if(xaResource == null){
			XAException xae = new XAException("It is not possible to commit transactions because ResourceManager "+getKeyName()+" is not available.");
			xae.errorCode = XAException.XAER_RMFAIL;
			throw xae;
		}
		
		boolean result = false;		
		ArrayList<Xid> xidsForRecover_copy = (ArrayList<Xid>)xidsForRecover.clone();
		for(Xid xidForRecover : xidsForRecover_copy){
			if(Arrays.equals(xidForRecover.getGlobalTransactionId(), globalTransactionID)){
				try {
					xaResource.commit(xidForRecover, false);
					result = xidsForRecover.remove(xidForRecover);
				} catch (XAException e) {
					throw e;// TODO evaluate exception codes and call forget in case of heuristics
				}
			}
		}
		return result;
	}

	public void rollbackTxForRecovery() throws XAException{
		
		if(rmUnreachable){
			SimpleLogger.trace(Severity.ERROR, LOCATION, "ResourceManager "+getKeyName()+" is not available. TransactionManager is not able to rollback its pending transactions.");
			recoveredSuccessfully  = true; 
		}	
		
		if(xaResource == null){
			XAException xae = new XAException("It is not possible to rollback transactions because ResourceManager "+getKeyName()+" is not available.");
			xae.errorCode = XAException.XAER_RMFAIL;
			throw xae;
		}
		
		for(Xid xidForRecover : xidsForRecover){
			try{
				xaResource.rollback(xidForRecover);
			} catch (XAException e) {
				throw e;// TODO evaluate exception codes and call forget in case of heuristics
			}
		}
	}
	
	public void commitPendingTx(Xid xid, boolean onePhaseCommit) throws XAException {
		
		if(rmUnreachable){
			SimpleLogger.trace(Severity.ERROR, LOCATION, "ResourceManager "+getKeyName()+" is not available. TransactionManager is not able to commit its pending transactions.");
			recoveredSuccessfully  = true; 
		}	
		if(xaResource != null){
			xaResource.commit(xid, onePhaseCommit);
		} else {
			 XAException xaex = new XAException("ResourceManager " + keyName + " is unavailable. It is not possible to complete pending transactions into which this RM participates.");
			 xaex.errorCode = XAException.XAER_RMFAIL;
			 throw xaex;
		}
	}
	
	public void rollbackPendingTx(Xid xid) throws XAException {
		
		if(rmUnreachable){
			SimpleLogger.trace(Severity.ERROR, LOCATION, "ResourceManager "+getKeyName()+" is not available. TransactionManager is not able to rollback its pending transactions.");
			recoveredSuccessfully  = true; 
		}		
		if(xaResource != null){
			xaResource.rollback(xid);
		} else {
			 XAException xaex = new XAException("ResourceManager " + keyName + " is unavailable. It is not possible to complete pending transactions into which this RM participates.");
			 xaex.errorCode = XAException.XAER_RMFAIL;
			 throw xaex;
		}
	}
	
	public void forget(Xid xid) throws XAException {
		
		if(rmUnreachable){
			SimpleLogger.trace(Severity.ERROR, LOCATION, "ResourceManager "+getKeyName()+" is not available. TransactionManager is not able to call forget its pending transactions.");
			recoveredSuccessfully  = true; 
		}
		if(xaResource != null){
			xaResource.forget(xid);
		} else {
			 XAException xaex = new XAException("It is not possible to call 'forget()' method on ResourceManager " + keyName + " because it is unavailable.");
			 xaex.errorCode = XAException.XAER_RMFAIL;
			 throw xaex;
		}
	}
	
}
