package com.sap.dictionary.database.sap;

import com.sap.dictionary.database.dbs.*;

import java.util.*;
import java.sql.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.sql.NativeSQLAccess;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Thomas Elvers
 * @version 1.0
 */


public class DbSapPrimaryKey extends DbPrimaryKey {

  private static Location loc = Logger.getLocation("sap.DbSapPrimaryKey");
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

  public DbSapPrimaryKey() {}

  public DbSapPrimaryKey(DbFactory factory, DbPrimaryKey other) {
    super(factory, other);
  }

  public DbSapPrimaryKey(DbFactory factory) {
    super(factory);
  }

  public DbSapPrimaryKey(DbFactory factory, DbSchema schema, String tablename) {
    super(factory, schema, tablename);
  }

  public DbSapPrimaryKey(DbFactory factory, String tablename) {
    super(factory, tablename);
  }

  public void setSpecificContentViaXml(XmlMap xml) {}

  public void setCommonContentViaDb() throws JddException {
    loc.entering("setCommonContentViaDb");
    Connection conn       = this.getDbFactory().getConnection();
    ArrayList  columnList = new ArrayList();
    
    String stmtStr = "SELECT COLUMNNAME FROM DOMAIN.COLUMNS WHERE SCHEMANAME = USER AND " +
                     "TABLENAME = ? and MODE = ? ORDER BY KEYPOS ";

    try {
      PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
      ps.setString (1, this.getTableName().toUpperCase());
      ps.setString (2, "KEY");
      java.sql.ResultSet rs = ps.executeQuery();

      while ( rs.next() ) {
        columnList.add(new DbIndexColumnInfo(rs.getString(1), false));
      }
      rs.close();
      ps.close();
    }
    catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "setCommonContentViaDb failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance(ex);
    }
    setContent(columnList);
    loc.exiting();
  }

  public void setSpecificContentViaDb() {}

  public void writeSpecificContentToXmlFile() {}

  public DbObjectSqlStatements getDdlStatementsForCreate() {
    loc.entering("getDdlStatementsForCreate");
    String columns = "\"" + ((DbIndexColumnInfo) super.getColumnNames().get(0)).getName().toUpperCase() + "\"";

    for(int column = 1; column < super.getColumnNames().size(); column ++)
      columns += ", \"" + ((DbIndexColumnInfo) super.getColumnNames().get(column)).getName().toUpperCase() + "\"";

    DbObjectSqlStatements dbObjectSqlStatements = new DbObjectSqlStatements(getTableName());
    DbSqlStatement        dbSqlStatement        = new DbSqlStatement();

    dbSqlStatement.addLine("ALTER TABLE \"" + getTableName().toUpperCase() + "\" ");
    dbSqlStatement.addLine("ADD PRIMARY KEY ( " + columns + " ) ");

    dbObjectSqlStatements.add(dbSqlStatement);
    loc.exiting();
    return dbObjectSqlStatements;
  }

  public DbObjectSqlStatements getDdlStatementsForDrop() {
    loc.entering("getDdlStatementsForDrop");
    DbObjectSqlStatements dbObjectSqlStatements = new DbObjectSqlStatements(getTableName());
    DbSqlStatement        dbSqlStatement        = new DbSqlStatement(true);

    dbSqlStatement.addLine("ALTER TABLE \"" + getTableName().toUpperCase() + "\" ");
    dbSqlStatement.addLine("DROP PRIMARY KEY");

    dbObjectSqlStatements.add(dbSqlStatement);
    loc.exiting();
    return dbObjectSqlStatements;
  }

  /**
   ** Check the primaryKeys's-width
   ** @return true - if primary Key-width is o.k
   **/
  public boolean checkWidth() {
    // compute length of one entry, compare against maximum (1024)
    loc.entering("checkWidth");
    Iterator iter = this.getColumnNames().iterator();
    String colName = null;
    DbColumns columns = this.getTable().getColumns();
    DbColumn column;
    int total = 0;
    int maxWidth = DbSapEnvironment.MaxKeyLength();

    while (iter.hasNext ()) {
      colName = ((DbIndexColumnInfo) iter.next()).getName();
      column = columns.getColumn(colName);

      if (column == null) {
        Object[] arguments = {this.getTableName()};
        cat.errorT(loc, "checkWidth {0}: no such column in table", arguments);
        loc.exiting();
        return false;
      }

      switch (column.getJavaSqlType()) {
        case java.sql.Types.BLOB:
        case java.sql.Types.CLOB:
        case java.sql.Types.LONGVARBINARY:
        case java.sql.Types.LONGVARCHAR:
          Object[] arguments = {this.getTableName()};
          cat.errorT(loc, "checkWidth {0}: index on BLOB, CLOB, LONGVARBINARY or LONGVARCHAR is not allowed", arguments);
          loc.exiting();
          return false; // not allowed in index/key
      }

      total += DbSapEnvironment.GetColumnLength(column, true);
    }

    if (total > maxWidth) {
      Object[] arguments = {this.getTableName(), new Integer(total), new Integer(maxWidth)};
      cat.errorT(loc, "checkWidth {0}: total width({1}) greater than allowed maximum ({2})", arguments);
      loc.exiting();
      return false;
    }
    loc.exiting();
    return true;
  }

  /**
   ** Checks if number of primary key-columns maintained is allowed
   ** @return true if number of primary-columns is correct, false otherwise
   **/
  public boolean checkNumberOfColumns() {
    loc.entering("checkNumberOfColumns");
    int numCols = this.getColumnNames().size();
    int maxCols = DbSapEnvironment.MaxKeysPerTable();

    if (numCols <= 0 || numCols > maxCols) {
      Object[] arguments = {this.getTableName(), new Integer(numCols), new Integer(maxCols)};
      cat.errorT(loc, "checkNumberOfColumns{0}: column count {1} not in allowed range [1..{2}]", arguments);
      loc.exiting();
      return false;
    }

    loc.exiting();
    return true;
  }
}
