package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.util.URI;

/**
 * The PACK archiving command compresses all resources
 * of one collection into one AXML resource.
 * 
 * @author D025792
 */
public class PackCommand extends AbstractArchCommand
{

   /**
    * @param session
    * @param archuser
    */
   public PackCommand(AbstractArchSession archSessionRef, String collection, String archuser)
   {
      super(archSessionRef, archuser);
      super.addParam("Content-Length", "0");
      super.addParam("method", "PACK");
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
