package com.sap.dictionary.database.dbs;

/**
 * Überschrift:   Tests zu jdbpersistency
 * Beschreibung:
 * Copyright:     Copyright (c) 2001
 * Organisation:
 * @author
 * @version 1.0
 */

public class DbTableDifference {
  private DbTable origin = null;
  private DbTable target = null;
  //Necessary DB-action following the columns-difference
  private Action action  = null;
  private DbColumnsDifference      columnsDifference    = null;
  private DbIndexesDifference      indexesDifference    = null;
  private DbPrimaryKeyDifference   primaryKeyDifference = null;

  public DbTableDifference(DbTable origin,DbTable target) {
    this.origin = origin;
    this.target = target;
  }

  public DbTableDifference(DbTable origin,DbTable target,Action action) {
    this.origin = origin;
    this.target = target;
    this.action = action;
  }

  public void setColumnsDifference(DbColumnsDifference difference) {
    columnsDifference = difference;
  }

  public void setIndexesDifference(DbIndexesDifference difference) {
    indexesDifference = difference;
  }

  public void setPrimaryKeyDifference(DbPrimaryKeyDifference difference) {
    primaryKeyDifference = difference;
  }

  public void setAction(Action action) {
  	this.action = action;
  }
  
  public void mergeAction(Action action) {
  	Action strongerAction = Action.max(this.action,action);
  	if (strongerAction != this.action)
  		this.action = action;
  }

  public DbTable getOrigin() {return origin;}

  public DbTable getTarget() {return target;}

  public Action getAction() {return action;}
  
  public DbColumnsDifference getColumnsDifference() {
  	return columnsDifference;
  }
  
  public DbIndexesDifference getIndexesDifference() {
  	return indexesDifference;
  }
  
  public DbPrimaryKeyDifference getPrimaryKeyDifference() {
  	return primaryKeyDifference;
  }

  public DbObjectSqlStatements getDdlStatements(String tableName) throws Exception {
    return getDdlStatements(tableName,null);
  }

	public DbObjectSqlStatements getDdlStatements(String tableName,
	 DbTable tableForStorageInfo) throws Exception {
		DbObjectSqlStatements stmts = new DbObjectSqlStatements(tableName);
		DbObjectSqlStatements stmtsTemp = null;
		if (columnsDifference != null) {
			stmtsTemp = columnsDifference.getDdlStatementsForAlter(tableName);
			stmtsTemp.setObjectType("Table");
			stmtsTemp.setObjectName(tableName);
			stmtsTemp.setParentObjectName(tableName);
			stmtsTemp.setKind("Alter");
			stmts.merge(stmtsTemp);
		}
		if (indexesDifference != null) {
			stmtsTemp = indexesDifference.getDdlStatements(tableName,
			 tableForStorageInfo);
			stmtsTemp.setObjectType("Index");
			stmtsTemp.setParentObjectName(tableName);
			stmts.merge(stmtsTemp);
		}
		if (primaryKeyDifference != null) {
			stmtsTemp = primaryKeyDifference.getDdlStatements(tableName,
			 tableForStorageInfo);
			stmtsTemp.setObjectType("PrimaryKey");
			stmtsTemp.setParentObjectName(tableName);
			stmts.merge(stmtsTemp);
		}
		return stmts;
	}

  public String toString() {
  	String res =
  		"Original Table   = " + (origin == null ? "null" : origin.getName()) + "\n" +
           "Target Table     = " + (target == null ? "null" : target.getName()) + "\n" +
           "Action           = " + (action == null ? "null" : action.getName())  + "\n" +
           "ColumnsAction    = " + (columnsDifference == null ? "null" : columnsDifference.getAction().getName() ) + "\n" +
           "IndexesAction    = " + (indexesDifference == null ? "null" : "ALTER") + "\n" +
           "PrimaryKeyAction = " + (primaryKeyDifference == null  ? "null" : primaryKeyDifference.getAction().getName() )  + "\n";
  	if (columnsDifference != null)
  		res += "columnsDifference = " + columnsDifference ;
  	if (indexesDifference != null)
  		res += "indexesDifference = " + indexesDifference ;
  	if (primaryKeyDifference != null)
  		res += "primaryKeyDifference = " + primaryKeyDifference ;
  	return res;
  }
}