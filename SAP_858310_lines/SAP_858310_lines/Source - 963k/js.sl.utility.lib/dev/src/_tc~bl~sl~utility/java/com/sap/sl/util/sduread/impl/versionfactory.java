package com.sap.sl.util.sduread.impl;

import java.math.BigInteger;
import java.util.StringTokenizer;
import com.sap.sl.util.sduread.api.Version;
import com.sap.sl.util.sduread.api.VersionFactoryIF;

/**
 * A factory for creating versions and implementation versions.
 */

final class VersionFactory implements VersionFactoryIF {
  VersionFactory() {
  }
  /**
   * Checks whether the specified version string is valid. A valid version 
   * string is a <code>String</code> consisting of a finite alternating 
   * sequence of non-negative integer numbers and dots, starting and 
   * ending with a number. 
   * 
   * <p>
   * Examples of valid version strings are &quot;1.2.3&quot;, 
   * &quot;0&quot; and &quot;001.200.000300&quot;. 
   * </p>
   * <p>
   * Examples of invalid version strings are &quot;1. 2. 3&quot; (spaces 
   * between numbers and dots), &quot;&quot;,
   * &quot;.1&quot;, &quot;SAP6.20&quot;, &quot;1.2.-3&quot; and 
   * &quot;12,34.5&quot;.
   * </p>
   * 
   * @param versionString a <code>String</code> representation of a version
   * @return <code>true</code> if the specified version string is valid; 
   *          <code>false</code> if it is not valid or <code>null</code>
   */
  public boolean isValidVersionString(String versionString) {
    if (versionString == null) {
      return false;
    }
    
    String[] stringTokens = getStringTokens(versionString);
    
    if (stringTokens.length == 0) {
      return false;
    }
    
    for (int i=0; i < stringTokens.length; i++) {
      BigInteger bigI;
      try {
        bigI = new BigInteger(stringTokens[i]);
      } catch (NumberFormatException e) {
        return false;
      }
      
      if (bigI.compareTo(BigInteger.ZERO) < 0) {
        return false;
      }
    }
    
    return true;
  }
  
  /**
   * Creates a <code>Version</code> from the specified valid version string. 
   * A valid version string is a <code>String</code> consisting of a finite 
   * alternating sequence of non-negative integer numbers and dots, 
   * starting and ending with a number. 
   * 
   * @param versionString a valid version string
   * @return a <code>Version</code> representing the version specified by 
   *          <code>versionString</code>
   * @throws IllegalArgumentException if <code>versionString</code> is not a 
   *          valid version string
   * @throws NullPointerException if <code>versionString</code> is 
   *          <code>null</code>
   * @see #isValidVersionString(String)
   */
  public Version createVersion(String versionString) {
    paramCheck("createVersion", versionString);
    
    if (isValidVersionString(versionString) == false) {
      throw new IllegalArgumentException("Invalid version string " + versionString);
    }
    
    return new VersionImpl(getStringTokens(versionString));
  }
  
  /**
   * Checks whether the specified sequence of <code>BigInteger</code>s defines
   * a version. A valid sequence of <code>BigInteger</code>s is non-empty and
   * contains exclusively <code>BigInteger</code>s that are non-negative.
   * 
   * @param bigInts a sequence of <code>BigInteger</code>s
   * @return <code>true</code> if the sequence defines a <code>Version</code>
   *          <code>false</code> if it is not a valid sequence or <code>null</code>
   */
  public boolean isValidSequence(BigInteger[] bigInts) {
    if (bigInts == null || bigInts.length == 0) {
      return false;
    }

    for (int i=0; i < bigInts.length; i++) {
      if (bigInts[i].compareTo(BigInteger.ZERO) < 0) {
        return false;
      }
    }

    return true;
  }
  
  /**
   * Creates a <code>Version</code> from the specified valid sequence of 
   * non-negative integer numbers. 
   * 
   * @param bigInts a valid sequence of non-negative integer numbers
   * @return a <code>Version</code> representing the version specified by 
   *          <code>bigInts</code>
   * @throws IllegalArgumentException if <code>bigInts</code> is not a valid 
   *          sequence 
   * @throws NullPointerException if <code>bigInts</code> is <code>null</code>
   * @see #isValidVersionSequence(BigInteger[])
   */
  public Version createFromBigInts(BigInteger[] bigInts) {
    paramCheck("createFromBigInts", bigInts);
    
    if (isValidSequence(bigInts) == false) {
      throw new IllegalArgumentException("Invalid version sequence");
    }
    
    return new VersionImpl( toStrings(bigInts) );
  }
  
  private String[] getStringTokens(String versionString) {
    StringTokenizer tok = new StringTokenizer(versionString, VersionImpl.DOT_STRING);
    String[] result = new String[ tok.countTokens() ];
    for (int i=0; i < result.length; i++) {
      result[i] = tok.nextToken();
    }
    
    return result;
  }
  
  private String[] toStrings(BigInteger[] bigInts) {
    String[] result = new String[ bigInts.length ];
    
    for (int i=0; i < result.length; i++) {
      result[i] = bigInts[i].toString();
    }
    
    return result;
  }
  
  /**
   * Checks whether the specified parameter object is not <code>null</code>.
   * 
   * @param methodName name of method to which the parameter has been passed
   * @param param the parameter object to be checked
   * @throws NullPointerException if <code>param</code> is <code>null</code>
   */
  private void paramCheck(String methodName, Object param) {
    if (param != null) {
      return;
    }
    
    String errText = VersionFactory.class.getName() + "." + methodName +": parameter is null";
    throw new NullPointerException(errText);
  }
}
