package com.sap.archtech.archconn.response;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchResponse;
import com.sap.archtech.archconn.exceptions.UnsupportedCommandException;
import com.sap.archtech.archconn.values.ArchivingPropertyValues;
import com.sap.archtech.archconn.values.ColSearchResult;
import com.sap.archtech.archconn.values.IndexPropDescription;
import com.sap.archtech.archconn.values.IndexPropValues;
import com.sap.archtech.archconn.values.LegalHoldValues;
import com.sap.archtech.archconn.values.ResourceData;
import com.sap.archtech.archconn.values.SessionInfo;
import com.sap.archtech.archconn.values.TechResKey;

public abstract class AbstractArchResponseImpl implements ArchResponse 
{
	private final ArchCommand archCommand;
	
	protected AbstractArchResponseImpl(ArchCommand archCommand)
	{
		this.archCommand = archCommand;
	}
	
	public ArrayList<? extends Serializable> getArchAdminData()	throws IOException, UnsupportedCommandException 
	{
		if(archCommand instanceof IGetArchAdminDataCommand)
		{
			return ((IGetArchAdminDataCommand)archCommand).getArchAdminData();
		}
		throw new UnsupportedCommandException("Method \"getArchAdminData()\" is not supported by " + archCommand.getClass().getName());
	}

	public ArchivingPropertyValues getArchivingPropertyValues() throws IOException, UnsupportedCommandException 
	{
		if(archCommand instanceof IGetArchivingPropertyValuesCommand)
		{
			return ((IGetArchivingPropertyValuesCommand)archCommand).getArchivingPropertyValues();
		}
		throw new UnsupportedCommandException("Method \"getArchivingPropertyValues()\" is not supported by " + archCommand.getClass().getName());
	}

	public InputStream getBody() throws IOException, UnsupportedCommandException 
	{
		if(archCommand instanceof IGetBodyCommand)
		{
			return ((IGetBodyCommand)archCommand).getBody();
		}
		throw new UnsupportedCommandException("Method \"getBody()\" is not supported by " + archCommand.getClass().getName());
	}

	public ColSearchResult getColSearchResult() throws IOException,	UnsupportedCommandException 
	{
		if(archCommand instanceof IGetColSearchResultCommand)
		{
			return ((IGetColSearchResultCommand)archCommand).getColSearchResult();
		}
		throw new UnsupportedCommandException("Method \"getColSearchResult()\" is not supported by " + archCommand.getClass().getName());
	}

	public IndexPropDescription getIndexProps() throws IOException,	UnsupportedCommandException 
	{
		if(archCommand instanceof IGetIndexPropDescriptionCommand)
		{
			return ((IGetIndexPropDescriptionCommand)archCommand).getIndexProps();
		}
		throw new UnsupportedCommandException("Method \"getIndexProps()\" is not supported by " + archCommand.getClass().getName());
	}

	public IndexPropValues getIndexValues() throws IOException,	UnsupportedCommandException 
	{
		if(archCommand instanceof IGetIndexValuesListCommand)
		{
			ArrayList<IndexPropValues> ipvList = getIndexValuesList();
	  	if(!ipvList.isEmpty())
	  	{
	  		return ipvList.get(0);
	  	}
	  	return null;
		}
		throw new UnsupportedCommandException("Method \"getIndexValues()\" is not supported by " + archCommand.getClass().getName());
	}

	public ArrayList<IndexPropValues> getIndexValuesList() throws IOException, UnsupportedCommandException 
	{
		if(archCommand instanceof IGetIndexValuesListCommand)
		{
			return ((IGetIndexValuesListCommand)archCommand).getIndexValuesList();
		}
		throw new UnsupportedCommandException("Method \"getIndexValuesList()\" is not supported by " + archCommand.getClass().getName());
	}

	public LegalHoldValues getLegalHoldValues() throws IOException,	UnsupportedCommandException 
	{
		if(archCommand instanceof IGetLegalHoldValuesCommand)
		{
			return ((IGetLegalHoldValuesCommand)archCommand).getLegalHoldValues();
		}
		throw new UnsupportedCommandException("Method \"getLegalHoldValues()\" is not supported by " + archCommand.getClass().getName());
	}

	public TechResKey getResKey() throws UnsupportedCommandException 
	{
		if(archCommand instanceof IGetResKeyCommand)
		{
			return ((IGetResKeyCommand)archCommand).getResKey();
		}
		throw new UnsupportedCommandException("Method \"getResKey()\" is not supported by " + archCommand.getClass().getName());
	}

	public ArrayList<ResourceData> getResourceData() throws IOException, UnsupportedCommandException 
	{
		if(archCommand instanceof IGetResourceDataCommand)
		{
			return ((IGetResourceDataCommand)archCommand).getResourceData();
		}
		throw new UnsupportedCommandException("Method \"getResourceData()\" is not supported by " + archCommand.getClass().getName());
	}

	public SessionInfo getSessionInfo() throws UnsupportedCommandException 
	{
		if(archCommand instanceof IGetSessionInfoCommand)
		{
			return ((IGetSessionInfoCommand)archCommand).getSessionInfo();
		}
		throw new UnsupportedCommandException("Method \"getSessionInfo()\" is not supported by " + archCommand.getClass().getName());
	}
	
	protected final ArchCommand getArchCommand()
	{
		return archCommand;
	}
}
