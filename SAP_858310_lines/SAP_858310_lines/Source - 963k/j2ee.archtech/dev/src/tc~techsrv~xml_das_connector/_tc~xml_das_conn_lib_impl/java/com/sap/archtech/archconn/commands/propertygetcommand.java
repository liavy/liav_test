package com.sap.archtech.archconn.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.response.IGetArchivingPropertyValuesCommand;
import com.sap.archtech.archconn.response.IHttpStreamResponse;
import com.sap.archtech.archconn.util.DasResponse;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.ArchivingPropertyValues;

/**
 * The PROPERTYGET command is used to retrieve the name-value-pairs stored with an archived resource or collection.
 */
public class PropertyGetCommand extends AbstractArchCommand implements IGetArchivingPropertyValuesCommand
{
	private static final Pattern propertyGetSplitPattern = Pattern.compile("\\#");
	
  protected PropertyGetCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    super.addParam("Content-Length", "0");
    super.addParam("method", "PROPERTY");
    super.addParam("submethod", "GET");
    // Default lookup: get all properties stored for the resource/collection
    super.addParam("property_name", "*");
  }

  public void addParam(URI uri)
  {
    if(getStringParam("uri") == null)
    {
      super.addParam("uri", uri.toString());
    }
  }

  public ArchivingPropertyValues getArchivingPropertyValues() throws IOException
  {
  	IHttpStreamResponse httpResponse = (IHttpStreamResponse)getResponse();
    if(httpResponse.getStatusCode() != DasResponse.SC_OK)
    {
      throw new IOException("Status not OK, Archiving Property Values not returned");
    }

    BufferedReader br = new BufferedReader(httpResponse.getInputStreamReader());
    try
    {
      return parsePropertyGetBody(br);
    }
    finally
    {
      if(br != null)
      {
        br.close();
      }
    }
  }
  
  private ArchivingPropertyValues parsePropertyGetBody(BufferedReader br) throws IOException
  {
    String inputLine = null;
    String[] nameValuePairs = null;
    ArchivingPropertyValues archPropValues = new ArchivingPropertyValues();
    String[] nameAndValue = null;
    while((inputLine = br.readLine()) != null)
    {
      nameValuePairs = propertyGetSplitPattern.split(inputLine);
      for(String nameValuePair : nameValuePairs)
      {
        nameAndValue = nameValuePair.split("\\=");
        archPropValues.putProp(nameAndValue[0], nameAndValue[1]);
      }
    }
    return archPropValues;
  }
}
