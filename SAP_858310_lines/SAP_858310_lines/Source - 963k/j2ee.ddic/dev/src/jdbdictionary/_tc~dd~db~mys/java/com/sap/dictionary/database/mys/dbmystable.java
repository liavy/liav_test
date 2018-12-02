package com.sap.dictionary.database.mys;

import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;

import com.sap.dictionary.database.dbs.*;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Title:       Analysis of table and view changes: MySQL specific classes
 * Description: MySQL specific analysis of table and view changes. Tool to
 *              deliver MySQL specific database information. 
 * Copyright:   Copyright (c) 2001
 * Company:     SAP AG
 * 
 * @author      Hakan Kuecuekyilmaz, <hakan at mysql dot com>, 2005-07-10
 * @version     1.0
 */

public class DbMysTable extends DbTable {
    private String dataFilegroup = "DEFAULT";
    private String textimageFilegroup = "DEFAULT";
    private static Location loc = Logger.getLocation("mys.DbMysTable");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
    
    
    public DbMysTable() {
        super();
    }

    public DbMysTable(DbFactory factory) {
        super(factory);
    }

    public DbMysTable(DbFactory factory, String name) {
        super(factory, name);
    }

    public DbMysTable(DbFactory factory, DbSchema schema, String name) {
        super(factory, schema, name);
    }

    public DbMysTable(DbFactory factory, DbTable other) {
        super(factory, other);
    }

    /**
     * Delivers the names of views using this table as basetable
     * 
     * @return The names of dependent views as ArrayList
     * @exception JddException
     *                error during selection detected
     */
    public ArrayList getDependentViews() throws JddException {
        ArrayList names = new ArrayList();
        Connection con = getDbFactory().getConnection();
        
        loc.entering("getDependentViews");
        // TODO: Evil parsing of "show create view"
        // See also WL #2776 VIEW_TABLE_USAGE view

        loc.exiting();
        return names;
    }
    
    /**
     *  Reads the table specific parameters out of the XmlMap and fills the
     *  corresponding database-dependend variables
     * 
     *  @param xmlMap   the table-XmlMap containing the values
     *                  for the specific properties    
     */
    public void setTableSpecificContentViaXml(XmlMap xmlMap) throws JddException {
        loc.entering("setTableSpecificContentViaXml");
        // TODO: Check which specific settings we need (hk)
        try {
            XmlMap dbspecMap = xmlMap.getXmlMap("db-spec");
            if (dbspecMap.isEmpty() == true)
                return;
            XmlMap tableMap = dbspecMap.getXmlMap("table");
            if (tableMap.isEmpty() == true)
                return;

            String xmlName = tableMap.getString("name");
            if (this.getName().equalsIgnoreCase(xmlName.trim()) == false) {
                throw new JddException(ExType.XML_ERROR, "table name "
                        + this.getName() + "differs from XML table name "
                        + xmlName);
            }

            // TODO: Do we have filegroup and textimage-filegroup?
//            XmlMap storage = tableMap.getXmlMap("storage-parameters");
//            if (!storage.isEmpty()) {
//                dataFilegroup = storage.getString("filegroup");
//                textimageFilegroup = storage.getString("textimage-filegroup");
//            }

            // primary key handling

            XmlMap primaryKeyMap = tableMap.getXmlMap("primary-key");
            DbPrimaryKey primaryKey = this.getPrimaryKey();
            if (primaryKey.isEmpty() == false && primaryKey != null) {
                ((DbMysPrimaryKey) primaryKey).setSpecificContentViaXml(primaryKeyMap);
            }

            XmlMap indexesMap = tableMap.getXmlMap("indexes");
            DbIndexes indexes = this.getIndexes();
            if (indexesMap.isEmpty() == false && indexes != null) {
                XmlMap nextIndexMap = null;
                DbIndex nextDbIndex = null;
                for (int i = 0; !(nextIndexMap = indexesMap.getXmlMap("index"
                        + (i == 0 ? "" : "" + i))).isEmpty(); i++) {
                    nextDbIndex = indexes.getIndex(nextIndexMap.getString("name"));
                    if (nextDbIndex == null) {
                        throw new JddException(ExType.XML_ERROR, "table "
                                + this.getName()
                                + ": XML describes unknown index "
                                + nextIndexMap.getString("name"));
                    }
                    ((DbMysIndex) nextDbIndex).setSpecificContentViaXml(nextIndexMap);
                }
            }
        } catch (Exception ex) {
            Object[] arguments = { ex.getMessage() };
            loc.errorT("setTableSpecificContentViaXml failed: {0}", arguments);
            loc.exiting();
            throw JddException.createInstance(ex);
        }
        loc.exiting();
    }


    /**
     * retrieve index informations from db
     */
    public void setIndexesViaDb() throws JddException {
        loc.entering("setIndexesViaDb");

        DbFactory factory    = getDbFactory();
        Connection con       = factory.getConnection();
        ArrayList indexNames = new ArrayList();

        String tabName = this.getName();

        // List index names excluding Primary Key
        try {
            String schema = factory.getSchemaName();
            
            Statement stmt = NativeSQLAccess.createNativeStatement(con);
            String query = "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS "
                         + "  WHERE TABLE_SCHEMA = '" + schema  + "' " 
                         + "    AND TABLE_NAME   = '" + tabName + "' "
                         + "    AND INDEX_NAME  != 'PRIMARY'";
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                indexNames.add(rs.getString(1));
            }
            
            if (!indexNames.isEmpty()) {
                DbIndexes indexes    = new DbIndexes(factory);

                for (int i = 0; i < indexNames.size(); i++) {
                    DbMysIndex index = 
                        new DbMysIndex(factory, getSchema(), tabName, (String) indexNames.get(i));
                    index.setIndexes(indexes);
                    index.setCommonContentViaDb();
                    indexes.add(index);
                }
                setIndexes(indexes);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Object[] arguments = { ex.getMessage()};
            cat.errorT(loc, "setIndexesViaDb failed: {0}", arguments);
            loc.exiting();
            throw JddException.createInstance(ex);
        }
        
        loc.exiting();
        return;
    }

    /**
     * retrieve primary key information from db
     */
    public void setPrimaryKeyViaDb() throws JddException {
        loc.entering("SetPrimaryKeyViaDb");
        
        DbFactory factory = getDbFactory();
        Connection con = factory.getConnection();
        
        String tabName = this.getName(); 
        String pkName = "";
        
        try {
            String schema = factory.getSchemaName();
            
            Statement stmt = NativeSQLAccess.createNativeStatement(con);
            String query = "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS "
                         + "  WHERE TABLE_SCHEMA = '" + schema  + "' " 
                         + "    AND TABLE_NAME   = '" + tabName + "' "
                         + "    AND INDEX_NAME   = 'PRIMARY'";
                
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                pkName = rs.getString(1);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            Object[] arguments = { ex.getMessage()};
            cat.errorT(loc, "setPrimaryKeyViaDb (det. of PK name) failed: {0}", arguments);
            loc.exiting();
            throw JddException.createInstance(ex);
        }        
        if (pkName.equals("") == false) {
            DbMysPrimaryKey primaryKey = new DbMysPrimaryKey(factory, this.getName());
            primaryKey.setCommonContentViaDb();

            super.setPrimaryKey(primaryKey);
        } else {
            super.setPrimaryKey(null);
        }

        loc.exiting();
        return;
    }

    /**
     * retrive MySQL specific table information from db
     */
    public void setTableSpecificContentViaDb() throws JddException {
        loc.entering("setTableSpecificContentViaDb");

        // Connection con = getDbFactory().getConnection();

        // TODO: Is there anything to set?

        super.setSpecificIsSet(true);

        loc.exiting();
        return;
    }
    
    /**
     * write specific data to XML
     * Currently we have nothing special 2005-09-15
     */
    public void writeTableSpecificContentToXmlFile(PrintWriter file, String offset0)
            throws JddException {
        loc.entering("writeTableSpecificContentToXmlFile");
//        try {
//            file.println(offset0 + XmlHelper.xmlTitle);
//            file.println(offset0 + "<db-spec>");
//            file.println(
//                   offset0 + "<table name=" + "\"" + this.getName() + "\"" + ">");
//
//            String offset1 = offset0 + XmlHelper.tabulate();
//            String offset2 = offset1 + XmlHelper.tabulate();
//
//
//            /*
//             * TODO: MySQL specific types?
//             * 
//             * getDbTableColumns().writeSpecificContentToXmlFile(file,offset1);
//             */
//
//            // primary key
//            DbPrimaryKey primaryKey = this.getPrimaryKey();
//
//            if (primaryKey != null) {
//                ((DbMysPrimaryKey) primaryKey).writeSpecificContentToXmlFile(
//                        file, offset1);
//            }
//
//            // indexes
//
//            DbIndexes indexes = this.getIndexes();
//
//            if (indexes != null) {
//                DbIndexIterator iterator = indexes.iterator();
//
//                if (iterator.hasNext()) {
//                    file.println(offset1 + "<indexes>");
//                    while (iterator.hasNext()) {
//                        ((DbMysIndex) iterator.next())
//                                .writeSpecificContentToXmlFile(file, offset2);
//                    }
//                    file.println(offset1 + "</indexes>");
//                }
//            }
//
//            file.println(offset0 + "</table>");
//            file.println(offset0 + "</db-spec>");
//        } catch (Exception ex) {
//            Object[] arguments = { ex.getMessage() };
//            loc.errorT("writeSpecificContentToXml failed: {0}", arguments);
//            loc.exiting();
//            throw JddException.createInstance(ex);
//        }

        loc.exiting();
        return;
    }

    /**
     * build DDL statement for CREATE TABLE
     */
    public DbObjectSqlStatements getDdlStatementsForCreate() throws JddException {
        loc.entering("getDdlStatementsForCreate");

        DbObjectSqlStatements tableDef = new DbObjectSqlStatements(this.getName());
        DbSqlStatement createLine = new DbSqlStatement();
        boolean doNotCreate = false;

        if (this.getDeploymentInfo() != null) {
            doNotCreate = this.getDeploymentInfo().doNotCreate();
        }
        if (!doNotCreate) {
            try {
                createLine.addLine("CREATE TABLE" + " " + "\"" + this.getName() + "\"");
                createLine.merge(this.getColumns().getDdlClause());
                tableDef.add(createLine);
                
                // We need utf8 tables
                // If one wants to test specific storage engine, this storage engine
                // should be set in my.cnf (default-storage-engine=InnoDB)
                createLine.addLine(" CHARACTER SET utf8 COLLATE utf8_bin");
                
                if (this.getIndexes() != null) {
                    tableDef.merge(this.getIndexes().getDdlStatementsForCreate());
                }
                if (this.getPrimaryKey() != null) {
                    tableDef.merge(this.getPrimaryKey().getDdlStatementsForCreate());
                }

                loc.exiting();
                return tableDef;
            } catch (Exception ex) {
                Object[] arguments = { ex.getMessage() };
                cat.errorT(loc, "getDdlStatementsForCreate failed: {0}",
                        arguments);
                loc.exiting();
                throw JddException.createInstance(ex);
            }
        }
        return null;
    }

    /**
     * existence check
     */
    public boolean existsOnDb() {
        loc.entering("existsOnDb");
        boolean exists = false;
        Connection con = getDbFactory().getConnection();

        try {
            String schema = getDbFactory().getSchemaName();
            
            Statement stmt = NativeSQLAccess.createNativeStatement(con);
            String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                         + "  WHERE TABLE_SCHEMA = '" + schema         + "' " 
                         + "    AND TABLE_NAME   = '" + this.getName() + "'";
                
            ResultSet rs = stmt.executeQuery(query);
            exists = (rs.next() == true);
            rs.close();
            stmt.close();
        } catch (SQLException sqlex) {
            Object[] arguments = { this.getName(), sqlex.getMessage() };
            loc.errorT("Existence check for table {0} failed: {1}", arguments);
            loc.exiting();
            sqlex.printStackTrace();
        } catch (Exception ex) {
            Object[] arguments = { this.getName(), ex.getMessage() };
            loc.errorT("Existence check for table {0} failed: {1}", arguments);
            loc.exiting();
            ex.printStackTrace();
        }

        Object[] arguments = { this.getName(), exists ? "exits " : "doesn't exist" };
        loc.infoT("Table {0} {1} on db", arguments);
        loc.exiting();
        return exists;
    }

    /**
     * check if table contains data
     */
    public boolean existsData() {
        loc.entering("existsData");
        boolean exists = false;
        Connection con = getDbFactory().getConnection();

        try {
            Statement stmt = NativeSQLAccess.createNativeStatement(con);
            // TODO: This can be slow!
            ResultSet rs = 
                stmt.executeQuery("SELECT * FROM \"" + this.getName() + "\" LIMIT 1");
            exists = (rs.next() == true);
            rs.close();
            stmt.close();
        } catch (SQLException sqlex) {
            Object[] arguments = { this.getName(), sqlex.getMessage() };
            loc.errorT("Data existence check for table {0} failed: {1}",
                    arguments);
            loc.exiting();
            sqlex.printStackTrace();
        } catch (Exception ex) {
            Object[] arguments = { this.getName(), ex.getMessage() };
            loc.errorT("Data existence check for table {0} failed: {1}",
                    arguments);
            loc.exiting();
            ex.printStackTrace();
        }

        Object[] arguments = { this.getName(), exists ? "contains" : "doesn't contain" };
        loc.infoT("table {0} {1} data", arguments);
        loc.exiting();
        return exists;
    }

    /**
     * Check the table-width (hk) table-width OR column-widths?
     * 
     * @return true - if table-width is o.k.
     */
    public boolean checkWidth() {
        loc.entering("checkWidth");
        
        DbColumns columns = this.getColumns();
        DbColumnIterator iter = columns.iterator();
        DbColumn column;
        boolean check = true;
        int total = 0;
        int colCnt = 0;
        int varCnt = 0;

        while (iter.hasNext()) {
            column = (DbColumn) iter.next();
            colCnt++;

            switch (column.getJavaSqlType()) {
            // TODO: implement DbMysTable::checkWidth()
            }
        }

        loc.exiting();
        return check;
    }
    
    /**
     * Check the table's name according to its length
     * 
     * @return true - if name-length is o.k
     */
    public boolean checkNameLength() {
        loc.entering("checkNameLength");
        int nameLen = this.getName().length();

        if (nameLen > 0 && nameLen <= 64) {
            loc.exiting();
            return true;
        } else {
            Object[] arguments = { this.getName(), new Integer(nameLen) };
            loc.errorT("checkNameLength {0}: length {1} invalid", arguments);

            loc.exiting();
            return false;
        }
    }

    /**
     * Checks if tablename is a reserved word
     * TODO: this is already handled by JDBC with getSQLKeywords()
     * 
     * @return true - if table-name has no conflict with reserved words, false
     *         otherwise
     */
    public boolean checkNameForReservedWord() {
        loc.entering("checkNameForReservedWord");
        boolean isReserved = DbMysEnvironment.isReservedWord(this.getName());

        if (isReserved == true) {
            Object[] arguments = { this.getName() };
            loc.errorT("checkNameForReservedWord {0}: reserved", arguments);
        }

        loc.exiting();
        return (isReserved == false);
    }
}
