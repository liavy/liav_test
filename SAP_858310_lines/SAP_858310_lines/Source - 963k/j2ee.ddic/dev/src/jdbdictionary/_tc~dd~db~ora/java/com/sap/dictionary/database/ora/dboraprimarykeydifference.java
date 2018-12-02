package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbPrimaryKey;
import com.sap.dictionary.database.dbs.DbPrimaryKeyDifference;
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
 * @author Michael Tsesis & Kerstin Hoeft. Markus Maurer
 * @version 1.0
 */

/**
 *
 */
public class DbOraPrimaryKeyDifference
extends DbPrimaryKeyDifference
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraPrimaryKeyDifference");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);

   /**
    * Constructor.
    * <p>
    * @param DbPrimaryKey origin
    *    The current, original version of this
    *    primary key.
    * @param DbPrimaryKey target
    *    The target version of the primary key.
    * @param Action action
    *    The action required to transform the
    *    original primary key version into the
    *    target version.
    */
   public DbOraPrimaryKeyDifference(DbPrimaryKey origin, DbPrimaryKey target, Action action)
   {
      super(origin, target, action);
   }

   /**
    * Returns the SQL statements required to perform
    * the origin to target transformation on the database.
    * <p>
    * @param String tableName
    *    the name of the table the primary key belongs to
    * @return DbObjectSqlStatements
    *    the statements performing the transformation
    * @throws JddException
    *    if an error occurs during statement creation
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
                          "getDdlStatements failed: exception occured for table {0}: {1}",
                          new String[] {
                                         tableName,
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
    *    the name of the table the primary key belongs to
    * @param DbTable tableForStorageInfo
    *    a table containing storage info
    * @return DbObjectSqlStatements
    *    the statements performing the transformation
    * @throws JddException
    *    if an error occurs during statement creation
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
                          "getDdlStatements failed: exception occured for table {0}: {1}",
                          new String[] {
                                         tableName,
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
