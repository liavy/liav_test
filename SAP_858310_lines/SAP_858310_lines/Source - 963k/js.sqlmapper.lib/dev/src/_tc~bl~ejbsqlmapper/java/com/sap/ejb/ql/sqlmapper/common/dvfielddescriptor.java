package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.FieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.BeanTable;
import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;

/**
 * Describes relavant OR mapping details of a dependent value (DV)
 * bean field for the <code>CommonSQLMapper</code>.
 * Instances of this class are created by the
 * <code>ORMappingManager</code>.
 *
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class DVFieldDescriptor extends FieldDescriptor {
	private DatabaseColumn[] columnNames;
	private String[] subFieldNames;
	private int[] jdbcTypes;

	/**
	 * Creates a <code>DVFieldDescriptor</code> instance.
	 * <p></p>
	 * @param table
	 * 		table to which the DV bean field's bean is mapped.
	 * @param columNames
	 * 		array of names of columns to which the DV bean field class'
	 * 		fields are mapped.
	 * @param subFieldNames
	 * 		array of dependent value class' field names.
	 * @param jdbcTypes
	 * 		array of columns' jdbc types.
	 */
	DVFieldDescriptor(
		BeanTable table,
		DatabaseColumn[] columnNames,
		String[] subFieldNames,
		int[] jdbcTypes) {

		this.table = table;
		this.columnNames = columnNames;
		this.jdbcTypes = jdbcTypes;
		this.subFieldNames = subFieldNames;
	}

	DatabaseColumn[] getColumnNames() {
		return this.columnNames;
	}

	String[] getSubFieldNames() {
		return this.subFieldNames;
	}

	int[] getJdbcTypes() {
		return this.jdbcTypes;
	}

	/**
	 * Creates string representation of <code>DVFieldDescriptor</code> instance.
	 * @return
	 *     string representation of <code>DVFieldDescriptor</code> instance.
	 **/
	public String toString() {
		StringBuffer strBuf = new StringBuffer("{ table = ");
		strBuf.append(this.table.toString());
		strBuf.append(", columnNames = ");
		if (this.columnNames != null) {
			strBuf.append("{ ");
			for (int i = 0; i < columnNames.length; i++) {
				if (i != 0)
					strBuf.append(", ");
				strBuf.append(this.columnNames[i].toString());
				strBuf.append(
					" ("
						+ this.jdbcTypes[i]
						+ ", "
						+ this.subFieldNames[i]
						+ ")");
			}
			strBuf.append(" }");
		} else {
			strBuf.append("null");
		}
		strBuf.append(", hashcode = ");
		strBuf.append(this.hashCode());
		strBuf.append(" }");
		return strBuf.toString();
	}

}
