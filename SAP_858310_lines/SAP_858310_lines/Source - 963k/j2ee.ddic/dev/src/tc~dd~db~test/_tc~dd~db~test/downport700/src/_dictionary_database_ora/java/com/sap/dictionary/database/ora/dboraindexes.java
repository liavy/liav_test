package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndexes;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
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
public class DbOraIndexes
extends DbIndexes
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraIndexes");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);

   /**
    * Constructor.
    */
   public DbOraIndexes()
   {
      super();
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory.
    *    The factory used by this set of indices.
    */
   public DbOraIndexes(DbFactory factory)
   {
      super(factory);
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory.
    *    The factory used by this set of indices.
    * @param DbColumns columns
    *    A set of indices to create this set from.
    */
   public DbOraIndexes(DbFactory factory, DbIndexes indexes)
   {
      super(factory, indexes);
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory.
    *    The factory used by this set of indices.
    * @param XmlMap xmlMap
    *    A XML map to create this set of indices
    *    from.
    */
   public DbOraIndexes(DbFactory factory, XmlMap xmlMap)
   throws Exception
   {
      super(factory, xmlMap);
   }

   /**
    * Checks whether the number of indexes maintained
    * is allowed.
    * <p>
    * @return boolean 
    *    <code>true</code>, if number of indexes is ok,
    *    <code>false</code> otherwise
    */
   public boolean checkNumber()
   {
      boolean correct = false;
      
      try
      {
         location.entering("checkNumber");

         correct = true; 

         if(!correct)
         {     
            category.errorT(
                             location,
                             "checkNumber failed: table {0} has too many indices",
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

   /**
    * Checks whether the db-specific parameters of
    * this set of indexes is ok.
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
                             "checkSpecificContent: a database-specific parameter of the index set of table {0} is invalid",
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