
/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *  History:
 *  Date          ID        Company      Description
 *  ----------    --------  -------      -------------
 *  Jan , 2003    Tom Daly  Sun          Creation date
 *
 */

package org.spec.jappserver.driver.http;
public class HttpActionFactory {


	public static HttpAction getInstance(int type) 
		throws Exception {
		
		if(type == HttpMethodConstants.GET)
			return GetHttpAction.getInstance();
		else if(type == HttpMethodConstants.POST)
			return PostHttpAction.getInstance();
		else
			throw new Exception("invalid HttpAction type");
	}
}
