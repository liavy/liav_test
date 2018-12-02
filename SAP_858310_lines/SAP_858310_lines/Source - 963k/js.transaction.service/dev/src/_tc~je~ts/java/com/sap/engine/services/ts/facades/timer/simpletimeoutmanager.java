package com.sap.engine.services.ts.facades.timer;

import java.util.Timer;
import java.util.TimerTask;

public class SimpleTimeoutManager implements TimeoutManager {

	protected final static int NUMBER_CANCALLED_TASK_UNTIL_PURGE = 1000;

	protected final Timer timer = new Timer(true);
	protected int cancalledTasks = 0;

	public void registerTimeoutListener(TimeoutListener listener,
			long delayTime, long repeatTime) {

		SimpleTimerTask tt = new SimpleTimerTask(listener);
		listener.associateObject(tt);

		if (repeatTime <= 0 ) {
			timer.schedule(tt, delayTime);
		} else {
			timer.schedule(tt, delayTime, repeatTime);
		}
	}

	public void unregisterTimeoutListener(TimeoutListener listener) {
		((SimpleTimerTask)listener.getAssociateObject()).cancel();
		if (cancalledTasks++  >= NUMBER_CANCALLED_TASK_UNTIL_PURGE) {
			timer.purge();
		}
	}


	protected static class SimpleTimerTask extends TimerTask {

		protected final TimeoutListener listener;

		SimpleTimerTask(TimeoutListener listener) {
			this.listener = listener;
		}

		@Override
		public void run() {
			if (listener.check()) {
				listener.timeout();
			}
		}
		
	}
}
