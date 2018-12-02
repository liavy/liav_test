package com.sap.dictionary.database.mss;

import com.sap.dictionary.database.dbs.*;
import com.sap.sql.NativeSQLAccess;
import java.sql.*;
import com.sap.tc.logging.*;
import java.util.ArrayList;
/*
 * @author d003550
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DbMssView extends DbView implements DbsConstants {
  private static Location loc = Logger.getLocation("mss.DbMssView");
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
  
					
  public DbMssView() {super();}

  public DbMssView(DbFactory factory) {super(factory);}

  public DbMssView(DbFactory factory, String name) {
	super(factory,name);}

  public DbMssView(DbFactory factory, DbView other) {
	super(factory,other); 
  }

  public DbMssView(DbFactory factory, DbSchema schema, String name) {
	super(factory,schema,name); 
  }	

  /**
   *  Analyses if view exists on database or not
   *  @return true - if table exists on database, false otherwise
   *  @exception JddException – error during analysis detected	 
   **/
  public boolean existsOnDb() throws JddException {
	  loc.entering("existsOnDb");
	  boolean exists = false;
	  Connection con = getDbFactory().getConnection();

	  String schemaName = retrieveSchemaName(con);
	  String prefix = null;
	  if (schemaName != null) {
		prefix = "'" + schemaName + "'";
	  } else {
	 	prefix = "user";
	  }

	  try {
		Statement dstmt = NativeSQLAccess.createNativeStatement(con); // gd 170303 con.createStatement();
		java.sql.ResultSet drs = dstmt.executeQuery(
		"select 1 from sysobjects " +
		"where id = object_id(" + prefix + " + '.' + '" + this.getName() + "') and " +
                "type = 'V' ");
		exists = (drs.next() == true);
		drs.close();
		dstmt.close();
	  }
	  catch (SQLException sqlex) {
		  Object[] arguments = {this.getName(), sqlex.getMessage()};
		  cat.errorT(loc, "existence check for view {0} failed: {1}", arguments);
		  loc.exiting();
			
		  throw new JddException(ExType.SQL_ERROR, sqlex.getMessage());
	  } 
	  catch (Exception ex) {
		  Object[] arguments = {this.getName(), ex.getMessage()};
		  cat.errorT(loc, "existence check for view {0} failed: {1}", arguments);
		  loc.exiting();
			
		  throw new JddException(ExType.OTHER, ex.getMessage());
	  }

	  Object[] arguments = {this.getName(), exists? "exits " : "doesn't exist"};
	  cat.infoT(loc, "view {0} {1} on db", arguments);
	  loc.exiting();
	  return exists;
	}	 

  
  /**
   *  Gets the base table Names of this view from database and sets it 
   *  for this view 
   *  @exception JddException error during analysis detected	 
   **/
  public void setBaseTableNamesViaDb() throws JddException {
  	ArrayList names = new ArrayList();
    Connection con = getDbFactory().getConnection();
    
    String schemaName = retrieveSchemaName(con);
    String prefix = null;
    if (schemaName != null) {
	prefix = "'" + schemaName + "'";
    } else {
	prefix = "user";
    }  

    loc.entering("setBaseTableNamesViaDb");
    
    /* 1. step: drop help table */
    try {
      Statement dstmt = NativeSQLAccess.createNativeStatement(con); 
      dstmt.execute("drop table #sap_depobjs");
      dstmt.close();
    } catch(SQLException ex) {
	  cat.infoT(loc, "errors on 'drop table #sap_depobjs' ignored");
      /*ignore any errors*/
    }
    
    /* 2. step: create help table */
	try {
      Statement dstmt = NativeSQLAccess.createNativeStatement(con); 
      dstmt.execute(
      "create table #sap_depobjs " +
      "( name sysname, " +
      "  id   int, " +
      "  lvl  int )");
      dstmt.close();
    } catch(SQLException ex) {
      Object[] arguments = {ex.getMessage()};
	  cat.errorT(loc, "setBaseTableNamesViaDb (creation help table) failed: {0}", arguments);
      loc.exiting();
      throw JddException.createInstance(ex);
    }  
    
    /* 3. step: fill help table */
    try {
    	Statement dstmt = NativeSQLAccess.createNativeStatement(con); 
      	dstmt.execute(
      	"declare @lvl int " +
      	"select @lvl = 0 " +
      
      	"insert into #sap_depobjs " +
		"select name, id, 0 from sysobjects " +
                "where id = object_id(" + prefix + " + '.' + '" + this.getName() + "') and " +
		"type = 'V' " + 

		/* determine all views directly and indirectly used by this view */

		"while @@rowcount > 0 " +
		"begin " +
		"select @lvl = @lvl + 1 " +

		"insert into #sap_depobjs " +
		"select distinct so.name, so.id, @lvl " +
		"from sysdepends sd, sysobjects so, #sap_depobjs sv " +
		"where  sv.lvl = @lvl - 1 " +
		"and    sd.id = sv.id " +
		"and    sd.depid = so.id " +
		"and    so.type = 'V' " +
		"end");
	} catch(SQLException ex) {
   		Object[] arguments = {ex.getMessage()};
		cat.errorT(loc, "setBaseTableNamesViaDb (filling help table) failed: {0}", arguments);
   		loc.exiting();
   		throw JddException.createInstance(ex);
  	} 

	/* 4. step: selecting results */
	try {
   		Statement dstmt = NativeSQLAccess.createNativeStatement(con); 
   		java.sql.ResultSet rs = dstmt.executeQuery(
   			"select distinct so.name " +
   			"from sysobjects so, sysdepends sd, #sap_depobjs sv " +
			"where sd.id = sv.id " +
			"and   sd.depid = so.id " +
			"and   so.type = 'U'"
   		);
      
   		while (rs.next()) { names.add(rs.getString(1)); }
      
   		rs.close();
   		dstmt.close();
  	} catch(SQLException ex) {
   		Object[] arguments = {ex.getMessage()};
		cat.errorT(loc, "setBaseTableNamesViaDb (creation help table) failed: {0}", arguments);
   		loc.exiting();
   		throw JddException.createInstance(ex);
  	}         

	/* 5. step: drop help table */
    try {
      Statement dstmt = NativeSQLAccess.createNativeStatement(con); 
      dstmt.execute("drop table #sap_depobjs");
      dstmt.close();
    } catch(SQLException ex) {
	  cat.infoT(loc, "errors on 'drop table #sap_depobjs' ignored");
      /*ignore any errors*/
    }
    
	setBaseTableNames(names);
  	
  	loc.exiting();
	return; 	
  }
  
  /**
	*  Gets the create statement of this view from the database and 
	*  sets it to this view with method setCreateStatement
	*  @exception JddException � error during detection detected	 
	**/  
  public void setCreateStatementViaDb() throws JddException {
    Connection con = getDbFactory().getConnection();
    String result = "";

	String schemaName = retrieveSchemaName(con);
        String prefix = null;
	if (schemaName != null) {
		prefix = "'" + schemaName + "'";
	} else {
		prefix = "user";
	}
	
	loc.entering("setCreateStatementViaDb");
	
	try {
	  Statement dstmt = NativeSQLAccess.createNativeStatement(con); 
	  java.sql.ResultSet rs = dstmt.executeQuery(
	  	"select text from syscomments sc " + 
      		"where sc.id = object_id(" + prefix + " + '.' + '" + this.getName() + "')");
	  while (rs.next()) {
	  	result = result + rs.getString(1);	
	  }  
	  rs.close();
	  dstmt.close();
	} catch (Exception ex) {
	  Object[] arguments = {ex.getMessage()};
	  cat.errorT(loc, "setCreateStatement failed: {0}", arguments);
	  loc.exiting();
	  throw JddException.createInstance(ex);
	}
	
		  
	if (result.length() == 0) {
		result = null; 
		// loc.errorT("setCreateStatementViaDb failed: view " + this.getName() + " doesn't exist");
   		// loc.exiting();
	  	// throw new JddException(ExType.OTHER, "view " + this.getName() + "doesn't exist");
	}
	  
	setCreateStatement(result);
	
	loc.exiting();
	return; 	
  } 	 


  /**
   * Retrieve the SQL Server release of the connected database
   * @param con  current connection
   * @return     SQL Server release (major)
   * @throws JddException
   */
  private long retrieveSQLServerRelease(Connection con) {
    long rel = 8;

    try {
      Statement schStmt = NativeSQLAccess.createNativeStatement(con);
			
      java.sql.ResultSet rs = schStmt.executeQuery("select (@@microsoftversion / 65536) / 256");
      rs.next();
      rel = rs.getInt(1);
      rs.close();
      schStmt.close();
    } catch (Exception ex) {
      loc.entering("retrieveSQLServerRelease");
      Object[] arguments = { ex.getMessage()};
      cat.errorT(loc, "retrieveSQLServerRelease failed: {0}", arguments);
      loc.exiting();
    }
  
    return rel;
  }
  
  private String retrieveSchemaName( Connection con ) {
	  String schemaName = null;

	  if (this.getSchema() != null) {
		schemaName = this.getSchema().getSchemaName();
	  }
	  else {
		  // determine schema name i'm in
		  try {
			  Statement schStmt = NativeSQLAccess.createNativeStatement(con); // gd 170303 con.createStatement();

			  java.sql.ResultSet rs = schStmt.executeQuery("select user");
			  rs.next();
			  schemaName = rs.getString(1);
			  rs.close();
			  schStmt.close();
		  } catch (Exception ex) {
                          loc.entering("retrieveSchemaName");
			  Object[] arguments = {ex.getMessage()};
			  cat.infoT(loc, "retrieveSchemaName failed: {0}", arguments);
			  cat.infoT(loc, "can be ignored");			  
			  loc.exiting();
          }
	  }
	  return schemaName;
  }
			
}
