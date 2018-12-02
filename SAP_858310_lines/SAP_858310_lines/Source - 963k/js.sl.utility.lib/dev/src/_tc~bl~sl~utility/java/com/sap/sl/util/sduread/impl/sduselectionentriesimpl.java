package com.sap.sl.util.sduread.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import com.sap.sl.util.jarsl.api.ConstantsIF;
import com.sap.sl.util.jarsl.api.JarSLManifestIF;
import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;
import com.sap.sl.util.sduread.api.SduSelectionEntries;
import com.sap.sl.util.sduread.api.SduSelectionEntry;

class SduSelectionEntriesImpl implements SduSelectionEntries, ConstantsIF {  
  Map selections=new HashMap();
  
  private String _readAttribute(JarSLManifestIF jarsl, String entry, String name) throws IllFormattedSduManifestException {
    Vector errorText=new Vector();
    String rc=jarsl.readAttribute(entry,name,errorText);
    if (errorText.size()>0) {
      StringBuffer tmpb = new StringBuffer();
      Iterator errIter = errorText.iterator();
      while (errIter.hasNext()) {
        tmpb.append((String)errIter.next());
      }
      throw new IllFormattedSduManifestException("Error during attribute reading (entry="+entry+"name="+name+"): "+tmpb.toString());
    }
    return rc;
  }
  
  SduSelectionEntriesImpl(JarSLManifestIF jarsl) throws IllFormattedSduManifestException {
    this(jarsl,"");
  }
  SduSelectionEntriesImpl(JarSLManifestIF jarsl, String entry) throws IllFormattedSduManifestException {
    String my_entry = null;
    if (null == entry || entry.equals("")) {
      my_entry = "";
    }
    else {
      my_entry = entry + "/";
    } 
    String[] selectattributes=ConstantsIF.selectionattributes; // make it here more generic the next time
    for (int i=0; i<selectattributes.length; ++i) {
      String selectatt=_readAttribute(jarsl,my_entry,selectattributes[i]);
      if (selectatt!=null) {
        selections.put(selectattributes[i],selectatt);
      }
    }
  }
  public String[] getSelectionAttributes() {
    return (String[])selections.keySet().toArray(new String[0]);
  }
  public String getAttributeValue(String name) {
    return (String)selections.get(name);
  }
  public SduSelectionEntry[] getSelectionEntries() {
    String[] attributes=getSelectionAttributes();
    SduSelectionEntry[] entries=new SduSelectionEntry[attributes.length];
    for (int i=0; i<entries.length; ++i)  {
      entries[i]=new SduSelectionEntryImpl(attributes[i],getAttributeValue(attributes[i]));
    }
    return entries;
  }
}