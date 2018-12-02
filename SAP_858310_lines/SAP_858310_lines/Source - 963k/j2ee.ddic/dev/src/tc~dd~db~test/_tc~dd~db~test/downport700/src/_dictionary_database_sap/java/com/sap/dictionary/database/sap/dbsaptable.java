package com.sap.dictionary.database.sap;

import com.sap.dictionary.database.dbs.*;
import java.sql.*;
import java.util.*;
import java.io.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.sql.NativeSQLAccess;

/**
 * Title:        DbSapTable
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Thomas Elvers
 */

public class DbSapTable extends DbTable {

  private static Location loc = Logger.getLocation("sap.DbSapTable");
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);

  public DbSapTable() {
    super();
  }

  public DbSapTable(DbFactory factory) {
    super(factory);
  }

  public DbSapTable(DbFactory factory, String name) {
    super(factory, name);
  }

  public DbSapTable(DbFactory factory, DbSchema schema, String name) {
    super(factory, schema, name);
  }

  public DbSapTable(DbFactory factory, DbTable other) {
    super(factory, other);
  }

  public void setTableSpecificContentViaXml(XmlMap xmlMap) {}

  public void setTableSpecificContentViaDb() {}
  
  /**
   *  Reads all columns for this table from database and creates an
   *  columns-object of class DbColumns for this table
   **/
  public void setColumnsViaDb(DbFactory factory) throws JddException {
    loc.entering("setColumnsViaDb");

    try {
      DbSapColumns cols = new DbSapColumns(factory);
      cols.setTable( this );
      cols.setContentViaDb( factory );
      setColumns( cols );
    }
    catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "setColumnsViaDb failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance(ex);
    }
    loc.exiting();
  }
  
  /**
   *  Reads all indexes for this table from database and creates an
   *  indexes-object of class DbIndexes for this table
   **/
  public void setIndexesViaDb() throws JddException {
    loc.entering("setIndexesViaDb");
    ArrayList names = new ArrayList();
    DbFactory factory = getDbFactory();
    Connection con = factory.getConnection();

    try {
      /* Get index names belonging to this table */
      names = dbGetIndexNames(con);
      if ( ! names.isEmpty() )
      {
        DbIndexes indexes = new DbIndexes(factory);
        for (int i=0; i < names.size(); i++)
        {
          String indexName = (String) names.get(i);
          DbSapIndex index = new DbSapIndex(factory, getSchema(), getName(), indexName);
          //Set parent
          index.setIndexes(indexes);
          index.setCommonContentViaDb();
          indexes.add(index);
        }
        setIndexes(indexes);
      }
    }
    catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "setIndexesViaDb failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance(ex);
    }
    loc.exiting();
  }
  
  /**
   *  Reads the primary key for this table from database and creates an
   *  primary key-object of class DbPrimaryKey for this table if the primary
   *  key exists
   **/
  public void setPrimaryKeyViaDb() throws JddException {
    loc.entering("setPrimaryKeyViaDb");
    String tabname = this.getName();
    DbSapPrimaryKey primaryKey = new DbSapPrimaryKey(getDbFactory(), tabname);

    try {
      primaryKey.setCommonContentViaDb();
      super.setPrimaryKey(primaryKey);
    }
    catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "setPrimaryKeyViaDb failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance(ex);
    }
    loc.exiting();
  }

  public DbObjectSqlStatements getDdlStatementsForCreate() throws JddException {
    //return super.getDdlStatementsForCreate();
    loc.entering("getDdlStatementsforCreate");       
    DbObjectSqlStatements tableDef = new DbObjectSqlStatements(this.getName());
    DbSqlStatement createLine = new DbSqlStatement();

    try {
      createLine.addLine("CREATE TABLE" + " " + "\"" + this.getName() + "\"");
      createLine.merge(this.getColumns().getDdlClause());
      tableDef.add(createLine);
    
      if (this.getPrimaryKey() != null) {
        tableDef.merge(this.getPrimaryKey().getDdlStatementsForCreate());
      }

      if (this.getIndexes() != null) {
        tableDef.merge(this.getIndexes().getDdlStatementsForCreate());
      }
      
      if (this.getColumns() != null) {
        DbColumnIterator iter = this.getColumns().iterator();
        DbSapColumn col;
        
        while (iter.hasNext()) {
          col = (DbSapColumn)iter.next();
          if ( col.getJavaSqlType() == java.sql.Types.LONGVARBINARY &&
               ! DbSapEnvironment.isSpecJ2EEColumn (this.getName(), col.getName(), col.getJavaSqlTypeName()) ) {    
            tableDef.merge(col.getDdlStatementsForLongVarbinary(this.getName()) );
          }
        }
      }
    }
    catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "getDdlStatementsForCreate failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance(ex);
    }
    loc.exiting();
    return tableDef;
  }
  
  /**
   *  Writes the table specific variables to an xml-document
   *  @param file              the destination file
   *  @param offset0           the base-offset for the outermost tag
   **/ 
  public void writeTableSpecificContentToXmlFile(PrintWriter file, String offset0) {
    //file.println(offset0 + "<db-spec>" + "</db-spec>");
  }

  private ArrayList dbGetIndexNames(Connection con) throws JddException {
    loc.entering("dbGetIndexNames");
    ArrayList names = new ArrayList();
    String stmtStr  = "SELECT INDEXNAME FROM DOMAIN.INDEXES WHERE " +
                      "SCHEMANAME = USER AND TABLENAME = ? AND " +
                      "INDEXNAME <> 'SYSPRIMARYKEYINDEX' ";
    
    try {
      PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(con, stmtStr);
      ps.setString (1, this.getName().toUpperCase());
      java.sql.ResultSet rs = ps.executeQuery();

      while ( rs.next() ) {
        names.add (rs.getString(1));
      }
      rs.close();
      ps.close();
    }
    catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "dbGetIndexNames failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance(ex);
    }
    loc.exiting();
    return names;
  }

  /**
   *  Analyses if table exists on database or not
   *  @return true - if table exists in database, false otherwise
   *  @exception JddException error during analysis detected   
   **/
  public boolean existsOnDb() throws JddException {
    loc.entering("existsOnDb");  
    boolean exists  = false;
    Connection conn = this.getDbFactory().getConnection();
    String stmtStr = "SELECT 1 FROM DOMAIN.TABLES " +
                     "WHERE SCHEMANAME = USER AND " +
                     "TABLENAME = ? AND TABLETYPE = ?";

    try {
      PreparedStatement  ps = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
      java.sql.ResultSet rs;
      
      ps.setString(1, this.getName().toUpperCase());
      ps.setString(2, "TABLE");
      rs = ps.executeQuery();
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

  /**
   *  Analyses if table has content 
   *  @return true - if table contains at least one record, false otherwise
   *  @exception JddException  error during analysis detected   
   **/
  public boolean existsData() throws JddException {
    loc.entering("existsData");
    
    boolean exists  = false;
    Connection conn = this.getDbFactory().getConnection();
    String stmtStr  = "SELECT 1 FROM \"" +  this.getName().toUpperCase() + "\" WHERE ROWNO <= 1";

    try {
      Statement          stmt = NativeSQLAccess.createNativeStatement(conn);
      java.sql.ResultSet rs   = stmt.executeQuery(stmtStr);
      exists = rs.next() ? true : false; 
      rs.close();
      stmt.close();
    }
    catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "existsData failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance(ex);
    }
    loc.exiting();
    return exists;
  }

  /**
   *  Delivers the names of views using this table as basetable
   *  @return The names of dependent views as ArrayList
   *  @exception JddException error during selection detected   
   **/
  public ArrayList getDependentViews() throws JddException  {
    loc.entering("getDependentViews");
    Connection conn  = this.getDbFactory().getConnection();
    ArrayList  names = new ArrayList();
    
    String stmtStr = "SELECT DISTINCT VIEWNAME FROM DOMAIN.VIEWCOLUMNS WHERE " +
                     "SCHEMANAME = USER AND TABLENAME = ? ";

    try {
      PreparedStatement ps = NativeSQLAccess.prepareNativeStatement(conn, stmtStr);
      ps.setString (1, this.getName().toUpperCase());
      java.sql.ResultSet rs = ps.executeQuery();

      while ( rs.next() ) {
        names.add(rs.getString(1));
      }
      rs.close();
      ps.close();
    }
    catch (Exception ex) {
      Object[] arguments = {ex.getMessage()};
      cat.errorT(loc, "getDependentViews failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance( ex );
    }

    loc.exiting();
    return names;
  }

  /**
   *  Check the table's name according to its length
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
   **  Checks if table-name is a reserved word
   **  @return true - if table-name has no conflict with reserved words,
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
   *  Check the table-width
   *  @return true - if table-width is o.
   * */
  public boolean checkWidth() {
    // compute width of one row in bytes and compare it against maximum
    loc.entering("checkWidth");
    DbColumns columns = this.getColumns();
    DbColumnIterator iter = columns.iterator();
    DbColumn column;
    int total = 0;
    DbSapEnvironment dbEnv = (DbSapEnvironment) this.getDbFactory().getEnvironment();
    int maxWidth = dbEnv.MaxTableLength();

    while (iter.hasNext ()) {
      column = (DbColumn)iter.next();
      total += DbSapEnvironment.GetColumnLength(column, false);
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
}
