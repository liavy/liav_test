/*
 * Copyright (c) 2003 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.timeout;

import com.sap.engine.frame.core.thread.ThreadSystem;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

/**
 * Class used for watching the integrity of the timeout intervals.
 * It detects all time changes out of interval [-EXPECTED, EXPECTED]
 *
 * @author Hristo Iliev, Dimitar Kostadinov
 * @version 6.30
 */
final class TimeoutIntegrityWatcher implements Runnable {

  /**
   * Location used for logging
   */
  public static final Location location = Location.getLocation(TimeoutIntegrityWatcher.class);

  private static final String THREAD_NAME = "Timeout Service Integrity Watcher";
  // wait time 1 sec
  private static final long INTERVAL = 1000;
  // max expected wait time 1 min.
  private static final long EXPECTED = 60000;
  // queue will be recalculated only if delta accumolation is more or less than 2 min.
  private static final long DELTA_TOLERANCE = 120000;

  // run method work flag
  private boolean work = false;
  // queue hold TimeoutNode objects
  private PriorityQueue queue = null;
  // system time before wait
  private long beforeWaitTime = 0;
  // holds delta accumulation
  private long deltaAccumulation = 0;
  // integrity thread
  private Thread integrityThread = null;

  TimeoutIntegrityWatcher(PriorityQueue queue, ThreadSystem threadSystem) {
    this.queue = queue;
    threadSystem.startThread(this, "", THREAD_NAME, true);
    ensureThreadStart();
  }

  public void run() {
    integrityThread = Thread.currentThread();
    int priority = integrityThread.getPriority();
    try {
      // max priority is needed for exact delta time calculation
      integrityThread.setPriority(Thread.MAX_PRIORITY);
      synchronized (this) {
        beforeWaitTime = System.currentTimeMillis();
        work = true;
        this.notify();
        while (work) {
          try {
            this.wait(INTERVAL);
          } catch (InterruptedException e) {
            //$JL-EXC$
            //Please do not remove this comment!
            continue;
          } finally {
            beforeWaitTime = accumulateDelta();
          }
        }
      }
    } finally {
      Thread.currentThread().setPriority(priority);
    }
  }

  /**
   * Returns current time and recalculate all TimeoutNode objects in queue
   * according deltaAccumulation. This method is used instead System.currentTimeMillis()
   * and the returned time is consistent in interval [-EXPECTED, EXPECTED].
   * This method can change all TimeoutNode.nextCallTime values and it must not be
   * used directly in expressions containing nextCallTime!
   *
   * @return current time
   */
  final synchronized long getCurrentTimeMillis() {
    long currentTime = accumulateDelta();
    if (deltaAccumulation != 0) {
      // make real recalculation only if |deltaAccumulation| is bigger than delta tolerance
      if (((deltaAccumulation < 0) ? -deltaAccumulation : deltaAccumulation) > DELTA_TOLERANCE) {
        SimpleLogger.trace(Severity.PATH, location, "ASJ.timeout.000019",
             " > Recalculating queue with delta [{0}]",
             deltaAccumulation);
        queue.recalculateIntervals(deltaAccumulation);
      }
      // reset deltaAccumulation and beforeWaitTime
      deltaAccumulation = 0;
      beforeWaitTime = currentTime;
    }
    return currentTime;
  }

  // stop integrity thread
  final void stop() {
    synchronized (this) {
      work = false;
      integrityThread.interrupt();
    }
  }

  // block calling thread until integrityThread is initialized
  private final void ensureThreadStart() {
    synchronized (this) {
      while (!work) {
        try {
          this.wait();
        } catch (InterruptedException e) {
          //$JL-EXC$
          //Please do not remove this comment!
          continue;
        }
      }
    }
  }

  // return current time, accumulate delta if needed
  private final long accumulateDelta() {
    long currentTime = System.currentTimeMillis();
    long delta = currentTime - beforeWaitTime;
    if (((delta < 0) ? -delta : delta) > EXPECTED) {
      deltaAccumulation += delta;
    }
    return currentTime;
  }

}