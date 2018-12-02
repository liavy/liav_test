package com.sap.engine.services.webservices.espbase.client.jaxws.cts;

import javax.xml.namespace.QName;

import com.sap.engine.services.webservices.espbase.configuration.ConfigurationRoot;

public class ServiceRefMetaData {

  private String applicationName; 
  private String serviceRefGroupName;
  private String serviceRefName;  
  private String serviceJndiName;
  private ConfigurationRoot configurationDescriptor;
  private String wsdlFile;  
  private QName serviceQName;
  	
  public ServiceRefMetaData(String applicationName, String serviceRefGroupName, String serviceRefName, ConfigurationRoot configurationDescriptor, String wsdlFile, QName serviceQName) {
    this.applicationName = applicationName; 
    this.serviceRefGroupName = applicationName;
    this.serviceRefName = serviceRefName;    
    this.configurationDescriptor = configurationDescriptor;    
    this.wsdlFile = wsdlFile;
    this.serviceQName = serviceQName;
  }
  
  public String getApplicationName() {
    return applicationName;
  }
  
  public String getServiceRefGroupName() {
    return serviceRefGroupName;
  }
  
  public ConfigurationRoot getConfigurationDescriptor() {
    return configurationDescriptor;
  }
  
  public String getWsdlFile() {
    return wsdlFile;
  }
  
  public QName getServiceQName() {
    return serviceQName;
  }
  
  public String getServiceJndiName() {
    return serviceJndiName;
  }

  public void setServiceJndiName(String jndiName) {
    this.serviceJndiName = jndiName;  
  }
  
}
