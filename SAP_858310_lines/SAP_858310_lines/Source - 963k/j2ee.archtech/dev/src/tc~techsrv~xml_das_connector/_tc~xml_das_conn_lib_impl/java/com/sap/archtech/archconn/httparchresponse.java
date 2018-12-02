package com.sap.archtech.archconn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.sap.archtech.archconn.response.AbstractArchResponseImpl;
import com.sap.archtech.archconn.response.IHttpStreamResponse;
import com.sap.archtech.archconn.util.DasResponse;

abstract class HttpArchResponse extends AbstractArchResponseImpl implements IHttpStreamResponse
{
  private final HashMap<? extends Object, ? extends Object> requestParams;

  protected HttpArchResponse(ArchCommand archCommand, HashMap<? extends Object, ? extends Object> requestParams)
  {
  	super(archCommand);
    this.requestParams = requestParams;
  }

  public HashMap<? extends Object, ? extends Object> getRequestParams()
  {
  	return requestParams;
  }
  
  public String getErrorMessage() throws IOException
  {
    if(getStatusCode() != DasResponse.SC_OK)
    {
      InputStreamReader isReader = getErrorStreamReader();
      if(isReader == null)
      {
        return "";
      }
      BufferedReader in = new BufferedReader(isReader);
      String inputLine;
      StringBuilder message = new StringBuilder();
      while((inputLine = in.readLine()) != null)
      {
        message.append(inputLine);
      }
      in.close();
      return message.toString();
    }
    return "";
  }

	public ArrayList<String> getStringResult() throws IOException
	{
		BufferedReader br = new BufferedReader(getInputStreamReader());
		String inputLine = null;
		ArrayList<String> strings = new ArrayList<String>();
		try
		{
			while((inputLine = br.readLine()) != null)
			{
				strings.add(inputLine);
			}
			return strings;
		}
		finally
		{
			if(br != null)
			{
				br.close();
			} 
		}
	}
}
