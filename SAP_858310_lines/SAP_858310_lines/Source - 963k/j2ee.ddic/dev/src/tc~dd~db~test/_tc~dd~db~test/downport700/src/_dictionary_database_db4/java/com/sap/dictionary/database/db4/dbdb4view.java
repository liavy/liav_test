package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.dbs.DbsConstants;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbView;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;



/**
 * Title:        DbDb4View
 * Copyright:    Copyright (c) 2004
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */

public class DbDb4View extends DbView implements DbsConstants {
    
    private static final Location loc = Logger.getLocation("db4.DbDb4View");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,
                                                             Logger.CATEGORY_NAME);
                                                             
    /** 
     * List of short DB4 system table names. Given the following objects
     * - Table SAPsidDB.T, 
     * - Alias SHDsidDB.A FOR SAPsidDB/T
     * - View SHDsidDB.V AS SELECT * FROM SHDsidDB.A
     * the list always contains the short system name of T (which DB4 considers 
     * to be the base table for V, regardless of A, and which has to be replaced 
     * in the view text) 
     * Order of list matches that of baseTableNames. 
     **/ 
    private ArrayList db4ShortBaseTableNames; 
    
    /**
     * List of schema names associated with db4ShortBaseTabNames. 
     */
    private ArrayList db4ShortBaseTableSchemaNames; 

    /**
     * Name of current schema.  
     **/ 
    private String currentSchema; 
    

    //----------------
    //  constructors  -------------------------------------------------------------------
    //----------------
    public DbDb4View() {
    }

    public DbDb4View(DbFactory factory) {
        super(factory);
    }

    public DbDb4View(DbFactory factory, String name) {
        super(factory,name);
    }

    public DbDb4View(DbFactory factory, DbView other) {
        super(factory,other);
    }

    public DbDb4View(DbFactory factory, DbSchema schema, String name) {
        super(factory,schema,name);
    }   

    /**
     *  Analyses if view exists on database or not
     *  @return true - if view exists in database, false otherwise
     *  @exception JddException - error during analysis detected     
    **/
    public boolean existsOnDb() throws JddException {
        loc.entering(cat, "existsOnDb()");
        boolean existsOnDb = true;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String viewName = null;
        String stmtStr = "SELECT CURRENT_SCHEMA FROM SYSVIEWS " +
                            "WHERE TABLE_NAME = ?";
        viewName = this.getName().trim().toUpperCase();

        if (this.getDbFactory() == null ||
            (con = this.getDbFactory().getConnection()) == null) {
            cat.errorT(loc, "No connection provided.");
            loc.exiting();
            throw new JddException(ExType.OTHER, "No connection provided.");
        }

        try {
            pstmt = NativeSQLAccess.prepareNativeStatement(con, stmtStr);
            pstmt.setString(1, viewName);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                existsOnDb = true;
                this.currentSchema = rs.getString(1).trim();
            } else {
                existsOnDb = false;
            }
        } catch (SQLException sqlEx) {              //$JL-EXC$
            String msg = "Exception caught executing '" + stmtStr +
            "'. The exception was: " + sqlEx.getErrorCode() +
            ", " + sqlEx.getSQLState() + ": " + sqlEx.getMessage();
            cat.errorT(loc, msg);
            throw JddException.createInstance(sqlEx);
        } finally {
            try {
                rs.close();
                pstmt.close();
            } catch (Exception e) {            //$JL-EXC$
                cat.errorT(loc, "Exception caught closing statement resources:\n {0}",
                                new Object[] {e.getMessage()});
                loc.exiting();
                throw JddException.createInstance(e);
            }
        }
        loc.debugT(cat, "existsOnDb() returns {0}.", 
                            new Object[] {new Boolean(existsOnDb)});
        loc.exiting();
        return existsOnDb;

    }
   
    /**
     *  Gets the base table names of this view from database and sets the 
     *  corresponding variable with method setBaseTableNames
     *  @exception JddException - error during analysis detected     
     **/
    public void setBasetableNamesViaDb() throws JddException {
        loc.entering(cat, "setBasttableNamesViaDb()");
        Connection con = null;
        PreparedStatement ps = null; 
        ResultSet rs = null;
        String viewName = null;
        String msg = null;
        ArrayList baseTabNames = new ArrayList();
        ArrayList db4ShortBaseTabNames = new ArrayList();
        ArrayList db4ShortBaseTabSchemaNames = new ArrayList();

        // List of questionable table names
        ArrayList _db4ShortBaseTabNames = new ArrayList();

        // --------------------------------------------------------------- //
        // Imagine the following situation:                                // 
        // - Table SAPsidDB.T                                              //
        // - Alias SHDsidDB.A FOR SAPsidDB/T                               //
        // - View SHDsidDB.V AS SELECT * FROM SHDsidDB.A                   //
        // In that case, SYSVIEWDEP will show SAPsidDB.T as the base table //
        // for SHDsidDB.V instead of SHDsidDB.A like on other platforms.   //
        // Same holds true for the view text in SYSVIEWS and QSYS2.VIEWS.  //
        // This is also functional: A can be dropped, while V stays. V is  //
        // only dependent on T.                                            //
        // --------------------------------------------------------------- //
        
        String stmt = "SELECT " +
                            "CURRENT_SCHEMA, " + 
                            "DEP.TABLE_SCHEMA, " +          // DB4 base table name: schema name
                            "DEP.TABLE_NAME, " +            // DB4 base table name: SQL name
                            "DEP.SYSTEM_TABLE_NAME, " +     // DB4 base table name: system name
                            "DEP.TABLE_TYPE, " +            // DB4 base table name: table type ('T')
                            "TAB.TABLE_SCHEMA, " +          // Alias, if existent
                            "TAB.TABLE_NAME, " +            // Alias, if existent
                            "TAB.TABLE_TYPE " +             // Alias, if 'A' 
                        "FROM QSYS2.SYSVIEWDEP AS DEP LEFT OUTER JOIN " +  
                             "QSYS2.SYSTABLES AS TAB " +
                        "ON DEP.TABLE_NAME = TAB.BASE_TABLE_NAME AND " +  
                           "DEP.TABLE_SCHEMA = TAB.BASE_TABLE_SCHEMA " +
                           "WHERE DEP.VIEW_NAME = ? AND " +
                           "DEP.VIEW_SCHEMA = CURRENT_SCHEMA AND " +       
                           "DEP.TABLE_TYPE IS NOT NULL";
                             
        // ------------------------------------------------- //
        // Consistency Checks                                //
        // ------------------------------------------------- //
        if ( (viewName = this.getName()) == null) {
            msg = "No view name provided.";
            cat.errorT(loc, msg);
            loc.exiting();
            throw new JddException(ExType.OTHER, msg);
        }
        viewName = viewName.trim().toUpperCase();
        
        if (this.getDbFactory() == null ||
            (con = this.getDbFactory().getConnection()) == null) {
            msg = "No connection provided.";
            cat.errorT(loc, msg);
            loc.exiting();
            throw new JddException(ExType.OTHER, msg);
        } 
        
        // ------------------------------------------------- //
        // Get the data                                      //
        // ------------------------------------------------- //
        try {
            ps = NativeSQLAccess.prepareNativeStatement(con, stmt);
            ps.setString(1, viewName);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                this.currentSchema = rs.getString(1).trim().toUpperCase();
                String tabSchema   = rs.getString(2).trim().toUpperCase();
                String tabName     = rs.getString(3).trim().toUpperCase();
                String sysTabName  = rs.getString(4).trim().toUpperCase();
                String tabType     = rs.getString(5).trim().toUpperCase();

                String aliasSchema = rs.getString(6);
                if (aliasSchema != null) aliasSchema.trim().toUpperCase();
                String aliasName   = rs.getString(7);
                if (aliasName != null) aliasName.trim().toUpperCase();
                String type        = rs.getString(8);
                if (type != null) type.trim().toUpperCase();
                
                // ------------------------------------------------- //
                // For now accept only SQL tables                    //
                // ------------------------------------------------- //
                if (tabType.equals("L") || tabType.equals("P")) {
                    msg = "Base table " + tabName + " to view " + 
                          viewName + " is a native iSeries file of type '" + 
                          tabType + "', not an SQL object.)";
                    cat.errorT(loc, msg);
                    loc.exiting();
                    throw new JddException(ExType.OTHER, msg);
                }

                // ------------------------------------------------- //
                // In the end, baseTableNames may only contain       // 
                // objects in current schema. Everything else cannot //
                // be handled by JDDI anyway as of today.            //
                // ------------------------------------------------- //
                if (isShadowSchema(con)) {
                    if (type == null) {
                        // ------------------------------------------------- //
                        // Base table w/o alias --> Error!                   //
                        // ------------------------------------------------- //
                        msg = "Shadow Upgrade: No alias found for base table " + 
                                tabSchema + "." + tabName + " to view " + 
                                currentSchema + "." + viewName + "."; 
                        cat.errorT(loc, msg);
                        loc.exiting();
                        throw new JddException(ExType.OTHER, msg);
                    } else if (type != null && type.equals("A") && 
                                aliasSchema.equals(this.currentSchema) ) {
                        // ------------------------------------------------- //
                        // Normal case                                       //
                        // ------------------------------------------------- //
                        baseTabNames.add(aliasName);
                        db4ShortBaseTabNames.add(sysTabName);
                        db4ShortBaseTabSchemaNames.add(tabSchema);
                    } else {
                        // ------------------------------------------------- //
                        // An alias exists, but it is not located in the     //
                        // shadow schema. Alias might exist in addition.     //
                        // ------------------------------------------------- //
                        if (!db4ShortBaseTabNames.contains(sysTabName)) {
                            _db4ShortBaseTabNames.add(sysTabName);
                            msg = "Shadow Upgrade: So far, the only alias " +
                                    aliasSchema + "." + aliasName + " for base table " + 
                                    tabSchema + "." + tabName + " (" +
                                    sysTabName + ") to view " + 
                                    currentSchema + "." + viewName + 
                                    " is not located in " + this.currentSchema + ".";
                            cat.warningT(loc, msg);
                        } else {
                            // An entry for that base table already exists.
                        }
                    }
                } else {
                    if (type == null) {
                        // ------------------------------------------------- //
                        // Normal case                                       //
                        // ------------------------------------------------- //
                        baseTabNames.add(tabName);
                        db4ShortBaseTabNames.add(sysTabName);
                        db4ShortBaseTabSchemaNames.add(tabSchema);
                    } else {
                        // ------------------------------------------------- //
                        // Alias might exist in addition.                    //
                        // ------------------------------------------------- //
                        if (!db4ShortBaseTabNames.contains(sysTabName)) {
                            baseTabNames.add(tabName);
                            db4ShortBaseTabNames.add(sysTabName);
                            db4ShortBaseTabSchemaNames.add(tabSchema);
                        }
                    }
                }
            }
            loc.debugT("The following base table names " + 
                        "were set from database for view " + viewName + ":");
            for (int i=0; i<baseTabNames.size(); i++) {
                loc.debugT("   " + baseTabNames.get(i) +
                           " (DB4: " + db4ShortBaseTabSchemaNames.get(i) + "/" +
                           db4ShortBaseTabNames.get(i) + ")");
            }
            
            // ------------------------------------------------- //
            // Now check list of questionable base tables        //
            // ------------------------------------------------- //
            for (int i=0; i<_db4ShortBaseTabNames.size(); i++) {
                if (!db4ShortBaseTabNames.contains(_db4ShortBaseTabNames.get(i))) {
                    msg = "Shadow Upgrade: No valid alias found for base table " + 
                        _db4ShortBaseTabNames.get(i) + "."; 
                    cat.errorT(loc, msg);
                    loc.exiting();
                    throw new JddException(ExType.OTHER, msg);
                }
            }
            
            this.setBaseTableNames(baseTabNames);
            this.setDb4ShortBaseTableNames(db4ShortBaseTabNames);
            this.setDb4ShortBaseTableSchemaNames(db4ShortBaseTabSchemaNames);
        } catch (SQLException sqlEx) {              //$JL-EXC$
            msg = "Exception caught executing '" + stmt +
            "'. The exception was: " + sqlEx.getErrorCode() +
            ", " + sqlEx.getSQLState() + ": " + sqlEx.getMessage();
            cat.errorT(loc, msg);
            throw JddException.createInstance(sqlEx);
        }
        finally {
            try {
                rs.close();
                ps.close();
            } catch (Exception e) {            //$JL-EXC$
                cat.errorT(loc, "Exception caught closing statement resources:\n {0}",
                                new Object[] {e.getMessage()});
                loc.exiting();
                throw JddException.createInstance(e);
            }
        }
        loc.exiting();
    }


    /**
     * Gets the create statement of this view from the database and 
     * sets the corresponding variable with method setCreateStatement 
     * @exception JddException - error during detection detected     
     **/  
    public void setCreateStatementViaDb() throws JddException {
        loc.entering(cat, "setCreateStatementViaDb()");
        Connection con = null;
        PreparedStatement ps = null; 
        ResultSet rs = null;
        String viewName = null;
        String msg = null;
        String select = null;
        String stmt         = "SELECT CURRENT_SCHEMA, VIEW_DEFINITION FROM SYSVIEWS " +
                                  "WHERE TABLE_NAME = ?";
        String fallbackStmt = "SELECT CURRENT_SCHEMA, VIEW_DEFINITION FROM QSYS2.VIEWS " +
                                  "WHERE TABLE_NAME = ?";
        String stmtStr = null;
        int pos = 0;
        String oldName = null;
        String sel1 = null;
        String sel2 = null;
                             
        // ------------------------------------------------- //
        // Consistency Checks                                //
        // ------------------------------------------------- //
        if ( (viewName = this.getName()) == null) {
            msg = "No view name provided.";
            cat.errorT(loc, msg);
            loc.exiting();
            throw new JddException(ExType.OTHER, msg);
        }
        viewName = viewName.trim().toUpperCase();
        
        if (this.getDbFactory() == null ||
           (con = this.getDbFactory().getConnection()) == null) {
            msg = "No connection provided.";
            cat.errorT(loc, msg);
            loc.exiting();
            throw new JddException(ExType.OTHER, msg);
        } 
        
        // ------------------------------------------------- //
        // Try getting data from local sys catalog view for  //
        // performance reasons.
        // ------------------------------------------------- //
        try {
            stmtStr = stmt;
            ps = NativeSQLAccess.prepareNativeStatement(con, stmtStr);
            ps.setString(1, viewName);
            rs = ps.executeQuery();
            if (!rs.next()) {
                this.setCreateStatement(null);
                loc.debugT(cat, "No view text found.");
                            loc.exiting();
                            return;
            } else {
                this.currentSchema = rs.getString(1).trim();
                select = rs.getString(2).trim();
                loc.debugT(cat, "Unmodfied SELECT portion from SYSVIEWDEP: {0}",
                        new Object[] {select});
            }
            // ------------------------------------------------- //
            // Statement is truncated (> 10,000 chars). Starting //
            // with V5R3, we can get it from QSYS2.VIEWS         //
            // ------------------------------------------------- //
            if ( (select == null) || (select.length() == 0) ) {
                stmtStr = fallbackStmt;
                ps = NativeSQLAccess.prepareNativeStatement(con, stmtStr);
                ps.setString(1, viewName);
                rs = ps.executeQuery();
                if (rs.next()) {
                    this.currentSchema = rs.getString(1).trim();
                    select = rs.getString(2).trim();
                }
                loc.debugT(cat, "Unmodfied SELECT portion from QSYS2.VIEWS: {0}",
                        new Object[] {select});
                // ------------------------------------------------- //
                // View definition still null. This should not occur //  
                // on DB releases >= V5R3.                           //
                // ------------------------------------------------- //
                if ( (select == null) || (select.length() == 0) ) {
                msg = "Unable to retrieve view text " +                      "for view " + viewName + ".";
                cat.errorT(loc, msg);
                loc.exiting();
                throw new JddException(ExType.OTHER, msg);
                }
            }
            
            // ------------------------------------------------- //
            // Replace system table names by SQL names and       //
            // remove schema names.
            // ------------------------------------------------- //
            if (this.getDb4ShortBaseTableNames() == null) {
                loc.debugT(cat, "Retrieve base table names.");
                this.setBasetableNamesViaDb();
            }
            ArrayList oldTableNames       = this.getDb4ShortBaseTableNames();
            ArrayList oldTableSchemaNames = this.getDb4ShortBaseTableSchemaNames();
            ArrayList newTableNames       = this.getBaseTableNames();
            
            for (int i=0; i<oldTableNames.size(); i++) {
                oldName = oldTableSchemaNames.get(i) + "." + oldTableNames.get(i);
                while (  (pos = select.indexOf(oldName)) != -1  ) {
                    sel1 = select.substring(0, pos);
                    sel2 = select.substring(pos + oldName.length());
                    select = sel1 + (String) newTableNames.get(i) + sel2;
                }
            }
            
            // ------------------------------------------------- //
            // Add CREATE clause                                 //
            // ------------------------------------------------- //
            this.setCreateStatement ("CREATE VIEW " + viewName + " AS " + select); 
            loc.debugT(cat, "Reconstructed " + this.getCreateStatement());
            loc.exiting();
        } catch (SQLException sqlEx) {      //$JL-EXC$
            msg = "SQLException caught executing '" + stmtStr +
                  "'. The exception was: " + sqlEx.getErrorCode() +
                  ", " + sqlEx.getSQLState() + 
                  ": " + sqlEx.getMessage();
            cat.errorT(loc, msg);
            loc.exiting();
            throw JddException.createInstance(sqlEx);
        } finally {
            try {
                rs.close();
                ps.close();
            } catch (Exception e) {            //$JL-EXC$
                cat.errorT(loc, "Exception caught closing statement resources:\n {0}",
                                new Object[] {e.getMessage()});
                loc.exiting();
                throw JddException.createInstance(e);
            }
        }
        loc.exiting();
    }     


    // ----------------
    // Private Methods ---------------------------------------------------------
    // ----------------
    

    /**
     * Gets list of short iSeries system table names. If available, its order 
     * matches the one of getBaseTableNames().    
     */
    private ArrayList getDb4ShortBaseTableNames() {
        return this.db4ShortBaseTableNames;
    }

    /**
     * Sets the list of short iSeries system table names. Its order must match 
     * the one of getBaseTableNames().    
     */
    private void setDb4ShortBaseTableNames(ArrayList sysNames) {
        this.db4ShortBaseTableNames = sysNames;
    }

    /**
     * Gets list of iSeries system table schema names. If available, its order 
     * matches the one of getBaseTableNames().    
     */
    private ArrayList getDb4ShortBaseTableSchemaNames() {
        return this.db4ShortBaseTableSchemaNames;
    }

    /**
     * Sets the list of iSeries system table schema names. Its order must match 
     * the one of getBaseTableNames().    
     */
    private void setDb4ShortBaseTableSchemaNames(ArrayList sysSchemaNames) {
        this.db4ShortBaseTableSchemaNames = sysSchemaNames;
    }
    
    /**
     * Determines whether we operate on the shadow library SHDsidDB.
     */
    private boolean isShadowSchema(Connection con) throws JddException {
        loc.entering(cat, "isShadowSchema()");
        String stmtStr = "SELECT CURRENT_SCHEMA FROM SYSTABLES WHERE TABLE_NAME = 'SYSTABLES'";
        String msg = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isShadow = false;
        
        // ------------------------------------------------- //
        // Current schema has not been determined yet.       //
        // ------------------------------------------------- //
        if (this.currentSchema == null) {
            try {
                pstmt = con.prepareStatement(stmtStr);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    this.currentSchema = rs.getString(1).trim();
                } else {
                    cat.errorT(loc, "Current schema could not be determined. Query: {0}",
                            new Object[] {stmtStr});
                    loc.exiting();
                    throw new JddException(ExType.OTHER, msg);
                }
            } catch (SQLException sqlEx) {      //$JL-EXC$
                msg = "SQLException caught executing '" + stmtStr +
                    "'. The exception was: " + sqlEx.getErrorCode() +
                    ", " + sqlEx.getSQLState() + 
                    ": " + sqlEx.getMessage();
                cat.errorT(loc, msg);
                loc.exiting();
                throw JddException.createInstance(sqlEx);
            } finally {
                try {
                    rs.close();
                    pstmt.close();
                } catch (Exception e) {            //$JL-EXC$
                    cat.errorT(loc, "Exception caught closing statement resources:\n {0}",
                                    new Object[] {e.getMessage()});
                    loc.exiting();
                    throw JddException.createInstance(e);
                }
            }
        }

        if (this.currentSchema.startsWith("SHD") && this.currentSchema.endsWith("DB")) {
            isShadow = true;
        } else {
            isShadow = false;
        }
        loc.exiting();
        return isShadow;
    }

}
