package com.sap.engine.core.thread;

import com.sap.engine.system.ShmThreadCallback;

/**
 * Callback wrapper. 
 * @author Elitsa Pancheva
 *
 */
public class ShmThreadCallbackImpl implements ShmThreadCallback {
		
	private ShmThreadCallback delegate = null;
	
	void setDelegate(ShmThreadCallback callback) {
		this.delegate = callback;
	}
	
	public String getTaskStack() {
		if (delegate != null) {
			return delegate.getTaskStack();
		} else {
			return "";
		}
	}
	
	
}
