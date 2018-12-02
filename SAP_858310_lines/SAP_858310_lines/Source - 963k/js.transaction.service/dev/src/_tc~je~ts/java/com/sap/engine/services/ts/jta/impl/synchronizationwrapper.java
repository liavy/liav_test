package com.sap.engine.services.ts.jta.impl;

import javax.transaction.Synchronization;

import com.sap.engine.interfaces.transaction.SynchronizationExtension;
import com.sap.engine.interfaces.transaction.SynchronizationPriorityExtension;

public class SynchronizationWrapper implements SynchronizationPriorityExtension {
	
	  private Synchronization synch = null;
	
	  public SynchronizationWrapper(Synchronization synch){
		  this.synch = synch; 		  
	  }
	
	  public void beforeRollback(){
		  if(synch instanceof SynchronizationExtension){
			  ((SynchronizationExtension)synch).beforeRollback();			  
		  }		  
	  }
	  
	  public int getPriority(){
		  return MIN_PRIORITY;		  
	  }
	  
	  public void beforeCompletion(){
		  synch.beforeCompletion();		  
	  }

	  public void afterCompletion(int status){
		  synch.afterCompletion(status);		  
	  }
	  
}
