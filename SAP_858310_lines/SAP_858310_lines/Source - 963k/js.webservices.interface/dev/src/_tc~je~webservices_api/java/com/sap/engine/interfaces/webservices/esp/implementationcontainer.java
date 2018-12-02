/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.interfaces.webservices.esp;

import java.lang.reflect.InvocationTargetException;

import com.sap.engine.interfaces.webservices.runtime.EventObject;
import com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException;

/**
 * <p>  This is base interfacace for implementation providers. The
 * Runtime invokes its methods for implementation manipulations.
 * <code>getImplementatoinID()</code> method is invoked when the concrete
 * implementation container is registered in the ImplementationContainerManager.
 * <code>getImplementationLoader(...)</code> and <code>invokeMethod</code> methods
 * are invoked in this order at runtime processing. <code>notify(..)</code> method
 * is invoked when any of the supported events occurs.</p>
 * 
 * Copyright (c) 2004, SAP-AG
 * @author       Dimitar Angelov
 * @version      1.0, 2004-9-10
 * @see com.sap.engine.services.webservices.runtime.component.ImplementationContainerManager
 */
public interface ImplementationContainer {

  /**
   *   Invoked by the Runtime to recieve the ID of the implementation
   * which this container supports.
   *
   * @return  the ID of the implementation which
   *          this container supports.
   */
  public String getImplementationID();

  /**
   *   Invoked by the Runtime to obtain a reference to the implementation
   * specific loader through which the parameter classes decribed in the VI
   * are loaded, instantiated and initialized with the request data.
   *
   * @param  context  the context associated with the call.
   * @return  a loader through which the Runtime can load the necessary
   *          classes for the objects to be created from the XML data.
   * @throws  com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException  In case anything goes wrong.
   */
  public ClassLoader getImplementationLoader(ConfigurationContext context) throws RuntimeProcessException;

  /**
   *  The Runtime invokes this method to indicate a call of the actual
   * implementation business method. The java-method parameter Class objects are passes because
   * in case of polimorphism the parameter Object classes' are different
   * than the method parameter Classes.
   *
   * @param methodName  the javaName of the method to be invoke.
   * @param parameters  the parameters for the method created from the XML data.
   * @param parameterClasses  the classes which are described in the VI interface
   * @param context     the context associate with the call.
   *
   * @return  the Object returned by the business method invokation. In case of void method, null must be returned.
   *
   * @throws com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException  in case anything goes wrong in the container.
   * @throws java.lang.reflect.InvocationTargetException  wraps any exception which is thrown by the business method.
   */
  public Object invokeMethod(String methodName, Class[] parameterClasses, Object parameters[], ConfigurationContext context) throws RuntimeProcessException, InvocationTargetException;

  /**
   *   Through this method the Runtime notifies the container for
   * a specific event which has happened.
   *
   * @param event  the event object containing the event specific data.
   * @param context  the context associated with the call.
   *
   * @throws com.sap.engine.interfaces.webservices.runtime.RuntimeProcessException  in case anything goes wrong.
   */
  public void notify(EventObject event, ConfigurationContext context) throws RuntimeProcessException;

}