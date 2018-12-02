package com.sap.sl.util.jarsl.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * @author d030435
 */

public interface JarSLIF extends JarSLManifestIF {
  /**
   * returns the name of the archive
   */
  public abstract File getArchiveName();
  /**
   * calculates the md5 fingerprint of the actual archive
   */
  public abstract String calcFingerprintFromArchive();
  /**
   * extracts a single file form the archive and returns it as ByteArrayInputStrean
   */
  public abstract ByteArrayInputStream extractSingleFileAsByteArray(String sfname);
  /**
   * This method extracts a single file from the archive and returns it as InputStrean.
   * The returned stream must be closed by the caller.
   */
  public abstract InputStream extractSingleFileAsStream(String sfname) throws JarSLException;
  /**
   * extracts a single file form the archive and writes it to targetname or
   * (if targetname == null) to the correct position relative to root
   */
  public abstract boolean extractSingleFile(String sfname, Vector errorTexts);
  public abstract boolean extractSingleFile(String sfname, String targetname, Vector errorTexts);
  /**
   * extracts single files form the archive and writes it to the given targetnames or
   * (if targetnames == null) to the correct position relative to root
   */
  public abstract boolean extractSingleFiles(String[] sfnames, Vector errorTexts);
  public abstract boolean extractSingleFiles(String[] sfnames, String[] targetnames, Vector errorTexts);
  /**
   * makes a simple copy of the actual archive
   */
  public abstract boolean copyArchive(String targetname);
  /**
   * This method opens the archive and extracts it relative to the given rootdirectory.
   * Also the included files are added to the filelist and the two manifests are read.
   * In case of errors, false is returned.
   */
  public abstract boolean extract();
  /**
   * This method opens the archive and extracts it relative to the given rootdirectory.
   * Also the included files are added to the filelist and the two manifests are read.
   * In case of errors, messages are added to the vector errTexts of String's,
   * and false is returned. If deleterootdirectory is true then the whole rootdir is deleted
   * at first.
   */
  public abstract boolean extract(Vector errTexts);
  public abstract boolean extract(Vector errTexts, boolean deleterootdirectory);
  /**
   * scans the directory structur beginning with rootdir and adds the located files to the
   * filelist, also the two manifests are read (if they are found at the correct location)
   */
  public abstract boolean addAllFiles();
  /**
   * This methods write the files contained in filelist to the archive. Also the two
   * manifests are created from the two manifest objects and added to the new archive.
   */
  public abstract boolean create(Vector errorTexts);
  public abstract boolean create(boolean compress, Vector errorTexts);
  public abstract boolean create(boolean compress, boolean writemftodisk, boolean writesapmanifest, Vector errorTexts);
  public abstract boolean create(String filename, boolean compress, boolean writemftodisk, boolean writesapmanifest, Vector errorTexts);
  /**
   * This methods write the files contained in filelist to the given output stream by using
   * the archive format. Also the two manifests are created from the two manifest objects and
   * added to the stream. The given stream must be closed by the caller.
   */
  public abstract boolean createAsStream(OutputStream ostream);
  public abstract boolean createAsStream(OutputStream ostream, boolean compress, Vector errorTexts);
  public abstract boolean createAsStream(OutputStream ostream, boolean compress, boolean writesapmanifest, Vector errorTexts);
  /**
   * This methods write the files contained in filelist to the archive. Also the two
   * manifests are created from the two manifest objects and added to the new archive.
   * In contrast to the create method it is possible to add additional files via stream to the
   * resulting archive (see JarSLFileStreamHandler). As soon the JarSLFileStream handler is closed
   * the resulting archive is built. The resulting archive is always compressed.
   */
  public abstract JarSLFileStreamHandlerIF createWithAdditionalFileStreams() throws JarSLException, IOException;
  public abstract JarSLFileStreamHandlerIF createWithAdditionalFileStreams(boolean writemftodisk, boolean writesapmanifest)throws JarSLException, IOException;
  public abstract JarSLFileStreamHandlerIF createWithAdditionalFileStreams(String filename, boolean writemftodisk, boolean writesapmanifest) throws JarSLException, IOException;
  /**
   * This methods write the files contained in filelist to the given output stream by using
   * the archive format. Also the two manifests are created from the two manifest objects and
   * added to stream. The given stream must be closed by the caller.
   * In contrast to the create method it is possible to add additional files via stream to the
   * resulting archive (see JarSLFileStreamHandler). As soon as the JarSLFileStream handler is 
   * closed the resulting archive is built. The resulting archive is always compressed.
   */
  public abstract JarSLFileStreamHandlerIF createAsStreamWithAdditionalFileStreams(OutputStream ostream) throws JarSLException, IOException;
  public abstract JarSLFileStreamHandlerIF createAsStreamWithAdditionalFileStreams(OutputStream ostream, boolean writesapmanifest) throws JarSLException, IOException;
  /**
   * removes a file from the filelist
   */
  public abstract boolean removeFile(String filename);
  public abstract boolean removeFile(String lroot, String filename);
  /**
   * removes a directory from the filelist
   */
  public abstract boolean removeDir(String dirname);
  public abstract boolean removeDir(String lroot, String dirname);
  /**
   * This method opens the entry named with entryname within the archive. The file is
   * read with the methods get...Data() and closed with the method closeSingleArchiveFile().
   */
  public abstract boolean openSingleArchiveFile(String entryname, Vector errorTexts);
  public abstract boolean closeSingleArchiveFile(Vector errorTexts);
  public abstract int getIntData() throws IOException;
  public abstract boolean getBooleanData() throws IOException;
  public abstract long getLongData() throws IOException;
  public abstract double getDoubleData() throws IOException;
  public abstract float getFloatData() throws IOException;
  public abstract short getShortData() throws IOException;
  public abstract char getCharData() throws IOException;
  public abstract byte getByteData() throws IOException;
  public abstract int getData(byte[] data, int off, int len) throws IOException;
  public abstract int getData(byte[] data) throws IOException;
  public abstract Object getObjectData() throws ClassNotFoundException, IOException;
  /**
   * This method creates the file named with filename. The file is
   * filled with the methods put...Data() and closed with the method closeFile().
   */
  public abstract boolean createFile(String filename, String archname, Vector errorTexts);
  public abstract boolean putIntData(int in);
  public abstract boolean putBooleanData(boolean in);
  public abstract boolean putLongData(long in);
  public abstract boolean putDoubleData(double in);
  public abstract boolean putFloatData(float in);
  public abstract boolean putShortData(short in);
  public abstract boolean putCharsData(String in);
  public abstract boolean putCharData(int in);
  public abstract boolean putByteData(int in);
  public abstract boolean putBytesData(String in);
  public abstract boolean putData(byte[] data, int off, int len);
  public abstract boolean putObjectData(Object in);
  /**
   * The new created file is closed and added to the filelist. When the first argument of the
   * method call is true then the file will be deleted after it was added to the archive
   */
  public abstract boolean closeFile(boolean deleteit, Vector errorTexts);
  /**
   * adds a file to the filelist. If archname is not equal null, it represents the name of the
   * added file in the archive, if it is null then the archive name is equal to the filename.
   */
  public abstract boolean addFile(String filename, int place, Vector errorTexts);
  public abstract boolean addFile(String filename, Vector errorTexts);
  public abstract boolean addFile(String filename, Vector errorTexts, boolean deleteit);
  public abstract boolean addFile(String lroot, String filename, Vector errorTexts);
  public abstract boolean addFile(String lroot, String filename, String archname, Vector errorTexts);
  public abstract boolean addFile(String lroot, String filename, String archname, Vector errorTexts, boolean deleteit);
  public abstract boolean addFile(String lroot, String filename, String archname, int place, Vector errorTexts, boolean deleteit);
  /**
   * adds a directory to the filelist
   */
  public abstract boolean addDir(String dirname, int place, Vector errorTexts);
  public abstract boolean addDir(String dirname, Vector errorTexts);
  public abstract boolean addDir(String lroot, String dirname, Vector errorTexts);
  public abstract boolean addDir(String lroot, String dirname, int place, Vector errorTexts);
  public abstract boolean createFingerPrintAttribute();
  /**
   * This method checks if the current archive is uptodate
   */
  public abstract boolean isCurrentArchiveUpToDate(long mintime);
  /**
   * This method returns the files contained in the corresponding archive
   */
  public abstract String[] getJarFileList();
  /**
    * writes the attributes of the JarSLManifest object into the manifest
    */
  public abstract boolean writeAttributesFromJarSLManifest(JarSLManifestIF jarslmf);
}