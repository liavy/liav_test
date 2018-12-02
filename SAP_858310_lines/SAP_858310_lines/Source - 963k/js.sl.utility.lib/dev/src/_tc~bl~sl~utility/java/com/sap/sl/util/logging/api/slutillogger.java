package com.sap.sl.util.logging.api;

import com.sap.sl.util.loader.Loader;

/**
 * SAP SL Util Logging
 *
 * @author  hen
 * @version 6
 * modified 02.11.2002 - hen
 * 			12.03.2003 - hen - send all messages to System.out.println & startLoggingTo/endLoggingTo
 * 			24.06.2003 - hen - add method exiting(String method, String message)
 *          25.06.2003 - md - copied to sl.util
 *      04.07.2003 - cg - separation of api and implementation
 */

public abstract class SlUtilLogger {
  private static Class IMPL_CLASS = null;
  private final static String IMPL_CLASS_NAME
    = "com.sap.sl.util.logging.impl.SlUtilLoggerImpl";

  
//  create a logger using an external defined log file
  public static SlUtilLogger getLogger() {
    return getLogger(null);
  }

//	create a logger
	public static SlUtilLogger getLogger(String className) {
		return getLogger(className, true);
	}

//  create a logger and read configuration from properties file
	public static SlUtilLogger getLogger(String className, boolean readProperties) {
    if (null == IMPL_CLASS) {
	IMPL_CLASS = Loader.getClass(IMPL_CLASS_NAME);
    }
    try {
      return (SlUtilLogger) IMPL_CLASS
        .getMethod("getLogger", new Class[] {String.class, boolean.class})
        .invoke(null, new Object[] {className, new Boolean(readProperties)});
    } catch (Exception e) {
      throw new IllegalStateException(
        "Can't invoke static method getLogger(String, boolean) "
        + "on implementation of CmsLogger: " + e);
    } 
	}

//  add user defined log file
	public abstract void startLoggingTo(String logFileName);

//  end user defined log file
	public abstract void endLoggingTo(String logFileName); 

	public abstract void newFile(String file);

//  trace ---------------------------------------------------------------------

	// entering
	public abstract void entering(String method);

	// exiting
	public abstract void exiting(String method);

	public abstract void exiting(String method, String message);

	// path
	public abstract void path(String message);
  
	public abstract void path(String user, String message);
  
	// debug
	public abstract void debug(String message);

	public abstract void debug(Throwable throwable);

	public abstract void debug(String message, Throwable throwable);
  
	public abstract void debug(String user, String message);
  
	public abstract void debug(String user, String message, Throwable throwable);

	// explicit level
	public abstract void trace(TraceLevel traceLevel, String message);

	public abstract void trace(TraceLevel traceLevel, Throwable throwable);

	public abstract void trace(TraceLevel traceLevel, String message, Throwable throwable);

	public abstract void trace(TraceLevel traceLevel, String user, String message);

	public abstract void trace(TraceLevel traceLevel, String user, String message, Throwable throwable);

	// default trace
	public abstract void trace(String message);
  
	public abstract void trace(Throwable throwable);
  
	public abstract void trace(String message, Throwable throwable);

	public abstract void trace(String user, String message);

	public abstract void trace(String user, String message, Throwable throwable);

// logging --------------------------------------------------------------------

	// info
	public abstract void info(String message);

	public abstract void info(Throwable throwable);

	public abstract void info(String message, Throwable throwable);

	public abstract void info(String user, String message);

	public abstract void info(String user, String message, Throwable throwable);

	// warning
	public abstract void warning(String message);

	public abstract void warning(Throwable throwable);
  
	public abstract void warning(String message, Throwable throwable);
  
	public abstract void warning(String user, String message);
  
	public abstract void warning(String user, String message, Throwable throwable);

	// error
	public abstract void error(String message);
  
	public abstract void error(Throwable throwable);
  
	public abstract void error(String message, Throwable throwable);
  
	public abstract void error(String user, String message);

	public abstract void error(String user, String message, Throwable throwable);

	// fatal
	public abstract void fatal(String message);

	public abstract void fatal(Throwable throwable);
  
	public abstract void fatal(String message, Throwable throwable);

	public abstract void fatal(String user, String message);
  
	public abstract void fatal(String user, String message, Throwable throwable);

	// explicit severity
	public abstract void alert(SlUtilSeverity severity, String message);

	public abstract void alert(SlUtilSeverity severity, Throwable throwable);

	public abstract void alert(SlUtilSeverity severity, String message, Throwable throwable);
  
	public abstract void alert(SlUtilSeverity severity, String user, String message);

	public abstract void alert(SlUtilSeverity severity, String user, String message, Throwable throwable);

	// default logging
	public abstract void log(String message);
  
	public abstract void log(String user, String message);

	// default alert
	public abstract void alert(String message);

	public abstract void alert(Throwable throwable);

	public abstract void alert(String message, Throwable throwable);
  
	public abstract void alert(String user, String message);

	public abstract void alert(String user, String message, Throwable throwable);

}
