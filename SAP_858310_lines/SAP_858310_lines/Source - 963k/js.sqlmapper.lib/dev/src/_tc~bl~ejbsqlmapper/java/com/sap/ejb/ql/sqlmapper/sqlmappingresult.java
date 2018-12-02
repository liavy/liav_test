package com.sap.ejb.ql.sqlmapper;

import com.sap.sql.tree.SQLStatement;
import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.InputDescriptor;
import com.sap.ejb.ql.sqlmapper.ResultDescriptor;

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
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 * @see com.sap.ejb.ql.sqlmapper.SQLMapper
 */

public interface SQLMappingResult {
	/**
	 * Retrieves the SQL statement string as computed by the EJB-QL to SQL mapping.
	 * <code>getStatementString()</code> basically uses <code>getSQLStatement().toSqlString()</code>, but
	 * will throw an exception if a database dependent SQL language element
	 * is encountered that is not supported by the current database vendor.
	 * This method is thread-safe.
	 * <p></p>
	 * @return
	 * 		SQL statement string.
	 * @throws SQLMappingException
	 * 		if a database dependent SQL language element is encountered,
	 * 		that is not supported by the current database vendor.
         * @see SQLMappingResult#setDatabaseVendor(String ProductName)
         * @see SQLMappingResult#setDatabaseVendor(int VendorID)
	 */
	public String getStatementString() throws SQLMappingException;

	/**
	 * Retrieves SQL statment tree as computed by the EJB-QL to SQL mapping.
	 * It is not recommended to invoke <code>getSQLStatement().toSqlString()</code> in case
	 * the SQL statement is database dependent. 
	 * <p></p>
	 * @return
	 * 		<code>SQLStatement</code>.
	 * @see SQLMappingResult#isStatementDatabaseDependent
	 */
	public SQLStatement getSQLStatement();
	
	/**
	 * Retrieves an array of the SQL statement's input parameters' descriptions.
	 * <p></p>
	 * @return
	 * 		<code>InputDescriptor[]</code>.
	 */
	public InputDescriptor[] getInputDescriptors();

	/**
	 * Retrieves an array of column descriptions for the result set to be returned by 
	 * SQL statement.
	 * <p></p>
         * If the result set represents an enterprise java bean you may expect the bean's
         * primary key columns to come first in both the result descriptor array and the
         * result set. This, in particular, implies that you may rely on the array's
         * first element to determine whether the result set represents enterprise java
         * bean objects (see {@link com.sap.ejb.ql.sqlmapper.ResultDescriptor#isObjectRepresentation()}).
         * </p><p>   
	 * @return
	 * 		<code>ResultDescriptor[]</code>.
	 */
	public ResultDescriptor[] getResultDescriptors();

        /**
         * Indicates whether selection of primary key fields only was forced for this <code>SQLMappingResult</code>.
         * </p><p>
         * By using methods <code>setSelectPrimaryKeyFields</code> and <code>setSelectAllBeanFields</code>
         * of class <code>SQLMapper</code> you may control whether an entity java bean is represented
         * in the generated <code>SQLStatement</code> by its primary key fields only or by all its bean
         * fields. Depending on their mapping to the database, some bean fields might be represented
         * by jdbc types that are not comparable. Among others, this means those must not appear in
         * a select list when the select statement is denoted to be distinct. While all primary
         * key fields of an entity java bean are necessarily mapped to comparable jdbc types,
         * this needn't apply to the remaining bean fields.
         * </p><p>
         * When <code>SQLMapper</code> has been set to select all bean fields for representation of
         * entity java beans and encounters a situation that would require to select a database column
         * that is not comparable where this is not allowed,
         * the <code>SQLMapper</code> will ignore this setting for the
         * respective (and only the respective) <code>SQLMappingResult</code> and behave as it had been
         * set to select primary key fields only for representation of entity java beans.
         * Another case where this setting is ignored is the following:
         * The bean fields of a bean are selected via selection of a cmr field of another bean.
         * The foreign key fields (resp. database columns) are contained in the table
         * the other bean is mapped to. Here a join of the tables the involved beans are mapped to
         * is avoided to be able to return null values if existing in the foreign key columns.
         * The alternative approach to select all bean fields (resp. database columns) via a join
         * of the involved tables would offer the possibility to select the complete database 
         * representation of the bean, but would not select potential existing null values
         * in the foreign key columns and hence is not admissable.
         * <code>wasSelectPrimaryKeyFieldsForced()</code> indicates that a situation has occurred
         * where only the primary key fields are selected and where it was ignored to select all
         * bean fields.
         * </p><p>
         * <b>Note</b> that <code>wasSelectPrimaryKeyFieldsForced()</code> will return <code>true</code>
         * if an only if <code>SQLMapper</code> was set to select all bean fields <b>and</b> this setting
         * was ignored for the current <code>SQLMappingResult</code>.
         * </p><p>
         * @return
         *     <code>true</code> iff explicit setting to select all bean fields was actually ignored.
         **/ 
        public boolean wasSelectPrimaryKeyFieldsForced();

        /**
         * Sets Open SQL statement string representation. Subsequent calls of {@link #getStatementString()}
         * are to generate statement string representations appropriate for Open SQL connections.
         * @return
         *             <code>true</code> if an Open SQL compliant statement string representation may be
         *             created for this <code>SQLMappingResult</code>
         *             - subsequent calls of <code>getStatemenString()</code>
         *             will do so.<br>
         *             <code>false</code> if an Open SQL compliant statement string representation may not
         *             be created for this <code>SQLMappingResult</code>
         *             - in that case, the method call will have no impact on further calls of
         *             <code>getStatemenString()</code>.
         * @see #isOpenSQL()
         * @see #hasOpenSQLVersion()
         */
        public boolean setOpenSQL();

        /**
         * Sets Native SQL statement string representation. Subsequent calls of {@link #getStatementString()}
         * are to generate statement string representations appropriate for Native SQL connections to given
         * database vendor.
         * </p><p>
         * You should only use SAP supported database platforms as database vendor, otherwise
         * vendor may be considered to be unknown; alternatively an <code>SQLMappingException</code> may
         * be thrown. This is considered to be implementation dependent.
         * <p></p>
         * @param databaseVendorID
         *              Integer representation of the database vendor as defined by class
         *               <code>NativeSQLAccess</code>.
         * @throws SQLMappingException
         *              if the database platform is not supported.
         * @see #isNativeSQL()
         * @see com.sap.sql.NativeSQLAccess
         */
        public void setNativeSQL(int databaseVendorID) throws SQLMappingException;

        /**
         * Sets Native SQL statement string representation. Subsequent calls of {@link #getStatementString()}
         * are to generate statement string representations appropriate for Native SQL connections to given
         * database vendor.
         * </p><p>
         * You should only use SAP supported database platforms as database vendor, otherwise
         * vendor may be considered to be unknown; alternatively an <code>SQLMappingException</code> may
         * be thrown. This is considered to be implementation dependent.
         * <p></p>
         * @param databaseVendorProductName
         *              Database vendor product name as reckognized by class
         *               <code>NativeSQLAccess</code>.
         * @throws SQLMappingException
         *              if the database platform is not supported.
         * @see #isNativeSQL()
         * @see com.sap.sql.NativeSQLAccess
         */
        public void setNativeSQL(String databaseVendorProductName) throws SQLMappingException;

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
        public boolean isOpenSQL();

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
        public boolean isNativeSQL();

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
        public int getCurrentDatabaseVendor();

        /** 
         * Tells whether a statement string representation appropriate for Open SQL connections may at all be
         * created for this <code>SQLMappingResult</code>. A call of {@link #setOpenSQL()} will succeed iff
         * this method returns <code>true</code>.
         * </p><p>
         * @return
         *             <code>true</code> if an Open SQL compliant statement string representation may be created;<br>
         *             <code>false</code> elsewise.
         * @see #setOpenSQL()
         */
        public boolean hasOpenSQLVersion();

        /**
         * Tells whether the SQL statement was generated under the assumption that all database tables
         * involved are not completely empty and would not work properly elsewise.
         * </p><p>
         * @return
         *             <code>true</code> if non empty tables are requiredr;<br>
         *             <code>false</code> elsewise.
         */
        public boolean requiresNonEmptyTables();

}
