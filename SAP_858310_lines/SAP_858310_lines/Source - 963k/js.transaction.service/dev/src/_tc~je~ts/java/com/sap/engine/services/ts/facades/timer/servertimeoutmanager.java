package com.sap.engine.services.ts.facades.timer;

public class ServerTimeoutManager implements TimeoutManager {

	protected final com.sap.engine.services.timeout.TimeoutManager serverManager;

	/**
	 * Initialize ServerTimeoutManager instance
	 * 
	 * @param serverManager
	 *            must be from com.sap.engine.services.timeout.TimeoutManager
	 *            type
	 */
	public ServerTimeoutManager(Object serverManager) {
		this.serverManager = (com.sap.engine.services.timeout.TimeoutManager)serverManager;
	}

	public void registerTimeoutListener(TimeoutListener listener,
			long delayTime, long repeatTime) {

		TimeoutListenerWrapper tlw = new TimeoutListenerWrapper(listener);
		listener.associateObject(tlw);

		serverManager.registerTimeoutListener(tlw, delayTime, repeatTime);
	}

	public void unregisterTimeoutListener(TimeoutListener listener) {
		serverManager.unregisterTimeoutListener((TimeoutListenerWrapper)listener.getAssociateObject());
	}


	protected static class TimeoutListenerWrapper implements com.sap.engine.services.timeout.TimeoutListener {

		protected final TimeoutListener targetListener;

		public TimeoutListenerWrapper(TimeoutListener targetListener) {
			this.targetListener = targetListener;
		}

		public boolean check() {
			return targetListener.check();
		}

		public void timeout() {
			targetListener.timeout();
		}
		
	}
}
