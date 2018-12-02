package com.sap.archtech.archconn.values;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * To be used for the <code>COLSEARCH</code> command
 */
public class ColSearchClause 
{
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
		
	private ArrayList<String> queryParts;
	
	public ColSearchClause()
	{
		queryParts = new ArrayList<String>(3);
	}
	
	/**
	 * Add a query string for the COLSEARCH command using the '&' character to connect query parts
	 * @param colPropName Name of the collection property to search for e.g. "origin"
	 * @param colPropValue Value of the collection property to search for e.g. "BUKRS"; pass "*" to search for all values
	 * @param comparisonOperator SQL comparison operator to be used with the <code>colName</code> parameter; optional parameter
	 * @param colName Name of the collection to search for, may contain the SQL placeholder character "%"; optional parameter
	 */
	public void addQueryPart(String colPropName, String colPropValue, String comparisonOperator, String colName)
	{
		addQueryPart(colPropName, colPropValue, comparisonOperator, colName, '&');
	}

	/**
	 * Add a query string for the COLSEARCH command
	 * @param colPropName Name of the collection property to search for e.g. "origin"
	 * @param colPropValue Value of the collection property to search for e.g. "BUKRS"; pass "*" to search for all values
	 * @param comparisonOperator SQL comparison operator to be used with the <code>colName</code> parameter; optional parameter
	 * @param colName Name of the collection to search for, may contain the SQL placeholder character "%"; optional parameter
	 * @param queryConnector Character to be used to connect the query parts (default is "&")
	 */
	public void addQueryPart(String colPropName, String colPropValue, String comparisonOperator, String colName, char queryConnector)
	{
		// parameter checks
		if(colPropName == null || "".equals(colPropName.trim()))
		{
			throw new IllegalArgumentException("Missing colPropName parameter!");
		}
		if(colPropValue == null || "".equals(colPropValue.trim()))
		{
			throw new IllegalArgumentException("Missing colPropValue parameter!");
		}
		if(comparisonOperator != null && !allowedOperators.contains(comparisonOperator))
		{
			throw new IllegalArgumentException("Value of comparisonParameter is not a valid SQL comparison operator: " + comparisonOperator);
		}
		boolean hasNoOperator = comparisonOperator == null || "".equals(comparisonOperator.trim());
		boolean hasNoColName = colName == null || "".equals(colName.trim());
		if(hasNoOperator ^ hasNoColName)
		{
			throw new IllegalArgumentException("Cannot create a query with only one of the parameters comparisonOperator or colName present!");
		}
		if(queryConnector != '&' && queryConnector != '+')
		{
			throw new IllegalArgumentException("Query connector character not supported: " + queryConnector);
		}
		
		// create query part
		StringBuilder tmp = isFirstQueryPart() ? 
				new StringBuilder(colPropName).append('.').append(colPropValue) 
			: new StringBuilder(' ').append(queryConnector).append(' ').append(colPropName).append('.').append(colPropValue);
		if(colPropValue.equals("*"))
		{
			// ignore comparisonOperator and colName
		}
		else
		{
			if(comparisonOperator != null)
			{
				tmp.append(' ').append(comparisonOperator).append(' ').append(colName);
			}
			else
			{
				tmp.append(" LIKE %");
			}
		}
		// add to list of query parts
		queryParts.add(tmp.toString());
	}
	
	private boolean isFirstQueryPart()
	{
		return queryParts.isEmpty();
	}
	
	public String toString()
	{
		StringBuilder tmp = new StringBuilder("");
		for(String queryPart : queryParts)
		{
			tmp.append(queryPart);
		}
		return tmp.toString();
	}
}
