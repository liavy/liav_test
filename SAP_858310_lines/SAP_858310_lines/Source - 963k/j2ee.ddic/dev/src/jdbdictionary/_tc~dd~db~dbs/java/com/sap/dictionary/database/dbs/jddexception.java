package com.sap.dictionary.database.dbs;

import java.io.*;
import java.sql.*;
import java.lang.reflect.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

/**
 * @author d019347
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class JddException extends Exception
																 implements IGenException {
	transient GenExceptionInfo info = null;
	private static final ExType    EX_TYPE_NULL = null;
	private static final Throwable CAUSE_NULL   = null;
	private static final Object[]  ARGS_NULL    = null;

	public JddException() {
	  info = new GenExceptionInfo(this, EX_TYPE_NULL, CAUSE_NULL);
	}

	public JddException(String message) {
	  super(message);
	  info = new GenExceptionInfo(this, EX_TYPE_NULL, CAUSE_NULL);
	}

	public JddException(ExType exType,String message) {
		super(message);
		info = new GenExceptionInfo(this,exType,CAUSE_NULL);
	}

	public JddException(ExType exType,Throwable cause) {
	  info = new GenExceptionInfo(this,exType,cause);
	}

	public JddException(ExType exType,String message,Throwable cause) {
		super(message);
		info = new GenExceptionInfo(this, exType, cause);
	}
  
	public JddException(Object msgCode,Category cat,int severity,
			Location loc) {
		this(EX_TYPE_NULL,CAUSE_NULL,msgCode,ARGS_NULL,cat,severity,loc);
	}
  
	public JddException(Object msgCode,Object[] args,
											Category cat,int severity,Location loc) {
		this(EX_TYPE_NULL,CAUSE_NULL,msgCode,args,cat,severity,loc);
	}
  
	public JddException(Throwable cause,Object msgCode,
											Category cat,int severity,Location loc) {
		this(EX_TYPE_NULL,cause,msgCode,ARGS_NULL,cat,severity,loc);
	}
  
	public JddException(Throwable cause,Object msgCode,Object[] args,
											Category cat,int severity,Location loc) {
		this(EX_TYPE_NULL,cause,msgCode,args,cat,severity,loc);
	}
  
	public JddException(ExType exType,Object msgCode,
											Category cat,int severity,Location loc) {
		this(exType,CAUSE_NULL,msgCode,ARGS_NULL,cat,severity,loc);
	}
  
	public JddException(ExType exType,Object msgCode,Object[] args,
											Category cat,int severity,Location loc) {
		this(exType,CAUSE_NULL,msgCode,args,cat,severity,loc);
	}
  
	public JddException(ExType exType,Throwable cause,Object msgCode,
											Category cat,int severity,Location loc) {
		this(exType,cause,msgCode,null,cat,severity,loc);
	}
  
	public JddException(ExType exType, Throwable cause, Object msgCode,
											 Object[] args, Category cat, int severity,
												 Location loc) {
		super( (msgCode == null ? "" : DbMsgHandler.get(msgCode,args)) + 
								(cause == null ? "" : " Caused by: " + cause.getMessage()));
		info = new GenExceptionInfo(this, exType, cause, msgCode, args, cat,
				severity,loc);							  
	}

	public void printStackTrace() {
		if (info.getCause() != null)
			info.getCause().printStackTrace();
		else
			super.printStackTrace();
	}

	public static JddException createInstance(Throwable cause) {
		if (cause instanceof InvocationTargetException) {
			cause = ((InvocationTargetException) cause).getTargetException();
		}
  	 
		if (cause instanceof JddException)
			 return (JddException) cause;
		else if (cause instanceof SQLException)
			return new JddException(ExType.SQL_ERROR,cause.getMessage(),cause);  
		else
			return new JddException(ExType.OTHER,cause.getMessage(),cause);
	}
  
	public static JddException createInstance(Throwable cause,
														 Category cat,int severity, Location loc) {
		if (cause instanceof InvocationTargetException)
			cause = ((InvocationTargetException) cause).getTargetException(); 
		if (cause instanceof IGenException) {
			IGenException iGenCause = (IGenException)cause;
			iGenCause.getInfo().intLog(cat,severity,loc);  
		}
		if (cause instanceof JddException) {
			return (JddException) cause;
		}
		else if (cause instanceof SQLException)
			return new JddException(ExType.SQL_ERROR,cause,null,null,cat,
															severity,loc);  
		else
			return new JddException(ExType.OTHER,cause,null,null,cat,
															severity,loc);
	}
  
	public static void log(Throwable cause, Category cat,
									int severity, Location loc) {
		createInstance(cause,cat,severity,loc);
	}
  
	public static void log(Throwable cause, Object msgCode, Category cat,
									int severity, Location loc) {
		log(cause, msgCode, null, cat, severity, loc);
	}
  
	public static void log(Throwable cause, Object msgCode, Object [] args,
									 Category cat, int severity, Location loc) {
		cat.log(severity,loc, msgCode, args);
		createInstance(cause,cat,severity,loc);
	}
  
	public String getStackTraceString() {
		return info.getStackTraceString();	
	} 
	
	public ExType getExType() {
		return info.getExType();
	}
  
	public GenExceptionInfo getInfo() {
		return info;
	}
  
	/**
	 *  Get the exception's cause
	 *  @return the exception's cause
	 **/
	public Throwable getCause() {
		if (info == null)
			return null;
		return info.getCause();
	}	  



}
