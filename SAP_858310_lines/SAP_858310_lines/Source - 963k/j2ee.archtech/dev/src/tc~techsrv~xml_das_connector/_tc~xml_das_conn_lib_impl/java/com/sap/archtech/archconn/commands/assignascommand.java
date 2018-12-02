package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.util.URI;

/**
 * 
 * ASSIGN_ARCHIVE_STORES command. Used for Admin
 * purposes. Not to be used in archiving applications.
 * 
 * @author d025792
 *
 */
public class AssignASCommand extends AbstractGetArchAdminDataCommandImpl
{

   protected AssignASCommand(AbstractArchSession archSessionRef, String archuser)
   {
      super(archSessionRef, archuser, false);
      super.addParam("Content-Length", "0");
      super.addParam("method", ArchCommandEnum.ASSIGNARCHIVESTORE.toString().toUpperCase());
   }

	/* (non-Javadoc)
	 * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
	 */
	public void addParam(URI uri)
	{
	  super.addParam("archive_path", uri.toString());
	}
}
