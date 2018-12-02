package com.sap.dictionary.database.dbs;

/**
 * @author D003550
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
import java.sql.*;

import com.sap.sql.NativeSQLAccess;
import com.sap.sql.services.OpenSQLServices;
import com.sap.tc.logging.*;

public abstract class DbTools implements DbsConstants,DbsSeverity {
  DbFactory factory = null;
  Connection con = null;
  private static final Location loc = Location.getLocation(DbTools.class);
  private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);  
  private static final int MAX_NAME_LENGTH = 18;
  public static final int ALIAS = 1;
  public static final int VIEW = 2;
  public static final int TABLE = 0;
  
  public DbTools(DbFactory factory) {
    this.factory = factory;
    con = factory.getConnection();
  }

  public DbFactory getFactory() {return factory;}

  /**
   * Renames a table on the database. If no exception is send, the table
   * could be renamed.
   * @param sourceName - current name of table
   * @param destinationName - new name of table
   * @exception JddException - The following error-situations should be
   *                  distinguished by the exception's ExType:
   *            ExType.NOT_ON_DB: Source-table does not exist on database
   *            ExType.EXISTS_ON_DB: Destination table already exists. 
   *            Every other error should be send with ExType.SQL_ERROR or
   *            ExType.OTHER.
   **/
  public abstract void renameTable(String sourceName, String destinationName)
                             throws JddException;
  	
  
  /**
   * Delivers the information if the given name is an alias (or synonym).
   * Aliases (or synonyms) can be thought of as alternate name for tables and views.
   * Aliases (or synonyms) refer to a table or view that in the same or in another database schema.
   * @param tableName - current name of table
   * @return Returns true if the given name is an alias (or synonym), false otherwise
   * @exception JddException - Thrown if the information about alias can not be retrieved. 
   *                           The following error-situations should be distinguished by the exception's ExType:
   *                           ExType.SQL_ERROR: Object with name could not be examined 
   **/
  public boolean isAlias(String name)
                             throws JddException {
  	boolean isAlias = false;
  	
    if (tableExistsOnDb(name)) return false;
    else {
      String statement = "SELECT COUNT(*) FROM \"&\"";	
      statement = concat(statement,name);
      try {
  	    PreparedStatement statementObject = 
  					 NativeSQLAccess.prepareNativeStatement(factory.getConnection(),statement);	  
  	    ResultSet result = statementObject.executeQuery();
  	    if (result.next()) {isAlias = true;} 
  	    result.close(); 
  	    statementObject.close();
  	  } 
  	  catch (SQLException ex) {
  	  	try {
  	  		if (!OpenSQLServices.objectUnknownSQLError(con, ex)) {
  	  			JddException.log(ex,cat,Severity.ERROR,loc);
  	  			throw new JddException(ExType.SQL_ERROR, ex.getMessage());
  	  		}
  	  	} catch (SQLException e) {
  	  		JddException.log(ex,cat,Severity.ERROR,loc);
	  			throw new JddException(ExType.SQL_ERROR, ex.getMessage());
  	  	}
  	  }
  	  return isAlias;
    }
  }
  
  /** 
   * Checks what kind of tablelike database object corresponds to name. It is checked
   * if we have an alias or a view on database with the given name. If this is the case the result 
   * is delivered as DbTools.KindOfTableLikeDbObject. In all other cases (including object 
   * is a table on database or object does not exist at all) the return value is null.
   * @param name Name of object to check
   * @return DbTools.KindOfTableLikeDbObject.View, if object is a view on database,
   *         DbTools.KindOfTableLikeDbObject.Alias, if object is an Alias on database,
   *         null in all other cases
   * @exception JddException is thrown if error occurs during analysis        
  **/
  public int getKindOfTableLikeDbObject(String name) 
                 throws JddException{
    return TABLE;
  }
  
   /**
   *  Analyses if table exists on database or not
   *  @param tableName current name of table
   *  @return true if table exists in database, false otherwise
   *  @exception JddException error during analysis detected	 
   **/
  public boolean tableExistsOnDb(String name) throws JddException {
    try {
  	  DbTable table = factory.makeTable(name);
      return table.existsOnDb();
    }
    catch (Exception ex) {
      throw new JddException(ExType.SQL_ERROR,ex);	
    }
  }

  /**
   *  Analyses if index exists on database or not
   *  @param indexName - current name of table
   *  @return true - if index exists in database, false otherwise
   *  @exception JddException error during analysis detected	 
   **/
  public boolean indexExistsOnDb(String tableName, String indexName) throws JddException {
    try {
  	  DbIndex index = factory.makeIndex(tableName,indexName);
      return index.existsOnDb();
    }
    catch (Exception ex) {
      throw new JddException(ExType.SQL_ERROR,ex);	
    }
  }
 
  /**
   *  Analyses if view exists on database or not
   *  @param viewName - current name of table
   *  @return true - if view exists in database, false otherwise
   *  @exception JddException error during analysis detected	 
   **/
  public boolean viewExistsOnDb(String viewName) throws JddException {
    try {
  	  DbView view = factory.makeView(viewName);
      return view.existsOnDb();
    }
    catch (Exception ex) {
      throw new JddException(ExType.SQL_ERROR,ex);	
    }
  }
  
	public int commit() {
		int rc = SUCCESS;
		//Send commit to manifest changes on database
		try {
			if (!con.getAutoCommit()) //autocommit switched off
			  con.commit();
		} catch (SQLException ex) {
			rc = ERROR;
			cat.error(loc, COMMIT_NOT_SEND);
			JddException.log(ex, cat, Severity.ERROR, loc);
		}
		return rc;
	}  
 
    public int invalidate(String name) {
        int rc = SUCCESS;
        if (name.equalsIgnoreCase(factory.getEnvironment().getRuntimeObjectsTableName()))
          return rc;
        //Invalidation of CatalogReader-buffer
        try {
            OpenSQLServices.invalidateTable(factory.getConnection(), name);
            cat.info(loc, BUFFER_RESET_SUCCESS, new Object[] { name });
        } catch (SQLException ex) {
            rc = WARNING;
            cat.warning(loc, BUFFER_RESET_FAILURE, new Object[] { name });
            JddException.log(ex, cat, Severity.WARNING, loc);
        }
        return rc;
    }
    
    private static String concat(String text, String replaceString) {
	  int position = text.indexOf('&');
	  return text.substring(0, position) + replaceString +
					   text.substring(position + 1, text.length());
    }
    
    /**
	*  Checks the table's name
	*  1. Name contains only characters A..Z 0..9 _
	*  2. First Character is of set A..Z
	*  3. Name contains one _ 
	*  4. Name <=18
	*  @param name   name to check
	*  @param allowedChars, firstChar, contains_, maneLength  checks to switch on and off
	*  @return true - if name is correctly maintained, false otherwise
	**/ 	
	public static boolean checkName(String name,boolean allowedChars,boolean firstChar,
			    boolean contains_, boolean nameLength) {
	  String allowedCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
	  String allowedAtFirstPosition = "ABCDEFGHIJKLMNOPQRSTUVWXYZ_";	
	  
	  name = name.toUpperCase();
	  char ch = ' ';
	  if (allowedChars) {
	    for (int i=0;i<name.length();i++) {
	      ch = name.charAt(i);
	      if (allowedCharacters.indexOf(ch) == -1) {
	        cat.error(loc,NAME_WITH_INVALID_CHARS,new Object[] {name});
	        return false;
	      }  
	    }
	  }
	  if (firstChar) {
	    ch = name.charAt(0);
	    if (allowedAtFirstPosition.indexOf(ch) == -1) {
	      cat.error(loc,FIRST_CHAR_IS_INVALID,new Object[] {name});
	      return false;
	    }  
	  }
	  if (contains_) {
	    ch = '_';
	    if (name.indexOf(ch) == -1) {
	      cat.error(loc,UNDERSCORE_MISSING,new Object[] {name});
	      return false;
	    }
	  }
	  if (nameLength) {
	    if (name.length() > MAX_NAME_LENGTH) {
	      cat.error(loc,NAME_IS_TOO_LONG,new Object[] {name,new Integer(MAX_NAME_LENGTH)});
	      return false;   
	    }
	  }
      return true;
	}
	
	public static long currentTime() {
		return System.currentTimeMillis();
	}
    
}
