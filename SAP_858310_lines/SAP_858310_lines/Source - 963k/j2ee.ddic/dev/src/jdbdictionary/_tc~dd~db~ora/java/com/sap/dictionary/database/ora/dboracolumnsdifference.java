package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumnDifference;
import com.sap.dictionary.database.dbs.DbColumnsDifference;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbSqlStatement;
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
public class DbOraColumnsDifference
extends DbColumnsDifference
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraColumnsDifference");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);

   /**
    * Constructor.
    */
   public DbOraColumnsDifference()
   {
      super();
   }
   
   /**
    * Returns the SQL statements for transforming the 
    * columns into their target by means of a simple
    * altering action.
    * <p>
    * @param String tableName
    *    The name of the table the columns belong to.
    * @return DbObjectsSqlStatements 
    *    The statements performing the transformation.
    * @throws Exception
    *    If an error occurs.
    */
   public DbObjectSqlStatements getDdlStatementsForAlter(String tableName)
   throws Exception
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements(tableName);
      
      try
      {
         location.entering("getDdlStatementsForAlter");
         
         DbSqlStatement statement;
         DbSqlStatement temporary;

         DbColumnsDifference.MultiIterator iterator = this.iterator();

         DbColumn column;

         // Statements for fields to be added
    
         statement = new DbSqlStatement();
    
         while(iterator.hasNextWithAdd())
         {
            statement.addLine(iterator.nextWithAdd().getTarget().getDdlClause()
                           + (iterator.hasNextWithAdd() ? ", " : "" ));
         }

         if(!statement.isEmpty())
         {
            temporary = new DbSqlStatement();
            temporary.addLine("ALTER TABLE \"" + tableName.toUpperCase() + "\" ADD ");
            temporary.addLine("( ");
            temporary.merge(statement);
            temporary.addLine(") ");

            statements.add(temporary);
         }

         // Statements for fields to be dropped
        
         statement = new DbSqlStatement();
    
         while(iterator.hasNextWithDrop())
         {
            statement.addLine("\"" + iterator.nextWithDrop().getOrigin().getName() + "\""
                                   + (iterator.hasNextWithDrop() ? ", " : "" ));
         }

         if(!statement.isEmpty())
         {
            temporary = new DbSqlStatement();
            temporary.addLine("ALTER TABLE \"" + tableName.toUpperCase() + "\" DROP ");
            temporary.addLine("( ");
            temporary.merge(statement);
            temporary.addLine(") ");
            temporary.addLine("CASCADE CONSTRAINTS ");

            statements.add(temporary);
         }

         //Statements of type, length or decimal changes
    
         statement = new DbSqlStatement();
    
         while(iterator.hasNextWithTypeLenDecChange())
         {
            DbColumnDifference columnDifference = iterator.nextWithTypeLenDecChange();
         
            if(columnDifference.getAction() == Action.ALTER )
            {
               column = columnDifference.getTarget();

               statement.addLine("\"" + column.getName().toUpperCase() +  "\" " + column.getDdlTypeClause()
                                + (iterator.hasNextWithTypeLenDecChange() ? " , " : "" ));
            }
         }

         if(!statement.isEmpty())
         {
            statement.addLine(" ) ");

            temporary = new DbSqlStatement();
            temporary.addLine("ALTER TABLE \"" + tableName.toUpperCase() + "\"");
            temporary.addLine(" MODIFY " + "(" );
            temporary.merge(statement);

            statements.add(temporary);
         }

         //Statements of default value changes
    
         statement = new DbSqlStatement();
    
         while (iterator.hasNextWithDefaultValueChange())
         {
            column = iterator.nextWithDefaultValueChange().getTarget();
         
            if(column.getDefaultValue() != null)
            {
               statement.addLine("\"" + column.getName().toUpperCase() + "\" " + column.getDdlDefaultValueClause()
                                + (iterator.hasNextWithDefaultValueChange() ? " , " : "" ));
            }
            else
            {
               statement.addLine("\"" + column.getName().toUpperCase() + "\" DEFAULT NULL"
                                + (iterator.hasNextWithDefaultValueChange() ? " , " : "" ));
            }
         }

         if(!statement.isEmpty())
         {
            statement.addLine(" ) ");

            temporary =  new DbSqlStatement();
            temporary.addLine("ALTER TABLE \"" + tableName.toUpperCase() + "\"");
            temporary.addLine(" MODIFY " + "(");
            temporary.merge(statement);

            statements.add(temporary);
         }

         //Preparation of nullability-change-statements
    
         statement = new DbSqlStatement();
      
         while(iterator.hasNextWithNullabilityChange())
         {
            column = iterator.nextWithNullabilityChange().getTarget();
         
            if(column.isNotNull())
            {
               // NULL -> NOT NULL
        
               statement = new DbSqlStatement();
               statement.addLine("UPDATE \"" + tableName.toUpperCase() + "\"");
               statement.addLine(" SET \"" + column.getName().toUpperCase() + "\" = " +
                                 column.getJavaSqlTypeInfo().getDefaultValuePrefix() +
                                 column.getDefaultValue() +
                                 column.getJavaSqlTypeInfo().getDefaultValueSuffix());
               statement.addLine(" WHERE \"" + column.getName().toUpperCase() + "\" IS NULL ");

               statements.add(statement);
            }
         }

         //Statements of nullability changes
    
         statement = new DbSqlStatement();
    
         DbColumnsDifference.MultiIterator iterator1 = this.iterator();
    
         while(iterator1.hasNextWithNullabilityChange())
         {
            column = iterator1.nextWithNullabilityChange().getTarget();

            if(column.isNotNull())
            {
               statement.addLine("\"" + column.getName().toUpperCase() + "\" NOT NULL"
                                + (iterator1.hasNextWithNullabilityChange() ? " , " : "" ));
            }
            else
            {
               statement.addLine("\"" + column.getName().toUpperCase() + "\" NULL"
                                + (iterator1.hasNextWithNullabilityChange() ? " , " : "" ));
            }
         }

         if(!statement.isEmpty())
         {
            statement.addLine(" ) ");

            temporary =  new DbSqlStatement();
            temporary.addLine("ALTER TABLE \"" + tableName.toUpperCase() + "\"");
            temporary.addLine(" MODIFY " + "( ");
            temporary.merge(statement);

            statements.add(temporary);
         }
      }
      finally
      {
         location.exiting();
      }

      return statements;
   }
   
   /**
    * Returns the SQL statement required for adding
    * columns as per the columns difference.
    * <p>
    * @return DbObjectSqlStatements
    *    The resulting ALTER TABLE ADD SQL statement.
    * @throws Exception
    *    If an error occurs.
    */
   private DbObjectSqlStatements getDdlStatementsForAdd(String tableName)
   throws Exception
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements("");
      
      try
      {
         location.entering("getDdlStatementsForAdd");

         DbColumnsDifference.MultiIterator iterator = this.iterator();
         
         DbSqlStatement statement = new DbSqlStatement();
         
         if(iterator.hasNextWithAdd())
         {
            statement.addLine("ALTER TABLE \"" + tableName + "\" ADD ");
            statement.addLine("( ");

            while(iterator.hasNextWithAdd())
            {
               statement.addLine(
                                   iterator.nextWithAdd().getTarget().getDdlClause()
                                + (iterator.hasNextWithAdd() ? ", " : "" )
                                );
            }

            statement.addLine(") ");

            statements.add(statement);
         }
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }

   /**
    * Returns the SQL statement required for droping
    * columns as per the columns difference.
    * <p>
    * @return DbObjectSqlStatements
    *    The resulting ALTER TABLE DROP SQL statement.
    * @throws Exception
    *    If an error occurs.
    */
   private DbObjectSqlStatements getDdlStatementsForDrop(String tableName)
   throws Exception
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements("");
      
      try
      {
         location.entering("getDdlStatementsForDrop");

         DbColumnsDifference.MultiIterator iterator = this.iterator();
         
         DbSqlStatement statement = new DbSqlStatement();
         
         if(iterator.hasNextWithDrop())
         {
            statement.addLine("ALTER TABLE \"" + tableName + "\" DROP ");
            statement.addLine("( ");

            while(iterator.hasNextWithDrop())
            {
               statement.addLine(
                                  "\"" +  iterator.nextWithDrop().getOrigin().getName() + "\""
                                       + (iterator.hasNextWithDrop() ? ", " : "")
                                );
            }

            statement.addLine(") ");
            statement.addLine("CASCADE CONSTRAINTS ");

            statements.add(statement);
         }
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }

   /**
    * 
    */
   private DbObjectSqlStatements getDdlStatementsForModifyI(String tableName)
   throws Exception
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements("");
      
      try
      {
         location.entering("getDdlStatementsForModifyI");

         DbColumnsDifference.MultiIterator iterator = this.iterator();
         
         DbSqlStatement statement = new DbSqlStatement();
         
         if(iterator.hasNextWithAdd())
         {
            statement.addLine("ALTER TABLE \"" + tableName + "\" ADD ");
            statement.addLine("( ");

            while(iterator.hasNextWithAdd())
            {
               statement.addLine(
                                   iterator.nextWithAdd().getTarget().getDdlClause()
                                + (iterator.hasNextWithAdd() ? ", " : "" )
                                );
            }

            statement.addLine(") ");

            statements.add(statement);
         }

         
         
         
         
         
/*     
         statement = new DbSqlStatement();
    
         while(iterator.hasNextWithTypeLenDecChange())
         {
            DbColumnDifference columnDifference = iterator.nextWithTypeLenDecChange();
         
            if(columnDifference.getAction() == Action.ALTER )
            {
               column = columnDifference.getTarget();

               statement.addLine("\"" + column.getName().toUpperCase() +  "\" " + column.getDdlTypeClause()
                                + (iterator.hasNextWithTypeLenDecChange() ? " , " : "" ));
            }
         }

         if(!statement.isEmpty())
         {
            statement.addLine(" ) ");

            temporary = new DbSqlStatement();
            temporary.addLine("ALTER TABLE \"" + tableName.toUpperCase() + "\"");
            temporary.addLine(" MODIFY " + "(" );
            temporary.merge(statement);

            statements.add(temporary);
         }
*/
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }

   /**
    * 
    */
   private DbObjectSqlStatements getDdlStatementsForModifyII(String tableName)
   throws Exception
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements("");
      
      try
      {
         location.entering("getDdlStatementsForModifyII");
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }

   /**
    * 
    */
   private DbObjectSqlStatements getDdlStatementsForModifyIII(String tableName)
   throws Exception
   {
      DbObjectSqlStatements statements = new DbObjectSqlStatements("");
      
      try
      {
         location.entering("getDdlStatementsForModifyIII");
      }
      finally
      {
         location.exiting();
      }
      
      return statements;
   }
}
