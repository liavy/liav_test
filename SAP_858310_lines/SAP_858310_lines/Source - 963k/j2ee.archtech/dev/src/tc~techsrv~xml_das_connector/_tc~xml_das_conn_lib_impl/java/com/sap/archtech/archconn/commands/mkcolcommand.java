package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.util.URI;

/**
 * The MKCOL archiving command is used 
 * to create a new collection.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class MkcolCommand extends AbstractArchCommand
{

  protected MkcolCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    super.addParam("Content-Length", "0");
    super.addParam("method", "MKCOL");
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri)
  {
    String colname;
    URI tempuri = uri.resolve("../");
    URI coluri = tempuri.relativize(uri);
    // the parameter collection_name has no slash
    // at the end 
    colname = coluri.toString();
    if ((colname.length() > 1) && (colname.endsWith("/")))
      colname = colname.substring(0, colname.length() - 1);
    super.addParam("archive_path", tempuri.toString());
    super.addParam("collection_name", colname);
  }

}
