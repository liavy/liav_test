package com.sap.sl.util.sduread.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;
import com.sap.sl.util.jarsl.api.ConstantsIF;
import com.sap.sl.util.jarsl.api.JarSLFactory;
import com.sap.sl.util.jarsl.api.JarSLIF;
import com.sap.sl.util.jarsl.api.JarSLManifestIF;
import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;
import com.sap.sl.util.sduread.api.SdaFile;
import com.sap.sl.util.sduread.api.SdaManifest;
import com.sap.sl.util.sduread.api.SduFileVisitor;

/**
 * @author d030435
 */

class SdaFileImpl extends SdaManifestImpl implements SdaFile,ConstantsIF {	
	String pathname=null;
  private int hash=0;
	
  SdaFileImpl(JarSLIF jarsl) throws IllFormattedSduManifestException, WrongManifestFormatException {
    this(jarsl,true);
  }
	SdaFileImpl(JarSLIF jarsl,boolean onlydeployable) throws IllFormattedSduManifestException, WrongManifestFormatException {
    super((JarSLManifestIF)jarsl,onlydeployable);
    pathname=jarsl.getArchiveName().getPath();
    if (this.getDevelopmentComponentVersion()!=null) {
      hash=("@1"+this.getPathName()+"@2"+this.getSoftwareType()+"@3"+this.getDevelopmentComponentVersion().toString()).hashCode();
    }
    else {
      hash=("@1"+this.getPathName()+"@2"+this.getSoftwareType()).hashCode();
    }
	}
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaFile#getDeploymentDescriptor()
   */
	public InputStream getDeploymentDescriptor() throws IOException {
		InputStream ins=null;
		Vector error=new Vector();
    JarSLIF jarsl=JarSLFactory.getInstance().createJarSL(pathname,"");
		if (deployfile!=null) {
			ins=jarsl.extractSingleFileAsByteArray(JARSL_METAINF+"/"+deployfile);
			if (ins==null) {
				StringBuffer tmpB = new StringBuffer();
        if (null != error) {
        	Iterator errIter = error.iterator();
          while (errIter.hasNext()) {
            tmpB.append((String)errIter.next());
          }
        }
        throw new IOException(tmpB.toString());
			}
		}	
		return ins;
	}
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaFile#getRefactoringFile()
   */
  public InputStream getRefactoringFile() throws IOException {
    InputStream ins=null;
    Vector error=new Vector();
    JarSLIF jarsl=JarSLFactory.getInstance().createJarSL(pathname,"");
    if (refactoringfile!=null) {
      ins=jarsl.extractSingleFileAsByteArray(JARSL_METAINF+"/"+refactoringfile);
      if (ins==null) {
        StringBuffer tmpB = new StringBuffer();
        if (null != error) {
          Iterator errIter = error.iterator();
          while (errIter.hasNext()) {
            tmpB.append((String)errIter.next());
          }
        }
        throw new IOException(tmpB.toString());
      }
    }
    return ins;
  }  
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduFile#getPathName()
   */
	public String getPathName() {
		return pathname;
	}
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduFile#accept()
   */
  public void accept(SduFileVisitor visitor) {
    visitor.visitSdaFile(this);
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaFile#toString()
   */
  public String toString() {
    return "SdaFile: pathname: '"+this.pathname+"' developmentComponentVersion: '" + (this.getDevelopmentComponentVersion()!=null?this.getDevelopmentComponentVersion().toString():"null") +"' softwareType: '" + this.getSoftwareType()+ "'.";
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaFile#equals()
   */
  public boolean equals(Object other) {
    if (this.archivetype.equals(((SdaManifest)other).getArchiveType())==false) {
      return false;
    }
    boolean depsequal=(other instanceof SdaFile) && ((SdaFile)other).getDependencies().length==this.getDependencies().length;
    for (int i=0; i<deps.length && depsequal; ++i) {
      boolean found=false;
      for (int j=0; j<deps.length && !found; ++j) {
        if (deps[i].equals(((SdaFile)other).getDependencies()[j])) {
          found=true;
        }
      }
      if (!found) {
        depsequal=false;
      }
    }
    if (depsequal &&
        ((SdaFile)other).getPathName().equals(this.getPathName()) &&
        ((SdaFile)other).getSoftwareType().equals(this.getSoftwareType())) {
      return false;
    }
    if (depsequal) {   
      if (this.getDevelopmentComponentVersion()==null && ((SdaFile)other).getDevelopmentComponentVersion()!=null) {
        return false;
      }
      else if (this.getDevelopmentComponentVersion()==null && ((SdaFile)other).getDevelopmentComponentVersion()==null) {
        return true;
      }
      else if (this.getDevelopmentComponentVersion().equals(((SdaFile)other).getDevelopmentComponentVersion())==false) {
        return false;
      }   
      return true;
    }
    else {
      return false;
    }
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SdaFile#hashCode()
   */
  public int hashCode() {
    return hash;
  } 
}
