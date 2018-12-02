package com.sap.dictionary.database.dbs;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public abstract class DbExtractor {

  public DbExtractor() {
  }

  public DbTable getTable(String name) {
    DbTable dbTable = null;

    return dbTable;
  }

  public DbColumns getColumns(String name) {
    DbColumns DbColumns = null;

    return DbColumns;
   }

  public DbIndex getIndex(String tableName, String indexName) {
    DbIndex dbIndex = null;

    return dbIndex;
  }

  public DbIndexes getIndexes(String tableName) {
    DbIndexes dbIndexes = null;

    return dbIndexes;
  }

  public DbPrimaryKey getPrimaryKey(String tableName) {
  	DbPrimaryKey key = null;
  	
  	return key;
  }	
}
