package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejbql.tree.Expression;
import com.sap.ejbql.tree.PathExpression;
import com.sap.ejbql.tree.ConditionalExpression;
import com.sap.ejbql.tree.NullComparisonExpression;
import com.sap.ejbql.tree.EmptyCollectionExpression;
import com.sap.ejbql.tree.CollectionMemberExpression;
import com.sap.ejbql.tree.InExpression;
import com.sap.ejbql.tree.LikeExpression;
import com.sap.ejbql.tree.BetweenExpression;
import com.sap.ejbql.tree.ComparisonExpression;
import com.sap.ejbql.tree.InputParameter;
import com.sap.ejbql.tree.IdentificationVariable;
import com.sap.ejbql.tree.Literal;
import com.sap.ejbql.tree.Type;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager;
import com.sap.ejb.ql.sqlmapper.common.WhereNodeManager;
import com.sap.ejb.ql.sqlmapper.common.ExpressionNodeManager;
import com.sap.ejb.ql.sqlmapper.common.BooleanNodeManager;
import com.sap.ejb.ql.sqlmapper.common.ProcessingHelper;
import com.sap.ejb.ql.sqlmapper.common.ArithmeticExpressionProcessor;
import com.sap.ejb.ql.sqlmapper.common.StringExpressionProcessor;
import com.sap.ejb.ql.sqlmapper.common.ComparativeOperator;
import com.sap.ejb.ql.sqlmapper.common.BeanField;
import com.sap.ejb.ql.sqlmapper.common.BeanObject;
import com.sap.ejb.ql.sqlmapper.common.LiteralValue;
import com.sap.ejb.ql.sqlmapper.common.InputParameterDefinition;
import com.sap.ejb.ql.sqlmapper.common.InputParameterOccurrence;

import com.sap.sql.tree.ComparisonOperator;
import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is meant for evaluation of simple conditional expression.
 * It sends appropriate commands to an associated <code>WhereNodeManager</code>
 * object, which therewith creates the SQL translation of the respective
 * simple conditional expression.
 * </p><p>
 * Simple conditional expression may contain arithmetic, string, boolean, date time
 * and entity bean expressions as arguments. When processing such expressions
 * class <code>SimpleConditionProcessor</code> sends commands to an associated
 * <code>ExpressionNodeManager</code> (non-booolean expressions) or <code>BooleanNodeManager</code>
 * (boolean expressions) object, which therewith creates an SQL translation of the 
 * respective expression.
 * </p><p>
 * Class <code>SimpleConditionProcessor</code> actually only evaluates 
 * boolean, date time and entity bean expressions itself; for arithmetic
 * and string expressions it creates an <code>ArithmeticExpressionProcessor</code>
 * and a <code>StringExpressionProcessor</code> object each with mutual references
 * to each other.
 * </p><p>
 * An instance of class <code>SimpleConditionProcessor</code> is created by a
 * <code>ConditionProcessor</code> object with an <code>SQLNodeManage</code>
 * object as argument, from which the associated <code>WhereNodeManager</code>,
 * <code>ExpressionNodeManager</code> and <code>BooleanNodeManager</code> objects
 * are retrieved. The <code>SQLNodeManage</code> object is also used for registering
 * input parameters.
 * </p><p>
 * Copyright (c) 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 * @see com.sap.ejb.ql.sqlmapper.common.ConditionProcessor
 * @see com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.WhereNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.ExpressionNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.BooleanNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.ArithmeticExpressionProcessor
 * @see com.sap.ejb.ql.sqlmapper.common.StringExpressionProcessor
 **/
public class SimpleConditionProcessor
{
  private static final Location loc = Location.getLocation(SimpleConditionProcessor.class);
  private static final String process = "process";
  private static final String processNullComparisonExpression = "processNullComparisonExpression";
  private static final String processEmptyCollectionExpression = "processEmptyCollectionExpression";
  private static final String processCollectionMemberExpression = "processCollectionMemberExpression";
  private static final String processInExpression = "processInExpression";
  private static final String processLikeExpression = "processLikeExpression";
  private static final String processBetweenExpression = "processBetweenExpression";
  private static final String processComparisonExpression = "processComparisonExpression";
  private static final String evaluateExpression = "evaluateExpression";
  private static final String buildDateTimeExpression = "buildDateTimeExpression";
  private static final String buildBooleanExpression = "buildBooleanExpression";
  private static final String buildEntityBeanExpression = "buildEntityBeanExpression";
  private static final String processParms[] = { "cond" };
  private static final String processNullComparisonExpressionParms[] = { "comp" };
  private static final String processEmptyCollectionExpressionParms[] = { "emptyCollExpr" };
  private static final String processCollectionMemberExpressionParms[] = { "collMemExpr" };
  private static final String processInExpressionParms[] = { "inExpr" };
  private static final String processLikeExpressionParms[] = { "likeExpr" };
  private static final String processBetweenExpressionParms[] = { "betweenExpr" };
  private static final String processComparisonExpressionParms[] = { "comp" };
  private static final String evaluateExpressionParms[] = { "expr" };
  private static final String buildDateTimeExpressionParms[] = { "expr" };
  private static final String buildBooleanExpressionParms[] = { "expr" };
  private static final String buildEntityBeanExpressionParms[] = { "expr" };

  private SQLTreeNodeManager treeNodeManager;	
  private WhereNodeManager whereNodeManager;
  private ExpressionNodeManager expNodeManager;
  private BooleanNodeManager booleanNodeManager;
  private ArithmeticExpressionProcessor arithmeticExpressionProcessor;
  private StringExpressionProcessor stringExpressionProcessor;

  /**
   * Creates a <code>SimpleConditionProcessor</code> instance with
   * an <code>SQLTreeNodeManager</code> object as argument.
   * </p><p>
   * From the <code>SQLTreeNodeManager</code> object a 
   * <code>WhereNodeManager</code>, <code>ExpressionNodeManager</code> 
   * and <code>BooleanNodeManager</code> object are retrieved each.
   * Furthermore an <code>ArithmeticExpressionProcessor</code>
   * and a <code>StringExpressionProcessor</code> are created.
   * @param treeNodeManager
   *     the associated <code>SQLTreeNodeManager</code> object.
   **/
  SimpleConditionProcessor(SQLTreeNodeManager treeNodeManager)
  {
    this.treeNodeManager = treeNodeManager;
    this.whereNodeManager = treeNodeManager.getWhereNodeManager();
    this.expNodeManager = treeNodeManager.getExpressionNodeManager();
    this.booleanNodeManager = treeNodeManager.getBooleanNodeManager();
    this.arithmeticExpressionProcessor = 
               new ArithmeticExpressionProcessor(treeNodeManager);
    this.stringExpressionProcessor =
               new StringExpressionProcessor(treeNodeManager);
    this.stringExpressionProcessor.setArithmeticExpressionProcessor(this.arithmeticExpressionProcessor);
    this.arithmeticExpressionProcessor.setStringExpressionProcessor(this.stringExpressionProcessor);
  }

  /** 
   * Processes a simple conditional expression.
   * @param cond
   *     simple conditional expression to be processed.
   * @throws SQLMappingException
   *     if an error is encountered during processing
   **/
  void process(ConditionalExpression cond)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { cond };
      DevTrace.entering(loc, process, processParms, inputValues);
    }

    if ( cond instanceof NullComparisonExpression )
    {
      this.processNullComparisonExpression( (NullComparisonExpression) cond);
      DevTrace.exiting(loc, process);
      return;
    }

    if ( cond instanceof EmptyCollectionExpression )
    {
      this.processEmptyCollectionExpression( (EmptyCollectionExpression) cond);
      DevTrace.exiting(loc, process);
      return;
    }

    if ( cond instanceof CollectionMemberExpression )
    {
      this.processCollectionMemberExpression( (CollectionMemberExpression) cond);
      DevTrace.exiting(loc, process);
      return;
    }

    if ( cond instanceof InExpression )
    {
      this.processInExpression( (InExpression) cond);
      DevTrace.exiting(loc, process);
      return;
    }

    if ( cond instanceof LikeExpression )
    {
      this.processLikeExpression( (LikeExpression) cond);
      DevTrace.exiting(loc, process);
      return;
    }

    if ( cond instanceof BetweenExpression )
    {
      this.processBetweenExpression( (BetweenExpression) cond);
      DevTrace.exiting(loc, process);
      return;
    }

    if ( cond instanceof ComparisonExpression )
    {
      this.processComparisonExpression( (ComparisonExpression) cond);
      DevTrace.exiting(loc, process);
      return;
    }

    String className = cond.getClass().getName();
    DevTrace.exitingWithException(loc, process);
    throw new SQLMappingException("Unbeknownst conditional expression " +
                                  className + ".",
                                  "In the EJB QL parsed tree, SQL mapper has "
                                  + "encountered unknown conditional expression "
                                  + className + "This is either a programming "
                                  + "error in the EJB QL parser or an inconsistency "
                                  + "between EJB QL parser and SQL mapper. Please "
                                  + "make sure that you run consistent versions "
                                  + "of EJB QL parser and SQL mapper.",
                                  "CSM015");
  }

  /**
   * Processes a null comparison expression.
   * @param comp
   *     null comparison expression to be processed.
   * @throws SQLMappingException
   *     if an error is encountered during processing.
   **/
  private void processNullComparisonExpression(NullComparisonExpression comp)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { comp };
      DevTrace.entering(loc, processNullComparisonExpression, processNullComparisonExpressionParms,
                                                              inputValues);
    }

    Expression expr = comp.getValue();

    if ( expr.isPath() )
    {
      DevTrace.debugInfo(loc, processNullComparisonExpression, "Path encountered.");
      PathExpression path = (PathExpression) expr;
      BeanField[] relation = ProcessingHelper.extractRelationsFromPath(path);
      BeanField beanField = ProcessingHelper.getPathDestination(path);

      if ( comp.isNegated() )
      {
        this.whereNodeManager.makeNode(ComparativeOperator.NOT_NULL, beanField, relation);
      }
      else
      {
        this.whereNodeManager.makeNode(ComparativeOperator.IS_NULL, beanField, relation);
      }

      DevTrace.exiting(loc, processNullComparisonExpression);
      return;
    }

    if ( expr.isInputParameter() )
    {
      DevTrace.debugInfo(loc, processNullComparisonExpression, "InputParameter encountered.");
      InputParameter inputParameter = (InputParameter) expr;
      InputParameterDefinition definition = new InputParameterDefinition(inputParameter, null);
      InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);
      

      if ( comp.isNegated() )
      {
        this.whereNodeManager.makeNode(ComparativeOperator.NOT_NULL, occurrence);
      }
      else
      {
        this.whereNodeManager.makeNode(ComparativeOperator.IS_NULL, occurrence);
      }

      DevTrace.exiting(loc, processNullComparisonExpression);
      return;
    }

    String className = expr.getClass().getName();
    DevTrace.exitingWithException(loc, processNullComparisonExpression);
    throw new SQLMappingException("Null Comparison: " + className + " is not a "
                                  + "path expression nor an input parameter.",
                                  "In the EJB QL parsed tree, SQL mapper has "
                                  + "encountered " + className + " within an "
                                  + "null comparison. However, only single valued "
                                  + "path expressions and input parameters are allowed "
                                  + "in this position."
                                  + "This is either a programming error in the EJB QL "
                                  + "parser and SQL mapper or an inconsistency "
                                  + "between EJB QL parser and SQL mapper. Please make "
                                  + "sure that you run consistent versions of EJB QL "
                                  + "parser and SQL mapper.",
                                  "CSM019");
  }

  /**
   * Processes an empty collection expression.
   * @param emptyCollExpr
   *     empty collection expression to be processed.
   * @throws SQLMappingException
   *     if an error occurs during processing.
   **/
  private void processEmptyCollectionExpression(EmptyCollectionExpression emptyCollExpr)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { emptyCollExpr };
      DevTrace.entering(loc, processEmptyCollectionExpression, processEmptyCollectionExpressionParms,
                                                               inputValues);
    }

    PathExpression path = emptyCollExpr.getPath();
    BeanField[] relation = ProcessingHelper.extractRelationsFromPath(path);
    BeanField beanField = ProcessingHelper.getPathDestination(path);

    if ( emptyCollExpr.isNegated() )
    {
      this.whereNodeManager.makeNode(ComparativeOperator.NOT_EMPTY, beanField, relation);
    }
    else
    {
      this.whereNodeManager.makeNode(ComparativeOperator.EMPTY, beanField, relation);
    }

    DevTrace.exiting(loc, processEmptyCollectionExpression);
    return;
  }

  /**
   * Processes a collection member expression.
   * @param collMemExpr
   *     collection member expression to be processed.
   * @throws SQLMappingException
   *     if an error occurs during processing.
   **/ 
  private void processCollectionMemberExpression(CollectionMemberExpression collMemExpr)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { collMemExpr };
      DevTrace.entering(loc, processCollectionMemberExpression, processCollectionMemberExpressionParms,
                                                                inputValues);
    }

    PathExpression path = collMemExpr.getPath();
    BeanField[] targetRelation = ProcessingHelper.extractRelationsFromPath(path);
    BeanField targetBeanField = ProcessingHelper.getPathDestination(path);

    Expression expr = collMemExpr.getValue();
    if ( expr.isPath() )
    {
      DevTrace.debugInfo(loc, processCollectionMemberExpression, "Path encountered.");
      BeanField[] relation = ProcessingHelper.extractRelationsFromPath( (PathExpression) expr);
      BeanField beanField = ProcessingHelper.getPathDestination( (PathExpression) expr);

      if ( collMemExpr.isNegated() )
      {
        this.whereNodeManager.makeNode(ComparativeOperator.NOT_MEMBER, 
                                      beanField, relation,
                                      targetBeanField, targetRelation);
      }
      else
      {
        this.whereNodeManager.makeNode(ComparativeOperator.MEMBER,
                                      beanField, relation,
                                      targetBeanField, targetRelation);
      }

      DevTrace.exiting(loc, processCollectionMemberExpression);
      return;
    }

    if ( expr.isIdentificationVariable() )
    {
      DevTrace.debugInfo(loc, processCollectionMemberExpression, "IdentificationVariable encountered.");
      IdentificationVariable idVar = (IdentificationVariable) expr;
      BeanObject beanObject = 
            new BeanObject(idVar.getDeclaration().getType().toString(), idVar.getName().toUpperCase());

      if ( collMemExpr.isNegated() )
      {
        this.whereNodeManager.makeNode(ComparativeOperator.NOT_MEMBER, 
                                      beanObject,
                                      targetBeanField, targetRelation);
      }
      else
      {
        this.whereNodeManager.makeNode(ComparativeOperator.MEMBER,
                                      beanObject,
                                      targetBeanField, targetRelation);
      }

      DevTrace.exiting(loc, processCollectionMemberExpression);
      return;
    } 

    if ( expr.isInputParameter() )
    {
      DevTrace.debugInfo(loc, processCollectionMemberExpression, "InputParameter encountered.");
      InputParameter inputParameter = (InputParameter) expr;
      InputParameterDefinition definition = new InputParameterDefinition(inputParameter, null);
      InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);

      if ( collMemExpr.isNegated() )
      {
        this.whereNodeManager.makeNode(ComparativeOperator.NOT_MEMBER,
                                       occurrence,
                                       targetBeanField, targetRelation);
      }
      else
      {
        this.whereNodeManager.makeNode(ComparativeOperator.MEMBER,
                                       occurrence,
                                       targetBeanField, targetRelation);
      }

      DevTrace.exiting(loc, processCollectionMemberExpression);
      return;
    }

    String className = expr.getClass().getName();
    DevTrace.exitingWithException(loc, processCollectionMemberExpression);
    throw new SQLMappingException("Unexpected expression " + className 
                            + " in CollectionMemberExpression.",
                            "In the EJB QL parsed tree, SQL mapper has encountered "
                            + "an expression " + className + " within a "
                            + "CollectionMemberExpression; however, only path "
                            + "expressions, identification variables and input "
                            + "parameters are allowed in this position. "
                            + "This is either a programming error in the EJB QL "
                            + "parser and SQL mapper or an inconsistency "
                            + "between EJB QL parser and SQL mapper. Please make "
                            + "sure that you run consistent versions of EJB QL "
                            + "parser and SQL mapper.",
                            "CSM021");
  }

  /**
   * Processes an in expression.
   * @param inExpr
   *     in expression to be processed.
   * @throws SQLMappingException
   *     if an error occurs during processing.
   **/
  private void processInExpression(InExpression inExpr)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object[] inputValues = { inExpr };
      DevTrace.entering(loc, processInExpression, processInExpressionParms, inputValues);
    }

    PathExpression path = inExpr.getPath();
    BeanField[] relation = ProcessingHelper.extractRelationsFromPath(path);
    BeanField beanField = ProcessingHelper.getPathDestination(path);

    Expression[] exprList = inExpr.getExpressionList();
    List<LiteralValue> literals = new ArrayList<LiteralValue>();
    List<InputParameterOccurrence> inputParameters = new ArrayList<InputParameterOccurrence>();

    for (int i = 0; i < exprList.length; i ++)
    {
      if ( exprList[i].isInputParameter() )
      {
        DevTrace.debugInfo(loc, processInExpression, "InputParameter encountered.");
        InputParameter inputParameter = (InputParameter) exprList[i];
        InputParameterDefinition definition = new InputParameterDefinition(inputParameter, beanField);
        InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);
        inputParameters.add(occurrence);
        continue;
      } 
      

      if ( exprList[i] instanceof Literal )
      {
        DevTrace.debugInfo(loc, processInExpression, "Literal encountered.");
        literals.add(ProcessingHelper.createLiteralFromLiteralExpression( (Literal) exprList[i] ));
        continue;
      }

      String className = exprList[i].getClass().getName();
      DevTrace.exitingWithException(loc, processInExpression);
      throw new SQLMappingException("Unexpected expression " + className
                              + " inInExpression.",
                              "In the EJB QL parsed tree, SQL mapper has encountered "
                              + "an expression " + className + " within a "
                              + "InExpression; however, only string literals and "
                              + "input parameters are allowed in this position. "
                              + "This is either a programming error in the EJB QL "
                              + "parser and SQL mapper or an inconsistency "
                              + "between EJB QL parser and SQL mapper. Please make "
                              + "sure that you run consistent versions of EJB QL "
                              + "parser and SQL mapper.",
                              "CSM023");
    }

    InputParameterOccurrence[] inputParameterArray = new InputParameterOccurrence[inputParameters.size()];
    inputParameters.toArray(inputParameterArray);
    LiteralValue[] literalArray = new LiteralValue[literals.size()];
    literals.toArray(literalArray);

    if ( inExpr.isNegated() )
    {
      this.whereNodeManager.makeNode(ComparativeOperator.NOT_IN,
                                    beanField, relation,
                                    literalArray, inputParameterArray);
    }
    else
    {
      this.whereNodeManager.makeNode(ComparativeOperator.IN,
                                    beanField, relation,
                                    literalArray, inputParameterArray);
    }

    DevTrace.exiting(loc, processInExpression);
    return;
  }

  /**
   * Processes a like expression.
   * @param likeExpr
   *     like expression to be processed.
   * @throws SQLMappingException
   *     if an error occurs during processing.
   **/
  private void processLikeExpression(LikeExpression likeExpr)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { likeExpr };
      DevTrace.entering(loc, processLikeExpression, processLikeExpressionParms, inputValues);
    }

    PathExpression path = likeExpr.getValue();
    BeanField[] relation = ProcessingHelper.extractRelationsFromPath(path);
    BeanField beanField = ProcessingHelper.getPathDestination(path);

    Expression pattern = likeExpr.getPattern();
    Expression escapeCharacter = likeExpr.getEscapeCharacter();

    if ( pattern instanceof Literal )
    {
      DevTrace.debugInfo(loc, processLikeExpression, "Pattern is Literal.");

      if ( escapeCharacter == null )
      {
        if ( likeExpr.isNegated() )
        {
          this.whereNodeManager.makeNode(ComparativeOperator.NOT_LIKE,
                                  beanField, relation,
                                  ProcessingHelper.createLiteralFromLiteralExpression( (Literal) pattern ),
                                  (LiteralValue) null
                                        );
        }
        else
        {
          this.whereNodeManager.makeNode(ComparativeOperator.LIKE,
                                  beanField, relation,
                                  ProcessingHelper.createLiteralFromLiteralExpression( (Literal) pattern ),
                                  (LiteralValue) null
                                        );
        }

        DevTrace.exiting(loc, processLikeExpression);
        return;
      }

      if ( escapeCharacter instanceof Literal )
      {
        DevTrace.debugInfo(loc, processLikeExpression, "ESC is Literal.");

        if ( likeExpr.isNegated() )
        {
          this.whereNodeManager.makeNode(ComparativeOperator.NOT_LIKE,
                                  beanField, relation,
                                  ProcessingHelper.createLiteralFromLiteralExpression( (Literal) pattern ),
                                  ProcessingHelper.createLiteralFromLiteralExpression( (Literal) escapeCharacter )
                                        );
        }
        else
        {
          this.whereNodeManager.makeNode(ComparativeOperator.LIKE,
                                  beanField, relation,
                                  ProcessingHelper.createLiteralFromLiteralExpression( (Literal) pattern ),
                                  ProcessingHelper.createLiteralFromLiteralExpression( (Literal) escapeCharacter )
                                        );
        }

        DevTrace.exiting(loc, processLikeExpression);
        return;
      }

      if ( escapeCharacter instanceof InputParameter )
      {
        DevTrace.debugInfo(loc, processLikeExpression, "ESC is InputParameter.");
        InputParameter inputParameter = (InputParameter) escapeCharacter;
        InputParameterDefinition definition = new InputParameterDefinition(inputParameter, null);
        InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);

        if ( likeExpr.isNegated() )
        {
          this.whereNodeManager.makeNode(ComparativeOperator.NOT_LIKE,
                                        beanField, relation,
                                        ProcessingHelper.createLiteralFromLiteralExpression( (Literal) pattern ),
                                        occurrence
                                        );
        }
        else
        {
          this.whereNodeManager.makeNode(ComparativeOperator.LIKE,
                                        beanField, relation,
                                        ProcessingHelper.createLiteralFromLiteralExpression( (Literal) pattern ),
                                        occurrence
                                        );
        }

        DevTrace.exiting(loc, processLikeExpression);
        return;
      }

      String className = escapeCharacter.getClass().getName();
      DevTrace.exitingWithException(loc, processLikeExpression);
      throw new SQLMappingException("Unexpected expression " + className
                              + " as escape character in LikeExpression.",
                              "In the EJB QL parsed tree, SQL mapper has encountered "
                              + "an expression " + className + " within a "
                              + "LikeExpression as escape character; however, only "
                              + "character literals and "
                              + "input parameters are allowed in this position. "
                              + "This is either a programming error in the EJB QL "
                              + "parser and SQL mapper or an inconsistency "
                              + "between EJB QL parser and SQL mapper. Please make "
                              + "sure that you run consistent versions of EJB QL "
                              + "parser and SQL mapper.",
                              "CSM025");
    }

    if ( pattern instanceof InputParameter )
    {
      DevTrace.debugInfo(loc, processLikeExpression, "Pattern is InputParameter.");
      InputParameter inputParameter = (InputParameter) pattern;
      InputParameterDefinition definition = new InputParameterDefinition(inputParameter, null);
      InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);

      if ( escapeCharacter == null )
      {
        if ( likeExpr.isNegated() )
        {
          this.whereNodeManager.makeNode(ComparativeOperator.NOT_LIKE,
                                         beanField, relation,
                                         occurrence,
                                         (LiteralValue) null
                                        );
        }
        else
        {
          this.whereNodeManager.makeNode(ComparativeOperator.LIKE,
                                         beanField, relation,
                                         occurrence,
                                         (LiteralValue) null
                                        );
        }

        DevTrace.exiting(loc, processLikeExpression);
        return;
      }

      if ( escapeCharacter instanceof Literal )
      {
        DevTrace.debugInfo(loc, processLikeExpression, "ESC is Literal.");

        if ( likeExpr.isNegated() )
        {
          this.whereNodeManager.makeNode(ComparativeOperator.NOT_LIKE,
                                beanField, relation,
                                occurrence,
                                ProcessingHelper.createLiteralFromLiteralExpression( (Literal) escapeCharacter ) 
                                        );
        }
        else
        {
          this.whereNodeManager.makeNode(ComparativeOperator.LIKE,
                                beanField, relation,
                                occurrence,
                                ProcessingHelper.createLiteralFromLiteralExpression( (Literal) escapeCharacter )
                                        );
        }

        DevTrace.exiting(loc, processLikeExpression);
        return;
      }

      if ( escapeCharacter instanceof InputParameter )
      {
        DevTrace.debugInfo(loc, processLikeExpression, "ESC is InputParameter.");
        InputParameter secondInputParameter = (InputParameter) escapeCharacter;
        InputParameterDefinition secondDefinition = new InputParameterDefinition(secondInputParameter, null);
        InputParameterOccurrence secondOccurrence = this.treeNodeManager.registerInputParameter(secondDefinition);

        if ( likeExpr.isNegated() )
        {
          this.whereNodeManager.makeNode(ComparativeOperator.NOT_LIKE,
                                         beanField, relation,
                                         occurrence,
                                         secondOccurrence
                                        );
        }
        else
        {
          this.whereNodeManager.makeNode(ComparativeOperator.LIKE,
                                         beanField, relation,
                                         occurrence,
                                         secondOccurrence
                                        );
        }

        DevTrace.exiting(loc, processLikeExpression);
        return;
      }

      String className = escapeCharacter.getClass().getName();
      DevTrace.exitingWithException(loc, processLikeExpression);
      throw new SQLMappingException("Unexpected expression " + className
                              + " as escape character in LikeExpression.",
                              "In the EJB QL parsed tree, SQL mapper has encountered "
                              + "an expression " + className + " within a "
                              + "LikeExpression as escape character; however, only "
                              + "character literals and "
                              + "input parameters are allowed in this position. "
                              + "This is either a programming error in the EJB QL "
                              + "parser and SQL mapper or an inconsistency "
                              + "between EJB QL parser and SQL mapper. Please make "
                              + "sure that you run consistent versions of EJB QL "
                              + "parser and SQL mapper.",
                              "CSM027");
   
    }

    String className = pattern.getClass().getName();
    DevTrace.exitingWithException(loc, processLikeExpression);
    throw new SQLMappingException("Unexpected expression " + className
                              + " as pattern value in LikeExpression.",
                              "In the EJB QL parsed tree, SQL mapper has encountered "
                              + "an expression " + className + " within a "
                              + "LikeExpression as pattern value; however, only "
                              + "string literals and "
                              + "input parameters are allowed in this position. "
                              + "This is either a programming error in the EJB QL "
                              + "parser and SQL mapper or an inconsistency "
                              + "between EJB QL parser and SQL mapper. Please make "
                              + "sure that you run consistent versions of EJB QL "
                              + "parser and SQL mapper.",
                              "CSM029");
 
  }

  /**
   * Processes a between expression.
   * @param betweenExpr
   *     between expression to be processed.
   * @throws SQLMappingException
   *     if an error occurs during processing.
   **/
  private void processBetweenExpression(BetweenExpression betweenExpr)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { betweenExpr };
      DevTrace.entering(loc, processBetweenExpression, processBetweenExpressionParms,
                                                       inputValues);
    }

    Type type;
    Object[] operand;
    Object[] lowerBound;
    Object[] upperBound;

    Expression operandExpr= betweenExpr.getValue();
    type = operandExpr.getType();

    if ( type.isNumeric() )
    {
      this.arithmeticExpressionProcessor.build(operandExpr);
      operand = this.expNodeManager.endOfExpression();
    }
    else
    {
      if ( type.isString() )
      {
        this.stringExpressionProcessor.build(operandExpr);
        operand = this.expNodeManager.endOfExpression();
      }
      else
      {
        String className = operandExpr.getClass().getName();
        DevTrace.exitingWithException(loc, processBetweenExpression);
        throw new SQLMappingException("Unbeknownst expression " + className
                                      + " in BetweenExpression.",
                                      "In the EJB QL parsed tree, SQL mapper has encountered "
                                      + "an expression " + className + " within a "
                                      + "BetweenExpression as operand; however, only "
                                      + "arithmetic expressions and string expressions "
                                      + "are allowed in this position. "
                                      + "This is either a programming error in the EJB QL "
                                      + "parser and SQL mapper or an inconsistency "
                                      + "between EJB QL parser and SQL mapper. Please make "
                                      + "sure that you run consistent versions of EJB QL "
                                      + "parser and SQL mapper.",
                                      "CSM031");
      }
    } 

    Expression lowerBoundExpr = betweenExpr.getLowerBound();
    type = lowerBoundExpr.getType();

    if ( type.isNumeric() )
    {
      this.arithmeticExpressionProcessor.build(lowerBoundExpr);
      lowerBound = this.expNodeManager.endOfExpression();
    }
    else
    {
      if ( type.isString() )
      {
        this.stringExpressionProcessor.build(lowerBoundExpr);
        lowerBound = this.expNodeManager.endOfExpression();
      }
      else
      {
        String className = lowerBoundExpr.getClass().getName();
        DevTrace.exitingWithException(loc, processBetweenExpression);
        throw new SQLMappingException("Unbeknownst expression " + className
                                      + " in BetweenExpression.",
                                      "In the EJB QL parsed tree, SQL mapper has encountered "
                                      + "an expression " + className + " within a "
                                      + "BetweenExpression as lower bound; however, only "
                                      + "arithmetic expressions and string expressions "
                                      + "are allowed in this position. "
                                      + "This is either a programming error in the EJB QL "
                                      + "parser and SQL mapper or an inconsistency "
                                      + "between EJB QL parser and SQL mapper. Please make "
                                      + "sure that you run consistent versions of EJB QL "
                                      + "parser and SQL mapper.",
                                      "CSM033");
      }
    }

    Expression upperBoundExpr = betweenExpr.getUpperBound();
    type = upperBoundExpr.getType();

    if ( type.isNumeric() )
    {
      this.arithmeticExpressionProcessor.build(upperBoundExpr);
      upperBound = this.expNodeManager.endOfExpression();
    }
    else
    {
      if ( type.isString() )
      {
        this.stringExpressionProcessor.build(upperBoundExpr);
        upperBound = this.expNodeManager.endOfExpression();
      }
      else
      {
        String className = upperBoundExpr.getClass().getName();
        DevTrace.exitingWithException(loc, processBetweenExpression);
        throw new SQLMappingException("Unbeknownst expression " + className
                                      + " in BetweenExpression.",
                                      "In the EJB QL parsed tree, SQL mapper has encountered "
                                      + "an expression " + className + " within a "
                                      + "BetweenExpression as upper bound; however, only "
                                      + "arithmetic expressions and string expressions "
                                      + "are allowed in this position. "
                                      + "This is either a programming error in the EJB QL "
                                      + "parser and SQL mapper or an inconsistency "
                                      + "between EJB QL parser and SQL mapper. Please make "
                                      + "sure that you run consistent versions of EJB QL "
                                      + "parser and SQL mapper.",
                                      "CSM035");
      }

    }

    if ( betweenExpr.isNegated() )
    {
      this.whereNodeManager.makeNode(ComparativeOperator.NOT_BETWEEN,
                                    operand, lowerBound, upperBound);
    }
    else
    {
      this.whereNodeManager.makeNode(ComparativeOperator.BETWEEN,
                                    operand, lowerBound, upperBound);
    }

    DevTrace.exiting(loc, processBetweenExpression);
    return;
    
  }

  /**
   * Processes a comparison expression.
   * <B>Note</B> that for boolean operands left to right
   * evaluation of operands is vital; <code>BooleanNodeManager</code>
   * requires this (natural) order to be observed.
   * @param comp
   *     comparison expression to be processed.
   * @throws SQLMappingException
   *     if an error occurs during processing.
   **/
  private void processComparisonExpression(ComparisonExpression comp)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { comp };
      DevTrace.entering(loc, processComparisonExpression, processComparisonExpressionParms,
                                                          inputValues);
    }

    final ComparisonOperator operator = this.getComparisonOperator(comp.getOperator());

    Expression leftExpr  = comp.getLeftOperand();
    Expression rightExpr = comp.getRightOperand();

    Object[] left  = this.evaluateExpression(leftExpr);
    Object[] right = this.evaluateExpression(rightExpr);

    this.whereNodeManager.makeNode(operator, left, right);

    DevTrace.exiting(loc, processComparisonExpression);
    return;
  }

  /**
   * Evaluates an expression which may occur as operand
   * of a simple conditional expression. Boolean, date time
   * and entity bean expressions are evaluated by respective
   * private methods, while arithmetic and string expressions
   * are delegated to the associated <code>ArithmeticExpressionProcessor</code>
   * and <code>StringExpressionProcessor</code> objects.
   * @param expr
   *     expression to be evaluated.
   * @throws SQLMappingException
   *     if an error is encountered during processing.
   * @return
   *     the SQL representation of <code>expr</code>.
   *     An array of <code>com.sap.sql.tree.ValueExpression</code>
   *     is returned. For entity bean expressions the number
   *     of elements in the array corresponds to the number
   *     of primary key fields of the respective abstract schema
   *     type; for all other expressions the array consists of
   *     one element only.
   * @see com.sap.sql.tree.ValueExpression
   **/
  private Object[] evaluateExpression(Expression expr)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { expr };
      DevTrace.entering(loc, evaluateExpression, evaluateExpressionParms,
                                                 inputValues);
    }

    Type type = expr.getType();
    Object[] result;

    if ( type.isNumeric() )
    {
      this.arithmeticExpressionProcessor.build(expr);
      result = this.expNodeManager.endOfExpression();
      DevTrace.exiting(loc, evaluateExpression, result);
      return result;
    }

    if ( type.isString() ) 
    {
      this.stringExpressionProcessor.build(expr);
      result = this.expNodeManager.endOfExpression();
      DevTrace.exiting(loc, evaluateExpression, result);
      return result;
    }

    if ( type.isDate() )
    {
      this.buildDateTimeExpression(expr);
      result = this.expNodeManager.endOfExpression();
      DevTrace.exiting(loc, evaluateExpression, result);
      return result;
    }

    if ( type.isBoolean() )
    {
      this.buildBooleanExpression(expr);
      result = this.booleanNodeManager.endOfExpression();
      DevTrace.exiting(loc, evaluateExpression, result);
      return result;
    }

    if ( type.isEntityBean() )
    {
      this.buildEntityBeanExpression(expr);
      result = this.expNodeManager.endOfExpression();
      DevTrace.exiting(loc, evaluateExpression, result);
      return result;
    }

    String className = expr.getClass().getName();
    DevTrace.exitingWithException(loc, evaluateExpression);
    throw new SQLMappingException("Unbeknownst expression " + className + " encountered.",
                                  "In the EJB QL parsed tree, SQL mapper has encountered "
                                  + "unexpected expression " + className + " of type "
                                  + type.toString() + ". This expression is neither arithmetic "
                                  + "nor string nor datetime nor boolean nor entity bean "
                                  + "expression. "
                                  + "Cause is either a programming error in the EJB QL "
                                  + "parser and SQL mapper or an inconsistency "
                                  + "between EJB QL parser and SQL mapper. Please make "
                                  + "sure that you run consistent versions of EJB QL "
                                  + "parser and SQL mapper.",
                                  "CSM047");

  }

  /**
   * Evaluates a date time expression.
   * @param expr
   *     date time expression to be evaluated.
   * @throws SQLMappingException
   *     if an error is encountered during processing.
   **/
  private void buildDateTimeExpression(Expression expr)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { expr };
      DevTrace.entering(loc, buildDateTimeExpression, buildDateTimeExpressionParms, inputValues);
    }

    if ( expr.isPath() )
    {
      DevTrace.debugInfo(loc, buildDateTimeExpression, "DateTimeExpression is Path.");
      BeanField[] relation = ProcessingHelper.extractRelationsFromPath( (PathExpression) expr);
      BeanField beanField = ProcessingHelper.getPathDestination( (PathExpression) expr);

      this.expNodeManager.makeExpression(beanField, relation);
      DevTrace.exiting(loc, buildDateTimeExpression);
      return;
    }

    if ( expr.isInputParameter() )
    {
      DevTrace.debugInfo(loc, buildDateTimeExpression, "DateTimeExpression is InputParameter.");
      InputParameter inputParameter = (InputParameter) expr;
      InputParameterDefinition definition = new InputParameterDefinition(inputParameter, null);
      InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);

      this.expNodeManager.makeExpression(occurrence);
      DevTrace.exiting(loc, buildDateTimeExpression);
      return;
    }

    String className = expr.getClass().getName();
    DevTrace.exitingWithException(loc, buildDateTimeExpression);
    throw new SQLMappingException("Unbeknownst datetime expression " + className + " encountered.",
                                  "In the EJB QL parsed tree, SQL mapper has encountered "
                                  + " expression " + className + " where a datetime expression was "
                                  + "expected." 
                                  + "This is either a programming error in the EJB QL "
                                  + "parser and SQL mapper or an inconsistency "
                                  + "between EJB QL parser and SQL mapper. Please make "
                                  + "sure that you run consistent versions of EJB QL "
                                  + "parser and SQL mapper.",
                                  "CSM049");
  }

  /**
   * Evaluates a boolean expression.
   * <B>Note</B> that, unlike with other expressions, 
   * <code>BooleanNodeManager</code> rather than
   * <code>ExpressionNodeManager</code> is used
   * as partner for SQL translation.
   * @param expr
   *     boolean expression to be evaluated.
   * @throws SQLMappingException
   *     if an error is encountered during processing.
   **/
  private void buildBooleanExpression(Expression expr)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { expr };
      DevTrace.entering(loc, buildBooleanExpression, buildBooleanExpressionParms, inputValues);
    }

    if ( expr.isPath() )
    {
      DevTrace.debugInfo(loc, buildBooleanExpression, "BooleanExpression is Path.");
      BeanField[] relation = ProcessingHelper.extractRelationsFromPath( (PathExpression) expr);
      BeanField beanField = ProcessingHelper.getPathDestination( (PathExpression) expr);

      this.booleanNodeManager.makeExpression(beanField, relation);
      DevTrace.exiting(loc, buildBooleanExpression);
      return;
    }

    if ( expr.isInputParameter() )
    {
      DevTrace.debugInfo(loc, buildBooleanExpression, "BooleanExpression is InputParameter.");
      InputParameter inputParameter = (InputParameter) expr;
      InputParameterDefinition definition = new InputParameterDefinition(inputParameter, null);
      InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);

      this.booleanNodeManager.makeExpression(occurrence);
      DevTrace.exiting(loc, buildBooleanExpression);
      return;
    }

    if ( expr instanceof Literal )
    {
      DevTrace.debugInfo(loc, buildBooleanExpression, "BooleanExpression is Literal.");
      this.booleanNodeManager.makeExpression(
             ProcessingHelper.createLiteralFromLiteralExpression( (Literal) expr )
                                          );
      DevTrace.exiting(loc, buildBooleanExpression);
      return;
    }

    String className = expr.getClass().getName();
    DevTrace.exitingWithException(loc, buildBooleanExpression);
    throw new SQLMappingException("Unbeknownst boolean expression " + className + " encountered.",
                                  "In the EJB QL parsed tree, SQL mapper has encountered "
                                  + " expression " + className + " where a boolean expression was "
                                  + "expected."
                                  + "This is either a programming error in the EJB QL "
                                  + "parser and SQL mapper or an inconsistency "
                                  + "between EJB QL parser and SQL mapper. Please make "
                                  + "sure that you run consistent versions of EJB QL "
                                  + "parser and SQL mapper.",
                                  "CSM051");

  }

  /**
   * Evaluates an entity bean expression.
   * @param expr
   *     entity bean expression to be evaluated.
   * @throws SQLMappingException
   *     if an error is encountered during processing.
   **/
  private void buildEntityBeanExpression(Expression expr)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { expr };
      DevTrace.entering(loc, buildEntityBeanExpression, buildEntityBeanExpressionParms,
                                                        inputValues);
    }

    if ( expr.isPath() )
    {
      DevTrace.debugInfo(loc, buildEntityBeanExpression, "EntityBeanExpression is Path.");
      BeanField[] relation = ProcessingHelper.extractRelationsFromPath( (PathExpression) expr);
      BeanField beanField = ProcessingHelper.getPathDestination( (PathExpression) expr);

      this.expNodeManager.makeExpression(beanField, relation);
      DevTrace.exiting(loc, buildEntityBeanExpression);
      return;
    }

    if ( expr.isIdentificationVariable() )
    {
      DevTrace.debugInfo(loc, buildEntityBeanExpression, "EntityBeanExpression is IdentificationVariable.");
      IdentificationVariable idVar = (IdentificationVariable) expr;
      BeanObject beanObject =
          new BeanObject(idVar.getDeclaration().getType().toString(), idVar.getName().toUpperCase());

      this.expNodeManager.makeExpression(beanObject);
      DevTrace.exiting(loc, buildEntityBeanExpression);
      return;
    }

    if ( expr.isInputParameter() )
    {
      DevTrace.debugInfo(loc, buildEntityBeanExpression, "EntityBeanExpression is InputParameter.");
      InputParameter inputParameter = (InputParameter) expr;
      InputParameterDefinition definition = new InputParameterDefinition(inputParameter, null);
      InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);

      this.expNodeManager.makeExpression(occurrence);
      DevTrace.exiting(loc, buildEntityBeanExpression);
      return;
    }

    String className = expr.getClass().getName();
    DevTrace.exitingWithException(loc, buildEntityBeanExpression);
    throw new SQLMappingException("Unbeknownst entity bean expression " + className + " encountered.",
                                  "In the EJB QL parsed tree, SQL mapper has encountered "
                                  + " expression " + className + " where an entity bean expression was "
                                  + "expected."
                                  + "This is either a programming error in the EJB QL "
                                  + "parser and SQL mapper or an inconsistency "
                                  + "between EJB QL parser and SQL mapper. Please make "
                                  + "sure that you run consistent versions of EJB QL "
                                  + "parser and SQL mapper.",
                                  "CSM053");
    
  }     

  /**
   * Translates comparison operators from EJB-QL parser representation
   * into SQL tree representation. Class <code>ComparativeOperator</code>
   * is used for that purpose.
   * @param operator
   *     EJB-QL parser representation of comparison operator.
   * @throws SQLMappingException
   *     if unknown operator is encountered.
   * @return
   *     SQL tree representation of comparison operator.
   * @see com.sap.ejb.ql.sqlmapper.common.ComparativeOperator
   **/
  private ComparisonOperator getComparisonOperator(ComparisonExpression.Operator operator)
    throws SQLMappingException
  {
    if ( operator == ComparisonExpression.Operator.LESS )
    {
      return ComparisonOperator.LESS_THAN;
    }

    if ( operator == ComparisonExpression.Operator.GREATER )
    {
      return ComparisonOperator.GREATER_THAN;
    }

    if ( operator == ComparisonExpression.Operator.LESS_EQUAL )
    {
      return ComparisonOperator.LESS_OR_EQUAL;
    }

    if ( operator == ComparisonExpression.Operator.GREATER_EQUAL )
    {
      return ComparisonOperator.GREATER_OR_EQUAL;
    }

    if ( operator == ComparisonExpression.Operator.EQUAL )
    {
      return ComparisonOperator.EQUAL;
    }

    if ( operator == ComparisonExpression.Operator.UNEQUAL )
    {
      return ComparisonOperator.NOT_EQUAL;
    }

    throw new SQLMappingException("Unbeknownst comparison Operator " + operator.toString()
                                 + ".",
                                 "In the EJB QL parsed tree, SQL mapper has encountered "
                                 + "unknown comparison operator " + operator.toString()
                                 + ". This is most likely a programming error in the "
                                 +"EJB QL parser.",
                                 "CSM017");
  }

}
