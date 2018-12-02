package com.sap.engine.services.webservices.jaxm.soap;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

/**
 * This is an interface to separate the interface/implementation. 
 * The interface is referenced in the HTTPSocket class and the implementation is instantiated by Class.forName().
 * Using this approach, no direct references of IAIK are used and the HTTPSocket class is usable, even when the IAIK classes are not available.
 * 
 * @author Martijn de Boer D039113
 * 04.02.2003
 */
public interface SSLSocketUtilInterface {

  public Socket createSSLSocket(String host, int port, Socket socket, List clientCertificateList, List serverCertificateList, boolean ignoreServerCertificates, boolean hasProxyAuthentication) throws IOException, UnknownHostException;
}
