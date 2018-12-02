package com.sap.sl.util.sduread.api;

/**
 * The selection specific entries of a SDU. Those entries can be used to perform
 * a query on the SCA content ==> see also method .extractSelection() from ScaFile
 * @author d030435
 */

public interface SduSelectionEntries {
  /**
   * The selection specify attributes contained in the SDU
   * @return array of attribute names
   */
  public String[] getSelectionAttributes();
  
  /**
   * The value of the selection attribute
   * @return value or NULL if attribute is not defined
   */
  public String getAttributeValue(String selectionattribute);
  
  /**
   * The selection specify entries contained in the SDU
   * @return array of {@link SduSelectionEntry}
   */
  public SduSelectionEntry[] getSelectionEntries();
}
