package com.sap.engine.services.servlets_jsp.server.jsp;

import com.sap.engine.services.httpserver.lib.ParseUtils;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParser;
import com.sap.engine.services.servlets_jsp.jspparser_api.JspParserFactory;
import com.sap.engine.services.servlets_jsp.server.LogContext;
import com.sap.engine.services.servlets_jsp.server.application.ApplicationContext;
import com.sap.tc.logging.Location;

import java.io.File;

/**
 * Compiles the all jsp found in the root directory of the default application
 * on start up of the service. Uses Java compiler.
 *
 */
public class JSPCompiler extends Thread {
  private static Location currentLocation = Location.getLocation(JSPCompiler.class);
  private static Location traceLocation = LogContext.getLocationRequestInfoServer();

  /**
   * The root directory where the jsps will be searched
   */
  private String rootdir = null;
  /**
   * A reference to ServletContext of this application
   */
  private ApplicationContext context;

  /**
   *  JspParser instance
   */
  JspParser jspParser = null;

  /**
   * Initiates with a reference to ServletContext and a root directory of the default application.
   *
   * @param   context  a context in naming where a data is stored
   * @param   root  a root directory of the default application
   */
  public JSPCompiler(ApplicationContext context, String root) {
    this.context = context;
    this.rootdir = root;
  }


  /**
   * Starts compiling of the all found jsps.
   *
   */
  public void run() {
    if (traceLocation.beInfo()) {
			traceLocation.infoT("Start compiling all JSP files in the application.");
		}
		try {
      compileJsp();
    } catch (OutOfMemoryError e) {
      throw e;
    } catch (ThreadDeath e) {
      throw e;
    } catch (Throwable e) {
    	//TODO:Polly ok
      LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000176",
        "Error in compiling all JSP files of the web application [{0}]. " +
        "Future requests to some of the JSP files that belong to this application will fail.", 
        new Object[]{context.getAliasName()}, e, null, null);
    }
    if (traceLocation.beInfo()) {
			traceLocation.infoT("The compiling of all JSP files in the application finished.");
		}
  }

  /**
   * Finds a jsp file in a given classpaths.
   *
   * @exception   Exception
   */
  private void compileJsp() throws Exception {
    jspParser = JspParserFactory.getInstance().getParserInstance(JSPProcessor.PARSER_NAME);  
    recursiveFind(rootdir);
  }

  private void recursiveFind(String dirName) throws Exception {
    File f = new File(dirName);
    if (f.isDirectory()) {
      File[] dirFiles = f.listFiles();
      if (dirFiles != null) {
        for (int i = 0; i < dirFiles.length; i++) {
          if (dirFiles[i].exists()) {
            if (dirFiles[i].isDirectory()) {
              recursiveFind(dirFiles[i].getPath().replace(File.separatorChar, ParseUtils.separatorChar));
            } else {
              if (!dirFiles[i].getName().toUpperCase().endsWith(".JSP")) {
                continue;
              }
              try{                
                jspParser.generateJspClass(dirFiles[i], null, context.getAliasName(), null);
              }catch (Exception e) {
            	  //TODO:Polly ok
                LogContext.getCategory(LogContext.CATEGORY_REQUESTS).logWarning(currentLocation, "ASJ.web.000177", 
                  "Error in generating a class file for the JSP file [{0}] in [{1}] web application. " +
                  "Future requests to this JSP file may fail.", new Object[]{dirFiles[i], context.getAliasName()}, e, null, null);              }
              //compile(dirFiles[i].getAbsolutePath().replace(File.separatorChar, ParseUtils.separatorChar));
            }
          }
        }
      }
    }
  }



}

