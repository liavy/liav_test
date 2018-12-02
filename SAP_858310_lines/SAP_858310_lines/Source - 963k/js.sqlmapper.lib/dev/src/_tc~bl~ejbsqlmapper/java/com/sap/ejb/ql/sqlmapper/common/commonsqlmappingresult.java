package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.SQLMappingResult;
import com.sap.ejb.ql.sqlmapper.InputDescriptor;
import com.sap.ejb.ql.sqlmapper.ResultDescriptor;
import com.sap.ejb.ql.sqlmapper.common.CommonInputDescriptor;
import com.sap.ejb.ql.sqlmapper.common.CommonResultDescriptor;

import com.sap.sql.tree.SQLStatement;
import com.sap.sql.NativeSQLAccess;

import com.sap.ejb.ql.sqlmapper.general.DevTrace;
import com.sap.tc.logging.Location;

/**
 * Describes the result of an EJB-QL to SQL mapping process
 * or an EJB load/store SQL statement creation.
 * Generally, an SQL mapping result is a database independent
 * representation of the resulting SQL statement. Only its
 * its statement string representation may be database dependent.
 * </p><p>
 * You may decide by means of methods {@link #setOpenSQL()} and
 * {@link #setNativeSQL(int)} resp. {@link #setNativeSQL(String)}
 * what kind of statement string representation {@link #getStatementString()}
 * will create.
 * </p><p>
 * If there is no Open SQL compliant string representation for a
 * <code>CommonSQLMappingResult</code> the statement string representation
 * returned by  {@link #getStatementString()} will default to Native
 * SQL for the database vendor evaluated by <code>CommonSQLMapper</code>
 * from the underlying OR mapping or property <code>databaseVendor</code>.
 * </p><p>
 * Copyright (c) 2002-2006, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.3
 * @see com.sap.ejb.ql.sqlmapper.SQLMapper
 * @see com.sap.ejb.ql.sqlmapper.common.CommonSQLMapper
 */
public class CommonSQLMappingResult implements SQLMappingResult {
	private final static Location loc =
		Location.getLocation(CommonSQLMappingResult.class);
	private final static String setDatabaseVendor = "setDatabaseVendor";
        private final static String setOpenSQL = "setOpenSQL";
        private final static String setNativeSQL = "setNativeSQL";
        private final static String setNativeSQLIntParms[] = { "databaseVendorId" };
        private final static String setNativeSQLStringParms[] = { "databaseVendorProductName" };

	private SQLStatement sqlStatement;
	private CommonInputDescriptor[] inputDescriptor;
	private CommonResultDescriptor[] resultDescriptor;
        private int databaseVendorID;
        private boolean isNotOpenSQL;
        private boolean forNativeUseOnly;
	private boolean PKFieldsForced;
        private boolean nativeStatementString;
        private boolean nonEmptyTablesRequired;

	/**
	 * Creates a <code>CommonSQLMappingResult</code> instance.
	 * To be invoked by <code>CommonSQLMapper</code> only.
	 * </p><p>
	 * @param sqlStatement
	 *    the <code>SQLStatement</code> describing the SQL statement
	 *    resulting from the mapping process.
	 * @param inputDescriptor
	 *    array of <code>CommonInputDescriptor</code>s 
	 *    resulting from the mapping process.
	 * @param resultDescriptor
	 *    array of <code>CommonResultDescriptor</code>s
	 *    resulting from the mapping process.
	 * @param databaseVendorID
	 *    preset database vendor ID.
         * @param isNotOpenSQL
         *    whether <code>SQLStatement</code> is database dependent.
         * @param forNativeUseOnly
         *    whether <code>SQLStatement</code> may be used for native SQL only.
         * @param pkFieldsForced
	 *    whether <code>SQLStatement</code> selects primary
	 *    key fields only though it originally had been
	 *    intended to select all bean fields.
         * @param nonEmptyTablesRequired
         *    whether non-empty database tables have been assumed.
	 */
	CommonSQLMappingResult(
		SQLStatement sqlStatement,
		CommonInputDescriptor[] inputDescriptor,
		CommonResultDescriptor[] resultDescriptor,
                int databaseVendorID,
                boolean isNotOpenSQL,
                boolean forNativeUseOnly,
		boolean pkFieldsForced,
                boolean nonEmptyTablesRequired) {

		this.sqlStatement = sqlStatement;
		this.inputDescriptor = inputDescriptor;
		this.resultDescriptor = resultDescriptor;
                this.databaseVendorID = databaseVendorID;
                this.isNotOpenSQL = isNotOpenSQL;
                this.forNativeUseOnly = forNativeUseOnly;
		this.PKFieldsForced = pkFieldsForced;
                this.nonEmptyTablesRequired = nonEmptyTablesRequired;
                this.nativeStatementString = this.isNotOpenSQL || this.forNativeUseOnly;
	}

	/**
	 * Retrieves SQL statment tree as computed by the EJB-QL to SQL mapping.
	 * It is not recommended to invoke <code>getSQLStatement().toSqlString()</code> in case
	 * the SQL statement is database dependent.
	 * <p></p>
	 * @return
	 *              <code>SQLStatement</code>.
	 */
	public SQLStatement getSQLStatement() {
		return this.sqlStatement;
	}

	/**
	 * Retrieves the SQL statement string as computed by the EJB-QL to SQL mapping.
	 * <code>getStatementString()</code> basically uses <code>getSQLStatement().toSqlString()</code>.
	 * This method is thread-safe. 
	 * <p></p>
	 * @return
	 *              SQL statement string.
	 * @see CommonSQLMappingResult#setDatabaseVendor(String ProductName)
	 * @see CommonSQLMappingResult#setDatabaseVendor(int VendorID)
	 */
	public String getStatementString() {		
		if (this.sqlStatement != null) {
			String statement;
                        int databaseVendorId = this.isNativeSQL()
                                                 ? this.databaseVendorID
                                                 : SQLStatement.VENDOR_OPENSQL;
			synchronized (this.sqlStatement) {
				statement = this.sqlStatement.toSqlString(databaseVendorId);
			}
			return statement;
		} else {
			return null;
		}
	}

	/**
	 * Retrieves an array of the SQL statement's input parameters' descriptions.
	 * <p></p>
	 * @return
	 *              <code>InputDescriptor[]</code>. Implementation chosen
	 *              for <code>InputDescriptor</code> is <code>CommonInputDescriptor</code>.
	 * @see CommonInputDescriptor
	 */
	public InputDescriptor[] getInputDescriptors() {
		return this.inputDescriptor;
	}

	/**
	 * Retrieves an array of column descriptions for the result set to be returned by
	 * SQL statement.
	 * <p></p>
	     * If the result set represents an enterprise java bean you may expect the bean's
	     * primary key columns to come first in both the result descriptor array and the
	     * result set. This, in particular, implies that you may rely on the array's
	     * first element to determine whether the result set represents enterprise java
	     * bean objects (see 
	     * {@link com.sap.ejb.ql.sqlmapper.common.CommonResultDescriptor#isObjectRepresentation()}).
	     * </p><p>
	 * @return
	 *              <code>ResultDescriptor[]</code>. Implementation chosen
	 *              for <code>ResultDescriptor</code> is <code>CommonResultDescriptor</code>.
	 * @see CommonResultDescriptor
	 */
	public ResultDescriptor[] getResultDescriptors() {
		return this.resultDescriptor;
	}

	/**
	 * Indicates whether selection of primary key fields only was forced for this <code>CommonSQLMappingResult</code>.
	 * </p><p>
	 * By using methods <code>setSelectPrimaryKeyFields</code> and <code>setSelectAllBeanFields</code>
	 * of class <code>CommonSQLMapper</code> as well as by property <code>com.sap.ejb.ql.sqlmapper.returnCompleteBeans</code>
	 * you may control whether an entity java bean is represented
	 * in the generated <code>SQLStatement</code> by its primary key fields only or by all its bean
	 * fields. Depending on their mapping to the database, some bean fields might be represented
	 * by jdbc types that are not comparable. Among others, this means those must not appear in
	 * a select list when the select statement is denoted to be distinct. While all primary
	 * key fields of an entity java bean are necessarily mapped to comparable jdbc types,
	 * this needn't apply to the remaining bean fields.
	 * </p><p>
	 * When <code>CommonSQLMapper</code> has been set to select all bean fields for representation of
	 * entity java beans and encounters a situation that would require to select a database column
	 * that is not comparable
	 * where this is not allowed, the <code>CommonSQLMapper</code> will ignore this setting for the
	 * respective (and only the respective) <code>CommonSQLMappingResult</code> and behave as it had been
	 * set to select primary key fields only for representation of entity java beans.
         * Another case where this setting is ignored is the following:
         * The bean fields of a bean are selected via selection of a cmr field of another bean.
         * The foreign key fields (resp. database columns) are contained in the table
         * the other bean is mapped to. Here a join of the tables the involved beans are mapped to
         * is avoided to be able to return null values if existing in the foreign key columns.
         * The alternative approach to select all bean fields (resp. database columns) via a join
         * of the involved tables would offer the possibility to select the complete database 
         * representation of the bean, but would not select potential existing null values
         * in the foreign key columns and hence is not admissible.
         * <code>wasSelectPrimaryKeyFieldsForced()</code> indicates that a situation has occurred
         * where only the primary key fields are selected and where it was ignored to select all
         * bean fields.
	 * </p><p>
	 * <b>Note</b> that <code>wasSelectPrimaryKeyFieldsForced()</code> will return <code>true</code>
	 * if an only if <code>CommonSQLMapper</code> was set to select all bean fields <b>and</b> this setting
	 * was ignored for the current <code>CommonSQLMappingResult</code>.
	 * </p><p>
	 * @return
	 *     <code>true</code> iff explicit setting to select all bean fields was actually ignored.
	 **/
	public boolean wasSelectPrimaryKeyFieldsForced() {
		return this.PKFieldsForced;
	}

        /**
         * Sets Open SQL statement string representation. Subsequent calls of {@link #getStatementString()}
         * are to generate statement string representations appropriate for Open SQL connections.
         * @return
         *             <code>true</code> if an Open SQL compliant statement string representation may be
         *             created for this <code>CommonSQLMappingResult</code>
         *             - subsequent calls of <code>getStatemenString()</code>
         *             will do so.<br>
         *             <code>false</code> if an Open SQL compliant statement string representation may not
         *             be created for this <code>CommonSQLMappingResult</code>
         *             - in that case, the method call will have no impact on further calls of
         *             <code>getStatemenString()</code>.
         * @see #isOpenSQL()
         * @see #hasOpenSQLVersion()
         */
        public synchronized boolean setOpenSQL()
        {
          DevTrace.entering(loc, setOpenSQL, null, null);
          if ( this.hasOpenSQLVersion() )
          {
            this.nativeStatementString = false;
            DevTrace.exiting(loc, setOpenSQL, true);
            return true;
          }
          else
          {
            DevTrace.exiting(loc, setOpenSQL, false);
            return false;
          }
        }

        /**
         * Sets Native SQL statement string representation. Subsequent calls of {@link #getStatementString()}
         * are to generate statement string representations appropriate for Native SQL connections to given
         * database vendor.
         * </p><p>
         * You should only use SAP supported database platforms as database vendor, otherwise
         * vendor may be considered to be unknown.
         * <p></p>
         * @param databaseVendorID
         *              Integer representation of the database vendor as defined by class
         *               <code>NativeSQLAccess</code>.
         * @throws SQLMappingException
         *              if the database platform is not supported (optional).
         * @see #isNativeSQL()
         * @see com.sap.sql.NativeSQLAccess
         */
        public synchronized void setNativeSQL(int databaseVendorID) throws SQLMappingException
        {
          if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object[] inputValues = { new Integer(databaseVendorID) };
            DevTrace.entering(loc, setNativeSQL, setNativeSQLIntParms, inputValues);
          }

          this.nativeStatementString = true;
          this.databaseVendorID = databaseVendorID;

          DevTrace.exiting(loc, setNativeSQL);
        }

        /**
         * Sets Native SQL statement string representation. Subsequent calls of {@link #getStatementString()}
         * are to generate statement string representations appropriate for Native SQL connections to given
         * database vendor.
         * </p><p>
         * You should only use SAP supported database platforms as database vendor, otherwise
         * vendor may be considered to be unknown.
         * <p></p>
         * @param databaseVendorProductName
         *              Database vendor product name as reckognized by class
         *               <code>NativeSQLAccess</code>.
         * @throws SQLMappingException
         *              if the database platform is not supported (optional).
         * @see #isNativeSQL()
         * @see com.sap.sql.NativeSQLAccess
         */
        public void setNativeSQL(String databaseVendorProductName) throws SQLMappingException
        {
          if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object[] inputValues = { databaseVendorProductName };
            DevTrace.entering(loc, setNativeSQL, setNativeSQLStringParms, inputValues);
          }

          this.nativeStatementString = true;
          this.databaseVendorID = NativeSQLAccess.getVendorID(databaseVendorProductName);

          DevTrace.exiting(loc, setNativeSQL);
        }


        /**
         * Tells whether subsequent call of {@link #getStatementString()} will return a statement string
         * representation appropriate for Open SQL connections.
         * </p><p>
         * @return
         *             <code>true</code> if subsequent call of {@link #getStatementString()}
         *             will return a statement string representation appropriate for Open SQL connections;<br>
         *             <code>false</code> elsewise.
         * @see #setOpenSQL()
         */
        public boolean isOpenSQL()
        {
          return (! this.nativeStatementString);
        }

        /**
         * Tells whether subsequent call of {@link #getStatementString()} will return a statement string
         * representation appropriate for Native SQL connections.
         * </p><p>
         * @return
         *             <code>true</code> if subsequent call of {@link #getStatementString()}
         *             will return a statement string representation appropriate for Native SQL connections;<br>
         *             <code>false</code> elsewise.
         * @see #setNativeSQL(int)
         * @see #setNativeSQL(String)
         */
        public boolean isNativeSQL() 
        {
          return this.nativeStatementString; 
        }

        /**
         * Retrieves ID of database vendor statement string representation are currently created for.
         * </p><p>
         * @return
         *             If {@link #isNativeSQL()} ID of database vendor a subsequent call of {@link #getStatementString()}
         *             will create statement string representation for;<br>
         *             if {@link #isOpenSQL()} {@link com.sap.sql.NativeSQLAccess.VENDOR_UNKNOWN} is returned.
         * @see #setNativeSQL(int)
         * @see #setNativeSQL(String)
         * @see com.sap.sql.NativeSQLAccess
         */
        public int getCurrentDatabaseVendor()
        {
          return this.nativeStatementString ? this.databaseVendorID
                                            : NativeSQLAccess.VENDOR_UNKNOWN;
        }

        /**
         * Tells whether a statement string representation appropriate for Open SQL connections may at all be
         * created for this <code>CommonSQLMappingResult</code>. A call of {@link #setOpenSQL()} will succeed iff
         * this method returns <code>true</code>.
         * </p><p>
         * @return
         *             <code>true</code> if an Open SQL compliant statement string representation may be created;<br>
         *             <code>false</code> elsewise.
         * @see #setOpenSQL()
         */
        public boolean hasOpenSQLVersion()
        {
          return (! this.isNotOpenSQL) && (! this.forNativeUseOnly);
        }

        /**
         * Tells whether the SQL statement was generated under the assumption that all database tables
         * involved are not completely empty and would not work properly elsewise.
         * </p><p>
         * @return
         *             <code>true</code> if non empty tables are requiredr;<br>
         *             <code>false</code> elsewise.
         */
        public boolean requiresNonEmptyTables()
        {
          return this.nonEmptyTablesRequired;
        }

	/**
	 * Creates string representation of <code>CommonSQLMappingResult</code> instance.
	 * @return
	 *     string representation of <code>CommonSQLMappingResult</code> instance.
	 **/
	public String toString() 
        {
		StringBuffer strBuf = new StringBuffer("{ SQLStatement = ");
		if (this.sqlStatement != null)
                {
                  strBuf.append("{ ");
                  synchronized (this.sqlStatement)
                  {
                    strBuf.append(this.sqlStatement.toSqlString(SQLStatement.VENDOR_UNKNOWN));
                  }
                  strBuf.append(" }");
                }
                else
                {
                  strBuf.append("null");
                }
		strBuf.append(", InputDescriptors = ");
		if (this.inputDescriptor != null) {
			for (int i = 0; i < this.inputDescriptor.length; i++) {
				if (i != 0)
					strBuf.append(", ");
				strBuf.append(this.inputDescriptor[i].toString());
			}
		} else {
			strBuf.append("null");
		}
		strBuf.append(", ResultDescriptors = ");
		if (this.resultDescriptor != null) {
			for (int i = 0; i < this.resultDescriptor.length; i++) {
				if (i != 0)
					strBuf.append(", ");
				strBuf.append(this.resultDescriptor[i].toString());
			}
		} else {
			strBuf.append("null");
		}
                strBuf.append(", preset db vendor = ");
                strBuf.append(this.databaseVendorID);
		if (this.PKFieldsForced) {
			strBuf.append(", PK fields enforced");
		}
                if ( this.isNotOpenSQL || this.forNativeUseOnly )
                {
                  strBuf.append(", NativeSQL");
                }
                else
                {
                  strBuf.append(", OpenSQL");
                }
                if ( this.forNativeUseOnly )
                {
                  strBuf.append(" on NativeSQL connections only");
                }
                else
                {
                  strBuf.append(" on any connection");
                }
                if ( this.nonEmptyTablesRequired )
                {
                  strBuf.append(", non-empty tables assumed");
                }
		strBuf.append(", hashcode = ");
		strBuf.append(this.hashCode());
		strBuf.append(" }");
		return strBuf.toString();

	}
}
