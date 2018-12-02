/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.tools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Locale;

import javax.xml.transform.TransformerException;

import com.sap.engine.lib.xml.parser.NestedSAXParseException;
import com.sap.engine.lib.xml.util.NestedException;
import com.sap.exception.IBaseException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * @author Alexander Zubev (alexander.zubev@sap.com)
 */
public class ExceptionManager {
  public static Throwable getOriginalError(Throwable thr) {
    Throwable cause = null;
    if (thr instanceof IBaseException) {
      cause = ((IBaseException) thr).getCause();
    } else if (thr instanceof NestedException) {
      cause = ((NestedException) thr).getCause();
    } else if (thr instanceof NestedSAXParseException) {
      cause = ((NestedSAXParseException) thr).getException();
    } else if (thr instanceof TransformerException) {
      cause = ((TransformerException) thr).getException();
    } else if (thr instanceof RemoteException) {
      cause = ((RemoteException) thr).detail;
    } else if (thr instanceof InvocationTargetException) {
      cause = ((InvocationTargetException) thr).getTargetException();
    } else { //use the standard cause
      cause = thr.getCause();
    }

    if (cause != null) {
      return getOriginalError(cause); 
    } else {
      return thr;
    }
  }
  
  public static String getErrorMessage(Throwable thr) {
    Throwable originalError = getOriginalError(thr);
    if (originalError instanceof UnknownHostException) {
      if (originalError.getMessage() != null) {
        return "Unknown host. Check your proxy settings: " + originalError.getMessage();
      } else {
        return originalError.toString();
      }
    } else if (originalError instanceof ClassCastException) {
      return originalError.toString(); 
    }
    String msg; 
    if (originalError instanceof IBaseException) {
      msg = ((IBaseException) originalError).getLocalizedMessage(new Locale("en", ""));
    } else {
      msg = originalError.getMessage();
    }
    if (msg != null) {
      return msg;
    } else {
      return originalError.toString();
    }
  }
  
  public static String getChainedErrors(Throwable thr) {
    if (thr == null) {
      return null;
    }
    Throwable cause = null;
    if (thr instanceof IBaseException) {
      cause = ((IBaseException) thr).getCause();
    } else if (thr instanceof NestedException) {
      cause = ((NestedException) thr).getCause();
    } else if (thr instanceof NestedSAXParseException) {
      cause = ((NestedSAXParseException) thr).getException();
    } else if (thr instanceof TransformerException) {
      cause = ((TransformerException) thr).getException();
    } else if (thr instanceof RemoteException) {
      cause = ((RemoteException) thr).detail;
    } else if (thr instanceof InvocationTargetException) {
      cause = ((InvocationTargetException) thr).getTargetException();
    } else { //use the standard cause
      cause = thr.getCause();
    }

    String res = thr.toString();   
    if (cause != null) {
      res += "->";
      return res += getChainedErrors(cause); 
    } else {
      return res;
    }
  }
  
  public static void logThrowable(
    int severity,
    Category category,
    Location location,
    String method,
    Throwable throwable) {
    StringWriter writer = new StringWriter();
    PrintWriter wrapper = new PrintWriter(writer);
    throwable.printStackTrace(wrapper);
    
    if (category != null) {
      category.logT(severity, location, method, getChainedErrors(throwable));
    }
    location.logT(severity, method, throwable.toString());
    location.logT(severity, method, writer.toString());
  }

  public static void logThrowable(
    int severity,
    Category category,
    Location location,
    String method,
    String message,
    Throwable throwable) {
      
    StringWriter writer = new StringWriter();
    PrintWriter wrapper = new PrintWriter(writer);
    throwable.printStackTrace(wrapper);
    
    if (category != null) {
      category.logT(severity, location, method, 
        message + " " + getChainedErrors(throwable));
    }
    location.logT(severity, method, throwable.toString());
    location.logT(severity, method, writer.toString());
  }

  public static void traceThrowable(int severity, Location location,
    String method, Throwable throwable) {
      
    StringWriter writer = new StringWriter();
    PrintWriter wrapper = new PrintWriter(writer);
    throwable.printStackTrace(wrapper);

    location.logT(severity, method, throwable.toString());
    location.logT(severity, method, writer.toString());
  }
  
}
