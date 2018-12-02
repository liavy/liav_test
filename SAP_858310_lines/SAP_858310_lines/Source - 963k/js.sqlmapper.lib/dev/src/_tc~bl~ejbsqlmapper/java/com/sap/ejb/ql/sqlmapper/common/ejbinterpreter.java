package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager;

/**
 * Summons up all classes interpreting EJB persistence requests.
 * </p><p>
 * There are basically two kinds of persistence requests from the EJB
 * container to the SQL mapper&nbsp;: EJB-QL query specifications and
 * load/store commands for abstract beans and CMRs. The classes dealing
 * with these different kinds of requests are embraced by this
 * common super class.
 * </p><p>
 * Copyright (c) 2004, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.0
 */

public abstract class EJBInterpreter
{
  /* EJB load/store request. */
  static final int LOAD_STORE = 0;
  /* EJB-QL query specifications */
  static final int EJBQL = 1;
  /* Contianer test for bean existence */
  static final int EXISTS = 2;

  /**
   * Prepares the <code>EJBInterpreter</code> instance for
   * processing a new EJB persistence request.
   * </p><p>
   * @param treeNodeManager
   *     the <code>SQLTreeNodeManager</code> instance the
   *     <code>EJBInterpreter</code> object is to work with.
   */
  void prepare(SQLTreeNodeManager treeNodeManager)
  {}

  /**
   * Tells the <code>EJBInterpreter</code> instance that
   * processing of an  EJB persistence request has been finished.
   */
  void clear()
  {}

}

