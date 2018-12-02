package com.sap.sl.util.sduread.impl;

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
import com.sap.sl.util.sduread.api.IllFormattedSduFileException;
import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;
import com.sap.sl.util.sduread.api.SduFile;
import com.sap.sl.util.sduread.api.SduManifest;
import com.sap.sl.util.sduread.api.SduReader;
import com.sap.sl.util.sduread.api.VersionInfoCompatibility;

/**
 * @author d030435
 */

class SduReaderImpl implements SduReader,ConstantsIF {
  SduReaderImpl() {
  }
  private String _readAttribute(JarSLManifestIF jarsl, String entry, String name) throws IllFormattedSduFileException {
    Vector errorText=new Vector();
    String rc=jarsl.readAttribute(entry,name,errorText);
    if (errorText.size()>0) {
      StringBuffer tmpb = new StringBuffer();
      Iterator errIter = errorText.iterator();
      while (errIter.hasNext()) {
        tmpb.append((String)errIter.next());
      }
      throw new IllFormattedSduFileException("Error during attribute reading (entry="+entry+"name="+name+"): "+tmpb.toString());
    }
    return rc;
  }
  
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduReader#readFile()
   */
  public SduFile readFile(String pathName) throws IllFormattedSduFileException, IOException {
    return readFile(pathName,true);
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduReader#readFile()
   */
	public SduFile readFile(String pathName,boolean onlydeployable) throws IllFormattedSduFileException, IOException {
    String sda_sdmsdacompvers=null;
    String ext_sda_sdmsdacompvers=null;
    SduFile sdufile=null;
		JarSLIF jarsl=null;
    if (!JarSLFactory.getInstance().checkFileExistence(pathName)) {
      throw new IOException("The file ("+pathName+") does not exist on the filesystem.");
    }
    jarsl=JarSLFactory.getInstance().createJarSL(pathName,"");
    sda_sdmsdacompvers=_readAttribute(jarsl,"",ATTSDMSDACOMPVERSION);
    ext_sda_sdmsdacompvers=_readAttribute(jarsl,"",ATTEXTSDMSDACOMPVERSION);
    if (ext_sda_sdmsdacompvers!=null) {
      int ext_sdm_sdmsdacompvers=VersionInfoCompatibility.extcompsdaversinfo;
      if ((new Integer(ext_sda_sdmsdacompvers)).intValue()>ext_sdm_sdmsdacompvers) {
        String message="The current SduReader is too old for the SDA ("+pathName+"). The SDM-SDA compatible version must be "+ext_sda_sdmsdacompvers+" or greater.";    
        throw new IllFormattedSduFileException(message);
      }
    }
    try {      
  		try {
  			sdufile=new SdaFileImpl(jarsl,onlydeployable);
  		}
  		catch (WrongManifestFormatException e) {
  			sdufile=null;
  		}
      catch (IllFormattedSduManifestException e) {
        throw new IllFormattedSduFileException(e.getMessage()+" ("+pathName+")",e);
      }
  		if (sdufile==null) {
  			try {
  				sdufile=new ScaFileImpl(jarsl);
  			}
  			catch (WrongManifestFormatException e) {
  				sdufile=null;
  			}
        catch (IllFormattedSduManifestException e) {
          throw new IllFormattedSduFileException(e.getMessage()+" ("+pathName+")",e);
        }
  		}
  		if (sdufile==null) {
  			throw new IllFormattedSduFileException("Archive is neither a SDA nor a SCA ("+pathName+").");
  		}   
    }
    catch (IllFormattedSduFileException e) {
      if (sda_sdmsdacompvers!=null && ext_sda_sdmsdacompvers==null) {
        throw new IllFormattedSduFileException("The archive ("+pathName+") must be deployed with a 6.20 SDM, which has a SDM-SDA compatible version "+sda_sdmsdacompvers+" or greater.");
      }
      else {
        throw e;
      }
    }
		return sdufile;
	}
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduReader#readManifests()
   */
  public SduManifest readManifests(String pathNameOfManifestFile, String pathNameOfSapManifestFile) throws IllFormattedSduManifestException, IOException {
    SduManifest sdumanifest=null;
    String ext_sda_sdmsdacompvers=null;
    JarSLManifestIF jarsl=null;
    InputStream manifest=null;
    InputStream sapmanifest=null;
    try {
      if (!JarSLFactory.getInstance().checkFileExistence(pathNameOfManifestFile)) {
        throw new IOException("The file ("+pathNameOfManifestFile+") does not exist on the filesystem.");
      }
      if (!JarSLFactory.getInstance().checkFileExistence(pathNameOfSapManifestFile)) {
        throw new IOException("The file ("+pathNameOfSapManifestFile+") does not exist on the filesystem.");
      }
      manifest=new FileInputStream(pathNameOfManifestFile);
      sapmanifest=new FileInputStream(pathNameOfSapManifestFile);
      jarsl=JarSLFactory.getInstance().createJarSLManifest(manifest,sapmanifest);
    }
    catch (JarSLManifestException e) {
      throw new IllFormattedSduManifestException(e.getMessage()+" ("+pathNameOfManifestFile+" / "+pathNameOfSapManifestFile+")",e);
    }
    finally {
      if (manifest!=null) {
        manifest.close();
      }
      if (sapmanifest!=null) {
        sapmanifest.close();
      }
    }
    //
    try {
      ext_sda_sdmsdacompvers=_readAttribute(jarsl,"",ATTEXTSDMSDACOMPVERSION);
    }
    catch (IllFormattedSduFileException e) {
      throw new IllFormattedSduManifestException(e.getMessage()+" ("+pathNameOfManifestFile+" / "+pathNameOfSapManifestFile+")",e);
    }
    if (ext_sda_sdmsdacompvers!=null) {
      int ext_sdm_sdmsdacompvers=VersionInfoCompatibility.extcompsdaversinfo;
      if ((new Integer(ext_sda_sdmsdacompvers)).intValue()>ext_sdm_sdmsdacompvers) {
        String message="The current SduReader is too old for the manifests ("+pathNameOfManifestFile+" / "+pathNameOfSapManifestFile+"). The SDM-SDA compatible version must be "+ext_sda_sdmsdacompvers+" or greater.";    
        throw new IllFormattedSduManifestException(message);
      }
    }     
    try {
      sdumanifest=new SdaManifestImpl(jarsl);
    }
    catch (WrongManifestFormatException e) {
      sdumanifest=null;
    }
    catch (IllFormattedSduManifestException e) {
      throw new IllFormattedSduManifestException(e.getMessage()+" ("+pathNameOfManifestFile+" / "+pathNameOfSapManifestFile+")",e);
    }
    if (sdumanifest==null) {
      try {
        sdumanifest=new ScaManifestImpl(jarsl);
      }
      catch (WrongManifestFormatException e) {
        sdumanifest=null;
      }
      catch (IllFormattedSduManifestException e) {
        throw new IllFormattedSduManifestException(e.getMessage()+" ("+pathNameOfManifestFile+" / "+pathNameOfSapManifestFile+")",e);
      }
    }
    if (sdumanifest==null) {
      throw new IllFormattedSduManifestException("The input values are neither manifests of a SDA nor of a SCA ("+pathNameOfManifestFile+" / "+pathNameOfSapManifestFile+")");
    }   
    return sdumanifest;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduReader#getManifestInputStream()
   */
  public InputStream getManifestInputStream(String pathName) throws IllFormattedSduFileException, IOException {
    JarSLIF jarsl=JarSLFactory.getInstance().createJarSL(pathName,null);
    InputStream is=null;
    Vector error=new Vector();
    // to check syntax
    readFile(pathName);
    //
    if((is=jarsl.getManifestAsStream(error))==null) {
      StringBuffer tmpB = new StringBuffer();
      if (null != error) {
        Iterator errIter = error.iterator();
        while (errIter.hasNext()) {
          tmpB.append((String)errIter.next());
        }
      }
      throw new IOException(tmpB.toString());
    }
    return is;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduReader#getSapManifestInputStream()
   */
  public InputStream getSapManifestInputStream(String pathName) throws IllFormattedSduFileException, IOException {
    JarSLIF jarsl=JarSLFactory.getInstance().createJarSL(pathName,null);
    InputStream is=null;
    Vector error=new Vector();
    // to check syntax
    readFile(pathName);
    //
    if((is=jarsl.getSapManifestAsStream(error))==null) {
      StringBuffer tmpB = new StringBuffer();
      if (null != error) {
        Iterator errIter = error.iterator();
        while (errIter.hasNext()) {
          tmpB.append((String)errIter.next());
        }
      }
      throw new IOException(tmpB.toString());
    }
    return is;
  }
}
