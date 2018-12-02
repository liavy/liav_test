/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 *
 */
package com.sap.engine.services.ssl.dispatcher;


import com.sap.engine.frame.state.ManagementInterface;

/**
 *  This interface is used to configure SSL server sockets on dispatchers of the cluster.
 *
 * @author  Stephan Zlatarev, Svetlana Stancheva
 * @version 4.0.2
 */
public interface RuntimeInterface extends ManagementInterface {

  /**
   *  Returns a state describing the certificate expiration status.
   *  Used for monitoring.
   *
   * @return  a state as described in monitor-configuration.xml
   */
  public String getExpirationState();


  /**
   *  Returns the headers of the table of expiring certificates.
   *  Used for monitoring.
   *
   * @return  column names of the expiring certificates table.
   */
  public String[] getExpirationTableHeaders();


  /**
   *  Returns the contents of the table of expiring certificates.
   *  Used for monitoring.
   *
   * @return  a two-dimension array of strings.
   */
  public String[][] getExpirationTableContents();

}

