package com.sap.ejb.ql.sqlmapper.common;

import com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager;
import com.sap.ejb.ql.sqlmapper.common.EJBInterpreter;

/**
 * Gathers a set of objects required to process EJB persistence requests.
 * Such a set currently consists of a database vendor ID, an
 * <code>SQLTreeNodeManager</code> and an <code>EJBInterpreter</code>.
 * </p><p>
 * Copyright (c) 2004-2006, SAP-AG
 * @author Rainer Schweigkoffer, Dirk Debertin
 * @version 1.1
 * @see com.sap.ejb.ql.sqlmapper.common.SQLTreeNodeManager
 * @see com.sap.ejb.ql.sqlmapper.common.EJBInterpreter
 */
public class MappingAgents
{
  private int databaseVendorID;
  private SQLTreeNodeManager treeNodeManager;
  private EJBInterpreter ejbInterpreter;

  /**
   * Creates a <code>MappingAgents</code> instance.
   * </p><p>
   * @param databaseVendorID
   *      preset database vendor id.
   * @param treeNodeManager
   *      the <code>SQLTreeNodeManager</code> object.
   * @param ejbInterpreter
   *      the <code>EJBInterpreter</code> object.
   */
  MappingAgents(int databaseVendorID, SQLTreeNodeManager treeNodeManager, EJBInterpreter ejbInterpreter)
  {
    this.databaseVendorID = databaseVendorID;
    this.treeNodeManager = treeNodeManager;
    this.ejbInterpreter = ejbInterpreter;
  }

  /**
   * Retrieves the preset database vendor ID.
   * </p><p>
   * @return
   *      preset database vendor id.
   */
  int getDatabaseVendor()
  {
    return this.databaseVendorID;
  }

  /**
   * Retrieves the <code>SQLTreeNodeManager</code> object.
   * </p><p>
   * @return
   *      the <code>SQLTreeNodeManager</code> object.
   */
  SQLTreeNodeManager getSQLTreeNodeManager()
  {
    return this.treeNodeManager;
  }

  /**
   * Retrieves the <code>EJBInterpreter</code> object.
   * @return
   *      the <code>EJBInterpreter</code> object.
   */
  EJBInterpreter getEJBInterpreter()
  {
    return this.ejbInterpreter;
  }
}
