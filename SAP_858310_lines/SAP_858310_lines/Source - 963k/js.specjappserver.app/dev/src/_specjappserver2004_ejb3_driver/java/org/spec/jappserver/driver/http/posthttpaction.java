
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
import java.util.Enumeration;

public class PostHttpAction extends AbstractHttpAction {


	private static PostHttpAction theInstance = null;

	private PostHttpAction(){}


	public static PostHttpAction getInstance() {
		if(theInstance==null)
			theInstance = new PostHttpAction();

		return theInstance;
	}


	public HttpResponseData execute(HttpRequestData req, Connection c)
		throws Exception {

            int reqLen = buildRequest(req, POST_STR, c.hostServer, c.reqBuf);
            sendRequest(c, reqLen);
             
	    HttpResponseData response = getResponse(c);
	    debugPrint("Got Response");
            return response;

        }

        protected int buildRequest (HttpRequestData req, String httpMethod, String hostServer, byte[] reqBuf) {

            int j;

            int reqLen;

            // GET
            System.arraycopy(httpMethod.getBytes(), 0, reqBuf, 0, httpMethod.length());
            j=httpMethod.length();
            reqBuf[j++] = ' ';
            reqLen = j;

            // Query String
            String queryString = req.getQueryString();
            System.arraycopy(queryString.getBytes(), 0, reqBuf, reqLen, queryString.length());

                reqBuf[reqLen++] = ' ';
                System.arraycopy(protocol.getBytes(), 0, reqBuf, reqLen, protocol.length()); 
                reqLen += protocol.length(); 

                System.arraycopy(NOCACHE.getBytes(), 0, reqBuf, reqLen, NOCACHE.length()); 
                reqLen += NOCACHE.length(); 

                // Cookie
                CookieStrings cookies = req.getCookie();
                if (cookies != null) {
                    String cookie;
                    for (int i = 0 ; i < cookies.num; i++) {
                        cookie = cookies.entries[i];
                        System.arraycopy(COOKIE.getBytes(), 0, reqBuf, reqLen, COOKIE.length());
                        reqLen += COOKIE.length();
                        System.arraycopy(cookie.getBytes(), 0, reqBuf, reqLen, cookie.length());
                        reqLen += cookie.length();
                    }
                } 

                System.arraycopy(ACCEPTHOST.getBytes(), 0, reqBuf, reqLen, ACCEPTHOST.length()); 
                reqLen += ACCEPTHOST.length(); 

                System.arraycopy(hostServer.getBytes(), 0, reqBuf, reqLen, hostServer.length()); 
                reqLen += hostServer.length(); 

                // post data and count the length
		Enumeration keys = req.getParamKeys();
                String key;
                String value;
                int postLen = 0;

                // assume there is less than 256 post data
                byte[] postBuf = new byte[256];

		if(keys.hasMoreElements()) {// get first element;
                        key = (String)keys.nextElement();
                        value = req.getParam((String)keys.nextElement());
                        System.arraycopy(key.getBytes(), 0, postBuf, postLen, key.length()); 
                        postLen += key.length();
                        postBuf[reqLen++] = '=';
                        System.arraycopy(value.getBytes(), 0, postBuf, postLen, value.length()); 
                        postLen += value.length();
                } 
		
		while(keys.hasMoreElements()){
                        postBuf[reqLen++] = '&';
                        key = (String)keys.nextElement();
                        value = req.getParam((String)keys.nextElement());
                        System.arraycopy(key.getBytes(), 0, postBuf, postLen, key.length()); 
                        postLen += value.length();
                        postBuf[reqLen++] = '=';
                        System.arraycopy(value.getBytes(), 0, postBuf, postLen, value.length()); 
                        postLen += value.length();
                }

                // content-length:
                String contentLen = CONTENTLEN + postLen + "\r\n\r\n"; 
                System.arraycopy(contentLen.getBytes(), 0, reqBuf, reqLen, contentLen.length());
                reqLen += contentLen.length();

                reqBuf[reqLen++] = '\r';
                reqBuf[reqLen++] = '\n';
                reqBuf[reqLen++] = '\r';
                reqBuf[reqLen++] = '\n';

                System.arraycopy(postBuf, 0, reqBuf, reqLen, postLen);
                reqLen += postLen;

                return reqLen;
    }
}

			
