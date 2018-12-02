package com.sap.sl.util.sduread.api;

/**
 * Title: sdm
 * Description: Software Deployment Manager
 * Copyright:   Copyright (c) 2001
 * Company:     SAP AG
 * @author
 * @version 1.0
 */

/**
 *  The SDA-SDA compatible version information (compsdaversinfo and extcompsdaversinfo) have to be incremented each time when
 *  the SDA format was changed in a way that an older SDM cannot handle it in a correct way.
 */

public interface VersionInfoCompatibility {
  //this versioninfo is relevant for a 620 SDM
  public static final int compsdaversinfo=       1; // this int value must be incremented
  //this versioninfo is relevant for a 630 SDM
  public static final int extcompsdaversinfo=    1; // this int value must be incremented
}