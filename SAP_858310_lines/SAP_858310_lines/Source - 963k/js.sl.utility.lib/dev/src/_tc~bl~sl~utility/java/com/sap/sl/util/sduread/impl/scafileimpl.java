package com.sap.sl.util.sduread.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;
import com.sap.sl.util.jarsl.api.ConstantsIF;
import com.sap.sl.util.jarsl.api.JarSLException;
import com.sap.sl.util.jarsl.api.JarSLIF;
import com.sap.sl.util.jarsl.api.JarSLManifestIF;
import com.sap.sl.util.sduread.api.ContentMember;
import com.sap.sl.util.sduread.api.DciaContentMember;
import com.sap.sl.util.sduread.api.DcsaContentMember;
import com.sap.sl.util.sduread.api.FileEntry;
import com.sap.sl.util.sduread.api.FileExtractionException;
import com.sap.sl.util.sduread.api.IllFormattedSduFileException;
import com.sap.sl.util.sduread.api.IllFormattedSduManifestException;
import com.sap.sl.util.sduread.api.ScaFile;
import com.sap.sl.util.sduread.api.SdaFile;
import com.sap.sl.util.sduread.api.SduContentMember;
import com.sap.sl.util.sduread.api.SduFile;
import com.sap.sl.util.sduread.api.SduFileVisitor;
import com.sap.sl.util.sduread.api.SduReaderFactory;
import com.sap.sl.util.sduread.api.SduSelectionEntries;
import com.sap.sl.util.sduread.api.SduSelectionEntry;
import com.sap.sl.util.jarsl.api.JarSLFactory;
import com.sap.sl.util.logging.api.SlUtilLogger;

/**
 * @author d030435
 */

class ScaFileImpl extends ScaManifestImpl implements ScaFile, ConstantsIF {
  private static final SlUtilLogger log = SlUtilLogger.getLogger(ScaManifestImpl.class.getName());
  String pathname=null;
  private int hash=0;
  
	ScaFileImpl(JarSLIF jarsl) throws IllFormattedSduManifestException, WrongManifestFormatException {
    super((JarSLManifestIF)jarsl);
    pathname=jarsl.getArchiveName().getPath();
    hash=("@1"+this.getPathName()+"@2"+this.getSoftwareComponentVersion().toString()).hashCode();
	}
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaFile#extract()
   */
  public SduFile[] extract(SduContentMember[] members, String[] targetFileNames) throws FileExtractionException, IllFormattedSduFileException {
    if (members == null || targetFileNames == null) {
      String errText = "Param is null.";
      log.fatal(errText);
      throw new IllegalArgumentException(errText);
    }  
    if (members.length != targetFileNames.length) {
      String errText = "Members and targetFileNames have different lengths.";
      log.fatal(errText);
      throw new IllegalArgumentException(errText);
    }
    String[] fileNamesInArchive = new String[members.length];
    for (int i=0; i < members.length; i++) {
      if (memberSet.contains(members[i]) == false) {
        String errText = "Member with index " + i + "("+members[i]+") not contained in SCA file ("+pathname+").";
        log.fatal(errText);
        throw new IllegalArgumentException(errText);
      }  
      fileNamesInArchive[i] = members[i].getFileNameWithinSdu();
    } 
    extractFiles(fileNamesInArchive, targetFileNames); 
    SduFile[] result = readExtractedSduFiles(targetFileNames); 
    return result;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaFile#extractSelection()
   */
  public SdaFile[] extractSelection(SduSelectionEntry[] selectionentries, String targetPathName) throws FileExtractionException, IllFormattedSduFileException {
    if (targetPathName == null) {
      String errText = "TargetDirectoryName is null.";
      log.fatal(errText);
      throw new IllegalArgumentException(errText);
    }
    Vector fileNamesInArchive=new Vector();
    Vector targetFileNames=new Vector();
    SduContentMember[] members=getAllContents(); // here only sdu contents are returned
    for (int i=0; i<members.length; ++i) {
      SduSelectionEntries memberselects=members[i].getSelectionEntries();
      boolean extract=true;
      if (selectionentries==null || selectionentries.length==0) {
        extract=false;
      }
      for (int j=0; extract && j<selectionentries.length; ++j) {
        String value=memberselects.getAttributeValue(selectionentries[j].getSelectionAttribute());
        if (value==null) {
          extract=false;
        }
        else {
          if (selectionentries[j].getAttributeValue().endsWith("*")) {
            String _value=selectionentries[j].getAttributeValue().substring(0,selectionentries[j].getAttributeValue().length()-1);
            if (!value.startsWith(_value)) {
              extract=false;
            }
          }
          else if (!value.equals(selectionentries[j].getAttributeValue())) {
            extract=false;
          }
        }
      }
      if (extract) {
        fileNamesInArchive.add(members[i].getFileNameWithinSdu());
        targetFileNames.add(targetPathName + '/' + members[i].getFileNameWithinSdu());
      }
    }
    extractFiles((String[])fileNamesInArchive.toArray(new String[0]),(String[])targetFileNames.toArray(new String[0]));  
    SdaFile[] result = readExtractedSdaFiles((String[])targetFileNames.toArray(new String[0])); 
    return result;  
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaFile#extractAll()
   */
  public SdaFile[] extractAll(String targetPathName) throws FileExtractionException, IllFormattedSduFileException {
    if (targetPathName == null) {
      String errText = "TargetDirectoryName is null.";
      log.fatal(errText);
      throw new IllegalArgumentException(errText);
    }
    SduContentMember[] members = getAllContents(); // here only sdu contents are returned
    Vector fileNamesInArchive=new Vector();
    Vector targetFileNames=new Vector();
    for (int i=0; i < members.length; i++) {
      // The method .extractAll(String targetDirectoryName) of the ScaFile interface extracts only contained SDAs with no defined ‘deploytarget’ attribute or with attribute value ‘J2EE_FS_DB’.
      if (members[i].getSelectionEntries().getAttributeValue(SELECT_DEPLOYTARGET)==null ||
          members[i].getSelectionEntries().getAttributeValue(SELECT_DEPLOYTARGET).equalsIgnoreCase("J2EE_FS_DB")) {
        fileNamesInArchive.add(members[i].getFileNameWithinSdu());
        targetFileNames.add(targetPathName + '/' + members[i].getFileNameWithinSdu());
      }
    }
    extractFiles((String[])fileNamesInArchive.toArray(new String[0]),(String[])targetFileNames.toArray(new String[0]));  
    SdaFile[] result = readExtractedSdaFiles((String[])targetFileNames.toArray(new String[0])); 
    return result;  
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaFile#extractAllContentMembers()
   */
  public FileEntry[] extractAllContentMembers(String targetPathName, boolean usingOriginalPath) throws FileExtractionException, IllFormattedSduFileException {
    if (targetPathName == null) {
      String errText = "TargetDirectoryName is null.";
      log.fatal(errText);
      throw new IllegalArgumentException(errText);
    }
    ContentMember[] members = getContentMembers();
    String[] fileNamesInArchive=new String[members.length];
    String[] targetFileNames=new String[members.length];
    for (int i=0; i<members.length; i++) {
      fileNamesInArchive[i]=members[i].getFileNameWithinSdu();
      if (usingOriginalPath && members[i].getOriginalFileNameWithinSdu()!=null) {
        targetFileNames[i]=targetPathName + '/' + members[i].getOriginalFileNameWithinSdu();
      }
      else {
        targetFileNames[i]=targetPathName + '/' + members[i].getFileNameWithinSdu();
      }
    }
    extractFiles(fileNamesInArchive,targetFileNames);  
    FileEntry[] result=new FileEntry[members.length];
    for (int i=0; i<members.length; i++) {
      if (members[i] instanceof SduContentMember) {
        SduFile sdur;
        try {
          sdur=SduReaderFactory.getInstance().createSduReader().readFile(targetFileNames[i]);
        }
        catch (IOException e) {
          throw new FileExtractionException(e.getMessage(),e);
        }   
        if ((sdur instanceof SdaFile) == false) {
          throw new IllFormattedSduFileException("Extracted file ("+members[i].getFileNameWithinSdu()+") is not a valid SDA file (sca="+pathname+").");
        }   
        result[i]=(SdaFile)sdur;
      }
      else if (members[i] instanceof DciaContentMember) {
        result[i]=new DciaFileImpl(targetFileNames[i]);
      }
      else if (members[i] instanceof DcsaContentMember) {
        result[i]=new DcsaFileImpl(targetFileNames[i]);
      }
      else {
        throw new IllFormattedSduFileException("Extracted file ("+members[i].getFileNameWithinSdu()+") is not a valid file (sca="+pathname+").");
      }
    }
    return result;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaFile#extractContentMembers()
   */
  public FileEntry[] extractContentMembers(ContentMember[] members, String targetPathName, boolean usingOriginalPath) throws FileExtractionException, IllFormattedSduFileException {
    if (targetPathName == null) {
      String errText = "TargetDirectoryName is null.";
      log.fatal(errText);
      throw new IllegalArgumentException(errText);
    }
    String[] targetFileNames=new String[members.length];
    for (int i=0; i<members.length; i++) {
      if (usingOriginalPath && members[i].getOriginalFileNameWithinSdu()!=null) {
        targetFileNames[i]=targetPathName + '/' + members[i].getOriginalFileNameWithinSdu();
      }
      else {
        targetFileNames[i]=targetPathName + '/' + members[i].getFileNameWithinSdu();
      }
    }
    return extractContentMembers(members,targetFileNames);
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaFile#extractContentMembers()
   */
  public FileEntry[] extractContentMembers(ContentMember[] members, String[] targetFileNames) throws FileExtractionException, IllFormattedSduFileException {
    if (members == null || targetFileNames == null) {
      String errText = "Param is null.";
      log.fatal(errText);
      throw new IllegalArgumentException(errText);
    }  
    if (members.length != targetFileNames.length) {
      String errText = "Members and targetFileNames have different lengths.";
      log.fatal(errText);
      throw new IllegalArgumentException(errText);
    }
    String[] fileNamesInArchive=new String[members.length];
    for (int i=0; i<members.length; i++) {
      if (memberSet.contains(members[i]) == false) {
        String errText = "Member with index " + i + "("+members[i]+") not contained in SCA file ("+pathname+").";
        log.fatal(errText);
        throw new IllegalArgumentException(errText);
      }
      fileNamesInArchive[i] = members[i].getFileNameWithinSdu();
    }
    extractFiles(fileNamesInArchive,targetFileNames);  
    FileEntry[] result=new FileEntry[members.length];
    for (int i=0; i<members.length; i++) {
      if (members[i] instanceof SduContentMember) {
        SduFile sdur;
        try {
          sdur=SduReaderFactory.getInstance().createSduReader().readFile(targetFileNames[i]);
        }
        catch (IOException e) {
          throw new FileExtractionException(e.getMessage(),e);
        }   
        if ((sdur instanceof SdaFile) == false) {
          throw new IllFormattedSduFileException("Extracted file ("+members[i].getFileNameWithinSdu()+") is not a valid SDA file (sca="+pathname+").");
        }   
        result[i]=(SdaFile)sdur;
      }
      else if (members[i] instanceof DciaContentMember) {
        result[i]=new DciaFileImpl(targetFileNames[i]);
      }
      else if (members[i] instanceof DcsaContentMember) {
        result[i]=new DcsaFileImpl(targetFileNames[i]);
      }
      else {
        throw new IllFormattedSduFileException("Extracted file ("+members[i].getFileNameWithinSdu()+") is not a valid file (sca="+pathname+").");
      }
    }
    return result;
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaFile#extractContentMemberAsStream()
   */
  public InputStream extractContentMemberAsStream(ContentMember member) throws FileExtractionException, IllFormattedSduFileException {
    JarSLIF jarSL=null;
    InputStream is=null;
    if (member==null) {
      String errText = "Param is null.";
      log.fatal(errText);
      throw new IllegalArgumentException(errText);
    } 
    if (memberSet.contains(member) == false) {
      String errText = "Member ("+member+") not contained in SCA file ("+pathname+").";
      log.fatal(errText);
      throw new IllegalArgumentException(errText);
    }
    try {
      jarSL=JarSLFactory.getInstance().createJarSL(pathname, "");
      is=jarSL.extractSingleFileAsStream(member.getFileNameWithinSdu());
      return is;
    }
    catch (JarSLException e) {
      throw new FileExtractionException(e.getMessage(),e);
    }
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduFile#accept()
   */
  public void accept(SduFileVisitor visitor) {
    visitor.visitScaFile(this);
  } 
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaFile#extractFiles()
   */
  private void extractFiles(String[] fileNamesInArchive, String[] targetFileNames) throws FileExtractionException {
    JarSLIF jarSL = JarSLFactory.getInstance().createJarSL(pathname, "");
    Vector errorTexts=new Vector();  
    if(jarSL.extractSingleFiles(fileNamesInArchive, targetFileNames, errorTexts)==false) {
      StringBuffer tmpB = new StringBuffer(); 
      Iterator errIter = errorTexts.iterator();
      while (errIter.hasNext()) {
        tmpB.append((String)errIter.next());
      }   
      throw new FileExtractionException(tmpB.toString());
    }
  }
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaFile#readExtractedSduFiles()
   */
  private SduFile[] readExtractedSduFiles(String[] extractedFileNames) throws IllFormattedSduFileException, FileExtractionException {
    SduFile[] result = new SduFile[extractedFileNames.length];
    for (int i=0; i < result.length; i++) {
      try {
        result[i] = SduReaderFactory.getInstance().createSduReader().readFile(extractedFileNames[i]);
      }
      catch (IOException e) {
        throw new FileExtractionException(e.getMessage(),e);
      }
    }   
    return result;
  } 
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaFile#readExtractedSdaFiles()
   */
  private SdaFile[] readExtractedSdaFiles(String[] extractedFileNames) throws IllFormattedSduFileException, FileExtractionException {
    SdaFile[] result = new SdaFile[extractedFileNames.length];
    for (int i=0; i < result.length; i++) {
      SduFile sdur;
      try {
        sdur = SduReaderFactory.getInstance().createSduReader().readFile(extractedFileNames[i]);
      }
      catch (IOException e) {
        throw new FileExtractionException(e.getMessage(),e);
      }   
      if ((sdur instanceof SdaFile) == false) {
        throw new IllFormattedSduFileException("Extracted file ("+sdur.getPathName()+") is not a valid SDA file (sca="+pathname+").");
      }   
      result[i] = (SdaFile) sdur;
    } 
    return result;
  }  
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduFile#getPathName()
   */
	public String getPathName() {
		return pathname;
	}
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.SduFile#getRefactoringFile()
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
   * @see com.sap.sdm.util.sduread.ScaFile#toString()
   */
  public String toString() {
    return "ScaFile: pathname: '"+this.pathname+"' softwareComponentVersion: '" + this.getSoftwareComponentVersion().toString() + "'.";
  } 
  /* (non-Javadoc)
   * @see com.sap.sdm.util.sduread.ScaFile#equals()
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }   
    if (other == null) {
      return false;
    }  
    if (this.getClass().equals(other.getClass()) == false) {
      return false;
    }  
    ScaFileImpl otherScaFile = (ScaFileImpl) other;  
    if (this.getPathName().equals(otherScaFile.getPathName()) == false) {
      return false;
    }  
    if (this.getSoftwareComponentVersion().equals(otherScaFile.getSoftwareComponentVersion()) == false) {
      return false;
    }   
    if (this.memberSet.equals(otherScaFile.memberSet) == false) {
      return false;
    }  
    return true;
  }  
  public int hashCode() {
    return hash;
  } 
}
