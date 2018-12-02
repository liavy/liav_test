package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejbql.tree.ConditionalExpression;
import com.sap.ejbql.tree.ConditionalCombination;

import com.sap.ejb.ql.sqlmapper.SQLMappingException;
import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager;
import com.sap.ejb.ql.sqlmapper.common.WhereNodeManager;
import com.sap.ejb.ql.sqlmapper.common.SimpleConditionProcessor;
import com.sap.ejb.ql.sqlmapper.common.LogicOperator;

import com.sap.tc.logging.Location;
import com.sap.ejb.ql.sqlmapper.general.DevTrace;

/**
 * This class is to traverse the where condition of a parsed EJB-QL tree.
 * <code>EJBQLTreeNodeManager</code> creates a <code>ConditionalProcessor</code>
 * object with a <code>SQLTreeNodeManager</code> instance as argument.
 * Yet, actually, the <code>ConditionalProcessor</code> object only retrieves
 * the associated <code>WhereNodeManager</code> instance from the 
 * <code>SQLTreeNodeManager</code> instance. While traversing the parsed EJB-QL tree's
 * where condition, the <code>ConditionalProcessor</code> object sends commands
 * to the <code>WhereNodeManager</code> instance, which enable the 
 * <code>WhereNodeManager</code> instance to build an SQL representation of
 * the parsed EJB-QL tree's where condition.
 * </p><p>
 * The <code>ConditionalProcessor</code> object only deals with conditional
 * combinations; for simple conditional expressions it creates an associated
 * <code>SimpleConditionProcessor</code> object, to which it delegates their
 * evaluation.
 * </p><p>
 * Class <code>ConditionalProcessor</code> takes use of class <code>LogicOperator</code>
 * to perform its job.
 * </p><p>
 * Copyright (c) 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 * @see com.sap.ejb.ql.sqlmapper.common.EJBQLTreeProcessor
 * @see com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.WhereNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.SimpleConditionProcessor
 * @see com.sap.ejbql.tree.Query
 * @see com.sap.ejb.ql.sqlmapper.common.LogicOperator
 **/
public class ConditionProcessor
{
  private static final Location loc = Location.getLocation(ConditionProcessor.class);
  private static final String process = "process";
  private static final String processParms[] = { "cond" };

  private WhereNodeManager whereNodeManager;
  private SimpleConditionProcessor simpleConditionProcessor;

  /**
   * Creates a <code>ConditionProcessor</code> instance.
   * @param treeNodeManager
   *     the <code>SQLTreeNodeManager</code> instance the
   *     <code>WhereNodeManager</code> to work with is 
   *     retrieved from.
   **/
  ConditionProcessor(SQLTreeNodeManager treeNodeManager)
  {
    this.whereNodeManager = treeNodeManager.getWhereNodeManager();
    this.simpleConditionProcessor = new SimpleConditionProcessor(treeNodeManager);
  }

  /**
   * Traverses conditional expression and delegates simple conditional
   * expressions to associated <code>SimpleConditionProcessor</code>
   * object.
   * </p><p>
   * <B>Note</B> this method may recursively call itself.
   * @param cond
   *     conditional expression to be processea.
   * @throws SQLMappingException
   *     if an error occurs during processing.
   **/
  void process(ConditionalExpression cond) throws SQLMappingException
  {
    if ( DevTrace.isOnDebugLevel(loc) )
    {
      Object inputValues[] = { cond };
      DevTrace.entering(loc, process, processParms, inputValues);
    }

    if ( cond instanceof ConditionalCombination )
    {
      DevTrace.debugInfo(loc, process, "ConditionalCombination encountered");
      ConditionalCombination condComb = (ConditionalCombination) cond;
      this.process(condComb.getLeftOperand());
      int operator = this.getLogicOperator(condComb.getOperator());
      if ( operator == LogicOperator.NOT )
      {
        DevTrace.debugInfo(loc, process, "Monadic logic operator encountered.");
        this.whereNodeManager.alterNode(operator);
        DevTrace.exiting(loc, process);
        return;
      }
      DevTrace.debugInfo(loc, process, "Dyadic logic operator encountered.");
      this.process(condComb.getRightOperand());
      this.whereNodeManager.popNode(operator);
      DevTrace.exiting(loc, process);
      return;
    }

    DevTrace.debugInfo(loc, process, "Simple Condition encountered.");
    this.simpleConditionProcessor.process(cond);

    DevTrace.exiting(loc, process);
    return;
 
  }

  /**
   * Translates the EJB-QL parser notation of logic operators into the
   * one of the SQL tree.
   * @param operator
   *    operator in EJB-QL parser notation.
   * @return
   *    operator in SQL tree notation.
   * @throws SQLMappingException
   *    if operator may not be identified.
   **/
  private int getLogicOperator(ConditionalCombination.Operator operator)
    throws SQLMappingException
  {

    if ( operator == ConditionalCombination.MonadicOperator.NOT )
    {
      return LogicOperator.NOT;
    }

    if ( operator == ConditionalCombination.DyadicOperator.AND )
    {
      return LogicOperator.AND;
    }

    if ( operator == ConditionalCombination.DyadicOperator.OR )
    {
      return LogicOperator.OR;
    }

    throw new SQLMappingException("Unbeknownst logical Operator " + operator.toString()
                                 + ".",
                                 "In the EJB QL parsed tree, SQL mapper has encountered "
                                 + "unknown logical operator " + operator.toString()
                                 + ". This is most likely a programming error in the "
                                 +"EJB QL parser.",
                                 "CSM013");
  } 

}
