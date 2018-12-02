package com.sap.dictionary.database.dbs;

import java.util.*;
import java.io.*;
import java.sql.*;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.sql.NativeSQLAccess;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbObjectSqlStatements implements DbsConstants {
  private String name = " ";
  private LinkedList statements = new LinkedList();
  private static final Location loc = 
		Location.getLocation(DbObjectSqlStatements.class);
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);

  public DbObjectSqlStatements(String name) {
    this.name = name;
  }

  public void add(DbSqlStatement statement) {
    statements.add(statement);
  }

  public void save() {
  }

  public boolean execute(Connection con) {
    boolean result = false;
    DbSqlStatement nextStatement = null;

    Iterator iterator = statements.iterator();
    while (iterator.hasNext()) {
      nextStatement = (DbSqlStatement) iterator.next();    	
      if (nextStatement.isDrop)
       if (nextStatement.execute(con) == false)
         return false;
    }
    
    iterator = statements.iterator();
    while (iterator.hasNext()) {
      nextStatement = (DbSqlStatement) iterator.next();    	
      if (!nextStatement.isDrop)
       if (nextStatement.execute(con) == false)
         return false;
    }
    
    return true;
  }

  public boolean execute(DbFactory factory) {
    return execute(factory.getConnection());
  }
  
  public void exec(Connection con) {
    DbSqlStatement nextStatement = null;
    Iterator iterator = statements.iterator();
    // firstly execute the drop-statements
    while (iterator.hasNext()) {
      nextStatement = (DbSqlStatement) iterator.next();    	
      if (nextStatement.isDrop)
      	nextStatement.exec(con);
    }   
    iterator = statements.iterator();
    while (iterator.hasNext()) {
      nextStatement = (DbSqlStatement) iterator.next();    	
      if (!nextStatement.isDrop)
      	nextStatement.exec(con);
    }
  }

  public void exec(DbFactory factory) {
    exec(factory.getConnection());
  }

  public void merge(DbObjectSqlStatements other) {
    statements.addAll(other.statements);
  }

  public DbSqlStatement getStatement(int position) {
    return ((DbSqlStatement) statements.get(position));
  }

  public boolean equals(DbObjectSqlStatements other) {
    Iterator iterator1 = statements.iterator();
    Iterator iterator2 = other.statements.iterator();

    if (statements.size() != other.statements.size()) {return false;}

    while (iterator1.hasNext()) {
      if (!(((DbSqlStatement) iterator1.next()).equals
            ((DbSqlStatement) iterator2.next())))  {
      return false;
      }
    }
    return true;
  }

  public String toString() {
    Iterator iterator = statements.iterator();
    String s = " ";

    while (iterator.hasNext()) {s = s + iterator.next();}
    return s;
  }
  
	public void setObjectType(String objectType) {
		Iterator iter = statements.iterator();
		while (iter.hasNext()) {
			((DbSqlStatement)iter.next()).setObjectType(objectType);
		}
	}
	
	public void setObjectName(String objectName) {
		Iterator iter = statements.iterator();
		while (iter.hasNext()) {
			((DbSqlStatement)iter.next()).setObjectName(objectName);
		}
	}
	
	public void setParentObjectName(String parentObjectName) {
		Iterator iter = statements.iterator();
		while (iter.hasNext()) {
			((DbSqlStatement)iter.next()).setParentObjectName(parentObjectName);
		}
	}
	
	public void setKind(String kind) {
		Iterator iter = statements.iterator();
		while (iter.hasNext()) {
			((DbSqlStatement)iter.next()).setKind(kind);
		}
	}
  
  public void toFile(String destinationPath,String tableName) {
  	File outputFile = new File(destinationPath +  File.separatorChar
                                 	+ tableName.toLowerCase() + "ddl.txt");
  	try {
  	  outputFile.createNewFile();	
  	  PrintWriter writer = new PrintWriter(new FileWriter(outputFile),true);
  	  Iterator iterator = statements.iterator();
    
      while (iterator.hasNext()) {writer.println(iterator.next());}
  	  writer.close();
  	}
  	catch (IOException ex) {
  	  ex.printStackTrace();
  	}     	
  }	
  
  public boolean toDatabase(Connection con,String tableName) {                               	
    Statement statementObject = null;  
    String deleteStatement = "delete from jddtddl where tablename = '" + 
                                      tableName + "'";                         
  	try {
      statementObject = NativeSQLAccess.createNativeStatement(con);
  	}  
    catch (Exception ex) {
      cat.error(loc, STATEMENT_PREP_EX,
					 new Object[] {deleteStatement});  
      ex.printStackTrace();
      return false;
    }
    //Delete existing entry
    try {statementObject.executeUpdate(deleteStatement);}
    catch (Exception ex) {
      cat.error(loc, STATEMENT_EXEC_EX,
                       new Object[] {ex.getMessage(),deleteStatement});        
      return false;
    }  
  	//statements.toString() not possible because [] and comma after each 
  	//entry are added
  	String s = "";
  	for (int i=0;i<=statements.size()-1;i++) {
  	  s = s + statements.get(i);	
  	}	
  	//Insert new statement
    String insertStatement = "insert into jddtddl values ('" +
             tableName +"','" + s + "')";        
    try {
      statementObject.executeUpdate(insertStatement);
      statementObject.close();
      return true;
    }
    catch (Exception ex) {
      cat.error(loc, STATEMENT_EXEC_EX,
                       new Object[] {ex.getMessage(),insertStatement});        
      return false;
    } 
  } 
}
