package com.sap.sl.util.sduread.api;

import java.io.IOException;
import java.io.InputStream;

public interface SduReader {
  /**
   * Reads the SDU file specified by <code>pathName</code> and returns a 
   * representation of the file.
   * 
   * @param pathName the path to the SDU file
   * @return SduFile a representation of the file
   * @throws IllFormattedSduFileException if the file is readable but does not
   *          correspond to one of the known file formats
   * @throws IOException if an I/O exception of some sort has occurred
   * @throws NullPointerException if <code>pathName</code> is <code>null</code>
   */
  public abstract SduFile readFile(String pathName) throws IllFormattedSduFileException, IOException;
  
  /**
   * Reads the SDU file specified by <code>pathName</code> and returns a 
   * representation of the file.
   * 
   * @param pathName the path to the SDU file
   * @param onlydeployable should be only deployable SDAs O.K.
   * @return SduFile a representation of the file
   * @throws IllFormattedSduFileException if the file is readable but does not
   *          correspond to one of the known file formats
   * @throws IOException if an I/O exception of some sort has occurred
   * @throws NullPointerException if <code>pathName</code> is <code>null</code>
   */
  public abstract SduFile readFile(String pathName,boolean onlydeployable) throws IllFormattedSduFileException, IOException;
  
  /**
   * Reads the SDU manifests specified by <code>pathNameOfManifestFile and pathNameOfSapManifestFile</code> and returns a 
   * representation of the manifest.
   * 
   * @param pathNameOfManifestFile the path to the SDU manifest file
   * @param pathNameOfSapManifestFile the path to the SDU sap_manifest file
   * @return SduManifest a representation of the manifests
   * @throws IllFormattedSduManifestException if the manifests are readable but does not
   *          correspond to one of the known file formats
   * @throws IOException if an I/O exception of some sort has occurred
   * @throws NullPointerException if <code>pathNameOfManifestFile or </code> is pathNameOfSapManifestFile<code>null</code>
   */
  public abstract SduManifest readManifests(String pathNameOfManifestFile, String pathNameOfSapManifestFile) throws IllFormattedSduManifestException, IOException;
  
  /**
   * Reads the SDU file specified by <code>pathName</code> and returns the 
   * manifest as stream
   * 
   * @param pathName the path to the SDU file
   * @return InputStream a stream of the manifest
   * @throws IllFormattedSduFileException if the file is readable but does not
   *          correspond to one of the known file formats
   * @throws IOException if an I/O exception of some sort has occurred
   * @throws NullPointerException if <code>pathName</code> is <code>null</code>
   */
  public abstract InputStream getManifestInputStream(String pathName) throws IllFormattedSduFileException, IOException;
  
  /**
   * Reads the SDU file specified by <code>pathName</code> and returns the 
   * sap_manifest as stream
   * 
   * @param pathName the path to the SDU file
   * @return InputStream a stream of the sap_manifest
   * @throws IllFormattedSduFileException if the file is readable but does not
   *          correspond to one of the known file formats
   * @throws IOException if an I/O exception of some sort has occurred
   * @throws NullPointerException if <code>pathName</code> is <code>null</code>
   */
  public abstract InputStream getSapManifestInputStream(String pathName) throws IllFormattedSduFileException, IOException;
}
