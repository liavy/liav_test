package com.sap.dictionary.database.db6;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.DbSqlStatement;
import com.sap.dictionary.database.dbs.XmlHelper;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.sql.NativeSQLAccess;

public class DbDb6IncludeColumns
{
	private final static String FETCH_INCLUDE_COLS = "select colname FROM syscat.indexcoluse WHERE indschema = CURRENT SCHEMA AND indname = ? AND colorder = 'I'";
	private ArrayList<String> includeColumnList = new ArrayList<String>();

	DbDb6IncludeColumns()
	{
	}

	DbDb6IncludeColumns(XmlMap xmlIndexmap)
	{
		XmlMap xmlColumns = xmlIndexmap.getXmlMap("columns");

		if (!xmlColumns.isEmpty())
		{
			XmlMap xmlnextCol = xmlColumns.getXmlMap("column");
			int ii = 0;

			while (!xmlnextCol.isEmpty())
			{
				includeColumnList.add(xmlnextCol.getString("name"));
				ii++;
				xmlnextCol = xmlColumns.getXmlMap("column" + ii);
			}
		}
	}

	public ArrayList getIncludeColumnList()
	{
		return includeColumnList;
	}

	public void getIncludeColumns(Connection con, String indexName) throws SQLException
	{
		PreparedStatement includeStatement = NativeSQLAccess.prepareNativeStatement(con, FETCH_INCLUDE_COLS);
		includeStatement.setString(1, indexName);
		ResultSet rset = includeStatement.executeQuery();

		while (rset.next())
		{
			includeColumnList.add(rset.getString(1));
		}
		rset.close();
		includeStatement.close();
	}

	public void writeContentToXmlFile(PrintWriter file, String offset)
	{
		if (includeColumnList.size() > 0)
		{
			String offset1 = offset + XmlHelper.tabulate();
			String offset2 = offset1 + XmlHelper.tabulate();

			file.println(offset + "<include-columns>");
			file.println(offset1 + "<columns>");
			for (int ii = 0; ii < includeColumnList.size(); ii++)
			{
				file.println(offset2 + "<column name=\"" + includeColumnList.get(ii).toString() + "\">");
				file.println(offset2 + "</column>");
			}
			file.println(offset1 + "</columns>");
			file.println(offset + "</include-columns>");
		}
	}

	public String toString()
	{
		String outstring = "";

		for (int ii = 0; ii < includeColumnList.size(); ii++)
		{
			outstring = outstring + "Include Column " + (ii + 1) + " : " + includeColumnList.get(ii) + "\n";
		}

		return (outstring);
	}

	public DbSqlStatement getDdlColumnsClause()
	{
		String line = null;
		DbSqlStatement colDef = new DbSqlStatement();
		int j = includeColumnList.size();

		colDef.addLine("(");
		for (int i = 0; i < j; i++)
		{
			line = "\"" + includeColumnList.get(i) + "\"";
			if (i < j - 1)
				line = line + ", ";
			colDef.addLine(line);
		}
		colDef.addLine(")");

		return colDef;
	}

	public boolean hasIncludeColumns()
	{
		return this.includeColumnList.size() > 0;
	}

	public boolean checkColumns(DbColumns cols)
	{
		String colName;

		for (int i = 0; i < includeColumnList.size(); i++)
		{
			colName = includeColumnList.get(i);
			if (cols.getColumn(colName) == null)
				return false;
		}
		return true;
	}

	public boolean isEmpty()
	{
		return includeColumnList.size() > 0 ? false : true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof DbDb6IncludeColumns))
			return false;
		DbDb6IncludeColumns db6IncludeColumns = (DbDb6IncludeColumns) obj;
		return this.includeColumnList.equals(db6IncludeColumns.includeColumnList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return this.includeColumnList.hashCode();
	}

}
