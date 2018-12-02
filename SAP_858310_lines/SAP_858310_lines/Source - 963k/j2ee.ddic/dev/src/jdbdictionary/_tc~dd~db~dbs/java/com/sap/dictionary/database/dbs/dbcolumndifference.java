package com.sap.dictionary.database.dbs;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbColumnDifference {
  private DbColumn origin                  = null;
  private DbColumn target                  = null;
  private DbColumnDifferencePlan diffPlan  = null;
  //Necessary DB-action following the columns-difference
  private Action action                         = null;

  public DbColumnDifference(DbColumn origin,DbColumn target,
                                 DbColumnDifferencePlan diffPlan,Action action) {
    this.origin      = origin;
    this.target      = target;
    this.diffPlan    = diffPlan;
    this.action      = action;
  }

  public void setAction(Action action) {this.action = action;}

  public DbColumn getOrigin() {return origin;}

  public DbColumn getTarget() {return target;}

  public DbColumnDifferencePlan getDifferencePlan() {return diffPlan;}

  public Action getAction() {return action;}

  public String toString() {
    return "\nOriginal Column = " + (origin == null ? "null" : origin.toString()) + "\n" +
           "Target Column   = " + (target == null ? "null" : target.toString()) + "\n" +
           "Action          = " + action  + "\n" +
           "Difference Plan: " + diffPlan + "\n";
  }
}