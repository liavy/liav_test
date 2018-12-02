package com.sap.archtech.archconn.commands;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.response.IGetIndexValuesListCommand;
import com.sap.archtech.archconn.response.IHttpStreamResponse;
import com.sap.archtech.archconn.util.DasResponse;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.IndexPropValues;

/**
 * The INDEXGET archiving command is used to get
 * the property index values for a resource.
 * 
 * 
 * @author d025792
 *
 */
public class IndexgetCommand extends AbstractArchCommand implements IGetIndexValuesListCommand
{

  protected IndexgetCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    super.addParam("Content-Length", "0");
    super.addParam("method", "INDEX");
    super.addParam("submethod", "GET");
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri)
  {
    super.addParam("uri", uri.toString());
  }

  public ArrayList<IndexPropValues> getIndexValuesList() throws IOException
  {
  	IHttpStreamResponse httpResponse = (IHttpStreamResponse)getResponse();
    if(httpResponse.getStatusCode() != DasResponse.SC_OK)
    {
      throw new IOException("Status not OK, Index Property Values not returned");
    }

    ObjectInputStream oist = httpResponse.getObjectInputStream();
    try
    {
      return (ArrayList<IndexPropValues>)oist.readObject();
    }
    catch(ClassNotFoundException ccex)
    {
      throw new IOException(ccex.getMessage());
    }
    finally
    {
      oist.close();
    }
  }
}
