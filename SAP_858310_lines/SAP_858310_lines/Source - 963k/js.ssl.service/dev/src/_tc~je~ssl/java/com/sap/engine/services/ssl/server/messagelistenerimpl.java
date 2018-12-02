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

import com.sap.engine.frame.cluster.message.MessageContext;
import com.sap.engine.frame.cluster.message.MessageListener;
import com.sap.engine.frame.cluster.message.MessageAnswer;
import com.sap.engine.frame.cluster.ClusterException;

import com.sap.engine.frame.cluster.ClusterElement;
import com.sap.engine.services.ssl.exception.SSLResourceAccessor;
import com.sap.engine.services.ssl.keystore.KeyStoreConnector;
import com.sap.tc.logging.Severity;

import java.io.UnsupportedEncodingException;


/**
 * @author Ilia Kacarov
 *         Copyright (c) 2002, SAP-AG
 */
public class MessageListenerImpl implements MessageListener {

  private static final int ICM_RELOAD_PSE = 1;
  private static final int J2EE_SSL_NOTIFY_FOR_PSE_UPDATE = 2;
  private static final int PSE_UPDATE_OK = 3;
  private static final int PSE_INITIAL_CHECK_OK = 4;
  
  private static MessageContext messageContext = null;
  private static int currentGroupID = -1;
  private static int currentNodeID = -1;
  private static String nodeIdAsString = null;
  private static byte[] nodeIdAsBytes = null;
  
  public static final synchronized void setMessageContext(MessageContext message_Context)  {
    messageContext = message_Context;
  }
  
  public static final synchronized void setCurrentGroupAndNodeID(int groupID, int nodeID) {
    currentGroupID = groupID;
    currentNodeID = nodeID;
    nodeIdAsString = Integer.toString(nodeID); 
    nodeIdAsBytes = nodeIdAsString.getBytes();
  }
  
  
//  public static void notify_for_successfull_update() {
//    ServerService.dump("notify_for_successfull_update { ");
//    
//    try {
//      messageContext.send(currentGroupID, ClusterElement.SERVER , PSE_UPDATE_OK, nodeIdAsBytes, 0, nodeIdAsBytes.length);
//
//      ServerService.dump("notify_for_successfull_update } ok");
//    } catch (ClusterException _) {
//      SSLResourceAccessor.traceThrowable(Severity.WARNING, " notify_for_successfull_update  ", _);
//      ServerService.dump("notify_for_successfull_update } ERR1: ", _);
//    } catch (Exception exc) {
//      ServerService.dump("notify_for_successfull_update } ERR2: ", exc);
//    }
//  }
  
  public static void notify_for_successfull_initial_check() {
    ServerService.dump("notify_for_successfull_initial_check { ");
    
    try {
      messageContext.send(currentGroupID, ClusterElement.SERVER, PSE_INITIAL_CHECK_OK, nodeIdAsBytes, 0, nodeIdAsBytes.length);

      ServerService.dump("notify_for_successfull_initial_check } ok");
    } catch (ClusterException _) {
      SSLResourceAccessor.traceThrowable(Severity.ERROR, " notify_for_successfull_initial_check", _);
      ServerService.dump("notify_for_successfull_initial_check } ERR1: ", _);
    } catch (Exception exc) {
      ServerService.dump("notify_for_successfull_initial_check } ERR2: ", exc);
    }
  }
  
  public static void notify_ICM_for_PSE_change()  {
    ServerService.dump("notify_ICM_for_PSE_change { ");
    try {
      messageContext.send(currentGroupID, ClusterElement.ICM, ICM_RELOAD_PSE, new byte[0], 0, 0);

      ServerService.dump("notify_ICM_for_PSE_change } ok");
    } catch (ClusterException _) {
      SSLResourceAccessor.traceThrowable(Severity.ERROR, " ICM from group " + currentGroupID + " not notified for PSE change", _);
      ServerService.dump("notify_ICM_for_PSE_change } ERR1: ", _);
    } catch (Exception exc) {
      ServerService.dump("notify_ICM_for_PSE_change } ERR2: ", exc);
    }
  }
  
  public static void notify_other_group_for_PSE_change(int otherGroupID, String pseViewName)  {
    ServerService.dump("notify_other_group_for_PSE_change: ");
     
    try {
      messageContext.send(otherGroupID, ClusterElement.SERVER, J2EE_SSL_NOTIFY_FOR_PSE_UPDATE, new byte[0], 0, 0);
      ServerService.dump("notify_other_group_for_PSE_change } ok");
    } catch (ClusterException _) {
      SSLResourceAccessor.traceThrowable(Severity.ERROR, " other SSL nodes from group " + currentGroupID + " not notified for PSE change", _);
      ServerService.dump("notify_other_group_for_PSE_change } ERR1: ", _);
    } catch (Exception exc) {
      ServerService.dump("notify_other_group_for_PSE_change } ERR2: ", exc);
    }
  }

  public static final synchronized void start() throws Exception {
    if (messageContext == null || currentGroupID == -1) {// not yet initialized
      throw new RuntimeException("MessageListener not fully initialized");
    }

    messageContext.registerListener(new MessageListenerImpl());
    ServerService.dump("SSL registered as listener");
  }

  public static final void stop() {
    messageContext.unregisterListener();
    ServerService.dump("SSL unregistered as listener");
  }

  public void receive(int clusterID, int messageType, byte[] bytes, int offset, int len) {
    ServerService.dump(" receive  { " + messageType + "] from node " + clusterID);



    if (bytes != null && nodeIdAsString.equals(new String(bytes,  offset, len))) {
      ServerService.dump(" receive } ok: message from the same node");
      return;
    }
    
    
    if (messageType == J2EE_SSL_NOTIFY_FOR_PSE_UPDATE) {
      ServerService.dump(" received message type J2EE_SSL_NOTIFY_FOR_PSE_UPDATE from node " + clusterID);
      KeyStoreConnector.preparePSE(); 
    } else if (messageType == PSE_INITIAL_CHECK_OK) {
      KeyStoreConnector.initialPseCheckDone();
    } else  {
      ServerService.dump(" received unknown message type [" + messageType + "] from node " + clusterID);
    }
  }

  public MessageAnswer receiveWait(int i, int i1, byte[] bytes, int i2, int i3) throws Exception {
    return null;  //not used 
  }

  private static final byte[] stringToBytes(String str) {
    try {
      return str.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      SSLResourceAccessor.traceThrowable(Severity.WARNING, "MessageListenerImpl.stringToBytes() failed, using the default encoding[" + System.getProperty("file.encoding") + "]", e);
      return str.getBytes();
    }
  }
  private static final String bytesToString(byte[] bytes, int offset, int len) {
    try {
      return new String(bytes, offset, len, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      SSLResourceAccessor.traceThrowable(Severity.WARNING, "MessageListenerImpl.stringToBytes() failed, using the default encoding[" + System.getProperty("file.encoding") + "]", e);
      return new String(bytes, offset, len);
    }
  }

}
