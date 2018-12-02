package com.sap.engine.services.servlets_jsp.filters;

import java.io.IOException;

import com.sap.engine.services.httpserver.chain.Chain;
import com.sap.engine.services.httpserver.chain.Filter;
import com.sap.engine.services.httpserver.chain.FilterConfig;
import com.sap.engine.services.httpserver.chain.FilterException;
import com.sap.engine.services.httpserver.chain.HTTPRequest;
import com.sap.engine.services.httpserver.chain.HTTPResponse;
import com.sap.engine.services.httpserver.interfaces.HttpHandler;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.server.RequestAnalizer;
import com.sap.engine.services.httpserver.server.ResponseImpl;
import com.sap.engine.services.httpserver.server.SessionRequestImpl;
import com.sap.engine.services.servlets_jsp.chain.ApplicationChain;
import com.sap.engine.services.servlets_jsp.chain.WebContainerScope;
import com.sap.engine.services.servlets_jsp.chain.impl.ApplicationChainImpl;
import com.sap.engine.services.servlets_jsp.chain.impl.ServletScopeImpl;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.SessionServletContext;
/**
 * Defines the servlet that should handle this request, filters that
 * should be executed and applies all the security constraints if any
 * 
 * <p>Important: This filter requires application scope access</p>
 */
public class ServletSelector implements Filter {

  public void process(HTTPRequest request, HTTPResponse response,
      Chain _chain) throws FilterException, IOException {
    // To reduce call stack this class implements Filter instead of extend
    // ApplicationFilter, thus class cast to ApplicationChain should be done here
    ApplicationChain chain = (ApplicationChain) _chain;
    WebContainerScope webScope = chain.getWebContainerScope();
    ApplicationContext appScope = 
      (ApplicationContext) chain.getApplicationScope();
    
    HttpParameters httpParams = request.getHTTPParameters();
    
    //Add here mark id in the client context if SessionIDRegenerationEnabled is false, get mark id from jsessionid cookie
    //this is added so the markid in the client context will be compared to this id taken from jsessionid cookie
    //it is added because of secure reasons - to hide jsessionid cookie
    String jsessionid = appScope.getJSessionCookie(httpParams);
    if (jsessionid != null) {
      SessionRequestImpl sessionRequest = (SessionRequestImpl) request
          .getClient().getRequestAnalizer().getSessionRequest();
      sessionRequest.setMarkIdInProtectionData(SessionServletContext.getMarkIdFromJSession(jsessionid));
    }
        
    // Defines and sets this request servlet scope regarding defined
    // servlet and filter mappings, security constraints, etc.
    RequestAnalizer analizer = request.getClient().getRequestAnalizer();
    
    byte checkMapResult = appScope.checkMap(analizer.getFilename1(), httpParams);
    ServletScopeImpl servletScope = new ServletScopeImpl(httpParams);
    ((ApplicationChainImpl)chain).setServletScope(servletScope);
    
    // Returns an error page to the client in case of some error
    // during the current request processing with respect to the
    // application error status code to error page mappings
    if (checkMapResult == HttpHandler.ERROR || analizer.getErrorData() != null) {
      response.sendError(analizer.getErrorData());
      return;
    }
    
    // Terminates request processing because response is done
    ResponseImpl oresponse = request.getClient().getResponse();
    if (checkMapResult == HttpHandler.RESPONSE_DONE) {
      //reply is done
      oresponse.done();
      return;
    }
        
    if (oresponse.isSchemeChanged()) {
      analizer.sendChangeSchemaToSSlAfterCheckMap();
      return;
    }
           
    chain.process(request, response);
  }

  public void init(FilterConfig arg0) throws FilterException {
    // TODO Auto-generated method stub

  }

  public void destroy() {
    // TODO Auto-generated method stub

  }

}
