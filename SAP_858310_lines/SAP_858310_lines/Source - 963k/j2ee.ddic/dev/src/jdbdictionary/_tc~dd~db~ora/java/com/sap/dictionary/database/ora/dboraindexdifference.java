package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbIndex;
import com.sap.dictionary.database.dbs.DbIndexDifference;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Title:        Analysis of dictionary changes, Oracle-specific part
 * Description:  Oracle specific analysis of dictionary changes
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Andrea Neufeld & Tobias Wenner. Markus Maurer
 * @version 1.0
 */

/**
 *
 */
public class DbOraIndexDifference
extends DbIndexDifference
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraIndexDifference");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);

   /**
    * Constructor.
    * <p>
    * @param DbIndex origin
    *    The current, original version of the
    *    index.
    * @param DbIndex target
    *    The target version of the index.
    * @param Action action
    *    The action required to transform the
    *    original index version into the target
    *    version.
    */
   public DbOraIndexDifference(DbIndex origin, DbIndex target, Action action)
   {
      super(origin, target, action);
   }

   /**
    * Returns the SQL statements required to perform
    * the origin to target transformation on the database.
    * <p>
    * @param String tableName
    *    The name of the table the index belongs to.
    * @throws JddException
    *    If an error occurs.
    */
   public DbObjectSqlStatements getDdlStatements(String tableName)
   throws JddException
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements("");

      try
      {
         location.entering("getDdlStatements");
                                                          
         DbObjectSqlStatements drop    = new DbObjectSqlStatements("");
         DbObjectSqlStatements create  = new DbObjectSqlStatements("");

         if((this.getAction() == Action.DROP       ) ||
            (this.getAction() == Action.DROP_CREATE))
            drop   = this.getOrigin().getDdlStatementsForDrop();
         
         if((this.getAction() == Action.CREATE     ) ||
            (this.getAction() == Action.DROP_CREATE))
            create = this.getTarget().getDdlStatementsForCreate(); 
         
         statements.merge(drop);
         statements.merge(create);
      }
      catch(Exception exception)
      {
         location.errorT(
                         "getDdlStatements failed: exception occured: {0}",
                          new String[] {
                                         exception.getMessage()
                                       }
                        );
         
         throw JddException.createInstance(exception);
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }

   /**
    * Returns the SQL statements required to perform
    * the origin to target transformation on the database.
    * <p>
    * @param String tableName
    *    The name of the table the index belongs to.
    * @param DbTable tableForStorageInfo
    *    A table containing storage info.
    * @throws JddException
    *    If an error occurs.
    */
   public DbObjectSqlStatements getDdlStatements(String tableName, DbTable tableForStorageInfo)
   throws JddException
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements("");

      try
      {
         location.entering("getDdlStatements");
                                                                
         DbObjectSqlStatements drop    = new DbObjectSqlStatements("");
         DbObjectSqlStatements create  = new DbObjectSqlStatements("");

         if((this.getAction() == Action.DROP       ) ||
            (this.getAction() == Action.DROP_CREATE))
            drop   = this.getOrigin().getDdlStatementsForDrop();
       
         if((this.getAction() == Action.CREATE     ) ||
            (this.getAction() == Action.DROP_CREATE))
            create = this.getTarget().getDdlStatementsForCreate(); 
       
         statements.merge(drop);
         statements.merge(create);
      }
      catch(Exception exception)
      {
         location.errorT(
                          "getDdlStatements failed: exception occured: {0}",
                          new String[] {
                                         exception.getMessage()
                                       }
                        );
         
         throw JddException.createInstance(exception);
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }
}
