package com.sap.engine.services.webservices.espbase.client.dynamic;

import java.rmi.Remote;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;

import javax.xml.rpc.ServiceException;

import org.xml.sax.EntityResolver;

import com.sap.engine.services.webservices.espbase.configuration.BindingData;
import com.sap.engine.services.webservices.espbase.discovery.TargetNotMappedException;
import com.sap.engine.services.webservices.espbase.wsdl.Definitions;
import com.sap.engine.services.webservices.jaxrpc.exceptions.WSResourceAccessor;
import com.sap.exception.BaseRuntimeException;
import com.sap.tc.logging.Location;

public abstract class DestinationsHelper {

  private static final Location LOCATION = Location.getLocation(DestinationsHelper.class);
  protected static DestinationsHelper inner;

  public static Remote getPortForLD(String lmtName, Service service) throws TargetNotMappedException, ServiceException {
    if (inner == null) {
      throw new BaseRuntimeException(LOCATION, WSResourceAccessor.getResourceAccessor(), GenericServiceFactory.METHOD_NOT_AVAILABLE, new Object[]{"getDestinationsHelper(String lmtName, QName interfaceName)"});
    }
    return inner.getPortForLDInner(lmtName, service);
  }

  public static Remote getPortForLD(String lmtName, Service service, QName portTypeName) throws TargetNotMappedException, ServiceException {
    if (inner == null) {
      throw new BaseRuntimeException(LOCATION, WSResourceAccessor.getResourceAccessor(), GenericServiceFactory.METHOD_NOT_AVAILABLE, new Object[]{"getDestinationsHelper(String lmtName, QName interfaceName)"});
    }
    return inner.getPortForLDInner(lmtName, service, portTypeName);
  }


  public static DestinationsHelper getDestinationsHelper(String lmtName, QName interfaceName)  throws ServiceException {
    if (inner == null) {
      throw new BaseRuntimeException(LOCATION, WSResourceAccessor.getResourceAccessor(), GenericServiceFactory.METHOD_NOT_AVAILABLE, new Object[]{"getDestinationsHelper(String lmtName, QName interfaceName)"});
    }
    return inner.getDestinationsHelperInner(lmtName, interfaceName);
  }

  public static DestinationsHelper getDestinationsHelper(String applicationName, String serviceRefID)  throws ServiceException {
    if (inner == null) {
      throw new BaseRuntimeException(LOCATION, WSResourceAccessor.getResourceAccessor(), GenericServiceFactory.METHOD_NOT_AVAILABLE, new Object[]{"getDestinationsHelper(String applicationName, String serviceRefID)"});
    }
    return inner.getDestinationsHelperInner(applicationName, serviceRefID);
  }


  public static String getClientAssertionTicket(String ifDefId, String portName) throws TargetNotMappedException, ServiceException {
    if (inner == null) {
      throw new BaseRuntimeException(LOCATION, WSResourceAccessor.getResourceAccessor(), GenericServiceFactory.METHOD_NOT_AVAILABLE, new Object[]{"getClientAssertionTicket(String ifDefId, String portName)"});
    }
    return inner.getClientAssertionTicketInner(ifDefId, portName);
  }

  //in order to prevent integration problems fake old implementation is applied
  public String getWSDLUrl(Definitions[] outWsdl) throws TargetNotMappedException, ServiceException {
    return getWSDLUrl();
  }

  //in order to prevent integration problems fake implementation is applied
  public BindingData selectBindingData(BindingData[] bindingData) throws ServiceException {
    return null;
  }

  public abstract EntityResolver getEntityResolverForTarget() throws TargetNotMappedException, ServiceException;

  public abstract BindingData getBindingData(String lmtName, QName interfaceName) throws ServiceException;

  public abstract String getDestinationName();

  public abstract QName getInterfaceName();

  public abstract String getWSDLUrl() throws TargetNotMappedException, ServiceException;

  protected abstract String getClientAssertionTicketInner(String ifDefId, String portName) throws TargetNotMappedException, ServiceException;

  protected abstract DestinationsHelper getDestinationsHelperInner(String lmtName, QName interfaceName) throws ServiceException;

  protected abstract DestinationsHelper getDestinationsHelperInner(String applicationName, String serviceRefID) throws ServiceException;

  protected abstract Remote getPortForLDInner(String lmtName, Service service) throws TargetNotMappedException, ServiceException;

  protected abstract Remote getPortForLDInner(String lmtName, Service service, QName portTypeName) throws TargetNotMappedException, ServiceException;


}
