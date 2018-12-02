package com.sap.sl.util.sduread.impl;

import com.sap.sl.util.sduread.api.SduSelectionEntry;

class SduSelectionEntryImpl implements SduSelectionEntry{
  private String attribute=null;
  private String value=null;
  SduSelectionEntryImpl(String selectionattribute, String value) {
    this.attribute=selectionattribute;
    this.value=value;
  }
  public String getSelectionAttribute() {
    return attribute;
  }
  public String getAttributeValue() {
    return value;
  }
}
