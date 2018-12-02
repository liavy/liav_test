/*
 * Copyright (c) 2002 by SAP Labs Sofia AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.webservices.jaxrpc.wsdl2java;

import com.sap.engine.interfaces.webservices.client.ClientFeatureProvider;
import com.sap.engine.interfaces.webservices.runtime.component.ClientComponentFactory;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;
import com.sap.engine.services.webservices.jaxrpc.util.PackageBuilder;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortFactory;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPorts;
import com.sap.engine.services.webservices.wsdl.WSDLDOMLoader;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * Server side Proxy generator. Used on server side to generate proxy classes.
 * Uses Logical Ports + WSDL. Logical ports must be matched to some correct binding e.g. must not be Logical Port Templates.
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ProxyGeneratorServer {
  
  private PackageBuilder builder;
  private String aditionalClassPath;
  private ClientComponentFactory factory;
  private Hashtable locationMap = null;
  private String additionalClasspath;

  public ProxyGeneratorServer(ClientComponentFactory factory) {
    builder = new PackageBuilder();
    aditionalClassPath = null;
    this.factory = factory;
  }

  /**
   * When using mirror wsdl structure must set a mapping from remote locations to local locations.
   * @param locationMap - map from real absolute locations to local mirror lcoations.
   */
  public void setLocationMap(Hashtable locationMap) {
    this.locationMap = locationMap;
  }

  public void setAdditionalClasspath(String additionalClasspath) {
    this.additionalClasspath = additionalClasspath;
  }

  /**
   * Does the proxy Generation. Generates all classes Interfaces and Stubs.
   * Package location.
   *  root. - package common classes
   *  root.types - serializers
   *  root.holders - custom holders
   *  also except SEI Interfaces and Stub implementations also following classes are generated
   *    -  types.xml - type mapping framework description use this to add custom serializers and deserializers.
   *    -  logicalPorts.xml - file describind defined logical ports use this to set any features you want.
   */
  public LogicalPorts generateProxy(File wsdlName, File outputDir, String outputPackage, boolean compile) throws Exception {

    if (outputDir.isDirectory() == false) {
      throw new Exception(" Output directory name given is not a directory name !");
    }

    outputDir.mkdirs();

    if (wsdlName == null) {
      throw new Exception(" Incorrect WSDL name passed !");
    }

    WSDLDOMLoader loader = new WSDLDOMLoader();
    WSDLDefinitions definitions = null;
    if (locationMap != null) {
      definitions = loader.loadMirrorWSDLDocument(wsdlName.getAbsolutePath(),locationMap);
    } else {
      definitions = loader.loadWSDLDocument(wsdlName.getAbsolutePath());
    }
    ClassGenerator generator = new ClassGenerator();

    if (outputPackage == null) {
      System.out.println(" * No output package specified. targetNamespace URI will be used.");//$JL-SYS_OUT_ERR$
      NameConvertor convertor = new NameConvertor();
      outputPackage = convertor.uriToPackage(definitions.getTargetNamespace());
    }

    System.out.println(" * Output root package set to : " + outputPackage);//$JL-SYS_OUT_ERR$
    ClientFeatureProvider[] tbindingsInterface = factory.listClientransportBindingInterfaces();
    ClientTransportBinding[] tbindings = new ClientTransportBinding[tbindingsInterface.length];
    for (int i=0; i<tbindingsInterface.length; i++) {
      tbindings[i] = (ClientTransportBinding) tbindingsInterface[i];
    }
    generator.init(definitions, outputDir, outputPackage, tbindings);
    LogicalPorts result = generator.generateStubs();
    if (aditionalClassPath != null) {
      builder.setAditionalClassPath(aditionalClassPath);
    } else {
      builder.setAditionalClassPath("");
    }

    builder.setPackageRoot(outputDir);
    builder.setPackageName(outputPackage);

    if (compile) { // if compile required
      builder.compilePackage();
    }
    return result;
  }
  
  /**
   * Does the proxy Generation. Generates all classes Interfaces and Stubs.
   * Package location.
   *  root. - package common classes
   *  root.types - serializers
   *  root.holders - custom holders
   *  also except SEI Interfaces and Stub implementations also following classes are generated
   *    -  types.xml - type mapping framework description use this to add custom serializers and deserializers.
   *    -  logicalPorts.xml - file describind defined logical ports use this to set any features you want.
   */
  public LogicalPorts generateProxy(File wsdlName, File outputDir, String outputPackage, boolean compile, File logicalPorts) throws Exception {
    LogicalPortFactory plfactory = new LogicalPortFactory();
    LogicalPorts ports = plfactory.loadLogicalPorts(logicalPorts);

    return generateProxy(wsdlName, null, outputDir, outputPackage, compile, ports);
  }

  public LogicalPorts generateProxy(File wsdlName, EntityResolver wsdlResolver, File outputDir, String outputPackage, boolean compile, LogicalPorts ports) throws Exception {
    if (wsdlName == null) {
      throw new Exception(" Incorrect WSDL name passed !");
    }

    WSDLDOMLoader loader = new WSDLDOMLoader();
    if (wsdlResolver != null) {
      loader.setWSDLResolver(wsdlResolver);
    }
    WSDLDefinitions definitions = null;
    if (locationMap != null) {
      definitions = loader.loadMirrorWSDLDocument(wsdlName.getAbsolutePath(),locationMap);
    } else {
      definitions = loader.loadWSDLDocument(wsdlName.getAbsolutePath());
    }

    return generateProxy(definitions, outputDir, outputPackage, compile, ports);
  }

//  public LogicalPorts generateProxy(String rootWSDL, EntityResolver wsdlResolver, File outputDir, String outputPackage, boolean compile, LogicalPorts ports) throws Exception {
//    WSDLDOMLoader loader = new WSDLDOMLoader();
//    loader.setWSDLResolver(wsdlResolver);
//    InputSource source = wsdlResolver.resolveEntity(null, rootWSDL);
//    if (source == null) {
//      throw new IOException("Cannot load WSDL from the classloader: " + rootWSDL);
//    }
//    InputStream in = source.getByteStream();
//    WSDLDefinitions definitions = loader.loadWSDLDocument(in, rootWSDL);
//    in.close();
//
//    return generateProxy(definitions, outputDir, outputPackage, compile, ports);
//  }

  private LogicalPorts generateProxy(WSDLDefinitions definitions, File outputDir, String outputPackage, boolean compile, LogicalPorts ports) throws Exception {
    if (outputDir.isDirectory() == false) {
      throw new Exception(" Output directory name given is not a directory name !");
    }

    outputDir.mkdirs();

    ClassGenerator generator = new ClassGenerator();

    if (outputPackage == null) {
      System.out.println(" * No output package specified. targetNamespace URI will be used.");//$JL-SYS_OUT_ERR$
      NameConvertor convertor = new NameConvertor();
      outputPackage = convertor.uriToPackage(definitions.getTargetNamespace());
    }

    System.out.println(" * Output root package set to : " + outputPackage);//$JL-SYS_OUT_ERR$
    ClientFeatureProvider[] tbindingsInterface = factory.listClientransportBindingInterfaces();
    ClientTransportBinding[] tbindings = new ClientTransportBinding[tbindingsInterface.length];
    for (int i=0; i<tbindingsInterface.length; i++) {
      tbindings[i] = (ClientTransportBinding) tbindingsInterface[i];
    }
    generator.init(definitions, outputDir, outputPackage, tbindings);
    LogicalPorts result = generator.generateStubs(ports);
    generator.generateServiceImpl(result, null, true);
    if (aditionalClassPath != null) {
      builder.setAditionalClassPath(aditionalClassPath);
    } else {
      builder.setAditionalClassPath("");
    }

    builder.setPackageRoot(outputDir);
    builder.setPackageName(outputPackage);

    if (compile) { // if compile required
      builder.setAditionalClassPath(additionalClasspath);
      builder.compilePackage();
    }
    return result;
  }
  
  

}
