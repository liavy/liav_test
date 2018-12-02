package com.sap.engine.services.servlets_jsp.server.runtime.client.preservation;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletRequestFacadeWrapper;
import com.sap.engine.services.servlets_jsp.server.runtime.client.HttpServletResponseFacadeWrapper;

import com.sap.engine.services.servlets_jsp.server.runtime.client.RequestPreservationContext;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public abstract class PreservationEntity{
	
	private static Location location = Location.getLocation(LogContext.getLocationRequestPreservation(),"PreservationEntity");
	
	private HttpServletRequestFacadeWrapper request = null;
	private HttpServletResponseFacadeWrapper response =null;
	private AtomicBoolean dispatched = new AtomicBoolean(false);
	
	
	protected RequestPreservationManager manager;
	protected long timeout = 0;
	protected AtomicBoolean timeoutEnabled = new AtomicBoolean(false);
	protected Set<RequestPreservationContext> contexts = null;
	protected AtomicBoolean scheduledForCleanUp = new AtomicBoolean(false);
	protected ReentrantLock lock = new ReentrantLock();
	protected String id;
	
	public PreservationEntity(HttpServletRequestFacadeWrapper request, HttpServletResponseFacadeWrapper response,
			ObservableHashSet<RequestPreservationContext> contexts,
				long timeout,
				boolean dispatch,
					RequestPreservationManager manager){
		this.id = getObjectInstanceId(this);
		this.response = response;
		this.request = request;
		this.contexts = contexts;
		this.manager = manager;
		this.timeout = timeout;
		
		if (this.timeout == -1){
			this.timeout = Long.MAX_VALUE;
			timeoutEnabled.set(true);
		}else if (timeout != 0){
			timeoutEnabled.set(true);
		}
		
		//dispatched.set(checkedDispatched());
		dispatched.set(dispatch);
		
		contexts.addObserver(new RequestPreservationObserver(manager,this));
		
	}

	public HttpServletRequestFacadeWrapper getRequest() {
		return request;
	}

	public HttpServletResponseFacadeWrapper getResponse() {
		return response;
	}
	
	public String toString(){
		return "id: "+id+"\t timoutEnabled: "+timeoutEnabled+"\t timeout: "+timeout+"\t request: "+getObjectInstanceId(request)+"\t response: "+ getObjectInstanceId(response)+"\t contexts: "+contexts;
	}

	public boolean isDispatched() {
		return dispatched.get();
	}

	public boolean isSetEmpty() {
		return contexts.isEmpty();
	}

	public abstract boolean isTimeoutEnabled();
	
	private boolean checkedDispatched(){
		//if all child contexts are dispatched then this is dispatched preservation entity
		boolean dispatched = true;
		try{
			lock.lock();
		
			Iterator<RequestPreservationContext> iter = contexts.iterator();
			while (iter!= null && iter.hasNext()){
				RequestPreservationContext context = iter.next();
				dispatched = dispatched && context.isDispatched();
				if (!dispatched){
					break;
				}
			}
		}catch (Throwable t) {
      if (location.beWarning()) {
        //SimpleLogger.trace(int severity, Location location, String dcName, String csnComponent, String messageID, String message, Exception exc, Object... args)
        SimpleLogger.trace(Severity.WARNING, location, null, null, "ASJ.web.000645", "checkedDispatched(): ", t, new Object[0]);
      }
		}finally{
			lock.unlock();
		}
		return dispatched;
	}

	public void scheduleForCleanUp() {
		if (location.beDebug()){
			location.debugT("@@@@@@-schedule for cleanup: "+getId());
		}
		scheduledForCleanUp.set(true);
	}
	
	public void unscheduleForCleanUp() {
		if (location.beDebug()){
			location.debugT("@@@@@@-unschedule for cleanup: "+getId());
		}
		scheduledForCleanUp.set(false);
	}
	
	public boolean isScheduledForCleanUp(){
			return scheduledForCleanUp.get();
	}
	
	public boolean isNotScheduledForCleanUp_set(){
		return scheduledForCleanUp.compareAndSet(false, true);
	}
	
	public String getId(){
		return id;
	}
	
	private String getObjectInstanceId(Object obj){
		if (obj == null){
			return "null";
		}
		return Integer.toString(System.identityHashCode(obj));
	}
	
}
