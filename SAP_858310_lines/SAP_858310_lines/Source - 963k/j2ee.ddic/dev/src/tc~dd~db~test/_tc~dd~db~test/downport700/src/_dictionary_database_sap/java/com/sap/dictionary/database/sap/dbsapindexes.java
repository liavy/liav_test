package com.sap.dictionary.database.sap;

import com.sap.dictionary.database.dbs.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author
 * @version 1.0
 */

public class DbSapIndexes extends DbIndexes {

  public DbSapIndexes() {
    super();
  }

  public DbSapIndexes(DbFactory factory) {
    super(factory);
  }

  public DbSapIndexes(DbFactory factory,DbIndexes other) {
    super(factory,other);
  }

  public DbSapIndexes(DbFactory factory, XmlMap xmlMap) throws Exception {
    super(factory,xmlMap);
  }

  /**
   *  Checks if number of indexes maintained is allowed
   *  @return true if number of indexes is o.k, false otherwise
   * */
  public boolean checkNumber() {
    DbIndexIterator iter = this.iterator();
    int cnt = 0;

    while (iter.hasNext()) {
      iter.next();
      cnt ++;
    }

    return (cnt <= DbSapEnvironment.MaxIndicesPerTable());
  }
}