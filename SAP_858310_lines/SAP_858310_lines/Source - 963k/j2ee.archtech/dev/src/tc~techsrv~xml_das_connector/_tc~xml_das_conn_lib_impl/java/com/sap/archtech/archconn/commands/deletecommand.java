package com.sap.archtech.archconn.commands;

import java.io.IOException;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchResponse;
import com.sap.archtech.archconn.util.DasResponse;
import com.sap.archtech.archconn.util.URI;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * 
 * The DELETE command is 
 * used to delete archived resources.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class DeleteCommand extends AbstractArchCommand
{
  private static final Location loc = Location.getLocation("com.sap.archtech.archconn.commands");
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");
  
  private final String user;
  private String uri;
  private boolean isCollDeletionRequested;
  
  protected DeleteCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    user = archuser;
    uri = null;
    isCollDeletionRequested = false;
    super.addParam("method", "DELETE");
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri)
  {
    String oldstyleuri;
    oldstyleuri = uri.toString();
    if ((oldstyleuri.length() > 1) && (oldstyleuri.endsWith("/")))
      oldstyleuri = oldstyleuri.substring(0, oldstyleuri.length() - 1);
    super.addParam("uri", oldstyleuri);
    this.uri = uri.toString();
  }
  
  public void addParam(String param)
  {
    super.addParam(param);
    if(ArchCommand.DELETE_RANGE_COL.equals(param))
    {
      // collection deletion requested
      isCollDeletionRequested = true;
    }
    else if(ArchCommand.DELETE_RANGE_RES.equals(param))
    {
      // resource deletion requested
      isCollDeletionRequested = false;
    }
  }
  
  public void addParam(String hfield, String value)
  {
    if("delete_range".equals(hfield))
    {
      addParam(new StringBuilder(hfield).append(":").append(value).toString());
    }
    else
    {
      super.addParam(hfield, value);
    }
  }
  
  /**
   * Get the response from the XMLDAS and log the result
   */
  public ArchResponse getResponse()
  {
    ArchResponse response = super.getResponse();
    try
    {
      StringBuilder tmp = null;
      if(response.getStatusCode() == DasResponse.SC_OK)
      {
        if(isCollDeletionRequested)
        {
          tmp = new StringBuilder("DELETE: Collection ");
        }
        else
        {
          tmp = new StringBuilder("DELETE: Resource ");
        }
        cat.infoT(loc, 
            tmp
            .append(uri)
            .append(" successfully deleted by user ")
            .append(user)
            .toString());
      }
      else
      {
        if(isCollDeletionRequested)
        {
          tmp = new StringBuilder("DELETE: User ").append(user).append(" tried to delete collection ");
        }
        else
        {
          tmp = new StringBuilder("DELETE: User ").append(user).append(" tried to delete resource ");
        }
        cat.errorT(loc, 
            tmp
            .append(uri)
            .append(" but failed. See XMLDAS log for further information.")
            .toString());
      }
    }
    catch(IOException e)
    {
      // thrown if "statusCode" retrieval failed
      cat.logThrowableT(Severity.ERROR, loc, "Could not get status code of XMLDAS response", e);
    }
    return response;
  }
}
