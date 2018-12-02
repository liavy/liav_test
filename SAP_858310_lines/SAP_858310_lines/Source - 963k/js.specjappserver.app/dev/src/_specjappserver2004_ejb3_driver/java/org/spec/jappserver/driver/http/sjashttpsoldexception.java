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
 * Class SJASHttpSoldException
 *
 * Thrown when a dealer thread tries to sell a car that has already been sold 
 * by that dealership
 * 
 * @author Tom Daly
 * @version %I%, %G%
 */

public class SJASHttpSoldException extends SJASHttpException {

	public SJASHttpSoldException() {}

	public SJASHttpSoldException(String s) {

		super(s);
	}
}
