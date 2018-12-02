package com.sap.engine.services.webservices.runtime.definition;

import com.sap.engine.services.webservices.runtime.registry.OperationMappingRegistry;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.exceptions.BaseComponentInstantiationException;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.interfaces.webservices.runtime.PropertyDescriptor;
import com.sap.engine.interfaces.webservices.runtime.OperationDefinition;
import com.sap.engine.interfaces.webservices.runtime.Feature;
import com.sap.engine.interfaces.webservices.runtime.definition.IWSEndpoint;
import com.sap.engine.lib.descriptors.ws04wsdd.OutsideInConfiguration;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2000
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class ServiceEndpointDefinition implements IWSEndpoint {

  //protocol order global feature
  private static final String  PROTOCOLORDER_FEATURE_NAME  =  "http://www.sap.com/webas/630/features/portocol-order/";
  //protocol order property
  private static final String  PROTOCOLORDER_PROPERTY_NAME  =  "protocolOrder";

  private String configurationName = null;
  private String vInterfaceName = null;

  private String serviceEndpointId = null;
  private com.sap.engine.interfaces.webservices.runtime.OperationDefinition[] operations = null;
  private String serviceEndpointInterface = null;
  private com.sap.engine.interfaces.webservices.runtime.ImplLink implLink = null;
  private String targetServerURL = null;
  private String transportBindingId = null;
  private com.sap.engine.interfaces.webservices.runtime.Config trBindingConfig = null;
  //contains the WS-DD as they are described there
  private Feature[] featuresChain = null;
  //contains the WS-DD and WSD properties merged.
  private FeatureInfo[] runtimeFeaturesChain = null;
  //contains the protocols' IDs in invokation order, loaded from ProtocolOrder feature.
  private String[] orderedProtocolIDs;
  //key ProtocolID, value Features[]
  private Hashtable protocolsFeatures = new Hashtable();

  private com.sap.engine.interfaces.webservices.runtime.Config globalFeaturesConfig = null;
  private transient OperationMappingRegistry operationRegistry = null;
  private WSRuntimeDefinition owner = null;

  private String viRelPath = null;
  // wsdl needed information
  private QName serviceEndpointQName = null;
  private QName portTypeQName = null;
  private QName wsdlBindingName = null;

  private OutsideInConfiguration outsideInConfiguration = null;

  private String sldName = null;

  //private boolean measurePerf;

  public ServiceEndpointDefinition() {

  }

//  public boolean isMeasurePerf() {
//    return measurePerf;
//  }
//
//  public void setMeasurePerf(boolean measurePerf) {
//    this.measurePerf = measurePerf;
//  }

  public String getvInterfaceName() {
    return vInterfaceName;
  }

  public void setvInterfaceName(String vInterfaceName) {
    this.vInterfaceName = vInterfaceName;
  }

  public String getConfigurationName() {
    return configurationName;
  }

  public void setConfigurationName(String configurationName) {
    this.configurationName = configurationName;
  }

  public void setServiceEndpointId(String serviceEndpointId) {
    this.serviceEndpointId = serviceEndpointId;
  }

  public String getServiceEndpointId() {
    return serviceEndpointId;
  }

  public void setOperations(com.sap.engine.interfaces.webservices.runtime.OperationDefinition[] operations) {
    this.operations = operations;
  }

  public com.sap.engine.interfaces.webservices.runtime.OperationDefinition[] getOperations() {
    return operations;
  }

  public void setServiceEndpointInterface(String serviceEndpointInterface) {
    this.serviceEndpointInterface = serviceEndpointInterface;
  }

  public String getServiceEndpointInterface() {
    return serviceEndpointInterface;
  }

  public void setImplLink(com.sap.engine.interfaces.webservices.runtime.ImplLink implLink) {
    this.implLink = implLink;
  }

  public com.sap.engine.interfaces.webservices.runtime.ImplLink getImplLink() {
    return implLink;
  }

  public String getTargetServerURL() {
    return targetServerURL;
  }

  public void setTargetServerURL(String targetServerURL) {
    this.targetServerURL = targetServerURL;
  }

  public void setTransportBinding(String trBindingId) {
    this.transportBindingId = trBindingId;
  }

  public String getTransportBindingId() {
    return transportBindingId;
  }

  public com.sap.engine.interfaces.webservices.runtime.Config getTrBindingConfig() {
    return trBindingConfig;
  }

  public void setTrBindingConfig(com.sap.engine.interfaces.webservices.runtime.Config trBindingConfig) {
    this.trBindingConfig = trBindingConfig;
  }

  public void setTrBindingConfig(Properties trBindingProperties) {
    this.trBindingConfig = new ConfigImpl(trBindingProperties);
  }

  public Feature[] getRuntimeFeaturesChain() {
    return runtimeFeaturesChain;
  }

  public String[] getOrderedProtocolIDs() {
    return orderedProtocolIDs;
  }

  public Hashtable getProtocolIDFeatureMappings() {
    return this.protocolsFeatures;
  }

  public void setFeaturesChain(FeatureInfo[] featuresChain) {
    if (featuresChain == null) {
      return;
    }

    //searching for protocolOrder feature. If found it is removed.
    for (int i = 0; i < featuresChain.length; i++) {
      if (featuresChain[i].getFeatureName().startsWith(PROTOCOLORDER_FEATURE_NAME)) {
        //loading the data from the feature
        loadProtocolOrder(featuresChain[i]);
        //removing this feature from the feature's list - not to be displayed.
        FeatureInfo[] newFeatureArr = new FeatureInfo[featuresChain.length - 1];
        featuresChain[i] = featuresChain[featuresChain.length - 1]; //replacing the orderFeature with the last one.
        System.arraycopy(featuresChain, 0, newFeatureArr, 0, featuresChain.length - 1);
        featuresChain = newFeatureArr;
        break;
      }
    }

    //setting the value for wsdl generation
    this.featuresChain = featuresChain;

    if (featuresChain != null && featuresChain.length > 0) {
      this.runtimeFeaturesChain = new FeatureInfo[featuresChain.length];
      //loading the information from the featuresChain in the runtimeFeaturesChain
      for (int i = 0; i < runtimeFeaturesChain.length; i++) {
        runtimeFeaturesChain[i] = new FeatureInfo();
        runtimeFeaturesChain[i].setName(featuresChain[i].getFeatureName());
        runtimeFeaturesChain[i].setProtocol(featuresChain[i].getProtocolID());
        ConfigImpl cfg = new ConfigImpl();
        PropertyDescriptor[] propDescr = featuresChain[i].getConfiguration().getProperties();
        //copying the properties' references
        for (int p = 0; p < propDescr.length; p++) {
          cfg.addProperty(propDescr[p]);
        }
        runtimeFeaturesChain[i].setConfiguration(cfg);
      }

      //traversing the designtime features and searching for corresponding deploytime feature.
      FeatureInfo[] designFeatures = owner.getDesigntimeFeatures();
      FeatureInfo curDesignF;
      for (int i = 0; i < designFeatures.length; i++) {
        curDesignF = designFeatures[i];
        for (int j = 0; j < runtimeFeaturesChain.length; j++) {
          if (runtimeFeaturesChain[j].getFeatureName().equals(curDesignF.getFeatureName())) {
            //setting desingtime properties to the runtimefeature
            PropertyDescriptor[] prop = curDesignF.getConfiguration().getProperties();
            for (int p = 0; p < prop.length; p++) {
              ((ConfigImpl) runtimeFeaturesChain[j].getConfiguration()).addProperty(prop[p]);
            }
          }
        }
      }

      //loads the protocolFeatures table
      loadProtocolIDFeatureMappingTable();


      if (this.orderedProtocolIDs == null) { //in case there was no feature contaning the protocol's order use arbitrary
        java.util.Set keySet = protocolsFeatures.keySet();
        this.orderedProtocolIDs = (String[]) keySet.toArray(new String[keySet.size()]);
      }

      String[] finalOrderedProtIDS = new String[orderedProtocolIDs.length];
      int count = 0;

      for (int i = 0; i < this.orderedProtocolIDs.length; i++) {
        //for this protocol no features are found, skip it
        if (protocolsFeatures.get(orderedProtocolIDs[i]) == null) {
          continue;
        }

        finalOrderedProtIDS[count++] = orderedProtocolIDs[i];
      }

      if (count < orderedProtocolIDs.length) {
        String newArr[] = new String[count];
        System.arraycopy(finalOrderedProtIDS, 0, newArr, 0, count);
        finalOrderedProtIDS = newArr;
      }

      this.orderedProtocolIDs = finalOrderedProtIDS;
    }
  }

  public Feature[] getFeaturesChain() {
    if (featuresChain == null) {
      featuresChain = new FeatureInfo[0];
    }
    return featuresChain;
  }

  public com.sap.engine.interfaces.webservices.runtime.Config getGlobalFeaturesConfig() {
    return globalFeaturesConfig;
  }

  public void setGlobalFeaturesConfig(com.sap.engine.interfaces.webservices.runtime.Config globalFeaturesConfig) {
    this.globalFeaturesConfig = globalFeaturesConfig;
  }

  public void setOperationMappingRegistry(OperationMappingRegistry operationRegistry) {
    this.operationRegistry = operationRegistry;
  }

  public OperationMappingRegistry getOperationMappingRegistry() {
    return operationRegistry;
  }

  public void setOwner(WSRuntimeDefinition owner) {
    this.owner = owner;
  }

  public WSRuntimeDefinition getOwner() {
    return owner;
  }

  public String getViRelPath() {
    return viRelPath;
  }

  public void setViRelPath(String viRelPath) {
    this.viRelPath = viRelPath;
  }

  public void setServiceEndpointQualifiedName(QName serviceEndpointQName) {
    this.serviceEndpointQName = serviceEndpointQName;
  }

  public QName getServiceEndpointQualifiedName() {
    return serviceEndpointQName;
  }

  public void setPortTypeName(QName portTypeQName) {
    this.portTypeQName = portTypeQName;
  }

  public QName getPortTypeName() {
    return portTypeQName;
  }

  public void setWsdlBindingName(QName wsdlBindingName) {
    this.wsdlBindingName = wsdlBindingName;
  }

  public QName getWsdlBindingName() {
    return wsdlBindingName;
  }

  public String getDocumentWsdlURI(String host, int port) {
    return  "http://" + host + ":" + port + getDocumentWsdlPathInfo();
  }

  public String getRpcWsdlURI(String host, int port) {
    return  "http://" + host + ":" + port + getRpcWsdlPathInfo();
  }

  public String getDocumentWsdlPathInfo() {
    if (serviceEndpointId.startsWith("/")) {
      return serviceEndpointId + "?wsdl=document";
    }
    return "/" + serviceEndpointId + "?wsdl=document";
  }

  public String getRpcWsdlPathInfo() {
    if (serviceEndpointId.startsWith("/")) {
      return serviceEndpointId + "?wsdl=rpc";
    }
    return "/" + serviceEndpointId + "?wsdl=rpc";
  }

  public String getVIWsdlName() {
    String viWsdlName = null;
    int index = viRelPath.lastIndexOf("/");
    if (index != -1) {
      viWsdlName = viRelPath.substring(index + 1);
    } else {
      viWsdlName = viRelPath;
    }
    return viWsdlName;
  }

  public String getVIWsdlPath(String style, boolean isSapMode) throws Exception {
    return this.getOwner().getWsDirsHandler().getViWsdlPath(viRelPath, configurationName, style, isSapMode);
  }

  public String getBindingWsdl(String style, boolean isSapMode) throws Exception {
    return this.getOwner().getWsDirsHandler().getBindingWsdlPath(configurationName, style, isSapMode);
  }

  public OutsideInConfiguration getOutsideInConfiguration() {
    return outsideInConfiguration;
  }

  public void setOutsideInConfiguration(OutsideInConfiguration outsideInConfiguration) {
    this.outsideInConfiguration = outsideInConfiguration;
  }

  /**
   * Loads the protocolsOrder String[] with the IDs extracted from the feature param.
   * @param feature
   */
  private void loadProtocolOrder(FeatureInfo feature) {
    PropertyDescriptor prOrder = feature.getConfiguration().getProperty(PROTOCOLORDER_PROPERTY_NAME);
    if (prOrder != null) { //if there is no order do not set nothing
      StringTokenizer strToken = new StringTokenizer(prOrder.getValue());
      this.orderedProtocolIDs = new String[strToken.countTokens()];
      int counter = 0;
      while (strToken.hasMoreTokens()) {
        orderedProtocolIDs[counter++] = strToken.nextToken();
      }

    }
  }

  /**
   * Sorts the features according to protocols.
   * The result is stored in protocolsFeature table
   */
  private void loadProtocolIDFeatureMappingTable() {
//    Hashtable hTalbe = new Hashtable(); //key ProtocolID, value ArrayList of Feature | String "mark" for operation
//    ArrayList list;
//    FeatureInfo[] features;
//
//    //global feature traversing
//    features = (FeatureInfo[])getRuntimeFeaturesChain();
//    if (features != null) {
//      for (int i = 0; i < features.length; i++) {
//        //feature with no runtime meaning
//        if (features[i].getProtocolID() == null || features[i].getProtocolID().length() == 0) {
//          continue;
//        }
//
//        try {
//          WSContainer.getComponentFactory().getProtocolInstance(features[i].getProtocolID());
//        } catch (BaseComponentInstantiationException bciE) {
//          Location loc = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//          loc.logT(Severity.PATH, "There is no registered protocol for ID: " + features[i].getProtocolID() + ". The protocol is skipped!");
//          continue;
//        }
//
//        list = (ArrayList) hTalbe.get(features[i].getProtocolID());
//        if (list == null) {
//          list = new ArrayList();
//          hTalbe.put(features[i].getProtocolID(), list);
//        }
//
//        list.add(features[i]);
//      }
//    }
//
//    String mark = "mark";
//
//    //operation traversing
//    OperationDefinition[] operations = getOperations();
//    int opSize = operations.length;
//
//    for (int i = 0; i < opSize; i++) {
//      features =  ((OperationDefinitionImpl) operations[i]).getFeaturesChain();
//
//      if (features == null) {
//        continue;
//      }
//
//      for (int j = 0; j < features.length; j++) {
//        //feature with no runtime meaning
//        if (features[j].getProtocolID() == null || features[j].getProtocolID().length() == 0) {
//          continue;
//        }
//
////        try {
////          WSContainer.getComponentFactory().getProtocolInstance(features[j].getProtocolID());
////        } catch (BaseComponentInstantiationException bciE) {
////          Location loc = Location.getLocation(WSLogging.DEPLOY_LOCATION);
////          loc.logT(Severity.PATH, "There is no registered protocol for ID: " + features[j].getProtocolID() + ". The protocol is skipped!");
////          continue;
////        }
//
//        if (hTalbe.get(features[j].getProtocolID()) == null) {
//          hTalbe.put(features[j].getProtocolID(), mark);
//        }
//      }
//    }
//
//    //setting the loaded data in the protocolsFeature table
//    Enumeration en = hTalbe.keys();
//    Object tmpPId, tmpValue;
//
//    while (en.hasMoreElements()) {
//      tmpPId = en.nextElement();
//      tmpValue = hTalbe.get(tmpPId);
//
//      if (tmpValue instanceof ArrayList) {
//        list = (ArrayList) tmpValue;
//        this.protocolsFeatures.put(tmpPId, list.toArray(new Feature[list.size()]));
//      } else { //this is only operation specific feature "mark" is the tmpValue
//        this.protocolsFeatures.put(tmpPId, new Feature[0]);
//      }
//    }
  }

  public String getSldName() {
    return sldName;
  }

  public void setSldName(String sldName) {
    this.sldName = sldName;
  }

  public String getEndpointName() {
    return getConfigurationName();
  }

  public OperationDefinition[] getWSOperations() {
    return operations;
  }

  public String getEndpointURI() {
    if (serviceEndpointId.startsWith("/")) {
      return serviceEndpointId;
    } else {
      return "/" + serviceEndpointId;
    }
  }

}

