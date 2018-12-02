/* $Id: //engine/j2ee.ddic/dev/src/jdbdictionary/_tc~dd~db~jdb/java/com/sap/dictionary/database/jdb/DbJdbIndexes.java#4 $
 * Last changelist: $Change: 257822 $
 * Last changed at: $DateTime: 2009/04/22 13:13:41 $
 * Last changed by: $Author: d019347 $
 */
package com.sap.dictionary.database.jdb;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndexIterator;
import com.sap.dictionary.database.dbs.DbIndexes;
import com.sap.dictionary.database.dbs.XmlMap;

public class DbJdbIndexes extends DbIndexes {
    public DbJdbIndexes() {
        super();
    }

    public DbJdbIndexes(DbFactory factory) {
        super(factory);
    }

    public DbJdbIndexes(DbFactory factory, DbIndexes other) {
        super(factory, other);
    }

    public DbJdbIndexes(DbFactory factory, XmlMap xmlMap) throws Exception {
        super(factory, xmlMap);
    }

    @Override
    public boolean checkNumber() {
        DbIndexIterator iter = this.iterator();
        int cnt = 0;
        while (iter.hasNext()) {
            iter.next();
            cnt++;
        }
        return (cnt <= DbJdbEnvironment.MaxIndicesPerTable());
    }
}