package com.sap.archtech.archconn;

import java.io.IOException;

import com.sap.archtech.archconn.commands.AbstractArchCommand;
import com.sap.archtech.archconn.commands.ArchCommandEnum;
import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.archtech.archconn.exceptions.SessionHandlingException;
import com.sap.archtech.archconn.exceptions.UnsupportedCommandException;
import com.sap.archtech.archconn.httpclients.ArchHTTPClient;
import com.sap.archtech.archconn.httpclients.DTRdserviceClient;
import com.sap.archtech.archconn.httpclients.W3CdserviceClient;
import com.sap.archtech.archconn.util.ArchConfigProviderSingle;
import com.sap.archtech.archconn.util.DasResponse;
import com.sap.archtech.archconn.util.URI;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.tssap.dtr.client.lib.protocol.Connection;
import com.tssap.dtr.client.lib.protocol.IConnectionTemplate;

/**
 * 
 * Skeletal implementation of the ArchSession interface.
 * 
 * @author d025792
 *
 */
public abstract class AbstractArchSession implements ArchSession
{

  // ---------
  // Constants ------------------------------------------------------
  // ---------	

  protected static final Location loc = Location.getLocation("com.sap.archtech.archconn");
  protected static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");

  private static final ArchConfigProviderSingle acps = ArchConfigProviderSingle.getArchConfigProviderSingle();
  
  // ------------------
  // Instance Variables ------------------------------------------------------
  // ------------------	

  private final String clientlib;
  private final String archdestination;
  private final String archprotocol;
  private final String archuser;
  private ArchHTTPClient httpclient;
  private final URI archsethome;
  private Connection conn;
  private IConnectionTemplate httpTemplate;
  private final int updfrq;
    
  protected AbstractArchSession(String archuser, String archiveset, boolean checkArchiveSet) throws ArchConfigException, SessionHandlingException
  {
    this(archuser, archiveset, checkArchiveSet, acps.getArchDest());
  }
  
  protected AbstractArchSession(String archuser, String archiveset, boolean checkArchiveSet, String destination) throws ArchConfigException, SessionHandlingException
  {
    if(destination == null || "".equals(destination))
    {
      destination = acps.getArchDest();
    }
    if(checkArchiveSet)
    {
      if(archiveset == null || archiveset.equals(""))
      {
        throw new SessionHandlingException("No Archiving Set specified.");
      }
      if(archiveset.length() > MAX_ARCHIVESET_LENGTH)
      {
        throw new SessionHandlingException("Name of Archiving Set \"" + archiveset + "\" is too long. It must not exceed " + MAX_ARCHIVESET_LENGTH + " characters.");
      }
    }
    this.clientlib = acps.getArchProtLib();
    this.archdestination = destination;
    this.archprotocol = acps.getArchProt();
    this.archuser = archuser;
    this.archsethome = new URI(acps.getArchSetHome(archiveset));
    _createNewHttpConnection(false);
    this.updfrq = acps.getUpdFrequency();
  }
  
  /**
   * Create an archiving session instance without any HTTP connection
   */
  protected AbstractArchSession(String archuser, String archiveset) throws ArchConfigException, SessionHandlingException
  {
  	if(archiveset == null || archiveset.equals(""))
    {
      throw new SessionHandlingException("No Archiving Set specified.");
    }
    if(archiveset.length() > MAX_ARCHIVESET_LENGTH)
    {
      throw new SessionHandlingException("Name of Archiving Set \"" + archiveset + "\" is too long. It must not exceed " + MAX_ARCHIVESET_LENGTH + " characters.");
    }
    // This archiving session does not need any HTTP connection 
    conn = null;
    clientlib = null;
    archdestination = null;
    archprotocol = null;
    this.archuser = archuser;
    archsethome = new URI(acps.getArchSetHome(archiveset));
    updfrq = 0;
  }
  
  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#close()
   */
  public void close() throws SessionHandlingException
  {
  	if(conn != null)
    {
  		try
  		{
  			invalidateXmldasSession();
  		}
  		catch(IOException e)
  		{
  			cat.errorT(loc, e.getMessage());
  		}
  		finally
  		{
  			conn.close();
  		}
    }
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#cancel()
   */
  public void cancel() throws SessionHandlingException
  {
  	if(conn != null)
    {
  		try
  		{
  			invalidateXmldasSession();
  		}
  		catch(IOException e)
  		{
  			cat.errorT(loc, e.getMessage());
    	}
  		finally
  		{
  			conn.close();
  		}
    }
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#getArchdestination()
   */
  public String getArchdestination()
  {
    return archdestination;
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#getArchprotocol()
   */
  public String getArchprotocol()
  {
    return archprotocol;
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#getClientlib()
   */
  public String getClientlib()
  {
    return clientlib;
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#getArchsethome()
   */
  public URI getArchsethome()
  {
    return archsethome;
  }

  /* (non-Javadoc)
   * @see com.sap.archtech.archconn.ArchSession#getArchsethome(java.lang.String)
   */
  public URI getArchsethome(String archiveset) throws ArchConfigException
  {
    return new URI(acps.getArchSetHome(archiveset));
  }

  public ArchHTTPClient getHttpClient()
  {
    return httpclient;
  }
  
  protected String getArchUser()
  {
    return archuser;
  }
  
  protected int getUpdateFrequency()
  {
    return updfrq;
  }
  
  private ArchHTTPClient determineClientLib(String destination, String protocol, String clientlib, IConnectionTemplate connTemplate, Connection httpConn) throws IOException
  {
    if ("DSW3C".equalsIgnoreCase(clientlib))
    {
      return new W3CdserviceClient(destination, protocol);
    }
    else if ("DSDTR".equalsIgnoreCase(clientlib))
    {
      return new DTRdserviceClient(connTemplate, httpConn);
    }
    else
    {
      cat.errorT(loc, "Invalid Client Library " + clientlib + " specified");
      return null;
    }
  }
  
  /**
   * For internal usage only!
   */
  public void createNewHttpConnection(Object caller, boolean isDestinationReloadRequired) throws ArchConfigException
  {
    if(!isFriend(caller))
    {
      throw new IllegalArgumentException("Caller is not allowed to invoke this method: " + caller);
    }
    _createNewHttpConnection(isDestinationReloadRequired);
  }
  
  private void _createNewHttpConnection(boolean isDestinationReloadRequired) throws ArchConfigException
  {
    // close existing connection first
    if(conn != null)
    {
      conn.close();
    }
    // create new connection based on HTTP template 
    httpTemplate = acps.getHttpTemplate(this, archdestination, isDestinationReloadRequired);
    conn = new com.tssap.dtr.client.lib.protocol.Connection(httpTemplate);
    conn.setSocketReadTimeout(acps.getReadTimeout());
    conn.setSocketConnectTimeout(acps.getConnectTimeout());
    conn.setSocketExpirationTimeout(acps.getExpTimeout());
    try
    {
      httpclient = determineClientLib(archdestination, archprotocol, clientlib, httpTemplate, conn);
    }
    catch (IOException ioex)
    {
      throw new ArchConfigException(ioex.getMessage());
    }
  }
  
  private boolean isFriend(Object caller)
  {
    if(caller instanceof AbstractArchCommand)
    {
      return true;
    }
    return false;
  }
  
	private void invalidateXmldasSession() throws IOException, SessionHandlingException
	{
		try
		{
			ArchCommand invalidateSessionCmd = createCommand(ArchCommandEnum.SESSIONINVALIDATE.toString());
			invalidateSessionCmd.execute();
			ArchResponse response = invalidateSessionCmd.getResponse();
			int responseStatusCode = response.getStatusCode();
			if(responseStatusCode != DasResponse.SC_OK)
			{
				String errMsg = new StringBuilder("XML DAS command \"SESSIONINVALIDATE\" returned ")
					.append(responseStatusCode)
					.append("(protocol message: ")
					.append(response.getProtMessage())
					.append(" , service message: ")
					.append(response.getServiceMessage())
					.append(")")
					.toString();
				cat.errorT(loc, errMsg);
			}
		}
		catch(UnsupportedCommandException e)
		{
			cat.errorT(loc, e.getMessage());
		}
	}
}
