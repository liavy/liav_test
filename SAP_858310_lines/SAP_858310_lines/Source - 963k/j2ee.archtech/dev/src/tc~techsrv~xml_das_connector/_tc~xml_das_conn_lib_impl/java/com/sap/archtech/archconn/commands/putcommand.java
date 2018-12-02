package com.sap.archtech.archconn.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.SequenceInputStream;
import java.sql.SQLException;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.ArchSessionAccessor;
import com.sap.archtech.archconn.LocalArchResponse;
import com.sap.archtech.archconn.QualifiedArchSession;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.util.DasResponse;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.IndexPropValues;

/**
 * The PUT archiving command is used
 * to archive a resource (an XML
 * document).
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class PutCommand extends AbstractArchCommand
{

  private boolean isResourceInputStreamAdded = false;
  private String collection;
  private int indexCount;

  protected PutCommand(AbstractArchSession archSessionRef, String collection, String archuser)
  {
    super(archSessionRef, archuser, true);
    this.collection = collection;
    super.addParam("method", "PUT");
    if(collection != null)
    {
    	super.addParam("archive_path", collection);
    }
    indexCount = 0;
  }

  /**
   * @see com.sap.archtech.archconn.ArchCommand#addParam(InputStream)
   */
  public void addParam(InputStream resourceInputStream) throws IOException
  {
    if(isResourceInputStreamAdded)
    {
    	throw new IOException("The resource input stream has already been added");
    }
    if(indexCount == 0)
    {
      // PUT request without index properties
      super.addParam("index_count", "0");
      addObjectParam("STREAM", resourceInputStream);
    }
    else
    {
      // PUT with index properties
    	Object stream = getObjectParam("STREAM");
      if(stream != null)
      {
      	// add resourceInputStream to the existing stream 
        SequenceInputStream sin = new SequenceInputStream((InputStream)stream, resourceInputStream);
        addObjectParam("STREAM", sin);
      }
      else
      {
      	// unexpected - the "STREAM" parameter should already contain a stream if indexCount > 0
      	throw new IllegalStateException("Unexpected program state in PUT method: \"STREAM\" parameter is not null but \"index_count\" is " + indexCount);
      }
    }
    isResourceInputStreamAdded = true;
  }

  /**
   * @see com.sap.archtech.archconn.ArchCommand#addParam(IndexPropValues)
   */
  public void addParam(IndexPropValues ipv) throws IOException
  {
    if(isResourceInputStreamAdded)
    {
    	throw new IOException("Index property values cannot be added after the resource input stream has been added");
    }
  	indexCount++;
  	super.addParam("index_count", Integer.valueOf(indexCount).toString());
  	// serialize IndexPropValues
  	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oser = new ObjectOutputStream(bos);
    oser.writeObject(ipv);
    ByteArrayInputStream ipvInputStream = new ByteArrayInputStream(bos.toByteArray());
    // Multiple IndexPropValues can be set
    Object stream = getObjectParam("STREAM");
    if(stream != null)
    {
    	// add the serialized IndexPropValues to the existing stream 
      SequenceInputStream sin = new SequenceInputStream((InputStream)stream, ipvInputStream);
      bos.flush();
      bos.close();
      oser.close();
      addObjectParam("STREAM", sin);
    }
    else
    {
    	bos.flush();
      bos.close();
      oser.close();
    	addObjectParam("STREAM", ipvInputStream);
    }
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri)
  {
    // add collection uri only if it is an unqualified archiving session
    // (and therefore not added before)
    if(getObjectParam("archive_path") == null)
    {
      super.addParam("archive_path", uri.toString());
    }
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
      if(getResponse().getStatusCode() == DasResponse.SC_OK)
      {
        // increment resource counter if no problems were reported from XMLDAS
        ((QualifiedArchSession)getArchSessionRef()).count_res(this);
      }
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
}
