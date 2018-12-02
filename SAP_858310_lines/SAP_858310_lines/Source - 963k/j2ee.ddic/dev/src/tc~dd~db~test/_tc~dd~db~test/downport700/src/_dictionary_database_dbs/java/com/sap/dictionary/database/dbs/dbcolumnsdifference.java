package com.sap.dictionary.database.dbs;

import java.util.*;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public abstract class DbColumnsDifference {
  private LinkedList diffs                       = new LinkedList();
  private LinkedList diffsWithAdd                = new LinkedList();
  private LinkedList diffsWithDrop               = new LinkedList();
  private LinkedList diffsWithModify             = new LinkedList();
  private LinkedList diffsWithTypeLenDecChange   = new LinkedList();
  private LinkedList diffsWithNullabilityChange  = new LinkedList();
  private LinkedList diffsWithDefaultValueChange = new LinkedList();
  private Action action = Action.ALTER;

  public DbColumnsDifference() {}

  protected void add(DbColumnDifference difference) {
    diffs.add(difference);
    if (difference.getOrigin() == null) {diffsWithAdd.add(difference);}
    if (difference.getTarget() == null) {diffsWithDrop.add(difference);}
    DbColumnDifferencePlan plan = difference.getDifferencePlan();
    if (plan != null) {
      if (plan.somethingIsChanged()) {diffsWithModify.add(difference);}
      if (plan.nullabilityIsChanged())  {diffsWithNullabilityChange.add(difference);}
      if (plan.defaultValueIsChanged()) {diffsWithDefaultValueChange.add(difference);}
      if (plan.typeLenDecIsChanged()) {diffsWithTypeLenDecChange.add(difference);}
    }
    if (difference.getAction() == Action.REFUSE)
    	action = Action.REFUSE;
    if (difference.getAction() == Action.CONVERT && action == Action.ALTER)
    	action = Action.CONVERT;
  }

  public void setAction(Action action) {this.action = action;}

  public Action getAction() {return action;}

  public boolean isEmpty() {
    return diffs.isEmpty();
  }

  public MultiIterator iterator() {
    return new MultiIterator();
  }


  /**
   *  Generates the ddl-statment for these columns
   *  @param tableName	            the current table 
   *  @return The statements as DbObjectSqlStatements   
   * */
  public abstract DbObjectSqlStatements getDdlStatementsForAlter(String tableName)
    throws Exception;

  public String toString() {
    String s = "";

    Iterator iterator = diffs.iterator();
    while (iterator.hasNext()) {
      s = s + iterator.next();
    }
    return s;
  }

  public class MultiIterator {
    private Iterator diffsIterator               = diffs.iterator();
    private Iterator addIterator                 = diffsWithAdd.iterator();
    private Iterator dropIterator                = diffsWithDrop.iterator();
    private Iterator modifyIterator              = diffsWithModify.iterator();
    private Iterator typeLenDecChangeIterator    = diffsWithTypeLenDecChange.iterator();
    private Iterator nullabilityChangeIterator   = diffsWithNullabilityChange.iterator();
    private Iterator defaultValueChangeIterator  = diffsWithDefaultValueChange.iterator();

    MultiIterator() {}

    public DbColumnDifference next() {
      return (DbColumnDifference) diffsIterator.next();
    }

     public boolean hasNext() {
      return diffsIterator.hasNext();
    }

    public DbColumnDifference nextWithAdd() {
      return (DbColumnDifference) addIterator.next();
    }

    public boolean hasNextWithAdd() {
      return addIterator.hasNext();
    }

    public DbColumnDifference nextWithDrop() {
      return (DbColumnDifference) dropIterator.next();
    }

    public boolean hasNextWithDrop() {
      return dropIterator.hasNext();
    }

    public DbColumnDifference nextWithModify() {
      return (DbColumnDifference) modifyIterator.next();
    }

    public boolean hasNextWithModify() {
      return modifyIterator.hasNext();
    }

    public DbColumnDifference nextWithTypeLenDecChange() {
      return (DbColumnDifference) typeLenDecChangeIterator.next();
    }

    public boolean hasNextWithTypeLenDecChange() {
      return typeLenDecChangeIterator.hasNext();
    }

    public DbColumnDifference nextWithNullabilityChange() {
      return (DbColumnDifference) nullabilityChangeIterator.next();
    }

    public boolean hasNextWithNullabilityChange() {
      return nullabilityChangeIterator.hasNext();
    }

    public DbColumnDifference nextWithDefaultValueChange() {
      return (DbColumnDifference) defaultValueChangeIterator.next();
    }

    public boolean hasNextWithDefaultValueChange() {
      return defaultValueChangeIterator.hasNext();
    }
  }
}
