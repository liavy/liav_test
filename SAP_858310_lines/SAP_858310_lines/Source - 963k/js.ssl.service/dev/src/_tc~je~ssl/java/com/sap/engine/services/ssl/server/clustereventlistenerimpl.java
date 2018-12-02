/**
 * Copyright (c) 2000 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.ssl.server;

import com.sap.engine.frame.cluster.event.ClusterEventListener;
import com.sap.engine.frame.cluster.ClusterElement;
import com.sap.engine.frame.cluster.monitor.ClusterMonitor;
import com.sap.engine.frame.state.ServiceState;
import com.sap.engine.services.ssl.keystore.KeyStoreConnector;

/**
 * @author Ilia Kacarov
 *         Copyright (c) 2002, SAP-AG
 */
public class ClusterEventListenerImpl implements ClusterEventListener {
  private static ClusterMonitor clusterMonitor = null;
  private static ServiceState serviceState = null;
  private static int currentGroupID = -1;
  private static ClusterEventListener listener = null;

  
  public static final synchronized int setClusterMonitor(ClusterMonitor cluster_Monitor) {
    clusterMonitor = cluster_Monitor;
    return currentGroupID = clusterMonitor.getCurrentParticipant().getGroupId();
  }
  
  public static final synchronized void setServiceState(ServiceState service_State) {
    serviceState = service_State;
  }

  public ClusterEventListenerImpl() {
    ClusterElement[] nodes = clusterMonitor.getParticipants();
    ServerService.dump(" current nodes: " + nodes.length);
    
    for (int i = 0; i < nodes.length; i++) {
      if (nodes[i].getType() == ClusterElement.ICM) {
        ServerService.dump(" ICM node: " + nodes[i]);
        KeyStoreConnector.preparePSE();
        break; // we have only one ICM per group
      } else {
        ServerService.dump(" server node: " + nodes[i]);
      }
    }
  }


  public void elementJoin(ClusterElement clusterElement) {
    if (clusterElement.getType() == ClusterElement.ICM && clusterElement.getGroupId() == currentGroupID) {
      ServerService.dump(" ICM node joined: " + clusterElement);
      KeyStoreConnector.preparePSE();
    } else {
      ServerService.dump(" server node joined: " + clusterElement);
    }
  }

  public void elementLoss(ClusterElement clusterElement) {
    // ignored
  }

  public void elementStateChanged(ClusterElement clusterElement, byte b) {
    // ignored
  }
  
  public static final synchronized void start() throws Exception {
    if (serviceState == null || currentGroupID == -1) {
      throw new Exception("ClusterEventListener not initialized");
    }

    listener = new ClusterEventListenerImpl();
    serviceState.registerClusterEventListener(listener);

    ServerService.dump(" cluster listener registered");
  }
  
  public static final void stop() {
    serviceState.unregisterClusterEventListener();
    ServerService.dump(" cluster listener unregistered");
  }
}
