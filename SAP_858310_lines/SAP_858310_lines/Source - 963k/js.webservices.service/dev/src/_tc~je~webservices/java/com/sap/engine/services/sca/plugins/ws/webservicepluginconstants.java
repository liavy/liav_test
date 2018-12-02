package com.sap.engine.services.sca.plugins.ws;

public class WebServicePluginConstants {
  
  public enum WsarGeneratonMode {WS, DII};
  
  public static final String SCA_IMPL_CONTAINER_NAME = "_SCA_IMPLEMENTATION_CONTAINER_";
  public static final String WS_CONTAINER_NAME = "webservices_container";
  public static final String SERVICE_IDENTIFIER_STR = "service-identifier";
  public static final String WEBSERVICE_CONTAINER_NAME = "webservices";
  public static final String DESTINATION_STR = "destination";
  public static final String WSDL_INTERFACE_STR = "wsdl.interface(";
  public static final String WS_PLUGIN_NAME_STR = "binding.ws";
  public static final String INTERFACE_DII_NAME_STR = "interface.dii";
  public static final String WS_BINDING_TYPE = "WS";
  public static final String SA_GET_HELPER_CONTEXT_METHOD_NAME = "getHelperContext";
  public static final String SCA_SPI_NAME = "tc~je~sca~spi";
  
  // WSARGenerator related.
  public static final String SAP_SCA_NS_10 = "http://www.sap.com/xmlns/sapsca/1.0";
  public static final String WSDL_EXTENSION = ".wsdl";
  public static final String SCA_COMPOSITE_EXTENSION = ".composite";
  //public static final String SCA_WSAR_FILE_NAME = "sca.wsar";
  public static final String DII_WSAR_FILE_NAME = "dii.wsar";
  public static final String WS_WSAR_FILE_NAME = "ws.wsar";
  public static final String META_INF_FOLDER_STR = "META-INF";
  public static final String SCA_ALT_FILE_NAME = "webservices-j2ee-engine-alt.xml";
  public static final String COMPOSITE_SERVICE_TAG_NAME = "service";
  public static final String COMPOSITE_COMPONENT_TAG_NAME = "component";
  public static final String COMPOSITE_INTERFACEWSDL_TAG_NAME = "interface.wsdl";
  public static final String COMPOSITE_INTERFACEJAVA_TAG_NAME = "interface.java";
  public static final String COMPOSITE_INTERFACEDII_TAG_NAME = "interface.dii";
  public static final String COMPOSITE_IMPLEMENTATION_EJB_TAG_NAME = "implementation.ejb";
  public static final String COMPOSITE_LOCATION_ATTR_NAME = "location";
  public static final String COMPOSITE_INTERFACE_ATTR_NAME = "interface";
  public static final String COMPOSITE_NAME_ATTR_NAME = "name";
  public static final String COMPOSITE_EJB_LINK_ATTR_NAME = "ejb-link";  
  public static final String HELPER_CONTEXT_IMPL_MANAGED = "implementationManaged";
  public static final String HELPER_CONTEXT_CNT_MANAGED = "containerManaged";
  public static final String HELPER_CONTEXT_MANAGEMENT_ATTR_NAME = "helperContextManagement";  
  public static final String WSDL_FOLDER_STR = "wsdl";
  public static final String SCA_IMPL_ID = "implementation-id";
  public static final String SCA_SERVICE_IDENTIFIER = "service-identifier";
  public static final String SCA_EJB_JAR_NAME = "jar-name";
  public static final String SCA_EJB_CLASS_NAME = "ejb-name";
  public static final String SCA_EJB_INTERFACE_NAME = "interface-name";
  public static final String SCA_EJB_INTERFACE_TYPE = "interface-type";
  public static final String SCA_EJB_INTERFACE_SEI_TYPE_VALUE = "sei";
  public static final String SCA_EJB_INTERFACE_LOCAL_TYPE_VALUE = "local";
  public static final String SCA_EJB_SCA_COMPONENT_NAME = "sca-component-name";
  public static final String SCA_SERVICE_NAME = "sca-service-name";
  public static final String COMPOSITE_BINDINGWS_TAG_NAME = "binding.ws";  
  
  public static final String DII_INTERFACE_XSD_URI = "com/sap/sca/sdo/sca-interface-dii.xsd";  						      
  public static final String WS_BINDING_XSD_URI = "com/sap/engine/services/sca/plugins/ws/sdo/sca-binding-webservice.xsd";
  public static final String SOAP_XSD_URI = "org/xmlsoap/schemas/soap/envelope/SOAP.xsd";
  public static final String ENCODING_XSD_URI = "org/xmlsoap/schemas/soap/encoding/encoding.xsd";
}