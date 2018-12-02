package com.sap.engine.services.servlets_jsp.server.servlet;

import static com.sap.engine.services.servlets_jsp.server.LogContext.getLocationServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.engine.services.httpserver.interfaces.ErrorData;
import com.sap.engine.services.httpserver.interfaces.ErrorPageTemplate;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.httpserver.lib.ResponseCodes;
import com.sap.engine.services.httpserver.lib.Responses;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.ServletContextImpl;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.engine.services.servlets_jsp.server.lib.FilterUtils;
import com.sap.engine.services.servlets_jsp.server.lib.StringUtils;
import com.sap.tc.logging.Location;

/**
 * 
 * @author I026706
 * @version 7.20
 */
public class ErrorHandlerServlet extends HttpServlet {
  transient private static Location currentLocation = Location.getLocation(ErrorHandlerServlet.class);
  private static final String IMG = "gerloaderimg";
  private static final String IMG_PATH = "com/sap/engine/services/servlets_jsp/server/servlet/images/";
  
  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String img = request.getParameter(IMG);
    if (img != null) {
      ApplicationContext applicationContext = ((ServletContextImpl) getServletContext()).getApplicationContext();
      OutputStream out = response.getOutputStream();
      String path = ParseUtils.canonicalize(IMG_PATH + img.replace(File.separatorChar, ParseUtils.separatorChar));
      response.setContentType("image/gif");
      InputStream is = null;
      try {
        is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is != null) {
          response.setStatus(ResponseCodes.code_ok);
          byte[] buff = new byte[1024];
          int c;
          while((c = is.read(buff)) != -1) {
            out.write(buff, 0, c);
          }
          out.flush();
        } else {
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000691",
            "Cannot find image in the specified path [{0}] for [{1}] web application.",
            new Object[]{path, applicationContext.getAliasName()}, applicationContext.getApplicationName(), applicationContext.getCsnComponent());
          response.setStatus(ResponseCodes.code_not_found);
          return;
        }
      } finally {
        if (is != null) {
          try {
            is.close();
          } catch (IOException e) {
            if (getLocationServletResponse().beWarning()) {
              LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000690", 
                "Cannot close an input stream to the resource [{0}]. This may lead to file handlers leak.", new Object[] {path}, e, null, null);
            }
          }
        }
      }
    } else {
      ErrorData errorData = FilterUtils.unWrapRequest(request).getHttpParameters().getErrorData();
      try {
        if (!response.isCommitted()) {
          response.setStatus(errorData.getErrorCode());
          response.setHeader("Pragma", "no-cache");
          response.setContentType("text/html;charset=ISO-8859-1");
        }
        // checkHtml means replace all unsafe HTML chars with their (numeric)
        // character references, including chars with codes greater than 127
        if (errorData.isHtmlAllowed()) {
          // If NO CSI ESCAPING LIBRARY is used (when checkHtml is true the csi escaping library is used) 
          // ensures correct display of Unicode chars, by replacing all
          // chars greater than 127 with their numeric character references
          errorData.setMessage(StringUtils.encodeToEntities(errorData.getMessage()));
          errorData.setAdditionalMessage(StringUtils.encodeToEntities(errorData.getAdditionalMessage()));
          errorData.getSupportabilityData().setCorrectionHints(StringUtils.encodeToEntities(errorData.getSupportabilityData().getCorrectionHints()));
        }

        String errorBody = getErrorResponse(errorData, request, response);

        if (!response.isCommitted()) {
          response.setContentLength(errorBody.length());
        }

        try {
          PrintWriter pw = response.getWriter();
          pw.println(errorBody);
          pw.flush();
        } catch (IllegalStateException e) {
          ServletOutputStream sos = response.getOutputStream();
          sos.println(errorBody);
          sos.flush();
        }
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000669", 
          "Cannot send an HTTP error response [{0} {1} (details: {2})].", 
          new Object[]{errorData.getErrorCode(), errorData.getMessage(), errorData.getAdditionalMessage()}, e, null, null);
      }
    }
  }//end of service(HttpServletRequest request, HttpServletResponse response)

  private String getErrorResponse(ErrorData errorData, HttpServletRequest request, HttpServletResponse response) {
    boolean detailedErrorResponse = ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().isDetailedErrorResponse();

    String url = (String) request.getAttribute(Constants.URI_FOR_GENERATING_ERROR_REPORT);
    
    ErrorPageTemplate errorPageTemplate = (ErrorPageTemplate) request.getAttribute(Constants.ERROR_PAGE_TEMPLATE);
    if (!"".equals(ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getErrorPageTemplateLocation())) {
      //We have custom error page template so we will ignore the template used with sendError 
      //because there will be an inconsistent error page presented in the browser
      if (getLocationServletResponse().beWarning()) {
        LogContext.getLocation(LogContext.LOCATION_SERVLET_RESPONSE).traceWarning("ASJ.web.000692", 
          "The error page template provided with sendError(...) method will be ignored. " +
          "Reason: HTTP Provider's property 'ErrorPageTemplateLocation' has a custom value.", null, null);
      }
      errorPageTemplate = null;
    }
    
    return Responses.getErrorResponse(errorData, ServiceContext.getServiceContext().getServerVersion(),
      getServletContext().getContextPath(), detailedErrorResponse ? errorData.getSupportabilityData().getMessageId() : "",
      url, ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getTroubleShootingGuideURL(),
      ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getTroubleShootingGuideSearchURL(), errorPageTemplate);    
  }//end of getErrorResponse(HttpServletRequestFacade request, HttpServletResponseFacade response)
  
}//end of class
