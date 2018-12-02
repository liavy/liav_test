package com.sap.ejb.ql.sqlmapper.common;

import com.sap.sql.catalog.Column;
import com.sap.sql.catalog.Table;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

import com.sap.sql.types.CommonTypes;

/**
 * Provides information about a column of a bean table.
 * <p></p>
 * Copyright &copy; 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 * @see com.sap.ejb.ql.sqlmapper.common.TableDescriptor
 */
public class ColumnDescriptor implements Column {
	private static final Location loc =
		Location.getLocation(ColumnDescriptor.class);
	private static final String getTable = "getTable";
	private static final String getName = "getName";
	private static final String getPosition = "getPosition";
	private static final String isPrimaryKey = "isPrimaryKey";
	private static final String getPrimaryKeyPosition = "getPrimaryKeyPosition";
	private static final String getJdbcType = "getJdbcType";
	private static final String getTypeName = "getTypeName";
	private static final String getSize = "getSize";
	private static final String getDecimals = "getDecimals";
	private static final String isNullable = "isNullable";
	private static final String getDefault = "getDefault";

	private String name;
	private Table myTable;
	private int jdbcType;
	private int position;
	private long size;
	private int decimals;

	/**
	 * Creates an instance of <code>ColumnDescriptor</code>
	 * </p><p>
	 * <b>Note</b> that current documentation expects <code>size</code>
	 * and <code>decimals</code> always to be 0.
	 * </p><p>
	 * @param name
	 *    this column's name.
	 * @param table
	 *    the corresponding table descriptor.
	 * @param jdbcType
	 *    column's jdbc type.
	 * @param position
	 *    column's ordinal position within table.
	 * @param size
	 *    column size.
	 * @param decimals
	 *    column's decimal size (number of digits of the fraction if any).
	 */
	ColumnDescriptor(
		String name,
		Table table,
		int jdbcType,
		int position,
		long size,
		int decimals) {

		this.name = name;
		this.myTable = table;
		this.jdbcType = jdbcType;
		this.position = position;
		this.size = size;
		this.decimals = decimals;
	}

	/**
	 * Retrieve descriptor of table the column belongs to.
	 * </p><p>
	 * @return
	 *     the table descriptor representing the bean table 
	 *     to which the column belongs.
	 */
	public Table getTable() {
		DevTrace.entering(loc, getTable, null, null);

		DevTrace.exiting(loc, getTable, this.myTable);
		return this.myTable;
	}

	/**
	 * Retrieve the column's name.
	 * </p><p>
	 * @return
	 *    case-sensitive name of column.
	 */
	public String getName() {
		DevTrace.entering(loc, getName, null, null);

		DevTrace.exiting(loc, getName, this.name);
		return this.name;
	}

	/**
	 * Retrieve the column's ordinal position within its table.
	 * </p><p>
	 * The column positions returned reflect a OR mapping dependent
	 * logical view on the table and do not necessarily match with the
	 * physical ordering of the columns within the actual database table
	 * (see {@link com.sap.sql.catalog.Column#getPosition()}). 
	 * </p><p>
	 * The numbering starts at position 1.
	 * </p><p>
	 * @return
	 *     the column's ordinal position.
	 */
	public int getPosition() {
		if (DevTrace.isOnDebugLevel(loc)) {

			DevTrace.entering(loc, getPosition, null, null);

			DevTrace.exiting(loc, getPosition, new Integer(this.position));
		}
		return this.position;
	}

	/**
	 * Checks if the column is one of the table's primary key columns.
	 * <p></p>
	 * @return
	 *    <code>true</code> if column is a primary key column;
	 *    <code>false</code> otherwise.
	 */
	public boolean isPrimaryKey() {
		DevTrace.entering(loc, isPrimaryKey, null, null);

		boolean isPrimKey = (this.position <= this.myTable.getPrimaryKeyCnt());

		DevTrace.exiting(loc, isPrimaryKey, isPrimKey);
		return isPrimKey;
	}

	/**
	 * Retrieve the column's ordinal position within its table's primary key.
	 * </p><p>
	 * The numbering starts at position 1.
	 * </p><p>
	 * @return
	 *    the column's ordinal position within the primary key; 0 will be
	 *     returned if the column is not part of the primary key.
	 */
	public int getPrimaryKeyPosition() {
		DevTrace.entering(loc, getPrimaryKeyPosition, null, null);
		int position;

		if (this.isPrimaryKey()) {
			position = this.position;
		} else {
			position = 0;
		}

		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.exiting(loc, getPrimaryKeyPosition, new Integer(position));
		}
		return position;
	}

	/**
	 * Retrieve the column's jdbc type as defined in {@link java.sql.Types}.
	 * </p><p>
	 * @return
	 *     the column's jdbc type, e.g. {@link java.sql.Types#CHAR}.
	*/
	public int getJdbcType() {
		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.entering(loc, getJdbcType, null, null);
			DevTrace.exiting(loc, getJdbcType, new Integer(this.jdbcType));
		}
		return this.jdbcType;
	}

	/**
	 * Retrieve the column's jdbc type name.
	 * </p><p>
	 * @return
	 *    the column's jdbc type name.
	 * @see com.sap.sql.catalog.Column#getTypeName()
	 */
	public String getTypeName() {
		DevTrace.entering(loc, getTypeName, null, null);

		String typeName = CommonTypes.getJdbcTypeName(this.jdbcType);
		DevTrace.exiting(loc, getTypeName, typeName);
		return typeName;
	}

	/**
	 * Retrieve column's size in terms of its jdbc type.
	 * </p><p>
	 * As this information is not available to OR mapping, this implementation
	 * will always return 0.
	 * </p><p>
	 * @return
	 *    0.
	 * @see com.sap.sql.catalog.Column#getSize()
	 */
	public long getSize() {
		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.entering(loc, getSize, null, null);
			DevTrace.exiting(loc, getSize, new Long(this.size));
		}
		return this.size;
	}

	/**
	 * Retrieve the number of digitals of the fraction for numeric and decimal types.
	 * </p><p>
	 * As this information is not available to OR mapping, this implementation
	 * will always return 0.
	 * </p><p>
	 * @return
	 *    0.
	 * @see com.sap.sql.catalog.Column#getDecimals()
	 */
	public int getDecimals() {
		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.entering(loc, getDecimals, null, null);
			DevTrace.exiting(loc, getDecimals, new Integer(this.decimals));
		}
		return this.decimals;
	}

	/**
	 * Retrieve whether null is a permitted value for this column.
	 * </p><p>
	 * As this information is not available to OR mapping, this implementation
	 * will always return <code>true</code>.
	 * </p><p>
	 * @return
	 *    <code>true</code>.
	 */
	public boolean isNullable() {
		DevTrace.debugInfo(loc, isNullable, "called.");
		return true;
	}

	/**
	 * Retrieve this column's default value if set.
	 * </p><p>
	 * As this information is not available to OR mapping, this implementation
	 * will always return <code>null</code>.
	 * </p><p>
	 * @return
	 *    <code>null</code>.
	 * @see com.sap.sql.catalog.Column#getDefault()
	 */
	public Object getDefault() {
		DevTrace.debugInfo(loc, getDefault, "called.");
		return null;
	}

	/**
	* Gets a String representation of this <code>ColumnDescriptor</code> object.
	* <p></p>
	* @return
	*     a <code>String</code> representation of this object.
	*/
	public String toString() {
		StringBuffer strBuf = new StringBuffer(this.name);
		strBuf.append(" ( JdbcType = ");
		strBuf.append(this.jdbcType);
		strBuf.append(", size = ");
		strBuf.append(this.size);
		strBuf.append(", decimals = ");
		strBuf.append(this.decimals);
		strBuf.append(", Table = ");
		strBuf.append(this.myTable.hashCode());
		strBuf.append(", hashcode = ");
		strBuf.append(this.hashCode());
		strBuf.append(")");

		return strBuf.toString();
	}
}
