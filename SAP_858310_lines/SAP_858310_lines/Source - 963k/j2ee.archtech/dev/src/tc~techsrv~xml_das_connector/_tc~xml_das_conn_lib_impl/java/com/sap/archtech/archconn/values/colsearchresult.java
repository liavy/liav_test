package com.sap.archtech.archconn.values;

/**
 * Holds the result of the <code>COLSEARCH</code> command
 */
public class ColSearchResult 
{
  public static final int RESULT_TYPE_COMPLETE = 1;
	public static final int RESULT_TYPE_COLLNAMES = 2;
	public static final int RESULT_TYPE_COLPROPVALUES = 3;
	
	private final int resultType;
	private final String[] bodyAsStrings;
	private final ResourceData[] bodyAsResources;
	
	public ColSearchResult(int resultType, String[] bodyAsStrings)
	{
		this.resultType = resultType;
		if(resultType != RESULT_TYPE_COLLNAMES && resultType != RESULT_TYPE_COLPROPVALUES)
		{
			throw new IllegalArgumentException("Unexpected resultType: " + resultType);
		}
		if(bodyAsStrings != null)
		{
			this.bodyAsStrings = new String[bodyAsStrings.length];
			System.arraycopy(bodyAsStrings, 0, this.bodyAsStrings, 0, bodyAsStrings.length);
		}
		else
		{
			this.bodyAsStrings = null;
		}
		this.bodyAsResources = null;
	}
	
	public ColSearchResult(ResourceData[] bodyAsResources)
	{
		this.resultType = RESULT_TYPE_COMPLETE;
		this.bodyAsStrings = null;
		if(bodyAsResources != null)
		{
			this.bodyAsResources = new ResourceData[bodyAsResources.length];
			System.arraycopy(bodyAsResources, 0, this.bodyAsResources, 0, bodyAsResources.length);
		}
		else
		{
			this.bodyAsResources = null;
		}
	}
	
	public int getResultType()
	{
		return resultType;
	}
	
	public String[] getResultStrings()
	{
		if(bodyAsStrings != null)
		{
			String[] tmp = new String[bodyAsStrings.length];
			System.arraycopy(bodyAsStrings, 0, tmp, 0, bodyAsStrings.length);
			return tmp;
		}
		return null;
	}

	public ResourceData[] getResultResources()
	{
		if(bodyAsResources != null)
		{
			ResourceData[] tmp = new ResourceData[bodyAsResources.length];
			System.arraycopy(bodyAsResources, 0, tmp, 0, bodyAsResources.length);
			return tmp;
		}
		return null;
	}
}
