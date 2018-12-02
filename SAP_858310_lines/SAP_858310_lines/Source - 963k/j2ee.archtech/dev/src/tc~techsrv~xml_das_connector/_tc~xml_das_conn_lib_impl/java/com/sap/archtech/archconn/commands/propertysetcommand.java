package com.sap.archtech.archconn.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.Map.Entry;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.exceptions.MethodNotApplicableException;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.ArchivingPropertyValues;

/**
 * The PROPERTYSET command is used to store arbitrary name-value-pairs with an archived resource or collection.
 */
public class PropertySetCommand extends AbstractArchCommand
{
  protected PropertySetCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser, true);
    super.addParam("method", "PROPERTY");
    super.addParam("submethod", "SET");
  }
  
  public void addParam(URI uri)
  {
    if(getObjectParam("uri") == null)
    {
      super.addParam("uri", uri.toString());
    }
  }
  
  public void addParam(String headerField, boolean value) throws MethodNotApplicableException
  {
    if("att_col".equals(headerField))
    {
      if(getObjectParam("att_col") == null)
      {
        super.addParam(headerField, value == true ? "Y" : "N");
      }
    }
    else
    {
      throw new MethodNotApplicableException("\"PropertySetCommand\" does not support the boolean parameter \"" + headerField + "\"");
    }
  }
  
  public void addParam(ArchivingPropertyValues archPropertyValues) throws IOException
  {
    // serialize archPropertyValues
    StringBuilder archPropertiesBuf = new StringBuilder("");
    Set<Entry<String, String>> entrySet = archPropertyValues.getAllProperties().entrySet();
    for(Entry<String, String> entry : entrySet)
    {
      archPropertiesBuf.append(entry.getKey()).append('=').append(entry.getValue()).append('#');
    }
    // cut trailing '#'
    int bufLength = archPropertiesBuf.length();
    if(bufLength > 0)
    {
      archPropertiesBuf = archPropertiesBuf.deleteCharAt(bufLength-1);
    }
    String archProperties = archPropertiesBuf.toString();
    ByteArrayOutputStream bos = null;
    try
    {
      bos = new ByteArrayOutputStream();
      bos.write(archProperties.getBytes("UTF-8"));
      bos.write(0x0D);
      bos.write(0x0A);
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
  
  public void addParam(String headerField, String value)
  {
    if("legal_hold".equals(headerField))
    {
      throw new UnsupportedOperationException("Use the \"LEGAL_HOLD\" command to define or change Legal Hold properties!");
    }
    super.addParam(headerField, value);
  }
  
  public void addParam(String param)
  {
    if(param.startsWith("legal_hold"))
    {
      throw new UnsupportedOperationException("Use the \"LEGAL_HOLD\" command to define or change Legal Hold properties!");
    }
    super.addParam(param);
  }
}
