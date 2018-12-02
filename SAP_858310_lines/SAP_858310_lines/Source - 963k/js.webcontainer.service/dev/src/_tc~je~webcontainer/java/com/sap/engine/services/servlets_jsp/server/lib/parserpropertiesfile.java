/*
 * Copyright (c) 2000-2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.sap.engine.boot.SystemProperties;
import com.sap.engine.compilation.CompilationFactory;
import com.sap.engine.compilation.Compiler;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParserFactory;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.tc.logging.Location;

/**
 * This class reads the file /work/parser.properties for each application start.
 * There is written some data from the previous run that is vital for the compilation.
 * If the data in the file is different that the current settings, then all the contents of the work directory is deleted and the new parser.properties file is created.
 * @author Todor Mollov
 * @version 7.0
 */
public class ParserPropertiesFile {

  //parser.properties constants
  private static final String FILE_NAME = "parser.properties";
  private static final String FILE_HEADER = "JSP Parser generates java files for JSPs with these settings";
  private static final String SYSTEM_PROPERTY_JDK = "java.specification.version";
  //keys for the properties file
  private static final String KEY_PARSER_VERSION =  "ParserVersion";
  private static final String KEY_JAVA_ENCODING = "JavaEncoding";
  private static final String KEY_JDK = "JDK";
  private static final String KEY_TARGET = "compiler.target";
  private static final String KEY_SOURCE = "compiler.source";
  private static final String KEY_COMPILER = "compiler.version";

  private String externalCompiler = "javac";
  private File workDir;
  private String applicationName;

  private String parserVersion = JspParserFactory.getParserVersion();
  private String target = "";
  private String source = "";
  private String encoding = "";
  private String jdk_version = "";
  //singleton
  private static ParserPropertiesFile instance;

  /**
   * Made it singleton, because if there are 500 applications to be started, 500 objects should be created.
   * @param workdir
   * @param applicationName
   * @param externalCompilerName
   * @param encoding
   * @return
   * @throws IOException
   */
  public static ParserPropertiesFile getInstance(File workdir, String applicationName, String externalCompilerName, String encoding) throws IOException {
    if ( instance == null ) {
      synchronized (ParserPropertiesFile.class) {
        if( instance == null ) {
          instance = initCompiler(externalCompilerName, encoding);
        }
      }
    }
    ParserPropertiesFile newInstance = new ParserPropertiesFile();
    newInstance.applicationName = applicationName;
    newInstance.encoding = instance.encoding;
    newInstance.externalCompiler = instance.externalCompiler;
    newInstance.workDir = workdir;
    newInstance.jdk_version = instance.jdk_version;
    newInstance.source = instance.source;
    newInstance.target = instance.target;
    return newInstance;
  }


  /**
   * Creates new instance and inits all fields with current runtime settings.
   * @param workDir
   * @param internalCompiler
   * @param externalCompilerName
   * @param encoding
   * @throws IOException
   */
  private static ParserPropertiesFile initCompiler(String externalCompilerName, String encoding) throws IOException {
    ParserPropertiesFile file = new ParserPropertiesFile();
    file.encoding = encoding;
    Compiler compiler = null;
    try {
      try {
        Properties props = new Properties();
        props.put(CompilationFactory.COMPILER_EXECUTABLE, externalCompilerName);
        compiler = CompilationFactory.getCompiler(props);
      } catch (IOException e) {
        //get default
        compiler = CompilationFactory.getCompiler((Properties) null);
      }
      if (compiler.getSource() != null) {
        file.source = compiler.getSource();
      } else {
        file.source = "";
      }
      if (compiler.getTarget() != null) {
        file.target = compiler.getTarget();
      } else {
        file.target = "";
      }
      if (externalCompilerName.equals("") || externalCompilerName.endsWith("javac") || externalCompilerName.endsWith("javac.exe")) {
        //get info for javac
        file.externalCompiler = compiler.getVersion();
        if (file.externalCompiler == null) {
          // Compiler library should not return null!
          file.externalCompiler = externalCompilerName;
        }
      } else {
        file.externalCompiler = externalCompilerName;
      }
    } finally {
      if (compiler != null) {
        compiler.close();
      }
    }
    file.jdk_version = SystemProperties.getProperty(SYSTEM_PROPERTY_JDK);
    return file;
  }

  /**
   * creates this specific file in the work directory that keeps information of the parsers version, java encoding and jdk
   *
   * @throws IOException
   */
  public void createParserPropertiesFile() throws IOException {
    File parserProperiesFile = new File(workDir, FILE_NAME);
    Properties parserProps = new Properties();
    parserProps.put(KEY_COMPILER, externalCompiler);
    parserProps.put(KEY_JAVA_ENCODING, encoding);
    parserProps.put(KEY_JDK, jdk_version);
    parserProps.put(KEY_PARSER_VERSION, parserVersion);
    parserProps.put(KEY_SOURCE, source);
    parserProps.put(KEY_TARGET, target);
    FileOutputStream fis = new FileOutputStream(parserProperiesFile);
    try {
      parserProps.store(fis, FILE_HEADER);
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
  }

  /**
   * returns the parsed name-value pairs form parser.properties (former parser.version) file
   * or null if file not found
   *
   * @return java.util.Properties or null
   */
  private Properties getParserProperties() throws IOException {
    File parserPropertiesFile = new File(workDir, FILE_NAME);
    if (!parserPropertiesFile.exists()) {
      return null;
    }

    Properties parserProps = new Properties();
    FileInputStream fis = new FileInputStream(parserPropertiesFile);
    try {
      parserProps.load(fis);
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
    return parserProps;
  }

  /**
   * Checks if some of properties in parser.properties are different
   *
   * @return true - if some of properties changed, false - in all of the properties have the same values
   */
  public boolean isParserPropertiesChanged() throws IOException {
    Location traceLocation = LogContext.getLocationDeploy();
    boolean bePath = traceLocation.bePath();
  	Properties parserProperties = getParserProperties();
    if (parserProperties == null) {
      if (bePath) {
				traceLocation.pathT("Parser.properties file not found in the application work directory [" + workDir + "]! It will be created.");
			}
			return true;
    }
    //check parser version
    if( !parserVersion.equals(parserProperties.getProperty(KEY_PARSER_VERSION)) ){
      if (bePath) {
				traceLocation.pathT("[" + KEY_PARSER_VERSION + "] parser property is changed! The old value is [" +
						parserProperties.getProperty(KEY_PARSER_VERSION) + "], the new is [" + parserVersion + "]");
			}
			return true;
    }
    //check java encoding
    if( !encoding.equals(parserProperties.getProperty(KEY_JAVA_ENCODING)) ){
       if (bePath) {
				traceLocation.pathT("[" + KEY_JAVA_ENCODING + "] parser property is changed! The old value is [" +
						parserProperties.getProperty(KEY_JAVA_ENCODING) + "], the new is [" + encoding + "]", applicationName);
			}
			return true;
    }
    //check target
    if( !target.equals(parserProperties.getProperty(KEY_TARGET)) ){
      if (bePath) {
				traceLocation.pathT("[" + KEY_TARGET + "] parser property is changed! The old value is [" +
						parserProperties.getProperty(KEY_TARGET) + "], the new is [" + target + "]");
			}
			return true;
    }
    //check source
    if( !source.equals(parserProperties.getProperty(KEY_SOURCE)) ){
      if (bePath) {
				traceLocation.pathT("[" + KEY_SOURCE + "] parser property is changed! The old value is [" +
						parserProperties.getProperty(KEY_SOURCE) + "], the new is [" + source + "]");
			}
			return true;
    }
    //check JDK
    if( !jdk_version.equals(parserProperties.getProperty(KEY_JDK)) ){
      if (bePath) {
				traceLocation.pathT("[" + KEY_JDK + "] parser property is changed! The old value is [" +
						parserProperties.getProperty(KEY_JDK) + "], the new is [" + jdk_version + "]");
			}
			return true;
    }
    //check compiler
    if( !externalCompiler.equals(parserProperties.getProperty(KEY_COMPILER)) ){
      if (bePath) {
				traceLocation.pathT("[" + KEY_COMPILER + "] parser property is changed! The old value is [" +
						parserProperties.getProperty(KEY_COMPILER) + "], the new is [" + externalCompiler + "]");
			}
			return true;
    }
    return false;
  }

  /**
   * deletes all files and directories in the given directory
   *
   */
  public void deleteWork() {
    File[] files = workDir.listFiles();
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        deleteRecursivelly(files[i]);
      }
    }
  }

  /**
   * deletes recursively this directory or directly deletes this file
   *
   * @param fileToDelete
   */
  private void deleteRecursivelly(File fileToDelete) {
    if (fileToDelete.isFile()) {
      fileToDelete.delete();
    } else {
      File[] filesInDir = fileToDelete.listFiles();
      if (filesInDir != null) {
        for (int i = 0; i < filesInDir.length; i ++) {
          deleteRecursivelly(filesInDir[i]);
        }
      }
      fileToDelete.delete();
    }
  }
}
