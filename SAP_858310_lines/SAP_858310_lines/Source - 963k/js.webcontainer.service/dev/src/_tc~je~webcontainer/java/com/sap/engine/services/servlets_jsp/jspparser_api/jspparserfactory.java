/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api;

import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParserInitializationException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Element;

/**
 * This class creates different types of parsers - portal, engine...
 * @author Todor Mollov, Bojidar Kadrev
 * 
 * @version 7.0
 *  
 */
public abstract class JspParserFactory {
  private static ConcurrentHashMapObjectObject instances = new ConcurrentHashMapObjectObject();

  /**
   * 1.0 was initial version
   *
   * 1.1 :
   * - the main change is that pageServoce method is removed, and all java code generated for jsp tags are in one method.
   * - Other change is renaming of parser.version to parser.properties, which contains besides parser version string and java encoding and jdk used for generated files.
   * 1.2 :
   * - Java/Class files will begin with parser name. This is necessary for cases where portal parser and engine parser are processing one JSP page.
   * 1.3 :
   * -  import list for generated java files, compatible with JSP 2.1
   */
  private static final String version = "1.3";

  protected static WebContainerParameters containerParameters;

  private static JspParserFactory instance;

  //singleton!
  JspParserFactory() {
  }

  /**
   * Singleton method for getting instance of the factory.  
   * @return JspParserFactory
   */
  public static JspParserFactory getInstance() {
    if (instance == null) {
      instance = new JspParserFactoryImpl();
      containerParameters = new WebContainerParameters();
    }
    return instance;
  };
  
  /**
   * Used for getting instance of specified by the method parameter type. 
   * @param name - constant. Used only for mapping name-instance.
   * @return JspParser
   * @throws JspParserInitializationException if an error occured
   */
  public JspParser getParserInstance(String name) throws JspParserInitializationException {
    JspParser jspParserProvider = (JspParser) instances.get(name);
    if (jspParserProvider == null) {
      throw new JspParserInitializationException(JspParserInitializationException.INVALID_INSTANCE_NAME, new Object[]{name});
    }
    return jspParserProvider;
  }

  /**
   * This method should be called only once. Creates instance for this parser type and maps it with the given name. 
   * @param name - the name that uniquely identifies this parser type.
   * @param handlers set of parsing elements
   * @param decorator - if null Engine instance of the parser will be created
   * @return - the newly created JspParserProvider instance
   * @throws JspParserInitializationException if an error occured
   */
  
  public JspParser registerParserInstance(String name, Element[] handlers, ComponentDecorator decorator) throws JspParserInitializationException {
    if (name == null) {
      throw new JspParserInitializationException(JspParserInitializationException.INVALID_INSTANCE_NAME, new Object[]{"null"});
    }
    JspParser jspParserProvider = new JspParser(name, handlers, decorator, containerParameters);
    instances.put(name, jspParserProvider);
    return jspParserProvider;
  }

  /**
   * Returns hardcoded String that describes current version of the parser Can
   * be compared with the version of the parser used to parse jsp files in the
   * deployed application.
   * 
   * @return String - current parser version
   */
  public static String getParserVersion() {
    return version;
  }
}