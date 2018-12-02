                                                                                                                        package com.sap.dictionary.database.dbs;

import java.util.*;

/**
 * Ueberschrift:   Tests zu jdbpersistency
 * Beschreibung:
 * Copyright:     Copyright (c) 2001
 * Organisation:
 * @author
 * @version 1.0
 */

public class DbIndexesDifference {
  private LinkedList diffs = new LinkedList();
  private DbIndexes origin = null;
  private DbIndexes target = null;

  public DbIndexesDifference(DbIndexes origin, DbIndexes target) {
    this.origin = origin;
    this.target = target;
  }

  public boolean isEmpty() {
    return diffs.isEmpty();
  }

  public void add(DbIndexDifference difference) {
    diffs.add(difference);
  }

  protected DbObjectSqlStatements getDdlStatements(String tableName) throws Exception {
    return getDdlStatements(tableName,null);
  }

  protected DbObjectSqlStatements getDdlStatements(String tableName,
                                                   DbTable tableForStorageInfo)
      throws Exception {
    DbObjectSqlStatements stmts = new DbObjectSqlStatements(tableName);

    if (origin == null) return target.getDdlStatementsForCreate();
    if (target == null) return origin.getDdlStatementsForDrop();
    Iterator iterator = diffs.iterator();
    DbIndexDifference diff = null;
    while (iterator.hasNext()) {
      diff = (DbIndexDifference) iterator.next();
      if (diff.getAction() == Action.DROP)
        stmts.merge(diff.getOrigin().getDdlStatementsForDrop());
      else if (diff.getAction() == Action.CREATE) {
        stmts.merge(diff.getTarget().getDdlStatementsForCreate()); 
      }
      else  
        stmts.merge((diff).getDdlStatements
                                       (tableName,tableForStorageInfo));
    }
    return stmts;
  }

  public Iterator iterator() {return diffs.iterator();}

  public String toString() {
    String s = "";

    Iterator iterator = diffs.iterator();
    while (iterator.hasNext()) { s = s + iterator.next();}
    return s;
  }
}