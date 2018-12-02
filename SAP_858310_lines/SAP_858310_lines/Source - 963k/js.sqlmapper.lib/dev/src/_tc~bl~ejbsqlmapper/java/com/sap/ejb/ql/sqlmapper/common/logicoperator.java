package com.sap.ejb.ql.sqlmapper.common;

/**
 * Provides constants for logic operators
 * for communication between <code>Processor</code>
 * and <code>Manager</code> classes.
 * </p><p>
 * Copyright (c) 2002-2003, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 **/
public final class LogicOperator
{
  /* logical NOT */
  static final int NOT = 1;
  /* logic AND */
  static final int AND = 2;
  /* logic OR */
  static final int OR  = 3;

  // to prevent accidental instantiation
  private LogicOperator()
  {
  }
}
