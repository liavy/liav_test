package com.sap.sl.util.sduread.impl;

import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;

class MissingComponentInfoSduManifestException extends IllFormattedSduManifestException {
  MissingComponentInfoSduManifestException(String message, Throwable e) {
    super(message,e);
  }
  MissingComponentInfoSduManifestException(String message) {
    super(message);
  }
}