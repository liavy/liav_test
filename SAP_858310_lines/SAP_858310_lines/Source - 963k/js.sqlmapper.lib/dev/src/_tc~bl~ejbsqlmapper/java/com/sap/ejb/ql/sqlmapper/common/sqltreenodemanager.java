package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;

import com.sap.ejb.ql.sqlmapper.common.BooleanNodeManager;
import com.sap.ejb.ql.sqlmapper.common.ExpressionNodeManager;
import com.sap.ejb.ql.sqlmapper.common.WhereNodeManager;
import com.sap.ejb.ql.sqlmapper.common.SubqueryBuilder;
import com.sap.ejb.ql.sqlmapper.common.InputParameter;
import com.sap.ejb.ql.sqlmapper.common.InputParameterDefinition;
import com.sap.ejb.ql.sqlmapper.common.InputParameterOccurrence;
import com.sap.ejb.ql.sqlmapper.common.CommonInputDescriptor;
import com.sap.ejb.ql.sqlmapper.common.CMRFieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.CMPFieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.DVFieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.M2MDescriptor;
import com.sap.ejb.ql.sqlmapper.common.HelperTableFacade;
import com.sap.ejb.ql.sqlmapper.common.BeanTable;
import com.sap.ejb.ql.sqlmapper.common.TableRepresentation;
import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;

import com.sap.sql.tree.ColumnReference;
import com.sap.sql.tree.ComparisonOperator;
import com.sap.sql.tree.GeneralSelectStatement;
import com.sap.sql.tree.NeutralCondition;
import com.sap.sql.tree.Query;
import com.sap.sql.tree.QuerySpecification;
import com.sap.sql.tree.RowValue;
import com.sap.sql.tree.RowValueElement;
import com.sap.sql.tree.RowValueElementList;
import com.sap.sql.tree.SQLStatement;
import com.sap.sql.tree.SearchCondition;
import com.sap.sql.tree.SelectSublist;
import com.sap.sql.tree.SetClause;
import com.sap.sql.tree.SetFunctionType;
import com.sap.sql.tree.SortSpecification;
import com.sap.sql.tree.SqlTreeBuilder;
import com.sap.sql.tree.TableAlias;
import com.sap.sql.tree.TableReference;
import com.sap.sql.tree.ValueExpression;
import com.sap.sql.tree.ejb21_ColumnReference;
import com.sap.sql.tree.ejb21_SortSpecification;
import com.sap.sql.tree.ejb21_TableReference;
import com.sap.sql.types.CommonTypes;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The <code>SQLTreeNodeManager</code> assembles an <code>SQLStatement</code>,
 * that represents either the EJB-QL query's translation into SQL or an
 * ejb load/store action. An array of <code>CommonInputDescriptor</code>s
 * and an array of <code>CommonResultDescriptor</code>s are created for the
 * assembled <code>SQLStatement</code>. These three objects will be retrieved
 * by the <code>CommonSQLMapper</code> and will be packed into a
 * <code>CommonSQLMappingResult</code>. 
 *
 * <p></p>
 * The methods of this class are invoked by the <code>process()</code>
 * methods of the <code>EJBQLTreeProcessor</code> or by the create methods of
 * the <code>EJBLoadStoreBuilder</code>.
 * 
 * <p></p> 
 * An initial created <code>SQLTreeNodeManager</code> instance has to be
 * fully initialized for processing by the call of method <code>prepare()</code> with a given 
 * <code>ORMappingManager</code> object. The <code>SQLTreeNodeManager</code> itself creates a
 * <code>SubqueryBuilder</code>, an <code>ExpressionNodeManager</code>, a
 * <code>WhereNodeManager</code> and a <code>BooleanNodeManager</code> for sub-processing
 * within the <code>prepare()</code> method if these managers do not already exists.
 * The <code>prepare()</code> methods of these underlying managers are called as well.
 * The managers are invoked from the <code>EJBQLTreeProcessor</code>'s sub-processors
 * and from the <code>EJBLoadStoreBuilder</code>.
 * The <code>SubqueryBuilder</code> is only invoked from the <code>WhereNodeManager</code>.
 * 
 * <p></p>
 * Each processing of a query (via <code>EJBQLTreePocessor</code>) and each processing
 * of a load or store action (via <code>EJBLoadStoreBuilder</code>) has to start with
 * an invocation of the <code>prepare()</code> method of this <code>SQLTreeNodeManager</code>
 * in order to set the flag useFullBeanDescriptors representing property
 * com.sap.ejb.ql.sqlmapper.returnCompleteBeans,
 * to set the flag forNativeUseOnly and to set a reference to an <code>SQLTextHelper</code> object.
 * If the flag useFullBeanDescriptors is set than
 * an <code>SQLStatement</code> is assembled where all existing database columns
 * of a table to which a bean is mapped are selected; even those columns which 
 * do not correspond to any field of the mapped bean. If useFullBeanDescriptors is not set,
 * than only the primary key fields are selected.
 * 
 * <p></p>
 * Each processing has to be finished by
 * invocation of the <code>clear()</code> method of this <code>SQLTreeNodeManager</code>
 * to reset this <code>SQLTreeNodeManager</code> instance for a
 * subsequent prepare and processing of a query, load or store action.
 * The clear methods of the underlying <code>SubqueryBuilder</code>, 
 * <code>WhereNodeManager</code>, <code>ExpressionNodeManager</code>
 * and <code>BooleanNodeManager</code> of this <code>SQLTreeNodeManager</code>
 * are called from the <code>SQLTreeNodeManager</code>'s clear method.
 *
 * <p></p> 
 * For the assembling process of an sql query statement
 * following sequence of method calls has to be obeyed:
 * <ul>
 *  <li><code>announceIdentifiers()</code> to assure that no generated
 *      alias name for a table is equal to an identifier existing in the EJBQL query.
 *  <li><code>createQuerySpecification()</code> to indicate, that the
 *      assembling of the <code>SQLStatement</code> starts and to transfer the
 *      information, if the query selects distinct or not distinct values.
 *  <li><code>setSelectList()</code> to specify the select list columns, that could be
 *      <br><br>
 *      <ul>
 *       <li>the primary key columns or all columns of the bean
 *           depending on the property com.sap.ejb.ql.sqlmapper.returnCompleteBeans.
 *       <li>a count(*)
 *       <li>one cmp field column
 *       <li>the columns of a dependent value field
 *       <li>the primary key columns or all columns of a bean a cmr field is pointing to
 *           depending on the property com.sap.ejb.ql.sqlmapper.returnCompleteBeans
 *       <li>an aggregated cmp field column
 *       <li>a count(*) for a bean a cmr field is pointing to
 *      </ul>
 *      <br>
 *  <li><code>addToFromList()</code> to add tables to the from
 *      clause of the <code>SQLStatement</code> (can be called multiple).
 *  <li><code>setFromClause()</code> to finalise the from clause assembling.
 *  <li><code>setWhereClause()</code> to pick up the where clause from the 
 *      <code>whereNodeManager()</code> that has assembled the where clause and that
 *      method's are invoked from the <code>EJBQLTreeProcessor</code>'s sub-processors.
 *  <li><code>addToOrderByList()</code> to add columns to
 *      the order by list of the <code>SQLStatement</code>  (can be called multiple).
 *  <li><code>setOrderByClause()</code> to finalise the order by clause assembling
 *  <li><code>endOfStatement()</code> to finalise the
 *      <code>SQLStatement</code> assembling at all.
 * </ul>
 * 
 * <p></p>
 * During the processing of the <code>setSelectList()</code> methods 
 * <code>CommonResultDescriptor</code> objects are created and stored in an array,
 * which is returned by method <code>getResultDescriptors()</code>.
 * 
 * <p></p>
 * Method <code>registerInputParameter()</code> can be called at any time to register
 * an <code>InputParameterDefinition</code>. <code>CommonInputDescriptor</code> objects
 * are created and stored in an array, which is returned by method
 * <code>getInputDescriptors()</code>.
 * 
 * <p></p>
 * The flag forNativeUseOnly retrievable from the <code>CommonSQLMappingResult</code>
 * with method <code>forNativeUseOnly()</code> will be set in the following cases:
 * <ul>
 *  <li>Parameter forNativeUseOnly is <code>true</code> when method <code>prepare()</code>
 *      is called. In this case, all <code>ejb21_TableReference<code>s will be created as
 *      native <code>com.sap.sql.tree.ejb21_TableReference<code>s (parameter
 *      isNative will be true when the <code>ejb21_TableReference<code>'s constructor is called).
 *  <li>Parameter forUpdate is <code>true</code> when method <code>createQuerySpecification()<code>
 *      is invoked.
 *  <li>At least one native <code>ejb21_TableReference<code> is created when a
 *      table name is given, which is not OpenSQL compliant (each given table name will be checked with
 *      method <code>isOpenSQLTableReference()</code> of <code>ORMappingManager</code> when forNativeUseOnly is not already set
 *      in methode <code>prepare()</code>.
 * </ul>
 * 
 * <p></p>
 * 
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 * 
 * @see com.sap.sql.tree.SQLStatement
 * @see com.sap.ejb.ql.sqlmapper.common.CommonInputDescriptor
 * @see com.sap.ejb.ql.sqlmapper.common.CommonResultDescriptor
 * @see com.sap.ejb.ql.sqlmapper.common.CommonSQLMapper
 * @see com.sap.ejb.ql.sqlmapper.common.CommonSQLMappingResult
 * @see com.sap.ejb.ql.sqlmapper.common.EJBQLTreeProcessor
 * @see com.sap.ejb.ql.sqlmapper.common.ORMappingManager
 * @see com.sap.ejb.ql.sqlmapper.common.WhereNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.ExpressionNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.BooleanNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.WhereNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.SubqueryBuilder
 * @see com.sap.sql.tree.ejb21_TableReference
 */

public class SQLTreeNodeManager {

	private static final Location loc =
		Location.getLocation(SQLTreeNodeManager.class);
	private static final String announceIdentifiers = "announceIdentifiers";
	private static final String announceIdentifiersParms[] = { "identifier" };
	private static final String prepare = "prepare";
	private static final String clear = "clear";
	private static final String prepareParms[] =
		{
			"orMapping",
			"useFullBeanDescriptors",
			"forNativeUseOnly",
			"failsafe" };
	private static final String createQuerySpecification =
		"createQuerySpecification";
	private static final String createQuerySpecificationParms[] =
		{ "distinct", "forUpdate", "noEmptyTables" };
	private static final String createUpdateStatement = "createUpdateStatement";
	private static final String createInsertStatement = "createInsertStatement";
	private static final String createDeleteStatement = "createDeleteStatement";

	private static final String setSelectList = "setSelectList";
	private static final String setSelectListBeanParms[] = { "beanObject" };
	private static final String setSelectListHelperTableParms[] =
		{ "helperTable" };
	private static final String setSelectListTypeAndBeanParms[] =
		{ "functionType", "beanObject" };
	private static final String setSelectListBeanFieldAndRelation[] =
		{ "beanField", "relation" };
	private static final String setSelectListFunctionTypeBeanFieldRelation[] =
		{ "functionType", "beanField", "relation" };
	private static final String addToFromList = "addToFromList";
	private static final String addToFromListHelperTableParms[] =
		{ "helperTable" };
	private static final String addToTableList = "addToTableList";
	private static final String addToTableListBeanParms[] = { "beanObject" };
	private static final String addToTableListHelperTableParms[] =
		{ "helperTable" };
	private static final String addToTableListTableAndAliasParms[] =
		{ "beanTable", "aliasName" };
	private static final String addToTableListIdentifierBeanFieldRelationParms[] =
		{ "identifier", "beanField", "relation" };
	private static final String setFromClause = "setFromClause";
	private static final String setWhereClause = "setWhereClause";
	private static final String addToOrderByList = "addToOrderByList";
	private static final String addToOrderByListParms[] =
		{ "isDescending", "beanField", "relation" };
	private static final String addToSetList = "addToSetList";
	private static final String addToSetListParms[] =
		{ "columns", "expressions" };
	private static final String addToValueList = "addToValueList";
	private static final String addToValueListParms[] =
		{ "columns", "expressions" };
	private static final String endOfStatement = "endOfStatement";
	private static final String createInputDescriptorRepresentingBean =
		"registerInputParameterRepresentingBean";
	private static final String registerInputParameter =
		"registerInputParameter";
	private static final String registerInputParameterParms[] =
		{ "parameterDefinition" };
	private static final String registerInputParameterParms2[] =
		{ "parameterDefinition", "representing" };
	private static final String recognizeRelation = "recognizeRelation";
	private static final String recognizeRelationParms[] =
		{ "relation", "includeLast", "definableIdentifiers" };
	private static final String computeRelation = "computeRelation";
	private static final String computeRelationParms[] =
		{ "relation", "includeLast" };
	private static final String checkMultiplicity = "checkMultiplicity";
	private static final String checkMultiplicityParms[] =
		{ "multiplicity", "valued", "fieldName" };
	private static final String getAliasForIdentifier = "getAliasForIdentifier";
	private static final String getAliasForIdentifierParms[] = { "identifier" };
	private static final String mapIdentifierToAlias = "mapIdentifierToAlias";
	private static final String mapIdentifierToAliasParms[] = { "identifier" };
	private static final String mergeHashMaps = "mergeHashMaps";
	private static final String mergeHashMapsParms2[] = { "map1", "map2" };
	private static final String mergeHashMapsParms3[] =
		{ "map1", "map2", "map3" };
	private static final String intersectJoinMaps = "intersectJoinMaps";
	private static final String intersectJoinMapsParms[] =
		{ "actualMap", "popMap" };
	private static final String intersectMaxJoinMaps = "intersectMaxJoinMaps";
	private static final String intersectMaxJoinMapsParms[] =
		{ "actualMap", "orList" };
	private static final String appendArrayLists = "appendArrayLists";
	private static final String appendArrayListsParms2[] = { "left", "right" };
	private static final String appendArrayListsParms3[] =
		{ "left", "middle", "right" };

    private static final SqlTreeBuilder BUILDER = SqlTreeBuilder.getInstance();

	private boolean useFullBeanDescriptorIgnored = false;
	private boolean distinct = false;
	private boolean forUpdate = false;
	private boolean noEmptyTables = false;
	private boolean onlyOneIdentifier = false;
	private boolean crossJoinOptimisationPossible = false;
	private boolean doCrossJoinOptimisation = false;
	private boolean countOutside = false;
	private boolean nativeTableNameEncountered = false;
	private int aliasCount = 1;
	private int selectListSizeBeforeOrderByProcessing = 0;
	private GeneralSelectStatement selectStatement = null;
	private SQLStatement modifyStatement = null;
	private int modifyMode = 0;
	private QuerySpecification querySpecification = null;
	private SearchCondition searchCondition = null;
	private CommonInputDescriptor[] inputDescriptor = null;
	private CommonResultDescriptor[] resultDescriptor = null;

	private List<SelectSublist> selectList = new ArrayList<SelectSublist>();
	private List<TableReference> tabRefList = new ArrayList<TableReference>();
	private List<SortSpecification> orderByList = new ArrayList<SortSpecification>();
	private List<SetClause> updateSetList = new ArrayList<SetClause>();
	private List<ColumnReference> insertColumnList = new ArrayList<ColumnReference>();
	private List<RowValue> insertValueList = new ArrayList<RowValue>();
	private List<SearchCondition> joinConditionList = new ArrayList<SearchCondition> ();
	private List<CommonInputDescriptor> inputDescriptorList = new ArrayList<CommonInputDescriptor>();
	private List<CommonResultDescriptor> resultDescriptorList = new ArrayList<CommonResultDescriptor>();
	private Map<Integer,InputParameter> inputParameterList = new HashMap<Integer,InputParameter>();
	private Map<String, String> relationAliasMap = new HashMap<String, String>();
	private Set<String> aliasesUsedByIdentifiers = new HashSet<String>();
	private Map<String, String> aliasesReplacingIdentifiers = new HashMap<String, String>();

	private Map<String, Integer> typeMap = null;

	private boolean useFullBeanDescriptors = false;
	private boolean isForNativeUseOnly = false;
	private boolean failsafe = false;

	private ORMappingManager orMapping = null;
	private WhereNodeManager whereManager = null;
	private ExpressionNodeManager expManager = null;
	private BooleanNodeManager boolManager = null;
	private SubqueryBuilder subqueryBuilder = null;

	private static final int UPDATE = 1;
	private static final int INSERT = 2;
	private static final int DELETE = 3;

	private static final String PRIMARY_KEY_TXT = "primary key fields";
	private static final String NON_KEY_FIELDS_TXT = "non primary key fields";
	private static final String ALL_FIELDS_TXT = "all fields";
	private static final String UNKNOWN_TXT = "unknown";

	private static final String aliasPraefix = "ALIAS_";
	static final int singleValuedCMR = 0;
	static final int collectionValuedCMR = 1;

	private boolean cleared = true;

	/**
	 * Creates an inital <code>SQLTreeNodeManager</code> instance.
	 * The instance created has to be fully initialized with method
	 * <code>prepare()</code> and a given <code>ORMappingManager</code>.
	 */
	SQLTreeNodeManager() {
	}

	void prepare(
		ORMappingManager orMapping,
		boolean useFullBeanDescriptors,
		boolean forNativeUseOnly,
		boolean failsafe) {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{
					orMapping,
					new Boolean(useFullBeanDescriptors),
					new Boolean(forNativeUseOnly),
					new Boolean(failsafe)};
			DevTrace.entering(loc, prepare, prepareParms, inputValues);
		}

		if (this.orMapping == null) {
			this.orMapping = orMapping;
			this.subqueryBuilder = new SubqueryBuilder(this);
			this.whereManager = new WhereNodeManager(this);
			this.expManager = new ExpressionNodeManager(this);
			this.boolManager = new BooleanNodeManager(this);
		}

		if (!this.cleared) {
			DevTrace.debugInfo(loc, prepare, "clear() was not called;");
			this.clear();
		}

		this.cleared = false;

		this.useFullBeanDescriptors = useFullBeanDescriptors;
		this.isForNativeUseOnly = forNativeUseOnly;
		this.failsafe = failsafe;

		this.subqueryBuilder.prepare();
		this.whereManager.prepare();
		this.expManager.prepare();
		this.boolManager.prepare();

		DevTrace.exiting(loc, prepare);
	}

	/**
	 * Resets this <code>SQLTreeNodeManager</code> instance for a
		 * subsequent prepare and processing of a query, load or store action.
		 */
	void clear() {
		DevTrace.debugInfo(loc, clear, "clearing.");
		this.useFullBeanDescriptorIgnored = false;
		this.distinct = false;
		this.forUpdate = false;
		this.noEmptyTables = false;
		this.onlyOneIdentifier = false;
		this.crossJoinOptimisationPossible = false;
		this.doCrossJoinOptimisation = false;
		this.countOutside = false;
		this.nativeTableNameEncountered = false;
		this.aliasCount = 1;
		this.selectListSizeBeforeOrderByProcessing = 0;

		this.selectStatement = null;
		this.modifyStatement = null;
		this.modifyMode = 0;
		this.querySpecification = null;
		this.searchCondition = null;
		this.inputDescriptor = null;
		this.resultDescriptor = null;

		this.selectList.clear();
		this.tabRefList.clear();
		this.orderByList.clear();
		this.updateSetList.clear();
		this.insertColumnList.clear();
		this.insertValueList.clear();
		this.joinConditionList.clear();
		this.inputDescriptorList.clear();
		this.resultDescriptorList.clear();
		this.inputParameterList.clear();
		this.relationAliasMap.clear();
		this.aliasesUsedByIdentifiers.clear();
		this.aliasesReplacingIdentifiers.clear();

		this.useFullBeanDescriptors = false;
		this.isForNativeUseOnly = false;
		this.failsafe = false;

		this.subqueryBuilder.clear();
		this.whereManager.clear();
		this.expManager.clear();
		this.boolManager.clear();
		this.cleared = true;
	}

	/**
	 * To announce used identifiers.
	 * Each identifier that is contained in the EJBQL query and 
	 * that has a prefix that is equal the aliasPraefix is 
	 * stored in the HasSet aliasesUsedByIdentifiers. 
	 * Method <code>getNewAliasName()</code> only generates table name aliases
	 * that are not in this set.
	 */
	void announceIdentifiers(String[] identifier) {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object[] inputValues = { identifier };
			DevTrace.entering(
				loc,
				announceIdentifiers,
				announceIdentifiersParms,
				inputValues);
		}

		this.onlyOneIdentifier = (identifier.length == 1);

		for (int i = 0; i < identifier.length; i++) {
			String subString =
				identifier[i].substring(
					0,
					Math.min(
						identifier[i].length(),
						SQLTreeNodeManager.aliasPraefix.length()));
			if (subString.equalsIgnoreCase(SQLTreeNodeManager.aliasPraefix)) {
				this.aliasesUsedByIdentifiers.add(identifier[i]);
				if (DevTrace.isOnDebugLevel(loc)) {
					DevTrace.debugInfo(
						loc,
						announceIdentifiers,
						identifier[i] + " is set in aliasesUsedByIdentifiers.");
				}
			}
		}
		DevTrace.exiting(loc, announceIdentifiers);
		return;
	}

	/**
	 * Starts the assembling of an SQLStatement for a query
	 * and transfers the distinct, forUpdate and non-empty table flag.
	 */
	void createQuerySpecification(
		boolean distinct,
		boolean forUpdate,
		boolean noEmptyTables) {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{
					new Boolean(distinct),
					new Boolean(forUpdate),
					new Boolean(noEmptyTables)};
			DevTrace.entering(
				loc,
				createQuerySpecification,
				createQuerySpecificationParms,
				inputValues);
		}

		this.distinct = distinct;
		this.forUpdate = forUpdate;
		this.noEmptyTables = noEmptyTables;

		DevTrace.exiting(loc, createQuerySpecification);
	}

	/**
	 * Starts the assembling of an SQLStatement for an ejb update action.
	 */
	void createUpdateStatement() {
		DevTrace.entering(loc, createUpdateStatement, null, null);
		this.modifyMode = UPDATE;
		DevTrace.exiting(loc, createUpdateStatement);
	}

	/**
	 * Starts the assembling of an SQLStatement for an ejb insert action.
	 */
	void createInsertStatement() {
		DevTrace.entering(loc, createInsertStatement, null, null);
		this.modifyMode = INSERT;
		DevTrace.exiting(loc, createInsertStatement);
	}

	/**
	 * Starts the assembling of an SQLStatement for an ejb remove action.
	 */
	void createDeleteStatement() {
		DevTrace.entering(loc, createDeleteStatement, null, null);
		this.modifyMode = DELETE;
		DevTrace.exiting(loc, createDeleteStatement);
	}

	/**
	 * Sets all or only the primary key columns of a bean's table
	 * into the select list depending on flag useFullBeanDescriptors.
	 */
	void setSelectList(BeanObject beanObject) throws SQLMappingException {
		// select object(o)
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanObject };
			DevTrace.entering(
				loc,
				setSelectList,
				setSelectListBeanParms,
				inputValues);
		}

		String beanType = beanObject.getType();
		BeanDescriptor beanDesc =
			(this.useFullBeanDescriptors)
				? (BeanDescriptor) this.orMapping.getFullBean(beanType)
				: (BeanDescriptor) this.orMapping.getBeanPK(beanType);

		boolean columnsComparable = true;
		if (this.useFullBeanDescriptors == true) {
			DevTrace.debugInfo(
				loc,
				setSelectList,
				"check columns' comparability.");

			Integer[] jdbcTypesForCheck = beanDesc.getJdbcTypes();
			DatabaseColumn[] columnNamesForCheck = beanDesc.getColumnNames();
			int i;
			int count = jdbcTypesForCheck.length;
			for (i = 0; i < count; i++) {
				if (!CommonTypes.isComparable(jdbcTypesForCheck[i].intValue()))
					break;
			}

			if (i < count) {
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"incomparable column "
						+ columnNamesForCheck[i].getName(this.isForNativeUseOnly)
						+ " with jdbc type "
						+ jdbcTypesForCheck[i]
						+ "found.");
				columnsComparable = false;
				if (this.distinct) {
					this.useFullBeanDescriptorIgnored = true;
					if (DevTrace.isOnDebugLevel(loc)) {
						DevTrace.debugInfo(
							loc,
							setSelectList,
							"incomparable jdbc type "
								+ jdbcTypesForCheck[i]
								+ " (bean field: "
								+ (beanDesc.getFieldNames())[i]
								+ ", bean type: "
								+ beanType
								+ ")"
								+ "; primary key is selected instead of complete bean");
					}
					beanDesc = this.orMapping.getBeanPK(beanType);
				}
			}
		}

		String identifierAlias =
			this.mapIdentifierToAlias(beanObject.getIdentifier());
		DatabaseColumn[] columns = beanDesc.getColumnNames();
		String[] fieldNames = beanDesc.getFieldNames();
		Integer[] jdbcTypes = beanDesc.getJdbcTypes();
		Boolean[] pkFlags = beanDesc.getPKFlags();

		for (int i = 0; i < columns.length; i++) {
			selectList.add(
                    BUILDER.createSelectSublist().setValue(new ejb21_ColumnReference(identifierAlias,
					columns[i].getName(this.isForNativeUseOnly),
					this.isForNativeUseOnly || columns[i].isNative())).build());

			// add ResultDescriptor
			CommonResultDescriptor rd =
				new CommonResultDescriptor(
					i + 1,
					jdbcTypes[i].intValue(),
					pkFlags[i].booleanValue(),
					true,
					beanType,
					fieldNames[i]);
			this.resultDescriptorList.add(rd);
		}

		if (this.noEmptyTables
			&& (this.distinct || (columnsComparable && this.onlyOneIdentifier))) {
			DevTrace.debugInfo(
				loc,
				setSelectList,
				"cross join optimisation is possible.");
			this.crossJoinOptimisationPossible = true;
		} else {
			DevTrace.debugInfo(
				loc,
				setSelectList,
				"cross join optimisation is not possible, noEmptyTables = "
					+ noEmptyTables
					+ ", columnsComparable = "
					+ columnsComparable
					+ ", onlyOneIdentifier = "
					+ this.onlyOneIdentifier
					+ ".");
		}

		if (!this.failsafe && this.crossJoinOptimisationPossible) {
			DevTrace.debugInfo(
				loc,
				setSelectList,
				"cross join optimisation will be done.");
			if (this.distinct == false) {
				this.distinct = true;
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"distinct is set implicit.");
			}
			this.doCrossJoinOptimisation = true;
		} else {
			DevTrace.debugInfo(
				loc,
				setSelectList,
				"cross join optimisation will not be done, failsafe = "
					+ this.failsafe
					+ ", crossJoinOptimisationPossible = "
					+ this.crossJoinOptimisationPossible
					+ ".");
		}

		DevTrace.exiting(loc, setSelectList);
	}

	/**
	 * Sets all columns of the helperTable's table into the select list.
	 */
	void setSelectList(HelperTableFacade helperTable)
		throws SQLMappingException {
		// select all columns from helper table
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { helperTable };
			DevTrace.entering(
				loc,
				setSelectList,
				setSelectListHelperTableParms,
				inputValues);
		}

		if (this.distinct == true) {
			throw new SQLMappingException(
				"If method setSelectList(helperTable) is invoked, "
					+ "distinct must not bet set.",
				"The SQL Mapper has tried to build up a select list for a"
					+ " helper table representing a M:N CMR and has recognized"
					+ " that the distinct flag was set."
					+ " This is not supported and the most likely cause"
					+ " is an internal programming error within SQL Mapper."
					+ " Please kindly open a problem ticket for SAP on"
					+ " component BC-JAS-PER-DBI.",
				"CSM066");
		}

		int k = 0;
		for (int i = HelperTableFacade.FRONT_SIDE;
			i < HelperTableFacade.NUMBER_OF_SIDES;
			i++) {
			DatabaseColumn[] columns = helperTable.getColumns(i);
			String beanName = helperTable.getBean(i);
			BeanDescriptor beanDescr = this.orMapping.getBeanPK(beanName);
			String[] fieldNames = beanDescr.getFieldNames();
			Integer[] jdbcTypes = beanDescr.getJdbcTypes();
			int j = 0;
			for (; j < columns.length; j++) {
				selectList.add(
                        BUILDER.createSelectSublist().setValue(
						 new ejb21_ColumnReference(helperTable
							.getName(this.isForNativeUseOnly),
						columns[j].getName(this.isForNativeUseOnly),
						this.isForNativeUseOnly || columns[j].isNative())).build());

				// add ResultDescriptor
				CommonResultDescriptor rd =
					new CommonResultDescriptor(
						k + j + 1,
						jdbcTypes[j].intValue(),
						true,
						true,
						beanName,
						fieldNames[j]);
				this.resultDescriptorList.add(rd);
			}
			k = j;
		}

		DevTrace.exiting(loc, setSelectList);
	}

	/**
	 * Sets the primary key columns into the select list of the
	 * assembled statement and
	 * sets flag countOutside so that is is indicated that a select count(*)
	 * has to be wrapped around the assembled SQLStatement in method
	 * <code>endOfStatement()</code>, where the assembling of the SQLStatement
	 * is finalised.
	 */
	void setSelectList(int functionType, BeanObject beanObject)
		throws SQLMappingException {
		// select count(o)
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { new Integer(functionType), beanObject };
			DevTrace.entering(
				loc,
				setSelectList,
				setSelectListTypeAndBeanParms,
				inputValues);
		}

		this.countOutside = true;
		DevTrace.debugInfo(loc, setSelectList, " flag countOutside is set.");

		PKBeanDescriptor beanDesc =
			this.orMapping.getBeanPK(beanObject.getType());
		DatabaseColumn[] pkColumns = beanDesc.getColumnNames();

		String identifierAlias =
			this.mapIdentifierToAlias(beanObject.getIdentifier());
		for (int i = 0; i < pkColumns.length; i++) {
			selectList.add(
                    BUILDER.createSelectSublist().setValue(
					new ejb21_ColumnReference(identifierAlias,
					pkColumns[i].getName(this.isForNativeUseOnly),
					this.isForNativeUseOnly || pkColumns[i].isNative())).build());
		}

		// add ResultDescriptor		
		CommonResultDescriptor rd =
			new CommonResultDescriptor(
				1,
				java.sql.Types.INTEGER,
				false,
				false,
				null,
				null);
		this.resultDescriptorList.add(rd);

		if (this.noEmptyTables && this.distinct) {
			DevTrace.debugInfo(
				loc,
				setSelectList,
				"cross join optimisation is possible.");
			this.crossJoinOptimisationPossible = true;
		} else {
			DevTrace.debugInfo(
				loc,
				setSelectList,
				"cross join optimisation is not possible, noEmptyTables = "
					+ noEmptyTables
					+ ", distinct = "
					+ this.distinct
					+ ".");
		}

		if (!this.failsafe && this.crossJoinOptimisationPossible) {
			DevTrace.debugInfo(
				loc,
				setSelectList,
				"cross join optimisation will be done.");
			this.doCrossJoinOptimisation = true;
		} else {
			DevTrace.debugInfo(
				loc,
				setSelectList,
				"cross join optimisation will not be done, failsafe = "
					+ this.failsafe
					+ ", crossJoinOptimisationPossible = "
					+ this.crossJoinOptimisationPossible
					+ ".");
		}

		DevTrace.exiting(loc, setSelectList);
	}

	/**
	 * Sets the column of an cmp field into the select list, if
	 * the beanField is a cmp field. If the beanField is a cmr
	 * field, this method sets the primary key columns
	 * or all columns of the bean the cmr field is poiting to
	 * into the select list depending on flag useFullBeanDescriptors.
	 * If the beanField is a dependent value field, all
	 * columns of this dependent value field are set into the select list.
	 */
	void setSelectList(BeanField beanField, BeanField[] relation)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanField, relation };
			DevTrace.entering(
				loc,
				setSelectList,
				setSelectListBeanFieldAndRelation,
				inputValues);
		}

		if (beanField.isRelation() == false) {
			// select (id.cmr1.cmr2.)cmp
			DevTrace.debugInfo(loc, setSelectList, " bean field is cmp.");
			String columnQualifier =
				(relation == null || relation.length == 0)
					? this.mapIdentifierToAlias(beanField.getParentName())
					: this.recognizeRelation(relation, true, true);
			String parentBean = beanField.getParentType();
			String fieldName = beanField.getFieldName();
			if (beanField.isDependentValue()) {
				DevTrace.debugInfo(loc, setSelectList, " bean field is dv.");
				DVFieldDescriptor fd =
					this.orMapping.getDVBeanField(parentBean, fieldName);

				DatabaseColumn[] columns = fd.getColumnNames();
				int[] jdbcTypes = fd.getJdbcTypes();

				if (this.distinct == true) {
					DevTrace.debugInfo(
						loc,
						setSelectList,
						"comparable check switched on.");
				}

				for (int i = 0; i < columns.length; i++) {

					if (this.distinct == true)
						this.checkJdbcTypeForComparability(
							jdbcTypes[i],
							fieldName,
							parentBean,
							columns[i].getName(this.isForNativeUseOnly));

					selectList.add(
                            BUILDER.createSelectSublist().setValue(new ejb21_ColumnReference(columnQualifier,
							columns[i].getName(this.isForNativeUseOnly),
							this.isForNativeUseOnly || columns[i].isNative())).build());

					// add ResultDescriptor
					CommonResultDescriptor rd =
						new CommonResultDescriptor(
							i + 1,
							jdbcTypes[i],
							false,
							true,
							parentBean,
							fieldName);
					this.resultDescriptorList.add(rd);
				}

				if (this.noEmptyTables && this.distinct) {
					DevTrace.debugInfo(
						loc,
						setSelectList,
						"cross join optimisation is possible.");
					this.crossJoinOptimisationPossible = true;
				} else {
					DevTrace.debugInfo(
						loc,
						setSelectList,
						"cross join optimisation is not possible, noEmptyTables = "
							+ noEmptyTables
							+ ", distinct = "
							+ this.distinct
							+ ".");
				}

				if (!this.failsafe && this.crossJoinOptimisationPossible) {
					DevTrace.debugInfo(
						loc,
						setSelectList,
						"cross join optimisation will be done.");
					this.doCrossJoinOptimisation = true;
				} else {
					DevTrace.debugInfo(
						loc,
						setSelectList,
						"cross join optimisation will not be done, failsafe = "
							+ this.failsafe
							+ ", crossJoinOptimisationPossible = "
							+ this.crossJoinOptimisationPossible
							+ ".");
				}
			} else {
				CMPFieldDescriptor fieldDescr =
					this.orMapping.getCMPBeanField(parentBean, fieldName);
				DatabaseColumn columnName = fieldDescr.getColumnName();

				int jdbcType = fieldDescr.getJdbcType();

				if (this.distinct == true)
					this.checkJdbcTypeForComparability(
						jdbcType,
						fieldName,
						parentBean,
						columnName.getName(this.isForNativeUseOnly));

				selectList.add(
                        BUILDER.createSelectSublist().setValue(new ejb21_ColumnReference(columnQualifier,
						columnName.getName(this.isForNativeUseOnly),
						this.isForNativeUseOnly || columnName.isNative())).build());

				// add ResultDescriptor						
				CommonResultDescriptor rd =
					new CommonResultDescriptor(
						1,
						jdbcType,
						false,
						true,
						parentBean,
						fieldName);
				this.resultDescriptorList.add(rd);

				boolean columnIsTheSingleKeyColumn = false;
				if ((relation == null || relation.length == 0)
					&& fieldDescr.isKeyField()) {
					BeanTable beanTable =
						this.orMapping.getBeanTable(parentBean);
					columnIsTheSingleKeyColumn =
						!beanTable.hasCompoundPrimaryKey();
				}

				if (this.noEmptyTables
					&& (this.distinct
						|| (columnIsTheSingleKeyColumn
							&& this.onlyOneIdentifier))) {
					DevTrace.debugInfo(
						loc,
						setSelectList,
						"cross join optimisation is possible.");
					this.crossJoinOptimisationPossible = true;
				} else {
					DevTrace.debugInfo(
						loc,
						setSelectList,
						"cross join optimisation is not possible, noEmptyTables = "
							+ noEmptyTables
							+ ", columnIsTheSingleKeyColumnAndRelationIsNull = "
							+ columnIsTheSingleKeyColumn
							+ ", onlyOneIdentifier = "
							+ this.onlyOneIdentifier
							+ ".");
				}

				if (!this.failsafe && this.crossJoinOptimisationPossible) {
					DevTrace.debugInfo(
						loc,
						setSelectList,
						"cross join optimisation will be done.");
					if (this.distinct == false) {
						this.distinct = true;
						DevTrace.debugInfo(
							loc,
							setSelectList,
							"distinct is set implicit.");
					}
					this.doCrossJoinOptimisation = true;
				} else {
					DevTrace.debugInfo(
						loc,
						setSelectList,
						"cross join optimisation will not be done, failsafe = "
							+ this.failsafe
							+ ", crossJoinOptimisationPossible = "
							+ this.crossJoinOptimisationPossible
							+ ".");
				}
			}
		} else {
			// select (id.cmr1.cmr2.)cmr3
			DevTrace.debugInfo(loc, setSelectList, " bean field is cmr.");
			String fieldName = beanField.getFieldName();
			String parentBean = beanField.getParentType();
			String beanName = beanField.getType();
			CMRFieldDescriptor fd =
				orMapping.getCMRBeanField(parentBean, fieldName);
			this.checkMultiplicity(
				fd.getMultiplicity(),
				SQLTreeNodeManager.singleValuedCMR,
				fieldName);

			DatabaseColumn[] fkColumns = fd.getColumnNames();
			boolean fkColumnsOnOtherSide =
				(fkColumns == null || fkColumns.length == 0) ? true : false;

			String columnQualifier =
				(relation == null || relation.length == 0)
					? this.mapIdentifierToAlias(beanField.getParentName())
					: this.recognizeRelation(
						relation,
						fkColumnsOnOtherSide,
						true);

			BeanDescriptor beanDesc = this.orMapping.getBeanPK(beanName);

			if (this.useFullBeanDescriptors) {
				if (fkColumnsOnOtherSide) {
					beanDesc = this.orMapping.getFullBean(beanName);
					if (this.distinct) {
						DevTrace.debugInfo(
							loc,
							setSelectList,
							"comparable check switched on.");

						Integer[] jdbcTypesForCheck = beanDesc.getJdbcTypes();

						int i;
						int count = jdbcTypesForCheck.length;
						for (i = 0; i < count; i++) {
							if (!CommonTypes
								.isComparable(jdbcTypesForCheck[i].intValue()))
								break;
						}

						if (i < count) {
							this.useFullBeanDescriptorIgnored = true;
							if (DevTrace.isOnDebugLevel(loc)) {
								DevTrace.debugInfo(
									loc,
									setSelectList,
									"incomparable jdbc type "
										+ jdbcTypesForCheck[i]
										+ " (bean field: "
										+ (beanDesc.getFieldNames())[i]
										+ ", bean type: "
										+ beanName
										+ ")"
										+ "; primary key is selected instead of complete bean");
							}
							beanDesc = this.orMapping.getBeanPK(beanName);
						}
					}
				} else {
					this.useFullBeanDescriptorIgnored = true;
					if (DevTrace.isOnDebugLevel(loc)) {
						DevTrace.debugInfo(
							loc,
							setSelectList,
							"selecting cmr field; "
								+ " primary key is selected instead of complete bean");
					}
				}
			}

			DatabaseColumn[] columnNames;
			String[] fieldNames;
			if (fkColumnsOnOtherSide) {
				columnNames = beanDesc.getColumnNames();
				fieldNames = beanDesc.getFieldNames();
			} else {
				columnNames = fd.getColumnNames();
				fieldNames = new String[columnNames.length];
				for (int i = 0; i < columnNames.length; i++) {
					fieldNames[i] = fieldName;
				}
			}
			Integer[] jdbcTypes = beanDesc.getJdbcTypes();
			Boolean[] pkFlags = beanDesc.getPKFlags();

			for (int i = 0; i < columnNames.length; i++) {
				selectList.add(
                        BUILDER.createSelectSublist().setValue(new ejb21_ColumnReference(columnQualifier,
						columnNames[i].getName(this.isForNativeUseOnly),
						this.isForNativeUseOnly || columnNames[i].isNative())).build());
			}

			// add ResultDescriptors
			for (int i = 0; i < fieldNames.length; i++) {

				CommonResultDescriptor rd =
					new CommonResultDescriptor(
						i + 1,
						jdbcTypes[i].intValue(),
						pkFlags[i].booleanValue(),
						true,
						(fkColumnsOnOtherSide) ? beanName : parentBean,
						fieldNames[i]);
				this.resultDescriptorList.add(rd);
			}

			if (this.noEmptyTables && this.distinct) {
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"cross join optimisation is possible.");
				this.crossJoinOptimisationPossible = true;
			} else {
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"cross join optimisation is not possible, noEmptyTables = "
						+ noEmptyTables
						+ ", distinct = "
						+ this.distinct
						+ ".");
			}

			if (!this.failsafe && this.crossJoinOptimisationPossible) {
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"cross join optimisation will be done.");
				this.doCrossJoinOptimisation = true;
			} else {
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"cross join optimisation will not be done, failsafe = "
						+ this.failsafe
						+ ", crossJoinOptimisationPossible = "
						+ this.crossJoinOptimisationPossible
						+ ".");
			}
		}

		DevTrace.exiting(loc, setSelectList);
	}

	/**
	 * Sets a column with an aggregate function into the select list,
	 * if the beanField is a cmp field. If the beanField is a cmr field,
	 * only COUNT is possible as the functionType. In this case a CountAll
	 * is set into the select list, if the distinct value is false.
	 * If the distinct value is true, the helper flag countOutside is set
	 * and the count(*) is wrapped around the assembled SQLStatement
	 * in funtcion <code>endOfStatement()</code> where the assembling is finalised.
	 */
	void setSelectList(
		SetFunctionType functionType,
		BeanField beanField,
		BeanField[] relation)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{ functionType, beanField, relation };
			DevTrace.entering(
				loc,
				setSelectList,
				setSelectListFunctionTypeBeanFieldRelation,
				inputValues);
		}

		String columnQualifier =
			(relation == null || relation.length == 0)
				? this.mapIdentifierToAlias(beanField.getParentName())
				: this.recognizeRelation(relation, true, true);
		if (beanField.isRelation() == false) {
			// select agr(id.cmr1.cmr2.cmp)
			DevTrace.debugInfo(loc, setSelectList, " bean field is cmp.");
			if (beanField.isDependentValue()) {
				DevTrace.exitingWithException(loc, setSelectList);
				throw new SQLMappingException(
					"Unexpected dependent value field "
						+ beanField.getFieldName()
						+ " is used in an aggregate select expression.",
					"The EJB-QL to SQL Mapper only supportes dependent value "
						+ "fields in a select expression, which is a "
						+ "cmp path expression",
					"CSM032");
			}
			String parentBean = beanField.getParentType();
			String fieldName = beanField.getFieldName();
			CMPFieldDescriptor fd =
				this.orMapping.getCMPBeanField(parentBean, fieldName);
			DatabaseColumn columnName = fd.getColumnName();
			int jdbcType = fd.getJdbcType();

			if (this.distinct) {
				this.checkJdbcTypeForComparability(
					jdbcType,
					fieldName,
					parentBean,
					columnName.getName(this.isForNativeUseOnly));
			}

			boolean columnIsTheSingleKeyColumn = false;
			if ((relation == null || relation.length == 0)
				&& fd.isKeyField()) {
				BeanTable beanTable = this.orMapping.getBeanTable(parentBean);
				columnIsTheSingleKeyColumn = !beanTable.hasCompoundPrimaryKey();
			}

			if (this.noEmptyTables
				&& (this.distinct
					|| (columnIsTheSingleKeyColumn && this.onlyOneIdentifier))) {
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"cross join optimisation is possible.");
				this.crossJoinOptimisationPossible = true;
			} else {
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"cross join optimisation is not possible, noEmptyTables = "
						+ noEmptyTables
						+ ", columnIsTheSingleKeyColumnAndRelationIsNull = "
						+ columnIsTheSingleKeyColumn
						+ ", onlyOneIdentifier = "
						+ this.onlyOneIdentifier
						+ ".");
			}

			if (!this.failsafe && this.crossJoinOptimisationPossible) {
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"cross join optimisation will be done.");
				if (this.distinct == false) {
					this.distinct = true;
					DevTrace.debugInfo(
						loc,
						setSelectList,
						"distinct is set implicit.");
				}
				this.doCrossJoinOptimisation = true;
			} else {
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"cross join optimisation will not be done, failsafe = "
						+ this.failsafe
						+ ", crossJoinOptimisationPossible = "
						+ this.crossJoinOptimisationPossible
						+ ".");
			}

			selectList.add(BUILDER.createSelectSublist().setValue(
                    BUILDER.createSetFunction(functionType,
                            new ejb21_ColumnReference(columnQualifier, columnName.getName(this.isForNativeUseOnly),
                                    this.isForNativeUseOnly || columnName.isNative()))
                                    .setDistinct(this.distinct).build()).build());

			//this.distinct is related to function type argument,
			//it is processed here, so clear it now
			this.distinct = false;

			// add ResultDescriptor
			CommonResultDescriptor rd =
				new CommonResultDescriptor(
					1,
					((functionType == SetFunctionType.COUNT)
						? java.sql.Types.INTEGER
						: jdbcType),
					false,
					false,
					null,
					null);
			this.resultDescriptorList.add(rd);

		} else if (functionType == SetFunctionType.COUNT) {
			// select count(id.cmr1.cmr2.cmr3)
			DevTrace.debugInfo(loc, setSelectList, " bean field is cmr.");
			String fieldName = beanField.getFieldName();
			CMRFieldDescriptor fd =
				this.orMapping.getCMRBeanField(
					beanField.getParentType(),
					fieldName);
			this.checkMultiplicity(
				fd.getMultiplicity(),
				SQLTreeNodeManager.singleValuedCMR,
				fieldName);

			if (this.distinct == false) {
				selectList.add(BUILDER.createSelectSublist().setValue(BUILDER.createCountAll().build()).build());
			} else {
				this.countOutside = true;
				DevTrace.debugInfo(
					loc,
					setSelectList,
					" flag countOutside is set.");
				String beanName = beanField.getType();
				PKBeanDescriptor beanDesc = this.orMapping.getBeanPK(beanName);
				DatabaseColumn[] pkColumns = beanDesc.getColumnNames();
				for (int i = 0; i < pkColumns.length; i++) {
					selectList.add(
                            BUILDER.createSelectSublist().setValue(
							new ejb21_ColumnReference(columnQualifier,
							pkColumns[i].getName(this.isForNativeUseOnly),
							this.isForNativeUseOnly
								|| pkColumns[i].isNative())).build());
				}
			}

			// add ResultDescriptor
			CommonResultDescriptor rd =
				new CommonResultDescriptor(
					1,
					java.sql.Types.INTEGER,
					false,
					false,
					null,
					null);
			this.resultDescriptorList.add(rd);

			if (this.noEmptyTables && this.distinct) {
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"cross join optimisation is possible.");
				this.crossJoinOptimisationPossible = true;
			} else {
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"cross join optimisation is not possible, noEmptyTables = "
						+ noEmptyTables
						+ ", distinct = "
						+ this.distinct
						+ ".");
			}

			if (!this.failsafe && this.crossJoinOptimisationPossible) {
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"cross join optimisation will be done.");
				this.doCrossJoinOptimisation = true;
			} else {
				DevTrace.debugInfo(
					loc,
					setSelectList,
					"cross join optimisation will not be done, failsafe = "
						+ this.failsafe
						+ ", crossJoinOptimisationPossible = "
						+ this.crossJoinOptimisationPossible
						+ ".");
			}

		} else {
			DevTrace.exitingWithException(loc, setSelectList);
			throw new SQLMappingException(
				"A function type not equal COUNT for a CMR field is not allowed "
					+ "in the select list",
				"In the EJB QL parsed tree, SQL mapper has found a "
					+ "CMR field in the select list where the function type "
					+ functionType
					+ " is used. This function type is not equal the COUNT "
					+ "function type.",
				"CSM010");
		}

		DevTrace.exiting(loc, setSelectList);
	}

	/** 
	 * Adds a bean's table to the table list of the SQLStatement.
	 */
	void addToFromList(BeanObject beanObject) throws SQLMappingException {
		this.addToTableList(beanObject);
	}

	void addToTableList(BeanObject beanObject) throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanObject };
			DevTrace.entering(
				loc,
				addToTableList,
				addToTableListBeanParms,
				inputValues);
		}

		BeanTable beanTable = this.orMapping.getBeanTable(beanObject.getType());
		String aliasName =
			(beanObject.getIdentifier() == null
				? null
				: this.mapIdentifierToAlias(beanObject.getIdentifier()));
		ejb21_TableReference tableReference =
			this.createTableReferenceDetectNativeNames(beanTable, aliasName);
		this.tabRefList.add(tableReference);

		DevTrace.exiting(loc, addToTableList);
	}

	/**
	 * Addds the helperTable's table to the table list of the SQLStatement
	 */
	void addToFromList(HelperTableFacade helperTable) {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { helperTable };
			DevTrace.entering(
				loc,
				addToFromList,
				addToFromListHelperTableParms,
				inputValues);
		}

		// with aliase
		ejb21_TableReference tableReference =
			this.createTableReferenceDetectNativeNames(
				helperTable,
				helperTable.getName(this.isForNativeUseOnly));
		this.tabRefList.add(tableReference);

		DevTrace.exiting(loc, addToFromList);
	}

	void addToTableList(HelperTableFacade helperTable) {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { helperTable };
			DevTrace.entering(
				loc,
				addToTableList,
				addToTableListHelperTableParms,
				inputValues);
		}

		// without alias
		ejb21_TableReference tableReference =
			this.createTableReferenceDetectNativeNames(helperTable, null);
		this.tabRefList.add(tableReference);

		DevTrace.exiting(loc, addToTableList);
	}

	/** 
	 * Adds the given tableName with given aliasName
	 * to the table list of the SQLStatement.
	 */
	void addToTableList(BeanTable beanTable, String aliasName) {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanTable, aliasName };
			DevTrace.entering(
				loc,
				addToTableList,
				addToTableListTableAndAliasParms,
				inputValues);
		}

		ejb21_TableReference tableReference =
			this.createTableReferenceDetectNativeNames(beanTable, aliasName);
		this.tabRefList.add(tableReference);

		DevTrace.exiting(loc, addToTableList);
	}

	/**
	 * This method is called when a EJBQL 'IN' clause is processed.
	 * The beanField has to be a collection valued cmr field;
	 * otherwise an SQLMappingException will be thrown.
	 * The relation path infront of the cmr field is recognized
	 * with methods recognizeRelation. This yields to additional
	 * joins in the SQLStatement for relations that are not
	 * already recognized. For the last relation, the collection
	 * valued beanField, a additional join is constructed. In case of
	 * an N:M multiplicity of this cmr field the helper table retrieved from
	 * the M2MDescriptor is included in the join.
	 */
	void addToFromList(
		String identifier,
		BeanField beanField,
		BeanField[] relation)
		throws SQLMappingException {

		this.addToTableList(identifier, beanField, relation);
	}

	void addToTableList(
		String identifier,
		BeanField beanField,
		BeanField[] relation)
		throws SQLMappingException {
		// IN (id.cmr1.cmr2.cmr3) cmr3:collectionValued
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { identifier, beanField, relation };
			DevTrace.entering(
				loc,
				addToTableList,
				addToTableListIdentifierBeanFieldRelationParms,
				inputValues);
		}

		String parentAlias = this.recognizeRelation(relation, false, true);
		String parentType = beanField.getParentType();
		String fieldName = beanField.getFieldName();
		CMRFieldDescriptor fd =
			this.orMapping.getCMRBeanField(parentType, fieldName);
		int multiplicity = fd.getMultiplicity();
		this.checkMultiplicity(
			multiplicity,
			SQLTreeNodeManager.collectionValuedCMR,
			fieldName);
		String beanName = beanField.getType();

		BeanTable beanTable = this.orMapping.getBeanTable(beanName);
		ejb21_TableReference tableReference =
			this.createTableReferenceDetectNativeNames(beanTable, identifier);
		this.tabRefList.add(tableReference);

		PKBeanDescriptor bd = this.orMapping.getBeanPK(parentType);
		DatabaseColumn[] pkColumns = bd.getColumnNames();
		if (multiplicity == this.orMapping.ONE_TO_MANY) {
			DevTrace.debugInfo(
				loc,
				addToTableList,
				" multiplicity is " + this.orMapping.ONE_TO_MANY);
			DatabaseColumn[] fkColumns = fd.getColumnNamesOfOtherSide();
			for (int i = 0; i < pkColumns.length; i++) {
				this.joinConditionList.add(
                        BUILDER.createComparisonPredicate(
						new ejb21_ColumnReference(
							parentAlias,
							pkColumns[i].getName(this.isForNativeUseOnly),
							this.isForNativeUseOnly || pkColumns[i].isNative()),
						ComparisonOperator.EQUAL,
						new ejb21_ColumnReference(
							this.mapIdentifierToAlias(identifier),
							fkColumns[i].getName(this.isForNativeUseOnly),
							this.isForNativeUseOnly
								|| fkColumns[i].isNative())).build());
			}
		} else {
			M2MDescriptor m2mDescr = fd.getM2MDescriptor();
			CheckableIdentifier helperTableIdentifier =
				fd.getHelperTableIdentifier();
			String helperAlias = this.getNewAliasName();
			if (DevTrace.isOnDebugLevel(loc)) {
				DevTrace.debugInfo(
					loc,
					addToTableList,
					" multiplicity is "
						+ this.orMapping.MANY_TO_MANY
						+ ", helperTable = "
						+ helperTableIdentifier.getName(this.isForNativeUseOnly)
						+ ", helperAlias = "
						+ helperAlias
						+ ".");
			}

			tableReference =
				this.createTableReferenceDetectNativeNames(
					helperTableIdentifier,
					helperAlias);
			this.tabRefList.add(tableReference);

			DatabaseColumn[] helperColumns = m2mDescr.getColumnsToMyTable();
			for (int i = 0; i < pkColumns.length; i++) {
				this.joinConditionList.add(
                    BUILDER.createComparisonPredicate(
						new ejb21_ColumnReference(
							parentAlias,
							pkColumns[i].getName(this.isForNativeUseOnly),
							this.isForNativeUseOnly || pkColumns[i].isNative()),
						ComparisonOperator.EQUAL,
						new ejb21_ColumnReference(
							helperAlias,
							helperColumns[i].getName(this.isForNativeUseOnly),
							this.isForNativeUseOnly
								|| helperColumns[i].isNative())).build());
			}
			helperColumns = m2mDescr.getColumnsToRefTable();
			bd = this.orMapping.getBeanPK(beanField.getType());
			pkColumns = bd.getColumnNames();
			for (int i = 0; i < pkColumns.length; i++) {
				this.joinConditionList.add(
                        BUILDER.createComparisonPredicate(
						new ejb21_ColumnReference(
							this.mapIdentifierToAlias(identifier),
							pkColumns[i].getName(this.isForNativeUseOnly),
							this.isForNativeUseOnly || pkColumns[i].isNative()),
                            ComparisonOperator.EQUAL,
						new ejb21_ColumnReference(
							helperAlias,
							helperColumns[i].getName(this.isForNativeUseOnly),
							this.isForNativeUseOnly
								|| helperColumns[i].isNative())).build());
			}
		}

		DevTrace.exiting(loc, addToTableList);
	}

	/**
	 * Finalise the assembling of the from clause. Nothing to do here.
	 */
	void setFromClause() {
		DevTrace.debugInfo(
			loc,
			setFromClause,
			"doCrossJoinOptimisation = " + this.doCrossJoinOptimisation + ".");
	}

	/**
	 * The where clause, assembled by the whereNodeManager before
	 * this method is called, is added with a boolean 'AND' to the
	 * allready assembled join conditions. The result is the
	 * final search condition for the SQLStatement.
	 */
	void setWhereClause() throws SQLMappingException {

		DevTrace.entering(loc, setWhereClause, null, null);

		this.whereManager.checkWhereStackisNull();
		WhereNode whereNode = this.whereManager.getWhereNode();
		HashMap simpleTableRepMap = null;
		HashMap simpleJoinConditionMap = null;
		SearchCondition simpleSearchCondition = null;
		ArrayList composedList = null;

		/*
		 * SINGLE:
		 * trMAP' = MERGE(trMAPEqui,trMAP)
		 * jcMAP' = jcMAP - jcMAPEqui
		 * -> SELECT ... FROM trMAP' WHERE jcMAPEqui AND jcMAP and SC 
		 * 
		 * COMPOSED_OR: 
		 * trMAP' = MERGE(trMapEqui, trMAP1, ... trMAPn)
		 * for (int i = 0; i < n; i++) {
		 *   jcMAPi = jcMAPi - jcMAPEqui
		 * }
		 * 
		 * -> SELECT ... FROM trMAP' WHERE jcMAPEqui AND (MAP1 AND SC1 OR ... OR MAPn AND SCn)
		 * 
		 * COMPOSED_AND:
		 * trMAP' = MERGE(trMapEqui, trMAP1, ... trMAPn)
		 * jcMAP' = MERGE(jcMapEqui, jcMAP1, ... jcMAPm)
		 * 
		 * -> SELECT ... FROM trMAP' WHERE jcMAP' AND (SC1 AND ... AND SCm) 
		 */
		if (whereNode != null) {
			WhereNodeElement simpleElem = null;
			if (whereNode.getType() == WhereNode.SIMPLE) {
				DevTrace.debugInfo(
					loc,
					setWhereClause,
					"current where node is simple.");
				simpleElem = whereNode.getSimple();
				simpleTableRepMap = simpleElem.getTableRepresentations();
				simpleJoinConditionMap = simpleElem.getJoinConditions();
				simpleSearchCondition = simpleElem.getCondition();

				// trMAP' = MERGE(trMAPEqui,trMAP)
				if (simpleTableRepMap != null
					&& simpleTableRepMap.size() > 0) {
					Iterator tabIter = this.tabRefList.iterator();
					ejb21_TableReference tabRef;
					TableRepresentation tabRep;
					while (tabIter.hasNext()) {
						tabRef = (ejb21_TableReference) tabIter.next();
						String key =
							tabRef.toSqlString(SQLStatement.VENDOR_UNKNOWN);
						tabRep =
							(TableRepresentation) simpleTableRepMap.remove(key);
						if (DevTrace.isOnDebugLevel(loc)) {
							DevTrace.debugInfo(
								loc,
								setWhereClause,
								"Key "
									+ key
									+ ((tabRep == null) ? " not" : "")
									+ " removed from simple table representations' map.");
						}
					}

					tabIter = simpleTableRepMap.values().iterator();
					while (tabIter.hasNext()) {
						this.tabRefList.add(
							((TableRepresentation) tabIter.next()).getTabRef());
					}
				}

				// jcMAP' = jcMAP - jcMAPEqui
				if (simpleJoinConditionMap != null
					&& simpleJoinConditionMap.size() > 0) {
					if (this.joinConditionList != null
						&& this.joinConditionList.size() > 0) {
						Iterator iter = this.joinConditionList.iterator();
						SearchCondition searchCond;
						while (iter.hasNext()) {
							String key =
								((SearchCondition) iter.next()).toSqlString(
									SQLStatement.VENDOR_UNKNOWN);
							searchCond =
								(
									SearchCondition) simpleJoinConditionMap
										.remove(
									key);
							if (DevTrace.isOnDebugLevel(loc)) {
								DevTrace.debugInfo(
									loc,
									setWhereClause,
									"Key "
										+ key
										+ ((searchCond == null) ? " not" : "")
										+ " removed from simple join condition map.");
							}
						}
					}
				}

				// COMPOSED
			} else {
				DevTrace.debugInfo(
					loc,
					setWhereClause,
					"current where node is composed.");
				composedList = whereNode.getComposed();
				// trMAP' = MERGE(trMapEqui, trMAP1, ... trMAPn)
				for (int i = 0; i < composedList.size(); i++) {
					HashMap tableRepMap =
						((WhereNodeElement) composedList.get(i))
							.getTableRepresentations();

					if (tableRepMap != null && tableRepMap.size() > 0) {
						Iterator tabIter = this.tabRefList.iterator();
						ejb21_TableReference tabRef;
						TableRepresentation tabRep;
						while (tabIter.hasNext()) {
							tabRef = (ejb21_TableReference) tabIter.next();
							String key =
								tabRef.toSqlString(
									SQLStatement.VENDOR_UNKNOWN);
							tabRep =
								(TableRepresentation) tableRepMap.remove(key);
							if (DevTrace.isOnDebugLevel(loc)) {
								DevTrace.debugInfo(
									loc,
									setWhereClause,
									"Key "
										+ key
										+ ((tabRep == null) ? " not" : "")
										+ " removed from table representation map "
										+ i
										+ ".");
							}
						}

						tabIter = tableRepMap.values().iterator();
						while (tabIter.hasNext()) {
							this.tabRefList.add(
								((TableRepresentation) tabIter.next())
									.getTabRef());
						}
					}
				}

				if (this.doCrossJoinOptimisation) {
					// remove double entries from MAP1 ... MAPn
					// for (int i = 0; i < n; i++) {
					//   MAPi = MAPi - this.MAP
					// }
					if (this.joinConditionList != null
						&& this.joinConditionList.size() > 0) {
						Iterator iter = this.joinConditionList.iterator();
						SearchCondition searchCond;
						while (iter.hasNext()) {
							String key =
								((SearchCondition) iter.next()).toSqlString(
									SQLStatement.VENDOR_UNKNOWN);
							for (int i = 0; i < composedList.size(); i++) {
								WhereNodeElement elem =
									(WhereNodeElement) composedList.get(i);
								HashMap joinConditionMap =
									elem.getJoinConditions();
								if (joinConditionMap != null
									&& joinConditionMap.size() > 0) {
									searchCond =
										(
											SearchCondition) joinConditionMap
												.remove(
											key);
									if (DevTrace.isOnDebugLevel(loc)) {
										DevTrace.debugInfo(
											loc,
											setWhereClause,
											"Key "
												+ key
												+ ((searchCond == null)
													? " not"
													: "")
												+ " removed from join condition map "
												+ i
												+ ".");
									}
								}
							}
						}
					}
				} else {
					// jcMAP' = MERGE(jcMapEqui, jcMAP1, ... jcMAPm)
					for (int i = 0; i < composedList.size(); i++) {
						HashMap joinConditionMap =
							((WhereNodeElement) composedList.get(i))
								.getJoinConditions();

						if (joinConditionMap != null
							&& joinConditionMap.size() > 0) {
							Iterator<SearchCondition> condIter =
								this.joinConditionList.iterator();
							SearchCondition searchCond;
							while (condIter.hasNext()) {
								String key =
									(
										condIter
											.next())
											.toSqlString(
										 SQLStatement.VENDOR_UNKNOWN);
								searchCond =
									(SearchCondition) joinConditionMap.remove(
										key);
								if (DevTrace.isOnDebugLevel(loc)) {
									DevTrace.debugInfo(
										loc,
										setWhereClause,
										"Key "
											+ key
											+ ((searchCond == null)
												? " not"
												: "")
											+ " removed from join condition map "
											+ i
											+ ".");
								}
							}

							condIter = joinConditionMap.values().iterator();
							while (condIter.hasNext()) {
								this.joinConditionList.add(condIter.next());
							}
						}
					}
				}
			}
		}

		// compute size of ANDs
		SearchCondition finalSearchCondition = null;
		int andCount = this.joinConditionList.size();
		andCount += (simpleJoinConditionMap != null)
			? simpleJoinConditionMap.size()
			: 0;
		if (simpleSearchCondition != null
			&& !(simpleSearchCondition instanceof NeutralCondition)) {
			andCount++;
		} else if (composedList != null && composedList.size() > 0) {
			if (this.doCrossJoinOptimisation) {
				finalSearchCondition = buildBooleanOr(composedList);
			} else {
				List<SearchCondition> scAndList = new ArrayList<SearchCondition>();
				for (int i = 0; i < composedList.size(); i++) {
					SearchCondition scElem =
						((WhereNodeElement) composedList.get(i)).getCondition();
					if (scElem != null
						&& !(scElem instanceof NeutralCondition)) {
						scAndList.add(scElem);
					}
				}
				if (scAndList.size() > 0) {
					SearchCondition[] scAndArray =
						new SearchCondition[scAndList.size()];
					scAndList.toArray(scAndArray);
					finalSearchCondition = BUILDER.createBooleanAnd(scAndArray).build();
				}
			}
			if (finalSearchCondition != null) {
				andCount++;
			}
		}

		SearchCondition[] booleanAndList = new SearchCondition[andCount];

		// jcMAPEqui
		int andIndex = 0;
		for (; andIndex < this.joinConditionList.size(); andIndex++) {
			booleanAndList[andIndex] =
				this.joinConditionList.get(andIndex);
		}

		if (whereNode != null) {
			if (whereNode.getType() == WhereNode.SIMPLE) {
				// AND MAP
				if (simpleJoinConditionMap != null
					&& simpleJoinConditionMap.size() > 0) {
					Iterator iter = simpleJoinConditionMap.values().iterator();
					while (iter.hasNext()) {
						booleanAndList[andIndex] =
							(SearchCondition) iter.next();
						andIndex++;
					}
				}

				// AND SC
				if (simpleSearchCondition != null
					&& !(simpleSearchCondition instanceof NeutralCondition)) {
					booleanAndList[andIndex] = simpleSearchCondition;
				}
			} else {
				// COMPOSED_OR:
				// AND (MAP1 AND SC1 OR MAP2 AND SC2 OR ... OR MAPn AND SCn)
				// 
				// COMPOSED_AND:
				// AND (SC1 AND ... AND SCm)
				if (finalSearchCondition != null) {
					booleanAndList[andIndex] = finalSearchCondition;
				}
			}
		}

		// this.SC = ...
		this.searchCondition =
			(andCount > 0) ? BUILDER.createBooleanAnd(booleanAndList).build() : null;

		this.selectListSizeBeforeOrderByProcessing = this.selectList.size();

		DevTrace.exiting(loc, setWhereClause);
	}

	/**
	 * Adds columns to the order by list of the SQLStatement.
	 * Eventuell existing relations are recognized. If an order
	 * by column is not already contained in the select list
	 * (this is the case, if the order by column's positions
	 * is greater than the select list size), this column is added
	 * to the select list columns.
	 */
	void addToOrderByList(
		boolean isDescending,
		BeanField beanField,
		BeanField[] relation)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{ new Boolean(isDescending), beanField, relation };
			DevTrace.entering(
				loc,
				addToOrderByList,
				addToOrderByListParms,
				inputValues);
		}

		if (beanField.isRelation() == true) {
			DevTrace.exitingWithException(loc, addToOrderByList);
			throw new SQLMappingException(
				"A CMR Field is not allowed in an order by clause",
				"In the EJB QL parsed tree, SQL mapper has found an "
					+ "order by clause where a CMR field with name "
					+ beanField.getFieldName()
					+ " and type "
					+ beanField.getType()
					+ " exists. "
					+ "This is not allowed according to the 2.1 EJB QL standard.",
				"CSM000");
		}

		CMPFieldDescriptor fd =
			this.orMapping.getCMPBeanField(
				beanField.getParentType(),
				beanField.getFieldName());

		if (!CommonTypes.isComparable(fd.getJdbcType())) {
			throw new SQLMappingException(
				"Jdbc type " + fd.getJdbcType() + " is not comparable.",
				"SQL Mapper has found an incomparable jdbc type in an order by clause."
					+ " The affected field is "
					+ beanField.getFieldName()
					+ " of bean type "
					+ beanField.getParentType()
					+ ". The mapped database column name is "
					+ fd.getColumnName().getName(this.isForNativeUseOnly)
					+ ".",
				"CSM040");
		}

		DatabaseColumn columnName = fd.getColumnName();
		String columnQualifier =
			(relation != null)
				? this.recognizeRelation(relation, true, false)
				: this.getAliasForIdentifier(beanField.getParentName());

		//check if column has already be added to order by list
		//the parser does not forbid ORDER BY o.name, o.name
		Iterator orderByListIter = this.orderByList.iterator();
		int counter = 0;
		while (orderByListIter.hasNext()) {
			ejb21_SortSpecification orderByItem =
				(ejb21_SortSpecification) orderByListIter.next();
			if (orderByItem
				.getColumnName()
				.equals(columnName.getName(this.isForNativeUseOnly))) {
				break;
			}
			counter++;
		}

		if (counter == this.orderByList.size()) {
			// column is not already included in order by list
			boolean addColumToSelectList = false;
			if (this.resultDescriptorList.get(0).isObjectRepresentation()) {
				if (this.selectListSizeBeforeOrderByProcessing
					< fd.getColumnPosition()) {
					addColumToSelectList = true;
				}
			} else {
				SelectSublist selectItem = this.selectList.get(0);
				ValueExpression selectItemValue = selectItem.getValue();
				if (!(selectItemValue instanceof ejb21_ColumnReference)) {
					throw new SQLMappingException(
						"The first select list value is not a ejb21_ColumnReference.",
						"SQL Mapper has found a select list value at first position in the"
							+ " assembled select list which is not a ejb21_ColumnReference. This is not possible"
							+ " when an order by sort specification is processed by the SQL Mapper."
							+ " Please kindly open a problem ticket for SAP on component BC-JAS-PER-DBI.",
						"CSM080");
				} else {
					ejb21_ColumnReference selectColumn =
						(ejb21_ColumnReference) selectItemValue;
					if (!(selectColumn
						.getColumnName()
						.equals(
							columnName.getName(
								this.isForNativeUseOnly
									|| selectColumn.isNative())))) {
						addColumToSelectList = true;
					}
				}
			}

			if (addColumToSelectList) {
				if (DevTrace.isOnDebugLevel(loc)) {
					DevTrace.debugInfo(
						loc,
						addToOrderByList,
						"add column "
							+ columnQualifier
							+ "."
							+ columnName.getName(this.isForNativeUseOnly)
							+ "to select list.");
				}
				selectList.add(
                        BUILDER.createSelectSublist().setValue(new ejb21_ColumnReference(columnQualifier,
						columnName.getName(this.isForNativeUseOnly),
						this.isForNativeUseOnly || columnName.isNative())).build());
			}

			orderByList.add(
				new ejb21_SortSpecification(
					columnName.getName(this.isForNativeUseOnly),
					this.isForNativeUseOnly || columnName.isNative(),
					isDescending));
		}

		DevTrace.exiting(loc, addToOrderByList);
	}

	/**
	 * Finalise the assembling of the order by clause. Nothing to do here. 
	 */
	void setOrderByClause() {
	}

	/**
	 * Adds column and expression pairs as SetClauses to the setList.
	 */
	void addToSetList(Object[] columns, Object[] expressions) {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { columns, expressions };
			DevTrace.entering(
				loc,
				addToSetList,
				addToSetListParms,
				inputValues);
		}

		for (int i = 0; i < columns.length; i++) {
			this.updateSetList.add(
                    BUILDER.createSetClause(
					(ejb21_ColumnReference) ((ExpressionNode) columns[i])
						.getExpression(),
					((ExpressionNode) expressions[i]).getExpression()).build());
		}

		DevTrace.exiting(loc, addToSetList);
	}

	/**
	 * Finalise the assembling of the set list. Nothing to do here. 
	 */
	void setSetList() {
	}

	/**
	 * Adds columns and expressions to the insertColumnList
	 * and to the insertExpressionList.
	 */
	void addToValueList(Object[] columns, Object[] expressions) {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { columns, expressions };
			DevTrace.entering(
				loc,
				addToValueList,
				addToValueListParms,
				inputValues);
		}

		for (int i = 0; i < expressions.length; i++) {
			this.insertValueList.add(
				((ExpressionNode) expressions[i]).getExpression());
		}

		for (int i = 0; i < columns.length; i++) {
			this.insertColumnList.add(
				(ejb21_ColumnReference) ((ExpressionNode) columns[i])
					.getExpression());
		}

		DevTrace.exiting(loc, addToValueList);
	}

	/**
	 * Finalise the assembling of the set list. Nothing to do here. 
	 */
	void setValueList() {
	}

	/**
	 * Finalise the assembling of the SQLStatement at all. 
	 * If the flag countOutside is true,
	 * a CountAll is wrapped around the already assembled SQLStatement.
	 * A CommonInputdescriptor array is created from the inputDescriptorList
	 * and a CommonResultDescriptor array is created from the resultDescriptorList.
	 */
	void endOfStatement() throws SQLMappingException {

		DevTrace.entering(loc, endOfStatement, null, null);

		Query[] tabRefListArray = new Query[this.tabRefList.size()];
		this.tabRefList.toArray(tabRefListArray);

		if (this.modifyMode == 0) {
			SelectSublist[] selectSublistArray =
				new SelectSublist[this.selectList.size()];
			this.selectList.toArray(selectSublistArray);

			CommonResultDescriptor rd;

			if ((this.forUpdate == true)
				&& (this.resultDescriptorList.size() > 0)
				&& ((rd = this.resultDescriptorList.get(0)).isObjectRepresentation())
				&& (this.orMapping.getBeanPK(rd.getEjbName()).getUpdateFlag())) {

				DevTrace.debugInfo(
					loc,
					endOfStatement,
					"forUpdate encountered");

				if (tabRefListArray.length != 1) {
					throw new SQLMappingException(
						"FOR UPDATE not admissible for queries refering"
							+ " to more than one database table.",
						"SQL Mapper has encountered a forUpdate request,"
							+ " but the number of ejb21_TableReferences"
							+ " detected in the from clause is "
							+ tabRefListArray.length
							+ " rather than 1, as expected.",
						"CSM054");
				}

				if (!(tabRefListArray[0] instanceof ejb21_TableReference)) {
					throw new SQLMappingException(
						"FOR UPDATE not admissible for queries in turn"
							+ " containing subqueries.",
						"When mapping query to the database SQL Mapper"
							+ " had to create an SQL statement containing a subquery."
							+ " Hence the forUpdate request received is not"
							+ " admissible for this query.",
						"CSM056");
				}

				if (this.distinct == true) {
					throw new SQLMappingException(
						"FOR UPDATE not admissible for DISTINCT queries.",
						"SQL Mapper has encountered a forUpdate request"
							+ " along with a distinct query.",
						"CSM058");
				}

				if (this.countOutside == true) {
					throw new SQLMappingException(
						"FOR UPDATE not admissible for queries in turn"
							+ " containing subqueries.",
						"When mapping a query containing a count aggregate"
							+ " to the database SQL Mapper"
							+ " had to create an SQL statement containing a subquery."
							+ " Hence the forUpdate request received is not"
							+ " admissible for this query.",
						"CSM060");
				}

				if (this.orderByList.size() != 0) {
					throw new SQLMappingException(
						"FOR UPDATE not admissible containing an order by clause.",
						"SQL Mapper has encountered a forUpdate request,"
							+ " for a query containing "
							+ this.orderByList.size()
							+ " order by columns.",
						"CSM062");
				}

				this.selectStatement =
                    BUILDER.createSelectForUpdateStatement((ejb21_TableReference) tabRefListArray[0],
                            this.searchCondition).setSelectList(selectSublistArray).build();
			} else {

				if (this.countOutside) {
					DevTrace.debugInfo(
						loc,
						endOfStatement,
						"countOutside encountered");

					SelectSublist[] selCountStar = new SelectSublist[1];
					selCountStar[0] = BUILDER.createSelectSublist().setValue(BUILDER.createCountAll().build()).build();

					if (tabRefListArray.length == 1) {
						//select count(*) from <table> ...
						this.selectStatement =
                            BUILDER.createSelectStatement(
                                    BUILDER.createQuerySpecification(tabRefListArray)
                                    .setDistinct(this.distinct)
                                    .setSelectList(selCountStar)
                                    .build()).build();
					} else {
						// wrap count(*) around
						// select count(*) from (select <pk> from <tables> ...) 

						QuerySpecification helpQuery =
                            BUILDER.createQuerySpecification(tabRefListArray).setDistinct(this.distinct).setSelectList(selectSublistArray)
                            .setWhereClause(this.searchCondition).build();

						Query[] queryCountStar = new Query[1];
						queryCountStar[0] = BUILDER.createDerivedTable(helpQuery, this.getNewAliasName()).build();
						this.selectStatement = BUILDER.createSelectStatement(
                                BUILDER.createQuerySpecification(queryCountStar)
                                .setSelectList(selCountStar)
                                .build()).build();
					}
				} else {
					this.querySpecification =
                        BUILDER.createQuerySpecification(tabRefListArray).setDistinct(this.distinct)
                        .setSelectList(selectSublistArray)
                        .setWhereClause(this.searchCondition).build();

					SortSpecification[] orderByListArray =
						new SortSpecification[this.orderByList.size()];
					this.orderByList.toArray(orderByListArray);
					this.selectStatement = BUILDER.createSelectStatement(this.querySpecification).setSortSpecifications(orderByListArray).build();
				}
			}
		} else {
			if (tabRefListArray.length != 1
				|| !(tabRefListArray[0] instanceof ejb21_TableReference)) {

				if (tabRefListArray.length != 1) {
					throw new SQLMappingException(
						"UPDATE/INSERT/DELETE statement not admissible"
							+ " for more than one table.",
						"SQL Mapper has encountered a UPDATE/INSERT/DELETE statement,"
							+ " but the number of ejb21_TableReferences"
							+ " detected in the table list is "
							+ tabRefListArray.length
							+ " rather than 1, as expected.",
						"CSM068");
				} else {
					throw new SQLMappingException(
						"UPDATE/INSERT/DELETE statement not admissible"
							+ " for a subquery in the table list.",
						"SQL Mapper has encountered a UPDATE/INSERT/DELETE statement"
							+ " but the first element of the table list was not"
							+ " a ejb21_TableReference (it was most probably a subquery)."
							+ " This would yield to a not admissible statement.",
						"CSM070");
				}
			}

			switch (this.modifyMode) {

				case UPDATE :
					SetClause[] setClauseArray =
						new SetClause[this.updateSetList.size()];
					this.updateSetList.toArray(setClauseArray);
					this.modifyStatement = BUILDER.createUpdateStatement((ejb21_TableReference) tabRefListArray[0], setClauseArray)
					    .setWhereClause(searchCondition).build();
					break;

				case INSERT :
					ColumnReference[] insertColumnArray =
						new ColumnReference[this
							.insertColumnList
							.size()];
					this.insertColumnList.toArray(insertColumnArray);
					RowValueElement[] insertValueArray =
						new RowValueElement[this.insertValueList.size()];
					this.insertValueList.toArray(insertValueArray);
                    final RowValueElementList insertList = BUILDER.createRowValueElementList(insertValueArray).build();
					this.modifyStatement = BUILDER.createInsertStatement((ejb21_TableReference) tabRefListArray[0], BUILDER.createTableValue(insertList).build()).setColumns(insertColumnArray).build();
					break;

				case DELETE :
					this.modifyStatement = BUILDER.createDeleteStatement((ejb21_TableReference) tabRefListArray[0]).setWhereClause(this.searchCondition).build();
					break;

				default :
					// not possible
					break;
			}
		}

		if (this.nativeTableNameEncountered) {
			DevTrace.debugInfo(
				loc,
				endOfStatement,
				"at least one native table name encountered, set isForNativeUseOnly");
			this.isForNativeUseOnly = true;
		}

		CommonInputDescriptor[] inputDescriptorArray =
			new CommonInputDescriptor[this.inputDescriptorList.size()];
		this.inputDescriptorList.toArray(inputDescriptorArray);
		this.inputDescriptor = inputDescriptorArray;

		CommonResultDescriptor[] resultDescriptorArray =
			new CommonResultDescriptor[this.resultDescriptorList.size()];
		this.resultDescriptorList.toArray(resultDescriptorArray);
		this.resultDescriptor = resultDescriptorArray;

		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.debugInfo(
				loc,
				endOfStatement,
				"compiled statement = {"
					+ ((this.modifyMode == 0)
						? this.selectStatement.toSqlString(
							SQLStatement.VENDOR_UNKNOWN)
						: this.modifyStatement.toSqlString(
							SQLStatement.VENDOR_UNKNOWN))
					+ "}");
		}

		DevTrace.exiting(loc, endOfStatement);
	}

	/**
	 * Registers InputParameterDefinitions, creates CommonInputDescriptor objects
	 * and returns an InputParameterOccurrence object.
	 */
	InputParameterOccurrence registerInputParameter(InputParameterDefinition parameterDefinition)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { parameterDefinition };
			DevTrace.entering(
				loc,
				registerInputParameter,
				registerInputParameterParms,
				inputValues);
		}
		int firstIndex = this.inputDescriptorList.size();
		int count = 1;
		int number = parameterDefinition.getNumber();
		String beanType = parameterDefinition.getType();
		boolean isDV = parameterDefinition.isDependentValue();
		boolean isBean = parameterDefinition.isEntityBean();

		if (!isBean) {
			DevTrace.debugInfo(
				loc,
				registerInputParameter,
				"input parameter does not represent a bean type.");
			if (!isDV) {
				inputDescriptorList.add(
					new CommonInputDescriptor(
						inputDescriptorList.size() + 1,
						this.mapTypeToJdbcType(beanType),
						number,
						false,
						null,
						null,
						false,
						null,
						false,
						null,
						null));
			} else {
				// is DV
				DevTrace.exitingWithException(loc, registerInputParameter);
				throw new SQLMappingException(
					"The input parameter "
						+ number
						+ " refers to a DV Field of type "
						+ beanType
						+ ". This is currently not supported by the SQL mapper.",
					"Please contact the SAP support mentioning id CSM036.",
					"CSM036");

				/**
				 * possible solution; request beanType bei DVInfo:
				 * DVFieldDescriptor fd = this.orMapping.getDVBeanField(beanType);
				 * String[] subFieldNames = fd.getSubFieldNames();
				 * int[] subJdbcTypes = fd.getJdbcTypes();
				 * count = subFieldNames.length;
				 * for (int i = 0; i < count; i++) {
				 *		inputDescriptorList.add(
				 *			new CommonInputDescriptor(
				 *				inputDescriptorList.size() + 1,
				 *				subJdbcTypes[i],
				 *				number,
				 *				false,
				 *				null,
				 *				null,
				 *				true,
				 *				subFieldNames[i],
				                 *                              false,
				                 *                              null,
				                 *                              null));
				 *	}
				 */
			}

		} else if (!isDV) {
			// is bean
			count =
				this.createInputDescriptorRepresentingBean(
					parameterDefinition,
					InputParameter.PRIMARY_KEY);
		} else {
			// is dv
			DevTrace.debugInfo(
				loc,
				registerInputParameter,
				"input parameter represents a dv field of a bean type.");
			BeanField beanField =
				parameterDefinition.getDependentValueDescription();
			String fieldName = beanField.getFieldName();
			String parentType = beanField.getParentName();
			DVFieldDescriptor fd =
				this.orMapping.getDVBeanField(parentType, fieldName);
			String[] subFieldNames = fd.getSubFieldNames();
			int[] subJdbcTypes = fd.getJdbcTypes();
			count = subFieldNames.length;
			for (int i = 0; i < count; i++) {
				inputDescriptorList.add(
					new CommonInputDescriptor(
						inputDescriptorList.size() + 1,
						subJdbcTypes[i],
						number,
						true,
						parentType,
						fieldName,
						true,
						subFieldNames[i],
						false,
						null,
						null));
			}
		}

		this.inputParameterList.put(
			new Integer(parameterDefinition.getNumber()),
			new InputParameter(parameterDefinition.getNumber(), count));
		InputParameterOccurrence rcInputParOcc =
			new InputParameterOccurrence(
				parameterDefinition.getNumber(),
				firstIndex);

		DevTrace.exiting(loc, registerInputParameter, rcInputParOcc);
		return rcInputParOcc;
	}

	/**
	 * Registers InputParameterDefinitions, creates CommonInputDescriptor objects
	 * and returns an InputParameterOccurrence object according to the given
	 * representing type (PRIMARY_KEY, ALL_FIELDS, NON_KEY_FIELDS).
	 * This method may only be called for InputParameterDefinition of input parameters
	 * with an abstract bean type. For input parameters with a primitve java type
	 * method registerInputParameter(InputParameterDefinition parameterDefinition) has
	 * to be called.
	 */
	InputParameterOccurrence registerInputParameter(
		InputParameterDefinition parameterDefinition,
		int representing)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{ parameterDefinition, new Integer(representing)};
			DevTrace.entering(
				loc,
				registerInputParameter,
				registerInputParameterParms2,
				inputValues);
		}
		if (!parameterDefinition.isEntityBean()) {
			throw new SQLMappingException(
				"Illegal call of method registerInputParameter(parameterDefinition, representing).",
				"Method registerInputParameter(parameterDefinition, representing) may only be called"
					+ " for InputParameterDefinitions of input parameters standing for an"
					+ " abstract bean type. This is an"
					+ " internal programming error within SQL"
					+ " mapper. Please kindly open a problem"
					+ " ticket for SAP on component BC-JAS-PER-DBI.",
				"CSM072");
		}

		int firstIndex = this.inputDescriptorList.size();
		int count =
			this.createInputDescriptorRepresentingBean(
				parameterDefinition,
				representing);

		this.inputParameterList.put(
			new Integer(parameterDefinition.getNumber()),
			new InputParameter(parameterDefinition.getNumber(), count));

		InputParameterOccurrence rcInputParOcc =
			new InputParameterOccurrence(
				parameterDefinition.getNumber(),
				firstIndex);

		DevTrace.exiting(loc, registerInputParameter, rcInputParOcc);
		return rcInputParOcc;
	}

	/**
	 * This method recognizes relations in a relation path.
	 * A relation path looks like id.cmr1.cmr2.cmr3,
	 * where cmr1 is a cmr field in a bean that is identified
	 * by the identification variable id. cmr1, cmr2 and
	 * cmr3 are cmr fields. cmr1 and cmr2 has to be single valued.
	 * cmr3 could be single or collection valued. In case of a collection
	 * valued cmr field as the last cmr field in the path, this
	 * field is not included in the processing; therefore
	 * includeLast has to be true in that case. In case of a single
	 * valued cmr field as the last cmr field in the path
	 * includeLast has to be true only if the cmr field has 
	 * no database columns (the foreign key is on the other side),
	 * elsewise includeLast hast to be false in that case.
	 * cmr<n> has the bean cmr<n-1> as his parent (n>1). A relation cmr<n>
	 * is already recognized when the relation path prefix
	 * of cmr<n> and cmr<n> has been contained in another already
	 * processed relation path. The relation path prefix
	 * of cmr2 in the above example is id.cmr1. So the relation cmr2
	 * is already recognized, when the sub-path
	 * id.cmr1.cmr2 has been contained in another already processed 
	 * relation path. If a relation is not already recognized a join
	 * condition is generated to represent this relation. This
	 * join is included in the resulting SQLStatement. 
	 * The last generated or retrieved alias name for a join table is returned
	 * by this method. The alias name is retrieved from the relationAliasMap,
	 * when the relation has already been recognized.
	 */
	String recognizeRelation(
		BeanField[] relation,
		boolean includeLast,
		boolean definableIdentifiers)
		throws SQLMappingException {
		// relation:  cmr1.cmr2.cmr3
		// cmr1 has parent with parent name as an identifier
		// cmr1, cmr2, cmr3 are singleValued
		// if cmr3 is collectionValued, includeLast must be false
		// up to the table list identifiers may occur that have not been
		// defined yet (definableIdentifiers == true)
		// afterwards they already must have been defined (definableIdentifiers == false)
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{
					relation,
					new Boolean(includeLast),
					new Boolean(definableIdentifiers)};
			DevTrace.entering(
				loc,
				recognizeRelation,
				recognizeRelationParms,
				inputValues);
		}

		if (relation == null) {
			DevTrace.exiting(loc, recognizeRelation, null);
			return null;
		}

		int count = relation.length;
		if (includeLast == false) {
			count--;
		}
		String identifier =
			(definableIdentifiers
				? this.mapIdentifierToAlias(relation[0].getParentName())
				: this.getAliasForIdentifier(relation[0].getParentName()));
		if (identifier == null) {
			DevTrace.exitingWithException(loc, recognizeRelation);
			throw new SQLMappingException(
				"Unrecognized identifier : " + relation[0].getParentName(),
				"No alias has been precomputed for identifier "
					+ relation[0].getParentName()
					+ ". This is an internal programming error "
					+ "of the SQL mapper or the EJB-QL parser. "
					+ "Please kindly open up a problem ticket on "
					+ "component BC-JAS-PER-DBI.",
				"CSM143");
		}
		String parentAlias = identifier;
		String actualAlias = identifier;
		String relationId = identifier;
		for (int i = 0; i < count; i++) {
			String actualName = relation[i].getFieldName();
			relationId = relationId + "." + actualName;
			parentAlias = actualAlias;
			actualAlias = this.relationAliasMap.get(relationId);
			if (actualAlias == null) {
				actualAlias = this.getNewAliasName();
				this.relationAliasMap.put(relationId, actualAlias);
				if (DevTrace.isOnDebugLevel(loc)) {
					DevTrace.debugInfo(
						loc,
						recognizeRelation,
						"new relation "
							+ relationId
							+ " (new alias: "
							+ actualAlias
							+ ") encountered.");
				}
				String actualBean = relation[i].getType();
				String parentBean = relation[i].getParentType();
				PKBeanDescriptor bd = this.orMapping.getBeanPK(actualBean);
				this.addToTableList(bd.getBeanTable(), actualAlias);
				CMRFieldDescriptor fd =
					this.orMapping.getCMRBeanField(parentBean, actualName);
				this.checkMultiplicity(
					fd.getMultiplicity(),
					SQLTreeNodeManager.singleValuedCMR,
					actualName);
				DatabaseColumn[] fkColumns = fd.getColumnNames();
				DatabaseColumn[] pkColumns = bd.getColumnNames();
				String fkQualifier = parentAlias;
				String pkQualifier = actualAlias;
				if (fkColumns.length == 0) {
					//single valued cmr has no foreign key db columns
					//take foreign key columns from other side
					fkColumns = fd.getColumnNamesOfOtherSide();
					PKBeanDescriptor bd2 = this.orMapping.getBeanPK(parentBean);
					pkColumns = bd2.getColumnNames();
					fkQualifier = actualAlias;
					pkQualifier = parentAlias;
				}

				for (int j = 0; j < fkColumns.length; j++) {
					this.joinConditionList.add(
                            BUILDER.createComparisonPredicate(
							new ejb21_ColumnReference(
								fkQualifier,
								fkColumns[j].getName(this.isForNativeUseOnly),
								this.isForNativeUseOnly
									|| fkColumns[j].isNative()),
							ComparisonOperator.EQUAL,
							new ejb21_ColumnReference(
								pkQualifier,
								pkColumns[j].getName(this.isForNativeUseOnly),
								this.isForNativeUseOnly
									|| pkColumns[j].isNative())).build());
				}
			}
		}

		DevTrace.exiting(loc, recognizeRelation, actualAlias);
		return actualAlias;
	}

	Relation computeRelation(BeanField[] relation, boolean includeLast)
		throws SQLMappingException {
		// relation:  cmr1.cmr2.cmr3
		// cmr1 has parent with parent name as an identifier
		// cmr1, cmr2, cmr3 are singleValued
		// if cmr3 is collectionValued, includeLast must be false
		// method is called only after table list has been finished,
		// so all identifiers must have been defined by now
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { relation, new Boolean(includeLast)};
			DevTrace.entering(
				loc,
				computeRelation,
				computeRelationParms,
				inputValues);
		}

		if (relation == null || relation.length == 0) {
			DevTrace.exiting(loc, computeRelation, null);
			return null;
		}

		HashMap<String, TableRepresentation> tableRepMap = null;
		HashMap<String, SearchCondition> joinConditionMap = new HashMap<String, SearchCondition>();
		ArrayList<TableAlias> aliasList = null;

		int count = relation.length;
		if (includeLast == false) {
			count--;
		}
		String identifier =
			this.getAliasForIdentifier(relation[0].getParentName());
		if (identifier == null) {
			DevTrace.exitingWithException(loc, computeRelation);
			throw new SQLMappingException(
				"Unrecognized identifier : " + relation[0].getParentName(),
				"No alias has been precomputed for identifier "
					+ relation[0].getParentName()
					+ ". This is an internal programming error "
					+ "of the SQL mapper or the EJB-QL parser. "
					+ "Please kindly open up a problem ticket on "
					+ "component BC-JAS-PER-DBI.",
				"CSM145");
		}
		String parentAlias = identifier;
		String actualAlias = identifier;
		TableAlias parentTableAlias = null;
		TableAlias actualTableAlias = null;
		String relationId = identifier;
		for (int i = 0; i < count; i++) {
			String actualName = relation[i].getFieldName();
			relationId = relationId + "." + actualName;
			parentAlias = actualAlias;
			parentTableAlias = actualTableAlias;
			String actualBean = relation[i].getType();
			String parentBean = relation[i].getParentType();
			PKBeanDescriptor bd = this.orMapping.getBeanPK(actualBean);

			actualAlias = this.relationAliasMap.get(relationId);
			boolean aliasObtained = false;
			if (actualAlias == null) {
				aliasObtained = true;
				actualAlias = this.getNewAliasName();
				this.relationAliasMap.put(relationId, actualAlias);
				if (DevTrace.isOnDebugLevel(loc)) {
					DevTrace.debugInfo(
						loc,
						computeRelation,
						"new relation "
							+ relationId
							+ " (new alias: "
							+ actualAlias
							+ ") encountered.");
				}
				if (this.doCrossJoinOptimisation) {
					this.addToTableList(bd.getBeanTable(), actualAlias);
				}
			}

			if (!this.doCrossJoinOptimisation) {
				if (tableRepMap == null) {
					tableRepMap = new HashMap<String, TableRepresentation>();
					aliasList = new ArrayList<TableAlias>();
				}
				actualTableAlias = 
					new TableAlias(actualAlias, actualAlias, aliasObtained);
				aliasList.add(actualTableAlias);
				ejb21_TableReference tableReference =
					new ejb21_TableReference(
						bd.getBeanTable().getName(this.isForNativeUseOnly),
						actualTableAlias,
						bd.getBeanTable().isNative()
							|| this.isForNativeUseOnly);
				TableRepresentation tableRep =
					new TableRepresentation(tableReference, actualBean, bd);
				tableRepMap.put(
					tableReference.toSqlString(SQLStatement.VENDOR_UNKNOWN),
					tableRep);
			}

			CMRFieldDescriptor fd =
				this.orMapping.getCMRBeanField(parentBean, actualName);
			this.checkMultiplicity(
				fd.getMultiplicity(),
				SQLTreeNodeManager.singleValuedCMR,
				actualName);
			DatabaseColumn[] fkColumns = fd.getColumnNames();
			DatabaseColumn[] pkColumns = bd.getColumnNames();
			String fkAlias = parentAlias;
			String pkAlias = actualAlias;
			TableAlias fkTableAlias = parentTableAlias;
			TableAlias pkTableAlias = actualTableAlias;
			boolean parentIsFK = true;

			if (fkColumns.length == 0) {
				//single valued cmr has no foreign key db columns
				//take foreign key columns from other side
				fkColumns = fd.getColumnNamesOfOtherSide();
				PKBeanDescriptor bd2 = this.orMapping.getBeanPK(parentBean);
				pkColumns = bd2.getColumnNames();
				fkAlias = actualAlias;
				pkAlias = parentAlias;
				fkTableAlias = actualTableAlias;
				pkTableAlias = parentTableAlias;
				parentIsFK = false;
			}

			for (int j = 0; j < fkColumns.length; j++) {
				ejb21_ColumnReference fkColumnReference = null;
				ejb21_ColumnReference pkColumnReference = null;

				if (this.doCrossJoinOptimisation) {
					fkColumnReference =
						new ejb21_ColumnReference(
							fkAlias,
							fkColumns[j].getName(this.isForNativeUseOnly),
							this.isForNativeUseOnly || fkColumns[j].isNative());
					pkColumnReference =
						new ejb21_ColumnReference(
							pkAlias,
							pkColumns[j].getName(this.isForNativeUseOnly),
							this.isForNativeUseOnly || pkColumns[j].isNative());
				} else {
					if (i == 0 && parentIsFK) {
						fkColumnReference =
							new ejb21_ColumnReference(
								fkAlias,
								fkColumns[j].getName(this.isForNativeUseOnly),
								this.isForNativeUseOnly
									|| fkColumns[j].isNative());
					} else {
						fkColumnReference =
							new ejb21_ColumnReference(
								fkTableAlias,
								fkColumns[j].getName(this.isForNativeUseOnly),
								this.isForNativeUseOnly
									|| fkColumns[j].isNative());
					}

					if (i == 0 && !parentIsFK) {
						pkColumnReference =
							new ejb21_ColumnReference(
								pkAlias,
								pkColumns[j].getName(this.isForNativeUseOnly),
								this.isForNativeUseOnly
									|| pkColumns[j].isNative());
					} else {
						pkColumnReference =
							new ejb21_ColumnReference(
								pkTableAlias,
								pkColumns[j].getName(this.isForNativeUseOnly),
								this.isForNativeUseOnly
									|| pkColumns[j].isNative());
					}
				}

				SearchCondition joinCondition =
					BUILDER.createComparisonPredicate(
						fkColumnReference,
						ComparisonOperator.EQUAL,
						pkColumnReference).build();
				joinConditionMap.put(
					joinCondition.toSqlString(SQLStatement.VENDOR_UNKNOWN),
					joinCondition);
			}
		}

		if (joinConditionMap.size() == 0) {
			joinConditionMap = null;
		}
		Relation computedRelation =
			new Relation(
				relationId,
				tableRepMap,
				joinConditionMap,
				aliasList,
				actualAlias);
		DevTrace.exiting(loc, computeRelation, computedRelation);
		return computedRelation;
	}

	/**
	 * This method checks a given multiplicity of an cmr field against
	 * a given cmr field multiplicity type. There are two of these types:
	 * collectionValuedCMR and singleValuedCMR. CollectionValued
	 * corresponds to a MANY_TO_MANY or to a ONE_TO_MANY relation.
	 * SingleValued corresponds to a MANY_TO_ONE or to a ONE_TO_ONE relation.
	 * If the check is not successful, an SQLMappingException is thrown.
	 */
	void checkMultiplicity(int multiplicity, int valued, String fieldName)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{ new Integer(multiplicity), new Integer(valued), fieldName };
			DevTrace.entering(
				loc,
				checkMultiplicity,
				checkMultiplicityParms,
				inputValues);
		}

		if (valued == SQLTreeNodeManager.collectionValuedCMR
			&& multiplicity != this.orMapping.MANY_TO_MANY
			&& multiplicity != this.orMapping.ONE_TO_MANY) {
			DevTrace.exitingWithException(loc, checkMultiplicity);
			throw new SQLMappingException(
				"The CMR Field " + fieldName + " is not collection valued.",
				"In the EJB QL parsed tree, SQL mapper has encountered a "
					+ "CMR field that is not collection valued "
					+ "(1:n or n:m relationship) where a "
					+ "collection valued field was expected. "
					+ "This is either a programming error in the EJB QL parser "
					+ "or an inconsistency between EJB QL parser and SQL mapper. "
					+ "Please make sure that you run consistent versions "
					+ "of EJB QL parser and SQL mapper.",
				"CSM012");
		}

		if (valued == SQLTreeNodeManager.singleValuedCMR
			&& multiplicity != this.orMapping.MANY_TO_ONE
			&& multiplicity != this.orMapping.ONE_TO_ONE) {
			DevTrace.exitingWithException(loc, checkMultiplicity);
			throw new SQLMappingException(
				"The CMR Field " + fieldName + " is not single valued.",
				"In the EJB QL parsed tree, SQL mapper has encountered a "
					+ "CMR field that is not single valued "
					+ "(1:1 or n:1 relationship) where a "
					+ "single valued field was expected. "
					+ "This is either a programming error in the EJB QL parser "
					+ "or an inconsistency between EJB QL parser and SQL mapper. "
					+ "Please make sure that you run consistent versions "
					+ "of EJB QL parser and SQL mapper.",
				"CSM014");
		}

		DevTrace.exiting(loc, checkMultiplicity);
	}

	/**
	 * Genereats new table alias names that are not contained in the
	 * set aliasesUsedByIdentifiers.
	 */
	String getNewAliasName() {
		String alias;
		for (;;) {
			alias = SQLTreeNodeManager.aliasPraefix + this.aliasCount;
			this.aliasCount++;
			if (!this.aliasesUsedByIdentifiers.contains(alias)) {
				break;
			}
		}
		return alias;
	}

	/**
	 * Creates a new ejb21_TableReference for given BeanTable and aliasName.
	 * If a non Open SQL table name is detected, flag nativeTableNameEncountered
	 * is set if this is not already the case.
	 */
	ejb21_TableReference createTableReferenceDetectNativeNames(
		CheckableIdentifier tableIdentifier,
		String aliasName) {

		boolean isNative = tableIdentifier.isNative();

		if (!this.isForNativeUseOnly && isNative) {
			this.nativeTableNameEncountered = true;
		}

		return new ejb21_TableReference(
			tableIdentifier.getName(this.isForNativeUseOnly),
			aliasName,
			this.isForNativeUseOnly || isNative);
	}

	/**
	 * Checks whether a given jdbc type is comparable. If not, an exception is
	 * thrown mentioning the given fieldName, beanName and columnName.
	 */
	void checkJdbcTypeForComparability(
		int jdbcType,
		String fieldName,
		String beanName,
		String columnName)
		throws SQLMappingException {

		if (!CommonTypes.isComparable(jdbcType)) {
			throw new SQLMappingException(
				"Jdbc type " + jdbcType + " is not comparable.",
				"SQL Mapper has encountered an incomparable jdbc type in a"
					+ " comparison predicate of a where clause."
					+ " The affected field is "
					+ fieldName
					+ " of bean type "
					+ beanName
					+ ". Assigned database column name is "
					+ columnName
					+ ".",
				"CSM042");

		}
	}

	/**
	 * Some get Methods.
	 */
	SQLStatement getSQLStatement() {
		if (this.modifyMode == 0) {
			return this.selectStatement;
		} else {
			return this.modifyStatement;
		}
	}

	CommonInputDescriptor[] getInputDescriptors() {
		return this.inputDescriptor;
	}

	CommonResultDescriptor[] getResultDescriptors() {
		return this.resultDescriptor;
	}

	boolean wasReturnCompleteBeansIgnored() {
		return this.useFullBeanDescriptorIgnored;
	}

	boolean isForNativeUseOnly() {
		return this.isForNativeUseOnly;
	}

	WhereNodeManager getWhereNodeManager() {
		return this.whereManager;
	}

	ExpressionNodeManager getExpressionNodeManager() {
		return this.expManager;
	}

	BooleanNodeManager getBooleanNodeManager() {
		return this.boolManager;
	}

	SubqueryBuilder getSubqueryBuilder() {
		return this.subqueryBuilder;
	}

	ORMappingManager getORMapping() {
		return this.orMapping;
	}

	Map<Integer,InputParameter> getInputParameterList() {
		return this.inputParameterList;
	}

	List<CommonInputDescriptor> getInputDescriptorList() {
		return this.inputDescriptorList;
	}

	boolean isFailsafeActive() {
		return this.failsafe;
	}

	/*
	 * Returns true, if SQL Statement is in principal
	 * cross join optimisable. This is the case, when
	 * the noEmptyTables flag is set and the query is specfied as
	 * distinct in method createQuerySpecification(). If the latter
	 * is not fulfilled but noEmptyTables is true, than the SQL
	 * Statement is still cross join optimsable, if there is
	 * only one table specified in the from clause and 
	 * the select clause is either 
	 * like "select object(o)"
	 * (in case of useFullBeanDescriptors == true, the selected table
	 * to which o is mapped, may not contain any column with an
	 * incomparable jdbc type) 
	 * or like "select o.cmp" or "select agr(o.cmp)", where
	 * cmp is the only existing primary key field of the table
	 * o is mapped to. The method returns false otherwise.   
	 */
	boolean isSQLStatementCrossJoinOptimisable() {
		return this.crossJoinOptimisationPossible;
	}

	/*
	 * Returns true if isSQLStatementCrossJoinOptimisable returns true
	 * and if the cross join optimisation has really be done
	 * so that the assembled SQL Statement is cross join optimised.
	 * If failsafe is true, this method will return false
	 * because in this case the cross join optimisation will not
	 * be performed.
	 */
	boolean isSQLStatementCrossJoinOptimised() {
		return this.doCrossJoinOptimisation;
	}

	/* Returns alias used for given identifier */
	String getAliasForIdentifier(String identifier) {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { identifier };
			DevTrace.entering(
				loc,
				getAliasForIdentifier,
				getAliasForIdentifierParms,
				inputValues);
		}
		String alias = this.aliasesReplacingIdentifiers.get(identifier);
		DevTrace.exiting(loc, getAliasForIdentifier, alias);
		return alias;
	}

	/* Returns alias for given identifier. If no alias has yet
	 * been defined, create a new one. If identifier is OpenSQL conform
	 * we use it as alias; elsewise a standard alias is created.
	 */
	private String mapIdentifierToAlias(String identifier) {
		String alias;

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { identifier };
			DevTrace.entering(
				loc,
				mapIdentifierToAlias,
				mapIdentifierToAliasParms,
				inputValues);
		}

		alias = this.getAliasForIdentifier(identifier);
		if (alias != null) {
			DevTrace.exiting(loc, mapIdentifierToAlias, alias);
			return alias;
		}

		if (ORMappingManager.isOpenSQLTableReference(identifier)) {
			alias = identifier;
		} else {
			alias = this.getNewAliasName();
		}

		this.aliasesReplacingIdentifiers.put(identifier, alias);

		DevTrace.exiting(loc, mapIdentifierToAlias, alias);
		return alias;
	}

	/**
	 * Maps a java type given as a String value to a jdbc type
	 * for the CommonSQLMapper. The common sql type mapping 
	 * is invoked here.
	 */
	private int mapTypeToJdbcType(String type) throws SQLMappingException {
		if (this.typeMap == null) { // fill type map
			this.typeMap = new HashMap<String, Integer>();
			this.typeMap.put(
				"boolean",
				new Integer(CommonTypes.defaultMapping(Short.class)));
			this.typeMap.put(
				"java.lang.Boolean",
				new Integer(CommonTypes.defaultMapping(Short.class)));
			this.typeMap.put(
				"java.lang.String",
				new Integer(CommonTypes.defaultMapping(String.class)));
			this.typeMap.put(
				"byte",
				new Integer(CommonTypes.defaultMapping(Short.class)));
			this.typeMap.put(
				"java.lang.Byte",
				new Integer(CommonTypes.defaultMapping(Short.class)));
			this.typeMap.put(
				"short",
				new Integer(CommonTypes.defaultMapping(Short.class)));
			this.typeMap.put(
				"java.lang.Short",
				new Integer(CommonTypes.defaultMapping(Short.class)));
			this.typeMap.put(
				"char",
				new Integer(CommonTypes.defaultMapping(String.class)));
			this.typeMap.put(
				"java.lang.Character",
				new Integer(CommonTypes.defaultMapping(String.class)));
			this.typeMap.put(
				"int",
				new Integer(CommonTypes.defaultMapping(Integer.class)));
			this.typeMap.put(
				"java.lang.Integer",
				new Integer(CommonTypes.defaultMapping(Integer.class)));
			this.typeMap.put(
				"long",
				new Integer(CommonTypes.defaultMapping(Long.class)));
			this.typeMap.put(
				"java.lang.Long",
				new Integer(CommonTypes.defaultMapping(Long.class)));
			this.typeMap.put(
				"float",
				new Integer(CommonTypes.defaultMapping(Float.class)));
			this.typeMap.put(
				"java.lang.Float",
				new Integer(CommonTypes.defaultMapping(Float.class)));
			this.typeMap.put(
				"double",
				new Integer(CommonTypes.defaultMapping(Double.class)));
			this.typeMap.put(
				"java.lang.Double",
				new Integer(CommonTypes.defaultMapping(Double.class)));
			this.typeMap.put(
				"Date",
				new Integer(
					CommonTypes.defaultMapping(java.sql.Timestamp.class)));
			this.typeMap.put(
				"java.util.Date",
				new Integer(
					CommonTypes.defaultMapping(java.sql.Timestamp.class)));
			this.typeMap.put(
				"Timestamp",
				new Integer(
					CommonTypes.defaultMapping(java.sql.Timestamp.class)));
			this.typeMap.put(
				"java.sql.Timestamp",
				new Integer(
					CommonTypes.defaultMapping(java.sql.Timestamp.class)));
			this.typeMap.put(
				"BigDecimal",
				new Integer(
					CommonTypes.defaultMapping(java.math.BigDecimal.class)));
			this.typeMap.put(
				"java.math.BigDecimal",
				new Integer(
					CommonTypes.defaultMapping(java.math.BigDecimal.class)));
		}

		Integer jdbcIntegerType = this.typeMap.get(type);
		if (jdbcIntegerType != null) {
			return jdbcIntegerType.intValue();
		} else {
			throw new SQLMappingException(
				"Unexpected java type " + type + " for jdbc type mapping.",
				"SQL Mapper do not know, who to map the java type "
					+ type
					+ " to a jdbc type.",
				"CSM016");
		}
	}

	/**
	 * Register input parameter of given parameterDefinition according to given
	 * representing type.
	 */
	private int createInputDescriptorRepresentingBean(
		InputParameterDefinition parameterDefinition,
		int representing)
		throws SQLMappingException {

		String registerTypeString = null;
		switch (representing) {
			case InputParameter.PRIMARY_KEY :
				registerTypeString = PRIMARY_KEY_TXT;
				break;
			case InputParameter.ALL_FIELDS :
				registerTypeString = ALL_FIELDS_TXT;
				break;
			case InputParameter.NON_KEY_FIELDS :
				registerTypeString = NON_KEY_FIELDS_TXT;
				break;
			default :
				registerTypeString = UNKNOWN_TXT;
				break;
		}

		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.debugInfo(
				loc,
				createInputDescriptorRepresentingBean,
				"input parameter represents a bean type,"
					+ " register "
					+ registerTypeString
					+ " as input parameters");
		}

		int number = parameterDefinition.getNumber();
		String beanType = parameterDefinition.getType();
		BeanDescriptor bd = this.orMapping.getFullBean(beanType);
		String[] beanFieldNames = bd.getFieldNames();
		Integer[] beanFieldJdbcTypes = bd.getJdbcTypes();
		Boolean[] beanFieldPKFlags = bd.getPKFlags();
		Boolean[] beanFieldDVFlags = bd.getDVFlags();
		String[] beanFieldSubFieldNames = bd.getSubFieldNames();
		Boolean[] beanFieldRelationFlags = bd.getRelationFlags();
		String[] beanFieldRelatedBeans = bd.getRelatedBeans();
		String[] beanFieldRelatedBeanFields = bd.getRelatedBeanFields();

		int count = 0;
		for (int i = 0; i < beanFieldJdbcTypes.length; i++) {
			boolean isKeyField = beanFieldPKFlags[i].booleanValue();
			boolean doAdd =
				((representing == InputParameter.ALL_FIELDS)
					|| (isKeyField && representing == InputParameter.PRIMARY_KEY)
					|| (!isKeyField
						&& representing == InputParameter.NON_KEY_FIELDS));
			if (doAdd) {
				inputDescriptorList.add(
					new CommonInputDescriptor(
						inputDescriptorList.size() + 1,
						beanFieldJdbcTypes[i].intValue(),
						number,
						true,
						beanType,
						beanFieldNames[i],
						beanFieldDVFlags[i].booleanValue(),
						beanFieldSubFieldNames[i],
						beanFieldRelationFlags[i].booleanValue(),
						beanFieldRelatedBeans[i],
						beanFieldRelatedBeanFields[i]));
				count++;
			}
		}

		return count;
	}

	HashMap mergeHashMaps(HashMap map1, HashMap map2) {
		//choose the map wich is the new one and which map will be removed

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { map1, map2 };
			DevTrace.entering(
				loc,
				mergeHashMaps,
				mergeHashMapsParms2,
				inputValues);
		}

		HashMap newMap = null;
		HashMap removeMap = null;

		if (map1 != null && map1.size() > 0) {
			newMap = map1;
			removeMap = map2;
		} else {
			newMap = map2;
			removeMap = map1;
		}

		// add entries of removeMap to newMap
		if (newMap != null) {
			if (removeMap != null) {
				newMap.putAll(removeMap);
			}
		}

		DevTrace.exiting(loc, mergeHashMaps, newMap);
		return newMap;
	}

	HashMap mergeHashMaps(HashMap map1, HashMap map2, HashMap map3) {
		//choose the map wich is the new one and which maps will be removed

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { map1, map2, map3 };
			DevTrace.entering(
				loc,
				mergeHashMaps,
				mergeHashMapsParms3,
				inputValues);
		}

		HashMap newMap = null;
		HashMap removeMapA = null;
		HashMap removeMapB = null;

		if (map1 != null && map1.size() > 0) {
			newMap = map1;
			removeMapA = map2;
			removeMapB = map3;
		} else if (map2 != null && map2.size() > 0) {
			newMap = map2;
			removeMapA = map1;
			removeMapB = map3;
		} else {
			newMap = map3;
			removeMapA = map1;
			removeMapB = map2;
		}

		// add entries of removeMapA and removeMapB to newMap
		if (newMap != null) {
			if (removeMapA != null) {
				newMap.putAll(removeMapA);
			}

			if (removeMapB != null) {
				newMap.putAll(removeMapB);
			}
		}
		DevTrace.exiting(loc, mergeHashMaps, newMap);
		return newMap;
	}

    HashMap<String, SearchCondition> intersectJoinMaps(HashMap<String, SearchCondition> actualMap, HashMap<String, SearchCondition> popMap) {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { actualMap, popMap };
			DevTrace.entering(
				loc,
				intersectJoinMaps,
				intersectJoinMapsParms,
				inputValues);
		}

		HashMap<String, SearchCondition> newMap = new HashMap<String, SearchCondition>();

		if (popMap != null && popMap.size() > 0) {
			if (actualMap != null && actualMap.size() > 0) {
				Iterator<String> iter = actualMap.keySet().iterator();
                SearchCondition compPred;
				// add keys contained in both maps to new map
				while (iter.hasNext()) {
					String key = iter.next();
					if (popMap.containsKey(key)) {
                        SearchCondition joinCondition = popMap.get(key);
						newMap.put(key, joinCondition);
					}
				}

				// remove keys contained in new map from the given maps
				if (newMap.size() > 0) {
					iter = newMap.keySet().iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						compPred = popMap.remove(key);
						if (DevTrace.isOnDebugLevel(loc)) {
							DevTrace.debugInfo(
								loc,
								intersectJoinMaps,
								"Key "
									+ key
									+ ((compPred == null) ? " not" : "")
									+ " removed from pop map.");
						}
						compPred = actualMap.remove(key);
						if (DevTrace.isOnDebugLevel(loc)) {
							DevTrace.debugInfo(
								loc,
								intersectJoinMaps,
								"Key "
									+ key
									+ ((compPred == null) ? " not" : "")
									+ " removed from pop map.");
						}
					}
				}
			}
		}

		DevTrace.exiting(
			loc,
			intersectJoinMaps,
			(newMap.size() > 0) ? newMap : null);
		return (newMap.size() > 0) ? newMap : null;
	}

	int intersectMaxJoinMaps(HashMap actualMap, ArrayList<WhereNodeElement> orList) {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { actualMap, orList };
			DevTrace.entering(
				loc,
				intersectMaxJoinMaps,
				intersectMaxJoinMapsParms,
				inputValues);
		}

		int index = -1;
		int max = 0;
		if (actualMap != null && actualMap.size() > 0) {
			for (int i = 0; i < orList.size(); i++) {
				int count = 0;
				WhereNodeElement elem = orList.get(i);
				HashMap joinConditionMap = elem.getJoinConditions();
				if (joinConditionMap != null && joinConditionMap.size() > 0) {
					Iterator<String> iter = joinConditionMap.keySet().iterator();
					while (iter.hasNext()) {
						String key = iter.next();
						if (actualMap.containsKey(key)) {
							count++;
						}
					}
				}
				if (count > max) {
					max = count;
					index = i;
				}
			}
		}

		DevTrace.exiting(loc, intersectMaxJoinMaps, new Integer(index));
		return index;
	}

    ArrayList appendArrayLists(ArrayList left, ArrayList right) {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { left, right };
			DevTrace.entering(
				loc,
				appendArrayLists,
				appendArrayListsParms2,
				inputValues);
		}

		ArrayList returnList = null;

		if (left != null) {
			if (right != null) {
				left.addAll(right);
			}
			returnList = left;
		} else {
			returnList = right;
		}

		DevTrace.exiting(loc, appendArrayLists, returnList);
		return returnList;
	}

    ArrayList appendArrayLists(
		ArrayList left,
		ArrayList middle,
		ArrayList right) {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { left, middle, right };
			DevTrace.entering(
				loc,
				appendArrayLists,
				appendArrayListsParms3,
				inputValues);
		}

		ArrayList returnList = null;
		if (left != null) {
			if (middle != null) {
				left.addAll(middle);
			}
			if (right != null) {
				left.addAll(right);
			}
			returnList = left;
		} else if (middle != null) {
			if (right != null) {
				middle.addAll(right);
			}
			returnList = middle;
		} else {
			returnList = right;
		}

		DevTrace.exiting(loc, appendArrayLists, returnList);
		return returnList;
	}

	SearchCondition buildExists(
		HashMap trMap,
		HashMap jcMap,
		ArrayList aliasList,
		SearchCondition sc)
		throws SQLMappingException {

		// compute new aliases if necessary
		// key: old Alias (id), value: new Alias
		HashMap<String, String> newAliases = new HashMap<String, String>();
		if (aliasList != null) {
			for (int i = 0; i < aliasList.size(); i++) {
				TableAlias tableAlias = (TableAlias) aliasList.get(i);
				if (!tableAlias.isGenuine()) {
					String key = tableAlias.getId();
					String aliasName = newAliases.get(key);
					if (aliasName == null) {
						aliasName = this.getNewAliasName();
						newAliases.put(key, aliasName);
					}
					tableAlias.setAlias(aliasName);
				} else {
					newAliases.put(tableAlias.getId(), tableAlias.getAlias());
				}
			}
		}

		if (trMap == null || trMap.size() == 0) {
			return sc;
		} else {
			return BUILDER.createExistsPredicate(this.subqueryBuilder.buildSubquery(trMap, jcMap, sc)).build();
		}
	}

	// (MAP1 AND SC1 OR MAP2 AND SC2 OR ... OR MAPn SCn)
	SearchCondition buildBooleanOr(ArrayList orList) {
		int orCount = 0;
		for (int i = 0; i < orList.size(); i++) {
			WhereNodeElement elem = (WhereNodeElement) orList.get(i);
			HashMap joinConditionMap = elem.getJoinConditions();
			SearchCondition searchCondition = elem.getCondition();
			if ((joinConditionMap != null && joinConditionMap.size() > 0)
				|| (searchCondition != null
					&& !(searchCondition instanceof NeutralCondition))) {
				orCount++;
			}
		}

		if (orCount > 0) {
			SearchCondition[] booleanOrList = new SearchCondition[orCount];
			int orIndex = 0;
			for (int i = 0; i < orList.size(); i++) {
				// MAPi AND SCi
				WhereNodeElement elem = (WhereNodeElement) orList.get(i);
				HashMap joinConditionMap = elem.getJoinConditions();
				SearchCondition searchCondition = elem.getCondition();

				// compute andCount
				int andCount =
					(joinConditionMap != null) ? joinConditionMap.size() : 0;
				if (searchCondition != null
					&& !(searchCondition instanceof NeutralCondition)) {
					andCount++;
				}

				SearchCondition[] andList = new SearchCondition[andCount];

				// MAPi
				int andIndex = 0;
				if (joinConditionMap != null && joinConditionMap.size() > 0) {
					Iterator iter = joinConditionMap.values().iterator();
					while (iter.hasNext()) {
						andList[andIndex] = (SearchCondition) iter.next();
						andIndex++;
					}
				}

				// AND SCi
				if (searchCondition != null
					&& !(searchCondition instanceof NeutralCondition)) {
					andList[andIndex] = searchCondition;
				}

				// OR
				if (andCount > 0) {
					booleanOrList[orIndex] = BUILDER.createBooleanAnd(andList).build();
					orIndex++;
				}
			}

			return BUILDER.createBooleanOr(booleanOrList).build();

		} else {

			return null;
		}
	}

	// (popRest AND popSC) OR (actualMAP AND actualSC)
	SearchCondition buildBooleanOr(
		HashMap popMap,
		SearchCondition popCondition,
		HashMap actualMap,
		SearchCondition actualCondition) {

		// popMAP AND popSC
		boolean skipNeutralTest = false;
		int count = (popMap != null) ? popMap.size() : 0;
		if (popCondition instanceof NeutralCondition) {
			// do not check if actualCondition will be NeutralCondition
			// possibly take NeutralConditon
			skipNeutralTest = true;
		} else {
			count++;
		}

		SearchCondition[] andPop = new SearchCondition[count];
		int i = 0;
		if (popMap != null && popMap.size() > 0) {
			Iterator iter = popMap.values().iterator();
			while (iter.hasNext()) {
				andPop[i] = (SearchCondition) iter.next();
				i++;
			}
		}

		if (!(popCondition instanceof NeutralCondition)) {
			andPop[i] = popCondition;
		}

		SearchCondition booleanAndPop =
			(andPop.length > 0) ? BUILDER.createBooleanAnd(andPop).build() : null;

		// actualMAP AND actualSC
		count = (actualMap != null) ? actualMap.size() : 0;
		if (skipNeutralTest
			|| !(actualCondition instanceof NeutralCondition)) {
			count++;
		}

		SearchCondition[] andActual = new SearchCondition[count];
		i = 0;
		if (actualMap != null && actualMap.size() > 0) {
			Iterator iter = actualMap.values().iterator();
			while (iter.hasNext()) {
				andActual[i] = (SearchCondition) iter.next();
				i++;
			}
		}
		if (skipNeutralTest
			|| !(actualCondition instanceof NeutralCondition)) {
			andActual[i] = actualCondition;
		}

        SearchCondition booleanAndActual =
			(andActual.length > 0) ? BUILDER.createBooleanAnd(andActual).build() : null;

		SearchCondition[] booleanOrList;
		if (booleanAndActual == null) {
			booleanOrList = new SearchCondition[] { booleanAndPop };
		} else if (booleanAndPop == null) {
			booleanOrList = new SearchCondition[] { booleanAndActual };
		} else {
			booleanOrList =
				new SearchCondition[] { booleanAndPop, booleanAndActual };
		}

		return BUILDER.createBooleanOr(booleanOrList).build();
	}

}
