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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspConfigurationProperties;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspPageInterface;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParser;
import com.sap.engine.services.servlets_jsp.jspparser_api.ParserParameters;
import com.sap.engine.services.servlets_jsp.jspparser_api.TagCompilerParams;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.GenerateJavaFile;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.ParserImpl;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.syntax.Element;
import com.sap.engine.services.servlets_jsp.lib.eclipse.Smap;
import com.sap.engine.services.servlets_jsp.lib.eclipse.SmapInstaller;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.ServletNotFoundException;
import com.sap.engine.services.servlets_jsp.server.jsp.exceptions.CompilingException;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;

/**
 * Generates java file and compiles it from a given jsp file. Parses the source of the jsp
 * and interprets its tags as valid java expressions. The generated java file is the servlet
 * realizing functionality of the jsp.
 */
public class JSPProcessor {

  public static final String PARSER_NAME="JEE";

  private static Location currentLocation = Location.getLocation(JSPProcessor.class);
  private static Location traceLocation = LogContext.getLocationRequestInfoServer();
	/**
	 * Context of the application that this JSP resides.
	 */
	private ApplicationContext applicationContext;

	/**
	 * The same as the 	private File jspFile; but path separators are well escaped. 
	 */
	private String jspFullPath;
	
	/**
	 * Full path to the JSP page.
	 */
	private File jspFile;
	/**
	 * The name of the class.
	 */
	private String className;

	/**
	 * The content of unwrappedRequest.getHttpParameters().getRequestPathMappings().getAliasValue().toString()
	 */
	private String httpAliasValue;
	/**
	 * The content of unwrappedRequest.getHttpParameters().getRequestPathMappings().getAliasName().toString()
	 */	
	private String httpAliasName;
	
	/**
	 * Custom map used from the portal parser;
	 */
	private ConcurrentHashMapObjectObject portalProperties;
	/**
	 * Parser instance that holds properties specific for this parser type instance. 
	 */
	private JspParser jspParser;

  /**
   * Initiates the instance with references to ServletContext for this application and context in the
   * naming to bind parsed jsps there.
   *
   * @param   applicationContext  a references to ServletContext
   */
  public JSPProcessor(ApplicationContext applicationContext, JspParser jspParser, ConcurrentHashMapObjectObject portalProperties, String httpAliasName, String httpAliasValue ) {
    this.applicationContext = applicationContext;
	this.portalProperties = portalProperties;
	this.jspParser = jspParser;
	this.httpAliasName = httpAliasName;
	this.httpAliasValue = httpAliasValue;
  }



	/**
	 * Parse given JSP file and if needed compile it.
	 *
	 * @param       jspFile          file to be parsed
	 * @param       needCompiling    if true , must compile
	 * @return a name of the generated class file
	 * @exception   JspParseException
	 */
	public String parse(String jspFile, boolean needCompiling) throws JspParseException{
		String newClass = null;
		if (needCompiling) {
			try {
				newClass = generateJavaFile(
						new File(jspFile).getCanonicalPath().replace(File.separatorChar, ParseUtils.separatorChar));
			} catch (OutOfMemoryError e) {
				throw e;
			} catch (ThreadDeath e) {
				throw e;
			} catch (JspParseException jspParseException) {
			  applicationContext.getClassNamesHashtable(getCurrentParserName()).remove(jspFile);
			  throw jspParseException;
      } catch (Throwable e) {
				applicationContext.getClassNamesHashtable(getCurrentParserName()).remove(jspFile);
				throw new JspParseException(JspParseException.RETHROW_EXCEPTION, new Object[]{ e.getMessage()}, e);
			}
		} else {
			//TODO check
			newClass = (String) applicationContext.getClassNamesHashtable(getCurrentParserName()).get(jspFile);
			if (newClass == null) {
				throw new JspParseException(JspParseException.IMPLICIT_PARSING_ERROR);
			}
			if (traceLocation.beInfo()) {
				traceLocation.infoT("The class for JSP file <" + jspFile + "> found. It will not be recompiled!\r\n" + "The servlet class is: " + newClass);
			}
		}
		return newClass;
	}
	
	/**
	 * Puts the classfile for this jsp file. If there was already class for this JSP , the java and class files are deleted. 
	 * @param jspFile
	 * @param newClass
	 * @param _javaFile
	 */
  private void putJSPinClassNamesHashtable(String jspFile, String newClass, String _javaFile) {
    String oldJSP = (String) applicationContext.getClassNamesHashtable(getCurrentParserName()).get(jspFile);
    applicationContext.getClassNamesHashtable(getCurrentParserName()).put(jspFile, newClass);
    if (oldJSP == null) {
      return;
    }
    String path = _javaFile.substring(0, _javaFile.lastIndexOf(ParseUtils.separatorChar) + 1);
		String oldJSPName = oldJSP.substring(oldJSP.lastIndexOf('.')+1);
    try {
      new File(path + oldJSPName + ".java").delete();
      new File(path + oldJSPName + ".class").delete();
    } catch (SecurityException _) {
    	//TODO:Polly type:ok content - possible problems during update of an application 
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation, "ASJ.web.000116",
        "Error deleting the old java and class files generated for the jsp file {0}.", new Object[]{jspFile}, _, null, null);
    }
  }
  
  /**
   * The JSP file is parsed and compiled. 
   * The classname is registered for this JSP file.
   * @param jspFile
   * @param unwrappedRequest
   * @return
   * @throws ServletNotFoundException
   * @throws IOException
   * @throws JspParseException
   */
  private String generateJavaFile(String jspFile)
      throws JspParseException {
    String className = null;
    if (traceLocation.beInfo()) {
      traceLocation.infoT("The JSP file <" + jspFile + "> is about to be parsed.");
    }
    File file = new File(jspFile);
    try {
      className = parse(file, portalProperties, applicationContext.getAliasName());
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    }catch (JspParseException jspParseException) {
      throw jspParseException;
    }catch (NoClassDefFoundError e) {
      if (traceLocation.beError()) {
				LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceError( "ASJ.web.000428",
						"Compilation library not loaded.", e, null, null);
			}
      throw new JspParseException(JspParseException.COMPILATION_LIBRARY_NOT_FOUND);
    } catch (Throwable e) {
      throw new JspParseException(JspParseException.RETHROW_EXCEPTION, new Object[] { e.getMessage() }, e);
    }
    String javaFileName = applicationContext.getCompiledFileDir(className) + className + ".java";
    putJSPinClassNamesHashtable(jspFile, className, javaFileName);

    if (traceLocation.beInfo()) {
      traceLocation.infoT("The generated Java file for the JSP file [" + jspFile + "] is successfully compiled.\r\n");
    }
    return className;
  }
  
  /**
   * Parses and compiles the file.
   * @param file - File object with absolute path to the JSP that should be parsed.
   * @param portalProperties - for engine parser this is null
   * @param applicationAlias - the name of the application
   * @return className - the name of the compiled classname
   * @throws JspParseException
   */
	private String parse( File file,
			ConcurrentHashMapObjectObject portalProperties, 
			String applicationAlias) throws JspParseException {
    this.jspFile = file;
    String javaFileName = null;

    ParserParameters parserParameters = new ParserParameters();
    parserParameters.setPortalProperties(portalProperties);
    try {
      this.jspFullPath = file.getCanonicalPath().replace(File.separatorChar, ParseUtils.separatorChar);
      javaFileName = createFileName(jspFullPath);
      // Java/Class files will begin with parser name. This is necessary for cases where portal parser and engine parser are processing one JSP page.
      className = getClassName(javaFileName);
      prepareParserParameters(parserParameters);
    } catch (IOException e1) {
      throw new JspParseException(JspParseException.IO_ERROR_IN_FILE, new Object[] { file.getAbsolutePath() }, e1);
    }
    JspPageInterface jspPageInterface = new ParserImpl(file, className, parserParameters, jspParser.getContainerParameters());

    if (jspParser.getTagHandlers() != null) {
      for (int i = 0; i < jspParser.getTagHandlers().length; i++) {
        jspPageInterface.registerParsingElement((Element) jspParser.getTagHandlers()[i]);
      }
    }
    // generate the java file in a memory stream to avoid
    // leaving a zero-sized file if an exception occurs
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    PrintWriter out = null;
    String javaFileEncoding = jspParser.getContainerParameters().getJavaEncoding();
    
    try {// close the streams
      try {
        out = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, javaFileEncoding));
      } catch (UnsupportedEncodingException e) {
        throw new JspParseException(JspParseException.PARSER_CANNOT_GENERATE_OUTPUT_WITH_ENCODING, e);
      }
      GenerateJavaFile generateJavaFile = new GenerateJavaFile(out);
      generateJavaFile.generateJavaFile(jspParser.getParsable(), jspPageInterface);

      try {
        compile(byteArrayOutputStream, javaFileName);
      } catch (IOException e) {
        throw new JspParseException(JspParseException.RETHROW_EXCEPTION, new Object[] { e.getMessage() }, e);
      }
        if (ServiceContext.getServiceContext().getWebContainerProperties().jspDebugSupport()) {
          if( LogContext.getLocationJspParser().beDebug() ){
            LogContext.getLocationJspParser().debugT("Adding SourceDebugExtension attribute to the generated class ["+javaFileName+"].");
          }
          try {
            String classFileName = javaFileName.substring(0, javaFileName.length() - 4) + "class";
            Smap smap = new Smap(classFileName, new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), javaFileEncoding);
            String smapTable = smap.generateSmap();
            SmapInstaller smapInstaller = new SmapInstaller(classFileName);
            smapInstaller.injectSmap(smapTable);          
          } catch (Exception e) {
        	  //TODO:Polly type:trace
        	  LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation,"ASJ.web.000117", 
        	    "Problem during instrumenting class file with debug info for JSR-045: {0}", 
        	    new Object[]{e.getMessage()}, e, null, null);
          }
        }
    } finally {
      out.flush();
      out.close();
    }
    return className;
  }
	
	/**
   * Creates the java file on the file system and compiles the class file. 
   * @param out - the array stream that contains the java file content.
   * @param javaFileName - the full path to the java file.
   * @throws IOException - when java file cannot be created on the file system.
   * @throws JspParseException - wraps the CompilingException
   */
	private void compile(ByteArrayOutputStream out, String javaFileName) throws  IOException, JspParseException{
    // write the generated source to a physical file
    FileOutputStream fileO = new FileOutputStream(javaFileName);
    try {
      fileO.write(out.toByteArray());
    } finally {
      fileO.close();
    }
    JavaCompiler compiler = new JavaCompiler();
    ThreadWrapper.pushSubtask("Compiling JSP '" + className + "'", ThreadWrapper.TS_PROCESSING);
      try {
        ServiceContext serviceContext = ServiceContext.getServiceContext();
        compiler.compile(javaFileName, applicationContext.getWorkingDir(), applicationContext.getJarClassPathHashtable(),
          serviceContext.getWebContainerProperties().internalCompiler(),
		      serviceContext.getWebContainerProperties().jspDebugSupport(),
		      serviceContext.getWebContainerProperties().getExternalCompiler(),
          applicationContext.getJavaVersionForCompilation());
      }catch (CompilingException e) {
        if (traceLocation.beInfo()) {
          traceLocation.infoT("Error in compiling of the JSP file <" + jspFile + "> !\r\n" + "The Java file is: " + javaFileName);
        }
        throw new JspParseException(JspParseException.RETHROW_EXCEPTION, new Object[]{e.getMessage()}, e);
      }finally{
        ThreadWrapper.popSubtask();
      }
	}
	
  /**
   * Creates an unique file name for the generated java file.
   * Format is: parserName_jspFileExtension_serverId_jspLastModifiedTime_currentTime.java
   * @param   jspFile fileName
   * @return Full path to the generated java file.
   */
	private String createFileName(String jspFile) throws IOException {
		int serverId = ServiceContext.getServiceContext().getServerId();
    long jspFileModificationDate = (new File(jspFile)).lastModified();

    //check for alias
    jspFile = composeFileNameWithAlias(jspFile);

    String className = jspFile.substring(applicationContext.getWebApplicationRootDir().length(), jspFile.lastIndexOf("."));
    String extention = jspFile.substring(jspFile.lastIndexOf(".") + 1) + "_";
		className = applicationContext.getValidClassNamePath(className);
    className = className.substring(0, className.lastIndexOf(ParseUtils.separator) + 1) +
      					extention + className.substring(className.lastIndexOf(ParseUtils.separator) + 1);
    String compiledFileDir = applicationContext.getCompiledFileDir(className);
    className = className.replace(ParseUtils.separatorChar,'.');
    File compiledDir = new File(compiledFileDir);
    if (!compiledDir.exists()){
      compiledDir.getCanonicalFile().mkdirs();
    }
    //TODO: Remove milliseconds. see also Application applicationContext.findCompiledJspClassName.
		return compiledFileDir + jspParser.getParserName() + "_" + className.substring(className.lastIndexOf('.') + 1) +
		  "_" + serverId + "_" + jspFileModificationDate + "_" +
      System.currentTimeMillis() + ".java";
  }
	
	/**
	 * Creates TagCompilerParams if null and fills all the necessary fields.
	 * @param parserParameters
	 * @throws IOException
	 */
	private void prepareParserParameters(ParserParameters parserParameters) throws IOException{

	  TagCompilerParams compilerParams = parserParameters.getTagCompilerParams();
	  if( compilerParams == null ){
	    compilerParams = TagCompilerParams.createInstance(applicationContext);
	    parserParameters.setTagCompilerParams(compilerParams);
	  }
	  //update parser params
	  //applicationContext.getClassLoader() can be used only for the engine parsing. portal uses different classloader than the application classloader.
    // It is the private classloader of the application if exists.
    // If no private classloader is available, then application classloader is the public one.  
    ClassLoader  appClassLoader = applicationContext.getPrivateClassloader();
    parserParameters.setAppClassLoader(appClassLoader );
	  if( parserParameters.getApplicationRootDir() == null ){
	    parserParameters.setApplicationRootDir(applicationContext.getWebApplicationRootDir());
	  }
    parserParameters.setIncludedFilesHashtable(applicationContext.getIncludedFilesHashtable(getCurrentParserName()));
    
    String absolutePath = jspFile.getAbsolutePath().replace(File.separatorChar, ParseUtils.separatorChar);
    parserParameters.setJspConfigurationProperties(prepareConfigurationProperties(absolutePath));
    parserParameters.setJspConfiguration(applicationContext.getJspConfiguration());
	  parserParameters.setTagLibraryDescriptors(applicationContext.getWebComponents().getTagLibDescriptors());
	  parserParameters.setTagLibraryValidators(applicationContext.getWebComponents().getTagLibraryValidators());
    parserParameters.setJspIdGenerator(applicationContext.getJspIdGenerator());
    parserParameters.setHttpAliasValue(httpAliasValue);
	}
	
	/**
	 * Gets the Jsp configuration for the given request.
	 * @param filePath
	 * @param unwrappedRequest - if not null this is the unwrapped with FilterUtils, http request
	 * @return
	 * @throws IOException
	 */
  private JspConfigurationProperties prepareConfigurationProperties(String filePath) throws IOException {
		filePath = composeFileNameWithAlias(filePath);
		String currentFileName = filePath.replace(File.separatorChar, ParseUtils.separatorChar);
    String uri = currentFileName.substring(applicationContext.getWebApplicationRootDir().length() - 1);
    return applicationContext.getJspConfiguration().getJspProperty(uri);
  }

  /**
   * Checks if this file contains alias. If contains then fake jspFile is constructed -
   * i.e. the jsp file is situated in the root directory of the default application("examples").
   * @param jspFile - canonical path to JSP file
   * @param unwrappedRequest
   * @return
   */
  private String composeFileNameWithAlias(String jspFile){
    if( httpAliasName != null && httpAliasValue != null ){
			if (applicationContext.isDefault() && httpAliasValue != null){
				int ind = jspFile.indexOf(httpAliasValue);
				if (ind != -1){
					jspFile = applicationContext.getWebApplicationRootDir() + httpAliasName + ParseUtils.separator + jspFile.substring(ind + httpAliasValue.length() +1);
				}
			}
    }
		return jspFile;
  }
  
	private String getClassName(String javaFileName){
	  String result = javaFileName.substring(applicationContext.getWorkingDir().length(), javaFileName.lastIndexOf("."));
	  result = result.replace(ParseUtils.separatorChar, '.');
	  return result;
	}
	
	/**
	 * Returns the name of the parser instance that triggers this JSP processing.
	 * @return
	 */
	public String getCurrentParserName(){
	  return jspParser.getParserName();
	}
}

