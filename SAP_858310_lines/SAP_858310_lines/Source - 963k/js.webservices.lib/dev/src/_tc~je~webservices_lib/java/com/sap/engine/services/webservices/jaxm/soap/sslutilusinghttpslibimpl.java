package com.sap.engine.services.webservices.jaxm.soap;

import iaik.security.ssl.KeyAndCert;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.sap.exception.io.SAPIOException;
import com.sap.tc.logging.Location;

/**
 *  
 * This is the implementation if the SSLSocketUtilInterface. See comments there
 * @see SSLSocketUtilInterface
 * @author Martijn de Boer D039113
 * 04.02.2003
 */
public class SSLUtilUsingHttpsLibImpl implements SSLSocketUtilInterface {

    final static int HASMAP_SIZE = 128;
    private static HashMap secureConnectionCache;

    static Constructor secureConnectionConstructor;
    static Method setIgnoreServerCertificatesMethod;
    static Method createSocketMethod;
    static Boolean initialized = new Boolean(false);
    static Location LOCATION =  Location.getLocation(SSLUtilUsingHttpsLibImpl.class);
    
    public SSLUtilUsingHttpsLibImpl() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        synchronized (initialized) {
            if (!initialized.booleanValue()) {
        Class secureConnectionFactoryClass = Class.forName("com.sap.security.core.server.https.SecureConnectionFactory");
        secureConnectionConstructor = secureConnectionFactoryClass.getConstructor(new Class[]{Certificate[].class, Object[].class});
        createSocketMethod = secureConnectionFactoryClass.getMethod("createSocket", new Class[] { String.class, int.class, Socket.class });
                setIgnoreServerCertificatesMethod = secureConnectionFactoryClass.getMethod("setIgnoreServerCertificate", new Class[] { boolean.class });
                Constructor LRUHashMapConstructor = Class.forName("com.sap.security.core.server.util0.LRUHashMap").getConstructor(new Class[] { int.class });
                secureConnectionCache = (HashMap) LRUHashMapConstructor.newInstance(new Object[] { new Integer(HASMAP_SIZE)});
                initialized = new Boolean(true);
            }
        }
    }

  public Socket createSSLSocket(String host, int port, Socket socket, List clientCertificateList, List serverCertificateList, boolean ignoreServerCertificates, boolean hasProxyAuthentication) throws IOException, UnknownHostException{
        try {
            Object connectionFactory = getSecureConnectionFactory(clientCertificateList, serverCertificateList, ignoreServerCertificates);
      return((Socket)(createSocketMethod.invoke(connectionFactory, new Object[]{host, new Integer(port), socket})));
        } catch (SecurityException e) {
            throw new SAPIOException(LOCATION,"Could not create SSL socket:" + e.getClass().getName() + " " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new SAPIOException(LOCATION,"Could not create SSL socket:" + e.getClass().getName() + " " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new SAPIOException(LOCATION,"Could not create SSL socket:" + e.getClass().getName() + " " + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new SAPIOException(LOCATION,"Could not create SSL socket:" + e.getClass().getName() + " " + e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new SAPIOException(LOCATION,"Could not create SSL socket:" + e.getClass().getName() + " " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new SAPIOException(LOCATION,"Could not create SSL socket:" + e.getClass().getName() + " " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
      if(e.getTargetException() != null) {
                throw new SAPIOException(LOCATION,"Could not create SSL socket:" + e.getTargetException().getClass().getName() + " " + e.getTargetException().getMessage(), e.getTargetException());
      } else {
                throw new SAPIOException(LOCATION,"Could not create SSL socket:" + e.getClass().getName() + " " + e.getMessage(), e);
    }
        }
    }

    /**
     * The SecureConnectionFactory is used for SSL caching on IP basis
     * To prevent different client certificates to mix for 1 IP address (->wrong user on server) an 
     * instance of the SecureConnectionFactory is used per client certificate
     * @param clientCertificateList
     * @param serverCertificateList
     * @param ignoreServerCertificates
     * @return
     */
    private Object getSecureConnectionFactory(List clientCertificateList, List serverCertificateList, boolean ignoreServerCertificates) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {

        String secureConnectionFactoryCacheKey = "ignoreServerCertificates=" + ignoreServerCertificates + ";";
        if (clientCertificateList != null) {
            secureConnectionFactoryCacheKey += "ClientCert={";
            for (Iterator certIterator = clientCertificateList.iterator(); certIterator.hasNext();) {
                KeyAndCert keyAndCert = (KeyAndCert) certIterator.next();
                X509Certificate certificates[] = keyAndCert.getCertificateChain();
                for (int i = 0; i < certificates.length; i++) {
                    secureConnectionFactoryCacheKey += certificates[i].getIssuerDN().getName() + "/" + certificates[i].getSubjectDN().getName() + ";";
                }
            }
            secureConnectionFactoryCacheKey += "}";
        }
        if (!ignoreServerCertificates) {
            secureConnectionFactoryCacheKey += "TrustedCert={";
            if (serverCertificateList != null) {
                for (Iterator certIterator = serverCertificateList.iterator(); certIterator.hasNext();) {
                    X509Certificate trustedCertificate = (X509Certificate) certIterator.next();
                    secureConnectionFactoryCacheKey += trustedCertificate.getIssuerDN().getName() + "/" + trustedCertificate.getSubjectDN().getName() + ";";
                }
            }
            secureConnectionFactoryCacheKey += "}";
        }

        if (!secureConnectionCache.containsKey(secureConnectionFactoryCacheKey)) {
            Certificate[] trustedServerCertificates = null;
            if (serverCertificateList != null)
                trustedServerCertificates = (Certificate[]) serverCertificateList.toArray(new Certificate[0]);
            Object keyAndCertObjects[] = null;
            if (clientCertificateList != null) {
                keyAndCertObjects = clientCertificateList.toArray();
            }

            Object connectionFactory = secureConnectionConstructor.newInstance(new Object[] { trustedServerCertificates, keyAndCertObjects });
            //SecureConnectionFactory connectionFactory = new SecureConnectionFactory(trustedServerCertificates, keyAndCertObjects);

            setIgnoreServerCertificatesMethod.invoke(connectionFactory, new Object[] { new Boolean(ignoreServerCertificates)});
            //connectionFactory.setIgnoreServerCertificate(ignoreServerCertificates);
            //connectionFactory.setSSLDebugStream(System.err);
            secureConnectionCache.put(secureConnectionFactoryCacheKey, connectionFactory);
        }
        return secureConnectionCache.get(secureConnectionFactoryCacheKey);
    }
}
