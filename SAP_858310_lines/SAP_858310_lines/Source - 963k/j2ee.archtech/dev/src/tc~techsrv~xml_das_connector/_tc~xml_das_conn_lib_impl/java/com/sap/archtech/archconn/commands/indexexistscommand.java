package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;

/**
 * The INDEX EXISTS archiving command is used to check the existence of a
 * Property Index.
 * 
 * @author D037874
 * @version 1.0
 *  
 */
public class IndexexistsCommand extends AbstractArchCommand
{

  protected IndexexistsCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    super.addParam("method", "INDEX");
    super.addParam("submethod", "EXISTS");
  }
}