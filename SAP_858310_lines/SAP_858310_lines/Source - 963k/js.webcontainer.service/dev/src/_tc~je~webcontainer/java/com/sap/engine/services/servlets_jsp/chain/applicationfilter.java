package com.sap.engine.services.servlets_jsp.chain;

import java.io.IOException;

import com.sap.engine.services.httpserver.chain.Chain;
import com.sap.engine.services.httpserver.chain.Filter;
import com.sap.engine.services.httpserver.chain.FilterException;
import com.sap.engine.services.httpserver.chain.HTTPRequest;
import com.sap.engine.services.httpserver.chain.HTTPResponse;

public abstract class ApplicationFilter implements Filter {

  public void process(HTTPRequest request, HTTPResponse response, Chain chain) 
      throws FilterException, IOException {
    process(request, response, (ApplicationChain)chain);
  }
  
  /**
   * This method makes the right cast of the passed <code>Chain</code> object
   * and gives access only to available surrounding scopes
   * 
   * @param request
   * a <code>Request</code> object that contains the client request
   * 
   * @param response
   * a <code>Response</code> object that contains the response to the client
   * 
   * @param chain
   * a <code>ApplicationChain</code> object that gives access to available 
   * surrounding scopes and allows request and response to be passed to 
   * the next <code>Filter</code>
   * 
   * @throws FilterException
   * if the request could not be processed
   * 
   * @throws java.io.IOException
   * if an input or output error is detected
   */
  public abstract void process(HTTPRequest request, HTTPResponse response, 
      ApplicationChain chain) throws FilterException, IOException;
}
