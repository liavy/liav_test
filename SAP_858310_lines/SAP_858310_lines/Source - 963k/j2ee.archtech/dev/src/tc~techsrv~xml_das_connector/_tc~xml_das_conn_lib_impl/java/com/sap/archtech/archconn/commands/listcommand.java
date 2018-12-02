package com.sap.archtech.archconn.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.response.IGetNextResourceDataBlockCommand;
import com.sap.archtech.archconn.response.IHttpStreamResponse;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.ResourceData;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * The LIST archiving command is
 * used to get meta data information about
 * resources and collections in the archive.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class ListCommand extends AbstractGetResourceDataCommandImpl implements IGetNextResourceDataBlockCommand
{
  private static final Location loc = Location.getLocation("com.sap.archtech.archconn.commands");
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");

  // initialized by "getNextResourceDataBlock()" 
  private BufferedReader bufferedReader = null;

  protected ListCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser, false);
    super.addParam("method", "LIST");
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri)
  {
    super.addParam("archive_path", uri.toString());
  }
  
  public ArrayList<ResourceData> getNextResourceDataBlock(int maxBlockLength) throws IOException
  {
  	IHttpStreamResponse httpResponse = (IHttpStreamResponse)getResponse();
    try
    {
      initBufferedReader(httpResponse);
      return parseResourceDataStream(maxBlockLength);
    }
    catch(IOException e)
    {
      closeResponseStream();
      throw e;
    }
  	
  }
  
  public void closeResponseStream()
  {
    if(bufferedReader != null)
    {
      try
      {
        bufferedReader.close();
      }
      catch(IOException e)
      {
        cat.logThrowableT(Severity.WARNING, loc, "Error when trying to close the response stream", e);
      }
      bufferedReader = null;
    }
  }
  
  public boolean isResponseStreamOpen()
  {
    try
    {
      return bufferedReader != null && bufferedReader.ready();
    }
    catch(IOException e)
    {
      // $JL-EXC$
      return false;
    }
  }
  
  private void initBufferedReader(final IHttpStreamResponse httpResponse) throws IOException
  {
    if(bufferedReader == null)
    {
      bufferedReader = new BufferedReader(httpResponse.getInputStreamReader());
    }
    else if(!bufferedReader.ready())
    {
      bufferedReader.close();
      bufferedReader = new BufferedReader(httpResponse.getInputStreamReader());
    }
  }

  private ArrayList<ResourceData> parseResourceDataStream(int maxBlockLength) throws IOException
  {
    ArrayList<ResourceData> resList = new ArrayList<ResourceData>();
    ResourceData resData = null;
    int counter = 0;
    try
    {
      // read only "maxBlockLength" lines
      boolean isEndOfStreamReached = false;
      while(counter < maxBlockLength)
      {
        String inputLine = bufferedReader.readLine();
        counter++;
        if(inputLine == null)
        {
          isEndOfStreamReached = true;
          break;
        }
        resData = getResourceDataFromLine(inputLine);
        resList.add(resData);
      }
      if(!isEndOfStreamReached)
      {
        // set position mark if end of stream has not been reached yet
        bufferedReader.mark(2);
        if(bufferedReader.read() != -1)
        {
          // there is more content in the stream -> reset position pointer back to mark
          bufferedReader.reset();
        }
        else
        {
          isEndOfStreamReached = true;
        }
      }
      if(isEndOfStreamReached)
      {
        closeResponseStream();
      }
      return resList;
    }
    catch(ParseException psex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Error parsing creation date", psex);
      throw new IOException("Error parsing creation date");
    }
  }
}
