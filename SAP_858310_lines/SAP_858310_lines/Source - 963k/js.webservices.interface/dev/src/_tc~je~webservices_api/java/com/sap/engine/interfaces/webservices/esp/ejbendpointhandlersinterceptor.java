/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.interfaces.webservices.esp;

import java.util.Map;

/**
 * This interface was provided becase of the necessity JAX-RPC handlers, associated with EJB endpoint,
 * to be invoked under transaction, security and naming context of the concrete EJB endpoint. It provides
 * methods which are dedicated to be invoked by the EJB container before and after business method invocation.  
 * 
 * Copyright (c) 2005, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2006-7-21
 */
public interface EJBEndpointHandlersInterceptor {
  /**
   * This method should be invoked before invocation of endpoint business method.
   * 
   * @return true if processing should continue with invocation of endpoint business method. If false
   *         endpoint business method should not be invoked. 
   */
  public boolean preInvoke();  
  /**
   * This method should be invoked after invocation of endpoint business method.
   * 
   * @param result the Object returned by business method invocation. In case of void method or abnormal method termination, null must be passed.
   * @param thr in case of abnormal method termination, the throwable object thrown by the business method must be passed, or null otherwise. 
   */
  public void postInvoke(Object result, Throwable thr);
  /**
   * Returns JAXWS's MessageContext object, associated with the current call. If the current call is not
   * to a jaxws endpoint, null is returned. 
   * @return
   */
  public Map<String, Object> getJAXWSMessageContext();
}
