package com.sap.engine.services.webservices.espbase.server.runtime.metering;

import java.sql.Timestamp;

class MeteringRecordKey 
{  
  long callerId, operationId;

  Timestamp beginTS, endTS;

  int month, year;

  public MeteringRecordKey(long callerId, long operationId, Timestamp beginTS, Timestamp endTS, int month, int year) {
    this.callerId    = callerId;
    this.operationId = operationId;
    this.beginTS     = beginTS;
    this.endTS       = endTS;
    this.month       = month;
    this.year        = year;
  }
}
