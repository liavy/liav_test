/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import com.sap.engine.interfaces.webservices.runtime.HTTPProxyResolver;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientHTTPTransport;
import com.sap.engine.services.webservices.jaxm.soap.HTTPSocket;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.PropertyContext;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.features.*;

/**
 * HTTP Transport : used to transport SOAP over http.
 * Has convinient way for setting and getting
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class HTTPTransport implements HTTPTransportInterface {

	public static final String REQUEST_METHOD_POST = "POST";
	public static final String REQUEST_METHOD_GET = "GET";
  // Use Logging property to set log stream for in/out clear property to stop logging.
  public static final String REQUEST_LOGGING_PROPERTY = "RequestLogging";
  public static final String RESPONSE_LOGGING_PROPERTY = "ResponseLogging";
  public static final String HTTP_PROXY_RESOLVER_PROPERTY = "HTTPProxyResolver"; //HTTP Proxy configured in Visual Admin
  public static final String GLOBAL_CLIENT_SETTINGS_PROPERTY = "GlobalClientSettings"; //Global WSClient settings configured in Visual Admin 
  
	private String endpointURL;
  private URL oldURL = null; // Original Endpoint URL
  private String oldProxy = null;
  private String oldProxyPort = null;
	private String requestMethod;
	private PropertyContext config;
	private HTTPSocket httpSocket = null;
	private Hashtable headers;

	public HTTPTransport() {
		config = new PropertyContext();
		headers = new Hashtable();
	}

	/**
	 *
	 */
	public void init(String endpoint, String requestMethod, PropertyContext config) throws Exception {
		headers.clear();
		this.endpointURL = endpoint;
		this.requestMethod = requestMethod;
    if (config != null) {
      this.config = config;
    }
	}



  public String getEndpoint() {
    return this.endpointURL;
  }

  public void setEndpoint(String endpoint) throws Exception {
    this.endpointURL = endpoint;
  }

	/**
	 * Returns feature properties.
	 * Use with caution this method never returns NULL but may retirn empty configuration.
	 */
	public PropertyContext getFeature(String featureName) {
		return config.getSubContext(featureName);
	}

	/**
	 * Creates new HTTP Socket for message transportation.
	 */
	private HTTPSocket createHTTPSocket(String endpoint, String requestMethod) throws Exception {
		URL url = null;
		/**
     * This is a little bit complicated, as the URL class doesn't know https and a protocol handler can only be set _once_ for the whole JVM, which should be prevented in a server
		 */
		if (endpoint.toLowerCase(Locale.ENGLISH).startsWith("https")) {
			String httpEndpoint = "http" + endpoint.substring(5);
			URL tmpUrl = new URL(httpEndpoint);
			URLStreamHandler httpsHandler = null;
			try {
				httpsHandler = (URLStreamHandler) Class.forName("iaik.protocol.https.Handler").newInstance();
			} catch (Exception ex) {
				throw new Exception("Could not create https handler:" + ex.getClass().getName()+" "+ex.getMessage());
			}
			url = new URL("https", tmpUrl.getHost(), tmpUrl.getPort(), tmpUrl.getFile(), httpsHandler);
		} else {
			url = new URL(endpoint);
    }
    boolean createNew = true;
    if (url.equals(oldURL) && httpSocket != null && httpSocket.isAlive()) {
      String proxyHost = null;
      String proxyPort = null;
      if (featureSet(ProxyFeature.PROXY_FEATURE)) { // HTTP Proxy feature
        PropertyContext feature = getFeature(ProxyFeature.PROXY_FEATURE);
        proxyHost = (String) feature.getProperty(ProxyFeature.PROXY_HOST_PROPERTY);
        if (proxyHost != null) {
          proxyPort = (String) feature.getProperty(ProxyFeature.PROXY_PORT_PROPERTY);
          if (proxyPort == null) {
            throw new Exception("ProxyHost found, but proxyPort is null");
          }
        }
      }
      if (proxyHost != null && proxyHost.equals(oldProxy) && proxyPort.equals(oldProxyPort)) {
      createNew = false;
    }
      if (proxyHost == null && oldProxy == null) {
        createNew = false;
      }
    }
    if (createNew) {
      if (httpSocket != null) {
        httpSocket.disconnect();
        httpSocket = null;
      }
      httpSocket = new HTTPSocket(url);
      boolean keepAliveFlag = false;
      httpSocket.setRequestMethod(requestMethod);
      if (featureSet(HTTPKeepAliveFeature.KEEP_ALIVE_FEATURE)) { // Keep-Alive feature
        PropertyContext feature = getFeature(HTTPKeepAliveFeature.KEEP_ALIVE_FEATURE);
        String keepAlive = (String) feature.getProperty(HTTPKeepAliveFeature.KEEP_ALIVE_PROPERTY);
        if (keepAlive != null && keepAlive.length() != 0) {
          keepAliveFlag = true;
        }
      }
      if (keepAliveFlag == false) {
        httpSocket.setHeader("Connection", "close");
      }
      if (featureSet(HTTPKeepAliveFeature.COMPRESS_RESPONSE_FEATURE)) { // Compress response feature
        PropertyContext feature = getFeature(HTTPKeepAliveFeature.COMPRESS_RESPONSE_FEATURE);
        String compressProp = (String) feature.getProperty(HTTPKeepAliveFeature.COMPRESS_RESPONSE_PROPERTY);
        if ("true".equalsIgnoreCase(compressProp)) {
          httpSocket.setHeader("Accept-Encoding", "gzip"); //TODO: Check compatibility
        }
      }
	    Object inputLogger = config.getProperty(RESPONSE_LOGGING_PROPERTY); //SASHO
	    Object outputLogger = config.getProperty(REQUEST_LOGGING_PROPERTY);
	    if (inputLogger != null || outputLogger != null) {
	      OutputStream inputLog;
	      OutputStream outputLog;
	      try {
	        inputLog = (OutputStream) inputLogger;
	        outputLog = (OutputStream) outputLogger;
	      } catch (ClassCastException cce) {
	        throw new Exception("OutputStream expected for REQUEST_LOGGING_PROPERTY and RESPONSE_LOGGING_PROPERTY: " + cce.toString());
	      }
	        httpSocket.setLogger(inputLog, outputLog);
	    }
      boolean preconfiguredTimeout = false;
      if (featureSet(TimeoutFeature.TIMEOUT_FEATURE)) { // Socket timeout feature
	      PropertyContext feature = getFeature(TimeoutFeature.TIMEOUT_FEATURE);
	      String timeOut = (String) feature.getProperty(TimeoutFeature.SO_TIMEOUT);
	      if (timeOut != null) {
	        try {
	          int realTimeout = Integer.parseInt(timeOut);
	          httpSocket.setSocketTimeout(realTimeout);          
	        } catch (NumberFormatException x) {
	          httpSocket.setSocketTimeout(HTTPSocket.DEFAULT_SOCKET_TIMEOUT);
	        }
	        preconfiguredTimeout = true;
	      }              
      }
      if (!preconfiguredTimeout) {
        int timeout = HTTPSocket.DEFAULT_SOCKET_TIMEOUT;
        httpSocket.setSocketTimeout(timeout);
      }
      boolean preconfiguredProxy = false;
      oldProxy = null;
      oldProxyPort = null;
      String proxyHost = null;
      String proxyPort = null;
      if (featureSet(ProxyFeature.PROXY_FEATURE)) { // HTTP Proxy feature
        PropertyContext feature = getFeature(ProxyFeature.PROXY_FEATURE);
        proxyHost = (String) feature.getProperty(ProxyFeature.PROXY_HOST_PROPERTY);
        if (proxyHost != null) {
          proxyPort = (String) feature.getProperty(ProxyFeature.PROXY_PORT_PROPERTY);
          if (proxyPort == null) {
            throw new Exception("ProxyHost found, but proxyPort is null");
          }
          int port;
          try {
            port = Integer.parseInt(proxyPort);
          } catch (Exception e) {
            throw new Exception("Transport configuration exception: " + e);
          }
          String proxyUser = (String) feature.getProperty(ProxyFeature.PROXY_USERNAME);
          String proxyPass = (String) feature.getProperty(ProxyFeature.PROXY_PASSWORD);
          httpSocket.setProxy(proxyHost, port, proxyUser, proxyPass);
          preconfiguredProxy = true;
        }
      }
      if (!preconfiguredProxy) {
        HTTPProxyResolver proxyResolver = (HTTPProxyResolver) config.getProperty(HTTP_PROXY_RESOLVER_PROPERTY);
        httpSocket.setHTTPProxyResolver(proxyResolver);
        }
      copyClientCertificates(config, httpSocket); // process security information
      this.oldURL = url;
      oldProxy = proxyHost;
      oldProxyPort = proxyPort;
    } else {
      // Reused socket also needs to reset theese settings
      httpSocket.setRequestMethod(requestMethod);
      //httpSocket.setHeader("Connection", "close");
      if (featureSet(HTTPKeepAliveFeature.COMPRESS_RESPONSE_FEATURE)) { // Compress response feature
        PropertyContext feature = getFeature(HTTPKeepAliveFeature.COMPRESS_RESPONSE_FEATURE);
        String compressProp = (String) feature.getProperty(HTTPKeepAliveFeature.COMPRESS_RESPONSE_PROPERTY);
        if ("true".equalsIgnoreCase(compressProp)) {
          httpSocket.setHeader("Accept-Encoding", "gzip"); //TODO: Check compatibility
        }
      }
      Object inputLogger = config.getProperty(RESPONSE_LOGGING_PROPERTY); //SASHO
      Object outputLogger = config.getProperty(REQUEST_LOGGING_PROPERTY);
      if (inputLogger != null || outputLogger != null) {
        OutputStream inputLog;
        OutputStream outputLog;
        try {
          inputLog = (OutputStream) inputLogger;
          outputLog = (OutputStream) outputLogger;
        } catch (ClassCastException cce) {
          throw new Exception("OutputStream expected for REQUEST_LOGGING_PROPERTY and RESPONSE_LOGGING_PROPERTY: " + cce.toString());
      }
      httpSocket.setLogger(inputLog, outputLog);
    }

    }
		return this.httpSocket;
	}

//	private static String encodeAuth(String userName, String password) {
//		byte[] result = com.sap.engine.lib.xml.util.BASE64Encoder.encode((userName + ":" + password).getBytes());
//		return new String(result, 0, result.length);
//	}
	

	/**This method copies the client, server certificates to the HTTPSocket
	 *
	 * @param config
	 * @param socket
	 */
	private void copyClientCertificates(PropertyContext config, HTTPSocket socket) {
		/**
		 * pass over client certificates
		 */
		if (config.getSubContext(AuthenticationFeature.AUTHENTICATION_FEATURE) != null) {
			PropertyContext authenticationContext = config.getSubContext(AuthenticationFeature.AUTHENTICATION_FEATURE);

			String authenticationMethod = (String) authenticationContext.getProperty(AuthenticationFeature.AUTHENTICATION_METHOD);
			/**
			 * pass on any client certificates
			 */
			if (AuthenticationFeature.METHOD_CERTIFICATE.equals(authenticationMethod));
			{
				List clientCertificateList = (List) authenticationContext.getProperty(AuthenticationFeature.AUTHENTICATION_CLIENT_CERT_LIST);
				socket.setClientCertificateList(clientCertificateList);
			}
			/**
			 * ssl server certificate behaviour
			 */
			Boolean ignoreServerCerts = (Boolean) authenticationContext.getProperty(AuthenticationFeature.AUTHENTICATION_SERVER_IGNORE_CERTS);

			if (ignoreServerCerts != null) {
				socket.setIgnoreServerCertificates(ignoreServerCerts.booleanValue());
				if (!ignoreServerCerts.booleanValue()) {
					List serverCertificateList = (List) authenticationContext.getProperty(AuthenticationFeature.AUTHENTICATION_SERVER_CERT_LIST);
					socket.setServerCertificateList(serverCertificateList);
				}
			}
		}

		if (featureSet(AuthenticationFeature.AUTHENTICATION_FEATURE)) { // HTTP Authorization feature
			PropertyContext feature = getFeature(AuthenticationFeature.AUTHENTICATION_FEATURE);
			String authenticationMethod = (String) feature.getProperty(AuthenticationFeature.AUTHENTICATION_METHOD);
			if (AuthenticationFeature.METHOD_BASIC.equals(authenticationMethod)) {
				String userName = (String) feature.getProperty(AuthenticationFeature.AUTHENTICATION_CREDENTIAL_USER);
				String password = (String) feature.getProperty(AuthenticationFeature.AUTHENTICATION_CREDENTIAL_PASSWORD);
				if (userName != null) {
					socket.setHeader("Authorization", ClientHTTPTransport.encodeAuth(userName, password));
				}
			}
		}
	}


	/**
	 * Returns request stream. Must set headers before call get output stream.
	 * Call flush to this stream before getting response input stream.
	 */
	public OutputStream getRequestStream() throws Exception {
    createHTTPSocket(this.endpointURL,this.requestMethod);
		// Session Feature implementation
		if (featureSet(SessionFeature.SESSION_FEATURE)) {
			PropertyContext feature = getFeature(SessionFeature.SESSION_FEATURE);
			String sessionMethod = (String) feature.getProperty(SessionFeature.SESSION_METHOD_PROPERTY);
			if (SessionFeature.HTTP_SESSION_METHOD.equals(sessionMethod)) { //httpCookies
				boolean maintainSession = true;
				if (SessionFeature
					.USE_SESSION_FALSE
					.equals(feature.getProperty(SessionFeature.HTTP_MAINTAIN_SESSION))) {
					maintainSession = false;
					feature.setProperty(SessionFeature.HTTP_MAINTAIN_SESSION, SessionFeature.USE_SESSION_TRUE);
				}

        Object cookieObject = feature.getProperty(SessionFeature.SESSION_COOKIE_PROPERTY);
        if (cookieObject != null) {
          ArrayList cookies = (ArrayList) cookieObject;
          for (int i=0; i<cookies.size(); i++) {
            String line = Cookie.getAsRequestString((Cookie) cookies.get(i));
            httpSocket.setHeader("Cookie",line);
          }
          if (maintainSession == false) { // Open new session
            feature.clearProperty(SessionFeature.SESSION_COOKIE_PROPERTY);
            httpSocket.setHeader(SessionFeature.HTTP_MAINTAIN_SESSION, SessionFeature.USE_SESSION_FALSE);
          }
        }
			}
		}
		Enumeration headerkeys = headers.keys();
		while (headerkeys.hasMoreElements()) { // HTTP Headers
			String headerName = (String) headerkeys.nextElement();
			String[] headerValue = (String[]) headers.get(headerName);
      for (int i=0; i<headerValue.length; i++) {
			  httpSocket.setHeader(headerName, headerValue[i]);
      }
		}
		return httpSocket.getOutputStream();
	}

	/**
	 * Returns response code of HTTP Response.
	 */
	public int getResponseCode() throws Exception {
		return httpSocket.getResponseCode();
	}

  	/**
	 * Returns response input stream.
	 */
	public InputStream getResponseStream() throws IOException {
		headers.clear();
		Enumeration answerheaders = httpSocket.getHeaderNames();
		while (answerheaders.hasMoreElements()) {
			String headerName = (String) answerheaders.nextElement();
			String[] headerValue = httpSocket.getHeader(headerName);
			headers.put(headerName, headerValue);
		}
		if (featureSet(SessionFeature.SESSION_FEATURE)) {
			PropertyContext feature = getFeature(SessionFeature.SESSION_FEATURE);
			String sessionMethod = (String) feature.getProperty(SessionFeature.SESSION_METHOD_PROPERTY);
			if (SessionFeature.HTTP_SESSION_METHOD.equals(sessionMethod)) {
				boolean maintainSession = true;
				if (SessionFeature
					.USE_SESSION_FALSE
					.equals(feature.getProperty(SessionFeature.HTTP_MAINTAIN_SESSION))) {
					maintainSession = false;
					feature.setProperty(SessionFeature.HTTP_MAINTAIN_SESSION, SessionFeature.USE_SESSION_TRUE);
				}
				String[] cookies = httpSocket.getHeader("Set-Cookie");
				if (cookies != null && cookies.length > 0) { // Server returns cookie
          // Get current cookies
          Object content = feature.getProperty(SessionFeature.SESSION_COOKIE_PROPERTY);
          //Object content = null;
					for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].indexOf("sap-contextid=") != -1) {
              int pos = cookies[i].indexOf("sap-contextid=");
              if (cookies[i].indexOf(";",pos) != -1) {
                //content = new String[] {cookies[i].substring(pos, cookies[i].indexOf(";",pos))};
                if ("sap-contextid=0".equals(content)) {
                  content = null;
                  break;
                }
              } else {
                //content = new String[] {cookies[i].substring(pos)};
                if ("sap-contextid=0".equals(content)) {
                  content = null;
                  break;
                }
              }
            }
            ArrayList cookieLine = Cookie.readCookies(cookies[i]);
            for (int j=0; j<cookieLine.size(); j++) {
              if (content == null) {
                content = new ArrayList();
              }
              Cookie.updateCookies(cookieLine,(ArrayList) content);
            }
					}
					if (maintainSession == false) { // Server maintains session
						//feature.clearProperty(SessionFeature.SESSION_COOKIE_PROPERTY);
					} else {
						if (content != null) {
							feature.setProperty(SessionFeature.SESSION_COOKIE_PROPERTY, content);
						}
					}
				}
			}
		}
		return httpSocket.getInputStream();
	}

	/**
	 * Call this to release open inputStream/OutputStream and sockets.
	 */
	public void closeSession() throws Exception {
		//this.permInputStream.close();
		//this.permOutputStream.close();
    if (httpSocket != null) {
		  httpSocket.disconnect();
      httpSocket = null;
    }
	}

	/**
	 * Returns true is some trabsport feature is set.
	 */
	public boolean featureSet(String featureName) {
		PropertyContext context = getFeature(featureName);
		return context.isDefined();
	}

	/**
	 * Returns header value.
	 */
	public String[] getHeader(String headerName) {
    //Object header = headers.get(headerName);
		return (String[]) headers.get(headerName);
	}

	/**
	 * Returns hashtable with http headers.
	 */
	public Hashtable getHeaders() {
		return this.headers;
	}

  public Enumeration listHeaders() {
    return headers.keys();
  }

  public void setHeader(String headerName, String[] headerValues) {
    headers.put(headerName, headerValues);
  }

	public void setHeader(String headerName, String headerValue) {
		headers.put(headerName, new String[] {headerValue});
	}

	public String getContentType() throws Exception {
		return this.httpSocket.getContentType();
	}

	public String getResponseMessage() throws Exception {
		return this.httpSocket.getResponseMessage();
	}

  public void setSocketTimeoutFast(int timeout) throws SocketException {
    if (this.httpSocket != null) {
      this.httpSocket.setSoTimeoutFast(timeout);
    }
  }

  public void resetSocketTimeoutFast() throws SocketException {
    if (this.httpSocket != null) {
      this.httpSocket.resetSoTimeoutFast();
    }
  }

  protected void  finalize() {
    try {
      super.finalize();
    } catch (Throwable x) {
      //$JL-EXC$
    }
    if (this.httpSocket != null) {
      try {
        httpSocket.disconnect();
      } catch (Throwable t) {
        httpSocket = null;
        //$JL-EXC$
      }
    }
  }
}
