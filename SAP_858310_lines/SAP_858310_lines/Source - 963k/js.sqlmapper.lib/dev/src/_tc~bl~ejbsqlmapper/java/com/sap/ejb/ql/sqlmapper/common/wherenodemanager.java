package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;

import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager;
import com.sap.ejb.ql.sqlmapper.common.ORMappingManager;
import com.sap.ejb.ql.sqlmapper.common.LogicOperator;
import com.sap.ejb.ql.sqlmapper.common.CMPFieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.CMRFieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.PKBeanDescriptor;
import com.sap.ejb.ql.sqlmapper.common.BeanTable;
import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;
import com.sap.ejb.ql.sqlmapper.common.ComparativeOperator;
import com.sap.ejb.ql.sqlmapper.common.SubqueryBuilder;
import com.sap.ejb.ql.sqlmapper.common.CommonInputDescriptor;
import com.sap.ejb.ql.sqlmapper.common.InputParameter;
import com.sap.ejb.ql.sqlmapper.common.InputParameterOccurrence;
import com.sap.ejb.ql.sqlmapper.common.LiteralValue;
import com.sap.ejb.ql.sqlmapper.common.LiteralValueMapper;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

import com.sap.sql.tree.ComparisonOperator;
import com.sap.sql.tree.NeutralCondition;
import com.sap.sql.tree.Query;
import com.sap.sql.tree.RowValueElement;
import com.sap.sql.tree.SearchCondition;
import com.sap.sql.tree.SqlTreeBuilder;
import com.sap.sql.tree.TableAlias;
import com.sap.sql.tree.ValueExpression;
import com.sap.sql.tree.ejb21_ColumnReference;
import com.sap.sql.tree.builder.LikePredicateBuilder;
import com.sap.tc.logging.Location;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.HashMap;
import java.util.ArrayList;

import java.math.BigDecimal;

/**
 * This class assembles the where clause of an SQL representation
 * of an EJB-QL expression or an EJB load action.
 * For that purpose, the class is designed
 * as a push down automaton, that is controlled by the 
 * <code>Processor</code> classes via method calls.
 * </p><p>
 * The push down automaton disposes of one processing unit 
 * (<code>whereNode</code>) and one stack (<code>whereStack</code>).
 * Via its <code>makeNode()</code> methods one may create a new 
 * <code>SearchCondition</code> as a new whereNode
 * within the processing unit. If there has been another node already 
 * present in the processing unit that previous node is pushed on top
 * of the stack. With the <code>alterNode()</code> method one may
 * change the node currently in the processing unit by adding a monadic
 * operator. The <code>popNode()</code> functions pulls the top element
 * from the stack and combines it (in that order) with the node currently
 * in the processing unit by a dyadic operator; the result is placed into
 * the processing unit. 
 * </p><p>
 * The <code>SQLTreeNodeManager</code> invokes its methods
 * <code>checkWhereStackIsNull()</code> to verify, that the stack is empty
 * and <code>getWhereNode()</code> to retrieve the where clause.
 * </p><p>
 * An <code>WhereNodeManager</code> instance is created with an
 * <code>SQLTreeNodeManager</code> object as argument, from which it retrieves
 * its <code>ORMappingManager</code>, its input parameter list hash map
 * its <code>SubqueryBuilder<</code> and ist input descriptor array.
 * An assemblage of a where clause has to be started with the invokation
 * of the <code>prepare</code> method to fully initialize the 
 * <code>WhereNodeManager</code> instance.
 * </p><p>
 * Before a subsequent assemblage of a where clause a 
 * <code>WhereNodeManager</code> instance has to be reset 
 * by invocation of method <code>clear()</code>. 
 * 
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 * @see com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.ORMappingManager
 * @see com.sap.ejb.ql.sqlmapper.common.SubqueryBuilder
 * @see com.sap.sql.tree.SearchCondition
 * 
 **/

public class WhereNodeManager {

	private static final Location loc =
		Location.getLocation(WhereNodeManager.class);
	private static final String makeNode = "makeNode";
	private static final String makeNodeOpFieldRelParms[] =
		{ "operator", "beanField", "relation" };
	private static final String makeNodeOpInputParms[] =
		{ "operator", "inputParameterOccurrence" };
	private static final String makeNodeOpFieldRelLitInputInClauseParms[] =
		{
			"operator",
			"beanField",
			"relation",
			"literal",
			"inputParameterOccurrence" };
	private static final String makeNodeOpFieldRelLitLitParms[] =
		{ "operator", "beanField", "relation", "pattern", "escapeCharacter" };
	private static final String makeNodeOpFieldRelInputLitParms[] =
		{
			"operator",
			"beanField",
			"relation",
			"patternParameter",
			"escapeCharacter" };
	private static final String makeNodeOpFieldRelLitInputParms[] =
		{ "operator", "beanField", "relation", "pattern", "escapeParameter" };
	private static final String makeNodeOpFieldRelInputInputParms[] =
		{
			"operator",
			"beanField",
			"relation",
			"patternParameter",
			"escapeParameter" };
	private static final String makeNodeOpObjObjObjParms[] =
		{ "operator", "operand", "lowerBound", "upperBound" };
	private static final String makeNodeOpObjObjParms[] =
		{ "operator", "leftOperand", "rightOperand" };
	private static final String makeNodeOpBeanFieldRelParms[] =
		{ "operator", "beanObject", "targetBeanField", "targetRelation" };
	private static final String makeNodeOpFieldRelFieldRelParms[] =
		{
			"operator",
			"beanField",
			"relation",
			"targetBeanField",
			"targetRelation" };
	private static final String makeNodeOpInputFieldRelParms[] =
		{
			"operator",
			"inputParameterOccurrence",
			"targetBeanField",
			"targetRelation" };
	private static final String alterNode = "alterNode";
	private static final String alterNodeParms[] = { "monadicLogicOperator" };
	private static final String popNode = "popNode";
	private static final String popNodeParms[] = { "logicOperator" };
	private static final String prepareLike = "prepareLike";
	private static final String prepareLikeParms[] =
		{ "operator", "beanField", "relation" };
	private static final String prepareMember = "prepareMember";
	private static final String prepareMemberParms[] = { "operator" };

    private static final SqlTreeBuilder BUILDER = SqlTreeBuilder.getInstance();

	private SQLTreeNodeManager myManager = null;
	private ORMappingManager orMapping = null;
	private SubqueryBuilder subqueryBuilder = null;

	private Map<Integer,InputParameter> inputParameterList = null;
    private List<CommonInputDescriptor> inputDescriptorList = null;
	private Stack<WhereNode> whereStack = null;
	private WhereNode whereNode = null;

	private boolean cleared = true;

	/**
	 * Creates an <code>WhereNodeManager</code> instance with
	 * an <code>SQLTreeNodeManager</code> object as argument.
	 */
	WhereNodeManager(SQLTreeNodeManager aManager) {
		this.myManager = aManager;
		this.orMapping = myManager.getORMapping();
		this.subqueryBuilder = myManager.getSubqueryBuilder();
		this.whereStack = new Stack<WhereNode>();
	}

	/**
	 * Perpares an <code>WhereNodeManager</code> instance for a 
	 * subsequent assemblage of a where clause.
	 */
	void prepare() {
		if (!this.cleared) {
			this.clear();
		}
		this.cleared = false;
		this.inputParameterList = myManager.getInputParameterList();
		this.inputDescriptorList = this.myManager.getInputDescriptorList();
	}

	/** 
	 * Resets an <code>WhereNodeManager</code> instance for a 
	 * subsequent assemblage of a where clause.
	 */
	void clear() {
		this.whereStack.clear();
		this.whereNode = null;
		this.inputParameterList = null;
		this.inputDescriptorList = null;
		this.cleared = true;
	}

	/**
	 * Legal operators : NOT_NULL, IS_NULL, EMPTY, NOT_EMPTY.
	 * Additional arguments: BeanField (with relation path).
	 * If beanField is a cmp field, only NOT_NULL and IS_NULL is allowed.
	 * If beanField is a cmr field, NOT_NULL and IS_NULL is only allowed, if
	 * the cmr field is single valued, EMPTY and NOT_EMPTY is only allowed, if
	 * the cmr field is collection valued. In the later case the 
	 * SubqueryBuilder is invoked. In case of NOT_NULL, IS_NULL NullPredicates 
	 * are created.
	 */
	void makeNode(int operator, BeanField beanField, BeanField[] relation)
		throws SQLMappingException {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{ new Integer(operator), beanField, relation };
			DevTrace.entering(
				loc,
				makeNode,
				makeNodeOpFieldRelParms,
				inputValues);
		}

		if (beanField.isRelation() == false) {
			// cmr1.cmr2.cmr3.cmp
			DevTrace.debugInfo(loc, makeNode, "bean field is cmp.");
			if (operator != ComparativeOperator.IS_NULL
				&& operator != ComparativeOperator.NOT_NULL) {
				DevTrace.exitingWithException(loc, makeNode);
				throw new SQLMappingException(
					"Unexpected comparative operator as argument of "
						+ "makeNode(int, BeanField, BeanField[]) for "
						+ "a CMP bean field as second argument.",
					"The EJBQLTreeProcessor has called a makeNode method "
						+ "of the SQLTreeNodeManager with an unexpected "
						+ "ComparativeOperator "
						+ operator
						+ ".",
					"CSM004");
			}

			String columnQualifier = null;
			Relation computedRelation = null;
			HashMap tableRepMap = null;
			HashMap joinConditionMap = null;
			ArrayList aliasList = null;
			TableAlias tableAlias = null;

			if (relation == null || relation.length == 0) {
				columnQualifier =
					this.myManager.getAliasForIdentifier(
						beanField.getParentName());
				if (columnQualifier == null) {
					DevTrace.exitingWithException(loc, makeNode);
					throw new SQLMappingException(
						"Unrecognized identifier : "
							+ beanField.getParentName(),
						"No alias has been precomputed for identifier "
							+ beanField.getParentName()
							+ ". This is an internal programming error "
							+ "of the SQL mapper or the EJB-QL parser. "
							+ "Please kindly open up a problem ticket on "
							+ "component BC-JAS-PER-DBI.",
						"CSM123");
				}
			} else {
				computedRelation =
					this.myManager.computeRelation(relation, true);
				tableRepMap = computedRelation.getTableRepresentations();
				joinConditionMap = computedRelation.getJoinConditions();
				aliasList = computedRelation.getAliases();
				if (aliasList != null && aliasList.size() > 0) {
					tableAlias =
						(TableAlias) (aliasList.get(aliasList.size() - 1));
				}
				columnQualifier = computedRelation.getAliasName();
			}

			this.pushWhereNodeIfNotNull();
			boolean isNotNull = (operator == ComparativeOperator.NOT_NULL);
			String parentType = beanField.getParentType();
			String fieldName = beanField.getFieldName();
			if (beanField.isDependentValue()) {
				DevTrace.debugInfo(loc, makeNode, "bean field is dv.");
				DVFieldDescriptor fd =
					this.orMapping.getDVBeanField(parentType, fieldName);
				DatabaseColumn[] dvColumns = fd.getColumnNames();
				int count = dvColumns.length;
				SearchCondition[] nullPredicates = new SearchCondition[count];
				for (int i = 0; i < count; i++) {
					ejb21_ColumnReference columnReference = null;
					if (tableAlias == null) {
						columnReference =
							new ejb21_ColumnReference(
								columnQualifier,
								dvColumns[i].getName(
									this.myManager.isForNativeUseOnly()),
								this.myManager.isForNativeUseOnly()
									|| dvColumns[i].isNative());
					} else {
						columnReference =
							new ejb21_ColumnReference(
								tableAlias,
								dvColumns[i].getName(
									this.myManager.isForNativeUseOnly()),
								this.myManager.isForNativeUseOnly()
									|| dvColumns[i].isNative());
					}
					nullPredicates[i] = 
                        BUILDER.createNullPredicate(columnReference).setNotNull(isNotNull).build();
					DevTrace.debugInfo(
						loc,
						makeNode,
						"new NullPredicate created.");
				}
				this.whereNode =
					new WhereNode(
						WhereNode.SIMPLE,
						new WhereNodeElement(
							tableRepMap,
							joinConditionMap,
							aliasList,
							BUILDER.createBooleanAnd(nullPredicates).build()));
			} else {
				CMPFieldDescriptor fd =
					this.orMapping.getCMPBeanField(parentType, fieldName);
				ejb21_ColumnReference columnReference = null;
				if (tableAlias == null) {
					columnReference =
						new ejb21_ColumnReference(
							columnQualifier,
							fd.getColumnName().getName(
								this.myManager.isForNativeUseOnly()),
							this.myManager.isForNativeUseOnly()
								|| fd.getColumnName().isNative());
				} else {
					columnReference =
						new ejb21_ColumnReference(
							tableAlias,
							fd.getColumnName().getName(
								this.myManager.isForNativeUseOnly()),
							this.myManager.isForNativeUseOnly()
								|| fd.getColumnName().isNative());
				}

				this.whereNode =
					new WhereNode(
						WhereNode.SIMPLE,
						new WhereNodeElement(
							tableRepMap,
							joinConditionMap,
							aliasList,
                            BUILDER.createNullPredicate(columnReference).setNotNull(isNotNull).build()));
				DevTrace.debugInfo(loc, makeNode, "new NullPredicate created.");
			}

		} else {
			// cmr1.cmr2.cmr3			
			DevTrace.debugInfo(loc, makeNode, "bean field is cmr.");

			this.pushWhereNodeIfNotNull();
			String fieldName = beanField.getFieldName();
			String parentType = beanField.getParentType();
			String actualType = beanField.getType();
			CMRFieldDescriptor fieldDescr =
				this.orMapping.getCMRBeanField(parentType, fieldName);
			int multiplicity = fieldDescr.getMultiplicity();

			switch (operator) {
				case ComparativeOperator.IS_NULL :
				case ComparativeOperator.NOT_NULL :
					this.myManager.checkMultiplicity(
						multiplicity,
						SQLTreeNodeManager.singleValuedCMR,
						fieldName);
					// cmr3 is singleValued
					DatabaseColumn[] fkColumns = fieldDescr.getColumnNames();
					boolean includeLast =
						(operator == ComparativeOperator.NOT_NULL
							&& fkColumns.length == 0);

					String columnQualifier = null;
					Relation computedRelation = null;
					HashMap tableRepMap = null;
					HashMap joinConditionMap = null;
					ArrayList aliasList = null;
					TableAlias tableAlias = null;

					if (relation == null || relation.length == 0) {
						columnQualifier =
							this.myManager.getAliasForIdentifier(
								beanField.getParentName());
						if (columnQualifier == null) {
							DevTrace.exitingWithException(loc, makeNode);
							throw new SQLMappingException(
								"Unrecognized identifier : "
									+ beanField.getParentName(),
								"No alias has been precomputed for identifier "
									+ beanField.getParentName()
									+ ". This is an internal programming error "
									+ "of the SQL mapper or the EJB-QL parser. "
									+ "Please kindly open up a problem ticket on "
									+ "component BC-JAS-PER-DBI.",
								"CSM125");
						}
					} else {
						computedRelation =
							this.myManager.computeRelation(
								relation,
								includeLast);
						tableRepMap =
							computedRelation.getTableRepresentations();
						joinConditionMap = computedRelation.getJoinConditions();
						aliasList = computedRelation.getAliases();
						if (aliasList != null && aliasList.size() > 0) {
							tableAlias =
								(TableAlias) (aliasList
									.get(aliasList.size() - 1));
						}
						columnQualifier = computedRelation.getAliasName();
					}

					boolean isNotNull =
						(operator == ComparativeOperator.NOT_NULL);

					SearchCondition[] nullPredicates =
						new SearchCondition[fkColumns.length];
					for (int i = 0; i < fkColumns.length; i++) {
						ejb21_ColumnReference columnReference = null;
						if (tableAlias == null) {
							columnReference =
								new ejb21_ColumnReference(
									columnQualifier,
									fkColumns[i].getName(
										this.myManager.isForNativeUseOnly()),
									this.myManager.isForNativeUseOnly()
										|| fkColumns[i].isNative());
						} else {
							columnReference =
								new ejb21_ColumnReference(
									tableAlias,
									fkColumns[i].getName(
										this.myManager.isForNativeUseOnly()),
									this.myManager.isForNativeUseOnly()
										|| fkColumns[i].isNative());
						}
						nullPredicates[i] =
                            BUILDER.createNullPredicate(columnReference).setNotNull(isNotNull).build();
						DevTrace.debugInfo(
							loc,
							makeNode,
							"new NullPredicate created.");
					}

					if (fkColumns.length != 0) {
						this.whereNode =
							new WhereNode(
								WhereNode.SIMPLE,
								new WhereNodeElement(
									tableRepMap,
									joinConditionMap,
									aliasList,
                                    BUILDER.createBooleanAnd(nullPredicates).build()));
					} else {
						if (operator == ComparativeOperator.NOT_NULL) {
							this.whereNode =
								new WhereNode(
									WhereNode.SIMPLE,
									new WhereNodeElement(
										tableRepMap,
										joinConditionMap,
										aliasList,
                                        BUILDER.createNeutralCondition().build()));
							if (DevTrace.isOnDebugLevel(loc)) {
								DevTrace.debugInfo(
									loc,
									makeNode,
									"IS NOT NULL, fkColumns.length == 0, "
										+ "new NeutralCondition created");
							}
						} else {
							PKBeanDescriptor pkDescr =
								this.orMapping.getBeanPK(parentType);
							DatabaseColumn[] pkColumns =
								pkDescr.getColumnNames();
							RowValueElement[] colRefArray =
								new RowValueElement[pkColumns.length];
							for (int i = 0; i < colRefArray.length; i++) {

								if (tableAlias == null) {
									colRefArray[i] =
										new ejb21_ColumnReference(
											columnQualifier,
											pkColumns[i].getName(
												this
													.myManager
													.isForNativeUseOnly()),
											this.myManager.isForNativeUseOnly()
												|| pkColumns[i].isNative());
								} else {
									colRefArray[i] =
										new ejb21_ColumnReference(
											tableAlias,
											pkColumns[i].getName(
												this
													.myManager
													.isForNativeUseOnly()),
											this.myManager.isForNativeUseOnly()
												|| pkColumns[i].isNative());
								}
							}

							DatabaseColumn[] otherSideColumns =
								fieldDescr.getColumnNamesOfOtherSide();
							BeanTable beanTable =
								this.orMapping.getBeanTable(actualType);
							Query subQuery =
								this.subqueryBuilder.buildSubquery(
									otherSideColumns,
									beanTable);
							this.whereNode =
								new WhereNode(
									WhereNode.SIMPLE,
									new WhereNodeElement(
										tableRepMap,
										joinConditionMap,
										aliasList,
                                        BUILDER.createInSubqueryPredicate(
                                                BUILDER.createRowValueElementList(colRefArray).build(),
                                                subQuery).setNotIn(true).build()));
							if (DevTrace.isOnDebugLevel(loc)) {
								DevTrace.debugInfo(
									loc,
									makeNode,
									"IS NULL, fkColumns.length == 0, "
										+ "new InSubqueryPredicate created");
							}
						}
					}
					break;

				case ComparativeOperator.EMPTY :
				case ComparativeOperator.NOT_EMPTY :
					/**
					 * select object(o1) from order o1 where o1.items is empty
					 * -> order.items is empty yields to subquery
					 * -> where not exists (select items-pk from items, 
					 *                      where items.order = o1.pk)
					 */
					this.myManager.checkMultiplicity(
						multiplicity,
						SQLTreeNodeManager.collectionValuedCMR,
						fieldName);
					// cmr3 is collectionValued
					SearchCondition condition =
                        BUILDER.createExistsPredicate(
							this.subqueryBuilder.buildSubquery(
								relation,
								fieldDescr.hasHelperTable())).build();

					DevTrace.debugInfo(
						loc,
						makeNode,
						"new ExistsPredicate created.");

					if (operator == ComparativeOperator.EMPTY) {
						condition = BUILDER.createBooleanNot(condition).build();
					}

					this.whereNode =
						new WhereNode(
							WhereNode.SIMPLE,
							new WhereNodeElement(null, null, null, condition));
					break;

				default :
					DevTrace.exitingWithException(loc, makeNode);
					throw new SQLMappingException(
						"Illegal comparative operator as argument of "
							+ "makeNode(int, BeanField, BeanField[]). ",
						"The EJBQLTreeProcessor has called a makeNode method "
							+ "of the SQLTreeNodeManager with an unexpected "
							+ "ComparativeOperator "
							+ operator
							+ ".",
						"CSM006");
			}
		}

		DevTrace.exiting(loc, makeNode);
	}

	/**
	 * Legal operators : NOT_NULL, IS_NULL.
	 * Additional arguments: InputParameterOccurrence.
	 * NullPredicates with a HostVariable is created.
	 */
	void makeNode(
		int operator,
		InputParameterOccurrence inputParameterOccurrence)
		throws SQLMappingException {
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{ new Integer(operator), inputParameterOccurrence };
			DevTrace.entering(loc, makeNode, makeNodeOpInputParms, inputValues);
		}

		if (operator != ComparativeOperator.IS_NULL
			&& operator != ComparativeOperator.NOT_NULL) {
			DevTrace.exitingWithException(loc, makeNode);
			throw new SQLMappingException(
				"Illegal comparative operator as argument of "
					+ "makeNode(int, int). ",
				"The EJBQLTreeProcessor has called a makeNode method "
					+ "of the SQLTreeNodeManager with an unexpected "
					+ "ComparativeOperator "
					+ operator
					+ ".",
				"CSM018");
		}

		this.pushWhereNodeIfNotNull();
		boolean isNotNull = (operator == ComparativeOperator.NOT_NULL);
		InputParameter input =
			this.inputParameterList.get(
				Integer.valueOf(
					inputParameterOccurrence.getInputParameterNumber()));
		int firstPosition = inputParameterOccurrence.getOccurrence() + 1;
		int count = input.getCount();
		SearchCondition[] nullPredicateList = new SearchCondition[count];

		for (int i = 0; i < count; i++) {
			nullPredicateList[i] =
                BUILDER.createNullPredicate(
                        BUILDER.createHostVariable(firstPosition + i).build()).setNotNull(isNotNull).build();
			DevTrace.debugInfo(loc, makeNode, "new NullPredicate created.");
		}

		this.whereNode =
			new WhereNode(
				WhereNode.SIMPLE,
				new WhereNodeElement(
					null,
					null,
					null,
					(count == 1)
						? nullPredicateList[0]
						: BUILDER.createBooleanAnd(nullPredicateList).build()));

		DevTrace.exiting(loc, makeNode);
	}

	/**
	 * Legal operators : IN, NOT_IN;
	 * Additional arguments: BeanField (with relation path), array of LiteralValues and 
	 * array of InputParameterOccurrences.
	 * The bean field has to be a cmp field.
	 * An InListPredicate is created with Literals and HostVariables.
	 */
	void makeNode(
		int operator,
		BeanField beanField,
		BeanField[] relation,
		LiteralValue[] literal,
		InputParameterOccurrence[] inputParameterOccurrence)
		throws SQLMappingException {

		// developer trace
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{
					new Integer(operator),
					beanField,
					relation,
					literal,
					inputParameterOccurrence };
			DevTrace.entering(
				loc,
				makeNode,
				makeNodeOpFieldRelLitInputInClauseParms,
				inputValues);
		}

		// operator check
		if (operator != ComparativeOperator.IN
			&& operator != ComparativeOperator.NOT_IN) {
			DevTrace.exitingWithException(loc, makeNode);
			throw new SQLMappingException(
				"Illegal comparative operator as argument of "
					+ "makeNode(int, BeanField, BeanField[], LiteralValue[], Integer[]).",
				"The EJBQLTreeProcessor has called a makeNode method "
					+ "of the SQLTreeNodeManager with an unexpected "
					+ "ComparativeOperator "
					+ operator
					+ ".",
				"CSM020");
		}

		this.pushWhereNodeIfNotNull();

		String fieldName = beanField.getFieldName();
		String beanType = beanField.getParentType();
		boolean beanFieldIsBoolean = beanField.isBoolean();

		CMPFieldDescriptor fd =
			this.orMapping.getCMPBeanField(beanType, fieldName);
		int jdbcType = fd.getJdbcType();
		DatabaseColumn columnName = fd.getColumnName();

		this.myManager.checkJdbcTypeForComparability(
			jdbcType,
			fieldName,
			beanType,
			columnName.getName(this.myManager.isForNativeUseOnly()));

		String columnQualifier = null;
		Relation computedRelation = null;
		HashMap tableRepMap = null;
		HashMap joinConditionMap = null;
		ArrayList aliasList = null;
		TableAlias tableAlias = null;

		if (relation == null || relation.length == 0) {
			columnQualifier =
				this.myManager.getAliasForIdentifier(beanField.getParentName());
			if (columnQualifier == null) {
				DevTrace.exitingWithException(loc, makeNode);
				throw new SQLMappingException(
					"Unrecognized identifier : " + beanField.getParentName(),
					"No alias has been precomputed for identifier "
						+ beanField.getParentName()
						+ ". This is an internal programming error "
						+ "of the SQL mapper or the EJB-QL parser. "
						+ "Please kindly open up a problem ticket on "
						+ "component BC-JAS-PER-DBI.",
					"CSM127");
			}
		} else {
			computedRelation = this.myManager.computeRelation(relation, true);
			columnQualifier = computedRelation.getAliasName();
			tableRepMap = computedRelation.getTableRepresentations();
			joinConditionMap = computedRelation.getJoinConditions();
			aliasList = computedRelation.getAliases();
			if (aliasList != null && aliasList.size() > 0) {
				tableAlias = (TableAlias) (aliasList.get(aliasList.size() - 1));
			}
		}

		boolean isNotIn = (operator == ComparativeOperator.NOT_IN);

		ejb21_ColumnReference columnReference = null;
		if (tableAlias == null) {
			columnReference =
				new ejb21_ColumnReference(
					columnQualifier,
					columnName.getName(this.myManager.isForNativeUseOnly()),
					this.myManager.isForNativeUseOnly()
						|| columnName.isNative());
		} else {
			columnReference =
				new ejb21_ColumnReference(
					tableAlias,
					columnName.getName(this.myManager.isForNativeUseOnly()),
					this.myManager.isForNativeUseOnly()
						|| columnName.isNative());
		}

		List<ValueExpression> valueList = new ArrayList<ValueExpression>();

		for (int i = 0; i < literal.length; i++) {
			if (beanFieldIsBoolean) {
				if (!literal[i].isBoolean()) {
					DevTrace.exitingWithException(loc, makeNode);
					throw new SQLMappingException(
						"Bean field "
							+ beanField.getFieldName()
							+ " is of type boolean, but literalValue is not.",
						null,
						"CSM034");
				}

				literal[i] =
					LiteralValueMapper.mapBoolean(literal[i], jdbcType);
			}

			if (literal[i].isNumeric() == false) {
				valueList.add(
                        BUILDER.createStringLiteral(literal[i].getValue()).build());
			} else if (literal[i].isFloaty() == false) {
				if (literal[i].isDecimal()) {
					valueList.add(
                            BUILDER.createBigDecimalLiteral(new BigDecimal(literal[i].getValue())).build());
				} else {
					valueList.add(
                            BUILDER.createIntegerLiteral(literal[i].getValue()).build());
				}
			} else {
				float floatValue =
					(new Float(literal[i].getValue())).floatValue();
				valueList.add(BUILDER.createFloatLiteral(floatValue).build());
			}
		}

		for (int i = 0; i < inputParameterOccurrence.length; i++) {
			int occurrence = inputParameterOccurrence[i].getOccurrence();
			InputParameter input =
				this.inputParameterList.get(
					Integer.valueOf(
						inputParameterOccurrence[i].getInputParameterNumber()));
			CommonInputDescriptor id = this.inputDescriptorList.get(occurrence);
			id.setJdbcType(jdbcType);
			int firstPosition = occurrence + 1;
			int count = input.getCount();
			for (int j = 0; j < count; j++) {
				valueList.add(BUILDER.createHostVariable(firstPosition + j).build());
			}
		}

		ValueExpression[] valueExpressionArray =
			new ValueExpression[valueList.size()];
		valueList.toArray(valueExpressionArray);
		this.whereNode =
			new WhereNode(
				WhereNode.SIMPLE,
				new WhereNodeElement(
					tableRepMap,
					joinConditionMap,
					aliasList,
                    BUILDER.createInListPredicate(columnReference, valueExpressionArray).setNotIn(isNotIn).build()));

		DevTrace.debugInfo(loc, makeNode, "new InListPredicate created.");

		DevTrace.exiting(loc, makeNode);
	}

	/**
	 * Legal operators : LIKE, NOT_LIKE.
	 * Additional arguments: BeanField (with relation path), pattern (LiteralValue)
	 * escapeCharacter (LiteralValue).
	 * Private method prepareLike is invoked and a LikePredicate is created.
	 */
	void makeNode(
		int operator,
		BeanField beanField,
		BeanField[] relation,
		LiteralValue pattern,
		LiteralValue escapeCharacter)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{
					new Integer(operator),
					beanField,
					relation,
					pattern,
					escapeCharacter };
			DevTrace.entering(
				loc,
				makeNode,
				makeNodeOpFieldRelLitLitParms,
				inputValues);
		}

		Relation computedRelation =
			this.prepareLike(operator, beanField, relation);

		String fieldName = beanField.getFieldName();
		String beanType = beanField.getParentType();
		CMPFieldDescriptor fd =
			this.orMapping.getCMPBeanField(beanType, fieldName);
		DatabaseColumn columnName = fd.getColumnName();
		String columnQualifier = null;
		HashMap tableRepMap = null;
		HashMap joinConditionMap = null;
		ArrayList aliasList = null;
		TableAlias tableAlias = null;
		ejb21_ColumnReference columnReference = null;

		if (relation == null || relation.length == 0) {
			columnQualifier =
				this.myManager.getAliasForIdentifier(beanField.getParentName());
			if (columnQualifier == null) {
				DevTrace.exitingWithException(loc, makeNode);
				throw new SQLMappingException(
					"Unrecognized identifier : " + beanField.getParentName(),
					"No alias has been precomputed for identifier "
						+ beanField.getParentName()
						+ ". This is an internal programming error "
						+ "of the SQL mapper or the EJB-QL parser. "
						+ "Please kindly open up a problem ticket on "
						+ "component BC-JAS-PER-DBI.",
					"CSM129");
			}
			columnReference =
				new ejb21_ColumnReference(
					columnQualifier,
					columnName.getName(this.myManager.isForNativeUseOnly()),
					this.myManager.isForNativeUseOnly()
						|| columnName.isNative());
		} else {
			columnQualifier = computedRelation.getAliasName();
			tableRepMap = computedRelation.getTableRepresentations();
			joinConditionMap = computedRelation.getJoinConditions();
			aliasList = computedRelation.getAliases();
			if (aliasList != null && aliasList.size() > 0) {
				tableAlias = (TableAlias) (aliasList.get(aliasList.size() - 1));
			}
			if (tableAlias == null
				|| this.myManager.isSQLStatementCrossJoinOptimised()) {
				columnReference =
					new ejb21_ColumnReference(
						columnQualifier,
						columnName.getName(this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| columnName.isNative());
			} else {
				columnReference =
					new ejb21_ColumnReference(
						tableAlias,
						columnName.getName(this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| columnName.isNative());
			}
		}

        this.whereNode =
			new WhereNode(
				WhereNode.SIMPLE,
				new WhereNodeElement(
					tableRepMap,
					joinConditionMap,
					aliasList,
                    createLikePredicate(columnReference,
                            BUILDER.createStringLiteral(pattern.getValue()).build(),
                            escapeCharacter,
                            operator)));

		DevTrace.debugInfo(loc, makeNode, "new LikePredicate created.");

		DevTrace.exiting(loc, makeNode);
	}

    private SearchCondition createLikePredicate(ejb21_ColumnReference columnReference, ValueExpression pattern, LiteralValue escapeCharacter, int operator) {
        final LikePredicateBuilder likeBuilder = 
            BUILDER.createLikePredicate(columnReference, pattern)
            .setNotLike(operator == ComparativeOperator.NOT_LIKE);
        if (escapeCharacter != null) {
            likeBuilder.setEscapeValue(BUILDER.createStringLiteral(escapeCharacter.getValue()).build());
        }
        return likeBuilder.build();
    }

	/**
	 * Legal operators : LIKE, NOT_LIKE.
	 * Additional arguments: BeanField (with relation path), 
	 * pattern parameter (InputParameterOccurence)
	 * escapeCharacter (LiteralValue).
	 * Private method prepareLike is invoked and a LikePredicate is created.
	 */
	void makeNode(
		int operator,
		BeanField beanField,
		BeanField[] relation,
		InputParameterOccurrence patternParameter,
		LiteralValue escapeCharacter)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{
					new Integer(operator),
					beanField,
					relation,
					patternParameter,
					escapeCharacter };
			DevTrace.entering(
				loc,
				makeNode,
				makeNodeOpFieldRelInputLitParms,
				inputValues);
		}

		Relation computedRelation =
			this.prepareLike(operator, beanField, relation);

		String fieldName = beanField.getFieldName();
		String beanType = beanField.getParentType();
		CMPFieldDescriptor fd =
			this.orMapping.getCMPBeanField(beanType, fieldName);
		DatabaseColumn columnName = fd.getColumnName();
		String columnQualifier = null;
		HashMap tableRepMap = null;
		HashMap joinConditionMap = null;
		ArrayList aliasList = null;
		TableAlias tableAlias = null;
		ejb21_ColumnReference columnReference = null;

		if (relation == null || relation.length == 0) {
			columnQualifier =
				this.myManager.getAliasForIdentifier(beanField.getParentName());
			if (columnQualifier == null) {
				DevTrace.exitingWithException(loc, makeNode);
				throw new SQLMappingException(
					"Unrecognized identifier : " + beanField.getParentName(),
					"No alias has been precomputed for identifier "
						+ beanField.getParentName()
						+ ". This is an internal programming error "
						+ "of the SQL mapper or the EJB-QL parser. "
						+ "Please kindly open up a problem ticket on "
						+ "component BC-JAS-PER-DBI.",
					"CSM131");
			}
			columnReference =
				new ejb21_ColumnReference(
					columnQualifier,
					columnName.getName(this.myManager.isForNativeUseOnly()),
					this.myManager.isForNativeUseOnly()
						|| columnName.isNative());
		} else {
			columnQualifier = computedRelation.getAliasName();
			tableRepMap = computedRelation.getTableRepresentations();
			joinConditionMap = computedRelation.getJoinConditions();
			aliasList = computedRelation.getAliases();
			if (aliasList != null && aliasList.size() > 0) {
				tableAlias = (TableAlias) (aliasList.get(aliasList.size() - 1));
			}
			if (tableAlias == null
				|| this.myManager.isSQLStatementCrossJoinOptimised()) {
				columnReference =
					new ejb21_ColumnReference(
						columnQualifier,
						columnName.getName(this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| columnName.isNative());
			} else {
				columnReference =
					new ejb21_ColumnReference(
						tableAlias,
						columnName.getName(this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| columnName.isNative());
			}
		}

		this.whereNode =
			new WhereNode(
				WhereNode.SIMPLE,
				new WhereNodeElement(
					tableRepMap,
					joinConditionMap,
					aliasList,
                    createLikePredicate(columnReference, 
                            BUILDER.createHostVariable(patternParameter.getOccurrence() + 1).build(),
                            escapeCharacter,
                            operator)));

		DevTrace.debugInfo(loc, makeNode, "new LikePredicate created.");

		DevTrace.exiting(loc, makeNode);
	}

	/**
	 * Legal operators : LIKE, NOT_LIKE.
	 * Additional arguments: BeanField (with relation path), pattern (LiteralValue)
	 * escapeCharacter parameter (InputParameterOccurrence).
	 * Private method prepareLike is invoked and a LikePredicate is created.
	 */
	void makeNode(
		int operator,
		BeanField beanField,
		BeanField[] relation,
		LiteralValue pattern,
		InputParameterOccurrence escapeParameter)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{
					new Integer(operator),
					beanField,
					relation,
					pattern,
					escapeParameter };
			DevTrace.entering(
				loc,
				makeNode,
				makeNodeOpFieldRelLitInputParms,
				inputValues);
		}

		Relation computedRelation =
			this.prepareLike(operator, beanField, relation);

		String fieldName = beanField.getFieldName();
		String beanType = beanField.getParentType();
		CMPFieldDescriptor fd =
			this.orMapping.getCMPBeanField(beanType, fieldName);
		DatabaseColumn columnName = fd.getColumnName();
		String columnQualifier = null;
		HashMap tableRepMap = null;
		HashMap joinConditionMap = null;
		ArrayList aliasList = null;
		TableAlias tableAlias = null;
		ejb21_ColumnReference columnReference = null;

		if (relation == null || relation.length == 0) {
			columnQualifier =
				this.myManager.getAliasForIdentifier(beanField.getParentName());
			if (columnQualifier == null) {
				DevTrace.exitingWithException(loc, makeNode);
				throw new SQLMappingException(
					"Unrecognized identifier : " + beanField.getParentName(),
					"No alias has been precomputed for identifier "
						+ beanField.getParentName()
						+ ". This is an internal programming error "
						+ "of the SQL mapper or the EJB-QL parser. "
						+ "Please kindly open up a problem ticket on "
						+ "component BC-JAS-PER-DBI.",
					"CSM133");
			}
			columnReference =
				new ejb21_ColumnReference(
					columnQualifier,
					columnName.getName(this.myManager.isForNativeUseOnly()),
					this.myManager.isForNativeUseOnly()
						|| columnName.isNative());
		} else {
			columnQualifier = computedRelation.getAliasName();
			tableRepMap = computedRelation.getTableRepresentations();
			joinConditionMap = computedRelation.getJoinConditions();
			aliasList = computedRelation.getAliases();
			if (aliasList != null && aliasList.size() > 0) {
				tableAlias = (TableAlias) (aliasList.get(aliasList.size() - 1));
			}
			if (tableAlias == null
				|| this.myManager.isSQLStatementCrossJoinOptimised()) {
				columnReference =
					new ejb21_ColumnReference(
						columnQualifier,
						columnName.getName(this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| columnName.isNative());
			} else {
				columnReference =
					new ejb21_ColumnReference(
						tableAlias,
						columnName.getName(this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| columnName.isNative());
			}
		}

		this.whereNode =
			new WhereNode(
				WhereNode.SIMPLE,
				new WhereNodeElement(
					tableRepMap,
					joinConditionMap,
					aliasList,
                    createLikePredicate(columnReference, 
                            BUILDER.createStringLiteral(pattern.getValue()).build(),
                            null,
                            operator)));

		DevTrace.debugInfo(loc, makeNode, "new LikePredicate created.");

		DevTrace.exiting(loc, makeNode);
	}

	/**
	 * Legal operators : LIKE, NOT_LIKE.
	 * Additional arguments: BeanField (with relation path),
	 * pattern parameter (InputParameterOccurrence)
	 * escape character parameter (InputParameterOccurrence).
	 * Private method prepareLike is invoked and a LikePredicate is created.
	 */
	void makeNode(
		int operator,
		BeanField beanField,
		BeanField[] relation,
		InputParameterOccurrence patternParameter,
		InputParameterOccurrence escapeParameter)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{
					new Integer(operator),
					beanField,
					relation,
					patternParameter,
					escapeParameter };
			DevTrace.entering(
				loc,
				makeNode,
				makeNodeOpFieldRelInputInputParms,
				inputValues);
		}

		Relation computedRelation =
			this.prepareLike(operator, beanField, relation);

		String fieldName = beanField.getFieldName();
		String beanType = beanField.getParentType();
		CMPFieldDescriptor fd =
			this.orMapping.getCMPBeanField(beanType, fieldName);
		DatabaseColumn columnName = fd.getColumnName();
		String columnQualifier = null;
		HashMap tableRepMap = null;
		HashMap joinConditionMap = null;
		ArrayList aliasList = null;
		TableAlias tableAlias = null;
		ejb21_ColumnReference columnReference = null;

		if (relation == null || relation.length == 0) {
			columnQualifier =
				this.myManager.getAliasForIdentifier(beanField.getParentName());
			if (columnQualifier == null) {
				DevTrace.exitingWithException(loc, makeNode);
				throw new SQLMappingException(
					"Unrecognized identifier : " + beanField.getParentName(),
					"No alias has been precomputed for identifier "
						+ beanField.getParentName()
						+ ". This is an internal programming error "
						+ "of the SQL mapper or the EJB-QL parser. "
						+ "Please kindly open up a problem ticket on "
						+ "component BC-JAS-PER-DBI.",
					"CSM135");
			}
			columnReference =
				new ejb21_ColumnReference(
					columnQualifier,
					columnName.getName(this.myManager.isForNativeUseOnly()),
					this.myManager.isForNativeUseOnly()
						|| columnName.isNative());
		} else {
			columnQualifier = computedRelation.getAliasName();
			tableRepMap = computedRelation.getTableRepresentations();
			joinConditionMap = computedRelation.getJoinConditions();
			aliasList = computedRelation.getAliases();
			if (aliasList != null && aliasList.size() > 0) {
				tableAlias = (TableAlias) (aliasList.get(aliasList.size() - 1));
			}
			if (tableAlias == null
				|| this.myManager.isSQLStatementCrossJoinOptimised()) {
				columnReference =
					new ejb21_ColumnReference(
						columnQualifier,
						columnName.getName(this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| columnName.isNative());
			} else {
				columnReference =
					new ejb21_ColumnReference(
						tableAlias,
						columnName.getName(this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| columnName.isNative());
			}
		}

		this.whereNode =
			new WhereNode(
				WhereNode.SIMPLE,
				new WhereNodeElement(
					tableRepMap,
					joinConditionMap,
					aliasList,
                    BUILDER.createLikePredicate(
                            columnReference,
                            BUILDER.createHostVariable(patternParameter.getOccurrence() + 1).build())
                            .setEscapeValue(BUILDER.createHostVariable(escapeParameter.getOccurrence() + 1).build())
                            .setNotLike(operator == ComparativeOperator.NOT_LIKE)
                            .build()));

		DevTrace.debugInfo(loc, makeNode, "new LikePredicate created.");

		DevTrace.exiting(loc, makeNode);
	}

	/**
	 * Legal operators: BETWEEN, NOT_BETWEEN.
	 * Additional arguments: array of operands, array of lowerBounds,
	 * array of upperBounds. 
	 * The length of the operand array must be 1; exception is thrown 
	 * otherwise. A BetweenPredicate us created.
	 */
	void makeNode(
		int operator,
		Object[] operand,
		Object[] lowerBound,
		Object[] upperBound)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{ new Integer(operator), operand, lowerBound, upperBound };
			DevTrace.entering(
				loc,
				makeNode,
				makeNodeOpObjObjObjParms,
				inputValues);
		}

		if (operator != ComparativeOperator.BETWEEN
			&& operator != ComparativeOperator.NOT_BETWEEN) {
			DevTrace.exitingWithException(loc, makeNode);
			throw new SQLMappingException(
				"Illegal comparative operator as argument of "
					+ "makeNode(int, Object[], Object[], Object[]).",
				"The EJBQLTreeProcessor has called a makeNode method "
					+ "of the SQLTreeNodeManager with an unexpected "
					+ "ComparativeOperator "
					+ operator
					+ ".",
				"CSM022");
		}

		if (operand.length != 1) {
			DevTrace.exitingWithException(loc, makeNode);
			throw new SQLMappingException(
				"The length of the operand Object[] is not equal 1.",
				null,
				"CSM024");

		}

		boolean notBetween = (operator == ComparativeOperator.NOT_BETWEEN);
		this.pushWhereNodeIfNotNull();

		HashMap newJoinConditionMap =
			this.myManager.mergeHashMaps(
				((ExpressionNode) operand[0]).getJoinConditions(),
				((ExpressionNode) lowerBound[0]).getJoinConditions(),
				((ExpressionNode) upperBound[0]).getJoinConditions());

		HashMap newTableRepMap =
			this.myManager.mergeHashMaps(
				((ExpressionNode) operand[0]).getTableRepresentations(),
				((ExpressionNode) lowerBound[0]).getTableRepresentations(),
				((ExpressionNode) upperBound[0]).getTableRepresentations());

		ArrayList newAliasList =
			this.myManager.appendArrayLists(
				((ExpressionNode) operand[0]).getAliases(),
				((ExpressionNode) lowerBound[0]).getAliases(),
				((ExpressionNode) upperBound[0]).getAliases());

		this.whereNode =
			new WhereNode(
				WhereNode.SIMPLE,
				new WhereNodeElement(
					newTableRepMap,
					newJoinConditionMap,
					newAliasList,
                    BUILDER.createBetweenPredicate(
						((ExpressionNode) operand[0]).getExpression(),
						((ExpressionNode) lowerBound[0]).getExpression(),
						((ExpressionNode) upperBound[0]).getExpression())
                        .setNotBetween(notBetween).build()));

		DevTrace.debugInfo(loc, makeNode, "new BetweenPredicate created.");

		DevTrace.exiting(loc, makeNode);
	}

	/**
	 * Legal operators: EQ, NE, LT, LE, GT, GE			
	 * Additional arguments: left operand, right operand.
	 * The length of the operand array must be 1; exception is thrown 
	 * ComparisonPredicates are created.
	 */
	void makeNode(ComparisonOperator operator, Object[] leftOperand, Object[] rightOperand) {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{ operator, leftOperand, rightOperand };
			DevTrace.entering(
				loc,
				makeNode,
				makeNodeOpObjObjParms,
				inputValues);
		}

		this.pushWhereNodeIfNotNull();

		HashMap newJoinConditionMap =
			this.myManager.mergeHashMaps(
				((ExpressionNode) leftOperand[0]).getJoinConditions(),
				((ExpressionNode) rightOperand[0]).getJoinConditions());

		HashMap newTableRepMap =
			this.myManager.mergeHashMaps(
				((ExpressionNode) leftOperand[0]).getTableRepresentations(),
				((ExpressionNode) rightOperand[0]).getTableRepresentations());

		ArrayList newAliasList =
			this.myManager.appendArrayLists(
				((ExpressionNode) leftOperand[0]).getAliases(),
				((ExpressionNode) rightOperand[0]).getAliases());

		// joinMap only at first element of the array, see ExpressionNodeManager
		int count = rightOperand.length;
		SearchCondition[] andList = new SearchCondition[rightOperand.length];
		for (int i = 0; i < count; i++) {
			andList[i] =
                BUILDER.createComparisonPredicate(
					((ExpressionNode) leftOperand[i]).getExpression(),
					operator,
					((ExpressionNode) rightOperand[i]).getExpression()).build();

			DevTrace.debugInfo(
				loc,
				makeNode,
				"new ComparisonPredicate created.");
		}

		this.whereNode =
			new WhereNode(
				WhereNode.SIMPLE,
				new WhereNodeElement(
					newTableRepMap,
					newJoinConditionMap,
					newAliasList,
					(count == 1)
						? andList[0]
						: BUILDER.createBooleanAnd(andList).build()));

		DevTrace.exiting(loc, makeNode);
	}

	/**
	 * Legal operators: MEMBER, NOT_MEMBER
	 * Additional arguments: BeanObject, target BeanField (with relations).
	 * The target BeanField has to be a collection valued cmr bean field.
	 * The SubqueryBuilder is invoked and a InSubqueryPredicate is created.
	 */
	void makeNode(
		int operator,
		BeanObject beanObject,
		BeanField targetBeanField,
		BeanField[] targetRelation)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{
					new Integer(operator),
					beanObject,
					targetBeanField,
					targetRelation };
			DevTrace.entering(
				loc,
				makeNode,
				makeNodeOpBeanFieldRelParms,
				inputValues);
		}

		this.prepareMember(operator);
		this.pushWhereNodeIfNotNull();

		String fieldName = targetBeanField.getFieldName();
		CMRFieldDescriptor targetFieldDescr =
			this.orMapping.getCMRBeanField(
				targetBeanField.getParentType(),
				fieldName);
		int multiplicity = targetFieldDescr.getMultiplicity();
		this.myManager.checkMultiplicity(
			multiplicity,
			SQLTreeNodeManager.collectionValuedCMR,
			fieldName);

		PKBeanDescriptor bd = this.orMapping.getBeanPK(beanObject.getType());
		DatabaseColumn[] pkColumns = bd.getColumnNames();
		RowValueElement[] colRefArray = new ejb21_ColumnReference[pkColumns.length];
		String columnQualifier =
			this.myManager.getAliasForIdentifier(beanObject.getIdentifier());
		if (columnQualifier == null) {
			DevTrace.exitingWithException(loc, makeNode);
			throw new SQLMappingException(
				"Unrecognized identifier : " + beanObject.getIdentifier(),
				"No alias has been precomputed for identifier "
					+ beanObject.getIdentifier()
					+ ". This is an internal programming error "
					+ "of the SQL mapper or the EJB-QL parser. "
					+ "Please kindly open up a problem ticket on "
					+ "component BC-JAS-PER-DBI.",
				"CSM119");
		}
		for (int i = 0; i < pkColumns.length; i++) {
			colRefArray[i] =
				new ejb21_ColumnReference(
					columnQualifier,
					pkColumns[i].getName(this.myManager.isForNativeUseOnly()),
					this.myManager.isForNativeUseOnly()
						|| pkColumns[i].isNative());
		}

		this.whereNode =
			new WhereNode(
				WhereNode.SIMPLE,
				new WhereNodeElement(
					null,
					null,
					null,
                    BUILDER.createInSubqueryPredicate(
                            BUILDER.createRowValueElementList(colRefArray).build(),
						this.subqueryBuilder.buildSubquery(
							targetRelation,
							targetFieldDescr.hasHelperTable()))
                        .setNotIn(operator == ComparativeOperator.NOT_MEMBER).build()));

		DevTrace.debugInfo(loc, makeNode, "new InSubqueryPredicate created.");

		DevTrace.exiting(loc, makeNode);
	}

	/**
	 * Legal operators: MEMBER, NOT_MEMBER
	 * Additional arguments: BeanField (with relations), target BeanField (with relations).
	 * The BeanField has to be a single valued cmr bean field and the
	 * target BeanField has to be a collection valued cmr bean field.
	 * The SubqueryBuilder is invoked a InSubqueryPredicate is created.
	 */
	void makeNode(
		int operator,
		BeanField beanField,
		BeanField[] relation,
		BeanField targetBeanField,
		BeanField[] targetRelation)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{
					new Integer(operator),
					beanField,
					relation,
					targetBeanField,
					targetRelation };
			DevTrace.entering(
				loc,
				makeNode,
				makeNodeOpFieldRelFieldRelParms,
				inputValues);
		}

		this.prepareMember(operator);
		this.pushWhereNodeIfNotNull();

		String fieldName = beanField.getFieldName();
		CMRFieldDescriptor fieldDescr =
			this.orMapping.getCMRBeanField(
				beanField.getParentType(),
				fieldName);
		int multiplicity = fieldDescr.getMultiplicity();
		this.myManager.checkMultiplicity(
			multiplicity,
			SQLTreeNodeManager.singleValuedCMR,
			fieldName);

		String targetFieldName = targetBeanField.getFieldName();
		CMRFieldDescriptor targetFieldDescr =
			this.orMapping.getCMRBeanField(
				targetBeanField.getParentType(),
				targetFieldName);
		int targetMultiplicity = targetFieldDescr.getMultiplicity();
		this.myManager.checkMultiplicity(
			targetMultiplicity,
			SQLTreeNodeManager.collectionValuedCMR,
			targetFieldName);

		DatabaseColumn[] columns = fieldDescr.getColumnNames();
		boolean includeLast = false;
		if (columns.length == 0) {
			includeLast = true;
			PKBeanDescriptor bd = this.orMapping.getBeanPK(beanField.getType());
			columns = bd.getColumnNames();
		}

		String columnQualifier = null;
		Relation computedRelation = null;
		HashMap tableRepMap = null;
		HashMap joinConditionMap = null;
		ArrayList aliasList = null;
		TableAlias tableAlias = null;

		if (relation == null || relation.length == 0) {
			columnQualifier =
				this.myManager.getAliasForIdentifier(beanField.getParentName());
			if (columnQualifier == null) {
				DevTrace.exitingWithException(loc, makeNode);
				throw new SQLMappingException(
					"Unrecognized identifier : " + beanField.getParentName(),
					"No alias has been precomputed for identifier "
						+ beanField.getParentName()
						+ ". This is an internal programming error "
						+ "of the SQL mapper or the EJB-QL parser. "
						+ "Please kindly open up a problem ticket on "
						+ "component BC-JAS-PER-DBI.",
					"CSM121");
			}
		} else {
			computedRelation =
				this.myManager.computeRelation(relation, includeLast);
			columnQualifier = computedRelation.getAliasName();
			joinConditionMap = computedRelation.getJoinConditions();
			tableRepMap = computedRelation.getTableRepresentations();
			aliasList = computedRelation.getAliases();
			if (aliasList != null && aliasList.size() > 0) {
				tableAlias = (TableAlias) (aliasList.get(aliasList.size() - 1));
			}
		}

		RowValueElement[] colRefArray = new ejb21_ColumnReference[columns.length];
		for (int i = 0; i < colRefArray.length; i++) {

			if (tableAlias == null) {
				colRefArray[i] =
					new ejb21_ColumnReference(
						columnQualifier,
						columns[i].getName(this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| columns[i].isNative());
			} else {
				colRefArray[i] =
					new ejb21_ColumnReference(
						tableAlias,
						columns[i].getName(this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| columns[i].isNative());
			}
		}

		this.whereNode =
			new WhereNode(
				WhereNode.SIMPLE,
				new WhereNodeElement(
					tableRepMap,
					joinConditionMap,
					aliasList,
                    BUILDER.createInSubqueryPredicate(
                            BUILDER.createRowValueElementList(colRefArray).build(),
						this.subqueryBuilder.buildSubquery(
							targetRelation,
							targetFieldDescr.hasHelperTable()))
                            .setNotIn(operator == ComparativeOperator.NOT_MEMBER).build()));

		DevTrace.debugInfo(loc, makeNode, "new InSubqueryPredicate created.");

		DevTrace.exiting(loc, makeNode);
	}

	/**
	 * Legal operators: MEMBER, NOT_MEMBER
	 * Additional arguments: InputParameterOccurrence, target BeanField (with relations).
	 * The target BeanField has to be a collection valued cmr bean field.
	 * Hostvariables are created.
	 * The SubqueryBuilder is invoked and a InSubqueryPredicate is created.
	 */
	void makeNode(
		int operator,
		InputParameterOccurrence inputParameterOccurrence,
		BeanField targetBeanField,
		BeanField[] targetRelation)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{
					new Integer(operator),
					inputParameterOccurrence,
					targetBeanField,
					targetRelation };
			DevTrace.entering(
				loc,
				makeNode,
				makeNodeOpInputFieldRelParms,
				inputValues);
		}

		this.prepareMember(operator);
		this.pushWhereNodeIfNotNull();

		String fieldName = targetBeanField.getFieldName();
		CMRFieldDescriptor targetFieldDescr =
			this.orMapping.getCMRBeanField(
				targetBeanField.getParentType(),
				fieldName);
		int multiplicity = targetFieldDescr.getMultiplicity();
		this.myManager.checkMultiplicity(
			multiplicity,
			SQLTreeNodeManager.collectionValuedCMR,
			fieldName);

		InputParameter input =
			this.inputParameterList.get(
				Integer.valueOf(
					inputParameterOccurrence.getInputParameterNumber()));
		int firstPosition = inputParameterOccurrence.getOccurrence() + 1;
		int count = input.getCount();
		RowValueElement[] rowValueArray = new ValueExpression[count];
		for (int i = 0; i < count; i++) {
			rowValueArray[i] =
                BUILDER.createHostVariable(firstPosition + i).build();
		}
		this.whereNode =
			new WhereNode(
				WhereNode.SIMPLE,
				new WhereNodeElement(
					null,
					null,
					null,
                    BUILDER.createInSubqueryPredicate(
                            BUILDER.createRowValueElementList(rowValueArray).build(),
						this.subqueryBuilder.buildSubquery(
							targetRelation,
							targetFieldDescr.hasHelperTable()))
                            .setNotIn(operator == ComparativeOperator.NOT_MEMBER).build()));

		DevTrace.debugInfo(loc, makeNode, "new InSubqueryPredicate created.");

		DevTrace.exiting(loc, makeNode);
	}

	/**
	 * This method change the node currently in the processing
	 * unit by adding a monadic operator. Only NOT is allowed.
	 */
	void alterNode(int monadicLogicOperator) throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { new Integer(monadicLogicOperator)};
			DevTrace.entering(loc, alterNode, alterNodeParms, inputValues);
		}

		if (monadicLogicOperator != LogicOperator.NOT) {
			DevTrace.exitingWithException(loc, alterNode);
			throw new SQLMappingException(
				"Unexpected monadic logic operator as argument of "
					+ "alterNode(int).",
				"The EJBQLTreeProcessor has called alterNode method "
					+ "of the SQLTreeNodeManager with an unexpected "
					+ "LogicOperator "
					+ monadicLogicOperator
					+ ".",
				"CSM024");
		}

		if (this.whereNode.getType() == WhereNode.SIMPLE) {
			// SIMPLE':
			// trMAP' = trMAP,
			// jcMAP' = jcMAP,
			// aliasAL' = aliasAL
			// SC' = ! SC 
			DevTrace.debugInfo(loc, alterNode, "current where node is simple.");
			WhereNodeElement elem = this.whereNode.getSimple();
			HashMap tableRepMap = elem.getTableRepresentations();
			HashMap joinConditionMap = elem.getJoinConditions();
			ArrayList aliasList = elem.getAliases();
			SearchCondition searchCondition = elem.getCondition();

			boolean scExists =
				(searchCondition != null)
					&& (!(searchCondition instanceof NeutralCondition));

			if (scExists) {
				// MAP' = MAP, SC' = ! SC
				this.whereNode.setSimple(
					new WhereNodeElement(
						tableRepMap,
						joinConditionMap,
						aliasList,
                        BUILDER.createBooleanNot(searchCondition).build()));
			} // else MAP' = MAP, SC' = SC

			// COMPOSED
		} else {
			DevTrace.debugInfo(
				loc,
				alterNode,
				"current where node is composed.");
			if (this.myManager.isSQLStatementCrossJoinOptimised()) {
				//
				// SIMPLE:
				// MAP = MERGE(MAP1, ..., MAPn)
				// SC  = ! SC1 AND ... AND ! SCn
				ArrayList orList = this.whereNode.getComposed();

				//MAP = MERGE(MAP1, ..., MAPn)
				int andCount = 0;
				WhereNodeElement elem0 = (WhereNodeElement) orList.get(0);
				HashMap map0 = elem0.getJoinConditions();
				SearchCondition sc0 = elem0.getCondition();
				if (sc0 != null && !(sc0 instanceof NeutralCondition)) {
					andCount++;
				}

				HashMap newJoinConditionMap = map0;

				for (int i = 1; i < orList.size(); i++) {
					WhereNodeElement elem = (WhereNodeElement) orList.get(i);
					HashMap map = elem.getJoinConditions();
					newJoinConditionMap =
						this.myManager.mergeHashMaps(newJoinConditionMap, map);
					SearchCondition sc = elem.getCondition();
					if (sc != null && !(sc instanceof NeutralCondition)) {
						andCount++;
					}
				}

				// SC = ! SC1 AND ... AND ! SCn
				int andIndex = 0;
				if (andCount > 0) {
					SearchCondition[] booleanAndList =
						new SearchCondition[andCount];

					for (int i = 0; i < orList.size(); i++) {
						SearchCondition sc =
							((WhereNodeElement) orList.get(i)).getCondition();
						if (sc != null && !(sc instanceof NeutralCondition)) {
							booleanAndList[andIndex] = BUILDER.createBooleanNot(sc).build();
							andIndex++;
						}
					}

					this.whereNode.setSimple(
						new WhereNodeElement(
							null,
							newJoinConditionMap,
							null,
                            BUILDER.createBooleanAnd(booleanAndList).build()));
				} else {
					this.whereNode.setSimple(
						new WhereNodeElement(
							null,
							newJoinConditionMap,
							null,
							BUILDER.createNeutralCondition().build()));
				}
			} else {
				//
				// SIMPLE:
				// trMAP = null, jcMAP = null, aliasAL = null
				// SC = ! (EXISTS(SC1) AND ... AND EXISTS(SCn))
				ArrayList andList = this.whereNode.getComposed();
				List<SearchCondition> booleanAndList = new ArrayList<SearchCondition>();
				for (int i = 0; i < andList.size(); i++) {
					WhereNodeElement elem = (WhereNodeElement) andList.get(i);
					SearchCondition elemExists =
						this.myManager.buildExists(
							elem.getTableRepresentations(),
							elem.getJoinConditions(),
							elem.getAliases(),
							elem.getCondition());
					if (elemExists != null) {
						booleanAndList.add(elemExists);
					}
				}

				if (booleanAndList.size() > 0) {
					SearchCondition[] booleanAndArray =
						new SearchCondition[booleanAndList.size()];
					booleanAndList.toArray(booleanAndArray);
					SearchCondition booleanAnd =
                        BUILDER.createBooleanAnd(booleanAndArray).build();
					this.whereNode.setSimple(
						new WhereNodeElement(
							null,
							null,
							null,
                            BUILDER.createBooleanNot(booleanAnd).build()));
				} else {
					this.whereNode.setSimple(
						new WhereNodeElement(
							null,
							null,
							null,
                            BUILDER.createNeutralCondition().build()));
				}
			}
		}
		DevTrace.exiting(loc, alterNode);
	}

	/**
	 * This method pulls the top element
	 * from the stack and combines it (in that order) with the node currently
	 * in the processing unit by a dyadic operator; the result is placed into
	 * the processing unit. Only AND and OR is allowed.
	 */
	void popNode(int logicOperator) throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { new Integer(logicOperator)};
			DevTrace.entering(loc, popNode, popNodeParms, inputValues);
		}

		if ((this.whereNode == null) || this.whereStack.empty()) {
			DevTrace.exitingWithException(loc, popNode);
			throw new SQLMappingException(
				"Too few arguments for logic operator.",
				"A logic operator was invoked with less than "
					+ "two arguments. This is most likely a programming "
					+ "error within SQL mapper. Operator code is "
					+ logicOperator
					+ ".",
				"CSM008");
		}

		WhereNode popWhereNode = this.whereStack.pop();

		// popNode == SIMPLE && curNode == SIMPLE
		if (popWhereNode.getType() == WhereNode.SIMPLE
			&& this.whereNode.getType() == WhereNode.SIMPLE) {
			//
			// if (doCrossJoinOptimisation)
			//
			//   AND: SIMPLE': 
			//        MAP' = MERGE(popMAP, curMap),
			//        SC' = popSC AND curSC
			//
			//   OR:  MAP* = INTERSECT(curMAP, popMAP)
			//        if (MAP' != null) {
			//          SIMPLE':
			//          MAP' = MAP*
			//          SC' = (popMAP* AND popSC) OR (curMAP* AND curSC) 
			//		  } else {
			//          COMPOSED_OR: [popNode, curNode]
			//      }
			// else
			//
			//   AND: COMPOSED_AND: [popNode, curNode]
			//   
			//   OR:  SIMPLE':
			//        trMAP' = null, jcMAP' = null, aliasAL' = null
			//        SC' = EXISTS(popNode) OR EXISTS(curNode)
			//
			DevTrace.debugInfo(
				loc,
				popNode,
				"current where node and pop where node are simple.");
			WhereNodeElement popElem = popWhereNode.getSimple();
			HashMap popTRMap = popElem.getTableRepresentations();
			HashMap popJCMap = popElem.getJoinConditions();
			ArrayList popAliasList = popElem.getAliases();
			SearchCondition popCondition = popElem.getCondition();

			WhereNodeElement curElem = this.whereNode.getSimple();
			HashMap curTRMap = curElem.getTableRepresentations();
			HashMap curJCMap = curElem.getJoinConditions();
			ArrayList curAliasList = curElem.getAliases();
			SearchCondition curCondition = curElem.getCondition();

			switch (logicOperator) {
				case LogicOperator.AND :
					if (this.myManager.isSQLStatementCrossJoinOptimised()) {
						// SIMPLE':
						// MAP' = MERGE(popMAP, curMap)
						HashMap newJoinConditionMap =
							this.myManager.mergeHashMaps(popJCMap, curJCMap);
						SearchCondition[] andList;

						// SC' = (popSC AND curSC)
						if (popCondition instanceof NeutralCondition) {
							andList = new SearchCondition[] { curCondition };
						} else if (curCondition instanceof NeutralCondition) {
							andList = new SearchCondition[] { popCondition };
						} else {
							andList =
								new SearchCondition[] {
									popCondition,
									curCondition };
						}

						this.whereNode.setSimple(
							new WhereNodeElement(
								null,
								newJoinConditionMap,
								null,
                                BUILDER.createBooleanAnd(andList).build()));
					} else {
						// COMPOSED_AND: [popNode, curNode]
						this.whereNode =
							new WhereNode(WhereNode.COMPOSED_AND, popElem);
						this.whereNode.add(curElem);
					}
					break;

				case LogicOperator.OR :
					if (this.myManager.isSQLStatementCrossJoinOptimised()) {
						// MAP' = INTERSECT(curMAP, popMAP)
						HashMap newJoinConditionMap =
							this.myManager.intersectJoinMaps(
								curJCMap,
								popJCMap);

						if (newJoinConditionMap != null) {
							// SIMPLE':
							// SC' = (popMAP* AND popSC) OR (curMAP* AND curSC) 
							SearchCondition booleanOr =
								this.myManager.buildBooleanOr(
									popJCMap,
									popCondition,
									curJCMap,
									curCondition);

							if (booleanOr == null) {
								booleanOr = BUILDER.createNeutralCondition().build();
							}
							this.whereNode.setSimple(
								new WhereNodeElement(
									null,
									newJoinConditionMap,
									null,
									booleanOr));
						} else {
							// COMPOSED_OR: [popNode, curNode]
							this.whereNode =
								new WhereNode(WhereNode.COMPOSED_OR, popElem);
							this.whereNode.add(curElem);
						}
					} else {
						// SIMPLE':
						// trMAP' = null, jcMAP' = null, aliasAL' = null
						// SC' = EXISTS(popNode) OR EXISTS(curNode)
						SearchCondition popExists =
							this.myManager.buildExists(
								popTRMap,
								popJCMap,
								popAliasList,
								popCondition);
						SearchCondition curExists =
							this.myManager.buildExists(
								curTRMap,
								curJCMap,
								curAliasList,
								curCondition);

						SearchCondition[] orList;
						if (popExists != null) {
							if (curExists != null) {
								orList =
									new SearchCondition[] {
										popExists,
										curExists };
							} else {
								orList = new SearchCondition[] { popExists };
							}
						} else if (curExists != null) {
							orList = new SearchCondition[] { curExists };
						} else {
							orList =
								new SearchCondition[] { BUILDER.createNeutralCondition().build() };
						}
						this.whereNode.setSimple(
							new WhereNodeElement(
								null,
								null,
								null,
                                BUILDER.createBooleanOr(orList).build()));
					}
					break;

				default :
					DevTrace.exitingWithException(loc, popNode);
					throw new SQLMappingException(
						"Unexpected logic operator as argument of popNode(int).",
						"The EJBQLTreeProcessor has called popNode method "
							+ "of the SQLTreeNodeManager with an unexpected "
							+ "LogicOperator "
							+ logicOperator
							+ ".",
						"CSM026");

			}
			// popNode == SIMPLE && curNode == COMPOSED ||
			// popNode == COMPOSED && curNode == SIMPLE	
		} else if (
			(popWhereNode.getType() == WhereNode.SIMPLE
				&& (this.whereNode.getType() == WhereNode.COMPOSED_OR
					|| this.whereNode.getType() == WhereNode.COMPOSED_AND))
				|| ((popWhereNode.getType() == WhereNode.COMPOSED_OR
					|| popWhereNode.getType() == WhereNode.COMPOSED_AND)
					&& this.whereNode.getType() == WhereNode.SIMPLE)) {
			//
			// if (doCrossJoinOptimisation)
			//
			//   AND: SIMPLE':
			//        MAP' = simpleMap
			//        if popNode == SIMPLE
			//          SC' = simpleSC AND (MAP1 AND SC1 OR ... OR MAPn AND SCn)
			//        else
			//          SC' = (MAP1 AND SC1 OR ... OR MAPn AND SCn) AND simpleSC
			//
			//   OR:  // compute the index out of 0...n-1 where the intersection count is the biggest
			//        x = INTERSECT_MAX(simpleMAP, MAP1...MAPn])
			//        if (x < 0)
			//          // no condition of simpleMAP is in any MAP of composedNode
			//          if popNode == SIMPLE
			//            [SIMPLE, COMPOSED_OR]
			//          else
			//            [COMPOSED_OR, SIMPLE]
			//        else
			//          COMPOSED_OR:
			//          MAP' = INTERSECT(simpleMAP, MAPx+1)			
			//          this.whereNode = orlistNode;
			//          this.whereNode.remove(x);
			//          if popNode==SIMPLE
			//			  SC' = (simpleMAP* AND simpleSC) OR (MAPx+1* AND SCx+1)			
			//          else
			//			  SC' = (MAPx+1* AND SCx+1) OR (simpleMAP* AND simpleSC)
			//          this.whereNode.add(new WhereNodeElement(MAP', SC'); 
			//
			// else
			//
			//   AND: COMPOSED_AND':
			//        if popNode == SIMPLE
			//          [SIMPLE, COMPOSED_AND]
			//        else
			//          [COMPOSED_AND, SIMPLE]
			//
			//   OR:  SIMPLE':
			//        trMAP' = null, jcMAP' = null, aliasAL' = null
			//        if popNode == SIMPLE
			//          SC' = EXISTS(SIMPLE) OR (EXISTS(COMPOSED_AND1) AND ... AND EXISTS(COMPOSED_ANDn))
			//        else
			//          SC' = (EXISTS(COMPOSED_AND1) AND ... AND EXISTS(COMPOSED_ANDn)) OR EXISTS(SIMPLE)
			//
			WhereNode simpleWhereNode = null;
			WhereNode composedWhereNode = null;
			if (popWhereNode.getType() == WhereNode.SIMPLE) {
				DevTrace.debugInfo(
					loc,
					popNode,
					"current where node is composed and pop where node is simple.");

				simpleWhereNode = popWhereNode;
				composedWhereNode = this.whereNode;
			} else {
				DevTrace.debugInfo(
					loc,
					popNode,
					"current where node is simple and pop where node is composed.");
				simpleWhereNode = this.whereNode;
				composedWhereNode = popWhereNode;
			}

			ArrayList composedList = composedWhereNode.getComposed();
			WhereNodeElement simpleElem = simpleWhereNode.getSimple();
			HashMap simpleTRMap = simpleElem.getTableRepresentations();
			HashMap simpleJCMap = simpleElem.getJoinConditions();
			ArrayList simpleAliasList = simpleElem.getAliases();
			SearchCondition simpleCondition = simpleElem.getCondition();

			switch (logicOperator) {
				case LogicOperator.AND :
					if (this.myManager.isSQLStatementCrossJoinOptimised()) {
						// SIMPLE':
						// SC' = (MAP1 AND SC1 OR ... OR MAPn AND SCn) 
						SearchCondition booleanOr =
							this.myManager.buildBooleanOr(composedList);
						// AND simpleSC
						SearchCondition[] booleanAndList;
						if (booleanOr == null) {
							booleanAndList =
								new SearchCondition[] { simpleCondition };
						} else if (
							simpleCondition instanceof NeutralCondition) {
							booleanAndList =
								new SearchCondition[] { booleanOr };
						} else {
							booleanAndList =
								(popWhereNode.getType() == WhereNode.SIMPLE)
									? (new SearchCondition[] { simpleCondition,
										booleanOr })
									: (new SearchCondition[] { booleanOr,
										simpleCondition });
						}

						this.whereNode.setSimple(
							new WhereNodeElement(
								null,
								simpleJCMap,
								null,
                                BUILDER.createBooleanAnd(booleanAndList).build()));
					} else {
						// COMPOSED_AND':
						if (popWhereNode.getType() == WhereNode.SIMPLE) {
							// [SIMPLE, COMPOSED_AND]
							this.whereNode.addFirst(simpleElem);
						} else {
							// [COMPOSED_AND, SIMPLE]
							this.whereNode = composedWhereNode;
							this.whereNode.add(simpleElem);
						}
					}
					break;

				case LogicOperator.OR :
					if (this.myManager.isSQLStatementCrossJoinOptimised()) {
						// x = INTERSECT_MAX(simpleMAP, MAP1...MAPn])
						int index =
							this.myManager.intersectMaxJoinMaps(
								simpleJCMap,
								composedList);
						if (index < 0) {
							// COMPOSED_OR: [popNode, curNode]
							if (popWhereNode.getType() == WhereNode.SIMPLE) {
								this.whereNode.addFirst(simpleElem);
							} else {
								this.whereNode = composedWhereNode;
								this.whereNode.add(simpleElem);
							}
						} else {
							WhereNodeElement myElem =
								(WhereNodeElement) composedList.get(index);
							HashMap myJCMap = myElem.getJoinConditions();
							SearchCondition myCondition = myElem.getCondition();
							// SIMPLE':
							// MAP' = INTERSECT(simpleMAP, MAPx+1)
							HashMap newJoinConditionMap =
								this.myManager.intersectJoinMaps(
									simpleJCMap,
									myJCMap);
							this.whereNode = composedWhereNode;
							this.whereNode.remove(index);

							// if popNode==SIMPLE
							//   SC' = (simpleMAP* AND simpleSC) OR (MAPx+1* AND SCx+1)			
							// else
							//   SC' = (MAPx+1* AND SCx+1) OR (simpleMAP* AND simpleSC)
							SearchCondition booleanOr =
								(popWhereNode.getType() == WhereNode.SIMPLE)
									? this.myManager.buildBooleanOr(
										simpleJCMap,
										simpleCondition,
										myJCMap,
										myCondition)
									: this.myManager.buildBooleanOr(
										myJCMap,
										myCondition,
										simpleJCMap,
										simpleCondition);

							if (booleanOr == null) {
								booleanOr = BUILDER.createNeutralCondition().build();
							}
							this.whereNode.add(
								new WhereNodeElement(
									null,
									newJoinConditionMap,
									null,
									booleanOr));
						}
					} else {
						// OR:  SIMPLE':
						//      trMAP' = null, jcMAP' = null, aliasAL' = null
						//      if popNode == SIMPLE
						//        SC' = EXISTS(SIMPLE) OR (EXISTS(COMPOSED_AND1) AND ... AND EXISTS(COMPOSED_ANDn))
						//      else
						//        SC' = (EXISTS(COMPOSED_AND1) AND ... AND EXISTS(COMPOSED_ANDn)) OR EXISTS(SIMPLE)
						SearchCondition simpleExists =
							this.myManager.buildExists(
								simpleTRMap,
								simpleJCMap,
								simpleAliasList,
								simpleCondition);

						List<SearchCondition> booleanAndList = new ArrayList<SearchCondition>();
						for (int i = 0; i < composedList.size(); i++) {
							WhereNodeElement elem =
								(WhereNodeElement) composedList.get(i);
							SearchCondition elemExists =
								this.myManager.buildExists(
									elem.getTableRepresentations(),
									elem.getJoinConditions(),
									elem.getAliases(),
									elem.getCondition());
							if (elemExists != null) {
								booleanAndList.add(elemExists);
							}
						}

						SearchCondition[] orList;
						if (booleanAndList.size() > 0) {
							SearchCondition[] booleanAndArray =
								new SearchCondition[booleanAndList.size()];
							booleanAndList.toArray(booleanAndArray);
							SearchCondition booleanAnd = BUILDER.createBooleanAnd(booleanAndArray).build();
							if (simpleExists != null) {
								if (popWhereNode.getType()
									== WhereNode.SIMPLE) {
									orList =
										new SearchCondition[] {
											simpleExists,
											booleanAnd };
								} else {
									orList =
										new SearchCondition[] {
											booleanAnd,
											simpleExists };
								}
							} else {
								orList = new SearchCondition[] { booleanAnd };
							}
						} else if (simpleExists != null) {
							orList = new SearchCondition[] { simpleExists };
						} else {
							orList =
								new SearchCondition[] { BUILDER.createNeutralCondition().build() };
						}
						this.whereNode.setSimple(
							new WhereNodeElement(
								null,
								null,
								null,
                                BUILDER.createBooleanOr(orList).build()));
					}
					break;

				default :
					DevTrace.exitingWithException(loc, popNode);
					throw new SQLMappingException(
						"Unexpected logic operator as argument of popNode(int).",
						"The EJBQLTreeProcessor has called popNode method "
							+ "of the SQLTreeNodeManager with an unexpected "
							+ "LogicOperator "
							+ logicOperator
							+ ".",
						"CSM026");
			}

			// popNode == COMPOSED && curNode == COMPOSED	
		} else {
			//
			// if (doCrossJoinOptimisation)
			//
			//   AND: SIMPLE:
			//        MAP = null
			//        SC  = (popMAP1 AND popSC1 OR ... OR popMAPn AND popSCn) AND
			//               curMAP1 AND curSC1 OR ... OR curMAPm AND curSCm
			//
			//   OR:  COMPOSED_OR': [popCOMPOSED_OR, curCOMPOSED_OR]
			//
			// else
			//
			//   AND: COMPOSED_AND': [popCOMPOSED_AND, curCOMPOSED_AND]
			//
			//   OR:  SIMPLE:
			//        trMAP = null, jcMAP = null, aliasAL = null
			//        SC = EXISTS(popCOMPOSED_AND1) AND ... AND EXISTS(popCOMPOSED_ANDn) 
			//             OR
			//             EXISTS(curCOMPOSED_AND1) AND ... AND EXISTS(curCOMPOSED_ANDm)
			//         
			DevTrace.debugInfo(
				loc,
				popNode,
				"current where node and pop where node are composed.");

			ArrayList popComposedList = popWhereNode.getComposed();
			ArrayList curComposedList = this.whereNode.getComposed();

			switch (logicOperator) {
				case LogicOperator.AND :
					if (this.myManager.isSQLStatementCrossJoinOptimised()) {
						// SIMPLE:
						// MAP = null
						// SC = (popMAP1 AND popSC1 OR ... OR popMAPn AND popSCn) AND
						//       curMAP1 AND curSC1 OR ... OR curMAPm AND curSCm
						SearchCondition popBooleanOr =
							this.myManager.buildBooleanOr(popComposedList);
						SearchCondition curBooleanOr =
							this.myManager.buildBooleanOr(curComposedList);

						SearchCondition[] andList;
						if (popBooleanOr != null) {
							if (curBooleanOr != null) {
								andList =
									new SearchCondition[] {
										popBooleanOr,
										curBooleanOr };
							} else {
								andList =
									new SearchCondition[] { popBooleanOr };
							}
						} else {
							if (curBooleanOr != null) {
								andList =
									new SearchCondition[] { curBooleanOr };
							} else {
								andList =
									new SearchCondition[] {
										 BUILDER.createNeutralCondition().build()};
							}
						}

						this.whereNode.setSimple(
							new WhereNodeElement(
								null,
								null,
								null,
                                BUILDER.createBooleanAnd(andList).build()));

					} else {
						// COMPOSED_AND': [popCOMPOSED_AND, curCOMPOSED_AND]
						this.whereNode = popWhereNode;
						for (int i = 0; i < curComposedList.size(); i++) {
							this.whereNode.add(
								(WhereNodeElement) curComposedList.get(i));
						}
					}
					break;

				case LogicOperator.OR :
					if (this.myManager.isSQLStatementCrossJoinOptimised()) {
						// COMPOSED_OR': [popCOMPOSED_OR, curCOMPOSED_OR]
						this.whereNode = popWhereNode;
						for (int i = 0; i < curComposedList.size(); i++) {
							this.whereNode.add(
								(WhereNodeElement) curComposedList.get(i));
						}
					} else {
						//   OR:  SIMPLE:
						//        trMAP = null, jcMAP = null, aliasAL = null
						//        SC = EXISTS(popCOMPOSED_AND1) AND ... AND EXISTS(popCOMPOSED_ANDn) 
						//             OR
						//             EXISTS(curCOMPOSED_AND1) AND ... AND EXISTS(curCOMPOSED_ANDm)
						//
						List<SearchCondition> booleanPopAndList = new ArrayList<SearchCondition>();
						for (int i = 0; i < popComposedList.size(); i++) {
							WhereNodeElement elem =
								(WhereNodeElement) popComposedList.get(i);
							SearchCondition elemExists =
								this.myManager.buildExists(
									elem.getTableRepresentations(),
									elem.getJoinConditions(),
									elem.getAliases(),
									elem.getCondition());
							if (elemExists != null) {
								booleanPopAndList.add(elemExists);
							}
						}

						List<SearchCondition> booleanCurAndList = new ArrayList<SearchCondition>();
						for (int i = 0; i < curComposedList.size(); i++) {
							WhereNodeElement elem =
								(WhereNodeElement) curComposedList.get(i);
							SearchCondition elemExists =
								this.myManager.buildExists(
									elem.getTableRepresentations(),
									elem.getJoinConditions(),
									elem.getAliases(),
									elem.getCondition());
							if (elemExists != null) {
								booleanCurAndList.add(elemExists);
							}
						}

						SearchCondition booleanPopAnd = null;
						SearchCondition booleanCurAnd = null;

						if (booleanPopAndList.size() > 0) {
							SearchCondition[] booleanPopAndArray =
								new SearchCondition[booleanPopAndList.size()];
							booleanPopAndList.toArray(booleanPopAndArray);
							booleanPopAnd = BUILDER.createBooleanAnd(booleanPopAndArray).build();
						}

						if (booleanCurAndList.size() > 0) {
							SearchCondition[] booleanCurAndArray =
								new SearchCondition[booleanCurAndList.size()];
							booleanCurAndList.toArray(booleanCurAndArray);
							booleanCurAnd = BUILDER.createBooleanAnd(booleanCurAndArray).build();
						}

						SearchCondition[] orList;
						if (booleanPopAnd != null) {
							if (booleanCurAnd != null) {
								orList =
									new SearchCondition[] {
										booleanPopAnd,
										booleanCurAnd };
							} else {
								orList =
									new SearchCondition[] { booleanPopAnd };
							}
						} else if (booleanCurAnd != null) {
							orList = new SearchCondition[] { booleanCurAnd };
						} else {
							orList =
								new SearchCondition[] { BUILDER.createNeutralCondition().build()};
						}

						this.whereNode.setSimple(
							new WhereNodeElement(
								null,
								null,
								null,
                                BUILDER.createBooleanOr(orList).build()));
					}
					break;

				default :
					DevTrace.exitingWithException(loc, popNode);
					throw new SQLMappingException(
						"Unexpected logic operator as argument of popNode(int).",
						"The EJBQLTreeProcessor has called popNode method "
							+ "of the SQLTreeNodeManager with an unexpected "
							+ "LogicOperator "
							+ logicOperator
							+ ".",
						"CSM026");
			}

		}

		DevTrace.exiting(loc, popNode);
	}

	/**
	 * Returns the current proccessing unit; current where node resp.
	 */
	WhereNode getWhereNode() {
		return this.whereNode;
	}

	/**
	 * Checks whether the processing stack is empty; if stack is not
	 * empty, this methods throws an SQLMappingException
	 */
	void checkWhereStackisNull() throws SQLMappingException {
		if (this.whereStack.isEmpty() == false) {
			throw new SQLMappingException(
				"The where clause stack is not empty.",
				"There exists still "
					+ this.whereStack.size()
					+ " elements in the where clause stack, "
					+ " which are not processed.",
				"CSM026");

		}
	}

	/**
	 * Pushes the processing unit on top of the stack, if the
	 * processing unit is not null
	 */
	private void pushWhereNodeIfNotNull() {
		if (this.whereNode != null) {
			this.whereStack.push(this.whereNode);
		}
	}

	/**
	 * Operator has to be LIKE or NOT_LIKE.
	 * beanField has to be a cmp field; relations are recognized.
	 */
	private Relation prepareLike(
		int operator,
		BeanField beanField,
		BeanField[] relation)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] =
				{ new Integer(operator), beanField, relation };
			DevTrace.entering(loc, prepareLike, prepareLikeParms, inputValues);
		}

		if (operator != ComparativeOperator.LIKE
			&& operator != ComparativeOperator.NOT_LIKE) {
			DevTrace.exitingWithException(loc, prepareLike);
			throw new SQLMappingException(
				"Unexpected comparative operator as argument of prepareLike().",
				"The EJBQLTreeProcessor has called a makeNode method "
					+ "of the SQLTreeNodeManager with an unexpected "
					+ "ComparativeOperator "
					+ operator
					+ ".",
				"CSM028");

		}
		this.pushWhereNodeIfNotNull();
		String fieldName = beanField.getFieldName();
		String beanType = beanField.getParentType();
		CMPFieldDescriptor fd =
			this.orMapping.getCMPBeanField(beanType, fieldName);
		int jdbcType = fd.getJdbcType();
		DatabaseColumn columnName = fd.getColumnName();

		Relation computedRelation = null;

		this.myManager.checkJdbcTypeForComparability(
			jdbcType,
			fieldName,
			beanType,
			columnName.getName(this.myManager.isForNativeUseOnly()));

		computedRelation = this.myManager.computeRelation(relation, true);

		DevTrace.exiting(loc, prepareLike, computedRelation);
		return computedRelation;
	}

	/**
	 * Operator has to be MEMBER or NOT_MEMBER; exception is thrown otherwise.
	 */
	private void prepareMember(int operator) throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { new Integer(operator)};
			DevTrace.entering(
				loc,
				prepareMember,
				prepareMemberParms,
				inputValues);
		}

		if (operator != ComparativeOperator.MEMBER
			&& operator != ComparativeOperator.NOT_MEMBER) {
			DevTrace.exitingWithException(loc, prepareMember);
			throw new SQLMappingException(
				"Unexpected comparative operator as argument of prepareMember().",
				"The EJBQLTreeProcessor has called a makeNode method "
					+ "of the SQLTreeNodeManager with an unexpected "
					+ "ComparativeOperator "
					+ operator
					+ ".",
				"CSM030");
		}

		DevTrace.exiting(loc, prepareMember);
	}

}
