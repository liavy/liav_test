package com.sap.archtech.archconn.servicereg;

/**
 * Registry for Primary Services used by the XMLDAS Connector.
 * Note, those services are registered via a special interface to avoid introducing
 * undesired references between a Primary Library and a Primary Service.
 */
public class ServiceRegistry 
{
	private static final ServiceRegistry theInstance = new ServiceRegistry();
	private ISchedulerService theSchedulerService;
	private IConfigurationService theConfigurationService;

	private ServiceRegistry(){}
	
	public static ServiceRegistry getInstance()
	{
		return theInstance;
	}
	
	public synchronized void registerSchedulerServiceImpl(ISchedulerService service)
	{
		theSchedulerService = service;
	}
	
	public synchronized void deregisterSchedulerServiceImpl()
	{
		theSchedulerService = null;
	}

	public synchronized void deregisterConfigurationServiceImpl()
	{
		theConfigurationService = null;
	}
	
	public synchronized void registerConfigurationServiceImpl(IConfigurationService service)
	{
		theConfigurationService = service;
	}

	public synchronized ISchedulerService getSchedulerService()
	{
		return theSchedulerService;
	}
	
	public synchronized IConfigurationService getConfigurationService()
	{
		return theConfigurationService;
	}
	
	public synchronized boolean isSchedulerServiceRegistered()
	{
		return theSchedulerService != null;
	}
	
	public synchronized boolean isConfigurationServiceRegistered()
	{
		return theConfigurationService != null;
	}
}
