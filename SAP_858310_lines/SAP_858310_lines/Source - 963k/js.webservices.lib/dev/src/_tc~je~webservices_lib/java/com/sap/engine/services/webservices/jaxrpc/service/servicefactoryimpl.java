package com.sap.engine.services.webservices.jaxrpc.service;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.ServiceFactory;

import com.sap.engine.services.webservices.jaxrpc.encoding.TypeMappingRegistryImpl;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortFactory;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPorts;

/**
 * Created by IntelliJ IDEA.
 * User: ivan-m
 * Date: 2004-6-2
 * Time: 11:38:54
 * To change this template use Options | File Templates.
 */
public class ServiceFactoryImpl extends ServiceFactory implements Constants {

  private LogicalPortFactory logicalPortFactory;

  public static ServiceFactory newInstance() {
    return(new ServiceFactoryImpl());
  }

  public ServiceFactoryImpl() {
  }

  public Service createService(URL wsdlUrl, QName serviceName) throws ServiceException {
    return(new SpecificService(serviceName, createTypeMappingRegistry(), wsdlUrl));
  }

  private TypeMappingRegistryImpl createTypeMappingRegistry() throws ServiceException {
    TypeMappingRegistryImpl typeMappingRegistry = null;
    try {
      typeMappingRegistry = new TypeMappingRegistryImpl();
      ClassLoader classLoader = getClass().getClassLoader();
      InputStream typesXMLInputStream = classLoader.getResourceAsStream(TYPES_XML_LOACTION);
      if(typesXMLInputStream != null) {
        typeMappingRegistry.fromXML(typesXMLInputStream, classLoader);
      }
    } catch(Exception exc) {
      throw new ServiceException(exc);
    }
    return(typeMappingRegistry);
  }

  public Service createService(QName serviceName) throws ServiceException {
    try {
      return(new BasicService(serviceName, createTypeMappingRegistry()));
    } catch(Exception exc) {
      throw new ServiceException(exc);
    }
  }

  public Service loadService(Class serviceClass) throws ServiceException {
    checkServiceInterface(serviceClass);
    try {
      Package serviceInterfacePackage = serviceClass.getPackage();
      String lportsXMLLocation = serviceInterfacePackage.getName().replace('.', '/') + "/lports.xml";
      InputStream lportsInputStream = serviceClass.getClassLoader().getResourceAsStream(lportsXMLLocation);
      LogicalPorts logicalPorts = determineLogicalPortFactory().loadLogicalPorts(lportsInputStream);
      Class serviceImplClass = Class.forName(logicalPorts.getImplementationName());
      return((Service)(serviceImplClass.newInstance()));
    } catch(Throwable tr) {
      throw new ServiceException(tr.getMessage());
    }
  }

  public Service loadService(URL wsdlUrl, Class serviceClass, Properties properties) throws ServiceException {
    return(loadService(serviceClass));
  }

  public Service loadService(URL wsdlUrl, QName serviceName, Properties properties) throws ServiceException {
    return(createService(wsdlUrl, serviceName));
  }

  private LogicalPortFactory determineLogicalPortFactory() {
    if(logicalPortFactory == null) {
      logicalPortFactory = new LogicalPortFactory();
    }
    return(logicalPortFactory);
  }

  private void checkServiceInterface(Class serviceInterfaceClass) throws ServiceException {
    if(!serviceInterfaceClass.isInterface()) {
      throw new ServiceException("ERROR : Class '" + serviceInterfaceClass.getName() + "' is not an interface.");
    }
    if(!Service.class.isAssignableFrom(serviceInterfaceClass)) {
      throw new ServiceException("ERROR : Class '" + serviceInterfaceClass.getName() + "' is not assignable for " + Service.class.getName() + ".");
    }
  }
}
