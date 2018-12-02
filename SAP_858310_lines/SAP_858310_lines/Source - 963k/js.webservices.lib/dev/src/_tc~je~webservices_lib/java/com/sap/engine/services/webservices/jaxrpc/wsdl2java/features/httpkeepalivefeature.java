/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java.features;

/**
 * Connection Timeout feature.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public interface HTTPKeepAliveFeature {
  public static final String KEEP_ALIVE_FEATURE = "http://www.sap.com/webas/630/soap/features/http/keepAlive";
  public static final String KEEP_ALIVE_PROPERTY = "keepAliveStatus";
  public static final String KEEP_ALIBE_TRUE = "true";

  public static final String COMPRESS_RESPONSE_FEATURE = "http://www.sap.com/webas/630/soap/features/http/gzip";
  public static final String COMPRESS_RESPONSE_PROPERTY = "compressResponse";
  public static final String COMPRESS_RESPONSE_TRUE = "true";

}
