package com.sap.ejb.ql.sqlmapper.common;

/**
 * Describes the number of fields on the database
 * an EJB-QL input parameter is representing.
 * The EJB-QL input parameter is defined throughout an EJB-QL
 * query by a natural number greater or equal 1. If the input parameter is of
 * primitive type the number of fields on the database it stands for is 1.
 * If it represents an abstract bean type, then
 * the number of fields is the number of primary key fields of this
 * abstract bean type. If it represents a dependent value field, the
 * number of fields is the number of fields the dependent value field's type
 * is represented by on the database. We refer to this number of fields
 * on the databse as the count of the input parameter.
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 */

public class InputParameter {

        static final int PRIMARY_KEY = 0;
        static final int NON_KEY_FIELDS = 1;
        static final int ALL_FIELDS = 2;

	private int number;
	private int count;

	InputParameter(int number, int count) {
		this.number = number;
		this.count = count;
	}

	int getCount() {
		return this.count;
	}

	int getNumber() {
		return this.number;
	}
        /**
         * Creates a string representation of <code>InputParameter</code>
         * instance.
         * @return
         *     string representation of <code>InputParameter</code> instance.
         **/
        public String toString()
        {
          StringBuffer strBuf = new StringBuffer("{ number = ");
          strBuf.append(this.number);
          strBuf.append(", count = ");
          strBuf.append(this.count);
          strBuf.append(" }");
          return strBuf.toString();
        }
}
