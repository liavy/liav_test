package com.sap.engine.services.ts.recovery;

import java.util.ArrayList;

import javax.transaction.xa.XAException;

import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.jta.impl2.SAPXidImpl;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionImpl;
import com.sap.engine.services.ts.jta.statistics.TransactionStatistics;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMExtendedPendingTransactionData;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMJ2eeActionStatus;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMPendingTransactionData;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMRMData;
import com.sap.engine.services.ts.tlog.InvalidRMKeyException;
import com.sap.engine.services.ts.tlog.RMPropsExtension;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.utils.TLogVersion;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class PendingTXData extends PendingTXDataKey{
	
    public final static int INITIAL_FINISH_STATUS = 0;
	public final static int ROLLBACK_FAILED = 1;
	public final static int COMMIT_FAILED = 2;
	public final static int COMMIT_FAILED_1PC = 3;
	
	private final static String COMMITTED_WITH_WARNING = "Committed with warnings";
	private final static String ROLLEDBACK_WITH_WARNING = "Rolledback with warnings";
	private final static String COMMITTED_WITH_ERROR = "Commit failed";
	private final static String ROLLEDBACK_WITH_ERROR = "Rollback failed";
	private final static String PENDING_FOR_COMMIT = "Pending for commit";
	private final static String PENDING_FOR_ROLLBACK = "Pending for rollback";

	
	
	private static final Location LOCATION = Location.getLocation(PendingRMInfo.class);	
	private int txFinishStatus = INITIAL_FINISH_STATUS;
	private ArrayList<PendingRMInfo> rmsInfo = null;	
	private String txClassifier = null;
	private String status = "N/A";

	private String briefMessageID = null;
	private String[] briefMessageParams = null;

	private String detailedMessageID = null;
	private String[] detailedMessageParams = null;
	
	private boolean operationForRetry = false;
	private boolean forgetSupported = false;  
	
	private long creationTime = 0;
	
	public PendingTXData(long transactionSequenceNumber, String txClassifier,  
			ArrayList<PendingRMInfo> rmInfos, int txFinishStatus){
		
		this.rmsInfo = rmInfos;
		this.txClassifier = txClassifier;
		this.transactionSequenceNumber = transactionSequenceNumber;
		this.txFinishStatus = txFinishStatus;
		this.tLogVersion = new TLogVersion(SAPXidImpl.getTLogVersion());
		boolean hasErrors = false;
		boolean hasWarnings = false;
		creationTime = System.currentTimeMillis();
		
		for(PendingRMInfo pendingRMInfo: rmInfos){			
			// It is extremely rare case but it is possible to have 2 or more rmInfos.
			//In this case the messages from last one will b used as messages for the transaction
			briefMessageID = pendingRMInfo.getTxBriefStatusID();
			briefMessageParams = pendingRMInfo.getTxBriefStatusParams();
			detailedMessageID = pendingRMInfo.getTxDetailedStatusID();
			detailedMessageParams = pendingRMInfo.getTxDetailedStatusParams();			
			operationForRetry = operationForRetry || pendingRMInfo.isOpertionForRetry();
			forgetSupported = forgetSupported || pendingRMInfo.isForgetSupported();
			hasErrors = hasErrors || pendingRMInfo.hasErrors();
			hasWarnings = hasWarnings || pendingRMInfo.hasWarnings();
		}
		
		if(txFinishStatus == COMMIT_FAILED || txFinishStatus == COMMIT_FAILED_1PC){
			if(operationForRetry){
				status = PENDING_FOR_COMMIT;
			} else if(hasErrors) {
				status = COMMITTED_WITH_ERROR;
			} else {
				status = COMMITTED_WITH_WARNING;
			}
		} else {
			if(operationForRetry){
				status = PENDING_FOR_ROLLBACK;
			} else if(hasErrors) {
				status = ROLLEDBACK_WITH_ERROR;
			} else {
				status = ROLLEDBACK_WITH_WARNING;
			}			
		} 
	}

	public boolean isOperationForRetry(){
	  return operationForRetry;
	}

	public boolean isForgetSupported(){
		return forgetSupported;
	}
	
	public boolean finishTransaction(TLog tLog) throws XAException{
		if(!operationForRetry){
			return true;
		}
		if( System.currentTimeMillis()  > (creationTime + (TransactionServiceFrame.abandonTimeoutForInboundTx*1000))){	
			//long transactionSequenceNumber, String txClassifier,
			String txID = null;
			if(txClassifier == null){
				txID = Long.toString(transactionSequenceNumber);
			} else {
				txID = txClassifier + ":" + transactionSequenceNumber;
			}
			SimpleLogger.trace(Severity.WARNING, LOCATION, "Transaction " + txID + " that "+ status + " cannot be completed for " + TransactionServiceFrame.abandonTimeoutForInboundTx + "TransactionManager will abadon it.");
			TransactionStatistics.tmWasnotAbleToCompleteTransaction();
			return true;
		}
		
		boolean result = true;
		boolean someRMsAreUnreachable = false;
		XAException xaExceptionToThrow = null;// only one exception will be thrown
		ArrayList<PendingRMInfo> rmsInfoCopy = (ArrayList<PendingRMInfo>)rmsInfo.clone();
		for(PendingRMInfo rmInfo: rmsInfoCopy){
			if(!rmInfo.isOpertionForRetry()){
				continue;
			}
			RMPropsExtension rmProps = null;
			try {
				rmProps = tLog.getRMProperties(rmInfo.getRmID());
			} catch (InvalidRMKeyException e) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Resource manager with id="+rmInfo.getRmID()+" is not available into transaction log. All pending transactions with this resource manager will not be completed. ", e);
				return false;
			} catch (TLogIOException e) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Cannot complete pending transactions because of unexpected exception from transaction log.", e);
				return false;
			}				
			RMContainerRegistryImpl rmContainerRegistryImpl = TransactionServiceFrame.getRmContainerRegistryImpl();
			if(rmContainerRegistryImpl == null){
				return false; // It is not possible to complete transactions if container registry is not initialized.
			}
			rmContainerRegistryImpl.fillRMPropertiesWithXAResources(new RMPropsExtension[]{rmProps});//if false to not continue 
			try{
				if(txFinishStatus == PendingTXData.COMMIT_FAILED){		
					rmProps.commitPendingTx(rmInfo.getXid(), false);
				} else if(txFinishStatus == PendingTXData.COMMIT_FAILED_1PC){				
					rmProps.commitPendingTx(rmInfo.getXid(), true);
				} else if(txFinishStatus == PendingTXData.ROLLBACK_FAILED){
					rmProps.rollbackPendingTx(rmInfo.getXid());
				}
				rmsInfo.remove(rmInfo);
			} catch (XAException xaEx){
				int errorCode = xaEx.errorCode;
				SimpleLogger.traceThrowable(Severity.INFO, LOCATION, "TransactionManager tried to complete pending transaction but XAException was thrown with error code " + TXR_TransactionImpl.convertXAErrorCodeToString(errorCode), xaEx);
				// TO DO check case wehem RM is unavailable. ... now it will retry a each 5 min.  
				if(errorCode == XAException.XA_RETRY || errorCode == XAException.XAER_RMFAIL){
					// have to retry and return this code to the caller 
					xaExceptionToThrow = xaEx;
					result = false;
				} else {					
					rmsInfo.remove(rmInfo);
					if(xaExceptionToThrow == null){
						xaExceptionToThrow = xaEx;
					}
					// result is not set to false because transaction is completed and will be deleted.
				}
			} catch (RuntimeException e ){
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "TransactionManager tried to complete pending transaction but Unexpected exception was thrown.", e);
				result = false;				
			}
			someRMsAreUnreachable = someRMsAreUnreachable || (rmProps != null && rmProps.rmUnreachable);  
		}
		
		if(result){
			try {
				tLog.removeTransactionRecordLazily(transactionSequenceNumber);
			} catch (TLogIOException e) {
//				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Unexpected exception occurred when TransactionManager tried to remove transaction record for a transaction which was pending.", e);
				SimpleLogger.trace(Severity.ERROR,LOCATION, "ASJ.trans.000084", "Unexpected exception occurred when TransactionManager tried to remove transaction record for a transaction which was pending.");
			}
			if(someRMsAreUnreachable){
				TransactionStatistics.tmWasnotAbleToCompleteTransaction();
			}
		}
		if(xaExceptionToThrow != null){
			throw xaExceptionToThrow;
		}
		return result;
	}
	
	public boolean forgetTransaction(TLog tLog){
		if(!forgetSupported){
			return true;
		}
		
		ArrayList<PendingRMInfo> rmsInfoCopy = (ArrayList<PendingRMInfo>)rmsInfo.clone();
		for(PendingRMInfo rmInfo: rmsInfoCopy){
			if(!rmInfo.isForgetSupported()){
				continue;
			}
			RMPropsExtension rmProps = null;
			try {
				rmProps = tLog.getRMProperties(rmInfo.getRmID());
			} catch (InvalidRMKeyException e) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Resource manager with id="+rmInfo.getRmID()+" is not available into transaction log. All pending transactions with this resource manager will not be resolved. ", e);
				return false;
			} catch (TLogIOException e) {
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Cannot complete pending transactions because of unexpected exception from transaction log.", e);
				return false;
			}				
			RMContainerRegistryImpl rmContainerRegistryImpl = TransactionServiceFrame.getRmContainerRegistryImpl();
			if(rmContainerRegistryImpl == null){
				return false; // It is not possible to complete transactions if container registry is not initialized.
			}
			rmContainerRegistryImpl.fillRMPropertiesWithXAResources(new RMPropsExtension[]{rmProps});
			try{
				rmsInfo.remove(rmInfo);//forget is called only one. There is no second call to forget even if there is an exception from  first forget call.
				rmProps.forget(rmInfo.getXid());
			} catch (XAException xaEx){
				int errorCode = xaEx.errorCode;
				SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "TransactionManager tried to abandon pending transaction but XAException was thrown with error code " + TXR_TransactionImpl.convertXAErrorCodeToString(errorCode), xaEx);
			}
		}
		return true;
	}
	

	
	public  SAP_ITSAMExtendedPendingTransactionData getITSAMExtendedPendingTxData(){
		// TODO calculate status
		SAP_ITSAMRMData[] rmsData = new SAP_ITSAMRMData[rmsInfo.size()];
		int id = 0;
		for(PendingRMInfo rmInfo: rmsInfo){
			rmsData[id] = rmInfo.getITSAMRMData();
			id++;
		}
		SAP_ITSAMExtendedPendingTransactionData result =  new SAP_ITSAMExtendedPendingTransactionData(
				null, rmsData, detailedMessageID, detailedMessageParams ,
                !forgetSupported, forgetSupported, tLogVersion.getSystemIDAsString(),
				tLogVersion.getNodeID(), tLogVersion.getTmStartupTime(), transactionSequenceNumber,
				txClassifier, status, briefMessageID, briefMessageParams,
				null, null, null);
		SAP_ITSAMJ2eeActionStatus actionStatus = new SAP_ITSAMJ2eeActionStatus();
		actionStatus.setCode(SAP_ITSAMJ2eeActionStatus.OK_CODE);
		result.setActionStatus(actionStatus);		
		return result;
	}
	
	public SAP_ITSAMPendingTransactionData getITSAMPendingTxData(){
		// TODO calculate status
		SAP_ITSAMPendingTransactionData result =  new	SAP_ITSAMPendingTransactionData(
				tLogVersion.getSystemIDAsString(), tLogVersion.getNodeID(),
				tLogVersion.getTmStartupTime(), transactionSequenceNumber,
				txClassifier, status, briefMessageID, briefMessageParams,
				null, null, null);	
		SAP_ITSAMJ2eeActionStatus actionStatus = new SAP_ITSAMJ2eeActionStatus();
		actionStatus.setCode(SAP_ITSAMJ2eeActionStatus.OK_CODE);
		result.setActionStatus(actionStatus);		
		return result;		
	}
	
}
