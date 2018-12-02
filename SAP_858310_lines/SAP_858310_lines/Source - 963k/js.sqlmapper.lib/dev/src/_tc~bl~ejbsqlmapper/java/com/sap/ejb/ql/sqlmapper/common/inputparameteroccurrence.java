package com.sap.ejb.ql.sqlmapper.common;

/**
 * Describes a particular occurrence of an EJB-QL input parameter
 * within an SQL <code>InputDescriptor</code> list, resp. within 
 * an SQL statement.
 * An EJB-QL input parameter is defined throughout an EJB-QL query
 * by a natural number greater or equal 1 and may be used (occur) 
 * several times within the
 * same EJB-QL query. If the EJB-QL input parameter's count, i.e., 
 * the number of database fields the EJB-QL input parameter is 
 * representing (see <code>InputParameter</code>),
 * is n, with n being greater or equal 1, then each occurrence of the
 * EJB QL input parameter is represented by a different set of n 
 * elements of the <code>InputDescriptor</code> list.
 * An instance of class <code>InputParameterOccurence</code>
 * describes a particular occurrence of an EJB-QL input parameter
 * within an EJB-QL query by assigning the input parameter's number
 * to the position in the <code>InputDescriptor</code> list of the first
 * of the n elements describing the current occurrence of the input
 * parameter within the resulting SQL statement.
 * <p></p>
 * Copyright (c) 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class InputParameterOccurrence {
	private int inputParameter;
	private int occurrence;

	InputParameterOccurrence(int number, int occurrence) {
		this.inputParameter = number;
		this.occurrence = occurrence;
	}

	int getInputParameterNumber() {
		return this.inputParameter;
	}

	int getOccurrence() {
		return this.occurrence;
	}

        /**
         * Creates string representation of <code>InputParameterOccurrence</code>
         * instance.
         * @return
         *    string representation of <code>InputParameterOccurrence</code> instance.
         **/
        public String toString()
        {
          StringBuffer strBuf = new StringBuffer("{ inputParameter = ");
          strBuf.append(this.inputParameter);
          strBuf.append(", occurrence = ");
          strBuf.append(this.occurrence);
          strBuf.append(" }");
          return strBuf.toString();
        }
}
