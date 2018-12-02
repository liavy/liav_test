/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ---------------------------------------------------------------
 *  2001        Shanti Subrmanyam, SUN    Created
 *  2002/05/03  Matt Hogstrom, IBM        Modified random method call to nextInt from (y) to (y+1) 
 *  2002/04/12  Matt Hogstrom, IBM        Conversion from ECperf 1.1 to SPECjAppServer2001
 *  2002/07/10  Russell R., BEA           Conversion from SPECjAppServer2001 to 
 *                                        SPECjAppServer2002 (EJB2.0).
 *
 * $Id: RandNum.java,v 1.6 2004/02/17 17:15:28 skounev Exp $
 */

package org.spec.jappserver.driver;
import java.util.Random;

/**
 * This file generates random numbers for the RTE programs
 * Adapted from the TPCC RTE program which is proprietary to 
 * Sun Microsystems Inc.
 * 
 * @author Shanti Subramanyam
 */
public class RandNum {
	private Random r;
	private static String alpha = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";


/*
 * Constructor
 * Initialize random number generators 
 */
	RandNum(long seed)
	{
		r = new Random(seed);
	}

	RandNum() {
		r = new Random();
	}


/**
 * Select a random number uniformly distributed between x and y,
 * inclusively, with a mean of (x+y)/2.
 * @param x The lower bound of the range
 * @param y The upper bound of the range
 * @return The selected random number
 */
public int random(int x, int y)
{
    return r.nextInt((y-x)+1)+x;
}

/**
 * Select a double random number in specified range.
 * @param x The lower bound of the range
 * @param y The upper bound of the range
 * @return The selected random number
 */
public double drandom(double x, double y)
{
	return ( x + (r.nextDouble()* (y - x)));
}


/**
 * Generates a random string of alphanumeric characters of random length
 * of mininum x, maximum y and mean (x+y)/2.
 * @param x The minimum length
 * @param y The maximum length
 * @return The generated string
 */
public String make_a_string(int x, int y)
{
	int len;	/* len of string */
	int i;
	String str = "";

	if ( x == y)
		len = x;
	else
		len = random(x, y);

	for (i=0; i < len; i++) {
		int j = random(0,61);
		str = str + alpha.substring(j,j+1);
	}
	return(str);
}

/**
 * Generates a random string of numeric characters of random length
 * of mininum x, maximum y and mean (x+y)/2.
 * @param x The minimum length
 * @param y The maximum length
 * @return The generated string
 */
public String make_n_string(int x, int y)
{
	int len, i;
	String str = "";

	if ( x == y)
		len = x;
	else
		len = random(x, y);

	for (i = 0; i < len; i++) 
		str = str + random(0, 9);
	return(str);
}
}
