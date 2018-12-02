package com.sap.engine.services.webservices.espbase.xi;

import com.sap.engine.services.webservices.espbase.messaging.ESPXIMessage;

public interface XIClientServiceMetering {
  public void addMeteringDataToXIMessage(ESPXIMessage xiMsg);
}
