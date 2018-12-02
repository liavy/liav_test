package com.sap.engine.services.ts.recovery;

import com.sap.engine.interfaces.transaction.RMContainer;

/**
 * This interface will be implemented from RecoveryManager in order to receive events when a 
 * new RMContainer is registered. 
 * 
 * @author I024163
 *
 */
public interface RMContainerRegistryListener {
	
	/**
	 * This event will be send from RMContainerRegistryImpl when a new RMContainer is registered.
	 *  
	 * @param rmContainerName the name of the registered RMContainer.
	 * @param rmContainer the RMContainer instance which was registered. 
	 */
	public void newRMContainerRegistered(String rmContainerName, RMContainer rmContainer);

}
