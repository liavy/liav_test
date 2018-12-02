package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndexColumnInfo;
import com.sap.dictionary.database.dbs.DbPrimaryKey;
import com.sap.dictionary.database.dbs.DbPrimaryKeyDifference;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Title:        DbDb4PrimaryKey
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */


public class DbDb4PrimaryKey extends DbPrimaryKey {

    /*
     * Note: Primary keys are not recognized by their name,
     * only by the table they belong to
     */

    private static final Location loc = Logger.getLocation("db4.DbDb4PrimaryKey");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,
                                                             Logger.CATEGORY_NAME);

    //----------------
    //  constructors  ---------------------------------------------------------
    //----------------
    
    public DbDb4PrimaryKey() {}
  
    public DbDb4PrimaryKey(DbFactory factory, DbPrimaryKey other) { 
        super(factory, other);
    }
  
    public DbDb4PrimaryKey(DbFactory factory) {
        super(factory);
    }

    public DbDb4PrimaryKey(DbFactory factory, String tableName) {
        super(factory, tableName);
    }
  
    public DbDb4PrimaryKey(DbFactory factory, 
                            DbSchema schema, 
                            String tableName) {
        super(  factory,
                schema,
                tableName);
    }

    //------------------
    //  public methods  -------------------------------------------------------
    //------------------
    
    /**
     *  Reads the primary key specific parameters out of the XmlMap and fills the
     *  corresponding database-dependent variables 
     *  @param xmlMap               the primary-key-XmlMap containing the values
     *                                for the specific properties    
     * */  
    public void setSpecificContentViaXml(XmlMap xml) {
        // Not yet needed.
        this.setSpecificIsSet(true);
        loc.debugT(cat, "setSpecificContentViaXml(XmlMap) entered.");
    }
  
    /**
     *  Reads the primary-information from the database and filles the variable
     *  columnsInfo.
     *  To set these variables the method setContent can be used from this 
     *  class  
     * */
    public void setCommonContentViaDb() throws JddException {
        loc.entering(cat, "setCommonContentViaDb()");
        DbFactory factory = null;
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList fieldList = new ArrayList();
        String tableName = null;
        
        // Functionally ok, but too slow. Sort afterwards
        String stmtStr = "SELECT T2.COLUMN_NAME, T2.ORDINAL_POSITION " + 
                    "FROM SYSCST AS T1 " + 
                    "JOIN SYSKEYCST AS T2 " +
                    "ON T1.CONSTRAINT_NAME = T2.CONSTRAINT_NAME " +
                    "WHERE T1.CONSTRAINT_TYPE = 'PRIMARY KEY' " + 
                        "AND T1.TABLE_NAME = ? " +
                    "ORDER BY T2.ORDINAL_POSITION";

        if (this.getTableName() == null) {
            cat.errorT(loc, "Empty table name.");
            loc.exiting();
            throw new JddException(ExType.OTHER, "Empty index name.");
        }
        tableName = this.getTableName().trim().toUpperCase();
                            
        if (this.getDbFactory() == null ||
            (con = this.getDbFactory().getConnection()) == null) {
            cat.errorT(loc, "No connection.");
            loc.exiting();
            throw new JddException(ExType.OTHER, "No connection.");
        } 

        try {
            // Retrieve Native SQL PreparedStatement from Open SQL connection
            pstmt = NativeSQLAccess.prepareNativeStatement(con, stmtStr);
            pstmt.setString(1, tableName);
            rs = pstmt.executeQuery();
            boolean noRows = true;
            while (rs.next()) { 
                // No ASC/DESC support for primary keys, thus "false"
                fieldList.add(new DbIndexColumnInfo(rs.getString(1).trim().toUpperCase(), 
                                                    false));
                noRows = false;
            }
            if (noRows) {
                cat.warningT(loc, "Primary key for table {0} does not exist on database.",
                    new Object[] {tableName});
                // Is this the behaviour expected by a caller???
                this.setContent(null);  // isSet=true
            } else {
                this.setContent(fieldList);
            }
        } catch (SQLException sqlEx) {      //$JL-EXC$
            String msg = "SQLException caught executing '" + stmtStr +
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
     *  Reads the primary key-specific information from the database and filles the 
     *  corresponding variables which are database dependent.   
     * */
    public void setSpecificContentViaDb() throws JddException {
        // Not yet needed.
        this.setSpecificIsSet(true);
    }  

    /**
     *  Generates the create-statement for primary key. A primary key
     *  has no reserved name!
     *  @return                      The create-statement
     *                               null in case of missing arguments      
     * */
    public DbObjectSqlStatements getDdlStatementsForCreate() {
        /*
         * !!!WARNING: As long as there is no way to query "isSet" this method might
         * return incorrect ddl statements in case a field list was incompletely 
         * set. (Note that this does not happen if setContent() was used.
         * Thus safe: setCommonContentViaDb().
         */
         
        loc.entering(cat, "getDdlStatementsForCreate()");
        String tableName = null;
        
        // Check imput
        if ((tableName = this.getTableName()) == null) {
            cat.errorT(loc, "Empty table name.");
            loc.exiting();
            return null;
        }
        tableName = tableName.trim().toUpperCase();

        DbObjectSqlStatements crtPrimKeyStmts = 
                    new DbObjectSqlStatements(tableName);
        DbSqlStatement crtPrimKeyStmt = new DbSqlStatement();
        
        // Generate field list
        if (this.getColumnNames().isEmpty()) {
            cat.errorT(loc, "Empty field list for primary key of table {0}.",
                    new Object[] {tableName});
            loc.exiting();
            return null;
        } 

        // Build statement. Let DB generate prim key names:
        // usually "QSYS_<table>_00001". (cur. max 127 chars for both)
        crtPrimKeyStmt.addLine("ALTER TABLE " + "\"" + tableName + "\"" + " ");
        crtPrimKeyStmt.addLine("ADD PRIMARY KEY ");
        crtPrimKeyStmt.merge(this.getDdlColumnsClause());

        // Add statement to "list"
        crtPrimKeyStmts.add(crtPrimKeyStmt);

        loc.debugT(cat, "Generated: {0}.", 
                new Object[] {crtPrimKeyStmts.toString()});
        loc.exiting();
        return crtPrimKeyStmts;
    }
  
    /**
     *  Generates the drop-statement for primary key. A primary key
     *  has no reserved name!
     *  @return                      The drop-statement     
     * */
    public DbObjectSqlStatements getDdlStatementsForDrop() {
        /*       
         * Returns null in case of missing arguments
         */
        loc.entering(cat, "getDdlStatementsForDrop()");
        String tableName = null;

        if ((tableName = this.getTableName()) == null) {
            cat.errorT(loc, "Empty table name.");
            loc.exiting();
            return null;
        }
        tableName = tableName.trim().toUpperCase();

        DbObjectSqlStatements dropPrimKeyStmts = new DbObjectSqlStatements(tableName);
        DbSqlStatement dropPrimKeyStmt = new DbSqlStatement(true);
                            
        // Build statement
        dropPrimKeyStmt.addLine("ALTER TABLE " + "\"" + tableName + "\" ");
        dropPrimKeyStmt.addLine("DROP PRIMARY KEY");    
        
        // add "CASCADE" later when foreign keys are allowed

        
        dropPrimKeyStmts.add(dropPrimKeyStmt);

        loc.debugT(cat, "Generated: {0}", 
                new Object[] {dropPrimKeyStmts.toString()});
        loc.exiting();
        return dropPrimKeyStmts;
    }


	public DbSqlStatement getDdlColumnsClause() {
		String line = "";
		Iterator iter = getColumnNames().iterator();
		DbSqlStatement colDef = new DbSqlStatement();

		colDef.addLine("(");
		while (iter.hasNext()) {
			DbIndexColumnInfo dbIndexColumnInfo = (DbIndexColumnInfo) iter.next();
			line = "\"" + dbIndexColumnInfo.getName() + "\"";
			if (iter.hasNext()) {
				line = line + ", ";
			}
			colDef.addLine(line);
		}
		colDef.addLine(")");

		return colDef;
	}
  
    /**
     *  Compares this primary key to its target version. The database-dependent
     *  comparison is done here, the specific parameters have to be compared
     *  in the dependent part
     *  @param target               the primary key's target version 
     *  @return the difference object for this primary key  
     * */
    public DbPrimaryKeyDifference compareTo(DbPrimaryKey target) throws JddException {
        loc.entering(cat, "compareTo(DbPrimaryKey) for table {0}", 
                            new Object[] {target.getTableName()});
        DbPrimaryKeyDifference keyDiff = super.compareTo(target);
/*      if (this.getSpecificIsSet()) {
            // Add comparisons for specifics. - Not needed yet.
        }
*/      loc.exiting();
        return keyDiff;  
    }   


    /**
     *  Check the primaryKeys's-width 
     *  @return true - if primary Key-width is o.k
     **/  
    public boolean checkWidth() {
        loc.entering(cat, "checkWidth()");
        boolean widthOk = true;
        int keyWidth = 0;
        int numberOfColumns = 0;
        int sqlType = 0;
        DbDb4Column col = null;
        int multiplier = 1;
        DbDb4Columns tableColumns = (DbDb4Columns) this.getTable().getColumns();
        
        for (int i=0; i<this.getKeyCnt(); i++) {
            col = (DbDb4Column) tableColumns.getColumn(
                        this.getKeyFieldName(i+1).trim().toUpperCase());
            numberOfColumns++;
            multiplier = 1;
            switch (col.getJavaSqlType()) {
                case Types.VARCHAR :
                case Types.LONGVARCHAR :        //$JL-SWITCH$
                    multiplier = 2;
                case Types.VARBINARY :
                case Types.LONGVARBINARY :
                    keyWidth += multiplier * col.getLengthOrDdlDefaultLength() + 2;
                    break;
                case Types.BINARY :
                    keyWidth += multiplier * col.getLengthOrDdlDefaultLength();
                    break;
                case Types.SMALLINT :
                    keyWidth += 2;
                    break;
                case Types.INTEGER :
                    keyWidth += 4;
                    break;
                case Types.BIGINT :
                    keyWidth += 8;
                    break;
                case Types.REAL :
                    keyWidth += 4;
                    break;
                case Types.DOUBLE :
                    keyWidth += 8;
                    break;
                case Types.DATE :
                    keyWidth += 10;
                    break;
                case Types.TIME :
                    keyWidth += 8;                  
                    break;
                case Types.TIMESTAMP :
                    keyWidth += 26;
                    break;
                case Types.DECIMAL :
                    keyWidth += (col.getLengthOrDdlDefaultLength() / 2) + 1;
                    break;
                case Types.CLOB :
                case Types.BLOB :
                default :
                    cat.errorT(loc, "Invalid type {0}.", 
                        new Object[] {new Integer(col.getJavaSqlType())});
                    widthOk = false;
            }
        }
        
        // Add 8 byte bytetmaps for null value handling
        keyWidth += (numberOfColumns + 7) / 8 * 8;
        
        /*
         * Always add 64 bytes overhead per row to be on the safe side. 
         * The overhead includes
         *   -  1 byte dentation
         *   -  In case there are varying length types (VARCHAR, VARGRAPHIC, BLOB, DBCLOB)
         *        -  up to 15 bytes to align varying length data to a 16 byte boundary
         *        -  pointer to varying length data
         *        -  ... ?
         */
        keyWidth += 64;
        
        if (keyWidth > DbDb4Environment.getMaxIndexWidthBytes()) {
            widthOk = false;        
        }
        DbDb4Environment.traceCheckResult(true, widthOk, cat, loc, 
                    "Width of primary key for table {0}: {1} ({2}) - return {3}.", 
                    new Object[] {this.getTableName(),
                                new Integer(keyWidth), 
                                new Integer(DbDb4Environment.getMaxIndexWidthBytes()), 
                                new Boolean(widthOk)});
        loc.exiting();
        return widthOk;
    }   
    
    /**
     *  Checks if number of primary key-columns maintained is allowed
     *  @return true if number of primary-columns is correct, false otherwise
     **/
    public boolean checkNumberOfColumns() {
        boolean isOk = (this.getColumnNames().size()  
                        <= DbDb4Environment.getMaxColumnsPerIndex())
                        ? true : false;
        DbDb4Environment.traceCheckResult(true, isOk, cat, loc, 
                                        "checkNumberOfColumns() returns {0}.", 
                                        new Object[] {new Boolean(isOk)});
        return isOk;
    }
   
    /**
     *  Checks if primary key-columns are not null 
     *  @return true - if number of primary-columns are all not null, 
     *                  false otherwise
     **/
    public boolean checkColumnsNotNull() {
        /*
         * DB4 allows primary keys with columns that are not tagged with 
         * "NOT NULL" as long as there are no NULL values. 
         * To avoid runtime errors during INSERT or late primary key
         * creation enable check for column property.
         */
        loc.entering(cat, "checkColumnsNotNull()");
        boolean allNotNull = true;
        ArrayList primKeyColumnNames = this.getColumnNames();
        DbColumns tableColumns = this.getTable().getColumns();
        DbColumn col = null;
        
        for (int i=0; i<this.getKeyCnt(); i++) {
            col = tableColumns.getColumn(
                    this.getKeyFieldName(i+1).trim().toUpperCase());
            allNotNull &= col.isNotNull();
        }
        DbDb4Environment.traceCheckResult(true, allNotNull, cat, loc, 
                                        "checkColumnsNotNull() returns {0}.", 
                                        new Object[] {new Boolean(allNotNull)});
        loc.exiting();
        return allNotNull;
    }


}