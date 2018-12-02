package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.db4.DbDb4HexString;
import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumnDifference;
import com.sap.dictionary.database.dbs.DbColumnDifferencePlan;
import com.sap.dictionary.database.dbs.DbDeploymentInfo;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import java.sql.Types;
import java.text.SimpleDateFormat;

/**
 * Title:        DbDb4Column
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */

       
 public class DbDb4Column extends DbColumn {


    private static final Location loc = Logger.getLocation("db4.DbDb4Column");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,
                                                             Logger.CATEGORY_NAME);
    

    //----------------
    //  constructors  ----------------------------------
    //----------------

    public DbDb4Column() {
        super();
    }

    public DbDb4Column(DbFactory factory) {
        super(factory);
    }
    
    //Empty constructor
    public DbDb4Column(DbFactory factory, DbColumn other) {
        super(factory, other);
    }

    //Constructor including src-Type that means java-Type
    public DbDb4Column(DbFactory factory, XmlMap xmlMap) {
        super(factory, xmlMap);
    }

    //Constructor excluding src-Type that means java-Type. Recommended if origin is
    //database which does not know how a database-table's column is used in java
    public DbDb4Column(DbFactory factory,
                        String name,
                        int position,
                        int javaSqlType,
                        String dbType,
                        long length,
                        int decimals,
                        boolean isNotNull,
                        String defaultValue) {
        super( factory, 
                name, 
                position, 
                javaSqlType, 
                dbType, 
                length, 
                decimals,
                isNotNull,
                defaultValue);
    }
    


    //------------------
    //  public methods  ----------------------------------
    //------------------

/*    public String getTypeClauseForDdl() throws Exception {
        //
        // super call sufficient:
        //    <DdlName> (<ByteFactor> * <length> [, <decimals>]) <DdlSuffix> 
        //
        String clause = super.getDdlTypeClause();
        return clause;
    }

*/

    public String getDdlDefaultValueClause() throws Exception  {
        loc.entering(cat, "getDdlDefaultValueClause()");
        String clause = "";
        String orgDefaultValue = this.getDefaultValue();
        String trgDefaultValue = orgDefaultValue;
        JavaDb4SqlTypeInfo javaSqlTypeInfo = (JavaDb4SqlTypeInfo) this.getJavaSqlTypeInfo();

        if (javaSqlTypeInfo.hasDefaultValue() && (orgDefaultValue != null)) {
            if (   this.getJavaSqlType() == Types.TIME
                || this.getJavaSqlType() == Types.TIMESTAMP
                || this.getJavaSqlType() == Types.DATE ) {
                SimpleDateFormat orgFormatter = (SimpleDateFormat) 
                                                javaSqlTypeInfo.getFormatterForDefaultString();
                SimpleDateFormat trgFormatter = (SimpleDateFormat) 
                                                javaSqlTypeInfo.getTargetFormatterForDefaultString();
                try {
                    trgDefaultValue = trgFormatter.format(orgFormatter.parse(trgDefaultValue));
                    loc.debugT(cat, "Default value ''{0}'' reformatted to ''{1}''.",
                               new Object[] {orgDefaultValue, trgDefaultValue});
                } catch (Exception e) {       //$JL-EXC$
                    cat.errorT(loc, "Exception caught reformatting default value " +
                                    "from ''{0}'' to ''{1}'': ''{2}''.",
                                    new Object[] {orgDefaultValue, trgDefaultValue,
                                                  e.toString()});
                    loc.exiting();
                    throw JddException.createInstance(e);
                }
            }
            
            /*
             * Character type default values must be converted into hex literals,
             * since DB4 uses EBCDIC system catalogs
             */
            if (    this.getJavaSqlType() == Types.VARCHAR
                 || this.getJavaSqlType() == Types.LONGVARCHAR
                 || this.getJavaSqlType() == Types.CLOB ) {
                    
                /*
                 * Hex delimiters UX'' are treated as being part of the default value, 
                 * while JDDIC thinks there are not pre/suffixes for string type
                 * default values (refer to Db4JavaSqlTypeInfo). Anything else 
                 * would be likely to end up in a big mess...
                 */
                trgDefaultValue = "UX'" + 
                                new DbDb4HexString(trgDefaultValue, null).getHexString()
                                + "'";
                loc.debugT(cat, "Default value ''{0}'' reformatted to ''{1}''.",
                           new Object[] {orgDefaultValue, trgDefaultValue});
            } 

            /*
             * BINARY default values are provided hexadecimal, uppercase, 
             * and with correct length (2 * field length [char]) - noop.
             */
            if (this.getJavaSqlType() == Types.BINARY) {
                trgDefaultValue = orgDefaultValue;
            }
            
            clause = "DEFAULT " 
                        + javaSqlTypeInfo.getDefaultValuePrefix() 
                        + trgDefaultValue
                        + javaSqlTypeInfo.getDefaultValueSuffix();
        }
        loc.exiting();
        return clause;
    }


    // Taken from .dbs to get Exception in case of empty name.
    public String getDdlClause() throws Exception {
        loc.entering(cat, "getDdlClause()");
        String columnName = null;
        if ((columnName = this.getName()) == null) {
            cat.errorT(loc, "Empty column name.");
            loc.exiting();
            throw new JddException(ExType.OTHER, "Empty column name.");
        }
        columnName = columnName.trim().toUpperCase();
        
        String clause = "\"" + columnName + "\"" + " " + getDdlTypeClause() + " " + 
              getDdlDefaultValueClause() + " ";
        if (this.isNotNull()) {
            clause  = clause + "NOT NULL";
        }
        return clause;
  }

    /**
     *  Compares this column to a target column
     *  @param target               the column's target version 
     *  @return The differences about datatypes, length, decimals, 
     *           notNull, defaultValue,   
     * */
    protected DbColumnDifference comparePartTo (DbColumn target) throws Exception {
        /*
         * DR: Consider implementing early exit when known that CONVERT 
         * will be necessary.
         */
        loc.entering(cat, "compareTo({0}) called.", new Object[] {target.getName()});   
        DbDb4Column origin = this;
        DbColumnDifferencePlan plan = new DbColumnDifferencePlan();
        int action = 0; // 0 == NOTHING, 1 == ALTER; 2 == CONVERT
        int originalType = origin.getJavaSqlType();
        int targetType = target.getJavaSqlType();

        // --- Compare types ---
        if (   (originalType != targetType)  

            // Don't differentiate between LONG and non LONG types
            && !(   (  (  (originalType == Types.LONGVARCHAR) 
                       || (originalType == Types.VARCHAR))
                    && (  (targetType == Types.LONGVARCHAR) 
                       || (targetType == Types.VARCHAR)) )  
                
                 || (  (  (originalType == Types.LONGVARBINARY) 
                       || (originalType == Types.VARBINARY))
                    && (  (targetType == Types.LONGVARBINARY) 
                       || (targetType == Types.VARBINARY)) )  )
            ) {
                        
            // Allowed: SMALLINT->INT->BIGINT and REAL -> DOUBLE
            if ((originalType == Types.SMALLINT 
                    &&  ((targetType == Types.INTEGER) 
                          || (targetType == Types.BIGINT)))
                ||  (originalType == Types.INTEGER 
                    && targetType == Types.BIGINT) 
                ||  (originalType == Types.REAL
                    && targetType == Types.DOUBLE)) {
                action = raiseActionLevel(action, 1);   // ALTER
                plan.setTypeIsChanged(true);
                
            } else {
                action = raiseActionLevel(action, 2);   // CONVERT
                plan.setTypeIsChanged(true);
                cat.infoT(loc, "Column {0}: incompatible type change requires conversion: " + 
                               "Source JDBC type {1}, target JDBC type {2}.", 
                          new Object[] {this.getName(), 
                                        new Integer(originalType), 
                                        new Integer(targetType)});
            }
        } else {
            plan.setTypeIsChanged(false);
        }
        
        // --- Compare default values ---
        // Note that hasDefaultValue()==true but getDefaultValues==null equals
        // non-existing default value
        String orgDefault = origin.getDefaultValue();
        String trgDefault = target.getDefaultValue();
        boolean orgHasDef = (origin.getJavaSqlTypeInfo().hasDefaultValue()
                                && (orgDefault != null));
        boolean trgHasDef = (target.getJavaSqlTypeInfo().hasDefaultValue()
                                && (trgDefault != null));
                                
        if ((!orgHasDef && !trgHasDef)
            || (orgHasDef && trgHasDef && orgDefault.equals(trgDefault)) ){
                // Nothing to do
                plan.setDefaultValueIsChanged(false);
        } else {
            plan.setDefaultValueIsChanged(true);
            if ((!orgHasDef && trgHasDef)
                || (orgHasDef && !trgHasDef)
                || (orgHasDef && trgHasDef && !orgDefault.equals(trgDefault)) ) {
                action = raiseActionLevel(action, 1);       // ALTER
            } else {
                action = raiseActionLevel(action, 2);       // CONVERT
                cat.infoT(loc, "Column {0}: "+
                               "default value change that requires conversion: " + 
                               "Source def.: ''{1}'' (hasDefaultValue {2}), " + 
                               "target def.: ''{3}'' (hasDefaultValue {4}), ",
                          new Object[] {this.getName(), 
                                        orgDefault, new Boolean(orgHasDef),
                                        trgDefault, new Boolean(trgHasDef)});
            }
        }
        
        // --- Compare lengthes ---
        long orgLength = origin.getLengthOrDdlDefaultLength();
        long trgLength = ((DbDb4Column)target).getLengthOrDdlDefaultLength();
        if (orgLength == trgLength) {
            // action = raiseActionLevel(action, 0);        // NOTHING
            plan.setLengthIsChanged(false);
        } else {
            plan.setLengthIsChanged(true);
            if ((originalType == targetType)
                /*
                 * Those are the cases where we really want to alter.
                 * 
                 * According to JDBC meeting 04/10/2003, length extensions 
                 * to type BINARY must lead to conversions. Open SQL will
                 * do correct padding at INSERT.
                 * (Note: theoretically, this is only necessary for the 
                 * 'old' type CHAR FOR BIT DATA which does blank padding 
                 * up to the field length.)
                 */
                &&  (orgLength < trgLength)
                &&  (   targetType == Types.VARCHAR
                    ||  targetType == Types.LONGVARCHAR
                    ||  targetType == Types.VARBINARY
                    ||  targetType == Types.LONGVARBINARY
                    ||  targetType == Types.CLOB
                    ||  targetType == Types.BLOB
                    ||  targetType == Types.DECIMAL) ) {
                action = raiseActionLevel(action, 1);       // ALTER
            } else {
                action = raiseActionLevel(action, 2);       // CONVERT
                cat.infoT(loc, "Column {0}: " + 
                               "target column shorter than source column, " + 
                               "conversion required. " + 
                               "Source length: {1}, target length: {2}",
                          new Object[] {this.getName(), 
                                        new Long(orgLength), new Long(trgLength)});
            }
        }
        
        // --- Compare decimals ---
        int orgDec = origin.getDecimals();
        int trgDec = target.getDecimals();
        if (orgDec != trgDec) {
            plan.setDecimalsAreChanged(true);
            // Check if length was sufficiently increased to not truncate
            long orgLen = origin.getLengthOrDdlDefaultLength();
            long trgLen = ((DbDb4Column)target).getLengthOrDdlDefaultLength();
            if (   (orgDec < trgDec)
                && (trgDec <= trgLen) // ensure the differences are not-negative
                && ((orgLen - orgDec) <= (trgLen - trgDec)) ) {
                action = raiseActionLevel(action, 1);   // ALTER
            } else {
                action = raiseActionLevel(action, 2);       // CONVERT
                cat.infoT(loc, "Column {0}: " +
                               "length/decimal change leading to data loss " +                               "requires conversion. " + 
                               "Source: DECIMAL ({1},{2}), target: DECIMAL ({3},{4}).", 
                          new Object[] {this.getName(),
                                        new Long(orgLen), new Long(orgDec),  
                                        new Long(trgLen), new Long(trgDec)});
            }
        } else {
            plan.setDecimalsAreChanged(false);
        }
        
        // --- Compare nullability ---
        if (origin.isNotNull() ^ target.isNotNull()) {
            plan.setNullabilityIsChanged(true);
            if (origin.isNotNull() && !target.isNotNull()) {
                action = raiseActionLevel(action, 1);       // ALTER
            } else {
                // Null values would lead to an error
                action = raiseActionLevel(action, 2);       // CONVERT
                cat.infoT(loc, "Column {0}: " +
                               "change from ''NULL allowed'' to ''NOT NULL'' " + 
                               "requires conversion.",
                               new Object[] {this.getName()});
            }
        } else {
            plan.setNullabilityIsChanged(false);
        }

        // --- Compare positions ---
        if (origin.getPosition() != target.getPosition()) {
            plan.setPositionIsChanged(true);
            DbDeploymentInfo info = target.getColumns().getTable().getDeploymentInfo();
            if (info == null || info.positionIsRelevant()) {
                // Hmm, here we rely on legal call sequences: 
                // DbTable.positionIsRelevant is false per default... 
                // (At least if called from DbTable.compareTo() column positions
                // should match already)
                action = raiseActionLevel(action, 2);       // CONVERT
                cat.infoT(loc, "Column {0}: " +
                                "column position changed, while position " + 
                                "is relevant; requires conversion. " + 
                                "Source pos.: {1}, target pos.: {2}. " + 
                                "DeploymentInfo: {3}", 
                            new Object[] {this.getName(),
                                          new Integer(origin.getPosition()), 
                                          new Integer(target.getPosition()), 
                                          info});
            }
        } else {
            plan.setPositionIsChanged(false);
        }

        // --- Create the difference object for the column ---
        String actionDescr = "";
        switch (action) {
            case 0:
                actionDescr = "NOTHING";
                // This can be null if nothing is to do.
                loc.debugT(cat, "{0} and {1} are the same. - Return null.",
                           new Object[] {this.getName(), target.getName()});
                loc.exiting();
                return null;
            case 1:
                actionDescr = "ALTER";
                loc.debugT(cat, "Column {0} changed:\n" +
                               "actionDescr = {1}, \n" +
                               "source: {2}, \n" +
                               "target: {3}, \n" +
                               "plan: {4}", 
                           new Object[] {this.getName(), 
                                         actionDescr,
                                         this, 
                                         target, 
                                         plan} );
                break;
            case 2:
                actionDescr = "CONVERT";
                cat.infoT(loc, "Column {0} changed:\n" +
                               "actionDescr = {1}, \n" +
                               "source: {2}, \n" +
                               "target: {3}, \n" +
                               "plan: {4}", 
                           new Object[] {this.getName(), 
                                         actionDescr,
                                         this, 
                                         target, 
                                         plan} );
                break;
            default :
                loc.warningT("Unknown action: {0}", new Object[] {new Integer(action)});
                break;
        }
        
        /*
         * origin == null => add column to "drop list"
         * target == null => add column to "create list"
         * diff   != null => add column to "modify list"
         */
        DbColumnDifference diff
                = new DbColumnDifference(   origin,
                                            target,
                                            plan,   
                                            Action.getInstance(actionDescr));
        loc.exiting();                                          
        return diff;
    }

    /**
     *  Analyses the behaviour in case a column has to be added
     *  @return true if a column can be added via a ddl-statement
     * */
    public boolean acceptedAdd() {
        boolean addOk = true;
        
        // ------------------------------------------------------------------- //
        // ALTER TABLE cannot append NOT NULL column w/o a default value being //
        // specified: existing rows must be provided with a default value.     //
        // acceptedAdd() must presume the worst case: table contains data.     // 
        // ------------------------------------------------------------------- //
        if (this.isNotNull() && this.getDefaultValue() == null )
        {
            addOk = false;
            cat.infoT(loc, "Column {0}: " +
                           "adding NOT NULL column w/o default value " +
                           "requires conversion " + 
                           "(Default: ''{1}'', isNotNull: ''{2}'').", 
                      new Object[] {this.getName(), 
                                    this.getDefaultValue(), 
                                    new Boolean(this.isNotNull())});
        } else {
            DbDb4Environment.traceCheckResult(true, addOk, cat, loc, 
                "acceptedAdd() returns {0}.", 
                new Object[] {new Boolean(addOk)});
        }
        return addOk;
    }
    
    /**
     *  Analyses the behaviour in case a column has to be dropped
     *  @return true if a column can be dropped via a ddl-statement
     * */
    public boolean acceptedDrop() {
        
        /*
         * Requires use of system reply list (entry "CPA32B2", "I")
         * and DB job being set to *SYSRPYL (--> JDBC exit program)
         */ 
        boolean dropOk = true;

        /*
         *  No restrictions on Db4 in case of "DROP COLUMN CASCADE"
         *  In case of "RESTRICT" only if no dependencies
         */
        DbDb4Environment.traceCheckResult(true, dropOk, cat, loc, 
                                "acceptedDrop() returns {0}.", 
                                new Object[] {new Boolean(dropOk)});
        return dropOk;
    }
    
    /**
     *  Check the column's name according to its length  
     *  @return true - if name-length is o.k
     **/  
    public boolean checkNameLength() {
        boolean lengthOk = false;
        lengthOk = (this.getName().trim().length() 
                    <= DbDb4Environment.getMaxColumnNameLength())
                        ? true : false;
        DbDb4Environment.traceCheckResult(true, lengthOk, cat, loc, 
                                "checkNameLength() returns {0}.", 
                                new Object[] {new Boolean(lengthOk)});
        return lengthOk;
    }
  
    /**
     *  Check the columns's attributes: type, length and decimals, e.g
     *  if length is to big for current type   
     *  @return true - if name-length is o.k
     **/  
    public boolean checkTypeAttributes() {
        loc.entering(cat, "checkTypeAttributes()");
        boolean attributesOk = true;
        long length = this.getLength();
        int decimals = this.getDecimals();

        switch (this.getJavaSqlType()) {
            /*
             * This check reflects the db side, which means that we do not 
             * scan for JDDI but only for DB4 restrictions
             */
            case Types.VARCHAR :
            case Types.LONGVARCHAR :
                if (length < 0 || 
                    length > (DbDb4Environment.getMaxVarcharLengthBytes() / 2)) {
                    attributesOk = false;
                    }
                break;
            case Types.VARBINARY :
            case Types.LONGVARBINARY :
                /*
                 * In accordance with Open SQL, we tolerate short VARBINARY fields with
                 * varying length semantics. Who - for whatever reason - managed to bypass
                 * JDDIC checks would have to live with the wrong comparison/sorting 
                 * semantics until the real VARBINARY type comes out...!
                 */
                if (length < 0 || 
                    length > (DbDb4Environment.getMaxVarbinaryLength())) {
                    attributesOk = false;
                    }
                break;
            case Types.BINARY :
                if (length < 0 || 
                    length > (DbDb4Environment.getMaxBinaryLength())) {
                    attributesOk = false;
                    }
                break;
            case Types.CLOB :
                if (length < 0 || 
                    length > (DbDb4Environment.getMaxClobLengthBytes() / 2)) {
                    attributesOk = false;
                    }
                break;
            case Types.BLOB :
                if (length < 0 || 
                    length > (DbDb4Environment.getMaxBlobLength())) {
                    attributesOk = false;
                    }
                break;
            case Types.SMALLINT :
            case Types.INTEGER :
            case Types.BIGINT :
            case Types.REAL:
            case Types.DOUBLE :
            case Types.DATE :
            case Types.TIME :
            case Types.TIMESTAMP :
                break;
            case Types.DECIMAL :
                decimals = this.getDecimals();
                if (length < 1 
                        || length > DbDb4Environment.getMaxDecimalLengthChars()
                        || decimals < 0 
                        || decimals > (DbDb4Environment.getMaxDecimalLengthChars() - 1)) {
                    attributesOk = false;
                }
                break;
            default :
                cat.errorT(loc, "Unknown SQL type {0}.", 
                                new Object[] {new Integer(this.getJavaSqlType())});
                attributesOk = false;
                break;
        }
        DbDb4Environment.traceCheckResult(true, attributesOk, cat, loc, 
                        "checkAttributes returns {0} for SQL type {1}.", 
                            new Object[] { new Boolean(attributesOk), 
                                            new Integer(this.getJavaSqlType())});
        loc.exiting();
        return attributesOk;
    }
  
    /**
     *  Checks if column-name is a reserved word
     *  @return true - if column-name has no conflict with reserved words, 
     *  false otherwise
     **/
    public boolean checkNameForReservedWord() {

        // ------------------------------------------------------------------ //
        // Method is not supported anymore: keyword check does no longer      //
        // include DB specific checks                                         //
        // ------------------------------------------------------------------ //
        // boolean isReserved = !(DbDb4Environment.isReservedWord(this.getName()));
        
        cat.warningT(loc, 
            "Method checkNameForReservedWord() should not be used anymore!"); 
        return true;
    }


    /**
     * Gets the length of the column. 
     * @return the length if it is not 0. If it is, but the column has got
     * a length attribute, it returns the default length
     */
    public long getLengthOrDdlDefaultLength() {
        long length = 0;
        if (this.getJavaSqlTypeInfo().hasLengthAttribute()) {
            length = this.getLength();
            if (length == 0) {
                length = this.getJavaSqlTypeInfo().getDdlDefaultLength();
            }
        }
        loc.debugT(cat, "getLengthOrDefaultLength() returns {0}.", 
                                    new Object[] {new Long(length)});
        return length;
    
    } 

    //-------------------
    //  private methods  ----------------------------------------------------------------
    //-------------------

    /**
     *  @return the maximum of currentAction and givenAction. 
     **/
    private int raiseActionLevel(int currentAction, int givenAction) {
        loc.debugT(cat, "raiseActionLevel({0}, {1}) called.", 
                    new Object[] {new Integer(currentAction), new Integer(givenAction)});
        int newAction = (currentAction < givenAction) ? givenAction : currentAction;
        return newAction;
    }

}