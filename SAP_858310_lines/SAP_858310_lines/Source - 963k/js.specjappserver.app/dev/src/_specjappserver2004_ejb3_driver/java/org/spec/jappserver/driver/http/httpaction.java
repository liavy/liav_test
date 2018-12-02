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

public interface HttpAction {

	public HttpResponseData execute(HttpRequestData data, Connection c)
		throws Exception;
}
