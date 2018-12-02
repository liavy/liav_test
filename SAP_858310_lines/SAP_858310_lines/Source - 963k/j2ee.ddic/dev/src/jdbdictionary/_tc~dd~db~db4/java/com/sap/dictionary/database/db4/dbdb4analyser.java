package com.sap.dictionary.database.db4;

import com.sap.dictionary.database.dbs.DbAnalyser;
import com.sap.dictionary.database.dbs.Logger;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;


/**
 * Title:        DbDb4Analyser
 * Copyright:    Copyright (c) 2002
 * Company:      SAP AG
 * @author       Michael Tsesis & Kerstin Hoeft, Dorothea Rink
 */
  

public class DbDb4Analyser extends DbAnalyser {


    private static final Location loc = Logger.getLocation("db4.DbDb4Analyser");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,
                                                             Logger.CATEGORY_NAME);

    //----------------
    //  constructors  ---------------------------------------------------------
    //----------------
    
    public DbDb4Analyser() {
        super();
    }

    //------------------
    //  public methods  -------------------------------------------------------
    //------------------

    public boolean hasData(String tableName) {
        // DR: To be implemented. - Connection?
        cat.errorT(loc, "hasData(String): not implemented.");
        boolean hasData = false;
        return hasData;
    }

    public boolean existsTable(String name) {
        // DR: To be implemented. - Connection?
        cat.errorT(loc, "existsTable(String): Not yet implemented.");
        boolean existsTable = false;
        return existsTable;
    }

    public boolean existsIndex(String tableName, String indexName) {
        // DR: To be implemented. - Connection?
        cat.errorT(loc, "existsIndex(String, String): Not yet implemented.");
        boolean existsIndex = false;
        return existsIndex;
    }

    public boolean existsView(String name) {
        // DR: To be implemented. - Connection?
        cat.errorT(loc, "existsView(String): Not yet implemented.");
        boolean existsView = false;
        return existsView;
    }

    public boolean hasIndexes(String tableName) {
        // DR: To be implemented. - Connection?
        cat.errorT(loc, "hasIndexes(String): Not yet implemented.");
        boolean existsIndexes = false;
        return existsIndexes;
    }
}