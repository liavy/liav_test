package com.sap.dictionary.database.db2;

import com.sap.sql.NativeSQLAccess;
import com.sap.dictionary.database.dbs.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;

/**
 * Title:        Analysis of table and view changes: DB2/390 specific classes
 * Description:  DB2/390 specific analysis of table and view changes. Tool to deliver Db2/390 specific database information.
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author Burkhard Diekmann
 * @version 1.0
 */

public class DbDb2Columns extends DbColumns {
	private static Location loc = Logger.getLocation("db2.DbDb2Columns");							
	private static Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
	private boolean isV9 = false;
	private DbDb2Environment db2Env = null;
	
	public DbDb2Columns() {
	        super();
        }
     
	public DbDb2Columns(DbFactory factory) {
		super(factory);
		setDbEnv(factory);
	}  
        
    public DbDb2Columns( DbFactory factory, DbColumns other ) {
       	super(factory, other);
       	setDbEnv(factory);
	}
        
    public DbDb2Columns( DbFactory factory, XmlMap xmlMap ) throws Exception {
	    super(factory, xmlMap);
	    setDbEnv(factory);
	}
    
    private void setDbEnv(DbFactory factory) {
    	db2Env = ((DbDb2Environment) factory.getEnvironment());
    	db2Env.getDb2Paramter().setValues(factory.getConnection());
    	isV9 = db2Env.isV9(factory.getConnection());
    }
	
    public void setContentViaDb(DbFactory factory) throws JddException {
		loc.entering("setContentViaDb");

		try {
			boolean found = db2Env.setColsViaDb(factory, this);
			if (!found) {
				Object[] arguments =
					{getTable().getName()};
				cat.infoT( loc,"Table {0} not found on DB.", arguments);
				loc.exiting();
				throw new JddException(
					ExType.NOT_ON_DB,
					DbMsgHandler.get(TABLE_ONDB_NOTFOUND, arguments));
			}
			loc.exiting();
			return;
		} catch (Exception ex) {
			Object[] arguments = { ex.toString()};
			cat.infoT( loc,"setColsViaDb encountered exception {0}.", arguments);
		}

		
		loc.exiting();
	}

	/**
	*  Checks if number of columns is allowed
	*  @return true if number of columns is o.k, false otherwise
	* */
	public boolean checkNumber() {
		loc.entering("checkNumber");
		boolean check = true;
		DbColumnIterator iterator = iterator();
		int colCount = 0;

		while (iterator.hasNext()) {
			colCount++;
			iterator.next();
		}

		if (colCount > DbDb2Parameters.maxTableColumns) {
			check = false;
			Object[] arguments =
				{
					getTable().getName(),
					new Integer(colCount),
					new Integer(DbDb2Parameters.maxTableColumns)};
			cat.errorT(loc,
				"checkNumber {0}: number of columns ({1}) greater than allowed maximum ({2})",
				arguments);
		}
		loc.exiting();
		return check;
	}
}