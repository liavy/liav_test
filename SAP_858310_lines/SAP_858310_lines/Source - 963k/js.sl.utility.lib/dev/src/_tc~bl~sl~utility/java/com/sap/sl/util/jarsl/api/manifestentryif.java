package com.sap.sl.util.jarsl.api;

/**
 * @author d030435
 */

public interface ManifestEntryIF {
  /**
   * returns the name of the manifest entry
   * @return entry name
   */
	public abstract String getEntryName();
  /**
   * returns the name of the manifest attribute
   * @return attribute name
   */
	public abstract String getAttributeName();
  /**
   * returns the value of the manifest attribute
   * @return attribute value
   */
	public abstract String getAttributeValue();
  /**
   * returns the type of the manifest attribute
   * @return true if archive entry, otherwise false
   */
  public abstract boolean isArchiveEntry();
}