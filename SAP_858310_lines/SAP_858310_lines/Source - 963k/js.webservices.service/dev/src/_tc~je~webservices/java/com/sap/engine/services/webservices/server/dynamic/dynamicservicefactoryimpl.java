/*
 * Copyright (c) 2003 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Bulgaria. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Bulgaria.
 */
package com.sap.engine.services.webservices.server.dynamic;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.xml.sax.InputSource;

import com.sap.engine.frame.core.load.LoadContext;
import com.sap.engine.interfaces.webservices.client.ClientFeatureProvider;
import com.sap.engine.interfaces.webservices.dynamic.DynamicServiceFactory;
import com.sap.engine.lib.descriptors.ws04clientsdd.ComponentScopedRefsDescriptor;
import com.sap.engine.lib.descriptors.ws04clientsdd.ServiceRefDescriptor;
import com.sap.engine.lib.descriptors.ws04clientsdd.WSClientDeploymentDescriptor;
import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.lib.processor.SchemaProcessorFactory;
import com.sap.engine.services.webservices.exceptions.WSLogging;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ClientTransportBinding;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProxyGenerator;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProxyGeneratorConfig;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.RuntimeInformation;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.ServiceBaseServer;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortFactory;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortType;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPorts;
import com.sap.engine.services.webservices.server.WSContainer;
import com.sap.engine.services.webservices.webservices630.server.deploy.WSDeployer;
import com.sap.tc.logging.Location;

/**
 * @author Alexander Zubev
 */
public class DynamicServiceFactoryImpl implements DynamicServiceFactory {

  private Vector getJarsVector(ClassLoader classLoader) {
    LoadContext loadContext = WSContainer.getServiceContext().getCoreContext().getLoadContext();
    Vector loaderJars = new Vector();

    while (classLoader != null) {
      String loaderName = loadContext.getName(classLoader);
      if (loadContext.getClassLoader(loaderName) == null) {
        break;
      }

      String[] resources = loadContext.getResourceNames(classLoader);

      for (int i = 0; i < resources.length; i++) {
        String jar = resources[i];
        if (!loaderJars.contains(jar)) {
//          System.out.println("DynamicServiceFactoryImpl.getJarsVector: adding: " + jar);
          loaderJars.addElement(jar);
        }
      }

      classLoader = classLoader.getParent();
    }

    return loaderJars;
  }

  public Object getWebService(Class wsInterface, String wsclientsDescriptorName) throws Exception {
    return getWebService(wsInterface, wsclientsDescriptorName, wsInterface.getName());
  }

  private static final String getPackageName(Class c) {
    String name = c.getName();
    int i = name.lastIndexOf('.');
    if (i != -1) {
      return name.substring(0, i);
    } else {
      return null;
    }
  }

  public Object getWebService(Class wsInterface, String wsclientsDescriptorName, String componentName) throws Exception {
    File tempDirectory = null;
    try {
      ClassLoader extLoader = wsInterface.getClassLoader();
      Vector loaderJars = new Vector();
      loaderJars.addAll(getJarsVector(this.getClass().getClassLoader()));
      loaderJars.addAll(getJarsVector(extLoader));

      String packageName = getPackageName(wsInterface);
      tempDirectory = getTempProxyDir();

      DynamicWSDLResolver wsdlResolver = new DynamicWSDLResolver(wsInterface);

      InputSource wsclientsDescriptor = wsdlResolver.getFromClassLoader("/META-INF/ws-clients-descriptors/" + wsclientsDescriptorName);

      InputStream input = wsclientsDescriptor.getByteStream();
      WSClientDeploymentDescriptor wsClientsDesc = (WSClientDeploymentDescriptor)SchemaProcessorFactory.getProcessor(SchemaProcessorFactory.WS04CLIENTDD).parse(input);
      input.close();
      ComponentScopedRefsDescriptor components[] = wsClientsDesc.getComponentScopedRefs();
      if (components.length == 0) {
        throw new Exception("Cannot find component descriptors in the ws-clients-deployment-descriptor!");
      }
      ServiceRefDescriptor[] serviceRefs =  components[0].getServiceRef();
      if (serviceRefs.length == 0) {
        throw new Exception("Cannot find service reference in the ws-clients-deployment-descriptor!");
      }
      ServiceRefDescriptor serviceRef = serviceRefs[0];
      String lpFileName = serviceRef.getLogicalPortsFile();
      InputSource source = wsdlResolver.getFromClassLoader("/META-INF/ws-clients-descriptors/" + lpFileName);
      LogicalPortFactory factory = new LogicalPortFactory();
      InputStream in = source.getByteStream();
      LogicalPorts originalPorts = factory.loadLogicalPorts(in);
      in.close();

      StringBuffer classpath = new StringBuffer();
      classpath.append(WSDeployer.getJarsPath());
      classpath.append(File.pathSeparator + tempDirectory.getAbsolutePath());
      for (int i = 0; i < loaderJars.size(); i++) {
        classpath.append(File.pathSeparator + loaderJars.elementAt(i));
      }
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.debugT("Classpath for stubs is: " + classpath.toString());

      String rootWSDL = serviceRef.getWsdlFile();
      String wsdlMappingFile = serviceRef.getUriMappingFile();
      String jndiName = serviceRef.getServiceRefName();

      StringTokenizer wsdlTokenizer = new StringTokenizer(rootWSDL, ";");
      if (!wsdlTokenizer.hasMoreTokens()) {
        throw new Exception("Incorrect WSDL file in the deployment descriptor!");
      }
      StringTokenizer mappingTokenizer = new StringTokenizer(wsdlMappingFile, ";");
      Vector lpVector = new Vector();
      String serviceName = null;
      while (wsdlTokenizer.hasMoreTokens()) {
        String wsdlFile = wsdlTokenizer.nextToken();
        String mappingFile;
        if (mappingTokenizer.hasMoreTokens()) {
          mappingFile = mappingTokenizer.nextToken();
        } else {
          throw new Exception("The number of mapping files is not equal to the number of the WSDL files!");
        }
        Properties wsdlMapping = null;
        if (wsdlMappingFile != null) {
          InputSource mappingSource = wsdlResolver.getFromClassLoader("/META-INF/ws-clients-descriptors/" + mappingFile);
          wsdlMapping = new Properties();
          InputStream mappingStream = mappingSource.getByteStream();
          wsdlMapping.load(mappingStream);
          mappingStream.close();
        }

        ProxyGeneratorConfig config = new ProxyGeneratorConfig(wsdlFile, tempDirectory.getAbsolutePath(), packageName);
        ClientFeatureProvider[] clientInterfaces = WSContainer.getComponentFactory().listClientransportBindingInterfaces();
        ClientTransportBinding[] bindings = new ClientTransportBinding[clientInterfaces.length];
        for (int i = 0; i < clientInterfaces.length; i++) {
          bindings[i] = (ClientTransportBinding) clientInterfaces[i];
        }
        config.setBindings(bindings);
        config.setLogicalPorts(originalPorts);
        config.setStubsOnly(true);
        config.setResolver(wsdlResolver);
        if (wsdlMapping != null) {
          config.setLocationMap(wsdlMapping);
        }
        config.setAdditionalClassPath(classpath.toString());
        config.setCompile(true);

        ProxyGenerator gen = new ProxyGenerator();
        gen.generateProxy(config);
        LogicalPorts logicalPorts = config.getLogicalPorts();
        serviceName = logicalPorts.getName();
        LogicalPortType[] portTypes = logicalPorts.getLogicalPort();
        for (int i = 0; i < portTypes.length; i++) {
          lpVector.addElement(portTypes[i]);
        }
      }

//      ProxyGeneratorServer generator = new ProxyGeneratorServer(WSContainer.getComponentFactory());
//      if (wsdlMapping != null) {
//        generator.setLocationMap(wsdlMapping);
//      }
//      generator.setAdditionalClasspath(classpath.toString());
//      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
//      wsLocation.debugT("Classpath for stubs is: " + classpath.toString());

//      LogicalPorts logicalPorts = generator.generateProxy(new File(rootWSDL), wsdlResolver,
//              tempDirectory, packageName, true, originalPorts);

      DynamicStubsLoader stubsLoader = new DynamicStubsLoader(extLoader, tempDirectory);
      if (serviceName == null || serviceName.trim().length() == 0) {
        serviceName = "DefaultServiceImpl";
      } else {
        serviceName += "Impl";
      }
      serviceName = packageName + "." + serviceName;
      Class serviceClass = stubsLoader.loadClass(serviceName);
      ServiceBaseServer service = (ServiceBaseServer) serviceClass.newInstance();
      LogicalPorts logicalPorts = new LogicalPorts();
      LogicalPortType[] portTypes = new LogicalPortType[lpVector.size()];
      lpVector.copyInto(portTypes);
      logicalPorts.setLogicalPort(portTypes);
      service.init(logicalPorts, WSContainer.getComponentFactory(), stubsLoader);
      RuntimeInformation runtimeInformation = new RuntimeInformation();
      runtimeInformation.setJndiName(jndiName);
      runtimeInformation.setApplicationName(componentName);
      runtimeInformation.setArchiveName("");  
      service.setRuntimeInformation(runtimeInformation);
      service.setHTTPProxyResolver(WSContainer.getHTTPProxyResolver());

      return service;
    } catch (Exception e) {
      Location wsLocation = Location.getLocation(WSLogging.DEPLOY_LOCATION);
      wsLocation.catching(e);

      throw e;
    } finally {
      if (tempDirectory != null) {
        FileUtils.deleteDirectory(tempDirectory);
      }
    }
  }

//  private void

  private synchronized File getTempProxyDir() {
    String tempDirectory = WSContainer.getServiceContext().getServiceState().getWorkingDirectoryName() + File.separatorChar + "dynamic";
//    String tempDirectory = "D:/develop/InQMy/AppServer/dev/bin/cluster/server/temp/webservices/dynamic";
    File tempDir;
    synchronized (tempDirectory) {
      do {
        tempDir = new File(tempDirectory, "proxy" + System.currentTimeMillis());
      } while (tempDir.exists());
      tempDir.mkdirs();
    }
    return tempDir;
  }
}
