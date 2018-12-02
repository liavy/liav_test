package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.BeanDescriptor;
import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;

/**
 * Describes relavant OR mapping details of a bean type 
 * for the <code>CommonSQLMapper</code>, where
 * only the primary key fields, primary key columns respectively,
 * are included. Instances of this class
 * are created by the <code>ORMappingManager</code>.
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */
public class PKBeanDescriptor extends BeanDescriptor {

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
	 * 		BeanTable object to which the bean type is mapped.
	 * @param fieldNames
	 * 		array of the bean's field names.
	 * @param columnsNames
	 * 		array of names of columns to which the bean fields
	 * 		are mapped.
	 * @param jdbcTypes
	 * 		array of columns' jdbc types.
	 * @param pkFlags
	 * 		array of primary key indicators.
         * @param haveForUpdate
         *              flag indicating whether bean should be locked at select.
	 */
	PKBeanDescriptor(
		BeanTable beanTable,
		String[] fieldNames,
		DatabaseColumn[] columnNames,
		Integer[] jdbcTypes,
		Boolean[] pkFlags,
                boolean haveForUpdate) {

		this.beanTable = beanTable;
		this.fieldNames = fieldNames;
		this.columnNames = columnNames;
		this.jdbcTypes = jdbcTypes;
		this.pkFlags = pkFlags;
                this.haveForUpdate = haveForUpdate;

                int i = this.columnNames.length;

                this.dvFlags = new Boolean[i];
                this.subFieldNames = new String[i];
                this.relFlags = new Boolean[i];
                this.relBeans = new String[i];
                this.relBeanFields = new String[i];

                for (int j = 0; j < i; j++)
                {
                  this.dvFlags[j] = new Boolean(false);
                  this.subFieldNames[j] = null;
                  this.relFlags[j] = new Boolean(false);
                  this.relBeans[j] = null;
                  this.relBeanFields[j] = null;
                }             
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
	 * Creates string representation of <code>PKBeanDescriptor</code> instance.
	 * @return
	 *     string representation of <code>PKBeanDescriptor</code> instance.
	 **/
	public String toString() {
		StringBuffer strBuf = new StringBuffer("{ beanTable= ");
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
						+ this.pkFlags[i].booleanValue()
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
