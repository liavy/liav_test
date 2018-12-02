package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.CheckableIdentifier;
import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;

/**
 * Describes a helper table representing an M:N container managed
 * relationship between two abstract beans.
 * </p><p>
 * Copyright (c) 2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class HelperTableFacade implements CheckableIdentifier {
	private String originalTableName;
	private String normalisedTableName;
	private boolean isNative;
	private String[] beans;
	private DatabaseColumn[][] columns;
	;
	private DatabaseColumn[] allColumns;

	static final int NUMBER_OF_SIDES = 2;
	static final int FRONT_SIDE = 0;
	static final int BACK_SIDE = 1;

	/**
	 * Creates a new <code>HelperTableFacade</code> instance.
	 * </p><p>
	 * @param originaltableName
	 *        name of helper table as given per OR mapping
	 * @param normalisedTableName
	 *        name of helper table normalised according to Open SQL rules.
	 * @param isNative
	 *        if helper table name is native or not.
	 * @param myBean
	 *        one of the abstract beans involved in the CMR.
	 * @param myColumns
	 *        database columns of helper table representing <code>firstBean</code>.
	 * @param otherBean
	 *        other abstract bean involved in the CMR.
	 * @param otherColumns
	 *        database columns of helper table representing <code>secondBean</code>.
	 */
	HelperTableFacade(
		String originalTableName,
		String normalisedTableName,
		boolean isNative,
		String myBean,
		DatabaseColumn[] myColumns,
		String otherBean,
		DatabaseColumn[] otherColumns) {

		this.originalTableName = originalTableName;
		this.normalisedTableName = normalisedTableName;
		this.isNative = isNative;
		this.columns = new DatabaseColumn[NUMBER_OF_SIDES][];
		this.columns[FRONT_SIDE] = myColumns;
		this.columns[BACK_SIDE] = otherColumns;

		this.beans = new String[NUMBER_OF_SIDES];
		this.beans[FRONT_SIDE] = myBean;
		this.beans[BACK_SIDE] = otherBean;

		int offset =
			(this.columns[FRONT_SIDE] == null)
				? 0
				: this.columns[FRONT_SIDE].length;
		int length =
			offset
				+ ((this.columns[BACK_SIDE] == null)
					? 0
					: this.columns[BACK_SIDE].length);

		this.allColumns = new DatabaseColumn[length];

		if (this.columns[FRONT_SIDE] != null) {
			System.arraycopy(
				this.columns[FRONT_SIDE],
				0,
				this.allColumns,
				0,
				this.columns[FRONT_SIDE].length);
		}

		if (this.columns[BACK_SIDE] != null) {
			System.arraycopy(
				this.columns[BACK_SIDE],
				0,
				this.allColumns,
				offset,
				this.columns[BACK_SIDE].length);
		}
	}

	/**
	 * Retrieves name of helper table.
	 * </p><p>
	     * @param nativeMode
	     *      indicating whether native mode has been set for the handling of table names.
	 * @return
	 *      helper table name.
	 */
	public String getName(boolean nativeMode) {
		if (nativeMode || this.isNative) {
			return this.originalTableName;
		} else {
			return this.normalisedTableName;
		}
	}

	/**
	 * Indicates whether the helper table's name is native or not. 
	 * @return
	 * true if the helper table's name is not an Open SQL identifier; false elsewise.
	 */
	public boolean isNative() {
		return this.isNative;
	}

	/**
	 * Retrieves the names of the abstract beans involved in this relationship.
	 * </p><p>
		 * @return
	 *      two-element-arary containing the names of the two abstract beans involved
	 *      in this CMR.
	 */
	String[] getBeans() {
		return this.beans;
	}
	
	/**
	 * Retrieves the names of the abstract beans involved in this relationship.
	 * For key == FRONT_SIDE front side bean name is returned,
	 * for key == BACK_SIDE back side bean name is returned. 
	 * </p><p>
	 * @param key
	 * 		side of facade. 
	 * @return
	 *      the name of the respective abstract bean.
	 */
	String getBean(int key) {
		return this.beans[key];
	}	

	/**
	 * Retrieves the name of the abstract bean representing the front side of
	 * this relationship.
	 * </p><p>
	 * @return
	 *      the name of the respective abstract bean.
	 */
	String getFrontSideBean() {
		return this.getBean(FRONT_SIDE);
	}
	
	/**
	 * Retrieves the name of the abstract bean representing the back side of
	 * this relationship.
	 * </p><p>
	 * @return
	 *      the name of the respective abstract bean.
	 */
	String getBackSideBean() {
		return this.getBean(BACK_SIDE);
	}
	

	/**
	 * Retrieves the database columns of the helper table.
	 * </p><p>
	 * @return
	 *      database columns of helper table.
	 */
	DatabaseColumn[] getColumns() {
		return this.allColumns;
	}

	/**
	 * Retrieves the helper table' database column representing a side of this facade.
	 * For key == FRONT_SIDE columns of front side is returned,
	 * for key == BACK_SIDE columns of back side is returned. 
	 * </p><p>
	 * @param key
	 * 		side of facade. 
	 * @return
	 *      the column names of the respective side.
	 */
	DatabaseColumn[] getColumns(int key) {
		return this.columns[key];
	}

	/**
	 * Retrieves the helper table's database columns representing front side of this facade.
	 * </p><p>
	 * @return
	 *      helper table's database columns representing front side of facade.
	 */
	DatabaseColumn[] getFrontSideColumns() {
		return this.getColumns(FRONT_SIDE);
	}

	/**
	 * Retrieves the helper table's database columns representing back side of this facade.
	 * </p><p>
	 * @return
	 *      helper table's database columns representing back side of facade.
	 */
	DatabaseColumn[] getBackSideColumns() {
		return this.getColumns(BACK_SIDE);
	}

	/**
	 * Indicates whether given abstract bean is involved in this CMR.
	 * </p><p>
	 * @param abstractBean
	 *      abstract bean name.
	 * @return
	 *      <code>true</code> if given abstract bean is part of this CMR;<br>
	 *      <code>false</code> elsewise.
	 */
	boolean isReferenced(String abstractBean) {
		return (
			abstractBean.equals(this.beans[FRONT_SIDE])
				|| abstractBean.equals(this.beans[BACK_SIDE]));
	}

	/**
	 * Creates string representation of a <code>HelperTable</code> instance.
	 */
	public String toString() {
		StringBuffer strBuf = new StringBuffer("{ orig. table name = ");
		strBuf.append(this.originalTableName);
		if (this.normalisedTableName != null) {
			strBuf.append(", norm. table name = ");
			strBuf.append(this.normalisedTableName);
		}
		strBuf.append(", isNative = ");
		strBuf.append(this.isNative);
		strBuf.append(", front side bean = ");
		strBuf.append(this.beans[FRONT_SIDE]);
		strBuf.append(", front side columns = ");
		strBuf = this.printColumns(strBuf, this.columns[FRONT_SIDE]);
		strBuf.append(", back side bean = ");
		strBuf.append(this.beans[BACK_SIDE]);
		strBuf.append(", back side columns = ");
		strBuf = this.printColumns(strBuf, this.columns[BACK_SIDE]);
		strBuf.append(" }");

		return strBuf.toString();
	}

	/**
	 * Prints an array of database columns into a <code>StringBuffer</code>.
	 * </p><p>
	 * @param strBuf
	 *      <code>StringBuffer</code> to be filled.
	 * @param columns
	 *      array of columns to be printed into <code>StringBuffer</code>.
	 * @return
	 *     filled <code>StringBuffer</code>.
	 */
	private StringBuffer printColumns(
		StringBuffer strBuf,
		DatabaseColumn[] columns) {
		strBuf.append("{ ");

		if (columns == null) {
			strBuf.append("null");
		} else {
			for (int i = 0; i < columns.length; i++) {
				if (i > 0) {
					strBuf.append(", ");
				}

				strBuf.append(columns[i].toString());
			}
		}

		strBuf.append(" }");

		return strBuf;
	}

}
