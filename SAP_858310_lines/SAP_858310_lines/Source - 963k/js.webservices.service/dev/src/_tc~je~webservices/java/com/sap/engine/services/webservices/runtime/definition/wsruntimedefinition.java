package com.sap.engine.services.webservices.runtime.definition;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import com.sap.engine.interfaces.webservices.runtime.JavaToQNameMappingRegistry;
import com.sap.engine.interfaces.webservices.runtime.definition.IWebService;
import com.sap.engine.interfaces.webservices.runtime.definition.WSIdentifier;
import com.sap.engine.interfaces.webservices.runtime.definition.IWSEndpoint;
import com.sap.engine.lib.descriptors.ws04wsrt.UDDIPublication;
import com.sap.engine.services.webservices.webservices630.server.deploy.ws.WSDirsHandler;
import com.sap.tc.logging.Location;

import java.io.Serializable;
import java.io.File;

/**
 * Title:
 * Description:
 * Copyright: Copyright (c) 2002
 * Company: Sap Labs Sofia
 * @author Dimitrina Stoyanova
 * @version 6.30
 */

public class WSRuntimeDefinition implements IWebService {

  private WSIdentifier wsId = null;
  private QName wsQName = null;
  private String standardNS = null;
  private ServiceEndpointDefinition[] serviceEndpointDefinitions = null;
  private transient TypeMappingRegistry typeMappingRegistry = null;
  private transient JavaToQNameMappingRegistry javaToSchemaMappingRegistry = null;
  private String uddiKey = null;
  private UDDIPublication[] uddiPublications;
  private SLDWebService sldWS = null;

  private String wsdRelPath = null;
  private UDDIPublication[] wsdUDDIPublications;
  private String wsdName;
  private String wsdDocumentation;

  private String[] wsdlSupportedStyles = new String[0];

  private String documentationRelPath = null;

  private FeatureInfo[] designtimeFeatures = null;

  private transient Location wsLocation = null;
  private String wsLocationName;

  private transient OutsideInDefinition outsideInDefinition = null;

  private boolean isFastEJB = false;

  private transient WSDirsHandler wsDirsHandler = null;

  public WSRuntimeDefinition() {
  }

  public WSIdentifier getWSIdentifier() {
    return wsId;
  }

  public boolean isFastEJB() {
     return isFastEJB;
  }

  public void setWSIdentifier(WSIdentifier wsId) {
    this.wsId = wsId;
  }

  public String getApplicationName() {
    return wsId.getApplicationName();
  }

  public String getServiceName() {
    return wsId.getServiceName();
  }

  public void setStandardNS(String standardNS) {
    this.standardNS = standardNS;
  }

  public String getStandardNS() {
    return standardNS;
  }

  public QName getWsQName() {
    return wsQName;
  }

  public void setWsQName(QName wsQName) {
    this.wsQName = wsQName;
  }

  public void setServiceEndpointDefinitions(ServiceEndpointDefinition[] serviceEndpointDefinitions) {
    this.serviceEndpointDefinitions = serviceEndpointDefinitions;
  }

  public ServiceEndpointDefinition[] getServiceEndpointDefinitions() {
    return serviceEndpointDefinitions;
  }

  public void setTypeMappingRegistry(TypeMappingRegistry typeMappingRegistry) {
    this.typeMappingRegistry = typeMappingRegistry;
  }

  public TypeMappingRegistry getTypeMappingRegistry() {
    return typeMappingRegistry;
  }

  public void setJavaToQNameMappingRegistry(JavaToQNameMappingRegistry javaToSchemaMappingRegistry) {
    this.javaToSchemaMappingRegistry = javaToSchemaMappingRegistry;
  }

  public JavaToQNameMappingRegistry getJavaToQNameMappingRegistry() {
    return javaToSchemaMappingRegistry;
  }

  public String getUddiKey() {
    return uddiKey;
  }

  public void setUddiKey(String uddiKey) {
    this.uddiKey = uddiKey;
  }

  public UDDIPublication[] getUddiPublications() {
    return uddiPublications;
  }

  public void setUddiPublications(UDDIPublication[] publications) {
    uddiPublications = publications;
  }

  public SLDWebService getSLDWebService() {
    return sldWS;
  }

  public void setSLDWebService(SLDWebService sldWS) {
    this.sldWS = sldWS;
  }

  public UDDIPublication[] getWsdUDDIPublications() {
    return wsdUDDIPublications;
  }

  public String getWsdRelPath() {
    return wsdRelPath;
  }

  public void setWsdRelPath(String wsdRelPath) {
    this.wsdRelPath = wsdRelPath;
  }

  public void setWsdUDDIPublications(UDDIPublication[] wsdUDDIPublications) {
    this.wsdUDDIPublications = wsdUDDIPublications;
  }

  public String getWsdName() {
    return wsdName;
  }

  public void setWsdName(String wsdName) {
    this.wsdName = wsdName;
  }

  public String getWsdDocumentation() {
    return wsdDocumentation;
  }

  public void setWsdDocumentation(String documentation) {
    this.wsdDocumentation = documentation;
  }

  public String[] getWsdlSupportedStyles() {
    return this.wsdlSupportedStyles;
  }

  public void setWsdlSupportedStyles(String[] wsdlSupportedStyles) {
    this.wsdlSupportedStyles = wsdlSupportedStyles;
  }

  public void addWsdlSupportedStyle(String style) {
    int oldLength = wsdlSupportedStyles.length;
    String[] newStyles = new String[oldLength + 1];

    System.arraycopy(wsdlSupportedStyles, 0, newStyles, 0, oldLength);
    newStyles[oldLength] = style;

    wsdlSupportedStyles = newStyles;
  }

  public String getDocumentationRelPath() {
    return documentationRelPath;
  }

  public void setDocumentationRelPath(String documentationRelPath) {
    this.documentationRelPath = documentationRelPath;
  }

  public FeatureInfo[] getDesigntimeFeatures() {
    return designtimeFeatures;
  }

  public void setDesigntimeFeatures(FeatureInfo[] designtimeFeatures) {
    if (designtimeFeatures == null) {
      return;
    }
    for (int i = 0; i < designtimeFeatures.length; i++) {
      if (designtimeFeatures[i].getFeatureName().startsWith(EJBImplConstants.FAST_EJB_FEATURE)) {
        this.isFastEJB = true;
        FeatureInfo[] newFeatureArr = new FeatureInfo[designtimeFeatures.length - 1];
        designtimeFeatures[i] = designtimeFeatures[designtimeFeatures.length - 1]; //replacing the orderFeature with the last one.
        System.arraycopy(designtimeFeatures, 0, newFeatureArr, 0, designtimeFeatures.length - 1);
        designtimeFeatures = newFeatureArr;
        break;
      }
    }
    this.designtimeFeatures = designtimeFeatures;
  }

  public Location getWsLocation() {
    return wsLocation;
  }

  public synchronized void setWsLocation(Location wsLocation) {
    this.wsLocation = wsLocation;
    wsLocationName = wsLocation.getName();
  }

  public String getWSLocationName() {
    return wsLocationName;
  }

  public boolean hasOutsideInDefinition() {
    return outsideInDefinition != null;
  }

  public OutsideInDefinition getOutsideInDefinition() {
    return outsideInDefinition;
  }

  public void setOutsideInDefinition(OutsideInDefinition outsideInDefinition) {
    this.outsideInDefinition = outsideInDefinition;
  }

  public WSDirsHandler getWsDirsHandler() {
    return wsDirsHandler;
  }

  public void setWsDirsHandler(WSDirsHandler wsDirsHandler) {
    this.wsDirsHandler = wsDirsHandler;
  }

  public IWSEndpoint[] getEndpoints() {
    return serviceEndpointDefinitions;
  }

  public String getWsDirectory() {
    return wsDirsHandler.getWsDirectory();
  }

}

