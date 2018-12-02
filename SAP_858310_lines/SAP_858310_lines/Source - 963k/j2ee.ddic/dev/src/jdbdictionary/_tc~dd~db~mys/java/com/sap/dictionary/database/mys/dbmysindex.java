package com.sap.dictionary.database.mys;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Iterator;

import com.sap.dictionary.database.dbs.DbColumn;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndex;
import com.sap.dictionary.database.dbs.DbIndexColumnInfo;
import com.sap.dictionary.database.dbs.DbSchema;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlHelper;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and Xml-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Hakan Kuecuekyilmaz, <hakan at mysql dot com>, 2005-07-10
 * @version 1.0
 */

/** BE AWARE: to use native sql statements, you have to acquire Statement-objects this way: 
    	Statement stmt = NativeSQLAccess.createNativeStatement(con);
              resp. PreparedStatement-objects:
        preparedStatement = NativeSQLAccess.prepareNativeStatement(connection, sql_string);
 **/

public class DbMysIndex extends DbIndex {
    // can we add additional descriptors/properties for MySQL
    // tables beside the common stuff? i.e. tablespace?

    private static Location loc = Logger.getLocation("mys.DbMysIndex");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);

    public DbMysIndex() {
        super();
    }

    public DbMysIndex(DbFactory factory) {
        super(factory);
    }

    public DbMysIndex(DbFactory factory, DbIndex other) {
        super(factory, other);
    }

    public DbMysIndex(DbFactory factory, String tableName, String indexName) {
        super(factory, tableName, indexName);
    }

    public DbMysIndex(DbFactory factory, DbSchema schema, String tableName,
            String indexName) {
        super(factory, schema, tableName, indexName);
    }

    /**
     * if there is specific contents (tablespace, fill factor for pages or what
     * ever) this method is reading it from a special XML-file (formatting is up
     * to port) and adding this information to the DbMysIndex-object
     */
    public void setSpecificContentViaXml(XmlMap xmlMap) throws JddException {
        loc.entering("setSpecificContentViaXml");

        try {
            XmlMap storage = xmlMap.getXmlMap("storage-parameters");
            if (storage.isEmpty() == true)
                return;
        } catch (Exception ex) {
            Object[] arguments = { ex.getMessage() };
            loc.errorT("setSpecificContentViaXml failed: {0}", arguments);
            loc.exiting();
            throw JddException.createInstance(ex);
        }

        loc.exiting();
        return;
    }

    /**
     * retrieves common description of index (uniqueness, columns) from database
     */
    public void setCommonContentViaDb() throws JddException {
        loc.entering("setCommonContentViaDb");

        Connection con = getDbFactory().getConnection();

        // to be implemented
        // use NativeSQLAccess.createNativeStatement and prepareNativeStatement
        // to gain
        // Statement and PreparedStatement object

        //setContent(isUnique,columnList);

        loc.exiting();
        return;
    }

    /**
     * retrieves MySQL specific description of index (storage parameters) from
     * database
     */
    public void setSpecificContentViaDb() throws JddException {
        loc.entering("setSpecificContentViaDb");

        Connection con = getDbFactory().getConnection();

        // to be implemented
        // use NativeSQLAccess.createNativeStatement and prepareNativeStatement
        // to gain
        // Statement and PreparedStatement object

        loc.exiting();
        return;
    }

    /**
     * check existence of index
     */
    public boolean existsOnDb() throws JddException {
        loc.entering("existsOnDb");
        boolean exists = false;
        Connection con = getDbFactory().getConnection();

        // to be implemented

        Object[] arguments = { this.getTableName(), this.getName(),
                exists ? "exits " : "doesn't exist" };
        loc.infoT("index {0}.{1} {2} on db", arguments);
        loc.exiting();
        return exists;
    }

    /**
     * create XML for MySQL specific part
     */
    public void writeSpecificContentToXmlFile(PrintWriter file, String offset0)
            throws JddException {
        loc.entering("writeSpecificContentToXmlFile");

        try {
            file.println(offset0 + "<index name=" + "\"" + this.getName()
                    + "\"" + ">");

            String offset1 = offset0 + XmlHelper.tabulate();
            String offset2 = offset1 + XmlHelper.tabulate();

            file.println(offset1 + "<storage-parameters>");

            // for example: additional storage parameters

            file.println(offset1 + "</storage-parameters>");

            file.println(offset0 + "</index>");
        } catch (Exception ex) {
            Object[] arguments = { ex.getMessage() };
            loc.errorT("writeSpecificContentToXmlFile failed: {0}", arguments);
            loc.exiting();
            throw JddException.createInstance(ex);
        }

        loc.exiting();
        return;
    }

    /**
     * Check the index's-width
     * 
     * @return true - if index-width is o.k
     */
    public boolean checkWidth() {
        loc.entering("checkWidth");

        // compute length of one entry, compare against maximum 3500
        Iterator iter = this.getColumnNames().iterator();
        String colName = null;
        DbColumns columns = this.getIndexes().getTable().getColumns();
        DbColumn column;
        boolean check = true;
        int total = 0;

        while (iter.hasNext()) {
            colName = ((DbIndexColumnInfo) iter.next()).getName();
            column = columns.getColumn(colName);
            if (column == null) {
                check = false;

                Object[] arguments = { this.getName(), colName,
                        this.getTableName() };
                loc.errorT("checkWidth {0}: no column {1} in table {2} ",
                        arguments);
                continue;
            }
            switch (column.getJavaSqlType()) {
            case java.sql.Types.BLOB:
            case java.sql.Types.CLOB:
            case java.sql.Types.LONGVARBINARY:
            case java.sql.Types.LONGVARCHAR: {
                check = false; // not allowed in index/key

                Object[] arguments = { this.getName(), colName };
                loc.errorT("checkWidth {0}: column of type LOB/LONGVAR ({1}) not allowed in index",
                           arguments);
            }
                break;

            // contribution of other types?
            }
        }

        if (total > 3500) {
            check = false;

            Object[] arguments = { this.getName(), new Integer(total) };
            loc.errorT("checkWidth {0}: total width of index ({1}) greater than allowed maximum (3500)",
                       arguments);
        }

        loc.exiting();
        return check;
    }

    /**
     * Check the index's name according to its length
     * 
     * @return true - if name-length is o.k
     */
    public boolean checkNameLength() {
        loc.entering("checkNameLength");

        int nameLen = this.getName().length();
        boolean check = (nameLen > 0 && nameLen <= 64);

        if (check == false) {
            Object[] arguments = { this.getName(), new Integer(nameLen) };
            loc.errorT("checkNameLength {0}: length {1} invalid (valid range [1..64])",
                       arguments);
        }
        loc.exiting();
        return check;
    }

    /**
     * Checks if indexname is a reserved word
     * TODO: this is already handled by JDBC with getSQLKeywords()
     * 
     * @return true - if index-name has no conflict with reserved words, false
     *         otherwise
     */
    public boolean checkNameForReservedWord() {
        loc.entering("checkNameForReservedWord");

        boolean check = (DbMysEnvironment.isReservedWord(this.getName()) == false);

        if (check == false) {
            Object[] arguments = { this.getName() };
            loc.errorT("{0} is a reserved word", arguments);
        }
        loc.exiting();
        return check;
    }

    /**
     * Checks if number of index-columns maintained is allowed
     * 
     * @return true if number of index-columns is correct, false otherwise
     */
    public boolean checkNumberOfColumns() {
        loc.entering("checkNumberOfColumns");

        int numCols = this.getColumnNames().size();
        boolean check = (numCols > 0 && numCols <= 16);

        if (check == false) {
            Object[] arguments = { this.getName(), new Integer(numCols) };
            loc.errorT("checkNumberOfColumns{0}: column count {1} not in allowed range [1..16]",
                       arguments);
        }
        loc.exiting();
        return check;
    }

}