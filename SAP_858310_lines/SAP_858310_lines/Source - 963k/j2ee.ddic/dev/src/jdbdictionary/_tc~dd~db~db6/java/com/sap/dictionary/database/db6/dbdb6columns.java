package com.sap.dictionary.database.db6;

import com.sap.dictionary.database.dbs.*;
import com.sap.tc.logging.*;
import java.sql.*;
import com.sap.sql.NativeSQLAccess;

/**
 * Title: Analysis of table and view changes: DB6 specific classes Description:
 * DB6 specific analysis of table and view changes. Tool to deliver DB6 specific
 * database information. Copyright: Copyright (c) 2001 Company: SAP AG
 * 
 * @author Frank-Martin Haas , Frank-Herwig Walter
 * @version 1.0
 */

public class DbDb6Columns extends DbColumns
{

	private static final Category cat = Category.getCategory(Category.SYS_DATABASE, Logger.CATEGORY_NAME);
	private static Location loc = Logger.getLocation("db6.DbDb6Columns");

	public DbDb6Columns(DbFactory factory)
	{
		super(factory);
	}

	public DbDb6Columns(DbFactory factory, DbColumns other)
	{
		super(factory, other);
	}

	public DbDb6Columns(DbFactory factory, XmlMap xmlMap) throws Exception
	{
		super(factory, xmlMap);
	}

	public void setContentViaDb(DbFactory factory) throws JddException
	{
		loc.entering("setContentViaDb");

		try
		{
			String tableName = this.getTable().getName().toUpperCase();
			DbDb6Environment db6Env = (DbDb6Environment) factory.getEnvironment();
			String schemaName = db6Env.getCurrentSchema();
			DatabaseMetaData dbmd = NativeSQLAccess.getNativeMetaData(factory.getConnection());
			ResultSet rs = dbmd.getColumns(null, schemaName, tableName, null);
			boolean found = false;

			while (rs.next())
			{
				if (tableName.equals(rs.getString("TABLE_NAME")))
				{

					found = true;

					String colName = rs.getString("COLUMN_NAME");
					short sqlType = rs.getShort("DATA_TYPE");
					String dbType = rs.getString("TYPE_NAME");
					int colSize = rs.getInt("COLUMN_SIZE");
					int decDigits = rs.getInt("DECIMAL_DIGITS");
					String defVal = rs.getString("COLUMN_DEF");
					int pos = rs.getInt("ORDINAL_POSITION");
					boolean isNotNull = rs.getString("IS_NULLABLE").trim().equalsIgnoreCase("NO");

					// BINARY, VARBINARY and LONGVARBINARY are mapped to VARCHAR FOR BIT DATA
					// (see JavaDb6SqlTypeInfo). BINARY can not be mapped to CHAR FOR BIT DATA
					// because CHAR FOR BIT DATA allows in DB6 only a length of 254, but
					// the DDIC assumes a maximum length of 255.
					// In the other direction, DB2 maps VARCHAR FOR BIT DATA via DatabaseMetaData
					// (see above) to VARBINARY. To get the correct mapping
					// for the DDIC here we change the resulting VARBINARY to 
					// BINARY OR LONGVARBINARY depending on the length.
					// Note that there is a semantically difference between BINARY (which is
					// fixed-length with trailing zero's) and (LONG)VARBINARY (which has
					// a variable length).
					if (sqlType == java.sql.Types.VARBINARY)
					{
						if (colSize < 256)
							sqlType = java.sql.Types.BINARY;
						else
							sqlType = java.sql.Types.LONGVARBINARY;
					}

					DbColumn col = factory
							.makeDbColumn(colName, pos, sqlType, dbType, colSize, decDigits, isNotNull, defVal);

					this.add(col);
				}
			}
			rs.close();

			if (!found)
			{
				Object[] arguments = {tableName};
				cat.infoT(loc, "DatabaseMetaData did not find table {0}.", arguments);
			}

		} catch (Exception ex)
		{
	      Object[] arguments = {ex.getMessage()};
	      cat.errorT(loc, "setContentViaDb failed: {0}", arguments);
	      loc.exiting();
	      throw JddException.createInstance(ex);
		}

		loc.exiting();
	}

	//
	// Checks if number of columns is allowed
	// return true if number of columns is o.k, false otherwise
	//
	public boolean checkNumber()
	{
		DbColumnIterator iter = this.iterator();
		int cnt = 0;

		loc.entering("checkNumber");

		while (iter.hasNext())
		{
			iter.next();
			cnt++;
		}

		//
		// DB2 allows 1012 columns in a table
		//
		if (cnt > 1012)
		{
			cat.errorT(loc, "checkNumber: DB2 allows only 1012 columns per table");
		}
		loc.exiting();
		return (cnt <= 1012);
	}

}