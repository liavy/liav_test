/*
 * Created on 2005-7-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.dynamic.impl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;

import com.sap.engine.services.webservices.espbase.client.bindings.ConfigurationUtil;
import com.sap.engine.services.webservices.espbase.client.bindings.ClientConfigurationContext;
import com.sap.engine.services.webservices.espbase.client.bindings.PublicProperties;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientConfigurationContextImpl;
import com.sap.engine.services.webservices.espbase.client.bindings.impl.ClientServiceContextImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.DInterface;
import com.sap.engine.services.webservices.espbase.client.dynamic.DOperation;
import com.sap.engine.services.webservices.espbase.client.dynamic.DInterfaceInvoker;
import com.sap.engine.services.webservices.espbase.client.dynamic.DestinationContainer;
import com.sap.engine.services.webservices.espbase.client.dynamic.DestinationsHelper;
import com.sap.engine.services.webservices.espbase.client.dynamic.GenericServiceFactory;
import com.sap.engine.services.webservices.espbase.client.dynamic.util.Util;
import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.configuration.InterfaceData;
import com.sap.engine.services.webservices.espbase.configuration.OperationData;
import com.sap.engine.services.webservices.espbase.configuration.PropertyListType;
import com.sap.engine.services.webservices.espbase.configuration.PropertyType;
import com.sap.engine.services.webservices.espbase.configuration.Service;
import com.sap.engine.services.webservices.espbase.configuration.ServiceCollection;
import com.sap.engine.services.webservices.espbase.configuration.ServiceData;
import com.sap.engine.services.webservices.espbase.mappings.InterfaceMapping;
import com.sap.engine.services.webservices.espbase.mappings.OperationMapping;
import com.sap.engine.services.webservices.espbase.wsdl.Interface;
import com.sap.engine.services.webservices.espbase.wsdl.Operation;
import com.sap.engine.services.webservices.jaxrpc.encoding.ExtendedTypeMapping;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WSResourceAccessor;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WebserviceClientException;
import com.sap.exception.BaseRuntimeException;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * @author Ivan-M
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DInterfaceImpl extends DDocumentableImpl implements DInterface {

  private Hashtable operationNameToOperationMapping;
  private InterfaceMapping interfaceMapping;
  private ClientServiceContextImpl serviceContext;
  private InterfaceData iData;
  //private DestinationsHelper destinationsHelper;

  private static final Location LOCATION = Location.getLocation(DInterfaceImpl.class);

  protected DInterfaceImpl(Interface portType, 
                           InterfaceMapping interfaceMapping,
                           ClientServiceContextImpl serviceContext,
                           DestinationsHelper destinationsHelper) throws ParserConfigurationException {
    this.interfaceMapping = interfaceMapping;
    this.serviceContext = serviceContext;
    this.iData = ConfigurationUtil.getInterfaceData(this.interfaceMapping,serviceContext.getCompleteConfiguration());
    //this.destinationsHelper = destinationsHelper;
    initOperationNameToOperationMapping(portType);
    initDocumentationElement(portType);
  }

  public QName[] getPortNames() {
    Vector lPortsNames = getPortNamesCollector();
    QName[] lPortsArray = new QName[lPortsNames.size()];
    lPortsNames.copyInto(lPortsArray);
    return(lPortsArray);
  }

  private Vector getPortNamesCollector() {
    Vector lPortsNames = new Vector();
    BindingData[] bindingDatas = getBindingData();
    for(int i = 0; i < bindingDatas.length; i++) {
      BindingData bindingData = bindingDatas[i];
      if(bindingData.getInterfaceMappingId().equals(interfaceMapping.getInterfaceMappingID())) {
        lPortsNames.add(QName.valueOf(bindingData.getName()));
      }
    }
    return(lPortsNames);
  }

  private BindingData[] getBindingData() {
    ServiceCollection serviceCollection = serviceContext.getCompleteConfiguration().getRTConfig();
    Service service = serviceCollection.getService()[0];
    ServiceData serviceData = service.getServiceData();
    return serviceData.getBindingData();
  }

  public synchronized DInterfaceInvoker getInterfaceInvoker(QName lPortName) throws ServiceException {
    Vector lPortNames = getPortNamesCollector();
    if(lPortNames.size() == 0) {
      return(null);
    }
    if(lPortName == null) {
      lPortName = (QName) lPortNames.firstElement();
    }    
    if(!lPortNames.contains(QName.valueOf(lPortName.toString()))) {
      throw new WebserviceClientException(WebserviceClientException.NO_PORT_AVAILABLE, lPortName.toString());
    }
//    String serviceRefName = (String) serviceContext.getProperty(ClientServiceContextImpl.SERVICE_REF_ID);
//    lPortName = new QName(null, serviceRefName);   
    return(getInterfaceInvoker_SpecifiedLPortName(lPortName));
  }

  public synchronized DInterfaceInvoker getInterfaceInvoker() throws ServiceException {
    if (LOCATION.beDebug()) {
      LOCATION.debugT("entering method getInterfaceInvoker()");
    }
    String serviceRefName = (String) serviceContext.getProperty(ClientServiceContextImpl.SERVICE_REF_ID);
    
    return getInterfaceInvoker(new QName(null, serviceRefName));
  }

  @Deprecated
  public synchronized DInterfaceInvoker getInterfaceInvoker(String destName) throws ServiceException {
    if (LOCATION.beDebug()) {
      LOCATION.debugT("entering deprecated method getInterfaceInvoker(String " + destName + ")");
    }
    return getInterfaceInvokerInner(destName, null);
  }
  
  /**
   * This method builds proxy for specific temporary destination.
   * @param destinationName
   * @param temporaryDestination
   * @return
   */
  private DInterfaceInvoker getInterfaceInvokerForTemporaryDestination(String destinationName,BindingData temporaryDestination) throws ServiceException {
    // Gets all ports for this interface QName
    Vector lPortNames = getPortNamesCollector();
    if (lPortNames.size() == 0) {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("no logical ports found");
      }
      return null;
    }
    //TODO: There is no reciever determination the first endpoint that links to specific interface is selected
    QName lpName = (QName) lPortNames.firstElement();
    if (LOCATION.beDebug()) {
      LOCATION.debugT("Selected logical port with QName ["+lpName.toString()+"].");
    }
    BindingData selectedBinding = ConfigurationUtil._getPortBindingData(serviceContext.getServiceData(), lpName);
    // Makes copy of the binding data without configuration copy. The resulting configuration is merged between the default and the destination
    BindingData resultBinding = makeCopy(selectedBinding,false);
    // Copy the endpoint URL from the destination
    if (temporaryDestination.getUrl() != null) {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("old endpoint url : " + resultBinding.getUrl());
        LOCATION.debugT("replaced endpoint url : " + temporaryDestination.getUrl());
      }      
      resultBinding.setUrl(temporaryDestination.getUrl());
    }
    // Merge the default properties with the passed properties
    PropertyType[] properties = selectedBinding.getSinglePropertyList().getProperty();
    ArrayList<PropertyType> resultProperties = new ArrayList<PropertyType>();
    for (int i=0; i<properties.length; i++) {
      PropertyType property =properties[i];
      if (!property.getNamespace().equals(PublicProperties.TRANSPORT_BINDING_FEATURE) &&
          !property.getNamespace().equals(PublicProperties.AUTHENTICATION_FEATURE) &&
          !property.getNamespace().equals(PublicProperties.TRANSPORT_WARANTEE_FEATURE)) {
        resultProperties.add(property);
      }
    }
    PropertyType []newProperties = temporaryDestination.getSinglePropertyList().getProperty();
    for (int j = 0; j < newProperties.length; j++) {
      PropertyType nextProperty = newProperties[j];
      if (nextProperty.getNamespace().equals(PublicProperties.AUTHENTICATION_FEATURE) ||
          nextProperty.getNamespace().equals(PublicProperties.TRANSPORT_WARANTEE_FEATURE) ||
          nextProperty.getNamespace().equals(PublicProperties.TRANSPORT_BINDING_FEATURE)) {
        resultProperties.add(nextProperty);
      }
    }
    PropertyType[] props = resultProperties.toArray(new PropertyType[resultProperties.size()]);
    resultBinding.getSinglePropertyList().setProperty(props);
    ClientConfigurationContextImpl clientContext = (ClientConfigurationContextImpl) ConfigurationUtil.createClientConfiguration(this.serviceContext, resultBinding);
    clientContext.getPersistableContext().setProperty(ClientConfigurationContextImpl.DESTINATION_NAME, destinationName);    
    return (new DInterfaceInvokerImpl(this, clientContext));
  }
  
  private synchronized DInterfaceInvoker getInterfaceInvokerInner(String destName, DestinationsHelper destinationsHelper) throws ServiceException {
    // Checks if this is temporary destination
	LOCATION.entering("getInterfaceInvokerInner", new Object[]{destName, destinationsHelper});
	
    DestinationContainer destinationContainer = DestinationContainer.getDestinationContainer();
    BindingData tempBindingData = destinationContainer.getDestination(destName);
    if (tempBindingData != null) {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("Found temporary destination ["+destName+"].");
      } 
      return getInterfaceInvokerForTemporaryDestination(destName,tempBindingData);
    }
    Vector lPortNames = getPortNamesCollector();
    if (lPortNames.size() == 0) {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("no logical ports found");
      }
      return (null);
    }

    if (destinationsHelper == null) {
       if (LOCATION.beDebug()) {
        LOCATION.debugT("will init the destination helper");
      }

      destinationsHelper = DestinationsHelper.getDestinationsHelper(destName, getInterfaceName());
      if (destinationsHelper == null) {
        throw new BaseRuntimeException(LOCATION, WSResourceAccessor.getResourceAccessor(), GenericServiceFactory.METHOD_NOT_AVAILABLE, new Object[]{"getInterfaceInvoker(String destName)"});
      }
    } else {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("destination helper is read from the context");
      }
    }

    //make copy of these datas, because one of them will be selected and changed from the destination settings 
    BindingData[] bindingData = getBindingData();
    for (int i = 0; i < bindingData.length; i++) {
    	bindingData[i] = makeCopy(bindingData[i],true);    	
    }    
    
    //check which one is needed 
    BindingData bData = destinationsHelper.selectBindingData(bindingData);
    if (bData == null) {
      if (LOCATION.beDebug()) {
        LOCATION.debugT("cannot select a binding data from the array " + bindingData.length);
      }

      bData = destinationsHelper.getBindingData(destName, null);
    } 

    if (LOCATION.beDebug()) {
      LOCATION.debugT("selected binding data name : " + bData.getName());
      LOCATION.debugT("endpoint url : " + bData.getUrl());
      dumpBindingData((QName) lPortNames.firstElement(), bData);
    }
    
    ClientConfigurationContextImpl clientContext = (ClientConfigurationContextImpl) ConfigurationUtil.createClientConfiguration(this.serviceContext, bData);
    clientContext.getPersistableContext().setProperty(ClientConfigurationContextImpl.DESTINATION_NAME, destName);
    return (new DInterfaceInvokerImpl(this, clientContext));
  }

  /*
  private com.sap.engine.services.webservices.espbase.configuration.PropertyType[] parseProperties(String url, com.sap.engine.services.webservices.espbase.configuration.PropertyType[] props) {
    String protocolOrder = BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY_QNAME.getLocalPart();
    PropertyType protocolOrderProp = null;
    for (int i = 0; i < props.length; i++) {
      if (props[i].getName().equals(protocolOrder)) {
        return props;
      }
    }

    PropertyType[] newProps = new PropertyType[props.length + 1];
    protocolOrderProp = new PropertyType();
    protocolOrderProp.setNamespace(BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY_QNAME.getNamespaceURI());
    protocolOrderProp.setName(BuiltInConfigurationConstants.PROTOCOL_ORDER_PROPERTY_QNAME.getLocalPart());
    protocolOrderProp.set_value(com.sap.engine.services.webservices.espbase.client.bindings.ConsumerProtocolFactory.DEFAULT_DEPL_SOAP_APP);
    System.arraycopy(props, 0, newProps, 0, props.length);
    newProps[props.length] = protocolOrderProp;
    return newProps;
  }*/

  private void dumpBindingData(QName lPortName, BindingData bindingData) {
  	LOCATION.logT(Severity.DEBUG, bindingData.dumpBindingData(bindingData));
  }


  private DInterfaceInvoker getInterfaceInvoker_SpecifiedLPortName(QName lPortName) throws WebserviceClientException {
    BindingData bdata = ConfigurationUtil._getPortBindingData(serviceContext.getServiceData(), lPortName);
    if (LOCATION.beDebug()) {
      dumpBindingData(lPortName, bdata);
    }
    ClientConfigurationContext clientConfigurationContext = ConfigurationUtil.createClientConfiguration(this.serviceContext ,bdata);
    return(new DInterfaceInvokerImpl(this, (ClientConfigurationContextImpl)clientConfigurationContext));
  }

  private void initOperationNameToOperationMapping(Interface portType) throws ParserConfigurationException {
    operationNameToOperationMapping = new Hashtable();
    OperationMapping[] operationMappings = interfaceMapping.getOperation();
    for(int i = 0; i < operationMappings.length; i++) {
      OperationMapping operationMapping = operationMappings[i];
      String operationName = operationMapping.getWSDLOperationName();
      Operation operation = portType.getOperation(operationName);
      ExtendedTypeMapping typeMapping = null;
      if (serviceContext.getTypeMappingRegistry() != null) {
        typeMapping = (ExtendedTypeMapping) serviceContext.getTypeMappingRegistry().getDefaultTypeMapping();
      }
      operationNameToOperationMapping.put(operationName, new DOperationImpl(operation, operationMapping, typeMapping, iData));
    }
  }

  public QName getInterfaceName() {
    return(interfaceMapping.getPortType());
  }

  public String[] getOperationNames() {
    Enumeration operationNamesEnum = operationNameToOperationMapping.keys();
    String[] operationNames = new String[operationNameToOperationMapping.size()];
    for(int i = 0; i < operationNames.length; i++) {
      operationNames[i] = (String)(operationNamesEnum.nextElement());
    }
    return(operationNames);
  }

  public DOperation[] getOperations() {
    Enumeration operationsEnum = operationNameToOperationMapping.elements();
    DOperation[] operations = new DOperation[operationNameToOperationMapping.size()];
    for(int i = 0; i < operations.length; i++) {
      operations[i] = (DOperation)(operationsEnum.nextElement());
    }
    return(operations);
  }

  public DOperation getOperation(String operationName) {
    return((DOperation)(operationNameToOperationMapping.get(operationName)));
  }

  public String toString() {
    StringBuffer toStringBuffer = new StringBuffer();
    initToStringBuffer(toStringBuffer, "");
    return(toStringBuffer.toString());
  }

  protected void initToStringBuffer(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendOffsetAndId(toStringBuffer, offset, "DInterface");
    initToStringBuffer_Documentation(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    initToStringBuffer_PortNames(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    Util.initToStringBuffer_ObjectValue(toStringBuffer, offset + Util.TO_STRING_OFFSET, "interface name : ", getInterfaceName());
    initToStringBuffer_Operations(toStringBuffer, offset + Util.TO_STRING_OFFSET);
  }

  private void initToStringBuffer_PortNames(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, "port names");
    QName[] portNames = getPortNames();
    for(int i= 0; i < portNames.length; i++) {
      QName portName = portNames[i];
      Util.initToStringBuffer_ObjectValue(toStringBuffer, offset + Util.TO_STRING_OFFSET, "port name : ", portName);
    }
  }

  private void initToStringBuffer_Operations(StringBuffer toStringBuffer, String offset) {
    Util.initToStringBuffer_AppendCReturnOffsetAndId(toStringBuffer, offset, "operations");
    DOperation[] operations = getOperations();
    for(int i = 0; i < operations.length; i++) {
      DOperationImpl operation = (DOperationImpl)(operations[i]);
      toStringBuffer.append("\n");
      operation.initToStringBuffer(toStringBuffer, offset + Util.TO_STRING_OFFSET);
    }
  }

  private static BindingData makeCopy(BindingData bindingDataToClone, boolean deep) {
      BindingData newBd = new BindingData();
      newBd.setName(bindingDataToClone.getName());
      newBd.setBindingName(bindingDataToClone.getBindingName());
      newBd.setBindingNamespace(bindingDataToClone.getBindingNamespace());
      newBd.setConfigurationId(bindingDataToClone.getConfigurationId());
      newBd.setActive(bindingDataToClone.isActive());
      newBd.setEditable(bindingDataToClone.isEditable());
      newBd.setGroupConfigId(bindingDataToClone.getGroupConfigId());
      newBd.setInterfaceId(bindingDataToClone.getInterfaceId());
      newBd.setInterfaceMappingId(bindingDataToClone.getInterfaceMappingId());
      newBd.setUrl(bindingDataToClone.getUrl());
      newBd.setVariantName(bindingDataToClone.getVariantName());
      newBd.setUrlScheme(bindingDataToClone.getUrlScheme());
      newBd.setConfigValue(bindingDataToClone.getConfigValue());
      newBd.setUDDIEntity(bindingDataToClone.getUDDIEntity());
      PropertyListType p = new PropertyListType();
      p.setSelected(Boolean.TRUE);
      newBd.setPropertyList(new PropertyListType[]{p});
      if (deep) {
        copyProperties(bindingDataToClone.getSinglePropertyList(),p);
      }
      OperationData thisOps[] = bindingDataToClone.getOperation();
      OperationData newIOps[] = new OperationData[thisOps.length];
      for (int i = 0; i < thisOps.length; i++) {
        newIOps[i] = makeCopy(thisOps[i],deep);
      }
      newBd.setOperation(newIOps);
      return newBd;
  }

  private static OperationData makeCopy(OperationData operationDataToClone, boolean deep) {
    OperationData newIOps = new OperationData();
    newIOps.setName(operationDataToClone.getName());
    newIOps.setConfigValue(operationDataToClone.getConfigValue());
    PropertyListType p = new PropertyListType();
    p.setSelected(Boolean.TRUE);
    newIOps.setPropertyList(new PropertyListType[] { p });
    if (deep) {
      copyProperties(operationDataToClone.getSinglePropertyList(),p);  
    }
    return newIOps;
  }
  
  private static void copyProperties(PropertyListType senderProps, PropertyListType receiverProps) {
    try {    
      PropertyType[] originalProps =  senderProps.getProperty();
      PropertyType[] copiedProps = new PropertyType[originalProps.length];
      for (int i=0; i<copiedProps.length; i++) {
        copiedProps[i] = (PropertyType) originalProps[i].clone();
      }
      receiverProps.setProperty(copiedProps);
    } catch (CloneNotSupportedException e) {
      // This is actually not possible to happen.
      return;
    }
  }

// private static HTTPProxyResolver getGlobalHTTPProxyResolver() {
//      try {
//        Context ctx = new InitialContext();
//        ServletsHelper servletsHelper = (ServletsHelper) ctx.lookup("wsContext/" + ServletsHelper.NAME);
//        return servletsHelper.getHTTPProxyResolver();
//      } catch (NamingException e) {
//        LOCATION.catching(e);
//        return null;
//      }
//    }

  /**
   * Returns the value of some interface design time property.
   * @param key
   * @return
   */
  public String getProperty(String key) {
    return null;
  }

}
