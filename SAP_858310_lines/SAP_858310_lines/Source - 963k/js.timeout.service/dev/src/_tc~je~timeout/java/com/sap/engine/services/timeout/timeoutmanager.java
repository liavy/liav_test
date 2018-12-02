/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * of SAP AG, Walldorf.. You shall not disclose such Confidential
 * This software is the confidential and proprietary information
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.timeout;

/**
 * Public interface TimeoutManager.
 * A facility for threads to schedule tasks for future execution in a background thread.
 * Tasks may be registered for one-time execution, or for repeated execution at regular intervals.
 *
 * Corresponding to each TimeoutManager object is a single background thread that is used to execute
 * all of the TimeoutManager's tasks.
 *
 * @author Georgi Stanev, Jasen Minov, Hristo Iliev
 * @version 6.30 July 2002
 */
public interface TimeoutManager {

  /**
   * Registers TimeoutListner.
   *
   * @param   listener - the TimeoutListener to be registered.
   * @param   delayTime - Shows after how much time in milliseconds
   *           the <code>timeout</code> method of
   *           the <code>listener</code> will be invoked.
   * @param   repeatTime  - if is <= 0 than the the <code>timeout</code>
   *          method of the the <code>listener</code> will be invoked only once,
   *          otherwise it shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   */
  public void registerTimeoutListener(TimeoutListener listener, long delayTime, long repeatTime);

  /**
   * Registers TimeoutListner.
   *
   * @param   listener - the TimeoutListener to be registered.
   * @param   delayTime - Shows after how much time in milliseconds
   *           the <code>timeout</code> method of
   *           the <code>listener</code> will be invoked.
   * @param   repeatTime  - if is <= 0 than the the <code>timeout</code>
   *          method of the the <code>listener</code> will be invoked only once,
   *          otherwise it shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   * @param   systemThread - if the parameter is true the thread that is
   *          launched on timeout is system, elswhere it is application thread
   */
  public void registerTimeoutListener(TimeoutListener listener, long delayTime, long repeatTime, boolean systemThread);

  /**
   * Registers TimeoutListner.
   *
   * @param   listener - the TimeoutListener to be registered.
   * @param   waitForTimeoutEvent - If TRUE waits for the timeout event to finish
   * @param   delayTime - Shows after how much time in milliseconds
   *           the <code>timeout</code> method of
   *           the <code>listener</code> will be invoked.
   * @param   repeatTime  - if is <= 0 than the the <code>timeout</code>
   *          method of the the <code>listener</code> will be invoked only once,
   *          otherwise it shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   */
  public void registerTimeoutListener(TimeoutListener listener, boolean waitForTimeoutEvent, long delayTime, long repeatTime);

  /**
   * Registers TimeoutListner.
   *
   * @param   listener - the TimeoutListener to be registered.
   * @param   waitForTimeoutEvent - If TRUE waits for the timeout event to finish
   * @param   delayTime - Shows after how much time in milliseconds
   *           the <code>timeout</code> method of
   *           the <code>listener</code> will be invoked.
   * @param   repeatTime  - if is <= 0 than the the <code>timeout</code>
   *          method of the the <code>listener</code> will be invoked only once,
   *          otherwise it shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   * @param   systemThread - if the parameter is true the thread that is
   *          launched on timeout is system, elswhere it is application thread
   */
  public void registerTimeoutListener(TimeoutListener listener, boolean waitForTimeoutEvent, long delayTime, long repeatTime, boolean systemThread);

  /**
   * Registers TimeoutListner.
   *
   * @param   listener - the TimeoutListener to be registered.
   * @param   delayTime - Shows after how much time in milliseconds
   *           the <code>timeout</code> method of
   *           the <code>listener</code> will be invoked.
   * @param   repeatTime  - if is <= 0 than the the <code>timeout</code>
   *          method of the the <code>listener</code> will be invoked only once,
   *          otherwise it shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   * @param   occurrences number of occurrences of the timeout event in case of periodic events
   *          i.e. repeatTime &gt; 0. A value &lt;= 0 means unlimited occurrences.
   */
  public void registerTimeoutListener(TimeoutListener listener, long delayTime, long repeatTime, long occurrences);

  /**
   * Registers TimeoutListner.
   *
   * @param   listener - the TimeoutListener to be registered.
   * @param   delayTime - Shows after how much time in milliseconds
   *           the <code>timeout</code> method of
   *           the <code>listener</code> will be invoked.
   * @param   repeatTime  - if is <= 0 than the the <code>timeout</code>
   *          method of the the <code>listener</code> will be invoked only once,
   *          otherwise it shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   * @param   occurrences number of occurrences of the timeout event in case of periodic events
   *          i.e. repeatTime &gt; 0. A value &lt;= 0 means unlimited occurrences.
   * @param   systemThread - if the parameter is true the thread that is
   *          launched on timeout is system, elswhere it is application thread
   */
  public void registerTimeoutListener(TimeoutListener listener, long delayTime, long repeatTime, long occurrences, boolean systemThread);

  /**
   * Registers TimeoutListner.
   *
   * @param   listener - the TimeoutListener to be registered.
   * @param   waitForTimeoutEvent - If TRUE waits for the timeout event to finish
   * @param   delayTime - Shows after how much time in milliseconds
   *           the <code>timeout</code> method of
   *           the <code>listener</code> will be invoked.
   * @param   repeatTime  - if is <= 0 than the the <code>timeout</code>
   *          method of the the <code>listener</code> will be invoked only once,
   *          otherwise it shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   * @param   occurrences number of occurrences of the timeout event in case of periodic events
   *          i.e. repeatTime &gt; 0. A value &lt;= 0 means unlimited occurrences.
   */
  public void registerTimeoutListener(TimeoutListener listener, boolean waitForTimeoutEvent, long delayTime, long repeatTime, long occurrences);

  /**
   * Registers TimeoutListner.
   *
   * @param   listener - the TimeoutListener to be registered.
   * @param   waitForTimeoutEvent - If TRUE waits for the timeout event to finish
   * @param   delayTime - Shows after how much time in milliseconds
   *           the <code>timeout</code> method of
   *           the <code>listener</code> will be invoked.
   * @param   repeatTime  - if is <= 0 than the the <code>timeout</code>
   *          method of the the <code>listener</code> will be invoked only once,
   *          otherwise it shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   * @param   occurrences number of occurrences of the timeout event in case of periodic events
   *          i.e. repeatTime &gt; 0. A value &lt;= 0 means unlimited occurrences.
   * @param   systemThread - if the parameter is true the thread that is
   *          launched on timeout is system, elswhere it is application thread
   */
  public void registerTimeoutListener(TimeoutListener listener, boolean waitForTimeoutEvent, long delayTime, long repeatTime, long occurrences, boolean systemThread);

  /**
   * Unregisters already registered TimeoutListener.
   *
   * @param   listener - the TimeoutListener to be unregistered
   */
  public void unregisterTimeoutListener(TimeoutListener listener);

  /**
   * Refreshes the timeout of the <code>listener</code>, It as equivalent
   * to unregister it and to register it again with the same delayTime.
   *
   * @param   listener - TimeoutListener, whcih have to be refresh
   */
  public void refreshTimeout(TimeoutListener listener);

  /**
   * Changes the repeat time of already registered TimeoutListener with
   * positive repeat time.
   *
   * @param   listener - the TimoutListener, which already have been registered
   *          with positive repeat time
   * @param   repeatTime -  the new value of the repeat time.
   *          It shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   */
  public void changeRepeatTime(TimeoutListener listener, long repeatTime);

  /**
   * Changes the repeat time and the number of occurrences of an already registered
   * TimeoutListener with positive repeat time.
   *
   * @param   listener - the TimoutListener, which already have been registered
   *          with positive repeat time
   * @param   repeatTime -  the new value of the repeat time.
   *          It shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   * @param   occurrences number of occurrences of the timeout event in case of periodic events
   *          i.e. repeatTime &gt; 0. A value &lt;= 0 means unlimited occurrences.
   */
  public void changeRepeatTime(TimeoutListener listener, long repeatTime, long occurrences);

  /**
   * Registers synchronous TimeoutListner. The <code>listener.timeout()<code> method is not executing in new thread.
   *
   * @param   listener - the TimeoutListener to be registered.
   * @param   delayTime - Shows after how much time in milliseconds
   *           the <code>timeout</code> method of
   *           the <code>listener</code> will be invoked.
   * @param   repeatTime  - if is <= 0 than the the <code>timeout</code>
   *          method of the the <code>listener</code> will be invoked only once,
   *          otherwise it shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   */
  public void registerSynchronousTimeoutListener(TimeoutListener listener, long delayTime, long repeatTime);

  /**
   * Registers synchronous TimeoutListner. The <code>listener.timeout()<code> method is not executing in new thread.
   *
   * @param   listener - the TimeoutListener to be registered.
   * @param   waitForTimeoutEvent - If TRUE waits for the timeout event to finish
   * @param   delayTime - Shows after how much time in milliseconds
   *           the <code>timeout</code> method of
   *           the <code>listener</code> will be invoked.
   * @param   repeatTime  - if is <= 0 than the the <code>timeout</code>
   *          method of the the <code>listener</code> will be invoked only once,
   *          otherwise it shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   */
  public void registerSynchronousTimeoutListener(TimeoutListener listener, boolean waitForTimeoutEvent, long delayTime, long repeatTime);

  /**
   * Registers synchronous TimeoutListner. The <code>listener.timeout()<code> method is not executing in new thread.
   *
   * @param   listener - the TimeoutListener to be registered.
   * @param   delayTime - Shows after how much time in milliseconds
   *           the <code>timeout</code> method of
   *           the <code>listener</code> will be invoked.
   * @param   repeatTime  - if is <= 0 than the the <code>timeout</code>
   *          method of the the <code>listener</code> will be invoked only once,
   *          otherwise it shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   * @param   occurrences number of occurrences of the timeout event in case of periodic events
   *          i.e. repeatTime &gt; 0. A value &lt;= 0 means unlimited occurrences.
   */
  public void registerSynchronousTimeoutListener(TimeoutListener listener, long delayTime, long repeatTime, long occurrences);

  /**
   * Registers synchronous TimeoutListner. The <code>listener.timeout()<code> method is not executing in new thread.
   *
   * @param   listener - the TimeoutListener to be registered.
   * @param   waitForTimeoutEvent - If TRUE waits for the timeout event to finish
   * @param   delayTime - Shows after how much time in milliseconds
   *           the <code>timeout</code> method of
   *           the <code>listener</code> will be invoked.
   * @param   repeatTime  - if is <= 0 than the the <code>timeout</code>
   *          method of the the <code>listener</code> will be invoked only once,
   *          otherwise it shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   * @param   occurrences number of occurrences of the timeout event in case of periodic events
   *          i.e. repeatTime &gt; 0. A value &lt;= 0 means unlimited occurrences.
   */
  public void registerSynchronousTimeoutListener(TimeoutListener listener, boolean waitForTimeoutEvent, long delayTime, long repeatTime, long occurrences);

  /**
   * Unregisters already registered synchronous TimeoutListener.
   *
   * @param   listener - the TimeoutListener to be unregistered
   */
  public void unregisterSynchronousTimeoutListener(TimeoutListener listener);

  /**
   * Refreshes the timeout of the synchronous <code>listener</code>, It as equivalent
   * to unregister it and to register it again with the same delayTime.
   *
   * @param   listener - TimeoutListener, whcih have to be refresh
   */
  public void refreshSynchronousTimeout(TimeoutListener listener);

  /**
   * Changes the repeat time of already registered synchronous TimeoutListener with
   * positive repeat time.
   *
   * @param   listener - the TimoutListener, which already have been registered
   *          with positive repeat time
   * @param   repeatTime -  the new value of the repeat time.
   *          It shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   */
  public void changeSynchronousRepeatTime(TimeoutListener listener, long repeatTime);

  /**
   * Changes the repeat time and the number of occurrences of an already registered
   * synchronous TimeoutListener with positive repeat time.
   *
   * @param   listener - the TimoutListener, which already have been registered
   *          with positive repeat time
   * @param   repeatTime -  the new value of the repeat time.
   *          It shows how much will be the time in milliseconds
   *          between two successive calls of the <code>timeout</code> method of
   *          the <code>listener</code>
   * @param   occurrences number of occurrences of the timeout event in case of periodic events
   *          i.e. repeatTime &gt; 0. A value &lt;= 0 means unlimited occurrences.
   */
  public void changeSynchronousRepeatTime(TimeoutListener listener, long repeatTime, long occurrences);

  /**
   * Returns all registered timeout listeners objects.
   *
   * @return   timeout listeners from timeout queue.
   */
  public TimeoutListener[] listRegisteredTimeoutListeners();

  /**
   * Returns all registered synchronous timeout listeners objects.
   *
   * @return   timeout listeners from synchronous timeout queue.
   */
  public TimeoutListener[] listRegisteredSinchronousTimeoutListeners();

}

