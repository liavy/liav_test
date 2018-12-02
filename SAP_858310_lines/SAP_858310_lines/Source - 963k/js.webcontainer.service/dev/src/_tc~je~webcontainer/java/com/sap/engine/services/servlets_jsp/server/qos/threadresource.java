package com.sap.engine.services.servlets_jsp.server.qos;

import com.sap.engine.lib.rcm.Resource;

public class ThreadResource implements Resource {

  private String name;
  private int quantity;

  public ThreadResource(String name, int quantity) {
      this.name = name;
      this.quantity = quantity;
  }
     
  void setTotalQuantity(int newQuantity) {
      this.quantity = newQuantity;
  }
  
  public String getName() {
      return name;
  }

  public long getTotalQuantity() {
      return quantity;
  }

  public String getUnitName() {
      return "thr";
  }

  public boolean isDisposable() {
      return true;
  }

  public boolean isUnbounded() {
      return true;
  }

}
