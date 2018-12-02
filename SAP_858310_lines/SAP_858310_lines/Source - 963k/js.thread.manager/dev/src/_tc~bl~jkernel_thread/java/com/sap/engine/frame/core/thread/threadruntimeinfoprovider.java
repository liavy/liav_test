package com.sap.engine.frame.core.thread;

import com.sap.engine.core.Names;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.interfaces.IThreadRuntimeInfoProvider;


public class ThreadRuntimeInfoProvider implements IThreadRuntimeInfoProvider {
	private static boolean registered = false;
	private static Location location = Location.getLocation(ThreadRuntimeInfoProvider.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);
	
	public static boolean isAlreadyRegistered() {
		return registered;
	} 
	
	public void setRegistered() {
		location.traceThrowableT(Severity.PATH, "The call stack of the ThreadRuntimeInfoProvider registration follows", new Exception("ThreadRuntimeInfoProvider.setRegistered() method is called."));
		registered = true;
	}
	
	public long getCorrelationID() {
		return ThreadWrapper.getCurrentTaskId();
	}

	public String getDsrTransactionID() {
		return ThreadWrapper.getTransactionId();
	}
	
}
