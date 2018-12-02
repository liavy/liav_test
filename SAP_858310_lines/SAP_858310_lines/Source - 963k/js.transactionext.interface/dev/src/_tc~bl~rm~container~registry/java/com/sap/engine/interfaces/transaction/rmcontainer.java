package com.sap.engine.interfaces.transaction;

import javax.transaction.xa.XAResource;

/**
 * This interface is implemented from each container that is responsible for deployment, 
 *  and lifecycle or resource managers. For example JDBC, JMS and Connector containers  
 *  provides implementation of this interface. The instance of this interface are used for 
 *  XAResource recreation during JTA transaction recovery.   
 * 
 * @author I024163
 *
 */
public interface RMContainer {
	
	/**
	 * Creates XAResource instance from specified ResourceManager. This method is used for 
	 * XAResource recreation during JTA transaction recovery.  
	 * 
	 * @param rmProps properties for the resource manager
	 * @return XAResource from specified Resource Manager.
	 * @throws RMUnavailableException if Resource Manager is not unavailable or unexpected problem occurred.  
	 */
	public XAResource createXAResource(RMProps rmProps) throws RMUnavailableException;
	
	/**
	 * This method will be called when recovery is completed and XAResource is no longer needed.
	 * All connections from resource manager that are used from this XAResource will be released.
	 *  
	 * @param xaResource the XAResource instance which is no longer needed.
	 * @throws RMUnavailableException if unexpected problem occurred.
	 */
	public void closeXAResource(XAResource xaResource)throws RMUnavailableException;

}
