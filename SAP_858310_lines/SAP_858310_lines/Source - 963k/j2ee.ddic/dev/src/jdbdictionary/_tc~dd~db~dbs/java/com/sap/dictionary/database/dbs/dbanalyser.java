package com.sap.dictionary.database.dbs;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public abstract class DbAnalyser {

  public DbAnalyser() {
  }

  public abstract boolean hasData(String tableName);

  public abstract boolean existsTable(String name);

  public abstract boolean existsIndex(String tableName, String indexName);

  public abstract boolean existsView(String name);

  public abstract boolean hasIndexes(String tableName);
}
