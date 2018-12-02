package com.sap.sl.util.sduread.api;

/**
 * Represents a well-formatted SDA manifest. An SDA manifest is 
 * well-formatted if and only if the following conditions are true:
 * <ul>
 * <li>its <code>DevelopmentComponentVersion</code> is valid</li>
 * <li>it has exactly one software type assigned</li>
 * <li>all its dependencies are valid</li>
 * <li>if it contains a deployment descriptor, then this deployment descriptor
 * is a well-formatted XML document.</li>
 * </ul>
 * 
 * @author d030435
 */

public interface SdaManifest extends SduManifest {
  /**
   * Returns the <code>DevelopmentComponentVersion</code> of this 
   * <code>SdaManifest</code>.
   * 
   * @return a <code>DevelopmentComponentVersion</code>
   */
  public DevelopmentComponentVersion getDevelopmentComponentVersion();
  
  /**
   * Returns the <code>SduSelectionEntries</code> of this 
   * <code>SdaManifest</code>.
   * 
   * @return a <code>SduSelectionEntries</code>
   */
  public SduSelectionEntries getSduSelectionEntries();  
  
  /**
   * Returns the <code>Dependency</code> objects defined in this 
   * <code>SdaManifest</code>.
   * 
   * @return an array of <code>Dependency</code> whose length equals the number
   *          of defined dependencies in the SDA manifest; in particular, if the SDA
   *          manifest contains no dependencies, the method returns an array of 
   *          zero length
   */
  public Dependency[] getDependencies();
  
  /**
   * Returns the software type of this <code>SdaManifest</code>.
   * 
   * @return a <code>String</code> representing the software type of this
   *          <code>SdaManifest</code>.
   */
  public String getSoftwareType();
  
  /**
   * Returns the software subtype of this <code>SdaManifest</code>. If no subtype
   * is set it returns null.
   * 
   * @return a <code>String</code> representing the software subtype of this
   *          <code>SdaManifest</code>.
   */
  public String getSoftwareSubType();
  
  /**
   * Returns the CSN component information of this <code>SdaManifest</code>. If no CSN component
   * information is set it returns null.
   * 
   * @return a <code>String</code> representing the CSN component information of this
   *          <code>SdaManifest</code>.
   */
  public String getCSNComponent();
  
  /**
   * Returns the archive type of this <code>SdaManifest</code>. If no archive type is set it returns DC.
   * Possible values:
   *  DC            Development Component
   *  BUSINESSCNT   Business Content
   * 
   * @return a <code>String</code> representing the archive type of this
   *          <code>SdaManifest</code>.
   */
  public String getArchiveType();
  
  /**
   * Returns the debug information of this SDA manifest.
   * 
   * @return a <code>String</code> containing the debug information;
   *          if this <code>SdaManifest</code> contains no debug information,
   *          the method returns <code>null</code>.
   */
  public String getDebugInfo();
  
  /**
   * Returns a textual representation of this <code>SdaManifest</code>.
   * 
   * @return a <code>String</code> representation of this <code>SdaManifest</code>
   */
  public String toString();

  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>SdaManifest</code>. The <code>other</code> object is considered
   * equal to this <code>SdaManifest</code>, if and only if it is also an instance 
   * of <code>SdaManifest</code> such that the two representations of SDA manifest have
   * equal attributes. More specifically, 
   * <ul>
   * <li>the <code>DevelopmentComponentVersion</code>s,</li>
   * <li>the software types,</li>
   * <li>and the two sets defined by the dependencies</li>
   * </ul>
   * of the two instances of <code>SdaManifest</code> are required to be equal. 
   * Strictly speaking, also their deployment descriptors are required to match, 
   * but due to technical reasons this property is not required here. 
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *          <code>SdaManifest</code> are equal;<code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>SdaManifest</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
}
