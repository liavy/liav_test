package com.sap.engine.core.thread.impl3;

import com.sap.engine.core.Names;
import com.sap.engine.frame.ProcessEnvironment;
//import com.sap.engine.lib.logging.LoggingHelper;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LoggingUtilities;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

/**
 * This class handles all exceptions, which are not handled anywhere in the engine. As for the OOMErrors,
 * it forwards them to the ProcessEnvironment class, which handles them. It logs the other errors and exceptions.
 * @author Lazar Kirchev
 *
 */
public class UncaughtExceptionHandler implements
java.lang.Thread.UncaughtExceptionHandler {

	/*
	 * Location used for messages tracing 
	 */
	private final static Location location = Location.getLocation(ErrorQueueHandler.class.getName(), Names.KERNEL_DC_NAME, Names.THREAD_MANAGER_CSN_COMPONENT);

	/*
	 * Category used for logging critical engine messages for AGS. 
	 */
	private static final Category catServerCritical = Category.getCategory(Category.SYS_SERVER, "Critical");

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		// TODO Auto-generated method stub
		try{
			if(e instanceof OutOfMemoryError){
				ProcessEnvironment.handleOOM((OutOfMemoryError)e);
			}else{
				SimpleLogger.log(Severity.WARNING, catServerCritical, location, "ASJ.krn_thd.000109", "The follwoing unhandled exception: [{0}] was detected in [{1}]", new Object[]{e, t});
				SimpleLogger.trace(Severity.WARNING, location, 
						LoggingUtilities.getDcNameByClassLoader(t.getClass().getClassLoader()),
						null,
						"ASJ.krn_thd.000109",
						"The following unhandled exception: [{0}] was detected in [{1}]", e, t, e.getMessage());
			}
		}catch(OutOfMemoryError oom){
			// Excluding this catch block from JLIN $JL-EXC$ since there is no need to log further
	        // Please do not remove this comment !
			ProcessEnvironment.handleOOM(oom);
		}
	}

}
