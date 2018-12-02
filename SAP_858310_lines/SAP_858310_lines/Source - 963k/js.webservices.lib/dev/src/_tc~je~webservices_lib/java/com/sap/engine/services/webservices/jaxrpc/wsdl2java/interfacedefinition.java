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

import com.sap.engine.services.webservices.jaxrpc.util.CodeGenerator;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;
import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;
import java.io.*;

/**
 * This class is used by the interface generator as a holder for interface definition.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class InterfaceDefinition {

  private NameConvertor convertor;
  // Interface Name.
  private String interfaceName;
  // Package Name.
  private String packageName;
  // Interface qName.
  private String qualifiedName;
  // Interface content.
  private CodeGenerator content;
  // Interface extends.
  private String[] extendsInterfaces;
  // Output code Generator.
  private CodeGenerator output;
  // interface imports
  private String[] imports;

  public InterfaceDefinition() {
    content = new CodeGenerator();
    output = new CodeGenerator();
    convertor = new NameConvertor();
  }

  public String getInterfaceName() {
    return interfaceName;
  }

  public void setInterfaceName(String interfaceName) {
    this.interfaceName = interfaceName;
    if (packageName != null && packageName.length() != 0) {
      qualifiedName = packageName+"."+interfaceName;
    } else {
      qualifiedName = interfaceName;
    }
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
    if (packageName != null && packageName.length() != 0) {
      qualifiedName = packageName+"."+interfaceName;
    } else {
      qualifiedName = interfaceName;
    }
  }

  public String getQualifiedName() {
    return qualifiedName;
  }

  public void setQualifiedName(String qualifiedName) {
    this.qualifiedName = qualifiedName;
    this.packageName = convertor.getPackage(qualifiedName);
    this.interfaceName = convertor.getClassName(qualifiedName);
  }

  public CodeGenerator getContent() {
    return content;
  }

  public void setContent(CodeGenerator content) {
    this.content = content;
  }

  public String[] getExtendsInterfaces() {
    return extendsInterfaces;
  }

  public void setExtendsInterfaces(String[] extendsInterfaces) {
    this.extendsInterfaces = extendsInterfaces;
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
    output.add("public interface " + interfaceName);
    if (extendsInterfaces != null && extendsInterfaces.length >0 ){
      output.add(" extends ");
      for (int i=0; i<extendsInterfaces.length; i++) {
        output.add(extendsInterfaces[i]);
        if (i!= (extendsInterfaces.length-1)) {
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
