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

package com.sap.engine.services.timeout;

import com.sap.engine.frame.state.ManagementInterface;

/**
 * @author Miroslav Petrov
 * @version 6.30
 */
public interface TimeoutManagementInterface extends ManagementInterface {

  public int getRegisteredListenersCount();

  public int getEstimatedFrequencyPerMinute();

}
