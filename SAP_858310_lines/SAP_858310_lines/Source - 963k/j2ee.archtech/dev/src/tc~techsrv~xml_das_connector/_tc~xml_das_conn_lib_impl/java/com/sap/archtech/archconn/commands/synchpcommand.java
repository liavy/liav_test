package com.sap.archtech.archconn.commands;

import java.io.IOException;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.ArchSessionAccessor;
import com.sap.archtech.archconn.util.DasResponse;
import com.sap.archtech.archconn.util.URI;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;

/**
 * The _SYNC_HOME_PATH command is
 * used only internally for ArchSetConfigurator.createSetHome().
 * @see com.sap.archtech.archconn.util.ArchSetConfigurator#createSetHome(java.lang.String)
 * 
 * @author d025792
 */
public class SyncHPCommand extends AbstractArchCommand
{
  private static final Location loc = Location.getLocation("com.sap.archtech.archconn.commands.SyncHPCommand");
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");
  
  private String homePathURI;
  private String action;
  
  protected SyncHPCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    
    super.addParam("method", "_SYNC_HOME_PATH");
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri)
  {
    homePathURI = uri.toString();
    super.addParam("home_path", homePathURI);
  }

  public void addParam(String hfield, String value)
  {
    if("home_path".equals(hfield))
    {
      homePathURI = value;
    }
    if("action".equals(hfield))
    {
      action = value;
    }
    super.addParam(hfield, value);
  }
  
  public void execute() throws IOException
  {
    super.execute();
    int statusCode = getResponse().getStatusCode();
    if(statusCode == DasResponse.SC_OK || statusCode == DasResponse.SC_HOME_COLLECTION_EXISTS)
    {
      try
      {
        if("I".equals(action))
        {
          boolean isHomePathCollExistent = false;
          if(statusCode == DasResponse.SC_HOME_COLLECTION_EXISTS)
          {
            // If Connector and XMLDAS reside on different servers, the Home Path Collection might not be part of
            // the hierarchy yet, although the sync step has been performed before.
            isHomePathCollExistent = ArchSessionAccessor.isHomePathCollectionInHierarchy(this, path2URI(homePathURI));
          }
          if(!isHomePathCollExistent)
          {
            // insert Home Path Collection into archive hierarchy
            ArchSessionAccessor.insertHomePathCollection(this, path2URI(homePathURI));
          }
        }
        else if("D".equals(action))
        {
          // remove Home Path Collection from archive hierarchy
          ArchSessionAccessor.removeHomePathCollection(this, path2URI(homePathURI));
        }
        else
        {
          throw new IllegalArgumentException("Unsupported value of parameter \"action\" in command \"SYNCHOMEPATH\": " + action);
        }
      }
      catch(Exception e)
      {
        cat.warningT(loc, "Could not insert Home Path Collection into archive hierarchy " + e.getMessage());
      }
    }
  }
  
  private String path2URI(String path)
  {
    if(path.endsWith("/"))
    {
      // cut last "/"
      return path.substring(0, path.length()-1);
    }
    return path;
  }
}
