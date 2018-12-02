package com.sap.engine.services.servlets_jsp.server.exceptions;

import com.sap.engine.services.deploy.container.migration.exceptions.CMigrationException;
import com.sap.localization.LocalizableTextFormatter;

import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * @author Violeta Georgieva
 * @version 7.1
 */
public class WebCMigrationException extends CMigrationException {
  public static String CANNOT_GET_OR_CREATE_SUBCONFIG = "servlet_jsp_0560";
  public static String CANNOT_CONVERT_AND_STORE_ALTDD = "servlet_jsp_0561";
  public static String CANNOT_CONVERT_AND_STORE_WAR = "servlet_jsp_0562";
  public static String CANNOT_CREATE_SECURITY_RESOURCES = "servlet_jsp_0563";
  public static String CANNOT_GET_SECURITY_SUBCONFIG = "servlet_jsp_0564";
  public static String CANNOT_STORE_WEB_DDS = "servlet_jsp_0565";
  public static String CANNOT_ADD_NEW_SECURITY_PROPERTIES = "servlet_jsp_0566";
  public static String CANNOT_REMOVE_AUTH_METHOD_AND_AUTH_TEMPLATE = "servlet_jsp_0567";
  public static String CANNOT_STORE_WEB_DD_OBJECT_2_DBASE = "servlet_jsp_0568";
  public static String CANNOT_STORE_APP_META_DATA_OBJECT_2_DBASE = "servlet_jsp_0569";

  public WebCMigrationException(String s, Object[] args, Throwable t) {
		super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s, args), t);
  }//end of constructor

  public WebCMigrationException(String s, Throwable t) {
		super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s), t);
  }//end of constructor

  public WebCMigrationException(String s, Object[] args) {
		super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s, args));
  }//end of constructor

  public WebCMigrationException(String s) {
		super(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(), s));
  }//end of constructor

  private Object writeReplace(){
    StringWriter stringWriter = new StringWriter();
    printStackTrace(new PrintWriter(stringWriter, true));
    return new CMigrationException(stringWriter.toString());
  }//end of writeReplace()

}//end of class
