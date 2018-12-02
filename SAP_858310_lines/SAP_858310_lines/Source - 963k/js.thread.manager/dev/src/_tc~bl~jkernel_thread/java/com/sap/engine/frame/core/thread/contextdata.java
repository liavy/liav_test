package com.sap.engine.frame.core.thread;

import java.security.AccessControlContext;


public interface ContextData {

	/**
	 * Extracts all the needed information from the parent ThreadContext and saves it so that when needed all the 
	 * related data will be loaded in the child thread.
	 * 
	 */
	public void inheritFromCurrentThread(boolean cleanInheritance);
	
	/**
	 * Loads the extracted data from the parent thread in the clild thread. We need this method to ensure 
	 * proper inheritance among threads when working in thread pool environment.
	 * @param childTC
	 */
	public void loadDataInTheCurrentThread();
	
	public void empty();
	
	public AccessControlContext getAccessControlContext();
	
	public ClassLoader getContextClassLoader();
	
	public boolean isSystem();
	
	public void startInSystemThread(boolean isSystem);
}
