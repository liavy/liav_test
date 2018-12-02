/*
 * Copyright (c) 2002 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.ts.jts;


import java.util.Properties;

import javax.jts.TransactionService;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TSIdentification;
import org.omg.CosTSPortability.Receiver;
import org.omg.CosTSPortability.Sender;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionManagerImpl;
import com.sap.engine.services.ts.jts.ots.CosTSPortability.impl.SenderReceiverImpl;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

/**
 * @author : Iliyan Nenov, ilian.nenov@sap.com
 * @version 1.0
 */
public class TransactionServiceImpl implements TransactionService {

  private static final Location LOCATION = Location.getLocation(TransactionServiceImpl.class); 

  private static Receiver receiver = null;
  private static Sender sender = null;
  private TXR_TransactionManagerImpl tManager = null;
  public static ORB orb = null;

  public TransactionServiceImpl(TXR_TransactionManagerImpl _tManager) {
    tManager = _tManager;
  }

  /**
   * This method is called by the ORB to pass on to the Transaction Manager
   * its TSIdentification object and any custom properties.
   *
   * @param orb  A CORBA ORB instance
   * @param tsi  A TSIDentification object implemented by the ORB
   * @param prop Properties that contain any custom information
   */
  public void identifyORB(ORB orb, TSIdentification tsi, Properties prop) {
	TransactionServiceImpl.orb = orb;
    receiver = new SenderReceiverImpl(tManager);
    sender = (Sender)receiver;
    try {
      tsi.identify_receiver(receiver);
      tsi.identify_sender(sender);
    } catch (Exception e) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "Exception in transaction service TransactionServiceImpl.identifyORB()", e);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000066", "Exception in transaction service TransactionServiceImpl.identifyORB()");
      }
    }
  }

  /**
   * Returns the receiver of the Transaction Service
   *
   * @return an instance to JTS sender object
   */
  public static Sender getSender() {
    return sender;
  }

  /**
   * Returns the Sender of the Transaction Service
   *
   * @return an instance to a JTS receiver object
   */
  public static Receiver getReceiver() {
    return receiver;
  }

}

