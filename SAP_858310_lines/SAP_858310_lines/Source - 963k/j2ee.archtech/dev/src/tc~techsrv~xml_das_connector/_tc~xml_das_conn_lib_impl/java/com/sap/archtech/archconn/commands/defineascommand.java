package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;

/**
 * 
 * DEFINE_ARCHIVE_STORES command. Used for Admin
 * purposes. Not to be used in archiving applications.
 * 
 * @author d025792
 *
 */
public class DefineASCommand extends AbstractGetArchAdminDataCommandImpl
{
   protected DefineASCommand(AbstractArchSession archSessionRef, String archuser)
   {
      super(archSessionRef, archuser, false);
      super.addParam("Content-Length", "0");
      super.addParam("method", ArchCommandEnum.DEFINEARCHIVESTORE.toString().toUpperCase());
   }
}
