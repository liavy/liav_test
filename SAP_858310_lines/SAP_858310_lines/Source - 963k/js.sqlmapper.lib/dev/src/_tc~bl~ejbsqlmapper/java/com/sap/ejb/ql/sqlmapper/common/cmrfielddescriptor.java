package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.FieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.M2MDescriptor;
import com.sap.ejb.ql.sqlmapper.common.BeanTable;
import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;

/**
 * Describes relavant OR mapping details of a CMR bean field 
 * for the <code>CommonSQLMapper</code>. Instances of this class
 * are created by the <code>ORMappingManager</code>.
 *
 * <p></p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public class CMRFieldDescriptor extends FieldDescriptor {
	private DatabaseColumn[] columnNames;
	private int multiplicity;
        private boolean haveHelperTable;
	private DatabaseColumn[] otherSideColumnNames;
	private M2MDescriptor m2mDesc;
	private CheckableIdentifier helperTableIdentifier;

	/**
	 * Creates a <code>CMRFieldDescriptor</code> instance.
	 * <p></p>
	 * @param table
	 * 		table to which the CMR bean field's bean is mapped.
	 * @param columNames
	 * 		array of names of columns to which the CMR bean field type's
	 * 		fields are mapped.
	 * @param multiplicity
	 * 		multiplicity of CMR bean field.
	 * @param otherSideColumnNames
	 * 		array of names of columns of the other side of the
	 * 		CMR bean field's relation.
	 * @param m2mDesc
	 * 		M2M information of CMR bean field, if it has
	 * 		multiplicity <code>MANY_TO_MANY</code>;
	 * 		<code>null</code> otherwise.
         * @param helperTableIdentifier
         *              helper table used by <code>MANY_TO_MANY</code> relation, if
         *              CMR bean field is such; <code>null</code> else.
	 */
	CMRFieldDescriptor(
		BeanTable table,
		DatabaseColumn[] columnNames,
		int multiplicity,
                boolean haveHelperTable,
		DatabaseColumn[] otherSideColumnNames,
		M2MDescriptor m2mDesc,
		CheckableIdentifier helperTableIdentifier) {

		this.table = table;
		this.columnNames = columnNames;
		this.multiplicity = multiplicity;
                this.haveHelperTable = haveHelperTable;
		this.otherSideColumnNames = otherSideColumnNames;
		this.m2mDesc = m2mDesc;
		this.helperTableIdentifier = helperTableIdentifier;
	}

	DatabaseColumn[] getColumnNames() {
		return this.columnNames;
	}

	int getMultiplicity() {
		return this.multiplicity;
	}

        boolean hasHelperTable()
        {
          return this.haveHelperTable;
        }

	DatabaseColumn[] getColumnNamesOfOtherSide() {
		return this.otherSideColumnNames;
	}

	M2MDescriptor getM2MDescriptor() {
		return this.m2mDesc;
	}

	CheckableIdentifier getHelperTableIdentifier() {
		return this.helperTableIdentifier;
	}

	/**
	 * Creates string representation of <code>CMRFieldDescriptor</code> instance.
	 * @return
	 *     string representation of <code>CMRFieldDescriptor</code> instance.
	 **/
	public String toString() {
		StringBuffer strBuf = new StringBuffer("{ table = ");
		strBuf.append(this.table.toString());
		strBuf.append(", columnNames = ");
		if (this.columnNames != null) {
			strBuf.append("{ ");
			for (int i = 0; i < this.columnNames.length; i++) {
				if (i != 0)
					strBuf.append(", ");
				strBuf.append(this.columnNames[i].toString());
			}
			strBuf.append(" }");
		} else {
			strBuf.append("null");
		}
		strBuf.append(", multiplicity = ");
		strBuf.append(this.multiplicity);
                strBuf.append(", hasHelperTable = ");
                if ( this.haveHelperTable )
                {
                  strBuf.append("true");
                }
                else
                {
                  strBuf.append("false");
                }
		strBuf.append(", otherSideColumnNames = ");
		if (this.otherSideColumnNames != null) {
			strBuf.append("{ ");
			for (int i = 0; i < this.otherSideColumnNames.length; i++) {
				if (i != 0)
					strBuf.append(", ");
				strBuf.append(
					this.otherSideColumnNames[i].toString());
			}
			strBuf.append(" }");
		} else {
			strBuf.append("null");
		}
                strBuf.append(", M2M = ");
                if ( this.m2mDesc != null )
                {
                  strBuf.append(this.m2mDesc.toString());
                }
                else
                {
                  strBuf.append("null");
                }
		strBuf.append(", helperTable = ");
		if (this.helperTableIdentifier != null) {
			strBuf.append(this.helperTableIdentifier.toString());
		} else {
			strBuf.append("null");
		}
		strBuf.append(", hashcode = ");
		strBuf.append(this.hashCode());
		strBuf.append(" }");
		return strBuf.toString();
	}
}
