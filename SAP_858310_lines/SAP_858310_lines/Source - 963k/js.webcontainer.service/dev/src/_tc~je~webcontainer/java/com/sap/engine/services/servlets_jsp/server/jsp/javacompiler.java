/*
 * Copyright (c) 2006 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */
package com.sap.engine.services.servlets_jsp.server.jsp;

/*
 *
 * @author Galin Galchev
 * @version 4.0
 */
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.sap.engine.boot.SystemProperties;
import com.sap.engine.compilation.CompilationFactory;
import com.sap.engine.compilation.Compiler;
import com.sap.engine.compilation.CompilerException;
import com.sap.engine.compilation.CompilerProcessFailureException;
import com.sap.engine.frame.container.ServiceNotLoadedException;
import com.sap.engine.lib.util.ArrayObject;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.jsp.exceptions.CompilingException;
import com.sap.tc.logging.Location;

/**
 * Compiles java file using javac compiler. It searches for java file into a given directories.
 */
public class JavaCompiler {
  private static Location currentLocation = Location.getLocation(JavaCompiler.class);
  private static Location traceLocation = LogContext.getLocationRequestInfoServer();
  /**
   * A system file separator
   */
  private static final String sepPath = SystemProperties.getProperty("path.separator");


  /**
   *
   * Compiles java file using external compiler. A classpath must be passed.
   * If the process returns IOException, one more try is performed with the compiler from "java.home".
   * @param file file to be compiled
   * @param contextWorkDir temp directory of this context
   * @param applicationName the name of the application
   * @param vClassPath a vector containing the all classpaths where the classes will be searched for.
   * @param addClasspath
   * @param externalCompilerName - serviceContext.getWebContainerProperties().getExternalCompiler()
   * @param compileWithDebugInfo - serviceContext.getWebContainerProperties().compileWithDebugInfo()
   * @throws CompilingException
   * @throws IOException
   */
  private void compileExternal(String file, String contextWorkDir, 
                               ArrayObject vClassPath, String externalCompilerName, boolean compileWithDebugInfo, String javaVersionForCompilation) throws CompilingException, IOException {
    String warClassPath = "";
    Object[] enumObjects = vClassPath.toArray();

    for (int i = 0; i < enumObjects.length; i++) { 
        warClassPath += (String) enumObjects[i] + sepPath;
    }

    String generateDir = contextWorkDir;

    if (contextWorkDir.endsWith(ParseUtils.separator)) {
      generateDir = contextWorkDir.substring(0, contextWorkDir.length() - 1);
    }
    Compiler compiler = null;
    try {
      boolean isJikes = externalCompilerName.toLowerCase().indexOf("jikes") > 0;
       compiler = getCompiler(externalCompilerName);
      compiler.addSourcepath(contextWorkDir);
      if ( compileWithDebugInfo ) {
        compiler.setGenerate("");
      }
      if( !isJikes ){
        compiler.setEncoding("UTF-8");
      }      
      compiler.setNowarn(true);
      if( isJikes ){
        compiler.addClasspath(SystemProperties.getProperty("sun.boot.class.path") + sepPath + SystemProperties.getProperty("java.class.path") + sepPath + warClassPath );  
      }else{
        compiler.addClasspath(SystemProperties.getProperty("java.class.path") + sepPath + warClassPath ); 
      }
      compiler.setDirectory(generateDir);
      compiler.addJavaFileToCompile(file);
      if( javaVersionForCompilation != null ) {
        compiler.setTarget(javaVersionForCompilation);
        compiler.setSource(javaVersionForCompilation);
      }
			if (traceLocation.beInfo()) {
				traceLocation.infoT("Compiling JSP [" + file + "] using external compiler. JAVAC arguments: [" + compiler.getCommandLine() + "]");
			}
      compiler.compile();
      
    } catch (CompilerProcessFailureException ex) {
      if( compiler!= null ){
        //don't forget to close the compiler, otherwise thousands of empty directories are created in j2ee\cluster\server0\compilerTempDir 
        compiler.close();
      }
        LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceError("ASJ.web.000113",
          "Cannot start compilation process with the specified external compiler [{0}]. "+
          " The full path to the java compiler will be now taken from the \"java.home\" system property and the operation will be started once again. ", 
          new Object[] {externalCompilerName}, ex, null, null);

      //probably javac is not in the path. Try again with full path to javac taken from the system property "java.home" 
      String javaHome = SystemProperties.getProperty("java.home");
      if (((javaHome != null) && (javaHome.length() > 0)) && (javaHome.endsWith("jre") || javaHome.endsWith("jre" + File.separator))) {
        javaHome = javaHome.substring(0, javaHome.indexOf("jre"));
        javaHome = javaHome + "bin" + File.separator + "javac";
      } else {
        javaHome = "javac";
      }

      compiler = getCompiler(javaHome);
      compiler.addSourcepath(contextWorkDir);
      if ( compileWithDebugInfo ) {
        compiler.setGenerate("");
      }
      compiler.setEncoding("UTF-8");
      compiler.setNowarn(true);
      compiler.addClasspath(SystemProperties.getProperty("java.class.path") + sepPath + warClassPath ); 
      compiler.setDirectory(generateDir);
      compiler.addJavaFileToCompile(file);
      if( javaVersionForCompilation != null ) {
        compiler.setTarget(javaVersionForCompilation);
        compiler.setSource(javaVersionForCompilation);
      }

			if (traceLocation.beInfo()) {
				traceLocation.infoT("Compiling JSP [" + file + "] using external compiler. JAVAC arguments: [" + compiler.getCommandLine() + "]");
			}
      try{ 
        compiler.compile();
      }catch (CompilerException e) {
        throw new CompilingException(CompilingException.ERROR_IN_EXECUTING_THE_PROCESS_OF_COMPILATION, new Object[] {e.getLocalizedMessage()}, e);
      }
    }catch (CompilerException e) {
      throw new CompilingException(CompilingException.ERROR_IN_EXECUTING_THE_PROCESS_OF_COMPILATION, new Object[] {e.getLocalizedMessage()}, e);
    }finally{
      if( compiler!= null ){
        //don't forget to close the compiler, otherwise thousands of empty directories are created in j2ee\cluster\server0\compilerTempDir 
        compiler.close();
      }
    }
  }
  
  
  
  /**
   * Compiles java file using internal compiler. A classpath must be passed.
   *
   * @param   file  file to be compiled
   * @param   contextWorkDir  temp directory of this context
   * @param   applicationName  the name of the application
   * @param   vClassPath  a vector containing the all classpaths where the classes will be searched for.
   * @param   compileWithDebugInfo  - serviceContext.getWebContainerProperties().compileWithDebugInfo()
   * @exception   CompilingException
   * @deprecated
   */
  private void compileInternal(String file, String contextWorkDir, 
                               ArrayObject vClassPath, boolean  compileWithDebugInfo, String javaVersionForCompilation) throws CompilingException, IOException {
    String warClassPath = "";
    Object[] enumObjects = vClassPath.toArray();

    for (int i = 0; i < enumObjects.length; i++) {
      warClassPath += (String) enumObjects[i] + sepPath;
    }
    String generateDir = contextWorkDir;

    if (contextWorkDir.endsWith(ParseUtils.separator)) {
      generateDir = contextWorkDir.substring(0, contextWorkDir.length() - 1);
    }
    Compiler compiler = getCompiler(null);
    compiler.addSourcepath(contextWorkDir);
    if ( compileWithDebugInfo ) {
      compiler.setGenerate("");
    }
    compiler.setEncoding("UTF-8");
    compiler.setNowarn(true);
    compiler.addClasspath(SystemProperties.getProperty("java.class.path") + sepPath + warClassPath); 
    compiler.setDirectory(generateDir);
    compiler.addJavaFileToCompile(file);
    if( javaVersionForCompilation != null ) {
      compiler.setTarget(javaVersionForCompilation);
      compiler.setSource(javaVersionForCompilation);
    }
    if (traceLocation.beInfo()) {
			traceLocation.infoT("Compiling JSP [" + file + "] using internal compiler. JAVAC arguments: [" + compiler.getCommandLine() + "]");
		}
    try{ 
      compiler.compile();
    }catch (CompilerException e) {
      throw new CompilingException(CompilingException.ERROR_IN_EXECUTING_THE_PROCESS_OF_COMPILATION, new Object[] {e.getLocalizedMessage()}, e);
    }finally{
      if( compiler!= null ){
        //don't forget to close the compiler, otherwise thousands of empty directories are created in j2ee\cluster\server0\compilerTempDir 
        compiler.close();
      }
    }
  }
  
  /**
   * Compiles java file. A classpath must be passed.
   *
   * @param file
   * @param contextWorkDir
   * @param applicationName
   * @param vClassPath
   * @param resourceNames - serviceContext.getLoadContext().getResourceNames(applicationName)
   * @param internalCompiler
   * @throws CompilingException
   * @throws IOException
   */	
  public void compile(String file, String contextWorkDir, ArrayObject vClassPath,  
  						boolean internalCompiler, boolean compileWithDebugInfo, String externalCompiler, String javaVersionForCompilation) throws CompilingException, IOException {
    if ( internalCompiler ) {
      compileInternal(file, contextWorkDir, vClassPath, compileWithDebugInfo, javaVersionForCompilation);
    } else { //for iternalCompiler
      compileExternal(file, contextWorkDir, vClassPath, externalCompiler, compileWithDebugInfo, javaVersionForCompilation);
    } //else for iternalCompiler
  }//compile


  
  /**
   * using almost singleton pattern, this method is responsible for the creation of the particular type of the compilers
   * we have 2 types of compilers: 
   * - internal - e.g. com.sun.tools.javac.Main
   * - external - JDK\bin\javac.exe i.e. compilation is performed as external process
   * 
   * @param compilerName - Only for external compilers. We recognize "javac" and "jikes". Any other compiler will use properties that "javac" would.
   * If NULL internal compiler will be used.    
   * @return
   * @throws IOException
   * @throws ServiceNotLoadedException - that indicates that classpath_resolver service is unavailable(or compilation_lib is missing)
   */
  private Compiler getCompiler(String compilerName) throws CompilingException {
    Compiler compiler;
    try {      
      if (compilerName != null) {
	      Properties props = new Properties();
	      props.put(CompilationFactory.COMPILER_EXECUTABLE, compilerName);
	      //do not use method CompilerFactory.getCompiler(String); - it will make IOExceptions if the String parameter is the same.
        compiler = CompilationFactory.getCompiler(props);
      } else {
        Properties props = new Properties();
        props.put(CompilationFactory.COMPILATION_METHOD, CompilationFactory.INTERNAL);
        // get the compiler with the properties
        compiler = CompilationFactory.getCompiler(props);
      }
    } catch (NoClassDefFoundError e) {
      throw new CompilingException(CompilingException.COMPILER_NOT_AVAILABLE);
    } catch (Exception e) {
      throw new CompilingException(CompilingException.ERROR_IN_EXECUTING_THE_PROCESS_OF_COMPILATION, new Object[] { e.getLocalizedMessage() }, e);
    }
    return compiler;
  }
  
 /* TODO: REMOVE after 30.VIII.2006 - deprecated faced is removed and old portal JSP parser is not used
  * portal integration compatibility **/
  
  public void compile(String file, String contextWorkDir, ArrayObject vClassPath,  
      boolean internalCompiler, boolean compileWithDebugInfo, String externalCompiler) throws CompilingException, IOException {
  }
}

