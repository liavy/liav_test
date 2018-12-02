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
package com.sap.engine.services.servlets_jsp;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * This interface gives statistics about the current requests processed by the web container.
 * It gives details about http sessions, security sessions,
 * number of requests and responses and the time needed for processing the requests.
 *
 * @author    Maria Jurova
 * @version   6.30
 */
public interface Monitoring {
  /**
   * Gives the number of currently available http sessions in all web applications started in the web container.
   *
   * @return    The number of http sessions available in the web container
   * @throws RemoteException  If some exception occurs in remote communication
   * 
   * @deprecated
   * Session management is now responsible for collecting such information
   */
  @Deprecated
  public int getCurrentHttpSessions() throws RemoteException;

  /**
   * Returns the number of all http sessions invalidated by an application itself.
   * I.e. these that haven't expired but have been internally invalidated.
   * The number is considered from the starting of the web container.
   *
   * @return    The number of all http sessions invalidated by an application
   * @throws RemoteException
   * 
   * @deprecated
   * Session management is now responsible for collecting such information
   */
  @Deprecated
  public long getHttpSessionsInvalidatedByApplication() throws RemoteException;

  /**
   * Returns the number all http sessions that have been expired.
   * I.e. these not invalidated by an application but timed out because the user hadn't access them for a timeout.
   * The number is considered from the starting of the web container.
   *
   * @return    The number of the expired http sessions
   * @throws RemoteException  If some exception occurs in remote communication
   * 
   * @deprecated
   * Session management is now responsible for collecting such information
   */
  @Deprecated
  public long getTimedOutHttpSessions() throws RemoteException;

  /**
   * Gives the number of currently available security sessions in all web applications started in the web container.
   *
   * @return    The number of currently available security sessions
   * @throws RemoteException  If some exception occurs in remote communication
   * 
   * @deprecated
   * Session management is now responsible for collecting such information
   */
  @Deprecated
  public int getCurrentSecuritySessions() throws RemoteException;

  /**
   * Returns the number of all security sessions invalidated by an application itself.
   * I.e. these that have been internally invalidated in invalidating the respective http session.
   * The number is considered from the starting of the web container.
   *
   * @return    The number of the security sessions invalidated by some application
   * @throws RemoteException  If some exception occurs in remote communication
   * 
   * @deprecated
   * Session management is now responsible for collecting such information
   */
  @Deprecated
  public long getSecuritySessionsInvalidatedByApplication() throws RemoteException;

  /**
   * Returns the number all security sessions that have been expired.
   * The number is considered from the starting of the web container.
   *
   * @return    The number of the expired security sessions
   * @throws RemoteException  If some exception occurs in remote communication
   * 
   * @deprecated
   * Session management is now responsible for collecting such information
   */
  @Deprecated
  public long getTimedOutSecuritySessions() throws RemoteException;

  /**
   * Returns the number of http requests received by the web container.
   * I.e. these that has been processed by servlets or filters or that are currently beeing processed.
   * The number is considered from the starting of the web container.
   *
   * @return    The number of http requests processed by the web container
   * @throws RemoteException  If some exception occurs in remote communication
   */
  public long getAllRequestsCount() throws RemoteException;

  /**
   * Returns the number of http responses returned by the web container.
   * I.e. these that has been processed by servlets or filters.
   * The number is considered from the starting of the web container.
   *
   * @return    The number of completed http responses returned by the web container
   * @throws RemoteException  If some exception occurs in remote communication
   */
  public long getAllResponsesCount() throws RemoteException;

  /**
   * Returns the total response time needed for the web container to process all its requests.
   * The time is considered from the starting of the web container. The time needed for processing a single request
   * is added to the total.
   *
   * @return    The total response time needed for the web container to process all its requests
   * @throws RemoteException  If some exception occurs in remote communication
   */
  public long getTotalResponseTime() throws RemoteException;
  
  /**
   * Returns the number of http responses with response code Internal Server Error 500 that are returned by the web container.
   * @return the number of http responses with response code Internal Server Error 500 that are returned by the web container.
   */
  public long getError500Count()throws RemoteException;
  
  /**
   * All the error 500 categorization entries are returned through a Serializable[][] object. 
   * Each line of this table represents a categorization entry and contains three columns which are as follows:
   * |categorization ID | number of occurrences | list of error report file names |
   * All the categorization entries are sorted in descending order by the number of occurrences of each categorization id.
   */
  public Serializable[][] getError500CategorizationEntries()throws RemoteException;

}
