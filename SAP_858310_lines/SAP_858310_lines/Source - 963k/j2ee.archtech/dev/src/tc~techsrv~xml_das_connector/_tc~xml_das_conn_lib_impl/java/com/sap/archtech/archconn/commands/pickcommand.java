package com.sap.archtech.archconn.commands;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.ArrayList;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.ArchSessionAccessor;
import com.sap.archtech.archconn.LocalArchResponse;
import com.sap.archtech.archconn.QualifiedArchSession;
import com.sap.archtech.archconn.exceptions.MethodNotApplicableException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.response.IGetBodyCommand;
import com.sap.archtech.archconn.response.IGetIndexValuesListCommand;
import com.sap.archtech.archconn.response.IGetResKeyCommand;
import com.sap.archtech.archconn.response.IHttpStreamResponse;
import com.sap.archtech.archconn.util.DASResponseInputStream;
import com.sap.archtech.archconn.util.DasResponse;
import com.sap.archtech.archconn.values.IndexPropValues;
import com.sap.archtech.archconn.values.TechResKey;

/**
 * The PICK archiving command is used to provide an arbitrary
 * selected resource out of a specified collection for
 * deletion in the local database. 
 * 
 * @author d025792
 *
 */
public class PickCommand extends AbstractArchCommand implements IGetBodyCommand, IGetResKeyCommand, IGetIndexValuesListCommand
{
  private final String collection;
  
  /**
   * @param session
   */
  public PickCommand(AbstractArchSession archSessionRef, String collection, String archuser)
  {
    super(archSessionRef, archuser);
    this.collection = collection;
    super.addParam("method", "PICK");
    if (collection != null)
      super.addParam("archive_path", collection);
  }

  public void addParam(TechResKey reskey) throws MethodNotApplicableException
  {
    super.addParam("confirmdelkey", reskey.toString());
  }
  
  public void execute() throws IOException
  {
    try
    {
      // check if session cancellation has been requested
      if(ArchSessionAccessor.isMarkedForCancellation(path2URI(collection)))
      {
      	LocalArchResponse localResponse 
    			= new LocalArchResponse(this, 
    				DasResponse.SC_CANCEL_REQUESTED,
    				"Session is marked for cancellation as requested by the client. The archiving command cannot be executed.",
    				"",
    				"Session cancellation requested");
        setResponse(localResponse);
        return;
      }
      super.execute();
      // increment resource counter
      ((QualifiedArchSession)getArchSessionRef()).count_res(this);
    }
    catch(SQLException e)
    {
      throw new IOException("Could not load Archiving Session data due to database-related problem: " + e.getMessage());
    }
    catch(SessionHandlingException e)
    {
      throw new IOException("Could not load Archiving Session data: " + e.getMessage());
    }
  }
  
  private String path2URI(String path)
  {
    if(path.endsWith("/"))
    {
      // cut last "/"
      return path.substring(0, path.length()-1);
    }
    return path;
  }
  
  public InputStream getBody() throws IOException
  {
  	IHttpStreamResponse httpResponse = (IHttpStreamResponse)getResponse();
    if(!httpResponse.getHeaderField("returntype").equals("xmldoc"))
    {
      throw new IllegalArgumentException("Index specified in PICK, use getIndexValuesList() to read index values");
    }
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
  
  public TechResKey getResKey()
  {
  	IHttpStreamResponse httpResponse = (IHttpStreamResponse)getResponse();
    String key = httpResponse.getHeaderField("reskey");
    if(key != null)
    {
      return new TechResKey(key);
    }
    return null;
  }
  
  public ArrayList<IndexPropValues> getIndexValuesList() throws IOException
  {
  	IHttpStreamResponse httpResponse = (IHttpStreamResponse)getResponse();
    if(!httpResponse.getHeaderField("returntype").equals("propvalues"))
    {
      throw new IllegalArgumentException("No index specified in PICK, complete resource is returned");
    }
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
