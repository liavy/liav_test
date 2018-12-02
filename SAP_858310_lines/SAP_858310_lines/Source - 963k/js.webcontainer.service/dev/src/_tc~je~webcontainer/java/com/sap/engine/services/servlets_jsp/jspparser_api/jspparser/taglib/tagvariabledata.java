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
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains additional information about actions used in jsp as
 * name of the instance of the main class of the action and name of the variable
 * responsible for result of the action (i.e. as EVAL_BODY_INCLUDE). This class
 * is used when the jsp file is parsing and java file is generating.
 *
 * @author Maria Jurova
 */
public class TagVariableData {

  /**
   * Name of the instance of the main class of the used action
   */
  public String tagHandlerInstanceName = null;
  /**
   * A name of the variable indicating if the body will be evaluated from the action class or not
   */
  public String tagEvalVarName = null;

  public boolean isSimpleTag = false;

  /**
   * A list of all variables with scope AT_BEGIN and AT_END declared in this tag
   */
  public List<String> declaredVariables = null;
  /**
   * A list of all variables with scope AT_BEGIN and AT_END that are
   * redeclared in this tag
   * Variable redeclaration is neccessary when tag implementation is taken out
   * in a separate method, or in invoke method for SimpleTags (when the body is a fragment)
   * Since the variables may be previously declared by some other tag, they are out of scope
   * in this tag's method 
   */
  public List<String> redeclaredVariables = null;

  /**
   * Initiates the instance with the names of the main class instance and name of the variable
   * indicating if the body will be evaluated.
   *
   * @param   tagHandlerInstanceName
   * @param   tagEvalVarName
   */
  public TagVariableData(String tagHandlerInstanceName, String tagEvalVarName, boolean isSimpleTag) {
    this.tagHandlerInstanceName = tagHandlerInstanceName;
    this.tagEvalVarName = tagEvalVarName;
    this.isSimpleTag = isSimpleTag;

    declaredVariables = new ArrayList<String>();
    redeclaredVariables = new ArrayList<String>();
  }
  
  /**
   * If this TagVariableData is the NULL object created in com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.Parser
   * @return
   */
  public boolean isRootTag() {
    return tagHandlerInstanceName == null &&  tagEvalVarName == null;
  }

}

