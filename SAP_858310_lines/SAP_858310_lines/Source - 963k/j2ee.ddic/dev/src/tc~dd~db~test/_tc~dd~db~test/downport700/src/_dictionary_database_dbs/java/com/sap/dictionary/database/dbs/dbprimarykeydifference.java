package com.sap.dictionary.database.dbs;

/**
 * Überschrift:   Tests zu jdbpersistency
 * Beschreibung:
 * Copyright:     Copyright (c) 2001
 * Organisation:
 * @author
 * @version 1.0
 */

public abstract class DbPrimaryKeyDifference {
  private DbPrimaryKey origin = null;
  private DbPrimaryKey target = null;
  //Necessary DB-action following the columns-difference
  private Action action  = null;

  public DbPrimaryKeyDifference(DbPrimaryKey origin,DbPrimaryKey target, 
                                Action action) {
    this.origin = origin;
    this.target = target;
    this.action = action;
  }

  public void setAction(Action action) {this.action = action;}

  public DbPrimaryKey getOrigin() {return origin;}

  public DbPrimaryKey getTarget() {return target;}

  public Action getAction() {return action;}

/**
   *  Generates the ddl-statement according to found differences concerning
   *  specific parameters
   *  @param tableName	            the corresponding table's Name    
   * */
  public abstract DbObjectSqlStatements getDdlStatements(String tableName)
    throws JddException;

  /**
   *  Generates the ddl-statement according to found differences concerning
   *  specific parameters
   *  @param tableName	            the corresponding table's Name    
   *  @param tableForStorageInfo	the table object to get its specific
   *                                parameters which can be used for this
   *                                ddl-statement                  
   * */ 
  public abstract DbObjectSqlStatements getDdlStatements(String tableName,
                                                DbTable tableForStorageInfo)
    throws JddException;

  public String toString() {
  	String res = "\nprimary key";
  	if (action != null)
  		res += "action = " + action.getName();
  	else
  		res += "action = null";
  	if (origin != null)
  		res += "origin = " + origin;
  	else
  		res += "origin = null";
  	if (target != null)
  		res += "target = " + target;
  	else
  		res += "target = null";
  	return res;
  }
}
 


