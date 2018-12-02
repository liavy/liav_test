/*
 * Created on 2004.10.4
 *
 */
package com.sap.engine.core.thread;

import java.util.concurrent.atomic.AtomicInteger;

import com.sap.engine.core.Names;
import com.sap.engine.frame.core.thread.ThreadContext;
import com.sap.engine.lib.util.ArrayLong;
import com.sap.engine.lib.util.Stack;
import com.sap.engine.lib.util.iterators.SnapShotEnumeration;
import com.sap.engine.system.MonitoredThread;
import com.sap.engine.system.ShmThread;
import com.sap.engine.system.ShmThreadCallback;
import com.sap.engine.system.ThreadWrapper;
import com.sap.guid.GUID;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;
import com.sap.tools.memory.trace.AllocationStatisticRegistry;

/**
 * @author Petio Petev, Elitsa Pancheva
 *
 */
public class MonitoredThreadImpl implements MonitoredThread {
  
	private final static Location location = Location.getLocation(MonitoredThreadImpl.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);
	private final static Location locationForOperationIds = Location.getLocation(ThreadWrapper.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);
  ShmThread shmThread;
  private static final String CALLER_NAME = ThreadWrapper.class.getName();
  private static ThreadAnnotationHandler annotationHandler = new ThreadAnnotationHandler();
  
  /**
   * This ID is generated for a single user request; once set will not be changed during the whole execution of the request. GUID (2 longs).
   */
  private String transactionId = null;
  
  /**
   * The counter is incremented per transaction ID. The AtomicInteger is propagated from parent to child => 
   * 2 child threads will generate different correlation IDs. If someone sets the correlation ID, 
   * the counter will be initialized with its value.
   */
  private AtomicInteger taskCounter = new AtomicInteger(0);
   
  /*
   * This ID is generated on every pushTask request and stays the same through all the push/pop tasks operations.
   * It is cleared when the thread is returned back in the pool.
   * */
  private long currentTaskId = -1;
    
  static final int DEFAULT_CLUSER_ID = 0;
  
  private static final String DEFAULT_TASK_NAME = "";
        
  private Stack taskNames = null;
  private Stack subtaskNames = null;
  private Stack taskStates = null;
  private Stack subtaskStates = null;
  private ArrayLong taskIds = null;
  private Stack subtasksCounters = null;
  
  private TaskStackPrinter printer = new TaskStackPrinter();
  
  /*
   * Keeps the number of subtasks owned by the current task
   **/
  private int subtasksCounter = 0;
  
  public MonitoredThreadImpl(ShmThread shmThread, ShmThreadCallbackImpl callback) {
    this.shmThread = shmThread;
    if (callback != null) {
      callback.setDelegate(printer);
    }
    this.taskNames = new Stack();
    this.subtaskNames = new Stack();
    this.taskStates = new Stack();
    this.subtaskStates = new Stack();
    this.taskIds = new ArrayLong(20);
    this.subtasksCounters = new Stack();
  }
  
  public void setSubtaskName(String name) {
  	if (location.bePath()) {
  		location.pathT("setSubtaskName() with parameter ["+name+"] is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
  	}
  	
  	if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
  	
  	if (name == null) {
  		name = "";
  	}
  	
  	// check if there is still no task name, create a default task to which this subtask should be connected
  	if (taskNames.isEmpty() && !name.equals("")) {
  		setTaskName(DEFAULT_TASK_NAME);
  	}

  	if (subtaskNames.isEmpty()) {
  		// no subtask is currently set
  		if (!name.equals("")) { // no need to push empty name
  			subtaskNames.push(name);
    		subtaskStates.push(new Integer(ThreadWrapper.TS_NONE));
    		subtasksCounters.pop();
    		subtasksCounter = 1;
    		subtasksCounters.push(subtasksCounter);
  		}  		
  	} else {
  		subtaskNames.pop();
  		subtaskNames.push(name);
  	}
  	  	
    shmThread.setSubtaskName(name);
    shmThread.store();
  }

  public String getSubtaskName() {
  	if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return null;
  	}
  	
    return shmThread.getSubtaskName();
  }

  public void setUser(String user) {
  	if (location.bePath()) {
  		location.pathT("setUser() with parameter ["+user+"] is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
  	}
  	
  	if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
  	
    shmThread.setUserName(user);
    shmThread.store();
     
    // set as thread annotation
    if (ThreadAnnotationHandler.THREAD_ANNOTATIONS_AVAILABLE) {
 		  ThreadAnnotationHandler.getHandler().setUser(user); 
 		}
  }

  public void setTaskName(String name) {
  	if (location.bePath()) {
  		location.pathT("setTaskName() with parameter ["+name+"] is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
  	}
  	
  	if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
  	
  	if (name == null) {
  		name = "";
  	}
  	
  	// update the thread structure
  	if (taskNames.isEmpty()) {
  		  // no task is currently set
  		  taskNames.push(name);
  		  taskStates.push(new Integer(ThreadWrapper.TS_NONE));
  		  taskIds.add(currentTaskId);
  		  subtasksCounter = 0;
  		  subtasksCounters.push(subtasksCounter);
  	} else {
  		taskNames.pop();
  		taskNames.push(name);
  	}
  	// update in shared memory
    shmThread.setTaskName(name);
    shmThread.store();
  }

  public String getTaskName() {
  	if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return null;
  	}
  	
    return shmThread.getTaskName();
  }

	public int getThreadState() {
		if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return ThreadWrapper.TS_NONE;
  	}
		
		return shmThread.getState();
	}

	public void setThreadState(int state) {
		if (location.bePath()) {
  		location.pathT("setThreadState() with parameter ["+state+"] is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
  	}
		
		if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
		
    // check if there is still no task name, create a default task to which this state should be connected
  	if (taskNames.isEmpty()) {
  		setTaskName(DEFAULT_TASK_NAME);
  	}

  	// check if there is subtask set and if so set the state for it, otherwise set the state to the task
  	if (subtaskStates.isEmpty()) {
  	  taskStates.pop();
		  taskStates.push(new Integer(state));
  	} else {
  		subtaskStates.pop();
  		subtaskStates.push(new Integer(state));
  	}
		
		shmThread.setState(state);
    shmThread.store();
	}

	public void pushSubtask(String name, int state) {
		if (location.bePath()) {
  		location.pathT("pushSubtask() with parameters ["+name+"] ["+state+"] is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
  	}
		
		if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
		
		if (name == null) {
		  if (location.beWarning()) {
		  		SimpleLogger.trace(Severity.WARNING, location, "ASJ.krn_thd.000013", 
							"Name parameter passed to pushSubtask() method is NULL. This can lead to incomplete or misleading monitoring information in MMC Thread View. The caller of the method is [{0}], thread monitoring enabled [{1}]",
							getCallerClassName(new Exception()), ThreadWrapper.isthreadMonitoringEnabled());

	  	}
		  name = "";
		}
		
    // check first if there is a task already set, if not create one default
		if (taskNames.isEmpty()) {
			taskNames.push(DEFAULT_TASK_NAME);
			taskStates.push(ThreadWrapper.TS_NONE);
			taskIds.add(currentTaskId);
			subtasksCounters.push(0);
			subtasksCounter = 0;
		}
		// push the subtask in the stacks
		subtaskNames.push(name);
		subtaskStates.push(state);
		subtasksCounter ++;
		subtasksCounters.pop();
		subtasksCounters.push(subtasksCounter);
				
		// update it in shared memory
    shmThread.setSubtaskName(name);
    shmThread.setState(state);
    shmThread.store();
	}
	
	public void popSubtask() {
		if (location.bePath()) {
  		location.pathT("popSubtask() is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
  	}
		
		if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
		
		// first check boundary scenarios
		if (subtasksCounter == 0) { // cannot pop subtasks out of other tasks
			return;
		}
		
		if (subtaskNames.isEmpty()) {
			// no current subtask available => pop method is called more times than push method
			if (location.beDebug()) {
	      location.debugT("popSubtask() method is called more times than pushSubtask(). Caller stack follows");
	      location.traceThrowableT(Severity.DEBUG, "popSubtask() method is called without corresponding pushSubtask() method to be previously called.", new Exception("Caller stack trace follows"));
	    }
			return;
		} 
    
		String name = null;
		int state = ThreadWrapper.TS_NONE;
		
    // clear up the current subtask
		subtaskNames.pop();
		subtaskStates.pop();
				
		// load the info from the stacks heads to the current data
		subtasksCounter = 0;
		
		if (!subtasksCounters.isEmpty()) {
			subtasksCounter = (Integer) subtasksCounters.pop();
      // decrement with 1 since we have removed already one of the subtasks
			subtasksCounter --;
			subtasksCounters.push(subtasksCounter);
		}
				
		if (subtasksCounter > 0) {
			if (!subtaskNames.isEmpty()) {
				name = (String) subtaskNames.getFirst();
			}
			
			if (!subtaskStates.isEmpty()) {
				state = (Integer) subtaskStates.getFirst();
			}
		} else {
			// try to get the state og the task then
			if (!taskStates.isEmpty()) {
				state = (Integer) taskStates.getFirst();
			}
		}
				    
    shmThread.setSubtaskName(name);
    shmThread.setState(state);
    shmThread.store();
	}

	public void pushTask(String name, int state) {
		if (location.bePath()) {
  		location.pathT("pushTask() with parameter ["+name+"] ["+state+"] is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
  	}
		
		if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
		
		if (name == null) {
		  if (location.beWarning()) {
		  		SimpleLogger.trace(Severity.WARNING, location, "ASJ.krn_thd.000017", 
							"Name parameter passed to pushTask() method is NULL. This can lead to incomplete or misleading monitoring information in MMC Thread View. The caller of the method is [{0}]", getCallerClassName(new Exception()));
	  	}
		  name = "";
		}
		
		// always push the task also in the stack  
    taskNames.push(name);
    taskStates.push(new Integer(state));
    subtasksCounter = 0;
    subtasksCounters.push(subtasksCounter);
    
    shmThread.setSubtaskName("");
    shmThread.setTaskName(name);
    shmThread.setState(state);
        
    shmThread.store();
    
    // generate new ID for the new task
    currentTaskId = generateTaskId();
    taskIds.add(currentTaskId);
	}
	
	public void popTask() {
		if (location.bePath()) {
  		location.pathT("popTask() is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
  	}
		
		if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
		
		if (taskNames.isEmpty()) {
		  // no current task available => pop method is called more times than push method
			if (location.beDebug()) {
    	  location.debugT("popTask() method is called more times than pushTask(). Caller stack follows");
    	  location.traceThrowableT(Severity.DEBUG, "popTask() method is called without corresponding pushTask() method to be previously called.", new Exception("Caller stack trace follows"));
    	}
			return;
		}
		
		// clear up the current task
		taskNames.pop();
		taskStates.pop();
		taskIds.removeLastElement();
		// clear up all the subtasks for this task
		int counter = (Integer) subtasksCounters.pop();
		for (int i = 0; i < counter; i++) {
			subtaskNames.pop();
			subtaskStates.pop();
		}
		
		// load the info from the stacks heads to the current data
		String name = null;
		int state = ThreadWrapper.TS_NONE;
		currentTaskId = -1;
		String subTaskName = null;
		subtasksCounter = 0;
		
		if (!taskNames.isEmpty()) {
			name = (String) taskNames.getFirst();
		}
		
		if (!taskStates.isEmpty()) {
			state = (Integer) taskStates.getFirst();
		}
		
		if (!taskIds.isEmpty()) {
			currentTaskId = taskIds.get(taskIds.size() -1);
		}
		
		if (!subtasksCounters.isEmpty()) {
			subtasksCounter = (Integer) subtasksCounters.getFirst();
		}
		
		if (subtasksCounter > 0) {
			if (!subtaskNames.isEmpty()) {
				subTaskName = (String) subtaskNames.getFirst();
			}
			
			if (!subtaskStates.isEmpty()) {
				state = (Integer) subtaskStates.getFirst();
			}
		}
			
		if (shmThread.getTaskName() != null) {
      shmThread.setTaskName(name);
      shmThread.setState(state);
      shmThread.setSubtaskName(subTaskName);
      shmThread.store();
		} // otherwise nothing to pop
	}

  /* (non-Javadoc)
   * @see com.sap.engine.system.MonitoredThread#getCurrentTaskId()
   */
  public long getCurrentTaskId() {
  	return currentTaskId;
  }
  
  public AtomicInteger getCurrentTaskCounter() {
  	return taskCounter;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.system.MonitoredThread#getCurrentSubtaskTaskId()
   */
  public long getCurrentSubtaskId() {
    // not supported
    return -1;
  }
  
  public String getTransactionId() {
  	if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return null;
  	}
  	
  	if (transactionId == null) { // not generated yet => generate it
  		transactionId = generateTransactionID();
  	}
//  	if (location.bePath()) {
//  		location.pathT("getTransactionID() ["+transactionId+"] is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
//  	}
  	return transactionId;
  }
  
  private String generateTransactionID() {
  	String trID = new GUID().toHexString();
//		if (location.bePath()) {
//  		location.pathT("getTransactionID() successfully generated new id ["+trID+"]");
//  	}
		return trID;
  }
  
  public void setTransactionId(String transactionID) {
  	if (location.bePath()) {
  		location.pathT("setTransactionId() with parameter ["+transactionID+"] is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
  	}
  	
  	if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
  	
  	this.transactionId = transactionID;
  }

  
  public void setCurrentTaskId(long id) {
  	if (location.bePath()) {
  		location.pathT("setCurrentTaskId() with parameter id ["+id+"] is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
  	}
  	
  	if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
  	
  	setCurrentTaskId(id, null);
  }
  
  public void setCurrentTaskId(long id, AtomicInteger counter) {
  	if (location.bePath()) {
  		location.pathT("setCurrentTaskId() with parameters id ["+id+"] and counter ["+counter+"] is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
  	}
  	
  	if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
  	
    //  check if there is still no task name, create a default task to which this state should be connected
  	if (taskNames.isEmpty()) {
  		setTaskName(DEFAULT_TASK_NAME);
  	}
  	
  	currentTaskId = id;  
  	//TODO : check if we need to store the counter and cluster ID
  	if (locationForOperationIds.beDebug()) {
  	  locationForOperationIds.traceThrowableT(Severity.DEBUG, "setCurrentTaskId("+id+") method is called.", new Exception("Caller stack trace follows"));
  	}
  	// set it in the stack also
  	taskIds.removeLastElement();
  	taskIds.add(currentTaskId);
  	if (counter != null) {
  	  taskCounter = counter;
  	}
  }
  
  private long generateTaskId() {
  	long tid = -1;
  	
  	// first check if transaction ID is available, if not generate it; task ID should be always coupled with transaction ID
  	if (transactionId == null) { // not generated yet => generate it
  		transactionId = generateTransactionID();
  	}
  	
//  	if (ShmThreadLoggerImpl.clusterId == 0) {
//  		tid = twoIntsToLong(DEFAULT_CLUSER_ID, taskCounter.incrementAndGet());
//  	} else {
//  		tid = twoIntsToLong(ShmThreadLoggerImpl.clusterId, taskCounter.incrementAndGet());
//  	}
  	tid = ((long)ShmThreadLoggerImpl.clusterId * (long)1000000000) + taskCounter.incrementAndGet();
  	  	
  	if (location.bePath()) {
			location.pathT("ID generated is [" + tid + "] clusterID is [" + ShmThreadLoggerImpl.clusterId + "] transaction id is [" + transactionId + "] counter is [" + taskCounter.get() + "]");
		}
  	
  	return tid;
  }
    
  public void clearTaskIds() {
  	currentTaskId = -1;
  	transactionId = null;
  	taskIds.clear();
  	taskCounter = new AtomicInteger(0);
  	subtasksCounter = 0;
  	// clear the rest of the data in the monitored thread to avoid getting old data 
  	// when the thread tryies to process new Runnable.
  	taskNames.clear();
  	taskStates.clear();
  	subtasksCounters.clear();
  	subtaskNames.clear();
  	subtaskStates.clear();
  }
  
  public void clearThreadRelatedData () {
  	clearTaskIds();
    ThreadContext thctx = ThreadContextImpl.getThreadContext();
    if (!thctx.isSystem()) {
    	thctx.empty();
    }
  }
  
  public void clearManagedThreadRelatedData () {
  	try {
  		if (location.bePath()) {
	  		location.pathT("clearManagedThreadRelatedData() is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
	  	}
	  	AllocationStatisticRegistry.clearThreadTag();
	  	Thread.interrupted();
	  	clearThreadRelatedData();
	  	Thread.currentThread().setContextClassLoader(null);
	  	if (ContextDataImpl.VM_MONITORING_ENABLED) {
	  		if (ThreadAnnotationHandler.THREAD_ANNOTATIONS_AVAILABLE) {
	  		  ThreadAnnotationHandler.getHandler().cleanThreadAnnotations();
	  		}
	  	}
	  	if (shmThread != null) {
		  	shmThread.setSubtaskName("");
		    shmThread.setTaskName("");
		    shmThread.setState(ThreadWrapper.TS_NONE);
		    shmThread.store(); 
	  	}
  	} catch (Exception e) {
  		location.traceThrowableT(Severity.PATH, "Failed to clean all managed thread related data because of " + e, e);
  	}
  }
  
  //-------------ThreadAnnotations------------------------------------------------------------
 	  
	public void setApplicationName(String appName) {
		if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
		
		if (ThreadAnnotationHandler.THREAD_ANNOTATIONS_AVAILABLE) {
			if (location.bePath()) {
	  		location.pathT("setApplicationName() with parameter ["+appName+"] is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
	  	}
		  ThreadAnnotationHandler.getHandler().setApplicationName(appName); 
		}
	}
	
	public void setSessionID(String sessionID) {
		if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
		
		if (ThreadAnnotationHandler.THREAD_ANNOTATIONS_AVAILABLE) {
			if (location.bePath()) {
	  		location.pathT("setSessionID() with parameter ["+sessionID+"] is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
	  	}
		  ThreadAnnotationHandler.getHandler().setSessionID(sessionID); 
		}
	}
	 
	public void setRequestID(String requestID) {
		if (!ThreadWrapper.isthreadMonitoringEnabled()) {
  	  return;
  	}
		
		if (ThreadAnnotationHandler.THREAD_ANNOTATIONS_AVAILABLE) {
			if (location.bePath()) {
	  		location.pathT("setRequestID() with parameter ["+requestID+"] is executed by component ["+getCallerClassName(new Exception())+"], thread monitoring enabled [" + ThreadWrapper.isthreadMonitoringEnabled() + "].");
	  	}
		  ThreadAnnotationHandler.getHandler().setRequestID(requestID); 
		}
	}
	  
  private String getCallerClassName(Exception e) {
  	StackTraceElement[] stackLines = e.getStackTrace();
  	if (stackLines != null) {
  	  for (int i = 0; i < stackLines.length; i++) {
	 		  if (stackLines[i].getClassName().startsWith(CALLER_NAME)) { // to get both ThreadWrapper and ThreadWrapperExt
	 		  	return stackLines[i + 1].toString();
	 		  }
		  }
  	  if (stackLines.length >= 2) {
  	  	// MonitoredThreadImpl is called directly from the Thread management
  	  	return stackLines[1].toString();
  	  } 
  	}
  	// should not get in this case ever
  	return "N/A";
  }
  
  String printContent() {
  	return printer.getTaskStack();
  }
    
  // ------------------------ correlation ID helper methods
  
  //converts two integers (clusterId, correlation ID counter) into a long
  public static final long twoIntsToLong(int a, int b) {
    return ((long)b << 32) | a;
  }

  // converts a long into two integers (clusterId, correlation ID counter)
  public static final int[] longTo2Ints(long a) {
    int[] rez = new int[2];
    rez[0] = (((byte)a) & 0x000000ff) | ((((byte)(a >> 8)) & 0x000000ff) << 8) | ((((byte)(a >> 16)) & 0x000000ff) << 16) | (((byte)(a >> 24)) << 24);
    rez[1] = ((((byte)(a >> 32)) & 0x000000ff) << 32) | ((((byte)(a >> 40)) & 0x000000ff) << 40) | ((((byte)(a >> 48)) & 0x000000ff) << 48) | (((byte)(a >> 56)) << 56);
    return rez;
  }
  
 
	private class TaskStackPrinter extends TableDelimiterConstants implements ShmThreadCallback {

		public String getTaskStack() {
			SnapShotEnumeration tasks = taskNames.elementsEnumeration();
	  	SnapShotEnumeration subtasks = subtaskNames.elementsEnumeration();
	  	SnapShotEnumeration counters = subtasksCounters.elementsEnumeration();
	  	SnapShotEnumeration tasksStates = taskStates.elementsEnumeration();
	  	long[] tasksIds = taskIds.toArray();
	  	SnapShotEnumeration subtasksStates = subtaskStates.elementsEnumeration();
	  	
	  	Integer count = null;
	  	int counter = 0;
	  	
	  	StringBuilder result = new StringBuilder((taskNames.size() + subtaskNames.size() + taskNames.size()*3 + 5)*LINE_DELIMITER.length() + TRID.length() + 32);
	  	if (transactionId != null) {
	  	  result.append(TRID).append(transactionId).append(END_LINE);
	  	}
	  	result.append(LINE_DELIMITER);
	  	result.append(HEADLINE);
	  	result.append(EMPTY_LINE);
	  		  	
	  	StringBuilder tempLine = new StringBuilder(LINE_DELIMITER.length());
	  	StringBuilder tempSubtasksBlock = new StringBuilder(subtaskNames.size()*LINE_DELIMITER.length());
  		
  		String task = null;
  		String subtask = null;
  		Integer taskState = null;
  		long taskId = -1;
  		boolean currentTask = true;
  		int taskNumber = tasks.size();
  		int additionalOffset = 0;
  		int taskIdsCounter = tasksIds.length; 
	  	while (tasks.hasNext()) {
	  		additionalOffset = 0;
	  		// start constructing the task line [ID] <task_name> + <state>
	  		if ((taskIdsCounter --) >= 0) { // decrement counter here
	  			taskId = tasksIds[taskIdsCounter];
	  			if (taskId == UNAVAILABLE_TASK_ID) {
	  				result.append(ID_FRONT_HEADLINE_DELIM);
	  				result.append(ID_NA_BACK_DELIM);
	  			} else {
	  			  result.append(ID_FRONT_HEADLINE_DELIM);
	  			  result.append(taskId);
	  			  result.append(ID_BACK_HEADLINE_DELIM);
	  			}
	  		}
	  		
	  		result.append(EMPTY_LINE);
	  		
	  		task = (String) tasks.next();
	  		--taskNumber;
	  		additionalOffset += getAdditionalOffset(taskNumber);
	  		
	  		if (task.length() <= TASK_COUNTER - additionalOffset) {
	  			if (task.equals("")) {
	  				task = DEFAULT_TASK_NAME;
	  			}
	  			
	  			result.append(TASK_COUNTER_FRONT_DELIM);
	  			result.append(taskNumber);
	  			result.append(COUNTER_BACK_DELIM);
	  			result.append(task);
	  			result.append(fillWithSpaces(task.length(), TASK_COUNTER - additionalOffset));
	  		  
	  		  if (tasksStates.hasNext()) {
	  		  	taskState = (Integer) tasksStates.next();
	  		  	// check if this task has subtasks in order to decide where to put the arrow pointing to the currently executed task/subtask 
	  		  	if (currentTask) {
	  		  		if (counters.hasNext()) {
	  		  		  count = (Integer) counters.get();
	  		  		  if (count != null) {
	  		  		  	counter = count.intValue();
	  		  		  	if (counter <= 0) {
	  		  		  		// we have no subtasks for this task => the current state if the state of the task
	  		  		  		result.append(getStateAsString(taskState, true));
	  		  		  		currentTask = false; // put the flag to false as we already marked a state as current one
	  		  		  	} else {
	  		  		  		// we have to mark as current the state of the last subtask so continue on
	  		  		  		result.append(getStateAsString(taskState, false));
	  		  		  	}
	  		  		  }	else {
                  // we have no subtasks for this task => the current state if the state of the task
	  		  		  	result.append(getStateAsString(taskState, true));
  		  		  		currentTask = false; // put the flag to false as we already marked a state as current one
	  		  		  }  		  		  
	  		  		} else {
                // we have no subtasks for this task => the current state if the state of the task
	  		  			result.append(getStateAsString(taskState, true));
		  		  		currentTask = false; // put the flag to false as we already marked a state as current one
  		  		  } 
	  		  	} else {
	  		  		// the current state is already set => proceed with default behavior
	  		  		result.append(getStateAsString(taskState, false));
	  		  	}
	    			
	    		} else {
	    			result.append(EMPTY_STATE_LINE);
	    		}
	  		  
	  		} else {
	  			result.append(TASK_COUNTER_FRONT_DELIM);
	  			result.append(taskNumber);
	  			result.append(COUNTER_BACK_DELIM);
	  			result.append(task.substring(0, TASK_COUNTER -1 - additionalOffset));
	  			result.append(NEXT_LINE_DELIMITER);
	  			
	  			if (tasksStates.hasNext()) {
	  		  	taskState = (Integer) tasksStates.next();
	  		  	// check if this task has subtasks in order to decide where to put the arrow pointing to the currently executed task/subtask 
	  		  	if (currentTask) {
	  		  		if (counters.hasNext()) {
	  		  		  count = (Integer) counters.get();
	  		  		  if (count != null) {
	  		  		  	counter = count.intValue();
	  		  		  	if (counter <= 0) {
	  		  		  		// we have no subtasks for this task => the current state if the state of the task
	  		  		  		result.append(getStateAsString(taskState, true));
	  		  		  		currentTask = false; // put the flag to false as we already marked a state as current one
	  		  		  	} else {
	  		  		  		// we have to mark as current the state of the last subtask so continue on
	  		  		  		result.append(getStateAsString(taskState, false));
	  		  		  	}
	  		  		  }	else {
                  // we have no subtasks for this task => the current state if the state of the task
	  		  		  	result.append(getStateAsString(taskState, true));
  		  		  		currentTask = false; // put the flag to false as we already marked a state as current one
	  		  		  }  		  		  
	  		  		} else {
                // we have no subtasks for this task => the current state if the state of the task
	  		  			result.append(getStateAsString(taskState, true));
		  		  		currentTask = false; // put the flag to false as we already marked a state as current one
  		  		  } 
	  		  	} else {
	  		  		// the current state is already set => proceed with default behavior
	  		  		result.append(getStateAsString(taskState, false));
	  		  	}
	    			
	    		} else {
	    			result.append(EMPTY_STATE_LINE);
	    		}
	  			
	  			do {
	  			  task = task.substring(TASK_COUNTER -1 - additionalOffset); //substract 1 because we have to put "/" at the end of the line
	  			  if (task.length() <= TASK_COUNTER - additionalOffset) {
	  			  	result.append(TASK_BLANK_SPACE);
	  			  	result.append(fillWithSpaces(0, additionalOffset));
	  			  	result.append(task);
	  			  	result.append(fillWithSpaces(task.length(), TASK_COUNTER - additionalOffset));
	  			  	result.append(EMPTY_STATE_LINE);
	  	  		} else {
	  	  			result.append(TASK_BLANK_SPACE);
	  	  			result.append(fillWithSpaces(0, additionalOffset));
	  	  			result.append(task.substring(0, TASK_COUNTER -1 - additionalOffset));
	  	  			result.append(NEXT_LINE_DELIMITER);
	  	  			result.append(EMPTY_STATE_LINE);
	  	  		}
	  			} while (task.length() > TASK_COUNTER - additionalOffset);
	  		}
  			
	  		tempSubtasksBlock = new StringBuilder(subtaskNames.size()*LINE_DELIMITER.length());
	  		
	  		if (counters.hasNext()) {
	  		  count = ((Integer) counters.next());
	  		  if (count != null) {
	  		  	counter = count.intValue();
            int subtaskOffset = 0;
	  		  	for (int i = counter-1; i >= 0; i--) {
	  		  		subtaskOffset = additionalOffset;
	  		  		subtaskOffset += getAdditionalOffset(i + 1);
	  		  		tempLine = new StringBuilder(LINE_DELIMITER.length());
	  		    	if (subtasks.hasNext()) {
	  		    	  subtask = (String) subtasks.next();
	  		    	  
					  		if (subtask.length() <= SUBTASK_COUNTER - subtaskOffset) {
					  		  tempLine.append(SUBTASK_COUNTER_FRONT_DELIM);
					  		  tempLine.append(taskNumber);
					  		  tempLine.append(".");
					  		  tempLine.append(i+1);
					  		  tempLine.append(COUNTER_BACK_DELIM);
					  		  tempLine.append(subtask);
					  		  tempLine.append(fillWithSpaces(subtask.length(), SUBTASK_COUNTER - subtaskOffset));
					  		  					  		  
					  		  if (subtasksStates.hasNext()) {
					  		  	// if this is the last subtask state to be written and current state is still not marked => mark it
					  		  	if (i == counter-1 && currentTask) {
					  		  		tempLine.append(getStateAsString((Integer) subtasksStates.next(), true));
					  		  		currentTask = false;
					  		  	} else {
					  		  		tempLine.append(getStateAsString((Integer) subtasksStates.next(), false));
					  		  	}
					    		} else {
					    			tempLine.append(EMPTY_STATE_LINE);
					    		}
					  		  
					  		} else {
					  			tempLine.append(SUBTASK_COUNTER_FRONT_DELIM);
					  			tempLine.append(taskNumber);
					  			tempLine.append(".");
					  			tempLine.append(i+1);
					  			tempLine.append(COUNTER_BACK_DELIM);
					  			tempLine.append(subtask.substring(0, SUBTASK_COUNTER -1 - subtaskOffset));
					  			tempLine.append(NEXT_LINE_DELIMITER);
					  			
					  			if (subtasksStates.hasNext()) {
					  		  	// if this is the last subtask state to be written and current state is still not marked => mark it
					  		  	if (i == counter-1 && currentTask) {
					  		  		tempLine.append(getStateAsString((Integer) subtasksStates.next(), true));
					  		  		currentTask = false;
					  		  	} else {
					  		  		tempLine.append(getStateAsString((Integer) subtasksStates.next(), false));
					  		  	}
					    		} else {
					    			tempLine.append(EMPTY_STATE_LINE);
					    		}
					  			
					  			do {
					  			  subtask = subtask.substring(SUBTASK_COUNTER -1 - subtaskOffset); // substrack 1 for the "/" symbol we have to put at the end
					  			  if (subtask.length() <= SUBTASK_COUNTER - subtaskOffset) {
					  			  	tempLine.append(SUBTASK_BLANK_SPACE);
					  			  	tempLine.append(fillWithSpaces(0, subtaskOffset));
					  			  	tempLine.append(subtask);
					  			  	tempLine.append(fillWithSpaces(subtask.length(), SUBTASK_COUNTER - subtaskOffset));
					  			  	tempLine.append(EMPTY_STATE_LINE);
					  	  		} else {
					  	  			tempLine.append(SUBTASK_BLANK_SPACE);
					  	  			tempLine.append(fillWithSpaces(0, subtaskOffset));
					  	  			tempLine.append(subtask.substring(0, SUBTASK_COUNTER -1 - subtaskOffset));
					  	  			tempLine.append(NEXT_LINE_DELIMITER);
					  	  			tempLine.append(EMPTY_STATE_LINE);
					  	  		}
					  			} while (subtask.length() > SUBTASK_COUNTER - subtaskOffset);
					  		}	
						  }
	  		    	tempSubtasksBlock.insert(0, tempLine);
					  }
	  		  }
	  		}
	  		
	  		result.append(tempSubtasksBlock);
	  		result.append(EMPTY_LINE);
	  	}
	  	
	  	result.append(LINE_DELIMITER);
	  		  	
	  	return result.toString();
  }
	  private int getAdditionalOffset(int number) {
	  	if (number <= 9) {
  	    return 0;
  	  } else if (number <= 99) {
  	  	return 1;
  	  } else if (number <= 999) {
  	  	return 2;
  	  } else {
  	  	return 3;
  	  }
		}

		private String getStateAsString(int state, boolean currentState) {
  	  StringBuilder result = new StringBuilder(STATE_LENGTH + 2);
			switch (state) {
  	    case ThreadWrapper.TS_NONE:	{
  	    	if (currentState) {
  	    	  result.append(ARROW_MARK);
  	    	  result.append("N/A                  |\n");
  	    		return result.toString();
  	    	} else {
  	    		result.append(STATE_FRONT_DELIM);
  	    	  result.append("N/A                  |\n");
  	    		return result.toString();
  	    	}
  	    } case ThreadWrapper.TS_IDLE: {
  	    	if (currentState) {
  	    		result.append(ARROW_MARK);
  	    	  result.append("idle                 |\n");
  	    		return result.toString();
  	    	} else {
  	    		result.append(STATE_FRONT_DELIM);
  	    	  result.append("idle                 |\n");
  	    		return result.toString();
  	    	}
  	    } case ThreadWrapper.TS_WAITING_FOR_TASK:	{
  	    	if (currentState) {
  	    		result.append(ARROW_MARK);
  	    	  result.append("waiting for a task   |\n");
  	    		return result.toString();
  	    	} else {
  	    		result.append(STATE_FRONT_DELIM);
  	    	  result.append("waiting for a task   |\n");
  	    		return result.toString();
  	    	}
  	    } case ThreadWrapper.TS_PROCESSING:	{
  	    	if (currentState) {
  	    		result.append(ARROW_MARK);
  	    	  result.append("processing           |\n");
  	    		return result.toString();
  	    	} else {
  	    		result.append(STATE_FRONT_DELIM);
  	    	  result.append("processing           |\n");
  	    		return result.toString();
  	    	}
  	    } case ThreadWrapper.TS_WAITING_ON_IO: {
  	    	if (currentState) {
  	    		result.append(ARROW_MARK);
  	    	  result.append("waiting for sync. I/O|\n");
  	    		return result.toString();
  	    	} else {
  	    		result.append(STATE_FRONT_DELIM);
  	    	  result.append("waiting for sync. I/O|\n");
  	    		return result.toString();
  	    	}
  	    } default: {
  	    	if (currentState) {
  	    		result.append(ARROW_MARK);
  	    	  result.append("N/A                  |\n");
  	    		return result.toString();
  	    	} else {
  	    		result.append(STATE_FRONT_DELIM);
  	    	  result.append("N/A                  |\n");
  	    		return result.toString();
  	    	}
  	    }
      }
	  }
  	
		private String fillWithSpaces(int currentNumberOfChars, int targetedNumberOfChars) {
	  	StringBuilder result = new StringBuilder(targetedNumberOfChars - currentNumberOfChars + 1);
			
	  	while (currentNumberOfChars < targetedNumberOfChars) {
	  		result.append(" ");
	  		currentNumberOfChars  ++;
	  	}

			return result.toString();
	  }
	}
  
}

class TableDelimiterConstants {
	static final int TASK_LENGTH = 57; // !never put value less than 50 because the headline won't fit
	static final int STATE_LENGTH = 25; // !never change it, it is optimal now - adjusted to the longest defines state: waiting for sync. I/O. To change this constant you should change also the state column constants
	static final String VERTICAL_DELIMITER = "|";
	static final String HORISONTAL_DELIMITER = "-";
	static final String END_LINE = "\n";
	static final String TRID = "Log Correlation ID: ";
	
	static final String LINE_DELIMITER;// = "|-------------------------------------------------------------------------------------------------|---------------------------|\n";
  static {
		//build the LINE_DELIMITER
		StringBuilder buffer = new StringBuilder(TASK_LENGTH + STATE_LENGTH + 10); 
		buffer.append(VERTICAL_DELIMITER);
		for (int i = 0; i < TASK_LENGTH; i++) {
			buffer.append(HORISONTAL_DELIMITER);
		}
		buffer.append(VERTICAL_DELIMITER);
		for (int i = 0; i < STATE_LENGTH; i++) {
			buffer.append(HORISONTAL_DELIMITER);
		}
		buffer.append(VERTICAL_DELIMITER);
		buffer.append(END_LINE);
		LINE_DELIMITER = buffer.toString();
  }
  
  static final String EMPTY_SPACE = " ";
  static final String EMPTY_LINE;// =     "|                                                                                                 |                           |\n";
  static {
		//build the EMPTY_LINE
		StringBuilder buffer = new StringBuilder(TASK_LENGTH + STATE_LENGTH + 10); 
		buffer.append(VERTICAL_DELIMITER);
		for (int i = 0; i < TASK_LENGTH; i++) {
			buffer.append(EMPTY_SPACE);
		}
		buffer.append(VERTICAL_DELIMITER);
		for (int i = 0; i < STATE_LENGTH; i++) {
			buffer.append(EMPTY_SPACE);
		}
		buffer.append(VERTICAL_DELIMITER);
		buffer.append(END_LINE);
		EMPTY_LINE = buffer.toString();
  }
  
  static final int ID_LENGTH = 16; // clusterID + max int
    
  static final String ID_DELIMITER_LEFT = "| [";
  static final String ID_DELIMITER_RIGHT = "] ";
  static final String ID_FRONT_HEADLINE_DELIM = "|---- ID [";
	static final String ID_BACK_HEADLINE_DELIM; // =  "] --------------------------------------------------------------------|---------------------------|\n";
	static {
		//build the ID_BACK_HEADLINE_DELIM
		StringBuilder buffer = new StringBuilder(TASK_LENGTH + STATE_LENGTH + 10); 
		buffer.append(ID_DELIMITER_RIGHT);
		for (int i = 0; i < (TASK_LENGTH - (ID_FRONT_HEADLINE_DELIM.length() - 1) 
		                                 - ID_LENGTH 
		                                 - ID_DELIMITER_RIGHT.length()); i++) { // ID_FRONT_DELIM.length() -1 because otherwise the vertical delimeter will also be counted
			buffer.append(HORISONTAL_DELIMITER);
		}
		buffer.append(VERTICAL_DELIMITER);
		for (int i = 0; i < STATE_LENGTH; i++) {
			buffer.append(HORISONTAL_DELIMITER);
		}
		buffer.append(VERTICAL_DELIMITER);
		buffer.append(END_LINE);
		ID_BACK_HEADLINE_DELIM = buffer.toString();
  }
	
	static final String NOT_AVAILABLE = "N/A";
	static final String ID_NA_BACK_DELIM; //= "N/A] -----------------------------------------------------------------------------------|---------------------------|\n";
	static {
		//build the ID_NA_BACK_DELIM
		StringBuilder buffer = new StringBuilder(TASK_LENGTH + STATE_LENGTH + 10); 
		buffer.append(NOT_AVAILABLE);
		buffer.append(ID_DELIMITER_RIGHT);
		for (int i = 0; i < TASK_LENGTH - (ID_FRONT_HEADLINE_DELIM.length() - 1) 
		                                - NOT_AVAILABLE.length() 
		                                - ID_DELIMITER_RIGHT.length(); i++) { // ID_FRONT_DELIM.length() - 1 because otherwise the vertical delimeter will also be counted
			buffer.append(HORISONTAL_DELIMITER);
		}
		buffer.append(VERTICAL_DELIMITER);
		for (int i = 0; i < STATE_LENGTH; i++) {
			buffer.append(HORISONTAL_DELIMITER);
		}
		buffer.append(VERTICAL_DELIMITER);
		buffer.append(END_LINE);
		ID_NA_BACK_DELIM = buffer.toString();
  }
	
	static final String TASK_COLUMN_NAME = " TASKS / SUBTASKS STACK (current task is topmost)";
	static final String STATE_COLUMN_NAME = "    STATE";
	static final String HEADLINE; //= "| TASKS / SUBTASKS STACK (current task is topmost);                                                |     STATE                 |\n";
	static {
		//build the HEADLINE
		StringBuilder buffer = new StringBuilder(TASK_LENGTH + STATE_LENGTH + 10); 
		buffer.append(VERTICAL_DELIMITER);
		buffer.append(TASK_COLUMN_NAME);
		for (int i = 0; i < TASK_LENGTH - TASK_COLUMN_NAME.length(); i++) {
			buffer.append(EMPTY_SPACE);
		}
		buffer.append(VERTICAL_DELIMITER);
		buffer.append(STATE_COLUMN_NAME);
		for (int i = 0; i < STATE_LENGTH - STATE_COLUMN_NAME.length(); i++) {
			buffer.append(EMPTY_SPACE);
		}
		buffer.append(VERTICAL_DELIMITER);
		buffer.append(END_LINE);
		HEADLINE = buffer.toString();
  }
	
	static final String TASK_COUNTER_FRONT_DELIM = "| (";
	static final String SUBTASK_COUNTER_FRONT_DELIM = "|     (";
	static final String COUNTER_BACK_DELIM = ") ";
	
	static final String STATE_FRONT_DELIM = "|    ";
	static final String ARROW_MARK = "|==> ";
	
	static final String EMPTY_STATE_LINE; //= "|                           |\n";
	static {
		//build the EMPTY_STATE_LINE
		StringBuilder buffer = new StringBuilder(TASK_LENGTH + STATE_LENGTH + 10); 
		buffer.append(VERTICAL_DELIMITER);
		for (int i = 0; i < STATE_LENGTH; i++) {
			buffer.append(EMPTY_SPACE);
		}
		buffer.append(VERTICAL_DELIMITER);
		buffer.append(END_LINE);
		EMPTY_STATE_LINE = buffer.toString();
  }
		
	static final String TASK_BLANK_SPACE = "|     ";
	static final String SUBTASK_BLANK_SPACE = "|           ";
	
	static final int TASK_COUNTER = TASK_LENGTH - TASK_COUNTER_FRONT_DELIM.length() - COUNTER_BACK_DELIM.length(); // the vertical delimiter in TASK_COUNTER_FRONT_DELIM is taken in account instead of counting the counter number itself.
	static final int SUBTASK_COUNTER = TASK_LENGTH - SUBTASK_COUNTER_FRONT_DELIM.length() - COUNTER_BACK_DELIM.length() -2; // the vertical delimiter in SUBTASK_COUNTER_FRONT_DELIM is taken in account instead of counting the counter number itself + "." + subtask counter.
	
	static final String NEXT_LINE_DELIMITER = "\\";
	static final String DEFAULT_TASK_NAME = "<>";	
	static final String NOT_SET_ID = "-1";
	static final String UNAVAILABLE_ID = "-1";
	static final long UNAVAILABLE_TASK_ID = -1;

}
