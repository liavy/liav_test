package com.sap.engine.services.ts;

import com.sap.tc.logging.Severity;
import com.sap.tc.logging.Location;


/**
 * Log utility for the transaction service
 *
 * @author Iliyan Nenov, ilian.nenov@sap.com
 * @version SAP J2EE Engine 6.30
 */
public class Log {

  private static final Location LOCATION = Location.getLocation(Log.class);
  private static final String[] STATUSES =
      {"STATUS_ACTIVE = 0",
       "STATUS_MARKED_ROLLBACK = 1",
       "STATUS_PREPARED = 2",
       "STATUS_COMMITTED = 3",
       "STATUS_ROLLEDBACK = 4",
       "STATUS_UNKNOWN = 5",
       "STATUS_NO_TRANSACTION = 6",
       "STATUS_PREPARING = 7",
       "STATUS_COMMITTING = 8",
       "STATUS_ROLLING_BACK = 9"};

  public static final String statusToString(int status) {
    try {
      return STATUSES[status];
    } catch (IndexOutOfBoundsException e) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "Status unknown. Full stacktrace: ", e);
      }
      return Integer.toString(status);
    }

  }

  public static final String objectToString(Object o) {
    return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
  }

}
