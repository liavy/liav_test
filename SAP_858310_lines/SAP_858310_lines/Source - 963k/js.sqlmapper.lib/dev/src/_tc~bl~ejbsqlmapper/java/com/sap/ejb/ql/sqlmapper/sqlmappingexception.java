package com.sap.ejb.ql.sqlmapper;

import com.sap.ejb.ql.sqlmapper.general.SysLog;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;
import com.sap.tc.logging.Location;

/**
 * Provides information on an error that occured during the mapping process
 * of an EJB-QL query or a EJB load/Store request to an SQL statement.
 *
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class SQLMappingException extends Exception {

        private static final long serialVersionUID = 5363313108482301449L;

        private final static Location loc = Location.getLocation(SQLMappingException.class);

	// attributes
	String detailedMessage;
	String errorLable;
	Exception caughtException;

	/**
	 * Creates an SQLMappingException with a detailed message
	 * and a caught exception.
	 * <p></p>
	 * @param reason
	 *   Short description of the problem encountered.
	 * @param detailedMessage
	 *   Detailed description of the problem encountered
	 *   along with hints on its possible cause and how
	 *   to work around it.
	 * @param errorLable
	 *   Unique identification of the source code location
	 *   where the problem occurred.
	 * @param caughtException
	 *   Exception that was received from an external
	 *   class or method invoked.
	 */
	public SQLMappingException(
		String reason,
		String detailedMessage,
		String errorLable,
		Exception caughtException) {
		super(reason);
		this.detailedMessage = detailedMessage;
		this.errorLable = errorLable;
		this.caughtException = caughtException;

                if ( this.caughtException != null )
                {
                  SysLog.catching(SQLMappingException.loc, this.caughtException.getMessage());
                }
                SysLog.error(SQLMappingException.loc, reason, this.errorLable);

                DevTrace.displayError(SQLMappingException.loc, this.detailedMessage);
	}

	/**
	 * Creates an SQLMappingException with a detailed message.
	 * <p></p>
	 * @param reason
	 *   Short description of the problem encountered
	 * @param detailedMessage
	 *   Detailed description of the problem encountered
	 *   along with hints on its possible cause and how
	 *   to work around it.
	 * @param errorLable
	 *   Unique identification of the source code location
	 *   where the problem occurred.
	 */
	public SQLMappingException(
		String reason,
		String detailedMessage,
		String errorLable) {
		this(reason, detailedMessage, errorLable, null);
	}

	/**
	 * Creates an SQLMappingException.
	 * <p></p>
	 * @param reason
	 *   Short description of the problem encountered.
	 */
	public SQLMappingException(String reason) {
		this(reason, null, null, null);
	}

	/**
	 * Creates an (empty) SQLMappingException.
	 */
	public SQLMappingException() {
		this(null, null, null, null);
	}

	/**
	 * Retrieves the detailed message description.
	 * <p></p>
	 * @return
	 * 		detailed message description String, if
	 *      it is available; <code>null</code> otherwise.
	 */
	public String getDetailedMessage() {
		return this.detailedMessage;
	}

	/**
	 * Retrieves an error lable uniquely identifying the coding
	 * location where exception occurred.
	 * <p></p>
	 * @return
	 * 		error lable identification String.
	 */
	public String getErrorLable() {
		return this.errorLable;
	}

	/**
	 * Retrieves an eventually caught exception from external class or method invocation.
	 * <p></p>
	 * @return 
	 * 		caught exception, if it is available; <code>null</code> otherwise.
	 */
	public Exception getCaughtException() {
		return this.caughtException;
	}
}
