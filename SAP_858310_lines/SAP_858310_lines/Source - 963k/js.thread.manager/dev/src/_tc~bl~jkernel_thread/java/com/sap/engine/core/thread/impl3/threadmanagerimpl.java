/*
 * Copyright (c) 1999 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.core.thread.impl3;

import com.sap.bc.proj.jstartup.JStartupFramework;
import com.sap.engine.core.Names;
import com.sap.engine.core.thread.ContextDataImpl;
import com.sap.engine.core.thread.JavaThreadsCallback;
import com.sap.engine.core.thread.LogHelper;
import com.sap.engine.core.thread.MonitoredThreadBuilder;
import com.sap.engine.core.thread.ShmThreadLoggerImpl;
import com.sap.engine.core.thread.ThreadContextImpl;
import com.sap.engine.core.thread.ThreadManagementInterface;
import com.sap.engine.core.thread.ThreadManager;
import com.sap.engine.core.thread.ThreadSystemJstartCallback;
import com.sap.engine.core.thread.execution.CentralExecutor;
import com.sap.engine.core.thread.execution.ExecutorFactoryImpl;
import com.sap.engine.frame.ProcessEnvironment;
import com.sap.engine.frame.core.thread.ClientIDPropagator;
import com.sap.engine.frame.core.thread.ContextData;
import com.sap.engine.frame.core.thread.ContextObject;
import com.sap.engine.frame.core.thread.ThreadContext;
import com.sap.engine.frame.core.thread.Task;
import com.sap.engine.frame.core.thread.ThreadRuntimeInfoProvider;
import com.sap.engine.frame.state.ManagementInterface;
import com.sap.engine.lib.util.WaitQueue;
import com.sap.engine.lib.util.base.BaseQueue;
import com.sap.engine.lib.util.base.NextItem;
import com.sap.engine.lib.util.iterators.RootIterator;
import com.sap.engine.system.ShmThreadImpl;
import com.sap.engine.system.ThreadWrapper;
import com.sap.jvm.monitor.thread.ThreadWatcher;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingManager;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidParameterException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class implements ThreadManager interface. It is a thread manager based on
 * FIFO (first in, first out) queue. Runnable objects are executed in the same
 * order they have been added. When some of the threads finishes its job, the
 * thread tries to get a job from Runnable queue. If there are no objects the
 * thread waits until there is at least one in job queue.
 *
 * @author Elitsa Pancheva
 * @version 7.10
 */
public class ThreadManagerImpl extends BaseQueue implements ThreadManager {

  protected final boolean isSystem = false;

  //info for Runnable objects
  WaitQueue queue;
  ActionObjectPool pool;

  //thread properties
  protected int initialThreadCount;
  protected int minThreadCount;
  protected int maxThreadCount;
  protected int timeToLiveIdle;
  protected int executorPoolMaxSize;
  protected int percentageOfParallelismAllowed = 0;
  protected int memoryThreshold;
  protected ProcessEnvironment.RestartModes restartMode;

  //runnable queue properties

  //position in Runnable arrays
  protected int waitRequestThreads;

  // shows if the thread monitoring is enabled or disabled
  boolean threadMonitoringEnabled = true;
  protected static final String MONITORING_ENABLED_PROPERTY_NAME = "ThreadMonitoringEnabled";  
  protected static final String OOM_RESTART_THRESHOLD_PROPERTY_NAME = "OOMRestartThreshold";
  protected static final String OOM_RESTART_MODE_NAME = "OOMRestartMode";
  protected static final String THREAD_LOCALS_CLEANUP_ENABLED_PROPERTY_NAME = "ThreadLocalsCleanupEnabled";
  protected static final String ALWAYS_RESTART_MODE = ProcessEnvironment.RestartModes.ALWAYS.toString();
  protected static final String RESTART_ON_UNHANDLED_MODE = ProcessEnvironment.RestartModes.RESTART_ON_UNHANDLED.toString();
  protected static final String RESTART_UNDER_THRESHOLD_MODE = ProcessEnvironment.RestartModes.RESTART_UNDER_THRESHOLD.toString();
  protected static final String DISABLED_RESTART_MODE = ProcessEnvironment.RestartModes.DISABLED.toString();
  protected static final int OOM_RESTART_THRESHOLD_DEFAULT = 1024*1024;
  protected boolean isResize = false;
  //references for service needs
  private ThreadContextImpl rootThreadContext = (ThreadContextImpl)ThreadContextImpl.getThreadContext();
  //framework-invokation indicators
  volatile protected boolean isShutDown = false;
  private Properties properties;
  protected static final String THREADS_NAME_HEADER = "Application";
  protected static ThreadGroup threadGroup = new ThreadGroup(THREADS_NAME_HEADER + "ThreadGroup");
  public static AtomicInteger threadStartCounter = new AtomicInteger(0);
  ErrorQueueHandler errorQueueHandlerThread = null;
  UncaughtExceptionHandler uncaughtExceptionHandler = null;
  
  /**
   * Used for logging
   */
  private final static Location location = Location.getLocation(ThreadManagerImpl.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);
  private final static Category category = Category.SYS_SERVER;
  private static ThreadManagementInterface tManagement = null;
  protected final String timeToIdleString = new Integer(timeToLiveIdle).toString();


  /**
   * Constructor. Framework invokes it.
   *
   */
  public ThreadManagerImpl() {
    waitRequestThreads = 0;
    rootThreadContext.setSystem(isSystem);
    tManagement = new ThreadManagementInterfaceImpl(this);
  }

  //{{ METHODS FROM INTERFACE com.sap.engine.core.Manager
  /**
   * Shutting down the Manager when reboot is needed.
   * All resources must be disposed.
   */
  public void shutDown(Properties properties) {
    SingleThread th;
    RootIterator iter;
    location.pathT("Server is shutting down.");
 
    try {
//    shutdown Executors logic
       ExecutorFactoryImpl.shutdown();
    } catch (Exception e) {
      location.infoT("Unexpected exception caught when shuting down thread executors " + e);
      location.catching("shutDown()", e);
    }

    synchronized(this) {
      isShutDown = true;
      notifyAll();
    }

    iter = this.elementsIterator();
    if (!iter.isAtEnd()) {
      while (!iter.isAtEnd()) {
        th = (SingleThread) iter.next();
        if (location.bePath() && th.getTaskDetails() != null) {
          location.pathT("Unexpected thread activity in shutdown of " + THREADS_NAME_HEADER + "ThreadManager:\nThread[" + th.getName() + "]\nTask: " + th.getTaskDetails() + "\nwill be marked for stop.");
        }
        th.stopThread();
      }
    }
    
    // wait a bit before interrupting the threads
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      location.catching(e);
    }
        
    iter = this.elementsIterator();
    if (!iter.isAtEnd()) {
      while (!iter.isAtEnd()) {
        th = (SingleThread) iter.next();
        try {
          th.interrupt();
        } catch (Exception ex) {
          location.traceThrowableT(Severity.ERROR, "Error occurs while interrupting threads for shutdown.", ex);
        }
      }
    }

    // give chance to threads to be interrupted 
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      location.catching(e);
    }

    iter = this.elementsIterator();
    if (!iter.isAtEnd()) {
      if (location.bePath()) {
        location.pathT("Unexpected thread activity in shutdown of " + THREADS_NAME_HEADER + "ThreadManager. Full thread dump will be made");
        ProcessEnvironment.getThreadDump("Unexpected thread activity in shutdown of " + THREADS_NAME_HEADER + "ThreadManager. Full thread dump will be made");
      }
      
      while (!iter.isAtEnd()) {
        th = (SingleThread) iter.next();        
        if (th.getTaskDetails() != null) {
          location.infoT("Unexpected thread activity after interrupt() is executed in shutdown of " + THREADS_NAME_HEADER + "ThreadManager:\nThread[" + th.getName() + "]\nTask: " + th.getTaskDetails());
        }
      }
    }
    
    errorQueueHandlerThread.stopThread();
      
    //    SingleThread.security = null;
    location.logT(Severity.INFO, "ThreadManager stopped.");
  }

  /**
   * Initialazing Manager with specified properties
   *
   * @param   properties  Manager Properties
   * @return   if initialization of the Manager is successful returns true
   * @exception InvalidParameterException if there are incorrect parameter
   */
  public boolean init(Properties properties) throws InvalidParameterException {
    //check properties for NULL value
    if (properties == null) {
      properties = new Properties();
    }
    this.properties = properties;
    
    // check if the ThreadLocals cleanup is enabled
    String stringValue = properties.getProperty(THREAD_LOCALS_CLEANUP_ENABLED_PROPERTY_NAME, "true");
    boolean toRemoveThreadLoclas = true;
		if (stringValue.equalsIgnoreCase("false")) {
			if (location.beInfo()) {
			  location.infoT(THREAD_LOCALS_CLEANUP_ENABLED_PROPERTY_NAME + " property value is ["+stringValue+"] => No ThreadLocals cleanup will be performed before the thread is returned back in the pool.");
			}
			toRemoveThreadLoclas = false;
		} else {  
			if (location.beDebug()) {
				location.debugT(THREAD_LOCALS_CLEANUP_ENABLED_PROPERTY_NAME + " property value is ["+stringValue+"] => No ThreadLocals cleanup will be performed before the thread is returned back in the pool.");
			}
			toRemoveThreadLoclas = true;
		}
		rootThreadContext.enableThreadLocalsCleanup(toRemoveThreadLoclas);
        
    // check if the thread monitoring is enabled
    stringValue = properties.getProperty(MONITORING_ENABLED_PROPERTY_NAME, "true");
		if (stringValue.equalsIgnoreCase("false")) {
			if (location.beInfo()) {
			  location.infoT(MONITORING_ENABLED_PROPERTY_NAME + " property value is ["+stringValue+"] => Thread Monitoring will be disabled.");
			}
			threadMonitoringEnabled = false;
		} else {  
			if (location.beDebug()) {
			  location.debugT(MONITORING_ENABLED_PROPERTY_NAME + " property value is ["+stringValue+"] => Thread Monitoring will be enabled.");
			}
			threadMonitoringEnabled = true;
		}
  			
    // if VM monitoring is enabled initialize additional thread monitoring and error queue
    if (ContextDataImpl.VM_MONITORING_ENABLED) {
      try {
			  ThreadWrapper.setMonitorThreadBuilder(new MonitoredThreadBuilder(), threadMonitoringEnabled);
			  
			  ThreadWatcher threadHandler = ThreadWatcher.getInstanceOnce();
			  threadHandler.registerCallback(new JavaThreadsCallback());
		  } catch (Exception e1) {
			  SimpleLogger.trace(Severity.WARNING, location, 
					  			"ASJ.krn_thd.000015", 
					  			"Cannot enable java.lang.Thread monitoring due to: [{0}]", 
					  			e1);
			  
    	  if (location.bePath()) {
    		  location.traceThrowableT(Severity.PATH, "Cannot enable java.lang.Thread monitoring", e1);
    	  }    	  
		  }
          
      // start the ErrorQueue handling thread
      errorQueueHandlerThread = new ErrorQueueHandler(this);
      errorQueueHandlerThread.start();
    } 
   
    try {
    	JStartupFramework.registerThreadManagerCallback(new ThreadSystemJstartCallback());
    } catch (ThreadDeath td) {
    	throw td;
    } catch (OutOfMemoryError oom) {
    	ProcessEnvironment.handleOOM(oom);
    } catch (Throwable t) {
    	location.warningT("Cannot register ThreadManagerCallback in JStartupFramework because of: " + t);
    	if (location.bePath()) {
    		location.traceThrowableT(Severity.PATH, "Cannot register ThreadManagerCallback in JStartupFramework", t);
    	}
    }

    int clusterId = 0;
    try {
    	clusterId = getProperty(properties, "ClusterId", 0);
    	
    } catch (Exception e) {
    	SimpleLogger.trace(Severity.WARNING, location, 
	  						"ASJ.krn_thd.000084", 
	  						"Cannot read clusterId property due to:  [{0}]", e);
    	if (location.bePath()) {
    		location.traceThrowableT(Severity.PATH, "Cannot read clusterId property", e);
    	}
    }
    
    if (threadMonitoringEnabled) { // if monitoring is disabled no need to register hooks in logging
	    // initialize the logger for ShmThreads
	  	ShmThreadLoggerImpl shmLogger = new ShmThreadLoggerImpl();
	    ShmThreadImpl.setLoggerToUse(shmLogger);
	    
	    if (clusterId != 0) {
    		shmLogger.setClusterId(clusterId);
    	}
	    
	    // register ContextObject for transaction ID propagation
	    registerContextObject(ClientIDPropagator.PROPAGATOR_CONTEXT_OBJECT_NAME, new ClientIDPropagator(null, false));
	    // register thread runtime info provider
	    ThreadRuntimeInfoProvider infoProvider = new ThreadRuntimeInfoProvider();
	    LoggingManager.registerThreadRuntimeInfoProvider(infoProvider);
	    infoProvider.setRegistered();
		}
    
    int initialSize = 0, maxSize = 0;
    //init free objects properties
    try {
      initialSize = getProperty(properties, "InitialFOQSize", 200);
      maxSize = getProperty(properties, "MaxFOQSize", 5000);

      switch (checkPoolProperties(initialSize, maxSize)) {
        case 1:
          {
            SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000035", "Negative values are not allowed for ApplicationThreadManager properties InitialFOQSize [{0}] and MaxFOQSize [{1}]", initialSize, maxSize);
            throw new InvalidParameterException("Negative values are not allowed for ApplicationThreadManager properties InitialFOQSize ["+initialSize+"] and MaxFOQSize ["+maxSize+"]");
          }
        case 2:
          {
            SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000036", "Wrong correlation between InitialFOQSize [{0}] and MaxFOQSize [{1}] properties' values of ApplicationThreadManager", initialSize, maxSize);
            throw new InvalidParameterException("Wrong correlation between InitialFOQSize ["+initialSize+"] and MaxFOQSize ["+maxSize+"] properties' values of ApplicationThreadManager");
          }
      }

      //initalize pool with free objects
      pool = new ActionObjectPool(initialSize, maxSize);
    } catch (NumberFormatException nfException) {
      nfException.printStackTrace();
      SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000037", "Not numerical vallues of ApplicationThreadManager properties InitialFOQSize [{0}] and MaxFOQSize [{1}]", initialSize, maxSize);
      location.catching("init()", nfException);
      throw new InvalidParameterException("Not numerical vallues of ApplicationThreadManager properties InitialFOQSize ["+initialSize+"] and MaxFOQSize ["+maxSize+"]");
    }
    
    //init runnable queue properties
    try {
      maxSize = getProperty(properties, "MaxRQSize", 5000);

      if (checkQueueProperties(maxSize) == 1) {
        String messageWithId = "Negative value [{0}] is specified for ApplicationThreadManager property [MaxRQSize]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.";
        SimpleLogger.log(Severity.ERROR, category, location, 
        				"ASJ.krn_thd.000064", 
        				"Negative value [{0}] is specified for ApplicationThreadManager property [MaxRQSize]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.",
        				new Object[]{maxSize});
        
        throw new InvalidParameterException(LogHelper.buildMessage(messageWithId, maxSize));
      }
      //initialize queue with Runnable objects
      queue = new WaitQueue(maxSize);

    } catch (NumberFormatException nfException) {
      location.catching(nfException);
      String messageWithId = "Incorrect value [{0}] is specified for ApplicationThreadManager property [MaxRQSize]. The value cannot be transformed to a number [{1}]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.";
      SimpleLogger.log(Severity.ERROR, category, location, 
    		  		   "ASJ.krn_thd.000065",
    		  		   "Incorrect value [{0}] is specified for ApplicationThreadManager property [MaxRQSize]. The value cannot be transformed to a number [{1}]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.",
    		  		   new Object[]{properties.getProperty("MaxRQSize"), nfException});
      throw new InvalidParameterException(LogHelper.buildMessage(messageWithId, properties.getProperty("MaxRQSize"), nfException));
    }
    
    //init thread count properties, parsing the given ones.
    try {
      initialThreadCount = getProperty(properties, "InitialThreadCount", 100);
      minThreadCount = getProperty(properties, "MinThreadCount", 50);
      maxThreadCount = getProperty(properties, "MaxThreadCount", 500);

      switch (checkProperties(initialThreadCount, minThreadCount, maxThreadCount)) {
        case 1:
          {
	        	int value = -1;
	        	String name = null;
	        	if (initialThreadCount < 0) {
	        		value = initialThreadCount;
	        		name = "InitialThreadCount";
	        	} else if (minThreadCount < 0) {
	        		value = minThreadCount;
	        		name = "MinThreadCount";
	        	} else {
	        		value = maxThreadCount;
	        		name = "MaxThreadCount";
	        	}
	        	
	        	String messageWithId = "Negative value [{0}] is specified for ApplicationThreadManager property [{1}]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.";

	        	SimpleLogger.log(Severity.ERROR, category, location,
	        						"ASJ.krn_thd.000067",
	        						"Negative value [{0}] is specified for ApplicationThreadManager property [{1}]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.",
	        						new Object[]{value, name});
	        	
	        	throw new InvalidParameterException(LogHelper.buildMessage(messageWithId, value, name));
          }
        case 2:
          {
        	  String messageWithId = null;
        	  if (initialThreadCount < minThreadCount) {
        		  messageWithId = "Incorrect value [{0}] is specified for ApplicationThreadManager property [InitialThreadCount]. The value [{1}] is smaller than the value[{2}] of property [MinThreadCount]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.";
        		  SimpleLogger.log(Severity.ERROR, category, location, 
      		  			"ASJ.krn_thd.000066", 
      		  			"Incorrect value [{0}] is specified for ApplicationThreadManager property [InitialThreadCount]. The value [{1}] is smaller than the value[{2}] of property [MinThreadCount]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.",
      		  			new Object[]{initialThreadCount, initialThreadCount, minThreadCount});
        		  
        		  messageWithId = LogHelper.buildMessage(messageWithId, new Object[]{initialThreadCount, initialThreadCount, minThreadCount});
        		  
        	  } else {
        		  messageWithId = "Incorrect value [{0}] is specified for ApplicationThreadManager property [InitialThreadCount]. The value [{1}] is bigger than the value[{2}] of property [MaxThreadCount]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.";
        		  SimpleLogger.log(Severity.ERROR, category, location, 
      		  			"ASJ.krn_thd.000095", 
      		  			"Incorrect value [{0}] is specified for ApplicationThreadManager property [InitialThreadCount]. The value [{1}] is bigger than the value[{2}] of property [MaxThreadCount]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.",
      		  			new Object[]{initialThreadCount, initialThreadCount, maxThreadCount});
        		  
        		  messageWithId = LogHelper.buildMessage(messageWithId, new Object[]{initialThreadCount, initialThreadCount, maxThreadCount});
        	  }
	          
	          throw new InvalidParameterException(messageWithId);
          }
      }

      this.setLimit(maxThreadCount);
      for(int i = 0; i < initialThreadCount; i++) {
        SingleThread th = newInstance();
        addLastItem(th);
        //the OutOfMemoryError will be processed from the invoker
        th.start();
      }
    } catch (NumberFormatException nfException) {
      location.catching("init()", nfException);
      String messageWithId = "Incorrect value is specified for one of the following properties of ApplicationThreadManager: [InitialThreadCount][{0}], [MinThreadCount][{1}], [MaxThreadCount][{2}]. The value cannot be transformed to a number [{3}]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.";
      SimpleLogger.log(Severity.ERROR, category, location, 
    		  			"ASJ.krn_thd.000068",
    		  			"Incorrect value is specified for one of the following properties of ApplicationThreadManager: [InitialThreadCount][{0}], [MinThreadCount][{1}], [MaxThreadCount][{2}]. The value cannot be transformed to a number [{3}]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.",
      					new Object[]{properties.getProperty("InitialThreadCount"), 
    		  						properties.getProperty("MinThreadCount"), 
    		  						properties.getProperty("MaxThreadCount"),
    		  						nfException});
      
      throw new InvalidParameterException(LogHelper.buildMessage(messageWithId,
						    		  		properties.getProperty("InitialThreadCount"), 
						    		  		properties.getProperty("MinThreadCount"), 
						    		  		properties.getProperty("MaxThreadCount"),
						    		  		nfException));
    }
    
    try {
      executorPoolMaxSize = getProperty(properties, "ExecutorPoolMaxSize", minThreadCount);

      if (checkQueueProperties(executorPoolMaxSize) != 0) {
        String messageWithId = "Invalid value [{0}] is specified for ApplicationThreadManager property [ExecutorPoolMaxSize]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.";

        SimpleLogger.log(Severity.ERROR, category, location,
        				"ASJ.krn_thd.000069",
        				"Invalid value [{0}] is specified for ApplicationThreadManager property [ExecutorPoolMaxSize]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.",
        				new Object[]{executorPoolMaxSize});
        
        throw new InvalidParameterException(LogHelper.buildMessage(messageWithId, executorPoolMaxSize));
      } 
    } catch (NumberFormatException nfException) {
      location.catching(nfException);
      String messageWithId = "Incorrect value [{0}] is specified for ApplicationThreadManager property [ExecutorPoolMaxSize]. The value cannot be transformed to a number [{1}]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.";

      SimpleLogger.log(Severity.ERROR, category, location,
    		  			"ASJ.krn_thd.000070",
    		  			"Incorrect value [{0}] is specified for ApplicationThreadManager property [ExecutorPoolMaxSize]. The value cannot be transformed to a number [{1}]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.",
    		  			new Object[]{properties.getProperty("ExecutorPoolMaxSize"), nfException});
      
      throw new InvalidParameterException(LogHelper.buildMessage(messageWithId, properties.getProperty("ExecutorPoolMaxSize"), nfException));
    }
    
    try {
    	percentageOfParallelismAllowed = getProperty(properties, "PercentageOfParallelismAllowed", 30);

      if (checkQueueProperties(percentageOfParallelismAllowed) != 0) {
    	  String messageWithID = "Invalid value [{0}] is specified for ApplicationThreadManager property [PercentageOfParallelismAllowed]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.";

    	  SimpleLogger.log(Severity.ERROR, category, location, 
    			  			"ASJ.krn_thd.000071",
						  	"Invalid value [{0}] is specified for ApplicationThreadManager property [PercentageOfParallelismAllowed]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.",
						  	new Object[]{percentageOfParallelismAllowed});
    	  
    	  throw new InvalidParameterException(LogHelper.buildMessage(messageWithID, percentageOfParallelismAllowed));
      } 
    } catch (NumberFormatException nfException) {
      location.catching(nfException);
      
      String messageWithId = "Incorrect value ["+properties.getProperty("ExecutorPoolMaxSize")+"] is specified for ApplicationThreadManager property [ExecutorPoolMaxSize]. The value cannot be transformed to a number ["+nfException+"]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.";
      
      SimpleLogger.log(Severity.ERROR, category, location,
    		  			"ASJ.krn_thd.000073",
    		  			"Incorrect value [{0}] is specified for ApplicationThreadManager property [ExecutorPoolMaxSize]. The value cannot be transformed to a number [{1}]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.",
    		  			new Object[]{properties.getProperty("ExecutorPoolMaxSize"), nfException});
      
      throw new InvalidParameterException(messageWithId);
    }
    
    // init the executors thread pool
    CentralExecutor ce = new CentralExecutor(executorPoolMaxSize, percentageOfParallelismAllowed);
    ExecutorFactoryImpl.init(ce);  

    //init properties for decrease of threads
    try {
      timeToLiveIdle = getProperty(properties, "ThreadsIdleTimeout", 5) * 60000;

      if (timeToLiveIdle <= 0) {
    	  String messageWithId = "Negative or zero value [{0}] is specified for ApplicationThreadManager property [ThreadsIdleTimeout]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.";

    	  SimpleLogger.log(Severity.ERROR, category, location,
    			  		  "ASJ.krn_thd.000072",
        				   "Negative or zero value [{0}] is specified for ApplicationThreadManager property [ThreadsIdleTimeout]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.",
        				   new Object[]{timeToLiveIdle});
    	  
        throw new InvalidParameterException(LogHelper.buildMessage(messageWithId, timeToLiveIdle));
      }
    } catch (NumberFormatException nfException) {
      location.catching("init()", nfException);
      String messageWithId = "Incorrect value [{0}] is specified for ApplicationThreadManager property [ThreadsIdleTimeout]. The value cannot be transformed to a number [{1}]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.";
      
      SimpleLogger.log(Severity.ERROR, category, location, 
    		  			"ASJ.krn_thd.000074", 
    		  			"Incorrect value [{0}] is specified for ApplicationThreadManager property [ThreadsIdleTimeout]. The value cannot be transformed to a number [{1}]. ApplicationThreadManager cannot be initialized. Use config tool to correct the property value.",
    		  			new Object[]{properties.getProperty("ThreadsIdleTimeout"), nfException}
      					);
      throw new InvalidParameterException(LogHelper.buildMessage(messageWithId, properties.getProperty("ThreadsIdleTimeout"), nfException));
    }
    
    // init property for memory threshold requiring restart of the node
    // its value should be non-negative
    try{
    	memoryThreshold = getProperty(properties, OOM_RESTART_THRESHOLD_PROPERTY_NAME, OOM_RESTART_THRESHOLD_DEFAULT);
    	
    	if(memoryThreshold < 0){
    		SimpleLogger.log(Severity.ERROR, category, location,
			  		  "ASJ.krn_thd.000101",
  				   "Negative value [{0}] is specified for ApplicationThreadManager property [OOMRestartThreshold]. The default value [{1}] will be used. Use config tool to correct the property value.",
  				   new Object[]{memoryThreshold, OOM_RESTART_THRESHOLD_DEFAULT});
    	}
    	
    	ProcessEnvironment.setMemoryThreshold(memoryThreshold);
    } catch (NumberFormatException nfException){
    	location.catching(nfException);
    	SimpleLogger.log(Severity.ERROR, category, location, 
	  			"ASJ.krn_thd.000102", 
	  			"Incorrect value [{0}] is specified for the ApplicationThreadManager property [OOMRestartThreshold]. The value cannot be transformed to a number [{1}]. The default value [{2}] will be used. Use config tool to correct the property value.",
	  			new Object[]{properties.getProperty(OOM_RESTART_THRESHOLD_PROPERTY_NAME), nfException, OOM_RESTART_THRESHOLD_DEFAULT}
				);
    	ProcessEnvironment.setMemoryThreshold(OOM_RESTART_THRESHOLD_DEFAULT);
    }
    
    try{
    	String strValue = properties.getProperty(OOM_RESTART_MODE_NAME, RESTART_ON_UNHANDLED_MODE);
    	restartMode = ProcessEnvironment.RestartModes.valueOf(strValue);
    }catch(IllegalArgumentException iae){
    	SimpleLogger.log(Severity.ERROR, category, location, 
    				"ASJ.krn_thd.000105", 
    				"Invalid value [{0}] is specified for ApplicationThreadManager property [OOMRestartMode]. The default value [{1}] will be used. Use the config tool to correct the property value.",
    				new Object[]{restartMode, RESTART_ON_UNHANDLED_MODE});
    		restartMode = ProcessEnvironment.RestartModes.RESTART_ON_UNHANDLED;
    }
    	
    ProcessEnvironment.setRestartMode(restartMode);    

    uncaughtExceptionHandler = new UncaughtExceptionHandler();
    Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
    
    location.pathT("ThreadManager started.");
    return true;
  }

//  public void setLimit(int limit) {
//    this.limit = limit <= 0 ? Integer.MAX_VALUE : limit;
//  }

  /**
   * Run-time calling the Manager to change properties
   *
   * @param   properties - Manager Properties to set
   * @return   if changed successful retruns true else reboot is needed
   */
  public boolean setProperties(Properties properties) throws InvalidParameterException {
    //check properties
    if (properties == null || isShutDown) {
      return true;
    }

    int tmpInitialThreadCount = 0, tmpMinThreadCount = 0, tmpMaxThreadCount = 0;

    try {
      tmpInitialThreadCount = getProperty(properties, "InitialThreadCount", initialThreadCount);
      tmpMinThreadCount = getProperty(properties, "MinThreadCount", minThreadCount);
      tmpMaxThreadCount = getProperty(properties, "MaxThreadCount", maxThreadCount);

      switch (checkProperties(tmpInitialThreadCount, tmpMinThreadCount, tmpMaxThreadCount)) {
        case 1:
          {
            SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000038", "Negative values are not allowed for ApplicationThreadManager properties InitialThreadCount [{0}], MinThreadCount [{1}] and MaxThreadCount [{2}]", tmpInitialThreadCount, tmpMinThreadCount, tmpMaxThreadCount);
            throw new InvalidParameterException("Negative values are not allowed for ApplicationThreadManager properties InitialThreadCount ["+tmpInitialThreadCount+"], MinThreadCount ["+tmpMinThreadCount+"] and MaxThreadCount ["+tmpMaxThreadCount+"]");
          }
        case 2:
          {
            SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000039", "Wrong correlation among InitialThreadCount [{0}], MinThreadCount [{1}] and MaxThreadCount [{2}] properties' values of ApplicationThreadManager", tmpInitialThreadCount, tmpMinThreadCount, tmpMaxThreadCount);
            throw new InvalidParameterException("Wrong correlation among InitialThreadCount ["+tmpInitialThreadCount+"], MinThreadCount ["+tmpMinThreadCount+"] and MaxThreadCount ["+tmpMaxThreadCount+"] properties' values of ApplicationThreadManager");
          }
      }

    } catch (NumberFormatException nfException) {
      SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000040", "Not numerical vallues of ApplicationThreadManager properties InitialThreadCount [{0}], MinThreadCount [{1}] and MaxThreadCount [{2}]", tmpInitialThreadCount, tmpMinThreadCount, tmpMaxThreadCount);
      location.catching("changeProperties(Properties)", nfException);
      throw new InvalidParameterException("Not numerical vallues of ApplicationThreadManager properties InitialThreadCount ["+tmpInitialThreadCount+"], MinThreadCount ["+tmpMinThreadCount+"] and MaxThreadCount ["+tmpMaxThreadCount+"]");
    }


    int tmpMaxRQSize = 0;
    try {
      tmpMaxRQSize = getProperty(properties, "MaxRQSize", queue.getLimit());

      if (checkQueueProperties(tmpMaxRQSize) == 1) {
        SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000042", "Negative value [{0}] is specified for ApplicationThreadManager property [MaxRQSize].", tmpMaxRQSize);
        
        throw new InvalidParameterException("Negative value ["+tmpMaxRQSize+"] is specified for ApplicationThreadManager property [MaxRQSize]");
      }
    } catch (NumberFormatException nfException) {
      SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000043", "Incorrect value [{0}] is specified for ApplicationThreadManager property [MaxRQSize]. The value cannot be transformed to a number [{1}].", nfException, tmpMaxRQSize, nfException.toString());
      location.catching("changeProperties(Properties)", nfException);
      throw new InvalidParameterException("Incorrect value ["+tmpMaxRQSize+"] is specified for ApplicationThreadManager property [MaxRQSize]. The value cannot be transformed to a number ["+nfException+"]");
    }


    //init free objects properties
    int initialFOQSize = 0, maxFOQSize = 0;
    try {
      initialFOQSize = getProperty(properties, "InitialFOQSize", 200);
      maxFOQSize = getProperty(properties, "MaxFOQSize", pool.getLimit());

      switch (checkPoolProperties(initialFOQSize, maxFOQSize)) {
        case 1:
          {
            SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000041", "Negative values are not allowed for ApplicationThreadManager properties InitialFOQSize [{0}] and MaxFOQSize [{1}]", initialFOQSize, maxFOQSize);
            throw new InvalidParameterException("Negative values are not allowed for ApplicationThreadManager properties InitialFOQSize ["+initialFOQSize+"] and MaxFOQSize ["+maxFOQSize+"]");
          }
        case 2:
          {
            SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000044", "Wrong correlation between InitialFOQSize [{0}] and MaxFOQSize [{1}] properties' values of ApplicationThreadManager", initialFOQSize, maxFOQSize);
            throw new InvalidParameterException("Wrong correlation between InitialFOQSize ["+initialFOQSize+"] and MaxFOQSize ["+maxFOQSize+"] properties' values of ApplicationThreadManager");
          }
      }

    } catch (NumberFormatException nfException) {
      SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000045", "Not numerical vallues of ApplicationThreadManager properties InitialFOQSize [{0}] and MaxFOQSize [{1}]", initialFOQSize, maxFOQSize);
      location.catching("changeProperties(Properties)", nfException);
      throw new InvalidParameterException("Not numerical vallues of ApplicationThreadManager properties InitialFOQSize ["+initialFOQSize+"] and MaxFOQSize ["+maxFOQSize+"]");
    }


    //set properties for decrease of threads
    int tmpTimeToLiveIdle = 0;
    try {
      tmpTimeToLiveIdle = Integer.parseInt(properties.getProperty("ThreadsIdleTimeout", "5")) * 60000;


      if (tmpTimeToLiveIdle <= 0) {
        SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000046", "Negative or zero value [{0}] is specified for ApplicationThreadManager property [ThreadsIdleTimeout]", tmpTimeToLiveIdle);
        throw new InvalidParameterException("Negative or zero value ["+tmpTimeToLiveIdle+"] is specified for ApplicationThreadManager property [ThreadsIdleTimeout]");
      }

    } catch (NumberFormatException nfException) {
      SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000047", "Incorrect value [{0}] is specified for ThreadManager property [ThreadsIdleTimeout]. The value cannot be transformed to a number [{1}]", nfException, tmpTimeToLiveIdle, nfException.toString());
      location.catching("changeProperties(Properties)", nfException);
      throw new InvalidParameterException("Incorrect value ["+tmpTimeToLiveIdle+"] is specified for ThreadManager property [ThreadsIdleTimeout]. The value cannot be transformed to a number ["+nfException+"]");
    }
    
    //set property for memory threshold for restart on OOM error
    int tmpMemoryThreshold;
    try {
    	tmpMemoryThreshold = getProperty(properties, OOM_RESTART_THRESHOLD_PROPERTY_NAME, memoryThreshold);
    	
    	if(tmpMemoryThreshold < 0){
    		SimpleLogger.log(Severity.ERROR, category, location,
    			   "ASJ.krn_thd.000106",
  				   "Negative value [{0}] is specified for ApplicationThreadManager property [OOMRestartThreshold]. The original value [{1}] will be used. Use config tool to correct the property value.",
  				   new Object[]{tmpMemoryThreshold, memoryThreshold});
    		tmpMemoryThreshold = memoryThreshold;
    	}
    } catch (NumberFormatException nfException) {
    	location.catching(nfException);
    	SimpleLogger.log(Severity.ERROR, category, location, 
	  			"ASJ.krn_thd.000107", 
	  			"Incorrect value [{0}] is specified for the ApplicationThreadManager property [OOMRestartThreshold]. The value cannot be transformed to a number [{1}]. The original value [{2}] will be used. Use config tool to correct the property value.",
	  			new Object[]{properties.getProperty(OOM_RESTART_THRESHOLD_PROPERTY_NAME), nfException, memoryThreshold}
				);
    	tmpMemoryThreshold = memoryThreshold;
    }
    
    if(memoryThreshold != tmpMemoryThreshold){
  	  memoryThreshold = tmpMemoryThreshold;
  	  ProcessEnvironment.setMemoryThreshold(memoryThreshold);
    }
    
    ProcessEnvironment.RestartModes tmpRestartMode;
    try{
    	String strValue = properties.getProperty(OOM_RESTART_MODE_NAME, restartMode.toString());
    	tmpRestartMode = ProcessEnvironment.RestartModes.valueOf(strValue);
    }catch(IllegalArgumentException iae){
    	SimpleLogger.log(Severity.ERROR, category, location, 
				"ASJ.krn_thd.000108", 
				"Invalid value [{0}] is specified for ApplicationThreadManager property [OOMRestartMode]. The original value [{1}] will be used. Use the config tool to correct the property value.",
				new Object[]{properties.getProperty(OOM_RESTART_MODE_NAME), restartMode.toString()});
    	tmpRestartMode = restartMode;
    }
	
	if(restartMode != tmpRestartMode){
	  	  restartMode = tmpRestartMode;
	  	  ProcessEnvironment.setRestartMode(restartMode);
	}
    
    synchronized(this) {
      boolean endResult = true;
      timeToLiveIdle = tmpTimeToLiveIdle;
      if (maxThreadCount != tmpMaxThreadCount) {
        endResult = false;
      }
      //TODO: Tuka triabwa da ima check ako wse pak mogat da se reznat malko chakashti, dali sha stane...

      initialThreadCount = tmpInitialThreadCount;

      //set Runnable Queue Properties
      if (queue.getLimit() != tmpMaxRQSize) {
        if (queue.size() > tmpMaxRQSize) {
          endResult = false;
        } else if (queue.getLimit() > tmpMaxRQSize) {
          queue.setLimit(tmpMaxRQSize);
        }
      }

      if (minThreadCount != tmpMinThreadCount) {
        minThreadCount = tmpMinThreadCount;

        if (this.size() < tmpMinThreadCount) {
          for(int i = this.size(); i < tmpMinThreadCount; i++) {
            SingleThread st = newInstance();
            addLastItem(st);
            //the OutOfMemoryError will be processed from the invoker
            st.start();
          }
        }
      }

      //set Free Objects properties
      if (pool.getLimit() != maxFOQSize) {
        pool.setLimit(maxFOQSize);
      }
      
      return endResult;
    }
  }

  public boolean setProperty(String key, String value) throws IllegalArgumentException {
    properties.put(key, value);
    return true;
  }

  /** @see com.sap.engine.core.Manager#updateProperties(Properties) */
  public void updateProperties(Properties properties) {
    //todo impl
  }

  public String getCurrentProperty(String key) {
    return properties.getProperty(key);
  }

  /**
   * Return names and current values of Thread Manager properties
   *
   * @return All properties of Thread Manager
   */
  public Properties getCurrentProperties() {
    return (Properties)properties.clone();
  }

  /**
   * The Framework invokes it when all Managers are initialized.
   * Second pass over all registered Managers. Thread Manager tries
   * to get references to Memory Manager and Timeout Manager.
   *
   */
  public void loadAdditional() {
    // nothing to do...
  }

  /**
   * The Framework invokes it to check the status of the manager. Returned
   * value is used from AI System.
   *
   * @return   status of the manager, which is number between 0 and 100.
   *           This is degree value of the burdering of manager.
   */
  public byte getStatus() {
    return (byte) (((this.size() - queue.getWaitingDequeueThreadsCount()) * 100) / this.getLimit());
  }

  public void startCleanThread(Runnable thread, boolean instantly) {
    if (thread == null) {
      //throw runtime exception
      SimpleLogger.trace(Severity.ERROR, location, "ASJ.krn_thd.000006", "Cannot start clean thread. The Runnable argument is NULL!");
      throw new NullPointerException("Cannot start clean thread. The Runnable argument is NULL!");
    }
    Task task = pool.getTask();
    task.init(thread, null, null, "IMPL3");
    this.startCleanThread(task, instantly);
  }

  public void startCleanThread(Task task, boolean instantly) {
    if (task == null) {
      //throw runtime exception
      SimpleLogger.trace(Severity.ERROR, location, "ASJ.krn_thd.000022", "Cannot start clean thread. The Task argument is NULL!");
      throw new NullPointerException("Cannot start clean thread. The Task argument is NULL!");
    }

    //TODO: Pyrwo waitQueue-to triabwa da tragne, posle sha startirame thread-owe.
    ContextData data = task.getContextData();
    if (data == null) {
    	task.setContextData(data = new ContextDataImpl());
    }
    data.inheritFromCurrentThread(true); // inheritance of ContextObjects must be skipped 

    internalLaunch(task, instantly);
  }

	private void internalLaunch(Task task, boolean instantly) {
		if (!suitableForEnqueue(task)) { //IF true - it's already enqueued
      if (instantly) {
        immediateStart(task);
      } else {
        if (!canResize(task)) { //If true - already resized
          queue.enqueue(task);
        }
      }
    }
	}

  /**
   * Adds a Runnable object to queue
   *
   * @param   thread  Runnable object to be added
   * @exception NullPointerException if <code>work</code> is NULL
   */
  public void startThread(Runnable thread) throws NullPointerException {
    startThread(thread, false);
  }

  /**
   * Adds a Runnable object to queue or start it immediately
   *
   * @param   thread  Runnable object to be added
   * @param   instantly Execute the thread immediately, or put the action in the queue.
   * @exception NullPointerException if <code>work</code> is NULL
   */
  public void startThread(Runnable thread, boolean instantly) {
    this.startThread(thread, null, null, instantly);
  }

  public void startThread(Runnable thread, String taskName, String threadName) {
    startThread(thread, taskName, threadName, false);
  }

  public void startThread(Task task, String taskName, String threadName, long timeout) {
    if (task == null) {
      //throw runtime exception
      SimpleLogger.trace(Severity.ERROR, location, "ASJ.krn_thd.000007", "Cannot start thread. The Task argument is NULL!");
      throw new NullPointerException("Cannot start thread. The Task argument is NULL!");
    }
    ContextData data = task.getContextData();
    if (data == null) {
    	task.setContextData(data = new ContextDataImpl());
    }
    task.getThreadInfo().setTaskName(taskName);
    task.getThreadInfo().setThreadName(threadName);

    data.inheritFromCurrentThread(false); //inheritance must happen!
    
    try {
    	if (!canResize(task)) {
        if (!queue.enqueue(task, timeout)) {
          immediateStart(task);
        }
    	} 
    } catch (InterruptedException e) {
      if (!this.isShutDown) {
        location.traceThrowableT(Severity.DEBUG, "Thread interrupted while waiting for new task to process. Probably the system is shutting down.", e);
      }
    }
  }

  /**
   * Adds a Runnable object to queue or start it immediately
   *
   * @param   thread  Runnable object to be added
   * @param   instantly Execute the thread immediately, or put the action in the queue.
   * @exception NullPointerException if <code>work</code> is NULL
   */
  public void startThread(Runnable thread, String taskName, String threadName, boolean instantly) {
    if (thread == null) {
      //throw runtime exception
      SimpleLogger.trace(Severity.ERROR, location, "ASJ.krn_thd.000023", "Cannot start thread. The Runnable argument is NULL!");
      throw new NullPointerException("Cannot start thread. The Runnable argument is NULL!");
    }
    Task task = pool.getTask();
    task.init(thread, taskName, threadName, "Application");
    task.getThreadInfo().setTaskName(taskName);
    task.getThreadInfo().setThreadName(threadName);
    
    this.startThread(task, instantly);
  }

  /**
   * Adds a Runnable object to queue
   *
   * @param   task  Runnable object to be added
   * @param   instantly Execute the thread immediately, or put the action in the queue.
   * @exception NullPointerException if <code>work</code> is NULL
   */
  public void startThread(Task task, boolean instantly) {
    if (task == null) {
      //throw runtime exception
      SimpleLogger.trace(Severity.ERROR, location, "ASJ.krn_thd.000007", "Cannot start thread. The Task argument is NULL!");
      throw new NullPointerException("Cannot start thread. The Task argument is NULL!");
    }

    //TODO: Pyrwo waitQueue-to triabwa da tragne, posle sha startirame thread-owe.
    ContextData data = task.getContextData();
    if (data == null) {
    	task.setContextData(data = new ContextDataImpl());
    }
    data.inheritFromCurrentThread(false); // inheritance must happen
    
    internalLaunch(task, instantly);
  }

  public void startThread(Task task, String taskName, String threadName, boolean instantly) {
    if (task == null) {
      //throw runtime exception
      SimpleLogger.trace(Severity.ERROR, location, "ASJ.krn_thd.000007", "Cannot start thread. The Task argument is NULL!");
      throw new NullPointerException("Cannot start thread. The Task argument is NULL!");
    }

    //TODO: Pyrwo waitQueue-to triabwa da tragne, posle sha startirame thread-owe.
    ContextData data = task.getContextData();
    if (data == null) {
    	task.setContextData(data = new ContextDataImpl());
    }
    task.getThreadInfo().setTaskName(taskName);
    task.getThreadInfo().setThreadName(threadName);
    
    this.startThread(task, instantly);
  }

  private synchronized boolean canResize(Task task) {
    if (this.size() < limit) {
      immediateStart(task);
      return true;
    } else {
      return false;
    }
  }

  private boolean suitableForEnqueue(Task task) {
    synchronized(queue) {
      if (queue.getWaitingDequeueThreadsCount() > queue.size()) {
        queue.enqueue(task);
        return true;
      } else {
        return false;
      }
    }
  }

  public synchronized int size() {
    return super.size();
  }

  public void startThread(Task task, long timeout) {
    startThread(task, null, null, timeout);
  }


  private void immediateStart(Task task) {
		SingleThread sh = newInstance();
		boolean rezult;
		synchronized(this) {
		  rezult = addLastItem(sh);
		}
		sh.setRunnable(task, rezult);
		try {
		  sh.start();
		} catch (OutOfMemoryError o) {
		    // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
		    // Please do not remove this comment !
		  ProcessEnvironment.handleOOM(o);
		}
  }

 //To be used only by the Executor Thread.
  protected void passiveImmediateStart(Task task) {
    if (!suitableForEnqueue(task)) {
      SingleThread sh;
      boolean rezult;
      synchronized(this) {
        sh = newInstance();
        rezult = addLastItem(sh);
      }
      sh.setRunnable(task, rezult);
      try {
        sh.start();
      } catch (OutOfMemoryError o) {
          // Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
          // Please do not remove this comment !
        ProcessEnvironment.handleOOM(o);
      }
    }
  }



  /**
   *
   */
  private int getProperty(Properties properties, String name, int defaultValue) throws NumberFormatException {
    String tmp = properties.getProperty(name);

    if (tmp != null) {
      tmp = tmp.trim();
      return Integer.parseInt(tmp);
    } else {
      return defaultValue;
    }
  }

  private byte checkProperties(int initial, int min, int max) {
    if ((min < 0) || (max < 0) || (initial < 0)) {
      return 1;
    }

    if ((initial < min) || (initial > max)) {
      return 2;
    }

    return 0;
  }

  private byte checkPoolProperties(int initial, int max) {
    if ((max < 0) || (initial < 0)) {
      return 1;
    }

    if (initial > max) {
      return 2;
    }

    return 0;
  }

  private byte checkQueueProperties(int max) {
    if (max < 0) {
      return 1;
    }
    
    if (max == 0) {
    	return 2;
    }

    return 0;
  }


//}} PRIVATE METHODS

  /**
   * Get debug information about the manager's current state.
   *
   * @param   flag can be used to determines which parts of the info to be returned.
   *
   * @return  a String object containing the debug info. A return value of <code>null</code>
   *          means that this manager does not provide debug information.
   */
  public String getDebugInfo(int flag) {
    StringBuffer sb = new StringBuffer();
    sb.append("\n-----------------------------------------");
    sb.append("\n  Debug Info for: " + THREADS_NAME_HEADER + "ThreadManager");
    sb.append("\n-----------------------------------------");
    sb.append("\n              flag: " + flag);
    sb.append("\n   Instances Count: " + threadStartCounter);
    sb.append("\n-----------------------------------------");
    sb.append("\n           MinSize: " + this.minThreadCount);
    sb.append("\n       CurrentSize: " + super.size());
    sb.append("\n           MaxSize: " + limit);
    sb.append("\n         queueSize: " + queue.size());
    sb.append("\n      dequeueCount: " + queue.getWaitingDequeueThreadsCount());
    sb.append("\n      enqueueCount: " + queue.getWaitingEnqueueThreadsCount());
    sb.append("\n-----------------------------------------");
    return sb.toString();
  }

  /**
   * It starts the <source> Runnable <source> object into a new thread from the
   * internal thread pool. If there is no free thread the object is add to a
   * waiting queue. You can't rely on priority, internal system can ignore it in
   * some situations. The thread context of the parent thread is copied to the
   * new thread using child method of Context objects. This is done by parent
   * thread.
   *
   * @param   thread - runnable object that is going to be executed
   * @param   system - indicate the type of thread that has to be used for
   * execution of the runnable object
   * @param   priority - this is the priority with which the thread has to be
   * executed. The interval of values for this parameter is the as for
   * java.lang.Thread priority
   *
   */

  /**
   * Get thread context of the current thread.
   *
   */
  public ThreadContext getThreadContext() {
    return ThreadContextImpl.getThreadContext();
  }

  /**
   * Register ContextObject, for the ApplicationThreads.
   * @param name Name of the Context Object
   * @param object Context Object to be registered
   * @return ID of the newly registered context object
   */
  public int registerContextObject(String name, ContextObject object) {
    return rootThreadContext.registerContextObject(name, object);
  }

  public int getContextObjectId(String name) {
    return rootThreadContext.getContextObjectId(name);
  }

  public void unregisterContextObject(String name) {
    rootThreadContext.unregisterContextObject(name);
  }

  /**
   * Retrieves the ManagementInterface implementation for the manager
   *
   * @return  an object that is used for runtime monitoring and management of this manager.
   */
  public ManagementInterface getManagementInterface() {
    return tManagement;
  }

  public SingleThread newInstance() {
    SingleThread sh = new SingleThread(this, queue, THREADS_NAME_HEADER + " [" + (threadStartCounter.incrementAndGet()) + "]");
    return sh;
  }

  protected synchronized boolean autoKillThread(SingleThread singleThread) {
    if (this.size() > minThreadCount) {
      this.removeItem(singleThread);
      singleThread.stopThread();
      return true;
    } else {
      return false;
    }
  }
  
  // add synchronization to removeItem()
  public synchronized NextItem removeItem(NextItem item) {
    return super.removeItem(item);
  }

 	private void writeObject(ObjectOutputStream oos) throws NotSerializableException {
	  throw new NotSerializableException(this.getClass().getName());
	}

	
	private void readObject(ObjectInputStream oos) throws NotSerializableException {
	  throw new NotSerializableException(this.getClass().getName());
	}
	
	public Object clone() {
	  return null;
	}

}
