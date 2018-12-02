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
package com.sap.engine.core.thread.impl5;

import com.sap.engine.core.Names;
import com.sap.engine.core.thread.ContextDataImpl;
import com.sap.engine.core.thread.LogHelper;
import com.sap.engine.core.thread.ShmThreadLoggerImpl;
import com.sap.engine.core.thread.ThreadContextImpl;
import com.sap.engine.core.thread.ThreadManagementInterface;
import com.sap.engine.core.thread.ThreadManager;
import com.sap.engine.frame.ProcessEnvironment;
import com.sap.engine.frame.core.thread.ContextData;
import com.sap.engine.frame.core.thread.ContextObject;
import com.sap.engine.frame.core.thread.ThreadContext;
import com.sap.engine.frame.core.thread.Task;
import com.sap.engine.frame.state.ManagementInterface;
import com.sap.engine.lib.util.WaitQueue;
import com.sap.engine.lib.util.NotSupportedException;
import com.sap.engine.lib.util.base.BaseQueue;
import com.sap.engine.lib.util.base.NextItem;
import com.sap.engine.lib.util.iterators.RootIterator;
import com.sap.engine.system.ShmThreadImpl;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
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

  protected final boolean isSystem = true;

  //info for Runnable objects
  WaitQueue queue;
  ActionObjectPool pool;

  //thread properties
  protected int initialThreadCount;
  protected int minThreadCount;
  protected int maxThreadCount;
  protected int timeToLiveIdle;


  //framework-invokation indicators
  volatile protected boolean isShutDown = false;
  private Properties properties;
  protected static final String THREADS_NAME_HEADER = "System";
  protected static ThreadGroup threadGroup = new ThreadGroup(THREADS_NAME_HEADER + "ThreadGroup");
  protected static AtomicInteger threadStartCounter = new AtomicInteger(0);
  protected static int additioanlThreadStartCounter = 0;

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

    synchronized (this) {
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
      Thread.sleep(400);
    } catch (InterruptedException e) {
      location.catching(e);
    }
    
    iter = this.elementsIterator();
    if (!iter.isAtEnd()) {
      
      if (location.bePath()) {
        location.pathT("Unexpected thread activity in shutdown of " + THREADS_NAME_HEADER + "ThreadManager. Full thread dump will be made");
        ProcessEnvironment.getThreadDump("Unexpected thread activity in shutdown of ThreadManager. Full thread dump will be made");
      }
      
      while (!iter.isAtEnd()) {
        th = (SingleThread) iter.next();
        if (th.getTaskDetails() != null) {
          location.infoT("Unexpected thread activity after interrupt() is executed in shutdown of " + THREADS_NAME_HEADER + "ThreadManager:\nThread[" + th.getName() + "]\nTask: " + th.getTaskDetails());
        }
      }
    }
    
    //    SingleThread.security = null;
    location.pathT("ThreadManager stopped.");
  }
  
  /**
   * Initialazing Manager with specified properties
   *
   * @param   properties  Manager Properties
   * @return   if initialization of the Manager is successful returns true
   * @exception InvalidParameterException if there are incorrect parameter
   */
  public boolean init(Properties properties) throws InvalidParameterException {
     //  initialize the logger for ShmThreads
     ShmThreadImpl.setLoggerToUse(new ShmThreadLoggerImpl());
     
    //check properties for NULL value
    if (properties == null) {
      properties = new Properties();
    }
    this.properties = properties;


    int initialSize = 0, maxSize = 0;
    //init free objects properties
    try {
      initialSize = getProperty(properties, "InitialFOQSize", 200);
      maxSize = getProperty(properties, "MaxFOQSize", 5000);

      switch (checkPoolProperties(initialSize, maxSize)) {
        case 1:
          {
            SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000048", "Negative values are not allowed for ThreadManager properties InitialFOQSize [{0}] and MaxFOQSize [{1}]", initialSize, maxSize);
            throw new InvalidParameterException("Negative values are not allowed for ThreadManager properties InitialFOQSize ["+initialSize+"] and MaxFOQSize ["+maxSize+"]");
          }
        case 2:
          {
            SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000049", "Wrong correlation between InitialFOQSize [{0}] and MaxFOQSize [{1}] properties' values of ThreadManager", initialSize, maxSize);
            throw new InvalidParameterException("Wrong correlation between InitialFOQSize ["+initialSize+"] and MaxFOQSize ["+maxSize+"] properties' values of ThreadManager");
          }
      }

      //initalize pool with free objects
      pool = new ActionObjectPool(initialSize, maxSize);
    } catch (NumberFormatException nfException) {
      SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000050", "Not numerical vallues of ThreadManager properties InitialFOQSize [{0}] and MaxFOQSize [{1}]", initialSize, maxSize);
      location.catching("init()", nfException);
      throw new InvalidParameterException("Not numerical vallues of ThreadManager properties InitialFOQSize ["+initialSize+"] and MaxFOQSize ["+maxSize+"]");
    }

    //init runnable queue properties
    try {
      maxSize = getProperty(properties, "MaxRQSize", 5000);

      if (checkQueueProperties(maxSize) == 1) {
      	String messageWithId = "Negative value [{0}] is specified for ThreadManager property [MaxRQSize]. ThreadManager cannot be initialized. Use config tool to correct the property value.";
      	SimpleLogger.log(Severity.ERROR, category, location, 
        				"ASJ.krn_thd.000075",
        				"Negative value [{0}] is specified for ThreadManager property [MaxRQSize]. ThreadManager cannot be initialized. Use config tool to correct the property value.",
        				new Object[]{maxSize});
        throw new InvalidParameterException(LogHelper.buildMessage(messageWithId, maxSize));
      }
      //initialize queue with Runnable objects
      queue = new WaitQueue(maxSize);

    } catch (NumberFormatException nfException) {
    	location.catching(nfException);
    	
    	String messageWithId = "Incorrect value [{0}] is specified for ThreadManager property [MaxRQSize]. The value cannot be transformed to a number [{1}]. ThreadManager cannot be initialized. Use config tool to correct the property value.";
    	SimpleLogger.log(Severity.ERROR, category, location, 
    		  			"ASJ.krn_thd.000076",
    		  			"Incorrect value [{0}] is specified for ThreadManager property [MaxRQSize]. The value cannot be transformed to a number [{1}]. ThreadManager cannot be initialized. Use config tool to correct the property value.",
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
	        	
	        	String messageWithId = "Negative value [{0}] is specified for ThreadManager property [{1}]. ThreadManager cannot be initialized. Use config tool to correct the property value.";
	        	SimpleLogger.log(Severity.ERROR, category, location, 
	        		  			"ASJ.krn_thd.000077",
	        		  			"Negative value [{0}] is specified for ThreadManager property [{1}]. ThreadManager cannot be initialized. Use config tool to correct the property value.",
	        		  			new Object[]{value, name});
	          
	          throw new InvalidParameterException(LogHelper.buildMessage(messageWithId, value, name));
          }
        case 2:
          {
	        	String reason = null;
	      	  if (initialThreadCount < minThreadCount) {
	      		  	SimpleLogger.log(Severity.ERROR, category, location,
	      				  			"ASJ.krn_thd.000091",
	      				  			"Incorrect value [{0}] is specified for ThreadManager property [InitialThreadCount]. The value [{1}] is smaller than the value[{2}] of property [MinThreadCount]. ThreadManager cannot be initialized. Use config tool to correct the property value.",
	      				  			initialThreadCount, initialThreadCount, minThreadCount);
	      		  
	      		  	reason = "Incorrect value ["+initialThreadCount+"] is specified for ThreadManager property [InitialThreadCount]. The value ["+initialThreadCount+"] is smaller than the value["+minThreadCount+"] of property [MinThreadCount].";
	      	  } else {
	      	  		SimpleLogger.log(Severity.ERROR, category, location, 
	      	  					 	"ASJ.krn_thd.000092", 
	      	  					 	"Incorrect value [{0}] is specified for ThreadManager property [InitialThreadCount]. The value [{1}] is bigger than the value[{2}] of property [MaxThreadCount].",
	      	  					 	initialThreadCount, initialThreadCount, maxThreadCount);
	      	  		
	      	  		reason = "Incorrect value ["+initialThreadCount+"] is specified for ThreadManager property [InitialThreadCount]. The value ["+initialThreadCount+"] is bigger than the value["+maxThreadCount+"] of property [MaxThreadCount].";

	      	  }
	      	  
	      	  String messageWithId = reason + " ThreadManager cannot be initialized. Use config tool to correct the property value.";
	          throw new InvalidParameterException(messageWithId);
          }
      }

      this.setLimit(maxThreadCount);
    } catch (NumberFormatException nfException) {
    	location.catching("init()", nfException);
    	String messageWithId = "Incorrect value is specified for one of the following properties of ThreadManager: [InitialThreadCount][{0}], [MinThreadCount][{1}], [MaxThreadCount][{2}]. The value cannot be transformed to a number [{3}]. ThreadManager cannot be initialized. Use config tool to correct the property value.";
    	SimpleLogger.log(Severity.ERROR, category, location,
    		  			"ASJ.krn_thd.000078",
    		  			"Incorrect value is specified for one of the following properties of ThreadManager: [InitialThreadCount][{0}], [MinThreadCount][{1}], [MaxThreadCount][{2}]. The value cannot be transformed to a number [{3}]. ThreadManager cannot be initialized. Use config tool to correct the property value.",
    		  			new Object[]{properties.getProperty("InitialThreadCount"), properties.getProperty("MinThreadCount"), properties.getProperty("MaxThreadCount"), nfException});
      
      throw new InvalidParameterException(LogHelper.buildMessage(messageWithId,
    		  							properties.getProperty("InitialThreadCount"), properties.getProperty("MinThreadCount"), properties.getProperty("MaxThreadCount"), nfException));
    }

    //init properties for decrease of threads
    try {
      timeToLiveIdle = getProperty(properties, "ThreadsIdleTimeout", 5) * 60000;

      if (timeToLiveIdle <= 0) {
      	String messageWithId = "Negative or zero value [{0}] is specified for ThreadManager property [ThreadsIdleTimeout]. ThreadManager cannot be initialized. Use config tool to correct the property value.";
      	SimpleLogger.log(Severity.ERROR, category, location,
      					 "ASJ.krn_thd.000079",
      					 "Negative or zero value [{0}] is specified for ThreadManager property [ThreadsIdleTimeout]. ThreadManager cannot be initialized. Use config tool to correct the property value.",
      					 new Object[]{timeToLiveIdle});
      	
        throw new InvalidParameterException(LogHelper.buildMessage(messageWithId, timeToLiveIdle));
      }
    } catch (NumberFormatException nfException) {
    	location.catching("init()", nfException);
    	String messageWithId = "Incorrect value [{0}] is specified for ThreadManager property [ThreadsIdleTimeout]. The value cannot be transformed to a number [{1}]. ThreadManager cannot be initialized. Use config tool to correct the property value.";
    	SimpleLogger.log(Severity.ERROR, category, location,
    					"ASJ.krn_thd.000080",
    					"Incorrect value [{0}] is specified for ThreadManager property [ThreadsIdleTimeout]. The value cannot be transformed to a number [{1}]. ThreadManager cannot be initialized. Use config tool to correct the property value.",
    					new Object[]{properties.getProperty("ThreadsIdleTimeout"), nfException});
    	throw new InvalidParameterException(LogHelper.buildMessage(messageWithId, properties.getProperty("ThreadsIdleTimeout"), nfException));
    }

    for (int i = 0; i < initialThreadCount; i++) {
      SingleThread th = newInstance();
        addLastItem(th);
      //the OutOfMemoryError will be processed from the invoker
      th.start();
    }

    //init Maintaining thread
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
            SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000051", "Negative values are not allowed for ThreadManager properties InitialThreadCount [{0}], MinThreadCount [{1}] and MaxThreadCount [{2}]", tmpInitialThreadCount, tmpMinThreadCount, tmpMaxThreadCount);
            throw new InvalidParameterException("Negative values are not allowed for ThreadManager properties InitialThreadCount ["+tmpInitialThreadCount+"], MinThreadCount ["+tmpMinThreadCount+"] and MaxThreadCount ["+tmpMaxThreadCount+"]");
          }
        case 2:
          {
            SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000052", "Wrong correlation among InitialThreadCount [{0}], MinThreadCount [{1}] and MaxThreadCount [{2}] properties' values of ThreadManager", tmpInitialThreadCount, tmpMinThreadCount, tmpMaxThreadCount);
            throw new InvalidParameterException("Wrong correlation among InitialThreadCount ["+tmpInitialThreadCount+"], MinThreadCount ["+tmpMinThreadCount+"] and MaxThreadCount ["+tmpMaxThreadCount+"] properties' values of ThreadManager");
          }
      }

    } catch (NumberFormatException nfException) {
      SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000053", "Not numerical vallues of ThreadManager properties InitialThreadCount [{0}], MinThreadCount [{1}] and MaxThreadCount [{2}]", tmpInitialThreadCount, tmpMinThreadCount, tmpMaxThreadCount);
      location.catching("changeProperties(Properties)", nfException);
      throw new InvalidParameterException("Not numerical vallues of ThreadManager properties InitialThreadCount ["+tmpInitialThreadCount+"], MinThreadCount ["+tmpMinThreadCount+"] and MaxThreadCount ["+tmpMaxThreadCount+"]");
    }


    int tmpMaxRQSize = 0;
    try {
      tmpMaxRQSize = getProperty(properties, "MaxRQSize", queue.getLimit());

      if (checkQueueProperties(tmpMaxRQSize) == 1) {
        SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000054", "Negative value [{0}] is specified for ThreadManager property [MaxRQSize].", tmpMaxRQSize);
        throw new InvalidParameterException("Negative value ["+tmpMaxRQSize+"] is specified for ThreadManager property [MaxRQSize].");
      }
    } catch (NumberFormatException nfException) {
      SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000055", "Incorrect value [{0}] is specified for ThreadManager property [MaxRQSize]. The value cannot be transformed to a number [{1}].", tmpMaxRQSize);
      location.catching("changeProperties(Properties)", nfException);
      throw new InvalidParameterException("Incorrect value ["+tmpMaxRQSize+"] is specified for ThreadManager property [MaxRQSize]. The value cannot be transformed to a number [{1}].");
    }


    //init free objects properties
    int initialFOQSize = 0, maxFOQSize = 0;
    try {
      initialFOQSize = getProperty(properties, "InitialFOQSize", 200);
      maxFOQSize = getProperty(properties, "MaxFOQSize", pool.getLimit());

      switch (checkPoolProperties(initialFOQSize, maxFOQSize)) {
        case 1:
          {
            SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000056", "Negative values are not allowed for ThreadManager properties InitialFOQSize [{0}] and MaxFOQSize [{1}]", initialFOQSize, maxFOQSize);
            throw new InvalidParameterException("Negative values are not allowed for ThreadManager properties InitialFOQSize ["+initialFOQSize+"] and MaxFOQSize ["+maxFOQSize+"]");
          }
        case 2:
          {
            SimpleLogger.log(Severity.WARNING, category, location, 
            				"ASJ.krn_thd.000057", 
            				"Wrong correlation between InitialFOQSize [{0}] and MaxFOQSize [{1}] properties' values of ThreadManager", initialFOQSize, maxFOQSize);
            throw new InvalidParameterException("Wrong correlation between InitialFOQSize ["+initialFOQSize+"] and MaxFOQSize ["+maxFOQSize+"] properties' values of ThreadManager");
          }
      }

    } catch (NumberFormatException nfException) {
      SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000058", "Not numerical vallues of ThreadManager properties InitialFOQSize [{0}] and MaxFOQSize [{1}]", initialFOQSize, maxFOQSize);
      location.catching("changeProperties(Properties)", nfException);
      throw new InvalidParameterException("Not numerical vallues of ThreadManager properties InitialFOQSize ["+initialFOQSize+"] and MaxFOQSize ["+maxFOQSize+"]");
    }


    //set properties for decrease of threads
    int tmpTimeToLiveIdle = 0;
    try {
      tmpTimeToLiveIdle = Integer.parseInt(properties.getProperty("ThreadsIdleTimeout", "5")) * 60000;

      if (tmpTimeToLiveIdle <= 0) {
        SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000059", "Negative or zero value [{0}] is specified for ThreadManager property [ThreadsIdleTimeout]", tmpTimeToLiveIdle);
        throw new InvalidParameterException("Negative or zero value ["+tmpTimeToLiveIdle+"] is specified for ThreadManager property [ThreadsIdleTimeout]");
      }

    } catch (NumberFormatException nfException) {
      SimpleLogger.log(Severity.WARNING, category, location, "ASJ.krn_thd.000060", "Incorrect value [{0}] is specified for ThreadManager property [ThreadsIdleTimeout]. The value cannot be transformed to a number [{1}]", tmpTimeToLiveIdle);
      location.catching("changeProperties(Properties)", nfException);
      throw new InvalidParameterException("Incorrect value [{0}] is specified for ThreadManager property [ThreadsIdleTimeout]. The value cannot be transformed to a number ["+tmpTimeToLiveIdle+"]");
    }
    synchronized (this) {
      boolean endResult = true;
      timeToLiveIdle = tmpTimeToLiveIdle;
      if (maxThreadCount != tmpMaxThreadCount) {
        endResult = false;
      }
      //TODO: Tuka triabwa da ima check ako wse pak mogat da se reznat malko chakashti, dali sha stane...

      initialThreadCount = tmpInitialThreadCount;

      if (minThreadCount != tmpMinThreadCount) {
        minThreadCount = tmpMinThreadCount;

        if (this.size() < tmpMinThreadCount) {
          for (int i = this.size(); i < tmpMinThreadCount; i++) {
            SingleThread st = newInstance();
            synchronized (this) {
              addLastItem(st);
            }
            //the OutOfMemoryError will be processed from the invoker
            st.start();
          }
        }
      }

      //set Runnable Queue Properties
      if (queue.getLimit() != tmpMaxRQSize) {
        if (queue.size() > tmpMaxRQSize) {
          endResult = false;
        } else if (queue.getLimit() > tmpMaxRQSize) {
          queue.setLimit(tmpMaxRQSize);
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
    return (Properties) properties.clone();
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

  public void startThread(Task task, long timeout) {
    throw new NotSupportedException();
  }

  public void startCleanThread(Runnable thread, boolean instantly) {
    startThread(thread, instantly);
  }

  public void startCleanThread(Task task, boolean instantly) {
    startThread(task, instantly);
  }

  /**
   * Adds a Runnable object to queue
   *
   * @param   thread  Runnable object to be added
   * @exception NullPointerException if <code>work</code> is NULL
   */
  public void startThread(Runnable thread) {
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
    startThread(thread, null, null, instantly);
  }

  public void startThread(Runnable thread, String taskName, String threadName) {
    startThread(thread, taskName, threadName, false);
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
      SimpleLogger.trace(Severity.ERROR, location, "ASJ.krn_thd.000093", "Cannot start thread. The Runnable argument is NULL!");
      throw new NullPointerException("Cannot start thread. The Runnable argument is NULL!");
    }
    Task task = pool.getTask();
    task.init(thread, taskName, threadName, "System");
    this.startThread(task, taskName, threadName, instantly);
  }

  public void startThread(Task task, String taskName, String threadName, boolean instantly) {
    task.getThreadInfo().setTaskName(taskName);
    task.getThreadInfo().setThreadName(threadName);
    startThread(task, instantly);
  }

  public void startThread(Task task, String taskName, String threadName, long timeout) {
    throw new NotSupportedException();
  }

  public void startThread(Task task, boolean instantly) {
  	ContextData data = task.getContextData();
    if (data == null) {
    	task.setContextData(data = new ContextDataImpl());
    }
    data.inheritFromCurrentThread(true); //no ContextObjects should be inherited here - so skip them! 
    
	  if (!suitableForEnqueue(task)) { //IF true - it's already enqueued
	    // no free threads at the moment
	    if (instantly) {
	      immediateStart(task);
	    } else {
	      if (!canResize(task)) { //If true - already resized
	        queue.enqueue(task);
	      }
	    }
	  }
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

  private void immediateStart(Task task) {
	  SingleThread sh = newInstance();
	  boolean rezult;
	  synchronized (this) {
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

  /**
   *
   */
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
    sb.append("\n           MaxSize: " + super.limit);
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
    throw new NotSupportedException();
  }

  public int getContextObjectId(String name) {
    throw new NotSupportedException();
  }

  public void unregisterContextObject(String name) {
    throw new NotSupportedException();
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

  //add synchronization to removeItem()
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
	  return null; //because of the LinkedList extension. 
	}
	
}

