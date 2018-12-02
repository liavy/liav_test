package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndexColumnInfo;
import com.sap.dictionary.database.dbs.DbPrimaryKey;
import com.sap.dictionary.database.dbs.DbPrimaryKeyDifference;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbSqlStatement;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;

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
public class DbOraPrimaryKey
extends DbPrimaryKey
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraPrimaryKey");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);

   /**
    * Constructor.
    */
   public DbOraPrimaryKey()
   {
      super();
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used by this primary key.
    */
   public DbOraPrimaryKey(DbFactory factory)
   {
      super(factory);
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used by this primary key.
    * @param DbPrimaryKey primaryKey
    *    A primary key to create this key from.
    */
   public DbOraPrimaryKey(DbFactory factory, DbPrimaryKey primaryKey)
   {
      super(factory, primaryKey);
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used by this primary key.
    * @param String tableName
    *    The name of the table this primary
    *    key belongs to.
    */
   public DbOraPrimaryKey(DbFactory factory, String tableName)
   {
      super(factory, tableName);
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used by this primary key.
    * @param DbSchema schema
    *    The schema this primary key comes to
    *    lie in.
    * @param String tableName
    *    The name of the table this primary
    *    key belongs to.
    */
   public DbOraPrimaryKey(DbFactory factory, DbSchema schema, String tableName)
   {
      super(factory, schema, tableName);
   }

   /**
    * Replaces the db-specific parameters of this primary key
    * with those of another primary key.
    * <p>
    * @param DbPrimaryKey primaryKey
    *    The primary key to take the replacement values from.
    **/   
   public void replaceSpecificContent(DbPrimaryKey primaryKey)
   {
      try
      {
         location.entering("replaceSpecificContent");
      }
      finally
      {
         location.exiting();
      }
   }

   /**
    * Compares the database-specific content of this
    * primary key to a target primary key.
    * <p>
    * @param DbPrimaryKey primaryKey
    *    The primary key to compare this key to.
    * @return boolean
    *    <code>true</code>, if the database-specific content
    *                       of the primary keys is equal,
    *    <code>false</code> otherwise.
    */
   public boolean equalsSpecificContent(DbPrimaryKey primaryKey)
   {
      boolean equal = false;
      
      try
      {
         location.entering("equalsSpecificContent");
         
         equal = true;
      }
      finally
      {
         location.exiting();
      }

      return equal;
   }     

   /**
    * Sets the specific content of this primary key
    * via data from the XML map.
    * <p>
    * @param XmlMap xmlMap
    *    The XML map containing the specific content.
    * @throws JddException
    *    If an error occurs.
    */
   public void setSpecificContentViaXml(XmlMap xmlMap)
   throws JddException
   {
      try
      {
         location.entering("setSpecificContentViaXml");
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "setSpecificContentViaXml failed: exception occured for primary key of table {0}: {1}",
                          new String[] {
                                         this.getTableName(),
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
    * Sets the common content of this primary key
    * via database.
    * <p>
    * @throws JddException
    *    If an error occurs.
    */
   public void setCommonContentViaDb()
   throws JddException
   {
      try
      {
         location.entering("setCommonContentViaDb");

         String   tableName = this.getTableName().toUpperCase();
         
         String[] result    = this.executeQueryOnDb(
                                                     "SELECT   \"USER_CONS_COLUMNS\".\"COLUMN_NAME\" "
                                                   + "FROM     \"USER_CONSTRAINTS\", \"USER_CONS_COLUMNS\" "
                                                   + "WHERE    \"USER_CONSTRAINTS\".\"CONSTRAINT_NAME\" = \"USER_CONS_COLUMNS\".\"CONSTRAINT_NAME\" "
                                                   + "AND      \"USER_CONSTRAINTS\".\"TABLE_NAME\" = '" + tableName + "' "
                                                   + "AND      \"USER_CONSTRAINTS\".\"CONSTRAINT_TYPE\" = 'P' "
                                                   + "ORDER BY \"USER_CONS_COLUMNS\".\"POSITION\" "
                                                   );

         ArrayList columnsInfo = new ArrayList();

         for(int index = 0; index < result.length; index ++)
         {
            columnsInfo.add(new DbIndexColumnInfo(result[index], false));            
         }

         this.setContent(columnsInfo); 
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "setCommonContentViaDb failed: exception occured for primary key of table {0}: {1}",
                          new String[] {
                                         this.getTableName(),
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
    * Sets the specific content via Db.
    * <p>
    * @throws JddException
    *    If an error occurs.
    */
   public void setSpecificContentViaDb()
   throws JddException
   {
      try
      {
         location.entering("setSpecificContentViaDb");
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "setSpecificContentViaDb failed: exception occured for primary key of table {0}: {1}",
                          new String[] {
                                         this.getTableName(),
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
    * Writes the specific content of this primary key
    * to an XML file.
    * <p>
    * @param PrintWriter file
    *    The writer to write the XML content to.
    * @param String offset0
    *    The current XML format offset.
    * @throws JddException
    *    If an error occurs.
    */
   public void writeSpecificContentToXmlFile(PrintWriter file, String offset0)
   throws JddException
   {
      try
      {
         location.entering("writeSpecificContentToXmlFile");
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "writeCommonContentToXmlFile failed: exception occured for primary key of table {0}: {1}",
                          new String[] {
                                         this.getTableName(),
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
    * this primary key.
    * <p>
    * @return DbObjectSqlStatements
    *    The SQL statements required for creating
    *    the primary key.
    */
   public DbObjectSqlStatements getDdlStatementsForCreate()
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements(this.getTableName() + " PK");
      
      try
      {
         location.entering("getDdlStatementsForCreate");      

         String tableName = this.getTableName().toUpperCase();
         
         
         DbSqlStatement statement  = new DbSqlStatement();

         statement.addLine("ALTER TABLE \""    + tableName   + "\" ");
         statement.addLine("ADD PRIMARY KEY "                       );
         statement.merge(this.getDdlColumnsClause());

         statements.add(statement);
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }
  
   /**
    * Returns the SQL statements required for dropping
    * this primary key.
    * <p>
    * @return DbObjectSqlStatements
    *    The SQL statements required for dropping
    *    the primary key.
    */
   public DbObjectSqlStatements getDdlStatementsForDrop()
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements(this.getTableName() + " PK");
      
      try
      {
         location.entering("getDdlStatementsForDrop");

         String tableName = this.getTableName().toUpperCase();
         
         
         DbSqlStatement statement = new DbSqlStatement(true);

         statement.addLine("ALTER TABLE \""    + tableName + "\" ");
         statement.addLine("DROP PRIMARY KEY"                     );

         statements.add(statement);
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }

   /**
    * Difference checks on primary key column count, column
    * names and DB-dependent specific primary key attributes
    * decide on whether the primary key is different from the
    * target primary key. If so, a DbPrimaryKeyDifference
    * object is returned with the necessary drop/create action.
    * <p>
    * @param DbOraPrimaryKey target
    *    The primary key to compare this primary key to
    * @return DbPrimaryKeyDifference
    *    The difference between this and the target primary key.
    * @throws JddException
    *    If an error occurs.
    */
   public DbPrimaryKeyDifference compareTo(DbOraPrimaryKey target)
   throws JddException
   {
      DbPrimaryKeyDifference difference = null;
      
      try
      {    
         location.entering("compareTo");

         difference = super.compareTo(target);
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "compareTo failed: exception occured for primary key of table {0}: {1}",
                          new String[] {
                                         this.getTableName(),
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
    * Checks whether the width of this primary key
    * is allowed.
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

         int blockSize = 4194304;
         int width     = 0; 


         DbColumns columns = this.getTable().getColumns();
         DbColumn  column; 

         for(Iterator iterator = this.getColumnNames().iterator(); iterator.hasNext(); )
         {
            column = columns.getColumn(((DbIndexColumnInfo) iterator.next()).getName());

            switch(column.getJavaSqlType())
            {
               case java.sql.Types.BIGINT:        width += 22;                                     break;
               case java.sql.Types.BINARY:        width += column.getLength();                     break;
               case java.sql.Types.BLOB:          width +=  0;                                     break;
               case java.sql.Types.CHAR:          width += Math.min(column.getLength() * 3, 2000); break;
               case java.sql.Types.CLOB:          width +=  0;                                     break;
               case java.sql.Types.DATE:          width +=  7;                                     break;
               case java.sql.Types.DECIMAL:       width += 22;                                     break;
               case java.sql.Types.DOUBLE:        width += 22;                                     break;
               case java.sql.Types.FLOAT:         width += 22;                                     break;
               case java.sql.Types.INTEGER:       width += 22;                                     break;
               case java.sql.Types.NUMERIC:       width += 22;                                     break;
               case java.sql.Types.OTHER:         width +=  0;                                     break;
               case java.sql.Types.SMALLINT:      width += 22;                                     break;
               case java.sql.Types.TIME:          width +=  7;                                     break;
               case java.sql.Types.TIMESTAMP:     width += 11;                                     break;
               case java.sql.Types.VARBINARY:     width += column.getLength();                     break;
               case java.sql.Types.VARCHAR:       width += Math.min(column.getLength() * 3, 4000); break;

               default:                           width +=  0;
            }
         }
    
         correct = (width < (int) (0.72 * (double) blockSize));
    
         if(!correct)
         {
            category.errorT(
                             location,
                             "checkWidth failed: width of primary key of table {0} is too large",
                             new String[] {
                                            this.getTableName()
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
    * Checks whether the number of primary key columns
    * maintained is allowed.
    * <p>
    * @return boolean
    *    <code>true</code>, if the number of columns is ok,
    *    <code>false</code> otherwise
    */
   public boolean checkNumberOfColumns()
   {
      boolean correct = false;
      
      try
      {
         location.entering("checkNumberOfColumns");

         correct = !(this.getColumnNames().size() > 32);

         if(!correct)
         {
            category.errorT(
                             location,
                             "checkNumberOfColumns failed: the primary key of table {0} has too many columns",
                             new String[] {
                                            this.getTableName()
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
    * Checks whether the primary key columns are not null.
    * <p>
    * @return boolean
    *    <code>true</code>, if the columns are ok,
    *    <code>false</code> otherwise
    */
   public boolean checkColumnsNotNull()
   {
      boolean correct = false;
      
      try
      {
         location.entering("checkColumnsNotNull");

         DbColumns columns = this.getTable().getColumns();
         DbColumn  column;
    
         correct = true;

         for(Iterator iterator = this.getColumnNames().iterator(); iterator.hasNext(); )
         {
            column  = columns.getColumn(((DbIndexColumnInfo) iterator.next()).getName());
      
            correct = correct && !((column == null) || (column.isNotNull() == false));     
         }
    
         if(!correct)
         {
            category.errorT(
                             location,
                             "checkColumnsNotNull failed: the primary key of table {0} uses a column allowing NULL values",
                             new String[] {
                                            this.getTableName()
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
    * Checks the db-specific parameters of the primary key.
    * <p>
    * @return boolean
    *    <code>true</code>, if the db-specific parameters are ok,
    *    <code>false</code> otherwise
   **/
   public boolean checkSpecificContent()
   {
      boolean correct = false;
      
      try
      {
         location.entering("checkSpecificContent");
      
         correct = true;

         if(!correct)
         {
            category.errorT(
                             location,
                             "checkSpecificContent failed: a database-specific parameter of the primary key for table {0} is invalid",
                             new String[] {
                                             this.getTableName()
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
