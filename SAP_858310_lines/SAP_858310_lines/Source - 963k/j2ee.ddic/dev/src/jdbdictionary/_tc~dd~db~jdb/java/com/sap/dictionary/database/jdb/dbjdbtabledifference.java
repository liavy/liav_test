/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/DbJdbTableDifference.java#4 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */
package com.sap.dictionary.database.jdb;

import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.DbTableDifference;

public class DbJdbTableDifference extends DbTableDifference {
    public DbJdbTableDifference(DbTable refTable, DbTable cmpTable) {
        super(refTable, cmpTable);
    }
}
