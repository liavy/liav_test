package com.sap.sl.util.sduread.impl;

import java.math.BigInteger;
import java.util.Arrays;
import com.sap.sl.util.sduread.api.Version;

/**
 * @author Java Change Management 20.08.2003
 */

final class VersionImpl implements Version {
  
  final static String DOT_STRING = ".";

  /**
   * The non-empty sequence of numerical strings constituting this 
   * <code>Version</code>.
   */
  private final String[] numericalStrings;
  
  /**
   * A representation of <code>numericalStrings</code> as 
   * <code>BigInteger</code>s without trailing zeroes. This data structure 
   * enables a more straightforward implementation of some methods.
   */
  private final BigInteger[] numStringsAsBigInts;
  
  /**
   * The result of the toString method stored
   */
  private final String res ;


  /**
   * Creates an instance of <code>Version</code> whose associated sequence is
   * specified by an array of <code>String</code>s.
   * This constructor does neither check that the specified array is non-empty,
   * nor does it check that all <code>String</code>s actually contain numeric
   * strings.
   * 
   * @param numericalStrings a non-empty array of numerical strings
   */
  VersionImpl(String[] numericalStrings) {
    this.numericalStrings = new String[ numericalStrings.length ];
    System.arraycopy(
      numericalStrings, 0, this.numericalStrings, 0, numericalStrings.length);
      
    this.numStringsAsBigInts = toBigIntegersNoTrailingZeroes(numericalStrings);
    StringBuffer result = null;
    result = new StringBuffer();

    for (int i=0; i < numericalStrings.length - 1; i++) {
      result.append(numericalStrings[i]);
      result.append(DOT_STRING);
    }
    result.append(numericalStrings[numericalStrings.length-1]);
    res = result.toString();
    return;
  }

  /**
   * Returns whether the specified <code>Object</code> is equal to this 
   * <code>Version</code>. The specified <code>Object</code> is considered 
   * equal to this <code>Version</code>, if and only if it is an instance of 
   * <code>Version</code> (different from <code>null</code>) that consists of
   * the same sequence of numerical strings as this <code>Version</code>.
   * 
   * @param other the specified <code>Object</code>.
   * @return <code>true</code> if <code>other</code> is equal to this 
   *          <code>Version</code>; <code>false</code> otherwise.
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    
    if (other == null) {
      return false;
    }
    
    if (this.getClass() != other.getClass()) {
      return false;
    }
    
    VersionImpl otherVersion = (VersionImpl) other;

    if (Arrays.equals(numericalStrings, otherVersion.numericalStrings) == false) {
      return false;
    }
    
    return true;
  }
  
  /**
   * Calculates a hash code of this <code>Version</code> such that two instances
   * of <code>Version</code> that are equal return the same hash code.
   * 
   * @return a hash code
   */
  public int hashCode() {
    final int offset = 17;
    final int multiplier = 59;
    int result = offset;

    for (int i=0; i < numericalStrings.length; i++) {
      result += multiplier * numericalStrings[i].hashCode();
    }

    return result;
  }
  
  /**
   * Returns whether this <code>Version</code> is lower than the specified 
   * <code>Version</code>.
   * 
   * @param otherVersion the version to be compared with this <code>Version</code>
   * @return <code>true</code> if this <code>Version</code> is lower than 
   *          <code>otherVersion</code>; <code>false</code> otherwise.
   * @throws NullPointerException if <code>otherVersion</code> is 
   *          <code>null</code>.
   */
  public boolean isLower(Version otherVersion) {
    paramCheck("isLower", otherVersion);
    
    VersionImpl ovImpl = (VersionImpl) otherVersion;
    for (int i=0; i < Math.min(numStringsAsBigInts.length, ovImpl.numStringsAsBigInts.length); i++) {
      if (numStringsAsBigInts[i].compareTo(ovImpl.numStringsAsBigInts[i]) < 0) {
        return true;
      } else if (numStringsAsBigInts[i].compareTo(ovImpl.numStringsAsBigInts[i]) > 0) {
        return false;
      }
    }
    
    if (numStringsAsBigInts.length < ovImpl.numStringsAsBigInts.length) {
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * Returns whether this <code>Version</code> is equivalent to the specified 
   * <code>Version</code>.
   * 
   * @param otherVersion the version to be compared with this <code>Version</code>
   * @return <code>true</code> if this <code>Version</code> is equivalent to 
   *          <code>otherVersion</code>; <code>false</code> otherwise.
   * @throws NullPointerException if <code>otherVersion</code> is 
   *          <code>null</code>.
   */
  public boolean isEquivalent(Version otherVersion) {
    paramCheck("isEquivalent", otherVersion);
    
    VersionImpl ovImpl = (VersionImpl) otherVersion;
    
    return Arrays.equals(numStringsAsBigInts, ovImpl.numStringsAsBigInts);
  }
  
  /**
   * A convenience method that returns whether this <code>Version</code> is
   * equivalent to or lower than the specified <code>Version</code>.
   * 
   * @param otherVersion the version to be compared with this <code>Version</code>
   * @return <code>true</code> if this <code>Version</code> is lower than 
   *          or equal to <code>otherVersion</code>; <code>false</code> otherwise.
   * @throws NullPointerException if <code>otherVersion</code> is 
   *          <code>null</code>.
   */
  public boolean isLowerOrEquivalent(Version otherVersion) {
    paramCheck("isLowerOrEquivalent", otherVersion);
    
    if (isLower(otherVersion) || isEquivalent(otherVersion)) {
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * Gets a representation of this <code>Version</code> as version string.
   * When invoked with the returned version string, the method 
   * <code>VersionFactory.create(versionString)</code> returns a 
   * <code>Version</code> equal to this <code>Version</code>. More formally, for
   * each instance <code>v</code> of <code>Version</code>,
   * <br>
   * <code>v.equals(VersionFactory.create(v.toString())) == true</code>.
   * 
   * @return a representation of this <code>Version</code> as version string.
   * @see com.sap.sdm.util.version.VersionFactory#createVersion(String)
   */
  public String toString() {
    return res;
  }

  /**
   * Gets a representation of this <code>Version</code> as sequence of 
   * <code>BigInteger</code>s.
   * 
   * When invoked with the returned version string, the method 
   * <code>VersionFactory.createFromBigInts(bigInts)</code> returns a 
   * <code>Version</code> equivalent (not necessarily equal) to this 
   * <code>Version</code>. More formally, for each instance <code>v</code> of 
   * <code>Version</code>,
   * <br>
   * <code>v.isEquivalent(VersionFactory.createFromBigInts(v.toBigIntegers())) == true</code>.
   * 
   * @return a representation of this <code>Version</code> as <code>BigInteger</code>s.
   * @see com.sap.sdm.util.version.VersionFactory#createFromBigInts(BigInteger[])
   */
  public BigInteger[] toBigIntegers() {
    BigInteger[] result = new BigInteger[ numStringsAsBigInts.length ];
    
    System.arraycopy(numStringsAsBigInts, 0, result, 0, result.length);
    
    return result;
  }
  
  private BigInteger[] toBigIntegersNoTrailingZeroes(String[] numStrings) {
    return removeTrailingZeroes(toBigIntegers(numStrings));
  }
  
  private BigInteger[] toBigIntegers(String[] numStrings) {
    
    BigInteger[] result = new BigInteger[numStrings.length];
    
    for (int i=0; i < numStrings.length; i++) {
      result[i] = new BigInteger(numStrings[i]);
    }
    
    return result;
  }
  
  private BigInteger[] removeTrailingZeroes(BigInteger[] bigInts) {
    int lengthNoTrailingZeroes 
      = Math.max(0, getIndexLastNonZero(bigInts)) + 1;

    BigInteger[] result = new BigInteger[lengthNoTrailingZeroes];
    System.arraycopy(bigInts, 0, result, 0, lengthNoTrailingZeroes);
    
    return result;
  }
  
  private int getIndexLastNonZero(BigInteger[] bigInts) {
    for (int i=bigInts.length-1; i >= 0; i--) {
      if (bigInts[i].compareTo(BigInteger.ZERO) != 0) {
        return i;
      }
    }
    
    return -1;
  }

  /**
   * Checks whether the specified parameter object is not <code>null</code>.
   * 
   * @param methodName name of method to which the parameter has been passed
   * @param param the parameter object to be checked
   * @throws NullPointerException if <code>param</code> is <code>null</code>
   */
  private static void paramCheck(String methodName, Object param) {
    if (param != null) {
      return;
    }
    
    String errText = Version.class.getName() + "." + methodName +": parameter is null";
    throw new NullPointerException(errText);
  }

}
