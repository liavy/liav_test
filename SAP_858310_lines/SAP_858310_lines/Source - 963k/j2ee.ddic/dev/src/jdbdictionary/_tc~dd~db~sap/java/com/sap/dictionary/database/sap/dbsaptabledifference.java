package com.sap.dictionary.database.sap;

import com.sap.dictionary.database.dbs.*;

/**
 * Title:        Analysis of table and view changes: Oracle specific classes
 * Description:  Oracle specific analysis of table and view changes. Tool to deliver Oracle specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbSapTableDifference extends DbTableDifference {

  public DbSapTableDifference(DbTable refTable,DbTable cmpTable) {
    super(refTable, cmpTable);

  //Returns Action what has to be done for this table:
  //Possible values: See class com.sap.jdd.dbs.Action
    Action action = null;

    //TODO: Compare Ora-specific parameters of a table
    setAction(action);
  }
}

