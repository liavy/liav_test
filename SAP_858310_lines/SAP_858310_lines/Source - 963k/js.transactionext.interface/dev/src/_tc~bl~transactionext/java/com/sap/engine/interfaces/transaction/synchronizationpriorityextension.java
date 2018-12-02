/*
 * Copyright (c) 2002 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.interfaces.transaction;


/**
 * This interface is implemented from transaction synchronization listeners which
 * want to receive notifications before or after other synchronization listeners. *  
 *
 * @author Nikola Arnaudov
 * @version 6.30
 */
public interface SynchronizationPriorityExtension extends SynchronizationExtension {

  public final static int MIN_PRIORITY = 0;
  public final static int LOW_PRIORITY = 25;
  public final static int DEFAULT_PRIORITY = 50;
  public final static int HIGH_PRIORITY = 75;
  public final static int MAX_PRIORITY = 100;

  /**
 * @return priority of this synchronization listener. 
 */
public int getPriority();

}

