package com.sap.sl.util.sduread.api;

import java.math.BigInteger;

/**
 * @author d030435
 */

public interface VersionFactoryIF {
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
  public abstract boolean isValidVersionString(String versionString);
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
  public abstract Version createVersion(String versionString);
  /**
   * Checks whether the specified sequence of <code>BigInteger</code>s defines
   * a version. A valid sequence of <code>BigInteger</code>s is non-empty and
   * contains exclusively <code>BigInteger</code>s that are non-negative.
   * 
   * @param bigInts a sequence of <code>BigInteger</code>s
   * @return <code>true</code> if the sequence defines a <code>Version</code>
   *          <code>false</code> if it is not a valid sequence or <code>null</code>
   */
  public abstract boolean isValidSequence(BigInteger[] bigInts);
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
  public abstract Version createFromBigInts(BigInteger[] bigInts);
}