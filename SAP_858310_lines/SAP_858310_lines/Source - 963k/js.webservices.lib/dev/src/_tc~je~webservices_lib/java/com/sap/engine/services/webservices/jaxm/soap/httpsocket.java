package com.sap.engine.services.webservices.jaxm.soap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import com.sap.engine.interfaces.webservices.client.WSConnection;
import com.sap.engine.interfaces.webservices.client.WSConnectionFactory;
import com.sap.engine.interfaces.webservices.runtime.HTTPProxy;
import com.sap.engine.interfaces.webservices.runtime.HTTPProxyResolver;
import com.sap.engine.lib.xml.parser.helpers.CharArray;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientHTTPTransport;
import com.sap.engine.services.webservices.tools.ChunkedInputStream;
import com.sap.engine.services.webservices.tools.GZIPInputStreamEOF;
import com.sap.tc.logging.Location;

/**
 *  The <code>HTTPSocket</code> represents a wrapper for the <code>java.net.Socket</code> class. It adds several
 * methods which support handling of HTTPConnections - proxy support, managing of headers, response code and message
 * exctraction. It is something like HttpUrlConnection, but the latter had problems, when setting proxies
 * 
 * 
 * @author Vladimir Savchenko, vladimir.savchenko@sap.com
 *  
 */
public class HTTPSocket {
  private static final String SAP_PASSPORT = "SAP-PASSPORT";
  public static final int DEFAULT_SOCKET_TIMEOUT = 60 * 1000;
  public static final int DEFAULT_SOCKET_CONNECTION_TIMEOUT = 0;
  public static final String CONNECTION_HEADER = "connection";
  public static final String CONNECTION_CLOSED = "close";

  private static final int PROXY_INITIAL_READ_TIMEOUT = 30000;

  public static HTTPProxyResolver PROXY_RESOLVER; //Set by WSContainer upon start up...

  public static boolean onServer = false;
  private OutputStream logInputStream = null;
  private OutputStream logOutputStream = null;
  private Socket sock = null;
  private WSConnection wsConnection;
  private InputStream connectionInputStream;
  private OutputStream connectionOutputStream;

  private boolean keepAlive = true; // Keep Alive flag
  private boolean connectionOpen = false; // Connection open flag
  private boolean connectionFailed = false; // Connection failed flag
  private String host = null;
  private String portStr = null;
  private int port = -1;
  private String file = null;
  private String proxyHost = null;
  private int proxyPort = -1;
  private String proxyUser;
  private String proxyPass;
  private HTTPProxyResolver proxyResolver;
  private Hashtable headers = new Hashtable();
  private Hashtable headerNames = new Hashtable();
  private String lastPutHeader;
  private BufferedInputStream inputStream = null;
  private BufferedOutputStream outputStream = null;
  private boolean bSentHeaders = false;
  private boolean bReadHeaders = false;
  private CharArray buffer = new CharArray(100);
  private int responseCode = -1;
  private String responseString = null;
  private String requestMethod = null;
  private boolean secure = false; //SASHO
  private int socketReadTimeout = DEFAULT_SOCKET_TIMEOUT; // Chavdar
  private int socketConnectionTimeout;
  
  /** Classes & methods needed by the sap passport */
  private static Class<?> httpHandlerClass;
  private static Method beforeSendingRequest;
  private static Method afterReceivingResponse;  
  private final static int PASSPORT_HTTP_FLAG = 0;
  private final static int PASSPORT_HTTPS_FLAG = 1; 
  
  
  
  /** Logger instance */
  private Location classLogger = Location.getLocation(HTTPSocket.class);
  
  /** Passport needed classes */
  
  
  static {
    try {
      httpHandlerClass = Class.forName("com.sap.jdsr.shared.httpapi.HttpHandler");
           
      beforeSendingRequest = httpHandlerClass.getMethod("beforeSendingRequest", new Class[]{int.class, String.class, String.class, int.class, String.class});
      
      afterReceivingResponse = httpHandlerClass.getMethod("afterReceivingResponse", new Class[]{});
    } catch (Exception e) {
      //$JL-EXC$
      //Do nothing. Check later if the classes are resolved.
    }
  }

  /**
   * Sets client socket timeout. The default value is 60 seconds.
   * @param timeout
   */
  public void setSocketTimeout(int socketReadTimeout) {
    this.socketReadTimeout = socketReadTimeout;
  }

  /**
   * Gets current client socket timeout.
   * @return
   */
  public int getSocketTimeout() {
    return(socketReadTimeout);
  }
  
  public void setSocketConnectionTimeout(int socketConnectionTimeout) {
    this.socketConnectionTimeout = socketConnectionTimeout;
  }
  
  public int setSocketConnectionTimeout() {
    return(socketConnectionTimeout);
  }

  /**
   * Sets Socket logger
   * @param logInputStream logOutputStream
   */
  public void setLogger(OutputStream logInputStream, OutputStream logOutputStream) {
    this.logInputStream = logInputStream;
    this.logOutputStream = logOutputStream;
  }

  public OutputStream getInputLogger() {
    return this.logInputStream;
  }

  public OutputStream getOutputLogger() {
    return this.logOutputStream;
  }

  /**
   * List of client certificates for authentication
   */
  List clientCertificateList;
  /**List of accepted server certificates
   * */
  List serverCertificateList;
  /**Boolean to indicate, if server certificates should be ignored.
   * Default: true;
   * */
  boolean ignoreServerCertificates = true;

  /**
   * Construct the object with an URL
   * 
   * @param   url  the url specifying the Server, Port, File to read and write to
   */
  public HTTPSocket(URL url) {
    if ("https".equals(url.getProtocol())) {
      secure = true;
    }

    host = url.getHost();
    portStr = Integer.toString(url.getPort());
    port = url.getPort();
    file = url.getFile();
    //System.out.println("HTTPSocket: " + port+" "+url);
  }

  /**
   * Construct a new HTTPSocket object
   * 
   * @param   host  the host name
   * @param   port  the port
   * @param   file  the file on the server which will be read (for example /myservlet)
   */
  public HTTPSocket(String host, int port, String file) {
    this.host = host;
    this.portStr = Integer.toString(port);
    this.port = port;
    this.file = file;
  }

  /**
   * Set the proxy which will be used to connect to the host
   * 
   * @param   host  the host of the proxy
   * @param   port  the port
   */
  public void setProxy(String host, int port) {
    setProxy(host, port, null, null);
  }

  public void setProxy(String host, int port, String proxyUser, String proxyPass) {
    this.proxyHost = host;
    this.proxyPort = port;
    this.proxyUser = proxyUser;
    this.proxyPass = proxyPass;
  }

  public void setHTTPProxyResolver(HTTPProxyResolver proxyResolver) {
    this.proxyResolver = proxyResolver;
  }

  /**
   * Set a header to the request. The header is converted to lower-case, and then saved to headers.
   * The headers hashtable holds a String[] for each header, so you might have several values for a header
   * The connection is http keep alive. If the connection:close header is set then the connections is closed
   * by the server after one request.
   * 
   * @param   headerName  the header to set
   * @param   value   the value for this header
   */
  public void setHeader(String headerName, String value) {
    String header = headerName.toLowerCase(Locale.ENGLISH);
    headerNames.put(header, headerName);
    if (CONNECTION_HEADER.equalsIgnoreCase(header.trim()) && CONNECTION_CLOSED.equalsIgnoreCase(value.trim())) {
      this.keepAlive = false;
    }
    String[] hh = (String[]) headers.get(header);
    String[] newhh = null;

    if (hh == null) {
      newhh = new String[1];
      newhh[0] = value;
    } else {
      newhh = new String[hh.length + 1];
      System.arraycopy(hh, 0, newhh, 0, hh.length);
      newhh[hh.length] = value;
    }

    lastPutHeader = header;
    headers.put(header, newhh);
  }

  private void setHeaderInternal(String headerName, String value) {
    String header = headerName.toLowerCase(Locale.ENGLISH);
    headerNames.put(header, headerName);
    String[] hh = (String[]) headers.get(header);
    hh[hh.length - 1] = hh[hh.length - 1] + value;
    headers.put(header, hh);
  }

  /**
   * Get the values corresponding to this header.
   * 
   * @param   header  the header to be queried. It is converted to lower-case before proceeding
   * @return a String[] with all the values set to this header
   */
  public String[] getHeader(String header) {
    header = header.toLowerCase(Locale.ENGLISH);
    return (String[]) headers.get(header);
  }

  /**
   * Removes header from the message headers
   * @param headerName
   */
  public void clearHeader(String headerName) {
    headerName = headerName.toLowerCase(Locale.ENGLISH);
    headers.remove(headerName);
  }

  /**
   * Returns all HTTP Header names.
   * @return
   */
  public Enumeration getHeaderNames() {
    return this.headers.keys();
  }

  public Hashtable getHeaders() {
    return headers;
  }

  /**
   * Retreive the response code, after sending the request
   * 
   * @return the response code
   * @exception   IOException  thrown on problem with the connection
   */
  public int getResponseCode() throws IOException {
    if (!bReadHeaders) {
      getInputStream();
    }

    return responseCode;
  }

  /**
   * Retreive the response message, after sending the request
   * 
   * @return a String representing the response message
   * @exception   IOException  thrown on error with the connection
   */
  public String getResponseMessage() throws IOException {
    if (!bReadHeaders) {
      getInputStream();
    }

    return responseString;
  }

  /**
   * Retreive the Content Type of the Response. As specified by the "Content-Type" Header
   * 
   * @return
   * @exception   IOException  thrown on error with the connection
   */
  public String getContentType() throws IOException {
    if (!bReadHeaders) {
      getInputStream();
    }

    String[] h = getHeader("Content-Type");

    if (h != null) {
      return h[h.length - 1];
    } else {
      return null;
    }
  }

  /**
   * Retreive the Content-Length of the Response. As specified by the "Content-Length" Header
   * 
   * @return
   * @exception   IOException  thrown on error with the connection
   */
  public int getContentLength() throws IOException {
    if (!bReadHeaders) {
      getInputStream();
    }

    String[] h = getHeader("Content-Length");

    if (h != null) {
      try {
        return Integer.parseInt(h[h.length - 1]);
      } catch (NumberFormatException x) {
        throw new IOException("The server returned invalid http response. It contains unparsable 'Content-Length' header with value '"+h[h.length - 1]+"'. The HTTP Response code is ("+this.responseCode+") "+this.responseString);
      }
    } else {
      return -1;
    }
  }

  /**
   * Get the inputStream to this connection. Before returing it to the user, first the Response is parsed,
   * the headers are read so that they can be retreived later with getHeader, and the data of the Response can be
   * read from the returned InputStream. If the ResponseCode, Message or Headers are needed, the appopriate methods
   * must be used
   * 
   * @return the InputStream, from which the body of the Response might be read
   * @exception   IOException  thrown on error with the connection
   */
  public InputStream getInputStream() throws IOException {
    if (bSentHeaders == false) {
      getOutputStream();
    }

    if (bReadHeaders == false) {
      InputStream resultStream = null;
      headers.clear();
      InputStreamLogger logger = null;
      // Result stream is buffered
      if (this.logInputStream != null) {
        logger = new InputStreamLogger(connectionInputStream, logInputStream);
        resultStream = logger;
      } else {
        resultStream = connectionInputStream;
      }
      // flag to keep track when  it becames unbuffered
      boolean unBuffered = false;
      // Parse headers
      String line = null;
      boolean bReadFirstLine = false;
      boolean bGotEmptyLine = false;
      while ((line = readLine(resultStream)) != null) {
        if (line.startsWith("HTTP/1.1 100")) {
          do { //ignore everything until the other HTTP
            line = readLine(resultStream);
            if (line == null) {
              throw new IOException("Unexpected end of response!");
            }
          } while (!line.startsWith("HTTP"));
        }

        if (!bGotEmptyLine && line.length() == 0) {
          break;
        }

        if (bReadFirstLine == false) {
          parseFirstLine(line);
          bReadFirstLine = true;
        } else {
          parseHeaderLine(line);
        }
      }
      // Turn off header parse by the logger stream
      if (logger != null) {
        logger.setParseHeaders(false);
      }
      boolean chunked = false;
      String[] encodings = getHeader("Transfer-Encoding");
      if (encodings != null) {
        for (int i = 0; i < encodings.length; i++) {
          if ("chunked".equalsIgnoreCase(encodings[i])) { // Chunked
            resultStream = new ChunkedInputStream(resultStream);
            unBuffered = true;
            chunked = true;
            break;
          }
        }
      }
      String[] contentLength = getHeader("Content-Length");
      if (!chunked && contentLength != null) {
        try {
          int byteCount = Integer.parseInt(contentLength[contentLength.length - 1]);
          resultStream = new LimitedInputStream(resultStream,byteCount);
        } catch (NumberFormatException x) {          
          throw new IOException("The server returned invalid http response. It contains unparsable 'Content-Length' header with value '"+contentLength[contentLength.length - 1]+"'. The HTTP Response code is ("+this.responseCode+") "+this.responseString);
        }
      }
      String[] compression = getHeader("Content-Encoding");
      if (compression != null) {
        for (int i = 0; i < compression.length; i++) {
          if ("gzip".equalsIgnoreCase(compression[i])) { // compressed
            if (unBuffered) {
              resultStream = new BufferedInputStream(resultStream);
            }
            resultStream = new GZIPInputStreamEOF(resultStream);
            unBuffered = true;
            clearHeader("Content-Encoding");
            break;
          }
        }
      }
      String[] connectionHeader = getHeader(CONNECTION_HEADER);
      if (connectionHeader != null) {
        for (int i = 0; i < connectionHeader.length; i++) {
          if (CONNECTION_CLOSED.equalsIgnoreCase(connectionHeader[i])) {
            keepAlive = false;
          }
        }
      }
      bReadHeaders = true;
      if (unBuffered) {
        inputStream = new BufferedInputStream(resultStream);
      } else {
        inputStream = (BufferedInputStream) resultStream;
      }
    }

    ////System.out.println("HTTPSock.getInputStream 3:");
    return inputStream;
  }

  /**
   * Get the OutputStream to this connection. Before calling this method you should set the RequestMethod and
   * RequestHeaders, because before returning the OutputStream to the user, first the headers are sent, and then
   * the user may write his request in the body of the message
   * 
   * @return the OutputStream to the connection
   * @exception   IOException  thrown on error with the connection
   */
  public OutputStream getOutputStream() throws IOException {
    if (!bSentHeaders) { //outputStream is not initialized
      initializeStreams();
      if (logOutputStream != null) {
        // output connection is loggable
        outputStream = new OutputStreamLogger(connectionOutputStream, logOutputStream);
      } else {
        // connection output stream is buffered
        outputStream = (BufferedOutputStream) connectionOutputStream;
      }
      sendRequest();
      sendHeaders();
      bSentHeaders = true;
    }
    return outputStream;
  }

  private void initializeStreams() throws IOException {
    if (connectionOpen == false) { // Connection has not been opened
      try {
      if (port == -1) {
        if (secure) {
          port = 443;
        } else {
          port = 80;
        }
      }
      if (proxyHost == null) {
        HTTPProxyResolver resolver = proxyResolver;
        if (resolver == null) {
          resolver = PROXY_RESOLVER;
        }
        if (resolver != null) {
          HTTPProxy proxy = resolver.getHTTPProxyForHost(host);
          if (proxy != null) {
            proxyHost = proxy.getProxyHost();
            proxyPort = proxy.getProxyPort();
            proxyUser = proxy.getProxyUser();
            proxyPass = proxy.getProxyPass();
          } else {
            proxyHost = null;
            proxyPort = 80;
            proxyUser = null;
            proxyPass = null;
          }
        }
      }

      if (onServer) {
        initStreamsFromConnection();
      } else {
        initStreamsFromSocket();
      }
      } catch (IOException x) {
        connectionFailed = true;
        sock = null;
        throw x;
      }
      connectionOpen = true;
    } else {      
      if (connectionFailed) {
        throw new IOException("Keep-Alive connection has died. Use isAlive() check before using keep-alive connection.");
      }      
    }
  }

  /**
   * Returns true if the connections is still alive.
   * Call this on Keep-Alive connections to reuse connection in the following order:
   * 1. create httpSocket
   * 2. set headers
   * 3. getOutputStream
   * 4. write output and flush it
   * 5. get input stream
   * 6. read input untils it ends EOF
   * 7. if isAlive - true then goto 2
   * @return
   */
  public boolean isAlive() throws IOException {
    if (keepAlive == false || connectionFailed) {
      return false;
    }
    if (connectionOpen == false) {
      throw new IOException("Do not call isAlive() before the connection is open.");
    }
    int result = 0;
    if (checkClosed()) { // Checks if the socket is closed
      disconnect();
      this.keepAlive = false;
      return false;
    }
    try {
      sock.setSoTimeout(1);
      result = this.connectionInputStream.read();
    } catch (InterruptedIOException x) {
      // socket is alive ! InputStream is not closed
      sock.setSoTimeout(socketReadTimeout);
      this.logInputStream = null;
      this.logOutputStream = null;
      this.headerNames.clear();
      this.headers.clear();
      this.buffer.clear();
      lastPutHeader = null;
      responseCode = -1;
      responseString = null;
      bSentHeaders = false;
      bReadHeaders = false;
      return true;
    } catch (SocketException x) {
      // connection has died
      disconnect(); // close the connection for good measure
      this.keepAlive = false;
      this.connectionFailed = true;
      return false;
    }
    if (result == -1) { // Stream EOF - The other side closed the connection
      disconnect(); // close the connection for good measure
      this.keepAlive = false;
      this.connectionFailed = true;
      return false;
    } else {
      int availableBytes = this.connectionInputStream.available();
      byte[] bytes = new byte[availableBytes];
      this.connectionInputStream.read(bytes);
      String temp = new String(bytes); //$JL-I18N$
      throw new IOException("Connection sync error ! Character ["+String.valueOf((char) result)+temp+"] recieved !");
    }
  }

  /**
   * Uses JDK1.4 method to check if the socket is closed.
   * @return
   */
  private boolean checkClosed() {
    Class socketClass = sock.getClass();
    try {
      Method method = socketClass.getMethod("isClosed",new Class[] {});
      Boolean bool = (Boolean) method.invoke(sock,new Object[] {});
      return bool.booleanValue();
    } catch (NoSuchMethodException e) {
      return false;
    } catch (SecurityException e) {
      return false;
    } catch (IllegalAccessException e) {
      return false;
    } catch (InvocationTargetException e) {
      return false;
    }
  }

  private void initStreamsFromConnection() throws IOException {
    WSConnectionFactory factory;
    try {
      Context ctx = new InitialContext();
      factory = (WSConnectionFactory) ctx.lookup("/wsContext/" + WSConnectionFactory.NAME);
    } catch (NamingException e) {
      throw new IOException(e.toString());
    }
    //    WSConnectionFactory factory = WSContainer.getWsConnectionFactory();
    if (secure) {
      wsConnection = factory.getSSLConnection(host, port, proxyHost, proxyPort);
    } else {
      wsConnection = factory.getConnection(host, port, proxyHost, proxyPort);
    }
    connectionOutputStream = wsConnection.getOutputStream();
    connectionInputStream = wsConnection.getInputStream();
  }

  private void initStreamsFromSocket() throws IOException {
    sock = createSocket(host, 
                        port, 
                        socketConnectionTimeout, 
                        proxyHost, 
                        proxyPort, 
                        proxyUser, 
                        proxyPass, 
                        secure,
                        secure ? createSSLUtil() : null,
                        clientCertificateList, 
                        serverCertificateList, 
                        isIgnoreServerCertificates());
    if(proxyHost != null && proxyUser != null && !secure) {
      setHeader("Proxy-Authorization", ClientHTTPTransport.encodeAuth(proxyUser, proxyPass));
    }
    sock.setSoTimeout(socketReadTimeout);
    sock.setKeepAlive(true);
    sock.setTcpNoDelay(true);
    connectionOutputStream = new BufferedOutputStream(sock.getOutputStream());
    connectionInputStream = new BufferedInputStream(sock.getInputStream());
    
  }

  public static Socket createSocket(String host, 
                                    int port, 
                                    int socketConnectionTimeout, 
                                    String proxyHost, 
                                    int proxyPort, 
                                    String proxyUser, 
                                    String proxyPass, 
                                    boolean secure,
                                    SSLSocketUtilInterface sslUtil,
                                    List clientCertificateList, 
                                    List serverCertificateList, 
                                    boolean isIgnoreServerCertificates) throws IOException {
    if(proxyHost != null) {
      return(secure ? sslUtil.createSSLSocket(host, port, createProxySocketForSSLSocket(proxyHost, proxyPort, socketConnectionTimeout, host, port, proxyUser, proxyPass), clientCertificateList, serverCertificateList, isIgnoreServerCertificates, proxyUser != null) : createSocket(proxyHost, proxyPort, socketConnectionTimeout)); 
          }
    return(secure ? sslUtil.createSSLSocket(host, port, createSocket(host, port, socketConnectionTimeout), clientCertificateList, serverCertificateList, isIgnoreServerCertificates, false) : createSocket(host, port, socketConnectionTimeout));
        }
  
  private static Socket createSocket(String host, int port, int socketConnectionTimeout) throws IOException {
    Socket socket = new Socket();
    try {
      socket.connect(new InetSocketAddress(host, port), socketConnectionTimeout);
    } catch(Throwable thr) {
      IOException ioe = new IOException("Unable to connect to " + host + ":" + port + " - " + thr.getMessage());
      ioe.initCause(thr);
      throw ioe;
    }
    return(socket);
      }

  private SSLSocketUtilInterface createSSLUtil() {
      try {
      return(new SSLUtilUsingHttpsLibImpl());
    } catch(Throwable thr_SSLUtilUsingHttpsLibImpl) {
      return(new SSLUtilImpl());
    }
  }

  private static Socket createProxySocketForSSLSocket(String proxyHost, int proxyPort, int socketConnectionTimeout, String host, int port, String proxyUser, String proxyPass) throws IOException {
    Socket proxySocket = createSocket(proxyHost, proxyPort, socketConnectionTimeout);  
    proxySocket.setSoTimeout(PROXY_INITIAL_READ_TIMEOUT);
    PrintWriter writer = iaik.security.ssl.Utils.getASCIIWriter(proxySocket.getOutputStream());
    writer.println("CONNECT " + host + ":" + port + " HTTP/1.0");
    
    if(proxyUser != null) {
      writer.print("Proxy-Authorization: ");
      writer.println(ClientHTTPTransport.encodeAuth(proxyUser, proxyPass));
    }
    writer.println();
    writer.flush();

    BufferedReader reader = iaik.security.ssl.Utils.getASCIIReader(proxySocket.getInputStream());
    String responseLine = reader.readLine();
    if (responseLine == null || !responseLine.startsWith("HTTP/1.")) {
      throw new IOException("Invalid response from proxy: " + responseLine);
        }
    String proxyResponse = responseLine.substring("HTTP/1.x".length()).trim(); 
    if(proxyResponse.startsWith("407")) {
      throw new IOException("Proxy authentication required or failed.");
      }
    if(!proxyResponse.startsWith("2")) {
      throw new IOException("Proxy responded: " + responseLine);
    }
    return(proxySocket);
  }

  /**
   * Set the request method for the request. Might be POST or GET
   * 
   * @param requestMethod
   */
  public void setRequestMethod(String requestMethod) {
    this.requestMethod = requestMethod;
  }

  /**
   * Close this connection
   * 
   * @exception IOException thrown on error with the connection
   */
  public void disconnect() throws IOException {
    if (sock != null) {
      if (inputStream != null) {
      inputStream.close();
        inputStream = null;
      }
      if (outputStream != null) {
      outputStream.close();
        outputStream = null;
      }
      sock.close();
      sock = null;
      //System.out.println("Socket closed !");
      keepAlive = false;
    }
    if (wsConnection != null) {
      if (inputStream != null) {
      inputStream.close();
        inputStream = null;
      }
      if (outputStream != null) {
      outputStream.close();
        outputStream = null;
      }
      wsConnection.close();
      wsConnection = null;
      //System.out.println("Socket closed !");
      keepAlive = false;
    }
  }

  /**
   * Write a String to the OutputStream of the connection
   * 
   * @param data the data to be written
   * @exception IOException
   */
  private void write(String data) throws IOException {
    for (int i = 0; i < data.length(); i++) {
      outputStream.write((byte) data.charAt(i));
    }
  }

  /**
   * Sends the request to the OutputStream of the connection
   * 
   * @exception IOException
   */
  private void sendRequest() throws IOException {
    write(requestMethod);
    write(" ");

    if ((!secure) && (proxyHost != null)) {
      write("http://");
      write(host);

      if (port != -1 && port != 80) {
        write(":");
        write(portStr);
      }
    }

    if (file.length() > 0 && file.charAt(0) != '/') {
      write("/");
    }
    write(file);
    write(" HTTP/1.1\r\n");
    write("Host: " + host + ":" + port + "\r\n");
  }

  /**
   * Send all the headers in the headers Hashtable to the OutputStream of the connection
   *
   * @exception IOException
   */
  private void sendHeaders() throws IOException {
    addSapPassport();  
    
    ////System.out.println("HTTPSocket.sendHeaders" );
    Enumeration headerKeys = headers.keys();
    boolean hasLog = outputStream instanceof OutputStreamLogger;

    while (headerKeys.hasMoreElements()) {
      String name = (String) headerKeys.nextElement();
      String realName = (String) headerNames.get(name);
      if (realName == null) {
        realName = name;
      }
      String values[] = (String[]) headers.get(name);
      for (int i = 0; i < values.length; i++) {
        if (hasLog && ("authorization".equalsIgnoreCase(name) || "cookie".equalsIgnoreCase(name) || "set-cookie".equalsIgnoreCase(name))) {
          ((OutputStreamLogger) outputStream).writeHidden(realName + ": " + values[i] + "\r\n", realName + ": <value is hidden>\r\n");
        } else {
          write(realName);
          write(": ");
          write(values[i]);
          write("\r\n");
        }
      }
    }

    //    write("Cookie: $Version=\"1\"; JSESSIONID=ID1DB123End; Max-Age=60\r\n");
    write("\r\n");   
  }
  
  /**
   * Add sap passport header if it is not already present.
   */
  private void addSapPassport(){
    String[] passports = getHeader(SAP_PASSPORT);

    // The passport is present. Do not set anything.
    if (passports != null && passports.length > 0) {
      return;
    }

    // The reflection failed. The classes are no available.
    if (httpHandlerClass == null || beforeSendingRequest == null) {
      classLogger.warningT("Classed needed for SAP passport not resolved. SAP passpowt won't be applied as header");
      return;
    }
    
    try{ 
      // Get the sap passport through the reflection method. Here is the original method declaration.
      // beforeSendingRequest(int protocol, String host, String ip, int port, String path) {
      int protocolFlag = secure? PASSPORT_HTTPS_FLAG: PASSPORT_HTTP_FLAG;
      String passportHeaderValue = (String) beforeSendingRequest.invoke(null, new Object[]{protocolFlag, host, null, port, file});
  
      if (passportHeaderValue != null){
        setHeader(SAP_PASSPORT, passportHeaderValue);
      }
    }catch (Exception e) {
      classLogger.warningT("Applying the SAP passport failed: " + e.getStackTrace());
    }    
        
    //Nofity the sap passport that a response is received.
    notifySapPassport();
  }
  
  /**
   * Notify sap passport that a response is receved.
   * Can be don right after we sent the request headers.
   */
  private void notifySapPassport(){
      try {
        afterReceivingResponse.invoke(null, new Object[]{});
      } catch (Exception e) {
        classLogger.warningT("Notifying the SAP passport that response is received failed: " + e.getStackTrace());
      }
  }

  /**
   * Reads a line of the InputStream. The line is supposed to end on a NewLine \n. And if there is a 
   * CarriageReturn \r it is skpped.
   * 
   * @return the line of data read or <code>null</code> if the end of stream is reached and no data was read
   * @exception IOException
   */
  private String readLine(InputStream streamToRead) throws IOException {
    int r = 0;
    buffer.clear();

    while ((r = streamToRead.read()) != -1 && r != '\n') {
      if (r != '\r') {
        buffer.append((char) r);
      }
    }

    if (r == -1 && buffer.length() == 0) {
      return null;
    } else {
      return buffer.toString();
    }
  }

  /**
   * Parses the first line of the Response and extracts the response code, and response message
   * 
   * @param   line  the line to be parsed
   * @exception IOException
   */
  private void parseFirstLine(String line) throws IOException {
    if (!line.startsWith("HTTP/1.0") && !line.startsWith("HTTP/1.1")) {
      throw new BadResponseException("Bad Response: " + line);
    }
    try {
      responseCode = Integer.parseInt(line.substring(9, 12));
    } catch (NumberFormatException x) {
      throw new IOException("Bad HTTP Response : "+line);
    }
    responseString = line.substring(13);
  }

  /**
   * Parses a generic header line. Finds the first : and assumes that the left part is the header name
   * and the right - the heade value. A header name might be associated to several values in several headers.
   * 
   * @param line
   * @exception IOException
   */
  private void parseHeaderLine(String line) throws IOException {
    if (line.startsWith(" ") || line.startsWith("\t")) {
      if (lastPutHeader == null) {
        throw new IOException("Incorrect header line: " + line);
      }

      setHeaderInternal(lastPutHeader, line.trim());
    } else {
      int idx = line.indexOf(':');

      if (idx == -1) {
        throw new IOException("Expected ':' in header line: " + line);
      }

      String header = line.substring(0, idx).trim();
      String value = line.substring(idx + 1).trim();
      setHeader(header, value);
    }
  }

  /**
   * Returns the clientCertificateList.
   * @return List
   */
  public List getClientCertificateList() {
    return clientCertificateList;
  }

  /**
   * Returns the serverCertificateList.
   * @return List
   */
  public List getServerCertificateList() {
    return serverCertificateList;
  }

  /**
   * Sets the clientCertificateList.
   * @param clientCertificateList The clientCertificateList to set
   */
  public void setClientCertificateList(List clientCertificateList) {
    this.clientCertificateList = clientCertificateList;
  }

  /**
   * Sets the serverCertificateList.
   * @param serverCertificateList The serverCertificateList to set
   */
  public void setServerCertificateList(List serverCertificateList) {
    this.serverCertificateList = serverCertificateList;
  }

  /**
   * Returns the ignoreServerCertificates.
   * @return boolean
   */
  public boolean isIgnoreServerCertificates() {
    return ignoreServerCertificates;
  }

  /**
   * Sets the ignoreServerCertificates.
   * @param ignoreServerCertificates The ignoreServerCertificates to set
   */
  public void setIgnoreServerCertificates(boolean ignoreServerCertificates) {
    this.ignoreServerCertificates = ignoreServerCertificates;
  }
 
  /* Special functions do not use !!! */
  public void setSoTimeoutFast(int timeOut) throws SocketException {
    if (this.sock != null) {
      this.sock.setSoTimeout(timeOut);
    }
  }

  public void resetSoTimeoutFast() throws SocketException {
    if (this.sock != null) {
      this.sock.setSoTimeout(socketReadTimeout);
    }
  }
 
    
  public String getFile(){
    return this.file;
  }
  
  
  public void setFile(String file){
    this.file = file;
  }
  
  public boolean isKeepAlive(){
    return this.keepAlive;
  }
  

  /**
   * Closes the output socket in case it was not closed.
   * @throws Throwable
   */
  protected void finalize() throws Throwable {
    super.finalize();
    this.disconnect();
  }
  /*
  public static void main(String[] args) throws Exception {
    BufferedReader reader = new BufferedReader(new InputStreamReader(
        new FileInputStream("e:/request.xml")));
    String message = reader.readLine();
    reader.close();
    //System.out.println("["+message+"]");
    //System.out.println(message.length());
    int i = 1;
    long time = System.currentTimeMillis();
    URL url = new URL(
        "http://localhost:50000/CalendarService/Config?style=document");
    HTTPSocket socket = new HTTPSocket(url);
    socket.setSocketTimeout(1000);
    socket.setRequestMethod("POST");
    socket.setHeader("Content-Type", "text/xml; charset=\"UTF-8\"");
    socket.setHeader("Content-Length", "560");
    socket.setHeader("SOAPAction", "\"\"");
    //socket.setHeader("Connection", "Close");
    //socket.setLogger(System.out,System.out);
    OutputStream output = socket.getOutputStream();
    output.write(message.getBytes());
    output.flush();
    InputStream input = socket.getInputStream();
    while (input.read() != -1) {
    }
    System.out.println("request " + String.valueOf(i));
    Thread.sleep(21000);
    //while (socket.isAlive() && i<2000) { // socket is alive
    if (socket.isAlive()) {
      socket.setRequestMethod("POST");
      socket.setHeader("Content-Type", "text/xml; charset=\"UTF-8\"");
      socket.setHeader("Content-Length", "560");
      socket.setHeader("SOAPAction", "\"\"");

      //socket.setLogger(System.out,System.out);
      output = socket.getOutputStream();
      output.flush();
      output.write(message.getBytes());
      output.flush();
      input = socket.getInputStream();
      while (input.read() != -1) {
      }
      i++;
      System.out.println("request " + String.valueOf(i));
    } else {
      socket.disconnect();
    }
    //}
    time = System.currentTimeMillis() - time;
    System.out.println(time);
  }*/
}

