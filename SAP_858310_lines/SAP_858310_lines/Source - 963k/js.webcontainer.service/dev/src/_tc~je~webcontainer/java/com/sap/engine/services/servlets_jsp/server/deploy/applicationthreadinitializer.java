/*
 * Copyright (c) 2002-2009 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */

package com.sap.engine.services.servlets_jsp.server.deploy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagLibraryValidator;

import com.sap.engine.frame.core.load.ClassInfo;
import com.sap.engine.frame.core.load.ClassLoaderResource;
import com.sap.engine.frame.core.load.ClassWithLoaderInfo;
import com.sap.engine.frame.core.load.LoadContext;
import com.sap.engine.interfaces.resourcecontext.ResourceContext;
import com.sap.engine.lib.descriptors5.javaee.ListenerType;
import com.sap.engine.lib.descriptors5.javaee.ParamValueType;
import com.sap.engine.lib.descriptors5.web.FilterType;
import com.sap.engine.lib.descriptors5.web.TaglibType;
import com.sap.engine.lib.util.HashMapObjectObject;
import com.sap.engine.services.accounting.Accounting;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspConfigurationProperties;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParser;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParserFactory;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParserFactoryImpl;
import com.sap.engine.services.servlets_jsp.jspparser_api.ParserParameters;
import com.sap.engine.services.servlets_jsp.jspparser_api.TagCompilerParams;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParserInitializationException;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.GenerateJavaFile;
import com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.tagfiles.TagFileParser;
import com.sap.engine.services.servlets_jsp.lib.jspruntime.JspApplicationContextImpl;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.application.WebApplicationConfig;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.JEEImplicitTlds;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagLibDescriptor;
import com.sap.engine.services.servlets_jsp.server.exceptions.ServletNotFoundException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebResourceAccessor;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebWarningException;
import com.sap.engine.services.servlets_jsp.server.jsp.JSPCompiler;
import com.sap.engine.services.servlets_jsp.server.jsp.JSPProcessor;
import com.sap.engine.system.ThreadWrapper;
import com.sap.localization.LocalizableTextFormatter;
import com.sap.tc.logging.Location;

/*
 * Initial all web application components in application thread
 *
 * @author
 * @version 6.30
 */

public class ApplicationThreadInitializer implements Runnable {
  private static Location currentLocation = Location.getLocation(ApplicationThreadInitializer.class);
  private static Location traceLocation = LogContext.getLocationRequestInfoServer();
  private WebApplicationConfig webAppConfig = null;
  private ClassLoader threadLoader = null;
  private ApplicationContext applicationContext = null;
  private Vector warnings = new Vector();
  private String allServlets[][] = null;
  private Thread currentThread = null;
  private Throwable exceptionDuringInit = null;
  private TagCompilerParams compilerParams = null;
  private ParserParameters parserParameters = null;
  

  public ApplicationThreadInitializer(WebApplicationConfig webApplicationConfig, ClassLoader thLoader,
                                      ApplicationContext applicationContext, String[][] allServlets) {
    this.webAppConfig = webApplicationConfig;
    threadLoader = thLoader; //??????
    this.applicationContext = applicationContext;
    this.allServlets = allServlets;
  }

  public Vector getWarnings() {
    return warnings;
  }

  public Throwable getException() {
    return exceptionDuringInit;
  }

  /**
   * When an object implementing interface <code>Runnable</code> is used
   * to create a thread, starting the thread causes the object's
   * <code>run</code> method to be called in that separately executing
   * thread.
   * <p/>
   * The general contract of the method <code>run</code> is that it may
   * take any action whatsoever.
   *
   * @see Thread#run()
   */
  public void run() {
    ResourceContext resourceContext = null;
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure("ApplicationThreadInitializer", ApplicationThreadInitializer.class);
      }//ACCOUNTING.start - END

      ThreadWrapper.pushTask("Initializing web components of web application [" + 
          ((applicationContext != null) ? applicationContext.getAliasName() : "") + "] of application [" + 
          ((applicationContext != null) ? applicationContext.getApplicationName() : "") + "] in WebContainer.", ThreadWrapper.TS_PROCESSING);
      
      try {
      	ThreadWrapper.pushSubtask("Initializing application context of web application [" + 
    				((applicationContext != null) ? applicationContext.getAliasName() : "") + "] in WebContainer.", ThreadWrapper.TS_PROCESSING);
    				
      	resourceContext = applicationContext.enterResourceContext();
      	currentThread = Thread.currentThread();
      	threadLoader = currentThread.getContextClassLoader();
      	currentThread.setContextClassLoader(applicationContext.getClassLoader());
      } finally {
      	ThreadWrapper.popSubtask();
      }
      
      HashMapObjectObject listenersFromTagLib = parseTagLibListeners();
      
      //adding tlds from jar-file
      processTldJars(listenersFromTagLib);

      //adding tlds from .tld file under WEB-INF and its subdirectories
      processTlds(listenersFromTagLib, currentThread, threadLoader);

      //adding tlds from engine libraries
      final String accountingTag0 = "AppThreadInitializer(" + applicationContext.getAliasName() + ")/ProcessLibraryReferences";
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag0, ApplicationThreadInitializer.class);
      }//ACCOUNTING.start - END
      
    /* in this case the references are transitive
     */
      LoadContext ld_context = ServiceContext.getServiceContext().getLoadContext();
      List<String> ref_string = new ArrayList<String>();
      ClassInfo info = null;
      ClassLoader[] parents = ld_context.getClassLoaderParents(applicationContext.getClassLoader());
      for (ClassLoader loader : parents) {
            info = ld_context.getLoaderComponentInfo(loader);
            if( (info != null)
                  && (info instanceof ClassWithLoaderInfo)
                  && ((ClassWithLoaderInfo)info).getComponentType() == LoadContext.COMP_TYPE_LIBRARY) {
                  // this parent is a library loader
            	  // && check if its lsf
            	  String l_name = info.getLoaderName();
            	  //if (l_name.equals("library:tc~ui~faces")){
            		  ref_string.add(l_name);
            	//	  break;
            	 // }
            }
      }

      if (!ref_string.isEmpty()){
    		try {
    		  List<ClassLoaderResource> resources = ld_context.getResources(ref_string,"tld");
    		  if (resources != null && !resources.isEmpty()) {
    			  for (ClassLoaderResource res: resources){
    				String name = res.getResourceEntryPath();
    					if(isValid(name)){
    					  InputStream is = res.getInputStream();
       				  processTld(name, is, null, listenersFromTagLib);
    	  				  if (LogContext.getLocation(LogContext.LOCATION_DEPLOY).getLocation().beDebug()){
    	  					  LogContext.getLocation(LogContext.LOCATION_DEPLOY).traceDebug("["+name+"] is added", applicationContext.getApplicationName());
    	  				  }	  
    				  }
    			  }
    		  }
    		} catch(Exception e) {
    	    	LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation,"ASJ.web.000603", 
    	    	  "Error in obtaining tld resources.", e, null, null);
    		}
  	  }
      
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag0);
      }//ACCOUNTING.end - END

      // parsing j2ee engine libraries for tlds
      // mind that implicit TLDs have higher precedence that the application TLDs
      // They should be parsed after application's and overwrite them if duplicate URLs are used. 
      processImplicitTLDs(listenersFromTagLib, true);     

      //SRV.9.12
      //Instants an instance of each event listener identified by a <listener> element in the deployment descriptor.
      //For instanted listener instances that implement ServletContextListener, call the contextInitialized() method.      
      loadListeners();
      
      loadTagLibListeners(listenersFromTagLib);
      
      final String accountingTag1 = "AppThreadInitializer(" + applicationContext.getAliasName() + ")/contextInitialized";
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag1, ApplicationThreadInitializer.class);
      }//ACCOUNTING.start - END
      
      if (applicationContext.getWebComponents().getELContextListeners() != null) {
        ((JspApplicationContextImpl)JspFactory.getDefaultFactory().getJspApplicationContext(applicationContext.getServletContext())).setELContextListeners(applicationContext.getWebComponents().getELContextListeners());
      }
      applicationContext.getWebEvents().contextInitialized(applicationContext);
      
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag1);
      }//ACCOUNTING.end - END
      
      //Instants an instance of each filter identified by a <filter> element in the deployment descriptor and
      //call each filter instance2"s init() method.      
      loadFilters();
      
      //Instants an instance of each servlet identified by a <servlet> element that includes a <load-on-startup> element
      //in the order defined by the load-on-startup element values, and call each servlet instance2"s init() method.   
      loadServlets();
      
      compileJspFiles();
      
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
        exceptionDuringInit = new WebDeploymentException(WebDeploymentException.ERROR_IN_STARTING_APPLICATION,
        new Object[]{applicationContext.getAliasName()}, e);
    } finally {
      try {
        currentThread.setContextClassLoader(threadLoader);
        applicationContext.exitResourceContext(resourceContext);
      } catch (Exception e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000026",
          "Error in finalizing resources after service stop.", e, null, null);
      } finally {
        ThreadWrapper.popTask();
        
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.endMeasure("ApplicationThreadInitializer");
        }//ACCOUNTING.end - END
      }
    }
  }
  

/**
   * Merges implicit TLDs and their listeners and validators into the application. 
   * @param listenersFromTagLib
   * @param parsed
   */
  private void processImplicitTLDs(HashMapObjectObject listenersFromTagLib,  boolean parsed) {
    String accountingTag = "AppThreadInitializer(" + applicationContext.getAliasName() + ")/processImplicitTLDs";
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag, ApplicationThreadInitializer.class);
      }//ACCOUNTING.start - END
      
      ThreadWrapper.pushSubtask("Processing implicit TLDs of web application [" + ((applicationContext != null) ? applicationContext.getAliasName() : "") + "].", ThreadWrapper.TS_PROCESSING);

      //Since JEE5 JSTL and JSF tlds are provided by the engine.
      JEEImplicitTlds implicit = ServiceContext.getServiceContext().getJspContext().getJeeImplicitTlds();
      boolean isJSFApplication = isJSFApplication();
      TagLibDescriptor[] implicit_descriptors = implicit.getJEEImplicitTlds(isJSFApplication);
      
      for (int i = 0; i < implicit_descriptors.length; i++){
        TagLibDescriptor tld_desc = implicit_descriptors[i];
        if (tld_desc.getURI() != null) {
          ListenerType[] listenersTL = tld_desc.getTagLibrary().getListener();
          if (listenersTL != null) {
            for (int j = 0; j < listenersTL.length; j++) {
              if (listenersTL[j].getListenerClass() != null) {
                listenersFromTagLib.put(listenersTL[j].getListenerClass().get_value(), listenersTL[j].getListenerClass().get_value());
              }
            }
          }
     
          if (parsed) {
            applicationContext.getWebComponents().addTagLibraryInfo(tld_desc.getURI(), tld_desc);
          }
        }
      }
      
      HashMap<String, TagLibraryValidator> validators = implicit.getTLDValidators(isJSFApplication);
      if( validators != null ) {
        // ConcurentHashMapObjectObject do not have putAll() method
        Iterator<String> keysIterator = validators.keySet().iterator();
        while (keysIterator.hasNext()) {
          String uri = keysIterator.next();
          TagLibraryValidator validator = validators.get(uri);
          applicationContext.getWebComponents().addTagLibraryValidator(uri, validator);  
        }
        
      }
    } finally {
      ThreadWrapper.popSubtask();
      
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag);
      }//ACCOUNTING.end - END
    }
  }

  /**
   * Process all TLD files under WEB-INF and its subdirectories
   *
   * @param listenersFromTagLib
   * @param currentThread
   * @param threadLoader
   */
  private void processTlds(HashMapObjectObject listenersFromTagLib, Thread currentThread, ClassLoader threadLoader) {
    String accountingTag = "AppThreadInitializer(" + applicationContext.getAliasName() + ")/processTlds";
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag, ApplicationThreadInitializer.class);
      }//ACCOUNTING.start - END
      
      ThreadWrapper.pushSubtask("Processing TLDs of web application [" + ((applicationContext != null) ? applicationContext.getAliasName() : "") + "] under WEB-INF and subdirectories.", ThreadWrapper.TS_PROCESSING);

      if (traceLocation.beInfo()) {
      	traceLocation.infoT("Searching for TLD files in WEB-INF directory.");
      }
      File webinf = new File(applicationContext.getWebApplicationRootDir() + "WEB-INF");
      if (webinf.exists() && webinf.isDirectory()) {
        processTldsInDir(webinf, listenersFromTagLib, currentThread, threadLoader);
      }
    } finally {
      ThreadWrapper.popSubtask();
      
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag);
      }//ACCOUNTING.end - END
    }
  }

  /**
   * Process all TLD files in the directory and its subdirectories.
   * dir is guarantees to be a directory, not a file
   *
   * @param dir
   * @param listenersFromTagLib
   * @param currentThread
   * @param threadLoader
   */
  private void processTldsInDir(File dir, HashMapObjectObject listenersFromTagLib, Thread currentThread, ClassLoader threadLoader) {
    File[] list = dir.listFiles();
    //Check the list for null nevertheless that we check previously that the File is a directory.
    //This method File.listFiles() returns null in two cases:
    //- this abstract pathname does not denote a directory (this is not our case)
    //- an I/O error occurs
    //For more details see CSN message 5557486 2007 (A1S SP7: portal UI not available after update)
    if (list == null) {
      warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.TOO_FEW_FILE_DESCRIPTORS, new Object[]{"*.tld", dir.getName(), applicationContext.getAliasName()}));
    	return;
    }
    for (int i = 0; i < list.length; i++) {
      try {
        if (list[i].isDirectory()) {
          if (traceLocation.beInfo()) {
          	traceLocation.infoT("Searching directory [" + list[i].getAbsolutePath() + "].");
          } 
          if( list[i].getPath().endsWith(File.separator+"classes") || list[i].getPath().endsWith(File.separator+"lib") || list[i].getPath().endsWith(File.separator+"tags")){
            // JSP.7.3.1        
            // TLD files should not be placed in /WEB-INF/classes, /WEB-INF/lib or /WEB-INF/tags 
            continue;
          }
          processTldsInDir(list[i], listenersFromTagLib, currentThread, threadLoader);
        } else if (list[i].getName().toLowerCase().endsWith(".tld")) {
          if (traceLocation.beInfo()) {
          	traceLocation.infoT("Found TLD file [" + list[i].getAbsolutePath() + "].");
          }
          FileInputStream fis = new FileInputStream(list[i]);
          try {
            processTld(list[i].getName(), fis, null, listenersFromTagLib);
          } finally {
            fis.close();
          }
        }
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000074",
          "Error parsing tag library descriptors from files.", e, null, null);
      }
    }
  }

  private void processTldJars(HashMapObjectObject listenersFromTagLib) {
    String jarLocation = applicationContext.getWebApplicationRootDir() + "WEB-INF" + File.separator + "lib";
    File lib = new File(jarLocation);
    if (lib.exists() && lib.isDirectory()) {
      String accountingTag = "AppThreadInitializer(" + applicationContext.getAliasName() + ")/processTldJars";
      try {
        if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
          Accounting.beginMeasure(accountingTag, ApplicationThreadInitializer.class);
        }//ACCOUNTING.start - END
        
        ThreadWrapper.pushSubtask("Processing TLDs of web application [" + applicationContext.getAliasName() + "] from jar-files under WEB-INF/lib.", ThreadWrapper.TS_PROCESSING);

        File[] list = lib.listFiles();
        //Check the list for null nevertheless that we check previously that the File is a directory.
        //This method File.listFiles() returns null in two cases:
        //- this abstract pathname does not denote a directory (this is not our case)
        //- an I/O error occurs
        //For more details see CSN message 5975922 2007 (BI fail to start due to NullPointerException in server)
        if (list == null) {
          warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
              WebWarningException.TOO_FEW_FILE_DESCRIPTORS, new Object[]{"*.tld", lib.getName(), applicationContext.getAliasName()}));
        	return;
        }
        for (int i = 0; i < list.length; i++) {
          if (list[i].getName().toLowerCase().endsWith(".jar")) {
            try {
              JarFile jarFile = new JarFile(list[i]);
              try {
                processTldJar(jarFile, listenersFromTagLib);
              } finally {
                jarFile.close();
              }
            } catch (OutOfMemoryError e) {
              throw e;
            } catch (ThreadDeath e) {
              throw e;
            } catch (Throwable e) {
              LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000075", 
                "Error parsing tag library descriptors from JAR files.", e, null, null);
            }
          }
        }//for
      } finally {
        ThreadWrapper.popSubtask();
        
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.endMeasure(accountingTag);
        }//ACCOUNTING.end - END
      }
    }
  }

  private void processTldJar(JarFile jarFile, HashMapObjectObject listenersFromTagLib) {
    Enumeration entries = jarFile.entries();
    try {
    while (entries.hasMoreElements()) { 
      JarEntry entry = (JarEntry) entries.nextElement();
      try {
        String name = entry.getName();
        if (!(name.startsWith("META-INF/") && name.endsWith(".tld"))) {
          continue;
        }
        processTld(name, jarFile.getInputStream(entry), jarFile, listenersFromTagLib);
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
    	  	LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000076",
          "Error in parsing [{0}] JAR file.", new Object[]{jarFile.getName()}, e, null, null);
      }
    }
    } finally {
    	try {
    		if (jarFile != null) {
    			jarFile.close();
    		}
    	} catch (IOException e) {
    		if (traceLocation.beWarning()) {
    			LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000511",
							"Cannot close [{0}].", new Object[]{jarFile.getName()}, e, null, null);
    		}
    	}
    }
  }

  /**
   * Process a single TLD from a InputStream
   *
   * @param tldName                The name of the TLD file, as found in the file system or the name of the jar file
   * @param input               The InputStream of the file data
   * @param listenersFromTagLib
   */
  private void processTld(String tldName, InputStream input, JarFile jarFile, HashMapObjectObject listenersFromTagLib) {
    String accountingTag = "AppThreadInitializer(" + applicationContext.getAliasName() + ")/processTld(" + tldName + ")";
    try { 
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag, ApplicationThreadInitializer.class);
      }//ACCOUNTING.start - END
      
      if (traceLocation.beInfo()) {
        if (jarFile == null) {
        	traceLocation.infoT("Processing [" + tldName + "] TLD file.");
        } else {
        	traceLocation.infoT("Processing [" + tldName + "] TLD file in the following [" + jarFile.getName() + "] JAR file.");
        }
      }
      TagLibDescriptor tagLib = new TagLibDescriptor(applicationContext.getClassLoader());
      try {
        tagLib.loadDescriptorFromStream(input);
        // if tagfiles are defined in the TLD with the <tag-file> directive
        for (int j = 0; j < tagLib.getTagFiles().length; j++) {
          TagFileInfo tagFile = tagLib.getTagFiles()[j];
          TagFileInfo parsedTagFileInfo;
          if (jarFile != null) {
            String entryName = tagFile.getPath().charAt(0) == '/' ? tagFile.getPath().substring(1) : tagFile.getPath();
            ZipEntry entry = jarFile.getEntry(entryName);
            if (entry == null) {
              LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000077",
                "Error in parsing tagfile [{0}:{1}]. The tagfile is missing in [{2}] JAR file.", 
                new Object[]{tldName, tagFile.getPath() , jarFile.getName()}, null, null);
              continue;
            }
            String jarName = jarFile.getName().replace(File.separatorChar, ParseUtils.separatorChar);
  
            //returns default properties
            JspConfigurationProperties jspConfigurationProperties = applicationContext.getJspConfiguration().getJspProperty(entryName);
            TagFileParser tagFileParser = getTagFileParser(tagLib);
            tagFileParser.setTagFileTLD(tagLib);
            parsedTagFileInfo = tagFileParser.parseTagFile(jarName.substring(jarName.lastIndexOf("/WEB-INF/lib")), tagFile.getName(), tagFile.getPath(), jarFile.getInputStream(entry), entry.getTime(), jspConfigurationProperties, tagLib);
          } else {
            String filename = applicationContext.getWebApplicationRootDir() + tagFile.getPath();
            File file = new File(filename);
  
            //returns default properties
            JspConfigurationProperties jspConfigurationProperties = applicationContext.getJspConfiguration().getJspProperty(filename);
            TagFileParser tagFileParser = getTagFileParser(tagLib);
            tagFileParser.setTagFileTLD(tagLib);
            parsedTagFileInfo = tagFileParser.parseTagFile(filename, tagFile.getName(), tagFile.getPath(), file.lastModified(), jspConfigurationProperties, tagLib);
          }
  
          /*
          * JSP.8.4.3 Packaging Directly in a Web Application
          * If a directory contains two files with the same tag name (e.g. a.tag and a.tagx),
          * it is considered to be the same as having a TLD file with two <tag> elements
          * whose <name> sub-elements are identical. The tag library is therefore considered
          * invalid.
          */
          for (int i = 0; i < j; i++) {
            TagFileInfo oneTagFile = tagLib.getTagFiles()[i];
            if (oneTagFile.getName().equals(parsedTagFileInfo.getName())) {
              throw new JspParseException(JspParseException.TAG_FILE_HAS_DUPLICATE_NAME, new Object[]{tagFile.getPath(), tagLib.getURI()});
            }
          }
          tagLib.setTagFileInfo(j, parsedTagFileInfo);
        }
  
  
        if (tagLib.getURI() != null) {
          /*
           * Check if a taglib with this URI already exists
           * Duplicates are not allowed
           * The precedence order is
           *   - web.xml
           *   - TLDs in Jar files
           *   - TLDs in WEB-INF
           *   - implicit map entries from the Container
           */
          if (applicationContext.getWebComponents().getTagLibraryInfo(tagLib.getURI()) != null) {
            if (traceLocation.beInfo()) {
            	traceLocation.infoT("A TLD with this URI [" + tagLib.getURI() + "] already exists, skipping ...");
            }
            return;
          }
          TagLibraryValidator tlv = GenerateJavaFile.instantiateValidator(applicationContext.getClassLoader(), tagLib);
          if( tlv != null ) {
            applicationContext.getWebComponents().addTagLibraryValidator(tagLib.getURI(), tlv);  
          }        
          applicationContext.getWebComponents().addTagLibraryInfo(tagLib.getURI(), tagLib);
        }
        
  
        //listeners should be added even there is no URI declared in the taglib
        //JSP2.1 - 7.1.9A Container is required to locate all TLD files (see Section JSP.7.3.1 for details on how they are
        //identified), read their listener elements, and treat the event listeners as extensions of
        //those listed in web.xml.
        ListenerType[] listenersTL = tagLib.getTagLibrary().getListener();
        if (listenersTL != null) {
          for (int j = 0; j < listenersTL.length; j++) {
            if (listenersTL[j].getListenerClass() != null) {
              listenersFromTagLib.put(listenersTL[j].getListenerClass().get_value(), listenersTL[j].getListenerClass().get_value());
            }
          }
        }
      } catch (OutOfMemoryError e) {
        throw e;
      } catch (ThreadDeath e) {
        throw e;
      } catch (Throwable e) {
        if (tagLib != null && tagLib.getURI() != null) {
          applicationContext.getWebComponents().addTagLibraryException(tagLib.getURI(), e);
        }
        if (jarFile == null) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000078",
            "Error in parsing [{0}] TLD file.", new Object[]{tldName}, e, null, null);
        } else {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000079",
            "Error in parsing [{0}] TLD file in the following [{1}] JAR file.", new Object[]{tldName, jarFile.getName()}, e, null, null);
        }
      }
    } finally {
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag);
      }//ACCOUNTING.end - END
    }
  }

  private HashMapObjectObject getFilterInitParameters(ParamValueType[] params) {
    HashMapObjectObject initParams = new HashMapObjectObject();
    if (params != null) {
      for (int i = 0; i < params.length; i++) {
        initParams.put(params[i].getParamName().get_value(), params[i].getParamValue().get_value());
      }
    }
    return initParams;
  }

  private HashMapObjectObject parseTagLibListeners() {
    String accountingTag = "AppThreadInitializer(" + applicationContext.getAliasName() + ")/parseTagLibListeners";
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag, ApplicationThreadInitializer.class);
      }//ACCOUNTING.start - END
    
      boolean parsed = true;
      HashMapObjectObject tagLibListeners = new HashMapObjectObject();
      TaglibType[] tagLib = webAppConfig.getTagLibs();
      if (tagLib == null) {
        return tagLibListeners;
      }
      for (int i = 0; i < tagLib.length; i++) {
        TagLibDescriptor tagLibDesc = new TagLibDescriptor(applicationContext.getClassLoader());
        parsed = true;
        try {
          if (tagLib[i].getTaglibLocation() != null) {
            // JSP.7.3.2 TLD resource path
            // The TLD resource path is interpreted relative to the root of the web
            // application and should resolve to a TLD file directly, or to a JAR file that has a
            // TLD file at location META-INF/taglib.tld. If the TLD resource path is not one of
            // these two cases, a fatal translation error will occur.
            if ((tagLib[i].getTaglibLocation().get_value().startsWith("/WEB-INF/")) && (tagLib[i].getTaglibLocation().get_value().toUpperCase().endsWith(".JAR"))) {
              String jarName = applicationContext.getWebApplicationRootDir() + tagLib[i].getTaglibLocation().get_value();
              ZipFile zipFile = new ZipFile(jarName);
              try {
                ZipEntry zipEntry = zipFile.getEntry("META-INF/taglib.tld");
                InputStream inZip = zipFile.getInputStream(zipEntry);
                tagLibDesc.loadDescriptorFromStream(inZip);
                for (int j = 0; j < tagLibDesc.getTagFiles().length; j++) {
                  TagFileInfo tagFile = tagLibDesc.getTagFiles()[j];
  
                  String entryName = tagFile.getPath().charAt(0) == '/' ? tagFile.getPath().substring(1) : tagFile.getPath();
                  ZipEntry entry = zipFile.getEntry(entryName);
                  if (entry == null) {
                    LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000080", 
                      "Error in parsing tagfile {0}:{1}. The tagfile is missing in the jar.", 
                      new Object[]{jarName, tagFile.getPath()}, null, null);
                  }
                  JspConfigurationProperties jspConfigurationProperties = applicationContext.getJspConfiguration().getJspProperty(entryName);
                  TagFileInfo tagFileInfo = getTagFileParser(tagLibDesc).parseTagFile(tagLib[i].getTaglibLocation().get_value(), tagFile.getName(), tagFile.getPath(), zipFile.getInputStream(entry), entry.getTime(), jspConfigurationProperties, tagLibDesc);
                  /*
                   * JSP.8.4.3 Packaging Directly in a Web Application
                   * If a directory contains two files with the same tag name (e.g. a.tag and a.tagx),
                   * it is considered to be the same as having a TLD file with two <tag> elements
                   * whose <name> sub-elements are identical. The tag library is therefore considered
                   * invalid.
                   */
                  for (int k = 0; j < k; k++) {
                    TagFileInfo alreadyAdded = tagLibDesc.getTagFiles()[k];
                    if (alreadyAdded.getName().equals(tagFile.getName())) {
                      throw new JspParseException(JspParseException.TAG_FILE_HAS_DUPLICATE_NAME, new Object[]{tagFile.getName(), tagFile.getPath()});
                    }
                  }
                  tagLibDesc.setTagFileInfo(j, tagFileInfo);
                }
              } finally {
                zipFile.close();
              }
            } else if (tagLib[i].getTaglibLocation().get_value().equals("/@@@META-INF/portlet.tld@@@")) {
              String tldFile = tagLib[i].getTaglibLocation().get_value();
              tldFile = tldFile.substring(4, tldFile.length() - 3); 
  
              String rootDir = applicationContext.getWebApplicationRootDir();
              String portletXml = rootDir + "WEB-INF" + File.separator + "portlet.xml";
              if ((new File(portletXml)).exists()) {
                parsed = loadTLDListenerFromClassLoaderRsource(tldFile, tagLibDesc);
              }
            } else if (tagLib[i].getTaglibLocation().get_value().equals("/@@@META-INF/portlet_2_0.tld@@@")) {
              String tldFile = tagLib[i].getTaglibLocation().get_value();
              tldFile = tldFile.substring(4, tldFile.length() - 3);
  
              String rootDir = applicationContext.getWebApplicationRootDir();
              String portletXml = rootDir + "WEB-INF" + File.separator + "portlet.xml";
              if ((new File(portletXml)).exists()) {
                parsed = loadTLDListenerFromClassLoaderRsource(tldFile, tagLibDesc);
              } 
            }
            /* else if (tagLib[i].getTaglibLocation().get_value().equals("/META-INF/jsf_core.tld")){
          	  //NOTE think of other solution
          	  LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logInfo(currentLocation,"Found JSF's jsf_core.tld",applicationContext.getAliasName());
          	  parsed = loadTLDListenerFromClassLoaderRsource(tagLib[i].getTaglibLocation().get_value(),tagLibDesc);
  
            }else if (tagLib[i].getTaglibLocation().get_value().equals("/META-INF/html_basic.tld")){
          	  LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logInfo(currentLocation,"Found JSF's hmtl_basic.tld",applicationContext.getAliasName());
          	  parsed = loadTLDListenerFromClassLoaderRsource(tagLib[i].getTaglibLocation().get_value(),tagLibDesc);
            }*/
            else {
  
              //Here the path should point to .tld directly (not directory from jar file)
              String loc = tagLib[i].getTaglibLocation().get_value();
              if (loc.charAt(0) != '/' && !loc.startsWith("WEB-INF")) {
                loc = "WEB-INF/" + loc;
              }
              tagLibDesc.loadDescriptorFromFile(applicationContext.getWebApplicationRootDir() + loc);
              for (int j = 0; j < tagLibDesc.getTagFiles().length; j++) {
                TagFileInfo tagFile = tagLibDesc.getTagFiles()[j];
  
                String filename = applicationContext.getWebApplicationRootDir() + tagFile.getPath();
                File file = new File(filename);
                JspConfigurationProperties jspConfigurationProperties = applicationContext.getJspConfiguration().getJspProperty(filename);
                TagFileInfo tagFileInfo = getTagFileParser(tagLibDesc).parseTagFile(filename, tagFile.getName(), tagFile.getPath(), file.lastModified(), jspConfigurationProperties, tagLibDesc);
  
                /*
                * JSP.8.4.3 Packaging Directly in a Web Application
                * If a directory contains two files with the same tag name (e.g. a.tag and a.tagx),
                * it is considered to be the same as having a TLD file with two <tag> elements
                * whose <name> sub-elements are identical. The tag library is therefore considered
                * invalid.
                */
                for (int k = 0; j < k; k++) {
                  TagFileInfo alreadyAdded = tagLibDesc.getTagFiles()[k];
                  if (alreadyAdded.getName().equals(tagFile.getName())) {
                    throw new JspParseException(JspParseException.TAG_FILE_HAS_DUPLICATE_NAME, new Object[]{tagFile.getName(), tagFile.getPath()});
                  }
                }
                tagLibDesc.setTagFileInfo(j, tagFileInfo);
              }
            }
            if (tagLib[i].getTaglibUri() != null) {
              TagLibraryValidator tlv = GenerateJavaFile.instantiateValidator(applicationContext.getClassLoader(), tagLibDesc);
              if( tlv != null ) {
                applicationContext.getWebComponents().addTagLibraryValidator(tagLib[i].getTaglibUri().get_value(), tlv);
              }
              if (parsed) {
                applicationContext.getWebComponents().addTagLibraryInfo(tagLib[i].getTaglibUri().get_value(), tagLibDesc);
              }
            }
            ListenerType[] listenersTL = tagLibDesc.getTagLibrary().getListener();
            if (listenersTL != null) {
              for (int j = 0; j < listenersTL.length; j++) {
                if (listenersTL[j].getListenerClass() != null) {
                  tagLibListeners.put(listenersTL[j].getListenerClass().get_value(), listenersTL[j].getListenerClass().get_value());
                }
              }
            }
          }
        } catch (OutOfMemoryError e) {
          throw e;
        } catch (ThreadDeath e) {
          throw e;
        } catch (Throwable e) {
          if (tagLib[i] != null && tagLib[i].getTaglibUri() != null) {
            applicationContext.getWebComponents().addTagLibraryException(tagLib[i].getTaglibUri().get_value(), e);
          }
            warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
            WebWarningException.ERROR_IN_TAGLIB_TAG_IN_WEBXML_CANNOT_LOCATE_OR_PARSE_IT,
            new Object[]{tagLib[i].getTaglibUri(), e.toString()}));
        }
      }
      return tagLibListeners;
    } finally {
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag);
      }//ACCOUNTING.end - END
    }
  }

  public boolean loadTLDListenerFromClassLoaderRsource(String tldFile, TagLibDescriptor tagLibDesc) throws Exception {
    boolean parsed = false;
    InputStream is = null;
    String tmpTLD = null;
    try {
      if (tldFile.charAt(0) == '/') {
        tmpTLD = tldFile.substring(1, tldFile.length());
      } else {
        tmpTLD = tldFile;
      }

      long startup = System.currentTimeMillis();
      is = applicationContext.getClassLoader().getResourceAsStream(tmpTLD);
      startup = System.currentTimeMillis() - startup;
      WebContainer.loadTLDListenerFromClassLoaderRsourceTime.addAndGet(startup);
      if (is == null) {
        String rootDir = applicationContext.getWebApplicationRootDir();
        String portletXml = rootDir + "WEB-INF" + File.separator + "portlet.xml";
        if ((new File(portletXml)).exists()) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000027",  
             "Resource file [{0}] cannot be found.", new Object[]{tmpTLD}, null, null);
        }
      } else {
        tagLibDesc.loadDescriptorFromStream(is);
        //Exception is thrown if the tld can not be parsed
        parsed = true;
      }
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException io) {
          if (traceLocation.beWarning()) {
          	LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000512",
    						"Error while closing InputStream for [{0}].", new Object[]{tldFile}, io, null, null);
          }
        }
      }
    }
    return parsed;
  }

  private void loadFilters() {
    String accountingTag = "AppThreadInitializer/loadFilters of (" + applicationContext.getAliasName() + ")";
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag, ApplicationThreadInitializer.class);
      }//ACCOUNTING.start - END
    
      FilterType[] filterDesc = webAppConfig.getFilters();
      if (filterDesc == null || filterDesc.length == 0) {
        return;
      }
      for (int i = 0; i < filterDesc.length; i++) {
        if (filterDesc[i].getFilterName() != null && filterDesc[i].getFilterName().get_value() != null
          && filterDesc[i].getFilterClass() != null && filterDesc[i].getFilterClass().get_value() != null) {
          
          try {
            ThreadWrapper.pushSubtask("Loading filter [" + filterDesc[i].getFilterName().get_value() + "] of web application [" + 
              ((applicationContext != null) ? applicationContext.getAliasName() : "") + "].", ThreadWrapper.TS_PROCESSING);
  
            ParamValueType[] params = filterDesc[i].getInitParam();
    
            Vector filterWarnings = applicationContext.getWebComponents().addFilter(filterDesc[i].getFilterName().get_value(),
              filterDesc[i].getFilterClass().get_value(),
              getFilterInitParameters(params));
            if (filterWarnings != null && filterWarnings.size() > 0) {
              Enumeration en = filterWarnings.elements();
              while (en.hasMoreElements()) {
                warnings.add(en.nextElement());
              }
            }
          } finally {
            ThreadWrapper.popSubtask();
          }
        }
      }//for
    } finally {
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag);
      }//ACCOUNTING.end - END     
    }
  }

  private void loadListeners() {
    String accountingTag = "AppThreadInitializer/loadListeners of (" + applicationContext.getAliasName() + ")";
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag, ApplicationThreadInitializer.class);
      }//ACCOUNTING.start - END
      
      Vector listnerWarnings = new Vector();
      String[] listenerNames = webAppConfig.getListeners();
      if (listenerNames != null) {
        Class listenerClass = null;
        for (int i = 0; i < listenerNames.length; i++) {
          try {
            ThreadWrapper.pushSubtask("Loading listener [" + listenerNames[i] + "] of web application [" + ((applicationContext != null) ? applicationContext.getAliasName() : "") + "]", ThreadWrapper.TS_PROCESSING);
            long startup = -1;
            if (Accounting.isEnabled()) {
              Accounting.beginMeasure("/loadClass(" + listenerNames[i] + ")", applicationContext.getClassLoader().getClass());
              startup = System.currentTimeMillis();        
            }//ACCOUNTING.start - END
            listenerClass = applicationContext.getClassLoader().loadClass(listenerNames[i]);
            if (Accounting.isEnabled()) {
              startup = System.currentTimeMillis() - startup;
              WebContainer.loadClassTime.addAndGet(startup);
              Accounting.endMeasure("/loadClass(" + listenerNames[i] + ")");
            }
            listnerWarnings = applicationContext.getWebComponents().addListener(listenerClass.newInstance());
          } catch (OutOfMemoryError e) {
            throw e;
          } catch (ThreadDeath e) {
            throw e;
          } catch (Throwable e) {
            warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
              WebWarningException.CANNOT_LOAD_LISTENER,
              new Object[]{listenerNames[i], e.toString()}));
          } finally {
            ThreadWrapper.popSubtask();
          }
        }
        if (listnerWarnings != null && listnerWarnings.size() > 0) {
          Enumeration en = listnerWarnings.elements();
          while (en.hasMoreElements()) {
            warnings.add(en.nextElement());
          }
        }
      }
    } finally {
      if (Accounting.isEnabled()) {// ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag);
      }//ACCOUNTING.end - END  
    }
  }

  private void loadTagLibListeners(HashMapObjectObject listenersFromTagLib) {
    //added listeners from taglib (they can double?)
    Vector listnerWarnings = new Vector();
    if (listenersFromTagLib.size() > 0) {
      String accountingTag = "AppThreadInitializer(" + applicationContext.getAliasName() + ")/loadTagLibListeners";
      try {
        if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
          Accounting.beginMeasure(accountingTag, ApplicationThreadInitializer.class);
        }//ACCOUNTING.start - END
        
        ThreadWrapper.pushSubtask("Loading taglib listeners of web application [" + 
          ((applicationContext != null) ? applicationContext.getAliasName() : "") + "].", ThreadWrapper.TS_PROCESSING);

        Class listenerClass = null;
        Enumeration en = listenersFromTagLib.elements();
        while (en.hasMoreElements()) {
          try {
            long startup = -1;
            if (Accounting.isEnabled()) {
              Accounting.beginMeasure("/loadClass", applicationContext.getClassLoader().getClass());
              startup = System.currentTimeMillis();        
            }//ACCOUNTING.start - END
            listenerClass = applicationContext.getClassLoader().loadClass((String) en.nextElement());
            if (Accounting.isEnabled()) {
              startup = System.currentTimeMillis() - startup;
              WebContainer.loadClassTime.addAndGet(startup);
              Accounting.endMeasure("/loadClass");
            }
            listnerWarnings = applicationContext.getWebComponents().addListener(listenerClass.newInstance());
          } catch (OutOfMemoryError e) {
            throw e;
          } catch (ThreadDeath e) {
            throw e;
          } catch (Throwable e) {
            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logWarning(currentLocation, "ASJ.web.000028",
              "Cannot load taglib listener [{0}].", new Object[]{listenerClass}, e, null, null);
          }
          if (listnerWarnings != null && listnerWarnings.size() > 0) {
            Enumeration enWarnings = listnerWarnings.elements();
            while (enWarnings.hasMoreElements()) {
              warnings.add(enWarnings.nextElement());
            }
          }
        } //while en.hasMoreElements()
      } finally {
        ThreadWrapper.popSubtask();
        
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.endMeasure(accountingTag);
        }//ACCOUNTING.end - END
      } 
    } //if listenersFromTagLib.size() > 0
  }

  private void loadServlets() {
    String accountingTag = "AppThreadInitializer/loadServlets of (" + applicationContext.getAliasName() + ")";
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag, ApplicationThreadInitializer.class);
      }//ACCOUNTING.start - END
      
      if (allServlets != null) {
        for (int i = 0; i < allServlets.length; i++) {
          try {
            ThreadWrapper.pushSubtask("Loading servlet [" + allServlets[i][1] + "] of web application [" + 
              ((applicationContext != null) ? applicationContext.getAliasName() : "") + "].", ThreadWrapper.TS_PROCESSING);
  
            String servletName = allServlets[i][1];
            String servletClass = allServlets[i][2];
            boolean isJsp = servletClass.endsWith(".jsp");
            if (isJsp) {
              try {
                servletClass = getClassName(servletClass);
                if (servletClass == null) {
                  continue;
                }
                //            servletClass = jspParser.parse(scf.getServletContext().getRealPath(servletClass).replace(File.separatorChar, ParseUtils.separatorChar));
                if (traceLocation.beInfo()) {
                	traceLocation.infoT("JSP [" + servletClass + "] parsed successfully.");
                }
              } catch (OutOfMemoryError e) {
                throw e;
              } catch (ThreadDeath e) {
                throw e;
              } catch (Throwable e) {
                warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
                  WebWarningException.CANNOT_PARSE_JSP_ERROR_IS,
                  new Object[]{servletClass, e.toString()}));
              }
            }
            if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
              Accounting.beginMeasure("loadServlets/addServlet (" + servletName + ")", ApplicationThreadInitializer.class);
            }//ACCOUNTING.start - END
            
            Vector servletWarnings = applicationContext.getWebComponents().addServlet(servletName, servletClass, isJsp);
           
            if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
              Accounting.endMeasure("loadServlets/addServlet (" + servletName + ")");
            }//ACCOUNTING.end - END
            
            if (servletWarnings.size() > 0) {
              Enumeration en = servletWarnings.elements();
              while (en.hasMoreElements()) {
                warnings.add(en.nextElement());
              }
            }
          } finally {
            ThreadWrapper.popSubtask();
          }
        } //for allServlets
      }
    } finally {
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag);
      }//ACCOUNTING.end - END
    }
  }

  private void compileJspFiles() {
    if (ServiceContext.getServiceContext().getWebContainerProperties().compileOnStartup()) {
      String accountingTag = "compileJspFiles of " + applicationContext.getAliasName();
      try {
        if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
          Accounting.beginMeasure(accountingTag, ApplicationThreadInitializer.class);
        }//ACCOUNTING.start - END
        
        ThreadWrapper.pushSubtask("Compiling jsp files of web application [" + 
          ((applicationContext != null) ? applicationContext.getAliasName() : "") + "].", ThreadWrapper.TS_PROCESSING);
      
        JSPCompiler jspCompiler = new JSPCompiler(applicationContext, applicationContext.getWebApplicationRootDir());
        jspCompiler.start();
        try {
          jspCompiler.join();
        } catch (InterruptedException e) {
          LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000081",
            "Thread interrupted while waiting to compile jsp files.", e, null, null);
        }
      } finally {
        ThreadWrapper.popSubtask();
        
        if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
          Accounting.endMeasure(accountingTag);
        }//ACCOUNTING.end - END
      }
    }//if
  }

  /**
   * Returns the classname for this JSP page.
   *
   * @param jspFile
   * @return - null if the given file doesn't exist or is a directory.
   * @throws javax.servlet.ServletException
   * @throws ServletNotFoundException
   * @throws IOException
   */
  private String getClassName(String jspFile) throws javax.servlet.ServletException, ServletNotFoundException, IOException {
    String accountingTag = "getClassName(" + jspFile + ")";
    try {
      if (Accounting.isEnabled()) {//ACCOUNTING.start - BEGIN
        Accounting.beginMeasure(accountingTag, ApplicationThreadInitializer.class);
      }//ACCOUNTING.start - END
    
      jspFile = applicationContext.getServletContext().getRealPath(jspFile).replace(File.separatorChar, ParseUtils.separatorChar);
      File f = new File(jspFile);
      if (!f.exists() || f.isDirectory()) {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.CANNOT_FIND_FILE_IN_WAR_FILE,
          new Object[]{jspFile}));
        return null;
      }
  
      JspParser jspParser = null;
      String className = null;
      try {
        jspParser = JspParserFactory.getInstance().getParserInstance(JSPProcessor.PARSER_NAME);
        className = jspParser.generateJspClass(f, null, applicationContext.getAliasName(), null);
      } catch (JspParserInitializationException initializationException) {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.CANNOT_PARSE_JSP_ERROR_IS,
          new Object[]{jspFile, "Cannot get JSP parser instance."}));
      } catch (JspParseException jspParseException) {
        warnings.add(new LocalizableTextFormatter(WebResourceAccessor.getResourceAccessor(),
          WebWarningException.CANNOT_PARSE_JSP_ERROR_IS,
          new Object[]{jspFile, jspParseException.getMessage()}));
      }
      return className; 
    } finally {
      if (Accounting.isEnabled()) {//ACCOUNTING.end - BEGIN
        Accounting.endMeasure(accountingTag);
      }//ACCOUNTING.end - END
    }
  }

  private TagFileParser getTagFileParser(TagLibDescriptor tld) {
    if( compilerParams == null ){
      compilerParams = TagCompilerParams.createInstance(applicationContext);
    }
    if( parserParameters == null ){
      parserParameters = new ParserParameters(applicationContext.getWebComponents().getTagLibDescriptors(),
          applicationContext.getWebComponents().getTagLibraryValidators(),
          applicationContext.getWebApplicationRootDir(),
          null,
          applicationContext.getJspConfiguration(),
          applicationContext.getClassLoader(),
          compilerParams,
          applicationContext.getIncludedFilesHashtable(JSPProcessor.PARSER_NAME), // this should be JSPProcessor.getCurrentParserName() called with Parser API.
          null,
          applicationContext.getJspIdGenerator()
        );        
    }
    TagFileParser tagfileParser = new TagFileParser(threadLoader, parserParameters, ((JspParserFactoryImpl) (JspParserFactory.getInstance())).getContainerProperties());
    tagfileParser.setTagFileTLD(tld);
    return tagfileParser;
  }

  public boolean isJSFApplication() {
    // if there is a FacesServlet in the web.xml, the application is a JSF application
    if (webAppConfig.getServletClasses().containsValue("javax.faces.webapp.FacesServlet")) {
      return true;
    }
    return false;
  }
  
  private boolean isValid(String name) {
	  String tmp = name.toUpperCase();
	  if (tmp.startsWith("/")){
		  tmp = tmp.substring(1,tmp.length());
	  }
	  if (!this.isJSFApplication() && (tmp.equals("META-INF/HTML_BASIC.TLD")||(tmp.equals("META-INF/JSF_CORE.TLD")))){
		  return false;
	  }else{ 
		  return tmp.startsWith("META-INF");
	  }
  }

}
