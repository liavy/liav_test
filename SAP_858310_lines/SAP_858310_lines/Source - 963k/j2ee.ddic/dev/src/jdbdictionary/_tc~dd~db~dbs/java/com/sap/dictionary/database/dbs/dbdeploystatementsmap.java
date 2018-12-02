/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.sap.dictionary.database.dbs;

import java.util.*;
import java.sql.*;
import com.sap.tc.logging.*;

/**
 * @author d003550
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DbDeployStatementsMap implements IDbDeployStatements,DbsSeverity,
		 DbsConstants {
  Map statements = null;
  Iterator iterForNext = null;     //Runs through all generated statements
  boolean firstForNext = false;
  private static final Location loc = 
	Location.getLocation(DbDeployStatementsMap.class);
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
  
  public DbDeployStatementsMap() {
	statements = Collections.synchronizedMap(new HashMap());
  }	
	  
  public void put(String name, long timestamp, DbObjectSqlStatements s) throws JddException {
	statements.put(name.toUpperCase(),new Object[] {name.toUpperCase(),new Long(timestamp),s});
  }
	  
  public DbObjectSqlStatements get(String name) throws JddException {
  	Object[] res = (Object[]) statements.get(name);
  	if (res == null) {return null;}
  	return (DbObjectSqlStatements) (((Object[]) statements.get(name))[2]);
  }
		
  public Object[] getRow(String name) throws JddException {
    return (Object[]) statements.get(name);
  }
	  
  public Object[] next() {
  	if (!firstForNext) {
    	  iterForNext = statements.keySet().iterator();;firstForNext = true;
    }  	
	return (Object[]) statements.get(iterForNext.next());
  }
	  
  public boolean hasNext() {
  	if (!firstForNext) {
    	  iterForNext = statements.keySet().iterator();;firstForNext = true;
    }	
  	return iterForNext.hasNext();
  }
	  
  public boolean isEmpty() {
  	return statements.isEmpty();
  }    
}
