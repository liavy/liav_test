package com.sap.engine.services.ts.recovery;

import java.util.Hashtable;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import com.sap.engine.interfaces.transaction.RMContainer;
import com.sap.engine.interfaces.transaction.RMContainerRegistry;
import com.sap.engine.interfaces.transaction.RMUnavailableException;
import com.sap.engine.services.ts.tlog.RMPropsExtension;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.SimpleLogger;

/**
 * This is an implementation of RMcontainerRegistry interface. This class will keep a 
 * cache with all RMContainers which are registered. Additionally this class will send notifications 
 * to RecoveryManager when a new container is registered. 
 * 
 * @author I024163
 */
public class RMContainerRegistryImpl implements RMContainerRegistry {

	private static final Location LOCATION = Location.getLocation(RMContainerRegistryImpl.class);	
	/**
	 * This is the actual registry which contains all registered RMContainers. 
	 */
	private Hashtable<String,RMContainer> theRegistry = null;
	
	/**
	 * This listener is submitted from RecoveryManager and will be notified when new RMContainer is registered.
	 * The listener will not be notified for already registered RMContainers. RecoveryManager will request them 
	 * using method getRMContainerByName();  
	 */
	private RMContainerRegistryListener theListener = null;
	
	
	/**
	 * Default constructor of the class is used.
	 */
	public RMContainerRegistryImpl(){
		theRegistry = new Hashtable<String,RMContainer>();	  
	}

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
	public void registerRMContainer(String rmContainerName, RMContainer rmContainer)throws IllegalArgumentException {
		theRegistry.put(rmContainerName, rmContainer);
		
		if(theListener != null){
			theListener.newRMContainerRegistered(rmContainerName, rmContainer);
		}		
	}
	
	/**
	 * Removes specified RMContainer from repository. This method is called during stop of the container which are 
	 * responsible for deployment and lifecycle of resource managers. 
	 * 
	 * @param rmContainerName the name of the container which will be remove from the repository.	
	 * @throws IllegalArgumentException when specified container was not registered into this registry.
	 */
	public void unregisterRMContainer(String rmContainerName)throws IllegalArgumentException {
		theRegistry.remove(rmContainerName);
	}

    /**
     * Events for all RMContainers which will be registered will be forwarded imediately to this listener.
     * 
     * @param listener
     * @throws IllegalArgumentException if another listener is already registered. Registration of more
     * listeners is not supported.
     */
    public void registerListener(RMContainerRegistryListener listener)throws IllegalArgumentException{
    	if(theListener != null && theListener != listener){
    		throw new IllegalArgumentException("Another is already registered. Registration of more listeners is not supported");
    	}
    	theListener = listener;
    }

    /**
     * returns a RMContainer from repository or null if RMContainer with this name was not registered. 
     * 
     * @param rmContainerName the name of the RMContainer. 
     * @return RMcontainer with specified name or null if such RMContianer does not exist into registry.
     * 
     */
    public RMContainer getRMContainerByName(String rmContainerName){
    	return theRegistry.get(rmContainerName);
    }
    
    /**
     * Used from recovery process and during resolving of pending transactions.
     * 
     * @param rmProperties array with properties for all resource managers that must be recovered.
     * @return true if all ResourceManager are available otherwise false.
     */
    public boolean fillRMPropertiesWithXAResources(RMPropsExtension[] rmProperties) { 
    	
    	boolean result = true;
    	for(RMPropsExtension rmProps : rmProperties){
    		if(rmProps.getXAResource() == null){
        		RMContainer rmContainer = theRegistry.get(rmProps.getRmContainerName());
        		if(rmContainer != null){
    				XAResource xaResourceForRecovery = null;
					try {
						xaResourceForRecovery = rmContainer.createXAResource(rmProps);
					} catch (RMUnavailableException e) {
						int errorCode = e.getErrorCode();
						if(errorCode == RMUnavailableException.RM_UNDEPLOYED){
							SimpleLogger.traceThrowable(Severity.INFO, LOCATION, "ResourceManager "+rmProps.getKeyName()+" is not available. TransactionManager is not able to recover its transactions", e);
							rmProps.rmUnreachable = true;
						} else {
							SimpleLogger.traceThrowable(Severity.INFO, LOCATION, "ResourceManager "+rmProps.getKeyName()+" is temporary unavailable.", e);
							result = false;
						}
					}
					if(xaResourceForRecovery == null){ 
						SimpleLogger.trace(Severity.INFO, LOCATION, "ResourceManager "+rmProps.getKeyName()+" is unreachable because returned instance from " + rmContainer + ".createXAResource(...) is null.");
						result = false;
					}
					
    				rmProps.setXAResource(xaResourceForRecovery);
        		}
    		}
    	}
    	//Only one RM is used if there are more same RM-s 
    	for (int i=0; i<rmProperties.length; i++){
    		if(rmProperties[i].getXAResource() != null && !rmProperties[i].ignoredBecauseAnotherSameRM){
	    		for(int j=i+1; j<rmProperties.length; j++){
	    			if(rmProperties[j].getXAResource()!= null && !rmProperties[j].ignoredBecauseAnotherSameRM){	    
	    				try{
		    				if( rmProperties[j].getXAResource().isSameRM(rmProperties[i].getXAResource())||
		    						rmProperties[i].getXAResource().isSameRM(rmProperties[j].getXAResource())){
		    					
		    					if(rmProperties[i].recoveredSuccessfully){
		    						rmProperties[i].ignoredBecauseAnotherSameRM = true; 
		    					} else {
		    						rmProperties[j].ignoredBecauseAnotherSameRM = true;
		    					}
		    				}
	    				} catch (XAException xae){
	    					// assume that RM-s are not the same
	    					SimpleLogger.traceThrowable(Severity.ERROR, LOCATION,xae,"ASJ.trans.000100", "Unexpected Erception occured during isSameRM operation of ResourceManager. Probably some transactions will not be recovered successfully.");
	    				}
	    			}
	    		}
    		}
    	}
    	return result;
    }
    
}
