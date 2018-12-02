package com.sap.engine.services.webservices.runtime.monitor;

/**
 * Copyright (c) 2003 by SAP Labs Bulgaria., 
 * All rights reserved.
 *
 * @author Dimitar Angelov
 */
public class WSOperationData {
  long nomInv;
  long accPrePrcssTime;
  long accImplPrcssTime;
  long accPostPrcssTime;

  String operationName;

  public WSOperationData(String opName) {
    this.operationName = opName;
  }

  public void registerOperationTimes(long prePrcssTime, long implPrcssTime, long postPrcssTime) {
    this.accPrePrcssTime += prePrcssTime;
    this.accImplPrcssTime += implPrcssTime;
    this.accPostPrcssTime += postPrcssTime;
    this.nomInv++;
  }

  public long getNomInv() {
    return nomInv;
  }

  public long getAccPrePrcssTime() {
    return accPrePrcssTime;
  }

  public long getAccImplPrcssTime() {
    return accImplPrcssTime;
  }

  public long getAccPostPrcssTime() {
    return accPostPrcssTime;
  }

  public long getAveragePrePrcssTime() {
    if (nomInv > 0) {
      return accPrePrcssTime / nomInv;
    }
    return 0;
  }

  public long getAverageImplPrcssTime() {
    if (nomInv > 0) {
      return accImplPrcssTime / nomInv;
    }
    return 0;
  }

  public long getAveragePostPrcssTime() {
    if (nomInv > 0) {
      return accPostPrcssTime / nomInv;
    }
    return 0;
  }

}
