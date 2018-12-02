package com.sap.archtech.archconn.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.SequenceInputStream;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.IndexPropValues;

/**
 * 
 * The INDEXINSERT archiving command is used to
 * set index properties of a specified
 * resource.
 * 
 * 
 * @author d025792
 *
 */
public class IndexinsertCommand extends AbstractArchCommand
{
	private int indexCount;
	
  /**
   * @param session
   * @param archuser
   */
  protected IndexinsertCommand(AbstractArchSession archSessionRef, String collection, String archuser)
  {
    super(archSessionRef, archuser, true);
    super.addParam("method", "INDEX");
    super.addParam("submethod", "INSERT");
    if (collection != null)
    {
      super.addParam("archive_path", collection);
    }
    indexCount = 0;
  }

  public void addParam(IndexPropValues ipv) throws IOException
  {
  	indexCount++;
  	super.addParam("index_count", Integer.valueOf(indexCount).toString());
  	// serialize IndexPropValues
  	ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oser = new ObjectOutputStream(bos);
    oser.writeObject(ipv);
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    // Multiple IndexPropValues can be set
    Object obj = getObjectParam("STREAM");
    if (obj != null)
    {
      SequenceInputStream sin = new SequenceInputStream((InputStream)obj, bis);
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
    	addObjectParam("STREAM", bis);
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
}
