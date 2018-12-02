package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.DbEnvironment;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import java.sql.Connection;

/**
 * Title:        Analysis of dictionary changes, Oracle-specific part
 * Description:  Oracle specific analysis of dictionary changes
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis. Markus Maurer
 * @version 1.0
 */

/**
 *
 */
public class DbOraEnvironment
extends DbEnvironment
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraEnvironment");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);
   
   /**
    * The database connection.
    */
   private final Connection connection;

   /**
    * An array of all reserved words of this database
    * platform.
    */
   private static String[] reservedWord = {
                                            "ACCESS", "ADD", "ALL", "ALTER", "AND", "ANY", "AS", "ASC", "AUDIT",
                                            "BETWEEN", "BY",
                                            "CHAR", "CHECK", "CLUSTER", "COLUMN", "COMMENT", "COMPRESS", "CONNECT", "CREATE", "CURRENT",
                                            "DATE", "DECIMAL", "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP",
                                            "ELSE", "EXCLUSIVE", "EXISTS",
                                            "FILE", "FLOAT", "FOR", "FROM",
                                            "GRANT", "GROUP",
                                            "HAVING",
                                            "IDENTIFIED", "IMMEDIATE", "IN", "INCREMENT", "INDEX", "INITIAL", "INSERT", "INTEGER", "INTERSECT", "INTO", "IS",
                                            "LEVEL", "LIKE", "LOCK", "LONG",
                                            "MAXEXTENTS", "MINUS", "MLSLABEL", "MODE", "MODIFY",
                                            "NOAUDIT", "NOCOMPRESS", "NOT", "NOWAIT", "NULL", "NUMBER",
                                            "OF", "OFFLINE", "ON", "ONLINE", "OPTION", "OR", "ORDER",
                                            "PCTFREE", "PRIOR", "PRIVILEGES", "PUBLIC",
                                            "RAW", "RENAME", "RESOURCE", "REVOKE", "ROW", "ROWID", "ROWNUM", "ROWS",
                                            "SELECT", "SESSION", "SET", "SHARE", "SIZE", "SMALLINT", "START", "SUCCESSFUL", "SYNONYM", "SYSDATE",
                                            "TABLE", "THEN", "TO", "TRIGGER",
                                            "UID", "UNION", "UNIQUE", "UPDATE", "USER",
                                            "VALIDATE", "VALUES", "VARCHAR", "VARCHAR2", "VIEW",
                                            "WHENEVER", "WHERE", "WITH"
                                          };

   /**
    * Constructor.
    */
   public DbOraEnvironment()
   {
      super();

      this.connection = null;
   }

   /**
    * Constructor.
    * <p>
    * @param Connection connection
    *    The underlying database connection.
    */
   public DbOraEnvironment(Connection connection)
   {
      super(connection);

      this.connection = connection;
   }

   /**
    * Returns whether a specified word is an Oracle reserved word.
    * <p>
    * @param String word
    *    The word to check.
    * @return boolean
    *    <code>true</code>, if the word is an Oracle reserved word,
    *    <code>false</code> otherwise.
    */
   public static boolean isReservedWord(String word)
   {
      boolean reserved = false;

      try
      {
         location.entering("isReservedWord");
         
         for(int i = 0; i < reservedWord.length; i ++)
            reserved = reserved || reservedWord[i].equalsIgnoreCase(word);
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "isReservedWord failed: exception occured while checking {0}: {1}",
                          new String[] {
                                         word,
                                         exception.getMessage()
                                       }
                        );
      }
      finally
      {
         location.exiting();
      }
      
      return reserved;
   }

   /**
    * Returns the name of the schema underlying the current
    * database connection in case a schema name could not be 
    * determined in the super class.
    * <p>
    * @return String
    *    The name of the schema underlying the current database
    *    connection if the super schema name is null, otherwise
    *    the super schema name. Returns <code>null</code> if the
    *    connection is <code>null</code>.
    * @throws SQLException
    *    If a SQL error occurs.
    */
   public String getSchemaName()
   {
      String schemaName = null;

      try
      {
         location.entering("getSchemaName");
         
         schemaName = super.getSchemaName();
         
         if(schemaName == null)
         {
            if(connection != null)
            {
               schemaName = connection.getMetaData().getUserName();
            }
         }
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "getSchemaName failed: exception occured: {0}",
                          new String[] {
                                         exception.getMessage()
                                       }
                        );
      }
      finally
      {
         location.exiting();
      }

      return schemaName;
   }
}