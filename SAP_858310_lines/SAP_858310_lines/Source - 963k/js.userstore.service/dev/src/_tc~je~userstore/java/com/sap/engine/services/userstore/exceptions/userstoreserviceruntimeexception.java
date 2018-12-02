package com.sap.engine.services.userstore.exceptions;

import com.sap.engine.frame.ServiceRuntimeException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Severity;

public class UserstoreServiceRuntimeException  extends ServiceRuntimeException {
  public static final String SERVICE_STOPPING_ERROR = "userstore_0001";

  public UserstoreServiceRuntimeException(String key, Throwable e) {
    super(new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, null), e);
    super.setLogSettings(UserstoreResourceAccessor.category, Severity.ERROR, UserstoreResourceAccessor.location);
  }

  public UserstoreServiceRuntimeException(String key) {
    super(new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, null), null);
    super.setLogSettings(UserstoreResourceAccessor.category, Severity.ERROR, UserstoreResourceAccessor.location);
  }

  public UserstoreServiceRuntimeException(String key, Object[] args, Throwable e) {
    super(new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, args), e);
    super.setLogSettings(UserstoreResourceAccessor.category, Severity.ERROR, UserstoreResourceAccessor.location);
  }
}

