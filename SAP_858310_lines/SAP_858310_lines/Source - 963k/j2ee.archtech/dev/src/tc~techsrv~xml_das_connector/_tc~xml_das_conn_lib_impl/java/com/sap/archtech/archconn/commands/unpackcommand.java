package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.util.URI;

/**
 * The UNPACK command decompresses an AXML resource
 * into all the single resources. Reverse operation of PACK.
 * 
 * @author D025792
 */
public class UnpackCommand extends AbstractArchCommand
{

   /**
    * @param session
    * @param archuser
    */
   public UnpackCommand(AbstractArchSession archSessionRef, String collection, String archuser)
   {
      super(archSessionRef, archuser);
      super.addParam("Content-Length", "0");
      super.addParam("method", "UNPACK");
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
