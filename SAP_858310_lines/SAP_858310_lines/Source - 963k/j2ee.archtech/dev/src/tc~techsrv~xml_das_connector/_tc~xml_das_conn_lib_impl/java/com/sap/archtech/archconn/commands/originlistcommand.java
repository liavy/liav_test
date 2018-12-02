package com.sap.archtech.archconn.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.util.URI;

/**
 * List "origin" property values stored with a given collection including all its ancestor URIs.
 */
public class OriginListCommand extends AbstractArchCommand 
{
	protected OriginListCommand(AbstractArchSession archSessionRef, String archuser)
	{
		super(archSessionRef, archuser, true);
		super.addParam("method", ArchCommandEnum.ORIGINLIST.toString().toUpperCase());
	}
	
	public void addParam(Set<URI> uris) throws IOException
	{
		if(!(uris instanceof LinkedHashSet))
		{
			throw new IllegalArgumentException("This methods expects a set with predictable iteration order!");
		}
				
		ByteArrayOutputStream bos = null;
		try
		{
			bos = new ByteArrayOutputStream();
			for(URI uri : uris)
			{
				if(uri == null)
				{
					throw new IllegalArgumentException("URI must not be null!");
				}
				bos.write(uri.toString().getBytes("UTF-8"));
				bos.write(0x0D);
				bos.write(0x0A);
			}
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			bos.flush();
			addObjectParam("STREAM", bis);
		}
		finally
		{
			if(bos != null)
			{
				bos.close();
			}
		}
	}
}
