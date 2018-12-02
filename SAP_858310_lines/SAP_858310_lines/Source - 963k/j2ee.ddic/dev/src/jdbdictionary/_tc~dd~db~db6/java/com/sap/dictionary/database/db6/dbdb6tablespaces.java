package com.sap.dictionary.database.db6;

/**
 * Title: Analysis of table and view changes: DB6 specific classes Description:
 * DB6 specific analysis of table and view changes. Tool to deliver DB6 specific
 * database information. Copyright: Copyright (c) 2001 Company: SAP AG
 * 
 * @author Frank-Martin Haas , Frank-Herwig Walter
 * @version 1.0
 */

public class DbDb6Tablespaces
{
	private String datatablespace = null;
	private String indextablespace = null;
	private String longtablespace = null;

	public DbDb6Tablespaces()
	{
	}

	public void setTableSpaces(String dataTbs, String indexTbs, String longTbs)
	{
		datatablespace = dataTbs;
		if (indextablespace == null)
			indextablespace = dataTbs;
		else
			indextablespace = indexTbs;
		if (longtablespace == null)
			longtablespace = dataTbs;
		else
			longtablespace = longTbs;
	}
	public String getDataTableSpace()
	{
		return datatablespace;
	}

	public String getIndexTableSpace()
	{
		return indextablespace;
	}

	public String getLongTableSpace()
	{
		return longtablespace;
	}

	public String toString()
	{
		return ("DataTableSpace        : " + datatablespace + "\n" + "IndexTableSpace       : " + indextablespace + "\n"
				+ "LongTableSpace        : " + longtablespace);
	}

}