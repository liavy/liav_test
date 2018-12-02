package com.sap.engine.interfaces.webservices.server.deploy;

import com.sap.engine.services.deploy.container.WarningException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfaceResourceAccessor;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.localization.LocalizableTextFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;

/**
 * Title: WSWarningException
 * Description: Such exception is thrown to indicate, that some error warnings have occured during the execution of any of the deployment or life cycle management phases.
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSWarningException extends WarningException {
  
  public WSWarningException() {
    this(null, null, null, Severity.NONE, null);
  }
  
  public WSWarningException(String s) {
    this(new LocalizableTextFormatter(WSInterfaceResourceAccessor.getResourceAccessor(), WSInterfacePatternKeys.EXCEPTION_TEXT, new Object[]{s}), null, null, Severity.NONE, null);         
  } 
    
  public WSWarningException(String s, Throwable cause) {
    this(new LocalizableTextFormatter(WSInterfaceResourceAccessor.getResourceAccessor(), WSInterfacePatternKeys.EXCEPTION_TEXT, new Object[]{s}), cause, null, Severity.NONE, null);         
  } 
 
  public WSWarningException(Throwable cause) {
    this(null, cause, null, Severity.NONE, null);
  }

  public WSWarningException(String patternKey, Object[] args) {
    this(new LocalizableTextFormatter(WSInterfaceResourceAccessor.getResourceAccessor(), patternKey, args), null, null, Severity.NONE, null);
  }

  public WSWarningException(String patternKey, Object[] args, Throwable cause) {
    this(new LocalizableTextFormatter(WSInterfaceResourceAccessor.getResourceAccessor(), patternKey, args), cause, null, Severity.NONE, null);
  }

  public WSWarningException(LocalizableTextFormatter locFormatter, Throwable cause, Category cat, int severity, Location loc) {
    super(locFormatter, cause, cat, severity, loc);
  }
  
  public static WSWarningException getNewException(Vector wsWarningExceptions) {
    if(wsWarningExceptions == null || wsWarningExceptions.size() == 0) {
      return null; 
    }
    
    WSWarningException wExcResult = new WSWarningException(); 
    StringBuffer strBuffer = new StringBuffer();     
    for(int i = 0; i < wsWarningExceptions.size(); i++) {      
      strBuffer.append(((WSWarningException)wsWarningExceptions.get(i)).getLocalizedMessage());     
    }
    
    return new WSWarningException(WSInterfacePatternKeys.EXCEPTION_TEXT, new Object[]{strBuffer.toString()}); 
  }
  
  private Object writeReplace() {
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new Exception(stringWriter.toString());
  }

  public void addWarnings(String[] newWarnings) {
    String[] warnings = getWarnings();
    String[] allWarnings = unifyStrings(new String[][]{warnings, newWarnings});
    setWarning(allWarnings);
  }

  public void addWarnings(Vector newWarnings) {
    if(newWarnings == null) {
      return;
    }

    String[] newWarningsArr = new String[newWarnings.size()];
    newWarnings.copyInto(newWarningsArr);

    addWarnings(newWarningsArr);
  }

  public Vector getWarningsVector() {
    String[] warnings = getWarnings();
    if(warnings == null) {
      return new Vector();
    }

    Vector warningsVector = new Vector();
    for(int i = 0; i < warnings.length; i++) {
      warningsVector.add(warnings[i]);
    }

    return warningsVector;
  }

  public static String[] unifyStrings(String[][] strings) {
    String[] allStrings = new String[0];

    for(int i = 0; i < strings.length; i++) {
       String[] currentStrings = strings[i];
       if (currentStrings == null) {
         continue;
       }
       String[] newStrings = new String[allStrings.length + currentStrings.length];
       System.arraycopy(allStrings, 0, newStrings, 0, allStrings.length);
       System.arraycopy(currentStrings, 0, newStrings, allStrings.length, currentStrings.length);
       allStrings = newStrings;
    }

    return allStrings;
  }

}
