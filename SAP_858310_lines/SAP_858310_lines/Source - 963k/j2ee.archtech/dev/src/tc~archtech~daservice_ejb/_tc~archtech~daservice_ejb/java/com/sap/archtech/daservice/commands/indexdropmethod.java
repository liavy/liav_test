package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.exceptions.MissingParameterException;
import com.sap.archtech.daservice.exceptions.NoSuchDBObjectException;
import com.sap.dictionary.database.dbs.JddException;
import com.sap.dictionary.database.opentools.DbTableOpenTools;
import com.sap.dictionary.database.opentools.OpenTools;
import com.sap.tc.logging.Severity;

public class IndexDropMethod extends MasterMethod 
{
	private final static String GET_INDEX = "SELECT * FROM BC_XMLA_INDEX_DICT WHERE indexName = ?";
	private final static String DEL_INDEX = "DELETE FROM BC_XMLA_INDEX_DICT WHERE indexName = ?";
	private final static String DEL_ASSOC = "DELETE FROM BC_XMLA_COL_INDEX WHERE indexId = ?";
	private final static String DEL_COLS = "DELETE FROM BC_XMLA_INDEX_COLS WHERE indexid = ?";
	private final static String DEL_MAXID = "DELETE FROM BC_XMLA_MAXIDS WHERE tablename = ?";
	
	private final static String INDEXPREFIX_MULTI = "BCGEN01_XMLA";

	private final Connection connection;
	private final String index_name;

	public IndexDropMethod(Connection connection, HttpServletResponse response,	String index_name) 
	{
		this.connection = connection;
		this.response = response;
		this.index_name = index_name == null ? "" : index_name.trim().toLowerCase();
	}

	public boolean execute() throws IOException 
	{
		PreparedStatement getIdx = null;
		PreparedStatement delIdx = null;
		PreparedStatement delAssoc = null;
		PreparedStatement delCols = null;
		PreparedStatement delMaxIds = null;
		ResultSet rs1 = null;
		try 
		{
			checkParams();
			// Check if index with this name exists
			getIdx = connection.prepareStatement(GET_INDEX);
			getIdx.setString(1, index_name);
			rs1 = getIdx.executeQuery();
			if(!rs1.next())
			{
				throw new NoSuchDBObjectException("Index " + index_name	+ " does not exist");
			}
			String tableName = rs1.getString("indexTable");
			long indexId = rs1.getLong("indexId");
			// delete table
			OpenTools dyndict = new DbTableOpenTools(connection);
			boolean isTableDropped = dyndict.dropTable(tableName);
			if(!isTableDropped)
			{
				throw new SQLException("Unable to drop index table " + tableName + ". Check log files.");
			}
			// OpenSQL-version without foreign keys:
			// delete entry from TABLES BC_XMLA_COL_INDEX,
			// BC_XMLA_INDEX_COLS and BC_XMLA_INDEX_DICT
			delAssoc = connection.prepareStatement(DEL_ASSOC);
			delAssoc.setLong(1, indexId);
			delAssoc.executeUpdate();
			delCols = connection.prepareStatement(DEL_COLS);
			delCols.setLong(1, indexId);
			delCols.executeUpdate();
			delIdx = connection.prepareStatement(DEL_INDEX);
			delIdx.setString(1, index_name);
			delIdx.executeUpdate();
			// delete entry from BC_XMLA_MAXIDS table
			if(tableName.startsWith(INDEXPREFIX_MULTI))
			{
				delMaxIds = connection.prepareStatement(DEL_MAXID);
				delMaxIds.setString(1, tableName);
				delMaxIds.executeUpdate();
			}
			
			response.setHeader("service_message", "Ok");
			return true;
		} 
		catch(SQLException sqlex) 
		{
			reportError(DasResponse.SC_SQL_ERROR, "INDEX DROP: "+ sqlex.getMessage(), sqlex);
			return false;
		} 
		catch(MissingParameterException msex) 
		{
			reportError(DasResponse.SC_PARAMETER_MISSING, "INDEX DROP: " + msex.getMessage(), msex);
			return false;
		} 
		catch(NoSuchDBObjectException nsdbex) 
		{
			reportError(DasResponse.SC_DOES_NOT_EXISTS, "INDEX DROP: " + nsdbex.getMessage(), nsdbex);
			return false;
		} 
		catch(JddException jddex) 
		{
			reportError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,	"INDEX DROP: " + jddex.getMessage(), jddex);
			return false;
		} 
		finally 
		{
			if(rs1 != null)
			{
				try
				{
					rs1.close();
				}
				catch(SQLException e)
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX DROP: " + e.getMessage(), e);
				}
			}
			if(getIdx != null)
			{
				try
				{
					getIdx.close();
				}
				catch(SQLException sqlex) 
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX DROP: " + sqlex.getMessage(), sqlex);
				}
			}
			if(delIdx != null)
			{
				try
				{
					delIdx.close();
				}
				catch(SQLException sqlex) 
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX DROP: " + sqlex.getMessage(), sqlex);
				}
			}
			if(delAssoc != null)
			{
				try
				{
					delAssoc.close();
				}
				catch(SQLException sqlex) 
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX DROP: " + sqlex.getMessage(), sqlex);
				}
			}
			if(delCols != null)
			{
				try
				{
					delCols.close();
				}
				catch(SQLException sqlex) 
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX DROP: " + sqlex.getMessage(), sqlex);
				}
			}
			if(delMaxIds != null)
			{
				try
				{
					delMaxIds.close();
				}
				catch(SQLException sqlex) 
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX DROP: " + sqlex.getMessage(), sqlex);
				}
			}
		}
	}
	
	private void checkParams() throws MissingParameterException 
	{
		if("".equals(index_name))
		{
			throw new MissingParameterException("No index name specified");
		}
	}
}