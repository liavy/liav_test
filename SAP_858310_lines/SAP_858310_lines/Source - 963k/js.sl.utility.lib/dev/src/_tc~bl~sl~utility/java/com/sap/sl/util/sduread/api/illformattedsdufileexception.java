package com.sap.sl.util.sduread.api;

/**
 * Thrown to indicate that an archive file does not satisfy a certain file
 * format.
 */

public class IllFormattedSduFileException extends SduFileException {
  public IllFormattedSduFileException(String message, Throwable e) {
    super(message,e);
  }
  public IllFormattedSduFileException(String message) {
    super(message);
  }
}
