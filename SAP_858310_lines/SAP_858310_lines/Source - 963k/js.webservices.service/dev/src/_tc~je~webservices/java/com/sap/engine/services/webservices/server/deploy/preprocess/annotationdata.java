/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

package com.sap.engine.services.webservices.server.deploy.preprocess;

/**
 * Title: AnnotationData
 * Description: AnnotationData 
 * 
 * @author Tatyana Nalbantova
 * @version
 */

public class AnnotationData {
    
    private String portType;
    private String serviceName;
    private String className;
  private String altPath; 
  
  public void setBindingDataName(String name) {
    this.portType = name;
  }
    
    public String getBindingDataName() {
        return this.portType;
    }
    
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return this.serviceName;
    }
    
    public void setFullClassName(String name) {
        this.className = name;
    }
    
    public String getFullClassName() {
        return className;
    }

  public void setAltPath(String altPath) {
    this.altPath = altPath;
  }
   
  public String getAltPath() {
    return altPath;
  }
 
}
