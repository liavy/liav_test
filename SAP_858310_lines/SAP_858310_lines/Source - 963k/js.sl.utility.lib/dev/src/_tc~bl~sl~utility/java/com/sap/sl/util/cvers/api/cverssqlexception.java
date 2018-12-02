package com.sap.sl.util.cvers.api;

/**
 *  Central exception for errors during DB access of CVERS
 *
 *@author     md
 *
 *@version    1.0
 */

public class CVersSQLException extends CVersAccessException {	//$JL-SER$

	public CVersSQLException(String message) {
	  super(message);
	}

	public CVersSQLException(String message, Exception exc) {
	  super(message, exc);
	}

}
