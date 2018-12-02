package com.sap.dictionary.database.dbs;

import java.io.*;
import java.sql.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
/**
 * Ueberschrift:   Tests zu jdbpersistency
 * Beschreibung:
 * Copyright:     Copyright (c) 2001
 * Organisation:
 * @author
 * @version 1.0
 */

public class GenExceptionInfo {
  private ExType exType    = null;
  private Throwable action = null;
  private Throwable cause = null;
  private boolean isLogged = false;

  public GenExceptionInfo(Throwable action) {
  	this.action = action;
  }

  public GenExceptionInfo(Throwable action, ExType exType) {
  	this.action = action;
    this.exType = exType;
  }

  public GenExceptionInfo(Throwable action, ExType exType, Throwable cause) {
  	this.action = action;
    this.exType = exType;
    this.cause = cause;
    if (cause != null) {
  		if (cause instanceof IGenException 
  										&& ((IGenException)cause).getInfo().isLogged)
  			isLogged = true;
    }
  }
    
  public GenExceptionInfo(Throwable action, ExType exType, Throwable cause,
  										 		Object msgCode, Object[] args, Category cat,
  										  	int severity,Location loc) {
  	this.action = action;
  	this.exType = exType;
    this.cause = cause;
    String addLogString = "";
    if (cause != null) {
  		if (cause instanceof IGenException 
  										&& ((IGenException)cause).getInfo().isLogged)
  			isLogged = true;
    }
  	if ( (cat != null || loc != null) && !isLogged ) {
  			isLogged = true;
  			String causeMessage = null;
  			if (cause != null) 
  				causeMessage = cause.getMessage();
  			if ( !isEmpty(causeMessage) )
					addLogString = " Caused by: " + cause.getMessage();
  			addLogString += " Stack trace: " + getStackTraceString();
				if (cause instanceof SQLException) {
					SQLException sqlex = (SQLException) cause;
					while (true) {
						SQLException nextsqlex = sqlex.getNextException();
						if (nextsqlex == null)
							break;
						String mess = nextsqlex.getMessage();
						if ( !isEmpty(mess) )
							addLogString += " \n Next Exception: " + nextsqlex.getMessage();
						addLogString += " Stack trace: " + getStackTraceString(nextsqlex);
						sqlex = nextsqlex;
					}
				}
  	} 	
  	if (cat != null) {
  		if (msgCode != null)
  			cat.log(severity,loc,msgCode,args);
  		if (!isEmpty(addLogString))
  			cat.logT(severity,loc,addLogString);
  	}	
  	else if (loc != null) {
  		if (msgCode != null)
  			loc.log(severity,cat,msgCode,args);	
  		if (!isEmpty(addLogString))
  			loc.logT(severity,cat,addLogString);
  	}								  
  }
 
  public void intLog(Category cat, int severity, Location loc) {
    String addLogString = "";  
  	if ( (cat != null || loc != null) && !isLogged ){
		String causeMessage = (cause != null) ? cause.getMessage() : null;
  		if ( !isEmpty(causeMessage) )
				addLogString = " Caused by: " + cause.getMessage();
			addLogString = addLogString + 
  											" Stack trace: " + getStackTraceString();
  		isLogged = true;
  	}
  	if (!isEmpty(addLogString)) {	
	  	if (cat != null) 
  			cat.logT(severity,loc,addLogString);
  		else if (loc != null) 
  			loc.logT(severity,cat,addLogString);
  	}	
  }
  

 	/**
	 * Method getStackTraceString.
	 * @return String
	 */
  public String getStackTraceString() {	
  	String trace = "";
  	try {
  		StringWriter sw = new StringWriter(128);
  		if (cause != null)
  			cause.printStackTrace(new PrintWriter(sw));
  		else
  			action.printStackTrace(new PrintWriter(sw));
  		sw.close();
  		trace = sw.toString();
  		return trace;
  	} catch (IOException ioex) {
  		return trace;
  	}
  }
  
 	/**
	 * Method getStackTraceString.
	 * @return String
	 */
  public static String getStackTraceString(Throwable ex) {	
  	String trace = "";
  	try {
  		StringWriter sw = new StringWriter(128);
  		if (ex != null)
  			ex.printStackTrace(new PrintWriter(sw));
  		sw.close();
  		trace = sw.toString();
  		return trace;
  	} catch (IOException ioex) {
  		return trace;
  	}
  } 

  public ExType getExType() {return exType;}
  
  public Throwable getCause() {
  	return cause;
  }
  
  public boolean isLogged() {
  	return isLogged;
  }
  
  private static boolean isEmpty (String str) {
  	return ( (str == null) || (str.trim().equals("")) ); 
  }

}