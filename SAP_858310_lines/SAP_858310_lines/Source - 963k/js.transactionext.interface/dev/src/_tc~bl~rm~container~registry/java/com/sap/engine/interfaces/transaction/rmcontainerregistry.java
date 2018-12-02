package com.sap.engine.interfaces.transaction;

/**
 *  This interface is exposed and implemented from transaction service.
 *  Each container that is responsible for deployment and lifecycle resource 
 *  manager have to register itself into this repository during its startup. 
 * 
 * @author I024163
 *
 */
public interface RMContainerRegistry {
	
	/**
	 * The name with which the implementation of this interface is store into ObjectRegistry. 
	 */
	public static final String RMCONTAINER_REGISTRY_INTERFACE_NAME = "txrmcontainerregistry_api";	
	
	/**
	 * Registers new resource manager container into RMContainerRepository which is 
	 * maintained from transaction service. Registered instances are used for XAResource
	 * recreation during JTA transaction recovery.  
	 *   
	 * @param rmContainerName unique name of the container
	 * @param rmContainer the RMContainer instance which will be stored into repository.	
	 * @throws IllegalArgumentException when rmContainerName was used from another container or when one 
	 * of the parameters is null. 
	 */
	public void registerRMContainer(String rmContainerName, RMContainer rmContainer)throws IllegalArgumentException;
	
	/**
	 * Removes specified RMContainer from repository. This method is called during stop of the container which are 
	 * responsible for deployment and lifecycle of resource managers. 
	 * 
	 * @param rmContainerName the name of the container which will be remove from the repository.	
	 * @throws IllegalArgumentException when specified container was not registered into this registry.
	 */
	public void unregisterRMContainer(String rmContainerName)throws IllegalArgumentException;

}
