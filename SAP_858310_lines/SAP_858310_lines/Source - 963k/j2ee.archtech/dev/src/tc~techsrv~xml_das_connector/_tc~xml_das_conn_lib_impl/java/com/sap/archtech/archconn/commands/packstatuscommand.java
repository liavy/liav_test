package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.util.URI;

/**
 * The PACKSTATUS archiving command. Gives information
 * about the progress of a PACK or UNPACK operation.
 * 
 * @author D025792
 */
public class PackStatusCommand extends AbstractArchCommand
{

   /**
    * @param session
    * @param archuser
    */
   public PackStatusCommand(AbstractArchSession archSessionRef, String collection, String archuser)
   {
      super(archSessionRef, archuser);
      super.addParam("Content-Length", "0");
      super.addParam("method", "PACKSTATUS");
      if (collection != null)
         super.addParam("archive_path", collection);
   }
   
	/* (non-Javadoc)
	 * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
	 */
	public void addParam(URI uri)
	{
	  super.addParam("archive_path", uri.toString());
	}   
}
