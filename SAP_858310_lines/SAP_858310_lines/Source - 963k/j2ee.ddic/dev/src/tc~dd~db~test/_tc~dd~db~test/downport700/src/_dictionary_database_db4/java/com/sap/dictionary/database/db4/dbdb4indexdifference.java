package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.dbs.Action;
import com.sap.dictionary.database.dbs.DbIndex;
import com.sap.dictionary.database.dbs.DbIndexDifference;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Title:        DbDb4IndexDifference
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */


public class DbDb4IndexDifference extends DbIndexDifference {


    private static final Location loc = Logger.getLocation("db4.DbDb4IndexDifference");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,
                                                             Logger.CATEGORY_NAME);

    //----------------
    //  constructors  ---------------------------------------------------------
    //----------------
    
    public DbDb4IndexDifference(    DbIndex origin,
                                    DbIndex target, 
                                    Action action) {
    super(  origin, 
            target, 
            action);
    }


    //------------------
    //  public methods  -------------------------------------------------------
    //------------------
    
    /**
     *  Generates the ddl-statement according to found differences concerning
     *  specific parameters
     *  @param tableName                the corresponding table's Name    
     * */
    public DbObjectSqlStatements getDdlStatements(String tableName)
                                                    throws JddException {
        loc.entering(cat, "getDdlStatements(String)");
        DbObjectSqlStatements stmts = null;

        // ------ DROP -------
        if (this.getAction().equals(Action.DROP)) {
            stmts = getDdlsForAction(Action.DROP);
        } else if (this.getAction().equals(Action.CREATE)) {
            stmts = getDdlsForAction(Action.CREATE);
        } else if (this.getAction().equals(Action.DROP_CREATE)) {
            stmts = getDdlsForAction(Action.DROP);
            stmts.merge(getDdlsForAction(Action.CREATE));
        } else {
            cat.warningT(loc, "Unexpected type of action: {0}",
                new Object[] {this.getAction().getName()});
            throw new JddException(ExType.OTHER, 
                    "Unexpected action " + this.getAction().getName() + ".");
        }
        loc.debugT(cat, "Generated: {0}.", new Object[] {stmts.toString()});
        loc.exiting();
        return stmts;
    }

    /**
     *  Generates the ddl-statement according to found differences concerning
     *  specific parameters
     *  @param tableName                the corresponding table's Name    
     *  @param tableForStorageInfo  the table object to get its specific
     *                                parameters which can be used for this
     *                                ddl-statement                  
     * */ 
    public DbObjectSqlStatements getDdlStatements(  String tableName,
                                                    DbTable tableForStorageInfo)
                                                            throws JddException {
        // Not needed. Map to method without tableStorageInfo parameter.
        return this.getDdlStatements(tableName);
    }

    //-------------------
    //  private methods  -------------------------------------------------------
    //-------------------

    /**
     * Returns ddl statements needed for that particular action.
     * Throws JddException if list of statements is null.
     * @param action    - allowed DROP, CREATE
     */
    private DbObjectSqlStatements getDdlsForAction(Action action) 
                                                 throws JddException {
        loc.entering(cat, "getDdlsForAction(Action)");
        if (!action.equals(Action.CREATE) && !action.equals(Action.DROP)) {
            throw new JddException(ExType.OTHER, 
                    "Unsupported type of action " + action.getName());
        }
        
        DbIndex index = action.equals(Action.DROP)
                        ? this.getOrigin() 
                        : this.getTarget();
        
        if (index == null) {
            cat.errorT(loc, "Index definition not found.");
            loc.exiting();
            throw new JddException(ExType.OTHER, "Index definition not found.");
        } 
        
        DbObjectSqlStatements stmts = action.equals(Action.CREATE)
                                        ? index.getDdlStatementsForCreate()
                                        : index.getDdlStatementsForDrop();
        if (stmts == null) {
            cat.errorT(loc, "Empty statement list.");
            loc.exiting();
            throw new JddException(ExType.OTHER, "Empty statement list.");
        }
        
        loc.exiting();
        return stmts;
    }
        
}