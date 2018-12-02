package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.SQLMappingResult;
import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.SQLMapperFactory;
import com.sap.ejb.ql.sqlmapper.general.SQLMapperImplementation;
import com.sap.ejb.ql.sqlmapper.general.SQLChecker;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;
import com.sap.ejb.ql.sqlmapper.common.ORMappingManager;
import com.sap.ejb.ql.sqlmapper.common.ORMappingManagerFactory;
import com.sap.ejb.ql.sqlmapper.common.EJBInterpreter;
import com.sap.ejb.ql.sqlmapper.common.EJBLoadStoreBuilder;
import com.sap.ejb.ql.sqlmapper.common.EJBQLTreeProcessor;
import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager;
import com.sap.ejb.ql.sqlmapper.common.MappingAgents;
import com.sap.ejb.ql.sqlmapper.common.ORMapCatalogReader;
import com.sap.ejb.ql.sqlmapper.common.EJBLoadStoreBuilderThreadLocal;
import com.sap.ejb.ql.sqlmapper.common.EJBQLTreeProcessorThreadLocal;
import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManagerThreadLocal;
import com.sap.ejb.ql.sqlmapper.common.CommonSQLMappingResult;
import com.sap.ejb.ql.sqlmapper.common.RelationField;
import com.sap.ejb.ql.sqlmapper.common.HelperTableFacade;

import com.sap.ejbql.tree.Query;

import com.sap.engine.interfaces.ejb.orMapping.CommonRelation;
import com.sap.sql.services.OpenSQLServices;
import com.sap.sql.catalog.CatalogReader;
import com.sap.sql.tree.SQLStatement;
import com.sap.sql.tree.StringRepresentationFailureException;
import com.sap.sql.jdbc.common.StatementAnalyzer;
import com.sap.sql.NativeSQLAccess;

import com.sap.tc.logging.Location;

import java.sql.SQLException;

/**
 * Implements the <code>SQLMapper</code> interface.
 * </p><p>
 * The purpose of this class is to provide methods for
 * mapping of EJB-QL queries to SQL statements and for creation of various
 * types of SQL statments needed by the EJB container in order to manage persistency
 * of EJB CMP. Generation of SQL statements is based on an underlying
 * OR mapping. This OR mapping is bound to the <code>CommonSQLMapper</code>
 * instance at time of instantiation and may no more be changed during the
 * life time of this instance. It is recommended that you only use one
 * <code>CommonSQLMapper</code> instance per OR mapping so that subsequent calls
 * of method <code>mapEjbQl()</code> and the various <code>create...()</code> methods
 * may benefit from the <code>CommonSQLMapper</code>'s
 * built in OR mapping caches.
 * <p></p>
 * The bahaviour of a <code>CommonSQLMapper</code> instance is influenced
 * by the following runtime properties set by the <code>createSQLMapper()</code>
 * method of </code>SQLMapperFactory</code>&nbsp;:
 * <ul>
 *   <li> <code>com.sap.ejb.ql.sqlmapper.databaseVendor</code>
 *        <p>
 *        To be prepared to deal with EJB-QL expressions whose SQL
 *        equivalents have database dependent SQL test representations,
 *        the <code>CommonSQLMapper</code> instance retrieves the
 *        current database vendor from the underlying OR mapping.
 *        That value may be overwritten by the above runtime 
 *        property.
 *        </p><p>
 *        <b>Note</b>, however, that current database vendor still may
 *        be changed any time after mapping an EJB-QL query to
 *        an SQL statement by <code>setNativeSQL()</code> methods
 *        of the resulting <code>SQLMappingResult</code>. <code>CommonSQLMapper</code>
 *        will do its job flawlessly even if database vendor is unknown
 *        at time of mapping.
 *        </p><p>
 *        Even more so, the database vendor may also be ovewritten at any time
 *        by the mapper's <code>setNativeMode()</code> methods.
 *        </p>
 *   </li>
 *   <li> <code>com.sap.ejb.ql.sqlmapper.returnCompleteBeans</code>
 *        <p>
 *        If this property is set to <code>true</code>,
 *        the <code>CommonSQLMapper</code> instance will map
 *        all EJB-QL queries with an identification variable as
 *        select clause to SQL statements that return the complete  
 *        database representation of the bean.
 *        Otherwise (especially if the property has not been set at all)
 *        the generated SQL statements will only return
 *        the primary key columns of the respective bean's database 
 *        representation.
 *        </p>
 *        <p>
 *        This property may be overwritten by <code>CommonSQLMapper</code>'s
 *        setSelect-methods. It may also be temporarily ignored, e.g. in the
 *        case that so-called incomparable jdbc types are encountered.
 *        For more details on the latter,
 *        see documentation of method <code>wasSelectPrimaryKeyFieldsForced()</code>
 *        of class <code>CommonSQLMappingResult</code>.
 *        </p>
 *   </li>
 *   <li> <code>com.sap.ejb.ql.sqlmapper.nativeMode</code>
 *        <p>
 *        If this property is set to <code>true</code>,
 *        the <code>CommonSQLMapper</code> will incorporate all occuring
 *        table names as are (i.e. without quotes or any other treatment
 *        like parsing, decomposing and composing). The resulting
 *        statements must be used for Native SQL and Vendor SQL only.
 *        This is indicated by the <code>hasOpenSQLVersion()</code> method
 *        of class <code>CommonSQLMappingResult</code> returning <code>false</code>.
 *        </p><p>
 *        This property may be overwritten by <code>CommonSQLMapper</code>'s
 *        method <code>setNativeMode()</code>.
 *        
 *   <li> <code>com.sap.ejb.ql.sqlmapper.OpenSQLVerification</code>
 *        <p>
 *        If set to either <code>syntax</code> or <code>semantics</code>
 *        any generated <code>SQLStatement</code> will be checked for its compability
 *        with <code>OpenSQL</code> unless it contains a <code>FOR UPDATE</code> clause.
 *        Value <code>syntax</code> will trigger
 *        syntactic checks only, while <code>semantics</code> will <b>also</b>
 *        include semantic checks.
 *        </p><p>
 *        If set to <code>none</code> or any other value no checks will be
 *        performed. In that case any generated <code>SQLStatement</code>
 *        is considered <code>OpenSQL</code> compliant unless it contains
 *        classes from package <code>com.sap.ejb.ql.sqlmapper.tree</code>.
 *        </p><p>
 *   </li>
 *   <li> <code>com.sap.ejb.ql.sqlmapper.checkResult</code>
 *        <p>
 *        If this property is set to <code>true</code>,
 *        after mapping an EJB-QL query to an SQL statement,
 *        the <code>CommonSQLMapper</code> instance will check
 *        the generated SQL statement against an external SQL checker (including those
 *        containing a <code>FOR UPDATE</code> clause.
 *        </p><p>
 *        For that purpose runtime property
 *        <code>com.sap.ejb.ql.sqlmapper.SQLChecker</code> is evaluated
 *        to load the therein specified implementation of the
 *        <code>SQLChecker</code> interface. <code>CommonSQLMapper</code>
 *        will apply the <code>checkSQLStatement()</code> method of interface
 *        <code>SQLChecker</code> on the generated SQL statement in
 *        order to perform the check. Any exceptions occurring
 *        during this check will be returned within an 
 *        <code>SQLMappingException</code>. If no exceptions
 *        occur the check is considered to have passed successfully.
 *        </p>
 *   </li>
 *   <li> <code>com.sap.ejb.ql.sqlmapper.failsafe</code>
 *        <p>
 *        If this property is set to <code>yes</code>,
 *        the <code>CommonSQLMapper</code> will switch to a so-called
 *        failsafe mode. This feature allows to introduce new implementation
 *        methods concurrently to existing coding. In case unforseen difficulties
 *        occur with the already activated new coding, <code>CommonSQLMapper</code>
 *        may be told to use the old coding instead by setting this property.
 *        Whether the property is currently effective and between which kind of
 *        implementations it switches is of temporary nature and needs to be asked from
 *        the authors.
 *        </p>
 *   </li>
 * </ul>
 * Current default settings for the properties listed above, as defined
 * in the <code>sqlm.properties</code> file of package <code>com.sap.ejb.ql.sqlmapper</code>,
 * are as follows&nbsp;:
 * </p><p>
 * <table rows=5 cols=2 border=1 cellpadding=2 cellspacing=2>
 *   <thead>
 *     <th>Property</th>
 *     <th>Default setting</th>
 *   </thead>
 *   <tr>
 *     <td align=left nowrap><code>com.sap.ejb.ql.sqlmapper.databaseVendor</code></td>
 *     <td align=left nowrap>(none)</td>
 *   </tr>
 *   <tr>
 *     <td align=left nowrap><code>com.sap.ejb.ql.sqlmapper.returnCompleteBeans</code></td>
 *     <td align=left nowrap>(none)</td>
 *   </tr>
 *   <tr>
 *     <td align=left nowrap><code>com.sap.ejb.ql.sqlmapper.nativeMode</code></td>
 *     <td align=left nowrap>(none)</td>
 *   </tr>
 *   <tr>
 *     <td align=left nowrap><code>com.sap.ejb.ql.sqlmapper.OpenSQLVerification</code></td>
 *     <td align=left nowrap>semantics</td>
 *   </tr>
 *   <tr>
 *     <td align=left nowrap><code>com.sap.ejb.ql.sqlmapper.checkResult</code></td>
 *     <td align=left nowrap>false</td>
 *   </tr>
 *   <tr>
 *     <td align=left nowrap><code>com.sap.ejb.ql.sqlmapper.SQLChecker</code></td>
 *     <td align=left nowrap>com.sap.ejb.ql.sqlmapper.debug.OpenSQLChecker</td>
 *   </tr>
 *   <tr>
 *     <td align=left nowrap><code>com.sap.ejb.ql.sqlmapper.failsafe</td>
 *     <td align=left nowrap>(none)</td>
 *   </tr>
 * </table>
 * <p></p>
 * Class <code>CommonSQLMapper</code> takes use of classes <code>SQLTreeNodeManager</code>
 * and <code>EJBQLTreeProcessor</code> to perform its job.
 * <p></p>
 * Copyright (c) 2002-2006, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.3
 * @see com.sap.ejb.ql.sqlmapper.SQLMapperFactory
 * @see com.sap.ejb.ql.sqlmapper.SQLMapper
 * @see com.sap.ejb.ql.sqlmapper.general.SQLMapperImplementation
 * @see com.sap.ejb.ql.sqlmapper.general.SQLChecker
 * @see com.sap.ejb.ql.sqlmapper.debug.OpenSQLChecker
 * @see com.sap.ejb.ql.sqlmapper.common.CommonSQLMappingResult
 * @see com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.EJBQLTreeProcessor
 */
public final class CommonSQLMapper extends SQLMapperImplementation {
        private ORMappingManager               orMapping;
        private boolean                        forNativeUseOnly;
        private boolean                        forNativeUseOnlyOverWritten;
        private boolean                        returnCompleteBeans;
        private boolean                        returnCompleteBeansOverWritten;
        private String                         databaseVendor;
        private int                            databaseVendorId;
        private int                            effectiveDatabaseVendorId;
        private boolean                        databaseVendorOverWritten;
	private String                         packageName;
        private boolean                        verifySyntax;
        private boolean                        verifySemantics;
        private CatalogReader                  catalogReader;
        private StatementAnalyzer              openSQLVerifier;
	private String                         sqlCheckerImpl;
	private SQLChecker                     sqlChecker;
        private boolean                        failsafe;
        private SQLTreeNodeManagerThreadLocal  sqlTreeNodeManagerInstance;
        private EJBQLTreeProcessorThreadLocal  ejbqlTreeProcessorInstance;
        private EJBLoadStoreBuilderThreadLocal ejbLoadStoreBuilderInstance;

        private static final Location loc = Location.getLocation(CommonSQLMapper.class);
        private static final String mapEjbQl = "mapEjbQl"; 
        private static final String createEJBSelect = "createEJBSelect";
        private static final String createEJBExists = "createEJBExists";
        private static final String createEJBInsert = "createEJBInsert";
        private static final String createEJBUpdate = "createEJBUpdate";
        private static final String createEJBDelete = "createEJBDelete";
        private static final String createEJBIndirectSelect = "createEJBIndirectSelect";
        private static final String createHelperTableSelect = "createHelperTableSelect";
        private static final String createHelperTableInsert = "createHelperTableInsert";
        private static final String createHelperTableDelete = "createHelperTableDelete";
        private static final String createHelperTableMultipleDelete = "createHelperTableMultipleDelete";
        private static final String setSelectPrimaryKeyFields = "setSelectPrimaryKeyFields";
        private static final String setSelectAllBeanFields = "setSelectAllBeanFields";
        private static final String setForNativeSQLOnly = "setForNativeSQLOnly";
        private static final String setNativeMode = "setNativeMode";
        private static final String isValidForOpenSQL= "isValidForOpenSQL";
        private static final String initialize = "initialize";
        private static final String checkStatement = "checkStatement";
        private static final String mapEjbQlParms[] = { "query", "makeDistinct", "haveForUpdate", "noEmptyTables" };
        private static final String createEJBSelectParms[] = { "abstractBeanName", "haveForUpdate" };
        private static final String createEJBExistsParms[] = { "abstractBeanName" };
        private static final String createEJBInsertParms[] = { "abstractBeanName" };
        private static final String createEJBUpdateParms[] = { "abstractBeanName" };
        private static final String createEJBDeleteParms[] = { "abstractBeanName" };
        private static final String createEJBIndirectSelectParms[] = { "relation", "haveForUpdate" };
        private static final String createHelperTableSelectParms[] = { "relation", "haveForUpdate" };
        private static final String createHelperTableInsertParms[] = { "relation" };
        private static final String createHelperTableDeleteParms[] = { "relation" };
        private static final String createHelperTableMultipleDeleteParms[] = { "relation" };
        private static final String checkStatementParms[] = { "treeNodeManager" };
        private static final String setNativeModeIntParms[] = { "databaseVendorID" };
        private static final String setNativeModeStringParms[] = { "databaseVendorProductName" };
        private static final String isValidForOpenSQLParms[] = { "tableName" };

        /**
         * Creates a <code>CommonSQLMapper</code> instance. This
         * constructor should only be called by <code>SQLMapperFactory</code>.
         */
	public CommonSQLMapper() {
		this.properties = null;
                super.orMapping = null;
		this.orMapping = null;
                this.forNativeUseOnly = false;
                this.forNativeUseOnlyOverWritten = false;		
                this.returnCompleteBeans = false;
                this.returnCompleteBeansOverWritten = false;
                this.databaseVendor = null;
                this.databaseVendorId = SQLStatement.VENDOR_UNKNOWN;
                this.effectiveDatabaseVendorId = SQLStatement.VENDOR_UNKNOWN;
                this.databaseVendorOverWritten = false;
		this.packageName = SQLMapperFactory.class.getPackage().getName();
                this.verifySyntax = false;
                this.verifySemantics = false;
                this.catalogReader = null;
                this.openSQLVerifier = null;
		this.sqlCheckerImpl = null;
		this.sqlChecker = null;
                this.failsafe = false;
                this.sqlTreeNodeManagerInstance = new SQLTreeNodeManagerThreadLocal();
                this.ejbqlTreeProcessorInstance = new EJBQLTreeProcessorThreadLocal();
                this.ejbLoadStoreBuilderInstance = new EJBLoadStoreBuilderThreadLocal();
	}

        /*
         * Maps a given EJB-QL query to a database independent SQL representation.
         * Both <b>EJB 2.0</b> and <b>EJB 2.1</b> are supported.
         * <p></p>
         * @param query
         *              EJB-QL query to be mapped to SQL.
         * @return
         *              the <code>SQLMappingResult</code> for a given EJB-QL query.
         *              A <code>CommonSQLMappingResult</code> instance is created
         *              as implementation of the <code>SQLMappingResult</code>
         *              interface.
         * @throws SQLMappingException
         *              if EJB-QL query can not be mapped to an SQL statement.
         * @see com.sap.ejbql.tree.Query
         * @see CommonSQLMappingResult
         */
	public SQLMappingResult mapEjbQl(Query query) throws SQLMappingException 
        {
          DevTrace.debugInfo(loc, mapEjbQl, "defaulting makeDistinct to false.");

          return this.mapEjbQl(query, false);
        }


        /**
         * Maps a given EJB-QL query to a database independent SQL representation.
         * Both <b>EJB 2.0</b> and <b>EJB 2.1</b> are supported.
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
         * @param query
         *              EJB-QL query to be mapped to SQL.
         * @param makeDistinct
         *              if <code>true</code> add <code>DISTINCT</code> to
         *              <code>SELECT</code> clause.
         * @return
         *              the <code>SQLMappingResult</code> for a given EJB-QL query.
         *              A <code>CommonSQLMappingResult</code> instance is created
         *              as implementation of the <code>SQLMappingResult</code>
         *              interface.
         * @throws SQLMappingException
         *              if EJB-QL query can not be mapped to an SQL statement.
         * @see com.sap.ejbql.tree.Query
         * @see CommonSQLMappingResult
         */
        public SQLMappingResult mapEjbQl(Query query, boolean makeDistinct)
                throws SQLMappingException
        {
          DevTrace.debugInfo(loc, mapEjbQl, "defaulting haveForUpdate to false.");

          return this.mapEjbQl(query, makeDistinct, false);
        }
         
        /**
         * Maps a given EJB-QL query to a database independent SQL representation.
         * Both <b>EJB 2.0</b> and <b>EJB 2.1</b> are supported.
         * </p><p>
         * When <code>haveForUpdate</code> is set to <code>true</code> a <code>FOR UPDATE</code>
         * clause is added to <code>SELECT</code> clause of generated SQL representation if
         * a bean object is selected and the respective been type has been tagged for update
         * in the OR mapping;
         * when <code>haveForUpdate</code> is set to <code>false</code> this method is identical
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
         * @param query
         *              EJB-QL query to be mapped to SQL.
         * @param makeDistinct
         *              if <code>true</code> add <code>DISTINCT</code> to
         *              <code>SELECT</code> clause.
         * @param haveForUpdate
         *              if <code>true</code> add <code>FOR UPDATE</code> to
         *              <code>SELECT</code> clause if selected bean type is tagged so.
         * @return
         *              the <code>SQLMappingResult</code> for a given EJB-QL query.
         *              A <code>CommonSQLMappingResult</code> instance is created
         *              as implementation of the <code>SQLMappingResult</code>
         *              interface.
         * @throws SQLMappingException
         *              if EJB-QL query can not be mapped to an SQL statement.
         * @see com.sap.ejbql.tree.Query
         * @see CommonSQLMappingResult
         */
        public SQLMappingResult mapEjbQl(Query query, boolean makeDistinct,
                                                      boolean haveForUpdate)
        throws SQLMappingException
        {
          DevTrace.debugInfo(loc, mapEjbQl, "defaulting noEmptyTables to false.");

          return this.mapEjbQl(query, makeDistinct, haveForUpdate, false);
        }

        /**
         * Maps a given EJB-QL query to a database independent SQL representation.
         * Both <b>EJB 2.0</b> and <b>EJB 2.1</b> are supported.
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
         * @param query
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
         *              A <code>CommonSQLMappingResult</code> instance is created
         *              as implementation of the <code>SQLMappingResult</code>
         *              interface.
         * @throws SQLMappingException
         *              if EJB-QL query can not be mapped to an SQL statement.
         * @see com.sap.ejbql.tree.Query
         * @see CommonSQLMappingResult
         */
        public SQLMappingResult mapEjbQl(Query query, boolean makeDistinct,
                                                      boolean haveForUpdate, boolean noEmptyTables)
                throws SQLMappingException
        {
                if ( DevTrace.isOnDebugLevel(loc) )
                {
                  Object inputValues[] = { query, new Boolean(makeDistinct), new Boolean(haveForUpdate),
                                                  new Boolean(noEmptyTables) };
                  DevTrace.entering(loc, mapEjbQl, mapEjbQlParms, inputValues);
                }

                MappingAgents agents = this.setupProcessingAgents(EJBInterpreter.EJBQL);
                EJBQLTreeProcessor processor = (EJBQLTreeProcessor) agents.getEJBInterpreter();

		processor.process(query, makeDistinct, haveForUpdate, noEmptyTables);

                CommonSQLMappingResult result = this.setupProcessingResult(agents);

                DevTrace.exiting(loc, mapEjbQl, result);
                return result;
	}

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
                throws SQLMappingException
        {
          if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object inputValues[] = { abstractBeanName, new Boolean(haveForUpdate) };
            DevTrace.entering(loc, createEJBSelect, createEJBSelectParms, inputValues);
          }

          MappingAgents agents = this.setupProcessingAgents(EJBInterpreter.LOAD_STORE);
          EJBLoadStoreBuilder loadStoreBuilder = (EJBLoadStoreBuilder) agents.getEJBInterpreter();

          loadStoreBuilder.createBeanLoading(abstractBeanName, haveForUpdate);

          CommonSQLMappingResult result = this.setupProcessingResult(agents);

          DevTrace.exiting(loc, createEJBSelect, result);
          return result;
          
        }


        /**
         * Creates an SQL statement to check existence of an enterprise
         * java bean with given primary key. To be used by the ejb container.
         * </p><p>
         * Currently the existence check is implemented as a select statement
         * returning the bean' s primay key.
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
                throws SQLMappingException
        {
          if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object inputValues[] = { abstractBeanName };
            DevTrace.entering(loc, createEJBExists, createEJBExistsParms, inputValues);
          }

          MappingAgents agents = this.setupProcessingAgents(EJBInterpreter.EXISTS);
          EJBLoadStoreBuilder loadStoreBuilder = (EJBLoadStoreBuilder) agents.getEJBInterpreter();

          loadStoreBuilder.createBeanLoading(abstractBeanName, false);

          CommonSQLMappingResult result = this.setupProcessingResult(agents);

          DevTrace.exiting(loc, createEJBExists, result);
          return result;

        }

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
                throws SQLMappingException
        {
          if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object inputValues[] = { abstractBeanName };
            DevTrace.entering(loc, createEJBInsert, createEJBInsertParms, inputValues);
          }

          MappingAgents agents = this.setupProcessingAgents(EJBInterpreter.LOAD_STORE);
          EJBLoadStoreBuilder loadStoreBuilder = (EJBLoadStoreBuilder) agents.getEJBInterpreter();

          loadStoreBuilder.createBeanInsert(abstractBeanName);

          CommonSQLMappingResult result = this.setupProcessingResult(agents);

          DevTrace.exiting(loc, createEJBInsert, result);
          return result;

        }

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
                throws SQLMappingException
        {
          if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object inputValues[] = { abstractBeanName };
            DevTrace.entering(loc, createEJBUpdate, createEJBUpdateParms, inputValues);
          }

          MappingAgents agents = this.setupProcessingAgents(EJBInterpreter.LOAD_STORE);
          EJBLoadStoreBuilder loadStoreBuilder = (EJBLoadStoreBuilder) agents.getEJBInterpreter();

          loadStoreBuilder.createBeanUpdate(abstractBeanName);

          CommonSQLMappingResult result = this.setupProcessingResult(agents);

          DevTrace.exiting(loc, createEJBUpdate, result);
          return result;

        }

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
                throws SQLMappingException
        {
          if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object inputValues[] = { abstractBeanName };
            DevTrace.entering(loc, createEJBDelete, createEJBDeleteParms, inputValues);
          }

          MappingAgents agents = this.setupProcessingAgents(EJBInterpreter.LOAD_STORE);
          EJBLoadStoreBuilder loadStoreBuilder = (EJBLoadStoreBuilder) agents.getEJBInterpreter();

          loadStoreBuilder.createBeanRemoval(abstractBeanName);

          CommonSQLMappingResult result = this.setupProcessingResult(agents);

          DevTrace.exiting(loc, createEJBDelete, result);
          return result;

        }

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
        public SQLMappingResult createEJBIndirectSelect(CommonRelation relation, boolean haveForUpdate)
                throws SQLMappingException
        {
          if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object inputValues[] = { relation, new Boolean(haveForUpdate) };
            DevTrace.entering(loc, createEJBIndirectSelect, createEJBIndirectSelectParms, inputValues);
          }

          MappingAgents agents = this.setupProcessingAgents(EJBInterpreter.LOAD_STORE);
          EJBLoadStoreBuilder loadStoreBuilder = (EJBLoadStoreBuilder) agents.getEJBInterpreter();

		  String abstractBeanName = this.orMapping.getSourceBeanName(relation);
          String relatedBeanName = this.orMapping.getTargetBeanName(relation);
		  RelationField relationField = this.orMapping.getInverseField(relation);

          if ( this.orMapping.isMultipleValued(relation, true) )
          {
            DevTrace.exitingWithException(loc, createEJBIndirectSelect);
            String virtuality = relationField.isReal() ? "" : "virtual ";
            throw new SQLMappingException("Indirect select not allowed on M:1 or M:N relations.",
                                          "The CMR from abstract bean " + relatedBeanName 
                                          + " to abstract bean " + abstractBeanName 
                                          + " via " + virtuality + "bean field " + relationField.getName()
                                          + " is an M:1 or M:N relation from the point of view of abstract bean "
                                          + abstractBeanName + ". On such relations indirect selects are not allowed."
                                          + " This is an internal programming error of the EJB container."
                                          + " Please kindly open a problem ticket for SAP on component BC-JAS-EJB.",
                                          "CSM087");
          }

          loadStoreBuilder.createLoadingOfRelatedBeans(relatedBeanName, relationField,
                                                       abstractBeanName, haveForUpdate);

          CommonSQLMappingResult result = this.setupProcessingResult(agents);

          DevTrace.exiting(loc, createEJBIndirectSelect, result);
          return result;
        }

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
        public SQLMappingResult createHelperTableSelect(CommonRelation relation, boolean haveForUpdate)
            throws SQLMappingException
        {
       	  if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object inputValues[] = { relation, new Boolean(haveForUpdate) };
            DevTrace.entering(loc, createHelperTableSelect, createHelperTableSelectParms, inputValues);
          }

          MappingAgents agents = this.setupProcessingAgents(EJBInterpreter.LOAD_STORE);
          EJBLoadStoreBuilder loadStoreBuilder = (EJBLoadStoreBuilder) agents.getEJBInterpreter();

          HelperTableFacade helperTable;
          if ( this.orMapping.hasHelperTable(relation) )
          {
            helperTable = this.orMapping.getHelperTable(relation);
          }
          else
          {
            DevTrace.exitingWithException(loc, createHelperTableSelect);
            String relatedBeanName = this.orMapping.getTargetBeanName(relation);
            RelationField relationField = this.orMapping.getInverseField(relation);
            String abstractBeanName = this.orMapping.getSourceBeanName(relation);
            throw new SQLMappingException("Helper table select on relation without helper table.",
                                          "The CMR from abstract bean " + relatedBeanName
                                          + " to abstract bean " + abstractBeanName
                                          + " via bean field " + relationField.getName()
                                          + " does not have a helper table. Consequently,"
                                          + " helper table selects are not allowed on this relation."
                                          + " This apparently is an internal programming error of the EJB container."
                                          + " Please kindly open a problem ticket for SAP on component BC-JAS-EJB.",
                                          "CSM107");
          }

          loadStoreBuilder.createSelectHelperTableEntries(helperTable, haveForUpdate);

          CommonSQLMappingResult result = this.setupProcessingResult(agents);

          DevTrace.exiting(loc, createHelperTableSelect, result);
          return result;

        }


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
        public SQLMappingResult createHelperTableInsert(CommonRelation relation)
                throws SQLMappingException
        {
          if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object inputValues[] = { relation };
            DevTrace.entering(loc, createHelperTableInsert, createHelperTableInsertParms, inputValues);
          }

          MappingAgents agents = this.setupProcessingAgents(EJBInterpreter.LOAD_STORE);
          EJBLoadStoreBuilder loadStoreBuilder = (EJBLoadStoreBuilder) agents.getEJBInterpreter();

          HelperTableFacade helperTable;
          if ( this.orMapping.hasHelperTable(relation) )
          {
            helperTable = this.orMapping.getHelperTable(relation);
          }
          else
          {
            DevTrace.exitingWithException(loc, createHelperTableInsert);
            String[] beans = this.orMapping.getAbstractBeanNames(relation);
            RelationField relationField = this.orMapping.getRelationField(relation);
            throw new SQLMappingException("Helper table insert on relation without helper table.",
                                          "The CMR from abstract bean " + beans[0] 
                                          + " to abstract bean " + beans[1]
                                          + " via bean field " + relationField.getName()
                                          + " does not have a helper table. Consequently,"
                                          + " helper table inserts are not allowed on this relation."
                                          + " This apparently is an internal programming error of the EJB container."
                                          + " Please kindly open a problem ticket for SAP on component BC-JAS-EJB.",
                                          "CSM111");
          }

          loadStoreBuilder.createInsertHelperTableEntry(helperTable);

          CommonSQLMappingResult result = this.setupProcessingResult(agents);

          DevTrace.exiting(loc, createHelperTableInsert, result);
          return result;
        }

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
        public SQLMappingResult createHelperTableDelete(CommonRelation relation)
                throws SQLMappingException
        {
          if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object inputValues[] = { relation };
            DevTrace.entering(loc, createHelperTableDelete, createHelperTableDeleteParms, inputValues);
          }

          MappingAgents agents = this.setupProcessingAgents(EJBInterpreter.LOAD_STORE);
          EJBLoadStoreBuilder loadStoreBuilder = (EJBLoadStoreBuilder) agents.getEJBInterpreter();

          HelperTableFacade helperTable;
          if ( this.orMapping.hasHelperTable(relation) )
          {
            helperTable = this.orMapping.getHelperTable(relation);
          }
          else
          {
            DevTrace.exitingWithException(loc, createHelperTableDelete);
            String[] beans = this.orMapping.getAbstractBeanNames(relation);
            RelationField relationField = this.orMapping.getRelationField(relation);
            throw new SQLMappingException("Helper table delete on relation without helper table.",
                                          "The CMR from abstract bean " + beans[0]
                                          + " to abstract bean " + beans[1]
                                          + " via bean field " + relationField.getName()
                                          + " does not have a helper table. Consequently,"
                                          + " helper table deletes are not allowed on this relation."
                                          + " This apparently is an internal programming error of the EJB container."
                                          + " Please kindly open a problem ticket for SAP on component BC-JAS-EJB.",
                                          "CSM113");
          }

          loadStoreBuilder.createDeleteHelperTableEntry(helperTable);

          CommonSQLMappingResult result = this.setupProcessingResult(agents);

          DevTrace.exiting(loc, createHelperTableDelete, result);
          return result;
        }

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
        public SQLMappingResult createHelperTableMultipleDelete(CommonRelation relation)
                throws SQLMappingException
        {
          if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object inputValues[] = { relation };
            DevTrace.entering(loc, createHelperTableMultipleDelete, createHelperTableMultipleDeleteParms, inputValues);
          }

          MappingAgents agents = this.setupProcessingAgents(EJBInterpreter.LOAD_STORE);
          EJBLoadStoreBuilder loadStoreBuilder = (EJBLoadStoreBuilder) agents.getEJBInterpreter();

          HelperTableFacade helperTable;
          if ( this.orMapping.hasHelperTable(relation) )
          {
            helperTable = this.orMapping.getHelperTable(relation);
          }
          else
          {
            DevTrace.exitingWithException(loc, createHelperTableMultipleDelete);
            String relatedBeanName = this.orMapping.getTargetBeanName(relation);
            RelationField relationField = this.orMapping.getInverseField(relation);
			String abstractBeanName = this.orMapping.getSourceBeanName(relation);
            throw new SQLMappingException("Helper table delete on relation without helper table.",
                                          "The CMR from abstract bean " + relatedBeanName
                                          + " to abstract bean " + abstractBeanName
                                          + " via bean field " + relationField.getName()
                                          + " does not have a helper table. Consequently,"
                                          + " helper table deletes are not allowed on this relation."
                                          + " This apparently is an internal programming error of the EJB container."
                                          + " Please kindly open a problem ticket for SAP on component BC-JAS-EJB.",
                                          "CSM115");
          }

          loadStoreBuilder.createDeleteHelperTableEntries(helperTable);

          CommonSQLMappingResult result = this.setupProcessingResult(agents);

          DevTrace.exiting(loc, createHelperTableMultipleDelete, result);
          return result;
        }

        /**
         * Ensures that EJB-QL queries with an identification variable as select
         * clause will be mapped to SQL statements that return the primary
         * key fields of the respective entity bean's database representation only.
         * Eventually differing property <code>com.sap.ejb.ql.sqlmapper.returnCompleteBeans</code>
         * set at creation of <code>CommonSQLMapper</code> instance is permanently
         * overwritten by this method.
         * </p><p>
         * Though this method is synchronized in order not to interfere with
         * initialization phase of method <code>mapEjbQl()</code>, handle with care
         * in case multiple threads share same <code>CommonSQLMapper</code> instance&nbsp;!
         **/
        public synchronized void setSelectPrimaryKeyFields()
        {
          this.returnCompleteBeansOverWritten = true;
          this.returnCompleteBeans = false;
          DevTrace.debugInfo(loc, setSelectPrimaryKeyFields, "returning complete beans unset.");
          return;
        }

        /**
         * Ensures that EJB-QL queries with an identification variable as select
         * clause will be mapped to SQL statements that return the complete database
         * representation of the bean.
         * Eventually differing property <code>com.sap.ejb.ql.sqlmapper.returnCompleteBeans</code>
         * set at creation of <code>CommonSQLMapper</code> instance is permanently
         * overwritten by this method.
         * </p><p>
         * <b>Note</b>, however, that this setting might be temporarily ignored,
         * e.g. in the case the <code>CommonSQLMapper</code> encounters so-called incomparable
         * jdbc types. For more details on this issue, please, refer to documentation
         * of method <code>wasSelectPrimaryKeyFieldsForced()</code> of class
         * <code>CommonSQLMappingResult</code>.
         * </p><p>
         * Though this method is synchronized in order not to interfere with
         * initialization phase of method <code>mapEjbQl()</code>, handle with care
         * in case multiple threads share same <code>CommonSQLMapper</code> instance&nbsp;!
         **/
        public synchronized void setSelectAllBeanFields()
        {
          this.returnCompleteBeansOverWritten = true;
          this.returnCompleteBeans = true;
          DevTrace.debugInfo(loc, setSelectAllBeanFields, "returning complete beans set.");
          return;
        }

        /**
         * Indicates that resulting SQL statements are to be used for Native SQL only.
         * </p><p>
         * In this mode, table name identifiers are incorporated into generated SQL statements
         * as are, i.e. without parsing, composition/decomposition or using quotes.
         * </p><p>
         * <B>Hint</B>&nbsp;: Handle with care in case multiple threads share
         * same <code>CommonSQLMapper</code> instance&nbsp;!
         **/
        public synchronized void setNativeMode()
        {
                this.forNativeUseOnlyOverWritten = true;
                this.forNativeUseOnly = true;
                        DevTrace.debugInfo(loc, setNativeMode, "native mode set.");
        }

        /**
         * Indicates that resulting SQL statements are to be used for Native SQL only.
         * </p><p>
         * In this mode, table name identifiers are incorporated into generated SQL statements
         * as are, i.e. without parsing, composition/decomposition or using quotes.
         * </p><p>
         * Additionally specifies the default database vendor for the <code>CommonSQLMappingResults</code>
         * that will be created by this mapper instance. This method overwrites the
         * database vendor settings of the OR mapping schema and of runtime property
         * <code>databaseVendor</code>.
         * </p><p>
         * You should only use SAP supported database platforms as database vendor, otherwise
         * vendor may be considered to be unknown.
         * </p><p>
         * <B>Hint</B>&nbsp;: Handle with care in case multiple threads share
         * same <code>CommonSQLMapper</code> instance&nbsp;!
         * <p></p>
         * @param databaseVendorID
         *              Integer representation of the database vendor as defined by class
         *               <code>NativeSQLAccess</code>.
         * @throws SQLMappingException
         *              if the database platform is not supported (optional).
         * @see com.sap.sql.NativeSQLAccess
         **/
        public synchronized void setNativeMode(int databaseVendorID)
        throws SQLMappingException
        {
                if ( DevTrace.isOnDebugLevel(loc) )
                {
                  Object inputValues[] = { new Integer(databaseVendorID) };
                  DevTrace.entering(loc, setNativeMode, setNativeModeIntParms,
                                                        inputValues);
                }

                this.forNativeUseOnlyOverWritten = true;
                this.forNativeUseOnly = true;
                this.databaseVendorOverWritten = true;
                this.effectiveDatabaseVendorId = databaseVendorID;

                DevTrace.exiting(loc, setNativeMode);
        }

        /**
         * Indicates that resulting SQL statements are to be used for Native SQL only.
         * </p><p>
         * In this mode, table name identifiers are incorporated into generated SQL statements
         * as are, i.e. without parsing, composition/decomposition or using quotes.
         * </p><p>
         * Additionally specifies the default database vendor for the <code>CommonSQLMappingResults</code>
         * that will be created by this mapper instance. This method overwrites the
         * database vendor settings of the OR mapping schema and of runtime property
         * <code>databaseVendor</code>.
         * </p><p>
         * You should only use SAP supported database platforms as database vendor, otherwise
         * vendor may be considered to be unknown.
         * <p></p>
         * <B>Hint</B>&nbsp;: Handle with care in case multiple threads share
         * same <code>CommonSQLMapper</code> instance&nbsp;!
         * <p></p>
         * @param databaseVendorProductName
         *              Database vendor product name as reckognized by class
         *               <code>NativeSQLAccess</code>.
         * @throws SQLMappingException
         *              if the database platform is not supported (optional).
         * @see com.sap.sql.NativeSQLAccess
         **/
        public synchronized void setNativeMode(String databaseVendorProductName)
        throws SQLMappingException
        {
                if ( DevTrace.isOnDebugLevel(loc) )
                {
                  Object inputValues[] = { databaseVendorProductName };
                  DevTrace.entering(loc, setNativeMode, setNativeModeStringParms,
                                                        inputValues);
                }

                this.forNativeUseOnlyOverWritten = true;
                this.forNativeUseOnly = true;
                this.databaseVendorOverWritten = true;
                this.effectiveDatabaseVendorId =
                          NativeSQLAccess.getVendorID(databaseVendorProductName);

                DevTrace.exiting(loc, setNativeMode);
        }

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
        public boolean isValidForOpenSQL(String tableName)
        {
          if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object inputValues[] = { tableName };
            DevTrace.entering(loc, isValidForOpenSQL, isValidForOpenSQLParms, inputValues);
          }

          boolean isValid = ORMappingManager.isOpenSQLTableReference(tableName);

          DevTrace.exiting(loc, isValidForOpenSQL, isValid); 
          return isValid;
        }

        /**
         * Initializes the <code>CommonSQLMapper</code> object.
         * Creates an <code>ORMappingManager</code> and evalautes the various
         * properties that may influence the <code>CommonSQLMapper</code>.
         */
        private void initialize() throws SQLMappingException
        {
          DevTrace.entering(loc, initialize, null, null);

          String property;

          // create OR Mapping Manager
          this.orMapping = ORMappingManagerFactory.createORMappingManager(super.orMapping);

          // check whether setNativeMopde() was called prior to initialize()
          if ( this.forNativeUseOnlyOverWritten )
          {
            DevTrace.debugInfo(loc, initialize,
                               "ignoring eventually set property nativeMode.");
          }
          else
          {
            property = this.properties.getProperty(this.packageName + ".nativeMode");
            if ( (property != null) && property.equalsIgnoreCase("true") )
            {
              this.forNativeUseOnly = true;
              DevTrace.debugInfo(loc, initialize, "creating statements for native use only as per property.");
            }
          }
          
		  // check whether setSelect...() was called prior to initialize()
		  if ( this.returnCompleteBeansOverWritten )
		  {
			DevTrace.debugInfo(loc, initialize,
							   "ignoring eventually set property returnCompleteBeans.");
		  }
		  else
		  {
			property = this.properties.getProperty(this.packageName + ".returnCompleteBeans");
			if ( (property != null) && property.equalsIgnoreCase("true") )
			{
			  this.returnCompleteBeans = true;
			  DevTrace.debugInfo(loc, initialize, "returning complete beans as per property.");
			}
		  }

          //  check whether setNativeMode() was called with database vendor as parameter prior to initialize
          if ( this.databaseVendorOverWritten )
          {
            DevTrace.debugInfo(loc, initialize, "ignoring eventually set property database Vendor as well as database vendor setting of the OR Mapping");

          }
          else
          { 
            if ( this.orMapping.providesDBVendor() )
            {
              this.databaseVendorId = this.orMapping.getDBVendorId();
              DevTrace.debugInfo(loc, initialize,
                               "database vendor id set to " + this.databaseVendorId);
            }

            property = this.properties.getProperty(this.packageName + ".databaseVendor");
            if ( (property != null) )
            {
              this.databaseVendor = property;
              DevTrace.debugInfo(loc, initialize,
                                 "database vendor overwritten to " + property);
            }
            this.effectiveDatabaseVendorId = ( this.databaseVendor == null )
                                                 ? this.databaseVendorId
                                                 : NativeSQLAccess.getVendorID(this.databaseVendor);
            DevTrace.debugInfo(loc, initialize, "effective database vendor id is " + this.effectiveDatabaseVendorId);
          }

          property = this.properties.getProperty(this.packageName + ".OpenSQLVerification");
          if ( property != null )
          {
            if ( property.equalsIgnoreCase("syntax") || property.equalsIgnoreCase("semantics") )
            {
              this.openSQLVerifier = OpenSQLServices.createStatementAnalyzer();
              if ( this.openSQLVerifier == null )
              {
                DevTrace.exitingWithException(loc, initialize);
                throw new SQLMappingException("Could not create StatementAnalyzer.",
                                              "Property " + this.packageName
                                              + ".OpenSQLVerification has been set to "
                                              + property + ". This triggers creation of a "
                                              + "so called StatementAnalyzer. For some unknown reason, "
                                              + "however, that StatementAnalyzer could not be created. "
                                              + "Please kindly open a problem ticket for SAP on component "
                                              + "BC-JAS-PER-DBI. As temporary workaround you may set "
                                              + "property " + this.packageName
                                              + ".OpenSQLVerification to \"none\" in a file named "
                                              + SQLMapperFactory.getLocalFileName()
                                              + ", which you place in the classpath or working directory "
                                              + "of your server.",
                                              "CSM079");
              }
              this.verifySyntax = true;
              if ( property.equalsIgnoreCase("semantics") )
              {
                DevTrace.debugInfo(loc, initialize,
                                   "OpenSQL verification set to semantics.");
                this.catalogReader = new ORMapCatalogReader(this.orMapping);
                this.verifySemantics = true;
              }
              else
              {
                DevTrace.debugInfo(loc, initialize,
                                   "OpenSQL verification set to syntax.");
              }
            }
          }

          property = this.properties.getProperty(this.packageName + ".failsafe");
          this.failsafe = ( (property != null) && property.equalsIgnoreCase("yes") );
          if ( this.failsafe )
          {
            DevTrace.debugInfo(loc, initialize, "switching to failsafe mode.");
          }
            
          DevTrace.exiting(loc, initialize);
          return;
        }

        /**
         * Checks an SQL statement created by the <code>SQLTreeNodeManager</code> for Open SQL
         * compatibility. Depending on the <code>CommonSQLMapper</code>'s configuration
         * syntax and/or semantics of the SQL statement are checked with the Open SQL parser.
         * Optionally an additional test is performed using the dynamic <code>SQLChecker</code>
         * interface. While the result of the checks with the Open SQL parser are reported 
         * via the boolean return value, the outcome of an eventual <code>SQLChecker</code>
         * test is communicated through an <code>SQLMappingException</code> (if negativ).
         * </p><p>
         * @param treeNodeManager
         *      the <code>SQLTreeNodeManager</code> housing the newly created SQL statement.
         * @return
         *      <code>true</code> if SQL statement is native, i.e., not Open SQL compliant;<br>
         *      <code>false</code> elsewise.
         * @throws SQLMappingException
         *      if test with <code>SQLChecker</code> reports an exception or <code>SQLChecker</code>
         *      cannot be loaded.
         * @see com.sap.ejb.ql.sqlmapper.general.SQLChecker
         */     
        private boolean checkStatement(SQLTreeNodeManager treeNodeManager)
        throws SQLMappingException
        {
          if ( DevTrace.isOnDebugLevel(loc) )
          {
            Object inputValues[] = { treeNodeManager };
            DevTrace.entering(loc, checkStatement, checkStatementParms, inputValues);
          }

          boolean isNativeSQL = false;
          if ( this.verifySyntax && (! treeNodeManager.isForNativeUseOnly()) )
          {
            try
            {
              DevTrace.debugInfo(loc, checkStatement,
                                 "parsing statement syntax.");
              SQLStatement parsedTree
                 = this.openSQLVerifier.parseSyntax(treeNodeManager.getSQLStatement().toSqlString(SQLStatement.VENDOR_OPENSQL));
              if ( this.verifySemantics )
              {
                DevTrace.debugInfo(loc, checkStatement,
                                   "checking statement syntax.");
                this.openSQLVerifier.checkSemantics(parsedTree, this.catalogReader);
              }
            }
            catch( SQLException sqlE)
            {
              // $JL-EXC$ as JLin is not satisfied with evaluating
              // the exception's message only below
              // see internal CSS 0120031469 0000684920 2004

              DevTrace.debugInfo(loc, checkStatement, "SQLException: " + sqlE.getMessage());
              DevTrace.debugInfo(loc, checkStatement, "SQL Statement considered native.");
              isNativeSQL = true;
            }
            catch (StringRepresentationFailureException srfe)
            {
              // $JL-EXC$ as JLin is not satisfied with evaluating
              // the exception's message only below
              // see internal CSS 0120031469 0000684920 2004

              DevTrace.debugInfo(loc, checkStatement, "StringRepresentationFailure: " + srfe.getMessage());
              DevTrace.debugInfo(loc, checkStatement, "SQL Statement considered native.");
              isNativeSQL = true;
            }
          }

          String property = this.properties.getProperty(this.packageName + ".checkResult");
          if (    (property != null)
               && property.equalsIgnoreCase("true")
             )
          {
            DevTrace.debugInfo(loc, checkStatement, "validating statement.");
            if (this.sqlChecker == null) 
            {
              this.sqlCheckerImpl =
                           this.properties.getProperty(
                                     this.packageName + ".SQLChecker");

              if (this.sqlCheckerImpl == null) 
              {
                                  DevTrace.exitingWithException(loc, checkStatement);
                                        throw new SQLMappingException(
                                                "No SQLCheckerimplementation defined.",
                                                "Property "
                                                        + this.packageName
                                                        + "SQLChecker"
                                                        + "has not been defined within "
                                                        + "properties files, though property "
                                                        + this.packageName
                                                        + "checkResult has been set to "
                                                        + "true. If you switch on SQL checking you also "
                                                        + "have to specify an SQL checker implementation.",
                                                "CSM001");
              }

              try {
                    this.sqlChecker =
                                      (SQLChecker) SQLMapperFactory
                                                        .class
                                                        .getClassLoader()
                                                        .loadClass(this.sqlCheckerImpl)
                                                        .newInstance();
                    this.sqlChecker.setProperties(this.properties);
                                } catch (Exception ex) {
                                        DevTrace.exitingWithException(loc, checkStatement);
                                        throw new SQLMappingException(
                                                "SQL Checker Implementation "
                                                        + this.sqlCheckerImpl
                                                        + "could not be loaded.",
                                                "Loading of "
                                                        + this.sqlCheckerImpl
                                                        + " has failed."
                                                        + "Please check settings for properties "
                                                        + this.packageName
                                                        + "SQLChecker and "
                                                        + this.packageName
                                                        + "checkResult in your property files. You might also "
                                                        + "want to check the properties required by the SQL checker "
                                                        + " implementation. See further attached exception.",
                                                "CSM003",
                                                ex);
                                }

                        DevTrace.debugInfo(loc, checkStatement,
                                           "sql checker implementation " + this.sqlCheckerImpl
                                           + " loaded.");

                        }

                        try {
                                this.sqlChecker.checkSQLStatement(
                                        treeNodeManager.getSQLStatement());
                        } catch (Exception ex) {
                                DevTrace.exitingWithException(loc, checkStatement);
                                throw new SQLMappingException(
                                        "Validation of generated SQL statement failed.",
                                        "The generated SQL statement could not be validated."
                                                + "This is most likely a programming error in either the "
                                                + "EJB QL parser or in SQL mapper implementation "
                                                + CommonSQLMapper.class.getPackage().getName()
                                                + ".CommonSQLMapper. Less likely, it "
                                                + "could also be a problem with SQL checker implementation "
                                                + this.sqlCheckerImpl
                                                + ". Please carefully check attached "
                                                + "exception.",
                                        "CSM005",
                                        ex);
                        }

                }

          DevTrace.exiting(loc, checkStatement, isNativeSQL);
          return isNativeSQL;
        }

        /**
         * Initializes the <code>CommonSQLMapper</code> if necessary and
         * provides and prepares the set of objects needed for processing an
         * EJB-QL mapping or EJB load/store creation request.
         * </p><p>
         * @param processingMode
         *      indicating whether an EJB-QL mapping request or an EJB
         *      load/store request is to be prepared - takes one of the
         *      constants defined in class
         *      {@link com.sap.ejb.ql.sqlmapper.common.EJBInterpreter}
         *      as possible values.
         * @return
         *      set of objects needed for processing an
         *      EJB-QL mapping or EJB load/store creation request.
         * @throws SQLMappingException
         *      in case an illegal <code>processingMode</code> was provided.
         */
        private MappingAgents setupProcessingAgents(int processingMode)
        throws SQLMappingException
        {
          boolean returningCompleteBeans;
          boolean forNativeUseOnly;
          int effectiveDatabaseVendorId;

          synchronized(this)
          {
            if ( this.orMapping == null )
            {
              // first call, initialize
              this.initialize();
            }

            forNativeUseOnly = this.forNativeUseOnly;
            effectiveDatabaseVendorId = this.effectiveDatabaseVendorId;
            if (    (processingMode == EJBInterpreter.EJBQL)
                 || (processingMode == EJBInterpreter.EXISTS)
               )
            {
              returningCompleteBeans = this.returnCompleteBeans;
            }
            else
            {
              returningCompleteBeans = true;
            }
          }

          SQLTreeNodeManager treeNodeManager = this.sqlTreeNodeManagerInstance.get();
          treeNodeManager.prepare(this.orMapping, returningCompleteBeans,
                                  forNativeUseOnly, this.failsafe);
          EJBInterpreter ejbInterpreter;
          switch ( processingMode )
          {
            case EJBInterpreter.LOAD_STORE:
            case EJBInterpreter.EXISTS:
              ejbInterpreter = this.ejbLoadStoreBuilderInstance.get();
              break;

            case EJBInterpreter.EJBQL:
              ejbInterpreter = this.ejbqlTreeProcessorInstance.get();
              break;

            default:
              throw new SQLMappingException("Unknown processing type " + processingMode + " encountered.",
                                            "When preparing for SQL statement generation, the SQL Mapper has"
                                            + " encountered an unsupported processing mode (" + processingMode
                                            + "). This is an internal programming error of the SQL Mapper."
                                            + " Please kindly open a problem ticket for SAP on component"
                                            + " BC-JAS-PER-DBI.",
                                            "CSM117");
          }
          
          ejbInterpreter.prepare(treeNodeManager);
          
          return new MappingAgents(effectiveDatabaseVendorId, treeNodeManager, ejbInterpreter);
       }

       /**
        * Creates a <code>CommonSQLMappingResult</code> to finish up mapping resp. statement
        * creation process. Additionally, objects involved in this process are cleared to
        * become ready for further processing.
        * @param agents
        *      set of objects needed for processing an
        *      EJB-QL mapping or EJB load/store creation request.
        * @return
        *      result of the mapping resp. statement creation process.
        * @throws SQLMappingException
        *      when {@link #checkStatement(SQLTreeNodeManager)} creates such.
        */
       private CommonSQLMappingResult setupProcessingResult(MappingAgents agents)
       throws SQLMappingException
       {
          int databaseVendorID = agents.getDatabaseVendor();
          SQLTreeNodeManager treeNodeManager = agents.getSQLTreeNodeManager();

          boolean isNativeSQL = this.checkStatement(treeNodeManager);

          CommonSQLMappingResult result = new CommonSQLMappingResult(
                                                   treeNodeManager.getSQLStatement(),
                                                   treeNodeManager.getInputDescriptors(),
                                                   treeNodeManager.getResultDescriptors(),
                                                   databaseVendorID,
                                                   isNativeSQL,
                                                   treeNodeManager.isForNativeUseOnly(),
                                                   treeNodeManager.wasReturnCompleteBeansIgnored(),
                                                   treeNodeManager.isSQLStatementCrossJoinOptimised());

          treeNodeManager.clear();
          agents.getEJBInterpreter().clear();

          return result;
       }
}
