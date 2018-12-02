package com.sap.sl.util.cvers.api;

/**
 *  Central exception for errors during DB access of CVERS
 *
 *@author     md
 *
 *@version    1.0
 */

public class CVersFinderException extends CVersAccessException {	//$JL-SER$

  public CVersFinderException(String message) {
	super(message);
  }

}