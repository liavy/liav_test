package com.sap.engine.services.webservices.espbase.xi;

import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.server.additions.attach.ProviderAttachmentProtocol;

public class XIFrameworkConstants {
  
  public static final String JNDI_ROOT_CONTEXT_NAME = "sap.com";
  public static final String JNDI_XI2ESP_CONTEXT_NAME = "xi2esp";
  public static final String JNDI_ESP2XI_CONTEXT_NAME = "esp2xi";
  public static final String JNDI_XI_MSG_PROC_NAME = "ESP2XI_Processor";
  public static final String JNDI_XI2ESP_MSG_PROC_NAME = JNDI_ROOT_CONTEXT_NAME + "/" + JNDI_XI2ESP_CONTEXT_NAME + "/" + JNDI_XI_MSG_PROC_NAME;
  public static final String JNDI_ESP2XI_MSG_PROC_NAME = JNDI_ROOT_CONTEXT_NAME + "/" + JNDI_ESP2XI_CONTEXT_NAME + "/" + JNDI_XI_MSG_PROC_NAME;
  
  public static final String DT_CONFIG_NS = "http://www.sap.com/xi/integration/";
  public static final String DT_CONFIG_IS_SYNCHRON_PROP_NAME = "IsSynchron";
  public static final String DT_CONFIG_XI_TRANSPORT_IS_POSSIBLE_PROP_NAME = "XITransportIsPossible";
  
  public static final String BOOLEAN_PROP_POSITIVE_VALUE = "true";
  public static final String BOOLEAN_PROP_NEGATIVE_VALUE = "false";
  
  public static final String XI_CLIENT_RUNTIME_VERSION_URI = "urn:sap-com:soap:runtime:application:client";
  public static final String XI_SERVER_RUNTIME_VERSION_URI = "urn:sap-com:soap:xms:application:xip";
  
  public static final String RT_CONFIG_NS = PublicProperties.TRANSPORT_BINDING_FEATURE;
  public static final String RT_CONFIG_RUNTIME_ENVIRONMENT_PROP_NAME = "runtimeEnvironment";
  public static final String RT_CONFIG_RUNTIME_ENVIRONMENT_XI_RUNTIME_PROP_VALUE = "XI";
  public static final String RT_CONFIG_RUNTIME_ENVIRONMENT_SOAP_RUNTIME_PROP_VALUE = "SOAP";
  public static final String RT_CONFIG_SENDER_PARTY_NAME_PROP_NAME = "xiSenderPartyName";
  public static final String RT_CONFIG_SENDER_SERVICE_PROP_NAME = "xiSenderService";
  
  public static final String API_SENDER_PARTY_NAME_PROP_NAME = "XI.Sender.Party.Name";
  public static final String API_SENDER_SERVICE_PROP_NAME = "XI.Sender.Service";
  public static final String API_SYS_ACK_REQUESTED_PROP_NAME = "XI.Sys.Ack.Requested";
  public static final String API_SYS_NEGATIVE_ACK_REQUESTED_PROP_NAME = "XI.Sys.Negative.Ack.Requested";
  public static final String API_APP_ACK_REQUESTED_PROP_NAME = "XI.App.Ack.Requested";
  public static final String API_APP_NEGATIVE_ACK_REQUESTED_PROP_NAME = "XI.App.Negative.Ack.Requested";
  public static final String API_QUERY_ID_PROP_NAME = "XI.Query.Id";
  public static final String API_SERVICE_INTERFACE_NAME_PROP_NAME = "XI.Service.Interface.Name";
  public static final String API_IS_ASYNC_PROP_NAME = "XI.Is.Async";
  public static final String API_RECEIVERS_PROP_NAME = "XI.Receivers";
  public static final String API_MESSAGE_PROCESSOR_PROP_NAME = "XI.Msg.Proc";
  public static final String API_XI_RUNTIME_PROP_NAME = "XI.Runtime";
  
  public static final String SOAPAPPLICATION_SERVICE_XI_VALUE = "URN:SAP-COM:SOAP:XMS:APPLICATION:XIP";
  public static final String[] XI_PROTOCOL_ORDER = new String[]{ProviderAttachmentProtocol.PROTOCOL_NAME};
  
  public static final String XI_MESSAGE_ENCODING = "UTF-8";
}
