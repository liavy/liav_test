package com.sap.engine.services.timeout;

import com.sap.engine.frame.core.thread.ThreadSystem;

class TimeoutManagerRunner implements Runnable {

  private TimeoutManagerImpl timeoutManager;
  private boolean synchronous;

  TimeoutManagerRunner(TimeoutManagerImpl timeoutManager, boolean synchronous, ThreadSystem ts, String threadName) {
    this.timeoutManager = timeoutManager;
    this.synchronous = synchronous;
    ts.startThread(this, "Processing timeout queue", threadName, true);
  }

  public void run() {
    if (synchronous) {
      timeoutManager.singleThreadRun();
    } else {
      timeoutManager.multiThreadRun();
    }
  }

}
