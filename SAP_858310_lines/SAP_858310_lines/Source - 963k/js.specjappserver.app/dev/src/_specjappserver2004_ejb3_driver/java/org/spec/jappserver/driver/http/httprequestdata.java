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
import java.util.Enumeration;
import java.util.Hashtable;

public class HttpRequestData implements Serializable {
    
        String queryString;
	CookieStrings cookies;
	Hashtable postData;
	
	public HttpRequestData(String queryString, CookieStrings cookies) {
                this.queryString = queryString;
		this.cookies = cookies;
		postData = new Hashtable();
	}

	public void addParam(String key, String value) {
		postData.put(key,value);
	}

	public String getParam(String key) {
	       return (String)postData.get(key);
	}

	public Enumeration getParamKeys() {
		return postData.keys();
	}

 	public String getQueryString() { return queryString;}

	public CookieStrings getCookie() { return cookies;}

	public Hashtable getPostData() {return postData;}
}
