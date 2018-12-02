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

public class DbArtColumnsDifference extends DbColumnsDifference
{
  public DbArtColumnsDifference() {}

  public DbObjectSqlStatements getDdlStatementsForAlter(String tableName)
    throws Exception {return null;}
}
