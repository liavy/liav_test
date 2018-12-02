package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.DbAnalyser;
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
public class DbOraAnalyser
extends DbAnalyser
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraAnalyser");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);

   /**
    * Constructor.
    */
   public DbOraAnalyser()
   {
      super();
   }
   
   /**
    * Returns whether the specified table contains data.
    * <i>This function is not implemented yet.</i>
    * <p>
    * @param String tableName
    *    The name of the table to check.
    * @return boolean
    *    <code>true</code>, if the table contains data,
    *    <code>false</code> otherwise.
    */
   public boolean hasData(String tableName)
   {
      boolean hasData = false;
      
      try
      {
         location.entering("hasData");
            
         category.errorT(
                          location,
                          "hasData: not implemented"
                        );
      }
      finally
      {
         location.exiting();
      }
      
      return hasData;
   }

   /**
    * Returns whether the specified table possesses indices.
    * <i>This function is not implemented yet.</i>
    * <p>
    * @param String tableName
    *    The name of the table to check.
    * @return boolean
    *    <code>true</code>, if the table possesses indices,
    *    <code>false</code> otherwise.
    */
   public boolean hasIndexes(String tableName)
   {
      boolean hasIndexes = false;

      try
      {
         location.entering("hasIndexes");
      
         category.errorT(
                          location,
                          "hasIndexes: not implemented"
                        );
      }
      finally
      {
         location.exiting();
      }
      
      return hasIndexes;
   }

   /**
    * Returns whether the specified table exists.
    * <i>This function is not implemented yet.</i>
    * <p>
    * @param String tableName
    *    The name of the table to check.
    * @return boolean
    *    <code>true</code>, if the table exists,
    *    <code>false</code> otherwise.
    */
   public boolean existsTable(String tableName)
   {
      boolean exists = false;

      try
      {
         location.entering("existsTable");
      
         category.errorT(
                          location,
                          "existsTable: not implemented"
                        );
      }
      finally
      {
         location.exiting();
      }
      
      return exists;
   }

   /**
    * Returns whether the index of a specified table
    * exists. <i>This function is not implemented yet.</i>
    * <p>
    * @param String tableName
    *    The name of the table to check.
    * @param String indexName
    *    The name of the index to check.
    * @return boolean
    *    <code>true</code>, if the index exists,
    *    <code>false</code> otherwise.
    */
   public boolean existsIndex(String tableName, String indexName)
   {
      boolean exists = false;

      try
      {
         location.entering("existsIndex");
      
         category.errorT(
                          location,
                          "existsIndex: not implemented"
                        );
      }
      finally
      {
         location.exiting();
      }
      
      return exists;
   }

   /**
    * Returns whether the specified view exists.
    * <i>This function is not implemented yet.</i>
    * <p>
    * @param String viewName
    *    The name of the view to check.
    * @return boolean
    *    <code>true</code>, if the view exists,
    *    <code>false</code> otherwise.
    */
   public boolean existsView(String viewName)
   {
      boolean exists = false;
      
      try
      {
         location.entering("existsView");
      
         category.errorT(
                          location,
                          "existsView: not implemented"
                        );
      }
      finally
      {
         location.exiting();
      }
      
      return exists;
   }
}
