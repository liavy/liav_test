/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaGenerator;
import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * 
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class GeneratorEnvironment {

  // The output root directory.
  public File outputRootDir;
  // The output package.
  public String outputPackage;
  // Name convertor instance for converting XML names to Java names.
  public NameConvertor convertor;
  // Gives access to wsdl that is currently processed.
  public WSDLDefinitions definitions;
  // Schema generator access
  public SchemaToJavaGenerator schema;
  // List of all generated files.
  public ArrayList outputFiles;
  // A way handler to return exception without declaring and throwing it
  public Throwable generateException;
  // Link to interface generator
  public InterfaceGenerator iGenerator;
  // contains the current method return type
  public String returnType;
  // holder of interface - operation mapping
  private Hashtable interfaces;
  // holder of portType - interfaces
  private Hashtable portTypes;
  // holder of java to schema mapping.
  public Hashtable javaToSchemaMapping;


  public GeneratorEnvironment() {
    convertor = new NameConvertor();
    outputFiles = new ArrayList();
    interfaces = new Hashtable();
    portTypes = new Hashtable();
  }

  public void clear() {
    convertor.clear();
    outputFiles.clear();
    interfaces.clear();
    portTypes.clear();
  }

  /**
   * Adds portType implementation.
   * @param portType
   * @param interfaceName
   */
  public void registerInterfaceName(QName portType, String interfaceName) {
    portTypes.put(portType, interfaceName);
  }

  /**
   * Adds interface method.
   * @param interfaceName
   * @param operation
   */
  public void registerInterfaceMethod(String interfaceName, OperationInterface operation) {
    ArrayList operations = null;
    operations = (ArrayList) interfaces.get(interfaceName);
    if (operations == null) {
      operations = new ArrayList();
      interfaces.put(interfaceName,operations);
    }
    operations.add(operation);
  }

  /**
   * Returns defined operations for this interface.
   * @param interfaceName
   * @return
   */
  public OperationInterface[] getInterfaceMethods(String interfaceName) {
    ArrayList operations = (ArrayList) interfaces.get(interfaceName);
    if (operations == null) {
      return null;
    }
    OperationInterface[] result = new OperationInterface[operations.size()];
    for (int i=0; i<operations.size(); i++) {
      result[i] = (OperationInterface) operations.get(i);
    }
    return result;
  }

  /**
   * Returns interface operations.
   * @param interfaceName
   * @return
   */
  public ArrayList getInterfaceMethodsAL(String interfaceName) {
    return (ArrayList) interfaces.get(interfaceName);
  }

  /**
   * Returns interface name for this portType.
   * @param portType
   * @return
   */
  public String getPortTypeInterface(QName portType) {
    return (String) portTypes.get(portType);
  }

  /**
   * Returns array uf used portTypes.
   * @return
   */
  public QName[] getPortTypes() {
    return (QName[]) portTypes.keySet().toArray(new QName[0]);
  }

  /**
   * Returns list of all generated interfaces.
   * @return
   */
  public String[] getInterfaces() {
    return (String[]) interfaces.keySet().toArray(new String[0]);
  }

  /**
   * Prints interface tree on std out.
   */
  public void printInterfaces() {
    String[] interfaces = getInterfaces();
    for (int i=0; i<interfaces.length; i++) {
      System.out.println(" \\ Interface Name :"+interfaces[i]);//$JL-SYS_OUT_ERR$
      OperationInterface[] operations = getInterfaceMethods(interfaces[i]);
      for (int j=0; j<operations.length; j++) {
        System.out.print("  +-- "+operations[j].operationJavaName);//$JL-SYS_OUT_ERR$
        System.out.print("(");//$JL-SYS_OUT_ERR$
        if (operations[j].inputParams!=null) {
          for (int k=0; k<operations[j].inputParams.length; k++) {
            if (k!=0) {
              System.out.print(",");//$JL-SYS_OUT_ERR$
            }
            System.out.print(operations[j].inputParams[k].contentClassName);//$JL-SYS_OUT_ERR$
          }
        }
        System.out.print("):");//$JL-SYS_OUT_ERR$
        if (operations[j].outputParams!= null && operations[j].outputParams.length!=0) {
          System.out.println(operations[j].outputParams[0].contentClassName);//$JL-SYS_OUT_ERR$
        } else {
          System.out.println("void");//$JL-SYS_OUT_ERR$
        }
      }
    }

  }

  public static File getNewJavaFile(File baseDir, String packageName, String className) throws ProxyGeneratorException {
    if (packageName != null && packageName.length() != 0) {
      className = packageName+"."+className;
    }
    return getNewJavaFile(baseDir,className);
  }

  public static File getNewJavaFile(File baseDir, String className) throws ProxyGeneratorException {
    File f = new File(baseDir, className.replace('.', File.separatorChar) + ".java");
    try {
      File fParent = f.getParentFile();
      if (!fParent.exists()) {
        fParent.mkdirs();
      }
      f.createNewFile();
    } catch (IOException e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR,e,f.getAbsolutePath());
    }
    return f;
  }



}
