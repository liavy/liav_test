/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.espbase.espsrv;

/**
 * @author Nikolai Dimitrov
 * @version ENHP1
 */
public class ESPTechnicalException extends Exception {

  public ESPTechnicalException() {
  }

  public ESPTechnicalException(String message) {
    super(message);
  }

  public ESPTechnicalException(String message, Throwable cause) {
    super(message, cause);
  }

  public ESPTechnicalException(Throwable cause) {
    super(cause);
  }
}
