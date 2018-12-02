package com.sap.sl.util.sduread.api;

/**
 * Thrown to indicate that an error concerning an SDU manifest has occurred.
 */

public abstract class SduManifestException extends Exception {
  SduManifestException(String message) {
    super(message);
  }
  SduManifestException(String message, Throwable e) {
    super(message,e);
  }
}