package com.sap.dictionary.database.db6;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.sap.dictionary.database.dbs.DbColumns;
import com.sap.dictionary.database.dbs.XmlHelper;
import com.sap.dictionary.database.dbs.XmlMap;
import com.sap.sql.NativeSQLAccess;

/**
 * @author d025792
 * 
 */
public class DbDb6MDC
{

	private final static String FETCH_MDC_COLS = "select colname, max(dimension) as max from syscat.coluse where type = 'C' "
			+ "and tabname = ? and tabschema = CURRENT SCHEMA group by colname order by max desc";
	private ArrayList mdcColumnList = new ArrayList();

	DbDb6MDC()
	{
	}

	DbDb6MDC(XmlMap xmlMDCmap)
	{
		XmlMap xmlColumns = xmlMDCmap.getXmlMap("columns");

		if (!xmlColumns.isEmpty())
		{
			XmlMap xmlnextCol = xmlColumns.getXmlMap("column");
			int ii = 0;

			while (!xmlnextCol.isEmpty())
			{
				mdcColumnList.add(xmlnextCol.getString("name"));
				ii++;
				xmlnextCol = xmlColumns.getXmlMap("column" + ii);
			}
		}
	}

	public ArrayList getMdcColumnList()
	{
		return mdcColumnList;
	}

	public void setMdcColumnList(ArrayList mdcColumnList)
	{
		this.mdcColumnList = mdcColumnList;
	}

	public void getMDCDimensions(Connection con, String tableName) throws SQLException
	{
		PreparedStatement mdcStatement = NativeSQLAccess.prepareNativeStatement(con, FETCH_MDC_COLS);
		mdcStatement.setString(1, tableName);
		ResultSet rset = mdcStatement.executeQuery();

		while (rset.next())
		{
			mdcColumnList.add(rset.getString(1));
		}
		rset.close();
		mdcStatement.close();
	}

	public void writeContentToXmlFile(PrintWriter file, String offset)
	{
		if (mdcColumnList.size() > 0)
		{
			String offset1 = offset + XmlHelper.tabulate();
			String offset2 = offset1 + XmlHelper.tabulate();

			file.println(offset + "<mdc-dimensions>");
			file.println(offset1 + "<columns>");
			for (int ii = 0; ii < mdcColumnList.size(); ii++)
			{
				file.println(offset2 + "<column name=\"" + mdcColumnList.get(ii) + "\">");
				file.println(offset2 + "</column>");
			}
			file.println(offset1 + "</columns>");
			file.println(offset + "</mdc-dimensions>");
		}
	}

	public String toString()
	{
		String outstring = "";

		for (int ii = 0; ii < mdcColumnList.size(); ii++)
		{
			outstring = outstring + "MDC Column " + (ii + 1) + " : " + mdcColumnList.get(ii) + "\n";
		}

		return (outstring);
	}

	public boolean checkColumns(DbColumns cols)
	{
		String colName;

		for (int i = 0; i < mdcColumnList.size(); i++)
		{
			colName = (String)mdcColumnList.get(i);
			if (cols.getColumn(colName) == null)
				return false;
		}
		return true;
	}

	public boolean isEmpty()
	{
		return mdcColumnList.size() > 0 ? false : true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof DbDb6MDC))
			return false;
		DbDb6MDC db6mdc = (DbDb6MDC) obj;
		return this.mdcColumnList.equals(db6mdc.mdcColumnList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return this.mdcColumnList.hashCode();
	}

}
