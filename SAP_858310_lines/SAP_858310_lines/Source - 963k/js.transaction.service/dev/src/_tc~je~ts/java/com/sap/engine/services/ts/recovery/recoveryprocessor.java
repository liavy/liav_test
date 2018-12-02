package com.sap.engine.services.ts.recovery;

import com.sap.engine.frame.core.thread.ThreadSystem;
import com.sap.engine.services.ts.tlog.TLogReaderWriter;

public class RecoveryProcessor {
	
	private TLogReaderWriter tlogReaderWriter = null;
	private ThreadSystem threadSystem = null;
	private RMContainerRegistryImpl rmContainerRegistryImpl = null;
//	public static ThreadSystem threadSystem = null;
	 
//	public RecoveryProcessor(TLogReaderWriter tlogReaderWriter){
//		this.tlogReaderWriter = tlogReaderWriter;		
//	}

	public RecoveryProcessor(ThreadSystem threadSystem, RMContainerRegistryImpl rmContainerRegistryImpl){
		this.threadSystem = threadSystem;
		this.rmContainerRegistryImpl = rmContainerRegistryImpl;
	}

	
	public boolean recoverTLogs(){
		RecoveryTask recoveryTask = new RecoveryTask(tlogReaderWriter, rmContainerRegistryImpl);		
		threadSystem.startCleanThread(recoveryTask, true);// true means that a new system thread will be started.
		return true;
	}
	
	
	
}
