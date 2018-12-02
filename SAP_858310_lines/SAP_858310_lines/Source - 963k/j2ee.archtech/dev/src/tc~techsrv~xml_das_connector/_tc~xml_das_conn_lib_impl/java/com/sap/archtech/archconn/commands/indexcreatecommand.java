package com.sap.archtech.archconn.commands;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.Map.Entry;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.values.IndexPropDescription;

/**
 * The INDEX CREATE archiving command is 
 * used to create a new Property Index.
 *  
 * @author D025792
 * @version 1.0
 * 
 */
public class IndexcreateCommand extends AbstractArchCommand
{
  private boolean ipdadded = false;

  protected IndexcreateCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser, true);
    super.addParam("method", "INDEX");
    super.addParam("submethod", "CREATE");
  }

  public void addParam(IndexPropDescription ipd) throws IOException
  {
    if(ipd == null)
    {
      return;
    }
    if(ipdadded)
    {
      throw new IOException("IndexPropDescription parameter added twice");
    }

    super.addParam("index_name", ipd.getIndexname());
    super.addParam("multi_val_support", ipd.isMultiValSupported() ? "Y" : "N");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    StringBuilder descline = null;
    String propName = null;
    String[] secIndices = null;
    Set<Entry<String, String>> entrySet = ipd.getPropDesc().entrySet();
    for(Entry<String, String> entry : entrySet)
    {
      propName = (String)entry.getKey();
      // add property name and type
      descline = new StringBuilder(propName).append(':').append(entry.getValue());
      // add secondary indices if specified
      secIndices = ipd.getSecondaryIndices(propName);
      for(int i = 0, n = secIndices.length; i < n; i++)
      {
        if(i == 0)
        {
          descline.append(':');
        }
        else
        {
          descline.append('.');
        }
        descline.append(secIndices[i]);
      }
      bos.write(descline.toString().getBytes("UTF-8"));
      bos.write(0x0D);
      bos.write(0x0A);
    }
    ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
    bos.flush();
    bos.close();
    addObjectParam("STREAM", bin);
    ipdadded = true;
  }
}
