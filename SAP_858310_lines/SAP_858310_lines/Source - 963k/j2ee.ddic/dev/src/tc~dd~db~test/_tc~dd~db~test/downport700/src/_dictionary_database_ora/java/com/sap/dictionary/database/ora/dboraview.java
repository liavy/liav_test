package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbView;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import java.sql.Connection;
import java.sql.ResultSet;
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
public class DbOraView
extends DbView
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraView");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);
   
   /**
    * Constructor.
    */
   public DbOraView()
   {
      super();
   }
   
   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used by this view.
    */
   public DbOraView(DbFactory factory)
   {
      super(factory);
   }
   
   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used by this view.
    * @param DbView view
    *    A view to create this view from.
    */
   public DbOraView(DbFactory factory, DbView view)
   {
      super(factory, view);
   }
   
   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used by this view.
    * @param String viewName
    *    The name of the view.
    */
   public DbOraView(DbFactory factory, String viewName)
   {
      super(factory, viewName);
   }
   
   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used by this view.
    * @param DbSchema schema
    *    The schema this view shall come to lie in.
    * @param String viewName
    *    The name of the view.
    */
   public DbOraView(DbFactory factory, DbSchema schema, String viewName)
   {
      super(factory, schema, viewName);
   }
   
   /**
    * Gets the base table names of this view from database
    * and sets the corresponding variable with method
    * setBaseTableNames.
    * <p>
    * @throws JddException
    *    If an error occurs.
    */
   public void setBaseTableNamesViaDb()
   throws JddException
   {
      try
      {
         location.entering("setBaseTableNamesViaDb");

         String[] result    = this.executeQueryOnDb(
                                                     "SELECT \"USER_USERS\".\"USERNAME\" "
                                                   + "FROM   \"USER_USERS\" "
                                                   );

         String   viewName  = this.getName().toUpperCase();
         String   viewOwner = result[0];
         
                  result    = this.executeQueryOnDb(
                                                     "SELECT \"USER_DEPENDENCIES\".\"REFERENCED_NAME\" "
                                                   + "FROM   \"USER_DEPENDENCIES\" "
                                                   + "WHERE  \"USER_DEPENDENCIES\".\"NAME\" = '" + viewName + "' "
                                                   + "AND    \"USER_DEPENDENCIES\".\"TYPE\" = 'VIEW' "
                                                   + "AND    \"USER_DEPENDENCIES\".\"REFERENCED_OWNER\" = '" + viewOwner + "' "
                                                   );

         ArrayList names = new ArrayList();

         for(int index = 0; index < result.length; index ++)
         {
            names.add(result[index]);
         }
         
         this.setBaseTableNames(names);
      }
      catch(Exception exception)
      {
         location.errorT(
                          "setBaseTableNamesViaDb failed: exception occured for view {0}: {1}",
                          new String[] {
                                         this.getName(),
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
    * Gets the create statement of this view from the
    * database and sets the corresponding variable with
    * method setCreateStatement.
    * <p> 
    * @throws JddException
    *    If an error occured. 
    */  
   public void setCreateStatementViaDb()
   throws JddException
   {
      try
      {
         location.entering("setCreateStatementViaDb");

         String   viewName = this.getName().toUpperCase();
         
         String[] result   = this.executeQueryOnDb(
                                                    "SELECT \"USER_VIEWS\".\"TEXT\" "
                                                  + "FROM   \"USER_VIEWS\" "
                                                  + "WHERE  \"USER_VIEWS\".\"VIEW_NAME\"  = '" + viewName  + "' "
                                                  );
         
         String statement  = "";
         
         if(result.length > 0)
         {
            statement = "CREATE VIEW \"" + viewName + "\" AS " + result[0];
         }

         this.setCreateStatement(statement);
      }
      catch(Exception exception)
      {
         location.errorT(
                          "setCreateStatementViaDb failed: exception occured for view {0}: {1}",
                          new String[] {
                                         this.getName(),
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
    * Checks whether this view exists on the database.
    * <p>
    * @return boolean
    *    <code>true</code>, if the view exists,
    *    <code>false</code> otherwise.
    * @throws JddException
    *    If an error occured.	 
    */
   public boolean existsOnDb()
   throws JddException
   {
      boolean exists = false;

      try
      {
         location.entering("existsOnDb");
         
         String   viewName = this.getName().toUpperCase();
         
         String[] result   = this.executeQueryOnDb(
                                                    "SELECT 1 "
                                                  + "FROM   \"DUAL\" "
                                                  + "WHERE  EXISTS(SELECT * "
                                                  + "              FROM   \"USER_VIEWS\" "
                                                  + "              WHERE  \"USER_VIEWS\".\"VIEW_NAME\" = '" + viewName + "') "
                                                  );
         
         exists = (result.length > 0);
      }
      catch(Exception exception)
      {
         location.errorT(
                         "existsOnDb failed: exception occured for view {0}: {1}",
                          new String[] {
                                         this.getName(),
                                         exception.getMessage()
                                       }
                        );
         
         throw JddException.createInstance(exception);
      }
      finally
      {
         location.exiting();
      }

      return exists;
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
      
      Connection connection = this.getDbFactory().getConnection();
      Statement  statement  = NativeSQLAccess.createNativeStatement(connection);
      ResultSet  resultSet  = statement.executeQuery(sql);

      while(resultSet.next())
         results.add(resultSet.getString(1));
      
      resultSet.close();
      statement.close();
      
      return (String[]) results.toArray(new String[0]);
   }
}