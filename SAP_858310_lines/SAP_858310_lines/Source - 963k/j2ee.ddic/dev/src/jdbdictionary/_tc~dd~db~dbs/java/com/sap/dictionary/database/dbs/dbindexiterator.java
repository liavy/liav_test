package com.sap.dictionary.database.dbs;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbIndexIterator {
  private DbIndex nextIndex = null;

  public DbIndexIterator(DbIndex first) {
    nextIndex = first;
  }

  public boolean hasNext() {
    if (nextIndex == null) return false;
    else return true;
  }

  public DbIndex next() {
    if (nextIndex == null) return null;
    DbIndex tempIndex = nextIndex;
    nextIndex = nextIndex.getNext();
    return tempIndex;
  }
}
