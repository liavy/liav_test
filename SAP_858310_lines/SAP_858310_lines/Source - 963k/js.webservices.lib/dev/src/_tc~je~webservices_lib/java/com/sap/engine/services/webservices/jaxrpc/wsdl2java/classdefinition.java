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
import com.sap.engine.services.webservices.jaxrpc.util.CodeGenerator;
import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;
import java.io.*;

/**
 * 
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ClassDefinition {

  private NameConvertor convertor;
  // Extends class
  private String extClass;
  // Class Name.
  private String className;
  // Package Name.
  private String packageName;
  // Interface qName.
  private String qualifiedName;
  // Interface content.
  private CodeGenerator content;
  // Interface extends.
  private String[] implInterfaces;
  // Output code Generator.
  private CodeGenerator output;
  // interface imports
  private String[] imports;

  public ClassDefinition() {
    content = new CodeGenerator();
    output = new CodeGenerator();
    convertor = new NameConvertor();
  }

  public String getExtClass() {
    return extClass;
  }

  public void setExtClass(String extClass) {
    this.extClass = extClass;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
    if (packageName != null && packageName.length() != 0) {
      qualifiedName = packageName+"."+className;
    } else {
      qualifiedName = className;
    }

  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
    if (packageName != null && packageName.length() != 0) {
      qualifiedName = packageName+"."+className;
    } else {
      qualifiedName = className;
    }

  }

  public String getQualifiedName() {
    return qualifiedName;
  }

  public void setQualifiedName(String qualifiedName) {
    this.qualifiedName = qualifiedName;
    this.packageName = convertor.getPackage(qualifiedName);
    this.className = convertor.getClassName(qualifiedName);
  }

  public CodeGenerator getContent() {
    return content;
  }

  public void setContent(CodeGenerator content) {
    this.content = content;
  }

  public String[] getImplInterfaces() {
    return implInterfaces;
  }

  public void setImplInterfaces(String[] implInterfaces) {
    this.implInterfaces = implInterfaces;
  }

  public CodeGenerator getOutput() {
    return output;
  }

  public void setOutput(CodeGenerator output) {
    this.output = output;
  }

  public String[] getImports() {
    return imports;
  }

  public void setImports(String[] imports) {
    this.imports = imports;
  }

  public File save(File outputDir) throws ProxyGeneratorException {
    File outFile = GeneratorEnvironment.getNewJavaFile(outputDir, qualifiedName);
    generateOutput();
    writeFile(output,outFile);
    return outFile;
  }

  /**
   * Outputs package declaration.
   * @param generator
   */
  private void writePackage(CodeGenerator generator) {
    if (packageName != null && packageName.length() != 0) {
      generator.addLine("package " + packageName + ";");
    }
  }

//  private File getNewFile(File baseDir, String className) throws ProxyGeneratorException {
//    File f = new File(baseDir, className.replace('.', File.separatorChar) + ".java");
//    try {
//      File fParent = f.getParentFile();
//      if (!fParent.exists()) {
//        fParent.mkdirs();
//      }
//      f.createNewFile();
//    } catch (IOException e) {
//      throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR,e,f.getAbsolutePath());
//    }
//    return f;
//  }

  private void writeFile(CodeGenerator generator, File outputFile) throws ProxyGeneratorException {
    try {
      PrintWriter output = new PrintWriter(new FileOutputStream(outputFile), true);
      output.write(generator.toString());
      output.close();
    } catch (FileNotFoundException e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR,outputFile.getAbsolutePath());
    }
  }

  private void generateOutput() {
    output.clear(2);
    writePackage(output);
    if (imports != null) {
      for (int i=0; i<imports.length; i++) {
        output.addLine("import "+imports[i]+";");
      }
    }
    output.addNewLine();
    output.add("public class " + className);
    if (extClass != null && extClass.length() >0 ){
      output.add(" extends "+extClass);
    }

    if (implInterfaces != null && implInterfaces.length >0 ){
      output.add(" implements ");
      for (int i=0; i<implInterfaces.length; i++) {
        output.add(implInterfaces[i]);
        if (i!= (implInterfaces.length-1)) {
          output.add(",");
        }
      }
    }
    output.add(" {");
    output.addNewLine();
    output.startSection();
    output.append(content);
    output.endSection();
    output.addLine("}");
  }

}
