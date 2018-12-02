package com.sap.engine.services.ts.jta.statistics;

import java.util.HashMap;

import javax.transaction.Status;

import com.sap.engine.frame.core.thread.ThreadSystem;
import com.sap.engine.services.ts.TransactionServiceFrame;
import com.sap.engine.services.ts.mbeans.SAP_ITSAMTransactionStatisticsData;

public class TransactionStatistics {
	
	public final static String UNSET_NAME = "N/A";
	private final static int MAX_ROUGH_STATISTICS = 100;
	
	private static TransactionStatisticsData globalTxStatisticsData = new TransactionStatisticsData();
	
	private static HashMap<String, TransactionStatisticsData> rmTxStatisticsData = new HashMap<String, TransactionStatisticsData>();
	// TODO remove RM data when RM is removed
	private static HashMap<String, TransactionStatisticsData> classifierTxStatisticsData = new HashMap<String, TransactionStatisticsData>();
	private static Object hashMapsLocker = new Object();

	private static SingleTransactionStatistic[] roughStatisticData = new SingleTransactionStatistic[MAX_ROUGH_STATISTICS];
	private static int arrayCounter = 0;


	
	public static void transactionStarted(){
		globalTxStatisticsData.increaseActiveTxCount();
	}
	
	public static void trasactionSuspended(){
		globalTxStatisticsData.increaseSuspendedTxCount();
		globalTxStatisticsData.decreaseActiveTxCount();
	}
	
	public static void transactionResumed(){
		globalTxStatisticsData.decreaseSuspendedTxCount();
		globalTxStatisticsData.increaseActiveTxCount();
	}
	
	public static void transactionCommitted(){
		globalTxStatisticsData.decreaseActiveTxCount();
		globalTxStatisticsData.increaseTotalCommittedTxCount();		
	}
	
	public static void transactionRolledback(){
		globalTxStatisticsData.decreaseActiveTxCount();
		globalTxStatisticsData.increaseTotalRollbackedTxCount();		
	}
	
	public static void tmWasnotAbleToCompleteTransaction(){
		globalTxStatisticsData.increaseNotRecoveredTxCount();
	}
	
	public static void transactionCommitted(SingleTransactionStatistic singleStatistic){
		globalTxStatisticsData.decreaseActiveTxCount();
		globalTxStatisticsData.increaseTotalCommittedTxCount();
		if(singleStatistic.isHeuristicallyCompleted()){
			globalTxStatisticsData.increaseTxWithHeuristicOutcomesCount();
		}
		if(TransactionServiceFrame.enableDetailedTransactionStatistics){
			Runnable runnable = null;	
			synchronized (TransactionStatistics.class) {
				roughStatisticData[arrayCounter] = singleStatistic;
				arrayCounter++;
				if(arrayCounter == MAX_ROUGH_STATISTICS){
					try{
						runnable = new TaskRunner(roughStatisticData, arrayCounter);
						roughStatisticData = new SingleTransactionStatistic[MAX_ROUGH_STATISTICS];
					} finally {
						arrayCounter = 0;
					}
				}
			}
			if(runnable != null ){
				ThreadSystem ts = TransactionServiceFrame.getThreadSystem();
				if (ts != null) {
					ts.startCleanThread(runnable, false);
				} else {
					Thread t = new Thread(runnable);
					t.setDaemon(true);
					t.start();
				}
			}
			
		}
	}
	
	public static void transactionRolledback(SingleTransactionStatistic singleStatistic){
		globalTxStatisticsData.decreaseActiveTxCount();
		globalTxStatisticsData.increaseTotalRollbackedTxCount();
		if(singleStatistic.isHeuristicallyCompleted()){
			globalTxStatisticsData.increaseTxWithHeuristicOutcomesCount();
		}		
		if(singleStatistic.isTimeouted()){
			globalTxStatisticsData.increaseTimeoutedTxCount();
		}
		if(TransactionServiceFrame.enableDetailedTransactionStatistics){
			Runnable runnable = null;			
			synchronized (TransactionStatistics.class) {
				roughStatisticData[arrayCounter] = singleStatistic;
				arrayCounter++;
				if(arrayCounter == MAX_ROUGH_STATISTICS){	
					try{
						runnable = new TaskRunner(roughStatisticData, arrayCounter);
						roughStatisticData = new SingleTransactionStatistic[MAX_ROUGH_STATISTICS];
					} finally {
						arrayCounter = 0;
					}
				}
			}
			if (runnable != null ) {
				ThreadSystem ts = TransactionServiceFrame.getThreadSystem();
				if (ts!=null) {
					ts.startCleanThread(runnable, false);
				} else {
					Thread t = new Thread(runnable);
					t.setDaemon(true);
					t.start();
				}

			}
		}
	}
	
	public static synchronized void clearTransactionStatistics(){
		// the operation is not absolutely thread safe but the risk is small 
		//because 2 or more manual operations must be started at same time 
		rmTxStatisticsData.clear();
		classifierTxStatisticsData.clear();
		globalTxStatisticsData.clearNotRecoveredTxCount();
	} 
	
	static synchronized void updateData(SingleTransactionStatistic[] roughStatisticData_copy, int realCapacity){
		synchronized(hashMapsLocker){
			for(int i=0; i< realCapacity; i++){
				SingleTransactionStatistic singleTransactionStatistic = roughStatisticData_copy[i];
				TransactionStatisticsData classifierStatisticsData = classifierTxStatisticsData.get(singleTransactionStatistic.getTransactionClassifier());
				if(classifierStatisticsData == null){
					classifierStatisticsData = new TransactionStatisticsData();
					classifierStatisticsData.setName(singleTransactionStatistic.getTransactionClassifier());
					classifierTxStatisticsData.put(singleTransactionStatistic.getTransactionClassifier(), classifierStatisticsData);
				}
				TransactionStatisticsData[] rmStatisticsDatas =  getTransactionStatisticsDatasForRMs(singleTransactionStatistic.getAllRMNames());
				
				if(singleTransactionStatistic.getFinishStatus() == Status.STATUS_COMMITTED){
					classifierStatisticsData.increaseTotalCommittedTxCount();
					for(TransactionStatisticsData data : rmStatisticsDatas){
						data.increaseTotalCommittedTxCount();
					}
				}
				
				if(singleTransactionStatistic.getFinishStatus() == Status.STATUS_ROLLEDBACK){
					classifierStatisticsData.increaseTotalRollbackedTxCount();
					for(TransactionStatisticsData data : rmStatisticsDatas){
						data.increaseTotalRollbackedTxCount();
					}					
				}
				
				if(singleTransactionStatistic.isHeuristicallyCompleted()){
					classifierStatisticsData.increaseTxWithHeuristicOutcomesCount();
					for(TransactionStatisticsData data : rmStatisticsDatas){
						data.increaseTxWithHeuristicOutcomesCount();
					}						
				}
				
				if(singleTransactionStatistic.isAbandoned()){
					globalTxStatisticsData.increaseAbandonedTxCount();
					classifierStatisticsData.increaseAbandonedTxCount();
					for(TransactionStatisticsData data : rmStatisticsDatas){
						data.increaseAbandonedTxCount();
					}											
				}
				
				if(singleTransactionStatistic.isRolledbackByApplication()){
					globalTxStatisticsData.increaseTxRollbackedByApplication();
					classifierStatisticsData.increaseTxRollbackedByApplication();
					for(TransactionStatisticsData data : rmStatisticsDatas){
						data.increaseTxRollbackedByApplication();
					}							
				}
						
				if(singleTransactionStatistic.isTimeouted()){					
					classifierStatisticsData.increaseTimeoutedTxCount();
					for(TransactionStatisticsData data : rmStatisticsDatas){
						data.increaseTimeoutedTxCount();
					}							
				}				
				
				if(singleTransactionStatistic.isRolledbackBecauseOfRMError()){
					globalTxStatisticsData.increaseTxRollbackedBecauseRMError();
					classifierStatisticsData.increaseTxRollbackedBecauseRMError();
					TransactionStatisticsData[] failedRmDatas =  getTransactionStatisticsDatasForRMs(singleTransactionStatistic.getAllNames_of_FAILED_RMs());
					for(TransactionStatisticsData data : failedRmDatas){
						data.increaseTxRollbackedBecauseRMError();
					}							
				}
				
//				TODO times statistics
					
			}
		}
	}
	
	public static TransactionStatisticsData getGlobalTransactionStatisticsData(){
		return globalTxStatisticsData;
	}
	
	public static SAP_ITSAMTransactionStatisticsData getTransactionStatisticsData(){
		SingleTransactionStatistic[] roughStatisticData_copy =  new SingleTransactionStatistic[MAX_ROUGH_STATISTICS];
		int realCapacity = 0;
		synchronized (TransactionStatistics.class){
			System.arraycopy(roughStatisticData, 0, roughStatisticData_copy, 0, MAX_ROUGH_STATISTICS);
			realCapacity = arrayCounter;
			arrayCounter = 0;
		}
		updateData(roughStatisticData_copy, realCapacity);
		return globalTxStatisticsData.createITSAMData();
	}
	
	public static SAP_ITSAMTransactionStatisticsData[] getTransactionStatisticsPerRM() {
		SingleTransactionStatistic[] roughStatisticData_copy =  new SingleTransactionStatistic[MAX_ROUGH_STATISTICS];
		int realCapacity = 0;
		synchronized (TransactionStatistics.class){
			System.arraycopy(roughStatisticData, 0, roughStatisticData_copy, 0, MAX_ROUGH_STATISTICS);
			realCapacity = arrayCounter;
			arrayCounter = 0;
		}
		updateData(roughStatisticData_copy, realCapacity);		
		TransactionStatisticsData[] datas = rmTxStatisticsData.values().toArray(new TransactionStatisticsData[]{});
		SAP_ITSAMTransactionStatisticsData[] result = new SAP_ITSAMTransactionStatisticsData[datas.length];
		for(int i=0; i< datas.length; i++){
			result[i] = datas[i].createITSAMData();
		}
		return result;		
	}
	
	public static SAP_ITSAMTransactionStatisticsData[] getTransactionStatisticsPerTxClassifier() {
		SingleTransactionStatistic[] roughStatisticData_copy =  new SingleTransactionStatistic[MAX_ROUGH_STATISTICS];
		int realCapacity = 0;
		synchronized (TransactionStatistics.class){
			System.arraycopy(roughStatisticData, 0, roughStatisticData_copy, 0, MAX_ROUGH_STATISTICS);
			realCapacity = arrayCounter;
			arrayCounter = 0;
		}
		updateData(roughStatisticData_copy, realCapacity);
		TransactionStatisticsData[] datas = classifierTxStatisticsData.values().toArray(new TransactionStatisticsData[]{});
		SAP_ITSAMTransactionStatisticsData[] result = new SAP_ITSAMTransactionStatisticsData[datas.length];
		for(int i=0; i< datas.length; i++){
			result[i] = datas[i].createITSAMData();
		}
		return result;
	}
	
	private static TransactionStatisticsData[] getTransactionStatisticsDatasForRMs(String[] rmNames){
		TransactionStatisticsData[] result = new TransactionStatisticsData[rmNames.length];
		for(int i=0; i< rmNames.length; i++){
			TransactionStatisticsData transactionStatisticsData = rmTxStatisticsData.get(rmNames[i]);
			if(transactionStatisticsData == null){
				transactionStatisticsData = new TransactionStatisticsData();
				transactionStatisticsData.setName(rmNames[i]);
				rmTxStatisticsData.put(rmNames[i], transactionStatisticsData);
			}
			result[i] = transactionStatisticsData; 
		}
		return result;
	}
}
