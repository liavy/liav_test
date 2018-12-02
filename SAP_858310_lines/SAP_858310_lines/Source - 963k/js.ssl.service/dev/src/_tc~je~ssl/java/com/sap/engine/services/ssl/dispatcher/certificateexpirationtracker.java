package com.sap.engine.services.ssl.dispatcher;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

import com.sap.engine.services.ssl.keystore.KeyStoreConnector;
import com.sap.engine.services.ssl.exception.KeyStoreConnectorException;
import com.sap.engine.services.ssl.exception.SSLResourceAccessor;
import com.sap.engine.services.ssl.factory.Credentials;
import com.sap.tc.logging.Severity;

public class CertificateExpirationTracker {

  private final static String STATE_UNKNOWN = "N/A";
  private final static String STATE_EXPIRES_AFTER_A_YEAR = "No certificates expire in a year time.";
  private final static String STATE_EXPIRES_IN_A_YEAR = "At least one certificate expires in less than 1 year.";
  private final static String STATE_EXPIRES_IN_3_MONTHS = "At least one certificate expires in less than 3 months.";
  private final static String STATE_EXPIRES_IN_1_MONTH = "At least one certificate expires in less than 1 month.";
  private final static String STATE_EXPIRES_IN_1_WEEK = "At least one certificate expires in less than 1 week.";
  private final static String STATE_EXPIRES_IN_5_DAYS = "At least one certificate expires in less than 5 days.";
  private final static String STATE_EXPIRES_IN_4_DAYS = "At least one certificate expires in less than 4 days.";
  private final static String STATE_EXPIRES_IN_3_DAYS = "At least one certificate expires in less than 3 days.";
  private final static String STATE_EXPIRES_IN_2_DAYS = "At least one certificate expires in less than 2 days.";
  private final static String STATE_EXPIRES_IN_1_DAY = "At least one certificate expires in less than 1 day.";
  private final static String STATE_EXPIRED = "At least one certificate has already expired.";
  private final static String STATE_MISSING_CERTIFICATES = "Missing certificates in keystore.";

  private static final String GET_CERTIFICATE_ERROR="ssl_cert_expiration_tracker_GET_CERTIFICATE_ERROR";


  private final static long ONE_DAY = 24 * 60 * 60 * 1000;
  private final static long ONE_MONTH = ONE_DAY * 31;
  private final static long ONE_YEAR = ONE_DAY * 365;

  private static long lastExpirationCheck = 0;
  private static String expirationStatus = STATE_UNKNOWN;

  public static String getExpirationStatus() {
    checkExpiration();
    return expirationStatus;
  }



  public static int getDaysToExpirationOfCertificate(String alias) {
    try {
      long currentTime = System.currentTimeMillis();
      Certificate certificate = null;
      certificate = getCertificate(alias);

      if (certificate instanceof X509Certificate) {
        X509Certificate x509certificate = (X509Certificate) certificate;

        if (differenceNotBefore(x509certificate.getNotBefore(), currentTime, 0) == -1) {
          return 0;
        }

        return (int) (differenceNotAfter(x509certificate.getNotAfter(), currentTime, Long.MAX_VALUE) / ONE_DAY);
      }
    } catch (KeyStoreConnectorException ksce) {
      SSLResourceAccessor.log(Severity.ERROR, ksce,GET_CERTIFICATE_ERROR, new Object[]{alias});
      SSLResourceAccessor.traceThrowable(Severity.ERROR, GET_CERTIFICATE_ERROR, new Object[]{alias}, ksce);

      expirationStatus = STATE_MISSING_CERTIFICATES;
    }
    return -1;
  }

  public static final String getCertificateAlgorithm(String alias) {
    Certificate certificate = getCertificate(alias);

    if (certificate != null) {
      return certificate.getPublicKey().getAlgorithm();
    }
    return "N/A";
  }

  public static void portsConfigurationChanged() {
    lastExpirationCheck = 0;
  }

  private static final Certificate getCertificate(String alias) {
    Certificate result = KeyStoreConnector.getCertificate(alias);

    if (result == null) {
      Credentials credentials = KeyStoreConnector.getCredentials(alias);
      if (credentials != null) {
        result = credentials.getCertificateChain()[0];
      }
    }

    return result;
  }

  private static void checkExpiration() {
    long currentTime = System.currentTimeMillis();

    if (currentTime < lastExpirationCheck + (ONE_DAY / 2)) {
      // last check was performed soon enough
      return;
    } else {
      lastExpirationCheck = currentTime;
    }

    long difference = Long.MAX_VALUE;

    String[] aliases = SSLPortsConfiguration.getUsedCredentialAliases();
    Certificate certificate = null;

    for (int i = 0; i < aliases.length; i++) {
      try {
        certificate = getCertificate(aliases[i]);
      } catch (KeyStoreConnectorException ksce) {
        SSLResourceAccessor.log(Severity.ERROR, ksce, GET_CERTIFICATE_ERROR, new Object[]{aliases[i]});
        SSLResourceAccessor.traceThrowable(Severity.ERROR, GET_CERTIFICATE_ERROR, new Object[]{aliases[i]}, ksce);

        expirationStatus = STATE_MISSING_CERTIFICATES;
        return;
      }
      if (certificate instanceof X509Certificate) {
        X509Certificate x509certificate = (X509Certificate) certificate;

        difference = differenceNotBefore(x509certificate.getNotBefore(), currentTime, difference);
        difference = differenceNotAfter(x509certificate.getNotAfter(), currentTime, difference);
      }
    }


    if (difference < 0) {
      expirationStatus = STATE_EXPIRED;
    } else if (difference < ONE_DAY) {
      expirationStatus = STATE_EXPIRES_IN_1_DAY;
    } else if (difference < ONE_DAY * 2) {
      expirationStatus = STATE_EXPIRES_IN_2_DAYS;
    } else if (difference < ONE_DAY * 3) {
      expirationStatus = STATE_EXPIRES_IN_3_DAYS;
    } else if (difference < ONE_DAY * 4) {
      expirationStatus = STATE_EXPIRES_IN_4_DAYS;
    } else if (difference < ONE_DAY * 5) {
      expirationStatus = STATE_EXPIRES_IN_5_DAYS;
    } else if (difference < ONE_DAY * 7) {
      expirationStatus = STATE_EXPIRES_IN_1_WEEK;
    } else if (difference < ONE_MONTH) {
      expirationStatus = STATE_EXPIRES_IN_1_MONTH;
    } else if (difference < ONE_MONTH * 3) {
      expirationStatus = STATE_EXPIRES_IN_3_MONTHS;
    } else if (difference < ONE_YEAR) {
      expirationStatus = STATE_EXPIRES_IN_A_YEAR;
    } else {
      expirationStatus = STATE_EXPIRES_AFTER_A_YEAR;
    }
  }

  private final static long differenceNotBefore(Date notBefore, long current, long lastBest) {
    return (current < notBefore.getTime()) ? -1 : lastBest;
  }

  private final static long differenceNotAfter(Date notAfter, long current, long lastBest) {
    long notAfterValue = notAfter.getTime();

    if (current > notAfterValue) {
      return -1;
    } else {
      long currentValue = notAfterValue - current;
      return (currentValue < lastBest) ? currentValue : lastBest;
    }
  }

}
