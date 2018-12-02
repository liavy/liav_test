package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.util.URI;

/**
 * The MODIFY_PATH command is used
 * to create all collections in an archive path
 * that do not yet exist.
 * 
 * 
 * @author d025792
 */
public class MPathCommand extends AbstractArchCommand
{

  protected MPathCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    super.addParam("Content-Length", "0");
    super.addParam("method", "MODIFYPATH");
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri)
  {
    super.addParam("archive_path", uri.toString());
  }

}
