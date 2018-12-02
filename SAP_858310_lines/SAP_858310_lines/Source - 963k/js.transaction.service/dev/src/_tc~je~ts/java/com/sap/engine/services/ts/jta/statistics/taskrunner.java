package com.sap.engine.services.ts.jta.statistics;



public class TaskRunner implements Runnable{

	private SingleTransactionStatistic[] singleTransactionStatistics = null;
	private int realCapacity = 0;
	
	public TaskRunner(SingleTransactionStatistic[] singleTransactionStatistics, int realCapacity){
		this.singleTransactionStatistics = singleTransactionStatistics;
		this.realCapacity = realCapacity;
	}
	
	public void run() {
		TransactionStatistics.updateData(singleTransactionStatistics, realCapacity);		
	}	
	

}
