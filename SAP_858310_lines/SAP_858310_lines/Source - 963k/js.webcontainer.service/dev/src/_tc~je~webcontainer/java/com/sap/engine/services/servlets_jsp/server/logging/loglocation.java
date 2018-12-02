/*
 * Copyright (c) 2000-2007 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.logging;

import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.LogRecord;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

public class LogLocation {
  private Location location = null;

  public LogLocation(String locationName) {
    location = Location.getLocation(locationName);
  }

  public Location getLocation() {
    return location;
  }

  //new logging methods related to messegaID adoption
  //---------------------------- without message arguments -------------------------------------------

  /**
   * Traces an error message trough a certain location using the messageID concept.
   * 
   * @param msgID messageID to be assigned to this message. MessageID format is [prefix].[range][number] (e.g. ASJ.web.000135
   * @param msg the message text itself
   * @param dcName deployment component name to be written in the trace entry. If you do not want to specify the DC name 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own DC name.
   * @param csnComponent CSN Component to be written in the trace entry. If you do not want to specify the CSN component 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own CSN component.
   * @return String - the logId of the LogRecord object encapsulating the message that has been written. null if no message has been written.
   */
  public String traceError(String msgID, String msg, String dcName, String csnComponent) {
    // SimpleLogger.trace(int severity, Location location, String dcName, String csnComponent, String messageID, String message, Exception exc, Object... args)
    LogRecord logRecord = SimpleLogger.trace(Severity.ERROR, location, dcName, csnComponent, msgID, msg, null, new Object[0]);
    String logId = null;
    if (logRecord != null) {
      logId = logRecord.getId().toString();
    }

    return logId;
  }

  /**
   * Traces an error message trough a certain location using the messageID concept. It is especially meant to format and write exception stack traces.
   * 
   * @param msgID messageID to be assigned to this message. MessageID format is [prefix].[range][number] (e.g. ASJ.web.000135
   * @param msg the message text itself
   * @param t if not null, the exception stack trace is formatted and appended at the end of the trace record as a message parameter
   * @param dcName deployment component name to be written in the trace entry. If you do not want to specify the DC name 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own DC name.
   * @param csnComponent CSN Component to be written in the trace entry. If you do not want to specify the CSN component 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own CSN component.
   * @return String - the logId of the LogRecord object encapsulating the message that has been written. null if no message has been written.
   */
  public String traceError(String msgID, String msg, Throwable t, String dcName, String csnComponent) {
    // SimpleLogger.trace(int severity, Location location, String dcName, String csnComponent, String messageID, String message, Exception exc, Object... args)
    LogRecord logRecord = SimpleLogger.trace(Severity.ERROR, location, dcName, csnComponent, msgID, msg, t, new Object[0]);
    String logId = null;
    if (logRecord != null) {
      logId = logRecord.getId().toString();
    }

    return logId;
  }

  /**
   * Traces a warning message trough a certain location using the messageID concept.
   * 
   * @param msgID messageID to be assigned to this message. MessageID format is [prefix].[range][number] (e.g. ASJ.web.000135
   * @param msg the message text itself
   * @param dcName deployment component name to be written in the trace entry. If you do not want to specify the DC name 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own DC name.
   * @param csnComponent CSN Component to be written in the trace entry. If you do not want to specify the CSN component 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own CSN component.
   * @return String - the logId of the LogRecord object encapsulating the message that has been written. null if no message has been written.
   */
  public String traceWarning(String msgID, String msg, String dcName, String csnComponent) {
    // SimpleLogger.trace(int severity, Location location, String dcName, String csnComponent, String messageID, String message, Exception exc, Object... args)
    LogRecord logRecord = SimpleLogger.trace(Severity.WARNING, location, dcName, csnComponent, msgID, msg, null, new Object[0]);
    String logId = null;
    if (logRecord != null) {
      logId = logRecord.getId().toString();
    }

    return logId;
  }

  /**
   * Traces a warning message trough a certain location using the messageID concept. It is especially meant to format and write exception stack traces.
   * 
   * @param msgID messageID to be assigned to this message. MessageID format is [prefix].[range][number] (e.g. ASJ.web.000135
   * @param msg the message text itself
   * @param t if not null, the exception stack trace is formatted and appended at the end of the trace record as a message parameter
   * @param dcName deployment component name to be written in the trace entry. If you do not want to specify the DC name 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own DC name.
   * @param csnComponent CSN Component to be written in the trace entry. If you do not want to specify the CSN component 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own CSN component.
   * @return String - the logId of the LogRecord object encapsulating the message that has been written. null if no message has been written.
   */
  public String traceWarning(String msgID, String msg, Throwable t, String dcName, String csnComponent) {
    // SimpleLogger.trace(int severity, Location location, String dcName, String csnComponent, String messageID, String message, Exception exc, Object... args)
    LogRecord logRecord = SimpleLogger.trace(Severity.WARNING, location, dcName, csnComponent, msgID, msg, t, new Object[0]);
    String logId = null;
    if (logRecord != null) {
      logId = logRecord.getId().toString();
    }

    return logId;
  }

  // --------------------------------- with message arguments -------------------------------------------------

  /**
   * Traces an error message (with arguments) trough a certain location using the messageID concept.
   * 
   * @param msgID messageID to be assigned to this message. MessageID format is [prefix].[range][number] (e.g. ASJ.web.000135
   * @param msg the message text itself
   * @param args values to be placed in the text message. Each of the message place-holders {0}, {1} will be replaced with args[0], args[1], etc.
   * @param dcName deployment component name to be written in the trace entry. If you do not want to specify the DC name 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own DC name.
   * @param csnComponent CSN Component to be written in the trace entry. If you do not want to specify the CSN component 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own CSN component.
   * @return String - the logId of the LogRecord object encapsulating the message that has been written. null if no message has been written.
   */
  public String traceError(String msgID, String msg, Object[] args, String dcName, String csnComponent) {
    // SimpleLogger.trace(int severity, Location location, String dcName, String csnComponent, String messageID, String message, Exception exc, Object... args)
    LogRecord logRecord = SimpleLogger.trace(Severity.ERROR, location, dcName, csnComponent, msgID, msg, null, args);
    String logId = null;
    if (logRecord != null) {
      logId = logRecord.getId().toString();
    }

    return logId;
  }

  /**
   * Traces an error message (with arguments) trough a certain location using the messageID concept. It is especially meant to format and write exception stack traces.
   * 
   * @param msgID messageID to be assigned to this message. MessageID format is [prefix].[range][number] (e.g. ASJ.web.000135
   * @param msg the message text itself
   * @param args values to be placed in the text message. Each of the message place-holders {0}, {1} will be replaced with args[0], args[1], etc.
   * @param t if not null, the exception stack trace is formatted and appended at the end of the trace record as a message parameter
   * @param dcName deployment component name to be written in the trace entry. If you do not want to specify the DC name 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own DC name.
   * @param csnComponent CSN Component to be written in the trace entry. If you do not want to specify the CSN component 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own CSN component.
   * @return String - the logId of the LogRecord object encapsulating the message that has been written. null if no message has been written.
   */
  public String traceError(String msgID, String msg, Object[] args, Throwable t, String dcName, String csnComponent) {
    // SimpleLogger.trace(int severity, Location location, String dcName, String csnComponent, String messageID, String message, Exception exc, Object... args)
    LogRecord logRecord = SimpleLogger.trace(Severity.ERROR, location, dcName, csnComponent, msgID, msg, t, args);
    String logId = null;
    if (logRecord != null) {
      logId = logRecord.getId().toString();
    }

    return logId;
  }

  /**
   * Traces a warning message (with arguments) trough a certain location using the messageID concept.
   * 
   * @param msgID messageID to be assigned to this message. MessageID format is [prefix].[range][number] (e.g. ASJ.web.000135
   * @param msg the message text itself
   * @param args values to be placed in the text message. Each of the message place-holders {0}, {1} will be replaced with args[0], args[1], etc.
   * @param dcName deployment component name to be written in the trace entry. If you do not want to specify the DC name 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own DC name.
   * @param csnComponent CSN Component to be written in the trace entry. If you do not want to specify the CSN component 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own CSN component.
   * @return String - the logId of the LogRecord object encapsulating the message that has been written. null if no message has been written.
   */
  public String traceWarning(String msgID, String msg, Object[] args, String dcName, String csnComponent) {
    // SimpleLogger.trace(int severity, Location location, String dcName, String csnComponent, String messageID, String message, Exception exc, Object... args)
    LogRecord logRecord = SimpleLogger.trace(Severity.WARNING, location, dcName, csnComponent, msgID, msg, null, args);
    String logId = null;
    if (logRecord != null) {
      logId = logRecord.getId().toString();
    }

    return logId;
  }

  /**
   * Traces an error message (with arguments) trough a certain location using the messageID concept. It is especially meant to format and write exception stack traces.
   * 
   * @param msgID messageID to be assigned to this message. MessageID format is [prefix].[range][number] (e.g. ASJ.web.000135
   * @param msg the message text itself
   * @param args values to be placed in the text message. Each of the message place-holders {0}, {1} will be replaced with args[0], args[1], etc.
   * @param t if not null, the exception stack trace is formatted and appended at the end of the trace record as a message parameter
   * @param dcName deployment component name to be written in the trace entry. If you do not want to specify the DC name 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own DC name.
   * @param csnComponent CSN Component to be written in the trace entry. If you do not want to specify the CSN component 
   * you must use <code>NULL</code> thus the logging infrastructure will place in the log record your own CSN component.
   * @return String - the logId of the LogRecord object encapsulating the message that has been written. null if no message has been written.
   */
  public String traceWarning(String msgID, String msg, Object[] args, Throwable t, String dcName, String csnComponent) {
    // SimpleLogger.trace(int severity, Location location, String dcName, String csnComponent, String messageID, String message, Exception exc, Object... args)
    LogRecord logRecord = SimpleLogger.trace(Severity.WARNING, location, dcName, csnComponent, msgID, msg, t, args);
    String logId = null;
    if (logRecord != null) {
      logId = logRecord.getId().toString();
    }

    return logId;
  }

  //Old logging methods

  /**
   * Used for tracing messages with severity - the current severity of the location.
   * 
   * @param msg trace message
   * @param webApp web application name is not passed. Sync with other methods. //TODO delete
   */
  public void trace(String msg, String webApp) {
    if (location.beDebug() || location.bePath()) {
      location.pathT(LogContext.getExceptionStackTrace(new Exception(msg)));
    } else {
      location.infoT(msg);
    }
  }

  /**
   * Used for tracing messages with severity INFO.
   * 
   * @param msg trace message
   * @param webApp web application name is not passed. Sync with other methods. //TODO delete
   */
  public void traceInfo(String msg, String webApp) {
    location.infoT(msg);
  }

  /**
   * Used for tracing messages with severity DEBUG.
   * 
   * @param msg trace message
   * @param webApp web application name is not passed. Sync with other methods. //TODO delete
   */
  public void traceDebug(String msg, String webApp) {
    location.debugT(msg);
  }

  /**
   * Used for info messages with exceptions.
   *
   * @param message a message to trace
   * @param t a <code>Throwable</code> to trace
   * @param webApp web application name is not passed. Sync with other methods //TODO delete
   * @return <code>null</code> if according to the current severity the message is not
   * logged, otherwise the log record ID
   */
  public String traceInfo(String msg, Throwable t, String webApp) {
    if (location.beInfo()) {
      LogRecord logRecord = location.infoT(msg + " The exception is: " + LogContext.getExceptionStackTrace(t));
      return (logRecord != null) ? String.valueOf(logRecord.getId()) : null;
    } else {
      return null;
    }
  }

  /**
   * Used for debug messages with exceptions.
   * 
   * @param msg trace message.
   * @param t Throwable to be traced.
   * @param webApp web application name is not passed. Sync with other methods. //TODO delete
   * @return <code>null</code> if according to the current severity the message is not logged, otherwise the log record ID
   */
  public String traceDebug(String msg, Throwable t, String webApp) {
    if (location.beDebug()) {
      LogRecord logRecord = location.debugT(msg + " The exception is: " + LogContext.getExceptionStackTrace(t));
      return (logRecord != null) ? String.valueOf(logRecord.getId()) : null;
    } else {
      return null;
    }
  }

  /**
   * Used for tracing messages with severity PATH.
   * 
   * @param msg trace message
   * @param webApp web application name is not passed. Sync with other methods. //TODO delete
   */
  public void tracePath(String msg, String webApp) {
    location.pathT(msg);
  }
}