/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.frame.client;

import com.sap.engine.frame.core.thread.ContextObjectNameIterator;

/**
 *
 *
 * @author Krasimir Semerdzhiev (krasimir.semerdzhiev@sap.com)
 * @version 6.30
 */
public class ContextObjectNameIteratorImpl implements ContextObjectNameIterator {

  Object[] names = null;
  int count;
  int index;

  public ContextObjectNameIteratorImpl(Object[] nms) {
    this(nms, nms.length);
  }

  public ContextObjectNameIteratorImpl(Object[] nms, int count) {
    index = 0;
    names = nms;
    this.count = count;
  }

  /**
   *  Returns true if there are more cotext objects
   *
   */
  public boolean hasNext() {
    return index < count;
  }

  /**
   *  Returns the name of next context object
   *
   */
  public String nextName() {
    return (String)names[index++];
  }

  /**
   *  Release iterator
   *
   */
  public void releaseIterator() {
    names = null;
  }

}

