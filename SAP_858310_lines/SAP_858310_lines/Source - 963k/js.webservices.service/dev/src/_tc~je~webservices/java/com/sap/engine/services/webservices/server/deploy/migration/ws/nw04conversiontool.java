package com.sap.engine.services.webservices.server.deploy.migration.ws;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;

import com.sap.engine.lib.descriptors.ws04vi.VirtualInterfaceState;
import com.sap.engine.lib.descriptors.ws04wsd.WebServiceDefinitionState;
import com.sap.engine.lib.descriptors.ws04wsdd.WSDeploymentDescriptor;
import com.sap.engine.lib.descriptors.ws04wsdd.WSDescriptor;
import com.sap.engine.lib.descriptors.ws04wsrt.WSRuntimeDescriptor;
import com.sap.engine.lib.processor.SchemaProcessorFactory;
import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.configuration.ServiceCollection;
import com.sap.engine.services.webservices.espbase.mappings.MappingRules;
import com.sap.engine.services.webservices.server.deploy.migration.ws.exception.ConversionException;
import com.sap.engine.services.webservices.tools.ExceptionManager;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Class for converting NW04 WebService archives to Paris WebService format.
 * Company: SAP Labs Sofia 
 * @author aneta-a
 */
public class NW04ConversionTool {
  private static final Location LOCATION = Location.getLocation(NW04ConversionTool.class); 

  //hashtable containing WSDeploymentDescriptors for all parsed ws-deployment-descriptor.xml files
//  private static Hashtable descriptorsLoaded = new Hashtable();  	
  
  //loads WSDeploymentDescriptor from ws-deployment-descriptor.xml file only if the file is not already loaded
//  public static WSDeploymentDescriptor loadWSDeploymentDescriptor(String wsDDFile) throws Exception {
//	  WSDeploymentDescriptor wsDescriptor = (WSDeploymentDescriptor)descriptorsLoaded.get(wsDDFile);
//	  if(wsDescriptor == null) {
//	    wsDescriptor = WSDeploymentParser.loadWSDeploymentDescriptor(wsDDFile); 
//	    if (wsDescriptor != null) descriptorsLoaded.put(wsDDFile, wsDescriptor);
//	  }
//	  return wsDescriptor;
//  } 
//  
//  public static WSRuntimeDescriptor loadWSRuntimeDescriptor(String wsRDFile) throws Exception {
//	  WSRuntimeDescriptor wsRuntimeDescriptor = (WSRuntimeDescriptor)descriptorsLoaded.get(wsRDFile);
//	  if(wsRuntimeDescriptor == null) {
//		  wsRuntimeDescriptor = WSRuntimeParser.loadWSRuntimeDescriptor(wsRDFile); 
//	    if (wsRuntimeDescriptor != null) descriptorsLoaded.put(wsRDFile, wsRuntimeDescriptor);
//	  }
//	  return wsRuntimeDescriptor;
//  }
  
  public static WSDeploymentDescriptor loadWSDeploymentDescriptor(String wsDDFile) throws Exception {
		WSDeploymentDescriptor wsDeploymentDescriptor;
		try {
			wsDeploymentDescriptor = (WSDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSDD).parse(wsDDFile);
		} catch (Exception e) {
      ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "loadWSDeploymentDescriptor(String wsDDFile)", "Loading of " + wsDDFile + " is unsuccessful. The application can't be started.", e);    
      throw e;
		}   
    return wsDeploymentDescriptor;
  } 
  
  public static WSRuntimeDescriptor loadWSRuntimeDescriptor(String wsRDFile) throws Exception {
		WSRuntimeDescriptor wsRuntimeDescriptor = null;
		try {
			wsRuntimeDescriptor = (WSRuntimeDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSRT).parse(wsRDFile);
		} catch (Exception e) {
      ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "loadWSRuntimeDescriptor(String wsRDFile)", "Loading of " + wsRDFile + " is unsuccessful. The application can't be started.", e);          
      throw e;
		}
    return wsRuntimeDescriptor;
  }
  
  //loads VirtualInterfaceState object from *.videf file
  private VirtualInterfaceState loadVi(String nw04ViFile) throws Exception {
		VirtualInterfaceState virtualInterfaceState;
    FileInputStream nw04ViStream = null;
    try {
      nw04ViStream = new FileInputStream(nw04ViFile);
			virtualInterfaceState = (VirtualInterfaceState)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04VI).parse(nw04ViStream);
		} catch (Exception e) {
      ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "loadVi(String nw04ViFile)", "Loading of " + nw04ViFile + " is unsuccessful. The application can't be started.", e);                
      throw e;
		}  finally {
      if (nw04ViStream != null) {
       try {
         nw04ViStream.close(); 
       } catch(IOException e) {
        //$JL-EXC$
       }
     }
  } 
	  return virtualInterfaceState;
  }
  
  //loads WebServiceDefinitionState object from *.wsdef file
  private WebServiceDefinitionState loadWsd(String nw04WsdFile) throws Exception { 
		WebServiceDefinitionState webServiceDefinitionState;
    FileInputStream nw04WsdStream = null;
    try {
			nw04WsdStream = new FileInputStream(nw04WsdFile);
			webServiceDefinitionState = (WebServiceDefinitionState)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSD).parse(nw04WsdStream);
		} catch (Exception e) {
      ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "loadWsd(String nw04WsdFile)", "Loading of " + nw04WsdFile + " is unsuccessful. The application can't be started.", e);          
      throw e;
		}  finally {
       if (nw04WsdStream != null) {
         try {
         nw04WsdStream.close(); 
        } catch(IOException e) {
          //$JL-EXC$
        }
     }
   }
    return webServiceDefinitionState;
  }
  
  public Hashtable nw04ToParisConversion(String wsDDFile, String wsRDFile, Hashtable vi_wsdStructure, String applicationName) throws Exception {  			
	  return nw04ToParisConversion(loadWSDeploymentDescriptor(wsDDFile), loadWSRuntimeDescriptor(wsRDFile), vi_wsdStructure, applicationName);
  }

  public Hashtable nw04ToParisConversion(WSDeploymentDescriptor nw04DeployDescriptor, WSRuntimeDescriptor wsRuntimeDescriptor, Hashtable vi_wsdStructures, String applicationName) throws ConversionException {		
	  Hashtable convertedObjects = new Hashtable();
    //inits MappingRules object for mapping.xml file
    NW04toMapping nw04Mapping = new NW04toMapping();
    
	  //inits Configuration-Root object
	  ConfigurationRoot configurationRoot = new ConfigurationRoot();
	  WSDescriptor[]    nw04Webservice    = nw04DeployDescriptor.getWebservice();		
	  //fills it with data from the ws-deployment-descriptor.xml and the webservice definition file
		try {
      ServiceCollection serviceCollection = new NW04toRTConfiguration().loadRTConfiguration(nw04Webservice, vi_wsdStructures, applicationName, nw04Mapping.getOperationProperties());
			configurationRoot.setRTConfig(serviceCollection); 
      configurationRoot.setDTConfig(new NW04toDTConfiguration().loadDTConfiguration(nw04Webservice, vi_wsdStructures, applicationName, nw04Mapping.getOperationProperties(), serviceCollection));

		} catch (ConversionException e) {
      ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "nw04ToParisConversion(WSDeploymentDescriptor nw04DeployDescriptor, WSRuntimeDescriptor wsRuntimeDescriptor, Hashtable vi_wsdStructures, String applicationName)", "Unable to load RTConfig (runtime configuration data). The application can't be started.", e);                
      throw e;
		}
    
	  MappingRules  mappings    = new MappingRules();			
	  //fills it with data from the ws-deployment-descriptor.xml and the virtualinterface file
		try {
			mappings.setInterface(nw04Mapping.loadInterface(vi_wsdStructures, nw04Webservice, wsRuntimeDescriptor.getWsRuntimeConfiguration(), applicationName));
		} catch (Exception e1) {
      ExceptionManager.logThrowable(Severity.FATAL, Category.SYS_SERVER, LOCATION, "nw04ToParisConversion(WSDeploymentDescriptor nw04DeployDescriptor, WSRuntimeDescriptor wsRuntimeDescriptor, Hashtable vi_wsdStructures, String applicationName)", "Unable to load interface mapping data in mappings.xml.", e1);                     
      throw new ConversionException(MigrationConstants.PROBLEM_LOAD_INTERFACE, e1);
		}
	  //mappings.setService(nw04Mapping.loadService());		
	
	  //keys are the root elements of the xml files ConfigurationRoot and mappings
	  convertedObjects.put("ConfigurationRoot", configurationRoot);
	  convertedObjects.put("mappings", mappings);

    return convertedObjects;
  }  
}
