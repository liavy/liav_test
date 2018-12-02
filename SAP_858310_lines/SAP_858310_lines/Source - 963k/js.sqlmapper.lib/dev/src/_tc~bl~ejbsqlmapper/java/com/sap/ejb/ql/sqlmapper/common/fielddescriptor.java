package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.BeanTable;

/**
 * Describes relavant OR mapping details of a bean field 
 * for the <code>CommonSQLMapper</code>.
 * <p></p>
 * Copyright (c) 2002, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public abstract class FieldDescriptor {
	/**
	 * Table to which bean field's bean is mapped.
	 */
        BeanTable table;

	/**
	 * Default Constructor - to be called by
         * CMPFieldDescriptor
         * CMRFieldDescriptor
         * DVFieldDescriptor
         * only.
	 */
	FieldDescriptor() {
	}

	/**
	 * Gets table's name.
	 * <p></p>
         * @param nativeMode
         *      indicating whether native mode has been set for handling of table names.
	 * @return
	 * 	the name of the table.
	 */
	final String getTableName(boolean nativeMode) {
		return this.table.getName(nativeMode);
	}

}
