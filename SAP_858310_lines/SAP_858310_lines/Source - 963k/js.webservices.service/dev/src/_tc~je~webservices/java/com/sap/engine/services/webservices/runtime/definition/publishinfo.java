package com.sap.engine.services.webservices.runtime.definition;

import java.io.Serializable;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class PublishInfo implements Serializable {

  private String url = null;

  public PublishInfo() {

  }

  public PublishInfo(String url) {
    this.url = url;
  }

  public void setURL(String url) {
    this.url = url;
  }

  public String getURL() {
    return url;
  }

}

