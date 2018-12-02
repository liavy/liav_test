package com.sap.dictionary.database.mys;

import com.sap.dictionary.database.dbs.*;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Hakan Kuecuekyilmaz, <hakan at mysql dot com>, 2005-07-10
 * @version 1.0
 */

public class DbMysIndexes extends DbIndexes {

  public DbMysIndexes() {
        super();
    }

    public DbMysIndexes(DbFactory factory) {
        super(factory);
    }

    public DbMysIndexes(DbFactory factory, DbIndexes other) {
        super(factory, other);
    }

    public DbMysIndexes(DbFactory factory, XmlMap xmlMap) throws Exception {
        super(factory, xmlMap);
    }

    /**
     * Checks if number of indexes maintained is allowed
     * 
     * (C5057465)
     * Attention: The number of indexes for a given table can be set while
     * compiling MySQL. 64 indexes per table is the default. 2005-09-15
     * 
     * @return true if number of indexes is o.k, false otherwise
     */
    public boolean checkNumber() {
        DbIndexIterator iter = this.iterator();
        int cnt = 0;
        while (iter.hasNext()) {
            iter.next();
            cnt++;
        }

        return (cnt < 65);
    }
}