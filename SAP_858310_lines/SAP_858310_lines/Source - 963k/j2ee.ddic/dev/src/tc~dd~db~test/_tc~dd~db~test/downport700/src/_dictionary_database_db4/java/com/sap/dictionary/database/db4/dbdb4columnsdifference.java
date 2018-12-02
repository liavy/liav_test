package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumnDifference;
import com.sap.dictionary.database.dbs.DbColumnDifferencePlan;
import com.sap.dictionary.database.dbs.DbColumnsDifference;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbSqlStatement;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Title:        DbDb4ColumnsDifference
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */

public class DbDb4ColumnsDifference extends DbColumnsDifference {

    private static final Location loc = Logger.getLocation("db4.DbDb4ColumnsDifference");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,
                                                             Logger.CATEGORY_NAME);

    //----------------
    //  constructors  ---------------------------------------------------------
    //----------------

    public DbDb4ColumnsDifference() {
        super();
    }


    //------------------
    //  public methods  -------------------------------------------------------
    //------------------

    /**
     *  Generates the ddl-statement for these columns
     *  @param tableName                the current table 
     *  @return The statements as DbObjectSqlStatements   
     * */
    public DbObjectSqlStatements getDdlStatementsForAlter(String tableName)
                                                            throws Exception {
        /* Most ALTER statements cause system internal table conversions.
         * That's why we generate only one statement.
         * Generate one clause for each column in overall "difference" list
         * which forms the union of the "add", "drop", and "modify" list without
         * duplicates.
         * Precedences:
         *    drop   + add      -  cannot occur simultaneously
         *    drop   + modify   -  modify
         *    create + modify   -  modify
         */
        loc.entering(cat, "getDdlStatementsForAlter({0})", new Object[] {tableName});
        DbObjectSqlStatements stmts = new DbObjectSqlStatements(tableName);
        DbColumnsDifference.MultiIterator multiIter = this.iterator();
        DbSqlStatement alterStmt = new DbSqlStatement();
        String action = "";
        
        /*
         * Find null values for table and column names doing string 
         * operations. Map NullPointerException to JddException then.
         */
        try {

            // --- Beginning of ALTER statement ---
            if (!multiIter.hasNext()) {
                loc.debugT(cat, "Table {0} did not change.", new Object[] {tableName});
                loc.exiting();
                return null;
            } else {
                loc.debugT(cat, "'ALTER TABLE...'");
                alterStmt.addLine("ALTER TABLE " + "\"" 
                                    + tableName.trim().toUpperCase() + "\" ");
            }
            
            // Loop over columns with changes
            while (multiIter.hasNext()) {
                
                // Inconsistent action
                DbColumnDifference diff = multiIter.next();
                if (!diff.getAction().equals(Action.ALTER)) {
                    cat.errorT(loc, "Action 'ALTER' expected. DbColumnDifference: {0}",
                        new Object[] {diff});
                        loc.exiting();
                        throw new JddException(ExType.OTHER, 
                            "Action 'ALTER' expected. Got " + diff.getAction() + ".");
                }
                
                DbColumnDifferencePlan plan = diff.getDifferencePlan();
                boolean addOrDrop = false;

                if (plan != null) {
                    if (!plan.somethingIsChanged()) {
                        // ----- ADD or DROP ------
                        addOrDrop = true;
                    } else {
                        // ----- ALTER -------
                        // (most conservative approach to not destroy data. 
                        //  - Ignore anything else.)
                        String typeLenDecClause = "ALTER COLUMN " 
                            + "\"" + diff.getTarget().getName().trim().toUpperCase() + "\" ";
                        if (plan.typeLenDecIsChanged()) { 
                            typeLenDecClause += getTypeLenDecClause(diff.getTarget());
                        }
                        if (plan.nullabilityIsChanged()) {
                            typeLenDecClause += getNullabilityClause(diff.getTarget());
                        }
                        if (plan.defaultValueIsChanged()) {
                            typeLenDecClause += getDefValClause(diff.getTarget());
                        }
                        alterStmt.addLine(typeLenDecClause);
                    }
                } 
                if ((plan == null) || (addOrDrop)) {
                    String addDropClause = "";
                    addDropClause = (diff.getOrigin() == null)
                                    // ------ DROP ------
                                    ? getAddClause(diff.getTarget())
                                    // ------ CREATE ------
                                    : getDropClause(diff.getOrigin());
                    if (addDropClause.equals("")) {
                        cat.warningT(loc, "DbColumnDifference without change: {0}",
                            new Object[] {diff.toString()});
                    } else {
                        alterStmt.addLine(addDropClause);
                    }
                }
            }
        } catch (Exception e) {            //$JL-EXC$
            // Map all NullPointerExceptions on JddException.
            cat.errorT(loc, "Exception caught generating ALTER statement: {0}.",
                new Object[]{e.getMessage()});
            loc.exiting();
            throw JddException.createInstance(e);
        }
        stmts.add(alterStmt);
        loc.debugT(cat, "Generated: {0}", new Object[] {alterStmt});
        loc.exiting();
        return stmts;
    }


    //-------------------
    //  private methods  -------------------------------------------------------
    //-------------------

    private String getAddClause(DbColumn col) throws Exception {
        loc.debugT(cat, "'...ADD COLUMN...'");
        return "ADD COLUMN " + col.getDdlClause() + " ";
    }
    
    private String getDropClause(DbColumn col) throws Exception {
        loc.debugT(cat, "'...DROP COLUMN...'");
        return "DROP COLUMN " + "\"" + col.getName().trim().toUpperCase() + "\" "
            + "CASCADE ";
    }
    
    private String getTypeLenDecClause(DbColumn target) throws Exception {
        loc.debugT(cat, "'...SET DATA TYPE...'");
        return "SET DATA TYPE " + target.getDdlTypeClause() + " ";
    }
    
    private String getNullabilityClause(DbColumn target) throws Exception {
        if (target.isNotNull()) {
            loc.debugT(cat, "'...SET NOT NULL...'");
            return "SET NOT NULL ";
        } else {
            loc.debugT(cat, "'...DROP NOT NULL...'");
            return "DROP NOT NULL ";
        }
    }
    
    private String getDefValClause(DbColumn target) throws Exception {
        if (target.getJavaSqlTypeInfo().hasDefaultValue() 
                && (target.getDefaultValue() != null)) {
            loc.debugT(cat, "'...SET [DEFAULT]..'");
            return "SET " + target.getDdlDefaultValueClause() + " ";
        } else {
            loc.debugT(cat, "'...DROP DEFAULT..'");
            return "DROP DEFAULT ";
        }
    }

}