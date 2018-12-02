package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejbql.tree.Expression;
import com.sap.ejbql.tree.ArithmeticExpression;
import com.sap.ejbql.tree.PathExpression;
import com.sap.ejbql.tree.InputParameter;
import com.sap.ejbql.tree.Literal;
import com.sap.ejbql.tree.BuiltinFunction;
import com.sap.ejbql.tree.BuiltinABS;
import com.sap.ejbql.tree.BuiltinSQRT;
import com.sap.ejbql.tree.BuiltinMOD;
import com.sap.ejbql.tree.BuiltinLENGTH;
import com.sap.ejbql.tree.BuiltinLOCATE;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager;
import com.sap.ejb.ql.sqlmapper.common.ExpressionNodeManager;
import com.sap.ejb.ql.sqlmapper.common.StringExpressionProcessor;
import com.sap.ejb.ql.sqlmapper.common.ProcessingHelper;
import com.sap.ejb.ql.sqlmapper.common.BeanField;
import com.sap.ejb.ql.sqlmapper.common.InputParameterDefinition;
import com.sap.ejb.ql.sqlmapper.common.InputParameterOccurrence;

import com.sap.sql.tree.ArithmeticExpressionOperator;
import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

/**
 * This class processes arithmetic expressions and sends appropriate
 * commands to an associated <code>ExpressionNodeManager</code> object
 * in order to create an SQL representation for the expression.
 * An <code>ArithmeticExpressionProcessor</code> instance is created
 * with an <code>SQLTreeNodeManager</code> object as argument.
 * It retrieves the <code>ExpressionNodeManager</code> object
 * from the <code>SQLTreeNodeManager</code> object and uses
 * the <code>SQLTreeNodeManager</code> object itself for registering
 * input parameters. Furthermore a reference to a 
 * <code>StringExpressionProcessor</code> object is required. For 
 * arithmetic expressions may contain subexpressions of type string,
 * whose evaluation is delegated to the <code>StringExpressionProcessor</code>
 * object.
 * </p><p>
 * Copyright (c) 2002-2006, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 * @see com.sap.ejb.ql.sqlmapper.common.ExpressionNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.StringExpressionProcessor
 **/
public class ArithmeticExpressionProcessor
{
  private static final Location loc = Location.getLocation(ArithmeticExpressionProcessor.class);
  private static final String build = "build";
  private static final String processAsArithmeticExpression = "processAsArithmeticExpression";
  private static final String processAsArithmeticPrimary = "processAsArithmeticPrimary";
  private static final String processFunction = "processFunction";
  private static final String setStringExpressionProcessor = "setStringExpressionProcessor";
  private static final String buildParms[] = { "expr" };
  private static final String processAsArithmeticExpressionParms[] = { "expr" };
  private static final String processAsArithmeticPrimaryParms[] = { "expr" };
  private static final String processFunctionParms[] = { "function" };

  private SQLTreeNodeManager treeNodeManager;
  private StringExpressionProcessor stringExpressionProcessor;
  private ExpressionNodeManager expNodeManager;

  /** 
   * Creates an <code>ArithmeticExpressionProcessor</code> instance
   * with an <code>SQLTreeNodeManager</code> object as argument.
   * An <code>ExpressionNodeManager</code> object is retrieved
   * from the <code>SQLTreeNodeManager</code> object.
   * @param treeNodeManager
   *     the associated <code>SQLTreeNodeManager</code> object.
   **/
  ArithmeticExpressionProcessor(SQLTreeNodeManager treeNodeManager)
  {
    this.treeNodeManager = treeNodeManager;
    this.stringExpressionProcessor = null;
    this.expNodeManager = this.treeNodeManager.getExpressionNodeManager();
  }

  /**
   * Sets reference to a <code>StringExpressionProcessor</code> object.
   * @param stringExpressionProcessor
   *     a <code>StringExpressionProcessor</code> object for delegation
   *     of string subexpressions.
   **/
  void setStringExpressionProcessor(StringExpressionProcessor stringExpressionProcessor)
  {
    DevTrace.debugInfo(loc, setStringExpressionProcessor, "Setting StringExpressionProcessor.");
    this.stringExpressionProcessor = stringExpressionProcessor;
  }

  /**
   * Processes an arithmetic expression. If expression cannot be
   * identified as arithmetic primary it is treated as complex
   * arithmetic expression.
   * @param expr
   *     arithmetic expression to be processed.
   * @throws SQLMappingException
   *     if an error occurs during processing.
   **/ 
  void build(Expression expr)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { expr };
      DevTrace.entering(loc, build, buildParms, inputValues);
    }

    if ( ! this.processAsArithmeticPrimary(expr) )
    {
      this.processAsArithmeticExpression( (ArithmeticExpression) expr);
    }

    DevTrace.exiting(loc, build);
    return;
  }

  /**
   * Processes a complex arithmetic expression.
   * <B>Note</B> that this method calls <code>build()</code> again
   * for processing of arithmetic subexpressions.
   * @param expr
   *     complex arithmetic expression.
   * @throws SQLMappingException
   *     if an error is encountered during processing.
   **/
  private void processAsArithmeticExpression( ArithmeticExpression expr)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { expr };
      DevTrace.entering(loc, processAsArithmeticExpression, processAsArithmeticExpressionParms,
                                                            inputValues);
    }

    ArithmeticExpression.Operator operator = expr.getOperator();
    Expression leftOperand =  expr.getLeftOperand();

    this.build(leftOperand);

    if ( operator instanceof ArithmeticExpression.MonadicOperator )
    {
      DevTrace.debugInfo(loc, processAsArithmeticExpression,
                         "Monadic arithmetic operator encountered.");
      this.expNodeManager.alterExpression(
            this.getMonadicOperator( (ArithmeticExpression.MonadicOperator) operator)
                                          );
      DevTrace.exiting(loc, processAsArithmeticExpression);
      return;
    }

    DevTrace.debugInfo(loc, processAsArithmeticExpression,
                       "Dyadic arithmetic operator encountered.");

    Expression rightOperand = expr.getRightOperand();

    this.build(rightOperand); 

    this.expNodeManager.popExpression(
           this.getDyadicOperator( (ArithmeticExpression.DyadicOperator) operator)
                                      );

    DevTrace.exiting(loc, processAsArithmeticExpression);
    return;
  }

  /**
   * Tries to process given arithmetic expression as arithmetic primary.
   * @param expr
   *     arithmetic expression to be processed.
   * @throws SQLMappingException
   *     if an error is encountered during processing.
   * @return
   *     <code>true<code> - if <code>expr</code> could be
   *     identified as arithmetic primary.<BR>
   *     <code>false</code> - elsewise.
   **/
  private boolean processAsArithmeticPrimary(Expression expr)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { expr };
      DevTrace.entering(loc, processAsArithmeticPrimary, processAsArithmeticPrimaryParms,
                                                         inputValues);
    }

    if (expr.isPath() )
    {
      DevTrace.debugInfo(loc, processAsArithmeticPrimary, "ArithmeticExpression is Path.");
      BeanField [] relation =
          ProcessingHelper.extractRelationsFromPath( (PathExpression) expr);
      BeanField beanField = ProcessingHelper.getPathDestination( (PathExpression) expr);

      this.expNodeManager.makeExpression(beanField, relation);
      DevTrace.exiting(loc, processAsArithmeticPrimary, true);
      return true;
    }

    if ( expr.isInputParameter() )
    {
      DevTrace.debugInfo(loc, processAsArithmeticPrimary, "ArithmeticExpression is InputParameter.");
      InputParameter inputParameter = (InputParameter) expr;
      InputParameterDefinition definition = new InputParameterDefinition(inputParameter, null);
      InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);

      this.expNodeManager.makeExpression(occurrence);
      DevTrace.exiting(loc, processAsArithmeticPrimary, true);
      return true;
    }

    if ( expr.isFunction() )
    {
      DevTrace.debugInfo(loc, processAsArithmeticPrimary, "ArithmeticExpression is Built-in Function.");
      this.processFunction( (BuiltinFunction) expr);
      DevTrace.exiting(loc, processAsArithmeticPrimary, true);
      return true;
    }

    if ( expr instanceof Literal )
    {
      DevTrace.debugInfo(loc, processAsArithmeticPrimary, "ArithmeticExpression is Literal.");
      this.expNodeManager.makeExpression(
           ProcessingHelper.createLiteralFromLiteralExpression( (Literal) expr)
                                          );
      DevTrace.exiting(loc, processAsArithmeticPrimary, true);
      return true;
    } 

    DevTrace.exiting(loc, processAsArithmeticPrimary, false);
    return false;
  }

  /**
   * Processes EJB-QL built-in functions.
   * Here string subexpressions may occur, which are delegated
   * to the associated <code>StringExpressionProcessor</code>
   * object.
   * @param function
   *     EJB-QL built-in function to be processed.
   * @throws SQLMappingException
   *     if an error is encountered during processing.
   **/
  private void processFunction(BuiltinFunction function)
    throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { function };
      DevTrace.entering(loc, processFunction, processFunctionParms, inputValues);
    }

    if ( function instanceof BuiltinABS )
    {
      DevTrace.debugInfo(loc, processFunction, "Function is ABS.");
      this.build( ( (BuiltinABS) function ).getArgument() );
      this.expNodeManager.addFunctionToExpression(ExpressionNodeManager.ABS);
      DevTrace.exiting(loc, processFunction);
      return;
    }

    if ( function instanceof BuiltinSQRT )
    {
      DevTrace.debugInfo(loc, processFunction, "Function is SQRT.");
      this.build( ( (BuiltinSQRT) function ).getArgument() );
      this.expNodeManager.addFunctionToExpression(ExpressionNodeManager.SQRT);
      DevTrace.exiting(loc, processFunction);
      return;
    }

    if ( function instanceof BuiltinMOD )
    {
      DevTrace.debugInfo(loc, processFunction, "Function is MOD.");
      BuiltinMOD modulo = (BuiltinMOD) function;

      this.build(modulo.getDividend());
      this.build(modulo.getDivisor());
      this.expNodeManager.popExpressionWithFunction(ExpressionNodeManager.MOD);
      DevTrace.exiting(loc, processFunction);
      return;
    }

    if ( function instanceof BuiltinLENGTH )
    {
      DevTrace.debugInfo(loc, processFunction, "Function is LENGTH.");
      this.stringExpressionProcessor.build( ( (BuiltinLENGTH) function ).getArgument() );
      this.expNodeManager.addFunctionToExpression(ExpressionNodeManager.LENGTH);
      DevTrace.exiting(loc, processFunction);
      return;
    }

    if ( function instanceof BuiltinLOCATE )
    {
      DevTrace.debugInfo(loc, processFunction, "Function is LOCATE.");
      BuiltinLOCATE locate = (BuiltinLOCATE) function;

      this.stringExpressionProcessor.build(locate.getContainedString());
      this.stringExpressionProcessor.build(locate.getContainerString());

      Expression start = locate.getStart();

      if ( start != null )
      {
        this.build(start);
        this.expNodeManager.doublePopExpressionWithFunction(ExpressionNodeManager.LOCATE);
      }
      else
      {
        this.expNodeManager.popExpressionWithFunction(ExpressionNodeManager.LOCATE);
      }

      DevTrace.exiting(loc, processFunction);
      return;
    }

    String className = function.getClass().getName();
    DevTrace.exitingWithException(loc, processFunction);
    throw new SQLMappingException("Unbeknownst (or illegal use of) built-in function "
                                  + className + " encountered.",
                                  "In the EJB QL parsed tree, SQL mapper has encountered "
                                  + "built-in function " + className + ". This function "
                                  + "is either unknown or its use within an arithmetic expression "
                                  + "is not allowed."
                                  + "This is either a programming error in the EJB QL "
                                  + "parser and SQL mapper or an inconsistency "
                                  + "between EJB QL parser and SQL mapper. Please make "
                                  + "sure that you run consistent versions of EJB QL "
                                  + "parser and SQL mapper.",
                                    "CSM041");

  }

  /**
   * Translates monadic operators from EJB-QL parser representation to
   * its SQL tree representation. Class <code>ArithmeticOperator</code>
   * is used for that purpose.
   * @param operator
   *     EJB-QL parser representation of monadic arithmetic operator.
   * @throws SQLMappingException
   *     if unknown monadic arithmetic operator is encountered.
   * @return
   *     SQL tree representation of monadic arithmetic operator.
   * @see com.sap.ejb.ql.sqlmapper.common.ArithmeticOperator
   **/  
  private ArithmeticExpressionOperator getMonadicOperator(ArithmeticExpression.MonadicOperator operator)
    throws SQLMappingException
  {
    if ( operator == ArithmeticExpression.MonadicOperator.MINUS )
    {
      return ArithmeticExpressionOperator.MINUS;
    }

    if ( operator == ArithmeticExpression.MonadicOperator.PLUS )
    {
      return ArithmeticExpressionOperator.PLUS;
    }

    throw new SQLMappingException("Unbeknownst monadic arithmetic operator "
                                  + operator.toString() + " encountered.",
                                  "In the EJB QL parsed tree, SQL mapper has encountered "
                                  + "unknown monadic arithmetic operator " + operator.toString() 
                                  + "within an arithmetic expression. "
                                  + "This is either a programming error in the EJB QL "
                                  + "parser and SQL mapper or an inconsistency "
                                  + "between EJB QL parser and SQL mapper. Please make "
                                  + "sure that you run consistent versions of EJB QL "
                                  + "parser and SQL mapper.",
                                    "CSM043");
  }

  /**
   * Translates dyadic operators from EJB-QL parser representation to
   * its SQL tree representation. Class <code>ArithmeticOperator</code>
   * is used for that purpose.
   * @param operator
   *     EJB-QL parser representation of dyadic arithmetic operator.
   * @throws SQLMappingException
   *     if unknown dyadic arithmetic operator is encountered.
   * @return
   *     SQL tree representation of dyadic arithmetic operator.
   * @see com.sap.ejb.ql.sqlmapper.common.ArithmeticOperator
   **/
  private ArithmeticExpressionOperator getDyadicOperator(ArithmeticExpression.DyadicOperator operator)
    throws SQLMappingException
  {
    if ( operator == ArithmeticExpression.DyadicOperator.PLUS )
    {
      return ArithmeticExpressionOperator.PLUS;
    }

    if ( operator == ArithmeticExpression.DyadicOperator.MINUS )
    {
      return ArithmeticExpressionOperator.MINUS;
    }

    if ( operator == ArithmeticExpression.DyadicOperator.TIMES )
    {
      return ArithmeticExpressionOperator.TIMES;
    }

    if ( operator == ArithmeticExpression.DyadicOperator.DIVIDE )
    {
      return ArithmeticExpressionOperator.DIVIDE;
    }

    throw new SQLMappingException("Unbeknownst dyadic arithmetic operator "
                                  + operator.toString() + " encountered.",
                                  "In the EJB QL parsed tree, SQL mapper has encountered "
                                  + "unknown dyadic arithmetic operator " + operator.toString()
                                  + "within an arithmetic expression. "
                                  + "This is either a programming error in the EJB QL "
                                  + "parser and SQL mapper or an inconsistency "
                                  + "between EJB QL parser and SQL mapper. Please make "
                                  + "sure that you run consistent versions of EJB QL "
                                  + "parser and SQL mapper.",
                                    "CSM045");
  }

}
