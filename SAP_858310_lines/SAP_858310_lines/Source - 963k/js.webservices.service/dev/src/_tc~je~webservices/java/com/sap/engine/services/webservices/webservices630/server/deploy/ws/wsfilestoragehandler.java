package com.sap.engine.services.webservices.webservices630.server.deploy.ws;

import com.sap.engine.services.webservices.runtime.definition.WSRuntimeDefinition;
import com.sap.engine.services.webservices.runtime.definition.JavaToQNameMappingRegistryImpl;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.lib.descriptors.ws04wsdd.WSDeploymentDescriptor;
import com.sap.engine.lib.descriptors.ws04wsrt.WSRuntimeDescriptor;
import com.sap.engine.lib.processor.SchemaProcessorFactory;
import com.sap.tc.logging.Location;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import java.util.HashMap;

/**
 * Title: WSFileStorageHandler
 * Description: The class provides methods for saving web services deployment files.
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSFileStorageHandler {

  public void saveDescriptors(WSDeploymentInfo wsDeploymentInfo) throws WSDeploymentException {
    WSRuntimeDefinition wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String excMsg = "Error occurred, trying to save descriptors for web service " + wsIdentifier.getServiceName() + ", application " + wsIdentifier.getApplicationName() + ". ";

    try {
      saveMappings(wsRuntimeDefinition);
      saveWSDeploymentDescriptor(wsDeploymentInfo);
      //saveWSRuntimeDescriptor(wsDeploymentInfo.getWsRuntimeDefinition());
      saveVIs(wsDeploymentInfo);
      saveWsd(wsDeploymentInfo);
      saveDoc(wsDeploymentInfo);
    } catch(WSDeploymentException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  public void saveMappings(WSRuntimeDefinition wsRuntimeDefinition) throws WSDeploymentException {
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String excMsg = "Error occurred, trying to save mappings for application " + wsIdentifier.getApplicationName() + ", ws client " + wsIdentifier.getServiceName() + ". ";

    Properties mappigns = wsRuntimeDefinition.getWsDirsHandler().getMappings();
    String mappingsPath = wsRuntimeDefinition.getWsDirsHandler().getMappingsPath();
    IOUtil.createParentDir(new String[]{mappingsPath});

    OutputStream out = null;
    try {
      
      out = new FileOutputStream(mappingsPath);
      mappigns.store(out, "Properties file, specifying the mapping for the web service directory names.");
    } catch(IOException e) {
      Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsDeployLocation.catching(e);

      Object[] args = new String[]{excMsg};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    } finally {
      if(out != null) {
        try {
          out.close();
        } catch(IOException iExc) {
          Location wsDeployLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
          String msg = "Warning! " + excMsg + "Unable to close output stream for file  " + mappingsPath + ". ";
          wsDeployLocation.catching(msg, iExc);
        }
      }
    }
  }

  public void saveWSDeploymentDescriptor(WSDeploymentInfo wsDeploymentInfo) throws WSDeploymentException {
    String excMessage = "Error occurred saving " + WebServicesConstants.WS_DEPLOYMENT_DESCRIPTOR  + ". ";

    WSRuntimeDefinition wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String wsDeploymentDescriptorPath = wsRuntimeDefinition.getWsDirsHandler().getWSDeploymentDescriptorPath();
    IOUtil.createParentDir(new String[]{wsDeploymentDescriptorPath});

    WSDeploymentDescriptor wsDeploymentDescriptor = wsDeploymentInfo.getWsDeploymentDescriptor();
    FileOutputStream wsDeploymentDescriptorOutput = null;

    try {
      wsDeploymentDescriptorOutput = new FileOutputStream(wsDeploymentDescriptorPath);
      SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSDD).build(wsDeploymentDescriptor, wsDeploymentDescriptorOutput);
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to save ws-deployment-descriptor", wsIdentifier.getApplicationName(), wsIdentifier.getJarName(), wsIdentifier.getServiceName()};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    } finally {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      String msg = excMessage +
                   WebServicesConstants.LINE_SEPARATOR + wsIdentifier.toString() +
                   WebServicesConstants.LINE_SEPARATOR + "Unable to close stream for file: " + wsDeploymentDescriptorPath;
      IOUtil.closeOutputStreams(new OutputStream[]{wsDeploymentDescriptorOutput}, new String[]{msg}, wsLocation);
    }
  }

  public void saveWSRuntimeDescriptor(WSRuntimeDefinition wsRuntimeDefinition) throws WSDeploymentException {
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    String excMsg = "Error occurred, trying to generate and/or save " + WebServicesConstants.WS_RUNTIME_DESCRIPTOR  + ", application " + wsIdentifier.getApplicationName() + ", service " + wsIdentifier.getServiceName() + ". ";

    WSRuntimeDescriptor wsRuntimeDescriptor = new WSDefinitionFactory().getWSRuntimeDescriptor(wsRuntimeDefinition);
    String wsRuntimeDescriptorPath = wsRuntimeDefinition.getWsDirsHandler().getWSRuntimeDescriptorPath();
    IOUtil.createParentDir(new String[]{wsRuntimeDescriptorPath});

    try {
      SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04WSRT).build(wsRuntimeDescriptor, wsRuntimeDefinition.getWsDirsHandler().getWSRuntimeDescriptorPath());
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{excMsg, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WS_COMMON_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  public void saveVIs(WSDeploymentInfo wsDeploymentInfo) throws WSDeploymentException {
    try {
      WSRuntimeDefinition wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();
      ArrayList virualInterfaces = wsDeploymentInfo.getVirtualInterfaces();
      ArrayList virtualInterfaceEntries = wsDeploymentInfo.getVirtualInterfaceEntries();
      int viSize = virualInterfaces.size();
      for (int i = 0; i < viSize; i++) {
        String viSourcePath = (String)virualInterfaces.get(i);
        String viEntry = (String)virtualInterfaceEntries.get(i);
        String viDestPath = wsRuntimeDefinition.getWsDirsHandler().getViPath(viEntry);
        IOUtil.createParentDir(new String[]{viDestPath});

        IOUtil.copyFile(viSourcePath, viDestPath);
      }
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      WSIdentifier wsIdentifier = wsDeploymentInfo.getWsRuntimeDefinition().getWSIdentifier();
      Object[] args = new Object[]{"Unable to save virtual interface files", wsIdentifier.getApplicationName(), wsIdentifier.getJarName(), wsIdentifier.getServiceName()};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  public void saveWsd(WSDeploymentInfo wsDeploymentInfo) throws WSDeploymentException {
    try {
      WSRuntimeDefinition wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();
      String wsdSourcePath = wsDeploymentInfo.getWsdRef();
      String wsdDestPath = wsRuntimeDefinition.getWsDirsHandler().getWsdPath(wsDeploymentInfo.getWsdRefEntry());
      IOUtil.createParentDir(new String[]{wsdDestPath});

      IOUtil.copyFile(wsdSourcePath, wsdDestPath);
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      WSIdentifier wsIdentifier = wsDeploymentInfo.getWsRuntimeDefinition().getWSIdentifier();
      Object[] args = new Object[]{"Unable to save wsd file", wsIdentifier.getApplicationName(), wsIdentifier.getJarName(), wsIdentifier.getServiceName()};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  public void saveDoc(WSDeploymentInfo wsDeploymentInfo) throws WSDeploymentException {
    try {
      WSRuntimeDefinition wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();
      if (wsDeploymentInfo.getWsDocPath() != null) {
        String docSourcePath = wsDeploymentInfo.getWsDocPath();
        String docDestPath =  wsRuntimeDefinition.getWsDirsHandler().getDocPath(wsDeploymentInfo.getWsDocEntry());
        IOUtil.createParentDir(new String[]{docDestPath});

        IOUtil.copyFile(docSourcePath, docDestPath);
      }
    } catch(IOException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      WSIdentifier wsIdentifier = wsDeploymentInfo.getWsRuntimeDefinition().getWSIdentifier();
      Object[] args = new Object[]{"Unable to save webservice documentation file", wsIdentifier.getApplicationName(), wsIdentifier.getJarName(), wsIdentifier.getServiceName()};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  public void saveOutsideIn(WSDeploymentInfo wsDeploymentInfo) throws WSDeploymentException {
    WSRuntimeDefinition wsRuntimeDefinition = wsDeploymentInfo.getWsRuntimeDefinition();
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();

    try {
      String javaQNameMappingDestPath = wsRuntimeDefinition.getWsDirsHandler().getOutsideInJavaToQNameMapppingPath(wsRuntimeDefinition.getOutsideInDefinition().getJavaQNameMappingFile());
      String javaQNameMappingSourcePath = wsDeploymentInfo.getJavaQNameMappingRefPath();
      IOUtil.createParentDir(new String[]{javaQNameMappingSourcePath});
      IOUtil.copyFile(javaQNameMappingDestPath, javaQNameMappingSourcePath);

      String rootWsdlSourcePath = wsDeploymentInfo.getWsdlRefPath();
      String rootWsdlEntry = wsRuntimeDefinition.getOutsideInDefinition().getWsdlRelPath();
      String wsdlSourceDir = rootWsdlSourcePath.substring(0, rootWsdlSourcePath.lastIndexOf(rootWsdlEntry));
      String wsdlDestDir = wsRuntimeDefinition.getWsDirsHandler().getOutsideInWsdlDir();

      IOUtil.copyDir(wsdlSourceDir, wsdlDestDir);
    } catch(IOException e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to save webservice outside in files file", wsIdentifier.getApplicationName(), wsIdentifier.getJarName(), wsIdentifier.getServiceName()};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  public void saveJavaToQNameMappingFile(WSRuntimeDefinition wsRuntimeDefinition, HashMap[] mappings) throws WSDeploymentException {
    String javaQNameMappingPath = wsRuntimeDefinition.getWsDirsHandler().getJavaToQNameMappingPath();
    IOUtil.createParentDir(new String[]{javaQNameMappingPath});

    try {
      HashMap literalMappings = mappings[0];
      HashMap encodedMappings = mappings[1];

      JavaToQNameMappingRegistryImpl javaToQNameMappingRegistry = new JavaToQNameMappingRegistryImpl();
      javaToQNameMappingRegistry.setLiteralMappings(literalMappings);
      javaToQNameMappingRegistry.setEncodedMappings(encodedMappings);

      javaToQNameMappingRegistry.saveToFile(new File(javaQNameMappingPath));
    } catch(Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to save " + javaQNameMappingPath, "none"};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

}
