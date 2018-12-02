package com.sap.sl.util.sduwrite.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import com.sap.sl.util.jarsl.api.ConstantsIF;
import com.sap.sl.util.jarsl.api.JarSLFactory;
import com.sap.sl.util.jarsl.api.JarSLIF;
import com.sap.sl.util.jarsl.api.JarSLManifestException;
import com.sap.sl.util.jarsl.api.JarSLManifestIF;
import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;
import com.sap.sl.util.sduread.api.ScaManifest;
import com.sap.sl.util.sduread.api.SdaFile;
import com.sap.sl.util.sduread.api.SduContentMember;
import com.sap.sl.util.sduread.api.SduManifest;
import com.sap.sl.util.sduread.api.SduReaderFactory;
import com.sap.sl.util.sduwrite.api.SduWriter;
import com.sap.sl.util.sduwrite.api.SduWriterException;

/**
 * @author d030435
 */

class SduWriterImpl implements SduWriter, ConstantsIF {
  private String _readAttribute(JarSLManifestIF jarsl, String entry, String name) throws IllFormattedSduManifestException {
    Vector errorText=new Vector();
    String rc=jarsl.readAttribute(entry,name,errorText);
    if (errorText.size()>0) {
      StringBuffer tmpb = new StringBuffer();
      Iterator errIter = errorText.iterator();
      while (errIter.hasNext()) {
        tmpb.append((String)errIter.next());
      }
      throw new IllFormattedSduManifestException("Error during attribute reading: "+tmpb.toString());
    }
    return rc;
  }
  
  SduWriterImpl() {
  }
  public void createScaFile(String targetPathOfScaFile, String pathNameOfManifestFile, String pathNameOfSapManifestFile, SdaFile[] containedSdas) throws SduWriterException, IllFormattedSduManifestException, IOException {
    Vector errortext=new Vector();
    boolean compressfile=false;
    // check
    SduManifest sdumanifest=SduReaderFactory.getInstance().createSduReader().readManifests(pathNameOfManifestFile,pathNameOfSapManifestFile);
    if (!(sdumanifest instanceof ScaManifest)) {
      throw new IllFormattedSduManifestException("The input values are no manifests of a SCA");
    }
    SduContentMember[] members=((ScaManifest)sdumanifest).getContents();
    if (members.length!=containedSdas.length) {
      throw new IllFormattedSduManifestException("The manifest content does not fit to the given SDAs");
    }
    // creation
    InputStream manifest=null;
    InputStream sapmanifest=null;
    JarSLManifestIF jarslmf=null;
    JarSLIF jarsl=null;
    try {
      manifest=new FileInputStream(pathNameOfManifestFile);
      sapmanifest=new FileInputStream(pathNameOfSapManifestFile);
      jarslmf=JarSLFactory.getInstance().createJarSLManifest(manifest,sapmanifest);
    }
    catch (JarSLManifestException e) {
      throw new IllFormattedSduManifestException(e.getMessage());
    }
    finally {
      if (manifest!=null) {
        manifest.close();
      }
      if (sapmanifest!=null) {
        sapmanifest.close();
      }
    }
    jarsl=JarSLFactory.getInstance().createJarSL(targetPathOfScaFile,null);
    if (!jarsl.writeAttributesFromJarSLManifest(jarslmf)) {
      throw new SduWriterException("Cannot create file "+targetPathOfScaFile+": error during manifest creation");
    }
    String compress=_readAttribute(jarsl,"",ATTCOMPRESS);
    if (compress!=null && compress.equalsIgnoreCase("true")) {
      compressfile=true;
    }
    jarsl.deleteAttribute("",ATTCOMPRESS);
    if (_readAttribute(jarsl,"",PR_TYPE)!=null) {  // NWDI format
      jarsl.deleteAttribute("",PR_BUILDARCHIVEDIR);
      jarsl.deleteAttribute("",PR_SOURCEARCHIVEDIR); 
      jarsl.deleteAttribute("",CE_SOURCEARCHIVEDIR);
      jarsl.deleteAttribute("",PR_ORIGINALBUILDARCHIVEDIR);
      jarsl.deleteAttribute("",PR_ORIGINALSOURCEARCHIVEDIR);
      jarsl.deleteAttribute("",CE_ORIGINALSOURCEARCHIVEDIR);
      //
      jarsl.deleteAttribute("",PR_PHYSICALSOURCEINCLUDED);
      jarsl.deleteAttribute("",PR_XIWSCONTENTEXPORTED);
      //
      if (!jarsl.writeAttribute("",PR_APPROVALSTATUS,"EXTERNAL")) {
        throw new SduWriterException("Cannot create file "+targetPathOfScaFile+": error during manifest creation");
      }
      if (!jarsl.writeAttribute("",ATTSAPMANIFESTORIGIN,"sduwriter(createScaFile)")) {
        throw new SduWriterException("Cannot create file "+targetPathOfScaFile+": error during manifest creation");
      }
    }
    for (int j=0; j<containedSdas.length; ++j) {
      boolean found=false;
      for (int i=0; i<members.length; ++i) {
        if (members[i].getComponentVersion().getName().equals(containedSdas[j].getDevelopmentComponentVersion().getName()) &&
            members[i].getComponentVersion().getVendor().equals(containedSdas[j].getDevelopmentComponentVersion().getVendor()) &&
            members[i].getComponentVersion().getLocation().equals(containedSdas[j].getDevelopmentComponentVersion().getLocation()) &&
            members[i].getComponentVersion().getCount().isEquivalent(containedSdas[j].getDevelopmentComponentVersion().getCount())) {
          found=true;
          if (!jarsl.addFile("",containedSdas[j].getPathName(),members[i].getFileNameWithinSdu(),errortext)) {
            StringBuffer tmpB = new StringBuffer();
            Iterator errIter = errortext.iterator();
            while (errIter.hasNext()) {
              tmpB.append((String)errIter.next());
            }
            throw new SduWriterException("Cannot create file "+targetPathOfScaFile+": "+tmpB.toString());
          }       
        }
      }
      if (!found) {
        throw new IllFormattedSduManifestException("The manifest content does not fit to the given SDAs");
      }
    }
    if (!jarsl.create(compressfile,false,true,errortext)) {
      StringBuffer tmpB = new StringBuffer();
      Iterator errIter = errortext.iterator();
      while (errIter.hasNext()) {
        tmpB.append((String)errIter.next());
      }
      throw new SduWriterException("Cannot create file "+targetPathOfScaFile+": "+tmpB.toString());
    }       
  }
}