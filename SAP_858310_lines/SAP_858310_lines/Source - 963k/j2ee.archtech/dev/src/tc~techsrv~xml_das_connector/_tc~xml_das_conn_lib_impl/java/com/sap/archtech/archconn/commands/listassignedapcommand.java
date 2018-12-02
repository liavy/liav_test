package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;

/**
 * 
 * LIST_ASSIGNED_ARCHIVE_PATHS command. Used for Admin
 * purposes. Not to be used in archiving applications.
 * 
 * @author d037874
 *
 */
public class ListAssignedAPCommand extends AbstractGetArchAdminDataCommandImpl
{
   protected ListAssignedAPCommand(AbstractArchSession archSessionRef, String archuser)
   {
      super(archSessionRef, archuser, false);
      super.addParam("Content-Length", "0");
      super.addParam("method", ArchCommandEnum.LISTASSIGNEDARCHIVEPATHS.toString().toUpperCase());
   }
}
