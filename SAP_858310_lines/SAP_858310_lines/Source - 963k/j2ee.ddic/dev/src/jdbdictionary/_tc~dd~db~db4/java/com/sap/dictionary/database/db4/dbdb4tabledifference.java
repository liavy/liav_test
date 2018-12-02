package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.dbs.DbTable;
import com.sap.dictionary.database.dbs.DbTableDifference;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Title:        DbDb4TableDifference
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */


public class DbDb4TableDifference extends DbTableDifference {


    private static final Location loc = Logger.getLocation("db4.DbDb4TableDifference");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,
                                                             Logger.CATEGORY_NAME);

    //----------------
    //  constructors  ---------------------------------------------------------
    //----------------
    
    public DbDb4TableDifference(DbTable origin, DbTable target) {
        super(origin, target);
    }
}