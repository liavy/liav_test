package com.sap.ejb.ql.sqlmapper;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;

import com.sap.tc.logging.Location;

/**
 * Provides information on an error that occured during the mapping process
 * of an EJB-QL query or a EJB load/Store request to an SQL statement.
 * In contrast to the general <code>SQLMappingException</code>, this
 * exception is not thrown when a data inconsistency or a programming error
 * is encountered, but when an attempt is made to create an SQL statement
 * that does not make sense. Eg. when an update statement is requested
 * for an abstract bean that has no non-primary-key fields on the database.
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class NoReasonableStatementException extends SQLMappingException {

        private static final long serialVersionUID = 5363313108482301449L;

        private final static Location loc = Location.getLocation(NoReasonableStatementException.class);

	/**
	 * Creates a NoReasonableStatementException with a detailed message
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
	public NoReasonableStatementException(
		String reason,
		String detailedMessage,
		String errorLable,
		Exception caughtException) {
		super(reason, detailedMessage, errorLable, caughtException);
	}

	/**
	 * Creates a NoReasonableStatementException with a detailed message.
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
	public NoReasonableStatementException(
		String reason,
		String detailedMessage,
		String errorLable) {
		this(reason, detailedMessage, errorLable, null);
	}

	/**
	 * Creates a NoReasonableStatementException.
	 * <p></p>
	 * @param reason
	 *   Short description of the problem encountered.
	 */
	public NoReasonableStatementException(String reason) {
		this(reason, null, null, null);
	}

	/**
	 * Creates an (empty) NoReasonableStatementException.
	 */
	public NoReasonableStatementException() {
		this(null, null, null, null);
	}

}
