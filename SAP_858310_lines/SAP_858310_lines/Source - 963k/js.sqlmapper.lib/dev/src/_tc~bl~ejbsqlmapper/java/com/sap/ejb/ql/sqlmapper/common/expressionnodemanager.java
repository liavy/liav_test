package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.NoReasonableStatementException;
import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager;
import com.sap.ejb.ql.sqlmapper.common.ORMappingManager;
import com.sap.ejb.ql.sqlmapper.common.PKBeanDescriptor;
import com.sap.ejb.ql.sqlmapper.common.CMPFieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.CMRFieldDescriptor;
import com.sap.ejb.ql.sqlmapper.common.BeanDescriptor;
import com.sap.ejb.ql.sqlmapper.common.InputParameter;
import com.sap.ejb.ql.sqlmapper.common.InputParameterOccurrence;
import com.sap.ejb.ql.sqlmapper.common.DatabaseColumn;
import com.sap.ejb.ql.sqlmapper.common.BeanField;
import com.sap.ejb.ql.sqlmapper.common.BeanObject;
import com.sap.ejb.ql.sqlmapper.common.LiteralValue;
import com.sap.ejb.ql.sqlmapper.common.HelperTableFacade;
import com.sap.ejb.ql.sqlmapper.common.ExpressionNode;
import com.sap.ejb.ql.sqlmapper.common.Relation;
import com.sap.sql.tree.ArithmeticExpressionOperator;
import com.sap.sql.tree.TableAlias;
import com.sap.sql.tree.ValueExpression;
import com.sap.sql.tree.ejb21_ColumnReference;

import com.sap.sql.tree.SqlTreeBuilder;

import com.sap.sql.types.CommonTypes;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

import java.util.Map;
import java.util.Stack;
import java.util.HashMap;
import java.util.ArrayList;

import java.math.BigDecimal;

/**
 * This class is to assemble an SQL representation of an EJB-QL expression.
 * For that purpose, the class is designed as a push down automaton,
 * that is controlled by the <code>Processor</code> classes via method calls.
 * </p><p>
 * The push down automaton disposes of one processing unit and one stack.
 * Via its <code>makeExpression()</code> methods one may create a new expression
 * within the processing unit. If there has been another expression already 
 * present in the processing unit that previous expression is pushed on top
 * of the stack. With the <code>alterExpression()</code> method one may
 * change the expression currently in the processing unit by adding a monadic
 * operator, with the <code>addFunctionToExpression()</code> by adding a 
 * one-argument built-in 
 * function. The <code>popExpression()</code> functions pulls the top element
 * from the stack and combines it (in that order) with the expression currently
 * in the processing unit by a dyadic operator; the result is placed into
 * the processing unit. The <code>popExpressionWithFunction()</code> method does
 * the same, using a two-argument built-in function instead of a dyadic operator.
 * Furthermore the <code>doublePopExpressionWithFunction()</code> method
 * pulls the top and the secondmost top element from the stack, and combines
 * the secondmost top element, the top element and the expression currently
 * in the processing unit (in that order) by a three-argument built-in function;
 * once again the result is placed into the processing unit.
 * Finally, the <code>endOfExpression()</code> method returns the expression
 * that is currently in the processing unit in a one-element array and clears the 
 * processing unit.
 * In case of entity bean expressions, however, with an abstract schema type that disposes
 * of a compound primary key with n primary key fields (n being a natural number
 * greater or equal 2) <code>endOfExpression()</code>, besides of the element
 * that is currently in the processing unit, will also pull the n - 1 topmost
 * elements from the stack and return them in the resulting array.
 * </p><p>
 * An <code>ExpressionNodeManager</code> instance is created with an
 * <code>SQLTreeNodeManager</code> object as argument, from which it retrieves
 * its <code>ORMappingManager</code>.
 * Assembling an SQL representation of an EJB-QL expression
 * has to be started by invoking of the <code>prepare</code> method
 * in order to fully initialise the <code>ExpressionNodeManager</code> instance
 * with an <code>InputParameterList</code>. 
 * Before a subsequenti assembly process the <code>ExpressionNodeManager</code> instance has
 * to be reset by invoking method <code>clear()</code>. 
 * </p><p>
 * Copyright (c) 2002-2006, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.2
 * @see com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.ORMappingManager
 * @see com.sap.ejb.ql.sqlmapper.common.InputParameter
 **/
public class ExpressionNodeManager {
    // Constants
    /**
     * This constant identifies a scalar function as of type ABS.
     */
    public static final int ABS = 1;
    /**
     * This constant identifies a scalar function as of type SQRT.
     */
    public static final int SQRT = 2;
    /**
     * This constant identifies a scalar function as of type MOD.
     */
    public static final int MOD = 3;
    /**
     * This constant identifies a scalar function as of type LENGTH.
     */
    public static final int LENGTH = 10;
    /**
     * This constant identifies a scalar function as of type LOCATE.
     */
    public static final int LOCATE = 11;
    /**
     * This constant identifies a scalar function as of type SUBSTRING.
     */
    public static final int SUBSTRING = 12;
    /**
     * This constant identifies a scalar function as of type CONCAT.
     */
    public static final int CONCAT = 13;

	private static final Location loc =
		Location.getLocation(ExpressionNodeManager.class);
	private static final String prepare = "prepare";
	private static final String clear = "clear";
	private static final String makeExpression = "makeExpression";
	private static final String makeSetExpression = "makeSetExpression";
	private static final String makeInsertExpression = "makeInsertExpression";
	private static final String addFunctionToExpression =
		"addFunctionToExpression";
	private static final String doublePopExpressionWithFunction =
		"doublePopExpressionWithFunction";
	private static final String popExpressionWithFunction =
		"popExpressionWithFunction";
	private static final String alterExpression = "alterExpression";
	private static final String popExpression = "popExpression";
	private static final String endOfExpression = "endOfExpression";
	private static final String makeExpressionPathParms[] =
		{ "beanField", "relation" };
	private static final String makeExpressionInputParameterParms[] =
		{ "occurrence" };
	private static final String makeExpressionIdentificationVariableParms[] =
		{ "beanObject" };
	private static final String makeExpressionLiteralParms[] = { "literal" };
	private static final String makeExpressionHelperTableParms2[] =
		{ "helperTable" , "key"};
	private static final String makeSetExpressionIdentificationVariableParms[] =
		{ "beanObject" };
	private static final String makeInsertExpressionIdentificationVariableParms[] =
		{ "beanObject" };
	private static final String addFunctionToExpressionParms[] =
		{ "scalarFunction" };
	private static final String doublePopExpressionWithFunctionParms[] =
		{ "scalarFunction" };
	private static final String popExpressionWithFunctionParms[] =
		{ "scalarFunction" };
	private static final String alterExpressionParms[] = { "operator" };
	private static final String popExpressionParms[] = { "operator" };

    private static final SqlTreeBuilder BUILDER = SqlTreeBuilder.getInstance();
	private SQLTreeNodeManager myManager = null;
	private ORMappingManager orMapping = null;

	private Map<Integer,InputParameter> inputParameterList = null;
	private Stack<ExpressionNode> expStack = null;
	private ExpressionNode expNode = null;
	private int multitude = 1;
	private boolean cleared = true;

	/**
	 * Creates an <code>ExpressionNodeManager</code> instance with
	 * an <code>SQLTreeNodeManager</code> object as argument.
	 * From the <code>SQLTreeNodeManager</code> object, the
	 * <code>ORMappingManager</code> and the <code>InputParameterList</code>
	 * is retrieved.
	 * </p><p>
	 * @param aManager
	 *     associated <code>SQLTreeNodeManager</code> object.
	 **/
	ExpressionNodeManager(SQLTreeNodeManager aManager) {
		this.myManager = aManager;
		this.orMapping = aManager.getORMapping();
		this.expStack = new Stack<ExpressionNode>();
	}

	/**
	 * Prepares an <code>ExpressionNodeManager</code> instance for processing
	 * an EJB-QL query specification.
	 * </p><p>
	 * Unless at first call, this method implicitely calls the <code>clear()</code>
	 * method if caller has omitted to do so after preceeding call of this
	 * <code>prepare()</code> method.
	 */
	void prepare() {
		DevTrace.debugInfo(loc, prepare, "preparing.");

		if (!this.cleared) {
			this.clear();
		}
		this.cleared = false;
		this.inputParameterList = this.myManager.getInputParameterList();

		return;
	}

	/**
	 * Tells an <code>ExpressionNodeManager</code> instance that processing
	 * of an EJBQL query specification has been finished and does the necessary
	 * clean-up.
	 */
	void clear() {
		DevTrace.debugInfo(loc, clear, "clearing.");

		this.expStack.clear();
		this.expNode = null;
		this.multitude = 1;
		this.inputParameterList = null;
		this.cleared = true;
	}

	/**
	 * Creates SQL representation of a path expression.
	 * The relations, given as an array of all cmr bean
	 * fields contained in a path, are recognized with
	 * method <code>recognizeRelations</code>. This method
	 * will add necessary join conditions to the SQL representation
	 * for each relation in the path, if this has not allready
	 * been done by an other recognization of this relation in
	 * another path.
	     * <p></p>
	 * @param beanField
	 *     path destination.
	 * @param relation
	 *     array of all cmr bean field's contained in path.
	 * @throws SQLMappingException
	 *     if an error is encountered during processing.
	 **/
	void makeExpression(BeanField beanField, BeanField[] relation)
		throws SQLMappingException {
		// Path
		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanField, relation };
			DevTrace.entering(
				loc,
				makeExpression,
				makeExpressionPathParms,
				inputValues);
		}

		this.pushExpNodeIfNotNull();

		if (beanField.isRelation()) {
			DevTrace.debugInfo(loc, makeExpression, "CMR path.");

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

			DatabaseColumn[] columnName = fieldDescr.getColumnNames();

			boolean includeLast = false;

			if (columnName.length == 0) {
				PKBeanDescriptor pkBeanDesc =
					this.orMapping.getBeanPK(beanField.getType());
				columnName = pkBeanDesc.getColumnNames();
				includeLast = true;
			}

			String qualifier = null;
			Relation computedRelation = null;
			HashMap tableRepMap = null;
			HashMap joinConditionMap = null;
			ArrayList aliasList = null;
			TableAlias tableAlias = null;

			if (relation == null || relation.length == 0) {
				qualifier = this.myManager.getAliasForIdentifier(
                                                             beanField.getParentName());
                                if ( qualifier == null )
                                {
                                  DevTrace.exitingWithException(loc, makeExpression);
                                  throw new SQLMappingException("Unrecognized identifier : "
                                               + beanField.getParentName(),
                                               "No alias has been precomputed for identifier "
                                               + beanField.getParentName()
                                               + ". This is an internal programming error "
                                               + "of the SQL mapper or the EJB-QL parser. "
                                               + "Please kindly open up a problem ticket on "
                                               + "component BC-JAS-PER-DBI.",
                                               "CSM137");
                                }
			} else {
				computedRelation =
					this.myManager.computeRelation(relation, includeLast);
				tableRepMap = computedRelation.getTableRepresentations();
				joinConditionMap = computedRelation.getJoinConditions();
				qualifier = computedRelation.getAliasName();
				aliasList = computedRelation.getAliases();
				if (aliasList != null) {
					tableAlias =
						(TableAlias) (aliasList.get(aliasList.size() - 1));
				}
			}

			// join Conditions only to first Expression should be enough
			ejb21_ColumnReference columnReference = null;
			if (tableAlias == null) {
				columnReference =
					new ejb21_ColumnReference(
						qualifier,
						columnName[0].getName(this.myManager.isForNativeUseOnly()),
                                                this.myManager.isForNativeUseOnly() || columnName[0].isNative());
			} else {
				columnReference =
					new ejb21_ColumnReference(
						tableAlias,
                                                columnName[0].getName(this.myManager.isForNativeUseOnly()),
                                                this.myManager.isForNativeUseOnly() || columnName[0].isNative());
			}

			this.expNode =
				new ExpressionNode(
					tableRepMap,
					joinConditionMap,
					aliasList,
					columnReference);

			for (int i = 1; i < columnName.length; i++) {
				this.expStack.push(this.expNode);

				ejb21_ColumnReference myColumnReference = null;
				if (tableAlias == null) {
					myColumnReference =
						new ejb21_ColumnReference(
							qualifier,
							columnName[i].getName(this.myManager.isForNativeUseOnly()),
                                                        this.myManager.isForNativeUseOnly() || columnName[i].isNative());
				} else {
					myColumnReference =
						new ejb21_ColumnReference(
							tableAlias,
                                                        columnName[i].getName(this.myManager.isForNativeUseOnly()),
                                                        this.myManager.isForNativeUseOnly() || columnName[i].isNative());
				}

				this.expNode =
					new ExpressionNode(null, null, null, myColumnReference);
			}
			this.multitude = columnName.length;
		} else {
			DevTrace.debugInfo(loc, makeExpression, "CMP path.");

			String qualifier = null;
			Relation computedRelation = null;
			HashMap tableRepMap = null;
			HashMap joinConditionMap = null;
			ArrayList aliasList = null;
			TableAlias tableAlias = null;

			if (relation == null || relation.length == 0) {
				qualifier = this.myManager.getAliasForIdentifier(
                                                               beanField.getParentName());
                                if ( qualifier == null )
                                {
                                  DevTrace.exitingWithException(loc, makeExpression);
                                  throw new SQLMappingException("Unrecognized identifier : "
                                               + beanField.getParentName(),
                                               "No alias has been precomputed for identifier "
                                               + beanField.getParentName()
                                               + ". This is an internal programming error "
                                               + "of the SQL mapper or the EJB-QL parser. "
                                               + "Please kindly open up a problem ticket on "
                                               + "component BC-JAS-PER-DBI.",
                                               "CSM139");
                               }
			} else {
				computedRelation =
					this.myManager.computeRelation(relation, true);
				tableRepMap = computedRelation.getTableRepresentations();
				joinConditionMap = computedRelation.getJoinConditions();
				qualifier = computedRelation.getAliasName();
				aliasList = computedRelation.getAliases();
				if (aliasList != null) {
					tableAlias =
						(TableAlias) (aliasList.get(aliasList.size() - 1));
				}
			}

			CMPFieldDescriptor fieldDescr =
				this.orMapping.getCMPBeanField(
					beanField.getParentType(),
					beanField.getFieldName());

			if (!CommonTypes.isComparable(fieldDescr.getJdbcType())) {
				DevTrace.exitingWithException(loc, makeExpression);
				throw new SQLMappingException(
					"Jdbc type "
						+ fieldDescr.getJdbcType()
						+ " is not comparable.",
					"SQL Mapper has encountered an incomparable jdbc type within an expression of a where clause."
						+ " Affected field is "
						+ beanField.getFieldName()
						+ " of bean type "
						+ beanField.getParentType()
						+ ". Assigned database column is "
						+ fieldDescr.getColumnName().getName(this.myManager.isForNativeUseOnly())
						+ ".",
					"CSM044");
			}

			ejb21_ColumnReference columnReference = null;
			if (tableAlias == null) {
				columnReference =
					new ejb21_ColumnReference(
						qualifier,
						fieldDescr.getColumnName().getName(this.myManager.isForNativeUseOnly()),
                                                this.myManager.isForNativeUseOnly() || fieldDescr.getColumnName().isNative());
			} else {
				columnReference =
					new ejb21_ColumnReference(
						tableAlias,
                                                fieldDescr.getColumnName().getName(this.myManager.isForNativeUseOnly()),
                                                this.myManager.isForNativeUseOnly() || fieldDescr.getColumnName().isNative());
			}

			this.expNode =
				new ExpressionNode(
					tableRepMap,
					joinConditionMap,
					aliasList,
					columnReference);
		}

		DevTrace.exiting(loc, makeExpression);
		return;

	}

	/**
	 * Creates SQL representation of an input parameter's occurrence within an expression.
	     * </p><p>
	 * @param occurrence
	 *     description of current occurrence of an EJB-QL input parameter.
	 * @throws SQLMappingException
	 *     if an error is encountered during processing.
	 **/
	void makeExpression(InputParameterOccurrence occurrence)
		throws SQLMappingException {
		// Input Parameter

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { occurrence };
			DevTrace.entering(
				loc,
				makeExpression,
				makeExpressionInputParameterParms,
				inputValues);
		}

		this.pushExpNodeIfNotNull();

		InputParameter input = 
            this.inputParameterList.get(
				Integer.valueOf(occurrence.getInputParameterNumber()));

		int start = occurrence.getOccurrence() + 1;
		int count = input.getCount();

		this.expNode =
			new ExpressionNode(null, null, null, BUILDER.createHostVariable(start).build());

		for (int i = 1; i < count; i++) {
			this.expStack.push(this.expNode);
			this.expNode =
				new ExpressionNode(
					null,
					null,
					null,
                    BUILDER.createHostVariable(start + 1).build());
		}

		this.multitude = count;
		DevTrace.debugInfo(
			loc,
			makeExpression,
			"Multitude set to " + count + ".");
		DevTrace.exiting(loc, makeExpression);
		return;

	}

	/**
	 * Creates SQL representation of the occurrence of an entity bean within
	 * an expression. This implies that triggering EJB-QL expression is an
	 * entity bean expression.
	     * </p><p>
	 * @param beanObject
	 *     description of the entity bean.
	 * @throws SQLMappingException
	 *     if an error is encountered during processing.
	 **/
	void makeExpression(BeanObject beanObject) throws SQLMappingException {
		// Identification Variable

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanObject };
			DevTrace.entering(
				loc,
				makeExpression,
				makeExpressionIdentificationVariableParms,
				inputValues);
		}

		this.pushExpNodeIfNotNull();

		BeanDescriptor beanDescriptor =
			this.orMapping.getBeanPK(beanObject.getType());
		DatabaseColumn[] columnName = beanDescriptor.getColumnNames();
                String alias;
                if ( beanObject.getIdentifier() == null )
                {
                  alias = null;
                }
                else
                {
                  alias = this.myManager.getAliasForIdentifier(beanObject.getIdentifier());
                  if ( alias == null )
                  {
                    DevTrace.exitingWithException(loc, makeExpression);
                    throw new SQLMappingException("Unrecognized identifier : "
                                 + beanObject.getIdentifier(),
                                 "No alias has been precomputed for identifier "
                                 + beanObject.getIdentifier()
                                 + ". This is an internal programming error "
                                 + "of the SQL mapper or the EJB-QL parser. "
                                 + "Please kindly open up a problem ticket on "
                                 + "component BC-JAS-PER-DBI.",
                                 "CSM147");
                  }
                }

		this.expNode =
			new ExpressionNode(
				null,
				null,
				null,
				new ejb21_ColumnReference(alias, columnName[0].getName(this.myManager.isForNativeUseOnly()),
                                                           this.myManager.isForNativeUseOnly()||columnName[0].isNative()));

		for (int i = 1; i < columnName.length; i++) {
			this.expStack.push(this.expNode);
			this.expNode =
				new ExpressionNode(
					null,
					null,
					null,
					new ejb21_ColumnReference(
						alias,
						columnName[i].getName(this.myManager.isForNativeUseOnly()),
                                                this.myManager.isForNativeUseOnly()||columnName[i].isNative()));
		}

		this.multitude = columnName.length;
		DevTrace.debugInfo(
			loc,
			makeExpression,
			"Multitude set to " + this.multitude + ".");
		DevTrace.exiting(loc, makeExpression);
		return;

	}

	/**
	 * Creates SQL representation of a literal value.
	     * </p><p>
	 * @param literal
	 *     description of the literal value.
	 * @throws SQLMappingException
	 *     if an error is encountered during processing.
	 **/
	void makeExpression(LiteralValue literal) throws SQLMappingException {
		// Literal

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { literal };
			DevTrace.entering(
				loc,
				makeExpression,
				makeExpressionLiteralParms,
				inputValues);
		}

		this.pushExpNodeIfNotNull();

		if (literal.isNumeric()) {
			if (literal.isFloaty()) {
				float floatValue = (new Float(literal.getValue())).floatValue();
				this.expNode =
					new ExpressionNode(
						null,
						null,
						null,
                        BUILDER.createFloatLiteral(floatValue).build());
			} else {
				if (literal.isDecimal()) {
					this.expNode =
						new ExpressionNode(
							null,
							null,
							null,
                            BUILDER.createBigDecimalLiteral(new BigDecimal(literal.getValue())).build());
				} else {
					this.expNode =
						new ExpressionNode(
							null,
							null,
							null,
                            BUILDER.createIntegerLiteral(literal.getValue()).build());
				}
			}
		} else {
			this.expNode =
				new ExpressionNode(
					null,
					null,
					null,
                    BUILDER.createStringLiteral(literal.getValue()).build());
		}

		DevTrace.exiting(loc, makeExpression);
		return;
	}

	/**
	 * Creates SQL representation of the occurrence of the part of a helper table that
	 * is determined by the given key. 
	 * </p><p>
	 * @param helperTable
	 *      description of the helper table.
	 * @param key
	 * 		side of facade
	 * @throws SQLMappingException
	 *     if an error is encountered during processing.
	 **/
	void makeExpression(HelperTableFacade helperTable, int key)
		throws SQLMappingException {
		// Helper Table

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { helperTable, new Integer(key) };
			DevTrace.entering(
				loc,
				makeExpression,
				makeExpressionHelperTableParms2,
				inputValues);
		}

		this.pushExpNodeIfNotNull();

		DatabaseColumn[] columnName = helperTable.getColumns(key);

		if ((columnName == null) || (columnName.length == 0)) {
			DevTrace.exitingWithException(loc, makeExpression);
			throw new SQLMappingException(
				"No columns for abstract bean "
					+ helperTable.getBean(key)
					+ " in helper table "
					+ helperTable.getName(this.myManager.isForNativeUseOnly())
					+ ".",
				"Helper table "
					+ helperTable.getName(this.myManager.isForNativeUseOnly())
					+ " does not contain any columns for given abstract bean "
					+ helperTable.getBean(key)
					+ ". This seems to be either a"
					+ " programming error in SQL mapper or EJB container"
					+ " or a problem with OR mapping. Please kindly open"
					+ " a problem ticket for SAP on component BC-JAS-PER-DBI.",
				"CSM105");
		}

		this.expNode =
			new ExpressionNode(
				null,
				null,
				null,
				new ejb21_ColumnReference(helperTable.getName(this.myManager.isForNativeUseOnly()),
													columnName[0].getName(this.myManager.isForNativeUseOnly()),
													this.myManager.isForNativeUseOnly()||columnName[0].isNative()));

		for (int i = 1; i < columnName.length; i++) {
			this.expStack.push(this.expNode);
			this.expNode =
				new ExpressionNode(
					null,
					null,
					null,
					new ejb21_ColumnReference(helperTable.getName(this.myManager.isForNativeUseOnly()),
															columnName[i].getName(this.myManager.isForNativeUseOnly()),
															this.myManager.isForNativeUseOnly()||columnName[i].isNative()));
		}

		this.multitude = columnName.length;
		DevTrace.debugInfo(
			loc,
			makeExpression,
			"Multitude set to " + this.multitude + ".");
		DevTrace.exiting(loc, makeExpression);
		return;

	}
	

	/**
	 * Creates SQL representation of the occurrence of an entity bean within the SET clause
	 * of an UPDATE statement.
	 * </p><p>
	 * @param beanObject
	 *     description of the entity bean.
	 * @throws SQLMappingException
	 *     if an error is encountered during processing.
	 **/
	void makeSetExpression(BeanObject beanObject) throws SQLMappingException {
		// Identification Variable

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanObject };
			DevTrace.entering(
				loc,
				makeSetExpression,
				makeSetExpressionIdentificationVariableParms,
				inputValues);
		}

		this.pushExpNodeIfNotNull();

		BeanDescriptor pkBeanDescriptor =
			this.orMapping.getBeanPK(beanObject.getType());
		int lenPrimaryKey = pkBeanDescriptor.getColumnNames().length;

		BeanDescriptor beanDescriptor =
			this.orMapping.getFullBean(beanObject.getType());
		DatabaseColumn[] columnName = beanDescriptor.getColumnNames();
		int lenAllColumns = columnName.length;

		if (lenAllColumns == lenPrimaryKey) {
			DevTrace.exitingWithException(loc, makeSetExpression);
			throw new NoReasonableStatementException(
				"Abstract bean "
					+ beanObject.getType()
					+ " yields empty SET clause.",
				"Abstract bean "
					+ beanObject.getType()
					+ " has no non-primary-key fields,"
					+ " hence an UPDATE statement makes no sense for this bean type. Apparently"
					+ " you have encountered a programming error in the EJB container. Please,"
					+ " kindly open a problem ticket for SAP on component BC-JAS-EJB.",
				"CSM091");
		}

                String alias;
                if ( beanObject.getIdentifier() == null )
                {
                  alias = null;
                }
                else
                {
                  alias = this.myManager.getAliasForIdentifier(beanObject.getIdentifier());
                  if ( alias == null )
                  {
                    DevTrace.exitingWithException(loc, makeSetExpression);
                    throw new SQLMappingException("Unrecognized identifier : "
                                 + beanObject.getIdentifier(),
                                 "No alias has been precomputed for identifier "
                                 + beanObject.getIdentifier()
                                 + ". This is an internal programming error "
                                 + "of the SQL mapper or the EJB-QL parser. "
                                 + "Please kindly open up a problem ticket on "
                                 + "component BC-JAS-PER-DBI.",
                                 "CSM149");
                  }
                }

		this.expNode =
			new ExpressionNode(
				null,
				null,
				null,
				new ejb21_ColumnReference(
					alias,
					columnName[lenPrimaryKey].getName(this.myManager.isForNativeUseOnly()),
                                        this.myManager.isForNativeUseOnly()||columnName[lenPrimaryKey].isNative()));

		for (int i = lenPrimaryKey + 1; i < columnName.length; i++) {
			this.expStack.push(this.expNode);
			this.expNode =
				new ExpressionNode(
					null,
					null,
					null,
					new ejb21_ColumnReference(
						alias,
						columnName[i].getName(this.myManager.isForNativeUseOnly()),
                                                this.myManager.isForNativeUseOnly()||columnName[i].isNative()));
		}

		this.multitude = lenAllColumns - lenPrimaryKey;
		DevTrace.debugInfo(
			loc,
			makeSetExpression,
			"Multitude set to " + this.multitude + ".");
		DevTrace.exiting(loc, makeSetExpression);
		return;
	}

	/**
	 * Creates SQL representation of the occurrence of an entity bean within the column list
	 * of an INSERT statement.
	 * </p><p>
	 * @param beanObject
	 *     description of the entity bean.
	 * @throws SQLMappingException
	 *     if an error is encountered during processing.
	 **/
	void makeInsertExpression(BeanObject beanObject)
		throws SQLMappingException {
		// Identification Variable

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { beanObject };
			DevTrace.entering(
				loc,
				makeInsertExpression,
				makeInsertExpressionIdentificationVariableParms,
				inputValues);
		}

		this.pushExpNodeIfNotNull();

		BeanDescriptor beanDescriptor =
			this.orMapping.getFullBean(beanObject.getType());
		DatabaseColumn[] columnName = beanDescriptor.getColumnNames();
                String alias;
                if ( beanObject.getIdentifier() == null )
                {
                  alias = null;
                }
                else
                {
                  alias = this.myManager.getAliasForIdentifier(beanObject.getIdentifier());
                  if ( alias == null )
                  {
                    DevTrace.exitingWithException(loc, makeInsertExpression);
                    throw new SQLMappingException("Unrecognized identifier : "
                                 + beanObject.getIdentifier(),
                                 "No alias has been precomputed for identifier "
                                 + beanObject.getIdentifier()
                                 + ". This is an internal programming error "
                                 + "of the SQL mapper or the EJB-QL parser. "
                                 + "Please kindly open up a problem ticket on "
                                 + "component BC-JAS-PER-DBI.",
                                 "CSM151");
                  }
                }

		this.expNode =
			new ExpressionNode(
				null,
				null,
				null,
				new ejb21_ColumnReference(alias, columnName[0].getName(this.myManager.isForNativeUseOnly()),
                                                           this.myManager.isForNativeUseOnly()||columnName[0].isNative()));

		for (int i = 1; i < columnName.length; i++) {
			this.expStack.push(this.expNode);
			this.expNode =
				new ExpressionNode(
					null,
					null,
					null,
					new ejb21_ColumnReference(
						alias,
						columnName[i].getName(this.myManager.isForNativeUseOnly()),
                                                this.myManager.isForNativeUseOnly()||columnName[i].isNative()));
		}

		this.multitude = columnName.length;

		DevTrace.debugInfo(
			loc,
			makeInsertExpression,
			"Multitude set to " + this.multitude + ".");
		DevTrace.exiting(loc, makeInsertExpression);
		return;

	}

	/**
	 * Creates SQL representation of the occurrence of the part of a helper table that is
	 * referring to one abstract bean within the columns list of an INSERT statement.
	 * </p><p>
	 * @param helperTable
	 *      description of the helper table.
	 * @param key
	 *      side of facade.
	 * @throws SQLMappingException
	 *     if an error is encountered during processing.
	 **/
	void makeInsertExpression(HelperTableFacade helperTable, int key)
		throws SQLMappingException {
		// Helper Table

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { helperTable, new Integer(key) };
			DevTrace.entering(
				loc,
				makeInsertExpression,
				makeExpressionHelperTableParms2,
				inputValues);
		}

		this.pushExpNodeIfNotNull();

		DatabaseColumn[] columnName = helperTable.getColumns(key);

		if ((columnName == null) || (columnName.length == 0)) {
			DevTrace.exitingWithException(loc, makeInsertExpression);
			throw new SQLMappingException(
				"No columns for abstract bean "
					+ helperTable.getBean(key)
					+ " in helper table "
					+ helperTable.getName(this.myManager.isForNativeUseOnly())
					+ ".",
				"Helper table "
					+ helperTable.getName(this.myManager.isForNativeUseOnly())
					+ " does not contain any columns for given abstract bean "
					+ helperTable.getBean(key)
					+ ". This seems to be either a"
					+ " programming error in SQL mapper or EJB container"
					+ " or a problem with OR mapping. Please kindly open"
					+ " a problem ticket for SAP on component BC-JAS-PER-DBI.",
				"CSM141");
		}

		this.expNode =
			new ExpressionNode(
				null,
				null,
				null,
				new ejb21_ColumnReference((String) null, columnName[0].getName(this.myManager.isForNativeUseOnly()),
                                                          this.myManager.isForNativeUseOnly()||columnName[0].isNative()));

		for (int i = 1; i < columnName.length; i++) {
			this.expStack.push(this.expNode);
			this.expNode =
				new ExpressionNode(
					null,
					null,
					null,
					new ejb21_ColumnReference((String) null, columnName[i].getName(this.myManager.isForNativeUseOnly()),
                                                                  this.myManager.isForNativeUseOnly()||columnName[i].isNative()));
		}

		this.multitude = columnName.length;
		DevTrace.debugInfo(
			loc,
			makeInsertExpression,
			"Multitude set to " + this.multitude + ".");
		DevTrace.exiting(loc, makeInsertExpression);
		return;

	}

	/** 
	 * Adds a one-argument built-in function to the expression currently in the processing
	 * unit. Currently supported functions are <code>ABS</code>, <code>SQRT</code> and 
	 * <code>LENGTH</code>.
	     * </p><p>
	 * @param scalarFunction
	 *     function code.
	 * @throws SQLMappingException
	 *     if an error is encountered during processing.
	 **/
	void addFunctionToExpression(int scalarFunction)
		throws SQLMappingException {
		// ABS, SQRT, LENGTH

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { new Integer(scalarFunction)};
			DevTrace.entering(
				loc,
				addFunctionToExpression,
				addFunctionToExpressionParms,
				inputValues);
		}

		if (this.expNode == null) {
			DevTrace.exitingWithException(loc, addFunctionToExpression);
			throw new SQLMappingException(
				"Call of built-in function without argument.",
				"An SQL built-in function was called without providing "
					+ "an argument. This is most likely a programming error "
					+ "within SQL mapper. Function code is "
					+ scalarFunction
					+ ". Please be so kind as to open a problem ticket for SAP"
                                        + " on component BC-JAS-PER-DBI.",
				"CSM055");
		}

                ValueExpression valExpr;

                switch (scalarFunction)
                {
                  case ABS:
                    valExpr = BUILDER.createAbsoluteValueExpression(expNode.getExpression()).build();
                    break;
                  case SQRT:
                    valExpr = BUILDER.createSquareRootExpression(expNode.getExpression()).build();
                    break;
                  case LENGTH:
                    valExpr = BUILDER.createLengthExpression(expNode.getExpression()).build();
                    break;
                  default:
                    DevTrace.exitingWithException(loc, addFunctionToExpression);
                    throw new SQLMappingException("Unbeknownst or unadmissable scalar function " + scalarFunction + ".",
                                                  "An internal method of the SQL mapper was called with an"
                                                  + " unexpected argument. Function code is "
                                                  + scalarFunction
                                                  + ". This is an internal programming of the SQL mapper."
                                                  + " Please be so kind as to open a problem ticket for SAP"
                                                  + " on component BC-JAS-PER-DBI.",
                                                  "CSM157");
                }
		this.expNode =
			new ExpressionNode(
				this.expNode.getTableRepresentations(),
				this.expNode.getJoinConditions(),
				this.expNode.getAliases(),
                                valExpr);
		DevTrace.exiting(loc, addFunctionToExpression);
		return;
	}

	/** 
	 * Combines secondmost top element of stack, top most element of stack
	 * and expression currently in the processing unit by a three-argument
	 * built-in function. Currently supported functions are <code>LOCATE</code>
	 * and <code>SUBSTRING</code>.
	     * </p><p>
	 * @param scalarFunction
	 *     function code.
	 * @throws SQLMappingException
	 *     if an error is encountered during processing.
	 **/
	void doublePopExpressionWithFunction(int scalarFunction)
		throws SQLMappingException {
		// LOCATE, SUBSTRING

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { new Integer(scalarFunction)};
			DevTrace.entering(
				loc,
				doublePopExpressionWithFunction,
				doublePopExpressionWithFunctionParms,
				inputValues);
		}

		if ((this.expNode == null) || this.expStack.empty()) {
			DevTrace.exitingWithException(loc, doublePopExpressionWithFunction);
			throw new SQLMappingException(
				"Too few arguments for built-in function.",
				"An SQL built-in function was called without providing "
					+ "even two of its three arguments required. This is most "
					+ "likely a programming error within SQL mapper. Function "
					+ "code is "
					+ scalarFunction
					+ ".  Please be so kind as to open a problem ticket for SAP"
                                        + " on component BC-JAS-PER-DBI.",
				"CSM059");
		}

		ExpressionNode node1 = this.expStack.pop();

		if (this.expStack.empty()) {
			DevTrace.exitingWithException(loc, doublePopExpressionWithFunction);
			throw new SQLMappingException(
				"Too few arguments for built-in function.",
				"An SQL built-in function was called without providing "
					+ "all its three arguments required. This is most likely a "
					+ "programming error within SQL mapper. Function code is "
					+ scalarFunction
					+ ". Please be so kind as to open a problem ticket for SAP"
                                        + " on component BC-JAS-PER-DBI.",
				"CSM061");
		}

		ExpressionNode node2 = this.expStack.pop();

		HashMap newJoinConditionMap =
			this.myManager.mergeHashMaps(
				node1.getJoinConditions(),
				node2.getJoinConditions(),
				this.expNode.getJoinConditions());

		HashMap newTableRepMap =
			this.myManager.mergeHashMaps(
				node1.getTableRepresentations(),
				node2.getTableRepresentations(),
				this.expNode.getTableRepresentations());

		ArrayList newAliasList =
			this.myManager.appendArrayLists(
				node2.getAliases(),
				node1.getAliases(),
				this.expNode.getAliases());

                ValueExpression valExpr;
                switch (scalarFunction)
                {
                  case LOCATE:
                    valExpr = BUILDER.createPositionExpression(node2.getExpression(), node1.getExpression()).setStart(expNode.getExpression()).build();
                    break;
                  case SUBSTRING:
                    valExpr = BUILDER.createSubstringExpression(node2.getExpression(), node1.getExpression(), expNode.getExpression()).build();
                    break;
                  default:
                    DevTrace.exitingWithException(loc, addFunctionToExpression);
                    throw new SQLMappingException("Unbeknownst or unadmissable scalar function " + scalarFunction + ".",
                                                  "An internal method of the SQL mapper was called with an"
                                                  + " unexpected argument. Function code is "
                                                  + scalarFunction
                                                  + ". This is an internal programming of the SQL mapper."
                                                  + " Please be so kind as to open a problem ticket for SAP"
                                                  + " on component BC-JAS-PER-DBI.",
                                                  "CSM159");
                }

		this.expNode =
			new ExpressionNode(
				newTableRepMap,
				newJoinConditionMap,
				newAliasList,
                                valExpr);
		DevTrace.exiting(loc, doublePopExpressionWithFunction);
		return;
	}

	/**
	 * Combines top most element of stack and expression currently in the
	 * processing unit by a two-argument built-in function. Currently 
	 * supported functions are <code>MODE</code>, <code>LOCATE</code>
	 * and <code>CONCAT</code>.
	     * </p><p>
	 * @param scalarFunction
	 *     function code.
	 * @throws SQLMappingException
	 *     if an error is encountered during processing.
	 **/
	void popExpressionWithFunction(int scalarFunction)
		throws SQLMappingException {
		// MOD, LOCATE, CONCAT

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { new Integer(scalarFunction)};
			DevTrace.entering(
				loc,
				popExpressionWithFunction,
				popExpressionWithFunctionParms,
				inputValues);
		}

		if ((this.expNode == null) || this.expStack.empty()) {
			DevTrace.exitingWithException(loc, popExpressionWithFunction);
			throw new SQLMappingException(
				"Too few arguments for built-in function.",
				"An SQL built-in function was called without providing "
					+ "all its two arguments required. This is most likely a "
					+ "programming error within SQL mapper. Function code is "
					+ scalarFunction
					+ ". Please be so kind as to open a problem ticket for SAP"
                                        + " on component BC-JAS-PER-DBI.",
				"CSM057");
		}

		ExpressionNode node = this.expStack.pop();

		HashMap newJoinConditionMap =
			this.myManager.mergeHashMaps(
				node.getJoinConditions(),
				this.expNode.getJoinConditions());

		HashMap newTableRepMap =
			this.myManager.mergeHashMaps(
				node.getTableRepresentations(),
				this.expNode.getTableRepresentations());

		ArrayList newAliasList =
			this.myManager.appendArrayLists(
				node.getAliases(),
				this.expNode.getAliases());

                ValueExpression valExpr;
                switch (scalarFunction)
                {
                  case MOD:
                    valExpr = BUILDER.createModulusExpression(node.getExpression(), this.expNode.getExpression()).build();
                    break;
                  case LOCATE:
                    valExpr = BUILDER.createPositionExpression(node.getExpression(), this.expNode.getExpression()).build();
                    break;
                  case CONCAT:
                    valExpr = BUILDER.createConcatenationExpression(node.getExpression(), this.expNode.getExpression()).build();
                    break;
                  default:
                    DevTrace.exitingWithException(loc, addFunctionToExpression);
                    throw new SQLMappingException("Unbeknownst or unadmissable scalar function " + scalarFunction + ".",
                                                  "An internal method of the SQL mapper was called with an"
                                                  + " unexpected argument. Function code is "
                                                  + scalarFunction
                                                  + ". This is an internal programming of the SQL mapper."
                                                  + " Please be so kind as to open a problem ticket for SAP"
                                                  + " on component BC-JAS-PER-DBI.",
                                                  "CSM161");
                }
		this.expNode =
			new ExpressionNode(
				newTableRepMap,
				newJoinConditionMap,
				newAliasList,
                                valExpr);

		DevTrace.exiting(loc, popExpressionWithFunction);
		return;
	}

	/**
	 * Applies a monadic operator to the expression currently in the
	 * processing unit. Currently supported operators are <code>PLUS</code>
	 * and <code>MINUS</code>.
	     * </p><p>
	 * @param operator
	 *     monadic operator.
	 * @throws SQLMappingException
	 *     if an error is encountered during processing.
	 **/
	final void alterExpression(final ArithmeticExpressionOperator operator) throws SQLMappingException {
		// PLUS, MINUS

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { operator };
			DevTrace.entering(
				loc,
				alterExpression,
				alterExpressionParms,
				inputValues);
		}

		if (this.expNode == null) {
			DevTrace.exitingWithException(loc, alterExpression);
			throw new SQLMappingException(
				"Monadic operator without argument.",
				"A monadic operator was invoked without argument."
					+ "This is most likely a programming error within "
					+ "SQL mapper. Operator is "
					+ operator
					+ ".",
				"CSM063");
		}

		this.expNode =
			new ExpressionNode(
				this.expNode.getTableRepresentations(),
				this.expNode.getJoinConditions(),
				this.expNode.getAliases(),
				BUILDER.createArithmeticExpression(operator).setOnlyOperand(this.expNode.getExpression()).build());
		DevTrace.exiting(loc, alterExpression);
		return;
	}

	/**
	 * Combines top most element of stack and expression currently in the
	 * processing unit by a dyadic operator. Currently supported
	 * operators are <code>PLUS</code>, <code>MINUS</code>
	 * <code>TIMES</codes> and <code>DIVIDE</code>.
	     * </p><p>
	 * @param scalarFunction
	 *     function code.
	 * @throws SQLMappingException
	 *     if an error is encountered during processing.
	 **/
	final void popExpression(final ArithmeticExpressionOperator operator) throws SQLMappingException {
		// PLUS, MINUS, TIMES, DIVIDE

		if (DevTrace.isOnDebugLevel(loc)) {
			Object inputValues[] = { operator };
			DevTrace.entering(
				loc,
				popExpression,
				popExpressionParms,
				inputValues);
		}

		if ((this.expNode == null) || this.expStack.empty()) {
			DevTrace.exitingWithException(loc, popExpression);
			throw new SQLMappingException(
				"Too few arguments for dyadic operator.",
				"A dyadic operator was invoked with less than "
					+ "two arguments. This is most likely a programming "
					+ "error within SQL mapper. Operator code is "
					+ operator
					+ ".",
				"CSM065");
		}

		ExpressionNode node = this.expStack.pop();

		HashMap newJoinConditionMap =
			this.myManager.mergeHashMaps(
				node.getJoinConditions(),
				this.expNode.getJoinConditions());

		HashMap newTableRepMap =
			this.myManager.mergeHashMaps(
				node.getTableRepresentations(),
				this.expNode.getTableRepresentations());

		ArrayList newAliasList =
			this.myManager.appendArrayLists(
				node.getAliases(),
				this.expNode.getAliases());

		this.expNode =
			new ExpressionNode(
				newTableRepMap,
				newJoinConditionMap,
				newAliasList,
                BUILDER.createArithmeticExpression(operator).setLeftOperand(node.getExpression()).setRightOperand(this.expNode.getExpression()).build());
		DevTrace.exiting(loc, popExpression);
		return;
	}

	/**
	 * Returns an array of SQL representations of expressions. Normally this method returns
	 * an array consisting of only one element, namely the expression that is
	 * currently in the processing unit. Only for entity bean expressions
	 * with an abstract schema type that disposes of a compound primary key with
	 * n primary key fields (n being a natural number greater or equal 2)
	 * the resulting array will also contain the n - 1 topmost elements
	 * of the stack as well.
	 * </p><p>
	 * @return
	 *     array of SQL representations of expressions.
	 **/
	Object[] endOfExpression() {
		DevTrace.entering(loc, endOfExpression, null, null);

		ExpressionNode[] resultArray = new ExpressionNode[this.multitude];

		resultArray[this.multitude - 1] = this.expNode;
		for (int i = this.multitude - 2; i >= 0; i--) {
			resultArray[i] = this.expStack.pop();
		}

		this.expNode = null;
		this.multitude = 1;

		DevTrace.exiting(loc, endOfExpression, resultArray);
		return resultArray;
	}

	//// private methods
	/**
	 * Pushes expression eventually present in the processing unit on
	 * top of the stack.
	     * </p><p>
	 * @throws SQLMappingException
	 *     if method is invoked in between processing of an entity bean expression
	 *     involving a compound primary key.
	 **/
	private void pushExpNodeIfNotNull() throws SQLMappingException {
		if (this.expNode != null) {
			if (this.multitude != 1) {
				DevTrace.displayStack(
					loc,
					"Expression Node Stack",
					this.expStack,
					this.expNode);
				throw new SQLMappingException(
					"Mismatch between entity bean and primitive expressions.",
					"While processing expressions, SQL Mapper has encountered "
						+ "a mismatch between an entity bean expression and a "
						+ "primitive expression, or an attempt to compose entity "
						+ "bean expressions. This is a programming error in "
						+ "either SQL Mapper or EJB QL parser. Multitude is "
						+ this.multitude
						+ ".",
					"CSM067");
			}

			this.expStack.push(this.expNode);
		}
	}

}
