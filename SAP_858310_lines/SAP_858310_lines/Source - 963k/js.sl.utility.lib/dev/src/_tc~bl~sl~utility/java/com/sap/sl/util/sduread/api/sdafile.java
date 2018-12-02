package com.sap.sl.util.sduread.api;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a well-formatted SDA file in the file system. An SDA file is 
 * well-formatted if and only if the following conditions are true:
 * <ul>
 * <li>its <code>DevelopmentComponentVersion</code> is valid</li>
 * <li>it has exactly one software type assigned</li>
 * <li>all its dependencies are valid</li>
 * <li>if it contains a deployment descriptor, then this deployment descriptor
 * is a well-formatted XML document.</li>
 * </ul>
 * 
 * @author Christian Gabrisch 18.11.2002 
 */

public interface SdaFile extends SduFile {
  /**
   * Returns the <code>DevelopmentComponentVersion</code> of this 
   * <code>SdaFile</code>.
   * 
   * @return a <code>DevelopmentComponentVersion</code>
   */
  public DevelopmentComponentVersion getDevelopmentComponentVersion();
  
  /**
   * Returns the <code>SduSelectionEntries</code> of this 
   * <code>SdaFile</code>.
   * 
   * @return a <code>SduSelectionEntries</code>
   */
  public SduSelectionEntries getSduSelectionEntries();  
  
  /**
   * Returns the <code>Dependency</code> objects defined in this 
   * <code>SdaFile</code>.
   * 
   * @return an array of <code>Dependency</code> whose length equals the number
   *          of defined dependencies in the SDA file; in particular, if the SDA
   *          file contains no dependencies, the method returns an array of 
   *          zero length
   */
  public Dependency[] getDependencies();
  
  /**
   * Returns the software type of this <code>SdaFile</code>.
   * 
   * @return a <code>String</code> representing the software type of this
   *          <code>SdaFile</code>.
   */
  public String getSoftwareType();
  
  /**
   * Returns the archive type of this <code>SdaFile</code>. If no archive type is set it returns DC.
   * Possible values:
   *  DC            Development Component
   *  BUSINESSCNT   Business Content
   * 
   * @return a <code>String</code> representing the archive type of this
   *          <code>SdaFile</code>.
   */
  public String getArchiveType();
  
  /**
   * Returns the software subtype of this <code>SdaFile</code>. If no subtype
   * is set it returns null.
   * 
   * @return a <code>String</code> representing the software subtype of this
   *          <code>SdaFile</code>.
   */
  public String getSoftwareSubType();
  
  /**
   * Returns the CSN component information of this <code>SdaFile</code>. If no CSN component
   * information is set it returns null.
   * 
   * @return a <code>String</code> representing the CSN component information of this
   *          <code>SdaFile</code>.
   */
  public String getCSNComponent();

  /**
   * Returns the root element of the deployment descriptor of this SDA file.
   * 
   * @return an <code>XMLElementIF</code> as the root of the deployment 
   *          descriptor; if this <code>SdaFile</code> contains no deployment
   *          descriptor, the method returns <code>null</code>.
   */
  public InputStream getDeploymentDescriptor() throws IOException;
  
  /**
   * Returns the debug information of this SDA file.
   * 
   * @return a <code>String</code> containing the debug information;
   *          if this <code>SdaFile</code> contains no debug information,
   *          the method returns <code>null</code>.
   */
  public String getDebugInfo();
  
  /**
   * Returns a textual representation of this <code>SdaFile</code>, consisting
   * of representations of its actual file format, its path name, its 
   * <code>DevelopmentComponentVersion</code>, its software type and of its 
   * <code>Dependency</code> objects.
   * 
   * @return a <code>String</code> representation of this <code>SdaFile</code>
   */
  public String toString();

  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>SdaFile</code>. The <code>other</code> object is considered
   * equal to this <code>SdaFile</code>, if and only if it is also an instance 
   * of <code>SdaFile</code> such that the two representations of SDA files have
   * equal attributes. More specifically, 
   * <ul>
   * <li>the path names,</li>
   * <li>the <code>DevelopmentComponentVersion</code>s,</li>
   * <li>the software types,</li>
   * <li>and the two sets defined by the dependencies</li>
   * </ul>
   * of the two instances of <code>SdaFile</code> are required to be equal. 
   * Strictly speaking, also their deployment descriptors are required to match, 
   * but due to technical reasons this property is not required here. 
   * 
   * <p>
   * It is not requested that the represented SDA files themselves are 
   * (byte-wise) equal.
   * </p>
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *          <code>SdaFile</code> are equal;<code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>SdaFile</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
}
