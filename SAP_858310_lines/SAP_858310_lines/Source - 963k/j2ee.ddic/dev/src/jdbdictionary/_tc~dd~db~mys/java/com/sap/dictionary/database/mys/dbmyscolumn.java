package com.sap.dictionary.database.mys;

import com.sap.dictionary.database.dbs.*;
import com.sap.dictionary.database.mys.DbMysColumn;

import java.sql.*;

import com.sap.tc.logging.*;

/**
 * Title:        DbMysColumn
 * Description:  
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Hakan Kuecuekyilmaz, <hakan at mysql dot com>, 2005-07-10
 * @version 1.0
 */

public class DbMysColumn extends DbColumn {
    private static Location loc = Logger.getLocation("mys.DbMysColumn");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

    public DbMysColumn(DbFactory factory) {
        super(factory);
    }

    public DbMysColumn(DbFactory factory, DbColumn other) {
        super(factory, other);
    }

    public DbMysColumn(DbFactory factory, XmlMap xmlMap) {
        super(factory, xmlMap);
    }


    // Constructor excluding src-Type that means java-Type. Recommended if
    // origin is database which does not know how a database-table's column 
    // is used in Java
    public DbMysColumn(DbFactory factory, String name, int position,
            int javaSqlType, String dbType, long length, int decimals,
            boolean isNotNull, String defaultValue) {
        // BLOB
        if (dbType.equalsIgnoreCase("LONGBLOB")) {
            javaSqlType = Types.BLOB;
            length = 0;
        }
        // CLOB
        else if (dbType.equalsIgnoreCase("LONGTEXT")) {
            javaSqlType = Types.CLOB;
            length = 0;
        }
        // VARCHAR
        else if (dbType.equalsIgnoreCase("VARCHAR")) {
            javaSqlType = Types.VARCHAR;
        }
        // CHAR
        else if (dbType.equalsIgnoreCase("CHAR")) {
            javaSqlType = Types.CHAR;
        }
        // FLOAT
        else if (dbType.equalsIgnoreCase("DOUBLE")) {
            javaSqlType = Types.DOUBLE;
        }
        // TINYINT
        else if (dbType.equalsIgnoreCase("TINYINT")) {
            javaSqlType = Types.TINYINT;
        }
        // SMALLINT
        else if (dbType.equalsIgnoreCase("SMALLINT")) {
            javaSqlType = Types.SMALLINT;
        }
        // INTEGER
        else if (dbType.equalsIgnoreCase("INTEGER")) {
            javaSqlType = Types.INTEGER;
        } 
        // TIMESTAMP
        else if (dbType.equalsIgnoreCase("DATETIME")) {
            javaSqlType = Types.TIMESTAMP;
        }
        
        if (length > 0) {
            if (!factory.getJavaSqlTypes().getInfo(javaSqlType).hasLengthAttribute()) {
                length = 0;
            }
        }
        constructorPart(factory, name, position, javaSqlType, dbType, length,
                decimals, isNotNull, defaultValue);
    }


    /**
     *  Compares this column to a target column
     *  @param target	            the column's target version 
     *  @return The differences about datatypes, length, decimals, 
     *           notNull, defaultValue,   
     */
    protected DbColumnDifference comparePartTo(DbColumn target) throws Exception {
        loc.entering("compareTo");
        try {
            DbMysColumn targetCol = (DbMysColumn) target;
            DbColumnDifferencePlan columnDifferencePlan = new DbColumnDifferencePlan();
            
            int originalType = getJavaSqlType();
            int targetType = targetCol.getJavaSqlType();
            
            // Compare type
            if (originalType != targetType) {
                columnDifferencePlan.setTypeIsChanged(true);
                return new DbColumnDifference(this, target, 
                        columnDifferencePlan, Action.CONVERT);
            }

            // Compare length
            if (getJavaSqlTypeInfo().hasLengthAttribute())
                if (getLength() != targetCol.getLength()) {
                    columnDifferencePlan.setLengthIsChanged(true);

                    if (getLength() > targetCol.getLength())
                        return new DbColumnDifference(this, target,
                                columnDifferencePlan, Action.CONVERT);
                }

            // Compare decimals
            if (getJavaSqlTypeInfo().hasDecimals())
                if (getDecimals() != targetCol.getDecimals()) {
                    columnDifferencePlan.setDecimalsAreChanged(true);

                    if (getDecimals() > targetCol.getDecimals())
                        return new DbColumnDifference(this, target,
                                columnDifferencePlan, Action.CONVERT);
                }

            // Compare scale
            if (getJavaSqlTypeInfo().hasDecimals())
                if (getLength() - getDecimals() != targetCol.getLength()
                        - targetCol.getDecimals()) {

                    if (getLength() - getDecimals() > targetCol.getLength()
                            - targetCol.getDecimals())
                        return new DbColumnDifference(this, target,
                                columnDifferencePlan, Action.CONVERT);
                }

            // Compare nullability
            if (isNotNull() != targetCol.isNotNull()) {
                columnDifferencePlan.setNullabilityIsChanged(true);

                // NULL -> NOT NULL: if there is a default value, all rows
                // containing the
                // NULL value are updated with the default value and the
                // ALTER TABLE statement
                // can be issued. If there is no default value, action
                // CONVERT is returned.
                if (targetCol.isNotNull() && targetCol.getDefaultValue() == null)
                    return new DbColumnDifference(this, target,
                            columnDifferencePlan, Action.CONVERT);
            }

            // Compare default value
            String orgDefVal = getDefaultValue();

            String targetDefVal = targetCol.getDefaultValue();
            
            if (orgDefVal == null) {
                columnDifferencePlan
                        .setDefaultValueIsChanged(!(targetDefVal == null));
            } else {
                columnDifferencePlan
                        .setDefaultValueIsChanged((!(orgDefVal.equals(targetDefVal))));
            }

            if (columnDifferencePlan.somethingIsChanged())
                return new DbColumnDifference(this, target,
                        columnDifferencePlan, Action.ALTER);
            else
                return null;
        } catch (Exception exception) {
            Object[] arguments = { getName(), exception.getMessage() };

            loc.errorT("compareTo({0}) failed: {1}", arguments);

            throw JddException.createInstance(exception);
        } finally {
            loc.exiting();
        }
    }

    /**
     *  Analyses the behaviour in case a column has to be dropped
     *  @return true if a column can be dropped via a ddl-statement 
     */
    public boolean acceptedDrop() {
        loc.entering("acceptedDrop");
        loc.exiting();
        return true;
    }

    /**
     *  Check the column's name according to its length
     *  @return true - if name-length is o.k
     */
    public boolean checkNameLength() {
        loc.entering("checkNameLength");

        int nameLen = this.getName().length();

        boolean check = (nameLen > 0 && nameLen <= 64);

        if (check == false) {
            Object[] arguments = { getName(), new Integer(nameLen)};
            cat.errorT(loc, 
                "checkNameLength {0}: length {1} invalid (allowed range [1..64])",
                arguments);
        }
        loc.exiting();
        return check;
    }

    /**
     *  Checks if column-name is a reserved word
     *  @return true - if column-name has no conflict with reserved words,
     *                    false otherwise
     */
    public boolean checkNameForReservedWord() {
        loc.entering("checkNameForReservedWord");

        boolean check = (DbMysEnvironment.isReservedWord(this.getName()) == false);

        if (check == false) {
            Object[] arguments = { this.getName() };
            loc.errorT("{0} is a reserved word", arguments);
        }
        loc.exiting();
        return check;
    }

    /**
     *  Check the columns's attributes: type, length and decimals, e.g
     *  if length is to big for current type
     *  @return true - if name-length is o.k
     */
    public boolean checkTypeAttributes() {
        // TODO:
        // System.out.println("TODO for Hakan: checkTypeAttributes not implemented");
        return true;
    }
}
