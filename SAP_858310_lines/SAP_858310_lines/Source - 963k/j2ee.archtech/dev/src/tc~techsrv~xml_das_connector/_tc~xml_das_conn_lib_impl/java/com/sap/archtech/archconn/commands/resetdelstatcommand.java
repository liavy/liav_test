package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.util.URI;

/**
 * 
 * The _RESET_DELSTAT comman is only used internally.
 * It sets all deletion status flags of one collection set to
 *  'P' (selected for deletion) back to 'N' (not deleted). 
 * It is started before a deletion run starts. It solves
 * the problem that some archived records will not be 
 * presented to the archive deletion program if an error
 * in the deletion of local DB entires appeared. 
 * 
 * @author D025792
 *
 */
public class ResetDelstatCommand extends AbstractArchCommand
{

   /**
    * @param session
    * @param archuser
    */
   public ResetDelstatCommand(AbstractArchSession archSessionRef, String archuser)
   {
      super(archSessionRef, archuser);
      super.addParam("Content-Length", "0");
      super.addParam("method", "_RESET_DELSTAT");
   }

   /* (non-Javadoc)
    * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
    */
   public void addParam(URI uri)
   {
      super.addParam("archive_path", uri.toString());
   }

}
