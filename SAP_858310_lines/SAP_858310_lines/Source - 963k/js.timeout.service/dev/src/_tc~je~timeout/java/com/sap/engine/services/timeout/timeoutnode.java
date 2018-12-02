/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf.. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.timeout;

import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;
import com.sap.tc.logging.LoggingUtilities;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class TimeoutNode. A task that can be scheduled for one-time or repeated
 * execution by a TimeoutManager.
 *
 * @author Georgi Stanev, Jasen Minov, Hristo Iliev, Dimitar Kostadinov
 * @version 6.30 July 2002
 */
final class TimeoutNode implements Runnable {

  /**
   * Location used for logging
   */
  public static Location location = Location.getLocation(TimeoutNode.class);

  // Timeout object for execution
  TimeoutListener work = null;
  // Timeout manager
  TimeoutManagerImpl timeoutManager = null;
  // Delay Interval
  long delay = 0;
  // Repeat interval
  long repeat = -1;
  // Number of occurrences in case of periodic execution
  long occurrences = 0;
  // Stores the type of the thread that is run after the timeout
  boolean system = true;
  // Stores the type of event handling. If true, waits for the timeout event for finish
  boolean waitForTimeoutEvent = false;
  // Time to call timeout() function
  long nextCallTime = 0;
  // Current position in priority queue, if queuePosition == -1 this node is not in queue
  int queuePosition = -1;
  // if true the node is unregistered and is currently not in queue (waitForTimeoutEvent=true case)
  boolean isCancel = false;
  // true if delay has expired and the listener is repeating
  boolean repeatState = false;

  static int totalNodes = 0;

  final int number;
  final AtomicInteger invocationCount = new AtomicInteger(0);

  public TimeoutNode() {
    number = totalNodes;
    totalNodes++;
  }

  // call in new thread from thread pool
  public void run() {
    ThreadWrapper.pushTask("Processing by " + work, ThreadWrapper.TS_PROCESSING);
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try {
      SimpleLogger.trace(Severity.DEBUG, location, "ASJ.timeout.000046",
          "timeout() for [{0}]", this);
      Thread.currentThread().setContextClassLoader(work.getClass().getClassLoader());
      long t1 = System.currentTimeMillis();
      work.timeout();
      long t2 = System.currentTimeMillis();
      SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000040",
          " > [{0}].timeout() method is processed for [{1}] ms.",
          work, (t2 - t1));
    } catch (RuntimeException re) {
      ClassLoader cl;
      if (work == null) {
        cl = TimeoutNode.class.getClassLoader();
      } else {
        cl = work.getClass().getClassLoader();
      }
      SimpleLogger.trace(Severity.WARNING, location,
                         LoggingUtilities.getDcNameByClassLoader(cl),
                         null, "ASJ.timeout.000042", "TimeoutNode.run()",
                         re);
    } finally {
      try {
        Thread.currentThread().setContextClassLoader(loader);
        if (waitForTimeoutEvent) {
          timeoutManager.processNode(this);
        }
      } finally {
        ThreadWrapper.popTask();
      }
    }
  }

  // call in synchronous timeout work thread
  final void synchronousRun() {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(work.getClass().getClassLoader());
      long t1 = System.currentTimeMillis();
      work.timeout();
      long t2 = System.currentTimeMillis();
      SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000041",
          " > [{0}].timeout() method is processed for [{1}] ms.",
          work, (t2 - t1));
    } catch (RuntimeException re) {
      SimpleLogger.trace(Severity.ERROR, location,
                         LoggingUtilities.getDcNameByClassLoader(work.getClass().getClassLoader()),
                         null, "ASJ.timeout.000013", "TimeoutNode.synchronousRun()",
                         re);
    } catch (ThreadDeath td) {
      //$JL-EXC$
      throw td;
    } catch (OutOfMemoryError oome) {
      //$JL-EXC$
      throw oome;
    } catch (Throwable t) {
      SimpleLogger.trace(Severity.ERROR, location,
                         LoggingUtilities.getDcNameByClassLoader(work.getClass().getClassLoader()),
                         null, "ASJ.timeout.000014", "TimeoutNode.synchronousRun()",
                         t);
    } finally {
      Thread.currentThread().setContextClassLoader(loader);
      if (waitForTimeoutEvent) {
        timeoutManager.processSynchronousNode(this);
      }
    }
  }

  final boolean check() {
    boolean result = true;
    try {
      result = work.check();
    } catch (RuntimeException e) {
      ClassLoader cl;
      if (work == null) {
        cl = TimeoutNode.class.getClassLoader();
      } else {
        cl = work.getClass().getClassLoader();
      }
      SimpleLogger.trace(Severity.ERROR, location,
                         LoggingUtilities.getDcNameByClassLoader(cl),
                         null, "ASJ.timeout.000015", "TimeoutNode.check()",
                         e);
    } catch (ThreadDeath td) {
      //$JL-EXC$
      throw td;
    } catch (OutOfMemoryError oome) {
      //$JL-EXC$
      throw oome;
    } catch (Throwable t) {
      ClassLoader cl;
      if (work == null) {
        cl = TimeoutNode.class.getClassLoader();
      } else {
        cl = work.getClass().getClassLoader();
      }
      SimpleLogger.trace(Severity.ERROR, location,
                         LoggingUtilities.getDcNameByClassLoader(cl),
                         null, "ASJ.timeout.000016", "TimeoutNode.check()",
                         t);
    }
    return result;
  }

  final int incrementAndGetInvocationCount() {
    return invocationCount.getAndIncrement();
  }

  public String toString() {
    // Use Integer.toHexString() instead of "%x" because we need unsigned hex
    return String.format(
        "TimeoutNode@%s {work [%s@%s],%n" +
        "\tid [%d], invokedCount [%d], nextCallTime [%d], queuePosition [%d],%n" +
        "\tdelay [%d], repeat [%d], occurrences [%d],%n" +
        "\twaitForTimeoutEvent [%s], system [%s], repeatState [%s], isCancel [%s]}",
        Integer.toHexString(hashCode()), work.getClass().getName(), Integer.toHexString(work.hashCode()),
        number, invocationCount.get(), nextCallTime, queuePosition,
        delay, repeat, occurrences,
        waitForTimeoutEvent, system, repeatState, isCancel
    );
  }

}