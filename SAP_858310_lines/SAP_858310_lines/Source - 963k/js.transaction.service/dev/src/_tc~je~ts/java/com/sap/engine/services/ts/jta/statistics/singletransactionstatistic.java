package com.sap.engine.services.ts.jta.statistics;

import javax.transaction.SystemException;

public interface SingleTransactionStatistic {

	public String getTransactionClassifier();
	
	public int getFinishStatus();
	
	public String[] getAllRMNames();
	
	public String[] getAllNames_of_FAILED_RMs();
	
	public boolean isRolledbackByApplication();
	
	public boolean isRolledbackBecauseOfRMError();
	
	public boolean isTimeouted();
	
	public boolean isAbandoned();
	
	public boolean isHeuristicallyCompleted();
	
	public long getCommitOrRollbackDurationInMillis();
	
}
