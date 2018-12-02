package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.util.URI;

/**
 * The LEGALHOLDGET command is used to get the Legal Hold cases stored for a given archived resource/collection.
 */
public class LegalHoldGetCommand extends AbstractGetLegalHoldValuesCommandImpl
{
  protected LegalHoldGetCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser, false);
    super.addParam("Content-Length", "0");
    super.addParam("method", "LEGALHOLD");
    super.addParam("submethod", "GET");
  }
  
  public void addParam(URI uri)
  {
    if(getStringParam("uri") == null)
    {
      super.addParam("uri", uri.toString());
    }
  }
}
