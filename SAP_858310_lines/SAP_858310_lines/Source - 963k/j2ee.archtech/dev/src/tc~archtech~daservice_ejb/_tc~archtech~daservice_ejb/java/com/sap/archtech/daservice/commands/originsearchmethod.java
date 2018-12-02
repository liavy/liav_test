package com.sap.archtech.daservice.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sap.archtech.archconn.util.DasResponse;
import com.sap.tc.logging.Severity;

public class OriginSearchMethod extends ColSearchMethod implements IColSearchExecutionTemplate
{
	private static final String SELECT_ORIGIN = "SELECT colPropTab.colpropvalue FROM BC_XMLA_COL colTab, BC_XMLA_COL_PROP colPropTab " +
		"WHERE colTab.colid = colPropTab.colid AND colPropTab.colpropname = 'origin' AND colTab.uri = ?";

	public OriginSearchMethod(HttpServletRequest request, HttpServletResponse response, Connection conn, String searchRootUri)
	{
		// colname_only not supported
		super(request, response, conn, null, searchRootUri, true);
	}
	
	// Implementation of IColSearchExecutionTemplate
	public ArrayList<String> parseRequestBody(BufferedReader bodyReader, BufferedWriter responseWriter) throws IOException
	{
		// the request body should only contain one line
		String queryString = bodyReader.readLine();
		// parse the queryString
		if(queryString == null || queryString.trim().equals("")) 
		{
			reportStreamError(DasResponse.SC_PARAMETER_MISSING,	"ORIGINSEARCH: Missing query string in request body", responseWriter);
			return new ArrayList<String>(0);
		}
		// the query may consist of several parts separated by "+"
		StringTokenizer tokenizerPLUS = new StringTokenizer(queryString.trim(), "+");
		ArrayList<String> queryParts = new ArrayList<String>();
		String queryPart = null;
		while(tokenizerPLUS.hasMoreTokens()) 
		{
			queryPart = tokenizerPLUS.nextToken().trim();
			if(queryPart.length() > 0)
			{
				// the request must not contain "ADMI_RUN_D"
				if(queryPart.indexOf("origin.ADMI_RUN_D") != -1)
				{
					reportStreamError(DasResponse.SC_PARAMETER_SYNTAX_INCORRECT, "ORIGINSEARCH: Query string \""	+ queryString	+ "\" must not contain the reserved origin property \"ADMIN_RUN_D\"",	responseWriter);
					return new ArrayList<String>(0);
				}
				queryParts.add(queryPart);
			} 
		}
		// add ADMI_RUN_D search
		queryParts.add("origin.ADMI_RUN_D LIKE %");
		return queryParts;
	}
	
	public void respondQueryResults(ArrayList<ResultSet> sqlResults, BufferedWriter responseWriter) throws SQLException, IOException
	{
		if(getResultType() != RESULT_TYPE_COMPLETE)
		{
			throw new UnsupportedOperationException("ORIGINSEARCH does not support queries other than for complete collection data");
		}
		
		String collUri = null;
		String collData = null;
		String origin = null;
		// one map for all collections representing an archiving session (origin = "ADMI_RUN_D")
		// -> archiving sessions are the leaves in the archive hierarchy
		HashMap<String, String> archSessionCollData2Uri = new HashMap<String, String>();
		// map to store results sorted by their origin
		HashMap<String, Set<String>> collUris2Origin = new HashMap<String, Set<String>>();
		Set<String> origins = getColPropValuesFromRequest();
		for(String requestedOrigin : origins)
		{
			// create an empty set for each origin requested
			collUris2Origin.put(requestedOrigin, new HashSet<String>());
		}
		Set<String> collUris = null;
		for(ResultSet result : sqlResults)	
		{	
			while(result.next()) 
			{
				collUri = result.getString("uri");
				origin = result.getString("colpropvalue");
				if("ADMI_RUN_D".equals(origin))
				{
					// store the metadata of all archiving session collections
					collData = new StringBuilder(collUri)
						.append(";COL;")
						.append(getUTCString(result.getTimestamp("creationtime"))).append(";")
						.append(result.getString("creationuser")).append(";")
						.append(result.getString("frozen")).append(";")
						.append(result.getString("coltype"))
						.toString();
					archSessionCollData2Uri.put(collUri, collData);
				}
				else
				{
					// for all other origins only the collection URIs must be kept
					// -> the next step in the algorithm checks which of the archiving session collections match these collection URIs
					// -> only those archiving session collections will be returned in the response
					collUris = collUris2Origin.get(origin);
					collUris.add(collUri);
				}
			}
		}
		// -> next step is not necessary any longer because archiving session collections cannot be hierarchically nested
//	removeAncestorUris(archSessionCollData2Uri);
	// check which of the archiving session collections match the request
		Set<String> ancestors = null;
		Set<String> ignorableAncestorsCache = new HashSet<String>();
		Set<String> invalidAncestorsCache = new HashSet<String>();
		String ancestorRootUri = getSearchRootUri();
		if("".equals(ancestorRootUri))
		{
			ancestorRootUri = "/";
		}
		boolean archSessionCollUriMatches = true;
		int[] nrOfMatchingAncestors = new int[1];// out param
		for(Entry<String, String> archSessionCollData2UriEntry : archSessionCollData2Uri.entrySet())
		{
			collUri = archSessionCollData2UriEntry.getKey();
			archSessionCollUriMatches = true;
			// create ancestors for all archiving session collection URIs and check if they match the request
			ancestors = OriginSearchUtil.createAncestors(collUri, ancestorRootUri);
			nrOfMatchingAncestors[0] = 0;
			for(String ancestor : ancestors)
			{
				if(ancestorViolatesSearchRequest(ancestor, ignorableAncestorsCache, invalidAncestorsCache, collUris2Origin, nrOfMatchingAncestors))
				{
					// do not return this collection URI (one of its ancestors violates the search request)
					archSessionCollUriMatches = false;
					break;
				}
			}
			// at least one of the ancestors must match the search request
			if(archSessionCollUriMatches && nrOfMatchingAncestors[0] == 0)
			{
				// do not return this collection URI (none of its ancestors matches the search request)
				archSessionCollUriMatches = false;
			}
			if(archSessionCollUriMatches)
			{
				// write collection metadata in response stream (one collection data per line)
				responseWriter.write(archSessionCollData2UriEntry.getValue());		
				// add an CR/LF platform independent
				responseWriter.write(13);
				responseWriter.write(10);
			}
		}
	}
	
//	private void removeAncestorUris(TreeMap<String, String> collData2Uri)
//	{
//		// put all URIs in list and revert their order, hence the URIs will be sorted in descending order
//		ArrayList<String> uriList = new ArrayList<String>(collData2Uri.keySet());
//		Collections.reverse(uriList);
//		// remove all collection URIs that are located hierarchically above another collection
//		String previousCollUri = null;
//		String nextCollUri = null;
//		for(Iterator<String> collIter = uriList.iterator(); collIter.hasNext();)
//		{
//			nextCollUri = (String)collIter.next();
//			if(previousCollUri == null)
//			{
//				// first URI in sorted set -> do nothing
//			 	previousCollUri = nextCollUri;
//			}
//			else
//			{
//				if(previousCollUri.startsWith(nextCollUri))
//				{
//					// nextCollUri refers to a collection which is located hierarchically above previousCollUri -> remove nextCollUri
//					collIter.remove();
//					// remove entry from the given collData map
//					collData2Uri.remove(nextCollUri);
//				}
//				previousCollUri = nextCollUri;
//			}
//		}
//	}
	
	private boolean ancestorViolatesSearchRequest(final String ancestor, final Set<String> ignorableAncestorsCache, final Set<String> invalidAncestorsCache, final Map<String, Set<String>> collUris2Origin, int[] nrOfMatchingAncestors)
	throws SQLException
	{
		//*** check if the given ancestor of a certain URI matches the origin search request
		//*** if it does not, then that URI must not be returned in the response
		
		// check caches first
		if(ignorableAncestorsCache.contains(ancestor))
		{
			return false;
		}
		if(invalidAncestorsCache.contains(ancestor))
		{
			return true;
		}
		
		PreparedStatement selectOrigin = null;
		ResultSet rs = null;
		try
		{
			// get origin value
			Connection conn = getConnection();
			selectOrigin = conn.prepareStatement(SELECT_ORIGIN);
			selectOrigin.setString(1, ancestor);
			rs = selectOrigin.executeQuery();
			if(!rs.next())
			{
				// -> no origin stored for this ancestor => this ancestor does not violate the request
				ignorableAncestorsCache.add(ancestor);
				return false;
			}
			String origin = rs.getString("colpropvalue");
			// get the set of collection URIs matching the request for this origin
			Set<String> collUris = collUris2Origin.get(origin);
			if(collUris == null)
			{
				// -> this origin was not part of the request => this ancestor does not violate the request
				ignorableAncestorsCache.add(ancestor);
				return false; 
			}
			// check if ancestor is contained in this set
			if(collUris.contains(ancestor))
			{
				// -> ancestor is contained in the set of URIs matching the request 
				// => URI containing this ancestor matches the request
				nrOfMatchingAncestors[0]++;
				return false; 
			}
			// -> ancestor has an origin value that does not match the request => this ancestor violates the request 
			invalidAncestorsCache.add(ancestor);
			return true;
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
			if(selectOrigin != null)
			{
				try 
				{
					selectOrigin.close();
				} 
				catch(SQLException e) 
				{
					cat.logThrowableT(Severity.WARNING,	loc, "Could not close SQL statement: " + e.getMessage(), e);
				}
			}
			// do not close "conn" here!
		}
	}
}
