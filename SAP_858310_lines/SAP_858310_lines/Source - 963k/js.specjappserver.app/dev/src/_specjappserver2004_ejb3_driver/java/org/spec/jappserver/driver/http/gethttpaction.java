/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *  History:
 *  Date          ID        Company      Description
 *  ----------    --------  ----------   ----------------------------------------------------------
 *  2003/01/??    Tom Daly  Sun          Creation date
 *  2003/08/??    Ning Sun  SUN          Modified to correct bugs in response parsing.
 *  2004/06/15    Ning Sun, SUN          Fixed problem with multiple cookie support on SAP WebAS.
 *                                       See osgjava-7221 and osgjava-7228.
 */

package org.spec.jappserver.driver.http;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;

public class GetHttpAction extends AbstractHttpAction {

	private static GetHttpAction theInstance = null;

	private GetHttpAction(){}

	public static GetHttpAction getInstance() {
		if(theInstance==null)
			theInstance = new GetHttpAction();

		return theInstance;
	}

	public HttpResponseData execute(HttpRequestData req, Connection c)
		throws Exception {


            int reqLen = buildRequest(req, GET_STR, c.hostServer, c.reqBuf);
            sendRequest(c, reqLen);
             
	    HttpResponseData response = getResponse(c);
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

          try { 
            // Query String
            String queryString = req.getQueryString();
            System.arraycopy(queryString.getBytes(), 0, reqBuf, reqLen, queryString.length());
            reqLen += queryString.length();

            // parameters
		Enumeration keys = req.getParamKeys();
                String key;
                String value;

		if(keys != null && keys.hasMoreElements()) {// get first element;
                        key = (String)keys.nextElement();
                        value = req.getParam(key);
                        key = URLEncoder.encode(key, "UTF-8");
                        value = URLEncoder.encode(value, "UTF-8");
                        System.arraycopy(key.getBytes(), 0, reqBuf, reqLen, key.length()); 
                        reqLen += key.length();

                        reqBuf[reqLen++] = '=';
                        System.arraycopy(value.getBytes(), 0, reqBuf, reqLen, value.length()); 
                        reqLen += value.length();

                } 
		
		while(keys.hasMoreElements()){
                        reqBuf[reqLen++] = '&';
                        //reqBuf.append(appendParam(keys,data));
                        key = (String)keys.nextElement();
                        value = req.getParam(key);
                        key = URLEncoder.encode(key, "UTF-8");
                        value = URLEncoder.encode(value, "UTF-8");
                        System.arraycopy(key.getBytes(), 0, reqBuf, reqLen, key.length()); 
                        reqLen += key.length();

                        reqBuf[reqLen++] = '=';
                        System.arraycopy(value.getBytes(), 0, reqBuf, reqLen, value.length()); 
                        reqLen += value.length();
                }

                reqBuf[reqLen++] = ' ';
                System.arraycopy(protocol.getBytes(), 0, reqBuf, reqLen, protocol.length()); 
                reqLen += protocol.length(); 

                System.arraycopy(NOCACHE.getBytes(), 0, reqBuf, reqLen, NOCACHE.length()); 
                reqLen += NOCACHE.length(); 

                // Cookie
                CookieStrings cookies = req.getCookie();
                if (cookies != null) {
                    String cookie;

                    for (int i=0; i < cookies.num; i++) {
                        cookie = cookies.entries[i];
                        if (cookie != null) { // entries[0] might be null even though cookies.num is > 0
                            System.arraycopy(COOKIE.getBytes(), 0, reqBuf, reqLen, COOKIE.length());
                            reqLen += COOKIE.length();
                            System.arraycopy(cookie.getBytes(), 0, reqBuf, reqLen, cookie.length());
                            reqLen += cookie.length();
                        }
                    }
                }

                System.arraycopy(KEEPALIVE.getBytes(), 0, reqBuf, reqLen, KEEPALIVE.length()); 
                reqLen += KEEPALIVE.length(); 


                System.arraycopy(ACCEPTHOST.getBytes(), 0, reqBuf, reqLen, ACCEPTHOST.length()); 
                reqLen += ACCEPTHOST.length(); 

                System.arraycopy(hostServer.getBytes(), 0, reqBuf, reqLen, hostServer.length()); 
                reqLen += hostServer.length(); 

                reqBuf[reqLen++] = '\r';
                reqBuf[reqLen++] = '\n';
                reqBuf[reqLen++] = '\r';
                reqBuf[reqLen++] = '\n';
/*
                try {
                    System.out.println("[" + reqLen + "]\n" + new String(reqBuf, 0, reqLen, "ISO-8859-1"));
                } catch (UnsupportedEncodingException e) {
                }
*/
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return reqLen;
    }

        
}

			
