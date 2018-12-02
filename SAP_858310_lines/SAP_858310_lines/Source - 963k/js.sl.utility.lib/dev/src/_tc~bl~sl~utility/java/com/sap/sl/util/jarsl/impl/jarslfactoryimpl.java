package com.sap.sl.util.jarsl.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import com.sap.sl.util.jarsl.api.JarSLFactory;
import com.sap.sl.util.jarsl.api.JarSLFileInfoIF;
import com.sap.sl.util.jarsl.api.JarSLIF;
import com.sap.sl.util.jarsl.api.JarSLManifestException;
import com.sap.sl.util.jarsl.api.JarSLManifestIF;

/**
 * @author d030435
 */

public class JarSLFactoryImpl extends JarSLFactory {
  public JarSLFactoryImpl() {
  }
  public JarSLManifestIF createJarSLManifest(InputStream manifest, InputStream sapmanifest) throws JarSLManifestException {
    return new JarSLManifest(manifest,sapmanifest);
  }
  public JarSLFileInfoIF createJarSLFileInfo(String filename) {
    return new JarSLFileInfo(filename);
  }
  public JarSLIF createJarSL(String jarfilename, String rootdir) {
    return new JarSL(jarfilename, rootdir);
  }
  /**
   * This method returns the current jarSL version      
   * @return String                    the jarSL version
   */
  public String getJarSLVersion() {
    return JarSL.getJarSLVersion();
  }
  /**
   * This method returns the current JarSLManifest version      
   * @return String                    the JarSLManifest version
   */
  public String getJarSLManifestVersion() {
    return JarSLManifest.getJarSLManifestVersion();
  }
  /**
   * This method calculates a MD5 fingerprint from a given file
   * @param String filename             the name of the file to be created  
   * @return String                     the MD5 fingerprint
   */
  public String getMD5FingerPrintFromGivenFile(String filename) {
    return JarSL.getMD5FingerPrintFromGivenFile(filename);
  }
  // I/O methods 
  /**
   * This method writes a file from a given InputStream
   * @param InputStream in              the InputStream
   * @param String filename             the name of the file to be created  
   * @param Vector errorTexts           contains the error message as String objects       
   * @return boolean                    true: file created; false: file not created, see errorTexts
   */
  public boolean writeFileFromInputStream(InputStream in, String filename, Vector errorTexts) {
    return JarSL.writeFileFromInputStream(in,filename,errorTexts);
  }
  /**
   * This method returns an OutputStream for a opened file with given filename
   * @param String filename             the name of the file to be created  
   * @param Vector errorTexts           contains the error message as String objects
   * @return OutputStream
   */
  public OutputStream writeFile(String filename, Vector errorTexts) {
    return JarSL.writeFile(filename,errorTexts);
  }
  /**
   * This method opens a given file and returns an InputStream
   * @param String filename             the name of the file to be read
   * @param Vector errorTexts           contains the error message as String objects
   * @return InputStream
   */
  public InputStream readFileAsInputStream(String filename, Vector errorTexts) {
    return JarSL.readFileAsInputStream(filename,errorTexts);
  }
  /**
   * This method returns the length of the corresponding file
   * @param String filename             the name of the file
   * @param Vector errorTexts           contains the error message as String objects
   * @return long                       the file length
   */
  public long getFileLength(String filename, Vector errorTexts) {
    return JarSL.getFileLength(filename,errorTexts);
  }
  /**
   * This method creates the given directory, including necessary subdirectories
   * @param String directory            the name of the directory to be created
   */
  public void createDirectory(String directory) {
    JarSL.createDirectory(directory);
  }
  /**
   * This method checks if the given file exists
   * @param String filename             the name of the file
   * @return boolean                    true: file exists; false: file does not exist
   */
  public boolean checkFileExistence(String filename) {
    return JarSL.checkFileExistence(filename);
  }
  /**
   * This method deletes a given file or directory
   * @param String filedirname          the name of the file or directory to be deleted
   */
  public void deleteFileOrDirectory(String filedirname) {
    JarSL.deleteFileOrDirectory(filedirname);
  }
  /**
   * This method scans a given directory for archives and returns their names in a
   * string array.
   * @param String filename             the name of the directory
   * @return String[]                   array list of found archives or NULL if directory does not contain any archives
   */
  public String[] getArchivesFromDirectory(String directory) {
    return JarSL.getArchivesFromDirectory(directory);
  }
  /**
   * This method scans a given directory for archives and returns their names in a
   * string array.
   * @param String filename             the name of the directory
   * @param boolean recursive           if true the subdirs are scanned, too
   * @return String[]                   array list of found archives or NULL if directory does not contain any archives
   */
  public String[] getArchivesFromDirectory(String directory, boolean recursive) {
    return JarSL.getArchivesFromDirectory(directory,recursive);
  }
  /**
   * This method renames a given file
   * @param String sourcefilename       the name of the file to be renamed
   * @param String targetfilename       the new file name
   * @return boolean                    true or false
   */
  public boolean renameFile(String sourcefilename, String targetfilename) {
    return JarSL.renameFile(sourcefilename,targetfilename);
  }
}
