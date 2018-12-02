package com.sap.archtech.archconn.commands;

import java.io.IOException;
import java.sql.SQLException;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.ArchSessionAccessor;
import com.sap.archtech.archconn.LocalArchResponse;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.response.IGetSessionInfoCommand;
import com.sap.archtech.archconn.util.DasResponse;
import com.sap.archtech.archconn.util.URI;
import com.sap.archtech.archconn.values.SessionInfo;
import com.sap.tc.logging.Location;

/**
 * The SESSIONINFO archiving command is used to
 * get information about an archiving session.
 * 
 * This is a local archiving command! (@see LocalArchResponse)
 * 
 * @author d025792
 *
 */
public class SessioninfoCommand extends AbstractArchCommand implements IGetSessionInfoCommand
{
  private static final Location loc = Location.getLocation("com.sap.archtech.archconn.commands.SessioninfoCommand");
  
  private SessionInfo sessionInfo;
  
  protected SessioninfoCommand(AbstractArchSession archSessionRef, String archuser)
  {
    super(archSessionRef, archuser);
    sessionInfo = null;
  }

  public void execute() throws IOException
  {
    LocalArchResponse localResponse = null;
    String uri = getStringParam("uri");
    if(uri == null)
    {
      localResponse = createResponse(DasResponse.SC_PARAMETER_MISSING, "URI not specified", null, null);
    }
    else
    {
      try
      {
        sessionInfo = ArchSessionAccessor.loadSessionInfo(this, uri);
        if(sessionInfo == null)
        {
          localResponse = createResponse(DasResponse.SC_DOES_NOT_EXISTS, "No archiving session found for this URI", null, null);
        }
        else
        {
        	localResponse = createResponse(DasResponse.SC_OK, null, "Ok", "Ok");
        }
      }
      catch(SQLException e)
      {
        loc.throwing("SESSIONINFO", e);
        localResponse = createResponse(DasResponse.SC_SQL_ERROR, e.getMessage(), null, null);
      }
      catch(SessionHandlingException e)
      {
        loc.throwing("SESSIONINFO", e);
        localResponse = createResponse(DasResponse.SC_SQL_ERROR, e.getMessage(), null, null);
      }
    }
    setResponse(localResponse);
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchCommand#addParam(com.sap.archtech.archconn.util.URI)
   */
  public void addParam(URI uri)
  {
    String colname;
    colname = uri.toString();
    if ((colname.length() > 1) && (colname.endsWith("/")))
      colname = colname.substring(0, colname.length() - 1);
    super.addParam("uri", colname);
  }

//  public ArchResponse getResponse()
//  {
//    return super.getResponse();
//  }

  private LocalArchResponse createResponse(int errorCode, String errorMessage, String serviceMessage, String protocolMessage)
  {
  	if(serviceMessage == null)
  	{
  		serviceMessage = Integer.toString(errorCode) + " " + errorMessage;
  	}
  	return new LocalArchResponse(this, errorCode, errorMessage, serviceMessage, protocolMessage);
  }
  
  public SessionInfo getSessionInfo()
  {
  	return sessionInfo;
  }
}
