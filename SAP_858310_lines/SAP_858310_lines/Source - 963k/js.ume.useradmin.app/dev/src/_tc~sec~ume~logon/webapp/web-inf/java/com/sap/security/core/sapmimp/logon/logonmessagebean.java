package com.sap.security.core.sapmimp.logon;

import com.sap.security.core.util.ResourceBean;
import java.util.Locale;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.InternalUMFactory;

/**
 * Title:        UM3 Authentication
 * Description:  UM3 Authentication
 * Copyright:    Copyright (c) 2001
 * Company:      SAPMarkets, Inc
 * 
 * @author Tim Tianwen Feng
 * @version 1.0
 */
public class LogonMessageBean extends ResourceBean {
  public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~logon/webapp/WEB-INF/java/com/sap/security/core/sapmimp/logon/LogonMessageBean.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
  private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

  public static final String beanId = "logonMessage";
  
  private static final String baseName = "logonMessages";
  private static final String CONSTRUCTOR = "constructor";

  public LogonMessageBean(Locale locale) {
    super(locale, baseName);
  }

  public LogonMessageBean() {
    super(Locale.getDefault(), baseName);
    trace.infoT(CONSTRUCTOR,"UME fallback: default LogonMessageBean created");
  }
}



