/*
 * Created on 2005-11-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.dynamic.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sap.engine.services.webservices.espbase.client.dynamic.DDocumentable;
import com.sap.engine.services.webservices.espbase.client.dynamic.DGenericService;
import com.sap.engine.services.webservices.espbase.client.dynamic.DInterface;
import com.sap.engine.services.webservices.espbase.client.dynamic.DOperation;
import com.sap.engine.services.webservices.espbase.client.dynamic.DParameter;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAnnotateable;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAnnotation;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAny;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAppInfo;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DAttribute;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DBaseType;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DComplexType;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DDocumentation;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DElement;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DField;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DGroup;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DNamedNode;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DOccurrenceable;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DSimpleContent;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DSimpleType;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.DStructure;
import com.sap.engine.services.webservices.espbase.client.dynamic.types.Facet;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaMappingConstants;
import com.sap.engine.services.webservices.tools.SharedTransformers;

/**
 * @author ivan-m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MetadataVisualPanel extends JPanel {
  
  private JScrollPane treeVisualizationScrollPane;
  
  public MetadataVisualPanel() {
    setLayout(new GridBagLayout());
    Insets insets = new Insets(5, 5, 0, 5);
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.insets = insets;
    gridBagConstraints.anchor = GridBagConstraints.WEST;
    add(new JLabel("Metadata:"), gridBagConstraints);
    
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = GridBagConstraints.CENTER;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    insets.bottom = 5;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    treeVisualizationScrollPane = new JScrollPane();
    add(treeVisualizationScrollPane, gridBagConstraints);
  }
  
  protected void visualize(DGenericService service) {
    JTree visualizationTree = new JTree(createServiceNode(service));
    visualizationTree.setVisible(true);
    treeVisualizationScrollPane.getViewport().add(visualizationTree);
    treeVisualizationScrollPane.repaint();
  }
  
  private DefaultMutableTreeNode createServiceNode(DGenericService service) {
    DefaultMutableTreeNode serviceNode = new DefaultMutableTreeNode("service");
    serviceNode.add(createTypeMappingNode(service.getTypeMetadata()));
    serviceNode.add(createInterfacesNode(service));
    serviceNode.add(createDocumentationNode(service));
    return(serviceNode);
  }
  
  private DefaultMutableTreeNode createDocumentationNode(DDocumentable documentable) {
    return(new DefaultMutableTreeNode("documentation : " + createDocumentationContent(documentable.getDocumentationElement())));
  }
  
  private String createDocumentationContent(Element content) {
    if(content != null) {
      ByteArrayOutputStream xmlByteArrayOutput = new ByteArrayOutputStream(); 
      try {
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(content), new StreamResult(xmlByteArrayOutput));
      } catch(Exception exc) {
        return(null);
      } finally {
        try {
          xmlByteArrayOutput.close();
        } catch(IOException ioExc) {
          //$JL-EXC$
          //nothing to do
        }
      }
      return(new String(xmlByteArrayOutput.toByteArray())); //$JL-I18N$
    }
    return(null);
  }
  
  private DefaultMutableTreeNode createInterfacesNode(DGenericService service) {
    DefaultMutableTreeNode interfacesNode = new DefaultMutableTreeNode("interfaces");
    QName[] portNames = service.getInterfaces();
    for(int i = 0; i < portNames.length; i++) {
      DInterface _interface = service.getInterfaceMetadata(portNames[i]);
      interfacesNode.add(createInterfaceNode(_interface));
    }
    return(interfacesNode);
  }
  
  private DefaultMutableTreeNode createInterfaceNode(DInterface _interface) {
    DefaultMutableTreeNode interfaceNode = new DefaultMutableTreeNode("interface : " + _interface.getInterfaceName());
    interfaceNode.add(createInterfaceLPortsNode(_interface));
    interfaceNode.add(new DefaultMutableTreeNode("interface name : " + _interface.getInterfaceName()));
    interfaceNode.add(createDocumentationNode(_interface));
    interfaceNode.add(createOperationsNode(_interface));
    return(interfaceNode);
  }
  
  private DefaultMutableTreeNode createInterfaceLPortsNode(DInterface _interface) {
    DefaultMutableTreeNode interfaceLPortsNode = new DefaultMutableTreeNode("logical ports");
    QName[] lPortNames = _interface.getPortNames();
    for(int i = 0; i < lPortNames.length; i++) {
      QName lportName = lPortNames[i];
      interfaceLPortsNode.add(new DefaultMutableTreeNode("logical port : " + lportName));
    }
    return(interfaceLPortsNode);
  }
  
  private DefaultMutableTreeNode createOperationsNode(DInterface _interface) {
    DefaultMutableTreeNode operationsNode = new DefaultMutableTreeNode("operations");
    DOperation[] operations = _interface.getOperations();
    for(int i = 0; i < operations.length; i++) {
      DOperation operation = operations[i];
      operationsNode.add(createOperationNode(operation));
    }
    return(operationsNode);
  }
  
  private DefaultMutableTreeNode createOperationNode(DOperation operation) {
    DefaultMutableTreeNode operationNode = new DefaultMutableTreeNode("operation : " + operation.getName());
    operationNode.add(createInputParamsNode(operation));
    operationNode.add(createOutputParamsNode(operation));
    operationNode.add(createInOutParamsNode(operation));
    operationNode.add(createFaultParamsNode(operation));
    operationNode.add(createReturnParamNode(operation));
    operationNode.add(new DefaultMutableTreeNode("name : " + operation.getName()));
    operationNode.add(createDocumentationNode(operation));
    return(operationNode);
  }
  
  private DefaultMutableTreeNode createInputParamsNode(DOperation operation) {
    DefaultMutableTreeNode inputParamsNode = new DefaultMutableTreeNode("input params");
    DParameter[] inputParams = operation.getInputParameters();
    initWithParameters(inputParamsNode, inputParams);
    return(inputParamsNode);
  }
  
  private DefaultMutableTreeNode createOutputParamsNode(DOperation operation) {
    DefaultMutableTreeNode outputParamsNode = new DefaultMutableTreeNode("output params");
    DParameter[] outputParams = operation.getOutputParameters();
    initWithParameters(outputParamsNode, outputParams);
    return(outputParamsNode);
  }
  
  private DefaultMutableTreeNode createInOutParamsNode(DOperation operation) {
    DefaultMutableTreeNode inoutParamsNode = new DefaultMutableTreeNode("inout params");
    DParameter[] inoutParams = operation.getInOutParameters();
    initWithParameters(inoutParamsNode, inoutParams);
    return(inoutParamsNode);
  }
  
  private DefaultMutableTreeNode createFaultParamsNode(DOperation operation) {
    DefaultMutableTreeNode faulParamsNode = new DefaultMutableTreeNode("fault params");
    DParameter[] faultParams = operation.getFaultParameters();
    initWithParameters(faulParamsNode, faultParams);
    return(faulParamsNode);
  }
  
  private DefaultMutableTreeNode createReturnParamNode(DOperation operation) {
    DefaultMutableTreeNode returnParamsNode = new DefaultMutableTreeNode("return param");
    DParameter returnParameter = operation.getReturnParameter();
    if(returnParameter != null) {
      returnParamsNode.add(createParameterNode(returnParameter));
    }
    return(returnParamsNode);
  }
  
  private void initWithParameters(DefaultMutableTreeNode node, DParameter[] params) {
    for(int i = 0; i < params.length; i++) {
      DParameter parameter = params[i];
      node.add(createParameterNode(parameter));
    }
  }
  
  private DefaultMutableTreeNode createParameterNode(DParameter parameter) {
    DefaultMutableTreeNode parameterNode = new DefaultMutableTreeNode("parameter : " + parameter.getName());
    parameterNode.add(new DefaultMutableTreeNode("type : " + parameter.getParameterType()));
    parameterNode.add(new DefaultMutableTreeNode("schema name : " + parameter.getSchemaName()));
    parameterNode.add(new DefaultMutableTreeNode("name : " + parameter.getName()));
    parameterNode.add(new DefaultMutableTreeNode("param class : " + parameter.getParameterClass()));
    return(parameterNode);
  }
  
  private DefaultMutableTreeNode createTypeMappingNode(ExtendedTypeMapping typeMapping) {
    DefaultMutableTreeNode typeMappingNode = new DefaultMutableTreeNode("type mapping");
    DefaultMutableTreeNode builtInTypeNodeNode = new DefaultMutableTreeNode("builtin types");
    DefaultMutableTreeNode soapEncodingTypeNodeNode = new DefaultMutableTreeNode("soap encoding types");
    DefaultMutableTreeNode customTypeNodeNode = new DefaultMutableTreeNode("custom types");
    typeMappingNode.add(builtInTypeNodeNode);
    typeMappingNode.add(soapEncodingTypeNodeNode);
    typeMappingNode.add(customTypeNodeNode);
    
    Enumeration typesEnum = typeMapping.getRegisteredSchemaTypes();
    while(typesEnum.hasMoreElements()) {
      QName typeName = (QName)(typesEnum.nextElement());
      DBaseType type = typeMapping.getTypeMetadata(typeName);
      if(type != null) {
        if(type.isBuiltIn()) {
          builtInTypeNodeNode.add(createBaseTypeNode(type));
        } else if(type.getTypeName().getNamespaceURI().equals(SchemaMappingConstants.SOAP_ENCODING)) {
          soapEncodingTypeNodeNode.add(createBaseTypeNode(type));
        } else {
          customTypeNodeNode.add(createBaseTypeNode(type));
        }
      }
    }
    return(typeMappingNode);
  }
  
  private DefaultMutableTreeNode createBaseTypeNode(DBaseType type) {
    if(type instanceof DComplexType) {
      return(createComplexTypeNode((DComplexType)type));
    }
    return(createSimpleTypeNode((DSimpleType)type));
  }
  
  private String getOrderType(int groupType) {
    switch (groupType) {
      case DComplexType.ALL: {
        return "Content : UNORDERED";
      }
      case  DComplexType.SEQUENCE: {
        return "Content : ORDERED";
      }
      case  DComplexType.CHOICE: {
        return "Content : CHOICE";
      }
      case  DComplexType.SIMPLE: {
        return "Content : SIMPLE Content";
      }
    }    
    return "";
  }  
  
  private DefaultMutableTreeNode createComplexTypeNode(DComplexType type) {
    DefaultMutableTreeNode complexTypeNode = new DefaultMutableTreeNode("Complex Type : " + type.getTypeName());
    initWithBaseType(complexTypeNode, type);
    if (type.isAbstract()) {
      complexTypeNode.add(new DefaultMutableTreeNode("Abstract = " + type.isAbstract()));
    }
    if (type.isMixedContent()) {
      complexTypeNode.add(new DefaultMutableTreeNode("MixedContent = " + type.isMixedContent()));
    }
    complexTypeNode.add(new DefaultMutableTreeNode(getOrderType(type.getType())));
    initWithStructure(complexTypeNode, type);
    complexTypeNode.add(createAttributesNode(type));
    addAnnotationNodes(complexTypeNode,type);    
    return(complexTypeNode);
  }
  
  public void initWithBaseType(DefaultMutableTreeNode node, DBaseType baseType) {
    node.add(new DefaultMutableTreeNode("Base Type : " + baseType.getBaseTypeName()));
    node.add(new DefaultMutableTreeNode("BuiltIn = " + baseType.isBuiltIn()+", Anonymous = " +baseType.isAnonymous()));
  }
  
  public void initWithStructure(DefaultMutableTreeNode node, DStructure structure) {
    DefaultMutableTreeNode fieldsNode = new DefaultMutableTreeNode("XML Fields");
    DField[] fields = structure.getFields();
    for(int i = 0; i < fields.length; i++) {
      fieldsNode.add(createFieldNode(fields[i]));
    }
    node.add(fieldsNode);
  }
  
  /**
   * Adds a Field node to the tree.
   * @param field
   * @return
   */
  private DefaultMutableTreeNode createFieldNode(DField field) {
    if(field instanceof DElement) {
      return(createElementNode((DElement)field));
    }
    if(field instanceof DAttribute) {
      return(createAttributeNode((DAttribute)field));
    }
    if(field instanceof DGroup) {
      return(createGroupNode((DGroup)field));
    }
    if(field instanceof DSimpleContent) {
      return(createSimpleContentNode((DSimpleContent)field));
    }
    return(createAnyNode((DAny)field));
  }
  
  /**
   * Adds field type and scope to the tree.
   * @param node
   * @param field
   */
  private void addTypeAndScope(DefaultMutableTreeNode node, DField field) {
    node.add(new DefaultMutableTreeNode("Type : " + field.getFieldType()));
    node.add(new DefaultMutableTreeNode("Scope : " + field.getFieldScope()));    
  }
  
  /**
   * Displays XML Element Node.
   * @param element
   * @return
   */
  private DefaultMutableTreeNode createElementNode(DElement element) {
    DefaultMutableTreeNode elementNode = new DefaultMutableTreeNode("Element : " + element.getFieldName());
    addTypeAndScope(elementNode, element);
    if (element.getDefaultValue() != null) {
      elementNode.add(new DefaultMutableTreeNode("Default = \"" + element.getDefaultValue()+"\""));
    }
    elementNode.add(new DefaultMutableTreeNode("Nillable = " + element.isNillable()));    
    addOccurence(elementNode,element);
    addAnnotationNodes(elementNode,element);    
    return(elementNode);
  }
  
  private void showNodeList(DefaultMutableTreeNode node,NodeList xmlNodes) {
    for (int i=0; i<xmlNodes.getLength(); i++) {
      Node xmlNode = xmlNodes.item(i);
      if (xmlNode.getNodeType() == Node.ELEMENT_NODE) {
        try {
          node.add(new DefaultMutableTreeNode(SharedTransformers.transform(SharedTransformers.DEFAULT_TRANSFORMER,xmlNode)));
        } catch (Exception x) {
          node.add(new DefaultMutableTreeNode(x.toString()));
        }
      }
    }
  }
  
  private DefaultMutableTreeNode createAnnotationNode(DAnnotation annotation, String annotationLabel) {
    DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(annotationLabel);
    DAppInfo[] appInfos =  annotation.getAppInfos();
    for (int i=0; i<appInfos.length; i++) {
      DefaultMutableTreeNode appInfoNode = new DefaultMutableTreeNode("AppInfo");
      treeNode.add(appInfoNode);        
      NodeList nodes = appInfos[i].getContent();
      showNodeList(appInfoNode,nodes);
    }
    DDocumentation[] documentations = annotation.getDocumentations();
    for (int i=0; i<documentations.length; i++) {
      DefaultMutableTreeNode documentationNode = new DefaultMutableTreeNode("Documentation");
      treeNode.add(documentationNode);
      NodeList nodes = documentations[i].getContent();
      showNodeList(documentationNode,nodes);        
    }      
    return treeNode;
  }
  
  private void addAnnotationNodes(DefaultMutableTreeNode node, DAnnotateable annotable) {
    if (annotable.getAnnotation() != null) {
      node.add(createAnnotationNode(annotable.getAnnotation(),"Annotation"));
    }    
    if (annotable.getTopLevelAnnotation() != null) {
      node.add(createAnnotationNode(annotable.getTopLevelAnnotation(),"Top Level Annotation"));
    }    
  }
  
  /**
   * Displays XML Attribute node.
   * @param attrib
   * @return
   */
  private DefaultMutableTreeNode createAttributeNode(DAttribute attrib) {
    DefaultMutableTreeNode attribNode = new DefaultMutableTreeNode("Attribute : "+attrib.getFieldName());
    addTypeAndScope(attribNode,attrib);
    if (attrib.getDefaultValue() != null) {
      attribNode.add(new DefaultMutableTreeNode("Default = \"" + attrib.getDefaultValue()+"\""));
    }    
    attribNode.add(new DefaultMutableTreeNode("Required = " + attrib.isRequired()));
    addAnnotationNodes(attribNode,attrib);
    return(attribNode);
  }
  
  private void addOccurence(DefaultMutableTreeNode node, DOccurrenceable field) {
    node.add(new DefaultMutableTreeNode("MinOccurs = " + field.getMinOccurs()+" MaxOccurs = "+field.getMaxOccurs()));
  }
    
  private DefaultMutableTreeNode createGroupNode(DGroup group) {
    DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode("Group : " + group.getFieldName());
    groupNode.add(new DefaultMutableTreeNode("Scope : " + group.getFieldScope()));
    groupNode.add(new DefaultMutableTreeNode(new DefaultMutableTreeNode(getOrderType(group.getType()))));
    addOccurence(groupNode,group);
    initWithStructure(groupNode, group);
    return(groupNode);
  }
  
  private DefaultMutableTreeNode createSimpleContentNode(DSimpleContent simpleContent) {
    DefaultMutableTreeNode simpleContentNode = new DefaultMutableTreeNode("SimpleContent : " + simpleContent.getFieldName());
    addTypeAndScope(simpleContentNode,simpleContent);
    return(simpleContentNode);
  }
  
  /**
   * Adds wildcard representation.
   * @param any
   * @return
   */
  private DefaultMutableTreeNode createAnyNode(DAny any) {
    DefaultMutableTreeNode anyNode = new DefaultMutableTreeNode("Any : "+any.getFieldName());
    anyNode.add(new DefaultMutableTreeNode("Scope : " + any.getFieldScope())); 
    addOccurence(anyNode,any);
    return(anyNode);
  }
  
  /**
   * Adds attribute nodes.
   * @param complexType
   * @return
   */
  private DefaultMutableTreeNode createAttributesNode(DComplexType complexType) {
    DefaultMutableTreeNode attributesNode = new DefaultMutableTreeNode("XML Attributes");
    DField[] attributes = complexType.getAttributes();
    for(int i = 0; i < attributes.length; i++) {
      DField field = attributes[i];
      attributesNode.add(createFieldNode(field));
    }
    return(attributesNode);
  }
  
  private DefaultMutableTreeNode createSimpleTypeNode(DSimpleType simpleType) {
    DefaultMutableTreeNode simpleTypeNode = new DefaultMutableTreeNode("Simple Type : " + simpleType.getTypeName());
    initWithBaseType(simpleTypeNode, simpleType);
    switch (simpleType.getType()) {
      case DSimpleType.LIST: {
        simpleTypeNode.add(new DefaultMutableTreeNode("CONTENT : " + "LIST"));
        break;
      }
      case DSimpleType.RESTRICTION: {
        simpleTypeNode.add(new DefaultMutableTreeNode("CONTENT : " + "RESTRICTION"));
        break;
      }
    }    
    simpleTypeNode.add(createFacetsNode(simpleType));
    addAnnotationNodes(simpleTypeNode,simpleType);
    return(simpleTypeNode);
  }
  
  private DefaultMutableTreeNode createFacetsNode(DSimpleType simpleType) {
    DefaultMutableTreeNode simpleTypeNode = new DefaultMutableTreeNode("Facets");
    Facet[] facets = simpleType.getFacets();
    for(int i = 0; i < facets.length; i++) {
      Facet facet = facets[i];
      simpleTypeNode.add(createFacetNode(facet));
    }
    return(simpleTypeNode);
  }
 
  private DefaultMutableTreeNode createFacetNode(Facet facet) {
    DefaultMutableTreeNode facetNode = new DefaultMutableTreeNode(facet.getName()+"=\""+facet.getValue()+"\"");
    facetNode.add(new DefaultMutableTreeNode("IntValue = " + facet.getIntValue()));
    if (facet.getObjectValue() != null) {
      facetNode.add(new DefaultMutableTreeNode("object value : " + facet.getObjectValue()));
    }
    return(facetNode);
  }
}
