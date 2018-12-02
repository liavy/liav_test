/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/DbJdbColumns.java#4 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */
package com.sap.dictionary.database.jdb;

import com.sap.dictionary.database.dbs.DbColumnIterator;
import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.XmlMap;

public class DbJdbColumns extends DbColumns {
    public DbJdbColumns(DbFactory factory) {
        super(factory);
    }

    public DbJdbColumns(DbFactory factory, DbColumns other) {
        super(factory, other);
    }

    public DbJdbColumns(DbFactory factory, XmlMap xmlMap) throws Exception {
        super(factory, xmlMap);
    }

    @Override
    public boolean checkNumber() {
        DbColumnIterator iter = this.iterator();
        int cnt = 0;
        while (iter.hasNext()) {
            iter.next();
            cnt++;
        }
        return (cnt <= DbJdbEnvironment.MaxColumnsPerTable());
    }

}
