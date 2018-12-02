/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company                Description
 *  ----------  ----------------           ----------------------------------------------
 *  2003/01/??  Tom Daly, Sun              Creation date
 */


package org.spec.jappserver.driver.http;
import java.io.IOException;

public abstract class AbstractHttpAction implements HttpAction {

    protected static final String CRLF = "\r\n";
    protected static final String ACCEPTHOST = "\r\nAccept: */*\r\nHost: ";
    protected static final String NOCACHE = "\r\nPragma: no-cache\r\nCache-Control: no-cache";
    protected static final String COOKIE = "\r\nCookie: ";
    protected static final String CONTENTLEN = "\r\nContent-Length: ";
    protected static final String GET_STR = "GET";
    protected static final String POST_STR = "POST";

    protected static final String HTTP10 = "HTTP/1.0";
    protected static final String HTTP11 = "HTTP/1.1";
    protected static final String KEEPALIVE = "\r\nConnection: Keep-Alive";

    protected String protocol = HTTP11;

    protected static final byte[] crlfBytes = CRLF.getBytes();


	public void debugPrint(String s) {
		if(false)
			System.out.println(s);
	}

	protected void sendRequest(Connection c, int reqLen)
		throws IOException  {
                c.sendRequest(reqLen);
                return;

        }
	protected HttpResponseData getResponse(Connection c)
		throws IOException  {

                return c.readResponse();

        }

}	
		
