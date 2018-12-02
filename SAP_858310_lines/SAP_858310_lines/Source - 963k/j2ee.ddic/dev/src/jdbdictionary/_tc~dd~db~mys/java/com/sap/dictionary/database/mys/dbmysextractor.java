package com.sap.dictionary.database.mys;

import com.sap.dictionary.database.dbs.*;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and 
 *               view-descriptions from database and XML-sources. 
 *               Analyser-classes allow to examine this objects for 
 *               structure-changes, code can be generated and executed 
 *               on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Hakan Kuecuekyilmaz, <hakan at mysql dot com>, 2005-07-10
 * @version 1.0
 */

public class DbMysExtractor extends DbExtractor {

    public DbMysExtractor() {
    }

    public DbTable getTable(String name) {
        DbTable dbTable = null;

        return dbTable;
    }

    public DbColumns getTableColumns(String name) {
        DbColumns dbColumns = null;

        return dbColumns;

    }

    public DbIndex getIndex(String tableName, String indexName) {
        DbIndex dbIndex = null;

        return dbIndex;
    }

    public DbIndexes getIndexes(String tableName) {
        DbIndexes dbIndexes = null;

        return dbIndexes;
    }

    public DbPrimaryKey getPrimaryKey(String tableName) {
        DbPrimaryKey primaryKey = null;

        return primaryKey;
    }
}