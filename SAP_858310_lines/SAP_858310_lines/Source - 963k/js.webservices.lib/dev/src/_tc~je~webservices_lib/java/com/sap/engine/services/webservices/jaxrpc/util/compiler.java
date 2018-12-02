package com.sap.engine.services.webservices.jaxrpc.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.sap.engine.lib.xml.SystemProperties;

/**
 * Compiler using the compilation_lib library. 
 * Used on the engine and by tools which have reference to it.  
 * 
 * @author Mariela Todorova
 */
public class Compiler {
  	
  public static boolean compile(String classPath, String destDir, String[] javaFiles, File outputPath, String externalCompiler) throws IOException {
    boolean serverCase = true;
	  
    if (javaFiles == null || javaFiles.length == 0) {
      return serverCase;
    }
        
    com.sap.engine.compilation.Compiler comp = null; 

    try {
      Properties props = new Properties();
      props.put(com.sap.engine.compilation.CompilationFactory.COMPILER_EXECUTABLE, externalCompiler);
      //do not use method CompilerFactory.getCompiler(String); 
      //it will throw IOExceptions if the String parameter is the same
      comp = com.sap.engine.compilation.CompilationFactory.getCompiler(props);
	
      comp.setNowarn(true);
      comp.setEncoding(SystemProperties.getProperty("file.encoding", "UTF8"));
      comp.addClasspath(classPath + File.pathSeparator + SystemProperties.getProperty("java.class.path"));
  	      
      if (outputPath != null) {
        comp.setDirectory(outputPath.getAbsolutePath());
      } else {
        //String dir = destDir - packageName (in fact, packageRoot)
        comp.setDirectory(destDir);
      }
  	      
      for (int i = 0; i < javaFiles.length; i++) {
        comp.addJavaFileToCompile(javaFiles[i]);
      }           
  	     
      comp.compile();
    } catch (NoClassDefFoundError nfe) {
   	  serverCase= false;  
    } catch (com.sap.engine.compilation.CompilerException e) {
      try {
        throw new IOException("Compilation fails..." + System.getProperty("line.separator") + e.getMessage());
      } catch (NoClassDefFoundError nce) {
        serverCase= false;
      }
    } finally {
      if (comp != null) {
        comp.close();
      }
    }
    
    return serverCase;
  }
  
}
