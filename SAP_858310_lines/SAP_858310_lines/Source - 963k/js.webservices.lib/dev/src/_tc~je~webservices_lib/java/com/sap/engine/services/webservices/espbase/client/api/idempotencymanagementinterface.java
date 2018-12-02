package com.sap.engine.services.webservices.espbase.client.api;

public interface IdempotencyManagementInterface {
  
  public void activateGlobalIdeopotency(boolean activate);
  
  public boolean isGlobalIdeopotencyActive();
  
  public void setRetrySleep(long sleep);
  
  public long getRetrySleep();
  
  public void setRetriesCount(int count);
  
  public int getRetriesCount();
}
