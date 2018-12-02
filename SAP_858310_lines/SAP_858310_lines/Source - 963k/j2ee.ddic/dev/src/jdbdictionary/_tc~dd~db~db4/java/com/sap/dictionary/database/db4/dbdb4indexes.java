package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.dbs.DbFactory;
import com.sap.dictionary.database.dbs.DbIndex;
import com.sap.dictionary.database.dbs.DbIndexes;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * Title:        DbDb4Index
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */


public class DbDb4Indexes extends DbIndexes {

    private static final Location loc = Logger.getLocation("db4.DbDb4Indexes");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,
                                                             Logger.CATEGORY_NAME);

    //----------------
    //  constructors  ---------------------------------------------------------
    //----------------
    
    public DbDb4Indexes() {
        super();
    }

    public DbDb4Indexes(DbFactory factory) {
        super(factory);
    }
  
    public DbDb4Indexes(DbFactory factory, DbIndexes other) {
        super(factory, other);
    }

    public DbDb4Indexes(DbFactory factory, XmlMap xmlMap) throws Exception {
        super(factory, xmlMap);
    }


    //------------------
    //  public methods  -------------------------------------------------------
    //------------------
    
    /**
     *  Checks if number of indexes maintained is allowed
     *  @return true if number of indexes is o.k, false otherwise
     * */
    public boolean checkNumber() {
        loc.entering(cat, "checkNumber()");
        boolean numberOk = true;
        DbIndex indx = this.getFirst();
        int number = 0;

        while (indx != null) {
            indx = indx.getNext();
            number++;
        }
        numberOk =  (number > DbDb4Environment.getMaxIndexesPerTable())
                            ? false : true; 
        DbDb4Environment.traceCheckResult(true, numberOk, cat, loc, 
                    "Number of indexes: {0} ({1}) - return {3}.", 
                    new Object[] {new Integer(number),
                                new Integer(DbDb4Environment.getMaxIndexesPerTable()),
                                new Boolean(numberOk)});
        loc.exiting();
        return numberOk;
    }

}