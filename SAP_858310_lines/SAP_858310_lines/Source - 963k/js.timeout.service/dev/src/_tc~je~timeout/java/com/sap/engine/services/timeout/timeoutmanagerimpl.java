/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * of SAP AG, Walldorf.. You shall not disclose such Confidential
 * This software is the confidential and proprietary information
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.timeout;

import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import com.sap.engine.frame.core.thread.ThreadSystem;
import com.sap.engine.frame.state.ManagementListener;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

/**
 * A facility for threads to schedule tasks for future execution in a background thread.
 * Tasks may be registrated for one-time execution, or for repeated execution at regular intervals.
 *
 * Corresponding to each TimeoutManager object is a single background thread that is used to execute
 * all of the TimeoutManager's tasks.
 *
 * @author Georgi Stanev, Jasen Minov, Hristo Iliev, Dimitar Kostadinov
 * @version 6.30 July 2002
 */
public final class TimeoutManagerImpl implements TimeoutManager, TimeoutManagementInterface {

  /**
   * Category used for logging
   */
  public static final Category category = Category.SYS_SERVER;

  /**
   * Location used for logging
   */
  private static final Location location = Location.getLocation(TimeoutManagerImpl.class);

  private static final String PARALLEL_THREAD_NAME = "Timeout Service Parallel Queue Processor";
  private static final String SYNCHRONOUS_THREAD_NAME = "Timeout Service Synchronous Queue Processor";
  // statistics
  private static final double FACTOR = 1000.0 * 60.0;
  private int registeredListenersCount;
  private double estimatedFrequencyPerMinute;

  // thread system
  private ThreadSystem threadSystem;
  // holds TimeoutListener to TimeoutNode mapping
  private HashMap<TimeoutListener, TimeoutNode> map;
  // holds TimeoutListener to synchronous TimeoutNode mapping
  private HashMap<TimeoutListener, TimeoutNode> synchronousMap;
  // holds TimeoutNodes sorted according their nextCallTime
  private PriorityQueue queue;
  // holds synchronous TimeoutNodes sorted according their nextCallTime
  private PriorityQueue synchronousQueue;

  // timeout manager work thread
  private Thread internalThread;
  // synchronous timeout manager work thread
  private Thread synchronousInternalThread;

  // integrity watcher instance
  private TimeoutIntegrityWatcher integrityWatcher;
  // synchronous integrity watcher instance
  private TimeoutIntegrityWatcher synchronousIntegrityWatcher;

  // timeout manager work thread priority
  private int threadPriority;
  private boolean work;
  private boolean synchronousWork;

  // operation locks
  private final Object lock;
  private final Object synchronousLock;
  private final Object statisticsLock;

  // constructor
  TimeoutManagerImpl(Properties properties, ThreadSystem ts) {
    lock = new Object();
    synchronousLock = new Object();
    statisticsLock = new Object();
    if (properties == null) {
      properties = new Properties();
    }
    threadSystem = ts;
    map = new HashMap<TimeoutListener, TimeoutNode>();
    synchronousMap = new HashMap<TimeoutListener, TimeoutNode>();
    queue = new PriorityQueue();
    synchronousQueue = new PriorityQueue();
    threadPriority = readIntProperty(properties, "InternalThreadPriority", Thread.NORM_PRIORITY);
    if (threadPriority < Thread.MIN_PRIORITY || threadPriority > Thread.MAX_PRIORITY) {
      SimpleLogger.log(Severity.WARNING, category, location, "ASJ.timeout.000002",
          "Value of [InternalThreadPriority] is not in range; default value [{0}] will be used",
          Thread.NORM_PRIORITY);
      threadPriority = Thread.NORM_PRIORITY;
    }
    integrityWatcher = new TimeoutIntegrityWatcher(queue, threadSystem);
    synchronousIntegrityWatcher = new TimeoutIntegrityWatcher(synchronousQueue, threadSystem);
    work = true;
    synchronousWork = true;
    // start timeout runners
    new TimeoutManagerRunner(this, false, threadSystem, PARALLEL_THREAD_NAME);
    new TimeoutManagerRunner(this, true, threadSystem, SYNCHRONOUS_THREAD_NAME);
    SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000020",
        "TimeoutManagerImpl initialized");
  }

  private static int readIntProperty(Properties properties, final String name, final int defaultValue) {
    try {
      return Integer.parseInt(properties.getProperty(name, Integer.toString(defaultValue)));
    } catch (NumberFormatException nfException) {
      //$JL-EXC$
      SimpleLogger.log(Severity.WARNING, category, location, "ASJ.timeout.000003",
          "Value of [{0}] is not valid; default value [{1}] will be used",
          name, defaultValue);
      return defaultValue;
    }
  }

  // stop timeout manager
  void stop() {
    synchronized(lock) {
      work = false;
      if (internalThread != null) {
        internalThread.interrupt();
      }
      integrityWatcher.stop();
      internalThread = null;
      integrityWatcher = null;
      threadSystem = null;
      map = null;
      queue = null;
    }
    synchronized(synchronousLock) {
      synchronousWork = false;
      if (synchronousInternalThread != null) {
        synchronousInternalThread.interrupt();
      }
      synchronousIntegrityWatcher.stop();
      synchronousInternalThread = null;
      synchronousIntegrityWatcher = null;
      synchronousMap = null;
      synchronousQueue = null;
    }
    SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000021",
        "TimeoutManagerImpl stopped");
  }

  public void registerTimeoutListener(TimeoutListener listener, long delayTime, long repeatTime) {
    register(listener, delayTime, repeatTime, 0, false, true);
  }

  public void registerTimeoutListener(TimeoutListener listener, long delayTime, long repeatTime, boolean systemThread) {
    register(listener, delayTime, repeatTime, 0, false, systemThread);
  }

  public void registerTimeoutListener(TimeoutListener listener, boolean waitForTimeoutEvent, long delayTime, long repeatTime) {
    register(listener, delayTime, repeatTime, 0, waitForTimeoutEvent, true);
  }

  public void registerTimeoutListener(TimeoutListener listener, boolean waitForTimeoutEvent, long delayTime, long repeatTime, boolean systemThread) {
    register(listener, delayTime, repeatTime, 0, waitForTimeoutEvent, systemThread);
  }

  public void registerTimeoutListener(TimeoutListener listener, long delayTime, long repeatTime, long occurrences) throws IllegalArgumentException {
    register(listener, delayTime, repeatTime, occurrences, false, true);
  }

  public void registerTimeoutListener(TimeoutListener listener, long delayTime, long repeatTime, long occurrences, boolean systemThread) {
    register(listener, delayTime, repeatTime, occurrences, false, systemThread);
  }

  public void registerTimeoutListener(TimeoutListener listener, boolean waitForTimeoutEvent, long delayTime, long repeatTime, long occurrences) {
    register(listener, delayTime, repeatTime, occurrences, waitForTimeoutEvent, true);
  }

  public void registerTimeoutListener(TimeoutListener listener, boolean waitForTimeoutEvent, long delayTime, long repeatTime, long occurrences, boolean systemThread) {
    register(listener, delayTime, repeatTime, occurrences, waitForTimeoutEvent, systemThread);
  }

  private void register(TimeoutListener listener, long delayTime, long repeatTime, long occurrences, boolean waitForTimeoutEvent, boolean systemThread) {
    checkForNullTimeoutListener(listener);
    TimeoutNode node;
    synchronized (lock) {
      SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000022",
          "register: [{0}], delay: [{1}], repeat: [{2}], ocurrences: [{3}], waitForTimeoutEvent: [{4}], systemThread: [{5}]",
          listener, delayTime, repeatTime, occurrences, waitForTimeoutEvent, systemThread);
      if (map.containsKey(listener)) {
        SimpleLogger.trace(Severity.WARNING, location, "ASJ.timeout.000004",
            "Listener already registered: [{0}]", listener);
        throw new IllegalArgumentException("Listener already registered");
      }
      node = new TimeoutNode();
      node.work = listener;
      node.delay = delayTime;
      node.repeat = repeatTime;
      node.occurrences = occurrences;
      node.waitForTimeoutEvent = waitForTimeoutEvent;
      node.system = systemThread;
      node.nextCallTime = integrityWatcher.getCurrentTimeMillis() + delayTime;
      node.timeoutManager = this;
      node.queuePosition = -1;
      node.isCancel = false;
      node.repeatState = false;
      map.put(listener, node);
      synchronized (statisticsLock) {
        registeredListenersCount++;
        if (node.repeat > 0) {
          estimatedFrequencyPerMinute += 1.0 / (node.repeat);
        }
      }
      // notify lock if node is added as queue head
      try {
        if (queue.add(node)) {
          lock.notify();
        }
      } catch (RuntimeException e) {
        SimpleLogger.traceThrowable(Severity.WARNING, location, "ASJ.timeout.000049",
            "Cannot register timeout listener", e);
        throw e;
      }
      SimpleLogger.trace(Severity.DEBUG, location, "ASJ.timeout.000048",
          "Registered [{0}]", node);
    }
  }

  public void unregisterTimeoutListener(TimeoutListener listener) {
    checkForNullTimeoutListener(listener);
    TimeoutNode node;
    synchronized (lock) {
      SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000023",
          "unregisterTimeoutListener [{0}]", listener);
      node = map.remove(listener);
      if (node != null) {
        SimpleLogger.trace(Severity.DEBUG, location, "ASJ.timeout.000044",
            "Unregistered [{0}]", node);

        synchronized (statisticsLock) {
          registeredListenersCount--;
          if (node.repeat > 0) {
            estimatedFrequencyPerMinute -= 1.0 / (node.repeat);
          }
        }
        if (node.queuePosition >= 0) {
          // notify lock if node is removed from queue head
          if (queue.remove(node)) {
            lock.notify();
          }
        } else {
          node.isCancel = true;
        }
      }
    }
  }

  public void changeRepeatTime(TimeoutListener listener, long repeatTime) {
    checkForNullTimeoutListener(listener);
    if (repeatTime <= 0) {
      SimpleLogger.trace(Severity.WARNING, location, "ASJ.timeout.000005",
          "Cannot register listener; value of parameter [repeatTime] is negative");
      throw new IllegalArgumentException("Cannot register listener; value of parameter [repeatTime] is negative");
    }
    synchronized (lock) {
      SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000024",
          "changeRepeatTime: [{0}], repeatTime: [{1}]",
          listener, repeatTime);
      TimeoutNode node = (TimeoutNode) map.get(listener);
      if (node != null && node.repeat > 0) {
        node.repeat = repeatTime;
      }
    }
  }

  public void changeRepeatTime(TimeoutListener listener, long repeatTime, long occurrences) {
    checkForNullTimeoutListener(listener);
    if (repeatTime <= 0) {
      SimpleLogger.trace(Severity.WARNING, location, "ASJ.timeout.000006",
          "Cannot register listener; value of parameter [repeatTime] is negative");
      throw new IllegalArgumentException("Cannot register listener; value of parameter [repeatTime] is negative");
    }
    if (occurrences < 0) {
      SimpleLogger.trace(Severity.WARNING, location, "ASJ.timeout.000007",
          "Cannot register TimeoutListener; value of parameter occurrences is negative");
      throw new IllegalArgumentException("Cannot register TimeoutListener; value of parameter occurrences is negative");
    }
    synchronized (lock) {
      SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000025",
          "changeRepeatTime: [{0}], repeatTime: [{1}], occurrences: [{2}]",
          listener, repeatTime, occurrences);
      TimeoutNode node = map.get(listener);
      if (node != null && node.repeat > 0) {
        node.repeat = repeatTime;
        node.occurrences = occurrences;
      }
    }
  }

  public void refreshTimeout(TimeoutListener listener) {
    checkForNullTimeoutListener(listener);
    TimeoutNode node = null;
    synchronized (lock) {
      SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000026",
          "refreshTimeout: [{0}]", listener);
      node = (TimeoutNode) map.get(listener);
      if (node != null) {
        node.nextCallTime = integrityWatcher.getCurrentTimeMillis() + ((node.repeatState) ? node.repeat : node.delay);
        if (node.queuePosition >= 0) {
          // notify lock if node is recalculated at queue head
          if (queue.recalculate(node)) {
            lock.notify();
          }
        }
      }
    }
  }

  public void registerSynchronousTimeoutListener(TimeoutListener listener, long delayTime, long repeatTime) {
    registerSynchronous(listener, delayTime, repeatTime, 0, false);
  }

  public void registerSynchronousTimeoutListener(TimeoutListener listener, boolean waitForTimeoutEvent, long delayTime, long repeatTime) {
    registerSynchronous(listener, delayTime, repeatTime, 0, waitForTimeoutEvent);
  }

  public void registerSynchronousTimeoutListener(TimeoutListener listener, long delayTime, long repeatTime, long occurrences) {
    registerSynchronous(listener, delayTime, repeatTime, occurrences, false);
  }

  public void registerSynchronousTimeoutListener(TimeoutListener listener, boolean waitForTimeoutEvent, long delayTime, long repeatTime, long occurrences) {
    registerSynchronous(listener, delayTime, repeatTime, occurrences, waitForTimeoutEvent);
  }

  private void registerSynchronous(TimeoutListener listener, long delayTime, long repeatTime, long occurrences, boolean waitForTimeoutEvent) {
    checkForNullTimeoutListener(listener);
    TimeoutNode node;
    synchronized (synchronousLock) {
      SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000027",
          "registerSynchronous: [{0}], delay: [{1}], repeat: [{2}], ocurrences: [{3}], waitForTimeoutEvent: [{4}]",
          listener, delayTime, repeatTime, occurrences, waitForTimeoutEvent);
      if (synchronousMap.containsKey(listener)) {
        SimpleLogger.trace(Severity.WARNING, location, "ASJ.timeout.000008",
            "Listener already registered");
        throw new IllegalArgumentException("Listener already registered");
      }
      node = new TimeoutNode();
      node.work = listener;
      node.delay = delayTime;
      node.repeat = repeatTime;
      node.occurrences = occurrences;
      node.waitForTimeoutEvent = waitForTimeoutEvent;
      node.nextCallTime = synchronousIntegrityWatcher.getCurrentTimeMillis() + delayTime;
      node.timeoutManager = this;
      node.queuePosition = -1;
      node.isCancel = false;
      node.repeatState = false;
      synchronousMap.put(listener, node);
      synchronized (statisticsLock) {
        registeredListenersCount++;
        if (node.repeat > 0) {
          estimatedFrequencyPerMinute += 1.0 / (node.repeat);
        }
      }
      // notify lock if node is added as queue head
      if (synchronousQueue.add(node)) {
        synchronousLock.notify();
      }
    }
  }

  public void unregisterSynchronousTimeoutListener(TimeoutListener listener) {
    checkForNullTimeoutListener(listener);
    TimeoutNode node;
    synchronized (synchronousLock) {
      SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000028",
          "unregisterSynchronousTimeoutListener: [{0}]", listener);
      node = synchronousMap.remove(listener);
      if (node != null) {
        synchronized (statisticsLock) {
          registeredListenersCount--;
          if (node.repeat > 0) {
            estimatedFrequencyPerMinute -= 1.0 / (node.repeat);
          }
        }
        if (node.queuePosition >= 0) {
          // notify lock if node is removed from queue head
          if (synchronousQueue.remove(node)) {
            synchronousLock.notify();
          }
        } else {
          node.isCancel = true;
        }
      }
    }
  }

  public void changeSynchronousRepeatTime(TimeoutListener listener, long repeatTime) {
    checkForNullTimeoutListener(listener);
    if (repeatTime <= 0) {
      SimpleLogger.trace(Severity.WARNING, location, "ASJ.timeout.000009",
          "Cannot register listener; value of parameter [repeatTime] is negative");
      throw new IllegalArgumentException("Cannot register listener; value of parameter [repeatTime] is negative");
    }
    synchronized (synchronousLock) {
      SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000029",
          "changeSynchronousRepeatTime: [{0}], repeatTime: [{1}]", listener, repeatTime);
      TimeoutNode node = (TimeoutNode) synchronousMap.get(listener);
      if (node != null && node.repeat > 0) {
        node.repeat = repeatTime;
      }
    }
  }

  public void changeSynchronousRepeatTime(TimeoutListener listener, long repeatTime, long occurrences) {
    checkForNullTimeoutListener(listener);
    if (repeatTime <= 0) {
      SimpleLogger.trace(Severity.WARNING, location, "ASJ.timeout.000010",
          "Cannot register listener; value of parameter [repeatTime] is negative");
      throw new IllegalArgumentException("Cannot register listener; value of parameter [repeatTime] is negative");
    }
    if (occurrences < 0) {
      SimpleLogger.trace(Severity.WARNING, location, "ASJ.timeout.000011",
          "Cannot register TimeoutListener; value of parameter occurrences is negative");
      throw new IllegalArgumentException("Cannot register TimeoutListener; value of parameter occurrences is negative");
    }
    synchronized (synchronousLock) {
      SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000030",
          "changeSynchronousRepeatTime: [{0}], repeatTime: [{1}], occurrences: [{2}]",
          listener, repeatTime, occurrences);
      TimeoutNode node = synchronousMap.get(listener);
      if (node != null && node.repeat > 0) {
        node.repeat = repeatTime;
        node.occurrences = occurrences;
      }
    }
  }

  public void refreshSynchronousTimeout(TimeoutListener listener) {
    checkForNullTimeoutListener(listener);
    TimeoutNode node = null;
    synchronized (synchronousLock) {
      SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000031",
          "refreshSynchronousTimeout: [{0}]", listener);
      node = (TimeoutNode) synchronousMap.get(listener);
      if (node != null) {
        node.nextCallTime = synchronousIntegrityWatcher.getCurrentTimeMillis() + ((node.repeatState) ? node.repeat : node.delay);
        if (node.queuePosition >= 0) {
          // notify lock if node is recalculated at queue head
          if (synchronousQueue.recalculate(node)) {
            synchronousLock.notify();
          }
        }
      }
    }
  }

  public int getRegisteredListenersCount() {
    return registeredListenersCount;
  }

  public int getEstimatedFrequencyPerMinute() {
    return (int) (estimatedFrequencyPerMinute * FACTOR);
  }

  public void registerManagementListener(ManagementListener managementListener) {
  }

  // main logic
  public void multiThreadRun() {
    internalThread = Thread.currentThread();
    int priority = Thread.currentThread().getPriority();
    Vector<TimeoutNode> timeoutTasks = new Vector<TimeoutNode>();
    try {
      Thread.currentThread().setPriority(threadPriority);
      TimeoutNode node = null;
      long delayTime;
      long currentTime;
      while (work) {
        synchronized (lock) {
          try {
            // wait while queue is empty
            ThreadWrapper.pushTask("", ThreadWrapper.TS_WAITING_FOR_TASK);
            try {
              while (queue.isEmpty()) {
                lock.wait();
              }
            } finally {
              ThreadWrapper.popTask();
            }
            //calculate actual wait time (some nodes can be added in the queue meantime)
            if ((node = queue.getFirst()) != null) {
              currentTime = integrityWatcher.getCurrentTimeMillis();
              delayTime = node.nextCallTime - currentTime;
              if (delayTime > 0) {
                ThreadWrapper.pushTask("", ThreadWrapper.TS_WAITING_FOR_TASK);
                try {
                  lock.wait(delayTime);
                } finally {
                  ThreadWrapper.popTask();
                }
              }
            }
            ThreadWrapper.pushTask("Processing Node", ThreadWrapper.TS_PROCESSING);
            final boolean beDebug = location.beDebug();  // Use local reference for fastest access
            try {
              currentTime = integrityWatcher.getCurrentTimeMillis();
              // process all nodes with time <= current time
              while ((node = queue.getFirst()) != null && (delayTime = node.nextCallTime - currentTime) <= 0) {
                boolean check = node.check();
                if (beDebug) {
                  SimpleLogger.trace(Severity.DEBUG, location, "ASJ.timeout.000045",
                      "check() returns [{0}] for [{1}]", check, node);
                }
                if (check) {
                  // call timeout() in thread
                  if (node.waitForTimeoutEvent) {
                    // remove from queue and process after timeout() return
                    queue.removeFirst();
                  }
                  timeoutTasks.add(node);
                  if (!node.waitForTimeoutEvent) {
                    processTimeoutNode(node);
                  }
                } else {
                  processTimeoutNode(node);
                }
              }
            } finally {
              ThreadWrapper.popTask();
            }
          } catch (InterruptedException iException) {
            SimpleLogger.traceThrowable(Severity.DEBUG, location, "ASJ.timeout.000032",
                "multiThreadRun()", iException);
            if (work) {
              SimpleLogger.trace(Severity.ERROR, location, "ASJ.timeout.000033",
                  "Main timeout thread is interrupted while working");
            } else {
              SimpleLogger.trace(Severity.INFO, location, "ASJ.timeout.000034",
                  "Main timeout thread is interrupted");
            }
          }
        }
        //start in threads here to prevent deadlock if thread manager block on startThread
        for (TimeoutNode timeoutNode : timeoutTasks) {
          final int nodeNumber = timeoutNode.number;
          final int invocationCount = timeoutNode.incrementAndGetInvocationCount();
          SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000035",
              "Execute timeout listener [{0}], id [{1}], invocation [{2}]",
              timeoutNode.work, nodeNumber, invocationCount);
          threadSystem.startThread(timeoutNode, "",
              "Timeout Event Processor, id [" + nodeNumber + "], invocation [" + invocationCount + "]", timeoutNode.system);
        }
        timeoutTasks.clear();
      }
    } finally {
      Thread.currentThread().setPriority(priority);
    }
  }

  // this method is call from work thread when waitForTimeoutEvent=true
  final void processNode(TimeoutNode node) {
    synchronized (lock) {
      // notify lock if node is added as queue head
      if (processTimeoutNode(node)) {
        lock.notify();
      }
    }
  }

  private boolean processTimeoutNode(TimeoutNode node) {
    boolean result = false;
    if (node.isCancel) {
      return false;
    }
    if (node.repeat > 0 && node.occurrences != 1) {
      if (!node.repeatState) {
        node.repeatState = true;
      }
      if (node.occurrences > 1) {
        node.occurrences--;
      }
      if (node.waitForTimeoutEvent) {
        node.nextCallTime = integrityWatcher.getCurrentTimeMillis() + node.repeat;
        result = queue.add(node);
      } else {
        node.nextCallTime += node.repeat;
        result = queue.recalculate(node);
      }
    } else {
      // automatic unregistration
      map.remove(node.work);
      SimpleLogger.trace(Severity.DEBUG, location, "ASJ.timeout.000043",
        "Automatically unregistered [{0}]", node);

      synchronized (statisticsLock) {
        registeredListenersCount--;
        if (node.repeat > 0) {
          estimatedFrequencyPerMinute -= 1.0 / (node.repeat);
        }
      }
      //if waitForTimeoutEvent=true the node is already removed from the queue
      if (!node.waitForTimeoutEvent) {
        queue.remove(node);
      }
    }

    SimpleLogger.trace(Severity.DEBUG, location, "ASJ.timeout.000047",
      "processTimeoutNode() returns [{0}] for [{1}]", result, node);

    return result;
  }

  public void singleThreadRun() {
    synchronousInternalThread = Thread.currentThread();
    int priority = Thread.currentThread().getPriority();
    Vector<TimeoutNode> timeoutTasks = new Vector<TimeoutNode>();
    try {
      Thread.currentThread().setPriority(threadPriority);
      TimeoutNode node = null;
      long delayTime;
      long currentTime;
      while (synchronousWork) {
        synchronized (synchronousLock) {
          try {
            // wait while queue is empty
            ThreadWrapper.pushTask("", ThreadWrapper.TS_WAITING_FOR_TASK);
            try {
              while (synchronousQueue.isEmpty()) {
                synchronousLock.wait();
              }
            } finally {
              ThreadWrapper.popTask();
            }
            //calculate actual wait time (some nodes can be added in the queue meantime)
            if ((node = synchronousQueue.getFirst()) != null) {
              currentTime = synchronousIntegrityWatcher.getCurrentTimeMillis();
              delayTime = node.nextCallTime - currentTime;
              if (delayTime > 0) {
                ThreadWrapper.pushTask("", ThreadWrapper.TS_WAITING_FOR_TASK);
                try {
                  synchronousLock.wait(delayTime);
                } finally {
                  ThreadWrapper.popTask();
                }
              }
            }
            ThreadWrapper.pushTask("Processing Node", ThreadWrapper.TS_PROCESSING);
            try {
              currentTime = synchronousIntegrityWatcher.getCurrentTimeMillis();
              // process all nodes with time <= current time
              while ((node = synchronousQueue.getFirst()) != null && (delayTime = node.nextCallTime - currentTime) <= 0) {
                boolean check = node.check();
                if (check) {
                  // call timeout()
                  if (node.waitForTimeoutEvent) {
                    // remove from queue and process after timeout() return
                    synchronousQueue.removeFirst();
                  }
                  timeoutTasks.add(node);
                  if (!node.waitForTimeoutEvent) {
                    processSynchronousTimeoutNode(node);
                  }
                } else {
                  processSynchronousTimeoutNode(node);
                }
              }
            } finally {
              ThreadWrapper.popTask();
            }
          } catch (InterruptedException iException) {
            SimpleLogger.traceThrowable(Severity.DEBUG, location, "ASJ.timeout.000036",
                "singleThreadRun()", iException);
            if (synchronousWork) {
              SimpleLogger.trace(Severity.ERROR, location, "ASJ.timeout.000037",
                  "Main timeout thread is interrupted while working");
            } else {
              SimpleLogger.trace(Severity.INFO, location, "ASJ.timeout.000038",
                  "Main timeout thread is interrupted");
            }
          }
        }
        //call synchronous run here to prevent deadlocks and release the lock
        for (TimeoutNode timeoutNode : timeoutTasks) {
          SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000039",
              "Execute synchronous timeout listener [{0}]", timeoutNode.work);
          timeoutNode.synchronousRun();
        }
        timeoutTasks.clear();
      }
    } finally {
      Thread.currentThread().setPriority(priority);
    }
  }

  // this method is call when waitForTimeoutEvent=true
  final void processSynchronousNode(TimeoutNode node) {
    synchronized (synchronousLock) {
      // notify lock if node is added as synchronous queue head
      if (processSynchronousTimeoutNode(node)) {
        synchronousLock.notify();
      }
    }
  }

  private boolean processSynchronousTimeoutNode(TimeoutNode node) {
    boolean result = false;
    if (node.isCancel) {
      return false;
    }
    if (node.repeat > 0 && node.occurrences != 1) {
      if (!node.repeatState) {
        node.repeatState = true;
      }
      if (node.occurrences > 1) {
        node.occurrences--;
      }
      if (node.waitForTimeoutEvent) {
        node.nextCallTime = synchronousIntegrityWatcher.getCurrentTimeMillis() + node.repeat;
        result = synchronousQueue.add(node);
      } else {
        node.nextCallTime += node.repeat;
        result = synchronousQueue.recalculate(node);
      }
    } else {
      // automatic unregistration
      synchronousMap.remove(node.work);
      synchronized (statisticsLock) {
        registeredListenersCount--;
        if (node.repeat > 0) {
          estimatedFrequencyPerMinute -= 1.0 / (node.repeat);
        }
      }
      //if waitForTimeoutEvent=true the node is already removed from the queue
      if (!node.waitForTimeoutEvent) {
        synchronousQueue.remove(node);
      }
    }
    return result;
  }

  private static void checkForNullTimeoutListener(TimeoutListener listener) {
    if (listener == null) {
      SimpleLogger.trace(Severity.WARNING, location, "ASJ.timeout.000012",
          "Cannot register listener NULL");
      throw new IllegalArgumentException("Cannot register listener NULL");
    }
  }

  public TimeoutListener[] listRegisteredTimeoutListeners() {
    synchronized (lock) {
      return queue.getAllItems();
    }
  }

  public TimeoutListener[] listRegisteredSinchronousTimeoutListeners() {
    synchronized (synchronousLock) {
      return synchronousQueue.getAllItems();
    }
  }

}