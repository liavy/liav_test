package com.sap.engine.services.webservices.jaxm.soap;

import iaik.security.ssl.KeyAndCert;
import iaik.security.ssl.SSLClientContext;
import iaik.security.ssl.SSLSocket;
import iaik.security.ssl.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;

/**
 *  
 * This is the implementation if the SSLSocketUtilInterface. See comments there
 * @see SSLSocketUtilInterface
 * @author Martijn de Boer D039113
 * 04.02.2003
 */
public class SSLUtilImpl implements SSLSocketUtilInterface {

  private SSLClientContext createSSLContext(List clientCertificateList, List serverCertificateList, boolean ignoreServerCertificates) {
        SSLClientContext sslContext = new SSLClientContext();
        sslContext.setSessionManager(new SSLWSSessionManager());
        if (clientCertificateList != null) {
      for(Iterator certIterator = clientCertificateList.iterator(); certIterator.hasNext();) {
                sslContext.addClientCredentials((KeyAndCert) certIterator.next());
        }
    }
        if (!ignoreServerCertificates) {
            sslContext.setChainVerifier(new V3ChainVerifier());
            if (serverCertificateList != null) {
        for(Iterator certIterator = serverCertificateList.iterator(); certIterator.hasNext();) {
                    sslContext.addTrustedCertificate((X509Certificate) certIterator.next());
            }
        }
        }
    return(sslContext);
    }

  public Socket createSSLSocket(String host, int port, Socket socket, List clientCertificateList, List serverCertificateList, boolean ignoreServerCertificates, boolean hasProxyAuthentication) throws IOException, UnknownHostException {
    if(hasProxyAuthentication) {
      throw new IOException("SSL over Proxy with proxy authentication failure. Missing library tc_sec_https.jar. Please see SAP Note 753002 2004");
            }
    SSLClientContext sslContext = createSSLContext(clientCertificateList, serverCertificateList, ignoreServerCertificates);
    SSLSocket sock = new SSLSocket(socket, sslContext, host, port);
    initSSLSocket(sock);
    return(sock);
        }

  private void initSSLSocket(SSLSocket sock) throws IOException {
    if (sock.getSession() != null) {
            sock.getSession().invalidate();
            sock.renegotiate();
        }
    if ("true".equals(System.getProperty("debug"))) {
            sock.setDebugStream(System.out);
    }
    }
}
