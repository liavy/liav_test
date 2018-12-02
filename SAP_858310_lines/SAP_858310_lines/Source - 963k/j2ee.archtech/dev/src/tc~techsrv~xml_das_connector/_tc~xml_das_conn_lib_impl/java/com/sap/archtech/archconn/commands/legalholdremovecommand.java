package com.sap.archtech.archconn.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.exceptions.MethodNotApplicableException;
import com.sap.archtech.archconn.util.URI;

/**
 * The LEGALHOLDADD command is used to add Legal Hold cases to archived resources/collections. 
 */
public class LegalHoldRemoveCommand extends AbstractGetLegalHoldValuesCommandImpl
{
  private static final int MAX_ILM_CASE_LENGTH = 64;
  
  protected LegalHoldRemoveCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser, true);
    super.addParam("method", "LEGALHOLD");
    super.addParam("submethod", "REMOVE");
  }
  
  public void addParam(String headerField, String value)
  {
    if("ilm_case".equals(headerField))
    {
      if(value.length() <= MAX_ILM_CASE_LENGTH)
      {
        super.addParam(headerField, value);
      }
      else
      {
        throw new IllegalArgumentException("Length of \"ilm_case\" parameter must not exceed " + MAX_ILM_CASE_LENGTH + " characters");
      }
    }
    else
    {
      super.addParam(headerField, value);
    }
  }
  
  public void addParam(String param)
  {
    if(param.startsWith("ilm_case"))
    {
      String[] nameAndValue = param.split("\\:");
      addParam(nameAndValue[0], nameAndValue[1]);
    }
    else
    {
      super.addParam(param);
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
      throw new MethodNotApplicableException("\"LEGALHOLDREMOVE\" command does not support the boolean parameter \"" + headerField + "\"");
    }
  }
  
  public void addParam(Set<URI> uris) throws IOException
  {
    ByteArrayOutputStream bos = null;
    try
    {
      bos = new ByteArrayOutputStream();
      for(URI uri : uris)
      {
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
