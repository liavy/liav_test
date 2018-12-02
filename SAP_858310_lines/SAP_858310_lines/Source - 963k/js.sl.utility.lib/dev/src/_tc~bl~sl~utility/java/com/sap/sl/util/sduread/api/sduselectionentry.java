package com.sap.sl.util.sduread.api;

/**
 * Represents a SduSelectionEntry that can be used to extract only a part
 * of the contained SDAs inside a SCA.
 */

public interface SduSelectionEntry {
  /**
   * The selection specify attribute
   * @return the attribute name
   */
  public String getSelectionAttribute();
  
  /**
   * The value of the selection attribute
   * @return the value
   */
  public String getAttributeValue();
}