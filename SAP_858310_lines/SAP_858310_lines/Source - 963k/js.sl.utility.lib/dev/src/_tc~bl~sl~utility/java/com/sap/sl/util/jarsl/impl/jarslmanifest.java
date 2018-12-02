package com.sap.sl.util.jarsl.impl;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import com.sap.sl.util.jarsl.api.ConstantsIF;
import com.sap.sl.util.jarsl.api.JarSLManifestException;
import com.sap.sl.util.jarsl.api.JarSLManifestIF;
import com.sap.sl.util.jarsl.api.ManifestEntryIF;

/**
 * @author d030435
 */

final class JarSLManifest implements ConstantsIF, JarSLManifestIF {
  /**
    * DEF_jarslversion should be updated with every change in com.sap.sl.util.jarsl
    */
  static private String DEF_jarslmfversion="20061120.1500";

  private ManifestSL manifestsd, manifestex; /* these two objects contain the MANIFEST.MF and the SAP_MANIFEST.MF content */
  private boolean changed;                   /* some changes to the initial manifests occured */

  /**
   * This method returns the current JarSLManifest version      
   * @return String the JarSLManifest version
   */
  static String getJarSLManifestVersion() {
    return DEF_jarslmfversion;
  }
  
  /**
   * creates a JarSLManifest instance
   * @param manifest    InputStream of the manifest file
   * @param sapmanifest InputStream of the sapmanifest file
   */
  JarSLManifest(InputStream manifest, InputStream sapmanifest) throws JarSLManifestException {
    Vector errorTexts=new Vector();
    manifestsd=new ManifestSL(JARSL_MANIFEST);      /* standard manifest */
    manifestex=new ManifestSL(JARSL_SAP_MANIFEST);  /* ext. manifest */
    if (manifestsd.readAllAttributesFromManifestInputStream(manifest,errorTexts)==false) {
      StringBuffer tmpB = new StringBuffer("manifest: ");
      if (null!=errorTexts) {
        Iterator errIter = errorTexts.iterator();
        while (errIter.hasNext()) {
          tmpB.append((String)errIter.next());
        }
      }
      throw new JarSLManifestException(tmpB.toString());
    }
    if (manifestex.readAllAttributesFromManifestInputStream(sapmanifest,errorTexts)==false) {
      StringBuffer tmpB = new StringBuffer("sap_manifest: ");
      if (null!=errorTexts) {
        Iterator errIter = errorTexts.iterator();
        while (errIter.hasNext()) {
          tmpB.append((String)errIter.next());
        }
      }
      throw new JarSLManifestException(tmpB.toString());
    }
    changed=false;
  }
  /* (non-Javadoc)
   * @see com.sap.sl.util.jarsl.api.JarSLManifestIF#getEntryListOfGivenAttributeValuePair(java.lang.String, java.lang.String)
   */
  public String[] getEntryListOfGivenAttributeValuePair(String attribute, String value) {
    return manifestex.getEntryListOfGivenAttributeValuePair(attribute,value);
  }
  /* (non-Javadoc)
   * @see com.sap.sl.util.jarsl.api.JarSLManifestIF#getArchiveEntriesFromManifest()
   */
  public String[] getArchiveEntriesFromManifest() {
    return manifestex.getArchiveEntryListFromManifest();
  }
  /* (non-Javadoc)
   * @see com.sap.sl.util.jarsl.api.JarSLManifestIF#getFilelistFromManifest()
   */
  public String[] getFilelistFromManifest() {
    return manifestex.getEntryListFromManifest();
  }
  /* (non-Javadoc)
   * @see com.sap.sl.util.jarsl.api.JarSLManifestIF#readAttribute(java.lang.String, java.lang.String)
   */
  public String readAttribute(String entry, String attribute, Vector error) {
    return readAttribute(entry,attribute);
  }
  public String readAttribute(String entry, String attribute) {
    boolean found=false;
    if (entry.endsWith("/") || entry.endsWith("\\")) {
      found=true;
    }
    if (found==false && ManifestSL.isBothAttribute(attribute)) {
      String value=null;
      value=manifestex.readAttribute(entry,attribute);
      if (value==null) {
        value=manifestsd.readAttribute(entry,attribute);
      }
      return value;
    }
    else if (found==false && ManifestSL.isStandardAttribute(attribute)) {
      return manifestsd.readAttribute(entry,attribute);
    }
    else {
      return manifestex.readAttribute(entry,attribute);
    }
  }
  /* (non-Javadoc)
   * @see com.sap.sl.util.jarsl.api.JarSLManifestIF#writeAttribute(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public boolean writeAttribute(String entry, String attribute, String value, boolean archiveentry) {
    boolean rv;
    boolean found=false;
    if (entry.endsWith("/") || entry.endsWith("\\")) {
      found=true;
    }
    if (found==false && ManifestSL.isStandardAttribute(attribute)) {
      rv=manifestsd.writeAttribute(entry,attribute,value,archiveentry);
    }
    else {
      rv=manifestex.writeAttribute(entry,attribute,value,archiveentry);
    }
    if (rv) {
      changed=true;
    }
    return rv;
  }
  /* (non-Javadoc)
   * @see com.sap.sl.util.jarsl.api.JarSLManifestIF#writeAttribute(java.lang.String, java.lang.String, java.lang.String)
   */
  public boolean writeAttribute(String entry, String attribute, String value) {
    boolean rv;
    boolean found=false;
    if (entry.endsWith("/") || entry.endsWith("\\")) {
      found=true;
    }
    if (found==false && ManifestSL.isStandardAttribute(attribute)) {
      rv=manifestsd.writeAttribute(entry,attribute,value);
    }
    else {
      rv=manifestex.writeAttribute(entry,attribute,value);
    }
    if (rv) {
      changed=true;
    }
    return rv;
  }
  /* (non-Javadoc)
   * @see com.sap.sl.util.jarsl.api.JarSLManifestIF#deleteAttribute(java.lang.String, java.lang.String)
   */
  public boolean deleteAttribute(String entry, String attribute) {
    boolean rv;
    boolean found=false;
    if (entry.endsWith("/") || entry.endsWith("\\")) {
      found=true;
    }
    if (found==false && ManifestSL.isStandardAttribute(attribute)) {
      rv=manifestsd.deleteAttribute(entry,attribute);
    }
    else {
      rv=manifestex.deleteAttribute(entry,attribute);
    }
    if (rv) {
      changed=true;
    }
    return rv;
  }
  /* (non-Javadoc)
   * @see com.sap.sl.util.jarsl.api.JarSLManifestIF#getAllManifestEntries()
   */
  public ManifestEntryIF[] getAllManifestEntries() {
    return manifestsd.returnAllManifestEntries();
  }
  /* (non-Javadoc)
   * @see com.sap.sl.util.jarsl.api.JarSLManifestIF#getAllSapManifestEntries()
   */
  public ManifestEntryIF[] getAllSapManifestEntries() {
    return manifestex.returnAllManifestEntries();
  }
  /* (non-Javadoc)
   * @see com.sap.sl.util.jarsl.api.JarSLManifestIF#getManifestAsStream(java.util.Vector)
   */
  public InputStream getManifestAsStream(Vector errTexts) {
    InputStream result=null;
    result=manifestsd.createManifest();
    if (result==null && errTexts!=null) {
      errTexts.add("Could not create manifest.");
    }
    return result;
  }
  /* (non-Javadoc)
   * @see com.sap.sl.util.jarsl.api.JarSLManifestIF#getSapManifestAsStream(java.util.Vector)
   */
  public InputStream getSapManifestAsStream(Vector errTexts) {
    InputStream result=null;
    if (changed) {
      deleteAttribute("",ATTJARSLMFVERSION);
      if (writeAttribute("",ATTJARSLMFVERSION,DEF_jarslmfversion)==false) {
        if (errTexts!=null) {
          errTexts.add("Could not write attribute to sapmanifest.");
        }
        return null;
      }
      deleteAttribute("",ATTSAPMANIFESTORIGIN);
      if (writeAttribute("",ATTSAPMANIFESTORIGIN,"jarslmanifest(getSapManifestAsStream)")==false) {
        if (errTexts!=null) {
          errTexts.add("Could not write attribute to sapmanifest.");
        }
        return null;
      }  
    }
    result=manifestex.createManifest();
    if (result==null && errTexts!=null) {
      errTexts.add("Could not create sap_manifest.");
    }
    return result;
  }
}
