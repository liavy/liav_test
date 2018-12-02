package com.sap.archtech.archconn.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.response.IGetColSearchResultCommand;
import com.sap.archtech.archconn.response.IHttpStreamResponse;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.ColSearchClause;
import com.sap.archtech.archconn.values.ColSearchResult;
import com.sap.archtech.archconn.values.OriginSearchClause;
import com.sap.archtech.archconn.values.ResourceData;

/**
 * Catalog query based on properties stored with the collection.
 */
public class OriginSearchCommand extends AbstractGetResourceDataCommandImpl implements IGetColSearchResultCommand
{
	private boolean clauseAdded = false;
	
	protected OriginSearchCommand(AbstractArchSession archSessionRef, String archuser)
	{
		super(archSessionRef, archuser, true);
		super.addParam("method", ArchCommandEnum.ORIGINSEARCH.toString().toUpperCase());
	}
	
	public void addParam(String headerField, String value)
	{
		if(!"search_root".equals(headerField))
		{
			throw new IllegalArgumentException("Unsupported header field for ORIGINSEARCH command: " + headerField);
		}
		super.addParam(headerField, value);
	}
	
	public void addParam(URI uri)
	{
		super.addParam("search_root", uri.toString().toLowerCase());
	}
	
	public void addParam(ColSearchClause originSearchClause) throws IOException
	{
		if(!(originSearchClause instanceof OriginSearchClause))
		{
			throw new IllegalArgumentException("Only OriginSearchClause supported here");
		}
		if(clauseAdded)
		{
			throw new IllegalArgumentException("OriginSearchClause parameter added twice");
		} 
		clauseAdded = true;
		
		String queryString = originSearchClause.toString();
		if(queryString == null || "".equals(queryString.trim()))
		{
			throw new IllegalArgumentException("Missing query string in search clause");
		}
		ByteArrayOutputStream bos = null;
		try
		{
			bos = new ByteArrayOutputStream();
			bos.write(queryString.getBytes("UTF-8"));
			bos.write(0x0D);
			bos.write(0x0A);
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			bos.flush();
			addObjectParam("STREAM", bis);
		}
		finally
		{
			if(bos != null)
			{
				bos.close();
			}
		}
	}

	public ColSearchResult getColSearchResult() throws IOException
  {
		IHttpStreamResponse httpResponse = (IHttpStreamResponse)getResponse();
    int resultType = Integer.parseInt(httpResponse.getHeaderField("result_type"));
    if(resultType == ColSearchResult.RESULT_TYPE_COMPLETE)
    {
      // get resource data from response stream
      ArrayList<ResourceData> resources = getResourceData();
      return new ColSearchResult(resources.toArray(new ResourceData[resources.size()]));
    }
    else
    {
    	// other result types are only supported for COLSEARCH
      ArrayList<String> strings = httpResponse.getStringResult();
      return new ColSearchResult(resultType, strings.toArray(new String[strings.size()]));
    }
  }
}
