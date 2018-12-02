package com.sap.engine.services.webservices.espbase.client.api;

import java.io.OutputStream;

public interface LoggingManagementInterface {
    public void suppressErrorCauseLogging(boolean suppress);
    public boolean isErrorCauseLoggingSuppressed();
    public void startLogging(OutputStream requestLog, OutputStream responseLog);
    public void stopLogging();	
}
