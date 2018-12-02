package com.sap.dictionary.database.db6;

import com.sap.dictionary.database.dbs.*;
import com.sap.sql.NativeSQLAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.io.*;

/**
 * Title: Analysis of table and view changes: DB6 specific classes Description:
 * DB6 specific analysis of table and view changes. Tool to deliver DB6 specific
 * database information. Copyright: Copyright (c) 2001 Company: SAP AG
 * 
 * @author Frank-Martin Haas , Frank-Herwig Walter
 * @version 1.0
 */

public class DbDb6PartitioningKey
{

	private static final String FETCH_PART_COLS = "SELECT COLNAME FROM SYSCAT.COLUMNS "
			+ "WHERE TABSCHEMA = CURRENT SCHEMA AND TABNAME = ? AND PARTKEYSEQ > 0 ORDER BY PARTKEYSEQ";
	private ArrayList<String> partitioningKeyColumns = new ArrayList<String>();

	DbDb6PartitioningKey()
	{
	}

	DbDb6PartitioningKey(XmlMap partKeyMap)
	{
		XmlMap xmlColumns = partKeyMap.getXmlMap("columns");

		if (!xmlColumns.isEmpty())
		{
			XmlMap xmlnextCol = xmlColumns.getXmlMap("column");
			int ii = 0;

			while (!xmlnextCol.isEmpty())
			{
				partitioningKeyColumns.add(xmlnextCol.getString("name"));
				ii++;
				xmlnextCol = xmlColumns.getXmlMap("column" + ii);
			}
		}
	}

	public void setPartitioningKey(ArrayList<String> partitioningKeyColumns)
	{
		this.partitioningKeyColumns = partitioningKeyColumns;
	}

	public ArrayList<String> getPartitioningKey()
	{
		return partitioningKeyColumns;
	}

	public void writeContentToXmlFile(PrintWriter file, String offset)
	{
		if (partitioningKeyColumns != null)
		{
			String offset1 = offset + XmlHelper.tabulate();
			String offset2 = offset1 + XmlHelper.tabulate();

			file.println(offset + "<partitioning-key>");
			file.println(offset1 + "<columns>");
			for (int ii = 0; ii < partitioningKeyColumns.size(); ii++)
			{
				file.println(offset2 + "<column name=\"" + partitioningKeyColumns.get(ii) + "\">");
				file.println(offset2 + "</column>");
			}
			file.println(offset1 + "</columns>");
			file.println(offset + "</partitioning-key>");
		}
	}

	public String toString()
	{
		String outstring = "";

		for (int ii = 0; ii < partitioningKeyColumns.size(); ii++)
		{
			outstring = outstring + "Partitioning Column " + (ii + 1) + " : " + partitioningKeyColumns.get(ii) + "\n";
		}

		return (outstring);
	}

	public void getPartitioningColumns(Connection con, String tableName) throws SQLException
	{
		PreparedStatement partkeyStatement = NativeSQLAccess.prepareNativeStatement(con, FETCH_PART_COLS);
		partkeyStatement.setString(1, tableName);
		ResultSet rset = partkeyStatement.executeQuery();

		while (rset.next())
		{
			partitioningKeyColumns.add(rset.getString(1));
		}
		rset.close();
		partkeyStatement.close();
	}

	public boolean checkColumns(DbColumns cols)
	{
		String colName;

		for (int i = 0; i < partitioningKeyColumns.size(); i++)
		{
			colName = partitioningKeyColumns.get(i);
			if (cols.getColumn(colName) == null)
				return false;
		}
		return true;
	}

	public boolean checkColumns(ArrayList<String> cols)
	{
		if (cols.containsAll(this.partitioningKeyColumns))
			return true;
		else
			return false;
	}
	
	public boolean isEmpty()
	{
		return partitioningKeyColumns.size() > 0 ? false : true; 
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof DbDb6PartitioningKey))
			return false;
		DbDb6PartitioningKey db6partkey = (DbDb6PartitioningKey) obj;
		return this.partitioningKeyColumns.equals(db6partkey.partitioningKeyColumns);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return this.partitioningKeyColumns.hashCode();
	}
}