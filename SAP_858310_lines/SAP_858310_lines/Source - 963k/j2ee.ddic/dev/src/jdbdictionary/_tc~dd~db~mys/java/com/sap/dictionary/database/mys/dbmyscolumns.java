package com.sap.dictionary.database.mys;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.sap.dictionary.database.dbs.*;
import com.sap.sql.NativeSQLAccess;
import com.sap.tc.logging.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author       Hakan Kuecuekyilmaz, <hakan at mysql dot com>, 2005-07-10
 * @version 1.0
 */

public class DbMysColumns extends DbColumns {
  	private static Location loc = Logger.getLocation("mys.DbMysColumns");
    private static final Category cat = Category.getCategory(Category.SYS_DATABASE,Logger.CATEGORY_NAME);
    
    private DbView dbView = null;
  	private DbTable dbTable = null;
    
  	// Constructor including src-Type that means java-Type
  	public DbMysColumns(DbFactory factory) {
        super(factory);
    }

  	public DbMysColumns(DbFactory factory, DbColumns other) {
        super(factory, other);
    }

  	public DbMysColumns(DbFactory factory, XmlMap xmlMap) throws Exception {
        super(factory, xmlMap);
    }
	
    public void setContentViaDb(DbFactory factory) throws JddException {
        String name = " ";
        DatabaseMetaData dbmd = null;
        boolean found = false;

        try {
            if (getTable().getName() != null)
              name = getTable().getName();
            else if (getView().getName() != null)
              name = getView().getName();
            dbmd = NativeSQLAccess.getNativeMetaData(factory.getConnection());
            String schemaName;
            try {  
              schemaName = factory.getSchemaName();
            }
            catch (SQLException ex) {
              cat.info(loc,NO_SCHEMA_NAME);
              schemaName = null;
            }
            java.sql.ResultSet rs = dbmd.getColumns(null, schemaName, name, null);
            while (rs.next()) {
                if (!rs.getString("TABLE_NAME").equals(name))
                    continue;
                found = true;
                /* Attention:
                 * Here it is necessary to get the values of the columns of the result
                 * set in the same order as the columns have in the result set.
                 * (Oracle has here a bug).
                 */
                String colName    = rs.getString("COLUMN_NAME");
                short sqlType     = rs.getShort("DATA_TYPE");
                String dbType     = rs.getString("TYPE_NAME");
                int colSize       = rs.getInt("COLUMN_SIZE");
                int decDigits     = rs.getInt("DECIMAL_DIGITS");
                String defVal     = rs.getString("COLUMN_DEF");
                int pos           = rs.getInt("ORDINAL_POSITION");
                boolean isNotNull = 
                           rs.getString("IS_NULLABLE").trim().equalsIgnoreCase("NO");

                // TODO: Bug #12518 if no default value is given and column is
                // set to "NOT NULL" COLUMN_DEFAULT is empty string. But COLUMN_DEFAULT
                // should be NULL
//                if (defVal != null) {
//                    if (defVal.length() == 0) {
//                        defVal = null;
//                    }
//                }
                
                DbColumn col = factory.makeDbColumn(colName, pos, sqlType, dbType,
                                 colSize, decDigits, isNotNull, defVal);
                add(col);
            }
            rs.close();         
        } catch (Exception ex) {
            Object[] arguments = { name };
            throw new JddException(ex,COLUMN_READ_VIA_DB_ERR,arguments,cat,Severity.ERROR,loc);
        }
        if (!found) {
          Object[] arguments = { name };
          cat.info(loc, TABLE_ONDB_NOTFOUND, arguments);
          //No exception because this is no error, table simply does not exist
        }
    }
    
  	/**
   	 *  Checks if number of columns is allowed
   	 *  @return true if number of columns is o.k, false otherwise
   	 **/  
  	public boolean checkNumber() {
        loc.entering("checkNumber");

        DbColumnIterator iter = this.iterator();
        int cnt = 0;

        while (iter.hasNext()) {
            iter.next();
            cnt++;
        }

        if (cnt <= 0 || cnt > 1000) {
            Object[] arguments = { new Integer(cnt) };
            loc.errorT("checkNumber: {0} columns given, maximal allowed number of columns is 1000",
                       arguments);
            loc.exiting();
            return false;
        } else {
            loc.exiting();
            return true;
        }
    }
}