package com.sap.sl.util.jarsl.api;

import java.io.InputStream;
import java.util.Vector;

/**
 * @author d030435
 */

public interface JarSLManifestIF {
  /**
   * returns every manifest entry which has the given attribute value pair entry in the SAP_MANIFEST.MF
   */
  public abstract String[] getEntryListOfGivenAttributeValuePair(String attribute, String value);
  /**
   * returns every archive entry contained in the SAP_MANIFEST.MF (only once)
   */
  public abstract String[] getArchiveEntriesFromManifest();
  /**
   * returns every entry contained in the SAP_MANIFEST.MF (only once)
   */
  public abstract String[] getFilelistFromManifest();
  /**
   * reads the attributes's value of the given entry from the manifest
   */
  public abstract String readAttribute(String entry, String attribute, Vector errTexts);
  public abstract String readAttribute(String entry, String attribute);
  /**
   * writes a attribute value pair for the given entry into the manifest
   */
  public abstract boolean writeAttribute(String entry, String attribute, String value, boolean archiveentry);
  public abstract boolean writeAttribute(String entry, String attribute, String value);
  /**
   * removes the given attribute from the manifest
   */
  public abstract boolean deleteAttribute(String entry, String attribute);
  /**
   * returns all entries form the manifest
   */
  public abstract ManifestEntryIF[] getAllManifestEntries();
  /**
   * returns all entries form the sap_manifest
   */
  public abstract ManifestEntryIF[] getAllSapManifestEntries(); 
  /**
   * returns the corresponding manifest as stream
   * In case of errors, messages are added to the vector errTexts of String's,
   * and null is returned.
   */
  public abstract InputStream getManifestAsStream(Vector errTexts);
  /**
   * returns the corresponding sap_manifest as stream
   * In case of errors, messages are added to the vector errTexts of String's,
   * and null is returned.
   */
  public abstract InputStream getSapManifestAsStream(Vector errTexts);
}
