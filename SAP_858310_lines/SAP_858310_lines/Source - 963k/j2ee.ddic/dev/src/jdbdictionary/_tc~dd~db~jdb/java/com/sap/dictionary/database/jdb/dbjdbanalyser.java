/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/DbJdbAnalyser.java#4 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */
package com.sap.dictionary.database.jdb;

import com.sap.dictionary.database.dbs.DbAnalyser;

public class DbJdbAnalyser extends DbAnalyser {
    public DbJdbAnalyser() {
    }

    @Override
    public boolean hasData(String tabname) {
        boolean hasData = false;
        return hasData;
    }

    @Override
    public boolean existsTable(String tablename) {
        boolean existsTable = false;
        return existsTable;
    }

    @Override
    public boolean existsIndex(String tablename, String indexname) {
        boolean existsIndex = false;
        return existsIndex;
    }

    @Override
    public boolean existsView(String viewname) {
        boolean existsView = false;
        return existsView;
    }

    @Override
    public boolean hasIndexes(String tablename) {
        boolean existsIndexes = false;
        return existsIndexes;
    }

}