package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.util.URI;

/**
 * 
 * The CHECK archiving command checks if
 * an XML resource is well-formed and validates it
 * against an XML schema.
 * 
 * @author d025792
 *
 */
public class CheckCommand extends AbstractArchCommand
{

  protected CheckCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    super.addParam("Content-Length", "0");
    super.addParam("method", "CHECK");
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri)
  {
    super.addParam("uri", uri.toString());
  }

}
