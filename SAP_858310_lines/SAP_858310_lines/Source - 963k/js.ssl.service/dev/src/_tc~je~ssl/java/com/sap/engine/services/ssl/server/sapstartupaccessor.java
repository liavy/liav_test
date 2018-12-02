package com.sap.engine.services.ssl.server;

import com.sap.engine.services.webservices.espbase.client.dynamic.ServiceFactoryConfig;
import com.sap.engine.services.webservices.espbase.client.dynamic.GenericServiceFactory;
import com.sap.engine.services.webservices.espbase.client.dynamic.DGenericService;
import com.sap.engine.services.webservices.espbase.client.dynamic.DInterface;
import com.sap.engine.services.webservices.espbase.client.dynamic.DInterfaceInvoker;
import com.sap.engine.services.webservices.espbase.client.dynamic.ParametersConfiguration;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.impl.ObjectFactoryImpl;
import com.sap.engine.services.webservices.espbase.client.dynamic.content.GenericObject;

import javax.xml.rpc.ServiceException;
import static javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY;
import javax.xml.namespace.QName;
import java.util.Vector;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

/**
 * User: I024108
 * Version: 7.11
 */
public class SAPStartupAccessor {
  private static String instanceNumber = null;
  private static String SAPStartupPort = null;

  public static void setInstanceNumber(String instanceNumber) {
    SAPStartupAccessor.instanceNumber = instanceNumber;
    SAPStartupAccessor.SAPStartupPort = "5" + instanceNumber + "13"; // the default SAPStartup webservice port
  }

  public static void setSAPStartupPort(String port) {
    SAPStartupAccessor.SAPStartupPort = port;
  }

  public static Vector<String> getHTTPSPorts() {
    try {
      return getHTTPSPorts(instanceNumber);
    } catch (Exception e) {
      ServerService.dump("WebService client error", e);
    }
    return new Vector<String>();
  }

  private static Vector<String> getHTTPSPorts(String instanceNumber) throws ServiceException, InvocationTargetException, RemoteException {
    ServiceFactoryConfig serviceFactoryConfig = new ServiceFactoryConfig();
    GenericServiceFactory serviceFactory = GenericServiceFactory.newInstance();

    String wsdlURL = "http://localhost:" + SAPStartupPort + "/?wsdl";
    String wsEndpoint = "http://localhost:" + SAPStartupPort + "/SAPControl.cgi";
    String namespace = "urn:SAPControl";
    String serviceName = "SAPControlPortType";
    QName serviceQN = new QName(namespace, serviceName);
    DGenericService service = serviceFactory.createService(wsdlURL, serviceFactoryConfig);
    DInterface _interface = service.getInterfaceMetadata(serviceQN);
    QName[] ports = _interface.getPortNames();

    DInterfaceInvoker invoker = _interface.getInterfaceInvoker(ports[0]);
    ParametersConfiguration cfg = invoker.getParametersConfiguration("GetAccessPointList");
    invoker.setProperty(ENDPOINT_ADDRESS_PROPERTY, wsEndpoint);
    invoker.invokeOperation("GetAccessPointList", cfg, new ObjectFactoryImpl(service.getTypeMetadata()));
    GenericObject result = (GenericObject) cfg.getOutputParameterValue("GetAccessPointListResponse");
    GenericObject accessPointField = (GenericObject) result._getField(new QName("accesspoint"));
    GenericObject[] points = (GenericObject[]) accessPointField._getField(new QName("item"));
    Vector<String> output = new Vector<String>(points.length);
    String prot = null;
    Integer port = null;

    int i = 0;
    for (GenericObject singleAccessPoint: points) {
      prot = (String) singleAccessPoint._getField(new QName("protocol"));
      port = (Integer) singleAccessPoint._getField(new QName("port"));
      if ("HTTPS".equals(prot)) {
        ServerService.dump("  (" + (i++) + "): prot." + prot + ", port." + port);
        output.add(port.toString());
      }
    }

    return output;
  }


}
