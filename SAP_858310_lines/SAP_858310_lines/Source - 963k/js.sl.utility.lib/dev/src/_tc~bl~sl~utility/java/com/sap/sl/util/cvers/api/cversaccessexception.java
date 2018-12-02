package com.sap.sl.util.cvers.api;

/**
 *  Central exception for errors during DB access of CVERS
 *
 *@author     md
 *
 *@version    1.0
 */

public class CVersAccessException extends Exception {	//$JL-SER$

  public CVersAccessException() {
  	super();
  }

  public CVersAccessException(String message) {
	super(message);
  }

  public CVersAccessException(String message, Exception exc) {
	super(message, exc);
  }

  public CVersAccessException(String message, Throwable exc) {
	super(message, exc);
  }

}