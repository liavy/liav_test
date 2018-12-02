package com.sap.engine.services.servlets_jsp.filters;

import java.io.IOException;

import com.sap.engine.services.httpserver.chain.Chain;
import com.sap.engine.services.httpserver.chain.Filter;
import com.sap.engine.services.httpserver.chain.FilterConfig;
import com.sap.engine.services.httpserver.chain.FilterException;
import com.sap.engine.services.httpserver.chain.HTTPRequest;
import com.sap.engine.services.httpserver.chain.HTTPResponse;
import com.sap.engine.services.httpserver.server.RequestAnalizer;
import com.sap.engine.services.servlets_jsp.DSRWebContainer;
import com.sap.engine.services.servlets_jsp.chain.ApplicationScope;
import com.sap.engine.services.servlets_jsp.chain.ServletScope;
import com.sap.engine.services.servlets_jsp.chain.impl.ServletChain;


public class DSRWebContainerFilter implements Filter {

  private static DSRWebContainer dsrWebContainer = null; 

  public void process(HTTPRequest request, HTTPResponse response, Chain chain) throws FilterException, IOException {       
    if (dsrWebContainer != null) {
      ServletChain servletChain = (ServletChain)chain;    
      ApplicationScope appScope = servletChain.getApplicationScope();      
      ServletScope servletScope = servletChain.getServletScope();
      
      
      String sessionId = request.getHTTPParameters().getSessionRequest().getSessionId();
      
      dsrWebContainer.containerStart(appScope.getApplicationName(), appScope.getAliasName(), servletScope.getServletClassName(), sessionId);      
    }
    
    //  leave only the calls which are for the DSR server and move the next two calls to the other filter
    RequestAnalizer analizer = request.getClient().getRequestAnalizer();    
    //TODO check if the servletScope.getServletName() is the same as analizer.getRequestPathMappings().getServletName() and if this is enough for starting the servlet
    if (analizer.getRequestPathMappings().getServletName() != null) {      
      analizer.startServlet(analizer.getRequestPathMappings().getServletName());
    }
    
    chain.process(request, response);
    
    if (dsrWebContainer != null) {   
      dsrWebContainer.containerEnd();
    }
  }

  public void destroy() {
    // TODO Auto-generated method stub

  }

  public void init(FilterConfig config) throws FilterException {
    // TODO Auto-generated method stub
  }
  
  /**
   * This method is invoked by DSR service in order to set real implementation.
   * Normaly it is invoked at startup of the service 
   */ 
  public static void registerWebContainer(DSRWebContainer real) {
    dsrWebContainer = real;
  }
  
  /**
   * This method is invoked by DSr service in order to unregiser real implementation
   * Normaly it is invoked when service is stopped
   */ 
  public static void unregisterWebContainer() {
    dsrWebContainer = null;
  }  
}
