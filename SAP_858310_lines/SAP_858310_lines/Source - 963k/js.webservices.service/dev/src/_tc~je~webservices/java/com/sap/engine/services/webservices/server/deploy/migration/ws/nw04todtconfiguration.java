package com.sap.engine.services.webservices.server.deploy.migration.ws;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import com.sap.engine.lib.descriptors.ws04vi.FunctionState;
import com.sap.engine.lib.descriptors.ws04vi.VirtualInterfaceState;
import com.sap.engine.lib.descriptors.ws04wsd.WebServiceDefinitionState;
import com.sap.engine.lib.descriptors.ws04wsdd.FeatureDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.NameDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.OperationConfigurationDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.QNameDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.WSConfigurationDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.WSDescriptor;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationMarshallerException;
import com.sap.engine.services.webservices.espbase.configuration.Description;
import com.sap.engine.services.webservices.espbase.configuration.IConfigurationMarshaller;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinition;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceDefinitionCollection;
import com.sap.engine.services.webservices.espbase.configuration.OperationData;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.configuration.ServiceCollection;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.configuration.Variant;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.deploy.migration.ws.exception.ConversionException;
import com.sap.engine.services.wssec.srt.features.designtimetoruntime.RTimePropertyGenerator;
import com.sap.engine.services.wssec.srt.features.exception.FeaturePropagationException;
import com.sap.tc.logging.Location;
/**
 * Class for loading the whole data from the ws-desployment-descriptor.xml 
 * in the design time configuration part of the configuration.xml
 * Company: Sap Labs Sofia 
 * @author aneta-a
 */
  class NW04toDTConfiguration extends Configuration {
  
    private static final Location LOCATION = Location.getLocation(NW04toDTConfiguration.class); 

	/*
	 * Loads DTConfig element in ConfigurationRoot element.
	 */
	public InterfaceDefinitionCollection loadDTConfiguration(WSDescriptor[] nw04Webservice, Hashtable wsdStructures, String applicationName, Hashtable operationProps, ServiceCollection serviceCollection) throws ConversionException {
    operationProperties = operationProps;
    InterfaceDefinitionCollection dTConfig = new InterfaceDefinitionCollection();
	  dTConfig.setInterfaceDefinition(loadInterfaceDefinition(nw04Webservice, wsdStructures, applicationName, serviceCollection));
	  return dTConfig;
	}
	
	/*
	 * Loads InterfaceDefinition element of DTConfig element.
	 */
	 
  private InterfaceDefinition[] loadInterfaceDefinition(WSDescriptor[] nw04Webservice, Hashtable vi_wsdStructures, String applicationName, ServiceCollection serviceCollection) throws ConversionException {
  	Hashtable interfaceDefinitions = new Hashtable();
  	Hashtable interfaceVariants = new Hashtable();
	  Vector variants;
  	InterfaceDefinition interfaceDefinition;
  	for (int i = 0; i < nw04Webservice.length; i++) {
  	  WSConfigurationDescriptor[] wsConfiguration = nw04Webservice[i].getWsConfiguration();
  	  NameDescriptor viName;
  	  // if ws-configuration is not null
  	  if (wsConfiguration != null) {
        for (int j = 0; j < wsConfiguration.length; j++) {
          viName =  wsConfiguration[j].getServiceEndpointViRef();      
          VirtualInterfaceState viStructure = ((VirtualInterfaceState)vi_wsdStructures.get(viName.getName()));
          String interfaceDefName = viStructure.getName();
          if (!viStructure.getPackage().trim().equals("")) {
            interfaceDefName = viStructure.getPackage() + "_" + interfaceDefName;
          } 
          NameDescriptor wsdName = wsConfiguration[j].getWebserviceDefinitionRef();      
          WebServiceDefinitionState wsdStructure = ((WebServiceDefinitionState)vi_wsdStructures.get(wsdName.getName()));      
          String variantName = generateName(wsdStructure.getPackage(), wsdStructure.getName(), ".wsdef");   
          
          String iDefName = applicationName + "_" + interfaceDefName;
          String interfaceMappingId = "";
          Service[] services = serviceCollection.getService();
          for (int s = 0; s < services.length; s++) {
            ServiceData serviceData = services[s].getServiceData();
            BindingData[] bindingDatas = serviceData.getBindingData();
            for (int k = 0; k < bindingDatas.length; k++) {
							if (bindingDatas[k].getInterfaceId().equals(iDefName)) {
                interfaceMappingId = bindingDatas[k].getInterfaceMappingId();
                break;
              }
						}
          }
          
          if (interfaceDefinitions.get(iDefName) == null) {
            interfaceDefinition = new InterfaceDefinition();
            //TODO: check default values
            interfaceDefinition.setName(iDefName);
            interfaceDefinition.setId(iDefName);
            interfaceDefinition.setInterfaceMappingId(interfaceMappingId);
            interfaceDefinition.setType(0);
            interfaceDefinitions.put(iDefName, interfaceDefinition);
            
            variants = new Vector();          
            variants.addElement(loadVariant(wsConfiguration[j], vi_wsdStructures));   
            interfaceVariants.put(iDefName, variants);
          } else {
            variants = (Vector)interfaceVariants.get(iDefName);
            if (!containsVariant(variants, variantName)) {
              variants.addElement(loadVariant(wsConfiguration[j], vi_wsdStructures));
            }   
          }
        }
  	  }
    }
	  
    Enumeration keys = interfaceDefinitions.keys();
	  InterfaceDefinition[] iDef = new InterfaceDefinition[interfaceDefinitions.size()];
	  for (int i = 0; i < iDef.length; i++) {
  		String key = (String)keys.nextElement();
	    iDef[i] = (InterfaceDefinition)interfaceDefinitions.get(key);
	    
      Vector result = (Vector)interfaceVariants.get(key);
		  Variant[] variant = new Variant[result.size()];
      result.copyInto(variant);
	    iDef[i].setVariant(variant);
	  }

	  return iDef;
	} 
  
  private boolean containsVariant(Vector variants, String variantName) {
    for (int i = 0; i < variants.size(); i++) {
			if (variantName.equals(((Variant)variants.elementAt(i)).getName())) {
        return true;
      }
		}
    return false;
  }
		
	/*
	 * Loads Variant element of InterfaceDefinition element.
	 */
	private Variant loadVariant(WSConfigurationDescriptor nw04WSConfiguration, Hashtable vi_wsdStructures) throws ConversionException {
    // wsd name comes from the ws-configuration's WebserviceDefinitionRef   
	  NameDescriptor wsdName = nw04WSConfiguration.getWebserviceDefinitionRef();		
    NameDescriptor viName = nw04WSConfiguration.getServiceEndpointViRef();  
	  WebServiceDefinitionState wsdStructure = (WebServiceDefinitionState)vi_wsdStructures.get(wsdName.getName());
    VirtualInterfaceState viStructure = (VirtualInterfaceState)vi_wsdStructures.get(viName.getName());		
     	
	  String s = generateName(wsdStructure.getPackage(), wsdStructure.getName(), ".wsdef");		
	  Variant variantData = new Variant();
	  variantData.setName(s);	   
    
    InterfaceData interfaceData = loadInterfaceData(nw04WSConfiguration, viStructure, wsdStructure);
//    try {
//	    new RTimePropertyGenerator(RTimePropertyGenerator.WSCONF_PROPAGATION).modifyInterfaceData(interfaceData); 
//		} catch (FeaturePropagationException e) {
//      throw new ConversionException("Exception occurred while converting security properties for interface data " + interfaceData.getName(), e);
//		}   
    InterfaceData newID = interfaceData.makeCopyWithoutProperties();
    try {
      LOCATION.debugT("loadVariant(): calling ConfigurationBuilder.convertNW04CfgToNy() with ...");
      WSContainer.getRuntimeProcessingEnv().getWSDLVisualizer().getInternalCFGBuilder().convertNW04CfgToNY(interfaceData, newID, IConfigurationMarshaller.PROVIDER_MODE);
    } catch (ConfigurationMarshallerException e) {
      throw new ConversionException("Exception occurred while converting properties.", e);
    }
	  variantData.setInterfaceData(newID);			    	 
	  return variantData;
	  
	}
   
	/*
	 * Loads InterfaceData element of Variant element.
	 */
	private InterfaceData loadInterfaceData(WSConfigurationDescriptor nw04WSConfiguration, VirtualInterfaceState viStructure, WebServiceDefinitionState wsdStructure) throws ConversionException {
    InterfaceData interfaceData = new InterfaceData();
      // TODO fill info
	  QNameDescriptor portTypeName = nw04WSConfiguration.getWsdlPorttypeName();
    interfaceData.setName(portTypeName.getLocalName());
    interfaceData.setNamespace(portTypeName.getNamespaceURI() + "/" + portTypeName.getLocalName());  
	  if (wsdStructure != null) {
      interfaceData.setPropertyList(loadIDPropertyList(wsdStructure));
	  }
    if (viStructure != null) {
      interfaceData.setOperation(loadOperation(nw04WSConfiguration.getOperationConfiguration(),  viStructure.getVirtualInterfaceFunctions().getFunction()));    
    }
    interfaceData.setUDDIEntity(loadUDDIEntity());//?
	  interfaceData.setDescription(loadDescription());//?	  
	  return interfaceData;
	}

	/*
	 * Loads Description element of InterfaceData element.
	 */
	private Description[] loadDescription() {
	  Description[] description = new Description[1];
	  for (int i = 0; i < description.length; i++) {
		  description[i] = new Description();
		  // TODO fill info
		  description[i].setLocale("en-us");//?
		  description[i].setShortText("");//?
	  	description[i].setLongText("");//?
	  }
	  return description;
	}
  
  private OperationData[] loadOperation(OperationConfigurationDescriptor[] operationConfig, FunctionState[] functions) throws ConversionException {
    Hashtable operations = new Hashtable();

    if (operationConfig != null) {
      for (int i = 0; i < operationConfig.length; i++) {
        if (operationConfig[i].getName() != null) {
          operations.put(operationConfig[i].getName(), operationConfig[i]);
        }       
        if (operationConfig[i].getUniqueViName() != null) {
          operations.put(operationConfig[i].getUniqueViName(), operationConfig[i]);
        }      
      } 
    }
    
   OperationData[] operationData = new OperationData[0];
   if (functions != null) {
      operationData = new OperationData[functions.length];     
      for (int i = 0; i < functions.length; i++) {
         operationData[i] = new OperationData();
         operationData[i].setName(functions[i].getNameMappedTo());
         //load properties for operation configuration
         OperationConfigurationDescriptor operationConiguration = (OperationConfigurationDescriptor)operations.get(functions[i].getName());         
         FeatureDescriptor[] featureDescriptors = new FeatureDescriptor[0];
         if (operationConiguration != null) {
           featureDescriptors = operationConiguration.getFeature();
         }
         operationData[i].setPropertyList(loadPropertyList(featureDescriptors, null));     
      }       
    }
    return operationData;
  }
}
