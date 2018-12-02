package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.FieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.BeanTable;
import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;

/**
 * Describes relavant OR mapping details of a CMP bean field 
 * for the <code>CommonSQLMapper</code>. Instances of this class
 * are created by the <code>ORMappingManager</code>.
 *
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class CMPFieldDescriptor extends FieldDescriptor {
	private DatabaseColumn columnName;
	private int columnPosition;
	private int jdbcType;
	private boolean isKeyField;

	/**
	  * Creates a <code>CMPFieldDescriptor</code> instance.
	  * </p><p>
	  * @param table
	  * 	table to which the CMP bean field's bean is mapped.
	  * @param columName
	  * 	name of column to which the CMP bean field is mapped.
	  * @param position
	  * 	position of column in the table.
	  * @param jdbcType
	  * 	jdbc type of column.
	  * @param isKeyField
	  * 	if the column/field is part of the primary key or not.
	  */
	CMPFieldDescriptor(
		BeanTable table,
		DatabaseColumn columnName,
		int pos,
		int jdbcType,
		boolean isKeyField) {

		this.table = table;
		this.columnName = columnName;
		this.columnPosition = pos;
		this.jdbcType = jdbcType;
		this.isKeyField = isKeyField;
	}

	DatabaseColumn getColumnName() {
		return this.columnName;
	}

	int getColumnPosition() {
		return this.columnPosition;
	}

	int getJdbcType() {
		return this.jdbcType;
	}
	
	boolean isKeyField() {
		return this.isKeyField;
	}

        BeanTable getTable()
        {
          return this.table;
        }

	/**
	 * Creates string representation of <code>CMPFieldDescriptor</code> instance.
	 * @return
	 *     string representation of <code>CMPFieldDescriptor</code> instance.
	 **/
	public String toString() {
		StringBuffer strBuf = new StringBuffer("{ table = ");
		strBuf.append(this.table.toString());
		strBuf.append(", column = ");
		strBuf.append(this.columnName.toString());
		strBuf.append(", columnPosition = ");
		strBuf.append(this.columnPosition);
		strBuf.append(", jdbcType = ");
		strBuf.append(this.jdbcType);
		strBuf.append(", isKeyField = ");
		strBuf.append(this.isKeyField);
		strBuf.append(", hashcode = ");
		strBuf.append(this.hashCode());
		strBuf.append(" }");
		return strBuf.toString();
	}
}
