package com.sap.security.core.sapmimp.logon;

import java.util.Locale;

import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.ResourceBean;

/**
 * Title:        UM3 Authentication
 * Description:  UM3 Authentication
 * Copyright:    Copyright (c) 2001
 * Company:      SAPMarkets, Inc
 * 
 * @author Tim Tianwen Feng
 * @version 1.0
 */
public class LogonLocaleBean extends ResourceBean {
  public final static String VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~logon/webapp/WEB-INF/java/com/sap/security/core/sapmimp/logon/LogonLocaleBean.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
  private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

  public static final String beanId = "logonLocale";

  private static final String labelBaseName = "logonLabels";
  private static final String pageBaseName = "logonPages";
  private static final String CONSTRUCTOR = "constructor";
  
  public LogonLocaleBean(Locale locale) {
    super(locale, labelBaseName, pageBaseName);
  }

  public LogonLocaleBean() {
    super(Locale.getDefault(), labelBaseName, pageBaseName);
    trace.infoT(CONSTRUCTOR,"UME fallback: default LogonLocaleBean created");
  }
}




