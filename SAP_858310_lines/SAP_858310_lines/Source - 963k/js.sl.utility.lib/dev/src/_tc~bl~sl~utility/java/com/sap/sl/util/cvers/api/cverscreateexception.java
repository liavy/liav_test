package com.sap.sl.util.cvers.api;

/**
 *  Central exception for errors during DB access of CVERS
 *
 *@author     md
 *
 *@version    1.0
 */

public class CVersCreateException extends CVersAccessException {	//$JL-SER$

  public CVersCreateException(String message) {
    super(message);
  }

}