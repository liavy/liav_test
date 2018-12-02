package com.sap.ejb.ql.sqlmapper;

/**
 * Provides methods to map an EJB-QL query to an SQL statement and to create various types
 * of SQL statements used by the EJB container to manage persistency of EJB CMP, based
 * on the SQL mapper's underlying OR mapping.
 * </p><p>
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 * @see SQLMapperFactory 
 * @see SQLMappingResult
 */

public interface SQLMapper {
	/**
	 * Maps a given EJB-QL query to a database independent SQL representation.
	 * <p></p>
	 * @param ejbQlQuery
	 * 		EJB-QL query to be mapped to SQL.
	 * @return
	 * 		the <code>SQLMappingResult</code> for a given EJB-QL query.
	 * @throws SQLMappingException
	 * 		if EJB-QL query can not be mapped to an SQL statement.
         * @see com.sap.ejbql.tree.Query
	 */
	public SQLMappingResult mapEjbQl(com.sap.ejbql.tree.Query ejbQlQuery)
		throws SQLMappingException;

        /**
         * Maps a given EJB-QL query to a database independent SQL representation.
         * When <code>makeDistinct</code> set to <code>true</code> <code>DISTINCT</code>
         * is added to <code>SELECT</code> clause of generated SQL representation,
         * if not already present anyway; when <code>makeDistinct</code> set to <code>false</code>
         * this method is identical to {@link #mapEjbQl(com.sap.ejbql.tree.Query ejbQlQuery)}.
         * <p></p>
         * Parameter <code>makeDistinct</code> may be used when the return type of the respective
         * EJB finder/select method requires the generated <code>SQLStatement</code> to
         * be a <code>SELECT DISTINCT</code> statement, irrespective whether the underlying EJBQL
         * query itself has been specified to be <code>DISTINCT</code> or not.
         * </p><p>
         * @param ejbQlQuery
         *              EJB-QL query to be mapped to SQL.
         * @param makeDistinct
         *              if <code>true</code> add <code>DISTINCT</code> to 
         *              <code>SELECT</code> clause.
         * @return
         *              the <code>SQLMappingResult</code> for a given EJB-QL query.
         * @throws SQLMappingException
         *              if EJB-QL query can not be mapped to an SQL statement.
         * @see com.sap.ejbql.tree.Query
         */
        public SQLMappingResult mapEjbQl(com.sap.ejbql.tree.Query ejbQlQuery, boolean makeDistinct)
                throws SQLMappingException;

        /**
         * Maps a given EJB-QL query to a database independent SQL representation.
         * When <code>haveForUpdate</code> is set to <code>true</code> a <code>FOR UPDATE</code>
         * clause is added to <code>SELECT</code> clause of generated SQL representation if
         * a bean object is selected and the respective been type has been tagged for update
         * in the OR mapping;
         * when <code>haveForUpdate</code> set to <code>false</code> this method is identical 
         * to {@link #mapEjbQl(com.sap.ejbql.tree.Query ejbQlQuery, boolean makeDistinct)}
         * <p></p>
         * Note that due to restrictions of the SQL92 standard reflected in the <code>com.sap.sql.tree</code>
         * package not all generated SQL statements may be added a <code>FOR UPDATE</code> clause;
         * a <code>SQLMappingException</code> will be thrown in that case.
         * <p></p>
         * When <code>makeDistinct</code> is set to <code>true</code> <code>DISTINCT</code>
         * is added to <code>SELECT</code> clause of generated SQL representation,
         * if not already present anyway. 
         * <p></p>
         * Parameter <code>makeDistinct</code> may be used when the return type of the respective
         * EJB finder/select method requires the generated <code>SQLStatement</code> to
         * be a <code>SELECT DISTINCT</code> statement, irrespective whether the underlying EJBQL
         * query itself has been specified to be <code>DISTINCT</code> or not.
         * </p><p>
         * @param ejbQlQuery
         *              EJB-QL query to be mapped to SQL.
         * @param makeDistinct
         *              if <code>true</code> add <code>DISTINCT</code> to
         *              <code>SELECT</code> clause.
         * @param haveForUpdate
         *              if <code>true</code> add <code>FOR UPDATE</code> to
         *              <code>SELECT</code> clause if selected bean type is tagged so. 
         * @return
         *              the <code>SQLMappingResult</code> for a given EJB-QL query.
         * @throws SQLMappingException
         *              if EJB-QL query can not be mapped to an SQL statement.
         * @see com.sap.ejbql.tree.Query
         */
        public SQLMappingResult mapEjbQl(com.sap.ejbql.tree.Query ejbQlQuery, boolean makeDistinct,
                                                                              boolean haveForUpdate)
                throws SQLMappingException;

        /**
         * Maps a given EJB-QL query to a database independent SQL representation.
         * </p><p>
         * When <code>noEmptyTables</code> is set to <code>true</code> the mapper will assume that none of
         * the tables from the abstract schema involved in this query will be completely empty. It
         * may then take use of this fact generating an optimised SQL statement that may only
         * work properly when this assumption is met. When <code>noEmptyTables</code> is
         * set to <code>false</code> this method is identical to
         * {@link #mapEjbQl(com.sap.ejbql.tree.Query ejbQlQuery, boolean makeDistinct, boolean haveForUpdate)}
         * </p><p>
         * When <code>haveForUpdate</code> is set to <code>true</code> a <code>FOR UPDATE</code>
         * clause is added to <code>SELECT</code> clause of generated SQL representation if
         * a bean object is selected and the respective been type has been tagged for update
         * in the OR mapping.
         * <p></p>
         * Note that due to restrictions of the SQL92 standard reflected in the <code>com.sap.sql.tree</code>
         * package not all generated SQL statements may be added a <code>FOR UPDATE</code> clause;
         * a <code>SQLMappingException</code> will be thrown in that case.
         * <p></p>
         * When <code>makeDistinct</code> is set to <code>true</code> <code>DISTINCT</code>
         * is added to <code>SELECT</code> clause of generated SQL representation,
         * if not already present anyway.
         * <p></p>
         * Parameter <code>makeDistinct</code> may be used when the return type of the respective
         * EJB finder/select method requires the generated <code>SQLStatement</code> to
         * be a <code>SELECT DISTINCT</code> statement, irrespective whether the underlying EJBQL
         * query itself has been specified to be <code>DISTINCT</code> or not.
         * </p><p>
         * @param ejbQlQuery
         *              EJB-QL query to be mapped to SQL.
         * @param makeDistinct
         *              if <code>true</code> add <code>DISTINCT</code> to
         *              <code>SELECT</code> clause.
         * @param haveForUpdate
         *              if <code>true</code> add <code>FOR UPDATE</code> to
         *              <code>SELECT</code> clause if selected bean type is tagged so.
         * @param noEmptyTables
         *              if <code>true</code> the mapper will assume that none of
         *              the tables from the abstract schema involved in this query
         *              will be completely empty.
         * @return
         *              the <code>SQLMappingResult</code> for a given EJB-QL query.
         * @throws SQLMappingException
         *              if EJB-QL query can not be mapped to an SQL statement.
         * @see com.sap.ejbql.tree.Query
         */
        public SQLMappingResult mapEjbQl(com.sap.ejbql.tree.Query ejbQlQuery, boolean makeDistinct,
                                                       boolean haveForUpdate, boolean noEmptyTables)
                throws SQLMappingException;

        /**
         * Creates a <code>Select</code> statement to load all database columns of a certain enterprise 
         * java bean with given primary key. To be used for <code>ejbLoad</code> methods.
         * </p><p>
         * @param abstractBeanName
         *              name of abstract bean that load statement is to be created for.
         * @param haveForUpdate
         *              indicates whether a <code>For Update</code> clause is to be added 
         *              to the <code>Select</code> statement if selected bean type is tagged so.
         * @return
         *              the <code>SQLMappingResult</code> describing the generated SQL
         *              statement.
         * @throws SQLMappingException
         *              if SQL statement cannot be created.
         */
        public SQLMappingResult createEJBSelect(String abstractBeanName, boolean haveForUpdate)
                throws SQLMappingException;

        /**
         * Creates an SQL statement to check existence of an enterprise
         * java bean with given primary key. To be used by the ejb container.
         * </p><p>
         * @param abstractBeanName
         *              name of abstract bean to be checked.
         * @return
         *              the <code>SQLMappingResult</code> describing the generated SQL
         *              statement.
         * @throws SQLMappingException
         *              if SQL statement cannot be created.
         */
        public SQLMappingResult createEJBExists(String abstractBeanName)
                throws SQLMappingException;

        /**
         * Creates an <code>Insert</code> statement to store all database columns of a certain enterprise
         * java bean. To be used for <code>ejbStore</code> methods.
         * </p><p>
         * @param abstractBeanName
         *              name of abstract bean that insert statement is to be created for.
         * @return
         *              the <code>SQLMappingResult</code> describing the generated SQL
         *              statement.
         * @throws SQLMappingException
         *              if SQL statement cannot be created.
         */
        public SQLMappingResult createEJBInsert(String abstractBeanName)
                throws SQLMappingException;

        /**
         * Creates an <code>Update</code> statement to modify all non-primary-key database columns 
         * of a certain enterprise java bean with given primary key. 
         * To be used for <code>ejbStore</code> methods.
         * </p><p>
         * @param abstractBeanName
         *              name of abstract bean that update statement is to be created for.
         * @return
         *              the <code>SQLMappingResult</code> describing the generated SQL
         *              statement.
         * @throws SQLMappingException
         *              if SQL statement cannot be created.
         */
        public SQLMappingResult createEJBUpdate(String abstractBeanName)
                throws SQLMappingException;

        /**
         * Creates a <code>Delete</code> statement to remove a certain enterprise java bean
         * from database with given primary key.
         * To be used for <code>ejbRemove</code> methods.
         * </p><p>
         * @param abstractBeanName
         *              name of abstract bean that delete statement is to be created for.
         * @return
         *              the <code>SQLMappingResult</code> describing the generated SQL
         *              statement.
         * @throws SQLMappingException
         *              if SQL statement cannot be created.
         */
        public SQLMappingResult createEJBDelete(String abstractBeanName)
                throws SQLMappingException;

        /**
         * Creates a <code>Select</code> statement to load all columns of all database rows of a particular
         * enterprise java bean that are in CMR with another particular enterprise java bean,
         * whose primary key is given.  
         * </p><p>
         * @param relation
         *              description of the underlying CMR.
         * @param haveForUpdate
         *              indicates whether a <code>For Update</code> clause is to be added
         *              to the <code>Select</code> statement if selected bean type is tagged so.
         * @return
         *              the <code>SQLMappingResult</code> describing the generated SQL
         *              statement.
         * @throws SQLMappingException
         *              if SQL statement cannot be created.
         */
		public SQLMappingResult createEJBIndirectSelect(com.sap.engine.interfaces.ejb.orMapping.CommonRelation relation,
													boolean haveForUpdate)
			throws SQLMappingException;
        
        /**
         * Creates a <code>Select</code> statement to load all foreign key columns related to a particular
         * enterprise java bean with given primary key. The foreign key columns are selected from a helper
         * table on the database representing an M:N CMR; for each row selected, the foreign key columns represent
         * primary key columns of the enterprise java bean residing <code>onOtherSideOfRelation</code>.
         * Given primary key is also included within the statement's result set.
         * </p><p>
         * @param relation
         *              description of the underlying M:N CMR.
         * @param haveForUpdate
         *              indicates whether a <code>For Update</code> clause is to be added
         *              to the <code>Select</code> statement if selected bean type is tagged so.
         * @return
         *              the <code>SQLMappingResult</code> describing the generated SQL
         *              statement.
         * @throws SQLMappingException
         *              if SQL statement cannot be created.
         */
        public SQLMappingResult createHelperTableSelect(com.sap.engine.interfaces.ejb.orMapping.CommonRelation relation,
                                                        boolean haveForUpdate)
                throws SQLMappingException;

        /**
         * Creates an <code>Insert</code> statement to store a row into a helper table on the database
         * representing an M:N CMR.
         * </p><p>
         * @param relation
         *              description of the underlying M:N CMR.
         * @return
         *              the <code>SQLMappingResult</code> describing the generated SQL
         *              statement.
         * @throws SQLMappingException
         *              if SQL statement cannot be created.
         */
        public SQLMappingResult createHelperTableInsert(com.sap.engine.interfaces.ejb.orMapping.CommonRelation relation)
                throws SQLMappingException;

        /**
         * Creates a <code>Delete</code> statement to remove a row from a helper table on the database
         * representing an M:N CMR.
         * </p><p>
         * @param relation
         *              description of the underlying M:N CMR.
         * @return
         *              the <code>SQLMappingResult</code> describing the generated SQL
         *              statement.
         * @throws SQLMappingException
         *              if SQL statement cannot be created.
         */
        public SQLMappingResult createHelperTableDelete(com.sap.engine.interfaces.ejb.orMapping.CommonRelation relation)
                throws SQLMappingException;

        /**
         * Creates a <code>Delete</code> statement to remove all entries that are related to a particular enterprise
         * java bean with given primary key from a helper table on the database representing an M:N CMR.
         * </p><p>
         * @param relation
         *              description of the underlying M:N CMR.
         * @return
         *              the <code>SQLMappingResult</code> describing the generated SQL
         *              statement.
         * @throws SQLMappingException
         *              if SQL statement cannot be created.
         */
		public SQLMappingResult createHelperTableMultipleDelete(com.sap.engine.interfaces.ejb.orMapping.CommonRelation relation)
				throws SQLMappingException;

        /**
         * Ensures that EJB-QL queries with an identification variable as select 
         * clause will be mapped to SQL statements that return the primary 
         * key fields of the respective entity bean's database representation only. 
         * Eventually differing properties set at creation of <code>SQLMapper</code>
         * instance are permanently overwritten by this method.
         * </p><p>
         * <B>Hint</B>&nbsp;: Handle with care in case multiple threads share 
         * same <code>SQLMapper</code> instance&nbsp;!
         **/
        public void setSelectPrimaryKeyFields();

        /**
         * Ensures that EJB-QL queries with an identification variable as select
         * clause will be mapped to SQL statements that return the complete database
         * representation of the bean.
         * Eventually differing properties set at creation of <code>SQLMapper</code>
         * instance are permanently overwritten by this method.
         * </p><p>
         * <b>Note</b>, however, that this setting might be temporarily ignored,
         * e.g. in the case the <code>SQLMapper</code> encounters
         * so-called incomparable jdbc types.
         * For more details on this issue, please, refer to documentation
         * of method <code>wasSelectPrimaryKeyFieldsForced()</code> of class
         * <code>SQLMappingResult</code>.
         * </p><p>
         * <B>Hint</B>&nbsp;: Handle with care in case multiple threads share
         * same <code>SQLMapper</code> instance&nbsp;!
         **/ 
        public void setSelectAllBeanFields();

        /**
         * Indicates that resulting SQL statements are to be used for Native SQL only.
         * </p><p>
         * In this mode, table name identifiers are incorporated into generated SQL statements
         * as are, i.e. without parsing, composition/decomposition or using quotes.
         * </p><p>
         * <B>Hint</B>&nbsp;: Handle with care in case multiple threads share
         * same <code>SQLMapper</code> instance&nbsp;!
         **/
        public void setNativeMode();

        /**
         * Indicates that resulting SQL statements are to be used for Native SQL only.
         * </p><p>
         * In this mode, table name identifiers are incorporated into generated SQL statements
         * as are, i.e. without parsing, composition/decomposition or using quotes.
         * </p><p>
         * Additionally specifies the default database vendor for the <code>SQLMappingResults</code>
         * that will be created by this mapper instance.
         * </p><p>
         * You should only use SAP supported database platforms as database vendor, otherwise
         * vendor may be considered to be unknown; alternatively an <code>SQLMappingException</code> may
         * be thrown. This is considered to be implementation dependent.
         * <p></p>
         * <B>Hint</B>&nbsp;: Handle with care in case multiple threads share
         * same <code>SQLMapper</code> instance&nbsp;!
         * <p></p>
         * @param databaseVendorID
         *              Integer representation of the database vendor as defined by class
         *               <code>NativeSQLAccess</code>.
         * @throws SQLMappingException
         *              if the database platform is not supported.
         * @see com.sap.sql.NativeSQLAccess
         **/
        public void setNativeMode(int databaseVendorID)
        throws SQLMappingException;

        /**
         * Indicates that resulting SQL statements are to be used for Native SQL only.
         * </p><p>
         * In this mode, table name identifiers are incorporated into generated SQL statements
         * as are, i.e. without parsing, composition/decomposition or using quotes.
         * </p><p>
         * Additionally specifies the default database vendor for the <code>SQLMappingResults</code>
         * that will be created by this mapper instance.
         * </p><p>
         * You should only use SAP supported database platforms as database vendor, otherwise
         * vendor may be considered to be unknown; alternatively an <code>SQLMappingException</code> may
         * be thrown. This is considered to be implementation dependent.
         * <p></p>
         * <B>Hint</B>&nbsp;: Handle with care in case multiple threads share
         * same <code>SQLMapper</code> instance&nbsp;!
         * <p></p>
         * @param databaseVendorProductName
         *              Database vendor product name as reckognized by class
         *               <code>NativeSQLAccess</code>.
         * @throws SQLMappingException
         *              if the database platform is not supported.
         * @see com.sap.sql.NativeSQLAccess
         **/
        public void setNativeMode(String databaseVendorProductName)
        throws SQLMappingException;

        /**
         * Checks whether a given table name meets the restrictions imposed on table name
         * space by Open SQL.
         * </p><p>
         * @param tableName
         *              table name to be checked for its Open SQL compatibility.
         * @return
         *              boolean, indicating whether given table name may be used with
         *              OpenSQL.
         **/
        public boolean isValidForOpenSQL(String tableName);        
}
