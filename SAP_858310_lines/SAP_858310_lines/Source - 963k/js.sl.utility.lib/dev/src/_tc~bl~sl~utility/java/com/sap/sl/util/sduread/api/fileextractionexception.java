package com.sap.sl.util.sduread.api;

/**
 * Thrown to indicate that an error occured while extracting an archive file
 * from another archive file. 
 */

public final class FileExtractionException extends SduFileException {
  public FileExtractionException(String message) {
    super(message);
  }
  public FileExtractionException(String message, Throwable e) {
    super(message,e);
  }
}
