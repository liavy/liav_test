package com.sap.archtech.archconn.commands;

import com.sap.archtech.archconn.AbstractArchSession;

/**
 * GET_WEBDAV_STORE_META_DATA command. Used for Admin
 * purposes. Not to be used in archiving applications.
 */
public class GetWebDavASDataCommand extends AbstractArchCommand
{
	protected GetWebDavASDataCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    super.addParam("Content-Length", "0");
    super.addParam("method", ArchCommandEnum.GET_WEBDAV_STORE_META_DATA.toString().toUpperCase());
  }
  
	public void addParam(String headerField, String value)
	{
		if(!"archive_store".equals(headerField))
		{
			throw new IllegalArgumentException("Unsupported header field for GET_WEBDAV_STORE_META_DATA command: " + headerField);
		}
		super.addParam(headerField, value);
	}

}
