package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbSqlStatement;
import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.DbTableDifference;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.dictionary.database.ora.DbOraEnvironment;
import com.sap.dictionary.database.ora.DbOraIndex;
import com.sap.dictionary.database.ora.DbOraIndexes;
import com.sap.dictionary.database.ora.DbOraPrimaryKey;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

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
public class DbOraTable
extends DbTable
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraTable");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);

   /**
    * Constructor.
    */
   public DbOraTable()
   {
      super();
   }
   
   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used by this table.
    */
   public DbOraTable(DbFactory factory)
   {
      super(factory);
   }
   
   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used by this table.
    * @param DbTable table
    *    A table to create this table from.
    */
   public DbOraTable(DbFactory factory, DbTable table)
   {
      super(factory, table);
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used by this table.
    * @param String tableName
    *    The name of this table.
    */
   public DbOraTable(DbFactory factory, String tableName)
   {
      super(factory, tableName);
   }
   
   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used by this table.
    * @param DbSchema schema
    *    The schema this table shall come to lie in.
    * @param String tableName
    *    The name of this table.
    */
   public DbOraTable(DbFactory factory, DbSchema schema, String tableName)
   {
      super(factory, schema, tableName);
   }
   
   /**
    * Replaces the db-specific parameters of this table
    * with those of another table.
    * <p>
    * @param DbTable table
    *    The table to take the replacement values from.
    **/   
   public void replaceTableSpecificContent(DbTable table)
   {
      try
      {
         location.entering("replaceTableSpecificContent");
      }
      finally
      {
         location.exiting();
      }
   }

   /**
    * Compares the database-specific content of this
    * table to a target table.
    * <p>
    * @param DbTable table
    *    The table to compare this table to.
    * @return boolean
    *    <code>true</code>, if the database-specific content
    *                       of the tables is equal,
    *    <code>false</code> otherwise.
    */
   public boolean equalsTableSpecificContent(DbTable table)
   {
      boolean equal = false;
      
      try
      {
         location.entering("equalsTableSpecificContent");

         equal = true;
      }
      finally
      {
         location.exiting();
      }

      return equal;
   }     

   /**
    * Sets the specific content of this table via 
    * an XML map.
    * <p>
    * @param XmlMap xmlMap
    *    The XML map defining this table.
    * @throws JddException
    *    If an error occurs.
    */
   public void setTableSpecificContentViaXml(XmlMap xmlMap)
   throws JddException
   {
      try
      {
         location.entering("setTableSpecificContentViaXml");
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "setTableSpecificContentViaXml failed: exception occured for table {0}: {1}",
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
    * Sets the specific content of this table via 
    * the database.
    * <p>
    * @throws JddException
    *    If an error occurs.
    */
   public void setTableSpecificContentViaDb()
   throws JddException
   {
      try
      {
         location.entering("setTableSpecificContentViaDb");
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "setTableSpecificContentViaDb failed: exception occured for table {0}: {1}",
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
    * Retrieves the indices of this table from the
    * database.
    * <p>
    * @throws JddException
    *    If an error occurs.
    */
   public void setIndexesViaDb()
   throws JddException
   {
      try
      {
         location.entering("setIndexesViaDb");
         
         String[] result     = this.executeQueryOnDb(
                                                      "SELECT \"USER_USERS\".\"USERNAME\" "
                                                    + "FROM   \"USER_USERS\" "
                                                    );

         String   tableName  = this.getName().toUpperCase();
         String   tableOwner = result[0];
         
                  result     = this.executeQueryOnDb(
                                                      "SELECT \"USER_INDEXES\".\"INDEX_NAME\" "
                                                    + "FROM   \"USER_INDEXES\" "
                                                    + "WHERE  \"USER_INDEXES\".\"TABLE_NAME\"  = '" + tableName  + "' "
                                                    + "AND    \"USER_INDEXES\".\"TABLE_OWNER\" = '" + tableOwner + "' "
                                                    + "AND    \"USER_INDEXES\".\"GENERATED\"   = 'N' "
                                                    + "AND    \"USER_INDEXES\".\"INDEX_TYPE\" IN ('NORMAL', 'BITMAP') "
                                                    );
         
         if(result.length > 0)
         {
            DbOraIndexes indexes = new DbOraIndexes(this.getDbFactory());
   
            for(int i = 0; i < result.length; i ++)
            {
               DbOraIndex index = new DbOraIndex(
                                                  this.getDbFactory(),
                                                  this.getSchema(),
                                                  this.getName(),
                                                  result[i]
                                                );
                
               index.setCommonContentViaDb();
               index.setSpecificContentViaDb();
               
               index.setIndexes(indexes);
  
               indexes.add(index);
            }
      
            this.setIndexes(indexes);
         }
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "setIndexesViaDb failed: exception occured for table {0}: {1}",
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
    * Retrieves the primary key of this table from
    * the database.
    * <p>
    * @throws JddException
    *    If an error occurs.
    */
   public void setPrimaryKeyViaDb()
   throws JddException
   {
      try
      {
         location.entering("setPrimaryKeyViaDb");
         
         DbOraPrimaryKey primaryKey = new DbOraPrimaryKey(
                                                           this.getDbFactory(),
                                                           this.getSchema(),
                                                           this.getName()
                                                         );

         primaryKey.setCommonContentViaDb();
         primaryKey.setSpecificContentViaDb();

         this.setPrimaryKey(primaryKey);
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "setPrimaryKeyViaDb failed: exception occured for table {0}: {1}",
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
    * Returns the SQL statements required for creating
    * this table.
    * <p>
    * @return DbObjectSqlStatements
    *    The SQL statements required for creating
    *    the table.
    */
   public DbObjectSqlStatements getDdlStatementsForCreate()
   throws JddException
   {
      DbObjectSqlStatements statements = null;
      
      try
      {
         location.entering("getDdlStatementsForCreate");

         statements = super.getDdlStatementsForCreate();
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "getDdlStatementsForCreate failed: exception occured for table {0}: {1}",
                          new String[] {
                                         this.getName(),
                                         exception.getMessage()
                                       }
                        );

         throw JddException.createInstance(exception);
      }
      
      return statements;
   }

   /**
    * Returns the SQL statements required for dropping
    * this table.
    * <p>
    * @return DbObjectSqlStatements
    *    The SQL statements required for dropping
    *    the table.
    */
   public DbObjectSqlStatements getDdlStatementsForDrop()
   throws JddException
   {
      DbObjectSqlStatements statements = null;
      
      try
      {
         location.entering("getDdlStatementsForDrop");

         statements = super.getDdlStatementsForDrop();
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "getDdlStatementsForDrop failed: exception occured for table {0}: {1}",
                          new String[] {
                                         this.getName(),
                                         exception.getMessage()
                                       }
                        );

         throw JddException.createInstance(exception);
      }
      
      return statements;
   }

   /**
    * Compares this table to a target table.
    * <p>
    * @param DbTable target
    *    The target table to compare this table to.
    * @return DbTableDifference 
    *    The difference between the tables.
    * @throws Exception
    *    If an error occurs.
    */
   public DbTableDifference compareTo(DbTable target)
   throws Exception
   {
      DbTableDifference difference = null;
  	
      try
      {    
         location.entering("compareTo");

         difference = super.compareTo(target);
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "compareTo failed: exception occured for table {0}: {1}",
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

      return difference;
   }	

   /**
    * Writes the specific content of this table
    * as XML content to a writer.
    * <p>
    * @param PrintWriter file
    *    The file to write the XML content to.
    * @param String offset0
    *    The current XML format offset.
    * @throws JddException
    *    If an error occurs.
    */
   public void writeTableSpecificContentToXmlFile(PrintWriter file, String offset0)
   throws JddException
   {
      try
      {
         location.entering("writeTableSpecificContentToXmlFile");
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "writeCommonContentToXmlFile failed: exception occured for table {0}: {1}",
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
    * Returns the names of all views on the database using
    * this table as their base table.
    * <p>
    * @return ArrayList
    *    The names of the dependent views.
    * @throws JddException
    *    If an error occurs.
    */
   public ArrayList getDependentViews()
   throws JddException
   {
      ArrayList viewNames = new ArrayList();

      try
      {
         location.entering("getDependentViews");

         String[] result     = this.executeQueryOnDb(
                                                      "SELECT \"USER_USERS\".\"USERNAME\" "
                                                    + "FROM   \"USER_USERS\" "
                                                    );

         String   tableName  = this.getName().toUpperCase();
         String   tableOwner = result[0];

                  result     = this.executeQueryOnDb(
                                                      "SELECT \"USER_DEPENDENCIES\".\"NAME\" "
                                                    + "FROM   \"USER_DEPENDENCIES\" "
                                                    + "WHERE  \"USER_DEPENDENCIES\".\"REFERENCED_NAME\"  = '" + tableName  + "' "
                                                    + "AND    \"USER_DEPENDENCIES\".\"REFERENCED_OWNER\" = '" + tableOwner + "' "
                                                    + "AND    \"USER_DEPENDENCIES\".\"TYPE\"  = 'VIEW' "
                                                    );
                                                
         for(int index = 0; index < result.length; index ++)
         {
            viewNames.add(result[index]);
         }
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "getDependentViews failed: exception occured for table {0}: {1}",
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
    
      return viewNames;
   } 

   /**
    * Checks whether the table exists on the database.
    * <p>
    * @return boolean
    *    <code>true</code>, if the table exists,
    *    <code>false</code> otherwise.
    * @throws JddException
    *    If an error occurs.
    */
   public boolean existsOnDb()
   throws JddException
   {
      boolean exists = false;

      try
      {
         location.entering("existsOnDb");

         String   tableName = this.getName().toUpperCase();
         
         String[] result    = this.executeQueryOnDb(
                                                     "SELECT 1 "
                                                   + "FROM   \"DUAL\" "
                                                   + "WHERE  EXISTS(SELECT * "
                                                   + "              FROM   \"USER_TABLES\" "
                                                   + "              WHERE  \"TABLE_NAME\" = '" + tableName + "') "
                                                   );
    
         exists = (result.length > 0);
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "existsOnDb failed: exception occured for table {0}: {1}",
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
    * Checks whether the table on the database
    * contains data.
    * <p>
    * @return boolean 
    *    <code>true</code>, if data exists,
    *    <code>false</code> otherwise.
    * @throws JddException
    *    If an error occurs.
    */
   public boolean existsData()
   throws JddException
   {
      boolean exists = false;

      try
      {
         location.entering("existsData");
         
         String   tableName = this.getName().toUpperCase();
            
         String[] result    = this.executeQueryOnDb(
                                                     "SELECT 1 "
                                                   + "FROM   \"DUAL\" "
                                                   + "WHERE  EXISTS(SELECT * "
                                                   + "              FROM   \"" + tableName + "\") "
                                                   );
    
         exists = (result.length > 0);
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "existsData failed: exception occured for table {0}: {1}",
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
    * Checks whether the width of the table is allowed.
    * <p>
    * @return boolean 
    *    <code>true</code>, if the width is ok,
    *    <code>false</code> otherwise.
    */  
   public boolean checkWidth()
   {
      boolean correct = false;
      
      try
      {
         location.entering("checkWidth");

         correct = true;

         if(!correct)
         {
            category.errorT(
                             location,
                             "checkWidth: the width of table {0} is too great",
                             new String[] {
                                            this.getName()
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
  
   /**
    * Checks whether the length of the table
    * name is allowed.
    * <p>
    * @return boolean 
    *    <code>true</code>, if the length is ok,
    *    <code>false</code> otherwise.
    */  
   public boolean checkNameLength()
   {
      boolean correct = false;
      
      try
      {
         location.entering("checkNameLength");

         correct = ((this.getName().length() >  0) &&
                    (this.getName().length() < 31));
  
         if(!correct)
         {
            category.errorT(
                             location,
                             "checkNameLength: the name of table {0} is too long",
                             new String[] {
                                            this.getName()
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
  
   /**
    * Checks whether the table name conflicts with a
    * reserved word.
    * <p>
    * @return boolean
    *    <code>true</code>, if the name does not conflict,
    *    <code>false</code> otherwise.
    */
   public boolean checkNameForReservedWord()
   {
      boolean correct = false;
      
      try
      {
         location.entering("checkNameForReservedWord");

         correct = !(DbOraEnvironment.isReservedWord(this.getName()));

         if(!correct)
         {
            category.errorT(
                             location,
                             "checkNameForReservedWord: the name of table {0} is a reserved word",
                             new String[] {
                                            this.getName()
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

   /**
    * Checks whether the db-specific parameters of
    * this table are ok.
    * <p>
    * @return boolean
    *    <code>true</code>, if the db-specific parameters are ok,
    *    <code>false</code> otherwise
   **/
   public boolean checkTableSpecificContent()
   {
      boolean correct = false;
      
      try
      {
         location.entering("checkTableSpecificContent");
      
         correct = true;
      
         if(!correct)
         {
            category.errorT(
                             location,
                             "checkTableSpecificContent: a database-specific parameter for table {0} is invalid",
                             new String[] {
                                            this.getName()
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
