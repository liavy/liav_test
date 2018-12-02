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
package com.sap.sapcontrol.client.dc.util;

/**
 * This class contains common functionalities for the
 * deploy controller client of the SAP Control web service.
 * 
 * @author Todor Stoitsev
 * @version 7.1
 */
public class DCUtils {

  public static final int DEPLOY_X_LOG_REGEX = 0;
  public static final int DEPLOY_X_TRC_REGEX = 1;
  
  private static final String[] FILE_NAMES_REGEX = {
    "deploy\\.\\d+\\.log",
    "deploy\\.\\d+\\.trc"};
  
  
  public static String getFileNameRegex(int type) {
    if(type < FILE_NAMES_REGEX.length) {
      return FILE_NAMES_REGEX[type];
    }
    return null;
  }

}
