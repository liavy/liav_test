package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndex;
import com.sap.dictionary.database.dbs.DbIndexColumnInfo;
import com.sap.dictionary.database.dbs.DbIndexDifference;
import com.sap.dictionary.database.dbs.DbObjectSqlStatements;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.DbSqlStatement;
import com.sap.dictionary.database.dbs.ExType;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Title:        DbDb4Index
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */


public class DbDb4Index extends DbIndex {


    private static final Location loc = Logger.getLocation("db4.DbDb4Index");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE, 
                                                             Logger.CATEGORY_NAME);
    
    //----------------
    //  constructors  ---------------------------------------------------------
    //----------------
    
    public DbDb4Index() {
        super();
    }

    public DbDb4Index(DbFactory factory) {
        super(factory);
    }
  
    public DbDb4Index(DbFactory factory, DbIndex other) {
        super(factory,other);
    }

    public DbDb4Index(DbFactory factory, String tableName, String name) {
        super(factory, tableName, name);
    }

    public DbDb4Index( DbFactory factory, 
                        DbSchema schema, 
                        String tabname, 
                        String name) {
        super(factory,
               schema,
               tabname,
               name);
    }

    //------------------
    //  public methods  -------------------------------------------------------
    //------------------
    
    public void writeSpecificContentToXmlFile(PrintWriter file, String offset0) {
        // EVI's
    }


   /**
    *  Reads the index specific parameters out of the XmlMap and fills the
    *  correspondig variables 
    *  @param xmlMap                the index-XmlMap containing the values
    *                                for the specific properties    
    * */
    public void setSpecificContentViaXml (XmlMap xmlMap) {
        // EVI's
        this.setSpecificIsSet(true);
        loc.debugT(cat, "setSpecificContentViaXml(XmlMap) entered.");
        
    }


   /**
    *  Reads the index-information from the database and filles the variables
    *  isUnique and columnsInfo.
    *  To set these variables the method setContent can be used from this 
    *  index-class  
    * */
    public void setCommonContentViaDb () throws JddException {
        loc.entering(cat, "setCommonContentViaDb()");
        boolean isUnique = false;
        boolean isDescending = false;
        ArrayList columnsInfo = new ArrayList();

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String indexName = null;
        
        /*
         * Functionally ok but with bad performance 
         * => replace ORDER BY with own sorting later
         */
        String stmtStr = "SELECT T1.IS_UNIQUE, T2.ORDERING, " +
                                "T2.COLUMN_NAME, T2.ORDINAL_POSITION " + 
                            "FROM SYSKEYS AS T2 " + 
                                "JOIN SYSINDEXES AS T1 " +
                                "ON T2.INDEX_NAME = T1.INDEX_NAME " +
                            "WHERE T1.INDEX_NAME = ? " +
                            "ORDER BY ORDINAL_POSITION";

        if ((indexName = this.getName()) == null) {
            cat.errorT(loc, "Empty index name.");
            loc.exiting();
            throw new JddException(ExType.OTHER, "Empty index name.");
        }
        indexName = indexName.trim().toUpperCase();

        if (this.getDbFactory() == null ||
            (con = this.getDbFactory().getConnection()) == null) {
            cat.errorT(loc, "No connection.");
            loc.exiting();
            throw new JddException(ExType.OTHER, "No connection.");
        } 
        
        try {
            boolean noRow = true;
            // Retrieve Native SQL PreparedStatement from Open SQL connection
            pstmt = NativeSQLAccess.prepareNativeStatement(con, stmtStr);
            pstmt.setString(1, indexName);
            rs = pstmt.executeQuery();
            while (rs.next()) { 
                if (noRow) {
                    // Retrieve global index information only once.
                    isUnique = (rs.getString(1).trim().equalsIgnoreCase("U")) ? true 
                                                                              : false;
                    noRow = false;
                }
                isDescending = (rs.getString(2).trim().equalsIgnoreCase("D")) ? true 
                                                                              : false;
                columnsInfo.add(new DbIndexColumnInfo(
                                    rs.getString(3).trim().toUpperCase(), 
                                    isDescending));
            }
            
            if (noRow) {
                cat.warningT(loc, "Index {0} does not exist on database.", 
                                                new Object[] {indexName});
                // Is this the behaviour expected by a caller or rather
                this.setContent(false, null); // isSet==true???
            } 
            else {
                this.setContent(isUnique, columnsInfo); // isSet==true
            }
        } catch (SQLException sqlEx) {      //$JL-EXC$
            String msg = "Exception caught executing '" + stmtStr +
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
            loc.exiting();
        } 
    }
    

    /**
    *  Reads the index-specific information from the database and filles the 
    *  corresponding variables which are database dependent.    
    * */
    public void setSpecificContentViaDb () throws JddException {
        // EVI's (read comment field) 
        this.setSpecificIsSet(true);
    }


    // Taken from dbs to call private getDdlColumnsClause(); 
    // quotes for tableName needed
    public DbObjectSqlStatements getDdlStatementsForCreate() {
        /*
         * !!!WARNING: As long as there is no way to query "isSet" this method might
         * return incorrect ddl statements in case a field list was incompletely 
         * set. (Note that this does not happen if setContent() was used.
         * Thus safe: setCommonContentViaDb().
         *       
         * Returns null in case of missing arguments
         */
        loc.entering(cat, "getDdlStatementsForCreate()");
        String tableName = null;
        String indexName = null;
        
        // Check imput
        if ((tableName = this.getTableName()) == null) {
            cat.errorT(loc, "Empty table name.");
            loc.exiting();
            return null;
        }
        tableName = tableName.trim().toUpperCase();
        
        if ((indexName = this.getName()) == null) {
            cat.errorT(loc, "Empty index name.");
            loc.exiting();
            return null;
        }
        indexName = indexName.trim().toUpperCase();
            

        DbObjectSqlStatements indexDef = new  DbObjectSqlStatements(indexName);
        DbSqlStatement createStatement = new DbSqlStatement();
        String unique = this.isUnique() ? "UNIQUE " : "";
        
        createStatement.addLine ("CREATE" + " " + unique + "INDEX " +
                             "\"" + indexName + "\"" +  
                             " ON " + "\"" + tableName + "\"" + " ");

        if (getDdlColumnsClause() == null) {
            cat.errorT(loc, "Missing field list.");
            loc.exiting();
            return null;
        }
        createStatement.merge(getDdlColumnsClause());

        indexDef.add(createStatement);
        loc.debugT(cat, "Generated: {0}", 
                new Object[] {createStatement.toString()});
        loc.exiting();
        return indexDef;
    }


    // Add asc/desc support to .dbs version
    public DbSqlStatement getDdlColumnsClause() {
        /*       
         * Method returns null if there are no columns.
         */
        loc.entering(cat, "getDdlColumnsClause()");
        String line = "";
        String descClause = "";
        boolean noColumns = true;
        Iterator iter = this.getColumnNames().iterator();
        DbSqlStatement colDef = new DbSqlStatement();

        colDef.addLine ("(");
        while (iter.hasNext()) {
            noColumns = false;
            DbIndexColumnInfo dbIndexColumnInfo = (DbIndexColumnInfo) iter.next();
            // JDD default = ascending = db default
            descClause = (dbIndexColumnInfo.isDescending()) ? "DESC " : "ASC ";
            line = "\"" + dbIndexColumnInfo.getName().trim().toUpperCase() + "\" " + descClause;
            if ( iter.hasNext() ) {
                line = line + ", ";
            }
            colDef.addLine (line);
        } while (iter.hasNext());
        colDef.addLine (")");

        if (noColumns) {
            cat.errorT(loc, "Missing field list.");
            loc.exiting();
            return null;
        }
        
        loc.exiting();
        return colDef;
    }
    

    // Taken from .dbs to get consistent logging.
    public DbObjectSqlStatements getDdlStatementsForDrop() {
        /*       
         * Method returns null in case of missing arguments
         */
        loc.entering(cat, "getDdlStatementsForDrop()");
        String indexName = null;
        
        if ((indexName = this.getName()) == null) {
            cat.errorT(loc, "Empty index name.");
            loc.exiting();
            return null;
        }
        indexName = indexName.trim().toUpperCase();
        
        DbObjectSqlStatements indexDef = new DbObjectSqlStatements(indexName);
        DbSqlStatement dropStatement = new DbSqlStatement(true);    // tolerate "Object not found"

        dropStatement.addLine("DROP INDEX " + "\"" + indexName + "\"");
        indexDef.add(dropStatement);
        
        loc.debugT(cat, "Generated: {0}.", 
                    new Object[] {dropStatement.toString()});
        loc.exiting();
        return indexDef;
    }


    /**
     *  Compares this index to its target version. The database-dependent
     *  comparison is done here, the specific parameters have to be compared
     *  in the dependent part
     *  @param target               the index's target version 
     *  @return the difference object for this index  
     * */
    public DbIndexDifference compareTo(DbIndex target) throws JddException {
        loc.entering(cat, "compareTo(DbIndex)"); 
        DbIndexDifference indexDiff = super.compareTo(target);
/*      if (this.getDbSpecificIsSet()) {
            // Add comparisons for specifics. - Not needed yet.
            // Everything including comparison on ASC/DESC, UNIQUE, positions
            // is done in .dbs version.
        }
*/      loc.exiting();
        return indexDiff;  
    }   


    /**
     *  Check the index's-width 
     *  @return true - if index-width is o.k
     **/  
    public boolean checkWidth() {
        loc.entering(cat, "checkWidth()");
        boolean widthOk = true;
        int indexWidth = 0;
        int numberOfColumns = 0;
        int sqlType = 0;
        int length = 0;
        int multiplier = 1;
        DbIndexColumnInfo info = null;
        DbDb4Column col = null;
        ArrayList indexColumnNames = this.getColumnNames();
        DbDb4Columns tableColumns = 
            (DbDb4Columns) this.getIndexes().getTable().getColumns();
        
        for (int i=0; i<indexColumnNames.size(); i++) {
            info = (DbIndexColumnInfo) indexColumnNames.get(i);
            col = (DbDb4Column) tableColumns.getColumn(
                                info.getName().trim().toUpperCase());
            numberOfColumns++;
            multiplier = 1;
            switch (col.getJavaSqlType()) {
                case java.sql.Types.VARCHAR :
                case java.sql.Types.LONGVARCHAR :       //$JL-SWITCH$ 
                    multiplier = 2;
                case java.sql.Types.VARBINARY :
                case java.sql.Types.LONGVARBINARY :
                    indexWidth += multiplier * col.getLengthOrDdlDefaultLength() + 2;
                    break;
                case java.sql.Types.BINARY :
                    indexWidth += multiplier * col.getLengthOrDdlDefaultLength();
                    break;
                case java.sql.Types.SMALLINT :
                    indexWidth += 2;
                    break;
                case java.sql.Types.INTEGER :
                    indexWidth += 4;
                    break;
                case java.sql.Types.BIGINT :
                    indexWidth += 8;
                    break;
                case java.sql.Types.REAL :
                    indexWidth += 4;
                    break;
                case java.sql.Types.DOUBLE :
                    indexWidth += 8;
                    break;
                case java.sql.Types.DATE :
                    indexWidth += 10;
                    break;
                case java.sql.Types.TIME :
                    indexWidth += 8;                    
                    break;
                case java.sql.Types.TIMESTAMP :
                    indexWidth += 26;
                    break;
                case java.sql.Types.DECIMAL :
                    indexWidth += (col.getLengthOrDdlDefaultLength() / 2) + 1;
                    break;
                case java.sql.Types.CLOB :
                case java.sql.Types.BLOB :
                default :
                    cat.errorT(loc, "Invalid type {0}.", 
                        new Object[] {new Integer(col.getJavaSqlType())});
                    widthOk = false;
            }
        }
        
        // Add 8 byte bytetmaps for null value handling
        indexWidth += (numberOfColumns + 7) / 8 * 8;
        
        /*
         * Always add 64 bytes overhead per row to be on the safe side. 
         * The overhead includes
         *   -  1 byte dentation
         *   -  In case there are varying length types (VARCHAR, VARGRAPHIC, BLOB, DBCLOB)
         *        -  up to 15 bytes to align varying length data to a 16 byte boundary
         *        -  pointer to varying length data
         *        -  ... ?
         */
        indexWidth += 64;
        
        if (indexWidth > DbDb4Environment.getMaxIndexWidthBytes()) {
            widthOk = false;        
        }
        DbDb4Environment.traceCheckResult(true, widthOk, cat, loc, 
                "Width of index {0}: {1} ({2}) - returns {3}.", 
                new Object[] { this.getName(),
                        new Integer(indexWidth), 
                        new Integer(DbDb4Environment.getMaxIndexWidthBytes()), 
                        new Boolean(widthOk)});
        loc.exiting();
        return widthOk;
    }   
    
    /**
     *  Check the index's name according to its length  
     *  @return true - if name-length is o.k
     **/  
    public boolean checkNameLength() {
        boolean isOk = (this.getName().trim().length() 
                        <= DbDb4Environment.getMaxIndexNameLength()) 
                        ? true : false;
        DbDb4Environment.traceCheckResult(true, isOk, cat, loc, 
                                            "checkNameLength() returns {0}.", 
                                            new Object[] { new Boolean(isOk) });
        return isOk;
    }   
    
    /**
     *  Checks if number of index-columns maintained is allowed
     *  @return true if number of index-columns is correct, false otherwise
     **/
    public boolean checkNumberOfColumns() {
        boolean isOk = (this.getColumnNames().size()  
                            <= DbDb4Environment.getMaxColumnsPerIndex())
                                ? true : false;
        DbDb4Environment.traceCheckResult(true, isOk, cat, loc, 
                                            "checkNumberOfColumns() returns {0}.", 
                                            new Object[] { new Boolean(isOk) });
        return isOk;
    }
   
    /**
     *  Checks if indexname is a reserved word
     *  @return true - if index-name has no conflict with reserved words, 
     *                    false otherwise
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
     *  Analyses if index exists on database or not
     *  @return true - if index exists on database, false otherwise
     *  @exception JddException - error during analysis detected   
     **/
    public boolean existsOnDb() throws JddException {
        loc.entering(cat, "existsOnDb()");
        boolean existsOnDb = true;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String indexName = null;
        String stmtStr = "SELECT INDEX_NAME FROM SYSINDEXES WHERE INDEX_NAME = ?";
        try {
            indexName = this.getName().trim().toUpperCase();
            con = this.getDbFactory().getConnection();
                
            // Retrieve Native SQL PreparedStatement from Open SQL connection
            pstmt = NativeSQLAccess.prepareNativeStatement(con, stmtStr);
            pstmt.setString(1, indexName);
            rs = pstmt.executeQuery();
            existsOnDb = rs.next() ? true : false;
        } catch (SQLException sqlEx) {            //$JL-EXC$
            String msg = "Exception caught executing '" + stmtStr +
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
                cat.errorT(loc, "Exception caught closing statement resources: {0}",
                                new Object[] {e.getMessage()});
                loc.exiting();
                throw JddException.createInstance(e);
                
            }
            loc.exiting();
        }
        loc.debugT(cat, "existsOnDb() returns {0}.", 
                            new Object[] {new Boolean(existsOnDb)});
        return existsOnDb;
    }

}
