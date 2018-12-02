/*
 * Copyright (c) 2002 by SAP AG.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.ts;

import com.sap.engine.frame.core.thread.ContextObject;
import com.sap.engine.frame.core.thread.ThreadContext;
import com.sap.engine.frame.core.thread.ThreadSystem;
import com.sap.engine.interfaces.transaction.TransactionExtension;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

import java.util.Stack;

/**
 * This class represents a Context Object registered by the Transaction Service
 *
 * @author : Iliyan Nenov, ilian.nenov@sap.com
 * @version 1.0
 */
public class TransactionContextObject extends ThreadLocal implements ContextObject {

  private static final Location LOCATION = Location.getLocation(TransactionServiceFrame.class);
	
  public static final String NAME = "TRANSACTION_CONTEXT_OBJECT";
  /**
   * A real transaction that is going to be registered into
   * the ThreadContext through a ContextObject
   */
  private TransactionExtension transaction = null;

  /**
   * Default transaction timeout
   */
//  protected static int defautTimeout;
  /**
   * User specific transaction timeout
   */
  private int timeout;
  
  /**
   * Stack representing the control flow of the method calls registered(),
   * registersNew() and commit(). Calls of registered() and registersNew()
   * push a TxTicket to the stack, whereas commit() removes one.
   */
  private Stack ticketStack = null;

  /**
   * Stack containing TxSynchronization objects registered with JTA
   * transactions. The stack reflects the control flow of jtaTxMgr.begin()
   * and jtaTxMgr.commit() calls. The top level element of the stack
   * is the TxSynchronization object registered with the current
   * transaction.
   */
  private Stack synchronizationsStack = null;
  
  /**
   * Context object that is used only for management of context objects in system threads.
   */
  private static TransactionContextObject systemThreadContextObject = new TransactionContextObject();

  /**
   * When context object is stored into system thread this parameter is true.
   */
  private boolean isInSystemThread = false; 

  /**
   * This is an empty constructor used only for registering a TransactionContextObject
   * in the ThreadContext for getting an id
   */
  public TransactionContextObject() {
  	super();    
	ticketStack = new Stack();
	synchronizationsStack = new Stack();
  }

  /**
   * Called by ThreadContext for retrieving child value of this ContextObject
   */
  public ContextObject childValue(ContextObject parent, ContextObject child) {
    if (child == null) {
      child = new TransactionContextObject();
    } else {
      ((TransactionContextObject) child).transaction = null;
	  ((TransactionContextObject) child).ticketStack.clear();
	  ((TransactionContextObject) child).synchronizationsStack.clear();
    }
    ((TransactionContextObject) child).timeout = ((TransactionContextObject)parent).timeout;

    return child;
  }

  /**
   * Called by the ThreadContext when a new TransctionContextObject is registered
   */
  public ContextObject getInitialValue() {
    TransactionContextObject result = new TransactionContextObject();    
    return result;
  }

  /**
   * Called by the ThreadContext when TransactionContextObject is unregistered after that is ready for reuse
   */
  public void empty() {
	if(transaction != null && transaction.isAlive()){
		try{
//			LOCATION.logT(Severity.WARNING, "Transaction " + transaction + " was not completed. Will try to rollback it automatically"); 
			SimpleLogger.trace(Severity.WARNING,LOCATION, "ASJ.trans.000273", "Transaction {0} was not completed. Will try to rollback it automatically",  new Object[] { transaction});
			transaction.rollback();			
		} catch (Exception e ){//catch all exceptions and just log them. There is no other option to react.
//			LOCATION.traceThrowableT(Severity.ERROR, "Transaction " + transaction + " was not completed. Automatic rollback of this transaction was not successful.",e);
			SimpleLogger.traceThrowable(Severity.ERROR,LOCATION,e, "ASJ.trans.000103", "Transaction {0} was not completed. Automatic rollback of this transaction was not successful.",new Object[] {transaction});
		}
	}  
    transaction = null;
    timeout = 0;
	ticketStack.clear();
	synchronizationsStack.clear();
  }

  public void setTimeout(int new_timeout) {
	if(new_timeout > 0){
		this.timeout = new_timeout; 
	} 
	// if  new_timeout == 0 the default timeout will be used according specification.  
	// else set is ignored and default is used. This method is called only from TransactionManager and value is positive. 
  }

  public int getTimeout() {
	  if(timeout <= 0){
		  return TransactionServiceFrame.txTimeout;
	  } else {
		  return timeout;
	  }   
  }
  
  /**
   * @return Returns the synchronizationsStack.
   */
  public Stack getSynchronizationsStack() {
	  return synchronizationsStack;
  }
  /**
   * @return Returns the ticketStack.
   */
  public Stack getTicketStack() {
	  return ticketStack;
  }

  /**
   * @return Returns the transaction.
   */
  public TransactionExtension getTransaction() {
  	return transaction;
  }
  /**
   * @param transaction The transaction to set.
   */
  public void setTransaction(TransactionExtension transaction) {
	  this.transaction = transaction;
//  	if(transaction == null  && isInSystemThread){
//  		systemThreadContextObject.remove(); 	
//  	}
  }   
  
  /**
   * This method is overrides default implementatation to create the appropriate initial object
   * for the current thread's view of the ThreadLocal.Used only for system threads.
   *
   * @return the initial value of the variable in this thread
   */  
  protected Object initialValue(){
  	TransactionContextObject result = (TransactionContextObject)getInitialValue();
	result.isInSystemThread = true;
    return result;	
  }
  
  public static TransactionContextObject getThransactionContextObject() {

  	TransactionContextObject result = null;
  	int txCtxId = TransactionServiceFrame.txContextID;
  	
    ThreadContext tc = null; 
    ThreadSystem threadSystem = TransactionServiceFrame.threadSystem;
    if(threadSystem != null){
    	tc = threadSystem.getThreadContext();
    }
    if (tc == null) {// in system Thread
    	result = (TransactionContextObject)systemThreadContextObject.get();
    } else {
      result = (TransactionContextObject)tc.getContextObject(txCtxId);
    }

    if (result == null) {
    	if(txCtxId == -1){
    		throw new RuntimeException("Transaction service is stopped.");
    	} else {
    		throw new RuntimeException("Wrapper of transaction objects that is associated into thread is null");
    	}
     }
     return result;
  }
 
}

