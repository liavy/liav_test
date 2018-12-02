
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
import java.io.Serializable;

public class HttpResponseData implements Serializable{

	HttpResponseParser data; //responseData
	CookieStrings cookies;
	int responseCode;
	
	public HttpResponseData(HttpResponseParser data, 
				CookieStrings cookies,
				int responseCode) {

		this.data = data;
		this.cookies=cookies;
		this.responseCode = responseCode;
	}


	public HttpResponseParser getData() { return data;}

	public CookieStrings getCookie() { return cookies;}

	public int getResponseCode() { return responseCode;}

}
