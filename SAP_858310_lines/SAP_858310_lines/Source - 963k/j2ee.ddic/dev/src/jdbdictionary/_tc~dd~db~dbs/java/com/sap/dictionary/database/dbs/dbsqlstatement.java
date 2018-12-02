package com.sap.dictionary.database.dbs;

import java.util.*;
import java.sql.*;

import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Severity;
import com.sap.sql.NativeSQLAccess;
import com.sap.sql.services.OpenSQLServices;

/**
 * Title:        Analyse Tables and Views for structure changes
 * Description:  Contains Extractor-classes which gain table- and view-descriptions from database and XML-sources. Analyser-classes allow to examine this objects for structure-changes, code can be generated and executed on the database.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Michael Tsesis & Kerstin Hoeft
 * @version 1.0
 */

public class DbSqlStatement implements DbsConstants {
  StringBuffer statement = new StringBuffer(300);
  String objectType = null;
  String objectName = null;
  String parentObjectName = null;
  String kind = null;
  boolean isDrop = false;
  
  private static final Location loc = 
		Location.getLocation(DbSqlStatement.class);
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);

  public DbSqlStatement() {}
  
  public DbSqlStatement(boolean isDrop) {
  	this.isDrop = isDrop;
  }

  public void addLine(String line) {
		statement.append(line);
  	if (!line.endsWith("\n"))
			statement.append("\n");
  }

  public boolean execute(Connection con) {
  	//System.out.println("execute" + statement);
    try {
      Statement statementObject = NativeSQLAccess.createNativeStatement(con);
      try {
        statementObject.executeUpdate(statement.toString());
        cat.info(loc, STATEMENT_EXEC_SU,new Object[] {statement});
        statementObject.close();
        return true;
      }
      catch (SQLException ex) {
        return analyseError(con,ex);
      }
    }
    catch (Exception ex) {
      cat.error(loc, STATEMENT_PREP_EX,
					 new Object[] {statement});  
      return false;
    }
  }

	public boolean analyseError(Connection con, SQLException sqlEx) {
		try {
			if (isDrop && OpenSQLServices.objectUnknownSQLError(con, sqlEx)) {
				cat.info(loc, NO_DROP_ERROR, new Object[] {});
				return true;
			} else {
				cat.error(loc,STATEMENT_EXEC_EX,new Object[]{sqlEx.getMessage(),statement});
				return false;
			}
		} catch (Exception ex) {
			cat.error(loc, STATEMENT_EXEC_EX,new Object[]{sqlEx.getMessage(),statement});
			return false;
		}
	}
	
	public void exec(Connection con) {
		//System.out.println("exec" + statement);
		Statement stmt = null;
		boolean notExistsError = false;
		try {
			stmt = NativeSQLAccess.createNativeStatement(con);
		} catch (Exception ex) {
			throw new JddRuntimeException(ex, STATEMENT_PREP_EX,
					new Object[]{stmt},cat,Severity.ERROR,loc);
		}
		try {
			stmt.executeUpdate(statement.toString());
			cat.info(loc, STATEMENT_EXEC_SU, new Object[]{statement});
		} catch (SQLException ex) {
			try {
				notExistsError = OpenSQLServices.objectUnknownSQLError(con, ex);
			} catch (SQLException e1) {
				throw new JddRuntimeException(ex, STATEMENT_EXEC_EX,
						new Object[]{ex.getMessage(),statement},cat,Severity.ERROR,loc);
			}
			if (isDrop && notExistsError) {
				cat.info(loc, NO_DROP_ERROR);
			} else {
				throw new JddRuntimeException(ex, STATEMENT_EXEC_EX,
						new Object[]{ex.getMessage(),statement},cat,Severity.ERROR,loc);
			}
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				throw JddRuntimeException.createInstance(e,cat,Severity.ERROR,loc);
			}
		}
	}
  
  public void merge(DbSqlStatement other) {
    statement.append(other.statement.toString());
  }

  public boolean equals(DbSqlStatement other) {
    if (statement.toString().trim().equals(other.statement.toString().trim())) {
      return true;
    }
    else {
     cat.info(loc,STATEMENTS_UNEQUAL,new Object[] {statement.toString().trim(),
     		  other.statement.toString()});  	
     return false;}
  }

  public String toString() {
    return statement.toString();
  }

  public boolean isEmpty() {
    return (statement.toString().trim().length() == 0);
  }
  
	public String getObjectType() {
		return objectType;
	}
	
	public String getObjectName() {
		return objectName;
	}
	
	public String getParentObjectName() {
		return parentObjectName;
	}
	
	public String getKind() {
		return kind;
	}
  
  public boolean isDrop() {return isDrop;} 
  
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	
	public void setParentObjectName(String parentObjectName) {
		this.parentObjectName = parentObjectName;
	}
	
	public void setKind(String kind) {
		this.kind = kind;
	}
  
	public void setIsDrop() {
		isDrop = true;
	} 
  	
}
