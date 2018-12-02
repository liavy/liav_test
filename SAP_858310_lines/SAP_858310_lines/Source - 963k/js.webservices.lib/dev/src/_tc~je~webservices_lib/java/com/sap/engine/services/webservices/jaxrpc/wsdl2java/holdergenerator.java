/*
 * Copyright (c) 2002 by SAP AG.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP AG.
 * Created : 2002-5-23
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import com.sap.engine.services.webservices.jaxrpc.util.CodeGenerator;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Place your javadoc here.
 * @author Chavdar Baykov, Chavdarb@yahoo.com
 * @version 1.0
 */
public class HolderGenerator {

  private NameConvertor nameConvertor;
  private String packageName;

  public HolderGenerator(NameConvertor nameConvertor) {
    this.nameConvertor = nameConvertor;
  }

  public File[] generateHolders(File workDir, String packageName, ArrayList holders) throws Exception {
    File holderPackageDir = new File(workDir, nameConvertor.packageToPath(packageName));
    this.packageName = packageName;
    File[] result = new File[holders.size()];
    if (holders.size() > 0) {
      holderPackageDir.mkdirs();

      for (int i = 0; i < holders.size(); i++) {
        HolderInfo holderInfo = (HolderInfo) holders.get(i);
        String className = holderInfo.getName();
        File outFile = new File(holderPackageDir, className + ".java");
        result[i] = outFile;
        CodeGenerator generator = new CodeGenerator();
        generateHolder(generator, holderInfo);
        PrintWriter output = new PrintWriter(new FileOutputStream(outFile), true);
        output.print(generator.toString());
        output.close();
      } 
    }
    return result;
  }

  /**
   * Process Generation of single Holdes class
   */
  private void generateHolder(CodeGenerator output, HolderInfo info) throws Exception {
    output.addLine("package " + packageName + ";");
    output.addLine();
    output.addLine("public final class " + info.getName() + " implements javax.xml.rpc.holders.Holder {");
    output.addLine();
    output.startSection();
    output.addLine("public "   + info.getType() + " value;");
    output.addLine();
    output.addLine("public " + info.getName() + "() {");
    output.addLine("}");
    output.addLine();
    output.addLine("public " + info.getName() + "("   + info.getType() + " value) {");
    output.addLine("  this.value = value;");
    output.addLine("}");
    output.endSection();
    output.addLine();
    output.addLine("}");
  }

}

