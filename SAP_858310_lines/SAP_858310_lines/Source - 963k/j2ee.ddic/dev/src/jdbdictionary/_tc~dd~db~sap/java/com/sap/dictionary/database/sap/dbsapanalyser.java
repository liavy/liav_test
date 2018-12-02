package com.sap.dictionary.database.sap;

import com.sap.dictionary.database.dbs.*;

/**
 * Title:        Analysis of table and view changes: SAPDB specific classes
 * Description:  SAPDB specific analysis of table and view changes. Tool to deliver SAPDB specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */
public class DbSapAnalyser extends DbAnalyser {

  public DbSapAnalyser() {
  }

  public boolean hasData(String tabname) {
    boolean hasData = false;

    return hasData;
  }

  public boolean existsTable(String tablename) {
    boolean existsTable = false;

    // select count(*) from domain.tables
    // where owner=USER and tabletype='TABLE' and tablename=?
    // oder
    // exists table <tablename>

    return existsTable;
  }

  public boolean existsIndex(String tablename, String indexname) {
    boolean existsIndex = false;

    // select indexname from domain.indexes
    // where owner=USER and tablename=? and indexname=?

    return existsIndex;
  }

  public boolean existsView(String viewname) {
    boolean existsView = false;

    // select viewname from domain.views
    // where owner=USER and viewname=?

    return existsView;
  }

  public boolean hasIndexes(String tablename) {
    boolean existsIndexes = false;

    // select count(*) from domain.indexes
    // where owner=USER and tablename=?

    return existsIndexes;
  }

  public boolean isPrimary(String indexname) {
    boolean isPrimary = false;

    // TODO : a primary key has no name ??

    return isPrimary;
  }
}