package com.sap.dictionary.database.art;

import com.sap.dictionary.database.dbs.*;

/**
 * Title:        Analysis of table and view changes: Oracle specific classes
 * Description:  Oracle specific analysis of table and view changes. Tool to deliver Oracle specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbArtExtractor extends DbExtractor {

  public DbArtExtractor() {
  }

    public DbTable getTable(String tablename) {
    DbTable dbTable = null;

    return dbTable;
  }

  public DbColumns getColumns(String tablename) {
    DbColumns dbColumns = null;

    return dbColumns;

  }

  public DbIndex getIndex(String indexname) {
    DbIndex dbIndex = null;

    return dbIndex;
  }

  public DbIndexes getIndexes(String tablename) {
    DbIndexes dbIndexes = null;

    return dbIndexes;
  }

  public DbPrimaryKey getPrimaryKey(String tablename) {
    DbPrimaryKey primaryKey = null;

    return primaryKey;
  }
}
