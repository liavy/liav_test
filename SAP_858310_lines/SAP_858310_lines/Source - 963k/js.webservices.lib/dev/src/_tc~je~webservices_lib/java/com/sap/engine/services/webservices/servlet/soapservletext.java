/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
 
package com.sap.engine.services.webservices.servlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import com.sap.engine.interfaces.webservices.esp.ServiceEndpointWrapper;
import com.sap.engine.interfaces.webservices.runtime.ApplicationWebServiceContext;

/**
 * Title: SOAPServletExt
 * Description: SOAPServletExt
 * 
 * @author Dimitrina Stoyanova
 * @version
 */

public class SOAPServletExt extends SoapServlet implements ServiceEndpointWrapper {

//  private static final String WS_DISPATCHER = "/wsContext/" + ServletDispatcher.NAME;
  private static final String APP_WSCONTEXT = "/wsContext/" + ApplicationWebServiceContext.APPLICATION_WSCONTEXT;


  private Object serviceEndpointInstance; //$JL-SER$
       
  public SOAPServletExt(Object serviceEndpointInstance) {
    this.serviceEndpointInstance = serviceEndpointInstance; 
  }
       
  public void start() throws Exception {
    super.start();
    
    if ((serviceEndpointInstance instanceof ServiceLifecycle) && (ctx != null)) {
      //create ServletEndpointContext
      ApplicationWebServiceContext appWSContext = (ApplicationWebServiceContext) ctx.lookup(APP_WSCONTEXT);
      ServletEndpointContext servletEndpointContext = new ServletEndpointContextImpl(this.getServletContext(), appWSContext);
      //init wrapped instance
      ((ServiceLifecycle) serviceEndpointInstance).init(servletEndpointContext);
    }
  }  

  public void init() throws ServletException {
    try {
      start();
    } catch(Exception e) {
      StringWriter w = new StringWriter();
      PrintWriter pw = new PrintWriter(w);
      e.printStackTrace(pw);
      pw.flush();
      throw new UnavailableException(w.toString(), 0);
    }
  }
  
  
  public void destroy() {
    super.destroy();
    //destroy wrapped instance
    if (serviceEndpointInstance instanceof ServiceLifecycle) {
      ((ServiceLifecycle) serviceEndpointInstance).destroy();      
    }    
  }      
  
  public Object getServiceEndpointInstance() {
    return serviceEndpointInstance;
  }

}
