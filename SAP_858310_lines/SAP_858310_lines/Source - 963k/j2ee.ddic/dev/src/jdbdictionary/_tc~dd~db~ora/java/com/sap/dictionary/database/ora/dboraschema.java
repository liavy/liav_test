package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

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
public class DbOraSchema
extends DbSchema
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraSchema");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);

   /**
    * Constructor.
    */
   public DbOraSchema()
   {
      super();
   }
   
   /**
    * Constructor.
    * <p>
    * @param String schemaName
    *    The name of the schema.
    */
   public DbOraSchema(String schemaName)
   {
      super(schemaName);
   }
   
   /**
    * Checks the schema name by its length.
    * <p>
    * @return boolean
    *    <code>true</code>, if the schema name is ok,
    *    <code>false</code> otherwise.
    */  
   public boolean checkNameLength()
   {
      boolean correct = false;
      
      try
      {
         location.entering("checkNameLength");
         
         correct = ((this.getSchemaName().length() >  0) &&
                    (this.getSchemaName().length() < 31));

         if(!correct)
         {
            category.errorT(
                             location,
                             "checkNameLength failed: the name of schema {0} is too long",
                             new String[] {
                                            this.getSchemaName()
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