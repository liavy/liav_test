package com.sap.archtech.archconn.commands;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.response.IGetBodyCommand;
import com.sap.archtech.archconn.response.IHttpStreamResponse;
import com.sap.archtech.archconn.util.DASResponseInputStream;
import com.sap.archtech.archconn.util.URI;

/**
 * The GET archiving command is used to
 * fetch archived resources.
 *  
 * @author D025792
 * @version 1.0
 * 
 */

public class GetCommand extends AbstractArchCommand implements IGetBodyCommand
{

  protected GetCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    super.addParam("method", "GET");
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri)
  {
    super.addParam("uri", uri.toString());
  }
  
  public InputStream getBody() throws IOException
  {
  	IHttpStreamResponse httpResponse = (IHttpStreamResponse)getResponse();
  	BufferedInputStream bufferedIS = httpResponse.getBufferedInputStream();
    int startPosOfStatusInfo = -1;
    try
    {
      startPosOfStatusInfo = Integer.valueOf(httpResponse.getHeaderField("reslength")).intValue();
    }
    catch(NumberFormatException e)
    {
      // $JL-EXC$
    }
    return new DASResponseInputStream(bufferedIS, startPosOfStatusInfo);  	
  }
}
