package com.sap.engine.services.webservices.espbase.client.dynamic.util;

public class InternalCacheUnit {
  
  private Object key;
  private Object value;
  private InternalCacheUnit previousUnit;
  private InternalCacheUnit nextUnit;
  private int creationIndex;
  private int uses;
  
  protected InternalCacheUnit() {
    uses = 0;
  }
  
  protected Object getKey() {
    return(key);
  }

  protected void setKey(Object key) {
    this.key = key;
  }

  protected Object getValue() {
    return(value);
  }

  protected void setValue(Object value) {
    this.value = value;
  }

  protected InternalCacheUnit getNextUnit() {
    return(nextUnit);
  }

  protected void setNextUnit(InternalCacheUnit nextUnit) {
    this.nextUnit = nextUnit;
  }

  protected InternalCacheUnit getPreviousUnit() {
    return(previousUnit);
  }

  protected void setPreviousUnit(InternalCacheUnit previousUnit) {
    this.previousUnit = previousUnit;
  }
  
  protected void use() {
    uses++;
  }
  
  protected void clearUses() {
    uses = 0;
  }
  
  protected void setCreationIndex(int creationIndex) {
    this.creationIndex = creationIndex;
  }
  
  protected double getUseFrequency(int lastCreationIndex) {
    int periodRating = lastCreationIndex - creationIndex;
    return(periodRating == 0 ? Double.MAX_VALUE : ((double)uses)/periodRating);
  }
}
