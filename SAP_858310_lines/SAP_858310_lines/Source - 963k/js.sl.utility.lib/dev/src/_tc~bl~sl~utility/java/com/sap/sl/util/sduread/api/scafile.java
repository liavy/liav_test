package com.sap.sl.util.sduread.api;

import java.io.InputStream;

/**
 * Represents a well-formatted SCA file in the file system. An SCA file is 
 * well-formatted if and only if the following conditions are true:
 * <ul>
 * <li>its <code>SoftwareComponentVersion</code> is valid</li>
 * </ul>
 * 
 * @author Christian Gabrisch 18.11.2002, Ralf Belger
 */

public interface ScaFile extends SduFile {
  /**
   * Returns the <code>SoftwareComponentVersion</code> of this 
   * <code>ScaFile</code>.
   * 
   * @return a <code>SoftwareComponentVersion</code>
   */
  public SoftwareComponentVersion getSoftwareComponentVersion();
  
  /**
   * Returns a description of the contents of this <code>ScaFile</code>.
   * 
   * @return an array of <code>ContentMember</code>
   */ 
  public ContentMember[] getContentMembers();

  /**
   * Extracts the specified <code>ContentMember</code> as stream
   * 
   * @param member the <code>ContentMember</code> to be
   *         extracted
   * @return The content of the extracted member as stream.
   *          The stream must be closed by the caller.
   * @throws FileExtractionException if an error occurs while the files 
   *          are extracted
   * @throws IllFormattedSduFileException if the contained files are not 
   *          well-formatted
   * @throws NullPointerException if any of the parameters is <code>null</code>
   * @throws IllegalArgumentException if <code>members</code> and 
   *          <code>targetFileNames</code> have different lengths, or not all 
   *          <code>SduContentMember</code> in <code>members</code> are actually
   *          content members of this SCA file
   */
  public InputStream extractContentMemberAsStream(ContentMember member) throws FileExtractionException, IllFormattedSduFileException;
  
  /**
   * Extracts the specified <code>ContentMember</code>s to the specified 
   * locations. Files already located at the specified locations will be 
   * overwritten.
   * 
   * @param members an array of <code>ContentMember</code>s to be
   *         extracted
   * @param targetFileNames an array of <code>String</code>s containing the 
   *         file names to which the files are to be extracted, where the 
   *         i-th member of <code>targetFileNames</code> specifies the location 
   *         of <code>members[i]</code>
   * @return an array of <code>FileEntry</code> representing the extracted 
   *          files.
   * @throws FileExtractionException if an error occurs while the files 
   *          are extracted
   * @throws IllFormattedSduFileException if the contained files are not 
   *          well-formatted
   * @throws NullPointerException if any of the parameters is <code>null</code>
   * @throws IllegalArgumentException if <code>members</code> and 
   *          <code>targetFileNames</code> have different lengths, or not all 
   *          <code>SduContentMember</code> in <code>members</code> are actually
   *          content members of this SCA file
   */
  public FileEntry[] extractContentMembers(ContentMember[] members, String[] targetFileNames) throws FileExtractionException, IllFormattedSduFileException;
  
  /**
   * Extracts the specified <code>ContentMember</code>s to the specified target directory 
   * and returns an array of <code>FileEntry</code>s representing the extracted 
   * files. Files already located at the specified directory will possibly 
   * overwritten.
   * 
   * @param members an array of <code>ContentMember</code>s to be
   *         extracted
   * @param targetDirectoryName a <code>String</code> representing the 
   *         directory path name to which the contained file will be 
   *         extracted
   * @param usingOriginalPath if set to true and the original path information is available
   *        in the SAP_MANIFEST.MF file, the extraction file location is
   *        <targetDirectoryName>/<originalPath>   
   * @return an array of <code>FileEntry</code> representing the extracted 
   *          files.
   * @throws FileExtractionException if an error occurs while the files 
   *          are extracted
   * @throws IllFormattedSduFileException if the contained files are not 
   *          well-formatted
   * @throws NullPointerException if any of the parameters is <code>null</code>
   */
  public FileEntry[] extractContentMembers(ContentMember[] members, String targetDirectoryName, boolean usingOriginalPath) throws FileExtractionException, IllFormattedSduFileException;
  
  /**
   * Extracts all contained file entries to the specified target directory and 
   * returns an array of <code>FileEntry</code>s representing the extracted 
   * files. Files already located at the specified directory will possibly 
   * overwritten.
   * 
   * @param targetDirectoryName a <code>String</code> representing the 
   *         directory path name to which the contained file will be 
   *         extracted
   * @param usingOriginalPath if set to true and the original path information is available
   *        in the SAP_MANIFEST.MF file, the extraction file location is
   *        <targetDirectoryName>/<originalPath>   
   * @return an array of <code>FileEntry</code> representing the extracted 
   *          files.
   * @throws FileExtractionException if an error occurs while the files 
   *          are extracted
   * @throws IllFormattedSduFileException if the contained files are not 
   *          well-formatted
   * @throws NullPointerException if any of the parameters is <code>null</code>
   */
  public FileEntry[] extractAllContentMembers(String targetDirectoryName, boolean usingOriginalPath) throws FileExtractionException, IllFormattedSduFileException;
  
  /**
   * Returns a textual representation of this <code>ScaFile</code>, consisting 
   * of representations of its actual file format, its path name, its
   * <code>SoftwareComponentVersion</code> and of its contained files entries.
   * 
   * @return a <code>String</code> representation of this <code>ScaFile</code>
   */
  public String toString();

  /**
   * Indicates whether the specified <code>Object</code> is equal to this 
   * <code>ScaFile</code>. The <code>other</code> object is considered
   * equal to this <code>ScaFile</code>, if and only if it is also an instance 
   * of <code>ScaFile</code> such that the two representations of SCA files 
   * have equal attributes. More specifically, 
   * <ul>
   * <li>the path names,</li>
   * <li>the <code>SoftwareComponentVersion</code>s,</li>
   * <li>and the two sets defined by the <code>ContentMember</code>s</li>
   * </ul>
   * of the two instances of <code>ScaFile</code> are required to be equal. 
   * 
   * <p>
   * It is not requested that the represented SCA files themselves are 
   * (byte-wise) equal.
   * </p>
   * 
   * @return <code>true</code> if <code>other</code> and this 
   *          <code>ScaFile</code> are equal;<code>false</code> otherwise.
   */
  public boolean equals(Object other);
  
  /**
   * Returns a hash code for this <code>ScaFile</code>.
   * 
   * @return an integer hash code 
   */
  public int hashCode();
  
  //
  //
  
  /**
   * @deprecated
   * Returns a description of the contents of this <code>ScaFile</code>.
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
   * Returns a description of the contents of this <code>ScaFile</code>.
   * 
   * Hereby only content of type Sdu (SCAs and SDAs) are returned!
   * 
   * @return an array of <code>SduContentMember</code>
   */ 
  public SduContentMember[] getAllContents();
  
  /**
   * @deprecated
   * Extracts the specified <code>SduContentMember</code>s to the specified 
   * locations. Files already located at the specified locations will be 
   * overwritten.
   * 
   * Hereby only content of type Sdu (SCAs and SDAs) are returned!
   * 
   * @param members an array of <code>SduContentMember</code>s to be
   *         extracted
   * @param targetFileNames an array of <code>String</code>s containing the 
   *         file names to which the files are to be extracted, where the 
   *         i-th member of <code>targetFileNames</code> specifies the location 
   *         of <code>members[i]</code>
   * @throws FileExtractionException if an error occurs while the files 
   *          are extracted
   * @throws IllFormattedSduFileException if the contained files are not 
   *          well-formatted
   * @throws NullPointerException if any of the parameters is <code>null</code>
   * @throws IllegalArgumentException if <code>members</code> and 
   *          <code>targetFileNames</code> have different lengths, or not all 
   *          <code>SduContentMember</code> in <code>members</code> are actually
   *          content members of this SCA file
   */
  public SduFile[] extract(SduContentMember[] members, String[] targetFileNames) throws FileExtractionException, IllFormattedSduFileException;
  
  /**
   * @deprecated
   * Extracts all contained SDA files to the specified target directory and 
   * returns an array of <code>SdaFile</code> representing the extracted SDA 
   * files. Files already located at the specified directory will possibly 
   * overwritten.
   * The method .extractAll extracts only contained SDAs with no defined
   * deploytarget attribute or with attribute value J2EE_FS_DB.
   * 
   * Hereby only content of type Sdu (SCAs and SDAs) are handled!
   * 
   * @param targetDirectoryName a <code>String</code> representing the 
   *         directory path name to which the contained SDA file will be 
   *         extracted
   * @return an array of <code>SdaFile</code> representing the extracted SDA 
   *          files.
   * @throws FileExtractionException if an error occurs while the SDA files 
   *          are extracted
   * @throws IllFormattedSduFileException if the contained SDA files are not 
   *          well-formatted
   * @throws NullPointerException if any of the parameters is <code>null</code>
   */
  public SdaFile[] extractAll(String targetDirectoryName) throws FileExtractionException, IllFormattedSduFileException;

  /**
   * @deprecated
   * Extracts all contained SDA files to the specified target directory, that contain the given selection entry.
   * An array of <code>SdaFile</code> representing the extracted SDA files is returned.
   * Files already located at the specified directory will possibly overwritten.
   * 
   * Hereby only content of type Sdu (SCAs and SDAs) are handled!
   * 
   * @param selectionentries an array of <code>SduSelectionEntry</code>
   * @param targetDirectoryName a <code>String</code> representing the 
   *         directory path name to which the contained SDA file will be 
   *         extracted
   * @return an array of <code>SdaFile</code> representing the extracted SDA 
   *          files.
   * @throws FileExtractionException if an error occurs while the SDA files 
   *          are extracted
   * @throws IllFormattedSduFileException if the contained SDA files are not 
   *          well-formatted
   * @throws NullPointerException if any of the parameters is <code>null</code>
   */
  public SdaFile[] extractSelection(SduSelectionEntry[] selectionentries, String targetDirectoryName) throws FileExtractionException, IllFormattedSduFileException;
}
