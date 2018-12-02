/*
 * Copyright (c) 2000 by InQMy Software AG.,
 * url: http://www.inqmy.com
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of InQMy Software AG.. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with InQMy.
 */

package com.sap.engine.services.webservices.webservices630.server.deploy.util.jar;

import com.sap.engine.lib.jar.JarUtils;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.WSUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipException;
import java.util.zip.ZipEntry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * Title: JarUtil 
 * Description: Adds new methods for jar and ordinary file processing
 * Copyright: Copyright (c) 2000
 * Company: InQMy
 * @author Dimitrina Stoyanova
 * @version 1.0
 */

public class JarUtil extends com.sap.engine.lib.jar.JarExtractor {

  private static final char SEPARATOR = '/';

  public JarUtil() {
  
  }

  public JarUtil(String jar) {
    super(jar);
  }
 
  public JarUtil(String jar, String outputDir) {
    super(jar, outputDir);
  }

  public static boolean hasEntry(String jar, String entryName) throws IOException {
    File jarFile = new File(jar);
    return hasEntry(jarFile, entryName);
  }

  public static boolean hasEntry(File jar, String entryName) throws IOException {
    JarFile jarFile = null;
    boolean result = false;
    try {
      jarFile = new JarFile(jar);
      result = hasEntry(jarFile, entryName);
    } finally {
      try {
        if(jarFile != null) {               
          jarFile.close();
        }
      } catch(IOException e) {
        // $JL-EXC$
      }
    }

    return result;
  }

  public static boolean hasEntry(JarFile jarFile, String entryName) {
    if (jarFile.getEntry(entryName) != null) return true;
    else return false;
  }
  
  public void extractFileWithPrefix(JarFile jarFile, String prefix, String destDir) throws IOException {
    Enumeration enum1 = jarFile.entries();
    while(enum1.hasMoreElements()) {
      JarEntry jarEntry = (JarEntry)enum1.nextElement(); 
      if(!jarEntry.isDirectory() && jarEntry.getName().startsWith(prefix)) {
        extractFile(jarFile, jarEntry.getName(), destDir);
      }
    }
  } 

  public void extractDir(JarFile jarFile, String entryName, String destDir) throws IOException {
    Enumeration entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry currentEntry = (JarEntry)entries.nextElement();
      if (currentEntry.getName().startsWith(entryName) && !currentEntry.isDirectory()) {
        extractFile(jarFile, currentEntry.getName(), destDir);
      }
    }
  }

  public void setJarFile(String s) {
    super.setJarFile(s);
  }

  public void extractFiles(String jarFileName, String[] prefixFilters, String[] prefixExcludeList, String[] suffixFilters, String[] suffixExcludeList, String workingDir, boolean cutPrefix) throws IOException {
    extractFiles(new File(jarFileName), prefixFilters, prefixExcludeList, suffixFilters, suffixExcludeList, workingDir, cutPrefix);
  }

  public void extractFiles(File jar, String[] prefixFilters, String[] prefixExcludeList, String[] suffixFilters, String[] suffixExcludeList, String workingDir, boolean cutPrefix) throws IOException {
    JarFile jarFile = null;
    try {
      jarFile = new JarFile(jar);
      extractFiles(jarFile, prefixFilters, prefixExcludeList, suffixFilters, suffixExcludeList, workingDir, cutPrefix);
    } finally {
      try {
        if(jarFile != null) {
          jarFile.close();
        }
      } catch(IOException e) {
        // $JL-EXC$
      }
    }
  }

  public void extractFiles(JarFile jarFile, String[] prefixFilters,  String[] prefixExcludeList, String[] suffixFilters, String[] suffixExcludeList, String workingDir, boolean cutPrefix) throws IOException {
    Enumeration enum1 = jarFile.entries();

    while(enum1.hasMoreElements()) {
      boolean isFile = true;
      boolean startsWith = true;
      String firstPrefixThatStartsWith = "";
      boolean notStartsWith = true;
      boolean endsWith = true;
      boolean notEndsWith = true;

      JarEntry jarEntry = (JarEntry)enum1.nextElement();
      String jarEntryName = jarEntry.getName();
      isFile = !jarEntry.isDirectory();
      if(prefixFilters != null && prefixFilters.length > 0) {
        firstPrefixThatStartsWith = WSUtil.startsWithPrefix(jarEntryName, prefixFilters);
        if(firstPrefixThatStartsWith == null) {
          startsWith = false;
        }
      }

      if(prefixExcludeList != null && prefixExcludeList.length > 0) {
        notStartsWith = !WSUtil.startsWith(jarEntryName, prefixExcludeList);
      }

      if(suffixFilters != null && suffixFilters.length > 0) {
        endsWith = WSUtil.endsWith(jarEntryName, suffixFilters);
      }

      if (suffixExcludeList != null && suffixExcludeList.length > 0) {
        notEndsWith = !WSUtil.endsWith(jarEntryName, suffixExcludeList);
      }

      if(isFile && startsWith && notStartsWith && endsWith && notEndsWith) {
        if(cutPrefix) {
          String relativeFileName = jarEntryName.substring(jarEntryName.indexOf(firstPrefixThatStartsWith) + firstPrefixThatStartsWith.length());
          extractFile(jarFile, jarEntryName, workingDir, relativeFileName);
        } else {
          extractFile(jarFile, jarEntryName, workingDir);
        }
      }
    }
  }

  public void extractAndPackageFiles(String workingDir, String jarFileName, String[] prefixFilters, String[] prefixExcludeList, String[] suffixFilters, String[] suffixExcludeList, String resultFileName) throws IOException {
    extractAndPackageFiles(workingDir, new File(jarFileName ), prefixFilters, prefixExcludeList,  suffixFilters, suffixExcludeList, resultFileName);
  }

  public void extractAndPackageFiles(String workingDir, File jar, String[] prefixFilters, String[] prefixExcludeList, String[] suffixFilters, String[] suffixExcludeList, String resultFileName) throws IOException {
    JarFile jarFile = null;
    try {
      jarFile = new JarFile(jar);
      extractAndPackageFiles(workingDir, jarFile, prefixFilters, prefixExcludeList, suffixFilters, suffixExcludeList, resultFileName);
    } finally {
      try {
        if (jarFile != null) {
          jarFile.close();
        }
      } catch(IOException e) {
        // $JL-EXC$
      }
    }
  }

  public void extractAndPackageFiles(String workingDir, JarFile jarFile, String[] prefixFilters,  String[] prefixExcludeList, String[] suffixFilters, String[] suffixExcludeList, String resultFileName) throws IOException {
    extractFiles(jarFile, prefixFilters, prefixExcludeList, suffixFilters, suffixExcludeList, workingDir, false);

    JarUtils jarUtils = new JarUtils();
    jarUtils.makeJarFromDir(resultFileName, workingDir);
  }
  
  public static void copyEntries(JarFile jarFile, String[] prefixFilters,  String[] prefixExcludeList, String[] suffixFilters, String[] suffixExcludeList, boolean cutPrefix, JarOutputStream jarOutputStream) throws IOException {
    copyEntries(jarFile, prefixFilters, prefixExcludeList, suffixFilters, suffixExcludeList, cutPrefix, null, jarOutputStream); 
  }
  
  public static void copyEntries(JarFile jarFile, String[] prefixFilters,  String[] prefixExcludeList, String[] suffixFilters, String[] suffixExcludeList, boolean cutPrefix, String prefix, JarOutputStream jarOutputStream) throws IOException {
    Enumeration enum1 = jarFile.entries();

    while(enum1.hasMoreElements()) {
      boolean isFile = true;
      boolean startsWith = true;
      String firstPrefixThatStartsWith = "";
      boolean notStartsWith = true;
      boolean endsWith = true;
      boolean notEndsWith = true;

      JarEntry jarEntry = (JarEntry)enum1.nextElement();
      String jarEntryName = jarEntry.getName();
      isFile = !jarEntry.isDirectory();

      if(prefixFilters != null && prefixFilters.length > 0) {
        firstPrefixThatStartsWith = WSUtil.startsWithPrefix(jarEntryName, prefixFilters);
        if(firstPrefixThatStartsWith == null) {
          startsWith = false;
        }
      }

      if(prefixExcludeList != null && prefixExcludeList.length > 0) {
        notStartsWith = !WSUtil.startsWith(jarEntryName, prefixExcludeList);
      }

      if(suffixFilters != null && suffixFilters.length > 0) {
        endsWith = WSUtil.endsWith(jarEntryName, suffixFilters);
      }

      if (suffixExcludeList != null && suffixExcludeList.length > 0) {
        notEndsWith = !WSUtil.endsWith(jarEntryName, suffixExcludeList);
      }

      if(isFile && startsWith && notStartsWith && endsWith && notEndsWith) {
        InputStream in = null;
        InputStream check = null;
        try {
         String newEntryName = jarEntryName;
          if(cutPrefix) {
            String jarEntryNameWithoutExt = IOUtil.getFileNameWithoutExt(jarEntryName);
            if(firstPrefixThatStartsWith.length() > 0 && firstPrefixThatStartsWith.length() < jarEntryNameWithoutExt.length()) {
              newEntryName = jarEntryName.substring(jarEntryName.indexOf(firstPrefixThatStartsWith) + firstPrefixThatStartsWith.length());
              if(newEntryName.startsWith("/") || newEntryName.startsWith("\\")) {
                newEntryName = newEntryName.substring(1);
              }
              if(prefix != null && !prefix.equals("")) {
                newEntryName = prefix + "/" + newEntryName;
              }
            }
          }
          in = jarFile.getInputStream(jarEntry);
          check = jarFile.getInputStream(jarEntry);
          makeEntry(jarOutputStream, newEntryName, in, check);
        } finally {
          IOUtil.closeInputStreams(new InputStream[]{in, check});
        }
      }
    }
  }

  public static void makeJarFile(String file, String[] prefixFilters,  String[] prefixExcludeList, String[] suffixFilters, String[] suffixExcludeList, String resultFileName) throws IOException {
    makeJarFile(new File(file), prefixFilters, prefixExcludeList, suffixFilters, suffixExcludeList, resultFileName);
  }

  public static void makeJarFile(File file, String[] prefixFilters,  String[] prefixExcludeList, String[] suffixFilters, String[] suffixExcludeList, String resultFileName) throws IOException {
    JarFile jarFile = null;
    try {
      jarFile = new JarFile(file);
      makeJarFile(jarFile, prefixFilters, prefixExcludeList, suffixFilters, suffixExcludeList, resultFileName);
    } finally {
      try {
        if (jarFile != null) {
          jarFile.close();
        }
      } catch(IOException e) {
        // $JL-EXC$
      }
    }
  }

  public static void makeJarFile(JarFile jarFile, String[] prefixFilters,  String[] prefixExcludeList, String[] suffixFilters, String[] suffixExcludeList, String resultFileName) throws IOException {
    JarOutputStream jarOut = null;
    FileOutputStream fileOut = null; 
    try {
      fileOut = new FileOutputStream(resultFileName); 
      jarOut = new JarOutputStream(new FileOutputStream(resultFileName));      
      copyEntries(jarFile, prefixFilters, prefixExcludeList, suffixFilters, suffixExcludeList, true, jarOut);
    } finally{
      try {      
        if(fileOut != null) {
          fileOut.close();
        }   
      } catch(IOException e) {
        // $JL-EXC$
      }
      try {      
        if(jarOut != null) {
          jarOut.close();
        }   
      } catch(IOException e) {
        // $JL-EXC$
      }      
    }
  }
  
  public static void addFile(JarOutputStream jarOut, String entryName, File file) throws IOException {
    InputStream in = null; 
    try {
      in = new FileInputStream(file); 
      jarOut.putNextEntry(new JarEntry(entryName)); 
      IOUtil.copy(in, jarOut); 
    } finally {
      try {
        jarOut.closeEntry();  
      } catch(Exception e) {
        // $JL-EXC$		
      }	 	
      try {
        if(in != null) {
          in.close(); 	
        }
      } catch(Exception e) {
        // $JL-EXC$		
      }	
    }   
  }

  public static JarEntry makeEntry(ZipOutputStream zip, String entryName, InputStream in, InputStream check) throws IOException {
    if (in == null) {
      return null;
    }

    JarEntry entry = null;
    try {
      int count = 0;
      int size = 0;
      byte[] buff = new byte[16 * 1024];
      CRC32 crc = new CRC32();
      entry = new JarEntry(entryName.replace('\\', '/'));
      try {
        while ((count = check.read(buff)) != -1) {
          size += count;
          crc.update(buff, 0, count);
        }

        entry.setMethod(JarEntry.STORED);
        entry.setCrc(crc.getValue());
        entry.setSize(size);
        entry.setCompressedSize(size);
        zip.putNextEntry(entry);

        while ((count = in.read(buff)) != -1) {
          zip.write(buff, 0, count);
        }
      } catch (EOFException ex) {
        throw new IOException(ex.getMessage());
      }
    } catch (ZipException zipEx) {
      throw new IOException(zipEx.getMessage());
    } finally {
      zip.closeEntry();
    }
    return entry;
  }
  
  public static String[] getEntryNames(String prefix, JarFile jarFile) {
	Enumeration<JarEntry> enumer = jarFile.entries();
 
    ArrayList<String> entries = new ArrayList<String>(); 
    ZipEntry entry;
    String entryName;
    while(enumer.hasMoreElements()) {
      entry = (ZipEntry)enumer.nextElement();
      entryName = entry.getName();
      if(entryName.startsWith(prefix)) {
        entries.add(entryName);
      }
    }
    
    return entries.toArray(new String[entries.size()]);
  }
  
}