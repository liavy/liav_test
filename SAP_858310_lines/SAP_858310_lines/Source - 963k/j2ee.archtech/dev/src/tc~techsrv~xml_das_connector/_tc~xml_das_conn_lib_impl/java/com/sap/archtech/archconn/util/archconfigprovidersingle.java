package com.sap.archtech.archconn.util;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sap.archtech.archconn.AbstractArchSession;
import com.sap.archtech.archconn.ArchSession;
import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.security.core.server.destinations.api.ConfigurationException;
import com.sap.security.core.server.destinations.api.Destination;
import com.sap.security.core.server.destinations.api.DestinationException;
import com.sap.security.core.server.destinations.api.DestinationService;
import com.sap.security.core.server.destinations.api.HTTPDestination;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.tssap.dtr.client.lib.protocol.IConnectionTemplate;

/**
 * Reads the configuration parameters for the
 * Archiving Connector. As the table for
 * archiving sets and their home collections
 * may be large, this class is realized as
 * a singleton.
 *
 * @author d025792
 *
 */
public class ArchConfigProviderSingle
{
  private static ArchConfigProviderSingle theInstance = null;

  private static final String SYSTEMPL = "<SYSID>";
  private static final String SYSPROP = "SAPSYSTEMNAME";
  private static final String INSTPROP = "SAPMYNAME";
  private static final String PROTOCOL = "HTTP";
  private static final String CONNPOOL = "jdbc/SAP/BC_XMLA";
  private static final String HOMEPATHPROP = "ahome";
  private static final String RESTARTPROP = "wstart";
  private static final Set<String> ilmRulesProps = new HashSet<String>(2);
  static
  {
  	ilmRulesProps.add("rulesetname");
  	ilmRulesProps.add("rulesdcname");
  }
  private static final String GET_ASET_CONF = "SELECT * FROM bc_xmla_asets WHERE asetname = ?";
  private static final String UPD_STATUS = "UPDATE bc_xmla_asessions SET sessionstatus = ? WHERE sessionstatus = ? AND ainstance = ?";

  private static final Location loc = Location.getLocation("com.sap.archtech.archconn");
  private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");

  private final DestinationService dstService;
  private final DataSource ds;
  private final ArchconnConfiguration aconf;
  private final Map<String, Destination> nonConfiguredDestinations;

  // Modifiable attributes
  private String latestConfiguredDestinationName;
  private Destination configuredDestination;
   
  private ArchConfigProviderSingle() throws NamingException, DestinationException, RemoteException
  {
    // get database connection pool
    Context ctx = null;
    try
    {
      ctx = new InitialContext();
      ds = (DataSource)ctx.lookup(CONNPOOL);
    }
    catch(NamingException namex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Lookup for database connection pool failed", namex);
      throw namex;
    }
    
    // reset open archiving sessions
    try
    {
      resetOpenSessions();
    }
    catch(SQLException sqlex)
    {
      // warning only, do not throw exception
      cat.logThrowableT(Severity.WARNING, loc, "Resetting open Archiving Sessions failed", sqlex);
    }
    
    // initialize ArchconnConfiguration and lookup destination service
    nonConfiguredDestinations = Collections.synchronizedMap(new HashMap<String, Destination>());
    aconf = new ArchconnConfiguration();
    // lookup a service from a library
    // der große Omelett-Trick (see Note 781457)
    // ClassLoader saveLoader = Thread.currentThread().getContextClassLoader();
    // Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    ClassLoader saveClassLoader = Thread.currentThread().getContextClassLoader();
    loc.debugT("Classloader of current thread is: " + saveClassLoader);
    try
    {
      // switch class loader of the current thread in order to let the J2EE engine find the correct object
      ClassLoader switchClassloader = this.getClass().getClassLoader();
      loc.debugT("Switch Classloader of current thread to: " + switchClassloader);
      Thread.currentThread().setContextClassLoader(switchClassloader);
      // Lookup destination Service
      dstService = (DestinationService)ctx.lookup(DestinationService.JNDI_KEY_LOCAL);
      if(dstService == null)
      {
        throw new NamingException("Destination service not available");
      }
      latestConfiguredDestinationName = aconf.getArchDestination();
      // destination type "HTTP" is used, even if it is a HTTPS connection
      configuredDestination = dstService.getDestination(PROTOCOL, latestConfiguredDestinationName);
    }
    catch(DestinationException dstex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Getting the Destination failed", dstex);
      throw dstex;
    }
    catch(RemoteException rmex)
    {
      cat.logThrowableT(Severity.ERROR, loc, "Getting the Destination failed", rmex);
      throw rmex;
    }
    finally
    {
      // reset the class loader of the current thread
      loc.debugT("Reset Classloader of current thread to: " + saveClassLoader);
      Thread.currentThread().setContextClassLoader(saveClassLoader);
      // close JNDI context
      if(ctx != null)
      {
        try
        {
          ctx.close();
        }
        catch(NamingException e)
        {
          cat.logThrowableT(Severity.WARNING, loc, "Closing the JNDI Context failed", e);
        }
      }
    }
  }

   // --------------
   // Public Methods ----------------------------------------------------------
   // --------------

   /**
    * @return Name of the destination in the SAP J2EE Engine
    *          destination service under which the XML Data
    *          Archiving Service (XML DAS) can be located.
    */
   public String getArchDest() throws ArchConfigException
   {
      String archdest = aconf.getArchDestination();
      if (archdest == null)
      {
        throw new ArchConfigException("Found no configuration for archiving connector");
      }
      return archdest;
   }

   /**
    * @return Name of the network protocol used to
    *          communicate with the XML DAS (e.g. HTTP, HTTPS)
    */
   public String getArchProt()
   {
      return PROTOCOL;
   }

   /**
    * @return Name of the client library used to
    *          communicate with the XML DAS
    */
   public String getArchProtLib() throws ArchConfigException
   {
      String archprotclient = aconf.getArchClientLib();
      if (archprotclient == null)
      {
        throw new ArchConfigException("Found no configuration for archiving connector");
      }
      return archprotclient;
   }

   /**
    * Delivers the home collection of the specified
    * archiving set.
    *
    * @param aset the archiving set
    * @return application specific part of the collection
    * hierarchy
    * @throws ArchConfigException if no configuration value
    * was found for this application
    */
   public String getArchSetHome(String aset) throws ArchConfigException
   {
      Map<String, String> hm = getArchSetConfig(aset);

      if (hm.containsKey(HOMEPATHPROP))
      {
        return (String) hm.get(HOMEPATHPROP);
      }
      throw new ArchConfigException("No home path found in the configuration of " + aset);
   }

   /**
    * Returns the complete customizing in form of a Map
    * for a specified archiving set. The key of the map contains
    * the configuration name (e.g. "AHOME" for the archiving
    * home path), the value of the map contains the
    * configuration value.
    *
    * @param aset Archiving Set
    * @return Map Configuration Entries for the Archiving Set.
    * @throws ArchConfigException if no configuration information was found
    * for this archiving set or an error occured during reading the
    * configuration data
    */

   public Map<String, String> getArchSetConfig(String aset) throws ArchConfigException
   {
      String method = "getArchSetConfig(String aset)";
      HashMap<String, String> hm = new HashMap<String, String>();
      Connection conn = null;
      PreparedStatement pst5 = null;
      String archprop;
      String propvalue;
      int sysindex = 0;

      // if the "global" archiving set is defined, return an empty
      // HashMap
      if (ArchSession.GLOBAL_ARCHSET.equals(aset))
      {
         hm.put(HOMEPATHPROP, "/");
         return hm;
      }

      ResultSet rs5 = null;
      try
      {
         loc.entering(method);
         conn = this.getConnection();
         pst5 = conn.prepareStatement(GET_ASET_CONF);
         pst5.setString(1, aset);
         rs5 = pst5.executeQuery();

         while (rs5.next())
         {
            archprop = rs5.getString("asetprop").toLowerCase();
            propvalue = rs5.getString("propvalue");
            sysindex = propvalue.indexOf(SYSTEMPL);

            if (sysindex >= 0)
            {
               propvalue =
                  propvalue.substring(0, sysindex)
                     + System.getProperty(SYSPROP).toLowerCase()
                     + propvalue.substring(sysindex + SYSTEMPL.length(), propvalue.length());
            }
            // Note: The values of the ILM rules properties are case-sensitive
            hm.put(archprop, ilmRulesProps.contains(archprop) ? propvalue : propvalue.toLowerCase());
            loc.debugT(method, "Archset " + aset + " conf entry: " + archprop + ": " + propvalue);
         }
      }
      catch (SQLException sqlex)
      {
         cat.logThrowableT(Severity.ERROR, loc, "ArchConfigProviderSingle()", sqlex);
         throw new ArchConfigException("Error reading customizing for archiving set " + aset);
      }
      finally
      {
      	if(rs5 != null)
      	{
      		try
      		{
      			rs5.close();
      		}
      		catch(SQLException e)
      		{
      			cat.logThrowableT(Severity.ERROR, loc, "ArchConfigProviderSingle()", e);
      		}
      	}
      	if(pst5 != null)
      	{
      		try
      		{
      			pst5.close();
      		}
      		catch(SQLException e)
      		{
      			cat.logThrowableT(Severity.ERROR, loc, "ArchConfigProviderSingle()", e);
      		}
      	}
      	if(conn != null)
      	{
      		try
      		{
      			conn.commit();
      			conn.close();
      		}
      		catch(SQLException e)
      		{
      			cat.logThrowableT(Severity.ERROR, loc, "ArchConfigProviderSingle()", e);
      		}
      	}
      }  
      
      if (hm.size() == 0)
         throw new ArchConfigException("No customizing data found for archiving set " + aset);

      loc.exiting();
      return hm;
   }

   /**
    * The frequency with which the counters for the number of
    * written or deleted documents are updated in the database
    * (session tracking).
    * 0 means update only at the end of the session (session close)
    * 1..MAXINT means the update happens after the specified number
    * of documents are written/deleted
    *
    * @return update frequency for the number of written/deleted documents
    */
   public int getUpdFrequency()
   {
      return aconf.getUpdateFreq();
   }

   /**
    *
    * @return Read Timeout for HTTP(S) connections between the Archiving Connector
    * and the XML DAS in milliseconds
    * @throws ArchConfigException if no configuration value was found
    */
   public int getReadTimeout()
   {
      return aconf.getReadTimeout();
   }

   /**
    *
    * @return Connect Timeout for HTTP(S) connections between the Archiving Connector
    * and the XML DAS in milliseconds
    * @throws ArchConfigException if no configuration value was found
    */
   public int getConnectTimeout()
   {
      return aconf.getConnTimeout();
   }

   /**
    *
    * @return Expiration Timeout for HTTP(S) connections between the Archiving Connector
    * and the XML DAS in milliseconds
    * @throws ArchConfigException if no configuration value was found
    */
   public int getExpTimeout()
   {
      return aconf.getExpTimeout();
   }

   /**
    * Convenience method. Better use getArchSetConfig(String archset).
    *
    * The open() of an archive write session contains a check if a broken write session for this
    * archive set exists. If so, no further archiving is allowed (delete first). This check can be
    * overwritten by setting an archset property named "WSTART" for an archive set to "NOCHECK" in
    * table BC_XMLA_ASETS.
    *
    * @param archset archiving set for which the write restart behaviour is determined
    * @return true if a new write session can be scheduled even if a broken write session exists;
    * false otherwise
    */
   public boolean getApplRestart(String archset)
   {
      Map<String, String> hm = null;
      boolean status = false;

      try
      {
         hm = getArchSetConfig(archset);
      }
      catch (ArchConfigException acex)
      {
        /*
         * If customizing can not be read, return also false.
         * JLin error if catch block would be empty.
         */
         status = false;
      }

      if ((hm != null) && (hm.containsKey(RESTARTPROP)))
         status = true;

      return status;
   }

   /**
    * @return the (single) instance of the ArchConfigProviderSingle
    */
  public static synchronized ArchConfigProviderSingle getArchConfigProviderSingle()
  {
    if(theInstance == null)
    {
      try
      {
        theInstance = new ArchConfigProviderSingle();
      }
      catch(Exception e)
      {
        throw new RuntimeException(e);
      }
    }
    return theInstance;
  }

   /**
    * @return HTTP connection template
    */
  public IConnectionTemplate getHttpTemplate() throws ArchConfigException
  { 
    String configuredDestinationName = aconf.getArchDestination();
    if(!latestConfiguredDestinationName.equals(configuredDestinationName))
    {
      // Name of Destination has been changed in Configuration Editor -> load the Destination instance
      latestConfiguredDestinationName = aconf.getArchDestination();
      configuredDestination = reloadDestination(latestConfiguredDestinationName);
    }
		else if(configuredDestination == null)
		{
			// if ArchConfigProviderSingle initialization failed
			configuredDestination = reloadDestination(configuredDestinationName);
		}
    try
    {
      // create IConnectionTemplate
      return ((HTTPDestination)configuredDestination).getHTTPConnectionTemplate();
    }
    catch(ConfigurationException e)
    {
      String errMsg = new StringBuilder("Creating an HTTP Connection Template for HTTP Destination \"")
      					.append(configuredDestinationName)
      					.append("\" failed")
      					.toString();
      cat.logThrowableT(Severity.ERROR, loc, errMsg, e);
      throw new ArchConfigException(errMsg);
    }
  } 
   
  private Destination reloadDestination(String destinationName) throws ArchConfigException
  {
    if(dstService == null)
    {
      throw new ArchConfigException("Archiving connector initialization failed. Check the log entries for details");
    }
    try
    {
      return dstService.getDestination(getArchProt(), destinationName);
    }
    catch(DestinationException e)
    {
      String errMsg = new StringBuilder("Getting the Destination \"").append(destinationName).append("\" failed").toString();
      cat.logThrowableT(Severity.ERROR, loc, errMsg, e);
      throw new ArchConfigException(errMsg);
    }
    catch(RemoteException e)
    {
      String errMsg = new StringBuilder("Getting the Destination \"").append(destinationName).append("\" failed").toString();
      cat.logThrowableT(Severity.ERROR, loc, errMsg, e);
      throw new ArchConfigException(errMsg);
    }
  }
  
  /**
   * For internal usage only!
   */
  public IConnectionTemplate getHttpTemplate(Object caller, String destinationName, boolean isDestinationReloadRequired) throws ArchConfigException
  { 
    if(!isFriend(caller))
    {
      throw new IllegalArgumentException("Caller is not allowed to invoke this method: " + caller);
    }

    try
    {
      Destination dest = null;
      if(nonConfiguredDestinations.containsKey(destinationName))
      {
        // destination has been determined before
        if(isDestinationReloadRequired)
        {
          dest = reloadDestination(destinationName);
          return ((HTTPDestination)dest).getHTTPConnectionTemplate();
        }
        else
        {
          dest = nonConfiguredDestinations.get(destinationName);
          return ((HTTPDestination)dest).getHTTPConnectionTemplate();
        }
      }
      if(!destinationName.equals(getArchDest()))
      {
        // destination has not been determined before
        // -> create a connection template for a destination other than "archdest" 
        dest = reloadDestination(destinationName);
        nonConfiguredDestinations.put(destinationName, dest);
        return ((HTTPDestination)dest).getHTTPConnectionTemplate();
      }
    }
    catch(ConfigurationException e)
    {
      String errMsg = new StringBuilder("Creating an HTTP Connection Template for HTTP Destination \"")
               .append(destinationName)
               .append("\" failed")
               .toString();
      cat.logThrowableT(Severity.ERROR, loc, errMsg, e);
      throw new ArchConfigException(errMsg);
    }
    
    // default: use given IConnectionTemplate
    if(isDestinationReloadRequired)
    {
      // force destination reload
      latestConfiguredDestinationName = "";
    }
    return getHttpTemplate();
  }
  
  private boolean isFriend(Object caller)
  {
    if(caller instanceof AbstractArchSession)
    {
      return true;
    }
    return false;
  }
  
  private Connection getConnection() throws SQLException
  {
    Connection conn;
    conn = ds.getConnection();
    conn.setAutoCommit(false);
    return conn;
  } /*
                         * Check if archiving sessions are open and set them to broken.
                         */
   private void resetOpenSessions() throws SQLException
   {
      Connection conn = null;
      PreparedStatement pst4 = null;
      int updcount = 0;
      String instance;
      try
      {
         conn = this.getConnection();
         instance = System.getProperty(INSTPROP);
         pst4 = conn.prepareStatement(UPD_STATUS);
         pst4.setString(1, ArchSession.WRITE_SESSION_CANCELED);
         pst4.setString(2, ArchSession.WRITE_SESSION_OPEN);
         pst4.setString(3, instance);
         updcount = pst4.executeUpdate();
         if (updcount != 0)
         {
            cat.infoT(loc, "Set " + updcount + " write session(s) to broken");
            updcount = 0;
         }

         pst4.setString(1, ArchSession.DELETE_SESSION_CANCELED);
         pst4.setString(2, ArchSession.DELETE_SESSION_OPEN);
         pst4.setString(3, instance);
         updcount = pst4.executeUpdate();
         if (updcount != 0)
         {
            cat.infoT(loc, "Set " + updcount + " delete session(s) to broken");
            updcount = 0;
         }

         pst4.setString(1, ArchSession.SESSION_CANCELED);
         pst4.setString(2, ArchSession.SESSION_OPEN);
         pst4.setString(3, instance);
         pst4.executeUpdate();
         if (updcount != 0)
         {
            cat.infoT(loc, "Set " + updcount + " one phase session(s) to broken");
            updcount = 0;
         }
      }
      finally
      {
         try
         {
            if (pst4 != null)
               pst4.close();
            if (conn != null)
            {
               conn.commit();
               conn.close();
            }
         }
         catch (SQLException sqlex2)
         {
            cat.logThrowableT(Severity.ERROR, loc, "resetOpenSesssions()", sqlex2);
         }
      }
   }
}
