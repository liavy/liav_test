/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf.. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.ssl.factory;

import com.sap.engine.frame.cluster.transport.TransportFactory;
import com.sap.engine.services.ssl.exception.BaseIOException;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 *  This factory provided by the SSLHttp transport supplier.
 *
 * @author  Vladislav Vladimirov
 *
 */
public class SSLHttpTransportFactory extends SSLTransportFactory implements TransportFactory {

  private static String ProxyHost = null;
  private static int ProxyPort = 0;
  private String RemoteHost = null;
  private int RemotePort = 0;
	static byte[] crlf_byte=new byte[]{13,10};
	static String crlf = new String(crlf_byte);
  private static String ProxyRequest = null; //"CONNECT "+RemoteHost+":"+RemotePort+" HTTP/1.0" +crlf +
								//		"User-Agent: SAPJ2EE Engine SSLHttpTunneling" + crlf+
								//		"Host: "+RemoteHost+crlf+
								//		"Content-Length: 0" + crlf+
								//		"Proxy-Connection: Keep-Alive" +crlf+
								//		"Pragma: no-cache" +
								//			crlf+crlf;

  public SSLHttpTransportFactory() {
    // do nothing
  }

  public SSLHttpTransportFactory(TransportFactory base) {
    super(base);
  }

  public Socket getSocket(String host, int port, Properties props) throws IOException {
		RemoteHost = host;
		RemotePort = port;
		if (ProxyRequest == null)  {
			ProxyRequest ="CONNECT "+RemoteHost+":"+RemotePort+" HTTP/1.0" +crlf + crlf;
		}
									//		"User-Agent: SAPJ2EE Engine SSLHttpTunneling" + crlf+

		RemoteHost = props.getProperty("Host");
		RemotePort = Integer.parseInt(props.getProperty("Port"));
		ProxyHost = props.getProperty("HTTP_Host", host);
		ProxyPort = Integer.parseInt(props.getProperty("HTTP_Port", String.valueOf(port)));
	  return getSocket(host, port);
  }

  public Socket getSocket(String host, int port) throws IOException {
    Socket socket = null;

    try {
      ClientSocketFactory clientSocketFactory = getClientSocketFactory();
			  Socket s = new Socket(ProxyHost,ProxyPort);
			  DataOutputStream out = new DataOutputStream(s.getOutputStream());
			  BufferedReader br=new BufferedReader(new InputStreamReader(s.getInputStream()));
				out.writeBytes(ProxyRequest);
			  out.flush();
			  String response = br.readLine();   // ?? moje da zawisne ako nqma reponse nikakyw

				if(response.indexOf("200") == -1) {
				  throw new IOException("HTTP Proxy doesn't support CONNECT Directive or Proxy is not Setup");
			  }
      	socket = clientSocketFactory.createSocket(s,ProxyHost, ProxyPort,false);

    } catch (IOException io_e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(io_e);
    } catch (Exception e) {
      //$JL-EXC$
      throw BaseIOException.wrapException(e);
    }
    return socket;
  }

	public ServerSocket getServerSocket(int a,int b,java.lang.String v) throws IOException {
		return null;
	}

}

