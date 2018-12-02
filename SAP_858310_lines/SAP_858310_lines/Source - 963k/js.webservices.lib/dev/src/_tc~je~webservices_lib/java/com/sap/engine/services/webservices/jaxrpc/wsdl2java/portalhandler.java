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
import com.sap.engine.services.webservices.wsdl.WSDLPortType;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;
import com.sap.engine.services.webservices.wsdl.WSDLService;
import com.sap.engine.services.webservices.wsdl.WSDLOperation;

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * 
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class PortalHandler implements InterfaceHandler  {


  private String defaultName = "PortalService";
  private InterfaceDefinition idefinition;
  private ClassDefinition cdefinition;

  public PortalHandler() {
  }

  public PortalHandler(String defaultName) {
    this.defaultName = defaultName;
  }

  // called when the generation process is started
  public boolean startGenreration(GeneratorEnvironment environment) {
    NameConvertor convertor = environment.convertor;
    WSDLDefinitions definitions= environment.definitions;
    ArrayList services = definitions.getServices();
    if (services.size() != 0) {
      WSDLService service = (WSDLService) services.get(0);
      defaultName = convertor.attributeToClassName(service.getName());
    }
    idefinition = new InterfaceDefinition();
    cdefinition = new ClassDefinition();
    idefinition.setPackageName(environment.outputPackage);
    cdefinition.setPackageName(environment.outputPackage);
    while (convertor.conains(environment.outputPackage,defaultName)) {
      defaultName = defaultName + "Service";
    }
    convertor.addClassName(environment.outputPackage, defaultName);
    String implName = defaultName + "Impl";
    while (convertor.conains(environment.outputPackage,implName)) {
      implName += "Impl";
    }
    convertor.addClassName(environment.outputPackage, implName);
    idefinition.setInterfaceName(defaultName);
    cdefinition.setClassName(implName);
    idefinition.setImports(getIImports());
    cdefinition.setImports(getImplImports(defaultName));
    idefinition.setExtendsInterfaces(new String[] {"IService"});
    cdefinition.setImplInterfaces(new String[] {idefinition.getQualifiedName()});
    fillImplementation(cdefinition.getContent());
    fillInterface(idefinition.getContent());
    return true;
  }

  // called when the generation process is finished
  public boolean endGeneration(GeneratorEnvironment environment) {
    try {
      idefinition.save(environment.outputRootDir);
      cdefinition.save(environment.outputRootDir);
    } catch (ProxyGeneratorException e) {
      environment.generateException = e;
      return false;
    }
    return true;
  }


  // called to process portType
  public boolean processPortType(WSDLPortType portType, GeneratorEnvironment environment) {
    // registers interface
    environment.registerInterfaceName(new QName(portType.getNamespace(),portType.getName()),idefinition.getQualifiedName());
    CodeGenerator icontent = idefinition.getContent();
    CodeGenerator ccontent = cdefinition.getContent();
    NameConvertor convertor = environment.convertor;
    ArrayList operations = portType.getOperations();
    try {
      for (int i = 0; i < operations.size(); i++) {
        WSDLOperation operation = (WSDLOperation) operations.get(i);
        environment.iGenerator.checkParams(operation);
        String methodName = convertor.attributeToMethodName(operation.getName());
        String methodHeader = environment.iGenerator.generateOperationHeader(methodName,operation);
        String[] methodExceptions = environment.iGenerator.getExceptions(operation.getFaultList(),methodName);
        environment.registerInterfaceMethod(idefinition.getQualifiedName(),environment.iGenerator.fillOperationInfo(methodName,operation));
        icontent.addIndent();
        ccontent.addIndent();
        icontent.add(methodHeader);
        ccontent.add(methodHeader);
        StringBuffer throwsExpression = new StringBuffer();
        if (methodExceptions.length != 0) {
          throwsExpression.append(" throws ");
          for (int j=0; j<methodExceptions.length;j++) {
            if (j!=0) {
              throwsExpression.append(",");
            }
            throwsExpression.append(methodExceptions[j]);
          }
        }
        icontent.add(throwsExpression.toString());
        ccontent.add(throwsExpression.toString());
        icontent.add(";");
        icontent.addNewLine();
        ccontent.add(" {");
        ccontent.addNewLine();
        if (environment.returnType != null) {
          String result = environment.iGenerator.getNull(environment.returnType);
          ccontent.addLine("  return "+result+";");
        }
        ccontent.addLine("}");
      }
    } catch (ProxyGeneratorException e) {
      environment.generateException = e;
    }
    return true;
  }

  public String[] getIImports() {
    return new String[] {"com.sapportals.portal.prt.service.IService"};
    //,"com.sapportals.portal.prt.service.soap.types.*"
  }

  public String[] getImplImports(String interfaceName) {
    String[] result = new String[3];
    result[0] = "com.sapportals.portal.prt.service.IServiceConfiguration";
    result[1] = "com.sapportals.portal.prt.service.IServiceContext";
    result[2] = "com.sapportals.portal.prt.resource.IResource";
//    result[3] = "com.sapportals.portal.prt.service.soap.ISOAPService";
//    result[4] = "com.sapportals.portal.prt.service.soap.IPRTSOAPCall";
//    result[5] = "com.sapportals.portal.prt.service.soap.SOAPParameter";
//    result[6] = "com.sapportals.portal.prt.service.soap.types.*";
//    result[7] = "com.sapportals.portal.prt.service.soap.util.*";
//    result[3] = interfaceName;
    return result;
  }

  private void fillInterface(CodeGenerator generator) {
    generator.addLine("public static final String KEY =\""+cdefinition.getQualifiedName()+"\";");
  }

  private void fillImplementation(CodeGenerator generator) {
    generator.addLine("IServiceContext mm_serviceContext;");
    generator.addLine("public IServiceContext getContext() {");
    generator.addLine("  return mm_serviceContext;");
    generator.addLine("}");
    generator.addLine("public String getKey() {");
    generator.addLine("  return "+idefinition.getQualifiedName()+".KEY;");
    generator.addLine("}");
    generator.addLine("public void afterInit() {");
    generator.addLine("}");
    generator.addLine("public void configure(IServiceConfiguration configuration) {");
    generator.addLine("}");
    generator.addLine("public void destroy() {");
    generator.addLine("}");
    generator.addLine("public void init(IServiceContext serviceContext) {");
    generator.addLine("  mm_serviceContext = serviceContext;");
    generator.addLine("}");
    generator.addLine("public void release() {");
    generator.addLine("}");
    generator.addLine("// Service implementation");
  }

//  public String[] appendIExtends(String interfaceName) {
//    return
//  }
//
//  public String appendImplExtends(String interfaceName, String className) {
//    return null;
//  }
//
//  public String[] appendImplImplements(String interfaceName, String className) {
//    return new String[] {interfaceName};
//  }
//
//  public String[] appendIMethodExceptions(String methodName, String interfaceName) {
//    return new String[0];
//  }
//
//  public String[] appendImplMethodExceptions(String methodName, String className) {
//    return new String[0];
//  }
//
//  public void appendIMethods(CodeGenerator codeGenerator, String interfaceName) {
//    codeGenerator.addLine("public static final String KEY =\""+interfaceName+"\"");
//  }
//
//
//  public String appendAddonClass(String packageName, String iName, String implName, CodeGenerator generator, WSDLPortType portType, NameConvertor convertor) {
//    return null;
//  }
}
