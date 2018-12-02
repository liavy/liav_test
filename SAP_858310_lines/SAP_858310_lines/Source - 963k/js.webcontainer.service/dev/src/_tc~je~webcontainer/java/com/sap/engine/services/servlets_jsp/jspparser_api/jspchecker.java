/*
 * Copyright (c) 2006 by SAP Labs Bulgaria AG.,
 * url: http://www.saplabs.bg
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia AG.
 */
package com.sap.engine.services.servlets_jsp.jspparser_api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import com.sap.engine.lib.util.ConcurrentHashMapIntObject;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.engine.services.servlets_jsp.server.exceptions.ServletNotFoundException;
import com.sap.engine.services.servlets_jsp.server.jsp.JSPProcessor;
import com.sap.tc.logging.Location;

/**
 * Performs check if given JSP should be parsed:
 * 1) does not have class file
 * 2) has class file but the JSP is newer
 * 3) has included JSPs and they are newer
 * Each ApplicationContext has its own JSPChecker.
 * @author Todor Mollov 
 * @version DEV_webcontainer 2006-1-13
 *  
 */
public class JSPChecker {
  public static final long WAIT_TIMEOUT = 60000;
  public static final String DOT_JAVA = ".java";
  public static final String DOT_CLASS = ".class";
  transient static private Location currentLocation = Location.getLocation(JSPChecker.class);
  transient static private Location traceLocation = LogContext.getLocationRequestInfoServer();
  transient private ApplicationContext applicationContext;
  private String workDir;
  private String alias;
  private ConcurrentHashMapIntObject compile = new ConcurrentHashMapIntObject();
  
  private static final Exception syncExc = new Exception();

  public JSPChecker(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    this.alias = applicationContext.getAliasName();
    this.workDir = applicationContext.getWorkingDir();
  }
  
  /**
   * 
   * @param jspFile
   * @param httpAliasName
   * @param httpAliasValue
   * @param processor
   * @param forceReparse
   * @return
   * @throws IOException
   * @throws ServletException
   */
  public String processJSPRequest(String jspFile, String httpAliasName,
  		String httpAliasValue, JSPProcessor processor, boolean forceReparse, boolean isProductionMode) throws JspParseException {
    String className;
    if ( forceReparse ) {
      String objectJspFile = (String)applicationContext.getClassNamesHashtable(processor.getCurrentParserName()).get(jspFile);
      // Checks if there is an old compiled file and adds it to classNames hashtable. 
      // later this old java/class file will be deleted, and only the new one will exists.
      if (objectJspFile == null) {
        String fileName = getExistingFullJavaFileName(jspFile, httpAliasName, httpAliasValue, processor.getCurrentParserName());
        if (fileName != null) {
          objectJspFile = fileName.substring(workDir.length(), fileName.length()).replace(ParseUtils.separatorChar, '.');
          applicationContext.getClassNamesHashtable(processor.getCurrentParserName()).put(jspFile, objectJspFile);
        }
      }
      /* uncomment for jsp_recompile JSP with inner classes which are not deleted
       * uncommenting this code is not enough for stress test.
      if( objectJspFile != null ) {
        //delete the old files       
        String jspRelativeFileName = getJspRelativeFileName(jspFile, httpAliasName, httpAliasValue);
        String compiledJspDir = applicationContext.getCompiledFileDir(jspRelativeFileName);
        try {
          String[] oldFiles = findFilesByPrefix(objectJspFile, compiledJspDir);
          deleteAll(oldFiles);
        } catch (Exception _) {
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logError(currentLocation,
              "Error deleting the old java and class files generated for the jsp file " + jspFile + ".", _, applicationContext.getAliasName());
        }

        
      }
      */
      
      className = getClassName(jspFile, true, processor);
      return className;
    }
    int hash = jspFile.hashCode();
    waitForLock(hash, jspFile);

    if( isProductionMode ){
      className = getClassNameForProduction(jspFile, hash, httpAliasName, httpAliasValue, processor);
    } else {
      File f = new File(jspFile);
      long fLastModified = f.lastModified();
      className = compileAndGetClassName(jspFile, hash, fLastModified, httpAliasName, httpAliasValue, processor);      
    }

    return className;
  }
  
  /**
   * Wait if somebody else started to compile this JSP. 
   * @param hash
   * @param jspFile
   */
  private void waitForLock(int hash, String jspFile){
    Object obj = null;
    synchronized (this) {
      obj = compile.get(hash);
    }
    if (obj != null) {
      synchronized (obj) {
        long startTime = System.currentTimeMillis();
        long delta = 0;
        while (obj != null && delta < WAIT_TIMEOUT) {
          try {
            if (traceLocation.beInfo()) {
            	traceLocation.infoT("The requested JSP File <" + jspFile + "> is being compiled by another client.\r\n" +
            			"Waiting while the file is compiled.");
            }
            obj.wait(WAIT_TIMEOUT);
          } catch (OutOfMemoryError e) {
            throw e;
          } catch (ThreadDeath e) {
            throw e;
          } catch (Throwable e) {
            LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000279", 
              "Thread interrupted while waiting for the JSP file [{0}] to be compiled. Synchronization may fail.", 
              new Object[]{jspFile}, e, null, null);
          }
          delta = System.currentTimeMillis() - startTime;
        }
      }
    }
  }
  
  /**
   * First searches for class file for this JSP. If found this is returned.
   * Otherwise processes the JSP.
   * @param jspFile
   * @param hash
   * @param httpAliasName
   * @param httpAliasValue
   * @param processor
   * @return
   * @throws JspParseException
   */
  private String getClassNameForProduction(String jspFile, int hash, String httpAliasName,
  		String httpAliasValue, JSPProcessor processor) throws JspParseException{
    String jspClass = (String) applicationContext.getClassNamesHashtable(processor.getCurrentParserName()).get(jspFile);
    // process JSP if only no class file is found.
    if( jspClass == null ){
      String fileName = getExistingFullJavaFileName(jspFile, httpAliasName, httpAliasValue, processor.getCurrentParserName());          
      if (fileName != null) {
        jspClass = fileName.substring(workDir.length(), fileName.length()).replace(ParseUtils.separatorChar, '.');
        applicationContext.getClassNamesHashtable(processor.getCurrentParserName()).put(jspFile, jspClass);
      } else{
		    File f = new File(jspFile);
		    long fLastModified = f.lastModified();
		    jspClass = compileAndGetClassName(jspFile, hash, fLastModified, httpAliasName, httpAliasValue, processor);
      }
    }
    return jspClass;
  }
  
  /**
   * 
   * @param jspFile
   * @param hash
   * @param fLastModified
   * @return
   * @throws javax.servlet.ServletException
   * @throws IOException
   */
  private String compileAndGetClassName(String jspFile, int hash, long fLastModified, String httpAliasName,
  		String httpAliasValue, JSPProcessor processor) throws JspParseException{
    try {
      File file = null;
      Object objectJspFile = applicationContext.getClassNamesHashtable(processor.getCurrentParserName()).get(jspFile);
      boolean needCompilig = (objectJspFile == null);
      if (objectJspFile != null) {
        String pathname = workDir + ((String) objectJspFile).replace('.', ParseUtils.separatorChar) + ".java";
        file = new File(pathname);      
        Object includedFiles = applicationContext.getIncludedFilesHashtable(processor.getCurrentParserName()).get(jspFile);
        if (includedFiles != null) {
          long javaLastModified = file.lastModified();
          String[] included = (String[]) includedFiles;
          for (int i = 0; i < included.length; i++) {   
            // should be canonicalized as in JspIncluldeDirective in verifyAttributes method.
            // and as JSPProcessor.parse otherwise problems may arise on UNIX
            String cannonicalPathIncluded = ParseUtils.canonicalizeFS(included[i]).replace(File.separatorChar, ParseUtils.separatorChar);
            Object includedFile = applicationContext.getClassNamesHashtable(processor.getCurrentParserName()).get(cannonicalPathIncluded);
            if (includedFile != null && getJspLastModified((String) includedFile) != (new File(cannonicalPathIncluded).lastModified())) {
              needCompilig = true;
              break;
            } else if (includedFile == null && javaLastModified < (new File(cannonicalPathIncluded).lastModified())) { // check against the creation time of java file of parent jsp
              //not a jsp
              needCompilig = true;
              break;
            }
          }
        }
      } else {
        String fileName = getExistingFullJavaFileName(jspFile, httpAliasName, httpAliasValue, processor.getCurrentParserName());          
        if (fileName != null) {
          objectJspFile = fileName.substring(workDir.length(), fileName.length()).replace(ParseUtils.separatorChar, '.');
          applicationContext.getClassNamesHashtable(processor.getCurrentParserName()).put(jspFile, objectJspFile);
          file = new File(fileName + ".java");
          needCompilig = false;
        }
      }

      if (file != null && getJspLastModified((String) objectJspFile) == fLastModified) {
        return getClassName(jspFile, needCompilig, processor);
      } else {
        throw syncExc;
      }
    } catch (Exception e) {
      boolean fl = false;
      synchronized (this) {
        if (compile.get(hash) == null) {
          compile.put(hash, new Object());
          fl = true;
        }
      }

      // no such element
      if (fl) {
        try {
          return getClassName(jspFile, true, processor);
        } finally {
          Object obj = null;
          synchronized (this) {
            obj = compile.get(hash);
          }
          synchronized (obj) {
            //Object obj = compile.get(hash);
            compile.remove(hash);
            obj.notifyAll();
          }
        }
      } else {
        Object obj = null;
        synchronized (this) {
          obj = compile.get(hash);
        }
        if (obj != null) {
          synchronized (obj) {
            long startTime = System.currentTimeMillis();
            long delta = 0;
            while (obj != null && delta < WAIT_TIMEOUT) {
              try {
                if (traceLocation.beInfo()) {
                	traceLocation.infoT("The requested JSP file <" + jspFile + "> is being compiled by another client.\r\n" +
                			"Waiting while the file is compiled.");
                }
                obj.wait(WAIT_TIMEOUT);
              } catch (OutOfMemoryError ex) {
                throw ex;
              } catch (ThreadDeath ex) {
                throw ex;
              } catch (Throwable ex) {
                LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000280", 
                  "Thread interrupted while waiting for the JSP file [{0}] to be compiled. Synchronization may fail.", 
                  new Object[]{jspFile}, ex, null, null);
              }
              delta = System.currentTimeMillis() - startTime;
            }
          }
        }
        return getClassName(jspFile, false, processor);
      }
    }
  }
  
  /**
   * 
   * @param jspFile
   * @param needCompiling
   * @return @throws
   *         javax.servlet.ServletException
   * @throws ServletNotFoundException
   * @throws IOException
   */
  private String getClassName(String jspFile, boolean needCompiling, JSPProcessor processor) throws JspParseException {
    String newClass = null;
    if (!needCompiling) {
      newClass = (String) applicationContext.getClassNamesHashtable(processor.getCurrentParserName()).get(jspFile);

      if (newClass == null) {
        throw new JspParseException(JspParseException.IMPLICIT_PARSING_ERROR);
      }
      if (traceLocation.beInfo()) {
      	traceLocation.infoT("The JSP file <" + jspFile + "> will not be recompiled.\r\n" +
      			"The servlet class is: " + newClass);
      }
    } else {
      if (traceLocation.beInfo()) {
      	traceLocation.infoT("The JSP file <" + jspFile + "> will be recompiled.\r\n");
      }
      newClass = processor.parse(jspFile, true);
    }
    return newClass;
  }
  
 
  /**
   * Searches for java file in the file system that corresponds to the specified
   * jspFile.
   *
   * @param jspFile    he name of the jsp file which corresponding class file would
   *                   be searched.
   * @param aliasName  the alias name if the jsp file is requested under alias, or null.
   * @param aliasValue the alias value, or null.
   * @return the full path name of java file as is on the file system that
   *         corresponds the parsed jspFile.
   */
  private String getExistingFullJavaFileName(String jspFile, String aliasName, String aliasValue, String parserInstanceName) {
    String jspRelativeFileName = getJspRelativeFileName(jspFile, aliasName, aliasValue);
    String compiledJspDir = applicationContext.getCompiledFileDir(jspRelativeFileName);
    String javaFileName = findCompiledJspClassName(jspRelativeFileName,
      compiledJspDir, (new File(jspFile)).lastModified(), jspFile.substring(jspFile.lastIndexOf(".") + 1) + "_", parserInstanceName);
    if (javaFileName == null || javaFileName.length() == 0 ) {
      return null;
    }
    return compiledJspDir + javaFileName;
  }
  
  /**
   * Returns the name of the java class file that should correspond to given
   * jspFile name.
   *
   * @param jspFile    the name of the jsp file.
   * @param aliasName  the alias name if the jsp file is requested under alias, or null.
   * @param aliasValue the alias value, or null.
   * @return the relative java class file without decorations for the class name.
   */
  private String getJspRelativeFileName(String jspFile, String aliasName, String aliasValue) {
    String jspRelativeFileName = null;
    if (applicationContext.isDefault() && aliasValue != null) {
      int ind = jspFile.indexOf(aliasValue);
      if (ind != -1) {
        jspRelativeFileName = aliasName + ParseUtils.separator + jspFile.substring(ind + aliasValue.length() + 1, jspFile.lastIndexOf("."));
      }
    } else {
      jspRelativeFileName = jspFile.substring(applicationContext.getWebApplicationRootDir().length(), jspFile.lastIndexOf("."));
    }
    if (jspRelativeFileName != null) {
      jspRelativeFileName = applicationContext.getValidClassNamePath(jspRelativeFileName);
    }
    return jspRelativeFileName;
  }


  /**
   * Searches on the file system in location pointed by the compiledJspDir, for
   * compiled class file corresponding to given jspRelativeFileName.
   *
   * @param jspRelativeFileName a name of the java class file as it would be
   *                            generated for the jsp file.
   * @param compiledJspDir      the directory to be searched in.
   * @param jspLastModified     the last modification date of a jsp file for which
   *                            a class file is searched.
   * @return a java class file name or null if no matched file is found.
   */
  private String findCompiledJspClassName(String jspRelativeFileName, String compiledJspDir, long jspLastModified, String extention, String parserInstanceName) {
    String javaClassName = null;
    String javaClassNamePrefix = parserInstanceName + "_" + extention + jspRelativeFileName.substring(jspRelativeFileName.lastIndexOf(ParseUtils.separator) + 1) +
      "_" + ServiceContext.getServiceContext().getServerId();
    try {
      String[] filePaths = findClassesByPrefix(javaClassNamePrefix, compiledJspDir);
      javaClassName = getNonInnerClassNameFromFile(filePaths);      
      if (getJspLastModified(javaClassName) != jspLastModified) {
        deleteAll(filePaths);
        return null;
      }
    }catch (IOException ioe) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000281",
        "Cannot check for already compiled class file for jsp [{0}].", new Object[]{jspRelativeFileName}, ioe, null, null);
   }
    
    return javaClassName; //JEE_jsp_jspfilename_209319950_1093366024758_1093366024777
  }

  public boolean isJspClassUpToDate(String className, long jspLastModified) {
    return getJspLastModified(className) == jspLastModified;
  }

  /**
   * Deletes the given set of files.
   * @param filePaths
   */
  private void deleteAll(String[] filePaths) {
    if( filePaths == null || filePaths.length == 0) {
      return;
    }
    for(String filePath : filePaths) {
      if (traceLocation.beInfo()) {
      	traceLocation.infoT("Deleting [" + filePath + "].");
			}
      new File(filePath).delete();
    }
  }

  /**
   * From all of the files returns only the file name (without the extension)
   * for the first non inner filePath.
   * The shortest filename is chosen. We rely on that inner classes 
   * have bigger names than the outer classes($1).
   * @param filePaths
   * @return
   */
  private String getNonInnerClassNameFromFile(String[] filePaths) {
    String result = "";
    if( filePaths == null ) {
      return result;
    }
    boolean manyClasses = false; 
    for(int i = 0; i < filePaths.length; i++) {
      if (!filePaths[i].endsWith(DOT_CLASS)) {
        continue;
      }
      String filePath = filePaths[i];
      String fileName =  filePaths[i].substring(filePath.lastIndexOf(File.separator) + 1, filePath.lastIndexOf("."));
      if( result.length() == 0 ) {
        // insert automatically the first
        result = fileName;
        continue;
      }
     
      if( result.length() > fileName.length() ) {
        result = fileName;
      } else if( !manyClasses && result.length() == fileName.length() && fileName.indexOf("$") < 0 ) {
        // if this is not an inner class and there is another class with the same file length
        // then we have 2 classes for one JSP -> not defined which one of them to use
        manyClasses = true;
      }
    }

    if (manyClasses) {
      //delete all classes. a new should be generated.
      try {
        if (traceLocation.beWarning()) {
        	LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO_SERVER).traceWarning("ASJ.web.000583", 
        	  "Found [{0}] classes for one JSP in application: [{1}] \r\n" +
            "One of them is [{2}]. It is abnormal to have more than one. " +
            "The files will be deleted and new one should be generated.", 
            new Object[] {filePaths.length, alias, filePaths[0] }, null, null);
        }
        deleteAll(filePaths);
      } catch (Exception e) {
        LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000282",
          "Found {0} classes for one JSP in application: {1} \r\n" +
          "One of them is {2}. Workaround: stop the application, delete the work directory and start the application again.", 
          new Object[]{filePaths.length, alias, filePaths[0]}, e, null, null);
      }
      return null;
    }
    return result;
  }
  
  /**
   * Returns all class filenames from the given directory which filename starts with the given prefix.
   * @param prefix - JEE_jsp_jspfilename_209319950_
   * @param dirString - most often this will be the work directory
   * @return
   * @throws IOException
   */
  private String[] findClassesByPrefix(String prefix, String dirString) throws IOException{
    List<String> result = new ArrayList<String>();
    File fileDir = new File(dirString);
    if( fileDir.exists() ) {
      File[] fileList = fileDir.listFiles();
      for (File file : fileList) {
        if (file.isFile()) {
          String name = file.getName();
          if( name.startsWith(prefix) && (name.endsWith(DOT_CLASS) || name.endsWith(DOT_JAVA))) {   //add java files too, in order to be deleted i case when they are not up to date
            result.add(file.getCanonicalPath());
          }
        }
      }  
    }
    return result.toArray(new String[result.size()]);
  }
  
  /**
   * Returns the last modification date of the jsp file coded into the specified
   * fileName.
   *
   * @param fileName java class name without the extension.
   * @return a number that represents the last modification date of the jsp file
   *         that corresponds to this fileName.
   */
  private long getJspLastModified(String fileName) {
    long result = 0;
    if( fileName == null || fileName.length()==0 ) {
      return result;
    }
    try {
      int lastUnderscore = fileName.lastIndexOf('_');
      String creationTime =
        fileName.substring(fileName.lastIndexOf('_', lastUnderscore - 1) + 1,
          lastUnderscore);
      result = Long.parseLong(creationTime);
    } catch (NumberFormatException nfe) {
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000283",
        "Incorrect last modified time found for java file [{0}].", new Object[]{fileName}, nfe, null, null);
    }
    return result;
  }
}
