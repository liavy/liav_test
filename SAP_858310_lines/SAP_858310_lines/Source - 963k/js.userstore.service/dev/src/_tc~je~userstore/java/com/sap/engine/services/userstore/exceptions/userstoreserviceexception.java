package com.sap.engine.services.userstore.exceptions;

import com.sap.engine.frame.ServiceException;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Severity;

public class UserstoreServiceException  extends ServiceException {
  public static final String UNEXPECTED_SERVICE_ERROR = "userstore_0000";
  public static final String UNEXPECTED_SERVER_LOCKING_ERROR = "userstore_0002";

  public UserstoreServiceException(String key, Throwable e) {
    super(new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, null), e);
    super.setLogSettings(UserstoreResourceAccessor.category, Severity.ERROR, UserstoreResourceAccessor.location);
  }

  public UserstoreServiceException(String key) {
    super(new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, null), null);
    super.setLogSettings(UserstoreResourceAccessor.category, Severity.ERROR, UserstoreResourceAccessor.location);
  }

  public UserstoreServiceException(String key, Object[] args, Throwable e) {
    super(new LocalizableTextFormatter(UserstoreResourceAccessor.getResourceAccessor(), key, args), e);
    super.setLogSettings(UserstoreResourceAccessor.category, Severity.ERROR, UserstoreResourceAccessor.location);
  }
}
