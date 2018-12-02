package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.BeanDescriptor;
import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;

/**
 * Describes relavant OR mapping details of a bean type 
 * for the <code>CommonSQLMapper</code>, where
 * all columns existing on the database for this bean type
 * are included; also those columns where no
 * bean field is existing. These kind of fields/columns are
 * called technical fields/columns. Instances of this class
 * are created by the <code>ORMappingManager</code>.
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class TechnicalBeanDescriptor extends BeanDescriptor {
	private FullBeanDescriptor fullBeanDesc;
	private String[] fieldNames;
	private DatabaseColumn[] columnNames;
	private Integer[] jdbcTypes;
	private Boolean[] pkFlags;
        private Boolean[] dvFlags;
        private String[] subFieldNames;
        private Boolean[] relFlags;
        private String[] relBeans;
        private String[] relBeanFields;

	/**
	 * Creates a <code>PKBeanDescriptor</code> instance.
	 * <p></p>
	 * @param tableName
	 * 		name of table to which the bean type is mapped.
	 * @param fullBeanDesc
	 *      full bean descriptor
		 * @param technicalFieldNames
	 * 		array of technical field names corresponding to the technical columns.
	 * @param technicalColumnNames
	 * 		array of technical column names. 
	 * @param technicalJdbcTypes
	 * 		array of technical columns' jdbc types.
         * @param technicalPKFlags
	 * 		array of primary key indicators of technical columns
         * @param technicalRelFlags
         *              array of relationship indicators of technical columns.
         * @param technicalRelBeans
         *              array of related beans of technical columns.
         * @param technicalRelBeanFields
         *              array of related bean fields of technical columns.
	 */

	TechnicalBeanDescriptor(
		FullBeanDescriptor fullBeanDesc,
		String[] technicalFieldNames,
		DatabaseColumn[] technicalColumnNames,
		Integer[] technicalJdbcTypes,
		Boolean[] technicalPKFlags,
                Boolean[] technicalRelFlags,
                String[] technicalRelBeans,
                String[] technicalRelBeanFields) {

		this.fullBeanDesc = fullBeanDesc;
		this.beanTable = fullBeanDesc.getBeanTable();
		
		String[] fullBeanFieldNames = fullBeanDesc.getFieldNames(); 
		int n = fullBeanFieldNames.length;
		int m = technicalFieldNames.length;
		this.fieldNames = new String[n + m];
		int i = 0;
		for (; i < n; i++) {
			this.fieldNames[i] = fullBeanFieldNames[i];
		}
		for (; i < n+m; i++) {
			this.fieldNames[i] = technicalFieldNames[i-n];
		}
		
		DatabaseColumn[] fullBeanColumnNames = fullBeanDesc.getColumnNames(); 
		n = fullBeanColumnNames.length;
		m = technicalColumnNames.length;
		this.columnNames = new DatabaseColumn[n + m];
		i = 0;
		for (; i < n; i++) {
			this.columnNames[i] = fullBeanColumnNames[i];
		}
		for (; i < n+m; i++) {
			this.columnNames[i] = technicalColumnNames[i-n];
		}
		
		Integer[] fullBeanJdbcTypes = fullBeanDesc.getJdbcTypes(); 
		n = fullBeanJdbcTypes.length;
		m = technicalJdbcTypes.length;
		this.jdbcTypes = new Integer[n + m];
		i = 0;
		for (; i < n; i++) {
			this.jdbcTypes[i] = fullBeanJdbcTypes[i];
		}
		for (; i < n+m; i++) {
			this.jdbcTypes[i] = technicalJdbcTypes[i-n];
		}
		
		Boolean[] fullBeanPKFlags = fullBeanDesc.getPKFlags(); 
		n = fullBeanPKFlags.length;
		m = technicalPKFlags.length;
		this.pkFlags = new Boolean[n + m];
		i = 0;
		for (; i < n; i++) {
			this.pkFlags[i] = fullBeanPKFlags[i];
		}
		for (; i < n+m; i++) {
			this.pkFlags[i] = technicalPKFlags[i-n];
		}

                Boolean[] fullBeanDVFlags = fullBeanDesc.getDVFlags();
                this.dvFlags = new Boolean[n + m];
                i = 0;
                for (; i < n; i++)
                {
                  this.dvFlags[i] = fullBeanDVFlags[i];
                }
                for (; i < n+m; i++)
                {
                  this.dvFlags[i] = new Boolean(false);
                }

                String[] fullBeanSubFieldNames = fullBeanDesc.getSubFieldNames();
                this.subFieldNames = new String[n + m];
                i = 0;
                for (; i < n; i++)
                {
                  this.subFieldNames[i] = fullBeanSubFieldNames[i];
                }
                for (; i < n+m; i++)
                {
                  this.subFieldNames[i] = null;
                }

                Boolean[] fullBeanRelFlags = fullBeanDesc.getRelationFlags();
                n = fullBeanRelFlags.length;
                m = technicalRelFlags.length;
                this.relFlags = new Boolean[n + m];
                i = 0;
                for (; i < n; i++) {
                        this.relFlags[i] = fullBeanRelFlags[i];
                }
                for (; i < n+m; i++) {
                        this.relFlags[i] = technicalRelFlags[i-n];
                }

                String[] fullBeanRelBeans = fullBeanDesc.getRelatedBeans();
                n = fullBeanRelBeans.length;
                m = technicalRelBeans.length;
                this.relBeans = new String[n + m];
                i = 0;
                for (; i < n; i++) {
                        this.relBeans[i] = fullBeanRelBeans[i];
                }
                for (; i < n+m; i++) {
                        this.relBeans[i] = technicalRelBeans[i-n];
                }

                String[] fullBeanRelBeanFields = fullBeanDesc.getRelatedBeanFields();
                n = fullBeanRelBeanFields.length;
                m = technicalRelBeanFields.length;
                this.relBeanFields = new String[n + m];
                i = 0;
                for (; i < n; i++) {
                        this.relBeanFields[i] = fullBeanRelBeanFields[i];
                }
                for (; i < n+m; i++) {
                        this.relBeanFields[i] = technicalRelBeanFields[i-n];
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
          return this.fullBeanDesc.getUpdateFlag();
        }

	/**
	 * Creates string representation of <code>TechnicalBeanDescriptor</code> instance.
	 * @return
	 *     string representation of <code>TechnicalBeanDescriptor</code> instance.
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
						+ this.pkFlags[i].booleanValue()
                                                + ", "
                                                + this.dvFlags[i].booleanValue()
                                                + ", "
                                                + ( (this.subFieldNames[i] == null)
                                                  ? "(null)" : this.subFieldNames[i])
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
                if ( this.fullBeanDesc.getUpdateFlag() )
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
