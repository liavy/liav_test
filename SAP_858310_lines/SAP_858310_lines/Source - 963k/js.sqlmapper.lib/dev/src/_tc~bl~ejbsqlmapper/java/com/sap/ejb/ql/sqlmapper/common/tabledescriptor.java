package com.sap.ejb.ql.sqlmapper.common;

import com.sap.sql.catalog.Table;
import com.sap.sql.catalog.Column;
import com.sap.sql.catalog.ColumnIterator;

import com.sap.ejb.ql.sqlmapper.common.ColumnDescriptor;
import com.sap.ejb.ql.sqlmapper.common.ORMapColumnIterator;

import java.util.Collections;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

/**
 * Provides information about a bean table.
 *
 * <p>
 * Access to the table's column descriptors is provided either by a column's
 * name or its ordinal position within the table. The latter may be
 * used to efficiently loop over the table's columns:
 * <pre>
 *   Table t = aCatologReader.getTable(&quot;ABC&quot;)
 *   for (int col_i = 1; col_i &lt;= t.getColumnCnt(); col_i++)
 *   {
 *     Column c = t.getColumn(col_i);
 *     // do something with c
 *   }
 * </pre>
 * </p>
 *
 * Copyright (c) 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 * @see com.sap.ejb.ql.sqlmapper.common.ColumnDescriptor
 */
public class TableDescriptor implements Table {
	private static final Location loc =
		Location.getLocation(TableDescriptor.class);
	private static final String getBufferKeyCnt = "getBufferKeyCnt";
	private static final String getClientColumn = "getClientColumn";
	private static final String getColumn = "getColumn";
	private static final String getColumnCnt = "getColumnCnt";
	private static final String getColumns = "getColumns";
	private static final String getName = "getName";
	private static final String getPrimaryKeyCnt = "getPrimaryKeyCnt";
	private static final String getPrimaryKeyColumn = "getPrimaryKeyColumn";
	private static final String getSchemaName = "getSchemaName";
	private static final String getTableType = "getTableType";
	private static final String isClientDependent = "isClientDependent";
	private static final String isBuffered = "isBuffered";
	private static final String getReferencedTableNames =
		"getReferencedTableNames";
	private static final String isUpdatable = "isUpdatable";
	private static final String isView = "isView";
	private static final String isGroupedView = "isGroupedView";
	private static final String getColumnPosParms[] = { "position" };
	private static final String getColumnNameParms[] = { "columnName" };
	private static final String getPrimaryKeyColumnParms[] = { "position" };

	private String name;
	private int primaryKeyCount;
	private ColumnDescriptor[] columnsByPosition;
	private Hashtable columnsByName;

	/**
	 * Creates a <code>TableDescriptor</code> instance.
	 * </p><p>
	 * @param name
	 *    table name.
	 * @param primKeyCnt
	 *    number of the table's primary key fields.
	 * @param columnsByPosition
	 *    array of table's columns in the order of their position within table.
	 * @param columnsByName
	 *    hashtable associating table's columns with their respective name.
	 */
	TableDescriptor(
		String name,
		int primKeyCnt,
		ColumnDescriptor[] columnsByPosition,
		Hashtable columnsByName) {
		this.name = name;
		this.primaryKeyCount = primKeyCnt;
		this.columnsByPosition = columnsByPosition;
		this.columnsByName = columnsByName;
	}

	void addColumns(ColumnDescriptor[] columns) {
		this.columnsByPosition = columns;
	}

	void addColumnNames(Hashtable columnByNames) {
		this.columnsByName = columnByNames;
	}

	/**
	 * Gets the number of primary key columns that form the buffer key
	 * according to which single rows or row sets are buffered.
	 * </p><p>
	 * This method is only meaningful within the <code>CommonJdbc</code> framework
	 * where a table buffer is available.
	 * Implementations of this interface outside this framework should always
	 * return -1.
	 * </p><p>
	 * Consequently this implementation always returns -1.
	 * </p><p>
	 * @return
	 *    -1.
	 */
	public int getBufferKeyCnt() {
		DevTrace.debugInfo(loc, getBufferKeyCnt, "called.");
		return -1;
	}

	/**
	 * Returns the client column, if the table is client dependent.
	 * <p></p>
	 * As this information is not available to the OR mapping, this
	 * implementation always returns <code>null</code>.
	 * @return
	 *      <code>null</code>.
	 *
	 */
	public Column getClientColumn() {
		DevTrace.debugInfo(loc, getClientColumn, "called.");
		return null;
	}

	/**
	 * Gets a description of the column specified by its ordinal position.
	 * </p><p>
	 * Counting starts with ordinal position 1.
	 * </p><p>
	 * @param position
	 *     the ordinal position of the column to be described.
	 * @return
	 *     a column descriptor containing information about the specified
	 *     column; <code>null</code>, if the given position is less than 1 or
	 *     greater than {@link #getColumnCnt()}.
	 */
	public Column getColumn(int position) {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { new Integer(position)};
			DevTrace.entering(loc, getColumn, getColumnPosParms, inputValues);
		}

		ColumnDescriptor column;

		if ((position <= this.columnsByPosition.length) && (position > 0)) {
			column = this.columnsByPosition[position - 1];
		} else {
			column = null;
		}

		DevTrace.exiting(loc, getColumn, column);
		return column;
	}

	/**
	 * Gets the description of a column specified by its name.
	 * </p><p>
	 * The given <code>columnName</code> is interpreted as a quoted SQL
	 * identifier. According to the SQL standard, this means that it will match
	 * with a column in the table iff
	 * </p><p>
	 * <ul>
	 * <li>the column was created with an unquoted identifier and
	 * <code>columnName</code> is the upper case representation of this
	 * identifier or</li>
	 * <li>the column was created with a quoted identifier and
	 * <code>columnName</code> exactly matches with this identifier.</li>
	 * </ul>
	 * </p><p>
	 * @param columnName
	 *     the case-sensitive name of the column to be described.
	 * @return
	 *     a column descriptor containing information about the specified
	 *     column; <code>null</code> is returned, if the table has no column
	 *     with the specified name.
	 * @see com.sap.ejb.ql.sqlmapper.common.ColumnDescriptor
	 */
	public Column getColumn(String columnName) {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { columnName };
			DevTrace.entering(loc, getColumn, getColumnNameParms, inputValues);
		}

		ColumnDescriptor column =
			(ColumnDescriptor) this.columnsByName.get(columnName);

		DevTrace.exiting(loc, getColumn, column);
		return column;
	}

	/**
	 * Retrieves number of table's columns.
	 * <p></p>
	 * @return
	 *     the number of columns.
	 */
	public int getColumnCnt() {
		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.entering(loc, getColumnCnt, null, null);

			DevTrace.exiting(
				loc,
				getColumnCnt,
				new Integer(this.columnsByPosition.length));
		}
		return this.columnsByPosition.length;
	}

	/**
	 * Gets a <code>ORMapColumnIterator</code> to loop over the table's columns.
	 * <p></p>
	 * @return
	 *     a column iterator.
	 * @see com.sap.ejb.ql.sqlmapper.common.ORMapColumnIterator
	 */
	public ColumnIterator getColumns() {
		DevTrace.entering(loc, getColumns, null, null);

		ORMapColumnIterator iterator =
			new ORMapColumnIterator(this.columnsByPosition);

		DevTrace.exiting(loc, getColumns, iterator);
		return iterator;
	}

	/**
	 * Retrieve the table's name
	 * <p></p>
	 * @return
	 *     the case-sensitive name of the table.
	 */
	public String getName() {
		DevTrace.entering(loc, getName, null, null);

		DevTrace.exiting(loc, getName, this.name);
		return this.name;
	}

	/**
	 * Retrieve number of table's primary key columns
	 * <p></p>
	 * @return
	 *     the number of primary key columns; 0 is returned if the table
	 *     doesn't have a primary key.
	 */
	public int getPrimaryKeyCnt() {
		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.entering(loc, getPrimaryKeyCnt, null, null);

			DevTrace.exiting(
				loc,
				getPrimaryKeyCnt,
				new Integer(this.primaryKeyCount));
		}
		return this.primaryKeyCount;
	}

	/**
	 * Gets the description of a primary key column specified by its ordinal
	 * position within the primary key.
	 * </p><p>
	 * Counting starts with ordinal position 1.
	 * </p><p>
	 * @param position
	 *     the ordinal position of the primary key column to be described.
	 * @return
	 *     a column descriptor containing information about the specified
	 *     column; <code>null</code>, if the given position is less than 1 or
	 *     greater than <code>getPrimaryKeyCnt()</code>.
	 */
	public Column getPrimaryKeyColumn(int position) {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { new Integer(position)};
			DevTrace.entering(
				loc,
				getPrimaryKeyColumn,
				getPrimaryKeyColumnParms,
				inputValues);
		}
		ColumnDescriptor column;

		if ((position > this.primaryKeyCount) || (position < 1)) {
			column = null;
		} else {
			column = this.columnsByPosition[position - 1];
		}

		DevTrace.exiting(loc, getPrimaryKeyColumn, column);
		return column;
	}

	/**
	 * Retrieve the name of the schema to which the table belongs
	 * <p></p>
	 * As this information is not available to OR mapping, this implementation
	 * always returns <code>null</code>.
	 * <p></p>
	 * @return
	 *    <code>null</code>.
	 */
	public String getSchemaName() {
		DevTrace.debugInfo(loc, getTableType, "called.");
		return null;
	}

	/**
	 * Gets the type of the table object.
	 * <p></p>
	 * For OR mapping, always table type &quot;Table&quot; is assumed.
	 * @return
	 *    <code>com.sap.sql.catalog.Table.TABLE_TYPE</code>.
	 */
	public int getTableType() {
		DevTrace.debugInfo(loc, getTableType, "called.");
		return Table.TABLE_TYPE;
	}

	/**
	 * Checks if the table object is client dependent. A table is client
	 * dependent iff the table has got a client column.
	 * <p></p>
	 * As this information is not available to OR mapping, this implementation
	 * always returns <code>false</code>.
	 * @return
	 *      <code>false</code>.
	 */
	public boolean isClientDependent() {
		DevTrace.debugInfo(loc, isClientDependent, "called.");
		return false;
	}

	/**
	 * Checks if table object is buffered. The result is
	 * <code>true</code> if the table object is buffered. The result
	 * is <code>false</code> if the table object is not bufferd or not known
	 * to be buffered.
	 * </p><p>
	 * This implementation will always return <code>false</code>.
	 * </p><p>
	 * @return
	 *      <code>true</code> if the table object is buffered.
	 *      <code>false</code> if the table object is not buffered or
	 *      not known to be buffered.
	 */
	public boolean isBuffered() {
		DevTrace.debugInfo(loc, isBuffered, "called.");
		return false;
	}

	/**
	 * Returns the Set of all table names that are referrenced by the table or
	 * view represented by this table object.
	 * <p>
	 * If this <code>TableDescriptor</code> object represents a database table,
	 * which actually is always the case,
	 * this method returns a Set containing only the table name.
	 * <p>
	 * The set contains only objects with type <code>String</code>.
	 * </p><p>
	 * @return 
	 *     A <code>java.util.Set</code> object containing the table names
	 *     leaf generally underlying this <code>TableDescriptor</code> object.
	 */
	public Set getReferencedTableNames() {
		DevTrace.entering(loc, getReferencedTableNames, null, null);

		Set tables = new HashSet(1);
		tables.add(this.name);

		DevTrace.exiting(loc, getReferencedTableNames, tables);
		return Collections.unmodifiableSet(tables);
	}

	/**
	 * Determines whether this table or view is updatable. An updatable table
	 * or view can modified with an INSERT, UPDATE or DELETE statement or can
	 * be the base table of another updatable view.
	 * <p>
	 * A table is not necessarily updatable. It may be read only to prevent it
	 * from beeing changed through Open SQL.
	 * </p><p>
	 * This implementation will always return <code>false</code>, as
	 * it is solely to be used for mapping of EJB QL queries to SQL
	 * (select) statements.
	 * </p><p>
	 * @return 
	 *    <code>true</code> if this table or view is updatable;
	 *    <code>false</code> otherwise
	 */
	public boolean isUpdatable() {
		DevTrace.debugInfo(loc, isUpdatable, "called.");
		return true;
	}

	/**
	 * Determines wether this table object represents a database
	 * view.
	 * </p><p>
	 * This implementation considers all its objects to be tables, not views.
	 * </p><p>
	 * @return 
	 *    <code>true</code> if this <code>Table</code> object represents a
	 *    database view; <code>false</code> if this this <code>Table</code> object
	 *    represents a database table.
	 */
	public boolean isView() {
		DevTrace.debugInfo(loc, isView, "called.");
		return false;
	}

	/**
	 * Determines whether this table object represents a grouped
	 * view. A view is a grouped view if the query specification of its view
	 * definition produces grouping columns (i.e. it contains a GROUP BY clause,
	 * a HAVING clause or the select list of the main query contains set
	 * functions).
	 * </p><p>
	 * This implementation considers all its objects not to be grouped views.
	 * </p><p>
	 * @return 
	 *    <code>true</code> if this <code>Table</code> object represents
	 *     a grouped view; <code>false</code> otherwise.
	 */
	public boolean isGroupedView() {
		DevTrace.debugInfo(loc, isGroupedView, "called.");
		return false;
	}

	/**
	 * Gets a String representation of this <code>TableDescriptor</code> object.
	 * <p></p>
	 * @return
	 *     a <code>String</code> representation of this object.
	 */
	public String toString() {
		StringBuffer strBuf = new StringBuffer(this.name);
		strBuf.append(" ( #columns = ");
		strBuf.append(this.columnsByPosition.length);
		strBuf.append(", #PKcolumns = ");
		strBuf.append(this.primaryKeyCount);
		strBuf.append(", Columns = { ");
		for (int i = 0; i < this.columnsByPosition.length; i++) {
			if (i > 0) {
				strBuf.append(", ");
			}
			strBuf.append(this.columnsByPosition[i].toString());
		}
		strBuf.append(" }, hascode = ");
		strBuf.append(this.hashCode());
		strBuf.append(" )");

		return strBuf.toString();
	}
}
