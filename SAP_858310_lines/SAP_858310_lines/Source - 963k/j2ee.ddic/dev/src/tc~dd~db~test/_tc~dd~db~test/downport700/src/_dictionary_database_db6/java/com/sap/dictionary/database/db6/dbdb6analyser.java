package com.sap.dictionary.database.db6;

import com.sap.dictionary.database.dbs.*;

/**
 * Title:        Analysis of table and view changes: DB6 specific classes
 * Description:  DB6 specific analysis of table and view changes. Tool to deliver DB6 specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Frank-Martin Haas , Frank-Herwig Walter
 * @version 1.0
 */

public class DbDb6Analyser extends DbAnalyser
{

  public DbDb6Analyser()
  {
  }

  public boolean hasData( String tabname )
  {
    boolean hasData = false;

    return hasData;
  }

  public boolean existsTable( String tablename )
 {
    boolean existsTable = false;

    return existsTable;
  }

  public boolean existsIndex( String tablename, String indexname )
  {
    boolean existsIndex = false;

    return existsIndex;
  }

  public boolean existsView( String viewname )
  {
    boolean existsView = false;

    return existsView;
  }

  public boolean hasIndexes( String tablename )
  {
    boolean existsIndexes = false;

    return existsIndexes;
  }

  public boolean isPrimary( String indexname )
  {
    boolean isPrimary = false;

    return isPrimary;
  }
}