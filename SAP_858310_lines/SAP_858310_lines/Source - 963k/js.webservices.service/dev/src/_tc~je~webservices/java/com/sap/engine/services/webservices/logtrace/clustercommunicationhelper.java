package com.sap.engine.services.webservices.logtrace;

import com.sap.engine.frame.cluster.ClusterElement;
import com.sap.engine.frame.cluster.ClusterException;
import com.sap.engine.frame.cluster.message.ListenerAlreadyRegisteredException;
import com.sap.engine.frame.cluster.message.MessageAnswer;
import com.sap.engine.frame.cluster.message.MessageContext;
import com.sap.engine.frame.cluster.message.MessageListener;
import com.sap.engine.services.webservices.espbase.server.runtime.metering.MeteringInMemoryRecordNotificator;
import com.sap.engine.services.webservices.espbase.server.runtime.metering.ServiceMeter;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

public class ClusterCommunicationHelper implements MessageListener {
  
  
  private ServiceMeter meter;
  private MessageContext messageContext;
  private static final Location LOC = Location.getLocation(ClusterCommunicationHelper.class);
  
  private static final Location LOCATION = Location.getLocation(ClusterCommunicationHelper.class);
  // TODO check which category to use
  private static final Category CATEGORY = Category.SYS_SERVER;
  
  public ClusterCommunicationHelper(MessageContext msgContext, ServiceMeter meter) {
    this.meter = meter;
    this.messageContext = msgContext;
    try {      
      messageContext.registerListener(this);
    } catch (ListenerAlreadyRegisteredException e) {
      LOC.traceThrowableT(Severity.WARNING, "[ctor] Error registering timeout listener", e);
      try {
        messageContext.unregisterListener();
        messageContext.registerListener(this);
      } catch (ListenerAlreadyRegisteredException e1) {
        throw new RuntimeException(e1);
      }
    }
  }
  
  public void receive(int clusterID, int messageType, byte[] message, int offset, int length) {
  }

  @Deprecated //no need for synchronous messaging when flushing to DB
  public MessageAnswer receiveWait(int clusterID, int messageType, byte[] message, int offset, int length) throws Exception {
    MessageAnswer ma = new MessageAnswer(new byte[]{0});
    switch(messageType){
    case ServiceMeter.AGGREGATE_METERING_DATA_MESSAGE:
      LOC.pathT("[receiveWait] Received AGGREGATE_METERING_DATA_MESSAGE message");
      startMeteringDataAggregation();
    }
    return ma;  }
  
  public void invalidate(){
    messageContext.unregisterListener();
  }
  
  private void startMeteringDataAggregation() {
    try{
      MeteringInMemoryRecordNotificator monitor = new MeteringInMemoryRecordNotificator();
      LOC.pathT("[startMeteringDataAggregation] Waiting for aggregation job to finish");
      synchronized (monitor) {
        meter.triggerAggregation(monitor);
        monitor.wait(30000);
      }
      LOC.pathT("[startMeteringDataAggregation] Stopped waiting, either job finished or timed out");
      
    }catch(Exception e){
      LOC.traceThrowableT(Severity.WARNING, "Error starting metering data aggregation", e);
    }
  }
  
  private void sendToAll(byte[] message,int messageType,int offset,int length){
    try{
      messageContext.send(-1, ClusterElement.SERVER, messageType, message, offset, length);
    } catch(ClusterException e) {
       LOCATION.traceThrowableT(Severity.DEBUG, 
                              "Failed to send message for flushing CallEntries to all server nodes",
                              e);
    }
  }

}
