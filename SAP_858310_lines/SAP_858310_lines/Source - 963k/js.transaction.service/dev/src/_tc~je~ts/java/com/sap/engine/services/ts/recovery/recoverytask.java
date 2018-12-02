package com.sap.engine.services.ts.recovery;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;

import javax.transaction.xa.XAException;

import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.tlog.InvalidRMKeyException;
import com.sap.engine.services.ts.tlog.RMPropsExtension;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogIOException;
import com.sap.engine.services.ts.tlog.TLogReaderWriter;
import com.sap.engine.services.ts.tlog.TransactionRecord;
import com.sap.engine.services.ts.utils.ByteArrayUtils;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class RecoveryTask implements Runnable {

	private static final Location LOCATION = Location.getLocation(RecoveryTask.class);		
	private TLogReaderWriter tLogReaderWriter = null;
	private RMContainerRegistryImpl rmContainerRegistryImpl = null;
	public RecoveryTask(TLogReaderWriter tLogReaderWriter, RMContainerRegistryImpl rmContainerRegistryImpl){
		this.tLogReaderWriter = tLogReaderWriter;
		this.rmContainerRegistryImpl = rmContainerRegistryImpl;
	}
	
	public void run() {
		

		TLog tlogForRecovery = null;
		try {
			
			Thread.sleep(TransactionServiceFrame.recoveryRetryInterval * 1000);
			while (true){
				tlogForRecovery = null;				
				while (tlogForRecovery == null){
					try{
						tlogForRecovery = tLogReaderWriter.lockOrphanedTLog();
						break;
					} catch (TLogIOException e){
						SimpleLogger.traceThrowable(Severity.INFO, LOCATION, "ASJ.trans.000085","RecoveryInfo:Tlog is temporary unavailable. Transaction recovery task will try to open transaction log after 2 seconds.", e);
					}
					Thread.sleep(2000);
				}
				if(tlogForRecovery == null){
					SimpleLogger.trace(Severity.INFO, LOCATION, "ASJ.trans.000086","RecoveryInfo:All transaction records are successfully recovered.");
					return;
				}			           
				doRecover(tlogForRecovery);
			}
		} catch (TLogIOException e) {
			SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000087","RecoveryError: Initial recovery cannot be performed because of unexpected exception", e);
			return;
		} catch (InvalidRMKeyException e) {
			SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000088","RecoveryError: Initial recovery cannot be performed because of unexpected exception", e);
			return;
		} catch (InterruptedException e) {
			SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000089","RecoveryError: Initial recovery cannot be performed because of unexpected exception", e);
			return; 
		}
		
	}
	
	
	private void doRecover(TLog tlogForRecovery) throws TLogIOException, InvalidRMKeyException, InterruptedException{
		
		
		int[] rmIds = tlogForRecovery.getAllUsedRMIDs();
		Hashtable<Integer,RMPropsExtension> resourceManagers = new Hashtable<Integer, RMPropsExtension>();
		
		for(int i=0; i<rmIds.length; i++){
			RMPropsExtension rmPropsExt = tlogForRecovery.getRMProperties(rmIds[i]);
			if(rmPropsExt != null){
				rmPropsExt.setRmID(rmIds[i]);
			} else {
				SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000090","RecoveryError: Cannot recover trnsactions because resource mnager with unknown ID was returned from transaction log.");
				continue;
			}
			resourceManagers.put(rmIds[i], rmPropsExt);
		}		
		boolean allRMsAreAvailable = false;
		boolean allRecordsRecovered = false;
		
		while(!allRMsAreAvailable || ! allRecordsRecovered){
			
			RMPropsExtension[] rmPropsExtensions = (RMPropsExtension[])resourceManagers.values().toArray(new RMPropsExtension[]{});
			if(!allRMsAreAvailable){
				allRMsAreAvailable = rmContainerRegistryImpl.fillRMPropertiesWithXAResources(rmPropsExtensions);
				
		    	for(RMPropsExtension rmPropsExt : rmPropsExtensions){
		    		try {
						rmPropsExt.recover(tlogForRecovery.getTLogVersion());
					} catch (XAException e) {	
						SimpleLogger.traceThrowable(Severity.INFO, LOCATION,e, "ASJ.trans.000091","RecoveryWarning: Exception occured from recovery method of resource manager {0}", new Object[] { rmPropsExt.getKeyName()});
						allRMsAreAvailable = false;
					} catch (IllegalArgumentException e){
						SimpleLogger.traceThrowable(Severity.INFO, LOCATION,e, "ASJ.trans.000092","RecoveryWarning: Resource manager {0} is temporary unavailable",  new Object[] { rmPropsExt.getKeyName()});
						allRMsAreAvailable = false;					
					}
		    	}		
			}
			
			if(!allRecordsRecovered){
				allRecordsRecovered = true;
				Iterator<TransactionRecord> transactionRecords = tlogForRecovery.getAllTransactionRecords();	
				while (transactionRecords.hasNext()){
					TransactionRecord transactionRecord = transactionRecords.next();
					boolean transactionRecordSuccessfullyRecovered = true;
					int[] txRMIDs = transactionRecord.getRMIDs();
					byte[] branchIterators = transactionRecord.getBranchIterators();
					if(txRMIDs == null){
						SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000093","RecoveryError: Transaction record #{0} into transaction managers logs is corrupted because array with resource managers is null. This transaction will not be recovered.", new Object[] {transactionRecord.getTransactionSequenceNumber()});
						transactionRecordSuccessfullyRecovered = false;
						continue;
					}
					if(branchIterators == null){
						SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000094","RecoveryError: Transaction record #{0} into transaction managers logs is corrupted because array with branch iterators is null. This transaction will not be recovered.", new Object[] {transactionRecord.getTransactionSequenceNumber()});
						transactionRecordSuccessfullyRecovered = false;
						continue;					
					}
					if(txRMIDs.length != branchIterators.length){
						SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000095","RecoveryError: Transaction record #{0} into transaction managers logs is corrupted because array with branch iterators is null. This transaction will not be recovered.", new Object[] {transactionRecord.getTransactionSequenceNumber()});
						transactionRecordSuccessfullyRecovered = false;
						continue;					
					}
					
					for (int i = 0; i<txRMIDs.length; i++){
						RMPropsExtension rmManagerForThisId = resourceManagers.get(txRMIDs[i]);
						if(rmManagerForThisId == null){
							SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000096","RecoveryError: There is a transaction record into transaction managers logs which points to unknown ResourceManager. This ResourceManager will be skiped.");
							continue;
						}
						if(rmManagerForThisId.ignoredBecauseAnotherSameRM){//other RM will complete the transactions
							continue;
						}
						if(!rmManagerForThisId.recoveredSuccessfully){
							SimpleLogger.trace(Severity.ERROR, LOCATION, "ASJ.trans.000097","RecoveryError: There is a transaction record into transaction managers logs which points to ResourceManager {0} which is temporary unavailable.", new Object[] { rmManagerForThisId.getKeyName()});
							transactionRecordSuccessfullyRecovered = false;
							continue;						
						}
						byte[] globalTransactionID = new byte[44];
						byte[] tlogVersion = tlogForRecovery.getTLogVersion().getTLogVersion();//15
						System.arraycopy(tlogVersion, 0, globalTransactionID, 0, 15);
						ByteArrayUtils.addLongInByteArray(transactionRecord.getTransactionAbandonTimeout(), globalTransactionID, 15);//8 abandon timeout
						ByteArrayUtils.addIntInByteArray(transactionRecord.getTransactionClassifierID(), globalTransactionID, 23);//23 = 15+8; 4 classifier ID
						ByteArrayUtils.addLongInByteArray(transactionRecord.getTransactionBirthTime(), globalTransactionID, 27);//27= 23+4;  8 bytes for tx birth time
						ByteArrayUtils.addLongInByteArray(transactionRecord.getTransactionSequenceNumber(), globalTransactionID, 35);// 35=27+8; 8 bytes for tx sequence number
						globalTransactionID[43] = branchIterators[i];// 1 byte for branch iterator 35+8+1 = 44
						
						try {
							rmManagerForThisId.commitTxForRecovery(globalTransactionID);
						} catch (XAException e) {
							// TODO evaluate XA return code
							// TODO TODO TODO how to implement second try, because now the xid will be rolledback at the end. 
							SimpleLogger.traceThrowable(Severity.ERROR, LOCATION,e, "ASJ.trans.000098", "RecoveryError: ResourceManager{0} was not able to commit its transaction branch.", new Object[] {  rmManagerForThisId.getKeyName()});
							transactionRecordSuccessfullyRecovered = false;
						} catch (RuntimeException e){
							SimpleLogger.traceThrowable(Severity.ERROR, LOCATION, "Recovery internal Error: ResourceManager"+rmManagerForThisId.getKeyName()+" was not able to commit its transaction branch.", e);
							transactionRecordSuccessfullyRecovered = false;							
						}	
					}
					if(transactionRecordSuccessfullyRecovered){
						tlogForRecovery.removeTransactionRecordLazily(transactionRecord.getTransactionSequenceNumber());
					} else {
						allRecordsRecovered = false;
					}				
				}
			}
	        
			for(RMPropsExtension rmPropsExtension : rmPropsExtensions){
				if(rmPropsExtension.recoveredSuccessfully){
					try {
						rmPropsExtension.rollbackTxForRecovery();
					} catch (XAException e) {
						SimpleLogger.traceThrowable(Severity.ERROR, LOCATION,e, "ASJ.trans.000099", "RecoveryError: ResourceManager {0} was not able to rollback its transaction branch.", new Object[] { rmPropsExtension.getKeyName()} );
					}
					resourceManagers.remove(rmPropsExtension.getRmID());
				}
			}	
			
			Thread.sleep(TransactionServiceFrame.recoveryRetryInterval * 1000);
		}
		
		
		tlogForRecovery.close();		
	}

    protected static final String getStackTrace(Throwable e) {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    (e).printStackTrace(new PrintStream(baos));
	    return baos.toString();
	}		
	
}
