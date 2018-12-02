/*
 * Created on Nov 9, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.dictionary.database.dbs;

/**
 * @author d003550
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DbChangeInfo {
  private String tabname = null;
  private Action action = null;
  private String databases = null;
  
  public DbChangeInfo(String tabname) {
    this.tabname = tabname;
    action = Action.NOTHING;
    databases = "ALL";
  }
  
  public String getTabname() {return tabname;}
  
  public Action getAction() {return action;}
  
  public void setAction(Action action) {
    this.action = action;
  }
  
  public String getDatabaseNames() {return databases;}

  public void setDatabaseNames(String databases) {
    this.databases = databases; 
  }
  
  public String toString() {
    return tabname + ": " + action + " " + databases;
  }
}
