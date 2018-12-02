package com.sap.archtech.archconn.httpclients;

import java.util.HashMap;
import java.io.IOException;

import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchResponse;

/**
 * The XML DAS Connector for Java allows the 
 * usage of different HTTP client libraries.
 * For each library a class is needed, which
 * implements the client-specific behavior.
 * All these classes must implement this
 * interface. This interface should <b>not</b> be
 * used by application programs.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public interface ArchHTTPClient
{
	public ArchResponse executeRequest(HashMap<? extends Object, ? extends Object> params, ArchCommand archCommand) throws IOException;
}
