package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbTools;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.sql.NativeSQLAccess; 
import com.sap.sql.services.OpenSQLServices;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Title:        DbDb4Operation
 * Copyright:    Copyright (c) 2003
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */


public class DbDb4Tools extends DbTools {


    private static final Location loc = Logger.getLocation("db4.DbDb4Operation");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,
                                                             Logger.CATEGORY_NAME);

    //----------------
    //  constructors  ---------------------------------------------------------
    //----------------

    public DbDb4Tools(DbFactory factory) {
        super(factory);
    }


    //------------------
    //  public methods  -------------------------------------------------------
    //------------------

    /**
     * Renames a table on the database. If no exception is send, the table
     * could be renamed.
     * @param sourceName - current name of table
     * @param destinationName - new name of table
     * @exception JddException - The following error-situations should be
     *                  distinguished by the exception's ExType:
     *            ExType.NOT_ON_DB: Source-table does not exist on database
     *            ExType.EXISTS_ON_DB: Destination table already exists. 
     *            Every other error should be send with ExType.SQL_ERROR or
     *            ExType.OTHER.
     **/
    public void renameTable(String sourceName, String destinationName) 
                                                            throws JddException {
        loc.entering(cat, "renameTable({0}, {1})", new Object[] {sourceName, destinationName});   
        Connection con = null;
        Statement stmt = null;
        String srcName = null;
        String destName = null; 
        String stmtStr = null;
        String msg = null;

        try {
            srcName = sourceName.trim().toUpperCase();
            destName = destinationName.trim().toUpperCase();
            stmtStr = "RENAME TABLE " + "\"" + srcName + "\" TO " + "\"" + destName + "\"";
            con = this.getFactory().getConnection();
            
            // Retrieve Native SQL Statement from Open SQL connection
            stmt = NativeSQLAccess.createNativeStatement(con);
            stmt.executeUpdate(stmtStr);
        } catch (SQLException sqlEx) {
            if (sqlEx.getErrorCode() == -204) {
                msg = "Table " + srcName + " does not exist.";
                cat.errorT(loc, msg);
                throw new JddException(ExType.NOT_ON_DB, msg, sqlEx);
            } else if (sqlEx.getErrorCode() == -601) {
                msg = "Table " + srcName + " can not be renamed to " + destName + ". " + 
                      "An object with that name already exists.";
                cat.errorT(loc, msg);
                throw new JddException(ExType.EXISTS_ON_DB, msg, sqlEx);
            } else {
                msg = "SQLException caught executing " + stmtStr +
                      ". The exception was: " + sqlEx.getErrorCode() +
                      ", " + sqlEx.getSQLState() + 
                      ": " + sqlEx.getMessage() + ".";
                cat.errorT(loc, msg);
                throw JddException.createInstance(sqlEx);
            }
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {            //$JL-EXC$
                cat.warningT(loc, 
                             "Exception caught closing statement resources:\n {0}",
                             new Object[] {e.getMessage()});
                loc.exiting();
                throw JddException.createInstance(e);
            }
        }
        loc.exiting();
    }
    
  /**
   * Examines if a given table name is an alias.
   * @param tableName - current name of table
   * @exception JddException - The following error-situations should be
   *                           distinguished by the exception's ExType:
   *            ExType.SQL_ERROR: Object with tableName could not be examined 
   **/
    public boolean isAlias(String tableName) throws JddException {

        loc.entering(cat, "isAlias()");
        boolean isAlias = false;
        Connection con = con = this.getFactory().getConnection();
        PreparedStatement pstmt = null;
        
        tableName = tableName.trim().toUpperCase();
        String stmt = "SELECT TABLE_TYPE FROM SYSTABLES WHERE TABLE_NAME = ?";
        try {
            pstmt = NativeSQLAccess.prepareNativeStatement(getFactory().getConnection(), stmt);
            pstmt.setString(1, tableName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getString(1).trim().equalsIgnoreCase("A")) {
                    isAlias = true;
                }
            }
            rs.close();
            pstmt.close();
        } catch (SQLException ex) {
            try {
                // If no alias with that name exists, ignore the error
                if (!OpenSQLServices.objectUnknownSQLError(con, ex)) {
                    JddException.log(ex, cat, Severity.ERROR,loc);
                    throw new JddException(ExType.SQL_ERROR, ex.getMessage());
                }
            } catch (SQLException e) {
                JddException.log(ex, cat, Severity.ERROR, loc);
                throw new JddException(ExType.SQL_ERROR, ex.getMessage());
    	    }
        } finally {
            loc.debugT(cat, "isAlias() for {0} returns {1}.", new Object[] {tableName, new Boolean(isAlias)});
            loc.exiting();
        }
        return isAlias;
    }
    
    /** 
     * Checks what kind of tablelike database object corresponds to name. It is checked
     * if we have an alias or a view on database with the given name. If this is the case the result 
     * is delivered as an integer. In all other cases (including object is a table 
     * on database or object does not exist at all) the return value is DBTools.TABLE (== 0).
     * @param name Name of object to check
     * @return DbTools.VIEW, if object is a view on database,
     *         DbTools.ALIAS, if object is an Alias on database,
     *         DbTools.TABLE in all other cases
     * @exception JddException is thrown if error occurs during analysis        
    **/
    public int getKindOfTableLikeDbObject(String name) throws JddException{
    	
        loc.entering(cat, "getKindOfTableLikeDbObject()");
        PreparedStatement pstmt = null;
        int type = DbTools.TABLE;
        
        name = name.trim().toUpperCase();
        String stmt = "SELECT TABLE_TYPE FROM SYSTABLES WHERE TABLE_NAME = ?";
        try {
            pstmt = NativeSQLAccess.prepareNativeStatement(getFactory().getConnection(), stmt);
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                if (rs.getString(1).trim().equalsIgnoreCase("A")) {
                	type = DbTools.ALIAS;
                } else if (rs.getString(1).trim().equalsIgnoreCase("V")) {
                	type = DbTools.VIEW;
                }
            } 
            rs.close();
        } catch (SQLException ex) {
            	JddException.log(ex, cat, Severity.ERROR,loc);
                throw new JddException(ExType.SQL_ERROR, ex.getMessage());
        } finally {
        	try {
        		pstmt.close();
       			loc.debugT(cat, "Table type of {0} is {1}.", new Object[] {name, new Integer(type)});
        		loc.exiting();
        	} catch (SQLException e) {}
        }
        return type;
    	
    }



}