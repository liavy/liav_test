package com.sap.engine.services.ts.recovery;

import javax.transaction.SystemException;

import com.sap.engine.interfaces.transaction.RMProps;
import com.sap.engine.interfaces.transaction.RMRepository;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionManagerImpl;
import com.sap.engine.services.ts.tlog.InvalidRMIDException;
import com.sap.engine.services.ts.tlog.InvalidRMKeyException;
import com.sap.engine.services.ts.tlog.RMNameAlreadyInUseException;
import com.sap.engine.services.ts.tlog.TLog;
import com.sap.engine.services.ts.tlog.TLogIOException;

public class RMRepositoryImpl implements RMRepository {

	private TXR_TransactionManagerImpl transactionManagerImpl = null;
	
	public RMRepositoryImpl(TXR_TransactionManagerImpl transactionManagerImpl){
		this.transactionManagerImpl = transactionManagerImpl;
	}
	
	public int addRM(String rmName, RMProps rmProps) throws IllegalArgumentException, SystemException {
		if(rmProps == null){
			throw new IllegalArgumentException("provided resource manager properties are null.");
		}
		if(rmProps.getKeyName() == null || rmProps.getKeyName().equals("")){
			rmProps.setKeyName(rmName);
		} else if(!rmProps.getKeyName().equals(rmName)){
		    throw new IllegalArgumentException("It is not possible to add this resource manager because the name which is set into RMProps is not the same as the provided resource manager name.");
		}		
		if(rmProps.getRmContainerName() == null || rmProps.getRmContainerName().equals("")){
			throw new IllegalArgumentException("Provided resource manager properties are not correct because RMContainerName is not set.");
		}
		if(rmProps.getNonSecureProperties() == null){
			throw new IllegalArgumentException("Provided resource manager properties are not correct because non secure properties are not set."); 
		}		
		if(rmProps.getSecureProperties() == null){
			throw new IllegalArgumentException("Provided resource manager properties are not correct because secure properties are not set."); 
		}
		try {
			TLog tlog = TransactionServiceFrame.getTLog();
			if(tlog == null){
				return transactionManagerImpl.generateUniqueRMId();
			} else {
				return tlog.registerNewRM(rmProps);
			}
		} catch (TLogIOException e) {
			SystemException sysEx = new SystemException("It is not possible to add resource manager " +rmProps.getKeyName()+" because of unexpected exception.");
			sysEx.initCause(e);
			throw sysEx;
		} catch (RMNameAlreadyInUseException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	public int getRMid(String rmName) throws SystemException{
		if(rmName == null){
			throw new SystemException("Provided resource manager name is null");
		}
		try {
			TLog tlog = TransactionServiceFrame.getTLog();
			if(tlog == null){
				throw new SystemException("It is not possible to obtain the ID for resource manager "+rmName+" because transaction log is not initialized.");
			}			
			return tlog.getRMIDByName(rmName);
		} catch (TLogIOException e) {
			SystemException systemException = new SystemException("It is not possible to obtain the ID for resource manager "+rmName+" due to " + e.toString());		
			systemException.initCause(e);			
			throw systemException;
		} catch (InvalidRMKeyException e) {
			SystemException sysEx = new SystemException("It is not possible to obtain the ID for resource manager "+rmName+" id due to " + e.toString());
			sysEx.initCause(e);
			throw sysEx;
		}
	}

	public void removeRM(String rmName) throws SystemException {
		if(rmName == null){
			throw new SystemException("Provided resource manager name is null");
		}
		int rmID = getRMid(rmName);
		try {
			TLog tlog = TransactionServiceFrame.getTLog();
			if(tlog == null){
				throw new SystemException("It is not possible to remove resource manager "+rmName+" because transaction log is not initialized. ");
			}						
			tlog.unregisterRM(rmID);
		} catch (TLogIOException e) {
			SystemException systemException = new SystemException("It is not possible to remove resource manager "+rmName+" due to " + e.toString());		
			systemException.initCause(e);			
			throw systemException;
		} catch (InvalidRMIDException e) {
			SystemException systemException = new SystemException("It is not possible to remove resource manager "+rmName+" due to " + e.toString());		
			systemException.initCause(e);			
			throw systemException;

		}
	}

}
