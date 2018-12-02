package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejbql.tree.Expression;
import com.sap.ejbql.tree.PathExpression;
import com.sap.ejbql.tree.InputParameter;
import com.sap.ejbql.tree.Literal;
import com.sap.ejbql.tree.BuiltinFunction;
import com.sap.ejbql.tree.BuiltinCONCAT;
import com.sap.ejbql.tree.BuiltinSUBSTRING;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager;
import com.sap.ejb.ql.sqlmapper.common.ExpressionNodeManager;
import com.sap.ejb.ql.sqlmapper.common.ProcessingHelper;
import com.sap.ejb.ql.sqlmapper.common.ArithmeticExpressionProcessor;
import com.sap.ejb.ql.sqlmapper.common.BeanField;
import com.sap.ejb.ql.sqlmapper.common.InputParameterDefinition;
import com.sap.ejb.ql.sqlmapper.common.InputParameterOccurrence;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

/**
 * This class processes string expressions and sends appropriate
 * commands to an associated <code>ExpressionNodeManager</code> object
 * in order to create an SQL representation for the expression.
 * A <code>StringExpressionProcessor</code> instance is created
 * with an <code>SQLTreeNodeManager</code> object as argument.
 * It retrieves the <code>ExpressionNodeManager</code> object
 * from the <code>SQLTreeNodeManager</code> object and uses
 * the <code>SQLTreeNodeManager</code> object itself for registering
 * input parameters. Furthermore a reference to an
 * <code>ArithmeticExpressionProcessor</code> object is required. For
 * string expressions may contain subexpressions of arithmetic type,
 * whose evaluation is delegated to the <code>ArithmeticExpressionProcessor</code>
 * object.
 * </p><p>
 * Copyright (c) 2002-2006, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 * @see com.sap.ejb.ql.sqlmapper.common.ExpressionNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.ArithmeticExpressionProcessor
 **/
public class StringExpressionProcessor
{
  private static final Location loc = Location.getLocation(StringExpressionProcessor.class);
  private static final String setArithmeticExpressionProcessor = "setArithmeticExpressionProcessor";
  private static final String build = "build";
  private static final String processFunction = "processFunction";
  private static final String buildParms[] = { "expr" };
  private static final String processFunctionParms[] = { "function" };

  private SQLTreeNodeManager treeNodeManager;
  private ExpressionNodeManager expNodeManager;
  private ArithmeticExpressionProcessor arithmeticExpressionProcessor;

  /**
   * Creates a <code>StringExpressionProcessor</code> instance
   * with an <code>SQLTreeNodeManager</code> object as argument.
   * An <code>ExpressionNodeManager</code> object is retrieved
   * from the <code>SQLTreeNodeManager</code> object.
   * @param treeNodeManager
   *     the associated <code>SQLTreeNodeManager</code> object.
   **/
  StringExpressionProcessor(SQLTreeNodeManager treeNodeManager)
  {
    this.treeNodeManager = treeNodeManager;
    this.expNodeManager = treeNodeManager.getExpressionNodeManager();
    this.arithmeticExpressionProcessor = null;
  }

  /**
   * Sets reference to an <code>ArithmeticExpressionProcessor</code> object.
   * @param arithmeticExpressionProcessor
   *     an <code>ArithmeticExpressionProcessor</code> object for delegation
   *     of arithmetic subexpressions.
   **/
  void setArithmeticExpressionProcessor(ArithmeticExpressionProcessor arithmeticExpressionProcessor)
  {
    DevTrace.debugInfo(loc, setArithmeticExpressionProcessor, 
                       "Setting ArithmeticExpressionProcessor.");
    this.arithmeticExpressionProcessor = arithmeticExpressionProcessor;
  }

  /**
   * Processes a string expression.
   * @param expr
   *     string expression to be processed.
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

    if ( expr.isPath() )
    {
      DevTrace.debugInfo(loc, build, "StringExpression is Path.");
      BeanField [] relation =
          ProcessingHelper.extractRelationsFromPath( (PathExpression) expr);
      BeanField beanField = ProcessingHelper.getPathDestination( (PathExpression) expr);

      this.expNodeManager.makeExpression(beanField, relation);
      DevTrace.exiting(loc, build);
      return;
    }

    if ( expr.isInputParameter() )
    {
      DevTrace.debugInfo(loc, build, "StringExpression is InputParameter.");
      InputParameter inputParameter = (InputParameter) expr;
      InputParameterDefinition definition = new InputParameterDefinition(inputParameter, null);
      InputParameterOccurrence occurrence = this.treeNodeManager.registerInputParameter(definition);

      this.expNodeManager.makeExpression(occurrence);
      DevTrace.exiting(loc, build);
      return;
    }

    if ( expr.isFunction() )
    {
      DevTrace.debugInfo(loc, build, "StringExpression is Built-in Function.");
      this.processFunction( (BuiltinFunction) expr);
      DevTrace.exiting(loc, build);
      return;
    }

    if ( expr instanceof Literal )
    {
      DevTrace.debugInfo(loc, build, "StringExpression is Literal.");
      this.expNodeManager.makeExpression(
            ProcessingHelper.createLiteralFromLiteralExpression( (Literal) expr )
                                          );
      DevTrace.exiting(loc, build);
      return;
    }

    String className = expr.getClass().getName();
    DevTrace.exitingWithException(loc, build);
    throw new SQLMappingException("Unbeknownst string expression " + className 
                                  + " encountered.",
                                  "In the EJB QL parsed tree, SQL mapper has encountered "
                                  + "unknown expression " + className + "within a string "
                                  + "expression."
                                  + "This is either a programming error in the EJB QL "
                                  + "parser and SQL mapper or an inconsistency "
                                  + "between EJB QL parser and SQL mapper. Please make "
                                  + "sure that you run consistent versions of EJB QL "
                                  + "parser and SQL mapper.",
                                    "CSM037"); 

  }

  /**
   * Processes EJB-QL built-in functions.
   * Here arithmetic subexpressions may occur, which are delegated
   * to the associated <code>ArithmeticExpressionProcessor</code>
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

    if ( function instanceof BuiltinCONCAT )
    {
      DevTrace.debugInfo(loc, processFunction, "Function is CONCAT.");
      BuiltinCONCAT concat = (BuiltinCONCAT) function;

      this.build(concat.getLeftString());
      this.build(concat.getRightString());
      this.expNodeManager.popExpressionWithFunction(ExpressionNodeManager.CONCAT);

      DevTrace.exiting(loc, processFunction);
      return;
    }

    if ( function instanceof BuiltinSUBSTRING )
    {
      DevTrace.debugInfo(loc, processFunction, "Function is SUBSTRING.");
      BuiltinSUBSTRING subString = (BuiltinSUBSTRING) function;

      this.build(subString.getSourceString());
      this.arithmeticExpressionProcessor.build(subString.getStart());
      this.arithmeticExpressionProcessor.build(subString.getLength());
      this.expNodeManager.doublePopExpressionWithFunction(ExpressionNodeManager.SUBSTRING);

      DevTrace.exiting(loc, processFunction);
      return;
    }

    String className = function.getClass().getName();
    DevTrace.exitingWithException(loc, processFunction);
    throw new SQLMappingException("Unbeknownst (or illegal use of) built-in function "
                                  + className + " encountered.",
                                  "In the EJB QL parsed tree, SQL mapper has encountered "
                                  + "built-in function " + className + ". This function "
                                  + "is either unknown or its use within a string expression "
                                  + "is not allowed."
                                  + "This is either a programming error in the EJB QL "
                                  + "parser and SQL mapper or an inconsistency "
                                  + "between EJB QL parser and SQL mapper. Please make "
                                  + "sure that you run consistent versions of EJB QL "
                                  + "parser and SQL mapper.",
                                    "CSM039");
  }
}
  
