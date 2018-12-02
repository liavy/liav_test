package com.sap.engine.services.servlets_jsp.jspparser_api.jspparser.taglib.tagfiles;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.jsp.tagext.TagFileInfo;
import javax.servlet.jsp.tagext.TagInfo;

import com.sap.engine.lib.util.ConcurrentHashMapObjectObject;
import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspConfigurationProperties;
import com.sap.engine.services.servlets_jsp.jspparser_api.ParserParameters;
import com.sap.engine.services.servlets_jsp.jspparser_api.WebContainerParameters;
import com.sap.engine.services.servlets_jsp.jspparser_api.exception.JspParseException;
import com.sap.engine.services.servlets_jsp.lib.eclipse.Smap;
import com.sap.engine.services.servlets_jsp.lib.eclipse.SmapInstaller;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagInfoImpl;
import com.sap.engine.services.servlets_jsp.server.deploy.descriptor.TagLibDescriptor;
import com.sap.engine.services.servlets_jsp.server.jsp.JavaCompiler;
import com.sap.engine.services.servlets_jsp.server.jsp.exceptions.CompilingException;
import com.sap.engine.system.ThreadWrapper;
import com.sap.tc.logging.Location;

/**
 * Created by
 * User: Mladen-M
 * Date: 2004-8-4
 * Time: 18:15:06
 */
public class TagFileParser {

  // generated java source and class files for tagfile in jars and these from
  // the filesystem are kept in separate packages, to avoid duplicate names
  public static final String TAG_FILE_PACKAGE_NAME_WEB = "web";
  public static final String TAG_FILE_PACKAGE_NAME_JAR = "jar";

  private static Location currentLocation = Location.getLocation(TagFileParser.class);
  private static Location traceLocation = LogContext.getLocationRequestInfoServer();
  ClassLoader threadLoader = null;
  private Hashtable tagInfos;
  private WebContainerParameters containerParameters;
  //parameters taken from Application and ServiceContext
  private ParserParameters parserParameters;
  /**
   * the TLD in which the tag-file is described or the implicit TLD.
   */
  private TagLibDescriptor tagFileTLD;

  public TagFileParser( ClassLoader serviceLoader, ParserParameters parserParameters, WebContainerParameters containerParameters) {

	this.tagInfos = new Hashtable();
	this.threadLoader = serviceLoader;
	this.parserParameters = parserParameters;
	this.containerParameters = containerParameters;
  }

  /**
   * Process a tagfile packed in a Jar file. If this tagfile is found in the tagiles cache, then a copy of its
   * TagFileInfo object is returned without parsing and compiling the tagfile.
   * <p>Also see the note of the timeLastModified parameter.
   *
   * @return A TagFileInfo object containing the parsed information for the tag
   * @param jarName  The name of the jar file, with the directory part starting from "WEB-INF/lib"
   * @param tagName  The name of the tag, as specified in the TLD with &lt;tag-name&gt;
   * @param tagPath  The name of the jar entry, as specified in the TLD with &lt;tag-path&gt;
   * @param inputStream The tagfile stream obtained from the JarFile
   * @param timeLastModified The tagfile's (inside the jar) last modification time. If a class file with last modification time
   *                         before this value is found, then the tagfile will not be re-compiled, but the existing class will
   *                         be used. However, if the tagfile is not found in the tagfiles cache, but there exists a
   *                         respective class file, then the tagile will still be parsed but not compiled. The parsing is
   *                         necessary to create the TagFileInfo structure.
   */
  public TagFileInfo parseTagFile(String jarName, String tagName, String tagPath, InputStream inputStream, long timeLastModified, JspConfigurationProperties configurationProperties, TagLibDescriptor tld)
  throws IOException, JspParseException, CompilingException {
  	setJspConfigProperties(configurationProperties);
    boolean bePath = traceLocation.bePath();
  if (bePath) {
		traceLocation.pathT("About to parse tagfile " + tagPath + "(tag name \"" + tagName + "\") from jar " + jarName);
	}
	//check if this is valid path value.
	if (!tagPath.startsWith(TagFilesUtil.META_INF_TAGS)) {
      throw new JspParseException(JspParseException.INVALID_TAG_FILE_PATH, new Object[] { tagPath, tld.getURI(), TagFilesUtil.META_INF_TAGS });
    }

	String dir = parserParameters.getTagCompilerParams().getWorkDir()+ TAG_FILE_PACKAGE_NAME_JAR;
	String javaFileName = tagPath.substring(tagPath.indexOf("META-INF/tags/") + "META-INF/tags/".length(), tagPath.lastIndexOf('.'));
	javaFileName = getFileName(javaFileName);

	String tagFileCache = "jar://" + jarName + ":/" + tagPath;
  if (bePath) {
  	traceLocation.pathT("Checking the tagfiles cache for " + tagFileCache);
	}
	String compiledClassName = isTagClassFileUpToDate(dir, javaFileName, timeLastModified);
	if ( parserParameters.getTagCompilerParams().getTagFiles().containsKey(tagFileCache) ) {
    if (bePath) {
    	traceLocation.pathT("Found it in tagfiles cache");
	  }
	  TagFileInfo tagFileInfo = (TagFileInfo) parserParameters.getTagCompilerParams().getTagFiles().get(tagFileCache);
	  if (compiledClassName != null) {
      if (bePath) {
      	traceLocation.pathT("Tagfile " + tagPath + " is up to date");
		  }
		  // return a copy of the TagFileInfo
		  return cloneTagFileInfo(tagFileInfo, tagName, tagPath);
	  } else {
      if (bePath) {
      	traceLocation.pathT("Tagfile " + tagPath + " found in cache, but is not up to date, parsing and compiling");
		  }
		javaFileName = dir + ParseUtils.separator + javaFileName + "_" + containerParameters.getServerID()+ "_" + System.currentTimeMillis() + ".java";
	  }
	} else if (compiledClassName != null) {
    if (bePath) {
    	traceLocation.pathT("Tagfile " + tagPath + " not found in cache, but is up to date");
	  }
	  javaFileName = dir + ParseUtils.separator + compiledClassName + "_" + containerParameters.getServerID() + "_" + System.currentTimeMillis() + ".java";
	} else {
    if (bePath) {
    	traceLocation.pathT("Tagfile " + tagPath + " found neither in cache, nor on filesystem, parsing and compiling");
	  }
	  javaFileName = dir + ParseUtils.separator + javaFileName + "_" + containerParameters.getServerID()+ "_" + System.currentTimeMillis() + ".java";
	}

	String className = javaFileName.substring(javaFileName.lastIndexOf(ParseUtils.separator) + 1, javaFileName.lastIndexOf(".")).replace(ParseUtils.separatorChar, '.');
	/*
	 * create a tag file in the work directory, because
	 * the parser can only parse a File, and not an InputStream
	 */
	String tagFileName = javaFileName.substring(0, javaFileName.lastIndexOf('.')) + tagPath.substring(tagPath.lastIndexOf('.'));
	File workdir = new File(tagFileName.substring(0, tagFileName.lastIndexOf(ParseUtils.separator)));
	if (!workdir.exists()) {
	  if (!workdir.mkdirs()) {
	    if (!workdir.exists()) {
	      throw new IOException("Cannot create directory " + workdir);
	    }
	  }
	}

	FileOutputStream out = new FileOutputStream(tagFileName);
    try {
      int len = 1024;
      byte[] buffer = new byte[len];
      do {
        len = inputStream.read(buffer, 0, 1024);
        if (len != -1) {
        out.write(buffer, 0, len);
        }
      } while (len != -1);
    } finally {
	    out.close();
    }

	TagFileInfo tagFileInfo = null;
	try {
	  TagInfoImpl tagInfo = generateJavaFile(tagFileName, javaFileName, TAG_FILE_PACKAGE_NAME_JAR + "." + className, null, threadLoader, compiledClassName == null);

    tagInfo.setTagName(tagName);
	  tagInfo.setTagClassName(TAG_FILE_PACKAGE_NAME_JAR + "." + className);
	  tagFileInfo = new TagFileInfo(tagName, tagPath, tagInfo.getTagInfo());

	  // register this tagfile, so that consecutive requests for it
	  // do not have to parse and compile it again
//		registerTagFile(null, "jar://" + jarName + ":/" + tagFileInfo.getPath(), tagFileInfo);
//		if (LogContext.isTracing()) {
//		  LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO).tracePath("Registered implicit taglibrary as " + "jar://" + jarName + ":/" + tagFileInfo.getPath(), aliasName);
//		}
  } catch (Exception we) {
      TagInfoImpl ti = new TagInfoImpl();
      ti.setTagName(tagName);
      tagFileInfo = new TagFileInfo(tagName, tagPath, ti.getTagInfo());
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000251",
        "Error parsing tagfile [{0}].", new Object[]{tagFileName}, we, null, null);
      //parserParameters.getTagCompilerParams().getExceptionsTable().put(tagPath, we);
      tld.getExceptionsTable().put(tagPath, we);
	} finally {
	  new File(tagFileName).delete();
	}

	return tagFileInfo;
  }

  /**
   * This method is invoked only for TLDs that are not in JAR files.
   * Process a single tagfile. If this tagfile is found in the tagiles cache, then a copy of its
   * TagFileInfo object is returned without parsing and compiling the tagfile.
   * <p>Also see the note of the timeLastModified parameter.
   *
   * @param tagFile The full path to the tagfile
   * @param tagName  The name of the tag, as specified in the TLD with &lt;tag-name&gt;
   * @param tagPath  The name of the jar entry, as specified in the TLD with &lt;tag-path&gt;
   * @param timeLastModified The tagfile's (inside the jar) last modification time. If a class file with last modification time
   *                         before this value is found, then the tagfile will not be re-compiled, but the existing class will
   *                         be used. However, if the tagfile is not found in the tagfiles cache, but there exists a
   *                         respective class file, then the tagile will still be parsed but not compiled. The parsing is
   *                         necessary to create the TagFileInfo structure.
   * @return A TagFileInfo object containing the parsed information for the tag
   */
  public TagFileInfo parseTagFile(String tagFile, String tagName, String tagPath, long timeLastModified, JspConfigurationProperties configurationProperties, TagLibDescriptor tld)
  throws JspParseException {
    //check if this is valid path value.
    // We are sure that this is TLD in WEB-INF/ not in a JAR file.
    if (!TagFilesUtil.isValidTagdir(tagPath)) {
      throw new JspParseException(JspParseException.INVALID_TAG_FILE_PATH, new Object[]{tagPath, tld.getURI(), TagFilesUtil.WEB_INF_TAGS});
    }
    return parse(tagFile, tagName, tagPath, null, timeLastModified, configurationProperties, tld);
  }

  /**
   * Process all tagfiles under directory "dir" and its subdirectories. Implicitly create a tag library to contain all
   * tagfiles found in this directory.
   *
   * @param dir The directory to search for tagfiles
   * @return A TagLibraryInfo object which is an implicitly created tag library for all the tags in this directory
   */
  public TagLibDescriptor parseTagFiles(String dir) throws IOException, JspParseException {
    TagLibDescriptor tld = null;
    dir = dir.replace(File.separatorChar, ParseUtils.separatorChar);

    if (traceLocation.beInfo()) {
    	traceLocation.infoT("Searching for tagfiles in " + dir + " directory");
    }
    File taglib = new File(dir);
    if (taglib.exists() && taglib.isDirectory()) {
      Vector tagFiles = parseTagFilesInDir(taglib);

      try {
        // shortname is created from the directory structure, omitting the
        // ".../WEB-INF/" part
        // all '/'s should have been replaced with '-', but this could lead to
        // non-unique names, like "tags/a/b" and "tags/a-b" both result in
        // "tags-a-b"
        String shortName = dir.substring(dir.indexOf("WEB-INF/tags") + "WEB-INF/".length());
        Vector tagFiles_ = new Vector();

        if (traceLocation.bePath()) {
        	traceLocation.pathT("Found " + tagFiles.size() + " tagfiles.");
        }
        for (Iterator files = tagFiles.iterator(); files.hasNext();) {
          TagFileInfo tagFile = (TagFileInfo) files.next();
          TagInfo tagInfo = tagFile.getTagInfo();

          /*
           * JSP.8.4.3 Packaging Directly in a Web Application If a directory
           * contains two files with the same tag name (e.g. a.tag and a.tagx),
           * it is considered to be the same as having a TLD file with two <tag>
           * elements whose <name> sub-elements are identical. The tag library
           * is therefore considered invalid.
           */
          for (int i = 0; i < tagFiles_.size(); i++) {
            TagFileInfo alreadyAdded = (TagFileInfo) tagFiles_.get(i);
            if (alreadyAdded.getName().equals(tagFile.getName())) {
              throw new JspParseException(JspParseException.TAG_FILE_HAS_DUPLICATE_NAME, new Object[] { tagFile.getName(), dir });
            }
          }

          if (tagInfo == null) {
            tagFiles_.add(tagFile);
          } else {
            TagFileInfo tagFile_ = new TagFileInfo(tagFile.getName(), tagFile.getPath(), new TagInfo(tagInfo.getTagName(), tagInfo.getTagClassName(), tagInfo.getBodyContent(),
                tagInfo.getInfoString(), null, tagInfo.getTagExtraInfo(), tagInfo.getAttributes(), tagInfo.getDisplayName(), tagInfo.getSmallIcon(), tagInfo.getLargeIcon(), tagInfo
                    .getTagVariableInfos(), tagInfo.hasDynamicAttributes()));
            tagFiles_.add(tagFile_);
            if (traceLocation.bePath()) {
            	traceLocation.pathT("TagFileInfo.getTagName(): " + tagFile.getName() + ", TagFileInfo.getTagPath(): " +
            			tagFile.getPath() + ", TagInfo.getTagName(): " + tagFile.getTagInfo().getTagName());
            }
          }
        }

        // create an implicit taglibraryinfo for each directory
        // the uri is the path to the tagdir
        tld = new TagLibDescriptor(shortName.replace('/', '-'), tagFiles_, threadLoader);
        for (Iterator files = tagFiles.iterator(); files.hasNext();) {
          ((TagFileInfo) files.next()).getTagInfo().setTagLibrary(tld);
        }
        //add exceptions from current parser table to taglib exceptions table
        Enumeration en = getExceptionsTable().keys();
        while (en.hasMoreElements()) {
          String tagPath = (String) en.nextElement();
          Object exc = getExceptionsTable().get(tagPath);
          tld.getExceptionsTable().put(tagPath, exc);
        }

        // the implicitly created tag library is registered, so that it can be
        // reused
        //		  context.getWebComponents().addTagLibraryInfo("/WEB-INF/" +
        // shortName, tld);
        //		  if (LogContext.isTracing()) {
        //			LogContext.getLocation(LogContext.LOCATION_REQUEST_INFO).tracePath("Registered
        // implicit taglibrary as " + "/WEB-INF/" + shortName, aliasName);
        //		  }
      } catch (JspParseException jspe) {
        throw jspe;
      } catch (Throwable t) {
        throw new IOException(dir);
      }
    } else {
      throw new IOException("Directory " + dir + " does not exist, or is not a directory.");
    }
    return tld;
  }

  /**
   * Process all tagfiles in the directory (without its subdirectories). dir is
   * guaranties to be a directory, not a file
   *
   * @param dir
   * @return a list of TagFileInfo objects for each tagfile in this directory
   */
  private Vector parseTagFilesInDir(File dir) throws IOException{
    // a list of all tagfiles found in this directory
    // an implicit tag library will be created for it
    Vector tagFiles = new Vector();

    File[] list = dir.listFiles();
    for (int i = 0; i < list.length; i++) {
        if (!list[i].isDirectory() && (list[i].getName().toLowerCase().endsWith(".tag") || list[i].getName().toLowerCase().endsWith(".tagx"))) {
          if (traceLocation.beInfo()) {
          	traceLocation.infoT("Found tagfile " + list[i].getAbsolutePath());
          }
          String tagFile = list[i].getCanonicalPath().replace(File.separatorChar, ParseUtils.separatorChar);
          TagFileInfo tagFileInfo = null;
          try {
            tagFileInfo = parse(tagFile, null, null, null, list[i].lastModified(), parserParameters.getJspConfigurationProperties(), null);
          } catch (OutOfMemoryError e) {
            throw e;
          } catch (ThreadDeath e) {
            throw e;
//          } catch (WebIOException we) {
//            throw we;
          } catch (Throwable e) {
            LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000252",
              "Error parsing tagfile [{0}].", new Object[]{tagFile}, e, null, null);
            //throw new WebIOException(WebIOException.ERROR_PARSING_TAGFILE, e);
            continue;
          }
          // add this tagfile to the list
          tagFiles.add(tagFileInfo);
        }

    }
    return tagFiles;
  }

  /**
   * <p>Check if a .class file exists in directory dir, with filename starting with javaFileName and with last
   * modification time after timeLastModified
   *
   * @param dir
   * @param javaFileName
   * @param timeLastModified
   * @return null if such a file does not exist. Returns the file name without the path and the .class extension,
   * if found.
   */
  private String isTagClassFileUpToDate(String dir, String javaFileName, long timeLastModified) {
	  File dir_ = new File(dir);
    if (!dir_.exists()) {
      return null;
    }

    File[] entries = dir_.listFiles();
    for (int i = 0; i < entries.length; i++) {
      String fileName = entries[i].getName();
      if (fileName.toLowerCase().endsWith(".class") && fileName.startsWith(javaFileName+"_")) {
        // add "_" in order to find exact file like tag_modelZoom_3914950_1217262396797.class
        // instead of  tag_modelZoomchooser_3914950_1217262401483.class
        if (entries[i].lastModified() >= timeLastModified) {
          return fileName.substring(fileName.lastIndexOf(File.separator) + 1, fileName.toLowerCase().lastIndexOf(".class"));
        } else {
          entries[i].delete();
          File javaFile = new File(dir_, fileName.substring(0,fileName.length() - 6)+".java");
          javaFile.delete();
        }
      }
    }
    return null;
  }

  /**
   *
   *
   * @param tagFile
   * @param tagName
   * @param tagPath
   * @param encoding
   * @param timeLastModified
   * @return A TagFileInfo object containing the parsed information for the tag
   * @throws JspParseException
   */
  //setsp -p TraceLevel 5 servlet_jsp
  private TagFileInfo parse(String tagFile, String tagName, String tagPath, String encoding, long timeLastModified, JspConfigurationProperties configurationProperties, TagLibDescriptor tld)
	  throws JspParseException {
    setJspConfigProperties(configurationProperties);
    boolean bePath = traceLocation.bePath();
    if (bePath) {
    	traceLocation.pathT("About to parse tagfile " + tagFile);
	}

	TagInfoImpl tagInfo = null;
	try {
	  String dir = parserParameters.getTagCompilerParams().getWorkDir()+ TAG_FILE_PACKAGE_NAME_WEB;
	  String javaFileName = tagFile.substring(tagFile.indexOf("WEB-INF" + ParseUtils.separator + "tags") + "WEB-INF/tags/".length(), tagFile.lastIndexOf('.'));
	  javaFileName = getFileName(javaFileName);

	  if (tagPath == null) {
		tagPath = tagFile.substring(tagFile.indexOf(ParseUtils.separator + "WEB-INF" + ParseUtils.separator + "tags"));//, tagFile.lastIndexOf(ParseUtils.separatorChar));
	  }
	  if (tagName == null) {
		tagName = tagFile.substring(tagFile.lastIndexOf(ParseUtils.separator) + 1, tagFile.lastIndexOf(".tag"));
	  }

	  String tagCacheName = tagFile.substring(tagFile.indexOf("/WEB-INF/tags"));
    if (bePath) {
    	traceLocation.pathT("Checking the tagfiles cache for " + tagCacheName);
	  }
	  String compiledClassName = isTagClassFileUpToDate(dir, javaFileName, timeLastModified);
	  if ( parserParameters.getTagCompilerParams().getTagFiles().containsKey(tagCacheName)) {
      // if the tag handler has already been compiled
      if (bePath) {
      	traceLocation.pathT("Found it in tagfiles cache");
      }
      TagFileInfo tagFileInfo = (TagFileInfo)parserParameters.getTagCompilerParams().getTagFiles().get(tagCacheName);
      if (compiledClassName != null) {
        // if the tag handler class is present and is up to date
        if (bePath) {
        	traceLocation.pathT("Tagfile " + tagFile + " is up to date");
        }
        // return a copy of the TagFileInfo
        return cloneTagFileInfo(tagFileInfo, tagName, tagPath);
      } else {
        // the tag handler has been compiled some time ago, but the class file is missing or is not up to date
        if (bePath) {
        	traceLocation.pathT("Tagfile " + tagFile + " found in cache, but is not up to date, parsing and compiling");
        }
        javaFileName = dir + ParseUtils.separator + javaFileName + "_" + containerParameters.getServerID()+ "_" + System.currentTimeMillis() + ".java";
      }
	  } else if (compiledClassName != null) {
      // the tag handler is not in the cache, but is present and is up to date
      // could happen when the server has been restarted
      if (bePath) {
      	traceLocation.pathT("Tagfile " + tagFile + " not found in cache, but is up to date");
      }
        javaFileName = dir + ParseUtils.separator + compiledClassName + ".java";
	  } else {
      // the tag handler has never been compiled
      if (bePath) {
      	traceLocation.pathT("Tagfile " + tagFile + " found neither in cache, nor on filesystem, parsing and compiling");
      }
      javaFileName = dir + ParseUtils.separator + javaFileName + "_" + containerParameters.getServerID()+ "_" + System.currentTimeMillis() + ".java";
	  }

    if (bePath) {
    	traceLocation.pathT("Will generate java source " + javaFileName);
	  }
	  String className = javaFileName.substring(javaFileName.lastIndexOf(ParseUtils.separator) + 1, javaFileName.lastIndexOf(".")).replace(ParseUtils.separatorChar, '.');

	  try {
      // generate the java file for the tagfile and also compile it
      tagInfo = generateJavaFile(tagFile, javaFileName, TAG_FILE_PACKAGE_NAME_WEB + "." + className, encoding, threadLoader, compiledClassName == null);
    } catch (Exception we) {
      TagInfoImpl ti = new TagInfoImpl();
      ti.setTagName(tagName);
      TagFileInfo tagFileInfo = new TagFileInfo(tagName, tagPath, ti.getTagInfo());
      LogContext.getCategory(LogContext.CATEGORY_DEPLOY).logError(currentLocation, "ASJ.web.000253",
        "Error parsing tagfile [{0}].", new Object[]{tagFile}, we, null, null);
      if (tld == null) {
        parserParameters.getTagCompilerParams().getExceptionsTable().put(tagPath, we);
      } else {
        tld.getExceptionsTable().put(tagPath, we);
      }
      return tagFileInfo;
    }

	  tagInfo.setTagName(tagName);
	  tagInfo.setTagClassName(TAG_FILE_PACKAGE_NAME_WEB + "." + className);

	  TagFileInfo tagFileInfo = new TagFileInfo(tagName, tagPath, tagInfo.getTagInfo());

	  // register this tagfile, so that consecutive requests for it
	  // do not have to parse and compile it again
	  registerTagFile(null, tagFileInfo.getPath(), tagFileInfo);
    if (bePath) {
    	traceLocation.pathT("Registered tagfile as " + tagFileInfo.getPath());
	  }

	  return tagFileInfo;


//	  } catch (CompilingException e) {
//		throw e;
	} catch (OutOfMemoryError e) {
	  throw e;
	} catch (ThreadDeath e) {
	  throw e;
	} catch (Throwable e) {
//		context.getClassNamesHashtable().remove(tagFile);
	  throw new JspParseException(JspParseException.ERROR_PARSING_TAGFILE, new Object[]{tagFile}, e);
	}
  }

  private TagFileInfo cloneTagFileInfo(TagFileInfo tagFileInfo, String tagName, String tagPath) {
	TagInfo tagInfo_ = tagFileInfo.getTagInfo();
	TagFileInfo tagFile_ = new TagFileInfo(tagName, tagPath,
		new TagInfo(tagName, tagInfo_.getTagClassName(), tagInfo_.getBodyContent(), tagInfo_.getInfoString(),
			null, tagInfo_.getTagExtraInfo(), tagInfo_.getAttributes(), tagInfo_.getDisplayName(), tagInfo_.getSmallIcon(),
			tagInfo_.getLargeIcon(), tagInfo_.getTagVariableInfos(), tagInfo_.hasDynamicAttributes()));
	return tagFile_;
  }

  /**
   * Generate a java source of the tag handler and compile it
   * to a class file
   *
   * @param tagFile       The full path the to tagfile. If the tagfile in process resides in a jar file, it must
   *                      have already been extracted to the filesystem
   * @param javaFileName  The full path to the java file to be created. If the directory does not exist, it is created
   * @param className     The classname of the tag handler
   * @param encoding
   * @param threadLoader
   * @param compile       Should the generated java source be written to a java file and
   *                      compiled.
   *
   * @return A TagInfo wrapper object containing the parsed tag information
   * @throws JspParseException
   * @throws IOException
   * @throws CompilingException
   */
  private TagInfoImpl generateJavaFile(String tagFile, String javaFileName, String className,
								  String encoding, ClassLoader threadLoader, boolean compile)
	  throws JspParseException, IOException, CompilingException {

	GenerateTagFileJavaFile javaFileGenerator = null;

	File dir = new File(javaFileName.substring(0, javaFileName.lastIndexOf(ParseUtils.separator)));
	if (!dir.exists()) {
	  if (!dir.mkdirs()) {
		throw new IOException("Cannot create directory " + dir);
	  }
	}
    String javaFileEncoding = containerParameters.getJavaEncoding();
	// generate the java file in a memory stream to avoid
	// leaving a zero-sized file if an exception occurs
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	PrintWriter outWriter = new PrintWriter(new OutputStreamWriter(out, javaFileEncoding));

	javaFileGenerator = new GenerateTagFileJavaFile(tagFile);
	javaFileGenerator.generateJavaFile(className, outWriter, threadLoader,parserParameters, containerParameters, tagFileTLD);

	if ( parserParameters.getTagLibraryValidators()!= null && parserParameters.getTagLibraryValidators().size() > 0 ) {
	  javaFileGenerator.validate(threadLoader, parserParameters.getTagLibraryValidators());
	}

	// if the file is to be compiled, then the generated java source is
	// written to a java file and it's afterwards compiled
	if (compile) {
	  // write the generated source to a physical file
	  FileOutputStream fileO = new FileOutputStream(javaFileName);
    try {
	    fileO.write(out.toByteArray());
    } finally {
      fileO.close();
    }
	  JavaCompiler compiler = new JavaCompiler();
    ThreadWrapper.pushSubtask("Compiling tagfile '" + className + "'", ThreadWrapper.TS_PROCESSING);
	  try {
		compiler.compile(javaFileName, 
        parserParameters.getTagCompilerParams().getWorkDir(), 
        parserParameters.getTagCompilerParams().getJarClassPathHashtable(),
				containerParameters.isInternalCompiler(),
        containerParameters.isJspDebugSupport(), 
        containerParameters.getExternalCompiler(), 
        parserParameters.getTagCompilerParams().getJavaVersionForCompilation());
	  } catch (CompilingException tr) {
      if (traceLocation.beInfo()) {
      	traceLocation.infoT("Error in compiling of the tagfile <" + tagFile + "> !\r\n" + "The Java file is: " + javaFileName);
		}
		throw tr;
	  } catch (OutOfMemoryError e) {
		throw e;
	  } catch (ThreadDeath e) {
		throw e;
	  } catch (Throwable e) {
		throw new CompilingException(CompilingException.ERROR_IN_COMPILING_THE_JSP_FILE, new Object[]{tagFile}, e);
	  } finally {
      ThreadWrapper.popSubtask();
	  }

      if (containerParameters.isJspDebugSupport()) {
        if( LogContext.getLocationJspParser().beDebug() ){
          LogContext.getLocationJspParser().debugT("Adding SourceDebugExtension attribute to the generated Java file for the tagfile ["+tagFile+"].");
        }
        try {
          String classNameForEclipse = javaFileName.substring(0, javaFileName.length() - 4) + "class";
          Smap smap = new Smap(classNameForEclipse, new ByteArrayInputStream(out.toByteArray()), javaFileEncoding);
          String smapTable = smap.generateSmap();
          SmapInstaller smapInstaller = new SmapInstaller(classNameForEclipse);
          smapInstaller.injectSmap(smapTable);
        } catch (Exception e) {
          LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000276", 
            "Problem during instrumenting class file with debug info for JSR-045: {0}", new Object[]{e.getMessage()}, e, null, null);
        }
      }
      if (traceLocation.beInfo()) {
      	traceLocation.infoT("The generated Java file for the tagfile <" + tagFile + "> is successfully compiled!\r\n");
      }
	}

	return javaFileGenerator.getTagInfo();
  }

  private String getFileName(String tagFile) {
	StringBuffer javaClassName = new StringBuffer();
	char c = tagFile.charAt(0);
	if (Character.isJavaIdentifierStart(c)) {
	  javaClassName.append(c);
	} else {
	  javaClassName.append("_" + Integer.toHexString(c) + "_");
	}
	for (int i = 1; i < tagFile.length(); i++) {
	  c = tagFile.charAt(i);
	  if (Character.isJavaIdentifierPart(c)) {
		javaClassName.append(c);
	  } else {
		javaClassName.append(c == '/' || c == '\\' ? "_" : "_" + Integer.toHexString(c) + "_");
	  }
	}
	tagFile = "tag_" + javaClassName;
	return tagFile;
  }

/*  private void registerImplicitTLD(String tldShortName, String tagName, String tagPath, TagInfoImpl tagInfo) {
	Vector tags = (Vector)tagInfos.get(tldShortName);
	if (tags == null) {
	  tags = new Vector();
	  tagInfos.put(tldShortName, tags);
	}

	TagFileInfo tagFileInfo = new TagFileInfo(tagName, tagPath, tagInfo.getTagInfo());
	tags.add(tagFileInfo);
  }*/

  public Vector getTagLibraryDescriptors() {
	Vector tlds = new Vector();
	for (Enumeration names = tagInfos.keys(); names.hasMoreElements(); ) {
	  String shortName = (String)names.nextElement();
	  Vector tagFileInfos = (Vector)tagInfos.get(shortName);

	  TagLibDescriptor tld = new TagLibDescriptor(shortName, tagFileInfos, threadLoader);
	  tlds.add(tld);
	}

	return tlds;
  }

  private void registerTagFile(String jarFile, String tagFileName, TagFileInfo tagFileInfo) {
		if (jarFile != null) {
		  tagFileName = "jar://" + jarFile + "/" + tagFileName;
		}
		parserParameters.getTagCompilerParams().getTagFiles().put(tagFileName, tagFileInfo);
  }


	private void setJspConfigProperties(JspConfigurationProperties jspConfigurationProperties)
	{
		parserParameters =  new ParserParameters(
		parserParameters.getTagLibraryDescriptors(),
		parserParameters.getTagLibraryValidators(),
		parserParameters.getApplicationRootDir(),
		jspConfigurationProperties,
    parserParameters.getJspConfiguration(),
    parserParameters.getAppClassLoader(),
		parserParameters.getTagCompilerParams(),
    parserParameters.getIncludedFilesHashtable(),
    parserParameters.getPortalProperties(),
    parserParameters.getJspIdGenerator()
		);


	}

  public ConcurrentHashMapObjectObject getExceptionsTable() {
    return parserParameters.getTagCompilerParams().getExceptionsTable();
  }

  public void setTagFileTLD(TagLibDescriptor tagFileTLD) {
    this.tagFileTLD = tagFileTLD;
  }
}
