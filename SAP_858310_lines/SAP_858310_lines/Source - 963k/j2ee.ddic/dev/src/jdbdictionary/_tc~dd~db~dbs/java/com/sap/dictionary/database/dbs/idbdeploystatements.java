/*
 * Created on 18.05.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.sap.dictionary.database.dbs;

/**
 * @author d019347
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface IDbDeployStatements {
	
	public void put(String name, long timestamp, DbObjectSqlStatements s)
		 throws JddException; 
	  
	public DbObjectSqlStatements get(String name) throws JddException;
		
	public Object[] getRow(String name) throws JddException;
	  
	public Object[] next();
	  
	public boolean hasNext();
	  
	public boolean isEmpty();
  
}



