package com.sap.archtech.archconn.response;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.HashMap;

/**
 * The <code>IHttpStreamResponse</code> interface exposes methods to access data contained in the HTTP stream
 * of the XML DAS response.
 */
public interface IHttpStreamResponse extends IGenericHttpResponseMethods, IGenericResponseMethods
{
	public BufferedInputStream getBufferedInputStream() throws IOException;

	public InputStreamReader getErrorStreamReader() throws IOException;

	public InputStreamReader getInputStreamReader() throws IOException;

	public InputStreamReader getBufferedInputStreamReader() throws IOException;

	public ObjectInputStream getObjectInputStream() throws IOException;
	
	public HashMap<? extends Object, ? extends Object> getRequestParams();
}
