/*
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * url: http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 *
 */
package com.sap.engine.services.ssl.dispatcher;

import com.sap.engine.services.ssl.exception.SSLConfigurationException;
import com.sap.engine.services.ssl.exception.SSLResourceAccessor;
import com.sap.engine.services.ssl.factory.ServerSocketFactory;
import com.sap.engine.services.ssl.factory.ServerSocket;
import com.sap.engine.services.ssl.util.Utility;
import com.sap.engine.services.ssl.dispatcher.RuntimeInterface;
import com.sap.engine.services.ssl.keystore.KeyStoreConnector;
import com.sap.engine.frame.state.ManagementListener;
import com.sap.tc.logging.Severity;

import java.util.Vector;
import java.util.Hashtable;
import java.lang.reflect.Method;

/**
 *  This interface is used to configure SSL server sockets on dispatchers of the cluster.
 *
 * @author  Stephan Zlatarev, Svetlana Stancheva
 * @version 4.0.2
 */
public class RuntimeInterfaceImpl implements RuntimeInterface {

  private final static String[] COLUMN_NAMES = new String[] {
    "type",
    "host",
    "port",
    "owner",
    "keystore entry",
    "algorithm",
    "expires in (days)"
  };

 

  public void registerManagementListener( ManagementListener managementListener ) {
    // todo:  registerManagmentListener
  }


  public boolean amICentralInstance() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  /**
   *  Returns a state describing the certificate expiration status.
   *  Used for monitoring.
   *
   * @return  a state as described in monitor-configuration.xml
   */
  public String getExpirationState() {
    return CertificateExpirationTracker.getExpirationStatus();
  }


  /**
   *  Returns the headers of the table of expiring certificates.
   *  Used for monitoring.
   *
   * @return  column names of the expiring certificates table.
   */
  public String[] getExpirationTableHeaders() {
    return COLUMN_NAMES;
  }


  private static final String SSL_PORT_NOT_AVAILABLE="ssl_runtime_interface_port_not_available";  //Unable to determine port of server socket

  /**
   *  Returns the contents of the table of expiring certificates.
   *  Used for monitoring.
   *
   * @return  a two-dimension array of strings.
   */
  public String[][] getExpirationTableContents() {
    String[][] result = null;
    String[] aliases = null;
    String[][] sockets = ServerSocketFactory.getActiveServerSockets();
    ServerSocket socket = null;
    Vector collection = new Vector(10, 10);
    Hashtable expiration = new Hashtable();
    Hashtable owners = getPortsUsage();

    aliases = ((ServerSocketFactory) ServerSocketFactory.getDefault()).getCredentials();
    for (int j = 0; j < aliases.length; j++) {
      collection.add(new Object[] { "server identity", aliases[j], null, "New Sockets", "N/A" });
    }
    aliases = ((ServerSocketFactory) ServerSocketFactory.getDefault()).getTrustedCertificates();
    for (int j = 0; j < aliases.length; j++) {
      if (aliases[j].trim().length() > 0) { // "" means that no certificate is also ok as client authentication
        collection.add(new Object[] { "trusted certification authority", aliases[j], null, "New Sockets", "N/A" });
      }
    }

    for (int i = 0; i < sockets.length; i++) {
      try {
        socket = ServerSocketFactory.getServerSocket(sockets[i][0], Integer.valueOf(sockets[i][1]).intValue());
      } catch (NumberFormatException e) {
        SSLResourceAccessor.log(Severity.WARNING, e, SSL_PORT_NOT_AVAILABLE,  new Object[]{sockets[i][0], sockets[i][1]});
        continue;
      }

      aliases = socket.getCredentials();
      for (int j = 0; j < aliases.length; j++) {
        collection.add(new Object[] { "server identity", aliases[j], socket, sockets[i][0], sockets[i][1] });
      }

      aliases = socket.getTrustedCertificates();
      for (int j = 0; j < aliases.length; j++) {
        if (aliases[j].trim().length() > 0) { // "" means that no certificate is also ok as client authentication
          collection.add(new Object[] { "trusted certification authority", aliases[j], socket, sockets[i][0], sockets[i][1] });
        }
      }
    }

    result = new String[collection.size()][COLUMN_NAMES.length];

    for (int i = 0; i < result.length; i++) {
      Object[] element = (Object[]) collection.elementAt(i);
      result[i][0] = (String) element[0];
      result[i][1] = (String) element[3];
      result[i][2] = (String) element[4];

      result[i][3] = (String) owners.get(result[i][2]);
      result[i][3] = (result[i][3] != null) ? result[i][3] : "";

      result[i][4] = (String) element[1];
      result[i][5] = CertificateExpirationTracker.getCertificateAlgorithm(result[i][4]);
      // uses a hastable to avoid multiple calls for the same certificate
      result[i][6] = (String) expiration.get(element[1]);
      if (result[i][6] == null) {
        result[i][6] = String.valueOf(CertificateExpirationTracker.getDaysToExpirationOfCertificate((String) element[1]));
        expiration.put(element[1], result[i][6]);
      }
    }

    // sort result by expiration
    String[] swap = new String[COLUMN_NAMES.length];
    for (int i = 0; i < result.length - 1; i++) {
      int bestIndex = i;
      int bestValue = new Integer(result[i][6]).intValue();

      for (int j = i + 1; j < result.length; j++) {
        int candidate = new Integer(result[j][6]).intValue();

        if(candidate < bestValue) {
          bestIndex = j;
          bestValue = candidate;
        }
      }

      if (bestIndex != i) {
        // swap them
        for (int k = 0; k < swap.length; k++) {
          swap[k] = result[i][k];
        }
        for (int k = 0; k < swap.length; k++) {
          result[i][k] = result[bestIndex][k];
        }
        for (int k = 0; k < swap.length; k++) {
          result[bestIndex][k] = swap[k];
        }
      }
    }

    return result;
  }
  private static final String GET_PORTS_USAGE = "";// Unexpected
  private final Hashtable getPortsUsage() {
    Hashtable result = new Hashtable();
//    try {
//      Object portsManager = DispatcherService.getServiceContext().getCoreContext().getReflectContext().getCoreComponent( "PortsManager" );
//      Method usedPortsMethod = portsManager.getClass().getMethod( "getUsedPorts", null );
//      PortNamePair[] pairs = (PortNamePair[]) usedPortsMethod.invoke( portsManager, null );
//
//      for (int i = 0; i < pairs.length; i++) {
//        result.put(String.valueOf(pairs[i].getPort()), pairs[i].getName());
//      }
//    } catch (Exception e) {
//      //$JL-EXC$
//      SSLResourceAccessor.log(Severity.WARNING, e, GET_PORTS_USAGE);
//      SSLResourceAccessor.traceThrowable(Severity.WARNING, GET_PORTS_USAGE, null, e);
//    }

    return result;
  }
}

