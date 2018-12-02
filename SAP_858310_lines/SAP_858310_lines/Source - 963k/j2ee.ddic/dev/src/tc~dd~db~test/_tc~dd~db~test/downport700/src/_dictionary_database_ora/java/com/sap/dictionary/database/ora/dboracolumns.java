package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumnIterator;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

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
public class DbOraColumns
extends DbColumns
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraColumns");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);

   /**
    * Constructor.
    */
   public DbOraColumns()
   {
      super();
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory.
    *    The factory used by this set of columns.
    */
   public DbOraColumns(DbFactory factory)
   {
      super(factory);
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory.
    *    The factory used by this set of columns.
    * @param DbColumns columns
    *    A set of columns to create this set from.
    */
   public DbOraColumns(DbFactory factory, DbColumns columns)
   {
      super(factory, columns);
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory.
    *    The factory used by this set of columns.
    * @param XmlMap xmlMap
    *    A XML map to create this set of columns
    *    from.
    */
   public DbOraColumns(DbFactory factory, XmlMap xmlMap)
   throws Exception
   {
      super(factory, xmlMap);
   }

   /**
    * Retrieves the description of each column of the
    * table or view from the database and adds the 
    * column to this set of columns.
    * <p>
    * @param DbFactory factory
    *    The factory used by this set of columns.
    */
   public void setContentViaDb(DbFactory factory)
   throws JddException
   {
      boolean success     = false;
      
      String  name        = " ";
      String  schemaName  = null;

      try
      {
         location.entering("setContentViaDb");

         // Retrieve table or view name, and schema name
         if(this.getTable() != null)
         {
            name = this.getTable().getName();
         }
         else
         if(this.getView()  != null)
         {
            name = this.getView().getName();
         }
			
         try
         {  
            schemaName      = factory.getSchemaName();
         }
         catch(SQLException sqlException)
         {
            category.info(
                           location,
                           NO_SCHEMA_NAME
                         );

            schemaName      = null;
			}        
         
         // Retrieve meta data for the table or view
         DatabaseMetaData databaseMetaData = NativeSQLAccess.getNativeMetaData(factory.getConnection());

         ResultSet        resultSet        = databaseMetaData.getColumns(null, schemaName, name, null);
         
         while(resultSet.next())
         {
            success = true;

            // Caution: do not change read sequence
            String  columnName    = resultSet.getString("COLUMN_NAME"     );
            short   sqlType       = resultSet.getShort ("DATA_TYPE"       );
            String  dbType        = resultSet.getString("TYPE_NAME"       );
            int     columnSize    = resultSet.getInt   ("COLUMN_SIZE"     );
            int     decimalDigits = resultSet.getInt   ("DECIMAL_DIGITS"  );
            String  defaultValue  = resultSet.getString("COLUMN_DEF"      );
            int     position      = resultSet.getInt   ("ORDINAL_POSITION");
            boolean isNotNull     = resultSet.getString("IS_NULLABLE"     ).trim().equalsIgnoreCase("NO");

            // Transform Oracle NULL to null pointer
            if((defaultValue != null) &&
                defaultValue.trim().equalsIgnoreCase("NULL"))
            { 
                defaultValue      = null;
            }
            
            // Create and add new column
            DbColumn column       = factory.makeDbColumn(
                                                          columnName,
                                                          position,
                                                          sqlType,
                                                          dbType,
                                                          columnSize,
                                                          decimalDigits,
                                                          isNotNull,
                                                          defaultValue
                                                        );
                                                         
            this.add(column);
         }
         
         resultSet.close();
         
         if(!success)
         {
            category.info(
                           location,
                           TABLE_ONDB_NOTFOUND,
                           new String[] {
                                          name
                                        }
                         );
         }         
      }
      catch(Exception exception)
      {
         throw new JddException(
                                 exception,
                                 COLUMN_READ_VIA_DB_ERR,
                                 new String[] {
                                                name
                                              },
                                 category,
                                 Severity.ERROR,
                                 location
                               );
      }
      finally
      {
         location.exiting();
      }
   }

   /**
    * Checks whether the number of columns in this set
    * is allowed.
    * <p>
    * @return boolean 
    *    <code>true</code>, if the number of columns is ok,
    *    <code>false</code> otherwise
    */
   public boolean checkNumber()
   {
      boolean correct = false;
      
      try
      {
         location.entering("checkNumber");

         int count = 0;

         for(DbColumnIterator iterator = this.iterator(); iterator.hasNext(); iterator.next())
            count ++;


         correct = !(count > 1000);

         if(!correct)
         {
            category.errorT(
                             location,
                             "checkNumber failed: table {0} has too many columns",
                             new String[] {
                                            this.getTable().getName()
                                          }
                           );
         }
      }
      finally
      {
         location.exiting();
      }
      
      return correct;
   }
}