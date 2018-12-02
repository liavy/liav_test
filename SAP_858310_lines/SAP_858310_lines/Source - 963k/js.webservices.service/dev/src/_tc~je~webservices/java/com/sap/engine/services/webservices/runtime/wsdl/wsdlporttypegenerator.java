package com.sap.engine.services.webservices.runtime.wsdl;

import com.sap.engine.interfaces.webservices.runtime.*;
import com.sap.engine.lib.xml.util.NS;
import com.sap.engine.services.webservices.jaxrpc.java2schema.JavaToSchemaGenerator;
import com.sap.engine.services.webservices.runtime.RuntimeExceptionConstants;
import com.sap.engine.services.webservices.wsdl.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Copyright (c) 2002 by SAP Labs Sofia.,
 * All rights reserved.
 *
 * Description:
 * @author       Dimiter Angelov
 * @version      6.30
 */

public class  WSDLPortTypeGenerator {

  private static final String TARGETNAMESPACE  =  "targetNamespace";
  private static final String TNS_PREFIX  =  "tns";
  private static final String INPUTMSG_SUFFIX  =  "In";
  private static final String OUTPUTMSG_SUFFIX  =  "Out";
  private static final String REQUEST_PART_NAME  =  "parameters";
  private static final String RESPONSE_PART_NAME  =  REQUEST_PART_NAME;
  private static final String SEPARATOR  =  "_";

  public static final String NAMESPACE  =  "namespace";
  private static final String IS_QUALIFIED  =  "isQualified";
  public static final String SAP_DEFINITIONS_HINT_START_ELEMENT  =  "<sap:SAP_WSDL xmlns:sap=\"http://www.sap.com/webas\" >";
  public static final String SAP_DEFINITIONS_HINT_END_ELEMENT  =  "</sap:SAP_WSDL>";

  private static final String SOAP_REQUEST_WRAPPER = "SoapRequestWrapper";
  private static final String SOAP_RESPONSE_WRAPPER = "SoapResponseWrapper";
  //feature which presentce denotes one-way operation
  private static final String ONE_WAY_OPERATION_FEATURE  =  "http://www.sap.com/webas/630/soap/features/mep/one-way";
  //for outsideIn support indicates the name of the fault elemet wrapper
  private static final String FAULT_ELEMENT_NAME  =  "fault-element-name";

  /**
   * Generates definition with literal portType.
   */
  public static DefinitionsHolder generateDocumentLiteralPortType(JavaToQNameMappingRegistry registry, DOMSource literalSchemas[], OperationDefinition[] operations, String targetNamespace, String operationsNS, String literalPortTypeName) throws Exception {

    WSDLDefinitions definitions = new WSDLDefinitions();

    definitions.addAdditionalAttribute(TARGETNAMESPACE, targetNamespace);
//    definitions.addAdditionalAttribute("xmlns:" + TNS_PREFIX, targetNamespace); //for literal schema reference

    PrefixFactory prefixFactory = new PrefixFactory();
    prefixFactory.registerPrefix(TNS_PREFIX, targetNamespace);

    Hashtable schemas = loadSchemasAsSchemaInfoObjects(literalSchemas);

//    //appends schemas to defition
//    for (int i = 0; i < literalSchemas.length; i++) {
//      definitions.addSchema(literalSchemas[i]);
//    }

    generateDocumentPortType0(schemas, definitions, prefixFactory, registry, operations, operationsNS, literalPortTypeName);

    //adding the schemas to the definitions
    ArrayList resovedSchemas = SchemaInfo.resolveSchemas(schemas);
    for (int i = 0; i < resovedSchemas.size(); i++) {
      definitions.addSchema(new DOMSource(((SchemaInfo) resovedSchemas.get(i)).normalizeSchema()));
    }

    //mappes used prefix to namespaces
    appendNamespaces(definitions, prefixFactory);

    DefinitionsHolder holder = new DefinitionsHolder();
    holder.setDefinitions(definitions);
    holder.setPortTypeDescriptors(new PortTypeDescriptor[]{new PortTypeDescriptorImpl(PortTypeDescriptor.LITERAL_PORTTYPE, new QName(targetNamespace, literalPortTypeName))});

    return holder;

  }

  /**
   * Generates definitions with encoded portType, messages and schemas.
   */
  public static DefinitionsHolder generateRPCEncodedPortType(JavaToQNameMappingRegistry registry, DOMSource encodedSchemas[], OperationDefinition[] operations, String targetNamespace, String encodedPortTypeName) throws Exception {
    WSDLDefinitions definitions = new WSDLDefinitions();
    definitions.addAdditionalAttribute(TARGETNAMESPACE, targetNamespace);

    PrefixFactory prefixFactory = new PrefixFactory();
    prefixFactory.registerPrefix(TNS_PREFIX, targetNamespace);

    generateRPCPortType0(definitions, prefixFactory, registry, operations, encodedPortTypeName, false, null, null);

    //appends schemas to defition
    for (int i = 0; i < encodedSchemas.length; i++) {
      definitions.addSchema(encodedSchemas[i]);
    }

    //mappes used prefix to namespaces
    appendNamespaces(definitions, prefixFactory);

    DefinitionsHolder holder = new DefinitionsHolder();
    holder.setDefinitions(definitions);
    holder.setPortTypeDescriptors(new PortTypeDescriptor[]{new PortTypeDescriptorImpl(PortTypeDescriptor.ENCODED_PORTTYPE, new QName(targetNamespace, encodedPortTypeName))});

    return holder;
  }

  public static DefinitionsHolder generateRPCLiteralPortType(JavaToQNameMappingRegistry registry, DOMSource encodedSchemas[], OperationDefinition operations[], String targetNamespace, String portTypeName, String operationsNS) throws Exception {
    WSDLDefinitions definitions = new WSDLDefinitions();
    definitions.addAdditionalAttribute(TARGETNAMESPACE, targetNamespace);

    PrefixFactory prefixFactory = new PrefixFactory();
    prefixFactory.registerPrefix(TNS_PREFIX, targetNamespace);

    Hashtable schemas = loadSchemasAsSchemaInfoObjects(encodedSchemas);
    generateRPCPortType0(definitions, prefixFactory, registry, operations, portTypeName, true, operationsNS, schemas);

    //generating headers schemas
    generateHeaderParameterElements(schemas, registry.getLiteralMappings() , operations, null);

    //appends schemas to defition
    ArrayList orderedSchemas = SchemaInfo.resolveSchemas(schemas);

    for (int i = 0; i < orderedSchemas.size(); i++) {
      definitions.addSchema(new DOMSource(((SchemaInfo) orderedSchemas.get(i)).normalizeSchema()));
    }

    //mappes used prefix to namespaces
    appendNamespaces(definitions, prefixFactory);

    PortTypeDescriptorImpl portTypeDescriptor = new PortTypeDescriptorImpl(PortTypeDescriptor.RPC_LITERAL_PORTTYPE, new QName(targetNamespace, portTypeName));
    DefinitionsHolder holder = new DefinitionsHolder(definitions, new PortTypeDescriptor[]{portTypeDescriptor});
    return holder;

  }

  public static DefinitionsHolder generateHTTPPortType(JavaToQNameMappingRegistry registry, DOMSource literalSchemas[], OperationDefinition operations[], String targetNamespace, String operationsNS, String portTypeName) throws Exception {
    WSDLDefinitions definitions = new WSDLDefinitions();
    definitions.addAdditionalAttribute(TARGETNAMESPACE, targetNamespace);

    PrefixFactory prefixFactory = new PrefixFactory();
    prefixFactory.registerPrefix(TNS_PREFIX, targetNamespace);

    WSDLPortType portType = new WSDLPortType();
    portType.setName(portTypeName);

    JavaToQNameMappings javaToQNameMappings = registry.getLiteralMappings();

    OperationDefinition tempOperation;
    WSDLMessage tempMessage;
    WSDLOperation wsdlOperation;
    String inputMessageName;
    String outputMessageName;
    String namespace;
    for (int i = 0; i < operations.length; i++) {
      tempOperation = operations[i];
      //input messge generation
      inputMessageName = getInputEncodedMessageName(portTypeName, tempOperation);
      tempMessage = BaseWSDLComponentGenerator.generateRPCEncodedMessage(inputMessageName, tempOperation.getInputParameters(), javaToQNameMappings, prefixFactory, false);
      definitions.addMessage(tempMessage);

      //output message generation
      outputMessageName = getOutputEncodedMessageName(portTypeName, tempOperation);
      Config outputCnf = tempOperation.getOutputConfiguration();
      try {
        namespace = outputCnf.getProperty(NAMESPACE).getValue();
      } catch (NullPointerException e) {
        namespace = operationsNS;
      }

      tempMessage = BaseWSDLComponentGenerator.generateDocumentLiteralMessage(outputMessageName,
                                                                              RESPONSE_PART_NAME, new QName(targetNamespace, getOutputElementName(tempOperation)), prefixFactory.getPrefix(namespace));
      definitions.addMessage(tempMessage);
      //operation creation
      wsdlOperation = BaseWSDLComponentGenerator.generatePortTypeOperation(tempOperation.getOperationName(), inputMessageName, outputMessageName, targetNamespace, TNS_PREFIX);
      //Adding operation to portType
      portType.addOperation(wsdlOperation);
    }

    //Adding port?Type to definitions
    definitions.addPortType(portType);

//    //adding typesschemas
//    for (int i = 0; i < literalSchemas.length; i++) {
//      definitions.addSchema(literalSchemas[i]);
//    }

    Hashtable schemasTable = loadSchemasAsSchemaInfoObjects(literalSchemas);

    //generation and addition of schemas to the definition
    generateLiteralElementSchemas(schemasTable, javaToQNameMappings, operations, operationsNS, false);

    //adding the schemas to the definitions
    ArrayList resovedSchemas = SchemaInfo.resolveSchemas(schemasTable);
    for (int i = 0; i < resovedSchemas.size(); i++) {
      definitions.addSchema(new DOMSource(((SchemaInfo) resovedSchemas.get(i)).normalizeSchema()));
    }

    PortTypeDescriptorImpl portTypeDescriptor = new PortTypeDescriptorImpl(PortTypeDescriptor.HTTP_PORTTYPE, new QName(targetNamespace, portTypeName));

    //mappes used prefix to namespaces
    appendNamespaces(definitions, prefixFactory);

    DefinitionsHolder holder = new DefinitionsHolder(definitions, new PortTypeDescriptor[]{portTypeDescriptor});
    return holder;
  }

  /**
   * Generates schemas element with all input, output and fault elements for literal mode.
   * The schemas' namespaces are taken from the Config namespace property.
   */
  public static void generateLiteralElementSchemas(Hashtable schemas, JavaToQNameMappings literalMappings, OperationDefinition operations[], String operationsDefaultNS, boolean generateInputAndFault) throws Exception {
    Element schemaElement = null;
    SchemaInfo schInfo;
    OperationDefinition operation;
    String namespace;
    Element baseElement, complexType, sequence, tempElement;
    String paramName, paramClassName, tempPrefix;
    QName tempQName;

    //generate the headers elements.
    generateHeaderParameterElements(schemas, literalMappings, operations, operationsDefaultNS);

    boolean isQualified = false;
    if (operations.length > 0) {
      isQualified = Boolean.valueOf(operations[0].getGeneralConfiguration().getProperty(IS_QUALIFIED).getValue().trim()).booleanValue();
    }
    for (int i = 0; i < operations.length; i++) {
      operation = operations[i];

      if (generateInputAndFault) { //for HTTP wsdl generation
        //input element creation
        Config inputCnf = operation.getInputConfiguration();
        try {
          namespace = inputCnf.getProperty(NAMESPACE).getValue();
        } catch (NullPointerException e) {
          namespace = operationsDefaultNS;
        }
        schInfo = (SchemaInfo) schemas.get(namespace);
        if (schInfo == null) {
          schInfo = new SchemaInfo(namespace, isQualified);
          schemas.put(namespace, schInfo);
        }
        schemaElement = schInfo.getSchemaElement();

        baseElement = schemaElement.getOwnerDocument().createElementNS(NS.XS, JavaToSchemaGenerator.SCHEMA_ELEMENT);
        complexType = schemaElement.getOwnerDocument().createElementNS(NS.XS, JavaToSchemaGenerator.SCHEMA_COMPLEXTYPE);
        sequence = schemaElement.getOwnerDocument().createElementNS(NS.XS, JavaToSchemaGenerator.SCHEMA_SEQUENCE);

        String inputOpElem = getInputElementName(operation);
        baseElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_NAME, inputOpElem);

        if (isSchemaElementAvailable(schemaElement, inputOpElem)) {
          throw new WSDLCreationException(RuntimeExceptionConstants.OPERATION_ELEMENT_ALREADY_IN_SCHEMA,
                  new Object[]{"request", namespace, inputOpElem});
        }


        ParameterNode params[] = operation.getInputParameters();

        for (int j = 0; j < params.length; j++) {
          if (! params[j].isExposed() || params[j].isHeader()) { //it is not exposed in the schema or it is header
            continue;
          }
          paramName = params[j].getParameterName();
          paramClassName = params[j].getJavaClassName();

          tempQName = literalMappings.getMappedQName(paramClassName);

          if (tempQName == null) {
            throw new WSDLCreationException(RuntimeExceptionConstants.CANNOT_FIND_QNAME_FOR_CLASS, new Object[]{paramClassName, literalMappings});
          }

          tempPrefix = schInfo.getPrefixForUri(tempQName.getNamespaceURI());

          tempElement = schemaElement.getOwnerDocument().createElementNS(NS.XS, JavaToSchemaGenerator.SCHEMA_ELEMENT);
          tempElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_NAME, paramName);
          tempElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_TYPE, tempPrefix + ":" + tempQName.getLocalPart());

          //in case it is optional
          if (params[j].isOptional()) {
            tempElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_MINOCCURS, "0");
          }

          //in case it is standard and Nillable
          if (StandardTypes.isStandardType(paramClassName) ) {
            if (StandardTypes.isNillableStandardType(paramClassName)) {
              tempElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_NILLABLE, "true");
            }
          } else {
            tempElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_NILLABLE, "true");
          }

          sequence.appendChild(tempElement);
        }
        complexType.appendChild(sequence);
        baseElement.appendChild(complexType);
        schemaElement.appendChild(baseElement);
      }

      //response element creation
      //in case it is one way operation miss the output element creation
      if (isOneWay(operation)) {
        continue;
      }

      Config outputCnf = operation.getOutputConfiguration();
      try {
        namespace = outputCnf.getProperty(NAMESPACE).getValue();
      } catch (NullPointerException e) {
          namespace = operationsDefaultNS;
      }
      schInfo = (SchemaInfo) schemas.get(namespace);
      if (schInfo == null) {
        schInfo = new SchemaInfo(namespace, isQualified);
        schemas.put(namespace, schInfo);
      }
      schemaElement = schInfo.getSchemaElement();


      String outOpElem = getOutputElementName(operation);
      baseElement = schemaElement.getOwnerDocument().createElementNS(NS.XS, JavaToSchemaGenerator.SCHEMA_ELEMENT);
      baseElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_NAME, outOpElem);
      complexType = schemaElement.getOwnerDocument().createElementNS(NS.XS, JavaToSchemaGenerator.SCHEMA_COMPLEXTYPE);
      sequence = schemaElement.getOwnerDocument().createElementNS(NS.XS,  JavaToSchemaGenerator.SCHEMA_SEQUENCE);

      if (isSchemaElementAvailable(schemaElement, outOpElem)) {
        throw new WSDLCreationException(RuntimeExceptionConstants.OPERATION_ELEMENT_ALREADY_IN_SCHEMA,
                new Object[]{"response", namespace, outOpElem});
      }

      ParameterNode outputParams[] = operation.getOutputParameters();

      if (outputParams.length > 0) {
        if (outputParams.length != 1) {
          throw new WSDLCreationException(RuntimeExceptionConstants.VI_FUNCTION_OUT_PARAM_NUMBER, new Object[]{new Integer(outputParams.length), operation.getOperationName()});
        }

        paramName = outputParams[0].getParameterName();
        paramClassName = outputParams[0].getJavaClassName();

        tempElement = schemaElement.getOwnerDocument().createElementNS(NS.XS, JavaToSchemaGenerator.SCHEMA_ELEMENT);
        tempElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_NAME, paramName);

        if (paramClassName == null || paramClassName.length() == 0 || paramClassName.equals(Void.TYPE.getName())) {
          tempElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_MINOCCURS, "0");
          Element complexTypeElementInner = schemaElement.getOwnerDocument().createElementNS(NS.XS, JavaToSchemaGenerator.SCHEMA_COMPLEXTYPE);
          Element sequenceInner = schemaElement.getOwnerDocument().createElementNS(NS.XS, SchemaConvertor.SCHEMA_SEQUENCE);
          complexTypeElementInner.appendChild(sequenceInner);
          tempElement.appendChild(complexTypeElementInner);
        } else {
          tempQName = literalMappings.getMappedQName(paramClassName);
          if (tempQName == null) {
            throw new WSDLCreationException(RuntimeExceptionConstants.CANNOT_FIND_QNAME_FOR_CLASS, new Object[]{paramClassName, literalMappings});
          }

          tempPrefix = schInfo.getPrefixForUri(tempQName.getNamespaceURI());
          tempElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_TYPE, tempPrefix + ":" + tempQName.getLocalPart());

          //in case it is standard and Nillable
          if (StandardTypes.isStandardType(paramClassName) ) {
            if (StandardTypes.isNillableStandardType(paramClassName)) {
              tempElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_NILLABLE, "true");
            }
          } else {
            tempElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_NILLABLE, "true");
          }
        }
        sequence.appendChild(tempElement);
      }

      complexType.appendChild(sequence);
      baseElement.appendChild(complexType);
      schemaElement.appendChild(baseElement);

      if (generateInputAndFault) {
        //fault element creation
        documentFaultsGeneration(operation, operationsDefaultNS, schemas, literalMappings);
      }
    } //for end

  }

  private static void documentFaultsGeneration(OperationDefinition operation, String operationsDefaultNS, Hashtable schemas,  JavaToQNameMappings literalMappings) throws Exception {

    Fault[] faults = operation.getFaults();
    Config faultConfig;
    Fault cFault;
    String namespace, tempPrefix;
    QName tempQName;
    SchemaInfo schInfo;
    Element schemaElement, baseElement;

    boolean isQualified = Boolean.valueOf(operation.getGeneralConfiguration().getProperty(IS_QUALIFIED).getValue().trim()).booleanValue();
    if (faults != null) {
      for (int j = 0; j < faults.length; j++) {
        cFault = faults[j];
        faultConfig = cFault.getFaultConfiguration();
        try {
          namespace = faultConfig.getProperty(NAMESPACE).getValue();
        } catch (NullPointerException e) {
          namespace = operationsDefaultNS;
        }
        schInfo = (SchemaInfo) schemas.get(namespace);
        if (schInfo == null) {
          schInfo = new SchemaInfo(namespace, isQualified);
          schemas.put(namespace, schInfo);
        }
        schemaElement = schInfo.getSchemaElement();

        tempQName = literalMappings.getMappedQName(cFault.getJavaClassName());
        if (tempQName == null) {
          throw new WSDLCreationException(RuntimeExceptionConstants.CANNOT_FIND_QNAME_FOR_CLASS, new Object[]{cFault.getJavaClassName(), literalMappings});
        }

        //in case the element is already added
        if (isElementAlreadyAdded(generateFaultElementName(operation, cFault), tempQName, schInfo, "faults")) {
          continue;
        }

        tempPrefix = schInfo.getPrefixForUri(tempQName.getNamespaceURI());

        baseElement = schemaElement.getOwnerDocument().createElementNS(NS.XS, JavaToSchemaGenerator.SCHEMA_ELEMENT);
        baseElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_NAME, generateFaultElementName(operation, cFault));
        baseElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_TYPE, tempPrefix + ":" + tempQName.getLocalPart());
        schemaElement.appendChild(baseElement);
      }
    }
  }


//=================Private methods
  private static Hashtable loadSchemasAsSchemaInfoObjects(DOMSource[] schemas) throws WSDLCreationException {
    Hashtable result = new Hashtable(schemas.length);
    SchemaInfo tmpSchema;

    for (int i = 0; i < schemas.length; i++) {
      tmpSchema = new SchemaInfo((Element) schemas[i].getNode().cloneNode(true));
      if (result.put(tmpSchema.targetNamespace, tmpSchema) != null) {
        throw new WSDLCreationException(RuntimeExceptionConstants.DUBLICATE_SCHEMA_URIES, new Object[]{tmpSchema.targetNamespace});
      }
    }

    return result;
  }

  private static void generateHeaderParameterElements(Hashtable schemas, JavaToQNameMappings literalMappings, OperationDefinition operations[], String operationsDefaultNS) throws Exception{
    String headerElNS;
    QName tempQName;
    SchemaInfo schInfo;
    ParameterNode[] inputParams;
    Element schemaElement, headerElement;
    String tempPrefix;
    String defaultNS;

    boolean isQualified = false;
    if (operations.length > 0) {
      isQualified = Boolean.valueOf(operations[0].getGeneralConfiguration().getProperty(IS_QUALIFIED).getValue().trim()).booleanValue();
    }
    for (int i = 0; i < operations.length; i++) {
      inputParams = operations[i].getInputParameters();

      try {
        defaultNS = operations[i].getInputConfiguration().getProperty(NAMESPACE).getValue();
      } catch (NullPointerException e) {
        defaultNS = operationsDefaultNS;
      }

      for (int p = 0; p < inputParams.length; p++) {
        //in case it is not header param miss it.
//        System.out.println("WSDLPortTypeGenerator parameter: " + inputParams[p].getParameterName() + " " + inputParams[p].isHeader());
        if ((! inputParams[p].isHeader()) || (! inputParams[p].isExposed())) {
          continue;
        }

        headerElNS = inputParams[p].getHeaderElementNamespace();
        if (headerElNS == null) {
          headerElNS = defaultNS;
        }

        schInfo = (SchemaInfo) schemas.get(headerElNS);
        if (schInfo == null) {
          schInfo = new SchemaInfo(headerElNS, isQualified);
          schemas.put(headerElNS, schInfo);
        }

        schemaElement = schInfo.getSchemaElement();

        tempQName = literalMappings.getMappedQName(inputParams[p].getJavaClassName());
        if (tempQName == null) {
          throw new WSDLCreationException(RuntimeExceptionConstants.CANNOT_FIND_QNAME_FOR_CLASS, new Object[]{inputParams[p].getJavaClassName(), literalMappings});
        }

        //in case header with same name and type is generated do not add it again.
        if (isElementAlreadyAdded(inputParams[p].getParameterName(), tempQName, schInfo, "headers")) {
          continue;
        }

        //prefix for the type
        tempPrefix = schInfo.getPrefixForUri(tempQName.getNamespaceURI());
        headerElement = schemaElement.getOwnerDocument().createElementNS(NS.XS, JavaToSchemaGenerator.SCHEMA_ELEMENT);
        headerElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_NAME, inputParams[p].getParameterName());
        headerElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_TYPE, tempPrefix + ":" + tempQName.getLocalPart());

//        //in case it is optional
//        if (params[j].isOptional()) {
//          tempElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_MINOCCURS, "0");
//        }

        //in case it is standard and Nillable
        if (StandardTypes.isStandardType(inputParams[p].getJavaClassName())) {
          if (StandardTypes.isNillableStandardType(inputParams[p].getJavaClassName())) {
            headerElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_NILLABLE, "true");
          }
        } else {
          headerElement.setAttributeNS("", JavaToSchemaGenerator.SCHEMA_NILLABLE, "true");
        }

        schemaElement.appendChild(headerElement);
      }
    }
  }

  private static WSDLPortType generateDocumentPortType0(Hashtable schemas, WSDLDefinitions definitions, PrefixFactory prefixFactory, JavaToQNameMappingRegistry registry, OperationDefinition[] operations, String operationsDefaultNS, String literalPortTypeName) throws Exception {

    WSDLPortType portType = new WSDLPortType();
    portType.setName(literalPortTypeName);

    OperationDefinition tempOperation;
    WSDLMessage tempMessage;
    WSDLOperation wsdlOperation;
    String inputMessageName;
    String outputMessageName;

    //Adding literal schemas
    generateLiteralElementSchemas(schemas, registry.getLiteralMappings(), operations, operationsDefaultNS, true);
//    for (int i = 0; i < elementSchemas.length; i++) {
//      definitions.addSchema(elementSchemas[i]);
//    }

    String namespace;

    for (int i = 0; i < operations.length; i++) {
      tempOperation = operations[i];
      outputMessageName = null;
      //input message generation
      inputMessageName = getInputLiteralMessageName(literalPortTypeName, tempOperation);
      try {
        namespace = tempOperation.getInputConfiguration().getProperty(NAMESPACE).getValue();
      } catch (NullPointerException e) {
        namespace = operationsDefaultNS;
      }

      tempMessage = BaseWSDLComponentGenerator.generateDocumentLiteralMessage(inputMessageName,
                                                                              REQUEST_PART_NAME, new QName(namespace, getInputElementName(tempOperation)), prefixFactory.getPrefix(namespace));
      //adding header parts
      tempMessage = appendHeaderParts(tempMessage,  tempOperation.getInputParameters(), namespace, prefixFactory);
      definitions.addMessage(tempMessage);

      //in case the operation is one-way no output and fault creation
      if (! isOneWay(tempOperation)) {
        //output message generation
        outputMessageName = getOutputLiteralMessageName(literalPortTypeName, tempOperation);
        try {
          namespace = tempOperation.getOutputConfiguration().getProperty(NAMESPACE).getValue();
        } catch (NullPointerException nE) {
          namespace = operationsDefaultNS;
        }

        tempMessage = BaseWSDLComponentGenerator.generateDocumentLiteralMessage(outputMessageName,
                                                                                RESPONSE_PART_NAME, new QName(namespace, getOutputElementName(tempOperation)), prefixFactory.getPrefix(namespace));
        definitions.addMessage(tempMessage);
      }

      //operation generation
      wsdlOperation = BaseWSDLComponentGenerator.generatePortTypeOperation(tempOperation.getOperationName(), inputMessageName, outputMessageName, null, TNS_PREFIX);
      portType.addOperation(wsdlOperation);

      //fault generation
      //in case the operation is one-way no output and fault creation
      if (! isOneWay(tempOperation)) {
        Fault[] faults = tempOperation.getFaults();
        for (int k = 0; k < faults.length; k++) {
          String messageName = generateLiteralFaultMessageName(literalPortTypeName, tempOperation, faults[k].getFaultName());
          String elementName = generateFaultElementName(tempOperation, faults[k]);
          try {
            namespace = faults[k].getFaultConfiguration().getProperty(NAMESPACE).getValue();
          } catch (NullPointerException nE) {
            namespace = operationsDefaultNS;
          }

          tempMessage = BaseWSDLComponentGenerator.generateLiteralFaultMessage(messageName, prefixFactory.getPrefix(namespace), elementName);
          definitions.addMessage(tempMessage);
          WSDLFault fault = BaseWSDLComponentGenerator.generateFaultChannel(faults[k].getFaultName(), TNS_PREFIX, messageName);
          wsdlOperation.addFault(fault);
        }
      }
    }

    definitions.addPortType(portType);
    return portType;
  }

  private static WSDLPortType generateRPCPortType0(WSDLDefinitions definitions, PrefixFactory prefixFactory, JavaToQNameMappingRegistry registry, OperationDefinition[] operations, String encodedPortTypeName, boolean isLiteral, String operationsNS, Hashtable schemas) throws Exception {

    WSDLPortType portType = new WSDLPortType();
    portType.setName(encodedPortTypeName);

    JavaToQNameMappings javaToQNameMappings;
    if (isLiteral) {//in case of rpc/literal
      javaToQNameMappings = registry.getLiteralMappings();
    } else {
      javaToQNameMappings = registry.getEncodedMappings();
    }

    OperationDefinition tempOperation;
    WSDLMessage tempMessage;
    WSDLOperation wsdlOperation;
    String inputMessageName;
    String outputMessageName;
    String faultMessageName;


    for (int i = 0; i < operations.length; i++) {
      tempOperation = operations[i];
      //in case of rpc_enc bypass operations with headers
      if ((! isLiteral) && isOperationWithHeaders(tempOperation)) {
        continue;
      }

      outputMessageName = null;
      //input messge generation
      inputMessageName = getInputEncodedMessageName(encodedPortTypeName, tempOperation);
      tempMessage = BaseWSDLComponentGenerator.generateRPCEncodedMessage(inputMessageName, tempOperation.getInputParameters(), javaToQNameMappings, prefixFactory, isLiteral);
      //adding header elements in the message
      if (isLiteral) {
        String headersNS;
        try {
          headersNS = tempOperation.getInputConfiguration().getProperty(NAMESPACE).getValue();
        } catch (NullPointerException e) {
          headersNS = operationsNS;
        }
        tempMessage = appendHeaderParts(tempMessage, tempOperation.getInputParameters(),  headersNS, prefixFactory);
      }

      definitions.addMessage(tempMessage);

      //output message generation
      if (! isOneWay(tempOperation)) {
        outputMessageName = getOutputEncodedMessageName(encodedPortTypeName, tempOperation);
        tempMessage = BaseWSDLComponentGenerator.generateRPCEncodedMessage(outputMessageName, tempOperation.getOutputParameters(), javaToQNameMappings, prefixFactory, isLiteral);
        definitions.addMessage(tempMessage);
      }

      //operation creation
      wsdlOperation = BaseWSDLComponentGenerator.generatePortTypeOperation(tempOperation.getOperationName(), inputMessageName, outputMessageName, null, TNS_PREFIX);
      //fault message generation
      if (! isOneWay(tempOperation)) {
        Fault[] faults = tempOperation.getFaults();
        if (faults != null) {
          if (isLiteral) { //in case of rpc-literal
            //addition of the fault elements in the schemas
            documentFaultsGeneration(tempOperation, operationsNS, schemas, javaToQNameMappings);
            String namespace;
            for (int k = 0; k < faults.length; k++) {
              String messageName = generateLiteralFaultMessageName(encodedPortTypeName, tempOperation, faults[k].getFaultName());
              String elementName = generateFaultElementName(tempOperation, faults[k]);
              try {
                namespace = faults[k].getFaultConfiguration().getProperty(NAMESPACE).getValue();
              } catch (NullPointerException nE) {
                namespace = operationsNS;
              }

              tempMessage = BaseWSDLComponentGenerator.generateLiteralFaultMessage(messageName, prefixFactory.getPrefix(namespace), elementName);
              definitions.addMessage(tempMessage);
              WSDLFault fault = BaseWSDLComponentGenerator.generateFaultChannel(faults[k].getFaultName(), TNS_PREFIX, messageName);
              wsdlOperation.addFault(fault);
            }
          } else {
            for (int k = 0; k < faults.length; k++) {
              faultMessageName = generateEncodedFaultMessageName(encodedPortTypeName, tempOperation, faults[k].getFaultName());
              String faultPartName = generateFaultElementName(tempOperation, faults[k]);
              tempMessage = BaseWSDLComponentGenerator.generateEncodedFaultMessage(faultMessageName, javaToQNameMappings, prefixFactory, faults[k], faultPartName);
              definitions.addMessage(tempMessage);
              WSDLFault wsdlFault = BaseWSDLComponentGenerator.generateFaultChannel(faults[k].getFaultName(), TNS_PREFIX, faultMessageName);
              wsdlOperation.addFault(wsdlFault);
            }
          }
        }
      }
      //Adding operation to portType
      portType.addOperation(wsdlOperation);
    }
    //Adding port?Type to definitions
    definitions.addPortType(portType);

    return portType;
  }

  private static WSDLMessage appendHeaderParts(WSDLMessage message, ParameterNode[] inputParameterNodes, String defaultNS, PrefixFactory factory) throws WSDLException{
    String prefix;
    WSDLPart part;
    String ns;

//    if (! isLiteral) {
//      return message;
//    }

//    System.out.println("WSDLPortTypeGenerator appendHeaders to message: " + message.getName());
    for (int i = 0; i < inputParameterNodes.length; i++) {
      if (inputParameterNodes[i].isHeader() && inputParameterNodes[i].isExposed()) {
        ns = inputParameterNodes[i].getHeaderElementNamespace();
        if (ns == null) {
          ns = defaultNS;
        }
//        System.out.println("WSDLPortTypeGenerator append part to message: " + inputParameterNodes[i].getParameterName());
        prefix = factory.getPrefix(ns);
        part = new WSDLPart();
        part.setName(inputParameterNodes[i].getParameterName() + "_" + i);
        part.setType(WSDLPart.STRUCTURED_TYPE, new com.sap.engine.lib.xml.util.QName(prefix, inputParameterNodes[i].getParameterName(), ns));
        message.addPart(part);
      }
    }

    return message;
  }

  private static String getInputEncodedMessageName(String portTypeName, OperationDefinition operation) {
    return operation.getOperationName() + INPUTMSG_SUFFIX;
  }

  private static String getOutputEncodedMessageName(String portTypeName, OperationDefinition operation) {
    return operation.getOperationName() + OUTPUTMSG_SUFFIX;
  }

  private static String getInputLiteralMessageName(String portTypeName, OperationDefinition operationDefinition) {
    return getInputEncodedMessageName(portTypeName, operationDefinition) + "_doc";
  }

  private static String getOutputLiteralMessageName(String portTypeName, OperationDefinition operationDefinition) {
    return getOutputEncodedMessageName(portTypeName, operationDefinition) + "_doc";
  }

  private static String getInputElementName(OperationDefinition operationDefinition) {
    Config cfg = operationDefinition.getGeneralConfiguration();
    if (cfg != null) {
      String soapReqestWrap = (cfg.getProperty(SOAP_REQUEST_WRAPPER) != null) ? cfg.getProperty(SOAP_REQUEST_WRAPPER).getValue() : null;
      if (soapReqestWrap != null) {
        return soapReqestWrap;
      }
    }

    return operationDefinition.getOperationName();
  }

  private static String getOutputElementName(OperationDefinition operationDefinition) {
    Config cfg = operationDefinition.getGeneralConfiguration();
    if (cfg != null) {
      String soapRespWrap = (cfg.getProperty(SOAP_RESPONSE_WRAPPER) != null) ? cfg.getProperty(SOAP_RESPONSE_WRAPPER).getValue() : null;
      if (soapRespWrap != null) {
        return soapRespWrap;
      }
    }

    return operationDefinition.getOperationName() + "Response";
  }

  private static String generateEncodedFaultMessageName(String portTypeName, OperationDefinition operationDefinition, String operationFault) {
    return operationDefinition.getOperationName() + SEPARATOR + operationFault;
  }

  private static String generateLiteralFaultMessageName(String portTypeName, OperationDefinition operationDefinition, String operationFault) {
    return generateEncodedFaultMessageName(portTypeName, operationDefinition, operationFault) + "_doc";
  }

  private static String generateFaultElementName(OperationDefinition operation, Fault fault) {
    String faultElementName = null;
    Config config = fault.getFaultConfiguration();
    if (config.getProperty(FAULT_ELEMENT_NAME) != null) {
      faultElementName = config.getProperty(FAULT_ELEMENT_NAME).getValue();
    }

    if (faultElementName == null) {
      faultElementName = operation.getOperationName() + SEPARATOR + fault.getFaultName();
    }

    return faultElementName;
  }

  private static WSDLDefinitions appendNamespaces(WSDLDefinitions definitions, PrefixFactory factory) {
    Hashtable mappings = factory.getMappings();
    Enumeration uris = mappings.keys();

    String tempPrefix;
    String tempUri;

    while (uris.hasMoreElements()) {
      tempUri = (String) uris.nextElement();
      tempPrefix = (String) mappings.get(tempUri);
      definitions.addAdditionalAttribute("xmlns:" + tempPrefix, tempUri);
    }

    return definitions;
  }

  //searches for one-way feature
  private static boolean isOneWay(OperationDefinition opD) {
    Feature fs[] = opD.getFeatures();
    if (fs != null) {
      for (int i = 0; i < fs.length; i++) {
        if (fs[i].getFeatureName().equals(ONE_WAY_OPERATION_FEATURE)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean isOperationWithHeaders(OperationDefinition opD) {
    ParameterNode[] params = opD.getInputParameters();
    for (int i = 0; i < params.length; i++) {
      if (params[i].isHeader() && params[i].isExposed()) { //in case of showed header only
        return true;
      }
    }

    return false;
  }

  //faultString is used in exceptions to say for header of fault or whatelse element is searched
  private static boolean isElementAlreadyAdded(String headerName, QName headerType, SchemaInfo schema, String faultString) throws WSDLCreationException {
    Element schemaEl = schema.getSchemaElement();
    NodeList list = schemaEl.getElementsByTagName(SchemaConvertor.SCHEMA_ELEMENT);
    Element curr;
    String typeAttr, uriPrefix, typeName, tmpUriPrefix;
    int pos, i;
    for (i = 0; i < list.getLength(); i++) {
      curr = (Element) list.item(i);
      if (curr.getAttribute(SchemaConvertor.SCHEMA_NAME).equals(headerName)) {
        typeAttr = curr.getAttribute(SchemaConvertor.SCHEMA_TYPE);
        pos = typeAttr.indexOf(":");
        uriPrefix = typeAttr.substring(0, pos);
        typeName = typeAttr.substring(pos + 1, typeAttr.length());

        if (typeName.equals(headerType.getLocalPart())) {
          tmpUriPrefix = schema.getPrefixForUri(headerType.getNamespaceURI());
          if (tmpUriPrefix.equals(uriPrefix)) {
            return true;
          } else {
            throw new WSDLCreationException(RuntimeExceptionConstants.ELEMENTS_WITH_EQUAL_NAMES_BUT_DIFFERENT_TYPES,
                        new Object[]{faultString, schema.getTargetNamespace(), headerName, headerType.getNamespaceURI(), typeName, headerType.getLocalPart()});
          }
        } else {
          throw new WSDLCreationException(RuntimeExceptionConstants.ELEMENTS_WITH_EQUAL_NAMES_BUT_DIFFERENT_TYPES,
                      new Object[]{faultString, schema.getTargetNamespace(), headerName, headerType.getNamespaceURI(), typeName, headerType.getLocalPart()});
        }
      }
    }

    return false;
  }

  private static boolean isSchemaElementAvailable(Element schemaElement, String newElementName) {
    NodeList nodes = schemaElement.getElementsByTagName(SchemaConvertor.SCHEMA_ELEMENT);
    for (int i = 0; i < nodes.getLength(); i++) {
      if (((Element) nodes.item(i)).getAttribute(SchemaConvertor.SCHEMA_NAME).equals(newElementName)) {
          return true;
      }
    }

    return false;
  }

}