package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.util.URI;

/**
 * 
 * The HEAD archiving command is
 * used to get (meta-)information about
 * resources or collections.
 * 
 * @author d025792
 *
 */
public class HeadCommand extends AbstractGetResourceDataCommandImpl
{

  protected HeadCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser, false);
    super.addParam("Content-Length", "0");
    super.addParam("method", "HEAD");
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri)
  {
    String colname;
    // the uri parameter in HEAD has
    // no slash at the end, even if it is a collection 
    colname = uri.toString();
    if ((colname.length() > 1) && (colname.endsWith("/")))
      colname = colname.substring(0, colname.length() - 1);
    super.addParam("uri", colname);
  }
}
