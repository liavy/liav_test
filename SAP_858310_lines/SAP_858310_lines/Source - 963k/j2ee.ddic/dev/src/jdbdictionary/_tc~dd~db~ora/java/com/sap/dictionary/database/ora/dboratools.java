package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbTools;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Title:        Analysis of dictionary changes, Oracle-specific part
 * Description:  Oracle specific analysis of dictionary changes
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Markus Maurer
 * @version 1.0
 */

/**
 *
 */
public class DbOraTools
extends DbTools
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraTools");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);
   
   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used by these tools.
    */
   public DbOraTools(DbFactory factory)
   {
      super(factory);
   }
   
   /**
   * Renames a table on the database. 
   * <p>
   * @param String source
   *    The current name of the table.
   * @param String target
   *    The target name of the table.
   * @throws JddException
   *    If an error occurs.
   **/
   public void renameTable(String sourceName, String targetName)
   throws JddException
   {
      try
      {
         location.entering("renameTable");

         sourceName = sourceName.toUpperCase();
         targetName = targetName.toUpperCase();
         
         this.executeOnDb(
                           "ALTER TABLE \"" + sourceName + "\" " +
                           "RENAME TO \""   + targetName + "\" "
                         );
      }
      catch(Exception exception)
      {
         location.errorT(
                          "renameTable failed: Could not rename table {0} to {1}: {2}",
                          new String[] {
                                         sourceName,
                                         targetName,
                                         exception.getMessage()
                                       }
                        );
         
         if(exception instanceof SQLException)
         {
            switch(((SQLException) exception).getErrorCode())
            {
               case 15225: throw new JddException(ExType.NOT_ON_DB,    exception);
               case 15335: throw new JddException(ExType.EXISTS_ON_DB, exception);
               default:    throw new JddException(ExType.SQL_ERROR,    exception);
            }
         }
         else
            throw JddException.createInstance(exception);
      }
      finally
      {
         location.exiting();
      }	
   }
   
   /** 
    * Checks what kind of table-like database object corresponds
    * to the specified name, and whether it describes an alias
    * or a view. In both cases the result is returned as a value
    * of DbTools.KindOfTableLikeDbObject. In all other cases,
    * including the object is a table does not exist at all,
    * the return value is null.
    * <p>
    * @param String objectName
    *    The name of the object to check.
    * @return DbTools.KindOfTableLikeDbObject
    *    <code>DbTools.KindOfTableLikeDbObject.VIEW</code>,
    *       if object is a view on database,
    *    <code>DbTools.KindOfTableLikeDbObject.ALIAS</code>,
    *       if object is an Alias on database,
    *    null otherwise.
    * @throws JddException
    *    If an error occurs.        
   **/
   public int getKindOfTableLikeDbObject(String objectName) 
   throws JddException
   {
      try
      {
         location.entering("getKindOfTableLikeDbObject");
         
         boolean  isTable    = false;
         boolean  isView     = false;
         boolean  isSynonym  = false;
         
                  objectName = objectName.toUpperCase();
         
         String[] result;
         
         if(!isTable && !isView && !isSynonym)
         {
            result    = this.executeQueryOnDb(
                                                "SELECT \"TABLE_NAME\" "
                                              + "FROM   \"USER_TABLES\" "
                                              + "WHERE  \"TABLE_NAME\" = '"   + objectName + "' "
                                              );
    
            isTable   = (result.length > 0);
         }
         
         if(!isTable && !isView && !isSynonym)
         {
            result    = this.executeQueryOnDb(
                                                "SELECT \"VIEW_NAME\" "
                                              + "FROM   \"USER_VIEWS\" "
                                              + "WHERE  \"VIEW_NAME\" = '"    + objectName + "' "
                                              );
 
            isView    = (result.length > 0);
         }
         
         if(!isTable && !isView && !isSynonym)
         {        
            result    = this.executeQueryOnDb(
                                               "SELECT \"SYNONYM_NAME\" "
                                             + "FROM   \"USER_SYNONYMS\" "
                                             + "WHERE  \"SYNONYM_NAME\" = '" + objectName + "' "
                                             );
    
            isSynonym = (result.length > 0);
         }
      
              if(isTable)   return DbTools.TABLE;
         else if(isView)    return DbTools.VIEW;
         else if(isSynonym) return DbTools.ALIAS;
         
         return DbTools.TABLE;      
      }
      catch(Exception exception)
      {
         location.errorT(
                          "getKindOfTableLikeDbObject failed: Determination of table kind failed for object {0}: {1}",
                          new String[] {
                                         objectName,
                                         exception.getMessage()
                                       }
                        );
         
         throw JddException.createInstance(exception);
      }
      finally
      {
         location.exiting();
      }
   }
   
   /**
    * Executes a SQL statement on the database.
    * <p>
    * @param String sql
    *    The SQL statement
    * @throws Exception
    *    If an error occurs.
    */
   private void executeOnDb(String sql)
   throws Exception
   {
      Connection connection = this.getFactory().getConnection();
      Statement  statement  = NativeSQLAccess.createNativeStatement(connection);

      statement.execute(sql);
      statement.close();
   }

   /**
    * Executes a single-column query on a character column
    * on the database.
    * <p>
    * @param String sql
    *    The SQL query, addressing a single character column.
    * @return String[]
    *    The resulting array of strings, one per row.
    * @throws Exception
    *    If an error occurs.
    */
   private String[] executeQueryOnDb(String sql)
   throws Exception
   {
      ArrayList  results    = new ArrayList();
      
      Connection connection = this.getFactory().getConnection();
      Statement  statement  = NativeSQLAccess.createNativeStatement(connection);
      ResultSet  resultSet  = statement.executeQuery(sql);

      while(resultSet.next())
         results.add(resultSet.getString(1));
      
      resultSet.close();
      statement.close();
      
      return (String[]) results.toArray(new String[0]);
   }

   /**
    * Executes a multi-column query on a character column
    * on the database.
    * <p>
    * @param String sql
    *    The SQL query, addressing multiple character columns.
    * @param String[] format
    *    A representative of a single row.
    * @return String[][]
    *    The resulting array of strings, one per row and column.
    * @throws Exception
    *    If an error occurs.
    */
   private String[][] executeQueryOnDb(String sql, String[] format)
   throws Exception
   {
      ArrayList  results    = new ArrayList();
      
      Connection connection = this.getFactory().getConnection();
      Statement  statement  = NativeSQLAccess.createNativeStatement(connection);
      ResultSet  resultSet  = statement.executeQuery(sql);

      while(resultSet.next())
      {
         String[] row = new String[format.length];
         
         for(int index = 0; index < row.length; index ++)
            row[index] = resultSet.getString(index + 1);

         results.add(row);
      }
      
      resultSet.close();
      statement.close();
      
      return (String[][]) results.toArray(new String[0][0]);
   }
}