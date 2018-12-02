package com.sap.archtech.daservice.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.archtech.daservice.exceptions.MissingParameterException;
import com.sap.archtech.daservice.exceptions.NoSuchDBObjectException;
import com.sap.tc.logging.Severity;

public class IndexDescribeMethod extends MasterMethod 
{
	private final static String GET_IDXINFO = "SELECT c.propName, c.jdbcType, c.vlength, indexcolid, i.indextable FROM BC_XMLA_INDEX_DICT i, BC_XMLA_INDEX_COLS c WHERE i.indexId = c.indexId AND i.indexName = ? ORDER BY indexColId";
	
	private final static String INDEXPREFIX_MULTI = "BCGEN01_XMLA";

	private final Connection connection;
	private final String index_name;

	public IndexDescribeMethod(Connection connection,	HttpServletResponse response, String index_name) 
	{
		this.connection = connection;
		this.response = response;
		this.index_name = index_name == null ? "" : index_name.trim().toLowerCase();
	}

	public boolean execute() throws IOException 
	{
		response.setContentType(MasterMethod.contentType);
		response.setHeader("service_message", "see message body");
		response.setHeader("index_name", index_name);

		BufferedWriter bwout = null;
		try 
		{
			checkParams();
			bwout = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"));
			generateOutput(readProps(), bwout);
			return true;
		} 
		catch(MissingParameterException msex) 
		{
			reportStreamError(DasResponse.SC_PARAMETER_MISSING, "INDEX DESCRIBE: " + msex.getMessage(), msex, bwout);
			return false;
		} 
		catch(NoSuchDBObjectException nsdbex) 
		{
			reportStreamInfo(DasResponse.SC_DOES_NOT_EXISTS, "INDEX DESCRIBE: " + nsdbex.getMessage(), bwout);
			return false;
		} 
		catch(SQLException sqlex) 
		{
			reportStreamError(DasResponse.SC_SQL_ERROR, "INDEX DESCRIBE: " + sqlex.getMessage(), sqlex, bwout);
			return false;
		} 
		catch(IOException ioex) 
		{
			reportStreamError(DasResponse.SC_IO_ERROR, "INDEX DESCRIBE: "	+ ioex.getMessage(), ioex, bwout);
			return false;
		} 
		finally
		{
			if(bwout != null)
			{
				try
				{
					bwout.close();
				}
				catch(IOException e)
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX DESCRIBE: " + e.getMessage(), e);
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

	private ArrayList<String> readProps()	throws SQLException, NoSuchDBObjectException 
	{
		PreparedStatement getIdx = null;
		ResultSet rs1 = null;
		try
		{
			getIdx = connection.prepareStatement(GET_IDXINFO);
			getIdx.setString(1, index_name);
			rs1 = getIdx.executeQuery();
			ArrayList<String> idxinfo = new ArrayList<String>();
			String propName = null;
			String propType = null;
			int vlength = 0;
			boolean isMultiValSupportSet = false;
			while(rs1.next()) 
			{
				if(!isMultiValSupportSet)
				{
					// table name is equal for all result rows
					response.setHeader("multi_val_support", rs1.getString("indextable").startsWith(INDEXPREFIX_MULTI) ? "Y" : "N");
					isMultiValSupportSet = true;
				}
				propName = rs1.getString("propName");
				propType = rs1.getString("jdbcType");
				if("VARCHAR".equalsIgnoreCase(propType)) 
				{
					vlength = rs1.getInt("vlength");
					propType = new StringBuilder(propType).append("(").append(vlength).append(")").toString();
				}
				idxinfo.add(new StringBuilder(propName).append(':').append(propType.toUpperCase()).toString());
			}
			if(idxinfo.isEmpty())
			{
				throw new NoSuchDBObjectException("Index " + index_name + " does not exist");
			}
			return idxinfo;
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
					cat.logThrowableT(Severity.WARNING, loc, "INDEX DESCRIBE: " + e.getMessage(), e);
				}
			}
			if(getIdx != null)
			{
				try
				{
					getIdx.close();
				}
				catch(SQLException e)
				{
					cat.logThrowableT(Severity.WARNING, loc, "INDEX DESCRIBE: " + e.getMessage(), e);
				}
			}
		}
	}

	private void generateOutput(final ArrayList<String> indexProps,	final BufferedWriter bwout) throws IOException 
	{
		for(String indexProp : indexProps) 
		{
			bwout.write(indexProp);
			bwout.write(13);
			bwout.write(10);
		}
		writeStatus(bwout, HttpServletResponse.SC_OK, "Ok");
		bwout.flush();
	}
}
