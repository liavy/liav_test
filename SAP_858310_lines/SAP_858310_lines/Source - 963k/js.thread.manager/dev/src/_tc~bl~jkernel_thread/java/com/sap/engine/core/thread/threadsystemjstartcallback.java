package com.sap.engine.core.thread;

  import com.sap.bc.proj.jstartup.ThreadManagerCallbackAdapter;

  public class ThreadSystemJstartCallback extends ThreadManagerCallbackAdapter {
  	
    public boolean isSystem() {
    	return ThreadContextImpl.getThreadContext().isSystem();
    }
    
    public void setSystem(boolean isSystem) {
    	((ThreadLocal) ThreadContextImpl.getThreadContext()).remove();
    	((ThreadContextImpl) ThreadContextImpl.getThreadContext()).markAsSystem();
    }
    
  }
