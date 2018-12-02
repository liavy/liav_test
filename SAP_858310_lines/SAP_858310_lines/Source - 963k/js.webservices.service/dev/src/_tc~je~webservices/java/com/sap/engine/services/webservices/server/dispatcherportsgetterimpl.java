/*
 * Copyright (c) 2003 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import com.sap.engine.frame.cluster.ClusterElement;
import com.sap.engine.frame.cluster.ClusterException;
import com.sap.engine.frame.cluster.message.MessageAnswer;
import com.sap.engine.frame.cluster.message.MessageContext;
import com.sap.engine.interfaces.webservices.uddi4j.DispatcherPortsGetter;
import com.sap.engine.services.webservices.common.WSConnectionConstants;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.tc.logging.Location;

/**
 * Copyright (c) 2003, SAP-AG
 * @author Alexander Zubev (alexander.zubev@sap.com)
 * @version 1.1, 2004-3-31
 */
public class DispatcherPortsGetterImpl implements DispatcherPortsGetter {

  public static Object[] getPort(byte portID) {
    MessageContext msgCtx = WSContainer.getServiceContext().getClusterContext().getMessageContext();
    int[] dispatcherIDs = WSContainer.getDispatcherIDs();
    int port = -1;
    for (int i = 0; i < dispatcherIDs.length; i++) {
      int dispatcherID = dispatcherIDs[i];
      ClusterElement clusterEl = WSContainer.getServiceContext().getClusterContext().getClusterMonitor().getParticipant(dispatcherID);
      if (clusterEl == null) {
        continue;
      }
      try {
        MessageAnswer msgAnswer = msgCtx.sendAndWaitForAnswer(dispatcherID,
                portID, new byte[0], 0, 0, 0);
        if (msgAnswer.getLength() != 0) {
          ByteArrayInputStream in = new ByteArrayInputStream(msgAnswer.getMessage(), msgAnswer.getOffset(), msgAnswer.getLength());
          DataInputStream dataStream = new DataInputStream(in);
          try {
            port = dataStream.readInt();
            if (port != -1) {
              Object[] res = new Object[2];
              res[0] = clusterEl.getAddress();
              res[1] = new Integer(port);
              return res;
            }
          } catch (IOException ioe) {
            Location.getLocation(WSLogging.SERVER_LOCATION).catching("An error occurred while parsing getSSLPort response: " + dispatcherIDs[i], ioe);
          }
        }
      } catch (ClusterException ce) {
        Location.getLocation(WSLogging.SERVER_LOCATION).catching("An error occurred while sending getSSLPort message to dispatcher: " + dispatcherIDs[i], ce);
      }
    }
    return null;
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.uddi4j.DispatcherPortsGetter#getHTTPPort()
   */
  public Object[] getHTTPPort() {
    return getPort(WSConnectionConstants.GET_HTTP_PORT);
  }

  /* (non-Javadoc)
   * @see com.sap.engine.interfaces.webservices.uddi4j.DispatcherPortsGetter#getHTTPSPort()
   */
  public Object[] getHTTPSPort() {
    return getPort(WSConnectionConstants.GET_SSL_PORT);
  }

}
