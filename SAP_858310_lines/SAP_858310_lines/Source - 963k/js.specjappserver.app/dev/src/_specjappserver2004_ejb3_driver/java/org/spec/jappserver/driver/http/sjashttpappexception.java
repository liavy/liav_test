/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *  History:
 *  Date          ID        Description
 *  ------------  --------  ----------------------------------------------
 *  May 13, 2003  Tom Daly  Creation date
 *
 */


package org.spec.jappserver.driver.http;
/**
 * Class SJASHttpAppException
 *
 * Thrown when a dealer thread gets an application level error or specifically 
 * when it recognises an error message in the html returned by the web application
 * 
 * @author Tom Daly
 * @version %I%, %G%
 */

public class SJASHttpAppException extends SJASHttpException {

	public SJASHttpAppException() {}

	public SJASHttpAppException(String s) {

		super(s);
	}
}
