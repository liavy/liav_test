package com.sap.archtech.archconn.values;


/**
 * To be used for the <code>ORIGINSEARCH</code> command
 */
public class OriginSearchClause extends ColSearchClause
{
	public OriginSearchClause()
	{
		super();
	}
	
	/**
	 * Add a query string for the ORIGINSEARCH command.
	 * @param originName Origin name to search for e.g. "BUKRS"; pass "*" to search for all values
	 * @param comparisonOperator SQL comparison operator to be used with the <code>originValue</code> parameter; optional parameter
	 * @param originValue Origin value to search for, may contain the SQL placeholder character "%"; optional parameter
	 */
	public void addOriginQuery(String originName, String comparisonOperator, String originValue)
	{
		super.addQueryPart("origin", originName, comparisonOperator, originValue, '+');
	}

	/**
	 * Add a query string for the ORIGINSEARCH command. Used for range queries
	 * @param originName Origin name to search for e.g. "BUKRS"
	 * @param comparisonOperator_1 SQL comparison operator to be used with the <code>originValue_1</code> parameter
	 * @param originValue_1 Origin value to search for (range limit 1)
	 * @param comparisonOperator_2 SQL comparison operator to be used with the <code>originValue_2</code> parameter
	 * @param originValue_2 Origin value to search for (range limit 2)
	 */
	public void addOriginQuery(String originName, String comparisonOperator_1, String originValue_1, String comparisonOperator_2, String originValue_2)
	{
		super.addQueryPart("origin", originName, comparisonOperator_1, originValue_1, '+');
		super.addQueryPart("origin", originName, comparisonOperator_2, originValue_2, '&');
	}
}
