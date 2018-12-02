package com.sap.archtech.archconn.util;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.sap.archtech.archconn.exceptions.ArchConfigException;
import com.sap.security.core.server.destinations.api.Destination;
import com.sap.security.core.server.destinations.api.DestinationException;
import com.sap.security.core.server.destinations.api.DestinationService;
import com.sap.security.core.server.destinations.api.HTTPDestination;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * The <code>DestinationFactory</code> class provides access to HTTP destinations
 * required for XML DAS communication.
 */
public class DestinationFactory
{
	private static final Location loc = Location.getLocation("com.sap.archtech.archconn");
	private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");
		
	private static final DestinationFactory theInstance = new DestinationFactory();
	
	private static final String DEST_TYPE_HTTP = "HTTP";
	private static final String DAS_DEST_PREFIX = "DAS";
	
	private final DestinationService destService;
	private final ArchConfigProviderSingle acps;
	
	private DestinationFactory()
	{
		acps = ArchConfigProviderSingle.getArchConfigProviderSingle();
		ClassLoader saveClassLoader = Thread.currentThread().getContextClassLoader();
		InitialContext ctx = null;
		try
		{
			// switch class loader of the current thread in order to let the J2EE engine find the correct object
			ClassLoader switchClassloader = this.getClass().getClassLoader();
			Thread.currentThread().setContextClassLoader(switchClassloader);
			// Lookup destination Service
			ctx = new InitialContext();
			destService = (DestinationService)ctx.lookup(DestinationService.JNDI_KEY_LOCAL);
			if(destService == null)
			{
				throw new NamingException("Destination service not available");
			}
		}
		catch(NamingException namex)
		{
			cat.logThrowableT(Severity.ERROR, loc, "Lookup for destination service failed", namex);
			throw new RuntimeException(namex.getMessage());
		}
		finally
		{
			// reset the class loader of the current thread
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
	
	public static DestinationFactory getInstance()
	{
		return theInstance;
	}
	
	/**
	 * Create a DAS HTTP destination from the given parameters if it does not exist yet.
	 */
	public String getHttpDestination(URL url, String user, String pw) throws RemoteException, DestinationException
	{
		// Check for the existence of the HTTP destination and create it if necessary
		String destinationName = getDestinationName(url);
		Destination dest = null;
		try
		{
			dest = destService.getDestination(DEST_TYPE_HTTP, destinationName);
		} 
		catch(DestinationException e)
		{
			// $JL-EXC$
			// destination does not exists
		}
		if(dest != null)
		{
			// destination exists -> update it
			HTTPDestination httpDest = (HTTPDestination)dest;
			httpDest.setName(destinationName);
			httpDest.setUrl(url.toString());
			httpDest.setUsernamePassword(user, pw);
			destService.updateDestination(DEST_TYPE_HTTP, httpDest);
			try
			{
				destService.ping(DEST_TYPE_HTTP, destinationName);
				cat.infoT(loc, "Updated HTTP destination " + destinationName);
			}
			catch(DestinationException e)
			{
				// ping failed
				throw e;
			}
		}
		else
		{
			// create new destination
			HTTPDestination httpDest = (HTTPDestination)destService.createDestination(DEST_TYPE_HTTP);
			httpDest.setName(destinationName);
			httpDest.setUrl(url.toString());
			httpDest.setUsernamePassword(user, pw);
			destService.storeDestination(DEST_TYPE_HTTP, httpDest);
			try
			{
				destService.ping(DEST_TYPE_HTTP, destinationName);
				cat.infoT(loc, "Created HTTP destination " + destinationName);
			}
			catch(DestinationException e)
			{
				// ping failed -> remove destination
				destService.removeDestination(DEST_TYPE_HTTP, destinationName);
				throw e;
			}
		} 
		return destinationName;
	}
	
	/**
	 * Get a map of all existing DAS HTTP destinations (key = name of the HTTP destination, value = URL of the HTTP destination).
	 * Includes name and URL of the default DAS destination specified with the "ARCHDEST" configuration property. 
	 */
	public Map<String, String> getExistingDasDestinations() throws RemoteException, DestinationException
	{
		List<String> dasDestinationNames = destService.getDestinationNames(DEST_TYPE_HTTP);
		Map<String, String> dasDestinationURLs = new HashMap<String, String>(dasDestinationNames.size());
		String destinationName = null;
		HTTPDestination httpDest = null;
		for(Iterator<String> iter = dasDestinationNames.listIterator(); iter.hasNext();)
		{	
			destinationName = iter.next();
			if(destinationName.startsWith(DAS_DEST_PREFIX))
			{
				httpDest = (HTTPDestination)destService.getDestination(DEST_TYPE_HTTP, destinationName); 
				dasDestinationURLs.put(destinationName, httpDest.getUrl());
			}
		}
		// add default DAS destination if it is not yet part of the map
		try
		{
			destinationName = acps.getArchDest();
			if(!dasDestinationURLs.containsKey(destinationName))
			{
				try
				{
					httpDest = (HTTPDestination)destService.getDestination(DEST_TYPE_HTTP, destinationName);
					dasDestinationURLs.put(destinationName, httpDest.getUrl());
				}
				catch(DestinationException e)
				{
					// $JL-EXC$
					// thrown if there is no destination named with the value of "ARCHDEST" (e.g. "DASdefault") -> can be ignored here
				}
			}
		}
		catch(ArchConfigException e)
		{
			// $JL-EXC$
			// thrown if there is no config entry for the "ARCHDEST" property -> can be ignored here
		}
		return dasDestinationURLs;
	}
	
	private String getDestinationName(final URL url)
	{
		String serverName = url.getHost();
		int port = url.getPort();
		return new StringBuilder(DAS_DEST_PREFIX).append('_').append(serverName).append('_').append(port).toString();
	}
}
