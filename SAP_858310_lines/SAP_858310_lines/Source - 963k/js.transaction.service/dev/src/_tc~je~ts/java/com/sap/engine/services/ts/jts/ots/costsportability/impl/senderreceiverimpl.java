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

package com.sap.engine.services.ts.jts.ots.CosTSPortability.impl;

import com.sap.engine.interfaces.transaction.TransactionExtension;
import com.sap.engine.services.ts.Log;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionImpl;
import com.sap.engine.services.ts.jta.impl2.TXR_TransactionManagerImpl;
import com.sap.engine.services.ts.jts.TransactionServiceImpl;
import com.sap.engine.services.ts.jts.ots.CosTransactions.impl.ControlImpl;
import com.sap.engine.services.ts.jts.ots.CosTransactions.impl.TransactionKey;
import com.sap.engine.services.ts.jts.ots.OTSTransaction;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

import org.omg.CORBA.Environment;
import org.omg.CORBA.WrongTransaction;
import org.omg.CosTSPortability.Receiver;
import org.omg.CosTSPortability.Sender;
import org.omg.CosTransactions.*;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import java.util.Arrays;
import java.util.Hashtable;

/**
 * This class is an entry point to the OTS Management. All iiop requests containing
 * Transaction context are forwarded to transaction interceptors which transfer PropagationContexts
 * in the message to the Sender and Receiver objects of JTS
 *
 * @author i024163
 * @version SAP J2EE Engine 6.30
 */
public class SenderReceiverImpl implements Sender, Receiver {

  private static final Location LOCATION = Location.getLocation(SenderReceiverImpl.class);

  /* We put here the propagated transactions*/
  protected static Hashtable<TransactionKey, Transaction> importedTransactions = new Hashtable<TransactionKey, Transaction>();

  /////////////////////////////////////////////////////////////////////////////////////////////
  // SERVER                      Receiver interface implementation                    SERVER //
  /////////////////////////////////////////////////////////////////////////////////////////////

  private TXR_TransactionManagerImpl tManager = null;
  /* This is an empty propagation context used to be send is some specific situations */
  private static PropagationContext nullPgpContext = new PropagationContext(0, new TransIdentity(null, null, new otid_t(-1, 0, new byte[0])), new TransIdentity[0], TransactionServiceImpl.orb.create_any());

  /**
   * Creates a new SenderReceiver. This object is used by CORBA Transaction interceptor
   * to deal with iiop messages carrying transaction context.
   *
   * @param _tManager an istance to transaction manager is passd, because dealing
   * with transactions is Transaction Manager responsibility
   */
  public SenderReceiverImpl(TXR_TransactionManagerImpl _tManager) {
    tManager = _tManager;
  }

  /**
   * Called by the transaction interceptor after a reply is received.
   *
   * @param idParam this is an unique id of the message passed by the interceptor
   * @param ctxParam PropagationContext instance carrying Coordinator and Terminator objects
   */
  public void received_request(int idParam, PropagationContext ctxParam) {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "SenderReceiverImpl.received_request({0}, {1})", new Object[]{new Integer(idParam), Log.objectToString(ctxParam)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000221", "SenderReceiverImpl.received_request({0}, {1})", new Object[]{new Integer(idParam), Log.objectToString(ctxParam)});
    }
    try {
      if (ctxParam == null || ctxParam.current == null) {
        return;
      }

      //-----------------
      TransactionExtension tx = (TransactionExtension)tManager.getTransaction();
      if (tx != null && !tx.isAlive()) {
        // check if we need for this
        return;
      }
      //------------------------------

      TransactionKey txKey = new TransactionKey(ctxParam.current.otid.tid);

      if (LOCATION.beLogged(Severity.DEBUG)) {
//        LOCATION.logT(Severity.DEBUG, "transaction key : {0}", new Object[]{TransactionKey.toString(txKey)});
        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000222", "transaction key : {0}", new Object[]{TransactionKey.toString(txKey)});
      }

      TransactionExtension impTX = (TransactionExtension)importedTransactions.get(txKey);

      if (impTX != null && impTX.isAlive()) {
        tManager.resume(impTX);
      } else {
        tManager.beginOTStransaction(ctxParam);
        importedTransactions.put(txKey, tManager.getTransaction());
      }
    } catch (NotSupportedException nse) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "Exception starting transaction.", nse);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,nse, "ASJ.trans.000030", "");
      }
    } catch (SystemException se) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "Exception starting transaction.", se);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,se, "ASJ.trans.000031", "Exception starting transaction.");
      }
    } catch (javax.transaction.InvalidTransactionException illTxE) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "Exception starting transaction.", illTxE);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,illTxE, "ASJ.trans.000032", "Exception starting transaction.");
      }
    }
  } // received_request()

  /**
   * Called by the transaction interceptor before a reply is going to be send
   *
   * @param idParam id to the message, unique number received by the IIOP implementation
   * @param ctxhParam a holder of a propagation context that is going to be send
   */
  public void sending_reply(int idParam, PropagationContextHolder ctxhParam) {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "SenderReceiverImpl.sending_reply({0}, {1})", new Object[]{new Integer(idParam), Log.objectToString(ctxhParam)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000223", "SenderReceiverImpl.sending_reply({0}, {1})", new Object[]{new Integer(idParam), Log.objectToString(ctxhParam)});
    }
    try {
      TransactionExtension tx = (TransactionExtension)tManager.getTransaction();

      if (tx instanceof OTSTransaction) {
        ctxhParam.value = ((OTSTransaction)tx).pgContext;
        return;
      }

      if (tx != null && tx.isAlive()) {
        ControlImpl ctrl = (ControlImpl)((TXR_TransactionImpl)tx).getControl();
        TransactionServiceImpl.orb.connect(ctrl);
        TransactionKey txKey = new TransactionKey(((TXR_TransactionImpl)tx).getGlobalTxIDWith_0_BranchIterator());

        if (LOCATION.beLogged(Severity.DEBUG)) {
//          LOCATION.logT(Severity.DEBUG, "transaction key : {0}", new Object[]{TransactionKey.toString(txKey)});
          SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000224", "transaction key : {0}", new Object[]{TransactionKey.toString(txKey)});
        }

        importedTransactions.put(txKey, tx);

        ctxhParam.value = new PropagationContext(86400, new TransIdentity(ctrl, ctrl, new otid_t( 0, 0, ((TXR_TransactionImpl)tx).getGlobalTxIDWith_0_BranchIterator())), new TransIdentity[0], TransactionServiceImpl.orb.create_any());
      } else {
        ctxhParam.value = null;
      }
    } catch (SystemException se) {
      if (LOCATION.beLogged(Severity.ERROR)) {
        LOCATION.traceThrowableT(Severity.ERROR, "Exception sending replay.", se);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,se, "ASJ.trans.000033", "Exception sending replay.");
      }
    }
  } // sending_reply()

  ///////////////////////////////////////////////////////////////////////////////////////////////
  // CLIENT                          Sender interface implementation                    CLIENT //
  ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Called by transaction interceptors when a reply was received
   *
   * @param idParam an unique id received by the iiop implementation
   * @param ctxParam PropagationContext that was received
   * @param envParam an environment parameter which shows if the message is carrying an exception
   * @throws WrongTransaction in case of SystemException or if we receive a reply
   * for a transaction that is no longer existing in the server
   */
  public void received_reply(int idParam, PropagationContext ctxParam, Environment envParam) throws WrongTransaction {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "SenderReceiverImpl.received_reply({0}, {1}, {2})", new Object[]{new Integer(idParam), Log.objectToString(ctxParam), Log.objectToString(envParam)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000225", "SenderReceiverImpl.received_reply({0}, {1}, {2})", new Object[]{new Integer(idParam), Log.objectToString(ctxParam), Log.objectToString(envParam)});
    }
    try {
      TransactionExtension tx = (TransactionExtension)tManager.getTransaction();

      if (tx != null && !tx.isAlive()) {
        tx = null;
      }

      if (tx != null && ctxParam.current == null) {
        if (LOCATION.beLogged(Severity.DEBUG)) {
//          LOCATION.logT(Severity.DEBUG, "SenderReceiverImpl.received_reply : ThreadContext has transaction, but received a PropagationContext with null current.");
          SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000226", "SenderReceiverImpl.received_reply : ThreadContext has transaction, but received a PropagationContext with null current.");
        }
        throw new WrongTransaction("ThreadContext has transaction, but received a PropagationContext with null current.");
      }

      if (tx == null && ctxParam.current == null) {
        return;
      }

      if (tx == null && ctxParam != null) {
        try {
          TransactionKey txKey = new TransactionKey(ctxParam.current.otid.tid);

          if (LOCATION.beLogged(Severity.DEBUG)) {
//            LOCATION.logT(Severity.DEBUG, "transaction key : {0}", new Object[]{TransactionKey.toString(txKey)});
            SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000227", "transaction key : {0}", new Object[]{TransactionKey.toString(txKey)});
          }

          TransactionExtension impTX = (TransactionExtension)importedTransactions.get(txKey);

          if (impTX != null && impTX.isAlive()) {
            tManager.resume(impTX);
          } else {
            tManager.beginOTStransaction(ctxParam);
            importedTransactions.put(txKey, tManager.getTransaction());
          }

        } catch (NotSupportedException nsE) {
          if (LOCATION.beLogged(Severity.ERROR)) {
//            LOCATION.traceThrowableT(Severity.ERROR, "Exception in received_reply.", nsE);
            SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,nsE, "ASJ.trans.000034", "Exception in received_reply.");
          }
        } catch (SystemException se) {
          if (LOCATION.beLogged(Severity.ERROR)) {
//            LOCATION.traceThrowableT(Severity.ERROR, "Exception in received_reply.", se);
            SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,se, "ASJ.trans.000035", "Exception in received_reply.");
          }
        } catch (javax.transaction.InvalidTransactionException ite) {
          if (LOCATION.beLogged(Severity.ERROR)) {
//            LOCATION.traceThrowableT(Severity.ERROR, "Exception in received_reply.", ite);
            SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,ite, "ASJ.trans.000036", "Exception in received_reply.");
          }
        }
      }

      if (tx != null && ctxParam != null) {

        boolean flag = false;
        if (tx instanceof OTSTransaction) {
          // todo ???
          flag = Arrays.equals(((OTSTransaction)tx).getTIDbytes(), ctxParam.current.otid.tid);
          flag = true;
        } else if (tx instanceof TXR_TransactionImpl) {
          flag = Arrays.equals(((TXR_TransactionImpl)tx).getGlobalTxIDWith_0_BranchIterator(), ctxParam.current.otid.tid);
        }

        if (flag) {
          if (envParam.exception() instanceof org.omg.CORBA.SystemException) {
            try {
              tManager.setRollbackOnly();
            } catch (SystemException se) {
              if (LOCATION.beLogged(Severity.DEBUG)) {
//                LOCATION.logT(Severity.DEBUG, "SenderReceiverImpl.received_reply : Received SystemException with PropagationContext and SystemException occurred while trying to setRollbackOnly the transaction in the ThreadContext");
                SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000228", "SenderReceiverImpl.received_reply : Received SystemException with PropagationContext and SystemException occurred while trying to setRollbackOnly the transaction in the ThreadContext");
              }
              if (LOCATION.beLogged(Severity.DEBUG)) {
                LOCATION.traceThrowableT(Severity.DEBUG, "Full stacktrace: ", se);
              }
              throw new WrongTransaction("Received SystemException with PropagationContext and SystemException occurred while trying to setRollbackOnly the transaction in the ThreadContext");
            }
          }
        } else {
          if (LOCATION.beLogged(Severity.DEBUG)) {
//            LOCATION.logT(Severity.DEBUG, "SenderReceiverImpl.received_reply : Nested OTSTransaction");
            SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000229", "SenderReceiverImpl.received_reply : Nested OTSTransaction");
          }
          throw new WrongTransaction("Nested OTSTransaction");
        }
      }
    } catch (SystemException se) {   // strange what will happen if SystemException is thrown
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "Exception received_reply.", se);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,se, "ASJ.trans.000037", "Exception received_reply.");
      }
    }
  } // received_reply()

  /**
   * Called by the transaction interceptor before a request is going to be send
   *
   * @param id an unique id received by the transaction interceptor
   * @param ctxh holder of propagation context tat is going to be send
   */
  public void sending_request(int id, PropagationContextHolder ctxh) {
    if (LOCATION.beLogged(Severity.DEBUG)) {
//      LOCATION.logT(Severity.DEBUG, "SenderReceiverImpl.sending_request({0}, {1})", new Object[]{new Integer(id), Log.objectToString(ctxh)});
      SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000230", "SenderReceiverImpl.sending_request({0}, {1})", new Object[]{new Integer(id), Log.objectToString(ctxh)});
    }

    TransactionExtension tx = null;
    try {
      tx = (TransactionExtension)tManager.getTransaction();
    } catch (javax.transaction.SystemException se) {
      if (LOCATION.beLogged(Severity.ERROR)) {
//        LOCATION.traceThrowableT(Severity.ERROR, "Exception sending_request.", se);
        SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,se, "ASJ.trans.000038", "Exception sending_request.");
      }
    }


    if (tx instanceof OTSTransaction) {
      ctxh.value = ((OTSTransaction)tx).pgContext;
      return;
    }

    if (tx != null) {
      ControlImpl ctrl = (ControlImpl)((TXR_TransactionImpl)tx).getControl();
      TransactionServiceImpl.orb.connect(ctrl);
      TransactionKey txKey = new TransactionKey(((TXR_TransactionImpl)tx).getGlobalTxIDWith_0_BranchIterator());

      if (LOCATION.beLogged(Severity.DEBUG)) {
//        LOCATION.logT(Severity.DEBUG, "transaction key : {0}", new Object[]{TransactionKey.toString(txKey)});
        SimpleLogger.trace(Severity.DEBUG,LOCATION, "ASJ.trans.000231", "transaction key : {0}", new Object[]{TransactionKey.toString(txKey)});
      }

      importedTransactions.put(txKey, tx);

      try {
        ctxh.value = ctrl.get_txcontext();
      } catch (Unavailable ue) {
        if (LOCATION.beLogged(Severity.DEBUG)) {
          LOCATION.traceThrowableT(Severity.DEBUG, "Error occurred. Full stacktrace: ", ue);
        }
        ctxh.value = nullPgpContext;
      }
    } else {
      ctxh.value = null;
    }

  } // sending_request()

  /**
   * Returns the static has table where propagated transaciton from other servers are kept
   *
   * @return Hashtable
   */
  public Hashtable<TransactionKey, Transaction> getImportedTx() {
    return SenderReceiverImpl.importedTransactions;
  }

}
