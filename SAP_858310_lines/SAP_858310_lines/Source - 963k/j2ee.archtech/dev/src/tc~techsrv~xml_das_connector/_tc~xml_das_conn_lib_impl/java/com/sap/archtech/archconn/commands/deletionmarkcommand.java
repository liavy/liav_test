package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;

/**
 * The DELETIONMARK archiving command flags
 * all resources of a collection as deleted.
 * 
 * @author D025792
 */
public class DeletionMarkCommand extends AbstractArchCommand
{

   protected DeletionMarkCommand(AbstractArchSession archSessionRef, String collection, String archuser)
   {
      super(archSessionRef, archuser);
      super.addParam("Content-Length", "0");
      super.addParam("method", "DELETIONMARK");
      if (collection != null)
      {
        super.addParam("archive_path", collection);
      }
   }
}
