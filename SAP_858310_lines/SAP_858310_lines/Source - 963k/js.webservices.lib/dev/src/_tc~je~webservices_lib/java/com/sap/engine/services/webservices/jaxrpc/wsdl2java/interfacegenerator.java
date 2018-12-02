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

import com.sap.engine.services.webservices.wsdl.*;
import com.sap.engine.services.webservices.jaxrpc.schema2java.*;
import com.sap.engine.services.webservices.jaxrpc.util.*;
import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;
import com.sap.engine.services.webservices.jaxrpc.encoding.FieldInfo;
import com.sap.engine.services.webservices.jaxrpc.encoding.AttributeInfo;
import com.sap.engine.lib.schema.components.TypeDefinitionBase;
import com.sap.engine.lib.schema.components.ComplexTypeDefinition;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;
import java.rmi.RemoteException;

/**
 * Outside-in approach wsdl-to-java generator. This generator does not use binding information because it generates just interfaces.
 * and empty stubs.
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class InterfaceGenerator {

  private InterfaceHandler ihandler= null;
  private ArrayList exceptionMessages = null;
  private Hashtable nullTable = null;
  private Properties uriToPackageMapping = null;
  private String typesSubpackage = "types";
  private Hashtable handlerRegistry = null;
  public static final String BEANS = "JAVA_BEANS";
  public static final String CLASSES = "JAVA_CLASSES";
  public static final String PORTALS = "PORTALS";
  public GeneratorEnvironment environment;
  private PackageBuilder builder;
  private String httpProxyHost;
  private String httpProxyPort;

  public void setHTTPProxy(String proxyHost, String proxyPort) {
    this.httpProxyHost = proxyHost;
    this.httpProxyPort = proxyPort;
  }

  public void clearHTTPProxy() {
    this.httpProxyHost = null;
    this.httpProxyPort = null;
  }

  public InterfaceGenerator() {
    builder = new PackageBuilder();
    exceptionMessages = new ArrayList();
    nullTable = new Hashtable();
    nullTable.put("int","0");
    nullTable.put("long","0");
    nullTable.put("byte","(byte) 0");
    nullTable.put("double","0.0");
    nullTable.put("float","0.0f");
    nullTable.put("boolean","false");
    nullTable.put("short","0");
    nullTable.put("float","0.0f");
    handlerRegistry = new Hashtable();
    ihandler = new BeanHandler();
    handlerRegistry.put(BEANS, new BeanHandler());
    handlerRegistry.put(PORTALS, new PortalHandler());
    environment = new GeneratorEnvironment();
  }

  public String getNull(String type) {
    if (type == null) {
      return "";
    }
    String result = "null";
    if (nullTable.get(type) != null) {
      result = (String) nullTable.get(type);
    }
    return result;
  }

  public void init(WSDLDefinitions definitions, String outputDir, String outputPackage, InterfaceHandler handler, Properties uriToPackageMapping) throws ProxyGeneratorException {
    environment.definitions = definitions;
    try {
      environment.definitions.loadSchemaInfo();
    } catch (WSDLException e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.GENERATION_PROBLEM,e);
    }
    environment.schema = definitions.getSchemaInfo();
    environment.outputRootDir = new File(outputDir);
    environment.outputRootDir.mkdirs();
    environment.outputPackage = outputPackage;
    this.ihandler = handler;
    environment.clear();
    environment.iGenerator = this;
    exceptionMessages.clear();
    this.uriToPackageMapping = uriToPackageMapping;
  }

  /**
   * Returns true is some operation is document style.
   * @param operation
   * @return
   * @throws ProxyGeneratorException
   */
  public boolean isDocumentStyle(WSDLOperation operation) throws ProxyGeneratorException {
    WSDLChannel input = operation.getInput();
    WSDLChannel output = operation.getOutput();
    if (input == null || output == null) {
      return false;
    }
    WSDLMessage inputMessage = getMessage(input.getMessage(),operation.getName());
    WSDLMessage outputMessage = getMessage(output.getMessage(),operation.getName());
    if (inputMessage.getPartCount() != 1 || outputMessage.getPartCount() != 1) {
      return false;
    }
    WSDLPart ipart = inputMessage.getPart(0);
    WSDLPart opart = outputMessage.getPart(0);
    if (ipart.getStyle() == WSDLPart.SIMPLE_TYPE || opart.getStyle() == WSDLPart.SIMPLE_TYPE) {
      return false;
    }
    TypeDefinitionBase itype = environment.schema.getElementType(ipart.getType().getURI(),ipart.getType().getLocalName());
    TypeDefinitionBase otype = environment.schema.getElementType(opart.getType().getURI(),opart.getType().getLocalName());
    if (environment.schema.isSimple(itype) || environment.schema.isSimple(otype)) {
      return false;
    }
    if (environment.schema.isDocumentArray((ComplexTypeDefinition) itype)) {
      return false;
    }
    if (environment.schema.isDocumentArray((ComplexTypeDefinition) otype)) {
      return false;
    }
    int ikind = environment.schema.getComplexTypeKind((ComplexTypeDefinition) itype);
    int okind = environment.schema.getComplexTypeKind((ComplexTypeDefinition) otype);
    if ((ikind == FieldInfo.FIELD_ALL || ikind == FieldInfo.FIELD_SEQUENCE) && (okind == FieldInfo.FIELD_ALL || okind == FieldInfo.FIELD_SEQUENCE)) {
      ArrayList inElements = environment.schema.getComplexFieldInfo((ComplexTypeDefinition) itype);
      ArrayList outElements = environment.schema.getComplexFieldInfo((ComplexTypeDefinition) otype);
      ArrayList outAttribs = environment.schema.getComplexAttributeInfo((ComplexTypeDefinition) otype);
      ArrayList inAttribs = environment.schema.getComplexAttributeInfo((ComplexTypeDefinition) itype);
      if (inAttribs.size() != 0) {
        return false;
      }
      if (outAttribs.size() != 0 || outElements.size() > 1) {
        return false;
      }
      for (int i = 0; i < inElements.size(); i++) {
        FieldInfo info = (FieldInfo) inElements.get(i);
        if (info.getFieldModel() != FieldInfo.FIELD_ELEMENT) {
          return false;
        }
        if (inElements.size() == 1 && itype.getName().equals(info.getTypeLocalName())) {
          if (itype.getTargetNamespace() != null && itype.getTargetNamespace().equals(info.getTypeUri())) {
            return false;
          }
          // both null
          if (itype.getTargetNamespace() == info.getTypeUri()) { //$JL-STRING$ 
            return false;
          }
        }
      }
      if (outElements.size() != 0) {
        FieldInfo info = (FieldInfo) outElements.get(0);
        if (info.getFieldModel() != FieldInfo.FIELD_ELEMENT) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private void prepareSchema() throws ProxyGeneratorException {
    try {
      String typesPackage = typesSubpackage;
      if (environment.outputPackage != null && environment.outputPackage.length()!=0) {
        typesPackage = environment.outputPackage+"."+typesPackage;
      }
      if (environment.schema == null) {
        environment.schema = new SchemaToJavaGenerator();
        environment.schema.setPackageBuilder(new PackageBuilder());
        environment.schema.prepareAll(typesPackage);
      } else {
        environment.schema.setMirrorLocations(environment.definitions.getMirrorLocations());
        environment.schema.setMirrorMapping(environment.definitions.getMirrorMapping());
        environment.schema.setHttpProxy(this.httpProxyHost,this.httpProxyPort);
        if (environment.schema.isLoaded() == false || uriToPackageMapping!=null) {
          if (this.uriToPackageMapping != null) {
            environment.schema.setUriToPackagetMapping(this.uriToPackageMapping);
          }
          environment.schema.prepareAll(typesPackage);
        }
      }
      environment.javaToSchemaMapping = environment.schema.getJavaToSchemaMapping();
    } catch (SchemaToJavaGeneratorException e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.GENERATION_PROBLEM,e);
    }
  }

  /**
   * Loads and generates schema java representation.
   */
  private void generateSchema() throws ProxyGeneratorException {
    try {
      String typesPackage = typesSubpackage;
      if (environment.outputPackage != null && environment.outputPackage.length()!=0) {
        typesPackage = environment.outputPackage+"."+typesPackage;
      }
      if (environment.schema == null) {
        environment.schema = new SchemaToJavaGenerator();
        environment.schema.setPackageBuilder(new PackageBuilder());
        environment.schema.prepareAll(typesPackage);
      } else {
        environment.schema.setMirrorLocations(environment.definitions.getMirrorLocations());
        environment.schema.setMirrorMapping(environment.definitions.getMirrorMapping());
        environment.schema.setHttpProxy(this.httpProxyHost,this.httpProxyPort);
        if (uriToPackageMapping != null) {
          environment.schema.setUriToPackagetMapping(uriToPackageMapping);
        }
        environment.schema.setContainerMode(true);
        environment.schema.generateAll(environment.outputRootDir, typesPackage);
        File[] filesArr = environment.schema.getFileList();
        for (int i=0; i<filesArr.length; i++) {
          environment.outputFiles.add(filesArr[i]);
        }
      }
      environment.javaToSchemaMapping = environment.schema.getJavaToSchemaMapping();
    } catch (SchemaToJavaGeneratorException e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.GENERATION_PROBLEM,e);
    }
  }

  private File getNewJavaFile(File baseDir, String packageName, String className) throws ProxyGeneratorException {
    if (packageName != null && packageName.length() != 0) {
      className = packageName+"."+className;
    }
    return getNewJavaFile(baseDir,className);
  }

  private File getNewJavaFile(File baseDir, String className) throws ProxyGeneratorException {
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
    environment.outputFiles.add(f);
    return f;
  }

  /**
   * By wsdl message returns Custom Exception class name.
   * @param message
   * @param packageSet
   * @return
   */
  private String getExceptionClassName(WSDLMessage message, boolean packageSet) throws ProxyGeneratorException {
    if (message.getPartCount() ==1) {
      WSDLPart part = message.getPart(0);
      TypeDefinitionBase type = getPartType(part);
      String result = message.getName(); // SimpleType
      if (environment.schema.isSimple(type) == false && environment.schema.isSoapArray(type) == false) { // Complex Type
        result = type.getName();
        if (result == null || result.length() == 0) { // Element link
          result = part.getType().getLocalName()+"Exception";
        }
      }
      result = environment.convertor.attributeToClassName(result);
      if (environment.outputPackage != null && environment.outputPackage.length() != 0 && packageSet) {
        result = environment.outputPackage+"."+result;
      }
      return result;
    } else {
      throw new ProxyGeneratorException(ProxyGeneratorException.WRONG_FAULT, message.getQName().toString());
    }
  }

  /**
   * Outputs package declaration.
   * @param generator
   */
  private void writePackage(CodeGenerator generator) {
    if (environment.outputPackage != null && environment.outputPackage.length() != 0) {
      generator.addLine("package " + environment.outputPackage + ";");
    }
  }

  /**
   * Adds WSDL Message to List of custom messages.
   */
  private void addExceptionMessage(WSDLMessage message) throws ProxyGeneratorException {
    if (exceptionMessages.contains(message)) {
      return;
    }
    ArrayList messageParts = message.getParts();
    if (messageParts.size() ==1) {
      WSDLPart part = (WSDLPart) messageParts.get(0);
      TypeDefinitionBase type = getPartType(part);
      if (environment.schema.isSimple(type) == false && environment.schema.isSoapArray(type) == false) { // This type is complex
        for (int i=0; i<exceptionMessages.size(); i++) {
          WSDLMessage cmessage = (WSDLMessage) exceptionMessages.get(i);
          if (cmessage.getPartCount() ==1) {
            WSDLPart cpart = cmessage.getPart(0);
            TypeDefinitionBase ctype = getPartType(cpart);
            if (ctype == type) {
              return;
            }
          }
        }
      }
    }
    exceptionMessages.add(message);
  }

  /**
   * Returns Java type corresponding to given WSDL part.
   */
  public TypeDefinitionBase getPartType(WSDLPart part) throws ProxyGeneratorException {
    com.sap.engine.lib.xml.util.QName typeLink = part.getType();
    TypeDefinitionBase type;
    if (part.getStyle() == WSDLPart.SIMPLE_TYPE) { // 'type' link
      type = environment.schema.getType(typeLink.getURI(), typeLink.getLocalName());
    } else { // 'element' link
      type = environment.schema.getElementType(typeLink.getURI(), typeLink.getLocalName());
    }
    if (type == null) {
      throw new ProxyGeneratorException(ProxyGeneratorException.MISSING_SCHEMA_NODE,typeLink.toString());
    }
    return type;
  }

  /**
   * Checks for in/out params of wsdl operation.
   * @param operation
   */
  public void checkParams(WSDLOperation operation) throws ProxyGeneratorException {
    WSDLChannel input = operation.getInput();
    WSDLChannel output = operation.getOutput();

    if (input == null || output == null) {
      return;
    }

    WSDLMessage inMessage = environment.definitions.getMessage(input.getMessage());
    WSDLMessage outMessage = environment.definitions.getMessage(output.getMessage());

    if (inMessage == null) {
      throw new ProxyGeneratorException(ProxyGeneratorException.MISSING_MESSAGE,input.getMessage().toString(),operation.getName());
    }
    if (outMessage == null) {
      throw new ProxyGeneratorException(ProxyGeneratorException.MISSING_MESSAGE,output.getMessage().toString(),operation.getName());
    }

    ArrayList inMessageParts = inMessage.getParts();
    ArrayList outMessageParts = outMessage.getParts();

    if (outMessageParts.size() == 1) {
      return;
    }
    for (int i = 0; i < inMessageParts.size(); i++) {
      WSDLPart partIn = (WSDLPart) inMessageParts.get(i);
      for (int j = 0; j < outMessageParts.size(); j++) {
        WSDLPart partOut = (WSDLPart) outMessageParts.get(j);
        if (partIn.getName().equals(partOut.getName())) {
            throw new ProxyGeneratorException(ProxyGeneratorException.INOUTPARAMS,operation.getName());
//          partIn.inout = true;
//          partOut.inout = true;
        }
      }
    }
    return;
  }

  /**
   * Returns message from WSDL definitions and throws exception if not found.
   */
  public WSDLMessage getMessage(com.sap.engine.lib.xml.util.QName messageLink, String operationName) throws ProxyGeneratorException {
    WSDLMessage message = environment.definitions.getMessage(messageLink.getLocalName(),messageLink.getURI());
    if (message == null) {
      throw new ProxyGeneratorException(ProxyGeneratorException.MISSING_MESSAGE,messageLink.toString(),operationName);
    }
    return message;
  }


  public String processAOperationHeader(String methodName, WSDLOperation operation) throws ProxyGeneratorException {
    // Method header
    StringBuffer result = new StringBuffer();
    com.sap.engine.lib.xml.util.QName messageInName = operation.getInput().getMessage();
    com.sap.engine.lib.xml.util.QName messageOutName = operation.getOutput().getMessage();
    WSDLMessage inMessage = environment.definitions.getMessage(messageInName);
    WSDLMessage outMessage = environment.definitions.getMessage(messageOutName);
    WSDLPart inpart = inMessage.getPart(0);
    WSDLPart outpart = outMessage.getPart(0);
    com.sap.engine.lib.xml.util.QName inType = inpart.getType();
    com.sap.engine.lib.xml.util.QName outType = outpart.getType();
    ComplexTypeDefinition inTypeDefinition = (ComplexTypeDefinition) environment.schema.getElementType(inType.getURI(),inType.getLocalName());
    ComplexTypeDefinition outTypeDefinition = (ComplexTypeDefinition) environment.schema.getElementType(outType.getURI(),outType.getLocalName());
    ArrayList inFields = environment.schema.getComplexFieldInfo(inTypeDefinition);
    ArrayList outFields = environment.schema.getComplexFieldInfo(outTypeDefinition);
    result.append("public ");
    environment.returnType = null;
    if (outFields.size() != 0) {
      FieldInfo info = (FieldInfo) outFields.get(0);
      environment.returnType = info.getTypeJavaName();
      result.append(info.getTypeJavaName());
    } else {
      result.append("void");
    }
    result.append(" "+methodName+"(");
    boolean flag = false;
    for (int i=0; i<inFields.size(); i++) {
      if (flag) {
        result.append(", ");
      }
      FieldInfo info = (FieldInfo) inFields.get(i);
      result.append(info.getTypeJavaName()+" _"+info.getFieldJavaName());
      flag = true;
    }
    result.append(")");
    return result.toString();
  }

  public String[] getExceptions(ArrayList faults, String operationName) throws ProxyGeneratorException {
    String [] result = new String[faults.size()];
    for (int i=0; i<faults.size(); i++) {
      WSDLFault fault = (WSDLFault) faults.get(i);
      WSDLMessage message = getMessage(fault.getMessage(),operationName);
      addExceptionMessage(message);
      result[i] = getExceptionClassName(message,true);
    }
    return result;
  }

  /**
   * Fills operation information from wsdl.
   * @param methodName
   * @param operation
   * @return
   * @throws ProxyGeneratorException
   */
  public OperationInterface fillOperationInfo(String methodName, WSDLOperation operation) throws ProxyGeneratorException {
    OperationInterface operationInterface = new OperationInterface();
    operationInterface.operationName = operation.getName();
    operationInterface.operationJavaName = methodName;
    operationInterface.operationRequestName = operation.getName();
    operationInterface.operationResponseName = operation.getName()+"Response";
    WSDLPortType portType = ((WSDLPortType) operation.getParentNode());
    operationInterface.portTypeName = new QName(portType.getNamespace(),portType.getNamespace());
    if (isDocumentStyle(operation)) {
      WSDLMessage inMessage = environment.definitions.getMessage(operation.getInput().getMessage());
      WSDLMessage outMessage = environment.definitions.getMessage(operation.getOutput().getMessage());
      WSDLPart inPart = inMessage.getPart(0);
      WSDLPart outPart = outMessage.getPart(0);
      operationInterface.operationRequestName = inPart.getType().getLocalName();
      operationInterface.operationRequestNamespace = inPart.getType().getURI();
      operationInterface.operationResponseName = outPart.getType().getLocalName();
      operationInterface.operationResponseNamespace = outPart.getType().getURI();
      com.sap.engine.lib.xml.util.QName inType = inPart.getType();
      com.sap.engine.lib.xml.util.QName outType = outPart.getType();
      ComplexTypeDefinition inTypeDefinition = (ComplexTypeDefinition) environment.schema.getElementType(inType.getURI(),inType.getLocalName());
      ComplexTypeDefinition outTypeDefinition = (ComplexTypeDefinition) environment.schema.getElementType(outType.getURI(),outType.getLocalName());
      ArrayList inFields = environment.schema.getComplexFieldInfo(inTypeDefinition);
      ArrayList outFields = environment.schema.getComplexFieldInfo(outTypeDefinition);
      if (outFields.size() == 1) {
        FieldInfo info = (FieldInfo) outFields.get(0);
        operationInterface.outputParams = new ServiceParam[1];
        ServiceParam param = new ServiceParam();
        param.isElement = false;
        param.schemaName = info.getTypeQName();
        param.contentClassName = info.getTypeJavaName();
        param.name = info.getFieldLocalName();
        param.namespace = info.getFieldUri();
        operationInterface.outputParams[0] = param;
      } else {
        operationInterface.outputParams = new ServiceParam[0];
      }
      operationInterface.inputParams = new ServiceParam[inFields.size()];
      for (int i=0; i<inFields.size(); i++) {
        FieldInfo info = (FieldInfo) inFields.get(i);
        ServiceParam param = new ServiceParam();
        param.isElement = false;
        param.schemaName = info.getTypeQName();
        param.contentClassName = info.getTypeJavaName();
        param.name = info.getFieldLocalName();
        param.namespace = info.getFieldUri();
        operationInterface.inputParams[i] = param;
      }
    } else {
      WSDLChannel input = operation.getInput();
      WSDLChannel output = operation.getOutput();
      if (output != null) {
        WSDLMessage message = getMessage(output.getMessage(),operation.getName());
        ArrayList parts = message.getParts();
        if (parts.size() == 1) { // this is method return
          WSDLPart part = (WSDLPart) parts.get(0);
          operationInterface.outputParams = new ServiceParam[1];
          ServiceParam param = new ServiceParam();
          param.wsdlPartName = part.getName();
          TypeDefinitionBase type = getPartType(part);
          if (part.getStyle() == WSDLPart.STRUCTURED_TYPE) { // element
            operationInterface.isDocumentStyle = true;
            param.isElement = true;
            param.name = part.getType().getLocalName();
            param.namespace = part.getType().getURI();
            operationInterface.operationResponseName = param.name;
            operationInterface.operationResponseNamespace = param.namespace;
          } else { // type
            param.isElement = false;
            param.name = part.getName();
          }
          param.contentClassName = environment.schema.getJavaPrimitive(type);
          param.schemaName = new QName(type.getTargetNamespace(),type.getName());
          operationInterface.outputParams[0] = param;
        } else { // method is void
          operationInterface.outputParams = new ServiceParam[0];
        }
      }
      if (input != null) {
        WSDLMessage message = getMessage(input.getMessage(),operation.getName());
        ArrayList parts = message.getParts();
        operationInterface.inputParams = new ServiceParam[parts.size()];
        for (int i = 0; i < parts.size(); i++) {
          ServiceParam param = new ServiceParam();
          WSDLPart part = (WSDLPart) parts.get(i);
          param.wsdlPartName = part.getName();
          TypeDefinitionBase type = getPartType(part);
          if (part.getStyle() == WSDLPart.STRUCTURED_TYPE)  { // Element
            operationInterface.isDocumentStyle = true;
            param.isElement = true;
            param.name = part.getType().getLocalName();
            param.namespace = part.getType().getURI();
            operationInterface.operationRequestName = param.name;
            operationInterface.operationRequestNamespace = param.namespace;
          } else { // Type
            param.isElement = false;
            param.name = part.getName();
          }
          param.contentClassName = environment.schema.getJavaPrimitive(type);
          param.schemaName = new QName(type.getTargetNamespace(),type.getName());
          operationInterface.inputParams[i] = param;
        }
      }
    }
    ArrayList faults = operation.getFaultList();
    operationInterface.faultParams = new ServiceParam[faults.size()];
    for (int i=0; i<faults.size(); i++) {
      WSDLFault fault = (WSDLFault) faults.get(i);
      WSDLMessage message = environment.definitions.getMessage(fault.getMessage());
      WSDLPart part = message.getPart(0);
      TypeDefinitionBase type = getPartType(part);
      ServiceParam param = new ServiceParam();
      param.wsdlPartName = part.getNamespace();
      if (part.getStyle() == WSDLPart.SIMPLE_TYPE) {
        param.name = part.getName();
        param.isElement = false;
        param.schemaName = new QName(part.getType().getURI(),part.getType().getName());
        param.contentClassName = environment.schema.getJavaPrimitive(type);
      } else {
        param.name = part.getType().getName();
        param.namespace = part.getType().getURI();
        param.isElement = true;
        param.schemaName = new QName(type.getTargetNamespace(),type.getName());
        param.contentClassName = environment.schema.getJavaPrimitive(type);
      }
      operationInterface.faultParams[i] = param;
    }
    return operationInterface;
  }

  public String generateOperationHeader(String methodName, WSDLOperation operation) throws ProxyGeneratorException {
    if (isDocumentStyle(operation)) {
      return processAOperationHeader(methodName ,operation);
    } else {
      return processOperationHeader(methodName, operation);
    }
  }

  public String processOperationHeader(String methodName, WSDLOperation operation) throws ProxyGeneratorException {
    StringBuffer result = new StringBuffer();
    result.append("public ");
    WSDLChannel input = operation.getInput();
    WSDLChannel output = operation.getOutput();
    environment.returnType = null;
    if (output != null) {
      com.sap.engine.lib.xml.util.QName messagelink = output.getMessage();
      WSDLMessage message = getMessage(messagelink,operation.getName());
      ArrayList parts = message.getParts();

      if (parts.size() == 1) { // this is method return
        WSDLPart part = (WSDLPart) parts.get(0);
        TypeDefinitionBase type = getPartType(part);
        environment.returnType = environment.schema.getJavaPrimitive(type);
        result.append(environment.returnType + " ");
      } else { // method is void
        result.append("void ");
      }
    } else { // return is void
      result.append("void ");
    }
    result.append(methodName + "(");
    if (input != null) {
      WSDLMessage message = getMessage(input.getMessage(),operation.getName());
      ArrayList parts = message.getParts();
      for (int i = 0; i < parts.size(); i++) {
        WSDLPart part = (WSDLPart) parts.get(i);
        TypeDefinitionBase type = getPartType(part);
        String paramType = environment.schema.getJavaPrimitive(type);
        if (i != 0) {
          result.append(", ");
        }
        result.append(paramType + " " + environment.convertor.attributeToIdentifier(part.getName()));
      }
    }
    result.append(")");
    return result.toString();
  }

  private void processExceptionComplexType(WSDLMessage message, CodeGenerator generator) throws ProxyGeneratorException {
    String className = getExceptionClassName(message,false);
    ArrayList messageParts = message.getParts();
    WSDLPart part = (WSDLPart) messageParts.get(0);
    TypeDefinitionBase partType = getPartType(part);
    String typeName = environment.schema.getJavaPrimitive(partType);
    String fieldJavaName = environment.convertor.attributeToIdentifier(part.getName());
    ArrayList fields = environment.schema.getComplexFieldInfo((ComplexTypeDefinition) partType);
    generator.clear(2);
    writePackage(generator);
    generator.addLine();
    generator.addLine("public class " + className + " extends java.lang.Exception {");
    generator.addLine("  private " + typeName + " " + fieldJavaName + ";");
    generator.addLine();
    generator.addLine("  public void init(" + typeName + " " + fieldJavaName + ") {");
    generator.addLine("    this." + fieldJavaName + "=" + fieldJavaName + ";");
    generator.addLine("  }");
    generator.addLine();
    if (fields.size() != 0) {
      generator.addLine("  public " + className + "() {");
      generator.addLine("    this." + fieldJavaName + "= new " + typeName+"();");
      generator.addLine("  }");
    }
    generator.addLine();
    generator.addLine("  public Class getContentClass() {");
    generator.addLine("    return "+typeName + ".class;");
    generator.addLine("  }");
    generator.addIndent();
    generator.add("  public " + className + "(");
    for (int i=0; i<fields.size(); i++) {
      FieldInfo field = (FieldInfo) fields.get(i);
      if (field.fieldModel != FieldInfo.FIELD_ELEMENT) {
        throw new ProxyGeneratorException(ProxyGeneratorException.WRONG_FAULT_ELEMENT,part.getName());
      }
      if (i!=0) {
        generator.add(", ");
      }
      if (field.maxOccurs !=1) {
        generator.add(field.typeJavaName+"[] "+environment.convertor.attributeToIdentifier(field.fieldLocalName));
      } else {
        generator.add(field.typeJavaName+" "+environment.convertor.attributeToIdentifier(field.fieldLocalName));
      }
    }
    generator.add(") {");
    generator.addNewLine();
    generator.addLine("    this." + fieldJavaName + "= new " + typeName+"();");
    for (int i=0; i<fields.size(); i++) {
      FieldInfo field = (FieldInfo) fields.get(i);
      generator.addLine("    this."+fieldJavaName+"."+field.getSetterMethod()+"("+environment.convertor.attributeToIdentifier(field.fieldLocalName)+");");
    }
    generator.addLine("  }");
    generator.addLine();
    for (int i=0; i<fields.size(); i++) {
      FieldInfo field = (FieldInfo) fields.get(i);
      if (field.fieldModel != FieldInfo.FIELD_ELEMENT) {
        throw new ProxyGeneratorException(ProxyGeneratorException.WRONG_FAULT_ELEMENT,part.getName());
      }
      if (field.maxOccurs != 1) {
        generator.addLine("  public " + field.typeJavaName + "[] " + field.getGetterMethod() + "() {");
        generator.addLine("    return this." + fieldJavaName + "."+field.getGetterMethod()+"();");
        generator.addLine("  }");
      } else {
        generator.addLine("  public " + field.typeJavaName + " " + field.getGetterMethod() + "() {");
        generator.addLine("    return this." + fieldJavaName + "."+field.getGetterMethod()+"();");
        generator.addLine("  }");
      }
    }
    generator.addLine("}");
    generator.addLine();
    File outFile = getNewJavaFile(environment.outputRootDir,environment.outputPackage,className);
    writeFile(generator,outFile);
  }

  private void writeFile(CodeGenerator generator, File outputFile) throws ProxyGeneratorException {
    try {
      PrintWriter output = new PrintWriter(new FileOutputStream(outputFile), true);
      output.write(generator.toString());
      output.close();
    } catch (FileNotFoundException e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.CREATE_FILE_ERROR,outputFile.getAbsolutePath());
    }
  }


  private void processExceptionSimpleType(WSDLMessage message, CodeGenerator generator) throws ProxyGeneratorException {
    String className = getExceptionClassName(message,false);
    ArrayList messageParts = message.getParts();
    WSDLPart part = (WSDLPart) messageParts.get(0);
    com.sap.engine.lib.xml.util.QName type = part.getType();
    TypeDefinitionBase partType = getPartType(part);
    String typeName = environment.schema.getJavaPrimitive(partType);
    String fieldName = part.getName(); // Returns part name
    if (part.getStyle() == WSDLPart.STRUCTURED_TYPE) { // Part links to element
      fieldName = type.getLocalName();
    }
    String fieldJavaName = environment.convertor.attributeToIdentifier(fieldName);
    String methodName = environment.convertor.attributeToMethodName("get" + environment.convertor.attributeToClassName(fieldName));
    generator.clear(2);
    writePackage(generator);
    generator.addLine();
    generator.addLine("public class " + className + " extends java.lang.Exception {");
    generator.addLine("  private " + typeName + " " + fieldJavaName + ";");
    generator.addLine();
    generator.addLine("  public " + className + "(" + typeName + " " + fieldJavaName + ") {");
    generator.addLine("    this." + fieldJavaName + "=" + fieldJavaName + ";");
    generator.addLine("  }");
    generator.addLine();
    generator.addLine("  public " + className + "() {");
    generator.addLine("  }");
    generator.addLine();
    generator.addLine("  public void init("+ typeName + " " + fieldJavaName + ") {");
    generator.addLine("    this." + fieldJavaName + "=" + fieldJavaName + ";");
    generator.addLine("  }");
    generator.addLine("  public " + typeName + " " + methodName + "() {");
    generator.addLine("    return this." + fieldJavaName + ";");
    generator.addLine("  }");
    generator.addLine("  public Class getContentClass() {");
    generator.addLine("    return "+typeName + ".class;");
    generator.addLine("  }");

    generator.addLine("}");
    generator.addLine();
    File outFile = getNewJavaFile(environment.outputRootDir, environment.outputPackage,className);
    writeFile(generator,outFile);
  }

  private void generateExceptions(ArrayList messages) throws ProxyGeneratorException {
    CodeGenerator codeGenerator = new CodeGenerator();
    for (int i=0; i<messages.size(); i++) {
      WSDLMessage message = (WSDLMessage) messages.get(i);
      WSDLPart part = message.getPart(0);
      TypeDefinitionBase type = getPartType(part);
      if (environment.schema.isSimple(type) || environment.schema.isSoapArray(type)) {
        processExceptionSimpleType(message,codeGenerator);
      } else {
        processExceptionComplexType(message,codeGenerator);
      }
    }
  }

  /**
   * Generates all
   * @param compile
   * @throws ProxyGeneratorException
   */
  public void generateAll(boolean compile) throws ProxyGeneratorException {
    generateAll(true,compile);
  }

  public void generateAll(boolean schemas, boolean compile) throws ProxyGeneratorException {
    if (schemas) {
      generateSchema();
    } else {
      prepareSchema();
    }
    if (ihandler.startGenreration(environment) == false && environment.generateException!= null) {
      if (environment.generateException instanceof  ProxyGeneratorException) {
        throw (ProxyGeneratorException) environment.generateException;
      } else {
        throw new ProxyGeneratorException(ProxyGeneratorException.GENERATION_PROBLEM,environment.generateException);
      }
    }
    ArrayList portTypes = environment.definitions.getPortTypes();
    for  (int i=0; i<portTypes.size(); i++) {
      WSDLPortType portType = (WSDLPortType) portTypes.get(i);
      if (ihandler.processPortType(portType,environment) == false && environment.generateException!= null) {
        if (environment.generateException instanceof  ProxyGeneratorException) {
          throw (ProxyGeneratorException) environment.generateException;
        } else {
          throw new ProxyGeneratorException(ProxyGeneratorException.GENERATION_PROBLEM,environment.generateException);
        }
      }
      generateExceptions(exceptionMessages);
    }
    if (ihandler.endGeneration(environment) == false && environment.generateException!= null) {
      if (environment.generateException instanceof  ProxyGeneratorException) {
        throw (ProxyGeneratorException) environment.generateException;
      } else {
        throw new ProxyGeneratorException(ProxyGeneratorException.GENERATION_PROBLEM,environment.generateException);
      }
    }
    if (compile) {
      builder.setPackageRoot(environment.outputRootDir);
      builder.setPackageName(environment.outputPackage);
      try {
        builder.compilePackage();

      } catch (Exception e) {
        throw new ProxyGeneratorException(ProxyGeneratorException.GENERATION_PROBLEM,e);
      }
    }

  }

  /**
   * Generates portal interface and empty impl from wsdl.
   * @param wsdlpath Path to wsdl
   * @param outputDir output directory
   * @param packageName output package name
   * @param compile compile output
   * @param uriToPackageMapping mapping from uri to package for generated types.
   */
  public void generatePortal(String wsdlpath, String outputDir, String packageName, boolean compile ,Hashtable uriToPackageMapping) throws WSDLException, ProxyGeneratorException {
    InterfaceGenerator generator = new InterfaceGenerator();
    WSDLDOMLoader loader = new WSDLDOMLoader();
    WSDLDefinitions definitions = loader.loadWSDLDocument(wsdlpath);
    generator.init(definitions,outputDir,packageName,new PortalHandler(),null);
    generator.generateAll(true,compile);
  }

  /**
   * Generates bean interface and empty impl from wsdl.
   * @param wsdlpath Path to wsdl
   * @param outputDir output directory
   * @param packageName output package name
   * @param compile compile output
   * @param uriToPackageMapping mapping from uri to package for generated types.
   */
  public void generateBean(String wsdlpath, String outputDir, String packageName, boolean compile ,Hashtable uriToPackageMapping) throws WSDLException, ProxyGeneratorException {
    InterfaceGenerator generator = new InterfaceGenerator();
    WSDLDOMLoader loader = new WSDLDOMLoader();
    WSDLDefinitions definitions = loader.loadWSDLDocument(wsdlpath);
    generator.init(definitions,outputDir,packageName,new BeanHandler(),null);
    generator.generateAll(true,compile);
  }

  public void generateBean(String httpProxyhost,String httpProxyport,String wsdlpath, String outputDir, String packageName, boolean compile ,Hashtable uriToPackageMapping) throws WSDLException, ProxyGeneratorException {
    InterfaceGenerator generator = new InterfaceGenerator();
    WSDLDOMLoader loader = new WSDLDOMLoader();
    loader.setHttpProxy(httpProxyhost,httpProxyport);
    WSDLDefinitions definitions = loader.loadWSDLDocument(wsdlpath);
    generator.init(definitions,outputDir,packageName,new BeanHandler(),null);
    generator.generateAll(true,compile);
  }

  public void generatePortal(String httpProxyhost,String httpProxyport,String wsdlpath, String outputDir, String packageName, boolean compile ,Hashtable uriToPackageMapping) throws WSDLException, ProxyGeneratorException {
    InterfaceGenerator generator = new InterfaceGenerator();
    WSDLDOMLoader loader = new WSDLDOMLoader();
    loader.setHttpProxy(httpProxyhost,httpProxyport);
    WSDLDefinitions definitions = loader.loadWSDLDocument(wsdlpath);
    generator.init(definitions,outputDir,packageName,new PortalHandler(),null);
    generator.generateAll(true,compile);
  }
  /*
  public static void main(String[] args) throws WSDLException, ProxyGeneratorException {
    InterfaceGenerator generator = new InterfaceGenerator();
    WSDLDOMLoader loader = new WSDLDOMLoader();
    System.out.println(System.getProperty("http.proxyHost"));
    loader.setHttpProxy("proxy","8080");
//    WSDLDefinitions definitions = loader.loadWSDLDocument("//vladimir-s/teamwork/webservices/WS-I/unzipped/Retailer.wsdl");
//    generator.init(definitions,"E:/outputDir/","check",new BeanHandler(),null);
//    generator.generateAll(true,true);
//    generator.init(definitions,"E:/outputDir/","checkPortals",new PortalHandler(),null);
//    generator.generateAll(true,true);
    WSDLDefinitions definitions = loader.loadWSDLDocument("E:/wsrp_service.wsdl");
//    generator.init(definitions,"E:/outputDir/","wsrp",new BeanHandler(),null);
//    generator.generateAll(true,true);
//    generator.environment.printInterfaces();
//    System.out.println();
    generator.init(definitions,"E:/outputDir/","wsrpPortals",new PortalHandler(),null);
    generator.setHTTPProxy("proxy","8080");
    generator.generateAll(true,true);
    generator.environment.printInterfaces();
    System.out.println();
    System.out.println(System.getProperty("http.proxyHost"));
//    definitions = loader.loadWSDLDocument("E:/r3_d_doclit.wsdl");
//    generator.init(definitions,"E:/outputDir/","r2abase",new BeanHandler(),null);
//    generator.generateAll(true,true);
//    generator.environment.printInterfaces();
//    Enumeration enumeration = generator.environment.javaToSchemaMapping.keys();
//    while (enumeration.hasMoreElements()) {
//      Object x = (Object) enumeration.nextElement();
//      System.out.println(x+" = "+generator.environment.javaToSchemaMapping.get(x));
//    }
  }
  */
}
