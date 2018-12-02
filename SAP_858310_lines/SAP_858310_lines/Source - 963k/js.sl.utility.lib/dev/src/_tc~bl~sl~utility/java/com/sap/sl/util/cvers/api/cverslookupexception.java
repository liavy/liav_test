package com.sap.sl.util.cvers.api;

/**
 *  Central exception for errors during loopup of DB connect
 *
 *@author     md
 *
 *@version    1.0
 */

public class CVersLookupException extends CVersAccessException {	//$JL-SER$

  public CVersLookupException(String message) {
    super(message);
  }

}