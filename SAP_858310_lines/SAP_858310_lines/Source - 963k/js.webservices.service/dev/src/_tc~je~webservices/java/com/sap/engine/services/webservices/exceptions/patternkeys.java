package com.sap.engine.services.webservices.exceptions;

/**
 * Title: PatternKeys
 * Description: The interface specifies base exception pattern keys, used by the webservices service framework.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Bulgaria
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public interface PatternKeys {

  public String BASE_WS_GENERATION_EXCEPTION = "webservices_5001";//WS generation error! The problem is: {0}. The error refers to application: {1}, jar {2}, descriptor: {3}. Additional info: {4}.

  public static String NO_SUCH_WS_ELEMENT    = "webservices_5011";//Element {0} not found in {1}! Additional information: {2}.
  public static String WS_DUBLICATE_ELEMENT  = "webservices_5012";//There has already been registered element {0} in {1} registry! Additional information: {2}.

  public String COMPONENT_INSTANTIATION      = "webservices_5020";//Plug-in component instanciation error! The problem is: {0}. The error refers to component: {1}, type: {2}. Additional info: {3}.

  public String WS_CONFIGURATION_EXCEPTION   = "webservices_5060";//WS configuration exception! The reason is: {0}. The error refers to configuration: {1}.

  public String WS_COMMON_WARNING_EXCEPTION  = "webservices_5080";//WS common warning exception! {0}

  public String EMPTY_PATTERN                = "webservices_5996";//""
  public String DEFAULT_PATTERN              = "webservices_5997";//{0}

}
