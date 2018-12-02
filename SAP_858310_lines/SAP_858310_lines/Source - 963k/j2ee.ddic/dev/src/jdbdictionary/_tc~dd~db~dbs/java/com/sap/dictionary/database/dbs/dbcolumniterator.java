package com.sap.dictionary.database.dbs;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Höft
 * @version 1.0
 */

public class DbColumnIterator {
  private DbColumn nextColumn = null;

  public DbColumnIterator(DbColumn first) {
    nextColumn = first;
  }

  public boolean hasNext() {
    if (nextColumn == null) return false;
    else return true;
  }

  public DbColumn next() {
    if (nextColumn == null) return null;
    DbColumn tempColumn = nextColumn;
    nextColumn = nextColumn.getNext();
    return tempColumn;
  }
}
