package com.sap.sl.util.sduread.impl;

import java.util.Iterator;
import java.util.Vector;
import com.sap.sl.util.components.api.ComponentElementIF;
import com.sap.sl.util.components.api.ComponentFactoryIF;
import com.sap.sl.util.jarsl.api.ConstantsIF;
import com.sap.sl.util.jarsl.api.JarSLManifestIF;
import com.sap.sl.util.sduread.api.ArchiveType;
import com.sap.sl.util.sduread.api.ComponentVersion;
import com.sap.sl.util.sduread.api.Dependency;
import com.sap.sl.util.sduread.api.DevelopmentComponentVersion;
import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;
import com.sap.sl.util.sduread.api.SdaManifest;
import com.sap.sl.util.sduread.api.SduReaderFactory;
import com.sap.sl.util.sduread.api.SduSelectionEntries;

/**
 * @author d030435
 */

class SdaManifestImpl implements SdaManifest,ConstantsIF {
  final static String EOL = System.getProperty("line.separator");
  
  DevelopmentComponentVersion dcv=null;
  SduSelectionEntries selection=null;
  String softwaretype=null;
  Dependency[] deps=null;
  String deployfile=null;
  String refactoringfile=null;
  String softwaresubtype=null;
  String csncomponent=null;
  String archivetype=ArchiveType.DEVELOPMENTCOMPONENT;
  StringBuffer debuginfo=null;
  ComponentElementIF componentelement=null;
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

  SdaManifestImpl(DevelopmentComponentVersion componentversion, SduSelectionEntries selectionentries) throws IllFormattedSduManifestException {
    boolean illegalformat=false;
    StringBuffer error= new StringBuffer("Manifest attributes are missing or have badly formatted value:" + EOL);
    String _archivetype=selectionentries.getAttributeValue(ATTARCHIVETYPE);
    if (_archivetype!=null) {
      archivetype=_archivetype;
      if (!archivetype.equals(ArchiveType.DEVELOPMENTCOMPONENT) && !archivetype.equals(ArchiveType.BUSINESSCONTENT)) {
        throw new IllFormattedSduManifestException("The archivetype '"+archivetype+"' is unknown.");
      }
    }
    selection=selectionentries;
    deployfile=selectionentries.getAttributeValue(ATTDEPLOYFILE);
    if (deployfile==null || deployfile.equalsIgnoreCase("empty")) {
      deployfile=null;
    }
    else {
      softwaretype=selectionentries.getAttributeValue(ATTSOFTWARETYPE);
      softwaresubtype=selectionentries.getAttributeValue(ATTSOFTWARESUBTYPE);
    }
    refactoringfile=selectionentries.getAttributeValue(ATTREFACTORINGFILE);
    if (refactoringfile!=null && refactoringfile.equalsIgnoreCase("empty")) {
      refactoringfile=null;
    }
    csncomponent=selectionentries.getAttributeValue(ATTCSNCOMPONENT);
    if (archivetype.equals(ArchiveType.DEVELOPMENTCOMPONENT)) {
      if (componentversion==null) {
        throw new IllFormattedSduManifestException("Missing component information in SDA manifest.");
      }
      dcv=componentversion;
      DependencyParser pars=new DependencyParser(selectionentries.getAttributeValue(ATTDEPENDENCYLIST));
      deps=pars.parseXml();
      if (deps==null) {
        deps=new Dependency[0];
      }
      String _componentelement=selectionentries.getAttributeValue(ATTCOMPONENTELEMENT);
      if (_componentelement!=null) {
        componentelement=ComponentElementDeXMLizer.getComponentFromXMLString(_componentelement,"SDA MF");
        if (componentelement.getVendor()!=null && !componentelement.getVendor().equals(dcv.getVendor())) {
          error.append("the vendor defined in the component element ("+componentelement.getVendor()+") does not fit to the keyvendor attribute ("+dcv.getVendor()+")"+EOL);
          illegalformat=true;
        }
        if (componentelement.getName()!=null && !componentelement.getName().equals(dcv.getName())) {
          error.append("the name defined in the component element ("+componentelement.getName()+") does not fit to the keyname attribute ("+dcv.getName()+")"+EOL);
          illegalformat=true;
        }
        if (componentelement.getLocation()!=null && !componentelement.getLocation().equals(dcv.getLocation())) {
          error.append("the location defined in the component element ("+componentelement.getLocation()+") does not fit to the keylocation attribute ("+dcv.getLocation()+")"+EOL);
          illegalformat=true;
        }
        if (componentelement.getCounter()!=null) {
          String _counter=SduReaderFactory.getInstance().getVersionFactory().createVersion(componentelement.getCounter()).toString();
          if (!_counter.equals(dcv.getCount().toString())) {
            error.append("the counter defined in the component element ("+_counter+") does not fit to the keycounter attribute ("+dcv.getCount().toString()+")"+EOL);
            illegalformat=true;
          }
        }
//        if (illegalformat) {
//          throw new IllFormattedSduManifestException("The information about the software component found in the manifest is either missing or incomplete!"+EOL+error.toString()+EOL+"Fix the component as described in note 1171457 and redeploy it in order to resolve this error!");
//        }
      }
      else {
        componentelement=ComponentFactoryIF.getInstance().createComponentElement(dcv.getVendor(),dcv.getName(),"DC",ComponentElementIF.DEFAULTSUBSYSTEM,dcv.getLocation(),dcv.getCount().toString(),null, null, null, null,null,ComponentElementIF.FULLVERSION, null, null, null, null, null, null,null,null,null,null);
      }
    }
    else {
      if (softwaretype!=null && !softwaretype.equals("CONTENT")) {
        throw new IllFormattedSduManifestException("The archivetype 'BUSINESSCNT' can only be used in combination with software type 'CONTENT'.");
      }
      deps=new Dependency[0];
    }
    if (this.getDevelopmentComponentVersion()!=null) {
      hash=("@1"+this.getSoftwareType()+"@2"+this.getDevelopmentComponentVersion().toString()).hashCode();
    }
    else {
      hash=("@1"+this.getSoftwareType()+"@2"+"empty").hashCode();
    }
  }
  SdaManifestImpl(JarSLManifestIF jarsl) throws WrongManifestFormatException, IllFormattedSduManifestException {
    this(jarsl,true);
  }
  SdaManifestImpl(JarSLManifestIF jarsl,boolean onlydeployable) throws WrongManifestFormatException, IllFormattedSduManifestException {
    boolean illegalformat=false;
    StringBuffer error= new StringBuffer("Manifest attributes are missing or have badly formatted value:" + EOL);
    String changelistnumber=_readAttribute(jarsl,"",ATTCHANGELISTNUMBER);
    String perforceserver=_readAttribute(jarsl,"",ATTPERFORCESERVER);
    String projectname=_readAttribute(jarsl,"",ATTPROJECTNAME);
    String dtrintnumber=_readAttribute(jarsl,"",ATTDTRINTEGRATIONSEQUENCENO);
    String dtrworkspace=_readAttribute(jarsl,"",ATTDTRWORKSPACE);
    String _archivetype=_readAttribute(jarsl,"",ATTARCHIVETYPE);
    if (_readAttribute(jarsl,"",ConstantsIF.ATTSPNUMBER)!=null || _readAttribute(jarsl,"",ConstantsIF.PR_TYPE)!=null) {
      throw new WrongManifestFormatException();
    }
    if (_archivetype!=null) {
      archivetype=_archivetype;
      if (!archivetype.equals(ArchiveType.DEVELOPMENTCOMPONENT) && !archivetype.equals(ArchiveType.BUSINESSCONTENT)) {
        throw new IllFormattedSduManifestException("The archivetype '"+archivetype+"' is unknown.");
      }
    }   
    selection=new SduSelectionEntriesImpl(jarsl);
    deployfile=_readAttribute(jarsl,"",ATTDEPLOYFILE);
    if (deployfile==null || deployfile.equalsIgnoreCase("empty")) {
      deployfile=null;
      if (onlydeployable) {
        error.append("there is no deployment descriptor declared"+EOL);
        illegalformat=true;
      }
    }
    else {
      softwaretype=_readAttribute(jarsl,"",ATTSOFTWARETYPE);
      if (softwaretype==null && onlydeployable) {
        error.append("there is no softwaretype descriptor declared"+EOL);
        illegalformat=true;
      }
      softwaresubtype=_readAttribute(jarsl,"",ConstantsIF.ATTSOFTWARESUBTYPE);
    }
    refactoringfile=_readAttribute(jarsl,"",ATTREFACTORINGFILE);
    if (refactoringfile!=null && refactoringfile.equalsIgnoreCase("empty")) {
      refactoringfile=null;
    }  
    csncomponent=_readAttribute(jarsl,"",ConstantsIF.ATTCSNCOMPONENT);
    if (illegalformat) {
      if (dcv!=null) {
        throw new IllFormattedSduManifestException("The information about the development component (vendor="+(dcv.getVendor()==null?"NULL":dcv.getVendor())+", name="+(dcv.getName()==null?"NULL":dcv.getName())+") found in the manifest is either missing or incomplete!"+EOL +error.toString());
      }
      else {
        throw new IllFormattedSduManifestException("The information about the development component found in the manifest is either missing or incomplete!"+EOL +error.toString());
      }
    }
    if (archivetype.equals(ArchiveType.DEVELOPMENTCOMPONENT)) {
//      try {
        dcv=new DevelopmentComponentVersionImpl(jarsl);
//      }
//      catch (MissingComponentInfoSduManifestException e) {
//        throw new IllFormattedSduManifestException("Missing component information in SDA manifest.",e);
//      }
      DependencyParser pars=new DependencyParser(_readAttribute(jarsl,"",ATTDEPENDENCYLIST));
      deps=pars.parseXml();
      if (deps==null) {
        deps=new Dependency[0];
      } 
      String _componentelement=_readAttribute(jarsl,"",ConstantsIF.ATTCOMPONENTELEMENT);
      if (_componentelement!=null) {
        componentelement=ComponentElementDeXMLizer.getComponentFromXMLString(_componentelement,"SDA MF");
        if (componentelement.getVendor()!=null && !componentelement.getVendor().equals(dcv.getVendor())) {
          error.append("the vendor defined in the component element ("+componentelement.getVendor()+") does not fit to the keyvendor attribute ("+dcv.getVendor()+")"+EOL);
          illegalformat=true;
        }
        if (componentelement.getName()!=null && !componentelement.getName().equals(dcv.getName())) {
          error.append("the name defined in the component element ("+componentelement.getName()+") does not fit to the keyname attribute ("+dcv.getName()+")"+EOL);
          illegalformat=true;
        }
        if (componentelement.getLocation()!=null && !componentelement.getLocation().equals(dcv.getLocation())) {
          error.append("the location defined in the component element ("+componentelement.getLocation()+") does not fit to the keylocation attribute ("+dcv.getLocation()+")"+EOL);
          illegalformat=true;
        }
        if (componentelement.getCounter()!=null) {
          String _counter=SduReaderFactory.getInstance().getVersionFactory().createVersion(componentelement.getCounter()).toString();
          if (!_counter.equals(dcv.getCount().toString())) {
            error.append("the counter defined in the component element ("+_counter+") does not fit to the keycounter attribute ("+dcv.getCount().toString()+")"+EOL);
            illegalformat=true;
          }
        }
//        if (illegalformat) {
//          if (dcv!=null) {
//            throw new IllFormattedSduManifestException("The information about the development component (vendor="+(dcv.getVendor()==null?"NULL":dcv.getVendor())+", name="+(dcv.getName()==null?"NULL":dcv.getName())+") found in the manifest is either missing or incomplete!"+EOL +error.toString()+EOL+"Fix the component as described in note 1171457 and redeploy it in order to resolve this error!"););
//          }
//          else {
//            throw new IllFormattedSduManifestException("The information about the development component found in the manifest is either missing or incomplete!"+EOL +error.toString()+EOL+"Fix the component as described in note 1171457 and redeploy it in order to resolve this error!"););
//          }
//        }
      } 
      else {
        String servertype=null;
        String sourceserver=null;
        String changenumber=null;
        if (dtrworkspace!=null) {
          servertype="DTR";
          sourceserver=dtrworkspace;
          changenumber=dtrintnumber;
        }
        else {
          if (perforceserver!=null) {
            servertype="P4";
            sourceserver=perforceserver;
            changenumber=changelistnumber;
          }
        }
        componentelement=ComponentFactoryIF.getInstance().createComponentElement(dcv.getVendor(),dcv.getName(),"DC",ComponentElementIF.DEFAULTSUBSYSTEM,dcv.getLocation(),dcv.getCount().toString(),null, null, null, null,null,ComponentElementIF.FULLVERSION, null, null, null, null, null, null,servertype,sourceserver,changenumber,projectname);
      }
    }
    else {
      if (!softwaretype.equals("CONTENT")) {
        throw new IllFormattedSduManifestException("The archivetype 'BUSINESSCNT' can only be used in combination with software type 'CONTENT'.");
      }
      deps=new Dependency[0];
    }
    // debuginfo
    debuginfo=new StringBuffer();
    if (changelistnumber!=null) {
      debuginfo.append(ATTCHANGELISTNUMBER+"="+changelistnumber+";");
    }
    if (perforceserver!=null) {
      debuginfo.append(ATTPERFORCESERVER+"="+perforceserver+";");
    }
    if (projectname!=null) {
      debuginfo.append(ATTPROJECTNAME+"="+projectname+";");    
    }
    if (dtrintnumber!=null) {
      debuginfo.append(ATTDTRINTEGRATIONSEQUENCENO+"="+dtrintnumber+";");    
    }
    if (dtrworkspace!=null) {
      debuginfo.append(ATTDTRWORKSPACE+"="+dtrworkspace+";");    
    }
    if (debuginfo.length()==0) {
      debuginfo=null;
    }
    //
    if (dcv!=null) {
      hash=("@1"+softwaretype+"@2"+dcv.toString()).hashCode();
    }
    else {
      hash=("@1"+softwaretype+"@2"+"empty").hashCode();
    }
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaManifest#getDevelopmentComponentVersion()
   */
  public DevelopmentComponentVersion getDevelopmentComponentVersion() {
    return dcv;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaManifest#SduSelectionEntries()
   */
  public SduSelectionEntries getSduSelectionEntries() {
    return selection;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaManifest#getDependencies()
   */
  public Dependency[] getDependencies() {
    return deps;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaManifest#getSoftwareType()
   */
  public String getSoftwareType() {
    return softwaretype;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaManifest#getCSNComponent()
   */
  public String getCSNComponent() {
    return csncomponent;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaManifest#getSoftwareSubType()
   */
  public String getSoftwareSubType() {
    return softwaresubtype;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaManifest#getDebugInfo()
   */
  public String getDebugInfo() {
    String rc=null;
    if (debuginfo!=null) {
      rc=debuginfo.toString();
      if (rc==null || rc.length()==0) {
        rc=null;
      }
      else {
        rc=rc.substring(0,rc.length()-1);
      }
    }
    return rc;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduManifest#getComponentVersion()
   */
  public ComponentVersion getComponentVersion() {
    return dcv;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduManifest#getComponentElement()
   */
  public ComponentElementIF getComponentElement() {
    return componentelement;
  } 
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaManifest#getArchiveType()
   */
  public String getArchiveType() {
    return archivetype;
  } 
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaManifest#toString()
   */
  public String toString() {
    return "SdaManifest: developmentComponentVersion: '" + (this.getDevelopmentComponentVersion()!=null?this.getDevelopmentComponentVersion().toString():"null") +"' softwareType: '" + this.getSoftwareType()+ "'.";
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaManifest#equals()
   */
  public boolean equals(Object other) {
    if (this.archivetype.equals(((SdaManifest)other).getArchiveType())==false) {
      return false;
    }
    boolean depsequal=(other instanceof SdaManifest) && ((SdaManifest)other).getDependencies().length==this.getDependencies().length;
    for (int i=0; i<deps.length && depsequal; ++i) {
      boolean found=false;
      for (int j=0; j<deps.length && !found; ++j) {
        if (deps[i].equals(((SdaManifest)other).getDependencies()[j])) {
          found=true;
        }
      }
      if (!found) {
        depsequal=false;
      }
    }
    if (depsequal && !((SdaManifest)other).getSoftwareType().equals(this.getSoftwareType())) {
      return false;
    }
    if (depsequal) {   
      if (this.getDevelopmentComponentVersion()==null && ((SdaManifest)other).getDevelopmentComponentVersion()!=null) {
        return false;
      }
      else if (this.getDevelopmentComponentVersion()==null && ((SdaManifest)other).getDevelopmentComponentVersion()==null) {
        return true;
      }
      else if (this.getDevelopmentComponentVersion().equals(((SdaManifest)other).getDevelopmentComponentVersion())==false) {
        return false;
      }   
      return true;
    }
    else {
      return false;
    }
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaManifest#hashCode()
   */
  public int hashCode() {
    return hash;
  } 
}
