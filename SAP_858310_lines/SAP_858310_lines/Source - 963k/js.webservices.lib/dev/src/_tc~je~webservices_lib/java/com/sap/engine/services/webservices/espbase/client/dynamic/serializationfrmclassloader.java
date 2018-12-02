/*
 * Created on 2005-7-26
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.sap.engine.lib.xml.parser.URLLoaderBase;

/**
 * @author ivan-m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SerializationFRMClassLoader extends ClassLoader { 

  private String tempDir;
  
  public SerializationFRMClassLoader(ClassLoader parent, String tempDir) {
    super(parent);
    this.tempDir = tempDir;
  }
  
  protected Class findClass(String name) throws ClassNotFoundException {
    String classFileName = createClassName(name);
    byte[] classBytes = null;
    try {
      classBytes = extractClassBytes(classFileName);
    } catch(Exception exc) {
      throw new ClassNotFoundException(exc.getMessage());
    }
    return(defineClass(classBytes, 0, classBytes.length));
  }
  
  public URL getResource(String name) {
    URL resourceURL = null;
    try {
      if(name.startsWith("/")) {
        name = name.substring(1);
      }
      resourceURL = URLLoaderBase.fileOrURLToURL(null, tempDir + File.separator + name);
    } catch(IOException ioExc) {
      return(null);
    }
    return(resourceURL);
  }

  private String createClassName(String className) {
    return(tempDir + File.separator + className.replace('.', File.separatorChar) + ".class");
  }
  
  private byte[] extractClassBytes(String classFilePath) throws Exception {
    FileInputStream classFileInput = null;
    try {
      File classFile = new File(classFilePath);
      classFileInput = new FileInputStream(classFile);
      byte[] classBytes = new byte[(int)(classFile.length())];
      int extractedClassBytesCount = 0;
      while((extractedClassBytesCount += classFileInput.read(classBytes, extractedClassBytesCount, classBytes.length)) < classBytes.length);
      return(classBytes);
    } finally {
      if(classFileInput != null) {
        classFileInput.close();
      }
    }
  }
}