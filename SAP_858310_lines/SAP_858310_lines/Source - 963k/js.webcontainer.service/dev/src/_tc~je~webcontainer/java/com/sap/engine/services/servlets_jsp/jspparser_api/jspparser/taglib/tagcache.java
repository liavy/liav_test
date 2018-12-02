/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib;

import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A simple cache to hold results of one-time evaluation for a custom
 * tag.
 *
 * @author Maria Jurova
 */
public class TagCache {
  /**
   * A class object represented the main class of the action
   */
  private Class tagHandlerClass;
  /**
   * Contains the names as key and methods as values.
   */
  private HashMap methods = new HashMap();
  /**
   * editors
   */
  private HashMap propertyEditorMaps = new HashMap(); 

  /**
   * Initiates with the name of the action.
   *
   */
  public TagCache() {
  }

  /**
   * Returns a setter method for the parameter attrName in the class tagHandlerClass.
   *
   * @param   attrName  A name of parameter
   * @return     A setter method for this parameter
   */
  public Method getSetterMethod(String attrName) {
    return (Method) methods.get(attrName);
  }

  /**
   * Sets a main class of the action as instance of Class. Fixes the all methods of the class.
   *
   * @param   tagHandlerClass  A Class object representing the main action class
   * @exception   JspParseException
   */
  public void setTagHandlerClass(Class tagHandlerClass) throws JspParseException {
    this.tagHandlerClass = tagHandlerClass;

    try {
      PropertyDescriptor[] pd = Introspector.getBeanInfo(tagHandlerClass).getPropertyDescriptors();

      for (int i = 0; i < pd.length; i++) {
        if (pd[i].getWriteMethod() != null) {
          methods.put(pd[i].getName(),pd[i].getWriteMethod());
        }
        if (pd[i].getPropertyEditorClass() != null) {
          propertyEditorMaps.put(pd[i].getName(), pd[i].getPropertyEditorClass());
        }
      }
    } catch (IntrospectionException e) {
      throw new JspParseException(JspParseException.CANNOT_INIT_BEANINFO_FOR_THE_TAG_HANDLER, new Object[]{tagHandlerClass}, e);
    }
  }

  /**
   * Returns a refference to the main Class object representing the main action class.
   *
   * @return     Class object representing the main action class
   */
  public Class getTagHandlerClass() {
    return tagHandlerClass;
  }

  public Class getPropertyEditorClass(String attrName) {
    return (Class) propertyEditorMaps.get(attrName);
  }

}

