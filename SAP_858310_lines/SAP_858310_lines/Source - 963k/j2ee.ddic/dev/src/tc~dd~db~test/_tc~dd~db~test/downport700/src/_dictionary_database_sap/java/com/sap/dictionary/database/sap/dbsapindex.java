package com.sap.dictionary.database.sap;

import java.util.*;
import com.sap.dictionary.database.dbs.*;
import java.sql.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.sql.NativeSQLAccess;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and Xml-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Thomas Elvers
 * @version 1.0
 */

public class DbSapIndex extends DbIndex {

  private static Location loc = Logger.getLocation("sap.DbSapIndex");
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

  public DbSapIndex() {
    super();
  }

  public DbSapIndex(DbFactory factory) {
    super(factory);
  }

  public DbSapIndex(DbFactory factory,DbIndex other) {
    super(factory,other);
  }

  public DbSapIndex(DbFactory factory, String tableName, String indexName) {
    super(factory,tableName,indexName);
  }

  public DbSapIndex(DbFactory factory, DbSchema schema, String tableName,
                    String indexName) {
    super(factory,schema,tableName,indexName);
  }

  public void setSpecificContentViaXml(XmlMap xmlMap) {}

  public void setCommonContentViaDb () throws JddException {
    loc.entering("setCommonContentViaDb");
    ArrayList          columnsInfo = new ArrayList();
    Connection         conn = this.getDbFactory().getConnection();
    PreparedStatement  ps;
    java.sql.ResultSet rs;
    boolean isUnique     = false;
    boolean isDescending = false;
    
    String stmtTypeStr = "SELECT TYPE FROM DOMAIN.INDEXES WHERE SCHEMANAME = USER AND " +
                         "INDEXNAME = ? and TABLENAME = ? ";

    String stmtSortStr = "SELECT COLUMNNAME, SORT FROM DOMAIN.INDEXCOLUMNS " + 
                         "WHERE SCHEMANAME = USER AND " +
                         "INDEXNAME = ? and TABLENAME = ? ORDER BY COLUMNNO ";

    try {
      ps = NativeSQLAccess.prepareNativeStatement(conn, stmtTypeStr);
      ps.setString (1, this.getName());
      ps.setString (2, this.getTableName().toUpperCase());
      rs = ps.executeQuery();

      if ( rs.next() ) {
        isUnique = rs.getString(1).equals("UNIQUE") ? true : false;
      }
      rs.close();
      ps.close();

      ps = NativeSQLAccess.prepareNativeStatement(conn, stmtSortStr);
      ps.setString (1, this.getName());
      ps.setString (2, this.getTableName().toUpperCase());
      rs = ps.executeQuery();

      while ( rs.next() ) {
        isDescending = rs.getString(2).equalsIgnoreCase("DESC") ? true : false;
        columnsInfo.add(new DbIndexColumnInfo(rs.getString(1), isDescending));
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
    setContent(isUnique, columnsInfo);
    loc.exiting();
  }

  public void setSpecificContentViaDb () {}

  public DbObjectSqlStatements getDdlStatementsForCreate() {
    loc.entering("getDdlStatementsForCreate");

    DbObjectSqlStatements indexDef = new  DbObjectSqlStatements(this.getName());
    DbSqlStatement createStatement = new DbSqlStatement();

    createStatement.addLine("CREATE " + (this.isUnique() ? "UNIQUE " : "") + "INDEX \"" +
                         this.getName() + "\" ON \"" + this.getTableName() + "\"");
    createStatement.merge(getDdlColumnsClause());
    indexDef.add(createStatement);

    loc.exiting();
    return indexDef;
  }

  public DbSqlStatement getDdlColumnsClause() {
    loc.entering("getDdlColumnsClause");

    DbSqlStatement colDef = new DbSqlStatement();

    colDef.addLine ("(");

    Iterator icIterator = this.getColumnNames().iterator();
    DbIndexColumnInfo indColumn = null;
    String sep = ",";
    while (icIterator.hasNext()) {
      indColumn = (DbIndexColumnInfo)icIterator.next();
      if (icIterator.hasNext() == false) sep = "";
      if (indColumn.isDescending())
        colDef.addLine("\"" + indColumn.getName() + "\" DESC" + sep);
      else
        colDef.addLine("\"" + indColumn.getName() + "\"" + sep);
    }

    colDef.addLine(")");
    loc.exiting();
    return colDef;
  }

  public DbObjectSqlStatements getDdlStatementsForDrop() {
    loc.entering("getDdlStatementsForDrop");

    DbObjectSqlStatements dropDef = new DbObjectSqlStatements(this.getName());
    DbSqlStatement dropLine = new DbSqlStatement(true);

    dropLine.addLine("DROP INDEX \"" + this.getName() + "\" ON \"" + this.getTableName() + "\"");
    dropDef.add(dropLine);

    loc.exiting();
    return dropDef;
  }

  /**
   ** Check the index's-width
   ** @return true - if index-width is o.k
   **/
  public boolean checkWidth() {
    // compute length of one entry, compare against maximum (1024)
    loc.entering("checkWidth");
    Iterator iter = this.getColumnNames().iterator();
    String colName = null;
    DbColumns columns = this.getIndexes().getTable().getColumns();
    DbColumn column;
    int total = 0;
    int maxWidth = DbSapEnvironment.MaxIndexLength();

    while (iter.hasNext ()) {
      colName = ((DbIndexColumnInfo) iter.next()).getName();
      column = columns.getColumn(colName);

      if (column == null) {
        Object[] arguments = {this.getName()};
        cat.errorT(loc, "checkWidth {0}: no such column in table", arguments);
        loc.exiting();
        return false;
      }

      switch (column.getJavaSqlType()) {
        case java.sql.Types.BLOB:
        case java.sql.Types.CLOB:
        case java.sql.Types.LONGVARBINARY:
        case java.sql.Types.LONGVARCHAR:
          Object[] arguments = {this.getName()};
          cat.errorT(loc, "checkWidth {0}: index on BLOB, CLOB, LONGVARBINARY or LONGVARCHAR is not allowed", arguments);
          loc.exiting();
          return false; // not allowed in index/key
      }

      total += DbSapEnvironment.GetColumnLength(column, true);
    }

    if (total > maxWidth) {
      Object[] arguments = {this.getName(), new Integer(total), new Integer(maxWidth)};
      cat.errorT(loc, "checkWidth {0}: total width({1}) greater than allowed maximum ({2})", arguments);
      loc.exiting();
      return false;
    }
    loc.exiting();
    return true;
  }

  /**
   *  Check the index's name according to its length
   *  @return true - if name-length is o.k
   * */
  public boolean checkNameLength() {
    loc.entering("checkNameLength");
    int nameLen = this.getName().length();
    int maxLen = DbSapEnvironment.MaxNameLength();

    if (nameLen > 0 && nameLen <= maxLen) {
      loc.exiting();
      return true;
    }
    else {
      Object[] arguments = {this.getName(), new Integer(nameLen), new Integer(maxLen)};
      cat.errorT(loc, "checkNameLength {0}: length {1} invalid (allowed range [1..{2}])", arguments);
      loc.exiting();
      return false;
    }
  }

  /**
   **  Checks if index-name is a reserved word
   **  @return true - if index-name has no conflict with reserved words,
   **                    false otherwise
   **/
  public boolean checkNameForReservedWord() {
    loc.entering("checkNameForReservedWord");
    boolean isReserved = DbSapEnvironment.isReservedWord(this.getName());

    if (isReserved == true) {
      Object[] arguments = {this.getName()};
      cat.errorT(loc, "{0} is a reserved word", arguments);
    }
    loc.exiting();
    return (isReserved == false);
  }

  /**
   *  Checks if number of index-columns maintained is allowed
   *  @return true if number of index-columns is correct, false otherwise
   * */
  public boolean checkNumberOfColumns() {
    loc.entering("checkNumberOfColumns");
    int numCols = this.getColumnNames().size();
    int maxCols = DbSapEnvironment.MaxColumnsPerIndex();

    if (numCols <= 0 || numCols > maxCols) {
      Object[] arguments = {this.getName(), new Integer(numCols), new Integer(maxCols)};
      cat.errorT(loc, "checkNumberOfColumns{0}: column count {1} not in allowed range [1..{2}]", arguments);
      loc.exiting();
      return false;
    }

    loc.exiting();
    return true;
  }
  
  /**
   *  Analyses if index exists on database or not
   *  @return true - if index exists in database, false otherwise
   * */
  public boolean existsOnDb() throws JddException {
    loc.entering("existsOnDb");
    
    boolean exists  = false;
    Connection conn = getDbFactory().getConnection();
    String stmtStr  = "SELECT 1 FROM DOMAIN.INDEXES WHERE SCHEMANAME = USER AND " +
                      "INDEXNAME = ? and TABLENAME = ? ";
                      
    try {
      PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
      ps.setString (1, this.getName());
      ps.setString (2, this.getTableName().toUpperCase());
      java.sql.ResultSet rs = ps.executeQuery();
      exists = rs.next() ? true : false; 
      rs.close();
      ps.close();
    }
    catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "existsOnDb failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance(ex);
    }    
    loc.exiting();
    return exists;
  }
  
}
