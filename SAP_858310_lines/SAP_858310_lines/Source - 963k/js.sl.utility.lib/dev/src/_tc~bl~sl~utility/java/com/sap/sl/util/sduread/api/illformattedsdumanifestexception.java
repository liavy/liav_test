package com.sap.sl.util.sduread.api;

/**
 * Thrown to indicate that an manifest does not satisfy a certain manifest
 * format.
 */

public class IllFormattedSduManifestException extends SduManifestException {
  public IllFormattedSduManifestException(String message) {
    super(message);
  }
  public IllFormattedSduManifestException(String message, Throwable e) {
    super(message,e);
  }
}