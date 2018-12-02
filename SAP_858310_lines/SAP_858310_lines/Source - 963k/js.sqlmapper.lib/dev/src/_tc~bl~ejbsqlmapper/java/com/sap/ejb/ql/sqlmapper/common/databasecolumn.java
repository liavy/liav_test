package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.CheckableIdentifier;

/**
 * Provides a database column name.
 * The column name can be native or not. This is retrievable
 * with method <code>isNative()</code>.
 * 
 * </p><p>
 * Copyright (c) 2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class DatabaseColumn implements CheckableIdentifier {

	private String originalColumnName;
        private String normalisedColumnName;
	private boolean isNative;

	/** 
	 * Creates a <code>DatbaseColumn</code> instance.
	 */
	DatabaseColumn(String originalColumnName, String normalisedColumnName, boolean isNative) {
		this.originalColumnName = originalColumnName;
                this.normalisedColumnName = normalisedColumnName;
		this.isNative = isNative;
	}

	/**
	 * Gets the column name of a column a bean is mapped to.
	 * @return
	 * the column's name.
	 */
	public String getName(boolean nativeMode) {
          if ( nativeMode || this.isNative )
          {
		return this.originalColumnName;
          }
          else
          {
                return this.normalisedColumnName;
          }
	}

	/**
	 * Indicates whether the column's name is native or not. 
	 * @return
	 * true if the column's name is not an Open SQL identifier; false elsewise.
	 */
	public boolean isNative() {
		return this.isNative;
	}

	/**
	 * Creates string representation of a <code>DatbaseColumn</code> instance.
	 */
	public String toString() {
		StringBuffer strBuf = new StringBuffer("{ orig. column name = ");
		strBuf.append(this.originalColumnName);
                if ( this.normalisedColumnName != null )
                {
                  strBuf.append(", norm. column name = ");
                  strBuf.append(this.normalisedColumnName);
                }
		strBuf.append(", isNative = ");
		strBuf.append(this.isNative);
		strBuf.append(" }");

		return strBuf.toString();
	}

}
