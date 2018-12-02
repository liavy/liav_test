package com.sap.archtech.archconn.servicereg;

/**
 * Interface for accessing the configuration service provided by SAP AS Java.
 * Note, the configuration service cannot be invoked directly by the XMLDAS Connector since this
 * would imply an undesired reference between a Primary Library and a Primary Service. 
 */
public interface IConfigurationService 
{
	public void loadArchconnConfig(boolean isFirstTime);
	public String getArchDestination();
	public int getUpdateFreq();
	public int getConnTimeout();
	public int getExpTimeout();
	public int getReadTimeout();
}
