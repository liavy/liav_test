package com.sap.engine.interfaces.transaction;

import javax.transaction.SystemException;

/**
 *  Resource Manager repository is maintained from TransactionManager and contains 
 * all ResourceManagers which are available for applications. This interface is used 
 * only from connection management system for static registration of resource managers. 
 *  Application server is responsible to detect all usages if these resource managers 
 *  and automatically enlist them into transaction.  
 * 
 * @author I024163
 *
 */
public interface RMRepository {

	
	/**
	 * The name with which the implementation of this interface is store into ObjectRegistry. 
	 */
	public static final String RMREPOSITORY_INTERFACE_NAME = "txrmrepository_api";
	
	/**
	 * Registers new resource manager into TransactionMnager repository. 
	 *  
	 * @param rmName the name of the resource manager. This name must be unique. 
	 * @param rmProps properties for the resource manager which are enough for XAResoruce 
	 * recreation.
	 * @return unique id of the resource manager which will be used during enlistment of 
	 * XAResources from specified RM. This id is positive and valid only during current run of the 
	 * transaction manager. 
	 * @throws IllegalArgumentException when one of the parameters is null or resource manager 
	 * name is not unique.  
	 * @throws SystemException when an unexpected error condition occurred.
	 */
	public int addRM (String rmName, RMProps rmProps) throws IllegalArgumentException, SystemException;
	
	/**
	 * Returns the id of specified resource manager.
	 * 
	 * @param rmName the name of resource manager
	 * @return unique id of the resource manager which will be used during enlistment of 
	 * XAResources from specified RM. This id is valid only during current run of the 
	 * transaction manager.
	 * @throws SystemException when an unexpected error condition occurred.
	 */
	public int getRMid(String rmName)throws SystemException;
	
	/**
	 * Unregisters resource manager with specified ID. 
	 * 
	 * @param rmID the ID of the resource manager which will be unregistered. 
	 * @throws IllegalArgumentException
	 * @throws SystemException when an unexpected error condition occurred.
	 */
	public void removeRM(String rmID) throws IllegalArgumentException, SystemException;
	
}
