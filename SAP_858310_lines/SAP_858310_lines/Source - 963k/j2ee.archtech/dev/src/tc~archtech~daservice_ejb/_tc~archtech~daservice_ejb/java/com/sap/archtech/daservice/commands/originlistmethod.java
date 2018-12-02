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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.archconn.util.DasResponse;
import com.sap.tc.logging.Severity;

public class OriginListMethod extends MasterMethod
{
	private static final String SELECT_ORIGINVALUES_FOR_URIS_PREFIX
		= "SELECT colPropTab.colpropvalue, colTab.uri FROM BC_XMLA_COL colTab, BC_XMLA_COL_PROP colPropTab WHERE colTab.colid = colPropTab.colid AND colPropTab.colpropname = 'origin' AND colTab.uri IN (";
	
	private final HttpServletRequest request;
	private final Connection conn;
	
	public OriginListMethod(HttpServletRequest request, HttpServletResponse response, Connection conn)
	{
		this.request = request;
		this.response = response;
		this.conn = conn;	
	}

	public boolean execute() throws IOException 
	{
		// get the request body
		BufferedReader bodyReader =	new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
		BufferedWriter responseWriter =	new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
		// write response header
		response.setContentType(MasterMethod.contentType);
		response.setHeader("service_message", "see message body");
		
		Statement sqlQuery = null;
		ResultSet rs = null;
		try 
		{
			// the request body may contain several lines each holding one URI
			String uri = null;
			Set<String> uriAndAncestors = null;
			StringBuilder responseLine = null;
			StringBuilder sqlQueryString = null;
			Map<String, String> origVal2Uri = null;
			String origVal = null;
			boolean hasBody = false;
			while((uri = bodyReader.readLine()) != null)
			{
				hasBody = true;
				// check the URI
				if(!checkUri(uri, responseWriter))
				{
					return false;
				}
				// create ancestor collection URIs from each given URI (trailing slashes have been cut)
				uriAndAncestors = OriginSearchUtil.createAncestors(uri, "/");
				// remove root collection -> has never an "origin" property set
				uriAndAncestors.remove("/");
				// add given URI to the set
				uriAndAncestors.add(uri);
				// get "origin" property value for each collection URI
				sqlQueryString = new StringBuilder(SELECT_ORIGINVALUES_FOR_URIS_PREFIX);
				for(String cachedUri : uriAndAncestors)
				{
					sqlQueryString.append("'").append(cachedUri).append("',");
				}
				// cut last ","
				sqlQueryString.deleteCharAt(sqlQueryString.length()-1);
				// append ")"
				sqlQueryString.append(')');
				// execute query and cache its result
				sqlQuery = conn.createStatement();	
				rs = sqlQuery.executeQuery(sqlQueryString.toString());
				origVal2Uri = new HashMap<String, String>();
				while(rs.next())
				{
					origVal2Uri.put(rs.getString("uri"), rs.getString("colpropvalue"));
				}
				// create response line
				responseLine = new StringBuilder("/");
				for(String cachedUri : uriAndAncestors)
				{
					origVal = origVal2Uri.get(cachedUri);
					responseLine.append(origVal != null ? origVal : "").append('/');
				}
				// write result to response stream
				responseWriter.write(responseLine.toString());
				// add an CR/LF platform independent
				responseWriter.write(13);
				responseWriter.write(10);
			}
			if(!hasBody)
			{
				// empty request body
				reportStreamError(DasResponse.SC_PARAMETER_MISSING,	"ORIGINLIST: Empty request body", responseWriter);
				return false;							
			}
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
			if(rs != null)
			{
				try 
				{
					rs.close();
				} 
				catch(SQLException e) 
				{
					cat.logThrowableT(Severity.WARNING,	loc, "Could not close SQL result set: " + e.getMessage(), e);
				}
			}
			if(sqlQuery != null)
			{
				try 
				{
					sqlQuery.close();
				} 
				catch(SQLException e) 
				{
					cat.logThrowableT(Severity.WARNING,	loc, "Could not close SQL statement: " + e.getMessage(), e);
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
	}
	
 	// Helpers
 	private boolean checkUri(final String uri, final BufferedWriter responseWriter) throws IOException
 	{
		if(uri == null || uri.equals("")) 
		{
			reportStreamError(DasResponse.SC_PARAMETER_MISSING,	"ORIGINLIST: Missing URI in request body", responseWriter);
			return false;
		}
		if(			uri.indexOf("//") != -1
				||	uri.indexOf("\\") != -1
				||	!uri.startsWith("/") 
				||	uri.endsWith("/")) 
		{
			reportStreamError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT, "ORIGINLIST: URI " + uri + " does not comply with specification", responseWriter);
			return false;
		}
		return true;
 	}
}
