package com.sap.archtech.initservice;

import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.engine.services.configuration.appconfiguration.ApplicationConfigHandlerFactory;
import com.sap.jmx.ObjectNameFactory;
import com.sap.security.core.server.destinations.api.DestinationException;
import com.sap.security.core.server.destinations.api.DestinationService;
import com.sap.security.core.server.destinations.api.HTTPDestination;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Severity;

/**
 * The class <code>DestinationInitializer</code> is responsible for the initialization of the HTTP Destination
 * used for communication with the XMLDAS.
 */
class DestinationInitializer
{
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Initialization Service");
  private static final Location loc = Location.getLocation("com.sap.archtech.initservice");
  
  private static final String XMLDAS_DESTNAME_PROPERTY = "ARCHCONN_XMLDASDEST";
  private static final String DEST_TYPE_HTTP = "HTTP";
  
  private final DestinationService destService;
  private final ApplicationConfigHandlerFactory appCfgHdlFctry;
  private final MBeanServer mbeanServer;
  
  DestinationInitializer() throws NamingException
  {
    Context ctx = null;
    try
    {
      ctx = new InitialContext();
      destService = (DestinationService)ctx.lookup(DestinationService.JNDI_KEY);
      if(destService == null)
      {
        throw new NamingException("Destination Service not available");
      }
      appCfgHdlFctry = (ApplicationConfigHandlerFactory)ctx.lookup("ApplicationConfiguration");
      if(appCfgHdlFctry == null)
      {
        throw new NamingException("Access to Configuration Service not available");
      }
      mbeanServer = (MBeanServer)ctx.lookup("jmx");
      if(mbeanServer == null)
      {
        throw new NamingException("Access to MBean Server not available");
      }
    }
    finally
    {
      if(ctx != null)
      {
        ctx.close();
      }
    }
  }
  
  /**
   * Create an HTTP Destination for communication between Archiving Connector and XMLDAS
   * @throws RemoteException Thrown if getting/creating the HTTP Destination failed
   * @throws DestinationException Thrown if creating the HTTP Destination failed
   */
  void initDstService() throws RemoteException, DestinationException
  {
    boolean isDestExistent = true;
    // Check for the existence of the HTTP Destination and create it if it is not existing
    try
    {
      isDestExistent = destService.getDestinationNames(DEST_TYPE_HTTP).contains(obtainConfigurationManagerEntry(XMLDAS_DESTNAME_PROPERTY));
    } 
    catch(DestinationException e)
    {
      // destination does not exists
      isDestExistent = false;
    }
    if(!isDestExistent)
    {
      HTTPDestination httpdest = (HTTPDestination)destService.createDestination(DEST_TYPE_HTTP);
      httpdest.setName(obtainConfigurationManagerEntry(XMLDAS_DESTNAME_PROPERTY));
      httpdest.setUrl(getXmlDasUrl());
      destService.storeDestination(DEST_TYPE_HTTP, httpdest);
      cat.infoT(loc, "HTTP destination created");
    } 
    else
    {
      loc.infoT("HTTP destination already existing");
    }
  }
  
  private String getXmlDasUrl()
  {
    // default values
    String host = "localhost";
    Integer port = Integer.valueOf(56000);
    try
    {
      // lookup the local instance mbean
      ObjectName localInstancePattern = new ObjectName(new StringBuilder("*:")
        									.append(ObjectNameFactory.J2EETYPE_KEY)
        									.append('=')
        									.append(ObjectNameFactory.SAP_J2EEInstance)
        									.append(',')
        									.append(ObjectNameFactory.NAME_KEY)
        									.append('=')
        									.append("local")
        									.append(",*")
        									.toString());
      Set<ObjectName> result = mbeanServer.queryNames(localInstancePattern, null);
      if(!result.isEmpty()) 
      {
        // local instance mbean found -> read its attributes
        ObjectName localInstanceON = result.iterator().next();
        host = (String)mbeanServer.getAttribute(localInstanceON, "Host"); 
        port = (Integer)mbeanServer.getAttribute(localInstanceON, "HttpPort");
      }
      else
      {
        throw new RuntimeException("MBean not found. Query was: " + localInstancePattern);
      }
    }
    catch(Exception e)
    {
      cat.logThrowableT(Severity.WARNING, loc, "Could not determine server name and port via MBean", e);
      cat.infoT(loc, "Default values will be used for HTTP Destination URL");
    }
    
    return new StringBuilder("http://").append(host).append(':').append(port).append("/DataArchivingService/DAS").toString();
  }

  private String obtainConfigurationManagerEntry(String property)
  {
    // Get Properties From sap.application.global.properties
    Properties appProps = appCfgHdlFctry.getApplicationProperties();
    if(appProps == null)
    {
      RuntimeException e = new RuntimeException("Property file is empty");
      cat.logThrowableT(Severity.ERROR, loc, e.getMessage(), e);
      throw e;
    }
    return (String)appProps.get(property);
  }
}
