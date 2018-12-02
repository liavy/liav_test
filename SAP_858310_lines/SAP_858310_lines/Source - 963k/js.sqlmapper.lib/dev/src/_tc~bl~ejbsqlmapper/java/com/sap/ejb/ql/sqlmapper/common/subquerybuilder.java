package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager;
import com.sap.ejb.ql.sqlmapper.common.ORMappingManager;
import com.sap.ejb.ql.sqlmapper.common.BeanField;
import com.sap.ejb.ql.sqlmapper.common.PKBeanDescriptor;
import com.sap.ejb.ql.sqlmapper.common.CMRFieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.M2MDescriptor;
import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;
import com.sap.ejb.ql.sqlmapper.common.BeanTable;
import com.sap.ejb.ql.sqlmapper.common.TableRepresentation;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;

import com.sap.sql.tree.ComparisonOperator;
import com.sap.sql.tree.NeutralCondition;
import com.sap.sql.tree.Query;
import com.sap.sql.tree.SQLStatement;
import com.sap.sql.tree.SearchCondition;
import com.sap.sql.tree.SelectSublist;
import com.sap.sql.tree.SqlTreeBuilder;
import com.sap.sql.tree.ejb21_ColumnReference;
import com.sap.sql.tree.ejb21_TableReference;
import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class builds a subquery for an SQL representation of an EJB-QL expression that
 * contains an EMPTY, NOT_EMPTY, MEMBER or NOT_MEMBER predicate in the where clause.
 * For that purpose method <code>buildSubquery</code> creates a <code>QuerySpecification</code>
 * representing a join on the involved tables. 
 * <p></p>
 * An <code>SubqueryBuilder</code> instance is created with a given
 * </code>SQLTreeNodeManager</code> object. Each build of a sub query
 * has to be started by a call of method <code>prepare</code>.
 * <p></p>
 * Method <code>buildSubquery(relation, withHelper)</code>
 * has to be invoked with a relation path 
 * (array of BeanFields) and a flag indicating whether a helper table 
 * for MANY_TO_MANY relations is involved. This is the case, if the last cmr
 * field in the relation path is of multiplicity MANY_TO_MANY.
 * The select list of this subquery is build out of the primary key columns
 * of the last cmr field in the relation path.
 * For each cmr field in the path, one table is added to the from clause.
 * If a helper table is involved this one is also added. The where clause
 * of the subquery is build out of the appropiate join conditions.
 * <p></p>
 * Method <code>buildSubquery(columns, tableName)</code> builds
 * a subquery, where just the given columns are selected from the given 
 * tableName.
 * </p><p>
 * Before a subsequent build of a subquery with this 
 * <code>SubqueryBuilder</code> instance it has to be reset 
 * by invocation of method <code>clear()</code>.
 * 
 * Copyright (c) 2002-2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 * @see com.sap.sql.tree.QuerySpecification
 **/

public class SubqueryBuilder {

	private static final Location loc =
		Location.getLocation(SubqueryBuilder.class);
	private static final String buildSubquery = "buildSubquery";
	private static final String buildSubqueryRelationParms[] =
		{ "relation", "withHelper" };
	private static final String buildSubqueryColumnsOfTableParms[] =
		{ "columns", "beanTable" };
	private static final String buildSubqueryTrJcScParms[] =
		{ "tableReferences", "joinConditions", "searchCondition" };
    private static final SqlTreeBuilder BUILDER = SqlTreeBuilder.getInstance();
	private SQLTreeNodeManager myManager = null;
	private ORMappingManager orMapping = null;
	private boolean cleared = true;

	SubqueryBuilder(SQLTreeNodeManager aManager) {
		this.myManager = aManager;
		this.orMapping = myManager.getORMapping();
	}

	void prepare() {
		// currently nothing to prepare.
		if (!this.cleared) {
			this.clear();
		}
		this.cleared = false;
	}

	void clear() {
		this.cleared = true;
	}

	Query buildSubquery(BeanField[] relation, boolean withHelper)
		throws SQLMappingException {
		// relation: cmr1.cmr2.cmr3, cmr3: collectionValued 
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { relation, new Boolean(withHelper)};
			DevTrace.entering(
				loc,
				buildSubquery,
				buildSubqueryRelationParms,
				inputValues);
		}

		int count = relation.length;
		String selectListBean = relation[count - 1].getType();
		PKBeanDescriptor bd = this.orMapping.getBeanPK(selectListBean);
		DatabaseColumn[] selectListColumns = bd.getColumnNames();
		SelectSublist[] selectList =
			new SelectSublist[selectListColumns.length];
		BeanTable selectListBeanTable = bd.getBeanTable();
		String selectListAlias = this.myManager.getNewAliasName();
		for (int i = 0; i < selectListColumns.length; i++) {
			selectList[i] = BUILDER.createSelectSublist().setValue(
                    new ejb21_ColumnReference(selectListAlias,
					selectListColumns[i].getName(
						this.myManager.isForNativeUseOnly()),
					this.myManager.isForNativeUseOnly()
						|| selectListColumns[i].isNative())).build();
		}
		Query[] fromList = new Query[(withHelper) ? count + 1 : count];
		ejb21_TableReference tableReference =
			this.myManager.createTableReferenceDetectNativeNames(
				selectListBeanTable,
				selectListAlias);
		fromList[0] = tableReference;

		List<SearchCondition> andList = new ArrayList<SearchCondition>();
		String parentAlias = relation[0].getParentName();
		// cmr1.cmr2
		for (int i = 0; i < count - 1; i++) {
			String actualAlias = this.myManager.getNewAliasName();
			String actualType = relation[i].getType();
			bd = this.orMapping.getBeanPK(actualType);

			BeanTable beanTable = bd.getBeanTable();
			tableReference =
				myManager.createTableReferenceDetectNativeNames(
					beanTable,
					actualAlias);
			fromList[i + 1] = tableReference;

			String parentType = relation[i].getParentType();
			String fieldName = relation[i].getFieldName();
			CMRFieldDescriptor fd =
				this.orMapping.getCMRBeanField(parentType, fieldName);

			DatabaseColumn[] fkColumns = fd.getColumnNames();
			DatabaseColumn[] pkColumns = bd.getColumnNames();
			String fkQualifier = parentAlias;
			String pkQualifier = actualAlias;
			if (fkColumns.length == 0) {
				//single valued cmr has no foreign key db columns
				//take foreign key columns from other side
				fkColumns = fd.getColumnNamesOfOtherSide();
				PKBeanDescriptor bd2 = this.orMapping.getBeanPK(parentType);
				pkColumns = bd2.getColumnNames();
				fkQualifier = actualAlias;
				pkQualifier = parentAlias;
			}
			for (int j = 0; j < fkColumns.length; j++) {
				andList.add(
                    BUILDER.createComparisonPredicate(
					new ejb21_ColumnReference(fkQualifier,
						fkColumns[j].getName(
							this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| fkColumns[j].isNative()),
					ComparisonOperator.EQUAL,
					new ejb21_ColumnReference(
						pkQualifier,
						pkColumns[j].getName(
							this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| pkColumns[j].isNative())).build());
			}
			parentAlias = actualAlias;
		}

		// cmr3, collectionValued
		if (withHelper) {
			CMRFieldDescriptor fd =
				this.orMapping.getCMRBeanField(
					relation[count - 1].getParentType(),
					relation[count - 1].getFieldName());
			M2MDescriptor m2mDescr = fd.getM2MDescriptor();
			CheckableIdentifier helperTableIdentifier =
				fd.getHelperTableIdentifier();
			String helperAlias = this.myManager.getNewAliasName();

			tableReference =
				this.myManager.createTableReferenceDetectNativeNames(
					helperTableIdentifier,
					helperAlias);
			fromList[count] = tableReference;

			DatabaseColumn[] helperColumns = m2mDescr.getColumnsToRefTable();
			for (int i = 0; i < selectListColumns.length; i++) {
				andList.add(
                    BUILDER.createComparisonPredicate(
					new ejb21_ColumnReference(selectListAlias,
						selectListColumns[i].getName(
							this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| selectListColumns[i].isNative()),
					ComparisonOperator.EQUAL,
					new ejb21_ColumnReference(
						helperAlias,
						helperColumns[i].getName(
							this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| helperColumns[i].isNative())).build());
			}

			bd = this.orMapping.getBeanPK(relation[count - 1].getParentType());
			DatabaseColumn[] pkColumns = bd.getColumnNames();
			helperColumns = m2mDescr.getColumnsToMyTable();
			for (int i = 0; i < pkColumns.length; i++) {
				andList.add(BUILDER.createComparisonPredicate(
					new ejb21_ColumnReference(parentAlias,
						pkColumns[i].getName(
							this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| pkColumns[i].isNative()),
					ComparisonOperator.EQUAL,
					new ejb21_ColumnReference(
						helperAlias,
						helperColumns[i].getName(
							this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| helperColumns[i].isNative())).build());
			}
		} else {
			String parentType = relation[count - 1].getParentType();
			bd = this.orMapping.getBeanPK(parentType);
			DatabaseColumn[] pkColumns = bd.getColumnNames();
			CMRFieldDescriptor fd =
				this.orMapping.getCMRBeanField(
					parentType,
					relation[count - 1].getFieldName());
			DatabaseColumn[] fkColumns = fd.getColumnNamesOfOtherSide();
			for (int i = 0; i < fkColumns.length; i++) {
				andList.add(BUILDER.createComparisonPredicate(
					new ejb21_ColumnReference(parentAlias,
						pkColumns[i].getName(
							this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| pkColumns[i].isNative()),
					ComparisonOperator.EQUAL,
					new ejb21_ColumnReference(
						selectListAlias,
						fkColumns[i].getName(
							this.myManager.isForNativeUseOnly()),
						this.myManager.isForNativeUseOnly()
							|| fkColumns[i].isNative())).build());
			}
		}

		SearchCondition[] andListArray = new SearchCondition[andList.size()];
		andList.toArray(andListArray);
		SearchCondition whereCondition = BUILDER.createBooleanAnd(andListArray).build();

		Query subQuery = BUILDER.createQuerySpecification(fromList).setSelectList(selectList).setWhereClause(whereCondition).build();

		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.debugInfo(
				loc,
				buildSubquery,
				"constructed subquery = { "
					+ subQuery.toSqlString(SQLStatement.VENDOR_UNKNOWN)
					+ " }");
		}

		DevTrace.exiting(loc, buildSubquery, subQuery);

		return subQuery;
	}

	Query buildSubquery(DatabaseColumn[] columns, BeanTable beanTable) {
		// select columns from tableName
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { columns, beanTable };
			DevTrace.entering(
				loc,
				buildSubquery,
				buildSubqueryColumnsOfTableParms,
				inputValues);
		}

		String aliasName = this.myManager.getNewAliasName();

		SelectSublist[] selectList = new SelectSublist[columns.length];

		for (int i = 0; i < columns.length; i++) {
			selectList[i] = BUILDER.createSelectSublist().setValue(
                    new ejb21_ColumnReference(aliasName,
					columns[i].getName(this.myManager.isForNativeUseOnly()),
					this.myManager.isForNativeUseOnly()
						|| columns[i].isNative())).build();
		}

		ejb21_TableReference tableReference =
			this.myManager.createTableReferenceDetectNativeNames(
				beanTable,
				aliasName);

		Query subQuery = BUILDER.createQuerySpecification(tableReference).setSelectList(selectList).build();

		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.debugInfo(
				loc,
				buildSubquery,
				"constructed subquery = { "
					+ subQuery.toSqlString(SQLStatement.VENDOR_UNKNOWN)
					+ " }");
		}

		DevTrace.exiting(loc, buildSubquery, subQuery);

		return subQuery;

	}

	Query buildSubquery(HashMap trMap, HashMap jcMap, SearchCondition sc)
		throws SQLMappingException {

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { trMap, jcMap, sc };
			DevTrace.entering(
				loc,
				buildSubquery,
				buildSubqueryTrJcScParms,
				inputValues);
		}

		// select clause
		Iterator tabIter = trMap.values().iterator();
		TableRepresentation tabRep = (TableRepresentation) tabIter.next();
		ejb21_TableReference tabRef = tabRep.getTabRef();
		DatabaseColumn pkColumnName = tabRep.getOneOfThePrimaryKeyColumns();
		SelectSublist selectList = BUILDER.createSelectSublist().setValue(
				new ejb21_ColumnReference(tabRef
					.getAliasName(),
				pkColumnName.getName(this.myManager.isForNativeUseOnly()),
				this.myManager.isForNativeUseOnly()
					|| pkColumnName.isNative())).build();

		// from clause
		Query[] fromList = new Query[trMap.size()];
		fromList[0] = tabRef;
		int i = 1;
		while (tabIter.hasNext()) {
			ejb21_TableReference tableReference =
				((TableRepresentation) tabIter.next()).getTabRef();
			fromList[i] = tableReference;
			i++;
		}

		// where clause
		int andCount = jcMap.size();
		if (sc != null && !(sc instanceof NeutralCondition)) {
			andCount++;
		}
		SearchCondition[] andList = new SearchCondition[andCount];
		Iterator condIter = jcMap.values().iterator();
		i = 0;
		while (condIter.hasNext()) {
			andList[i] = (SearchCondition) condIter.next();
			i++;
		}
		if (sc != null && !(sc instanceof NeutralCondition)) {
			andList[i] = sc;
		}

		Query subQuery = BUILDER.createQuerySpecification(fromList).setSelectList(selectList).setWhereClause(BUILDER.createBooleanAnd(andList).build()).build();

		if (DevTrace.isOnDebugLevel(loc)) {
			DevTrace.debugInfo(
				loc,
				buildSubquery,
				"constructed subquery = { "
					+ subQuery.toSqlString(SQLStatement.VENDOR_UNKNOWN)
					+ " }");
		}

		DevTrace.exiting(loc, buildSubquery, subQuery);

		return subQuery;
	}
}
