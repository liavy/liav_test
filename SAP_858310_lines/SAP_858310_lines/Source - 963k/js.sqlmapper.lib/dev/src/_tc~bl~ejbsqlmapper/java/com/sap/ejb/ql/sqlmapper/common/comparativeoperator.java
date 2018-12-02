package com.sap.ejb.ql.sqlmapper.common;

import com.sap.sql.tree.ComparisonOperator;

/**
 * Provides constants for comparative operators
 * for communication between <code>Processor</code>
 * and <code>Manager</code> classes.
 * For basic comparative operators (a.k.a comparison
 * operators) such as <code>=</code>, <code>&lt;&gt;</code>,
 * <code>&lt;=</code> and so forth constants from
 * package <code>com.sap.sql.tree</code> are referred.
 * </p><p>
 * Copyright (c) 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 * @see com.sap.sql.tree.ComparisonOperator
 **/
public final class ComparativeOperator
{
  /* = */
  static final int EQ = ComparisonOperator.EQ;
  /* <> */
  static final int NE = ComparisonOperator.NE;
  /* < */
  static final int LT = ComparisonOperator.LT;
  /* <= */
  static final int LE = ComparisonOperator.LE;
  /* > */
  static final int GT = ComparisonOperator.GT;
  /* >= */
  static final int GE = ComparisonOperator.GE;

  /* IS NULL */
  static final int IS_NULL     = 2 * ComparisonOperator.GE + 1;
  /* IS NOT NULL */
  static final int NOT_NULL    = IS_NULL + 1;
  /* IS EMPTY */
  static final int EMPTY       = NOT_NULL + 1;
  /* IS NOT EMPTY */
  static final int NOT_EMPTY   = EMPTY + 1;
  /* IS MEMBER OF */
  static final int MEMBER      = NOT_EMPTY + 1;
  /* IS NOT MEMBER OF */
  static final int NOT_MEMBER  = MEMBER + 1;
  /* IN */
  static final int IN          = NOT_MEMBER + 1;
  /* NOT IN */
  static final int NOT_IN      = IN + 1;
  /* LIKE */
  static final int LIKE        = NOT_IN + 1;
  /* NOT LIKE */
  static final int NOT_LIKE    = LIKE + 1;
  /* BETWEEN */
  static final int BETWEEN     = NOT_LIKE + 1;
  /* NOT BETWEEN */
  static final int NOT_BETWEEN = BETWEEN + 1;

  // to prevent accidental instantiation
  private ComparativeOperator()
  {
  }
} 
