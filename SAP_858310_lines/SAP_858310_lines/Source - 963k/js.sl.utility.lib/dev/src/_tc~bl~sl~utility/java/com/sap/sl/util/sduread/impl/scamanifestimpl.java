package com.sap.sl.util.sduread.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.api.ComponentFactoryIF;
import com.sap.sl.util.jarsl.api.ConstantsIF;
import com.sap.sl.util.jarsl.api.JarSLManifestIF;
import com.sap.sl.util.sduread.api.ArchiveType;
import com.sap.sl.util.sduread.api.ComponentVersion;
import com.sap.sl.util.sduread.api.ContentMember;
import com.sap.sl.util.sduread.api.DevelopmentComponentVersion;
import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;
import com.sap.sl.util.sduread.api.ScaManifest;
import com.sap.sl.util.sduread.api.SdaManifest;
import com.sap.sl.util.sduread.api.SduContentMember;
import com.sap.sl.util.sduread.api.SduReaderFactory;
import com.sap.sl.util.sduread.api.SduSelectionEntries;
import com.sap.sl.util.sduread.api.SoftwareComponentVersion;

/**
 * @author d030435
 */

class ScaManifestImpl implements ScaManifest,ConstantsIF {
  final static String EOL = System.getProperty("line.separator");
  
  SoftwareComponentVersion scv=null;
  Set memberSet=null;
  ComponentElementIF componentelement;
  String refactoringfile=null;
  private int hash=0;
  
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
  private String[] cutOffString(String in,String delim) {
    if (in==null) {
      return new String[0];
    }
    else {
      Vector elements=new Vector();
      in=in.trim();
      StringTokenizer st=new StringTokenizer(in,delim);
      while(st.hasMoreTokens()) {
        elements.addElement(st.nextToken().trim());
      }
      return (String[])elements.toArray(new String[0]);
    }
  }
 
  ScaManifestImpl(JarSLManifestIF jarsl) throws WrongManifestFormatException, IllFormattedSduManifestException {
    boolean illegalformat=false;
    StringBuffer error= new StringBuffer("Manifest attributes are missing or have badly formatted value:" + EOL);
    String servicelevel=_readAttribute(jarsl,"",ConstantsIF.ATTSPNUMBER);
    String patchlevel=_readAttribute(jarsl,"",ConstantsIF.ATTSPPATCHLEVEL);
    String release=_readAttribute(jarsl,"",ConstantsIF.ATTRELEASE);
    String pr_type=_readAttribute(jarsl,"",ConstantsIF.PR_TYPE);
    String pr_servicelevel=_readAttribute(jarsl,"",ConstantsIF.PR_SERVICELEVEL);
    String pr_patchlevel=_readAttribute(jarsl,"",ConstantsIF.PR_PATCHLEVEL);
    String pr_release=_readAttribute(jarsl,"",ConstantsIF.PR_RELEASE);
    if (servicelevel==null && (pr_type==null || !pr_type.equals("SC"))) {
      throw new WrongManifestFormatException();
    }
    if (pr_type!=null && !pr_type.equals("SC")) {
      error.append("the component type defined in the pr_type attribute ("+pr_type+") does not fit to archive format (SC)"+EOL);
      illegalformat=true;
    }
    if (servicelevel==null) {
      servicelevel=pr_servicelevel;
    }
    else if (pr_servicelevel!=null && !pr_servicelevel.equals(servicelevel)) {
      error.append("the servicelevel defined in the pr_servicelevel attribute ("+pr_servicelevel+") does not fit to the SP-Number attribute ("+servicelevel+")"+EOL);
      illegalformat=true;
    }
    if (patchlevel==null) {
      patchlevel=pr_patchlevel;
    }
    else if (pr_patchlevel!=null && !pr_patchlevel.equals(patchlevel)) {
      error.append("the patchlevel defined in the pr_patchlevel attribute ("+pr_patchlevel+") does not fit to the SP-Patchlevel attribute ("+patchlevel+")"+EOL);
      illegalformat=true;
    }
    if (release==null) {
      release=pr_release;
    }
    else if (pr_release!=null && !pr_release.equals(release)) {
      error.append("the release defined in the pr_release attribute ("+pr_release+") does not fit to the release attribute ("+release+")"+EOL);
      illegalformat=true;
    }
    if (illegalformat) {
      throw new IllFormattedSduManifestException("The information about the software component found in the manifest is either missing or incomplete!"+EOL +error.toString());
    }
    scv=new SoftwareComponentVersionImpl(jarsl);
    refactoringfile=_readAttribute(jarsl,"",ATTREFACTORINGFILE);
    if (refactoringfile!=null && refactoringfile.equalsIgnoreCase("empty")) {
      refactoringfile=null;
    }
    String _componentelement=_readAttribute(jarsl,"",ConstantsIF.ATTCOMPONENTELEMENT);
    if (_componentelement!=null) {
      componentelement=ComponentElementDeXMLizer.getComponentFromXMLString(_componentelement,"SCA MF");
      if (componentelement.getVendor()!=null && !componentelement.getVendor().equals(scv.getVendor())) {
        error.append("the vendor defined in the component element ("+componentelement.getVendor()+") does not fit to the keyvendor attribute ("+scv.getVendor()+")"+EOL);
        illegalformat=true;
      }
      if (componentelement.getName()!=null && !componentelement.getName().equals(scv.getName())) {
        error.append("the name defined in the component element ("+componentelement.getName()+") does not fit to the keyname attribute ("+scv.getName()+")"+EOL);
        illegalformat=true;
      }
      if (componentelement.getComponentType()!=null && !componentelement.getComponentType().equals("SC")) {
        error.append("the component type defined in the component element ("+componentelement.getComponentType()+") does not fit to archive format (SC)"+EOL);
        illegalformat=true;
      }
      if (componentelement.getLocation()!=null && !componentelement.getLocation().equals(scv.getLocation())) {
        error.append("the location defined in the component element ("+componentelement.getLocation()+") does not fit to the keylocation attribute ("+scv.getLocation()+")"+EOL);
        illegalformat=true;
      }
      if (componentelement.getCounter()!=null) {
        String _counter=SduReaderFactory.getInstance().getVersionFactory().createVersion(componentelement.getCounter()).toString();
        if (!_counter.equals(scv.getCount().toString())) {
          error.append("the counter defined in the component element ("+_counter+") does not fit to the keycounter attribute ("+scv.getCount().toString()+")"+EOL);
          illegalformat=true;
        }
      }
      if (componentelement.getSCName()!=null && !componentelement.getSCName().equals(scv.getName())) {
        error.append("the scname defined in the component element ("+componentelement.getSCName()+") does not fit to the keyname attribute ("+scv.getName()+")"+EOL);
        illegalformat=true;
      }
      if (componentelement.getSCVendor()!=null && !componentelement.getSCVendor().equals(scv.getVendor())) {
        error.append("the scvendor defined in the component element ("+componentelement.getSCVendor()+") does not fit to the keyvendor attribute ("+scv.getVendor()+")"+EOL);
        illegalformat=true;
      }
      if (componentelement.getRelease()!=null && !componentelement.getRelease().equals(release)) {
        error.append("the release defined in the component element ("+componentelement.getRelease()+") does not fit to the release attribute ("+release+")"+EOL);
        illegalformat=true;
      }
      if (componentelement.getServiceLevel()!=null && !componentelement.getServiceLevel().equals(servicelevel)) {
        error.append("the servicelevel defined in the component element ("+componentelement.getServiceLevel()+") does not fit to the servicelevel attribute ("+servicelevel+")"+EOL);
        illegalformat=true;
      }
      if (componentelement.getPatchLevel()!=null && !componentelement.getPatchLevel().equals(patchlevel)) {
        error.append("the patchlevel defined in the component element ("+componentelement.getPatchLevel()+") does not fit to the patchlevel attribute ("+patchlevel+")"+EOL);
        illegalformat=true;
      }
//      if (illegalformat) {
//        if (scv!=null) {
//          throw new IllFormattedSduManifestException("The information about the software component (vendor="+(scv.getVendor()==null?"NULL":scv.getVendor())+", name="+(scv.getName()==null?"NULL":scv.getName())+") found in the manifest is either missing or incomplete!"+EOL +error.toString()+EOL+"Fix the component as described in note 1171457 and redeploy it in order to resolve this error!"););
//        }
//        else {
//          throw new IllFormattedSduManifestException("The information about the software component found in the manifest is either missing or incomplete!"+EOL +error.toString()+EOL+"Fix the component as described in note 1171457 and redeploy it in order to resolve this error!"););
//        }
//      }
    }
    else {
      String ppmsnumber=_readAttribute(jarsl,"",ConstantsIF.ATTSOFTPPMSNUMBER);
      String deltaversion=_readAttribute(jarsl,"",ConstantsIF.PR_DELTAVERSION);
      String updateversion=_readAttribute(jarsl,"",ConstantsIF.PR_UPDATEVERSION);
      if (deltaversion==null) {
        deltaversion=ComponentElementIF.FULLVERSION;
      }   
      componentelement=ComponentFactoryIF.getInstance().createComponentElement(
        scv.getVendor(), scv.getName(), "SC",
        ComponentElementIF.DEFAULTSUBSYSTEM, scv.getLocation(),
        scv.getCount().toString(), scv.getVendor(), scv.getName(),
        release, servicelevel, patchlevel, deltaversion, updateversion,
        null, ppmsnumber, null, null, null, null, null, null, null);
    }
    // analysing of members
    Set tempMemberSet = new HashSet();   
    String[] originalfilenames=null;
    // SDUs
    String[] containedDCs=cutOffString(jarsl.readAttribute("",PR_DEPLOYARCHIVEDIR),";");
    if (containedDCs!=null && containedDCs.length!=0) {
      originalfilenames=cutOffString(jarsl.readAttribute("",PR_ORIGINALDEPLOYARCHIVEDIR),";");
      if (originalfilenames!=null && originalfilenames.length!=0 && originalfilenames.length!=containedDCs.length) {
        throw new IllFormattedSduManifestException("The mapping between the archive file entries and the original file name information is invalid.");
      }
      for (int i=0; containedDCs!=null && i<containedDCs.length; ++i) {
        DevelopmentComponentVersion currCV=null;
        SdaManifest sdamanifest=null;
        SduSelectionEntries selections=new SduSelectionEntriesImpl(jarsl, containedDCs[i]);
        if (selections.getAttributeValue(ATTARCHIVETYPE)==null || selections.getAttributeValue(ATTARCHIVETYPE).equals(ArchiveType.DEVELOPMENTCOMPONENT)) {
          sdamanifest=new SdaManifestImpl(new DevelopmentComponentVersionImpl(jarsl, containedDCs[i]),selections);
        }
        else if (selections.getAttributeValue(ATTARCHIVETYPE).equals(ArchiveType.BUSINESSCONTENT)) {
          sdamanifest=new SdaManifestImpl(null,selections);
        }
        else {
          throw new IllFormattedSduManifestException("The information about the software component found in the manifest is invalid, because entry ("+containedDCs[i]+") has an unknown archivetype");
        }
        if (originalfilenames!=null && originalfilenames.length!=0) {
          tempMemberSet.add(new SdaContentMemberImpl(sdamanifest, containedDCs[i], originalfilenames[i]));
        }
        else {
          tempMemberSet.add(new SdaContentMemberImpl(sdamanifest, containedDCs[i], null));
        }
      }   
    }
    else {
      containedDCs=jarsl.getArchiveEntriesFromManifest();
      for (int i=0; containedDCs!=null && i<containedDCs.length; ++i) {
        DevelopmentComponentVersion currCV=null;
        SdaManifest sdamanifest=null;
        SduSelectionEntries selections=new SduSelectionEntriesImpl(jarsl, containedDCs[i]);
        if (selections.getAttributeValue(ATTARCHIVETYPE)==null || selections.getAttributeValue(ATTARCHIVETYPE).equals(ArchiveType.DEVELOPMENTCOMPONENT)) {
          sdamanifest=new SdaManifestImpl(new DevelopmentComponentVersionImpl(jarsl, containedDCs[i]),selections);
        }
        else if (selections.getAttributeValue(ATTARCHIVETYPE).equals(ArchiveType.BUSINESSCONTENT)) {
          sdamanifest=new SdaManifestImpl(null,selections);
        }
        else {
          throw new IllFormattedSduManifestException("The information about the software component found in the manifest is invalid, because entry ("+containedDCs[i]+") has an unknown archivetype");
        }
        tempMemberSet.add(new SdaContentMemberImpl(sdamanifest, containedDCs[i], null));
      }   
    }
    // DCIAs
    String[] containedDCIAs=cutOffString(jarsl.readAttribute("",PR_BUILDARCHIVEDIR),";");
    originalfilenames=cutOffString(jarsl.readAttribute("",PR_ORIGINALBUILDARCHIVEDIR),";");
    if (containedDCIAs!=null && originalfilenames!=null && originalfilenames.length!=0 && originalfilenames.length!=containedDCIAs.length) {
      throw new IllFormattedSduManifestException("The mapping between the archive file entries and the original file name information is invalid.");
    }
    for (int i=0; i<containedDCIAs.length; ++i) {
      if (originalfilenames!=null && originalfilenames.length!=0) {
        tempMemberSet.add(new DciaContentMemberImpl(containedDCIAs[i],originalfilenames[i]));
      }
      else {
        tempMemberSet.add(new DciaContentMemberImpl(containedDCIAs[i],null));
      }
    }
    // DCSs
    String[] containedDCSAs=cutOffString(jarsl.readAttribute("",CE_SOURCEARCHIVEDIR),";");
    originalfilenames=cutOffString(jarsl.readAttribute("",CE_ORIGINALSOURCEARCHIVEDIR),";");
    if (containedDCSAs!=null && originalfilenames!=null && originalfilenames.length!=0 && originalfilenames.length!=containedDCSAs.length) {
      throw new IllFormattedSduManifestException("The mapping between the archive file entries and the original file name information is invalid.");
    }
    for (int i=0; i<containedDCSAs.length; ++i) {
      if (originalfilenames!=null && originalfilenames.length!=0) {
        tempMemberSet.add(new DcsaContentMemberImpl(containedDCSAs[i],originalfilenames[i]));
      }
      else {
        tempMemberSet.add(new DcsaContentMemberImpl(containedDCSAs[i],null));
      }
    }
    // 
    memberSet=Collections.unmodifiableSet(tempMemberSet);
    hash=("@1"+this.getSoftwareComponentVersion().toString()).hashCode();
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaManifest#getSoftwareComponentVersion()
   */
  public SoftwareComponentVersion getSoftwareComponentVersion() {
    return scv;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduManifest#getComponentVersion()
   */
  public ComponentVersion getComponentVersion() {
    return scv;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduManifest#getComponentElement()
   */
  public ComponentElementIF getComponentElement() {
    return componentelement;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaManifest#getContents()
   */
  public SduContentMember[] getContents() {
    Set _memberset=new HashSet();
    Iterator iter=memberSet.iterator();
    while (iter.hasNext()) {
      ContentMember cmem=(ContentMember)iter.next();
      if (cmem instanceof SduContentMember) {
        SduContentMember mem=(SduContentMember)cmem;
        if (mem.getSelectionEntries().getAttributeValue(SELECT_DEPLOYTARGET)==null ||
            mem.getSelectionEntries().getAttributeValue(SELECT_DEPLOYTARGET).equalsIgnoreCase("J2EE_FS_DB")) {
        _memberset.add(mem);
        }
      }
    }
    Set __memberset=Collections.unmodifiableSet(_memberset);  
    return (SduContentMember[]) __memberset.toArray(new SduContentMember[__memberset.size()]);
  } 
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaManifest#getAllContents()
   */
  public SduContentMember[] getAllContents() {
    Set _memberset=new HashSet();
    Iterator iter=memberSet.iterator();
    while (iter.hasNext()) {
      ContentMember cmem=(ContentMember)iter.next();
      if (cmem instanceof SduContentMember) {
        SduContentMember mem=(SduContentMember)cmem;
        _memberset.add(mem);
      }
    }
    Set __memberset=Collections.unmodifiableSet(_memberset);  
    return (SduContentMember[]) __memberset.toArray(new SduContentMember[__memberset.size()]);
  } 
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaManifest#getContentMembers()
   */
  public ContentMember[] getContentMembers() {
    return (ContentMember[])memberSet.toArray(new ContentMember[memberSet.size()]);
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaManifest#toString()
   */
  public String toString() {
    return "ScaManifest: softwareComponentVersion: '" + this.getSoftwareComponentVersion().toString() + "'.";
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaManifest#equals()
   */
  public boolean equals(Object other) {
    if (this==other) {
      return true;
    } 
    else if (other==null) {
      return false;
    }
    else if (this.getClass().equals(other.getClass()) == false) {
      return false;
    }
    ScaManifestImpl otherMf = (ScaManifestImpl)other;
    if (this.getSoftwareComponentVersion().equals(otherMf.getSoftwareComponentVersion())==false) {
      return false;
    }  
    else if (this.memberSet.equals(otherMf.memberSet)==false) {
      return false;
    }   
    return true;
  } 
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaManifest#hashCode()
   */
  public int hashCode() {
    return hash;
  }
}
