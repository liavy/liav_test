/*
 * Created on 2004-12-2
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.transaction;

/**
 * Interface, implementations of which denote that the user is owner of the current transaction.
 *
 */
public interface TransactionTicket {
	/**
	 * Has the JTA transaction been started on the transaction level associated
	 * with this ticket?
	 * <p>
	 * @return
	 *     <code>true</code> if the JTA transaction was started on this
	 *     ticket's transaction level, <code>false</code> otherwise.
	 */
	public abstract boolean wasStarted();

	/**
	 * Has the JTA transaction associated with this ticket alread been
	 * completed, i.e. committed or rolled back.
	 * <p>
	 * @return
	 *     <code>true</code> if the JTA transaction associated with this 
	 *     ticket has already been completed, <code>false</code> otherwise.
	 */
	
}