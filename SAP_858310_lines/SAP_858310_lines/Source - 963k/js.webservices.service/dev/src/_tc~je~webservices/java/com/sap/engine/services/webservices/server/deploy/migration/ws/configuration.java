package com.sap.engine.services.webservices.server.deploy.migration.ws;

import java.util.Hashtable;
import java.util.Vector;

import com.sap.engine.lib.descriptors.ws04vi.FunctionState;
import com.sap.engine.lib.descriptors.ws04vi.VirtualInterfaceState;
import com.sap.engine.lib.descriptors.ws04wsd.FeatureState;
import com.sap.engine.lib.descriptors.ws04wsd.PropertyState;
import com.sap.engine.lib.descriptors.ws04wsd.SoapApplicationState;
import com.sap.engine.lib.descriptors.ws04wsd.WebServiceDefinitionState;
import com.sap.engine.lib.descriptors.ws04wsdd.FeatureDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.OperationConfigurationDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.PropertyDescriptor;
import com.sap.engine.services.webservices.espbase.configuration.OperationData;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.UDDIEntity;
import com.sap.engine.services.webservices.espbase.configuration.UDDIServer;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.server.deploy.migration.ws.exception.ConversionException;
import com.sap.tc.logging.Location;

/**
 * Common java objects structures for both design time and runtime configuration.
 * Company: Sap Labs Sofia 
 * @author aneta-a
 */

 class Configuration {
 	
  private static final Location LOCATION = Location.getLocation(Configuration.class); 
  private static final String ONE_WAY = "http://www.sap.com/webas/630/soap/features/mep/one-way";
  protected Hashtable operationProperties;
   
  protected OperationData[] loadOperation(OperationConfigurationDescriptor[] operationConfiguration, VirtualInterfaceState viStructure) throws ConversionException {		
    OperationData[] operationData = new OperationData[operationConfiguration.length];
    for (int i = 0; i < operationData.length; i++) {
	  String name = null;
	  if (operationConfiguration[i].getUniqueViName() != null) {
	    name = operationConfiguration[i].getUniqueViName();
	    FunctionState[] functions = viStructure.getVirtualInterfaceFunctions().getFunction();
	    for (int j = 0; j < functions.length; j++) {
	      if (name.equals(functions[j].getName())) {
		    name = functions[j].getNameMappedTo();
		  }
		}
	  } else if(operationConfiguration[i].getName() != null) {
	    name = operationConfiguration[i].getName();
	  }
	  operationData[i] = new OperationData();
	  if (name != null) {
	    operationData[i].setName(name);
	  } else {
	    throw new ConversionException(MigrationConstants.OPERATION_NAME_MISSING);		 
	  }		 	
	  operationData[i].setPropertyList(loadPropertyList(operationConfiguration[i].getFeature(), name));//?
	}
	return operationData;
  }
	
  // Property list for ServiceData
  protected PropertyListType[] loadPropertyList() {
  //	TODO fill info
    PropertyListType[] propertyListType = new PropertyListType[1];//? size is not correct
    for (int i = 0; i < propertyListType.length; i++) {
	  propertyListType[i] = new PropertyListType();		
	  propertyListType[i].setSelected(Boolean.TRUE);//? 
	  propertyListType[i].setProperty(loadPropertyType());//? 
    }
    return propertyListType;
  }
	
  // Properties for ServiceData
  protected PropertyType[] loadPropertyType() {
    PropertyType[] propertyType = new PropertyType[1];
    for (int i = 0; i < propertyType.length; i++) {
	  propertyType[i] = generatePropertyType("", "", "");//? name, namespace, value
    }
    return propertyType;
  }

  protected UDDIEntity[] loadUDDIEntity() {
    // TODO fill info
    UDDIEntity[] uddiEntity = new UDDIEntity[1];//? size is not correct
    for (int i = 0; i < uddiEntity.length; i++) { 
    // set UDDIEntity attributes
	  uddiEntity[i] = new UDDIEntity();
	  uddiEntity[i].setUddiKey("");//?
	  uddiEntity[i].setSubscribtionKey("");//?
	  uddiEntity[i].setType(0);//?
      // set UDDIEntity elements
	  uddiEntity[i].setUDDIServer(loadUDDIServer());//?	   
	}
	return uddiEntity;
  }
   
  protected UDDIServer loadUDDIServer() {
    UDDIServer uddiServer = new UDDIServer();
  // TODO fill info
    uddiServer.setInquiryURL("");//? tova ili 
    uddiServer.setSIDName("");// ? drugoto
    return uddiServer;
  }
	
  // Property list for BindingData or Operation elements
  protected PropertyListType[] loadPropertyList(FeatureDescriptor[] features, String operationName) throws ConversionException {
    PropertyListType[] propertyListType = new PropertyListType[1];
    if (features == null || features.length == 0) {
      propertyListType[0] = new PropertyListType();     
      propertyListType[0].setSelected(Boolean.TRUE);
    } else {
      propertyListType[0] = new PropertyListType();     
      // TODO fill info
      propertyListType[0].setSelected(Boolean.TRUE);
      propertyListType[0].setProperty(loadPropertyType(features, operationName));
    } 		  
	return propertyListType;
  }
	
    // Properties for BindingData, InterfaceData, Operation elements
    // Parameter operationName is passed only for operation properties, in case of interfacedata and bindingdata it is null
  private PropertyType[] loadPropertyType(FeatureDescriptor[] features, String operationName) throws ConversionException {	    
   //properties comming from the ws-configuration 
    PropertyDescriptor[] propertyDescriptor; 
    Vector newProperties = new Vector();
    for (int i = 0; i < features.length; i++) {
	  propertyDescriptor = features[i].getProperty();	
	  if (propertyDescriptor == null) {
	    newProperties.addElement(generatePropertyType("default", features[i].getName(), ""));
	  } else {		  	
		if (features[i].getName().equals(ONE_WAY) && operationName != null) {
		  //section when ONE_WAY operation feature is reached and has to be converted as operation mapping
		  com.sap.engine.services.webservices.espbase.mappings.PropertyType oPropertyType = new com.sap.engine.services.webservices.espbase.mappings.PropertyType();
		  oPropertyType.setName(OperationMapping.OPERATION_MEP);
		  oPropertyType.set_value(OperationMapping.MEP_ONE_WAY);
		  operationProperties.put(operationName, oPropertyType);
		} else {
		  for (int j = 0; j < propertyDescriptor.length; j++) {
		  //if a security-roles property is reached
		    if (propertyDescriptor[j].getName().equals("security-roles") && propertyDescriptor[j].getValue() == null) {
		  	  PropertyDescriptor.Choice1 choice = propertyDescriptor[j].getChoiceGroup1();
			  if (choice != null) {
			    PropertyDescriptor[] securityProperty = choice.getProperty();
			    for (int k = 0; k < securityProperty.length; k++) {
			   	  newProperties.addElement(generatePropertyType(propertyDescriptor[j].getName(), features[i].getName(), securityProperty[k].getValue()));                                     
				}
			  }
			} else {
			  newProperties.addElement(generatePropertyType(propertyDescriptor[j].getName(), features[i].getName(), propertyDescriptor[j].getValue()));                       
			}
		  }	
		}			
	  }
	}	  
	return convertToArray(newProperties);
  }
 
   private PropertyType generatePropertyType(String name, String namespace, String value) {
	 PropertyType propertyType = new PropertyType();
	 propertyType.setName(name);
	 propertyType.setNamespace(namespace);
	 propertyType.set_value(value);
	 return propertyType;
   } 
   
   //Property list for InterfaceData elements	
   protected PropertyListType[] loadIDPropertyList(WebServiceDefinitionState wsdStructure) {
 	 SoapApplicationState[] soapApplication = wsdStructure.getWebServiceDefinitionSoapApplications().getSoapApplication();		
	 if (soapApplication[0].getSoapApplicationFeatures() == null || soapApplication[0].getSoapApplicationFeatures().getFeature().length == 0) return new PropertyListType[0];
     FeatureState[] features = soapApplication[0].getSoapApplicationFeatures().getFeature();       
	 
	 PropertyListType[] propertyListType = new PropertyListType[1];  
	 for (int i = 0; i < propertyListType.length; i++) {
	   propertyListType[i] = new PropertyListType();
	   // TODO fill info
	   propertyListType[i].setSelected(Boolean.TRUE);//?
	   propertyListType[i].setProperty(loadIDPropertyType(features));//?								  
	 }   	 
     return propertyListType;
   }
 
  // Loads Properties for InterfaceData element
   protected PropertyType[] loadIDPropertyType(FeatureState[] features ) {	
	//init default properties values, it should be changed
	 Hashtable defaultValues = new Hashtable();
	 defaultValues.put("Enabled", "true");

	 PropertyState[] featureProperties;
	 Vector newProperties = new Vector();     
	 for (int i = 0; i < features.length; i++) {
	   if (features[i].getFeatureProperties() == null) {	
   	     newProperties.addElement(generatePropertyType("*", features[i].getName(), "*"));
	   } else {
         featureProperties = features[i].getFeatureProperties().getProperty();
  	     for (int j = 0; j < featureProperties.length; j++) {
  	       newProperties.addElement(generatePropertyType(featureProperties[j].getName(), features[i].getName(), featureProperties[j].getValue()));
  	   }	
	 }
   }
   return convertToArray(newProperties);
  }

  /* Method for converting Vector with PropertyType */
  private PropertyType[] convertToArray(Vector newProperties) {
	PropertyType[] pTypeArr = new PropertyType[newProperties.size()];
	newProperties.copyInto(pTypeArr);
	return pTypeArr;
  }
  
  protected String generateName(String packageName, String fileName, String fileExt) {
	if (!"".equals(packageName.trim())) {
	  fileName = packageName + "." + fileName;
	}
	int index = fileName.indexOf(fileExt);
	if (index != -1) {
	  fileName = fileName.substring(0, index);
	}
	return fileName;
  } 
}
