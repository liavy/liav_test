package com.sap.engine.frame.core.thread;

import java.util.concurrent.atomic.AtomicInteger;

import com.sap.engine.core.Names;
import com.sap.engine.frame.client.ClientException;
import com.sap.engine.frame.client.ClientFactory;
import com.sap.engine.system.MonitoredThread;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class ClientMonitorThreadBuilder implements MonitoredThread {
	
	private static Location location = Location.getLocation(ClientMonitorThreadBuilder.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);
	
	public void clearManagedThreadRelatedData() {
	}

	public long getCurrentSubtaskId() {
		return ThreadWrapper.ID_NOT_AVAILABLE;
	}

	public AtomicInteger getCurrentTaskCounter() {
		return null;
	}

	public long getCurrentTaskId() {
		return ThreadWrapper.ID_NOT_AVAILABLE;
	}

	public String getSubtaskName() {
		return null;
	}

	public String getTaskName() {
		return null;
	}

	public int getThreadState() {
		return ThreadWrapper.TS_NONE;
	}

	public String getTransactionId() {
		String result = null;
		try {
			ClientIDPropagator propagator = (ClientIDPropagator) ClientFactory.getThreadContextFactory().getThreadContext().getContextObject(ClientIDPropagator.PROPAGATOR_CONTEXT_OBJECT_NAME);
			if (propagator != null) {
			  result = propagator.getTransactionId();
			}
		} catch (ClientException e) {			
			location.traceThrowableT(Severity.PATH, "Cannot get access to ClientIDPropagator context object, transaction id to return will be null.", e);
		}
		return result;
	}

	public void popSubtask() {
	}

	public void popTask() {
	}

	public void pushSubtask(String subtask, int state) {
	}

	public void pushTask(String task, int state) {
	}

	public void setApplicationName(String appName) {
	}

	public void setCurrentTaskId(long tid) {
	}

	public void setCurrentTaskId(long tid, AtomicInteger counter) {
	}

	public void setRequestID(String rid) {
	}

	public void setSessionID(String sid) {
	}

	public void setSubtaskName(String name) {
	}

	public void setTaskName(String name) {
	}

	public void setThreadState(int state) {
	}

	public void setTransactionId(String trid) {
		try {
			ClientIDPropagator propagator = (ClientIDPropagator) ClientFactory.getThreadContextFactory().getThreadContext().getContextObject(ClientIDPropagator.PROPAGATOR_CONTEXT_OBJECT_NAME);
			if (propagator != null) {
			  propagator.setTransactionId(trid);
			}
		} catch (ClientException e) {
			location.traceThrowableT(Severity.PATH, "Cannot get access to ClientIDPropagator context object, transaction id cannot be set to " + trid, e);
		}
	}

	public void setUser(String arg0) {
	}

}
