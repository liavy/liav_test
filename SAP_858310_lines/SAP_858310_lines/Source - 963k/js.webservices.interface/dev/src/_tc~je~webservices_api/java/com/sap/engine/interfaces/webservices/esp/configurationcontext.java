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

import java.util.Iterator;   

/**
 * <p>
 * This interface provides tree-like structure for storing data in form of name-value properties. 
 * </p>
 * Copyright (c) 2004, SAP-AG
 * @author Dimitar Angelov
 * @version 1.0, 2004-9-10
 */
public interface ConfigurationContext {
  
//  /**  
//   * String used for separation of context-names. For example parantName/yourName; 
//   */
//  String CONTEXT_SEPARATOR  =  "/";
   
  /**
   * Sets property into this context.
   * 
   * @param name  the name of the property
   * @param value the value of the property
   * @return  the previous value bound under the <tt>name</tt> or <tt>null</tt> if none.
   */
  public Object setProperty(String name, Object value);
  /**
   * Returns the object bound under <tt>name</tt>.
   * 
   * @param name
   */
  public Object getProperty(String name);
  /**
   * Creates and attaches new child <tt>ConfigurationContext</tt> object to this context.
   * If context with such name already exists, it is returned.
   * 
   * @param name the name of the new context
   * @return the newly created context
   */
  public ConfigurationContext createSubContext(String name);
  /**
   * Returns the child <tt>ConfigurationContext</tt> of this context corresponding to the <tt>name</tt> parameter.  
   *  
   * @param name
   */
  public ConfigurationContext getSubContext(String name);
  /**
   * Returns the descendant <tt>ConfigurationContext</tt> of this context corresponding to the <tt>path</tt> parameter.  
   *  
   * @param path
   */
  public ConfigurationContext getSubContext(String[] path);
  /**
   * Returns the parent context, or <tt>null</tt> if there is no parent. 
   */
  public ConfigurationContext getParent(); 
  /**
   * Removes the context specifed by <tt>name</tt> parameter.
   *   
   * @param name
   * @return the removed context, or <tt>null</tt> if there is no context to remove.
   */
  public ConfigurationContext removeSubContext(String name);
  /**
   * Removes the descendant context specifed by <tt>path</tt> parameter.
   *   
   * @param path
   * @return the removed context, or <tt>null</tt> if there is no context to remove.
   */
  public ConfigurationContext removeSubContext(String[] path);
  /**
   * Returns the names of all properties bound.
   */
  public Iterator properties();
  /**
   * Returns the names of all subcontexts.
   */
  public Iterator subContexts();
  /**
   * Removes property with specified <code>name</code>,
   * and returns the value mapped under that name.
   */  
  public Object removeProperty(String name);
  /**
   * @return context name
   */    
  public String getName();
  /**
   * @return array context name, starting fron the root context up to this context
   */  
  public String[] getPath();
  /**
   * Clears all properties and children of this instance.
   * The mode and name of the context are not cleared. 
   */  
  public void clear();
  /**
   * Clears all properties of all child context.
   * The context tree structure remains unchanged.
   */
  public void clearPropertiesRecursive();
}
