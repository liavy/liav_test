package com.sap.engine.services.webservices.dispatcher;

import com.sap.engine.lib.util.HashMapIntInt;
import com.sap.engine.lib.util.HashMapIntObject;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSCommunicationProcessor {

  private HashMapIntInt connectionApplicationMapping = new HashMapIntInt();
  private HashMapIntObject wsClientSockets = new HashMapIntObject();

  public WSCommunicationProcessor() {
  
  }
  
}