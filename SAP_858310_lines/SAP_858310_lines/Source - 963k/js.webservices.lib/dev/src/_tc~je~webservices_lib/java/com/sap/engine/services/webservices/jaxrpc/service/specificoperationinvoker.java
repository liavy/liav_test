package com.sap.engine.services.webservices.jaxrpc.service;

import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceParam;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ClientTransportBinding;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.MimeHttpBinding;
import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaAutoImportURIResolver;
import com.sap.engine.services.webservices.wsdl.*;
import com.sap.engine.lib.schema.components.*;
import com.sap.engine.lib.jaxp.MultiSource;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.util.*;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-7-23
 * Time: 9:11:07
 * To change this template use Options | File Templates.
 */
public class SpecificOperationInvoker extends AbstractOperationInvoker {

  private SpecificTransportBindingInvoker invoker;

  protected SpecificOperationInvoker(WSDLOperation wsdlOperation, WSDLBinding wsdlBinding, ClientTransportBinding clientTransportBinding, WSDLDefinitions wsdlDefinitions, TypeMappingRegistryImpl typeMappingRegistry, String endpointAddress, SpecificTransportBindingInvoker invoker) {
    super(clientTransportBinding, typeMappingRegistry);
    this.invoker = invoker;
    operationParametersConfig = new OperationParametersConfiguration(wsdlOperation.getParameterOrder());
    operationName = new QName(wsdlOperation.getQName().getURI(), wsdlOperation.getQName().getLocalName());
    WSDLBindingOperation wsdlBindingOperation = wsdlBinding.getOperation(wsdlOperation.getName());
    if (wsdlBindingOperation == null) {
      throw new JAXRPCException(createErrorInfo() + " ERROR : Operation '" + wsdlOperation.getName() + "' not described in binding '" + wsdlBinding.getName() + "'.");
    }
    try {
      clientTransportBinding.getMainBindingConfig(wsdlBinding, bindingConfiguration);
      clientTransportBinding.getOperationBindingConfig(wsdlBindingOperation, wsdlOperation, bindingConfiguration, wsdlDefinitions);
    } catch(Exception exc) {
      throw new JAXRPCException(createErrorInfo() + " ERROR : " + exc.getMessage());
    }
    bindingConfiguration.setProperty(MimeHttpBinding.SOAP_STYLE, MimeHttpBinding.RPC_STYLE);
    setTargetEndpointAddress(endpointAddress);

    Schema schema = null;
    WSDLChannel wsdlInputChanel = wsdlOperation.getInput();
    if (wsdlInputChanel != null) {
      com.sap.engine.lib.xml.util.QName messagelink = wsdlInputChanel.getMessage();
      ArrayList partsCollector = getMessageParts(messagelink.getLocalName(), messagelink.getURI(), wsdlDefinitions);
      for (int i = 0; i < partsCollector.size(); i++) {
        WSDLPart wsdlPart = (WSDLPart)(partsCollector.get(i));
        if(wsdlPart.getStyle() == WSDLPart.SIMPLE_TYPE) {
          ServiceParam serviceParam = createServiceParam(wsdlPart);
          inputServiceParamsCollector.add(serviceParam);
          operationParametersConfig.processInParameter(serviceParam);
        } else {
          String elementNamespace = wsdlPart.getType().getURI();
          String elementName = wsdlPart.getType().getLocalName();
          if(schema == null) {
            schema = determineSchema(wsdlDefinitions.getSchemaDefinitions());
          }
          ServiceParamsIterator serviceParamsIterator = new ServiceParamsIterator(schema, elementNamespace, elementName, typeMappingRegistry);
          bindingConfiguration.getSubContext("input").setProperty(MimeHttpBinding.SOAP_OPERATION_NAMESPACE, serviceParamsIterator.getElementDeclaration().getTargetNamespace());
          ServiceParam serviceParam = null;
          String parts = "";
          while((serviceParam = serviceParamsIterator.next()) != null) {
            parts += serviceParam.name + " ";
            inputServiceParamsCollector.add(serviceParam);
            operationParametersConfig.processInParameter(serviceParam);
          }
          bindingConfiguration.getSubContext("input").setProperty(MimeHttpBinding.SOAP_PARTS, parts.trim());
        }
      }
    }
    WSDLChannel wsdlOutputChanel = wsdlOperation.getOutput();
    if (wsdlOutputChanel != null) {
      com.sap.engine.lib.xml.util.QName messagelink = wsdlOutputChanel.getMessage();
      ArrayList partsCollector = getMessageParts(messagelink.getLocalName(), messagelink.getURI(), wsdlDefinitions);
      boolean isSingle = partsCollector.size() == 1;
      for (int i = 0; i < partsCollector.size(); i++) {
        WSDLPart wsdlPart = (WSDLPart)(partsCollector.get(i));
        if(wsdlPart.getStyle() == WSDLPart.SIMPLE_TYPE) {
          ServiceParam serviceParam = createServiceParam(wsdlPart);
          outputServiceParamsCollector.add(serviceParam);
          operationParametersConfig.processOutParameter(serviceParam, isSingle);
        } else {
          String elementNamespace = wsdlPart.getType().getURI();
          String elementName = wsdlPart.getType().getLocalName();
          if(schema == null) {
            schema = determineSchema(wsdlDefinitions.getSchemaDefinitions());
          }
          ServiceParamsIterator serviceParamsIterator = new ServiceParamsIterator(schema, elementNamespace, elementName, typeMappingRegistry);
          bindingConfiguration.getSubContext("output").setProperty(MimeHttpBinding.SOAP_OPERATION_NAMESPACE, serviceParamsIterator.getElementDeclaration().getTargetNamespace());
          ServiceParam serviceParam = null;
          while((serviceParam = serviceParamsIterator.next()) != null) {
            outputServiceParamsCollector.add(serviceParam);
            operationParametersConfig.processOutParameter(serviceParam, isSingle);
          }
        }
      }
    }
    ArrayList faultsCollector = wsdlOperation.getFaultList();
    for(int i = 0; i < faultsCollector.size(); i++) {
      WSDLFault wsdlFault = (WSDLFault)(faultsCollector.get(i));
      com.sap.engine.lib.xml.util.QName messageLink = wsdlFault.getMessage();
      ArrayList partsCollector = getMessageParts(messageLink.getLocalName(), messageLink.getURI(), wsdlDefinitions);
      faultsServiceParamsCollector.add(createServiceParam((WSDLPart)(partsCollector.get(0))));
    }
  }

  private Schema determineSchema(ArrayList schemaSources) {
    try {
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      SchemaAutoImportURIResolver resolver = new SchemaAutoImportURIResolver();
      transformer.setURIResolver(resolver);
      MultiSource multiSource = new MultiSource();
      int nSchemaSources = schemaSources.size();
      if (nSchemaSources == 0) {
        URL url = SchemaAutoImportURIResolver.class.getResource("preloaded/soapenc.xsd");
        if (url != null) {
          multiSource.addSource(new StreamSource(url.toString()));
        }
      } else {
        for (int i = 0; i < nSchemaSources; i++) {
          multiSource.addSource((Source)(schemaSources.get(i)));
        }
      }
      SchemaComponentResult schemaComponentResult = new SchemaComponentResult();
      transformer.transform(multiSource, schemaComponentResult);
      Schema schema = schemaComponentResult.getSchema();
      return(schema);
    } catch(Exception exc) {
      throw new JAXRPCException(createErrorInfo() + " ERROR : " + exc.getMessage());
    }
  }

  private ServiceParam createServiceParam(WSDLPart wsdlPart) {
    ServiceParam serviceParam = new ServiceParam();
    serviceParam.isElement = false;
    serviceParam.name = wsdlPart.getName();
    serviceParam.wsdlPartName = wsdlPart.getName();
    QName typeName = new QName(wsdlPart.getType().getURI(), wsdlPart.getType().getLocalName());
    serviceParam.schemaName = typeName;
    serviceParam.contentClassName = typeMappingRegistry.getDefaultTypeMappingImpl().getDefaultJavaType(typeName);
    setContentClass(serviceParam);
    return(serviceParam);
  }

  private ArrayList getMessageParts(String messageName, String messageNamespace, WSDLDefinitions wsdlDefinitions) {
    WSDLMessage message = wsdlDefinitions.getMessage(messageName, messageNamespace);
    if (message == null) {
      throw new JAXRPCException(createErrorInfo() + " ERROR : Message '" + messageName + "' is no defined.");
    }
    return(message.getParts());
  }

  protected String createErrorInfo() {
    return(invoker.createErrorInfo() + " [Operation (namespace : " + operationName.getNamespaceURI() + "; name : " + operationName.getLocalPart() + ")]");
  }

  protected OperationParametersConfiguration getParametersConfiguration() {
    return(operationParametersConfig);
  }

  private void setContentClass(ServiceParam serviceParam) {
    if(serviceParam.contentClassName == null) {
      throw new JAXRPCException(createErrorInfo() + " ERROR : Type " + serviceParam.schemaName.toString() + " is not registered in the type mapping registry.");
    }
    serviceParam.contentClass = ClassResolver.resolve(serviceParam.contentClassName, getClass().getClassLoader());
  }

  private class ServiceParamsIterator {

    private Vector particlesCollector;
    private int index;
    private TypeMappingRegistryImpl typeMappingRegistry;
    private ElementDeclaration elementDeclaration;

    private ServiceParamsIterator(Schema schema, String ownerElemDeclarationNamespace, String ownerElemDeclarationName, TypeMappingRegistryImpl typeMappingRegistry) {
      elementDeclaration = schema.getTopLevelElementDeclaration(ownerElemDeclarationNamespace, ownerElemDeclarationName);
      if(elementDeclaration == null) {
        throw new JAXRPCException(createErrorInfo() + " ERRPR : Element {" + ownerElemDeclarationNamespace + "} : " + ownerElemDeclarationName + " is not defined.");
      }
      TypeDefinitionBase typeDefinitionBase = elementDeclaration.getTypeDefinition();
      if(typeDefinitionBase instanceof SimpleTypeDefinition) {
        throw new JAXRPCException(createErrorInfo() + " ERRPR : Element {" + ownerElemDeclarationNamespace + "} : " + ownerElemDeclarationName + " must be defined with complex type.");
      }
      ComplexTypeDefinition complexTypeDefinition = (ComplexTypeDefinition)typeDefinitionBase;
      Particle particle = complexTypeDefinition.getContentTypeContentModel();
      if(particle == null || !(particle.getTerm() instanceof ModelGroup) || !((ModelGroup)(particle.getTerm())).isCompositorSequence()) {
        throw new JAXRPCException(createErrorInfo() + " ERRPR : Element {" + ownerElemDeclarationNamespace + "} : " + ownerElemDeclarationName + " must be defined with complex type which contains particle whose term is sequence model group.");
      }
      ModelGroup modelGroup = (ModelGroup)(particle.getTerm());
      particlesCollector = new Vector();
      modelGroup.getParticles(particlesCollector);
      index = 0;
      this.typeMappingRegistry = typeMappingRegistry;
    }

    private ElementDeclaration getElementDeclaration() {
      return(elementDeclaration);
    }

    private ServiceParam next() {
      if(index == particlesCollector.size()) {
        return(null);
      }
      Particle particle = (Particle)(particlesCollector.get(index++));
      Base term = particle.getTerm();
      if(!(term instanceof ElementDeclaration)) {
        throw new JAXRPCException(createErrorInfo() + " ERROR : Element declaration expected as a term of a particle.");
      }
      ElementDeclaration elementDeclaration = (ElementDeclaration)term;
      String elemDeclarationNamespace = elementDeclaration.getTargetNamespace();
      String elemDeclarationName = elementDeclaration.getName();
      TypeDefinitionBase typeDefinitionBase = elementDeclaration.getTypeDefinition();
      String typeNamespace = typeDefinitionBase.getTargetNamespace();
      String typeName = typeDefinitionBase.getName();
      QName elementQName = new QName(elemDeclarationNamespace, elemDeclarationName);
      QName typeQName = new QName(typeNamespace, typeName);
      typeMappingRegistry.getDefaultTypeMappingImpl().setTypeForElement(elementQName, typeQName);
      ServiceParam serviceParam = new ServiceParam();
      serviceParam.schemaName = typeQName;
      serviceParam.name = elemDeclarationName;
      serviceParam.isElement = false;
      serviceParam.wsdlPartName = elemDeclarationName;
      serviceParam.contentClassName = typeMappingRegistry.getDefaultTypeMappingImpl().getDefaultJavaType(typeQName);
      setContentClass(serviceParam);
      return(serviceParam);
    }
  }

  protected Map getOutputParams() {
    Hashtable map = new Hashtable();
    for(int i = 0; i < outputServiceParamsCollector.size(); i++) {
      ServiceParam outputServiceParam = (ServiceParam)(outputServiceParamsCollector.get(i));
      if(outputServiceParam.content != null) {
        map.put(outputServiceParam.name, outputServiceParam.content);
      } else {
        throw new JAXRPCException(createErrorInfo() + " ERROR : The method getOutputParams() is invoked before any invoke method has been called.");
      }
    }
    return(map);
  }

  protected List getOutputValues() {
    ArrayList list = new ArrayList();
    for(int i = 0; i < outputServiceParamsCollector.size(); i++) {
      ServiceParam outputServiceParam = (ServiceParam)(outputServiceParamsCollector.get(i));
      if(outputServiceParam.content != null) {
        list.add(outputServiceParam.content);
      } else {
        throw new JAXRPCException(createErrorInfo() + " ERROR : The method getOutputValues() is invoked before any invoke method has been called.");
      }
    }
    return(list);
  }
}
