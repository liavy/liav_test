package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;

/**
 *
 * The INDEX DROP archiving command is
 * used to delete a property index.
 * 
 *  @author d025792
 *
 */
public class IndexdropCommand extends AbstractArchCommand
{

  protected IndexdropCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    super.addParam("Content-Length", "0");
    super.addParam("method", "INDEX");
    super.addParam("submethod", "DROP");
  }

}
