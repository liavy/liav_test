package com.sap.engine.services.servlets_jsp.server.runtime.client;

import com.sap.engine.services.servlets_jsp.server.runtime.client.preservation.TimeoutListener;

public class RequestPreservationContext {
	
	private boolean dispatched = false;
	private boolean invoked = false;
	private long timeout = 0 ;
	private TimeoutListener listener= null;
	
	public RequestPreservationContext(){
	}
	
	public void setDispatched(boolean dispatched){
		this.dispatched = dispatched;
	}
	
	public boolean isDispatched(){
		return this.dispatched;
	}
	
	
	public void setInvoked(boolean invoked, long timeout){
		this.invoked = invoked;
		if (timeout > this.timeout){
			this.timeout = timeout;
			if (listener != null){
				listener.timeout(timeout);
			}
		}
	}
	
	public boolean isInvoked(){
		return this.invoked;
	}
	
	
	public long getTimeout(){
		return this.timeout;
	}
	
	public void registerTimeoutListener(TimeoutListener listener){
		this.listener = listener;
	}
	
	public String toString(){
		return Integer.toHexString(System.identityHashCode(this))+"\t dispatched : "+dispatched +"\t invoked : "+invoked+"\n";
	}
	
}
