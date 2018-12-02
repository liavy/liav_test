package com.sap.archtech.archconn.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.response.IGetIndexPropDescriptionCommand;
import com.sap.archtech.archconn.response.IHttpStreamResponse;
import com.sap.archtech.archconn.values.IndexPropDescription;

/**
 * The INDEX DESCRIBE archiving command is
 * used to get information about an
 * existing Property Index.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class IndexdescribeCommand extends AbstractArchCommand implements IGetIndexPropDescriptionCommand
{
	
	protected IndexdescribeCommand(AbstractArchSession archSessionRef, String archuser)
	{
		super(archSessionRef, archuser);
    super.addParam("method", "INDEX");
		super.addParam("submethod", "DESCRIBE");
	}
	
	public IndexPropDescription getIndexProps() throws IOException
  {
		IHttpStreamResponse httpResponse = (IHttpStreamResponse)getResponse();
    IndexPropDescription ipd = new IndexPropDescription(httpResponse.getHeaderField("index_name"), "Y".equals(httpResponse.getHeaderField("multi_val_support")) ? true : false);
    BufferedReader br = null;
    try
    {
      br = new BufferedReader(httpResponse.getBufferedInputStreamReader());
      String inputLine;
      String propname;
      String proptype;
      while((inputLine = br.readLine()) != null)
      {
        StringTokenizer tok = new StringTokenizer(inputLine, ":");
        propname = tok.nextToken();
        proptype = tok.nextToken();
        ipd.putDesc(propname, proptype);
      }
      return ipd;
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
