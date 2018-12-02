package com.sap.sl.util.logging.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.sap.sl.util.logging.api.SlUtilLogger;
import com.sap.sl.util.logging.api.SlUtilSeverity;
import com.sap.sl.util.logging.api.TraceLevel;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.FileLog;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.PropertiesConfigurator;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.TraceFormatter;

/**
 * SAP Change Management Server
 *
 * @author  hen
 * @version 6
 * modified 02.11.2002 - hen
 * 			12.03.2003 - hen - send all messages to System.out.println & startLoggingTo/endLoggingTo
 * 			24.06.2003 - hen - add method exiting(String method, String message)
 *      25.06.2003 - md - copied to sl.util
 *      04.07.2003 - cg - separation of api and implementation
 */

public class SlUtilLoggerImpl extends SlUtilLogger {

	private Location loc = null;
	private Category cat = null;
	GregorianCalendar cale;
	boolean fileLog = false;
	boolean println = false;		// true: send to System.out.println
	private final Map fileLogMap = new HashMap();
	private final static TraceFormatter LOG_FILE_FORMATTER = new TraceFormatter("%24d %7s: %m");

//  create a logger and read configuration from properties file
	public static SlUtilLogger getLogger(String className, boolean readProperties) {
		if(readProperties) {
			PropertiesConfigurator pC;
			File cfgfile = new File("saplogging.config");
			pC = new PropertiesConfigurator(cfgfile);
			pC.configure();
		} else {
			File cfgfile = new File("slutil.debug");
			if (cfgfile != null) {
				if (cfgfile.exists()) {
					Location loc = Location.getLocation("sl");
					Category cat = Category.getCategory("/SLLogs");
					loc.setEffectiveSeverity(100);
					cat.setEffectiveSeverity(100);
				}
			}
		}

    if (className == null) {
      return new SlUtilLoggerImpl();
    } else {
      return new SlUtilLoggerImpl(className);
    }
	}

  private SlUtilLoggerImpl() {
    loc = Location.getLocation("sl");
    cat = Category.getCategory("/SLLogs");
    fileLog = true;
  }

	private SlUtilLoggerImpl(String className) {
		String application = null;
		loc = Location.getLocation(className);
		if (className.indexOf("com.sap.cms.pcs") != -1) {
			application = "/Applications/CMS/PCS";
		}
		if (className.indexOf("com.sap.cms.tcs") != -1) {
			application = "/Applications/CMS/TCS";
		}
		if (className.indexOf("com.sap.sdm") != -1) {
			application = "/Applications/CMS/SDM";
		}
		if (className.indexOf("com.sap.sl.util") != -1) {
			application = "/Applications/SL/UTIL";
		}
		if (application == null) {
			application = "/Applications/SL";
		}
		cat = Category.getCategory(application);
		cale = new GregorianCalendar();
		loc.pathT(cale.get(Calendar.HOUR_OF_DAY) + ":"
		    	+ cale.get(Calendar.MINUTE) + ":"
		    	+ cale.get(Calendar.SECOND) + " "
				+ application
		    	+ " entering class " + className);
	}

//  add user defined log file
	public void startLoggingTo(String logFileName) {
		if (logFileName == null) {
			return;
		}		
		if (fileLogMap.containsKey(logFileName)) {	// logger already exists
			loc.addLog((FileLog) fileLogMap.get(logFileName));	//$JL-LOG_CONFIG$
			return;
		}		
		FileLog additionalFileLog = new FileLog(logFileName, LOG_FILE_FORMATTER, true); // append if file already exists
		loc.addLog(additionalFileLog);		//$JL-LOG_CONFIG$
		fileLogMap.put(logFileName, additionalFileLog);
	}

//  end user defined log file
	public void endLoggingTo(String logFileName) {
		if (logFileName == null) {
			return;
		}
	    FileLog additionalFileLog = (FileLog) fileLogMap.get(logFileName);
		if (additionalFileLog == null) {
			return;
		}
		loc.removeLog(additionalFileLog);	//$JL-LOG_CONFIG$
	}

	public void newFile(String file) {
		if (fileLog) {
			FileLog fileLog = new FileLog(file);
			fileLog.setFormatter(LOG_FILE_FORMATTER);
			cat.removeLogs();							//$JL-LOG_CONFIG$
   	   		cat.addPrivateLog(fileLog);					//$JL-LOG_CONFIG$
			cat.setEffectiveSeverity(Severity.INFO);	//$JL-LOG_CONFIG$
		}
	}

	// get logging category
    public Category getCategory() {
    	return cat;
	}

//  trace ---------------------------------------------------------------------

	// entering
	public void entering(String method) {
		if (loc.getEffectiveSeverity(loc) <= 200) {
			cale = new GregorianCalendar();
			slutiltrace(TraceLevel.PATH, "", 
			    						"--> entering "
			    						+ method, null);
		}
	}

	// exiting
	public void exiting(String method) {
		slutiltrace(TraceLevel.PATH, "", "<--- exiting " + method, null);
	}
	public void exiting(String method, String message) {
		slutiltrace(TraceLevel.PATH, "", "<--- exiting " + method + " (" + message + ")", null);
	}

	// path
	public void path(String message) {
		slutiltrace(TraceLevel.PATH, "", message, null);
	}
	public void path(String user, String message) {
		slutiltrace(TraceLevel.PATH, user, message, null);
	}

	// debug
	public void debug(String message) {
		slutiltrace(TraceLevel.DEBUG, "", message, null);
	}
	public void debug(Throwable throwable) {
		slutiltrace(TraceLevel.DEBUG, "", "", throwable);
	}
	public void debug(String message, Throwable throwable) {
		slutiltrace(TraceLevel.DEBUG, "", message, throwable);
	}
	public void debug(String user, String message) {
		slutiltrace(TraceLevel.DEBUG, user, message, null);
	}
	public void debug(String user, String message, Throwable throwable) {
		slutiltrace(TraceLevel.DEBUG, user, message, throwable);
	}

	// explicit level
	public void trace(TraceLevel traceLevel, String message) {
		slutiltrace(traceLevel, "", message, null);
	}
	public void trace(TraceLevel traceLevel, Throwable throwable) {
		slutiltrace(traceLevel, "", "", throwable);
	}
	public void trace(TraceLevel traceLevel, String message, Throwable throwable) {
		slutiltrace(traceLevel, "", message, throwable);
	}
	public void trace(TraceLevel traceLevel, String user, String message) {
		slutiltrace(traceLevel, user, message, null);
	}
	public void trace(TraceLevel traceLevel, String user, String message, Throwable throwable) {
		slutiltrace(traceLevel, user, message, throwable);
	}

	// default trace
	public void trace(String message) {
		slutiltrace(TraceLevel.DEBUG, "", message, null);
	}
	public void trace(Throwable throwable) {
		slutiltrace(TraceLevel.DEBUG, "", "", throwable);
	}
	public void trace(String message, Throwable throwable) {
		slutiltrace(TraceLevel.DEBUG, "", message, throwable);
	}
	public void trace(String user, String message) {
		slutiltrace(TraceLevel.DEBUG, user, message, null);
	}
	public void trace(String user, String message, Throwable throwable) {
		slutiltrace(TraceLevel.DEBUG, user, message, throwable);
	}

	private void slutiltrace(TraceLevel traceLevel, String user, String message, Throwable throwable) {
		if (loc.getEffectiveSeverity(loc) <= 200 || println) {
			String msg = "slutiltrace, ";
			if (user.toString().equalsIgnoreCase("")) {
				msg = msg + message;
			} else {
				msg = msg + "User='" + user + "' Msg=" + message;
			}
			switch (traceLevel.intValue()) {
				case 1 :
					if (throwable != null) {
						if (msg.toString().equalsIgnoreCase("")) {
							msg = "Exc=" + throwable.toString();
						} else {
							msg = msg + " Exc=" + throwable.toString();
						}
					}
					if (println) {
						System.out.println("SL Path >" + msg);		//$JL-SYS_OUT_ERR$				
					}
					loc.pathT(msg);
					break;
				case 2 :
					if (loc.getEffectiveSeverity(loc) <= 100 || println) {
 						if (throwable != null) {
 							Exception e = (Exception) throwable;
 							ByteArrayOutputStream stream = new ByteArrayOutputStream();
 							PrintWriter printWriter = new PrintWriter(stream);
 						    e.printStackTrace(printWriter);
   							printWriter.flush();
 						    String stringException = stream.toString();
 						    printWriter.close();
							if (msg.toString().equalsIgnoreCase("")) {
								msg = "Exc=" + stringException;
							} else {
								msg = msg + " Exc=" + stringException;
							}
 						}
						if (println) {
							System.out.println("SL Debug>" + msg);	//$JL-SYS_OUT_ERR$					
						}
						loc.debugT("  " + msg);
					}
					break;
				default :
					if (println) {
						System.out.println("SL Path>" + msg);		//$JL-SYS_OUT_ERR$				
					}
					loc.pathT(msg);
					break;
			}
		}
		if (throwable != null) {
	 		cat.logThrowableT(Severity.ERROR, loc, message, throwable);
	 	}
	}

// logging --------------------------------------------------------------------

	// info
	public void info(String message) {
		slutilalert(SlUtilSeverity.INFO, "", message, null);
	}
	public void info(Throwable throwable) {
		slutilalert(SlUtilSeverity.INFO, "", "", throwable);
	}
	public void info(String message, Throwable throwable) {
		slutilalert(SlUtilSeverity.INFO, "", message, throwable);
	}
	public void info(String user, String message) {
		slutilalert(SlUtilSeverity.INFO, user, message, null);
	}
	public void info(String user, String message, Throwable throwable) {
		slutilalert(SlUtilSeverity.INFO, user, message, throwable);
	}

	// warning
	public void warning(String message) {
		slutilalert(SlUtilSeverity.WARNING, "", message, null);
	}
	public void warning(Throwable throwable) {
		slutilalert(SlUtilSeverity.WARNING, "", "", throwable);
	}
	public void warning(String message, Throwable throwable) {
		slutilalert(SlUtilSeverity.WARNING, "", message, throwable);
	}
	public void warning(String user, String message) {
		slutilalert(SlUtilSeverity.WARNING, user, message, null);
	}
	public void warning(String user, String message, Throwable throwable) {
		slutilalert(SlUtilSeverity.WARNING, user, message, throwable);
	}

	// error
	public void error(String message) {
		slutilalert(SlUtilSeverity.ERROR, "", message, null);
	}
	public void error(Throwable throwable) {
		slutilalert(SlUtilSeverity.ERROR, "", "", throwable);
	}
	public void error(String message, Throwable throwable) {
		slutilalert(SlUtilSeverity.ERROR, "", message, throwable);
	}
	public void error(String user, String message) {
		slutilalert(SlUtilSeverity.ERROR, user, message, null);
	}
	public void error(String user, String message, Throwable throwable) {
		slutilalert(SlUtilSeverity.ERROR, user, message, throwable);
	}

	// fatal
	public void fatal(String message) {
		slutilalert(SlUtilSeverity.FATAL, "", message, null);
	}
	public void fatal(Throwable throwable) {
		slutilalert(SlUtilSeverity.FATAL, "", "", throwable);
	}
	public void fatal(String message, Throwable throwable) {
		slutilalert(SlUtilSeverity.FATAL, "", message, throwable);
	}
	public void fatal(String user, String message) {
		slutilalert(SlUtilSeverity.FATAL, user, message, null);
	}
	public void fatal(String user, String message, Throwable throwable) {
		slutilalert(SlUtilSeverity.FATAL, user, message, throwable);
	}

	// explicit severity
	public void alert(SlUtilSeverity severity, String message) {
		slutilalert(severity, "", message, null);
	}
	public void alert(SlUtilSeverity severity, Throwable throwable) {
		slutilalert(severity, "", "", throwable);
	}
	public void alert(SlUtilSeverity severity, String message, Throwable throwable) {
		slutilalert(severity, "", message, throwable);
	}
	public void alert(SlUtilSeverity severity, String user, String message) {
		slutilalert(severity, user, message, null);
	}
	public void alert(SlUtilSeverity severity, String user, String message, Throwable throwable) {
		slutilalert(severity, user, message, throwable);
	}

	// default logging
	public void log(String message) {
		cat.infoT(loc, message);
	}
	public void log(String user, String message) {
  		cat.infoT(loc, "User=" + user + " Msg=" + message);
	}

	// default alert
	public void alert(String message) {
		slutilalert(SlUtilSeverity.ERROR, "", message, null);
	}
	public void alert(Throwable throwable) {
		slutilalert(SlUtilSeverity.ERROR, "", "", throwable);
	}
	public void alert(String message, Throwable throwable) {
		slutilalert(SlUtilSeverity.ERROR, "", message, throwable);
	}
	public void alert(String user, String message) {
		slutilalert(SlUtilSeverity.ERROR, user, message, null);
	}
	public void alert(String user, String message, Throwable throwable) {
		slutilalert(SlUtilSeverity.ERROR, user, message, throwable);
	}

	private void slutilalert(SlUtilSeverity severity, String user, String message, Throwable throwable) {
		String msg;
		if (user.toString() == "") {
			msg = message;
		} else {
			msg = "User=" + user + " Msg=" + message;
		}
		if (throwable != null) {
			if (msg.toString().equalsIgnoreCase("")) {
				msg = "Exc=" + throwable.toString();
			} else {
				msg = msg + " Exc=" + throwable.toString();
			}
		}
		switch (severity.intValue()) {
			case 1 :
				if (println) {
					System.out.println("SL Fatal>" + msg);		//$JL-SYS_OUT_ERR$				
				}
				cat.fatalT(loc, msg);
				break;
			case 2 :
				if (println) {
					System.out.println("SL Error>" + msg);		//$JL-SYS_OUT_ERR$				
				}
				cat.errorT(loc, msg);
				break;
			case 3 :
				if (println) {
					System.out.println("SL Warning>" + msg);	//$JL-SYS_OUT_ERR$					
				}
				cat.warningT(loc, msg);
				break;
			case 4 :
				if (println) {
					System.out.println("SL Info>" + msg);		//$JL-SYS_OUT_ERR$				
				}
				cat.infoT(loc, msg);
				break;
			default :
				if (println) {
					System.out.println("SL Error>" + msg);		//$JL-SYS_OUT_ERR$				
				}
				cat.errorT(loc, msg);
				break;
		}
		if (throwable != null) {
			slutiltrace(TraceLevel.DEBUG, "", "stack trace:", throwable);
		}
	}
}