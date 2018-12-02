/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.webservices.jaxws.j2w;

import java.io.File;
import java.io.FileInputStream;
import java.util.Set;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 * Used to load compiled beans 
 */
public class JaxWsBeanClassLoader extends ClassLoader {

  private String classDir;
  private Set<String> beanClasses;
  
  public JaxWsBeanClassLoader(ClassLoader parent, String classDir, Set<String> beanClasses) {
    super(parent);
    if(classDir == null){
      throw new IllegalArgumentException("null class directory!");
    }      
    this.classDir = classDir;
    this.beanClasses = beanClasses;
  }
  
  protected Class<?> findClass(String name) throws ClassNotFoundException{
        
    try{

      String fullName = classDir + File.separator + name.replace('.', File.separatorChar) + ".class";
      byte[] classData = loadClassData(fullName);
      Class cl = defineClass(name, classData, 0, classData.length); 
//      System.out.println("!!!!!!!!!!!! JaxWsBeanClassLoader.findClass(): loaded class '" + cl.getName() + "'");
      return cl;
    }
    catch (Exception e){
//      System.out.println("FAILD: JaxWsBeanClassLoader.findClass(): loaded class '" + name + "'");
      throw new ClassNotFoundException("Error loading bean class " + name + " from " + classDir, e);
    }
            
  }
  
  private byte[] loadClassData(String classFilePath) throws Exception{
    
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

  @Override
  protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class c = findLoadedClass(name);
    if (c == null) {
      try {
        if (beanClasses.contains(name)) {
          c = findClass(name);
        }
      } catch (NoClassDefFoundError cnfE) {
        //$JL-EXC$
      }
      if (c == null) {
        c = super.loadClass(name, resolve);
      }
    }
    if (resolve) {
        resolveClass(c);
    }
    return c;
  }
  
}
