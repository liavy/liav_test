package com.sap.archtech.archconn.httpclients;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.archtech.archconn.ArchCommand;
import com.sap.archtech.archconn.ArchResponse;
import com.sap.archtech.archconn.DefaultArchResponse;
import com.sap.security.core.server.destinations.api.ConfigurationException;
import com.sap.security.core.server.destinations.api.Destination;
import com.sap.security.core.server.destinations.api.DestinationException;
import com.sap.security.core.server.destinations.api.DestinationService;
import com.sap.security.core.server.destinations.api.HTTPDestination;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Implementation of the client-specific behavior
 * using the SAP J2EE Engine destination service
 * and the Jigsaw W3C HTTP Client API for the
 * communication with the archiving service. 
 * 
 * @see com.sap.archtech.archconn.httpclients#ArchHTTPClient
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class W3CdserviceClient implements ArchHTTPClient
{
  private static final Location loc = Location.getLocation("com.sap.archtech.archconn");
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");

  private final HTTPDestination httpDestination;

  public W3CdserviceClient(String dest, String protocol) throws IOException
  {
    // TEMPORARY: for the IAIK-Libs
    //    System.getProperties().put("org.w3c.www.protocol.http.connections.max", String.valueOf(Integer.MAX_VALUE));
    //    System.getProperties().put("org.w3c.www.protocol.http.connections.timeout", "10000");
    try
    {
      // lookup a service from a library
      // der große Omelett-Trick
      ClassLoader saveLoader = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

      Context ctx = new InitialContext();
      Object obj = ctx.lookup(DestinationService.JNDI_KEY);
      DestinationService dstService = (DestinationService) obj;

      Thread.currentThread().setContextClassLoader(saveLoader);

      if (dstService == null)
        throw new NamingException("Destination service not available");

      Destination destination = dstService.getDestination(protocol, dest);
      //for HTTP destination: cast
      httpDestination = (HTTPDestination) destination;
    }
    catch (NamingException nmex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "W3CdserviceClient()", nmex);
      throw new IOException(nmex.getMessage());
    }
    catch (DestinationException dstex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "W3CdserviceClient()", dstex);
      throw new IOException(dstex.getMessage());
    }
  }

  public ArchResponse executeRequest(HashMap<? extends Object, ? extends Object> params, ArchCommand archCommand) throws IOException
  {
    boolean hasStream = false;
    HttpURLConnection connection = getConFromDestService();
    connection.setRequestProperty("Content-Type", "application/octet-stream");

    // assemble request
    HashMap<Object, Object> tmp = new HashMap<Object, Object>(params);
    Set<Entry<Object, Object>> entrySet = tmp.entrySet();
    for (Entry<Object, Object> entry : entrySet)
    {
      if (entry.getKey().equals("STREAM"))
      {
      	hasStream = true;
      }
      else
      {
      	connection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
      }
    }

    if (hasStream)
    {
      connection.setDoOutput(true);
      BufferedOutputStream bos = new BufferedOutputStream(connection.getOutputStream());
      BufferedInputStream bis = new BufferedInputStream((InputStream) params.get("STREAM"));

      int i = 0;
      while ((i = bis.read()) != -1)
      {
      	bos.write(i);
      }
      bos.flush();
      bos.close();
      bis.close();
    }

    // send request
    connection.connect();

    // fill ArchResponse Object
    return new DefaultArchResponse(connection, archCommand, params, this);
  }

  private HttpURLConnection getConFromDestService() throws IOException
  {
    try
    {
      //obtain a HTTPUrlConnection from the destinationHttpURLConnection
      return httpDestination.getURLConnection("POST");
    }
    catch (ConfigurationException cfex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "W3CdserviceClient().getConFromDestService()", cfex);
      throw new IOException(cfex.getMessage());
    }
  }
}
