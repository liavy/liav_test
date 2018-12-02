package com.sap.engine.services.servlets_jsp.server;

import com.sap.engine.frame.cluster.ClusterElement;

import java.util.Vector;

public class ClusterContext {
  private com.sap.engine.frame.cluster.ClusterContext clusterContext = null;

  public ClusterContext(com.sap.engine.frame.cluster.ClusterContext clusterContext) {
    this.clusterContext = clusterContext;
  }

  public ClusterElement getClusterElement(int clusterId) {
    return clusterContext.getClusterMonitor().getParticipant(clusterId);
  }

  public int getCurrentClusterId() {
    return clusterContext.getClusterMonitor().getCurrentParticipant().getClusterId();
  }

  // TODO - obsolete method
  public String getInstanceName(int serverId) {
    //todo - send the instance name to all server nodes
    int groupId = getGroupId(serverId);
    if (groupId <= 0) {
      return null;
    } else {
      return Integer.toString(groupId);
    }
  }

  public int getGroupId(int serverId) {
    ClusterElement clusterElement = clusterContext.getClusterMonitor().getParticipant(serverId);
    if (clusterElement == null) {
      return -1;
    }
    return clusterElement.getGroupId();
  }

  public String[] getAllServerNamesInInstance() {
    int serverId = getCurrentClusterId();

    int groupId = getGroupId(serverId);
    if (groupId <= 0) {
      return new String[]{getClusterElement(serverId).getName()};
    }

    Vector result = new Vector();
    result.add(getClusterElement(serverId).getName());

    ClusterElement[] clusterElements = clusterContext.getClusterMonitor().getParticipants();
    for (int i = 0; clusterElements != null && i < clusterElements.length; i++) {
      ClusterElement element = clusterElements[i];
      if (groupId == element.getGroupId() && element.getType() == ClusterElement.SERVER) {
        result.add(element.getName());
      }
    }

    if (LogContext.getLocationService().beDebug()) {
    	LogContext.getLocationService().debugT("ClusterContext.getAllServerNamesInInstance(): get result " + result.toString());
    }
    
    return (String[]) result.toArray(new String[result.size()]);
  }//end of getParticipantsByGroupId(int groupId)
}
