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

import com.sap.engine.services.webservices.wsdl.WSDLPortType;
import com.sap.engine.services.webservices.wsdl.WSDLOperation;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;
import com.sap.engine.services.webservices.jaxrpc.util.CodeGenerator;
import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * Bean Interface generator
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class BeanHandler implements InterfaceHandler {

  public final String[] homeExtends = new String[] {"EJBHome"};
  public final String[] homeImports = new String[] {"javax.ejb.EJBHome","java.rmi.RemoteException","javax.ejb.CreateException"};
  public final String[] extendsInterfaces = new String[] {"EJBObject"};
  public final String[] interfaceImports = new String[] {"javax.ejb.EJBObject","java.rmi.RemoteException"};;
  public final String[] implImports = new String[] {"javax.ejb.SessionBean","javax.ejb.SessionContext","javax.ejb.CreateException","java.rmi.RemoteException"};
  public ArrayList beans = new ArrayList();

  public class BeanStruct {
    public String iname;
    public String cname;
    public String hname;
  }
  // called when the generation process is started
  public boolean startGenreration(GeneratorEnvironment environment) {
    beans.clear();
    return true;
  }

  // called when the generation process is finished
  public boolean endGeneration(GeneratorEnvironment environment) {
    return true;
  }

  // called to process portType
  public boolean processPortType(WSDLPortType portType, GeneratorEnvironment environment) {
    InterfaceDefinition idefinition = new InterfaceDefinition();
    ClassDefinition cdefinition = new ClassDefinition();
    InterfaceDefinition hdefinition = new InterfaceDefinition();
    hdefinition.setPackageName(environment.outputPackage);
    idefinition.setPackageName(environment.outputPackage);
    cdefinition.setPackageName(environment.outputPackage);
    NameConvertor convertor = environment.convertor;
    String interfaceName = convertor.attributeToClassName(portType.getName());
    while (convertor.conains(environment.outputPackage,interfaceName)) {
      interfaceName += "Int";
    }
    String className = interfaceName+"Bean";
    while (convertor.conains(environment.outputPackage,className)) {
      className = className+"Impl";
    }
    String homeName = interfaceName+"Home";
    while (convertor.conains(environment.outputPackage,homeName)) {
      homeName = homeName+"H";
    }
    convertor.addClassName(environment.outputPackage,interfaceName);
    convertor.addClassName(environment.outputPackage,className);
    convertor.addClassName(environment.outputPackage,homeName);
    idefinition.setInterfaceName(interfaceName);
    cdefinition.setClassName(className);
    hdefinition.setInterfaceName(homeName);
    QName portName = new QName(portType.getNamespace(),portType.getName());
    environment.registerInterfaceName(portName,idefinition.getQualifiedName());
    hdefinition.setImports(homeImports);
    hdefinition.setExtendsInterfaces(homeExtends);
    fillHomeContent(hdefinition.getContent(),idefinition.getQualifiedName());
    idefinition.setImports(interfaceImports);
    idefinition.setExtendsInterfaces(extendsInterfaces);
    cdefinition.setImplInterfaces(new String[] {"SessionBean"});
    cdefinition.setImports(implImports);
    CodeGenerator icontent = idefinition.getContent();
    CodeGenerator ccontent = cdefinition.getContent();
    fillImplementation(ccontent);
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
        icontent.add(" throws RemoteException");
        ccontent.add(" throws RemoteException");
        for (int j=0; j<methodExceptions.length;j++) {
          icontent.add(",");
          icontent.add(methodExceptions[j]);
          ccontent.add(",");
          ccontent.add(methodExceptions[j]);
        }
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
      environment.outputFiles.add(idefinition.save(environment.outputRootDir));
      environment.outputFiles.add(cdefinition.save(environment.outputRootDir));
      environment.outputFiles.add(hdefinition.save(environment.outputRootDir));
      BeanStruct bean = new BeanStruct();
      bean.cname = cdefinition.getQualifiedName();
      bean.iname = idefinition.getQualifiedName();
      bean.hname = hdefinition.getQualifiedName();
      beans.add(bean);
    } catch (ProxyGeneratorException e) {
      environment.generateException = e;
    }
    return true;
  }

  private void fillImplementation(CodeGenerator generator) {
    generator.addLine("public void ejbRemove() {");
    generator.addLine("}");
    generator.addLine("public void ejbActivate() {");
    generator.addLine("}");
    generator.addLine("public void ejbPassivate() {");
    generator.addLine("}");
    generator.addLine("public void setSessionContext(SessionContext context) {");
    generator.addLine("  myContext = context;");
    generator.addLine("}");
    generator.addLine("private SessionContext myContext;");
    generator.addLine("/**");
    generator.addLine(" * Create Method.");
    generator.addLine(" */");
    generator.addLine("public void ejbCreate() throws CreateException {");
    generator.addLine("  // TODO : Implement");
    generator.addLine("}");
  }

  private void fillHomeContent(CodeGenerator generator, String iname) {
    generator.addLine("/**");
    generator.addLine(" * Create Method.");
    generator.addLine(" */");
    generator.addLine("public "+iname+" create() throws CreateException, RemoteException;");
  }

}
