package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.BeanDescriptor;
import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;

/**
 * Describes relavant OR mapping details of a bean type 
 * for the <code>CommonSQLMapper</code>, where
 * all columns on the database are included for which a
 * corresponding bean field is existing. Instances of this class
 * are created by the <code>ORMappingManager</code>.
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class FullBeanDescriptor extends BeanDescriptor {
	private String[] fieldNames;
	private DatabaseColumn[] columnNames;
	private Integer[] jdbcTypes;
	private Boolean[] pkFlags;
        private Boolean[] dvFlags;
        private String[] subFieldNames;
        private Boolean[] relFlags;
        private String[] relBeans;
        private String[] relBeanFields;
        private boolean haveForUpdate;

	/**
	 * Creates a <code>PKBeanDescriptor</code> instance.
	 * <p></p>
	 * @param beanTable
	 * 		beanTable object to which the bean type is mapped.
	 * @param fieldNames
	 * 		array of field names.
	 * @param columnNames
	 * 		array of names of columns to which the bean fields
	 * 		are mapped.
	 * @param jdbcTypes
	 * 		array of columns' jdbc types.
	 * @param pkFlags
	 * 		array of primary key indicators.
         * @param dvFlags
         *              array of dependent value field indicators.
         * @param subFieldNames
         *              array of eventual sub field names for dependent
         *              value fields.
         * @param relFlags
         *              array of relationship indicators.
         * @param relBeans
         *              array of related beans.
         * @param relBeanFields
         *              array of related bean fields.
         * @param haveForUpdate
         *              indicates whether bean is to be locked at select.
	 */

	FullBeanDescriptor(
		BeanTable beanTable,
		String[] fieldNames,
		DatabaseColumn[] columnNames,
		Integer[] jdbcTypes,
		Boolean[] pkFlags,
                Boolean[] dvFlags,
                String[] subFieldNames,
                Boolean[] relFlags,
                String[] relBeans,
                String[] relBeanFields,
                boolean haveForUpdate) {

		this.beanTable = beanTable;
		this.fieldNames = fieldNames;
		this.columnNames = columnNames;
		this.jdbcTypes = jdbcTypes;
		this.pkFlags = pkFlags;
                this.dvFlags = dvFlags;
                this.subFieldNames = subFieldNames;
                this.relFlags = relFlags;
                this.relBeans = relBeans;
                this.relBeanFields = relBeanFields;
                this.haveForUpdate = haveForUpdate;
	}

	String[] getFieldNames() {
		return this.fieldNames;
	}

	DatabaseColumn[] getColumnNames() {
		return this.columnNames;
	}

	Integer[] getJdbcTypes() {
		return this.jdbcTypes;
	}

	Boolean[] getPKFlags() {
		return this.pkFlags;
	}

        Boolean[] getDVFlags()
        {
          return this.dvFlags;
        }

        String[] getSubFieldNames()
        {
          return this.subFieldNames;
        }

        Boolean[] getRelationFlags()
        {
          return this.relFlags;
        }

        String[] getRelatedBeans()
        {
          return this.relBeans;
        }

        String[] getRelatedBeanFields()
        {
          return this.relBeanFields;
        }

        boolean getUpdateFlag()
        {
          return this.haveForUpdate;
        }

	/**
	 * Creates string representation of <code>FullBeanDescriptor</code> instance.
	 * @return
	 *     string representation of <code>FullBeanDescriptor</code> instance.
	 **/
	public String toString() {
		StringBuffer strBuf = new StringBuffer("{ beanTable = ");
		strBuf.append(this.beanTable.toString());
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
						+ this.fieldNames[i]
						+ ", "
						+ this.pkFlags[i].booleanValue()                                                + ", "
                                                + this.dvFlags[i].booleanValue()
                                                + ", "
                                                + ( (this.subFieldNames[i] == null)
                                                  ? "(null)" : this.subFieldNames[i] )
						+ ", "
                                                + this.relFlags[i].booleanValue()
                                                + ", "
                                                + ( (this.relBeans[i] == null)
                                                  ? "(null)" : this.relBeans[i] )
                                                + ", "
                                                + ( (this.relBeanFields[i] == null)
                                                  ? "(null)" : this.relBeanFields[i] )
                                                + ")");
			}
			strBuf.append(" }");
		} else {
			strBuf.append("null");
		}
                strBuf.append(", updateFlag = ");
                if ( this.haveForUpdate )
                {
                  strBuf.append("true");
                }
                else
                {
                  strBuf.append("false");
                }
		strBuf.append(", hashcode = ");
		strBuf.append(this.hashCode());
		strBuf.append(" }");
		return strBuf.toString();
	}
}
