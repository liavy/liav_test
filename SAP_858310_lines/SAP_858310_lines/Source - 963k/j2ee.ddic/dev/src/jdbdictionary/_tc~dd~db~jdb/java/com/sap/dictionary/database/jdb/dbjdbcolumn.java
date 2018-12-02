/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/DbJdbColumn.java#5 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */
package com.sap.dictionary.database.jdb;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumnDifference;
import com.sap.dictionary.database.dbs.DbColumnDifferencePlan;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

public class DbJdbColumn extends DbColumn {
    private static final Location LOCATION = Location.getLocation(DbJdbColumn.class);
    private static final Category CATEGORY = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

    public DbJdbColumn(DbFactory factory) {
        super(factory);
    }

    public DbJdbColumn(DbFactory factory, DbColumn other) {
        super(factory, other);
    }

    public DbJdbColumn(DbFactory factory, XmlMap xmlMap) {
        super(factory, xmlMap);
    }

    public DbJdbColumn(DbFactory factory, String name, int position, int javaSqlType, String dbType, long length, int decimals,
            boolean isNotNull, String defaultValue) {
        if (javaSqlType == java.sql.Types.BIGINT || javaSqlType == java.sql.Types.DOUBLE || javaSqlType == java.sql.Types.REAL) {
            length = 0;
        }
        if (length > 0 && javaSqlType != java.sql.Types.LONGVARBINARY) {
            if (!factory.getJavaSqlTypes().getInfo(javaSqlType).hasLengthAttribute()) {
                length = 0;
            }
        }
        constructorPart(factory, name, position, javaSqlType, dbType, length, decimals, isNotNull, defaultValue);
    }

    @Override
    public String getDdlDefaultValueClause() throws Exception {
        try {
            String clause = "";
            int javaSqlType = super.getJavaSqlType();
            JavaJdbSqlTypeInfo javaSqlTypeInfo = (JavaJdbSqlTypeInfo) super.getJavaSqlTypeInfo();
            if (javaSqlTypeInfo.hasDefaultValue()) {
                String defVal = super.getDefaultValue();
                if (defVal != null) {
                    if (javaSqlType == java.sql.Types.DATE || javaSqlType == java.sql.Types.TIME
                            || javaSqlType == java.sql.Types.TIMESTAMP) {
                        if (defVal.equalsIgnoreCase("DATE") || defVal.equalsIgnoreCase("TIME")
                                || defVal.equalsIgnoreCase("TIMESTAMP")) {
                            // get current date or time
                            clause = "DEFAULT " + defVal.toUpperCase();
                        } else {
                            clause = "DEFAULT " + javaSqlTypeInfo.getDefaultValuePrefix() + defVal
                                    + javaSqlTypeInfo.getDefaultValueSuffix();
                        }
                    } else {
                        clause = super.getDdlDefaultValueClause();
                    }
                }
            }
            return clause;
        } catch (Exception ex) {
            Object[] arguments = { ex.getMessage() };
            CATEGORY.errorT(LOCATION, "getDdlDefaultValueClause failed: {0}", arguments);
            throw JddException.createInstance(ex);
        }
    }

    @Override
    public String getDdlClause() throws Exception {
        String clause = "";
        clause = super.getDdlClause();
        return clause;
    }

    @Override
    protected DbColumnDifference comparePartTo(DbColumn target) throws Exception {
        DbJdbColumn orgCol = this;
        DbJdbColumn targetCol = null;
        DbColumnDifferencePlan plan = new DbColumnDifferencePlan();
        boolean hasToConvert = false;
        targetCol = (DbJdbColumn) target;
        // check type
        int orgType = orgCol.getJavaSqlType();
        int targetType = targetCol.getJavaSqlType();
        // following java SQL types will be used as the same database type
        // REAL, FLOAT, DOUBLE -> FLOAT
        // VARCHAR, LONGVARCHAR -> VARCHAR
        // BINARY, VARBINARY -> VARCHAR BYTE
        // BLOB, LONGVARBINARY -> LONG BYTE
        if ((orgType != targetType)
                && !(((orgType == java.sql.Types.REAL) || (orgType == java.sql.Types.FLOAT) || (orgType == java.sql.Types.DOUBLE)) && ((targetType == java.sql.Types.REAL)
                        || (targetType == java.sql.Types.FLOAT) || (targetType == java.sql.Types.DOUBLE)))
                && !(((orgType == java.sql.Types.VARCHAR) && (targetType == java.sql.Types.LONGVARCHAR)))
                && !(((orgType == java.sql.Types.LONGVARCHAR) && (targetType == java.sql.Types.VARCHAR)))
                && !(((orgType == java.sql.Types.BINARY) && (targetType == java.sql.Types.VARBINARY)))
                && !(((orgType == java.sql.Types.VARBINARY) && (targetType == java.sql.Types.BINARY)))
                && !(((orgType == java.sql.Types.VARBINARY) && (targetType == java.sql.Types.LONGVARBINARY)))
                && !(((orgType == java.sql.Types.BLOB) && (targetType == java.sql.Types.LONGVARBINARY)))) {
            plan.setTypeIsChanged(true);
            Object[] arguments = { getName(), orgCol.getJavaSqlTypeName(), targetCol.getJavaSqlTypeName() };
            if (!(orgType == java.sql.Types.SMALLINT && targetType == java.sql.Types.INTEGER)) {
                hasToConvert = true;
                CATEGORY.infoT(LOCATION, "compareTo ({0}): CONVERT: original type {1} incompatible to target type {2}",
                        arguments);
            } else {
                CATEGORY.infoT(LOCATION, "compareTo ({0}): ALTER type from {1} to {2}", arguments);
            }
        }
        // check special J2EE column (only if the database connection exists
        if (hasToConvert == false) {
            if (getColumns().getTable().getDbFactory().getConnection() != null) {
                String dbTypeOfOrgCol = orgCol.getDbType();
                if (dbTypeOfOrgCol != null && dbTypeOfOrgCol.equals("LONG BYTE")) {
                    plan.setTypeIsChanged(true);
                    hasToConvert = true;
                    Object[] arguments = { getName(), orgCol.getDbType(), new Long(targetCol.getLength()) };
                    CATEGORY.infoT(LOCATION,
                            "compareTo ({0}): CONVERT: original type {1} incompatible to target type VARCHAR({2}) BYTE",
                            arguments);
                }
            }
        }
        // compare length
        if (hasToConvert == false) {
            if (orgCol.getJavaSqlTypeInfo().hasLengthAttribute() || orgType == java.sql.Types.LONGVARBINARY
                    || (orgType == java.sql.Types.BLOB && targetType == java.sql.Types.LONGVARBINARY)) {
                long orgLength = orgCol.getLength();
                long targetLength = targetCol.getLength();
                if (orgLength == 0)
                    orgLength = orgCol.getJavaSqlTypeInfo().getDdlDefaultLength();
                if (targetLength == 0)
                    targetLength = targetCol.getJavaSqlTypeInfo().getDdlDefaultLength();
                if (orgLength != targetLength) {
                    plan.setLengthIsChanged(true);
                    Object[] arguments = { getName(), new Long(orgLength), new Long(targetLength) };
                    if (orgLength > targetLength) {
                        hasToConvert = true;
                        CATEGORY.infoT(LOCATION,
                                "compareTo ({0}): CONVERT: original length {1} greater than target length {2}", arguments);
                    } else {
                        CATEGORY.infoT(LOCATION, "compareTo ({0}): ALTER length from {1} to {2}", arguments);
                    }
                }
            }
        }
        // Compare scale and precision
        if (hasToConvert == false && orgCol.getJavaSqlTypeInfo().hasDecimals()) {
            if (orgCol.getDecimals() != targetCol.getDecimals()) {
                plan.setDecimalsAreChanged(true);
                if (orgCol.getDecimals() > targetCol.getDecimals()) {
                    hasToConvert = true;
                    Object[] arguments = { getName(), new Long(orgCol.getDecimals()), new Long(targetCol.getDecimals()) };
                    CATEGORY.infoT(LOCATION,
                            "compareTo ({0}): CONVERT: original decimals {1} greater than target decimals {2}", arguments);
                } else {
                    long precOrigin = orgCol.getLength() - orgCol.getDecimals();
                    long precTarget = targetCol.getLength() - targetCol.getDecimals();
                    if (precOrigin > precTarget) {
                        hasToConvert = true;
                        Object[] arguments = { getName(), new Long(precOrigin), new Long(precTarget) };
                        CATEGORY
                                .infoT(LOCATION,
                                        "compareTo ({0}): CONVERT: original precision {1} greater than target precision {2}",
                                        arguments);
                    } else {
                        Object[] arguments = { getName(), new Long(orgCol.getDecimals()), new Long(precOrigin),
                                new Long(targetCol.getDecimals()), new Long(precTarget) };
                        CATEGORY.infoT(LOCATION, "compareTo ({0}): ALTER decimals from ({1}.{2}) to ({3}.{4})", arguments);
                    }
                }
            }
        }
        // compare nullability
        if (hasToConvert == false && orgCol.isNotNull() != targetCol.isNotNull()) {
            plan.setNullabilityIsChanged(true);
            Object[] arguments = { getName() };
            if (targetCol.isNotNull() && targetCol.getDefaultValue() == null) {
                // NULL -> NOT NULL without a default : action CONVERT is returned.
                hasToConvert = true;
                CATEGORY.infoT(LOCATION, "compareTo ({0}): CONVERT: NULL to NOT NULL without a default value", arguments);
            } else {
                if (targetCol.isNotNull())
                    CATEGORY.infoT(LOCATION, "compareTo ({0}): ALTER column to NOT NULL", arguments);
                else
                    CATEGORY.infoT(LOCATION, "compareTo ({0}): ALTER column to NULL", arguments);
            }
        }
        // compare default value
        if (hasToConvert == false) {
            if (orgCol.getDefaultValue() == null) {
                if (targetCol.getDefaultValue() != null) {
                    plan.setDefaultValueIsChanged(true);
                    Object[] arguments = { getName(), targetCol.getDefaultValue() };
                    CATEGORY.infoT(LOCATION, "compareTo ({0}): ALTER default to {1}", arguments);
                }
            } else if (targetCol.getDefaultValue() == null) {
                plan.setDefaultValueIsChanged(true);
                CATEGORY.infoT(LOCATION, "compareTo ({0}): ALTER default to NULL", new Object[] { getName() });
            } else {
                String orgDefVal = orgCol.getDefaultValue();
                String targetDefVal = targetCol.getDefaultValue();
                boolean differs = false;
                switch (targetType) {
                case (java.sql.Types.SMALLINT):
                case (java.sql.Types.INTEGER):
                case (java.sql.Types.BIGINT):
                    long targetIntVal = Long.valueOf(targetDefVal).longValue();
                    long orgIntVal = Long.valueOf(orgDefVal).longValue();
                    differs = targetIntVal != orgIntVal;
                    break;
                case (java.sql.Types.DECIMAL):
                case (java.sql.Types.FLOAT):
                case (java.sql.Types.DOUBLE):
                case (java.sql.Types.REAL):
                    double targetFloatVal = Double.valueOf(targetDefVal).doubleValue();
                    double orgFloatVal = Double.valueOf(orgDefVal).doubleValue();
                    differs = targetFloatVal != orgFloatVal;
                    break;
                case (java.sql.Types.TIME):
                case (java.sql.Types.DATE):
                case (java.sql.Types.TIMESTAMP):
                    differs = !orgDefVal.equalsIgnoreCase(targetDefVal);
                    break;
                default:
                    if (orgDefVal.equals(targetDefVal) == false)
                        differs = true;
                    break;
                }
                if (differs) {
                    plan.setDefaultValueIsChanged(true);
                    Object[] arguments = { getName(), targetCol.getDefaultValue() };
                    CATEGORY.infoT(LOCATION, "compareTo ({0}): ALTER default to {1}", arguments);
                }
            }
        }
        if (hasToConvert == true) {
            return (new DbColumnDifference(this, target, plan, Action.CONVERT));
        } else if (plan.somethingIsChanged()) {
            return (new DbColumnDifference(this, target, plan, Action.ALTER));
        } else {
            return null;
        }
    }

    @Override
    public boolean acceptedAdd() {
        if (!(isNotNull() == true && getDefaultValue() == null))
            return true;
        Object[] arguments = { getName() };
        CATEGORY.infoT(LOCATION, "Add column {0} not accepted: NOT NULL is specified but no DEFAULT value is set", arguments);
        return false;
    }

    @Override
    public boolean acceptedDrop() {
        return false;
    }

    /**
     * * Check the column's name according to its length *
     * @return true - if name-length is o.k
     */
    @Override
    public boolean checkNameLength() {
        int nameLen = getName().length();
        int maxLen = DbJdbEnvironment.MaxNameLength();
        if (nameLen > 0 && nameLen <= maxLen) {
            return true;
        } else {
            Object[] arguments = { getName(), new Integer(nameLen), new Integer(maxLen) };
            CATEGORY.errorT(LOCATION, "checkNameLength {0}: length {1} invalid (allowed range [1..{2}])", arguments);
            return false;
        }
    }

    /**
     * * Checks if column-name is a reserved word *
     * @return true - if column-name has no conflict with reserved words, * false otherwise
     */
    @Override
    public boolean checkNameForReservedWord() {
        boolean isReserved = DbJdbEnvironment.isReservedWord(getName());
        if (isReserved == true) {
            Object[] arguments = { getName() };
            CATEGORY.errorT(LOCATION, "{0} is a reserved word", arguments);
        }
        return (isReserved == false);
    }

    /**
     * * Check the columns's attributes: type, length and decimals, e.g * if length is to big for current type *
     * @return true - if name-length is o.k
     */
    @Override
    public boolean checkTypeAttributes() {
        boolean check = true;
        long len;
        long maxLen;
        switch (getJavaSqlType()) {
        case java.sql.Types.DECIMAL:
        case java.sql.Types.NUMERIC:
            len = getLength();
            maxLen = DbJdbEnvironment.MaxDecimalLength();
            if (len < 0 || len > maxLen) {
                check = false;
                Object[] arguments = { getName(), new Long(len), new Long(maxLen) };
                CATEGORY.errorT(LOCATION,
                        "checkTypeAttributes {0}: a length of {1} is invalid for decimal-fields (allowed range [1..{2}])",
                        arguments);
            }
            if (len < getDecimals()) {
                check = false;
                Object[] arguments = { getName(), new Integer(getDecimals()), new Long(len) };
                CATEGORY.errorT(LOCATION, "checkTypeAttributes {0}: scale {1} is greater than precision {2}", arguments);
            }
            break;
        case java.sql.Types.VARBINARY:
        case java.sql.Types.BINARY:
            len = getLength();
            maxLen = DbJdbEnvironment.MaxBinaryLength();
            if (len < 0 || len > maxLen) {
                check = false;
                Object[] arguments = { getName(), new Long(len), new Long(maxLen) };
                CATEGORY.errorT(LOCATION,
                        "checkTypeAttributes {0}: length of {1} is out of range for binary-fields (allowed range [1..{2}])",
                        arguments);
            }
            break;
        case java.sql.Types.VARCHAR:
        case java.sql.Types.CHAR:
            len = getLength();
            maxLen = DbJdbEnvironment.MaxCharacterLength();
            if (len < 0 || len > maxLen) {
                check = false;
                Object[] arguments = { getName(), new Long(len), new Long(maxLen) };
                CATEGORY.errorT(LOCATION,
                        "checkTypeAttributes {0}: length of {1} is out of range for character-fields (allowed range [1..{2}])",
                        arguments);
            }
            break;
        }
        return check;
    }
}
