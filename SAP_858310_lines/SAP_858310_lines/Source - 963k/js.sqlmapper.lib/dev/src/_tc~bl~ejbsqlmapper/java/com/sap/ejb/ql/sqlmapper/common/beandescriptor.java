package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;

/**
 * Describes relavant OR mapping details of a bean type 
 * for the <code>CommonSQLMapper</code>.
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public abstract class BeanDescriptor {
	
	/**
	 * BeanTable to which bean type is mapped.
	 */	
	BeanTable beanTable;

	/**
	 * Default constructor - to be invoked only from 
	 * <code>PKBeanDescriptor</code> or
	 * <code>FullBeanDescriptor</code>
	 */
	BeanDescriptor() {
	}

	/**
	 * Gets BeanTable object
	 * <p></p>
	 * @return
	 * 		the BeanTable object to which bean type is mapped.
	 */
	final BeanTable getBeanTable() {
		return this.beanTable;
	}

	abstract String[] getFieldNames();
	abstract DatabaseColumn[] getColumnNames();
	abstract Integer[] getJdbcTypes();
	abstract Boolean[] getPKFlags();
        abstract Boolean[] getDVFlags();
        abstract String[] getSubFieldNames();
        abstract Boolean[] getRelationFlags();
        abstract String[] getRelatedBeans();
        abstract String[] getRelatedBeanFields();
        abstract boolean getUpdateFlag();
}
