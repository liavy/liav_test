package com.sap.sl.util.sduread.api;

/**
 * Represents a well-formatted SCA manifest. An SCA manifest is 
 * well-formatted if and only if the following conditions are true:
 * <ul>
 * <li>its <code>SoftwareComponentVersion</code> is valid</li>
 * </ul>
 * 
 * @author d030435
 */

public interface ScaManifest extends SduManifest {
  /**
   * Returns the <code>SoftwareComponentVersion</code> of this 
   * <code>ScaManifest</code>.
   * 
   * @return a <code>SoftwareComponentVersion</code>
   */
  public SoftwareComponentVersion getSoftwareComponentVersion();
  
  /**
   * Returns a description of the contents of this <code>ScaManifest</code>.
   * 
   * @return an array of <code>ContentMember</code>
   */ 
  public ContentMember[] getContentMembers();

  /**
   * Returns a textual representation of this <code>ScaManifest</code>.
   * 
   * @return a <code>String</code> representation of this <code>ScaManifest</code>
   */
  public String toString();

  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>ScaManifest</code>. The <code>other</code> object is considered
   * equal to this <code>ScaManifest</code>, if and only if it is also an instance 
   * of <code>ScaManifest</code> such that the two representations of SCA manifests 
   * have equal attributes. More specifically, 
   * <ul>
   * <li>the <code>SoftwareComponentVersion</code>s,</li>
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *          <code>ScaManifest</code> are equal;<code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>ScaManifest</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
  
  //
  //
  
  /**
   * @deprecated
   * Returns a description of the contents of this <code>ScaManifest</code>.
   * The method .getContents returns only members with no defined
   * deploytarget attribute or with attribute value J2EE_FS_DB.
   * 
   * Hereby only content of type Sdu (SCAs and SDAs) are returned!
   * 
   * @return an array of <code>SduContentMember</code>
   */ 
  public SduContentMember[] getContents();
  
  /**
   * @deprecated
   * Returns a description of the contents of this <code>ScaManifest</code>.
   * 
   * Hereby only content of type Sdu (SCAs and SDAs) are returned!
   * 
   * @return an array of <code>SduContentMember</code>
   */ 
  public SduContentMember[] getAllContents();
}
