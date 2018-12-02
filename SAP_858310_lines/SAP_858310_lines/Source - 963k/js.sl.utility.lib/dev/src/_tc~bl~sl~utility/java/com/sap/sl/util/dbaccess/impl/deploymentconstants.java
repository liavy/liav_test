package com.sap.sl.util.dbaccess.impl;

/**
 * <p>Constants for sda-dd.xml. </p>
 * 
 * @author Uli Auer
 * @version 1.0
 */

interface DeploymentConstants
{
  // derived from com.sap.sdm.util.deployment.DeploymentConstants.java
  
    // elements of SDA-DD
    static final String SDADD_ROOT                    = "SDA";
    static final String SDADD_SOFTWARETYPE            = "SoftwareType";
  
    // attributes of SDA-DD
    static final String SDADD_ROOT_NAME               = "name";
  
  
  // derived from com.sap.sdm.util.Constants
    
    // SoftwareTypes
    final static String SOFTWARETYPE_DBSC     = "DBSC";        // import DB content into DBMS
}