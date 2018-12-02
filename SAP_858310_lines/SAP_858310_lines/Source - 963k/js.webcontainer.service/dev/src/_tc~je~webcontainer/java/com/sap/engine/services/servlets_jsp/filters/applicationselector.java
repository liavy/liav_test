package com.sap.engine.services.servlets_jsp.filters;

import static com.sap.engine.services.httpserver.lib.ResponseCodes.code_ok;
import static com.sap.engine.services.httpserver.lib.protocol.HeaderNames.entity_header_content_length_;
import static com.sap.engine.services.httpserver.lib.protocol.HeaderNames.entity_header_content_type_;
import static com.sap.engine.services.httpserver.lib.protocol.HeaderNames.entity_header_date_;
import static com.sap.engine.services.httpserver.lib.protocol.HeaderNames.response_header_server_;
import static com.sap.engine.services.httpserver.lib.protocol.HeaderValues.text_palin_;
import static com.sap.engine.services.servlets_jsp.server.LogContext.getExceptionStackTrace;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.sap.engine.lib.io.DataCollectorHelper;
import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.deploy.container.ExceptionInfo;
import com.sap.engine.services.httpserver.chain.Chain;
import com.sap.engine.services.httpserver.chain.ChainComposer;
import com.sap.engine.services.httpserver.chain.Filter;
import com.sap.engine.services.httpserver.chain.FilterConfig;
import com.sap.engine.services.httpserver.chain.FilterException;
import com.sap.engine.services.httpserver.chain.HTTPRequest;
import com.sap.engine.services.httpserver.chain.HTTPResponse;
import com.sap.engine.services.httpserver.chain.HostChain;
import com.sap.engine.services.httpserver.chain.HostScope;
import com.sap.engine.services.httpserver.interfaces.ErrorData;
import com.sap.engine.services.httpserver.interfaces.HttpParameters;
import com.sap.engine.services.httpserver.interfaces.SupportabilityData;
import com.sap.engine.services.httpserver.lib.util.MessageBytes;
import com.sap.engine.services.httpserver.server.errorreport.ErrorCategorizationEntry;
import com.sap.engine.services.httpserver.server.errorreport.ErrorReportInfoBean;
import com.sap.engine.services.httpserver.server.SessionRequestImpl;
import com.sap.engine.services.servlets_jsp.chain.WebContainerScope;
import com.sap.engine.services.servlets_jsp.chain.impl.ApplicationChainImpl;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.WebContainerProvider;
import com.sap.engine.services.servlets_jsp.server.deploy.impl.module.WebModule;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebApplicationException;
import com.sap.engine.services.servlets_jsp.server.lib.Constants;
import com.sap.engine.services.servlets_jsp.server.lib.WebParseUtils;
import com.sap.engine.services.servlets_jsp.server.runtime.client.ApplicationSession;
import com.sap.engine.services.servlets_jsp.lib.util.WebContainerExecituionDetails;
import com.sap.engine.session.exec.ClientContextImpl;
import com.sap.engine.session.exec.SessionExecContext;
import com.sap.tc.logging.Location;
import com.sap.engine.services.httpserver.lib.*;

/**
 * Defines this request application scope, creates new chain based on Web
 * Container <code>ChainComposer</code> and passes the request through it
 * 
 * <p>Returns service unavailable page to the client if:
 * <ul>
 * <li>application scope could not be defined</li>
 * <li>the defined application scope is destroying</li>
 * <li>the application alias is listed in StoppedApplicationAliases property</li>
 * </ul>
 * </p>
 * 
 * <p> This filter is an extension filter, i.e. the decision Web Container
 * to be included in the processing chain should be taken here</p>
 * 
 * <p>Important: This filter requires host scope access</p>
 */
public class ApplicationSelector implements Filter {
  /**
   * Holds this class logging location
   */
  private static final Location location = Location.getLocation(ApplicationSelector.class);
  private SimpleDateFormat date = null;
  
  /**
   * Holds Web Container chain composer
   */
  ChainComposer composer;
  
   //ExpirationHashMap ehp should  be used only for setting new validity period if needed. It's initial value is equal to GenerateNewErrorReportTimeout http property
   //For storing and accessing error categorization entries the thread-safe map allErrorCategorizationEntries should be used
   ExpirationHashMap<Integer, ErrorCategorizationEntry> ehp = new ExpirationHashMap<Integer, ErrorCategorizationEntry>(30, ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getGenerateNewErrorReportTimeout());
   private Map<Integer, ErrorCategorizationEntry> allErrorCategorizationEntries = Collections.synchronizedMap(ehp);

  public ApplicationSelector(ChainComposer composer) {
    this.composer = composer;
  }

  public void process(HTTPRequest request, HTTPResponse response,
      Chain _chain) throws FilterException, IOException {
    // To reduce call stack this class implements Filter instead of extend
    // HostFilter, thus class cast to HostChain should be done here
    HostChain chain = (HostChain) _chain;
    HostScope hostScope = chain.getHostScope();
    
    boolean accountingOn = ServiceContext.isAccountingEnabled();
    try {//accounting-begin
      if (accountingOn) {
        Accounting.beginMeasure("Request in WebContainer", ApplicationSelector.class);
      }//accounting-begin

      String clientIP = ParseUtils.ipToString(request.getClient().getIP());
      //Delegate client IP to the SessionManagement - it will expose to NWA for user sessions UI
      SessionExecContext.getExecutionContext().setDetails(new WebContainerExecituionDetails(clientIP));
  
      String[] protectionData = null;
      // Check SessionIPProtectionEnabled property  
      if (getServiceContext().getWebContainerProperties().getSessionIPProtectionEnabled()){
        protectionData = new String[] {null, null};
        protectionData[0] = clientIP;
      }
      if (getServiceContext().getWebContainerProperties().getSessionIDRegenerationEnabled()){
        if (protectionData == null) {
          protectionData = new String[] {null, null};
        }
        //todo cookies
        ArrayObject cookies = request.getHTTPParameters().getRequest().getCookies(false);
        for (int i = 0; i < cookies.size(); i++) {
          HttpCookie httpCookie = (HttpCookie) cookies.elementAt(i);
          if (CookieParser.jsession_mark_cookie.equalsIgnoreCase(httpCookie.getName())) {
            protectionData[1] = httpCookie.getValue();
          }
        }
      }
      if (protectionData != null) {
        SessionRequestImpl sessionRequest = (SessionRequestImpl) request.getClient().getRequestAnalizer().getSessionRequest();
        sessionRequest.setProtectionData(protectionData);
      }
  
      // Check for "generate error report" request
      //TODO : Vily G : move this in a separate filter - has to think twice!!!
  		if (chain.getServerScope().getHttpProperties().isGenerateErrorReports() &&
      		request.getURLPath().startsWith("/@@@GenerateErrorReport@@@")) {
  			date = new SimpleDateFormat("yyyy MM dd HH:mm:ss:SSS", Locale.US); // for example '2008 03 14 10:28:04:328' 
        
  			String resultResponse = generateErrorReport(request, response, hostScope);
      		
     		response.getRawResponse().setResponseCode(code_ok);
     		response.getRawResponse().getHeaders().putHeader(entity_header_content_type_, text_palin_);
     		response.getRawResponse().getHeaders().putIntHeader(entity_header_content_length_, resultResponse.getBytes().length); 
      	response.getRawResponse().getHeaders().putDateHeader(entity_header_date_);
      	if (chain.getServerScope().getHttpProperties().getUseServerHeader()) {
      		response.getRawResponse().putHeader(response_header_server_, request.getClient().getRequestAnalizer().getHostDescriptor().getVersion());
      	}
      	response.getRawResponse().makeAnswerMessage(resultResponse.getBytes()); 
      	
  			return;
      }
  		
      HttpParameters httpParams = request.getHTTPParameters();
      
      // TODO: Clear all MessageBytes usages from Web Container
      MessageBytes _aliasName = httpParams.getRequestPathMappings().getAliasName();
      // If there isn't any application or http alias found for the
      // request, then Web Container removes it self from the chain
      // and leaves this request processing to HTTP Server 
      if (_aliasName == null) { 
        chain.proceed(); 
        return;
      }
  
      ApplicationContext appScope = null;
      String aliasName = _aliasName.toString();
      // Defines the application context for this alias. If application is
      // stopped and is in lazy startup mode then first starts it. If defined
      // alias is an http alias then the default application context is used
      if (hostScope.getHostProperties().isApplicationAlias(aliasName)) {
  			appScope = getServiceContext().getDeployContext().startLazyApplication(_aliasName);
      } else {     
  			appScope = getServiceContext().getDeployContext().startLazyApplication(Constants.defaultAliasMB);      
      }
      
      // If application scope for this request could not be defined or 
      // the defined one is destroying an service unavailable page should
      // be returned to the client
      if (appScope == null || appScope.isDestroying()) {
        String applicationName = getApplicationName(aliasName);
        try {
          ExceptionInfo exceptionInfo = null;
          if (applicationName != null && 
              (exceptionInfo = getServiceContext().getDeployContext().getDeployCommunicator().getExceptionInfo(applicationName)) != null) {
          	
          	String logExId = LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceError("ASJ.web.000591", 
  						"Web application [{0}] cannot be started. The error is: {1}" , new Object[]{aliasName, exceptionInfo.getStackTrace()}, null, null);
          	
            SupportabilityData supportabilityData = new SupportabilityData(true, exceptionInfo.getStackTrace(), logExId);
  					if (supportabilityData.getMessageId().equals("")) {
  					  supportabilityData.setMessageId("com.sap.ASJ.web.000591");
  					}
  		      //TODO : Vily G : if there is no DC and CSN in the supportability data
  		      //and we are responsible for the problem then set our DC and CSN
  		      //otherwise leave them empty
            if (!chain.getServerScope().getHttpProperties().isDetailedErrorResponse()) {
  					  Throwable logId;
  					  if (logExId == null) {  //in case of logRecord.getId() return null
  				      logId = new WebApplicationException(WebApplicationException.Log_ID_NULL, new Object[] {LogContext.LOCATION_REQUEST_INFO_SERVER_NAME});      
  				    } else {
  				      WebContainerProvider webContainerProvider = ((WebContainerProvider)ServiceContext.getServiceContext().getWebContainer().getIWebContainerProvider());
  				      if (webContainerProvider.getDeployedWebApplications().containsKey(Constants.LOG_VIEWER_ALIAS_NAME)) {
  				        logExId = "<a href=" + Constants.LOG_VIEWER_ALIAS_NAME + "/LVApp?conn=filter[Log_ID:" + logExId + "]view[Default%20Trace%20(Java)]>" + logExId + "</a>";
  				      }
  				      logId = new WebApplicationException(WebApplicationException.Log_ID_,new Object[] {logExId});
  				    }
  					  sendApplicationStoppedResponse(httpParams, Responses.mess19, 
  						  logId.getLocalizedMessage(), supportabilityData);
  					
  					} else {
  						sendApplicationStoppedResponse(httpParams, Responses.mess19, 
  						  Responses.toHtmlView(exceptionInfo.getStackTrace()), supportabilityData);
  					}
          } else {
            sendApplicationStoppedResponse(httpParams, 
              Responses.mess14, Responses.mess15, new SupportabilityData());//here we do not need user action
          }
        } catch (IOException ioe) {
          // TODO: Check if this exception should be handled here
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(
            location, "ASJ.web.000273", "Cannot send HTTP error response [{0} Application stopped]. Possible reason: IO streams closed, client socket closed, or cluster communication error.", new Object[]{ResponseCodes.code_service_unavailable}, 
            ioe, null, null);
        }
        return;
      }
      
      // Returns an service unavailable page to the client if application
      // alias is listed in "ApplicationStoppedAliases" Web Container property 
      String[] appStoppedAliases = 
        getServiceContext().getWebContainerProperties().appStoppedAliases();
      if (appStoppedAliases != null) {
        String zoneName = httpParams.getRequestPathMappings().getZoneName();
        if (zoneName != null && !httpParams.getRequestPathMappings().isZoneExactAlias()) {
          aliasName += chain.getServerScope().getHttpProperties().getZoneSeparator() 
            + zoneName;
        }
        for (int i = 0; i < appStoppedAliases.length; i++) {
          if (aliasName.equals(appStoppedAliases[i])) {
            try {
              sendApplicationStoppedResponse(httpParams, 
                Responses.mess14, Responses.mess15, new SupportabilityData());//here we do not need user action
            } catch (IOException ioe) {
              LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(
                location, "ASJ.web.000274", "Cannot send HTTP error response [{0} Application stopped]. Possible reason: IO streams closed, client socket closed, or cluster communication error.", new Object[]{ResponseCodes.code_service_unavailable},
                ioe, null, null);
            }
            return;
          }
        }
      }
      
      // Creates a new chain that will process the request and gives
      // access to surrounding web container and application scopes
      WebContainerScope webScope = getServiceContext();
      ApplicationChainImpl appChain = new ApplicationChainImpl(chain,
          composer.getFilters(), webScope);
      appChain.setApplicationScope(appScope);
      
      // Any exception thrown by the Web Container is terminated here 
      // with respect to application exception to error page mappings 
      try {
        appChain.process(request, response);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
      	String logId = LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(location, "ASJ.web.000325",
          "Web Container failed to process the request [{0}].", 
          new Object[] {request.getID()}, e, null, null);
      	SupportabilityData supportabilityData = new SupportabilityData(true, getExceptionStackTrace(e), logId);
        if (supportabilityData.getMessageId().equals("")) {
          supportabilityData.setMessageId("com.sap.ASJ.http.000325");
        }
        //TODO : Vily G : if there is no DC and CSN in the supportability data
        //and we are responsible for the problem then set our DC and CSN
        //otherwise leave them empty
        if (chain.getServerScope().getHttpProperties().isDetailedErrorResponse()) {
          ErrorData errorData = new ErrorData(ResponseCodes.code_internal_server_error,
            Responses.mess17.replace("{ID}", request.getID() + ""), getExceptionStackTrace(e), false,
            supportabilityData);
          errorData.setErrorByCode(false);
          errorData.setException(e);
          response.sendError(errorData);
        } else {
          ErrorData errorData = new ErrorData(ResponseCodes.code_internal_server_error,
            Responses.mess17.replace("{ID}", request.getID() + ""), "", false,
            supportabilityData);
          errorData.setErrorByCode(false);
          errorData.setException(e);
          response.sendError(errorData);
        }
      }
    } finally {//accounting-end
      if (accountingOn) {
        Accounting.endMeasure("Request in WebContainer");
      }
    }//accounting-end
  }

  public void init(FilterConfig arg0) throws FilterException {
    // Nothing to initialize
  }

  public void destroy() {
    // Nothing to destroy
  }
  
  /**
   * Short-method that returns current <code>ServiceContext</code>
   * 
   * @return
   * current <code>ServiceContext</code> 
   */
  private ServiceContext getServiceContext() {
    return ServiceContext.getServiceContext();
  }
  
  private String getApplicationName(String alias) {
    try {
      String applications[] = getServiceContext().getDeployContext()
        .getDeployCommunicator().getMyApplications();
      for (int i = 0; i < applications.length; i++) {
        String aliases[] = getServiceContext().getDeployContext()
          .getDeployCommunicator().getAliases(applications[i]);
        if (aliases == null || aliases.length == 0) {
          continue;
        }
        for (int j = 0; j < aliases.length; j++) {
          if (aliases[j].equals(alias)) {
            return applications[i];
          }
        }
      }
    } catch (DeploymentException e) {
    	//TODO:Polly Check this
    	//TODO:Polly type:ok
    	//Change message
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(location, "ASJ.web.000249",
        "Cannot get the Java EE application name of a web application with alias [{0}].", new Object[]{alias}, e, null, null);
    }
    return null;
  }
  
  private void sendApplicationStoppedResponse(HttpParameters httpParameters, 
      String msg, String details, SupportabilityData supportabilityData) throws IOException {
    String appStoppedFileName = 
      getServiceContext().getWebContainerProperties().appStoppedFile();
    if (appStoppedFileName == null) {
      httpParameters.getResponse().sendError(new ErrorData(ResponseCodes.code_service_unavailable, 
        msg, details, false, supportabilityData));
      return;
    }
    
    RandomAccessFile rFile = null;
    byte[] file = ServiceContext.applicationStoppedContent;
    try {
      File appStoppedFile = new File(appStoppedFileName);
      if (appStoppedFile.exists())  {
        if(appStoppedFile.lastModified() > ServiceContext.applicationStoppedContentModified) {
          rFile = new  RandomAccessFile(appStoppedFile,"r");
          try {
            int len = (int)rFile.length();
            file = new byte[len];
            rFile.seek(0);
            int readed = 0;
            int offset = 0;
            while (readed != -1 && offset < len) {
              readed = rFile.read(file, offset, len - offset);
              offset += readed;
            }
            ServiceContext.applicationStoppedContent = file;
            ServiceContext.applicationStoppedContentModified = appStoppedFile.lastModified();
          } finally {
            rFile.close();
          }
        }
      } else {
        ServiceContext.applicationStoppedContent = null;
        ServiceContext.applicationStoppedContentModified = 0;
        file = null;
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(
          location, "ASJ.web.000275",
          "Cannot send a customizable HTTP error response [503 Application Stopped]. The file [{0}] containing the response HTML code is not found. Will send a non-customized error response.", new Object[]{appStoppedFileName},
          null, null);
      }
    } catch (IOException ioe) {
      ServiceContext.applicationStoppedContent = null;
      ServiceContext.applicationStoppedContentModified = 0;
      file = null;
      //TODO:Polly type:ok
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(location, "ASJ.web.000250",
        "Cannot send a customizable HTTP error response [503 Application Stopped]. Probably the HTTP connection is lost or the file [{0}] containing the response HTML code is not accessible.", new Object[]{appStoppedFileName}, ioe,
        null, null);
    }
    if (file == null) {
      httpParameters.getResponse().sendError(new ErrorData(ResponseCodes.code_service_unavailable, 
        Responses.mess14, Responses.mess15, false, new SupportabilityData(true, "", "")));
    } else {
      httpParameters.getResponse().sendApplicationStopped(file);
    }
  }
  
  private String generateErrorReport(HTTPRequest request, HTTPResponse response, HostScope hostScope) {
  	HashMap<String, String> dataCollectorParams = new HashMap<String, String>();

  	String falseResponse = Responses.mess68;
		HashMapObjectObject parameters = getParameterValueFromRequest(request.getHTTPParameters());
		if (parameters.isEmpty()) {
			if (LogContext.getLocationRequestInfoClient().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceWarning("ASJ.web.000600",
					"Special request [/@@@GenerateErrorReport@@@] does not contain the needed parameters. " +
					"Error report will not be generated.", null, null);
			}			
			return falseResponse;
    } 
		
		String id = ((String[])parameters.get("id"))[0];
		if (id.equals("")) {
			if (LogContext.getLocationRequestInfoClient().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceWarning("ASJ.web.000601",
					"Special request [/@@@GenerateErrorReport@@@] contains the needed parameters, but they are empty. " +
					"Error report will not be generated.", null, null);
			}			
			return falseResponse;
		}
		
		String categorization = "There is no End-User's input.";
		if (parameters.containsKey("cat")) {
      categorization = ((String[]) parameters.get("cat"))[0];
      if (categorization == null || categorization.equals("")) { 
        categorization = "There is no End-User's input.";
      }
    } 
		
    ErrorReportInfoBean errorReportInfoBean = getServiceContext().getHttpProvider().getErrorReportInfos().remove(id);
		if (errorReportInfoBean == null) {
			if (LogContext.getLocationRequestInfoClient().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceWarning("ASJ.web.000602",
					"ErrorReportInfoBean is [null] for id [{0}]. Error report will not be generated.", new Object[]{id}, null, null);
			}			
			return falseResponse;			
		}
		
		if (!errorReportInfoBean.getClientIp().equals(ParseUtils.ipToString(request.getClient().getIP()))) {
			if (LogContext.getLocationRequestInfoClient().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceWarning("ASJ.web.000594",
					"ErrorReportInfoBean.getClientIp() is [{0}], but request.getClient().getIP() is [{1}]. " +
					"Error report will not be generated.", new Object[]{errorReportInfoBean.getClientIp(), ParseUtils.ipToString(request.getClient().getIP())}, null, null);
			}			
			return falseResponse;
		}
		
    long time = errorReportInfoBean.getTime();
    String aliasName = errorReportInfoBean.getWebApp();
    String applicationName = "";
		
  	String newline = "<br>";
  	
  	StringBuilder content = new StringBuilder();
		content.append("Time when error happened = ").append(date.format(new Date(time)));
		content.append(" [").append(time).append("]").append(newline);
		
		content.append("Log ID = ").append(errorReportInfoBean.getSupportabilityData().getLogId()).append(newline);
		String logIdISE500 = errorReportInfoBean.getLogIdISE500();
		if (logIdISE500 != null && !logIdISE500.equals("")) {
      content.append("Log ID ISE 500 = ").append(logIdISE500).append(newline);
    }
    content.append("Message ID = ").append(errorReportInfoBean.getSupportabilityData().getMessageId()).append(newline);
		content.append("DC name = ").append(errorReportInfoBean.getSupportabilityData().getDcName()).append(newline);
    content.append("CSN component = ").append(errorReportInfoBean.getSupportabilityData().getCsnComponent()).append(newline).append(newline);
		
	  content.append("End-User's description and categorization of the problem:").append(newline).append(categorization).append(newline).append(newline);
		
		content.append("Client ID = ").append(request.getClient().getClientId()).append(newline);
		content.append("Client IP = ").append(errorReportInfoBean.getClientIp()).append(newline);
		content.append("Host name = ").append(request.getHTTPParameters().getHostName()).append(newline).append(newline);
		
		content.append("Context root = ").append(aliasName).append(newline);	
		
		WebModule webModule = (WebModule)((WebContainerProvider)ServiceContext.getServiceContext().getWebContainer().
				getIWebContainerProvider()).getDeployedAppl(aliasName);
		if (webModule != null) {
			content.append("Application name = ").append(webModule.getWholeApplicationName()).append(newline);
			applicationName = webModule.getWholeApplicationName();
			content.append("Web application root directory = ").append(webModule.getRootDirectory()).append(newline).append(newline);
		}		
		
		ApplicationContext appScope = null;
		boolean found = false;
		if (hostScope.getHostProperties().isApplicationAlias(aliasName)) {
			appScope = ServiceContext.getServiceContext().getDeployContext().getStartedWebApplicationContext(new MessageBytes(aliasName.getBytes()));
			if (appScope != null) {
	      Enumeration allSessions = appScope.getSessionServletContext().getSession().enumerateSessions();
	      while (allSessions.hasMoreElements()) {
	      	ApplicationSession appSession = (ApplicationSession)allSessions.nextElement();
					if (errorReportInfoBean.getJsessionId().equals(appSession.getIdInternal())) { 
						found = true;
						content.append(getApplicationSessionInfo(appSession)).append(newline);
						dataCollectorParams.put("logStartTimeMillis", ((Long)appSession.getCreationTime()).toString()); // start time is the session creation time
						break;
					}	  			
	      }
			} 
		} 
				
  	File tempFile = new File(getServiceContext().getWorkDirectory(), "WebAppReport_" + id + ".html");
  	try {
  		FileUtils.writeToFile(content.toString().getBytes(), tempFile);
    } catch (Exception e) {
			if (LogContext.getLocationRequestInfoClient().beWarning()) {
				LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceWarning("ASJ.web.000593",
					"Exception occurs while trying to write to file [{0}]. Error report will not be generated.", 
					new Object[]{tempFile.getName()}, e, null, null);
			}			
    	return falseResponse;
		}
		
		if (!found) {
			dataCollectorParams.put("logStartTimeMillis", ((Long) (time - 300000)).toString()); //time minus 5 min
		}    
		dataCollectorParams.put("logEndTimeMillis", ((Long)(time + 60000)).toString()); //time plus 1 min
		dataCollectorParams.put("reportFileName", tempFile.getName());
		
    StringBuilder dataCollectorGeneralInfo = new StringBuilder();
    dataCollectorGeneralInfo.append("Creating Server Side snapshot with DataCollector call with dataset: errorreport").append('\n'); 
    dataCollectorGeneralInfo.append("End-User's description and categorization of the problem: ").append(categorization).append('\n');
    dataCollectorGeneralInfo.append("Message ID: ").append(errorReportInfoBean.getSupportabilityData().getMessageId()).append('\n');
    dataCollectorGeneralInfo.append("Internal categorization: ");
    int categorizationId = ErrorReportInfoBean.getInternalCategorization(errorReportInfoBean.getResponseCode(),errorReportInfoBean.getSupportabilityData(), aliasName, applicationName);
    dataCollectorGeneralInfo.append(categorizationId).append('\n');
    
    //Check the specified timeout and generate error report if it expired 
    //The idea is to prevent flooding the system with similar data collections.
    long timeout = ServiceContext.getServiceContext().getHttpProvider().getHttpProperties().getGenerateNewErrorReportTimeout();
    if (ehp.getValidityPeriod() < timeout){
    	ehp.setValidityPeriod(timeout);
    }
    if (timeout <= 0 || isTimeoutExpired(timeout, categorizationId)){
    	String fileName = "";
			try {
				fileName = DataCollectorHelper.createSnapshot(dataCollectorGeneralInfo.toString(), "errorreport", dataCollectorParams);
				addCategorizationEntry(categorizationId, fileName);
				if (errorReportInfoBean.getResponseCode()== 500 && ServiceContext.getServiceContext().getWebMonitoring().isMonitoringStarted()) {
					ServiceContext.getServiceContext().getWebMonitoring().addErrorReportToCategorizationID(categorizationId, fileName);
				}
			} catch (IOException e) {
				LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceError("ASJ.web.000592",
						"Exception occurs while invoking DataCollectorHelper.createSnapshot([{0}], [{1}], [{2}]). Error report will not be generated.", 
						new Object[]{dataCollectorGeneralInfo.toString(), "errorreport", dataCollectorParams}, e, null, null);
				return falseResponse;
			}
			return Responses.mess70.replace("{NAME}",fileName);
    }
    String lastErrorReportName = getLastErrorReportName(categorizationId);

    return Responses.mess69.replace("{NAME}", lastErrorReportName).replace("{TIMEOUT}", String.valueOf(timeout));    
   
  }//end of checkGenerateErrorReport(HTTPRequest request, HTTPResponse response, HostScope hostScope)
  
  
  /**
   * Adds error categorization entries to allErrorCategorizationEntries
   */
  private void addCategorizationEntry(int categorizationId, String fileName){
	  ErrorCategorizationEntry catEntry = allErrorCategorizationEntries.get(categorizationId);
	  if (catEntry!= null){
		  catEntry.newErrorReport(fileName);
	  }else{
		  allErrorCategorizationEntries.put(categorizationId, new ErrorCategorizationEntry(categorizationId, fileName));
	  }
  }
  
  
  /**
   * Returns the last error report that was added to the categorization entry with the specified id.
   * If there is no such entry or no files are added to the existing one, the method returns null.
   * @param categorizationId
   * @return
   */
 private String getLastErrorReportName(int categorizationId){
	  ErrorCategorizationEntry catEntry = allErrorCategorizationEntries.get(categorizationId);
	  if (catEntry!= null){
		  return catEntry.getLastErrorReportName();
	  }
	  return null;
  }
  
  /**
   * Returns the time the last error report file name was added to the categorization entry with the specified id.
   * @param categorizationId
   * @return
   */
  private Date getLastErrorReportDate(int categorizationId){
	  ErrorCategorizationEntry catEntry = allErrorCategorizationEntries.get(categorizationId);
	  if (catEntry!= null){
		  return catEntry.getLastErrorReportTime();
	  }
	  return null;
  }
  
  private boolean isTimeoutExpired(long timeoutSeconds, int categorizationId){
	  Date lastErrorReportDate = getLastErrorReportDate(categorizationId);
	  Date currentDate = new Date();
	  if (lastErrorReportDate==null || (currentDate.getTime() - lastErrorReportDate.getTime() >= timeoutSeconds*1000)) {
			return true;
	  }else {
			return false;
	  }
  }
  
  private HashMapObjectObject getParameterValueFromRequest(HttpParameters request) {
		String characterEncoding = null;
		try {
			characterEncoding = WebParseUtils.parseEncoding(request);
			if (characterEncoding == null || characterEncoding.equals("")) {
				characterEncoding = Constants.DEFAULT_CHAR_ENCODING;
			}

			// Ensure that the specified encoding is valid
			byte buffer[] = new byte[1];
			buffer[0] = (byte) 'a';
			new String(buffer, characterEncoding);
		} catch (UnsupportedEncodingException e) {
			characterEncoding = Constants.DEFAULT_CHAR_ENCODING;
		}

		HashMapObjectObject parameters = new HashMapObjectObject();
		if (request.getRequest().getRequestLine().getQuery() != null) {
			try {
				WebParseUtils.parseQueryString(parameters, request.getRequest().getRequestLine().getQuery().getBytes(), characterEncoding);
			} catch (UnsupportedEncodingException e) {
				if (LogContext.getLocationRequestInfoClient().beWarning()) {
					LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_CLIENT).traceWarning("ASJ.web.000442",
						"Cannot parse the parameters of the request. Incorrect encoding [{0}] specified in it. " + 
						"Error report will not be generated.", new Object[] { characterEncoding }, e, null, null);
				}				
			}
		}

		return parameters;
	}//end of getParameterValueFromRequest(HttpParameters request)

  private String getApplicationSessionInfo(ApplicationSession applicationSession) {
    StringBuilder content = new StringBuilder();
  	String newline = "<br>";
  	content.append("Application session: ").append(newline);
    if (ClientContextImpl.getByClientId(applicationSession.getIdInternal()) != null) {
    	content.append("- User Name = ").
      append(ClientContextImpl.getByClientId(applicationSession.getIdInternal()).getUser()).append(newline);
    }          
    content.append("- ID = ").append(applicationSession.getIdInternal()).append(newline);
    content.append("- Creation time = ").append(date.format(new Date(applicationSession.getCreationTime())));
    content.append(" [").append(applicationSession.getCreationTime()).append("]").append(newline);
    content.append("- Last access time = ").append(date.format(new Date(applicationSession.getLastAccessedTime())));
    content.append(" [").append(applicationSession.getLastAccessedTime()).append("]").append(newline);
    content.append("- Expiration date = ");
    String expire = "never";
    if (applicationSession.getMaxInactiveInterval() != -1) {
      expire = date.format(new Date(applicationSession.getLastAccessedTimeInternal() + applicationSession.getMaxInactiveInterval() * 1000));
    }
    content.append(expire).append(newline);
		content.append("- Is sticky = ").append(applicationSession.isSticky()).append(newline);
		content.append("- Attributes: ").append(newline);
		Enumeration attributeNames = applicationSession.getAttributeNames();
		while (attributeNames.hasMoreElements()) {
			String key = (String)attributeNames.nextElement();
			content.append("  - Name = ").append(key).append(newline);
		}

		return content.toString();
  }//end of getApplicationSessionInfo(ApplicationSession applicationSession)
  
}//end of class
