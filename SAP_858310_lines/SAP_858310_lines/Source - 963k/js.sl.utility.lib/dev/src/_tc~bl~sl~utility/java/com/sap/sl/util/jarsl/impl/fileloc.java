package com.sap.sl.util.jarsl.impl;

/**
 * Title: FileLoc
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company: SAP AG
 * @author  Ralf Belger
 * @version 1.0
 */

final class FileLoc {
  private String basename;      /* name of the root */
  private String filename;      /* name of the file including path relativ to root */
  private String pathname;
  private String archfilename;  /* only when filename is not equal to the entry name in the archive */
  private boolean removefileaftercreation; /* if true then the file is deleted after it was added to the archive */
  private int hash=0;
  FileLoc(String basename, String filename) {
    this(basename,filename,null,false);
  }
  FileLoc(String basename, String filename, String archfilename) {
    this(basename,filename,archfilename,false);
  }
  FileLoc(String basename, String filename, boolean deletion) {
    this(basename,filename,null,deletion);
  }
  FileLoc(String basename, String filename, String archfilename,boolean deletion) {
    this.basename=basename;
    this.filename=filename;
    if (basename!=null && basename.compareTo("")==0) {
      this.pathname=filename;
    }
    else {
      this.pathname=basename+"/"+filename;
    }
    this.archfilename=archfilename;
    this.removefileaftercreation=deletion;
    //
    if (archfilename!=null) {
      hash=archfilename.toLowerCase().hashCode();
    }
    else if (filename!=null) {
      hash=filename.toLowerCase().hashCode();
    }
    else {
      hash=0;
    }
  }
  String base() {
    return basename;
  }
  String fname() {
    return filename;
  }
  String pathname() {
    return pathname;
  }
  String archfilename() {
    return archfilename;
  }
  boolean deleteIt() {
    return removefileaftercreation;
  }
  /**
   * The equality of the given object with the current FileLoc object is checked.
   */
  public boolean equals(Object lc) {
    String thisfilename;
    String lcfilename;
    if (lc instanceof FileLoc) {
      if (archfilename!=null) {
        thisfilename=archfilename;
      }
      else {
        thisfilename=filename;
      }
      if (((FileLoc)lc).archfilename()!=null) {
        lcfilename=((FileLoc)lc).archfilename();
      }
      else {
        lcfilename=((FileLoc)lc).fname();
      }   
      if (thisfilename.equalsIgnoreCase(lcfilename)) {
        return true;
      }
      else {
        return false;
      }
    }
    else {
      return false;
    }
  }
  public int hashCode() {
    return hash;
  }
}