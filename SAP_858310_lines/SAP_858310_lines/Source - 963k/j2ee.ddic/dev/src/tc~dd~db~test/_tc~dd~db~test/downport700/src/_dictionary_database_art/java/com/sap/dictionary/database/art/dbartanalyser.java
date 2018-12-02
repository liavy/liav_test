package com.sap.dictionary.database.art;

import com.sap.dictionary.database.dbs.*;

/**
 * Title:        Analysis of table and view changes: Oracle specific classes
 * Description:  Oracle specific analysis of table and view changes. Tool to deliver Oracle specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author A. Neufeld & T. Wenner
 * @version 1.0
 */
public class DbArtAnalyser extends DbAnalyser
{
// not yet implemented!!
  public DbArtAnalyser() {}

  public boolean hasData (String tableName)
  {
    boolean hasData = false;

    return hasData;
  }

  public boolean existsTable (String tableName)
  {
    boolean existsTable = false;

    return existsTable;
  }

  public boolean existsIndex (String tableName, String indexName)
  {
    boolean existsIndex = false;

    return existsIndex;
  }

  public boolean existsView (String viewName)
  {
    boolean existsView = false;

    return existsView;
  }

  public boolean hasIndexes (String tableName)
  {
    boolean existsIndexes = false;

    return existsIndexes;
  }

  public boolean isPrimary(String indexname)
  {
    boolean isPrimary = false;

    return isPrimary;
  }
}
