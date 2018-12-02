package com.sap.sl.util.sduread.api;

import java.math.BigInteger;

/**
 * Implements the concept of a version. A version consists of a non-empty, 
 * finite sequence of numerical character strings. A numerical character string 
 * in turn is a finite sequence of digits <code>'0' - '9'</code>. 
 * 
 * <p>
 * Valid examples of versions are 
 * <br><code>(&quot;1&quot;, &quot;2&quot;, &quot;3&quot;)</code> and
 * <br><code>(&quot;001&quot;, &quot;002&quot;, &quot;003&quot;)</code>. 
 * </p>
 * <p>The following sequences do not represent versions:
 * <br><code>(&quot;&quot;)</code>,
 * <br><code>(&quot;-&quot;)</code>,
 * <br><code>(&quot;-1&quot;)</code>,
 * <br><code>(&quot;A&quot;, &quot;2&quot;, &quot;3&quot;)</code>,
 * <br><code>(&quot;-1&quot;, &quot;2&quot;, &quot;3&quot;)</code>,
 * <br><code>(&quot;2&quot;, &quot; 3&quot;, &quot;4&quot;)</code> and
 * <br><code>(&quot;1,5&quot;, &quot;23&quot;)</code>.
 * </p>
 * 
 * <p>
 * Versions are usually represented as an alternating sequence of numerical 
 * strings and dots, with the first and last element being a numerical string. 
 * This representation is also referred to as version strings. For example, the 
 * above examples of valid versions are represented as 
 * <code>&quot;1.2.3&quot;</code> and <code>&quot;001.002.003&quot;</code>. 
 * For easier readability, we will use the representation as version strings 
 * when referring to concrete examples of versions from now on.
 * </p>
 * 
 * <p>
 * Two versions are considered equal if and only if the length of their 
 * sequences is equal, and for each <code>i</code> in 
 * <code>[0..length-1]</code>, their <code>i</code>-th numerical strings are
 * equal. Thus, the versions <code>&quot;1.2.3&quot;</code> and 
 * <code>&quot;001.002.003&quot;</code> are not equal.
 * </p>
 * 
 * <p>
 * There is a natural mapping <code>toInt</code> from numerical strings to 
 * non-negative integers: With possibly leading <code>'0'</code>'s being cut 
 * off, numerical strings can be considered the decimal representation of 
 * integers. Thus, for a numerical string <code>nString</code>, 
 * <code>toInt(nString) == n</code> such that <code>n</code> is the integer
 * represented by <code>nString</code>. This in turn implies an equivalence
 * relation and an ordering on the set of numerical strings: For any two 
 * numerical strings <code>nString</code> and <code>mString</code>, we have
 * <ul>
 * <li><code>nString</code> and <code>mString</codE> are equivalent, if and 
 * only if <code>toInt(nString) == toInt(mString)</code>;
 * </li>
 * <li><code>nString &lt; mString</code>, if and only if 
 * <code>toInt(nString) < toInt(mString)</code>.
 * </li>
 * </ul>
 * It follows that <code>nString</code> and <code>mString</code> are equivalent 
 * if and only if neither <code>nString &lt; mString</code> nor 
 * <code>mString &lt; nString</code>. Note that two equivalent numerical 
 * strings are not necessarily equal.
 * </p>
 * 
 * <p>
 * The ordering and equivalence relation on numerical strings motivate an
 * ordering and equivalence relation on versions, such that for any two versions 
 * <code>v</code> and <code>w</code> exactly one of the following conditions 
 * holds:
 * <ul>
 * <li><code>v</code> is lower than <code>w</code>,</li>
 * <li><code>w</code> is lower than <code>v</code>,</li>
 * <li>neither one of the above two conditions holds; then <code>v</code> 
 * and <code>w</code> are considered equivalent.</li>
 * </ul>
 * 
 * To be more precise about the ordering relation on versions, let 
 * <code>n</code> be the length of <code>v</code>, and <code>m</code> be the 
 * length of <code>w</code>. Then <code>v</code> is considered lower than 
 * <code>w</code> if and only if one of the following two conditions hold:
 * <ul>
 * <li><code>n</code> and <code>m</code> are arbitrary, let <code>min</code> be 
 * the minimum of <code>{n, m}</code>. Then there is an <code>i</code> in 
 * <code>[0..min-1]</code> such that the <code>0</code>-th 
 * to <code>i-1</code>-th numeric strings of <code>v</code> and <code>w</code> 
 * are equivalent and the <code>i</code>-th numeric string of <code>v</code> is 
 * less than the <code>i</code>-th numeric string of <code>w</code>.
 * </li>
 * <li><code>n &lt; m</code>, and for all <code>i</code> in 
 * <code>[0..n-1]</code>, the <code>i</code>-th numerical strings of 
 * <code>v</code> and <code>w</code> are equivalent, and there is a 
 * <code>j</code> in <code>[n..m-1]</code> such that the <code>j</code>-th 
 * numerical string of <code>w</code> is not equivalent to the numerical string
 * <code>&quot;0&quot;</code>.
 * </ul>
 * </p>
 * 
 * <p>
 * Examples: The version <code>&quot;1.4.3.0&quot;</code> is lower than the
 * version <code>&quot;1.4.4&quot;</code> which in turn is lower than the 
 * version <code>&quot;1.4.4.5&quot;</code>.
 * </p>
 * 
 * <p>
 * From the definition of &quot;is lower than&quot; and the definition of 
 * &quot;is equivalent to&quot; in terms of &quot;is lower than&quot;, it 
 * follows that two versions <code>v</code> and <code>w</code> are equivalent
 * if and only if their corresponding sequences differ exlusively in a different 
 * number of trailing number strings equivalent to <code>&quot;0&quot;</code>.
 * Note that two versions that are equivalent are not necessarily equal.
 * </p>
 * 
 * <p>
 * Examples: The version <code>&quot;1.2.3&quot;</code> is equivalent to the 
 * version <code>&quot;1.2.3.0.0&quot;</code> which in turn is equivalent
 * <code>&quot;1.2.3.0&quot;</code>.
 * </p>
 * 
 * @author Christian Gabrisch 07.10.2002 
 */

public interface Version {
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
  public boolean equals(Object other);  
  
  /**
   * Calculates a hash code of this <code>Version</code> such that two instances
   * of <code>Version</code> that are equal return the same hash code.
   * 
   * @return a hash code
   */
  public int hashCode();
    
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
  public boolean isLower(Version otherVersion);
  
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
  public boolean isEquivalent(Version otherVersion);
  
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
  public boolean isLowerOrEquivalent(Version otherVersion);

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
  public String toString();
  
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
  public BigInteger[] toBigIntegers();
}
