package com.sap.archtech.daservice.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.daservice.data.DasResponse;
import com.sap.tc.logging.Severity;

public class ColSearchMethod extends MasterMethod implements IColSearchExecutionTemplate
{
	private static final String SELECT_ALL_COLPROPVALUES = "SELECT DISTINCT colpropvalue FROM BC_XMLA_COL_PROP WHERE colpropname = ";
	private static final String SELECT_ALL_COLPROPVALUES_BELOWROOT = "SELECT DISTINCT colPropTab.colpropvalue FROM BC_XMLA_COL colTab, BC_XMLA_COL_PROP colPropTab WHERE colTab.colid = colPropTab.colid AND colPropTab.colpropname = ";
	private static final String SELECT_PREFIX_JOIN = "SELECT colTab.uri, colTab.creationtime, colTab.creationuser, colTab.frozen, colTab.coltype, colPropTab.colpropvalue FROM BC_XMLA_COL colTab, BC_XMLA_COL_PROP colPropTab WHERE colTab.colid = colPropTab.colid AND";
	private static final String SELECT_PREFIX_PROPONLY = "SELECT colPropTab.colname FROM BC_XMLA_COL_PROP colPropTab WHERE";
	private static final String SELECT_PREFIX_PROPONLY_BELOWROOT = "SELECT colPropTab.colname FROM BC_XMLA_COL colTab, BC_XMLA_COL_PROP colPropTab WHERE colTab.colid = colPropTab.colid AND";
	private static final String AND_CLAUSE_COLPROPNAME = " colPropTab.colpropname = ";
	private static final String AND_CLAUSE_COLPROPVALUE = " AND colPropTab.colpropvalue = ";
	private static final String AND_CLAUSE_COLNAME = " AND colPropTab.colname ";
	private static final String AND_CLAUSE_SEARCHROOTURI = " AND colTab.uri LIKE ";
	private static final Set<String> allowedOperators = new HashSet<String>(7);

	static 
	{
		allowedOperators.add(">");
		allowedOperators.add("<");
		allowedOperators.add("=");
		allowedOperators.add(">=");
		allowedOperators.add("<=");
		allowedOperators.add("<>");
		allowedOperators.add("LIKE");
	}

	protected static final int RESULT_TYPE_COMPLETE = 1;
	private static final int RESULT_TYPE_COLLNAMES = 2;
	private static final int RESULT_TYPE_COLPROPVALUES = 3;

	private final HttpServletRequest request;
	private final Connection conn;
	private final boolean colnameOnly;
	private final String searchRootUri;
	private int resultType;
	private final Set<String> colPropValues;

	public ColSearchMethod(HttpServletRequest request, HttpServletResponse response, Connection conn, String colnameOnly, String searchRootUri) 
	{
		this(request, response, conn, colnameOnly, searchRootUri, false);
	}

	protected ColSearchMethod(HttpServletRequest request, HttpServletResponse response, Connection conn, String colnameOnly, String searchRootUri, boolean mustKeepColPropValues) 
	{
		this.response = response;
		this.request = request;
		this.conn = conn;
		if(colnameOnly == null || !"Y".equals(colnameOnly.toUpperCase())) 
		{
			this.colnameOnly = false;
		} 
		else 
		{
			this.colnameOnly = true;
		}
		if(searchRootUri != null)
		{
			searchRootUri = searchRootUri.toLowerCase().trim();
			if(!"".equals(searchRootUri) && searchRootUri.endsWith("/"))
			{
				// cut trailing slash
				searchRootUri = searchRootUri.substring(0, searchRootUri.length() - 1);
			}
			this.searchRootUri = searchRootUri;
		}
		else
		{
			this.searchRootUri = "";
		}
		resultType = this.colnameOnly ? RESULT_TYPE_COLLNAMES : RESULT_TYPE_COMPLETE;
		if(mustKeepColPropValues)
		{
			colPropValues = new HashSet<String>();
		}
		else
		{
			colPropValues = null;
		}
	}

	// Template method
	public boolean execute() throws IOException 
	{
		// get the request body
		BufferedReader bodyReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
		BufferedWriter responseWriter = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
		// write response header
		response.setContentType(MasterMethod.contentType);
		response.setHeader("service_message", "see message body");
		// may be overwritten later on
		response.setHeader("result_type", new Integer(resultType).toString());
		
		ArrayList<Statement> sqlQueries = null;
		ArrayList<ResultSet> sqlResults = null;
		try 
		{
			// get query parts
			ArrayList<String> queryParts = parseRequestBody(bodyReader, responseWriter);
			if(queryParts.size() == 0)
			{
				return false;
			}
			sqlQueries = new ArrayList<Statement>(queryParts.size());
			sqlResults = new ArrayList<ResultSet>(queryParts.size());
			// create SQL query from each query part
			String queryStatement = null;
			Statement sqlQuery = null;
			for(String queryPart : queryParts)
			{
				// create SQL statement
				queryStatement = createQueryStatement(queryPart, responseWriter);
				if(queryStatement == null) 
				{
					return false;
				}
				// execute SQL statement
				sqlQuery = conn.createStatement();
				if(resultType == RESULT_TYPE_COLPROPVALUES)
				{
					// special case: only 1 query
					// -> remove all other queries (if there were any)
					sqlQueries.clear();
					sqlResults.clear();
					sqlQueries.add(sqlQuery);// keep reference for clean-up
					sqlResults.add(sqlQuery.executeQuery(queryStatement));
					break;
				}
				else
				{
					sqlQueries.add(sqlQuery);// keep reference for clean-up
					sqlResults.add(sqlQuery.executeQuery(queryStatement));
				}
			}
			// write query results
			respondQueryResults(sqlResults, responseWriter);
			// write result_type
			response.setHeader("result_type", Integer.valueOf(resultType).toString());
			// write response status
			writeStatus(responseWriter, HttpServletResponse.SC_OK, "Ok");
			responseWriter.flush();
			return true;
		} 
		catch(SQLException e) 
		{
			reportStreamError(DasResponse.SC_SQL_ERROR,	e.getMessage(),	responseWriter);
			return false;
		} 
		catch(RuntimeException e)
		{
			reportStreamError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), responseWriter);
			return false;
		}
		finally 
		{
			cleanUp(sqlResults, sqlQueries, bodyReader, responseWriter);
		}
	}

	// Implementation of IColSearchExecutionTemplate
	public ArrayList<String> parseRequestBody(BufferedReader bodyReader, BufferedWriter responseWriter) throws IOException
	{
		//	the request body should only contain one line
		String queryString = bodyReader.readLine();
		if(queryString == null || queryString.trim().equals("")) 
		{
			reportStreamError(DasResponse.SC_PARAMETER_MISSING,	"COLSEARCH: Missing query string in request body", responseWriter);
			return new ArrayList<String>(0);
		}
		// there is no nesting in the query string of this method
		ArrayList<String> queryParts = new ArrayList<String>(1);
		queryParts.add(queryString);
		return queryParts;
	}
	
	public void respondQueryResults(ArrayList<ResultSet> sqlResults, BufferedWriter responseWriter) throws SQLException, IOException
	{
		switch(resultType) 
		{
			case RESULT_TYPE_COMPLETE:
				for(ResultSet result : sqlResults)	
				{	
					while(result.next()) 
					{
						responseWriter.write(result.getString("uri") + ";");
						responseWriter.write("COL;");
						responseWriter.write(getUTCString(result.getTimestamp("creationtime")) + ";");
						responseWriter.write(result.getString("creationuser") + ";");
						responseWriter.write(result.getString("frozen") + ";");
						responseWriter.write(result.getString("coltype"));
						// add an CR/LF platform independent
						responseWriter.write(13);
						responseWriter.write(10);
					}
				}
				break;
				
			case RESULT_TYPE_COLLNAMES:
				for(ResultSet result : sqlResults)	
				{	
					while(result.next()) 
					{
						responseWriter.write(result.getString("colname"));
						// add an CR/LF platform independent
						responseWriter.write(13);
						responseWriter.write(10);
					}
				}
				break;
				
			case RESULT_TYPE_COLPROPVALUES:
				for(ResultSet result : sqlResults)	
				{	
					while(result.next()) 
					{
						responseWriter.write(result.getString("colpropvalue"));
						// add an CR/LF platform independent
						responseWriter.write(13);
						responseWriter.write(10);
					}
				}
				break;

			default:
				break;
		}
	}
	
	// Helpers
	private String createQueryStatement(String queryString, BufferedWriter responseWriter) throws IOException, SQLException 
	{
		// the query may consist of several parts separated by "&"
		StringTokenizer tokenizerAND = new StringTokenizer(queryString.trim(), "&");
		String queryPart = null;
		StringTokenizer tokenizerBLANK = null;
		String colpropnameAndValue = null;
		String colPropName = null;
		String colPropValue = null;
		String operator = null;
		String colname = null;
		int dotIdx = 0;
		// SELECT prefix
		StringBuilder sql = new StringBuilder(createSelectPrefix());
		boolean isFirstQueryPart = true;
		while(tokenizerAND.hasMoreTokens()) 
		{
			queryPart = tokenizerAND.nextToken().trim();
			// first token must contain "colpropname.colpropvalue"
			tokenizerBLANK = new StringTokenizer(queryPart, " ");
			int tokensByBLANK = tokenizerBLANK.countTokens();
			colpropnameAndValue = tokenizerBLANK.nextToken();
			dotIdx = colpropnameAndValue.indexOf('.');
			if(dotIdx == -1	|| dotIdx == 0 || dotIdx == colpropnameAndValue.length() - 1) 
			{
				reportStreamError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT, "COLSEARCH: Query string \""	+ queryString	+ "\" did not comply with specification",	responseWriter);
				return null;
			}
			colPropName = colpropnameAndValue.substring(0, dotIdx).toLowerCase();
			colPropValue = colpropnameAndValue.substring(dotIdx + 1, colpropnameAndValue.length());
			if(colPropValues != null)
			{
				// keep colPropValue for later usage
				colPropValues.add(colPropValue);
			}
			if("*".equals(colPropValue)) 
			{
				// special case: select all colpropvalues, ignore the rest of the query string
				return createSelect4AllPropValues(colPropName);
			}
			// each query part must contain 3 tokens separated by blanks
			if(tokensByBLANK != 3) 
			{
				reportStreamError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT, "COLSEARCH: Query string \""	+ queryString	+ "\" did not comply with specification",	responseWriter);
				return null;
			}
			// second token must contain a valid comparation operator
			operator = tokenizerBLANK.nextToken();
			if(!allowedOperators.contains(operator)) 
			{
				reportStreamError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT, "COLSEARCH: Query string \"" + queryString	+ "\" did not comply with specification",	responseWriter);
				return null;
			}
			// third token contains the colname (may contain '%' characters -> LIKE)
			colname = tokenizerBLANK.nextToken().toLowerCase();
			// add colPropName, colPropValue, operator and colname to sql string
			sql.append(createANDClauses(isFirstQueryPart, colPropName, colPropValue, operator, colname));
			
			isFirstQueryPart = false;
		}
		// add LIKE clause if searchRootUri has been passed
		sql.append(createANDClause4SearchRoot());
		return sql.toString();
	}
	
	private void cleanUp(ArrayList<ResultSet> sqlResults, ArrayList<Statement> sqlQueries, BufferedReader bodyReader, BufferedWriter responseWriter)
	{
		if(sqlResults != null)
		{
			for(ResultSet result : sqlResults)	
			{		
				try 
				{
					result.close();
				} 
				catch(SQLException e) 
				{
					cat.logThrowableT(Severity.WARNING,	loc, "Could not close SQL result set: " + e.getMessage(), e);
				}
			}
		}
		
		if(sqlQueries != null)
		{
			for(Statement statement : sqlQueries)	
			{	
				try 
				{
					statement.close();
				} 
				catch(SQLException e) 
				{
					cat.logThrowableT(Severity.WARNING,	loc, "Could not close SQL statement: " + e.getMessage(), e);
				}
			}
		}
		
		if(bodyReader != null) 
		{
			try
			{
				bodyReader.close();
			}
			catch(IOException e)
			{
				cat.logThrowableT(Severity.WARNING,	loc, "Could not close HTTP request body reader: " + e.getMessage(), e);
			}
		}
		if(responseWriter != null) 
		{
			try
			{
				responseWriter.close();
			}
			catch(IOException e)
			{
				cat.logThrowableT(Severity.WARNING,	loc, "Could not close HTTP response writer: " + e.getMessage(), e);
			}
		}
	}

	private boolean hasSearchRootUri()
	{
		return !"".equals(searchRootUri);
	}
	
	private String createSelectPrefix()
	{
		if(colnameOnly)
		{
			// return only collection names
			if(hasSearchRootUri())
			{
				// take search root into account
				return SELECT_PREFIX_PROPONLY_BELOWROOT;
			}
			else
			{
				return SELECT_PREFIX_PROPONLY;
			}
		}
		else
		{
			// return collection metadata
			return SELECT_PREFIX_JOIN;
		}
	}
	
	private String createSelect4AllPropValues(final String colPropName)
	{
		resultType = RESULT_TYPE_COLPROPVALUES;
		if(hasSearchRootUri())
		{
			return new StringBuilder(SELECT_ALL_COLPROPVALUES_BELOWROOT)
				.append("'").append(colPropName).append("'")
				.append(AND_CLAUSE_SEARCHROOTURI).append("'").append(searchRootUri).append("%'").toString();
		}
		else
		{
			return new StringBuilder(SELECT_ALL_COLPROPVALUES)
				.append("'").append(colPropName).append("'").toString();
		} 
	}
	
	private String createANDClauses(final boolean isFirstANDClause, final String colPropName, final String colPropValue, final String operator, final String colName)
	{
		// add colPropName and colPropValue to sql string
		StringBuilder sql = new StringBuilder("");
		if(!isFirstANDClause) 
		{
			sql.append(" AND");
		}
		sql.append(AND_CLAUSE_COLPROPNAME).append("'").append(colPropName).append("'")
			 .append(AND_CLAUSE_COLPROPVALUE).append("'").append(colPropValue).append("'");
		// add operator to sql string
		sql.append(AND_CLAUSE_COLNAME).append(operator).append(' ');
		// add colname to sql string
		sql.append("'").append(colName).append("'");
		return sql.toString();
	}
	
	private String createANDClause4SearchRoot()
	{
		if(hasSearchRootUri())
		{
			return new StringBuilder(AND_CLAUSE_SEARCHROOTURI).append("'").append(searchRootUri).append("%'").toString();
		}
		return "";
	}
	
	protected int getResultType()
	{
		return resultType;
	}
	
	protected String getSearchRootUri()
	{
		return searchRootUri;
	}
		
	protected Connection getConnection()
	{
		return conn;
	}

	protected Set<String> getColPropValuesFromRequest()
	{
		return colPropValues != null ? new HashSet<String>(colPropValues) : new HashSet<String>(0);
	}
}
