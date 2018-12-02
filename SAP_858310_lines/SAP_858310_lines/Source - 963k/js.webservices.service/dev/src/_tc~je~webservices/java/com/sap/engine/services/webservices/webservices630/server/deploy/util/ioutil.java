package com.sap.engine.services.webservices.webservices630.server.deploy.util;

import com.sap.tc.logging.Location;
import com.sap.engine.lib.io.hash.HashUtils;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.services.webservices.exceptions.WSLogging;

import java.io.*;
import java.util.*;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2003
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class IOUtil {

  private static final String separator = "/";

  public static boolean mkDirs(String[] dirs) {
    boolean madeDirs = true;

    int dirsSize = dirs.length;
    for(int i = 0; i < dirsSize; i++) {
      String dir = dirs[i];
      File dirFile = new File(dir);
      madeDirs = madeDirs && dirFile.mkdirs();
    }

    return madeDirs;
  }

  public static boolean mkDirs(File[] dirs) {
    boolean madeDirs = true;

    int dirsSize = dirs.length;
    for(int i = 0; i < dirsSize; i++) {
      File dir = dirs[i];
      madeDirs = madeDirs && dir.mkdirs();
    }

    return madeDirs;
  }

  public static boolean isEmptyDir(String dir) throws IOException {
    return isEmptyDir(new File(dir));
  }

  public static boolean isEmptyDir(File dir) throws IOException {
    if (!dir.exists()) {
      return true;
    }

    if (!dir.isDirectory()) {
      throw new IOException("Error occurred, checking if directory is empty: " + dir.getAbsolutePath() + ". The current file is not a directory.");
    }

    File[] files = dir.listFiles();
    return (files == null || files.length == 0);
  }

  public static boolean deleteDir(String dir) throws IOException {
    return deleteDir(new File(dir));
  }

  public static boolean deleteDir(File dir) throws IOException {
    deleteDirSubTree(dir);
    dir.delete();

    return (!dir.exists());
  }

  public static boolean deleteDirSubTree(String dir) throws IOException {
    return deleteDirSubTree(new File(dir));
  }

  public static boolean deleteDirSubTree(File dir) throws IOException {
    if (IOUtil.isEmptyDir(dir)) {
      return true;
    }
    if (!dir.isDirectory()) {
      throw new IOException("The current file is not a directory.");
    }

    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        deleteDir(files[i]);
      }
      if (files[i].isFile()) {
        files[i].delete();
      }
    }

    files = dir.listFiles();

    return (files == null || files.length == 0);
  }

  public static boolean deleteDirs(String[] dirs) throws IOException {
    int dirsSize = dirs.length;
    for(int i = 0; i < dirsSize; i++) {
      String dir = dirs[i];
      if(!deleteDir(dir)) {
        return false;
      }
    }

    return true;
  }

  public static void deleteDirs(File[] dirs) throws IOException {
    int dirsSize = dirs.length;
    for(int i = 0; i < dirsSize; i++) {
      File dir = dirs[i];
      deleteDir(dir);
    }
  }

  public static void deleteDir(String prefixFilter, File dirFile) throws IOException {
    if(dirFile.exists() && dirFile.isDirectory() && dirFile.getName().startsWith(prefixFilter)) {
      deleteDir(dirFile);
    }
  }

  public static void deleteDirs(String prefixFilter, File[] dirs) throws IOException {
    int dirsSize = dirs.length;
    for (int i = 0; i < dirsSize; i++) {
      File dir = dirs[i];
      deleteDir(prefixFilter, dir);
    }
  }

  public static void deleteDirs(String prefixFilter, String parentDir) throws IOException {
    File[] dirs = new File(parentDir).listFiles();

    deleteDirs(prefixFilter, dirs);
  }
 
  public static void copyDir(String sourceDir, String destinationDir) throws IOException {
    copyDir(new File(sourceDir), new File(destinationDir));
  }

  public static void copyDir(File sourceDir, File destinationDir) throws IOException {
    copyDir(sourceDir, destinationDir, true);
  }
  
  public static void copyDir(File sourceDir, File destinationDir, boolean delete) throws IOException {
    if(destinationDir.exists() && delete) {
      deleteDir(destinationDir);
    }
    destinationDir.mkdirs();

    if(sourceDir.exists() && sourceDir.isDirectory()) {
      File[] files = sourceDir.listFiles();
      for (int i = 0; i < files.length; i++) {
        File currentFile = files[i];
        File newDestination = new File(destinationDir, currentFile.getName());
        if (currentFile.isDirectory()) {
          copyDir(currentFile, newDestination);
        } else {
          copyFile(currentFile, newDestination);
        }
      }
    }
  }

  public static void copyFile(String source, String destination) throws IOException {
    copyFile(new File(source), new File(destination));
  }

  public static void copyFile(File source, File destination) throws IOException {
    InputStream in = null;
    OutputStream out = null;

    try {
      in = new FileInputStream(source);
      out = new FileOutputStream(destination);
      copy(in, out);
    } finally {
      IOUtil.closeInputStreams(new InputStream[]{in});
      IOUtil.closeOutputStreams(new OutputStream[]{out});
    }
  }

  public static void copyFile(String source, String destDir, String destRelFile) throws IOException {
    File sourceFile = new File(source);

    File destFile = new File(destDir + separator + destRelFile);
    destFile.getParentFile().mkdirs();
    destFile.createNewFile();
    copyFile(sourceFile, destFile);

  }

  public static void copy(InputStream in, OutputStream out) throws IOException {
    int  bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];
    int count = 0;
    int allCount = 0;
    while ((count = in.read(buffer)) != -1) {
     out.write(buffer, 0 , count);
      allCount += count;
    }

    out.flush();
  }

  public static void createParentDir(String[] fileNames) {
    if (fileNames == null) {
      return;
    }

    for(int i = 0; i < fileNames.length; i++) {
      String fileName = fileNames[i];
      File parentDir = new File(fileName).getParentFile();
      parentDir.mkdirs();
    }
  }

  public static File[] getFiles(String parentDir, String[] relPaths) {
    if(relPaths == null) {
      return new File[0];
    }

    File[] files = new File[relPaths.length];
    for(int i = 0; i < relPaths.length; i++) {
      files[i] = new File(parentDir, relPaths[i]);
    }

    return files;
  }

  public static File[] getFiles(String[] filePaths) {
    if(filePaths == null) {
      return new File[0];
    }

    File[] files = new File[filePaths.length];
    for(int i = 0; i < filePaths.length; i++) {
      files[i] = new File(filePaths[i]);
    }

    return files;
  }

  public static String[] getFileAbsolutePaths(File[] files) {
    int length = files.length;

    String[] filePaths = new String[length];
    for(int i = 0; i < length; i++) {
      filePaths[i] = files[i].getAbsolutePath();
    }

    return filePaths;
  }

  public static String getFilePath(String parentDir, String relPath) {
    return parentDir + "/" + relPath;
  }

  public static String[] getFilePaths(String parentDir, String[] relPaths) {
    if(relPaths == null) {
      return new String[0];
    }

    String[] filePaths = new String[relPaths.length];
    for(int i = 0; i < relPaths.length; i++) {
      filePaths[i] = getFilePath(parentDir, relPaths[i]);
    }

    return filePaths;
  }

  public static String[] getFileNames(File[] files) {
    if(files == null) {
      return new String[0];
    }

    String[] fileNames = new String[files.length];
    for(int i = 0; i < files.length; i++) {
      fileNames[i] = files[i].getName();
    }

    return fileNames;
  }

  public static String getFileNameWithoutExt(File file) {
    return getFileNameWithoutExt(file.getAbsolutePath());
  }

  public static String getFileNameWithoutExt(String fileName) {
    int cutIndex = fileName.lastIndexOf(".");
    if(cutIndex != -1) {
      return fileName.substring(0, cutIndex);
    }
    return fileName;
  }

  public static String getFileExtension(File file) {
    return getFileExtension(file.getName());
  }

  public static String getFileExtension(String fileName) {
    return fileName.substring(fileName.lastIndexOf("."));
  }

  public static String[] convertPackageToDirPaths(String[] packagePaths, String[] fileNames) {
    if(packagePaths == null) {
      return new String[0];
    }

    String[] dirPaths = new String[packagePaths.length];
    for(int i = 0; i < packagePaths.length; i++) {
      dirPaths[i] = convertPackageToDirPath(packagePaths[i], fileNames[i]);
    }

    return dirPaths;
  }

  public static String convertPackageToDirPath(String packagePath, String fileName) {
    return convertPackageToDirPath(packagePath, fileName, '/');
  }

  public static String convertPackageToDirPath(String packagePath, String fileName, char separator) {   
    String filePath = fileName;
    if(packagePath != null && !packagePath.equals("")) {
      filePath = packagePath.replace('.', separator) + separator + fileName;
    }

    return filePath;
  }

  public static String getAsJarName(String name) {
    name = new File(name).getName();

    String jarName = name.substring(0, name.lastIndexOf(".")) + ".jar";                                                                     

    return jarName;
  }

  public static String getRelativeDir(String relativeFilePath) {
    return getParentPath(relativeFilePath, "/");
  }

  public static String getParentPath(String path, String delimiter) {
    if(path.endsWith(delimiter)) {
      path = path.substring(0, path.length() - 1);
    }

    int cutIndex = path.lastIndexOf(delimiter);                                                                                                    
    if(cutIndex < 0) {
      cutIndex = 0;
    }

    return path.substring(0, cutIndex);
  }

  public static String getName(String path, String delimiter) {
    if(path.endsWith(delimiter)) {
      path = path.substring(0, path.length() - 1);
    }

    int cutIndex = path.lastIndexOf(delimiter) + 1;
    if(cutIndex < 0) {
      cutIndex = 0;
    }

    return path.substring(cutIndex);
  }


  public static void closeInputStreams(InputStream[] ins) {
    if (ins == null) {
      return;
    }

    for (int i = 0; i < ins.length; i++) {
      InputStream in = ins[i];
      try {
        if (in != null) {
          in.close();
        }
      } catch(IOException e) {
        // $JL-EXC$ 
      }
    }
  }

  public static void closeInputStreams(InputStream[] ins, String[] msgs, Location location) {
    if (ins == null) {
      return;
    }

    for (int i = 0; i < ins.length; i++) {
      InputStream in = ins[i];
      try {
        if (in != null) {
          in.close();
        }
      } catch(IOException e) {
        String msg = msgs[i];
        if(msg != null) {
          location.catching(msg, e) ;
        }
      }
    }
  }

  public static void closeOutputStreams(OutputStream[] outs) {
    if (outs == null) {
      return;
    }

    for(int i = 0; i < outs.length; i++) {
      OutputStream out = outs[i];
      try {
        if(out != null) {
          out.close();
        }
      } catch(IOException e) {
        // $JL-EXC$ 
      }
    }
  }

  public static void closeOutputStreams(OutputStream[] outs, String[] msgs, Location location) {
    if (outs == null) {
      return;
    }

    for (int i = 0; i < outs.length; i++) {
      OutputStream out = outs[i];
      try {
        if (out != null) {
          out.close();
        }
      } catch(IOException e) {
        String msg = msgs[i];
        if(msg != null) {
          location.catching(msg, e);
        }
      }
    }
  }

  public static File[] collectFiles(File[] files, Set filterList) {
    if(files == null) {
      return new File[0];
    }

    if(filterList == null) {
      filterList = new HashSet();
    }

    Vector filteredFiles = new Vector();
    for(int i = 0; i < files.length; i++) {
      File file = files[i];
      if(filterList.contains(file.getName())) {
        filteredFiles.add(file);
      }
    }

    File[] filteredFilesArr = new File[filteredFiles.size()];
    filteredFiles.copyInto(filteredFilesArr);

    return filteredFilesArr;
  }

  public static Hashtable getModuleCrcTable(File[] moduleArchives) throws IOException {
    if(moduleArchives == null) {
      return new Hashtable();
    }

    Hashtable moduleCrcTable = new Hashtable();
    for(int i = 0; i < moduleArchives.length; i++) {
      File moduleArchive = moduleArchives[i];
      byte[] moduleCrc = HashUtils.generateFileHash(moduleArchive);
      moduleCrcTable.put(moduleArchive.getName(), moduleCrc);
    }

    return moduleCrcTable;
  }

  public static File[] collectFiles(Hashtable filesTable) {
    if(filesTable == null) {
      return new File[0];
    }

    File[] elements = new File[filesTable.size()];
    Enumeration enum1 = filesTable.keys();
    int i = 0;
    while(enum1.hasMoreElements()) {
      elements[i++] = (File)filesTable.get((String)enum1.nextElement());
    }

    return elements;
  }

  public static File[] filterFiles(File[] files, Set fileNamesExcludeList) {
    if(files == null) {
      return new File[0];
    }

    Vector filesFiltered = new Vector();
    for(int i = 0; i < files.length; i++) {
      File file = files[i];
      if(!fileNamesExcludeList.contains(files[i].getName())) {
        filesFiltered.add(file);
      }
    }

    File[] filesFilteredArr = new File[filesFiltered.size()];
    filesFiltered.copyInto(filesFilteredArr);

    return filesFilteredArr;
  }

}
