package com.sap.engine.services.webservices.espbase.client.api.impl;

import java.io.OutputStream;

import com.sap.engine.services.webservices.espbase.client.api.LoggingManagementInterface;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;

public class LoggingManagementInterfaceImpl implements LoggingManagementInterface {
	
  private ClientConfigurationContext clientContext;  
      
  /**
   * Default constructror. Pass the client context as parameter.
   */
  public LoggingManagementInterfaceImpl(ClientConfigurationContext clientContext) {
    this.clientContext = clientContext; 	     
  }
	  
  public void suppressErrorCauseLogging(boolean suppress) {
    PublicProperties.setDynamicProperty(PublicProperties.SUPPRESS_ERROR_TRACING, String.valueOf(suppress), clientContext);
  }
	
  public boolean isErrorCauseLoggingSuppressed() {
    return PublicProperties.isTrue((String) PublicProperties.getDynamicProperty(PublicProperties.SUPPRESS_ERROR_TRACING, clientContext));
  }
  
  /**
   * Starts http log of request and response using the passed streams.
   * @param requestLog
   * @param responseLog
   */
  public void startLogging(OutputStream requestLog, OutputStream responseLog) {
    if (requestLog == null) {
      this.clientContext.getDynamicContext().removeProperty(PublicProperties.P_REQUEST_LOG_STREAM);
    } else {
      this.clientContext.getDynamicContext().setProperty(PublicProperties.P_REQUEST_LOG_STREAM, requestLog);
    }
    if (responseLog == null) {
      this.clientContext.getDynamicContext().removeProperty(PublicProperties.P_RESPONSE_LOG_STREAM);
    } else {
      this.clientContext.getDynamicContext().setProperty(PublicProperties.P_RESPONSE_LOG_STREAM, responseLog);
    }
  }
  
  /**
   * Stops the HTTP logging feature.
   */
  public void stopLogging() {
    this.clientContext.getDynamicContext().removeProperty(PublicProperties.P_REQUEST_LOG_STREAM);
    this.clientContext.getDynamicContext().removeProperty(PublicProperties.P_RESPONSE_LOG_STREAM);    
  }
  
}
