package com.sap.dictionary.database.ora;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumnDifference;
import com.sap.dictionary.database.dbs.DbColumnDifferencePlan;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.dictionary.database.ora.DbOraColumn;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

import java.sql.Types;

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
public class DbOraColumn
extends DbColumn
{
   /**
    *
    */
   private static final Location location = Logger.getLocation("ora.DbOraColumn");
   private static final Category category = Category.getCategory(Category.SYS_DATABASE,
                                                                 Logger.CATEGORY_NAME);

   /**
    * Constructor.
    */
   public DbOraColumn()
   {
      super();
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory.
    *    The factory used by this column.
    */
   public DbOraColumn(DbFactory factory)
   {
      super(factory);
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory.
    *    The factory used by this column.
    * @param DbColumn column
    *    A column to create this column from.
    */
   public DbOraColumn(DbFactory factory, DbColumn column)
   {
      super(factory, column);
   }

   /**
    * Constructor.
    * <p>
    * @param DbFactory factory.
    *    The factory used by this column.
    * @param XmlMap xmlMap
    *    A XML map to create this column from.
    */
   public DbOraColumn(DbFactory factory, XmlMap xmlMap)
   {
      super(factory, xmlMap);
   }

   /**
    * Constructor excluding source type that means
    * Java type. Recommended if the origin is a
    * database which does not know how a database
    * table's column is used in Java.
    */
   public DbOraColumn(DbFactory factory, String name, int position, int javaSqlType,
                      String dbType, long length, int decimals, boolean isNotNull,
                      String defaultValue)
   {
      /*
       * Reading the types FLOAT, CLOB, BLOB from the
       * database catalog returns the Java SQL type
       * OTHER and length 4000 in case of CLOB and BLOB.
       */
      if (dbType.equals("FLOAT"))
      {
         if(length == 63)
            javaSqlType = Types.REAL;
         else
            javaSqlType = Types.FLOAT;
      }
      else
         
      /*
       * The Java SQL types DECIMAL, INTEGER and SMALLINT
       * are all mapped to the database type NUMBER. The
       * Oracle JDBC driver decides to return type DECIMAL.
       */
      if(dbType.equals("NUMBER"))
      {
         if(decimals == 0)
         {
            if(length ==  5)
              javaSqlType = Types.SMALLINT;
            else
            if(length == 10)
              javaSqlType = Types.INTEGER;
            else
            if(length == 19)
              javaSqlType = Types.BIGINT;
         }
      }
      else
         
      /*
       * Since DatabaseMetaData returns datatype OTHER
       * (1111) with byte semantics for the Oracle
       * national character types, these types must be
       * adapted manually as below.
       */
      if(dbType.equals("NCHAR"))
      {
         javaSqlType  = Types.CHAR;
         length      /= 3;
      } 
      else
      if(dbType.equals("NVARCHAR2"))
      {
         javaSqlType  = Types.VARCHAR;
         length      /= 3;
      }
      else
      if(dbType.equals("NCLOB"))
      {
         javaSqlType  = Types.CLOB;
         length       = 0;
      }
      else
      if(dbType.equals("BLOB"))
      {
         javaSqlType  = Types.BLOB;
         length       = 0;
      }
      else
         
      /*
       * TIMESTAMP (93) when DATE, OTHER (1111) when
       * TIMESTAMP. These are bugs in Oracle9i
       * DatabaseMetaData
       */
      if(dbType.equals("DATE"))
      {
         javaSqlType  = Types.DATE;
         length       = 0;
         decimals     = 0;
      }
      else
      if(dbType.equals("TIMESTAMP(6)"))
      {
         javaSqlType  = Types.TIMESTAMP;
         length       = 0;
         decimals     = 0;
      }
      else
      
      /*
       * Database type RAW needs to be transformed to 
       * the respective binary type according to the
       * type length.
       */
      if(dbType.equals("RAW"))
      {
      	 if(length <  256)
      	    javaSqlType = Types.BINARY;
      	 else
             javaSqlType = Types.LONGVARBINARY;	
      }
      
      if(!factory.getJavaSqlTypes().getInfo(javaSqlType).hasLengthAttribute())
      {
         length = 0;
      }

      this.constructorPart(factory, name, position, javaSqlType, dbType, length, decimals, isNotNull, defaultValue);
   }

   /**
    * Returns a java.sql.Types representative that
    * is mapped to the same database datatype as the
    * datatype of this column.
    * <p>
    * @return int
    *    The java.sql.Types representative.
    */
   public int getEquivalentJavaSqlType()
   { 
      try
      {
         location.entering("getEquivalentJavaSqlType");
         
         switch(this.getJavaSqlType())
         {
            case java.sql.Types.BIGINT:        return java.sql.Types.DECIMAL;
            case java.sql.Types.BIT:           return java.sql.Types.DECIMAL;
            case java.sql.Types.DOUBLE:        return java.sql.Types.FLOAT;
            case java.sql.Types.INTEGER:       return java.sql.Types.DECIMAL;
            case java.sql.Types.LONGVARBINARY: return java.sql.Types.BINARY;
            case java.sql.Types.LONGVARCHAR:   return java.sql.Types.VARCHAR;
            case java.sql.Types.NUMERIC:       return java.sql.Types.DECIMAL;
            case java.sql.Types.SMALLINT:      return java.sql.Types.DECIMAL;
            case java.sql.Types.TIME:          return java.sql.Types.DATE;
            case java.sql.Types.TINYINT:       return java.sql.Types.DECIMAL;
            case java.sql.Types.VARBINARY:     return java.sql.Types.BINARY;

            default:                           return getJavaSqlType();
         }
      }
      finally
      {
         location.exiting();
      }
   }
   
   /**
    * If the DbColumn object represents the database
    * state (<code>dbType != null</code>) and dbType,
    * respecting length and decimals, can be mapped
    * to two or more different JDBC types the method
    * returns all these alternatives, otherwise
    * <code>null</code>.
    * <p>
    * @return int[]
    *    The array of JDBC type alternatives,
    *    or <code>null</code>
    */
   public int[] getJavaSqlTypeAlternatives()
   {
      int[] alternatives = null;

      try
      {
         location.entering("getJavaSqlTypeAlternatives");
         
         if(this.getDbType() != null)
         {
            if(this.getDbType().equals("DATE"))
            {
               alternatives = new int[] {
                                           Types.DATE,
                                           Types.TIME
                                        };
            }
         }
      }
      finally
      {
         location.exiting();
      }

      return alternatives;
   }

   /**
    * Compares this column to a target column.
    * <p>
    * @param DbColumn target
    *    The target column to compare this column to.
    * @return DbColumnDifference
    *    The difference between this column and the
    *    target column.
    * @throws Exception
    *    If an error occurs.
    */
   protected DbColumnDifference comparePartTo(DbColumn target)
   throws Exception
   {
      DbColumnDifference     difference     = null;
      DbColumnDifferencePlan differencePlan = new DbColumnDifferencePlan();
      
      try
      {
         location.entering("compareTo");
     
         /*
          * Compare the Java SQL type of the column with
          * the target type. If the equivalent types are
          * different, action CONVERT will be returned.
          */
         if(true)
         {
            if(this.getEquivalentJavaSqlType() != ((DbOraColumn) target).getEquivalentJavaSqlType())
            {
               differencePlan.setTypeIsChanged(true);

               category.infoT(
                               location,
                               "compareTo: Java SQL type {1} of column {0} not equivalent to target type {2}",
                               new String[] {
                                              this.getName(),
                                              this.getJavaSqlTypeName(),
                                              target.getJavaSqlTypeName()
                                            }
                             );   

               if(true)
               {                   
                  return new DbColumnDifference(this, target, differencePlan, Action.CONVERT);
               }
            }
         }
         
         /*
          * Compare the length of the column with the
          * target length. If the actual length is 
          * greater than the target length, action
          * CONVERT will be returned.
          */
         if(this.getJavaSqlTypeInfo().hasLengthAttribute())
         {
            if(this.getLength() != target.getLength())
            {
               differencePlan.setLengthIsChanged(true);
          
               category.infoT(
                               location,
                               "compareTo: length {1} of column {0} is not equal to length {2} of the target column",
                               new String[] {
                                               this.getName(),
                                               "" + this.getLength(),
                                               "" + target.getLength()
                                            }
                             );   

               if(this.getLength() > target.getLength())
               {     
                  return new DbColumnDifference(this, target, differencePlan, Action.CONVERT);
               }
            }
         }
         
         /*
          * Compare the number of decimals of the column
          * with the target decimals. If the actual number
          * of decimals is greater than the target number,
          * action CONVERT will be returned.
          */
         if(this.getJavaSqlTypeInfo().hasDecimals())
         {
            if(this.getDecimals() != target.getDecimals())
            {
               differencePlan.setDecimalsAreChanged(true);

               category.infoT(
                               location,
                               "compareTo: {1} decimals of column {0} is not equal to {2} decimals of the target column",
                               new String[] {
                                              this.getName(),
                                              "" + this.getDecimals(),
                                              "" + target.getDecimals()
                                            }
                             );   

               if(this.getDecimals() > target.getDecimals())
               {
                  return new DbColumnDifference(this, target, differencePlan, Action.CONVERT);
               }
            }
         }

         /*
          * Compare the scale of the column with the scale
          * of the target column. If the actual scale is
          * greater than the target scale, action CONVERT
          * will be returned.
          */
         if(this.getJavaSqlTypeInfo().hasDecimals())
         {
            if(this.getLength() - this.getDecimals() != target.getLength() - target.getDecimals())
            {


               category.infoT(
                               location,
                               "compareTo: scale {1} of column {0} is not equal to scale {2} of the target column",
                               new String[] {
                                              this.getName(),
                                              "" + (  this.getLength() -   this.getDecimals()),
                                              "" + (target.getLength() - target.getDecimals())
                                            }
                             );   

               if(this.getLength() - this.getDecimals() > target.getLength() - target.getDecimals())
               {     
                  return new DbColumnDifference(this, target, differencePlan, Action.CONVERT);
               }
            }
         }

         /*
          * Compare the nullability of the column with the
          * nullability of the target column. In case of a
          * transition NULL to NOT NULL, all rows containing
          * a NULL value are updated with the default, after
          * that the ALTER TABLE statement can be issued. If
          * there is no default value, action CONVERT will 
          * be returned.
          */
         if(true)
         {
            if(this.isNotNull() != target.isNotNull())
            {
               differencePlan.setNullabilityIsChanged(true);

               category.infoT(
                               location,
                               "compareTo: column {0} and its target column differ in nullability",
                               new String[] {
                                              this.getName()
                                            }
                             );   

               if(target.isNotNull() && target.getDefaultValue() == null)
               {                   
                  return new DbColumnDifference(this, target, differencePlan, Action.CONVERT);
               }
            }
         }

         /*
          * Compare the default value of the column with the
          * default of the target column. This will always
          * result in action ALTER.
          */
         if(this.getDefaultValue() == null)
         {
            if(target.getDefaultValue() != null)
            {
               differencePlan.setDefaultValueIsChanged(true);

               category.infoT(
                               location,
                               "compareTo: default value {1} of column {0} differs from default {2} of the target column",
                               new String[] {
                                              this.getName(),
                                              this.getDefaultValue(),
                                              target.getDefaultValue()
                                            }
                              );
            }
         }
         else
         {
            if(!this.getDefaultValue().equals(target.getDefaultValue()))
            {
               differencePlan.setDefaultValueIsChanged(true);

               category.infoT(
                               location,
                               "compareTo: default value {1} of column {0} differs from default {2} of the target column",
                               new String[] {
                                              this.getName(),
                                              this.getDefaultValue(),
                                              target.getDefaultValue()
                                            }
                              );
            }
         }

         if(differencePlan.somethingIsChanged())
            return new DbColumnDifference(this, target, differencePlan, Action.ALTER);
         else
            return null;
      }
      catch(Exception exception)
      {
         category.errorT(
                          location,
                          "compareTo failed: exception occured while comparing column {0}: {1}",
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
    * Checks whether adding this column to its table
    * is allowed.
    * <p>
    * @return boolean
    *    <code>true</code>, if adding is allowed,
    *    <code>false</code> otherwise.
    */
   public boolean acceptedAdd()
   {
      boolean accepted = false;
      
      try
      {
         location.entering("acceptedAdd");

         accepted = !(this.isNotNull() && (this.getDefaultValue() == null));

         if(!accepted)
         {
            category.errorT(
                             location,
                             "acceptedAdd failed: column {0} is NOT NULL but has no default value",
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
      
      return accepted;
   }

   /**
    * Checks whether dropping this column from its
    * table is allowed.
    * <p>
    * @return boolean
    *    <code>true</code>, if dropping is allowed,
    *    <code>false</code> otherwise.
    */
   public boolean acceptedDrop()
   {
      return true;
   }

   /**
    * Checks whether the length of the column name
    * is allowed.
    * <p>
    * @return boolean
    *    <code>true</code>, if the name length is ok,
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
                             "checkNameLength failed: length of column {0} invalid",
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
                             "checkNameForReservedWord failed: {0} is a reserved word",
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
    * Checks whether the column attributes - type,
    * length and decimals - are ok, e.g. whether
    * the length is too big for the current type.
    * <p>
    * @return boolean 
    *    <code>true</code>, if the parameters are ok,
    *    <code>false</code> otherwise.
    */
   public boolean checkTypeAttributes()
   {
      boolean correct = false;
      
      try
      {
         location.entering("checkTypeAttributes");

         switch(this.getJavaSqlType())
         {
            case java.sql.Types.BIGINT:        correct = !((this.getLength() <                  0) ||
                                                           (this.getLength() >                 19));
                                                 
                                               break;

            case java.sql.Types.BINARY:        correct = !((this.getLength() <                  0) ||
                                                           (this.getLength() >                255));
                                                 
                                               break;

            case java.sql.Types.BLOB:          correct = true;
                                               
      
                                               break;

            case java.sql.Types.CHAR:          correct = !((this.getLength() <                  0) ||
                                                           (this.getLength() >               2000));
                                                 
                                               break;

            case java.sql.Types.CLOB:          correct = true;
                                               
      
                                               break;

            case java.sql.Types.DATE:          correct = true;
      
      
                                               break;

            case java.sql.Types.DECIMAL:       correct = !((this.getLength() <                  0) ||
                                                           (this.getLength() < this.getDecimals()) ||
                                                           (this.getLength() >                 38));
            
                                               break;

            case java.sql.Types.DOUBLE:        correct = true;

            
                                               break;

            case java.sql.Types.FLOAT:         correct = true;

      
                                               break;

            case java.sql.Types.INTEGER:       correct = !((this.getLength() <                  0) ||
                                                           (this.getLength() >                 10));
                                                 
                                               break;

            case java.sql.Types.LONGVARBINARY: correct = !((this.getLength() <                  0) ||
                                                           (this.getLength() >               2000));
                                                 
                                               break;

            case java.sql.Types.NUMERIC:       correct = !((this.getLength() <                  0) ||
                                                           (this.getLength() < this.getDecimals()) ||
                                                           (this.getLength() >                 38));
      
                                               break;
      
            case java.sql.Types.OTHER:         correct = true;
      
      
                                               break;

            case java.sql.Types.SMALLINT:      correct = !((this.getLength() <                  0) ||
                                                           (this.getLength() >                  5));
                                                 
                                               break;

            case java.sql.Types.TIME:          correct = true;
      
      
                                               break;

            case java.sql.Types.TIMESTAMP:     correct = true;
      
                                         
                                               break;

            case java.sql.Types.VARBINARY:     correct = !((this.getLength() <                  0) ||
                                                           (this.getLength() >               2000));
                                                 
                                               break;

            case java.sql.Types.VARCHAR:       correct = !((this.getLength() <                  0) ||
                                                           (this.getLength() >               1333));
                                                 
                                               break;

            default:                           correct = true;
         }

         if(!correct)
         {
            category.errorT(
                             location,
                             "checkTypeAttributes failed: type attributes of column {0} are invalid",
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
}  
