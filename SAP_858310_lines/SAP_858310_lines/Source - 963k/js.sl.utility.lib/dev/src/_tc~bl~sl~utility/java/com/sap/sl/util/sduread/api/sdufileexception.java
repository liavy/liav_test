package com.sap.sl.util.sduread.api;

/**
 * Thrown to indicate that an error concerning an SDU file has occurred.
 */

public abstract class SduFileException extends SduManifestException {
  SduFileException(String message) {
    super(message);
  }
  SduFileException(String message, Throwable e) {
    super(message,e);
  }
}
