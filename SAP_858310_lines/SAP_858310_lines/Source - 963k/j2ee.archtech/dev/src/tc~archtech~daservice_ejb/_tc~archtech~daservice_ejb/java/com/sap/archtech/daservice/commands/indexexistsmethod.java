package com.sap.archtech.daservice.commands;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.exceptions.MissingParameterException;
import com.sap.tc.logging.Severity;

public class IndexExistsMethod extends MasterMethod 
{
	private final static String SEL_IDX_DICT = "SELECT INDEXID FROM BC_XMLA_INDEX_DICT WHERE INDEXNAME = ?";

	private final Connection connection;
	private final String index_name;

	public IndexExistsMethod(Connection connection,	HttpServletResponse response, String index_name) 
	{
		this.response = response;
		this.connection = connection;
		this.index_name = index_name == null ? "" : index_name.trim().toLowerCase();
	}

	public boolean execute() throws IOException 
	{
		PreparedStatement selectIdxDict = null;
		ResultSet result = null;
		try 
		{
			checkParams();
			// check if index exists
			selectIdxDict = connection.prepareStatement(SEL_IDX_DICT);
			selectIdxDict.setString(1, index_name);
			result = selectIdxDict.executeQuery();
			if(!result.next()) 
			{
				reportInfoTrace(DasResponse.SC_DOES_NOT_EXISTS,	"INDEX EXISTS: Index " + index_name + " does not exist");
				return false;
			}
			
			response.setHeader("service_message", "Ok");
			return true;
		} 
		catch(SQLException sqlex) 
		{
			reportError(DasResponse.SC_SQL_ERROR, "INDEX EXISTS: " + sqlex.toString(), sqlex);
			return false;
		} 
		catch(MissingParameterException msex) 
		{
			reportError(DasResponse.SC_PARAMETER_MISSING, "INDEX EXISTS: " + msex.getMessage(), msex);
			return false;
		} 
		finally 
		{
			if(result != null)
			{
				try
				{
					result.close();
				}
				catch(SQLException e)
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX EXISTS: " + e.getMessage(), e);
				}
			}
			if(selectIdxDict != null)
			{
				try 
				{
					selectIdxDict.close();
				} 
				catch(SQLException sqlex) 
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX EXISTS: " + sqlex.getMessage(), sqlex);
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