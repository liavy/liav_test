package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;

import com.sap.sql.tree.ejb21_TableReference;

import com.sap.ejb.ql.sqlmapper.common.PKBeanDescriptor;
import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;

/**
 * Represents all of information concerning a database table
 * required to later from an EXISTS subquery on that table.
 * </p><p>
 * Copyright (c) 2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class TableRepresentation
{

	private ejb21_TableReference tabRef;
        private String beanName;
        private PKBeanDescriptor pkBDesc;

	/** 
	 * Creates a <code>TableRepresentation</code> instance.
         * </p><p>
         * @param tabRef
         *     table reference.
         * @param beanName
         *     name of represented bean.
         * @param pkBDesc
         *     primary key bean descriptor.
	 */
	TableRepresentation(ejb21_TableReference tabRef, String beanName, PKBeanDescriptor pkBDesc)
        {
          this.tabRef = tabRef;
          this.beanName = beanName;
          this.pkBDesc = pkBDesc;
	}

	    ejb21_TableReference getTabRef()
        {
          return this.tabRef;
        }

        DatabaseColumn getOneOfThePrimaryKeyColumns()
        throws SQLMappingException
        {
          DatabaseColumn[] pkColumns = this.pkBDesc.getColumnNames();

          if ( pkColumns == null || pkColumns.length == 0 )
          {
            throw new SQLMappingException("No primary key column found for table " + this.tabRef.getTableName(),
                                          "SQL mapper could not determine a primary key column for table "
                                          + this.tabRef.getTableName() + "representing EJB " + this.beanName
                                          + ". This is an error in the OR mapping or an internal programming error"
                                          + " in the ejb service or the SQL mapper. Please kindly open a problem"
                                          + " ticket for SAP on component BC-JAS-EJB.",
                                          "CSM153");
          }
          else
          {
            return pkColumns[0];
          }
       }
        
	/**
	 * Creates string representation of a <code>TableRepresentation</code> instance.
	 */
	public String toString() {
		StringBuffer strBuf = new StringBuffer("{ tabRef = ");
		strBuf.append(this.tabRef.toString());
		strBuf.append(", bean = ");
		strBuf.append(this.beanName);
                strBuf.append(", pkBDesc = ");
                strBuf.append(this.pkBDesc.toString());
		strBuf.append(" }");

		return strBuf.toString();
	}

}
