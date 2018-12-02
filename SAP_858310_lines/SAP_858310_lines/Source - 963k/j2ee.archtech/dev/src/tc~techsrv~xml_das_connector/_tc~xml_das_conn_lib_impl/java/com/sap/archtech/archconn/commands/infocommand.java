package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.util.URI;

/**
 * 
 * The INFO archiving command is used to retrieve information
 * about the version of the archiving components in use.
 * Retrieves physical path of resources.
 * 
 * @author d025792
 *
 */
public class InfoCommand extends AbstractArchCommand
{

  protected InfoCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    super.addParam("Content-Length", "0");
    super.addParam("method", "INFO");
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri)
  {
    super.addParam("archive_path", uri.toString());
  }

}
