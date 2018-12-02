package com.sap.dictionary.database.sap;

import com.sap.dictionary.database.dbs.*;
import java.text.SimpleDateFormat;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;


/**
 * Title:        Analysis of table and view changes: SAPDB specific classes
 * Description:  SAPDB specific analysis of table and view changes. Tool to deliver SAPDB specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Thomas Elvers
 * @version 1.0
 */

public class DbSapColumn extends DbColumn {

  private static Location loc = Logger.getLocation("sap.DbSapColumn");
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

  public DbSapColumn(DbFactory factory) {
    super(factory);
  }

  public DbSapColumn(DbFactory factory,DbColumn other) {
    super(factory,other);
  }

  public DbSapColumn(DbFactory factory,XmlMap xmlMap) {
    super(factory, xmlMap);
  }

  //Constructor excluding src-Type that means java-Type. Recommended if origin is
  //database which does not know how a database-table's column is used in java
  public DbSapColumn(DbFactory factory, String name, int position, int javaSqlType,
                          String dbType, long length, int decimals ,
                          boolean isNotNull, String defaultValue) {
   
    if ( javaSqlType == java.sql.Types.BIGINT ||
         javaSqlType == java.sql.Types.DOUBLE ||
         javaSqlType == java.sql.Types.REAL ) {
      length = 0;
    }
 
    if ( length > 0 && javaSqlType != java.sql.Types.LONGVARBINARY) {
      if ( ! factory.getJavaSqlTypes().getInfo(javaSqlType).hasLengthAttribute() ) {
        length = 0;
      }
    }
    
    // build default value with prefix and suffix
    if (defaultValue != null) {
      JavaSqlTypeInfo typeInfo = factory.getJavaSqlTypes().getInfo(javaSqlType);
      defaultValue = typeInfo.getDefaultValuePrefix() + defaultValue + typeInfo.getDefaultValueSuffix();
    }
    
    constructorPart(factory, name, position, javaSqlType, dbType, length,
                    decimals,isNotNull, defaultValue);
  }

  public String getDdlTypeClause() throws Exception {
    loc.entering("getDdlTypeClause");
    String clause = "";
    
    if ( DbSapEnvironment.isSpecJ2EEColumn (this.getColumns().getTable().getName(), 
                                            this.getName(), 
                                            this.getJavaSqlTypeName()) ) {
      clause = "VARCHAR("  + this.getLength() + ") BYTE";
    }
    else {
      clause = super.getDdlTypeClause();
    }
    loc.exiting();
    return clause;
  }

  public String getDdlDefaultValueClause() throws Exception {
    loc.entering("getDdlDefaultValueClause");

    try {
      String clause = "";
      int javaSqlType = super.getJavaSqlType();
      JavaSapSqlTypeInfo javaSqlTypeInfo = (JavaSapSqlTypeInfo) super.getJavaSqlTypeInfo();

      if (javaSqlTypeInfo.hasDefaultValue()) {
        String defVal = super.getDefaultValue();

        if (defVal != null) {
          switch (javaSqlTypeInfo.getIntCode()) {
          
            case (java.sql.Types.CHAR) :
            case (java.sql.Types.VARCHAR) :
            case (java.sql.Types.LONGVARCHAR) :
              
              if (defVal.indexOf('\'') >= 0) {
                String defValMod = escapeQuotes(defVal);
                clause = "DEFAULT " + javaSqlTypeInfo.getDefaultValuePrefix() + defValMod +
                          javaSqlTypeInfo.getDefaultValueSuffix();
              } 
              else {
                clause = super.getDdlDefaultValueClause();
              }
              break;
              
            case (java.sql.Types.DATE) :
            case (java.sql.Types.TIME) :
            case (java.sql.Types.TIMESTAMP) : 

              if (defVal.equalsIgnoreCase("DATE") ||
                  defVal.equalsIgnoreCase("TIME") ||
                  defVal.equalsIgnoreCase("TIMESTAMP") ) {
                // get current date or time
                clause = "DEFAULT " + defVal.toUpperCase();
              }
              else {  
                SimpleDateFormat orgFormatter = (SimpleDateFormat) javaSqlTypeInfo.getFormatterForDefaultString();
                SimpleDateFormat myFormatter  = (SimpleDateFormat) javaSqlTypeInfo.getTargetFormatterForDefaultString();
               
                try {
                  defVal = myFormatter.format(orgFormatter.parse(defVal));
                } catch (Exception ex) {
                  Object[] arguments = {ex.toString(), defVal, orgFormatter.toPattern(), myFormatter.toPattern()};
                  cat.errorT(loc, "Exception caught reformatting default value:\n" +
                             "  {0}\n" +
                             "  Failed to convert {1} from {2} to {3}.", arguments);
                  loc.exiting();
                  throw JddException.createInstance(ex);
                }
              
                clause = "DEFAULT " + javaSqlTypeInfo.getDefaultValuePrefix() + defVal + 
                         javaSqlTypeInfo.getDefaultValueSuffix();   
              }
              break;
              
            default:
              clause = super.getDdlDefaultValueClause();
              break;
          }
        }
      }
      loc.exiting();
      return clause;
    }
    catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "getDdlDefaultValueClause failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance(ex);
    }
  }

  public String getDdlClause() throws Exception {
    loc.entering("getDdlClause");
    String clause = "";
    clause = super.getDdlClause();
    loc.exiting();
    return clause;
  }
  
  public DbObjectSqlStatements getDdlStatementsForLongVarbinary(String name) {
    loc.entering("getDdlStatementsForLongVarinary");
    DbObjectSqlStatements dbObjectSqlStatements = new DbObjectSqlStatements(name);
    DbSqlStatement        dbSqlStatement        = new DbSqlStatement();
    dbSqlStatement.addLine(this.getDdlLongVarbinaryClause(name));
    dbObjectSqlStatements.add(dbSqlStatement);
    loc.exiting();
    return dbObjectSqlStatements;
  }

  public String getDdlLongVarbinaryClause(String tabName) {
    loc.entering("getDdlLongVarbinbaryClause");
    String clause = "COMMENT ON COLUMN " + "\"" + tabName + "\".\"" + this.getName() + "\" " +
                    "IS '" + this.getJavaSqlTypeName() + " (" + this.getLength() + ")'" ;
    loc.exiting();
    return clause;
  }
    
  protected DbColumnDifference comparePartTo(DbColumn target) throws Exception {
    loc.entering("compareTo");
    DbColumnDifference colDiff  = null;
    DbSapColumn orgCol          = this;
    DbSapColumn targetCol       = null;
    DbColumnDifferencePlan plan = new DbColumnDifferencePlan();
    boolean hasToConvert        = false;
    
    targetCol = (DbSapColumn) target;

    // check type
    int orgType    = orgCol.getJavaSqlType();
    int targetType = targetCol.getJavaSqlType();
    
    // following java SQL types will be used as the same database type
    // REAL, FLOAT, DOUBLE      -> FLOAT
    // VARCHAR, LONGVARCHAR     -> VARCHAR
    // BINARY, VARBINARY        -> VARCHAR BYTE
    // BLOB, LONGVARBINARY      -> LONG BYTE
    
    if ( (orgType != targetType) &&
        !( ((orgType == java.sql.Types.REAL) || 
            (orgType == java.sql.Types.FLOAT) || 
            (orgType == java.sql.Types.DOUBLE)) &&
           ((targetType == java.sql.Types.REAL) || 
            (targetType == java.sql.Types.FLOAT) || 
            (targetType == java.sql.Types.DOUBLE)) ) && 
        !( ((orgType == java.sql.Types.VARCHAR) && (targetType == java.sql.Types.LONGVARCHAR)) ) &&
        !( ((orgType == java.sql.Types.LONGVARCHAR) && (targetType == java.sql.Types.VARCHAR)) ) &&
        !( ((orgType == java.sql.Types.BINARY) && (targetType == java.sql.Types.VARBINARY)) ) &&
        !( ((orgType == java.sql.Types.VARBINARY) && (targetType == java.sql.Types.BINARY)) ) && 
        !( ((orgType == java.sql.Types.VARBINARY) && (targetType == java.sql.Types.LONGVARBINARY) && 
            DbSapEnvironment.isSpecJ2EEColumn (this.getColumns().getTable().getName(), this.getName(), null)) ) &&
        !( ((orgType == java.sql.Types.BLOB) && (targetType == java.sql.Types.LONGVARBINARY)) )) {
      plan.setTypeIsChanged(true);
      Object[] arguments = {getName(), orgCol.getJavaSqlTypeName(), targetCol.getJavaSqlTypeName()};
      
      if (! (orgType == java.sql.Types.SMALLINT && targetType == java.sql.Types.INTEGER ) ) {
        hasToConvert = true;
        cat.infoT(loc, "compareTo ({0}): CONVERT: original type {1} incompatible to target type {2}", arguments);
      }
      else {
        cat.infoT(loc, "compareTo ({0}): ALTER type from {1} to {2}", arguments);
      }
    }

    // check special J2EE column (only if the database connection exists   
    if (hasToConvert == false) {
      if (this.getColumns().getTable().getDbFactory().getConnection() != null) {
      	String dbTypeOfOrgCol = orgCol.getDbType();
        if (dbTypeOfOrgCol != null && dbTypeOfOrgCol.equals("LONG BYTE") &&
            DbSapEnvironment.isSpecJ2EEColumn (this.getColumns().getTable().getName(), 
                                               this.getName(), 
                                               this.getJavaSqlTypeName()) ) {
          plan.setTypeIsChanged(true);
          hasToConvert = true;
          Object[] arguments = {getName(), orgCol.getDbType(), new Long(targetCol.getLength())};
          cat.infoT(loc, "compareTo ({0}): CONVERT: original type {1} incompatible to target type VARCHAR({2}) BYTE", arguments);
        }
      }
    }

    // compare length
    if (hasToConvert == false) {
      if (orgCol.getJavaSqlTypeInfo().hasLengthAttribute() || 
          orgType == java.sql.Types.LONGVARBINARY ||
          (orgType == java.sql.Types.BLOB && targetType == java.sql.Types.LONGVARBINARY)) {
        long orgLength    = orgCol.getLength();
        long targetLength = targetCol.getLength();
      
        if (orgLength == 0)
          orgLength = orgCol.getJavaSqlTypeInfo().getDdlDefaultLength();
        if (targetLength == 0)
          targetLength = targetCol.getJavaSqlTypeInfo().getDdlDefaultLength();

        if (orgLength != targetLength) {
          plan.setLengthIsChanged(true);
          Object[] arguments = {getName(), new Long(orgLength), new Long(targetLength)};
        
          if (orgLength > targetLength) {
            hasToConvert = true;
            cat.infoT(loc, "compareTo ({0}): CONVERT: original length {1} greater than target length {2}", arguments);
          }
          else {
            cat.infoT(loc, "compareTo ({0}): ALTER length from {1} to {2}", arguments);
          }
        }
      }
    }

    // Compare scale and precision
    if (hasToConvert == false && 
        orgCol.getJavaSqlTypeInfo().hasDecimals()) {
      if ( orgCol.getDecimals() != targetCol.getDecimals() ) {
        plan.setDecimalsAreChanged(true);
        if ( orgCol.getDecimals() > targetCol.getDecimals() ) {
          hasToConvert = true;
          Object[] arguments = {getName(), new Long(orgCol.getDecimals()), new Long(targetCol.getDecimals())};
          cat.infoT(loc, "compareTo ({0}): CONVERT: original decimals {1} greater than target decimals {2}", arguments);
        }
        else {
          long precOrigin = orgCol.getLength() - orgCol.getDecimals();
          long precTarget = targetCol.getLength() - targetCol.getDecimals();
          if ( precOrigin > precTarget ) {
            hasToConvert = true;
            Object[] arguments = {getName(), new Long(precOrigin), new Long(precTarget)};
            cat.infoT(loc, "compareTo ({0}): CONVERT: original precision {1} greater than target precision {2}", arguments);
          }
          else {
            Object[] arguments = {getName(), new Long(orgCol.getDecimals()), new Long(precOrigin),
                                  new Long(targetCol.getDecimals()), new Long(precTarget)};
            cat.infoT(loc, "compareTo ({0}): ALTER decimals from ({1}.{2}) to ({3}.{4})", arguments);
          }
        }
      }
    }
    
    // compare nullability
    if (hasToConvert == false && 
        orgCol.isNotNull() != targetCol.isNotNull()) {
      plan.setNullabilityIsChanged(true);
      Object[] arguments = {getName()};
      if (targetCol.isNotNull() && targetCol.getDefaultValue() == null) {
        // NULL -> NOT NULL without a default : action CONVERT is returned.
        hasToConvert = true;
        cat.infoT(loc, "compareTo ({0}): CONVERT: NULL to NOT NULL without a default value", arguments);
      }
      else { 
        if (targetCol.isNotNull())
          cat.infoT(loc, "compareTo ({0}): ALTER column to NOT NULL", arguments);
        else
          cat.infoT(loc, "compareTo ({0}): ALTER column to NULL", arguments);
      }
    }

    // compare default value
    if (hasToConvert == false) {
      if (orgCol.getDefaultValue() == null) {
        if (targetCol.getDefaultValue() != null) {
          plan.setDefaultValueIsChanged(true);
          Object[] arguments = {getName(), targetCol.getDefaultValue()};
          cat.infoT(loc, "compareTo ({0}): ALTER default to {1}", arguments);
        }
      }
      else if (targetCol.getDefaultValue() == null) {
        plan.setDefaultValueIsChanged(true);
        cat.infoT(loc, "compareTo ({0}): ALTER default to NULL", new Object[] {getName()} );
      }
      else {
        String orgDefVal    = orgCol.getDefaultValue();
        String targetDefVal = targetCol.getDefaultValue();
        boolean differs = false;

        switch (targetType) {
          case (java.sql.Types.SMALLINT):
          case (java.sql.Types.INTEGER):
          case (java.sql.Types.BIGINT):
            long targetIntVal = Long.valueOf(targetDefVal).longValue();
            long orgIntVal    = Long.valueOf(orgDefVal).longValue();
            differs = targetIntVal != orgIntVal;
            break;

          case (java.sql.Types.DECIMAL):
          case (java.sql.Types.FLOAT):
          case (java.sql.Types.DOUBLE):
          case (java.sql.Types.REAL):
            double targetFloatVal = Double.valueOf(targetDefVal).doubleValue();
            double orgFloatVal    = Double.valueOf(orgDefVal).doubleValue();
            differs = targetFloatVal != orgFloatVal;
            break;

          case (java.sql.Types.TIME):
          case (java.sql.Types.DATE):
          case (java.sql.Types.TIMESTAMP):
            if (orgDefVal.equalsIgnoreCase(targetDefVal) == false) {
              SimpleDateFormat orgFormatter = (SimpleDateFormat) targetCol.getJavaSqlTypeInfo().getFormatterForDefaultString();
              SimpleDateFormat myFormatter  = (SimpleDateFormat)((JavaSapSqlTypeInfo)targetCol.getJavaSqlTypeInfo()).getTargetFormatterForDefaultString();
          
              try {
                 targetDefVal = myFormatter.format(orgFormatter.parse(targetDefVal));
              } catch (Exception ex) {
                Object[] arguments = {ex.toString(), targetDefVal, orgFormatter.toPattern(), myFormatter.toPattern()};
                cat.errorT(loc, "Exception caught reformatting default value:\n" +
                           "  {0}\n" +
                           "  Failed to convert {1} from {2} to {3}.", arguments);
                loc.exiting();
                throw JddException.createInstance(ex);
              }
            
              if (orgDefVal.equals(targetDefVal) == false)
                differs = true;
            }
            break;

          default:
            if (orgDefVal.equals(targetDefVal) == false)
              differs = true;
            break;
        }

        if (differs) {
          plan.setDefaultValueIsChanged(true);
          Object[] arguments = {getName(), targetCol.getDefaultValue()};
          cat.infoT(loc, "compareTo ({0}): ALTER default to {1}", arguments);
        }
      }
    }

    if (hasToConvert == true) {
      loc.exiting();
      return (new DbColumnDifference(this, target, plan, Action.CONVERT));
    }
    else if (plan.somethingIsChanged()) {
      loc.exiting();
      return (new DbColumnDifference(this, target, plan, Action.ALTER));
    }
    else {
      //cat.infoT(loc, "compareTo ({0}) OK", new Object[] {getName()});
      loc.exiting();
      return null;
    }
  }

  public boolean acceptedAdd()
  {
    if (!(isNotNull() == true && getDefaultValue() == null)) 
      return true;
 
    Object[] arguments = {getName()};
    cat.infoT(loc,"Add column {0} not accepted: NOT NULL is specified but no DEFAULT value is set", arguments );
     
    return false;
  }

  public boolean acceptedDrop() {
    return false;
  }

  /**
   ** Check the column's name according to its length
   ** @return true - if name-length is o.k
   **/
  public boolean checkNameLength() {
    loc.entering("checkNameLength");
    int nameLen = this.getName().length();
    int maxLen = DbSapEnvironment.MaxNameLength();

    if (nameLen > 0 && nameLen <= maxLen) {
      loc.exiting();
      return true;
    }
    else {
      Object[] arguments = {this.getName(), new Integer(nameLen), new Integer(maxLen)};
      cat.errorT(loc, "checkNameLength {0}: length {1} invalid (allowed range [1..{2}])", arguments);
      loc.exiting();
      return false;
    }
  }

  /**
   **  Checks if column-name is a reserved word
   **  @return true - if column-name has no conflict with reserved words,
   **                    false otherwise
   **/
  public boolean checkNameForReservedWord() {
    loc.entering("checkNameForReservedWord");
    boolean isReserved = DbSapEnvironment.isReservedWord(this.getName());

    if (isReserved == true) {
      Object[] arguments = {this.getName()};
      cat.errorT(loc, "{0} is a reserved word", arguments);
    }
    loc.exiting();
    return (isReserved == false);
  }

 /**
  ** Check the columns's attributes: type, length and decimals, e.g
  ** if length is to big for current type
  ** @return true - if name-length is o.k
  **/
  public boolean checkTypeAttributes() {
    loc.entering("checkTypeAttributes");
    boolean check = true;
    long len;
    long maxLen;

    switch (this.getJavaSqlType()) {
      case java.sql.Types.DECIMAL:
      case java.sql.Types.NUMERIC:
        len = this.getLength();
        maxLen = DbSapEnvironment.MaxDecimalLength();

        if (len < 0 || len > maxLen) {
          check = false;
          Object[] arguments = {this.getName(), new Long(len), new Long(maxLen)};
          cat.errorT(loc, "checkTypeAttributes {0}: a length of {1} is invalid for decimal-fields (allowed range [1..{2}])", arguments);
        }
        if (len < this.getDecimals()) {
          check = false;
          Object[] arguments = {this.getName(), new Integer(this.getDecimals()), new Long(len)};
          cat.errorT(loc, "checkTypeAttributes {0}: scale {1} is greater than precision {2}", arguments);
        }
        break;

      case java.sql.Types.VARBINARY:
      case java.sql.Types.BINARY:
        len = this.getLength();
        maxLen = DbSapEnvironment.MaxBinaryLength();

        if (len < 0 || len > maxLen) {
          check = false;
          Object[] arguments = {this.getName(), new Long(len), new Long(maxLen)};
          cat.errorT(loc, "checkTypeAttributes {0}: length of {1} is out of range for binary-fields (allowed range [1..{2}])", arguments);
        }
        break;

      case java.sql.Types.VARCHAR:
      case java.sql.Types.CHAR:
        len = this.getLength();
        maxLen = DbSapEnvironment.MaxCharacterLength();

        if (len < 0 || len > maxLen) {
          check = false;
          Object[] arguments = {this.getName(), new Long(len), new Long(maxLen)};
          cat.errorT(loc, "checkTypeAttributes {0}: length of {1} is out of range for character-fields (allowed range [1..{2}])", arguments);
        }
        break;
    }

    loc.exiting();
    return check;
  }
  
  /**
   * If the DbColumn object represents the database state (dbType != null)
   * and the dbType (respecting length and decimals) can be mapped to 2 or more
   * different jdbc types
   * the method returns all this alternatives.
   * otherwise returns null.
   * The porting should overwrite this method only if necessary:
   * @return  the array of jdbc types or null
   */
  public int[] getJavaSqlTypeAlternatives() { // overload at porting if necessary
    
    int [] sqltype = null; 
    
    if ((getDbType().equals("FIXED") || getDbType().equals("DECIMAL")) && getLength() == 19) {
      sqltype = new int [2];
      sqltype[0] = java.sql.Types.BIGINT;
      sqltype[1] = java.sql.Types.DECIMAL;
    }
    else if (getDbType().equals("LONG BYTE") || getDbType().equals("LONG RAW")) {
      sqltype = new int [2];
      sqltype[0] = java.sql.Types.BLOB;
      sqltype[1] = java.sql.Types.LONGVARBINARY;      
    }
 
    return sqltype;
  }

  
  private String escapeQuotes (String str) {
    String escapeSign = "'";
    int length = str.length();
    char[] charArray = str.toCharArray();
    
    StringBuffer strBuffer = new StringBuffer(length);

    for (int i = 0; i < length; i++) {
 
      if (charArray[i] == '\'') {
        strBuffer.append(escapeSign + charArray[i]);
        //strBuffer.append(charArray[i]);
      } else {
        strBuffer.append(charArray[i]);
      }
    }
    return strBuffer.toString();
  }
}
