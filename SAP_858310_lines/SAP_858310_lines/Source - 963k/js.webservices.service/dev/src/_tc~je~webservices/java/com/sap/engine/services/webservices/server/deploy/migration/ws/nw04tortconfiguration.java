package com.sap.engine.services.webservices.server.deploy.migration.ws;

import java.util.Hashtable;

import com.sap.engine.lib.descriptors.ws04vi.VirtualInterfaceState;
import com.sap.engine.lib.descriptors.ws04wsd.WebServiceDefinitionState;
import com.sap.engine.lib.descriptors.ws04wsdd.QNameDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.WSConfigurationDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.WSDescriptor;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationMarshallerException;
import com.sap.engine.services.webservices.espbase.configuration.IConfigurationMarshaller;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.configuration.ServiceCollection;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.configuration.URLSchemeType;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.server.deploy.migration.ws.exception.ConversionException;
import com.sap.engine.services.webservices.tools.ExceptionManager;
import com.sap.engine.services.wssec.srt.features.designtimetoruntime.RTimePropertyGenerator;
import com.sap.engine.services.wssec.srt.features.exception.FeaturePropagationException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Class for loading the whole data from the ws-desployment-descriptor.xml 
 * in the runtime configuration part of the configuration.xml
 * Company: Sap Labs Sofia 
 * @author aneta-a
 */
  class NW04toRTConfiguration extends Configuration {
    
  private static final Location LOCATION = Location.getLocation(NW04toRTConfiguration.class); 
	
	/*
	 * Loads RTConfig element in ConfigurationRoot.
	 */
	public ServiceCollection loadRTConfiguration(WSDescriptor[] nw04Webservice, Hashtable vi_wsdStructures, String applicationName, Hashtable operationProps) throws ConversionException {
    operationProperties = operationProps;
    ServiceCollection rTConfig = new ServiceCollection();
    rTConfig.setService(loadService(nw04Webservice, vi_wsdStructures, applicationName));	  
	  return rTConfig;
	}
	
	/*
	 * Loads Service element of RTConfig element.
	 */
	private Service[] loadService(WSDescriptor[] nw04Webservice, Hashtable vi_wsdStructures, String applicationName) throws ConversionException {
	  Service[] service = new Service[nw04Webservice.length];    
	  for (int i = 0; i < nw04Webservice.length; i++) {
		  service[i] = new Service();
      // TODO fill data
		  //set Service attributes
		  service[i].setName(nw04Webservice[i].getWebserviceInternalName());		
		  service[i].setId("");//?
		  service[i].setType(new Integer(0));//?		
		  //set Service elements
		  service[i].setServiceData(loadServiceData(nw04Webservice[i], vi_wsdStructures, applicationName));
		  service[i].getServiceData().setContextRoot("");
	  }
	  return service;
	}
	/*
	 * Loads ServiceData element of Service element.
	 */
	private ServiceData loadServiceData(WSDescriptor nw04Webservice, Hashtable vi_wsdStructures, String applicationName) throws ConversionException {
	  ServiceData serviceData = new ServiceData();		  	
      // TODO fill data	
	  //set ServiceData attributes
	  QNameDescriptor webserviceName = nw04Webservice.getWebserviceName();
	  serviceData.setName(webserviceName.getLocalName());
	  serviceData.setNamespace(webserviceName.getNamespaceURI());
	  serviceData.setDomainId("");//?
	  //set ServiceData elements
	  //serviceData.setPropertyList(loadPropertyList());//? service data property list
    BindingData[] bindingData = loadBindingData(nw04Webservice.getWsConfiguration(), vi_wsdStructures, applicationName, nw04Webservice.getWebserviceInternalName());
	  for (int i = 0; i < bindingData.length; i++) {      
//			try {
//			new RTimePropertyGenerator(RTimePropertyGenerator.WSCONF_PROPAGATION).modifyBindingData(bindingData[i]);
//			} catch (FeaturePropagationException e) {
//        throw new ConversionException("Exception occurred while converting security properties for binding data " + bindingData[i].getUrl(), e);
//			}
      BindingData newBD = bindingData[i].makeCopyWithoutProperties();
      
      try {
        LOCATION.debugT("loadServiceData(): calling ConfigurationBuilder.convertNW04CfgToNy() with ...");
        WSContainer.getRuntimeProcessingEnv().getWSDLVisualizer().getInternalCFGBuilder().convertNW04CfgToNY(bindingData[i], newBD, IConfigurationMarshaller.PROVIDER_MODE);
      } catch (ConfigurationMarshallerException e) {
        throw new ConversionException("Exception occurred while converting properties.", e);
      }
      bindingData[i] = newBD;
		}
    serviceData.setBindingData(bindingData);
	  serviceData.setUDDIEntity(loadUDDIEntity());//?
	  return serviceData;
	}
	
	/*
	 * Loads BindingData element of ServiceData element.
	 */
	private BindingData[] loadBindingData(WSConfigurationDescriptor[] nw04WSConfiguration, Hashtable vi_wsdStructures, String applicationName, String webserviceInternalName) throws ConversionException {
	  BindingData[] bindData = new BindingData[nw04WSConfiguration.length];     
	  for (int i = 0; i < bindData.length; i++) {
		  bindData[i] = new BindingData();
		  // TODO fill info
		  // set BindingData attributes	
		  bindData[i].setName(nw04WSConfiguration[i].getServiceEndpointName().getLocalName());
		  bindData[i].setActive(true);
//		bindData[i].setEditable(new Boolean(true)); this value is true by default
		  bindData[i].setUrl(nw04WSConfiguration[i].getTransportAddress());
		 
      VirtualInterfaceState viStructure = ((VirtualInterfaceState)vi_wsdStructures.get(nw04WSConfiguration[i].getServiceEndpointViRef().getName()));
      WebServiceDefinitionState wsdStructure = ((WebServiceDefinitionState)vi_wsdStructures.get(nw04WSConfiguration[i].getWebserviceDefinitionRef().getName()));    

      com.sap.engine.lib.descriptors.ws04wsdd.QNameDescriptor
      bindingQNameDescriptor = nw04WSConfiguration[i].getTransportBinding().getWsdlBindingName();
		  bindData[i].setBindingName(bindingQNameDescriptor.getLocalName().trim());
		  bindData[i].setBindingNamespace("urn:" + wsdStructure.getName() + "/" + nw04WSConfiguration[i].getConfigurationName());
		  bindData[i].setUrlScheme(URLSchemeType.http); //TODO add check for HTTPS.
      bindData[i].setConfigurationId(nw04WSConfiguration[i].getConfigurationName());
      
		  bindData[i].setVariantName(generateName(wsdStructure.getPackage(), wsdStructure.getName(), ".wsdef"));     
      String bindingDataId = viStructure.getName();
      if (!viStructure.getPackage().trim().equals("")) {
        bindingDataId = viStructure.getPackage() + "_" + bindingDataId;
      }
      bindData[i].setInterfaceId(applicationName + "_" + bindingDataId);
      bindData[i].setInterfaceMappingId(applicationName + "_" + webserviceInternalName + "_" + nw04WSConfiguration[i].getServiceEndpointName().getLocalName());
		  bindData[i].setGroupConfigId("");//?
		// set BindingData elements	

			try {
				bindData[i].setPropertyList(loadPropertyList(nw04WSConfiguration[i].getGlobalFeatures().getFeature(), null));      
			} catch (ConversionException e) {
        ExceptionManager.logThrowable(Severity.ERROR, Category.SYS_SERVER, LOCATION, "loadBindingData(WSConfigurationDescriptor[] nw04WSConfiguration, Hashtable vi_wsdStructures, String applicationName, String webserviceInternalName)", "Error occurred while trying to load global properties in BindingData property list. The application can't be started.", e);
        throw e;
			}

			try {
				bindData[i].setOperation(loadOperation(nw04WSConfiguration[i].getOperationConfiguration(), viStructure));
			} catch (ConversionException e1) {
        ExceptionManager.logThrowable(Severity.ERROR, Category.SYS_SERVER, LOCATION, "loadBindingData(WSConfigurationDescriptor[] nw04WSConfiguration, Hashtable vi_wsdStructures, String applicationName, String webserviceInternalName)", "Error occurred while trying to load operations in BindingData operations. The application can't be started.", e1);
        throw e1;
			}
		  bindData[i].setUDDIEntity(loadUDDIEntity());//?
	   }	 
	  return bindData;
	 }   
}
