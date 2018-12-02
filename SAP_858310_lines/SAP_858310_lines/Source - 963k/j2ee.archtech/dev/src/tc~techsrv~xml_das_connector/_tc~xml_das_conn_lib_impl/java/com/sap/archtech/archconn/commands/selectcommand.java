package com.sap.archtech.archconn.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.exceptions.MethodNotApplicableException;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.SelectClause;

/**
 * The SELECT archiving command is 
 * used to search for archived
 * resources based on properties and
 * meta data information.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class SelectCommand extends AbstractGetResourceDataCommandImpl
{
  private boolean clauseadded = false;

  protected SelectCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser, true);
    super.addParam("method", "SELECT");
  }

  /**
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.values.SelectClause)
   */
  public void addParam(SelectClause slc) throws IOException, MethodNotApplicableException
  {
    if (clauseadded)
      throw new IOException("SelectClause parameter added twice");

    clauseadded = true;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oser = new ObjectOutputStream(bos);

    oser.writeObject(slc);

    bos.flush();
    // convert to InputStream
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    bos.close();
    oser.close();
    addObjectParam("STREAM", bis);
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri)
  {
    super.addParam("archive_path", uri.toString());
  }
}
