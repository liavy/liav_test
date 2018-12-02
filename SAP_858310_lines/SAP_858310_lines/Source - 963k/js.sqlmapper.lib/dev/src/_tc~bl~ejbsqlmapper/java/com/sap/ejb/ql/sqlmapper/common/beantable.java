package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.CheckableIdentifier;

/**
 * Provides the table name of a table a bean is mapped to.
 * The table name can be native or not. This is retrievable
 * with method <code>isNative()</code>.
 * 
 * </p><p>
 * Copyright (c) 2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class BeanTable implements CheckableIdentifier {

	private String originalTableName;
        private String normalisedTableName;
	private boolean isNative;
        private boolean compoundPrimaryKey;

	/** 
	 * Creates a <code>BeanTable</code> instance.
	 */
	BeanTable(String originalTableName, String normalisedTableName, boolean isNative, boolean compoundPrimaryKey) {
		this.originalTableName = originalTableName;
                this.normalisedTableName = normalisedTableName;
		this.isNative = isNative;
                this.compoundPrimaryKey = compoundPrimaryKey;
	}

	/**
	 * Gets the table name of a table a bean is mapped to.
	 * @return
	 * the table's name.
	 */
	public String getName(boolean nativeMode) {
          if ( nativeMode || this.isNative )
          {
		return this.originalTableName;
          }
          else
          {
                return this.normalisedTableName;
          }
	}

	/**
	 * Indicates whether the table's name is native or not. 
	 * @return
	 * true if the table's name is not an Open SQL identifier; false elsewise.
	 */
	public boolean isNative() {
		return this.isNative;
	}

        /**
         * Indicates whether the table has compound primary key.
         * @return
         *     true if table has compound primary key.
         **/
        boolean hasCompoundPrimaryKey()
        {
          return this.compoundPrimaryKey;
        }

	/**
	 * Creates string representation of a <code>BeanTable</code> instance.
	 */
	public String toString() {
		StringBuffer strBuf = new StringBuffer("{ orig. table name = ");
		strBuf.append(this.originalTableName);
                if ( this.normalisedTableName != null )
                {
                  strBuf.append(", norm. table name = ");
                  strBuf.append(this.normalisedTableName);
                }
		strBuf.append(", isNative = ");
		strBuf.append(this.isNative);
                strBuf.append(", comp. PK = ");
                strBuf.append(this.compoundPrimaryKey);
		strBuf.append(" }");

		return strBuf.toString();
	}

}
