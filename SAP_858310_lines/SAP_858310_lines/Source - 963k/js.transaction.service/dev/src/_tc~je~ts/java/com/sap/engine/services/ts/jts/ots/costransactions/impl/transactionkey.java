package com.sap.engine.services.ts.jts.ots.CosTransactions.impl;

import com.sap.engine.services.ts.Util;
import com.sap.tc.logging.Severity;
import com.sap.tc.logging.Location;

import java.util.Arrays;
import java.util.zip.CRC32;

import org.apache.derby.iapi.services.io.ArrayUtil;

/**
 * This is implementation of control object using OTS API
 *
 * @author Iliyan Nenov, ilian.nenov@sap.com
 * @version SAP J2EE Engine 6.30
 */
public class TransactionKey {

  private static final Location LOCATION = Location.getLocation(TransactionKey.class);
  /**
   * Cached toString() result
   */
  private String toString = null;
  /* the otid for a propagated transaction */
  private byte[] otid = null;

  /**
   * Constructor for the Transaction key
   *
   * @param otid
   */
  public TransactionKey(byte[] otid) {
    this.otid = otid;
  }


  public int hashCode() {
    CRC32 crc = new CRC32();
    crc.update(otid);
    return (int)crc.getValue();
  }


  /**
   * Compares two byte arrays
   *
   * @param otid
   * @return true if the two byte arrays are eqivalent else returns false
   */
  public boolean equals(Object otid) {
    try {
      byte[] newOtid = ((TransactionKey)otid).otid;
      return Arrays.equals(this.otid, newOtid);
    } catch (ClassCastException cce) {
      if (LOCATION.beLogged(Severity.DEBUG)) {
        LOCATION.traceThrowableT(Severity.DEBUG, "Error occurred. Full stacktrace: ", cce);
      }
      return false;
    }
  }

  /**
   * @return String representation of this transaction key
   */
  public String toString() {
    if (toString == null) {
      StringBuffer sb = new StringBuffer();
      sb.append("tx tid:");
      for(int i = 0; i < otid.length; i++) {
        sb.append(Integer.toHexString(otid[i]).toUpperCase());
      }
      sb.append(":");
      toString = sb.toString();
    }
    return toString;
  } // toString()

  public static String toString(TransactionKey txKey) {
    return txKey == null ? "null" : txKey.toString();
  }
}
