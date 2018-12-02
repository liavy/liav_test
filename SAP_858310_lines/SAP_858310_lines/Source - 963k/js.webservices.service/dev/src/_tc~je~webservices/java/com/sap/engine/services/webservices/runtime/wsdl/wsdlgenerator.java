package com.sap.engine.services.webservices.runtime.wsdl;

import com.sap.engine.services.webservices.runtime.definition.*;
import com.sap.engine.services.webservices.runtime.interfaces.RuntimeTransportBinding;
import com.sap.engine.services.webservices.runtime.TransportBindingProvider;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.VISchemasInfo;
import com.sap.engine.services.webservices.wsdl.*;
import com.sap.engine.interfaces.webservices.server.deploy.WSDeploymentException;
import com.sap.engine.interfaces.webservices.server.accessors.WSInterfacePatternKeys;
import com.sap.engine.interfaces.webservices.runtime.JavaToQNameMappingRegistry;
import com.sap.engine.interfaces.webservices.runtime.PortTypeDescriptor;
import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.tc.logging.Location;

import javax.xml.namespace.QName;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2004
 * Company: Sap Labs Sofia
 * 
 * @author  Dimiter Angelov
 * @version 6.30
 */

public class WSDLGenerator {

  private static final String GENERATE_ALL_STYLES  =  "generate_all_styles";

  private TransportBindingProvider trBindingProvider = null;

  public WSDLGenerator(TransportBindingProvider trBindingProvider) {
    this.trBindingProvider = trBindingProvider;
  }

  public void generateStandAloneWsdls(WSRuntimeDefinition wsRuntimeDefinition, Hashtable viSchemasInfoes, String outputDir, String style, String hostAddress, boolean sapMode) throws WSDeploymentException {
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();
    try {
      VISchemasInfo viSchemaInfo;
      ServiceEndpointDefinition endpoint;

      ServiceEndpointDefinition definitions[] = wsRuntimeDefinition.getServiceEndpointDefinitions();
      HashMapObjectObject hashTable = new HashMapObjectObject();

      for (int i = 0; i < definitions.length; i++) { //traversing all configuration
        endpoint = definitions[i];
        viSchemaInfo = (VISchemasInfo) viSchemasInfoes.get(endpoint.getViRelPath());

        generateEndpointWSDLs(endpoint, wsRuntimeDefinition, viSchemaInfo, outputDir, style, sapMode);
        hashTable.put(endpoint.getTransportBindingId(), trBindingProvider.getTransportBinding(endpoint.getTransportBindingId()));
      } //end wsRuntime processing

      WSDLDefinitions def = ServiceGenerator.generateSOAPHTTPServiceDefinitionsInternal(wsRuntimeDefinition.getWsQName().getLocalPart(), hostAddress, wsRuntimeDefinition, style, sapMode, hashTable, true, false, null);
      WSDLDefinitionsParser parser = new WSDLDefinitionsParser();
      parser.parseDefinitionsToFile(def, new File(outputDir + "/main.wsdl"));

    } catch (Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to generate web service's wsdls ", wsIdentifier.getApplicationName(), wsIdentifier.getJarName(), wsIdentifier.getServiceName()};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  public void generateWsdls(WSRuntimeDefinition wsRuntimeDefinition, Hashtable viSchemasInfoes, HashMap mappings[]) throws WSDeploymentException {
    WSIdentifier wsIdentifier = wsRuntimeDefinition.getWSIdentifier();

    try {
      VISchemasInfo viSchemaInfo;
      ServiceEndpointDefinition endpoint;
      String[] endpointStyles;
      ArrayList mergedSupportedStyles = new ArrayList();

      JavaToQNameMappingRegistryImpl javaToQNameRegistry = new JavaToQNameMappingRegistryImpl();
      javaToQNameRegistry.setLiteralMappings(mappings[0]);
      javaToQNameRegistry.setEncodedMappings(mappings[1]);
      wsRuntimeDefinition.setJavaToQNameMappingRegistry(javaToQNameRegistry);

      ServiceEndpointDefinition definitions[] = wsRuntimeDefinition.getServiceEndpointDefinitions();

      for (int i = 0; i < definitions.length; i++) { //traversing all configuration
        endpoint = definitions[i];
        viSchemaInfo = (VISchemasInfo) viSchemasInfoes.get(endpoint.getViRelPath());

        //generation of runtime-wsdl
        generateEndpointWSDLs(endpoint, wsRuntimeDefinition, viSchemaInfo, null, null, false);
        generateEndpointWSDLs(endpoint, wsRuntimeDefinition, viSchemaInfo, null, null, true);

        endpointStyles = ((RuntimeTransportBinding)trBindingProvider.getTransportBinding(endpoint.getTransportBindingId())).getSupportedSyles();
        for (int j = 0; j < endpointStyles.length; j++) {
          //adding the styles in the supported list
          if (! mergedSupportedStyles.contains(endpointStyles[j])) {
            mergedSupportedStyles.add(endpointStyles[j]);
          }
        }

        //generation of standalone wsdls
        File res = new File(wsRuntimeDefinition.getWsDirsHandler().getWsdlDir(), "alone");
        generateEndpointWSDLs(endpoint, wsRuntimeDefinition, viSchemaInfo, res.getAbsolutePath(), GENERATE_ALL_STYLES, false);
        generateEndpointWSDLs(endpoint, wsRuntimeDefinition, viSchemaInfo, res.getAbsolutePath(), GENERATE_ALL_STYLES, true);

      } //end wsRuntime processing

      wsRuntimeDefinition.setWsdlSupportedStyles((String[]) mergedSupportedStyles.toArray(new String[mergedSupportedStyles.size()]));

    } catch (Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      Object[] args = new Object[]{"Unable to generate web service's wsdls ", wsIdentifier.getApplicationName(), wsIdentifier.getJarName(), wsIdentifier.getServiceName()};
      throw new WSDeploymentException(WSInterfacePatternKeys.WEBSERVICES_DEPLOYMENT_EXCEPTION, args, e);
    }
  }

  private String getPortTypeTNS(String wsdName, String viName, String style) {
     return "urn:" + wsdName + "/" + viName + "/" + style;
   }

   private String getBindingTNS(String wsdName, String configName, String style) {
     return "urn:" + wsdName + "/" + configName + "/" + style;
   }

   private String getPortTypeImportLocation(ServiceEndpointDefinition endpoint, String style, boolean sapMode) {
     if (sapMode) {
       return endpoint.getServiceEndpointId() + "/porttypes?wsdl&" + "style=" + style + "&mode=sap_wsdl";
     } else {
       return endpoint.getServiceEndpointId() + "/porttypes?wsdl&" + "style=" + style;
     }
   }

  private String getOperationsNS(String wsdName, String viName) {
     return "urn:" + wsdName + "/" + viName;
   }

  private DefinitionsHolder getBindingWSDLDefinitions(ArrayList imports, String targetNamespace) throws Exception {
    String prefix = "prt";

    WSDLDefinitions definitions = new WSDLDefinitions();
    definitions.targetNamespace = targetNamespace;

    String portTypeTNS;
    String portTypeLocation;
    PortTypeInfo ptInfo;
    ArrayList importDeclarations = new ArrayList(imports.size());

    for (int i = 0; i < imports.size(); i++) {
      ptInfo = (PortTypeInfo) imports.get(i);
      portTypeTNS = ptInfo.portTypeDescriptor.getQName().getNamespaceURI();
      portTypeLocation = ptInfo.importLocation;
      importDeclarations.add(new WSDLImport(null, portTypeLocation, portTypeTNS));
      definitions.addAdditionalAttribute("xmlns:" + prefix + i, portTypeTNS);
    }

    definitions.setImportDeclaratuions(importDeclarations);

    PortTypeDescriptor[] result = new PortTypeDescriptor[imports.size()];

    for (int i = 0; i < result.length; i++) {
     result[i] = ((PortTypeInfo) imports.get(i)).portTypeDescriptor;
    }

    return  new DefinitionsHolder(definitions, result);
  }

  private String getBindingOutputFilePath(String outDir, String confName, String style, boolean sapMode) {
    if (sapMode) {
      return outDir + "/bindings/" + confName + "_" + style + "_sap.wsdl";
    } else {
      return outDir + "/bindings/" + confName + "_" + style + ".wsdl";
    }
  }

  private String getPortTypeOutputFilePath(String outDir, String viName, String confName, String style, boolean sapMode) {
    if (sapMode) {
      return outDir + "/porttypes/" + confName + "_" + viName + "_" + style + "_sap.wsdl";
    } else {
      return outDir + "/porttypes/" + confName + "_" + viName + "_" + style + ".wsdl";
    }
  }

  private void generateEndpointWSDLs(ServiceEndpointDefinition endpoint, WSRuntimeDefinition wsRuntimeDefinition, VISchemasInfo viSchemaInfo, String outputDir, String specificStyle, boolean sapMode) throws Exception {
   RuntimeTransportBinding transportBinding = (RuntimeTransportBinding)trBindingProvider.getTransportBinding(endpoint.getTransportBindingId());
   JavaToQNameMappingRegistry javaToQNameRegistry = wsRuntimeDefinition.getJavaToQNameMappingRegistry();
   String confName = endpoint.getConfigurationName();

   String styles[] = transportBinding.getSupportedSyles();
   int[] portTypeTypes = transportBinding.getNecessaryPortTypes();
   if (styles.length != portTypeTypes.length) {
     throw new WSDLCreationException(new Exception("The styles: " + styles.length + " and portTypes:" + portTypeTypes.length + "arrays differ"));
   }

   boolean isStandAlone = false;
   File outputDirFile = null;
   if (outputDir != null) { //this indicates that standAlone generation is required
     isStandAlone = true;
     outputDirFile = new File(outputDir);
     outputDirFile.mkdirs();

     if (specificStyle == null) { //in this case use the TB default styles
       styles = transportBinding.getDefaultStyles();
       portTypeTypes = getPortTypeCodes(transportBinding, transportBinding.getDefaultStyles());
     } else if (specificStyle.equals(GENERATE_ALL_STYLES)) {
       styles = transportBinding.getSupportedSyles();
       portTypeTypes = transportBinding.getNecessaryPortTypes();
     } else {
       int i;
       for (i = 0; i < styles.length; i++) {
         if (styles[i].equals(specificStyle)) {
           portTypeTypes = new int[]{portTypeTypes[i]};
           styles = new String[]{specificStyle};
           break;
         }
       }
       //there is nothing to generate so return from the method.
       if (i == styles.length) {
         return;
       }
     }
   }

   File portTypeFile;
   File bindingFile;
   String targetNamespace;
   DefinitionsHolder defHolder;
   ArrayList bindingImports = new ArrayList();
   WSDLDefinitionsParser wsdlDefinitionsParser = new WSDLDefinitionsParser();

   for (int pt = 0; pt < styles.length; pt++) { //traversing the styles

     bindingImports.clear();

     //creating the necessary portTypes
     int prtTCode = portTypeTypes[pt];

     //creation of rpc-encoded porttype
     if ((prtTCode & PortTypeDescriptor.ENCODED_PORTTYPE) == PortTypeDescriptor.ENCODED_PORTTYPE) {
       String portTypeType = PortTypeDescriptorImpl.getPortTypeType(PortTypeDescriptor.ENCODED_PORTTYPE);
       String portTypeName = getPortTypeName(viSchemaInfo.getViName(), portTypeType);
       targetNamespace = getPortTypeTNS(wsRuntimeDefinition.getWsdName(), viSchemaInfo.getViName(), portTypeType);
       String importLocation = null;
       if (isStandAlone) {
         portTypeFile = new File(getPortTypeOutputFilePath(outputDir, viSchemaInfo.getViName(), confName, portTypeType, sapMode));
         importLocation = getPortTypeOutputFilePath(getUpperDirsDots(outputDirFile, portTypeFile.getParentFile()), viSchemaInfo.getViName(), confName, portTypeType, sapMode);
       } else {
         portTypeFile = new File(endpoint.getVIWsdlPath(portTypeType, sapMode));
         importLocation = getPortTypeImportLocation(endpoint, portTypeType, sapMode);
       }
       PortTypeDescriptor desc = new PortTypeDescriptorImpl(PortTypeDescriptor.ENCODED_PORTTYPE, new QName(targetNamespace, portTypeName));
       bindingImports.add(new PortTypeInfo(desc, importLocation));

       if (! portTypeFile.exists()) { //this is the first generation
         portTypeFile.getParentFile().mkdirs(); //create the directory

         defHolder = WSDLPortTypeGenerator.generateRPCEncodedPortType(javaToQNameRegistry, viSchemaInfo.getEncodedSchemas(), endpoint.getOperations(), targetNamespace, portTypeName);
         //setting the WSD features stuf
         if (sapMode) {
           appendDesigntimeFeatures(defHolder.getDefinitions(), wsRuntimeDefinition.getDesigntimeFeatures());
         }
         wsdlDefinitionsParser.parseDefinitionsToFile(defHolder.getDefinitions(), portTypeFile);
         wsdlDefinitionsParser.init();
       }
     }

      //creation of document-literal porttype
     if ((prtTCode & PortTypeDescriptor.LITERAL_PORTTYPE) == PortTypeDescriptor.LITERAL_PORTTYPE) {
       String portTypeType = PortTypeDescriptorImpl.getPortTypeType(PortTypeDescriptor.LITERAL_PORTTYPE);
       String portTypeName = getPortTypeName(viSchemaInfo.getViName(), portTypeType);
       targetNamespace = getPortTypeTNS(wsRuntimeDefinition.getWsdName(), viSchemaInfo.getViName(), portTypeType);

       String importLocation = null;
       if (isStandAlone) {
         portTypeFile = new File(getPortTypeOutputFilePath(outputDir, viSchemaInfo.getViName(), confName, portTypeType, sapMode));
         importLocation = getPortTypeOutputFilePath(getUpperDirsDots(outputDirFile, portTypeFile.getParentFile()), viSchemaInfo.getViName(), confName, portTypeType, sapMode);
       } else {
         portTypeFile = new File(endpoint.getVIWsdlPath(portTypeType, sapMode));
         importLocation = getPortTypeImportLocation(endpoint, portTypeType, sapMode);
       }
       PortTypeDescriptor desc = new PortTypeDescriptorImpl(PortTypeDescriptor.LITERAL_PORTTYPE, new QName(targetNamespace, portTypeName));
       bindingImports.add(new PortTypeInfo(desc, importLocation));

       String operationsNS = getOperationsNS(wsRuntimeDefinition.getWsdName(), viSchemaInfo.getViName());

       if (! portTypeFile.exists()) { //this is the first generation
         portTypeFile.getParentFile().mkdirs(); //create the directory

         defHolder = WSDLPortTypeGenerator.generateDocumentLiteralPortType(javaToQNameRegistry, viSchemaInfo.getLiteralSchemas(), endpoint.getOperations(), targetNamespace, operationsNS, portTypeName);

         //setting the WSD features stuf
         if (sapMode) {
           appendDesigntimeFeatures(defHolder.getDefinitions(), wsRuntimeDefinition.getDesigntimeFeatures());
         }
         wsdlDefinitionsParser.parseDefinitionsToFile(defHolder.getDefinitions(), portTypeFile);
         wsdlDefinitionsParser.init();
       }
     }

      //creation of rpc-literal porttype
     if ((prtTCode & PortTypeDescriptor.RPC_LITERAL_PORTTYPE) == PortTypeDescriptor.RPC_LITERAL_PORTTYPE) {
       String portTypeType = PortTypeDescriptorImpl.getPortTypeType(PortTypeDescriptor.RPC_LITERAL_PORTTYPE);
       String portTypeName = getPortTypeName(viSchemaInfo.getViName(), portTypeType);
       targetNamespace = getPortTypeTNS(wsRuntimeDefinition.getWsdName(), viSchemaInfo.getViName(), portTypeType);

       String importLocation = null;
       if (isStandAlone) {
         portTypeFile = new File(getPortTypeOutputFilePath(outputDir, viSchemaInfo.getViName(), confName, portTypeType, sapMode));
         importLocation = getPortTypeOutputFilePath(getUpperDirsDots(outputDirFile, portTypeFile.getParentFile()), viSchemaInfo.getViName(), confName, portTypeType, sapMode);
       } else {
         portTypeFile = new File(endpoint.getVIWsdlPath(portTypeType, sapMode));
         importLocation = getPortTypeImportLocation(endpoint, portTypeType, sapMode);
       }
       PortTypeDescriptor desc = new PortTypeDescriptorImpl(PortTypeDescriptor.RPC_LITERAL_PORTTYPE, new QName(targetNamespace, portTypeName));
       bindingImports.add(new PortTypeInfo(desc, importLocation));

       String operationsNS = getOperationsNS(wsRuntimeDefinition.getWsdName(), viSchemaInfo.getViName());

       if (! portTypeFile.exists()) { //this is the first generation
         portTypeFile.getParentFile().mkdirs(); //create the directory

         defHolder = WSDLPortTypeGenerator.generateRPCLiteralPortType(javaToQNameRegistry, viSchemaInfo.getLiteralSchemas(), endpoint.getOperations(), targetNamespace, portTypeName, operationsNS);

         //setting the WSD features stuf
         if (sapMode) {
           appendDesigntimeFeatures(defHolder.getDefinitions(), wsRuntimeDefinition.getDesigntimeFeatures());
         }
         wsdlDefinitionsParser.parseDefinitionsToFile(defHolder.getDefinitions(), portTypeFile);
         wsdlDefinitionsParser.init();
       }
     }

      //creation of http portType
     if ((prtTCode & PortTypeDescriptor.HTTP_PORTTYPE) == PortTypeDescriptor.HTTP_PORTTYPE) {
       String portTypeType = PortTypeDescriptorImpl.getPortTypeType(PortTypeDescriptor.HTTP_PORTTYPE);
       String portTypeName = getPortTypeName(viSchemaInfo.getViName(), portTypeType);
       targetNamespace = getPortTypeTNS(wsRuntimeDefinition.getWsdName(), viSchemaInfo.getViName(), portTypeType);
       String importLocation = null;

       if (isStandAlone) {
         portTypeFile = new File(getPortTypeOutputFilePath(outputDir, viSchemaInfo.getViName(), confName, portTypeType, sapMode));
         importLocation = getPortTypeOutputFilePath(getUpperDirsDots(outputDirFile, portTypeFile.getParentFile()), viSchemaInfo.getViName(), confName, portTypeType, sapMode);
       } else {
         portTypeFile = new File(endpoint.getVIWsdlPath(portTypeType, sapMode));
         importLocation = getPortTypeImportLocation(endpoint, portTypeType, sapMode);
       }
       PortTypeDescriptor desc = new PortTypeDescriptorImpl(PortTypeDescriptor.HTTP_PORTTYPE, new QName(targetNamespace, portTypeName));
       bindingImports.add(new PortTypeInfo(desc, importLocation));

       String operationsNS = getOperationsNS(wsRuntimeDefinition.getWsdName(), viSchemaInfo.getViName());

       if (! portTypeFile.exists()) { //this is the first generation
         portTypeFile.getParentFile().mkdirs(); //create the directory

         defHolder = WSDLPortTypeGenerator.generateHTTPPortType(javaToQNameRegistry, viSchemaInfo.getLiteralSchemas(), endpoint.getOperations(), targetNamespace, operationsNS, portTypeName);

         //setting the WSD features stuf
         if (sapMode) {
           appendDesigntimeFeatures(defHolder.getDefinitions(), wsRuntimeDefinition.getDesigntimeFeatures());
         }
         wsdlDefinitionsParser.parseDefinitionsToFile(defHolder.getDefinitions(), portTypeFile);
         wsdlDefinitionsParser.init();
       }
     }

     //creating the binding wsdls
     targetNamespace = getBindingTNS(wsRuntimeDefinition.getWsdName(), endpoint.getConfigurationName(), styles[pt]);
     DefinitionsHolder holder = getBindingWSDLDefinitions(bindingImports, targetNamespace);

     //standard wsdl generation
     transportBinding.generateBinding(styles[pt], endpoint.getWsdlBindingName(), endpoint.getTrBindingConfig(), endpoint.getOperations(), holder.getPortTypeDescriptors(), holder.getDefinitions());
     if (isStandAlone) {
       bindingFile = new File(getBindingOutputFilePath(outputDir, confName, styles[pt], sapMode));
     } else {
       if (sapMode) {
         bindingFile = new File(endpoint.getBindingWsdl(styles[pt], true));
       } else {
         bindingFile = new File(endpoint.getBindingWsdl(styles[pt], false));
       }
     }

     bindingFile.getParentFile().mkdirs();
     if (sapMode) {
       //sap-wsdl generation
       FeatureBindingAppender.appendFeatures(holder.getDefinitions(), new ServiceEndpointDefinition[]{endpoint});
     }
     wsdlDefinitionsParser.parseDefinitionsToFile(holder.getDefinitions(), bindingFile);
     wsdlDefinitionsParser.init();
    }
  }

  private class PortTypeInfo {
    PortTypeDescriptor portTypeDescriptor;
    String importLocation;

    PortTypeInfo(PortTypeDescriptor portTypeDescriptor, String importLocation) {
      this.portTypeDescriptor = portTypeDescriptor;
      this.importLocation = importLocation;
    }
  }

  private int[] getPortTypeCodes(RuntimeTransportBinding trb, String[] defStyles) throws Exception {
    int[] res = new int[defStyles.length];

    String[] styles = trb.getSupportedSyles();
    int[] portTypeCodes  = trb.getNecessaryPortTypes();

    int nom = 0;
    for (int i = 0; i < styles.length; i++) {
      for (int j = 0; j < defStyles.length; j++) {
        if (styles[i].equals(defStyles[j])) {
          res[nom++] = portTypeCodes[i];
        }
      }
    }

    return res;
  }

  private String getUpperDirsDots(File baseDir, File curDir) {
    String res="";
    while (! baseDir.equals(curDir)) {
      res += "../";
      curDir = curDir.getParentFile();
    }

    if (res.length() > 0) {
      return res.substring(0, res.length() - 1);
    }
    return res;
  }

  private static String getPortTypeName(String viName, String ptStyle) {
    return viName + "_" + ServiceGenerator.getUpperLetteredString(ptStyle);
  }

  private String getFeatureWSDL_ID(String portTypeName, int nom) {
    return portTypeName + "_design-feature_" + nom;
  }

  /**
   * Appends to the portType inside the WSDLDefinitions (def param) the WSD features
   */
  private void appendDesigntimeFeatures(WSDLDefinitions def, FeatureInfo[] features) throws Exception {
    //there must be only one portType in the def.
    WSDLPortType portType = (WSDLPortType) def.getPortTypes().get(0);
    String portTypeName = portType.getName();

    final String feature_NS_pref = "fns";

    FeatureInfo cur;
    String featureID;
    String fPref;
    String tnsPref = FeatureBindingAppender.setTNSPrefix(def);

    for (int i = 0; i < features.length; i++) { //traversing the features
      cur = features[i];
      //creation of sapUSEFeature and setting it to the WSDLPortType
      SAPUseFeature useFeature = new SAPUseFeature();
      featureID = getFeatureWSDL_ID(portTypeName, i);
      useFeature.setFeatureQName(new com.sap.engine.lib.xml.util.QName(tnsPref, featureID, cur.getFeatureName()));
      portType.addUseFeature(useFeature);

      //creation of sapFeature and setting it in the WSDLDefinitions
      SAPFeature sapFeature = new SAPFeature();
      sapFeature.setName(featureID);
      sapFeature.setUri(cur.getFeatureName());
      //setting ns prefix for the feature namespace if it is not set
      fPref = FeatureBindingAppender.getPrefixForUri(def, cur.getFeatureName());
      if (fPref == null) {
        fPref = feature_NS_pref + i;
        def.addAdditionalAttribute("xmlns:" + fPref, cur.getFeatureName());
      }

      FeatureBindingAppender.loadProperties(sapFeature, cur.getConfiguration(), fPref);
      def.addFeature(sapFeature);
    }
  }

}
