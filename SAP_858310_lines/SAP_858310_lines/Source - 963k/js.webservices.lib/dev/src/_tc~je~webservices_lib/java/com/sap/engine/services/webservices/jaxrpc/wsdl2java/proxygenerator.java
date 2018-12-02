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

import com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException;
import com.sap.engine.services.webservices.jaxrpc.schema2java.SchemaToJavaGenerator;
import com.sap.engine.services.webservices.jaxrpc.util.NameConvertor;
import com.sap.engine.services.webservices.jaxrpc.util.PackageBuilder;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.httpgetpost.HttpGetPostBinding;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.soapbinding.MimeHttpBinding;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPorts;
import com.sap.engine.services.webservices.jaxrpc.wsdl2java.lpapi.LogicalPortFactory;
import com.sap.engine.services.webservices.wsdl.WSDLDOMLoader;
import com.sap.engine.services.webservices.wsdl.WSDLDefinitions;
import com.sap.engine.services.webservices.wsdl.WSDLException;

import java.io.File;
import java.util.Properties;
import java.util.Iterator;

/**
 * Highly customizable Proxy generator.
 * It keeps binding implementations separated from it's core.
 *
 * @author Chavdar Baikov (chavdar.baikov@sap.com)
 * @version 6.30
 */
public class ProxyGenerator {

  private PackageBuilder builder;
  private boolean showWarnings = false;

  /**
   * Shows warning messages.
   */
  public void showWarnings() {
    this.showWarnings = true;
  }

  /**
   * Hides warnings.
   */
  public void hideWarnings() {
    this.showWarnings = false;
  }

  /**
   * Default constructor.
   */
  public ProxyGenerator() {
    builder = new PackageBuilder();
//    aditionalClassPath = null;
  }

  /**
   * Returns available transport binding implementations.
   * This method should be more bradly implemented and allow plugging external binding implementations.
   * @return
   */
  private ClientTransportBinding[] getTransportBindings() {
    ClientTransportBinding[] transportBindings = new ClientTransportBinding[2];
    transportBindings[0] = new MimeHttpBinding();
    transportBindings[1] = new HttpGetPostBinding();
    return transportBindings;
  }

  /**
   * Prints waring message to Syste.out
   * @param warningMessage
   */
  private void printWarining(String warningMessage) {
    if (showWarnings) {
      System.out.println("Waring : "+warningMessage);//$JL-SYS_OUT_ERR$
    }
  }

  /**
   * Returns default schema namespace to package mapping of generated custom types.
   * @param config
   * @throws com.sap.engine.services.webservices.jaxrpc.exceptions.ProxyGeneratorException When loading wsdl is not possible.
   */
  public void getDefaultSchemaMapping(ProxyGeneratorConfig config) throws ProxyGeneratorException {
    if (config.getWsdlLocation() == null) {
      throw new ProxyGeneratorException(ProxyGeneratorException.NO_WSDLPATH);
    }
    WSDLDOMLoader loader = new WSDLDOMLoader();
    if (config.getResolver() != null) {
      loader.setWSDLResolver(config.getResolver());
    }
    if (config.isUseProxy()) {
      loader.setHttpProxy(config.getProxyHost(),config.getProxyPort());
    }
    WSDLDefinitions definitions = config.getDefinitions();
    if (definitions == null) {
      try {
        if (config.getLocationMap() != null) {
          definitions = loader.loadMirrorWSDLDocument(config.getWsdlLocation(),config.getLocationMap());
        } else {
          definitions = loader.loadWSDLDocument(config.getWsdlLocation());
        }
      } catch (WSDLException e) {
        throw new ProxyGeneratorException(ProxyGeneratorException.WSDL_PARSING_PROBLEM,e);
      }
    }
    //ClassGenerator generator = new ClassGenerator();
    if (config.getOutputPackage() == null) {
      NameConvertor convertor = new NameConvertor();
      try {
        config.setOutputPackage(convertor.uriToPackage(definitions.getTargetNamespace()));
      } catch (Exception e) {
        config.setOutputPackage(null);
      }
    }
    try {
      definitions.loadSchemaInfo();
      if (definitions.getSchemaInfo() != null && config.isUseProxy()) {
        definitions.getSchemaInfo().setHttpProxy(config.getProxyHost(),config.getProxyPort());
      }
      SchemaToJavaGenerator schema = definitions.getSchemaInfo();
      schema.setPackageBuilder(new PackageBuilder());
      String typesPackage = "types";
      String packageName = config.getOutputPackage();
      if (packageName!= null && packageName.length()!=0) {
        typesPackage = packageName+"."+typesPackage;
      }
      if (schema != null) {
        schema.setMirrorLocations(definitions.getMirrorLocations());
        schema.setMirrorMapping(definitions.getMirrorMapping());
        schema.prepareAll(typesPackage);
        config.setUriToPackageMapping(schema.getUriToPackageMapping());
      } else {
        config.setUriToPackageMapping(new Properties());
      }
    } catch (Exception e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.INVALID_CONFIG,e);
    }
  }
  /**
   * Generates proxy by  given proxy generator configuration.
   * Note! All generator configuration is passed through it's config object.
   * @param config
   */
  public void generateProxy(ProxyGeneratorConfig config) throws ProxyGeneratorException {
    File outputDirFile = new File(config.getTargetDir());
    if (outputDirFile.exists() == false) {
      if  (outputDirFile.mkdirs() == false) {
        throw new ProxyGeneratorException(ProxyGeneratorException.CANT_CREATEPATH,config.getTargetDir());
      }
    }
    if (outputDirFile.isDirectory() == false) {
      throw new ProxyGeneratorException(ProxyGeneratorException.PATH_NOT_DIR,config.getTargetDir());
    }

    if (config.getWsdlLocation() == null && config.getDefinitions() == null) {
      throw new ProxyGeneratorException(ProxyGeneratorException.NO_WSDLPATH);
    }
    WSDLDOMLoader loader = new WSDLDOMLoader();
    if (config.getResolver() != null) {
      loader.setWSDLResolver(config.getResolver());
    }

    if (config.isUseProxy()) {
      loader.setHttpProxy(config.getProxyHost(),config.getProxyPort());
    }
    WSDLDefinitions definitions = config.getDefinitions();
    if (definitions == null) {
      try {
        if (config.getLocationMap() != null) {
          definitions = loader.loadMirrorWSDLDocument(config.getWsdlLocation(),config.getLocationMap());
        } else {
          definitions = loader.loadWSDLDocument(config.getWsdlLocation());
        }
        config.setDefinitions(definitions);
      } catch (WSDLException e) {
        throw new ProxyGeneratorException(ProxyGeneratorException.WSDL_PARSING_PROBLEM,e);
      }
    }
    ClassGenerator generator = new ClassGenerator();
    if (config.getOutputPackage() == null) {
      printWarining("No output package specified. targetNamespace URI will be used.");
      NameConvertor convertor = new NameConvertor();
      try {
        config.setOutputPackage(convertor.uriToPackage(definitions.getTargetNamespace()));
        printWarining("Output root package set to : " + config.getOutputPackage());
      } catch (Exception e) {
        // leave output package null.
        printWarining("Target namespace can not be used to build package name so none will be used.");
      }
    }
    ClientTransportBinding[] tbindings = new ClientTransportBinding[0];
    if (config.getBindings() != null) {
      tbindings = config.getBindings();
    } else {
      tbindings = getTransportBindings(); // Gets Transpot Bindings
    }
    try {
      generator.init(definitions, outputDirFile, config.getOutputPackage(), tbindings);
      if (definitions.getSchemaInfo() != null && config.isUseProxy()) {
        definitions.getSchemaInfo().setHttpProxy(config.getProxyHost(),config.getProxyPort());
      }
      generator.setDefaultSchemaMapping(config.getUriToPackageMapping());
      if (config.getLogicalPortName() != null) {
        generator.setLogicalPortName(config.getLogicalPortName());
      }
      if (config.getLogicalPortPath() != null) {
        generator.setLogicalPortPath(config.getLogicalPortPath());
      }
    } catch (Exception e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.INVALID_CONFIG,e);
    }
    generator.setAlternativeMethods(config.isAdditionalMethods());
    generator.setUseGetPortMethods(config.isJaxRpcMethods());
    generator.setCustomServiceName(config.getCustomServiceName());
    generator.setIsolatedPortType(config.getIsolatePortType());
    generator.setContainerMode(config.isContainerMode());
    generator.setSchemaFrameworkPath(config.getSchemaFrameworkPath());
    generator.setSchemaToJavaMapping(config.getApplicationSchemaToJavaMapping());
    try {
      if (config.isStubsOnly()) {
        LogicalPorts ports = config.getLogicalPorts();
        if (ports==null) {
          if (config.getLogicalPortPath() == null || config.getLogicalPortName() == null) {
            throw new Exception("Path to logical ports ot logical ports not set ! Can not generate Implementation.");
          }
          LogicalPortFactory plfactory = new LogicalPortFactory();
          ports = plfactory.loadLogicalPorts(config.getLogicalPortPath()+File.separator+config.getLogicalPortName());
        }
        ports = generator.generateImplementation(ports,config.isServerHosted());
        config.setLogicalPorts(ports);
      } else {
        if (config.isLPonly()) {
          generator.generateLogicalPorts();
        } else {
          if (config.isInterfacesOnly()) {
            generator.generateInterfaces();
          } else {
            generator.setGenericMode(config.getGenericMode());
            generator.generateAll();
          }
        }
      }
    } catch (Exception e) {
      throw new ProxyGeneratorException(ProxyGeneratorException.GENERATION_PROBLEM,e);
    }
    builder.clear();
    if (config.getAdditionalClassPath() != null) {
      builder.setAditionalClassPath(config.getAdditionalClassPath());
    } else {
      builder.setAditionalClassPath("");
    }
    builder.setPackageRoot(outputDirFile);
    builder.setPackageName(config.getOutputPackage());
    if (config.getUriToPackageMapping()!= null) {
      Iterator it = config.getUriToPackageMapping().values().iterator();
      while (it.hasNext()) {
        String packageName = (String) it.next();
        if (!packageName.startsWith(config.getOutputPackage())) {
          builder.addExternalPackage(packageName);
        }
      }
    }
    try {
      if (config.isCompile()) { // if compile required
        builder.compilePackage();
      }
      if (config.getJarName() != null) { // if make Jar required
        if (config.getJarExtensions() == null || config.getJarExtensions().length == 0) {
          builder.loadPackageClasses(true);
        } else {
          builder.loadPackageFiles(true, config.getJarExtensions());
        }
        //builder.buildJar(new File(outputDirFile, config.getJarName()));
        builder.buildJar(config.getJarName());
      }
    } catch (Exception e) {
      //e.printStackTrace();
      throw new ProxyGeneratorException(ProxyGeneratorException.GENERATION_PROBLEM,e);
    }
    config.setGeneratedSEIFiles(generator.getGeneratedSeiList());
    config.setGeneratedServices(generator.getGeneratedServiceInterfaces());
    config.setAllGeneratedFiles(generator.getFileList());
  }

  public void generateProxy(String wsdlName, String outputDir, String outputPackage, boolean compile, String jarName, boolean interfaces, String[] extensions, boolean additionalMethods) throws Exception {
    ProxyGeneratorConfig config = new ProxyGeneratorConfig(wsdlName,outputDir,outputPackage);
    config.setCompile(compile);
    config.setJarName(jarName);
    config.setAdditionalMethods(additionalMethods);
    config.setJarExtensions(extensions);
    generateProxy(config);
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
  public void generateProxy(String wsdlName, String outputDir, String outputPackage, boolean compile, String jarName, boolean interfaces, boolean additionalMethods) throws Exception {
    if (jarName != null) {
      compile = true;
    }
    generateProxy(wsdlName, outputDir, outputPackage, compile, jarName, interfaces, null , additionalMethods);
  }

  /**
   * Returns output package.
   */
  private static boolean getInterfaces(String[] args) {
    for (int i = 2; i < args.length; i++) {
      if (args[i].equals("-i")) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns output package.
   */
  public static String getPackage(String[] args) {
    for (int i = 2; i < args.length; i++) {
      if (args[i].equals("-p") && (i + 1) < args.length) {
        return (args[i + 1]);
      }
    }

    return null;
  }

  /**
   * Returns additional methods flag.
   * @param args
   * @return
   */
  private static boolean getAdditionalMethods(String[] args) {
    for (int i = 2; i < args.length; i++) {
      if (args[i].equals("-d")) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns compile option. true - on, false - off.
   */
  private static boolean getCompile(String[] args) {
    for (int i = 2; i < args.length; i++) {
      if (args[i].equals("-c")) {
        return true;
      }
    }

    return false;
  }

  /**
   * Jax-Rpc option
   * @param args
   * @return
   */
  private static boolean getPortGet(String[] args) {
    for (int i = 2; i < args.length; i++) {
      if (args[i].equals("-x")) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns output jar name.
   */
  public static String getJar(String[] args) throws ProxyGeneratorException {
    for (int i = 2; i < args.length; i++) {
      if (args[i].equals("-j") && (i + 1) < args.length) {
        try {
          new File(args[i + 1]);
        } catch (Exception e) {
          throw new ProxyGeneratorException(ProxyGeneratorException.NOT_A_FILE,args[i+1]);
        }
        return (args[i + 1]);
      }
    }

    return null;
  }

  /**
   * Returns output http Proxy option.
   */
  public static void getHttpProxy(ProxyGeneratorConfig config,String[] args) {
    for (int i = 2; i < args.length; i++) {
      if (args[i].equals("-h") && (i + 2) < args.length) {
        config.setProxy(args[i+1],args[i+2]);
      }
    }
  }

  /**
   * Returns output http Proxy option.
   */
  public static void getGenericMode(ProxyGeneratorConfig config,String[] args) {
    for (int i = 2; i < args.length; i++) {
      if (args[i].equals("-g")) {
        config.setGenericMode(true);
      }
    }
  }

  /**
   * From this line dowm follows command line processing routines.
   */
  public static void printHelp() {
    System.out.println(" Usage: com.sap.engine.services.webservices.jaxrpc.wsdl2java.ProxyGenerator wsdl_file target_dir [options]");//$JL-SYS_OUT_ERR$
    System.out.println(" option list:");//$JL-SYS_OUT_ERR$
    System.out.println("  [-p package] - package name");//$JL-SYS_OUT_ERR$
    System.out.println("  [-c] - compile");//$JL-SYS_OUT_ERR$
    System.out.println("  [-j jarname] - create jar");//$JL-SYS_OUT_ERR$
    System.out.println("  [-i] - generates interfaces only");//$JL-SYS_OUT_ERR$
    System.out.println("  [-d] - generates rpc style method where possible");//$JL-SYS_OUT_ERR$
    System.out.println("  [-x] - generate JAX-RPC style getXXX methods in service interface");//$JL-SYS_OUT_ERR$
    System.out.println("  [-h proxyHost proxyPort] - use http proxy");//$JL-SYS_OUT_ERR$
  }

  /**
   * ProxyGenerator <wsdl file> <outdir> [options]
   * option list:
   * [-p package] - package name.
   * [-c] - compile.
   * [-j jarname] - create jar.
   * [-i] - create only interfaces.
   */
  public static void main(String args[]) throws ProxyGeneratorException {
    System.out.println("SAP WSDL Proxy Generator 6.30");//$JL-SYS_OUT_ERR$

    if (args.length < 2) {
      printHelp();
      return;
    } else {
      ProxyGenerator generator = new ProxyGenerator();
      System.out.println(" * Generating Proxy from:" + args[0]);//$JL-SYS_OUT_ERR$
      System.out.println(" * To directory:" + args[1]);//$JL-SYS_OUT_ERR$
      ProxyGeneratorConfig config = new ProxyGeneratorConfig(args[0], args[1], getPackage(args));
      config.setCompile(getCompile(args));
      config.setJarName(getJar(args));
      config.setInterfacesOnly(getInterfaces(args));
      config.setAdditionalMethods(getAdditionalMethods(args));
      config.setJaxRpcMethods(getPortGet(args));
      getHttpProxy(config,args);
      getGenericMode(config,args);
      generator.generateProxy(config);
      System.out.println(" * Done");//$JL-SYS_OUT_ERR$
    }
  }

}

