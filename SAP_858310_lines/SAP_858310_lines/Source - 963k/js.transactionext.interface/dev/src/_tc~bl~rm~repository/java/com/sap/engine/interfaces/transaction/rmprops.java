package com.sap.engine.interfaces.transaction;

import java.util.Properties;

/**
 * This class contains information for one resource manager which will be enough to connect to RM and get XAResource instance. 
 * 
 * @author I024163
 *
 */
public class RMProps {
	
	/**
	 * Name of the resource manager. The name must be unique. 
	 */
	protected String keyName;
	
	/**
	 * Non secure properties for the specified resource manager.
	 */
	protected Properties nonSecureProperties;
	
	/**
	 * Secure properties for the specified resource manager. 
	 */
	protected Properties secureProperties;

	/**
	 * Name of the RMContainer which is responsible for this resource manager. 
	 */	
	protected String rmContainerName;
	
	/**
	 * @return the name of the resource manager. The name is unique.
	 */
	public String getKeyName() {
		return keyName;
	}

	/**
	 * Set the name of the resource manager. The name must be unique.
	 * @param keyName unique name of the resource manager.
	 */
	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	/**
	 * @return the non secure Properties for the specified resource manager.
	 */
	public Properties getNonSecureProperties() {
		return nonSecureProperties;
	}

	/**
	 * Set non secure properties for the resource manager.
	 *  
	 * @param nonSecureProperties the non secure Properties for the resource manager.
	 */
	public void setNonSecureProperties(Properties nonSecureProperties) {
		this.nonSecureProperties = nonSecureProperties;
	}

	/**
	 * @return the secure Properties for the specified resource manager.
	 */
	public Properties getSecureProperties() {
		return secureProperties;
	}

	/**
	 * Set secure properties for the resource manager. Property values will be encrypted into TLog file of DB table.  
	 * @param secureProperties the secure Properties for the specified resource manager.
	 */
	public void setSecureProperties(Properties secureProperties) {
		this.secureProperties = secureProperties;
	}
	
	public int hashCode() {
		return ((rmContainerName != null) ? rmContainerName.hashCode() : 0) +
				((keyName != null) ? keyName.hashCode() : 0);
	}	
	
	/**
	 * @return the name of the RMContainer which is responsible for this resource manager
	 */
	public String getRmContainerName() {
		return rmContainerName;
	}

	/**
	 * Sets the name of the RMContainer which is responsible for this resource manager
	 * 
	 * @param rmContainerName the name of the RMContainer which is responsible for this resource manager
	 */
	public void setRmContainerName(String rmContainerName) {
		this.rmContainerName = rmContainerName;
	}	
	
    public boolean equals(Object obj) {
    	
    	RMProps  otherRMProps = null;
    	
       if(obj == null){
    	   return false;
       }
       if(obj instanceof RMProps) {
		otherRMProps = (RMProps) obj;
	   } else {
		   return false;
	   }
       if(
    	 (rmContainerName != null && rmContainerName.equals(otherRMProps.rmContainerName))&& //rmContainer name must not be null    		   
    	 (keyName != null && keyName .equals(otherRMProps.keyName)) && // name must not be null
    	 ((nonSecureProperties != null && nonSecureProperties.equals(otherRMProps.nonSecureProperties)) || 
    			 (nonSecureProperties == null && otherRMProps.nonSecureProperties == null)) && 
    	 ((secureProperties != null && secureProperties.equals(otherRMProps.secureProperties)) || 
    			 (secureProperties == null && otherRMProps.secureProperties == null ))    	 
         ){
    	   return true;
       }
       return false;
    }		

}
