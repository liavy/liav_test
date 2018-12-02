package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.DbTableDifference;

/**
 * Title:        Analysis of dictionary changes, Oracle-specific part
 * Description:  Oracle specific analysis of dictionary changes
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft. Markus Maurer
 * @version 1.0
 */

/**
 * 
 */
public class DbOraTableDifference
extends DbTableDifference
{
   /**
    * Constructor.
    * <p>
    * @param DbTable origin
    *    The current, original version of this table.
    * @param DbTable target
    *    The target version of the table.
    */
   public DbOraTableDifference(DbTable original, DbTable target)
   {
      super(original, target);
   }

   /**
    * Constructor.
    * <p>
    * @param DbTable origin
    *    The current, original version of this table.
    * @param DbTable target
    *    The target version of the table.
    * @param Action action 
    *    The action transforming the original version
    *    of this table into the target version.
    */
   public DbOraTableDifference(DbTable original, DbTable target, Action action)
   {
      super(original, target, action);
   }
}

