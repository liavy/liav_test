package com.sap.sl.util.jarsl.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import com.sap.sl.util.loader.Loader;

/**
 * @author d030435
 */

public abstract class JarSLFactory {
  private static JarSLFactory INSTANCE = null;
  private final static String FACTORY_IMPL_CLASS = "com.sap.sl.util.jarsl.impl.JarSLFactoryImpl";
  /**
   * Gets an instance of <code> JarSLFactory. </code>
   * <p>
   * If you want a special class loader to be
   * used for the loading of the class use
   * {@link com.sap.sl.util.loader.Loader#setClassloader}
   *
   *@return A <code> JarSLFactory </code> instance
   *@see  java.lang.ClassLoader
   */
  public static JarSLFactory getInstance() {
    if (null == JarSLFactory.INSTANCE) {
      JarSLFactory.INSTANCE 
        = (JarSLFactory)Loader.getInstanceOfClass(FACTORY_IMPL_CLASS);
    }
    return INSTANCE;
  }
  /**
   * Gets an instance of <code>JarSLManifest</code>
   * @param InputStreammanifest         InputStream of the manifest file
   * @param InputStream sapmanifest     InputStream of the sapmanifest file
   */
  public abstract JarSLManifestIF createJarSLManifest(InputStream manifest, InputStream sapmanifest) throws JarSLManifestException;
  /**
   * Gets an instance of JarSLFileInfo
   */           
  public abstract JarSLFileInfoIF createJarSLFileInfo(String filename); 
  /**
   * Gets an instance of JarSL
   */           
  public abstract JarSLIF createJarSL(String jarfilename, String rootdir); 
  /**
   * This method returns the current jarSL version      
   * @return String                     the jarSL version
   */
  public abstract String getJarSLVersion();
  /**
   * This method returns the current JarSLManifest version      
   * @return String                     the JarSLManifest version
   */
  public abstract String getJarSLManifestVersion();
  /**
   * This method calculates a MD5 fingerprint from a given file
   * @param String filename             the name of the file to be created  
   * @return String                     the MD5 fingerprint
   */
  public abstract String getMD5FingerPrintFromGivenFile(String filename);
  // I/O methods 
  /**
   * This method writes a file from a given InputStream
   * @param InputStream in              the InputStream
   * @param String filename             the name of the file to be created  
   * @param Vector errorTexts           contains the error message as String objects       
   * @return boolean                    true: file created; false: file not created, see errorTexts
   */
  public abstract boolean writeFileFromInputStream(InputStream in, String filename, Vector errorTexts);
  /**
   * This method returns an OutputStream for a opened file with given filename
   * @param String filename             the name of the file to be created  
   * @param Vector errorTexts           contains the error message as String objects
   * @return OutputStream
   */
  public abstract OutputStream writeFile(String filename, Vector errorTexts);
  /**
   * This method opens a given file and returns an InputStream
   * @param String filename             the name of the file to be read
   * @param Vector errorTexts           contains the error message as String objects
   * @return InputStream
   */
  public abstract InputStream readFileAsInputStream(String filename, Vector errorTexts);
  /**
   * This method returns the length of the corresponding file
   * @param String filename             the name of the file
   * @param Vector errorTexts           contains the error message as String objects
   * @return long                       the file length
   */
  public abstract long getFileLength(String filename, Vector errorTexts);
  /**
   * This method creates the given directory, including necessary subdirectories
   * @param String directory            the name of the directory to be created
   */
  public abstract void createDirectory(String directory);
  /**
   * This method checks if the given file exists
   * @param String filename             the name of the file
   * @return boolean                    true: file exists; false: file does not exist
   */
  public abstract boolean checkFileExistence(String filename);
  /**
   * This method deletes a given file or directory
   * @param String filedirname          the name of the file or directory to be deleted
   */
  public abstract void deleteFileOrDirectory(String filedirname);
  /**
   * This method scans a given directory for archives and returns their names in a
   * string array.
   * @param String filename             the name of the directory
   * @return String[]                   array list of found archives or NULL if directory does not contain any archives
   */
  public abstract String[] getArchivesFromDirectory(String directory);
  /**
   * This method scans a given directory for archives and returns their names in a
   * string array.
   * @param String filename             the name of the directory
   * @param boolean recursive           if true the subdirs are scaned, too
   * @return String[]                   array list of found archives or NULL if directory does not contain any archives
   */
  public abstract String[] getArchivesFromDirectory(String directory, boolean recursive);
  /**
   * This method renames a given file
   * @param String sourcefilename       the name of the file to be renamed
   * @param String targetfilename       the new file name
   * @return boolean                    true or false
   */
  public abstract boolean renameFile(String sourcefilename, String targetfilename);
}
