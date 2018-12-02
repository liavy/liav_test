package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Title:        DbDb4Columns
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */


public class DbDb4Columns extends DbColumns {

    private static final Location loc = Logger.getLocation("db4.DbDb4Columns");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,
                                                             Logger.CATEGORY_NAME);
                                                             


    //----------------
    //  constructors  ----------------------------------
    //----------------

    public DbDb4Columns(DbFactory factory) {
        super(factory);
    }

    public DbDb4Columns (DbFactory factory, DbColumns other) {
        super(factory, other);
    }

    public DbDb4Columns(DbFactory factory, XmlMap xmlMap) throws Exception {
        super(factory, xmlMap);
    }


    //------------------
    //  public methods  ----------------------------------
    //------------------

    public void setContentViaDb(DbFactory factory) throws JddException {
        loc.entering(cat, "setContentViaDb(DbFactory)");
        String msg = null;
        Connection con = null;
        PreparedStatement ps = null; 
        ResultSet rs = null;
        String tableName = null;
        String viewName = null;
        String nativeId = null;
        String sqlId = null;
        boolean logged = false;
        // Sort the result later manually to avoid slow ORDER BY...
        String stmt = "SELECT T2.COLUMN_NAME, T2.DATA_TYPE, " + 
                        "T2.LENGTH, T2.NUMERIC_SCALE, T2.COLUMN_DEFAULT, " + 
                        "T2.ORDINAL_POSITION, T2.IS_NULLABLE, T2.CCSID, " +
                        "T1.TABLE_TYPE " +
                        "FROM SYSTABLES AS T1 JOIN SYSCOLUMNS AS T2 " + 
                        "ON T1.TABLE_NAME = T2.TABLE_NAME " + 
                        "WHERE T1.TABLE_NAME = ? " +
                             "ORDER BY T2.ORDINAL_POSITION";
                             
        // ------------------------------------------------- //
        // Are we dealing with a table or a view?            //
        // ------------------------------------------------- //
        if ( (this.getTable() != null) && 
             ((tableName = this.getTable().getName()) != null) ) {
            tableName = tableName.trim().toUpperCase();
            if (tableName.equals("")) tableName = null;
        }
        if ((this.getView() != null) && 
            ((viewName = this.getView().getName()) != null) ) { 
            viewName = viewName.trim().toUpperCase();
            if (viewName.equals("")) viewName = null;
        }
        if (tableName != null && viewName != null) {
            msg = "Conflicting information provided: " +
                  "tableName = " + this.getTable().getName() + ", " +
                  "viewName = " + this.getView().getName() + ".";
            cat.errorT(loc, msg);
            loc.exiting();
            throw new JddException(ExType.OTHER, msg); 
        }            
        if (tableName == null && viewName == null) {
            msg = "Neither view nor table name provided.";
            cat.errorT(loc, msg);
            loc.exiting();
            throw new JddException(ExType.OTHER, msg); 
        }
        // ------------------------------------------------- //
        // More consistency Checks...                        //
        // ------------------------------------------------- //
        if (factory == null || (con = factory.getConnection()) == null) {
            msg = "No connection.";
            cat.errorT(loc, msg);
            loc.exiting();
            throw new JddException(ExType.OTHER, msg);
        } 
        // Reset not possible. Cancel, if there are already columns.
        if (this.getColumnCnt() > 0) {
            cat.warningT(loc, "Columns are already set. - Return.");
            loc.exiting();
            return;
        }
        
        // ------------------------------------------------- //
        // Retrieve columns                                  //
        // ------------------------------------------------- //
        try {
            ps = NativeSQLAccess.prepareNativeStatement(con, stmt);
            if (tableName != null) {
                ps.setString(1, tableName);
                sqlId = "T";
                nativeId = "P";
            } else {
                ps.setString(1, viewName);
                sqlId = "V";
                nativeId = "L";
            }
            rs = ps.executeQuery();
            int numberOfColumns = 0;
            
            while (rs.next()) {

                // -------------------------------------------------------//
                // Name Collision Check, part I                           //      
                // -------------------------------------------------------//
                // Searched for table; found view, log. file, MQT.        //
                // Searched for view; found table, phys. file, view, MQT. //
                // => Don't recognize such matches.                       //
                // -------------------------------------------------------//
                if (!rs.getString(9).trim().equalsIgnoreCase(sqlId) &&
                    !rs.getString(9).trim().equalsIgnoreCase(nativeId)) {
                    cat.warningT(loc, "Treat object {0} of unexpected type {1} " +
                                      "as nonexisting. (Expected types: {2} or {3}).",
                                      new Object[] {sqlId, nativeId});
                    break;
                }
                // -------------------------------------------------------//
                // Name Collision Check, part II                          //      
                // -------------------------------------------------------//
                // Searched for table; found phys. file.                  //
                // Searched for view; found log. file.                    //
                // => Treat iSeries native objects equivalently           //
                // -------------------------------------------------------//
                if (!logged && !rs.getString(9).trim().equalsIgnoreCase(sqlId)) {
                    cat.warningT(loc, "Treat iSeries native object {0} of type {1} " +
                                      "as nonexisting. It is not an SQL object.",
                                      new Object[] {sqlId, nativeId});
                    logged = true;
                }
                 
                // -------------------------------------------------------//
                // Get and convert the data                               //      
                // -------------------------------------------------------//
                String colName = rs.getString (1).trim().toUpperCase();
                String dbType = rs.getString(2).trim().toUpperCase();
                int colSize = rs.getInt(3);
                int decDigits = rs.getInt(4);
                String defVal = rs.getString(5);
                int pos = rs.getInt(6);
                boolean isNotNull = 
                        rs.getString(7).trim().equalsIgnoreCase("N");
                int ccsid = rs.getInt(8);
                        
                // Map db type to sql type
                int sqlType = DbDb4Environment.mapToSqlType(dbType, colSize, ccsid);
                
                // Map db default value to JDDIC default value
                defVal = DbDb4Environment.mapToJddicDefaultValue(defVal, sqlType);
                
                DbDb4Column col 
                    = new DbDb4Column(  factory,
                                        colName, 
                                        pos, 
                                        sqlType, 
                                        dbType,
                                        colSize, 
                                        decDigits, 
                                        isNotNull, 
                                        defVal );
                
                this.add(col);
                numberOfColumns++;
            }
            
            if (numberOfColumns==0) {
                cat.warningT(loc, "Table {0} does not exist on database.", 
                            new Object[] {tableName});
            }
        } catch (SQLException sqlEx) {      //$JL-EXC$
            msg = "Exception caught executing '" + stmt +
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


    /**
     *  Checks if number of columns is allowed
     *  @return true if number of columns is o.k, false otherwise
     */
    public boolean checkNumber() {
        boolean numberOk = (this.getColumnCnt() 
                            > DbDb4Environment.getMaxColumnsPerTable())
                            ? false : true; 
        DbDb4Environment.traceCheckResult(true, numberOk, cat, loc, 
                                        "checkNumber() returns {0}.", 
                                        new Object[] { new Boolean(numberOk)});
        return numberOk;
    }
    
}