package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndex;
import com.sap.dictionary.database.dbs.DbIndexColumnInfo;
import com.sap.dictionary.database.dbs.DbIndexDifference;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbSqlStatement;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlHelper;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.dictionary.database.ora.DbOraEnvironment;
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
 * @author Andrea Neufeld & Tobias Wenner. Markus Maurer
 * @version 1.0
 */

/**
 *
 */
public class DbOraIndex
extends DbIndex
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraIndex");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);
   
   /**
    * 
    */
   public static final int TYPE_NORMAL = 0;
   public static final int TYPE_BITMAP = 1;
   
   /**
    * 
    */
   private int type = DbOraIndex.TYPE_NORMAL;

   /**
    * Constructor.
    */
   public DbOraIndex()
   {
      super();
      
      this.setSpecificIsSet(true);
   }
   
   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used to create this index.
    */
   public DbOraIndex(DbFactory factory)
   {
      super(factory);
      
      this.setSpecificIsSet(true);
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used to create this index.
    * @param DbIndex other
    *    Another index.
    */
   public DbOraIndex(DbFactory factory, DbIndex other)
   {
      super(factory, other);
      
      this.setSpecificIsSet(true);
	}

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used to create this index.
    * @param String tableName
    *    The name of the table this index belongs to.
    * @param String indexName
    *    The name of this index.
    */
   public DbOraIndex(DbFactory factory, String tableName, String indexName)
   {
      super(factory, tableName, indexName);
      
      this.setSpecificIsSet(true);
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory
    *    The factory used to create this index.
    * @param DbSchema schema
    *    The schema this index comes to lie in.
    * @param String tableName
    *    The name of the table this index belongs to.
    * @param String indexName
    *    The name of this index.
    */
   public DbOraIndex(DbFactory factory, DbSchema schema, String tableName, String indexName)
   {
      super(factory, schema, tableName, indexName);
   
      this.setSpecificIsSet(true);
	}

   /**
    * Returns the type of this index. If the index
    * is unique, the returned type should always
    * be normal.
    * <p>
    * @return int
    *    <code>DbOraIndex.TYPE_NORMAL</code> or
    *    <code>DbOraIndex.TYPE_BITMAP</code>
    */
   public int getType()
   {
      return this.type;
   }

   /**
    * Sets the type of this index. If the index is
    * unique, the resulting type will always be
    * normal.
    * <p>
    * @param int type
    *    The type of this index, 
    *    <code>DbOraIndex.TYPE_NORMAL</code> or
    *    <code>DbOraIndex.TYPE_BITMAP</code>
    */
   public void setType(int type)
   {
      switch(type)
      {
         case DbOraIndex.TYPE_NORMAL: this.type = DbOraIndex.TYPE_NORMAL; break;
         case DbOraIndex.TYPE_BITMAP: this.type = DbOraIndex.TYPE_BITMAP; break;
         
         default:                     ;
      }
      
      if(this.isUnique())
      {
         this.type = DbOraIndex.TYPE_NORMAL;
      }
   }
   
   /**
    * Replaces the db-specific parameters of this index
    * with those of another index.
    * <p>
    * @param DbIndex index
    *    The index to take the replacement values from.
    **/   
   public void replaceSpecificContent(DbIndex index)
   {
      try
      {
         location.entering("replaceSpecificContent");
         
         if(index == null)
            return;

         DbOraIndex oraIndex = (DbOraIndex) index;
         
         this.setType(oraIndex.getType());
      }
      finally
      {
         location.exiting();
      }
   }
    
   /**
    * Compares the database-specific content of this
    * index to a target index.
    * <p>
    * @param DbIndex index
    *    The index to compare this index to.
    * @return boolean
    *    <code>true</code>, if the database-specific content
    *                       of the indices is equal,
    *    <code>false</code> otherwise.
    */
   public boolean equalsSpecificContent(DbIndex index)
   {
      boolean equal = false;
      
      try
      {
         location.entering("equalsSpecificContent");

         DbOraIndex oraIndex = (DbOraIndex) index;

         equal = (this.getType() == oraIndex.getType());
      }
      finally
      {
         location.exiting();
      }

      return equal;
   }     

   /**
    * Sets the specific content of this index via
    * data from the XML map.
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

         if(( xmlMap !=  null) &&
            (!xmlMap.isEmpty()))
         {
            XmlMap xmlMapOra = xmlMap.getXmlMap("ora");
            
            if(( xmlMapOra !=  null) &&
               (!xmlMapOra.isEmpty()))
            {
               String xmlMapOraType = xmlMapOra.getString("type");
            
               if(xmlMapOraType !=  null)
               {
                  if(xmlMapOraType.equalsIgnoreCase("normal"))
                     this.setType(DbOraIndex.TYPE_NORMAL);
                  else
                  if(xmlMapOraType.equalsIgnoreCase("bitmap" ))
                     this.setType(DbOraIndex.TYPE_BITMAP);
               }
            }
         }
      }
      catch(Exception exception)
      { 
         category.errorT(
                          location,
                          "setSpecificContentViaXml failed: exception occured for index {0}: {1}",
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
    * Sets the common content of this index
    * via the database.
    * <p>
    * @throws JddException
    *    If an error occurs.
    */
   public void setCommonContentViaDb()
   throws JddException
   {
      boolean     isUnique    = false;
      ArrayList   columnsInfo = new ArrayList();

      try
      {
         location.entering("setCommonContentViaDb");
         
         String[] result     = this.executeQueryOnDb(
                                                      "SELECT \"USER_USERS\".\"USERNAME\" "
                                                    + "FROM   \"USER_USERS\" "
                                                    );

         String   indexName  = this.getName().toUpperCase();
         String   tableName  = this.getTableName().toUpperCase();
         String   tableOwner = result[0];
         
                  result     = this.executeQueryOnDb(
                                                      "SELECT \"USER_INDEXES\".\"UNIQUENESS\" "
                                                    + "FROM   \"USER_INDEXES\" "
                                                    + "WHERE  \"USER_INDEXES\".\"INDEX_NAME\"  = '" + indexName  + "' "
                                                    + "AND    \"USER_INDEXES\".\"TABLE_NAME\"  = '" + tableName  + "' "
                                                    + "AND    \"USER_INDEXES\".\"TABLE_OWNER\" = '" + tableOwner + "' "
                                                    );

         if(result.length > 0)
         {
            isUnique = result[0].equals("UNIQUE");
         }
         
                  result     = this.executeQueryOnDb(
                                                      "SELECT   \"USER_IND_COLUMNS\".\"COLUMN_NAME\" "
                                                    + "FROM     \"USER_IND_COLUMNS\" "
                                                    + "WHERE    \"USER_IND_COLUMNS\".\"INDEX_NAME\" = '" + indexName + "' "
                                                    + "AND      \"USER_IND_COLUMNS\".\"TABLE_NAME\" = '" + tableName + "' "
                                                    + "ORDER BY \"USER_IND_COLUMNS\".\"COLUMN_POSITION\" "
                                                    );
    
         for(int index = 0; index < result.length; index ++)
         {
            /*
             * CAUTION: Oracle doesn't support option DESC,
             * therefore always <code>false</code> is returned.
             */
            DbIndexColumnInfo columnInfo = new DbIndexColumnInfo(result[index], false);

            columnsInfo.add(columnInfo);
         }

         this.setContent(isUnique, columnsInfo);
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "setCommonContentViaDb failed: exception occured for index {0}: {1}",
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
    * Sets the specific content of this index
    * via the database.
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
         
         String[] result     = this.executeQueryOnDb(
                                                      "SELECT \"USER_USERS\".\"USERNAME\" "
                                                    + "FROM   \"USER_USERS\" "
                                                    );

         String   indexName  = this.getName().toUpperCase();
         String   tableName  = this.getTableName().toUpperCase();
         String   tableOwner = result[0];
         
                  result     = this.executeQueryOnDb(
                                                      "SELECT \"USER_INDEXES\".\"INDEX_TYPE\" "
                                                    + "FROM   \"USER_INDEXES\" "
                                                    + "WHERE  \"USER_INDEXES\".\"INDEX_NAME\"  = '" + indexName  + "' "
                                                    + "AND    \"USER_INDEXES\".\"TABLE_NAME\"  = '" + tableName  + "' "
                                                    + "AND    \"USER_INDEXES\".\"TABLE_OWNER\" = '" + tableOwner + "' "
                                                    );

         if(result.length > 0)
         {
            if(result[0].equals("BITMAP"))
               this.setType(DbOraIndex.TYPE_BITMAP);
            else
               this.setType(DbOraIndex.TYPE_NORMAL);
         }
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "setCommonContentViaDb failed: exception occured for index {0}: {1}",
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
    * Writes the specific content of this index
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
         
         String xmlMapOraType = "normal";
         
         if(this.getType() == DbOraIndex.TYPE_NORMAL)
            xmlMapOraType  = "normal";
         else
         if(this.getType() == DbOraIndex.TYPE_BITMAP)
            xmlMapOraType  = "bitmap";
         
         
         String offset1       = offset0 + XmlHelper.tabulate();

         file.println(offset0 + "<ora>");
         file.println(offset1 +    "<type>" + xmlMapOraType + "</type>");
			file.println(offset0 + "</ora>");
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "writeSpecificContentToXmlFile failed: exception occured for index {0}: {1}",
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
    * Returns the SQL statements required for creating this
    * index. If a system-generated primary index of at least
    * a subset of the index columns exists, the primary key
    * is disabled, to allow for index creation in case the
    * column list is the same.
    * <p>
    * @return DbObjectSqlStatements
    *    The SQL statements for creating this index.
    */
   public DbObjectSqlStatements getDdlStatementsForCreate()
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements(this.getName());

      try
      {
         location.entering("getDdlStatementsForCreate");
         
         boolean disable = this.disablePrimaryKeyForCreate();
         
         if(disable)
         {
            statements.merge(this.getDdlStatementsForDisablingPrimaryKey(false));
            statements.merge(this.getDdlStatementsForCreateInternal());
            statements.merge(this.getDdlStatementsForEnablingPrimaryKey(false));
         }
         else
            statements.merge(this.getDdlStatementsForCreateInternal());
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }

   /**
    * Returns the basic SQL statements required for creating
    * this index, regardless of the index being used by a 
    * primary key.
    * <p>
    * @return DbObjectSqlStatements
    *    The SQL statements for creating this index.
    */
   private DbObjectSqlStatements getDdlStatementsForCreateInternal()
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements("");

      try
      {
         location.entering("getDdlStatementsForCreateInternal");
         
         String indexName =  this.getName().toUpperCase();
         String tableName =  this.getTableName().toUpperCase();
         
         String unique    = (this.isUnique()                          ) ? "UNIQUE " : "";
         String type      = (this.getType()  == DbOraIndex.TYPE_BITMAP) ? "BITMAP " : "";

         DbSqlStatement create  = new DbSqlStatement();
            
         create.addLine("CREATE " + unique + type + "INDEX " + "\"" + indexName + "\" ");
         create.addLine("ON "                                + "\"" + tableName + "\"" );
         create.merge(this.getDdlColumnsClause());
            
         statements.add(create);
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }

   /**
    * Returns the SQL statements necessary for dropping this
    * index. If a primary key holds a lock on this index,
    * statements are added for disabling the primary key before
    * index deletion.
    * <p>
    * @return DbObjectSqlStatements
    *    The SQL statements for dropping this index.
    */
   public DbObjectSqlStatements getDdlStatementsForDrop()
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements(this.getName());

      try
      {
         location.entering("getDdlStatementsForDrop");

         boolean disable = this.disablePrimaryKeyForDrop();
         
         if(disable)
         {
            statements.merge(this.getDdlStatementsForDisablingPrimaryKey(true));
            statements.merge(this.getDdlStatementsForDropInternal());
            statements.merge(this.getDdlStatementsForEnablingPrimaryKey(true));
         }
         else
            statements.merge(this.getDdlStatementsForDropInternal());
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }

   /**
    * Returns the basic SQL statements required for dropping
    * this index, regardless of the index being used by a
    * primary key.
    * <p>
    * @return DbObjectSqlStatements
    *    The SQL statements for dropping this index.
    */
   private DbObjectSqlStatements getDdlStatementsForDropInternal()
   {
      DbObjectSqlStatements statements = null;

      try
      {
         location.entering("getDdlStatementsForDropInternal");

         statements = super.getDdlStatementsForDrop();
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }

   /**
    * Returns the SQL statements necessary for disabling the
    * primary key in case that the primary key uses this index
    * for realizing its primary key constraint.
    * <p>
    * @param boolean drop
    *    Whether the statement is called in an attempt to
    *    drop the index.
    * @return DbObjectSqlStatements
    *    The SQL statements for disabling the primary key.
    */
   private DbObjectSqlStatements getDdlStatementsForDisablingPrimaryKey(boolean drop)
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements("");

      try
      {
         location.entering("getDdlStatementsForDisable");

         String tableName = this.getTableName().toUpperCase();

         
         DbSqlStatement disable = new DbSqlStatement(drop);
         
         disable.addLine("ALTER TABLE \""      + tableName + "\" ");
         disable.addLine("DISABLE PRIMARY KEY "                   );

         statements.add(disable);
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }
   
   /**
    * Returns the SQL statements necessary for re-enabling the
    * primary key after a disable took place, in case that the
    * primary key uses this index for realizing its primary key
    * constraint.
    * <p>
    * @param boolean drop
    *    Whether the statement is called in an attempt to
    *    drop the index.
    * @return DbObjectSqlStatements
    *    The SQL statements for enabling the primary key.
    */
   private DbObjectSqlStatements getDdlStatementsForEnablingPrimaryKey(boolean drop)
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements("");

      try
      {
         location.entering("getDdlStatementsForEnable");

         String tableName = this.getTableName().toUpperCase();

         
         DbSqlStatement enable = new DbSqlStatement(drop);
         
         enable.addLine("ALTER TABLE \""      + tableName + "\" ");
         enable.addLine("ENABLE PRIMARY KEY "                    );

         statements.add(enable);
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }

   /**
    * Returns whether we need to disable the underlying table's
    * primary key before index creation. This function always
    * returns <code>false</code>.
    * <p>
    * @return boolean
    *    If the primary key needs to be disabled.
    */
   private boolean disablePrimaryKeyForCreate()
   {
      return false;
   }
   
   /**
    * Returns whether we need to disable the underlying table's
    * primary key before index deletion. This is required if the
    * index is being used as the primary index of the underlying
    * table's primary key.
    * <p>
    * @return boolean
    *    <code>true</code>, if the primary key needs to be disabled,
    *    <code>false</code> otherwise.
    */
   private boolean disablePrimaryKeyForDrop()
   {
      boolean disable = false;

      try
      {  
         location.entering("disablePrimaryKeyForDrop");
   
         String     name      = this.getName().toUpperCase();
         String     tableName = this.getTableName().toUpperCase();
                  
         String[][] result    = this.executeQueryOnDb(
                                                       "SELECT \"USER_CONSTRAINTS\".\"STATUS\", "
                                                     + "       \"USER_CONSTRAINTS\".\"INDEX_NAME\" "
                                                     + "FROM   \"USER_CONSTRAINTS\" "
                                                     + "WHERE  \"USER_CONSTRAINTS\".\"TABLE_NAME\"      = '" + tableName + "' "
                                                     + "AND    \"USER_CONSTRAINTS\".\"CONSTRAINT_TYPE\" = 'P' ",
                                                       
                                                       new String[2]
                                                     );
    
         if(result.length > 0)
         {
            String status    = result[0][0];
            String indexName = result[0][1];
            
            if(status.equals("ENABLED") && indexName.equals(name))
            {
               disable = true;
            }
         }
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "disablePrimaryKeyForDrop failed: exception occured for index {0}: {1}",
                          new String[] {
                                         this.getName(),
                                         exception.getMessage()
                                       }
                        );
      }
      finally
      {
         location.exiting();
      }

      return disable;
   }

   /**
    * Compares this index to a target index.
    * <p>
    * @param DbIndex target
    *    The target index to compare this index to.
    * @return DbIndexDifference
    *    The difference between this index and the
    *    target index.
    * @throws Exception
    *    If an error occurs.
    */
   public DbIndexDifference compareTo(DbIndex target)
   throws JddException
   {
      DbIndexDifference difference = null;
      
      try
      {
         location.entering("compareTo");       
                
         DbOraIndex index     = (DbOraIndex) target;
         
         boolean    different = false;
//       boolean    different = (this.getType() != index.getType());
            
         if(different)
            difference = this.getDbFactory().makeDbIndexDifference(this, target, Action.DROP_CREATE);
         else
            difference = super.compareTo(target);
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "compareTo failed: exception occured for index {0}: {1}",
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
    * Checks whether the index exists on the database.
    * <p>
    * @return boolean
    *    <code>true</code>, if the index exists,
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

         String[] result     = this.executeQueryOnDb(
                                                      "SELECT \"USER_USERS\".\"USERNAME\" "
                                                    + "FROM   \"USER_USERS\" "
                                                    );

         String   indexName  = this.getName().toUpperCase();
         String   tableName  = this.getTableName().toUpperCase();
         String   tableOwner = result[0];

                  result     = this.executeQueryOnDb(
                                                      "SELECT 1 "
                                                    + "FROM   \"DUAL\" "
                                                    + "WHERE  EXISTS(SELECT * "
                                                    + "              FROM   \"USER_INDEXES\" "
                                                    + "              WHERE  \"USER_INDEXES\".\"INDEX_NAME\"  = '" + indexName  + "' "
                                                    + "              AND    \"USER_INDEXES\".\"TABLE_NAME\"  = '" + tableName  + "' "
                                                    + "              AND    \"USER_INDEXES\".\"TABLE_OWNER\" = '" + tableOwner + "') "
                                                    );
    
         exists = (result.length > 0);
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "existsOnDb failed: exception occured for index {0}: {1}",
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
    * Checks whether the width of this index
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


         DbColumns columns = this.getIndexes().getTable().getColumns();
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
               case java.sql.Types.LONGVARBINARY: width += column.getLength();                     break;
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
                             "checkWidth failed: width of index {0} is too large",
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
    * Checks whether the length of the index name
    * is allowed.
    * <p>
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
                             "checkNameLength failed: the name of index {0} is too long",
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
    * Checks whether the number of columns of this
    * index is allowed.
    * <p>
    * @return boolean
    *    <code>true</code>, if the number is ok,
    *    <code>false</code> otherwise.
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
                             "checkNumberOfColumns failed: index {0} has too many columns",
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
    * Checks whether the index name conflicts with a
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
                             "checkNameForReservedWord: name of index {0} is a reserved word",
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
    * this index are ok.
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

         if(this.isUnique())
         {
            correct = (this.getType() == DbOraIndex.TYPE_NORMAL);
         }
         else
         {
            correct = (this.getType() == DbOraIndex.TYPE_NORMAL) ||
                      (this.getType() == DbOraIndex.TYPE_BITMAP);
         }
         
         if(!correct)
         {
            category.errorT(
                             location,
                             "checkSpecificContent failed: a database-specific parameter for index {0} is invalid",
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
      
      Connection connection = this.getDbFactory().getConnection();
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
