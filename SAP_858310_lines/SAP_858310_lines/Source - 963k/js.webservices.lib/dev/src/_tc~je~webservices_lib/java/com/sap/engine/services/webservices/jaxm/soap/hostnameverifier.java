package com.sap.engine.services.webservices.jaxm.soap;

/**
 * HostnameVerifier provides a callback mechanism so that 
 * implementers of this interface can supply a policy 
 * for handling the case where the host to connect to and 
 * the server name from the certificate mismatch. 
 * 
 *  <p>Copyright (c) 2003 SAP AG.
 */
public interface HostnameVerifier {

	/**
	 * Compares the hostname with the name from server certificate.
	 * 
	 * @param urlHostname the hostname 
	 * @param certHostname the name from server certifiate
	 * 
	 * @return true if the hostname is acceptable or if the connection should be established anyway.
	 */
	public boolean verify(String urlHostname, String certHostname) ;


}
