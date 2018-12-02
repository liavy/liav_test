package com.sap.engine.interfaces.webservices.server.deploy;

import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfaceResourceAccessor;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.localization.LocalizableTextFormatter;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Title: WSDeploymentException
 * Description: Such exception is thrown to indicate, that some errors have occured during the execution of any of the deployment or life cycle management phases.
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSDeploymentException extends DeploymentException {

  public WSDeploymentException() {
    this(null, null, null, Severity.NONE, null);         
  }
  
  public WSDeploymentException(String s) {
    this(new LocalizableTextFormatter(WSInterfaceResourceAccessor.getResourceAccessor(), WSInterfacePatternKeys.EXCEPTION_TEXT, new Object[]{s}), null, null, Severity.NONE, null);         
  } 
    
  public WSDeploymentException(String s, Throwable cause) {
    this(new LocalizableTextFormatter(WSInterfaceResourceAccessor.getResourceAccessor(), WSInterfacePatternKeys.EXCEPTION_TEXT, new Object[]{s}), cause, null, Severity.NONE, null);         
  } 
   
  public WSDeploymentException(Throwable cause) {
    this(new LocalizableTextFormatter(), cause, null, Severity.NONE, null);         
  }

  public WSDeploymentException(String patternKey, Object[] args) {
    this(new LocalizableTextFormatter(WSInterfaceResourceAccessor.getResourceAccessor(), patternKey, args), null,  null, Severity.NONE, null);     
  }

  public WSDeploymentException(String patterKey, Object[] args, Throwable cause) {
    this(new LocalizableTextFormatter(WSInterfaceResourceAccessor.getResourceAccessor(), patterKey, args), cause, null, Severity.NONE, null);     
  }  
 
  public WSDeploymentException(LocalizableTextFormatter locFormatter, Throwable cause) {
    super(locFormatter, cause, null, Severity.NONE, null);
  }
    
  public WSDeploymentException(LocalizableTextFormatter locFormatter, Throwable cause, Category cat, int severity, Location loc) {
    super(locFormatter, cause, cat, severity, loc);
  }

  private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new Exception(stringWriter.toString());
  }

}