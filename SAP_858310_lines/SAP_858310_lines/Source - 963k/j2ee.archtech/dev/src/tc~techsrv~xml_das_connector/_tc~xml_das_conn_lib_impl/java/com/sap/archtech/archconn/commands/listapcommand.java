package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;

/**
 * 
 * LIST_ARCHIVE_PATHS command. Used for Admin
 * purposes. Not to be used in archiving applications.
 * 
 * @author d025792
 *
 */
public class ListAPCommand extends AbstractGetArchAdminDataCommandImpl
{
   protected ListAPCommand(AbstractArchSession archSessionRef, String archuser)
   {
      super(archSessionRef, archuser, false);
      super.addParam("Content-Length", "0");
      super.addParam("method", ArchCommandEnum.LISTARCHIVEPATHS.toString().toUpperCase());
   }
}
