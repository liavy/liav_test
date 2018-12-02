package com.sap.engine.services.webservices.jaxrpc.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import com.sap.engine.lib.jar.InfoObject;
import com.sap.engine.lib.jar.JarUtils;
import com.sap.engine.lib.xml.SystemProperties;
import com.sap.engine.services.webservices.wsdl.WSDLException;

/**
 * WSDL Proxy generator Utility.
 * Util for compiling, building jars
 * @author       Chavdar Baykov , Chavdarb@yahoo.com
 * @version      2.0
 */
public class PackageBuilder implements PackageBuilderInterface {
  /**
   * Boolean for debugging.
   */
  private static boolean debug = false;
  /**
   * Aditional classpath to be added.
   */
  private String additionalClassPath = "";
  /**
   * Util for creating jar files
   */
  private JarUtils jarUtil;
  /**
   * Class files extension
   */
  private String classExt = "class";
  /**
   * Holds files wich will be included in jar file as infoObjects
   */
  private Vector classFiles;
  /**
   * The root directory containing contents to be archived or compiled
   */
  private File packageRoot;
  /**
   * Package to be built
   */
  private File packageDir;
  private String packageName;
  private File outputPath;
  /**
   * Name Convertor util
   */
  private NameConvertor nameConvertor;

  private ArrayList externalPackages = new ArrayList();
  /**
   * Buffers used when compiling files
   */
  private static ByteArrayOutputStream buferOut;
  private static ByteArrayOutputStream buferErr;

//  private static Method sun_tools_javac_Main_compile = null;
//  private static Constructor sun_tools_javac_Main_constructor = null;

  static {
    try {
      debug = Boolean.getBoolean("com.sap.engine.services.webservices.jaxrpc.util.PackageBuilder.debug");
//      Class sun_tools_javac_Main_class = Class.forName("sun.tools.javac.Main");
//      sun_tools_javac_Main_constructor = sun_tools_javac_Main_class.getConstructor(new Class[] {OutputStream.class, String.class});
//      sun_tools_javac_Main_compile = sun_tools_javac_Main_class.getMethod("compile", new Class[] {String[].class});
    } catch (Exception e) {
      //    $JL-EXC$
      //nothing to do
      //probably classpath to tools.jar is not set
    }
  }

  public PackageBuilder() {
    jarUtil = new JarUtils();
    classFiles = new Vector();
    buferOut = new ByteArrayOutputStream();
    buferErr = new ByteArrayOutputStream();
    nameConvertor = new NameConvertor();
  }

  /**
   * For object reusing
   */
  public void clear() {
    externalPackages.clear();
    classFiles.clear();
    buferOut.reset();
    buferErr.reset();
    packageRoot = null;
    packageDir = null;
    packageName = null;
    outputPath = null;
  }

  /**
   * Sets the place where source package is found
   */
  public void setPackageRoot(File packageRoot) {
    this.packageRoot = packageRoot;
  }

  public void addExternalPackage(String param) {
    this.externalPackages.add(param);
  }
  /**
   * Sets package wich will be included in Jar and compiled
   */
  public void setPackageName(String packageName) {
    if (packageRoot == null) { // If package root not set use current dir as root
      String currentDir = SystemProperties.getProperty("user.dir");
      packageRoot = new File(currentDir);
    }

    this.packageName = packageName;
    if (packageName != null) {
      packageDir = new File(packageRoot, File.separator + nameConvertor.packageToPath(this.packageName) + File.separator);
    } else {
      // If there is no package set then set the package root to compile all
      packageDir = packageRoot;
    }
  }
  
  /**
   * Sets the output path for the compiled sources.
   */
  public void setOutputPath(File outputPath) {
    this.outputPath = outputPath;
  }

  /**
   * add's a file to Archive project by package name and file path
   */
  public void addArchiveFile(String packageName, File filePath) throws Exception {
    String jarFileName = nameConvertor.packageToPath(packageName) + filePath.getName();
    classFiles.add(new InfoObject(jarFileName.substring(1), filePath.getCanonicalPath()));
  }

  /**
   * Returns current class path as "javac" param + package root included
   */
  private String getClassPath() throws Exception {
    return packageRoot.getCanonicalPath() + File.pathSeparator + additionalClassPath;
  }

  /**
   * Default loader - loads all class files found in package dir
   * deep - include subpackages (Yes = true | No = false )
   */
  public void loadPackageClasses(boolean deep) throws Exception {
    loadClassFiles(packageDir, packageName, deep);
    for (int i=0; i<externalPackages.size(); i++) {
      String ePackage = (String) externalPackages.get(i);
      File pDir = new File(packageRoot, File.separator + nameConvertor.packageToPath(ePackage) + File.separator);
      loadClassFiles(pDir,ePackage,false);
    }
  }

  /**
   * Default loader - loads all files with the specified extensions found in package dir
   * deep - include subpackages (Yes = true | No = false )
   */
  public void loadPackageFiles(boolean deep, String[] extensions) throws Exception {
    for (int i = 0; i < extensions.length; i++) {
      loadFiles(packageDir, packageName, deep, extensions[i]);
      for (int j=0; j<externalPackages.size(); j++) {
        String ePackage = (String) externalPackages.get(j);
        File pDir = new File(packageRoot, File.separator + nameConvertor.packageToPath(ePackage) + File.separator);
        loadFiles(pDir,ePackage,false,extensions[i]);
      }
    }
  }

  /**
   * Traverces sub dirs in seek of certain files  of certain packages
   */
  private void loadClassFiles(File filePlace, String jarPlace, boolean deep) throws Exception {
    loadFiles(filePlace, jarPlace, deep, classExt);
  }

  private void loadFiles(File filePlace, String jarPlace, boolean deep, String extension) throws Exception {
    String nextPlace;
    File[] files = filePlace.listFiles();

    for (int i = 0; i < files.length; i++) {
      if (files[i].isFile()) {
        if (files[i].getName().endsWith(extension)) {
          addArchiveFile(jarPlace, files[i]); // Adds class file
        }
      } else {
        if (files[i].isDirectory() && deep == true) {
          if (jarPlace.length() != 0) {
            nextPlace = jarPlace + '.' + files[i].getName();
          } else {
            nextPlace = files[i].getName();
          }

          loadFiles(files[i], nextPlace, true, extension);
        }
      }
    } 
  }

//  private void compileDir(File workDir) throws Exception {
//  }
//
//  private void traverceCompile(File workDir) throws Exception {
//    compileDir(workDir);
//  }

  public void compilePackage() throws Exception {
    String classPath = getClassPath();
    compileExternal(classPath, packageRoot);
    for (int i=0; i<externalPackages.size(); i++) {
      String ePackage = (String) externalPackages.get(i);
      File pDir = new File(packageRoot, File.separator + nameConvertor.packageToPath(ePackage) + File.separator);
      compileExternal(classPath,pDir);
    }
  }

  /**
   * Builds jar from class files loaded to Compress
   */
  public void buildJar(String jarName) throws Exception {
    URL url = new URL(packageRoot.toURL(),"file://"+jarName);
    //System.out.println("Output Dir : "+packageRoot.getAbsolutePath());
    //System.out.println("Output Jar : "+jarName);
    //System.out.println("Output Resolved : "+url.toExternalForm().substring(7));
    jarUtil.makeJarFromFiles(url.toExternalForm().substring(7), classFiles);
  }

  /**
   * Builds jat from class file loaded to Compress full file path is given.
   */
  public void buildJar(File jarFile) throws Exception {
    jarUtil.makeJarFromFiles(jarFile.getCanonicalPath(), classFiles);
  }

  /**
   * Internal class used to clear OS buffers
   */
  static class StreamReader extends Thread {

    // Stream to read and write
    private InputStream in;
    private OutputStream out;

    public StreamReader(InputStream in, OutputStream out) {
      this.in = in;
      this.out = out;
    }

    public void run() {
      try {
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(in)); //$JL-I18N$
        PrintStream output = new PrintStream(out, true);

        while ((line = reader.readLine()) != null) {
          synchronized(output) {
            output.println(line);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
        return;
      } finally {
        try {
          in.close();
        } catch (Exception x) {
          //    $JL-EXC$
        }
      }
    }
  }

  public String getAditionalClassPath() {
    return additionalClassPath;
  }

  public void setAditionalClassPath(String aditionalClassPath) {
    this.additionalClassPath = aditionalClassPath;
  }

//  private void traversThrough(String classPath, File workDir) throws IOException {
//    if (!workDir.exists()) {
//      return;
//    }
//    File dirs[] = workDir.listFiles(dirFilter);
//    int i;
//
//    for (i = 0; i < dirs.length; i++) {
//      traversThrough(classPath, dirs[i]);
//    } 
//
//    File javaFiles[] = workDir.listFiles(javaFileFilter);
//    String javaStringArray[] = new String[javaFiles.length];
//
//    for (i = 0; i < javaFiles.length; i++) {
//      //System.out.println("PackageBuilder setting javaFile: " + javaFiles[i].getCanonicalPath());
//      javaStringArray[i] = javaFiles[i].getName();
//    } 
//
//    ByteArrayOutputStream errStream = new ByteArrayOutputStream();
//    PrintStream ps = new PrintStream(errStream, true);
//    Object main = null;
//    try {
//      main = sun_tools_javac_Main_constructor.newInstance(new Object[] {ps, "javac"});
//    } catch (Exception e) {
//      main = null; // pointless.
//    }
//
//    if (main != null) {
//      compileInternal(classPath, workDir.getCanonicalPath(), javaStringArray, main, errStream,this.outputPath);
//    } else {
////    AdminUtils.compile(classPath, workDir.getCanonicalPath(), javaStringArray, new Properties());
//      compileExternal(classPath, workDir.getCanonicalPath(), javaStringArray, errStream,this.outputPath);
//    }
//  }

  private void traversThrough(String classPath, File workDir) throws IOException {
    ArrayList list = new ArrayList();
    traversThrough0(classPath, workDir, list);
    ByteArrayOutputStream errStream = new ByteArrayOutputStream();
//    PrintStream ps = new PrintStream(errStream, true);
//    Object main = null;
//    try {
//      main = sun_tools_javac_Main_constructor.newInstance(new Object[] {ps, "javac"});
//    } catch (Exception e) {
//      main = null; // pointless.
//    }

//    if (main != null) {
//      if (debug) {
//        System.err.println("PackageBuilder: compileInternal...");
//      }
//      compileInternal(classPath, workDir.getCanonicalPath(), (String[]) list.toArray(new String[]{}), main, errStream,this.outputPath);
//    } else {
//    AdminUtils.compile(classPath, workDir.getCanonicalPath(), javaStringArray, new Properties());
      if (debug) {
        System.err.println("PackageBuilder: compileExternal..."); //$JL-SYS_OUT_ERR$
      }
      compileExternal(classPath, workDir.getCanonicalPath(), (String[]) list.toArray(new String[]{}), errStream,this.outputPath);
//    }
    
  }
  
  private void traversThrough0(String classPath, File workDir, ArrayList javaFilePaths) throws IOException {
    if (!workDir.exists()) {
      return;
    }
    File dirs[] = workDir.listFiles(dirFilter);
    int i;
    for (i = 0; i < dirs.length; i++) {
      traversThrough0(classPath, dirs[i], javaFilePaths);
    } 

    File javaFiles[] = workDir.listFiles(javaFileFilter);
    //String javaStringArray[] = new String[javaFiles.length];

    for (i = 0; i < javaFiles.length; i++) {
      //System.out.println("PackageBuilder setting javaFile: " + javaFiles[i].getCanonicalPath());
      javaFilePaths.add(javaFiles[i].getAbsolutePath());
    } 

//    ByteArrayOutputStream errStream = new ByteArrayOutputStream();
//    PrintStream ps = new PrintStream(errStream, true);
//    Object main = null;
//    try {
//      main = sun_tools_javac_Main_constructor.newInstance(new Object[] {ps, "javac"});
//    } catch (Exception e) {
//      main = null; // pointless.
//    }
//
//    if (main != null) {
//      compileInternal(classPath, workDir.getCanonicalPath(), javaStringArray, main, errStream,this.outputPath);
//    } else {
////    AdminUtils.compile(classPath, workDir.getCanonicalPath(), javaStringArray, new Properties());
//      compileExternal(classPath, workDir.getCanonicalPath(), javaStringArray, errStream,this.outputPath);
//    }
  }

  private static void compileExternal(String classPath, String destDir, String[] javaFiles, ByteArrayOutputStream errStream, File outputPath) throws IOException {
    String externalCompiler = System.getProperty("ExternalCompiler", "javac");
    
    //In general compilation_library should be used.
    //Still, backwards compatibility for tools already using Dynamic API 
    //requires that we keep the old implementation for cases 
    //when a reference to compilation_lib is not available.
    //So, at first we try to compile via compilation_lib. 
    //If a reference to the compilation_lib (or the very library) is missing
    //a NoClassDefFoundError is thrown.
    //We catch it and try to compile using the old implementation.

    boolean serverCase = true;
    
    try {    	
      if (!Compiler.compile(classPath, destDir, javaFiles, outputPath, externalCompiler)) {
        serverCase = false; 
      }
    } catch (NoClassDefFoundError ncdfe) {
      serverCase =  false;	
    }
    
    if (!serverCase) {
      String[] cmd = new String[6];
      if (outputPath != null) {
        cmd = new String[8];
      }
      // If runs on windows platform every file name is quoted by "
      String quote;
      try {
        quote = SystemProperties.getProperty("os.name").toUpperCase(Locale.ENGLISH).startsWith("WINDOWS") ? "\"" : "";
      } catch (Exception e) {
        quote = "";
      }
      // Java comiler executable file name
      cmd[0] = externalCompiler; //System.getProperty("champion.javacompiler", "javac ");
      cmd[1] = "-nowarn";
      cmd[2] = "-encoding";
      cmd[3] = SystemProperties.getProperty("file.encoding", "UTF8");
      //  classpath parameter is constucted by concatenating classPath variable with system classpath
      cmd[4] = "-classpath";
      cmd[5] = quote + classPath + File.pathSeparator + SystemProperties.getProperty("java.class.path") + quote;
      if (outputPath != null) {
        cmd[6] = "-d";
        cmd[7] = quote + outputPath.getAbsolutePath() + quote;
      }    
      int filesForCompile = javaFiles.length;
      int pt = 0;
      int currentFilesForCompile = 0;
      int compilationCycles = 0;
      while (filesForCompile > 0) {
        currentFilesForCompile = filesForCompile;
        if (currentFilesForCompile > 50) {
          currentFilesForCompile = 50;
        }
        compilationCycles++;
        int offset = 6;
        if (outputPath != null) {
          offset = 8;
        }
        String[] cmd1 = new String[currentFilesForCompile + offset];
        System.arraycopy(cmd, 0, cmd1, 0, offset);

        for (int i = 0; i < currentFilesForCompile; i++) {
//        cmd1[i + offset] = destDir + File.separatorChar + javaFiles[pt++];
          cmd1[i + offset] = quote + javaFiles[pt++] + quote;
        }

        filesForCompile = filesForCompile - currentFilesForCompile;
        File c_arg_file = createFileWithCompilerArguments(cmd1, compilationCycles, destDir);
        String[] newcmd = new String[2];
        newcmd[0] = cmd1[0];
        newcmd[1] = "@" + c_arg_file.getAbsolutePath();
        //starting the javac process
        Process compiler = Runtime.getRuntime().exec(newcmd);

        //starting threads to read from the process out and error streams
        (new StreamReader(compiler.getInputStream(), errStream)).start();
        (new StreamReader(compiler.getErrorStream(), errStream)).start();

        int res = 1;
        try {
          res = compiler.waitFor();
        } catch (InterruptedException e) {
          throw new IOException("Compilation fails... Build interrupted !");
        }
        if (debug) { //when debugging do not delete cfg_files
          System.err.println("PackageBuilder: cfg_file '" + c_arg_file.getAbsolutePath()); //$JL-SYS_OUT_ERR$
        } else {
          //delete the arg file
          //c_arg_file.delete();
        }
        if (res != 0) {
          throw new IOException("Compilation fails..." + System.getProperty("line.separator") + errStream.toString()); //$JL-I18N$
        } //if not compiled
      } //while
    } 
  } //compileExternal
  /**
   * Based on compiler arguments gathered inside <code>c_cmd</code> a file is created containing each
   * of the arguments on a new line iside the file. The argument of '0' index is the compiler itself, so
   * it is not taken in consideration.
   * @param c_cmd
   * @param compilationCycle
   * @param destDir the directory into which the file to be created.
   * @return the newly created file.
   */
  private static File createFileWithCompilerArguments(String[] c_cmd, int compilationCycle, String destDir) throws IOException {
    String f_name = "javac_args" + compilationCycle + ".cfg";
    File file = new File(destDir, f_name);
    FileOutputStream f_out = new FileOutputStream(file);
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(f_out)); //$JL-I18N$
    try {
      for (int i = 1; i < c_cmd.length; i++) {
        String r = null;
        /* During testing of JAX-WS integration, it turned out, that when compiling on windows with JDK5_06, there are 
         * compile time problems in case there are mixed slashes or only / slashesh.
         * If they are replaced with \\, then it was alright. 
         * But for linux the behavoir is not yet clear, but it is clear that with this slash \ it does not work on linux, 
         * therefore we implemented a check - if the filename starts with / then - linux mode is assumed, and / is assumed
         * as the default slash
         * 
         */
        if (c_cmd[i].charAt(0) == '/') {
          r = c_cmd[i].replace('\\', '/');
        } else {
          String a = c_cmd[i].replace('\\', '/');
          StringBuffer res = new StringBuffer();
          for (int j=0; j < a.length(); j++) {
            if (a.charAt(j) == '/') {
              res.append("\\\\");
            } else {
              res.append(a.charAt(j));
            }
          }
          r = res.toString();
        }
        out.write(r);
        out.newLine();
      }
    } finally {
      out.close();
    }
    return file;
  }
  
//  private static void compileInternal(String classPath, String destDir, String[] javaFiles, Object main, ByteArrayOutputStream errStream,File outputPath) throws IOException {
//
//    boolean compiled = false;
//    String[] cmd = new String[4];
//    if (outputPath != null) {
//      cmd = new String[6];  
//    }
//    cmd[0] = "-encoding";
//    cmd[1] = SystemProperties.getProperty("file.encoding", "UTF8");
//    cmd[2] = "-classpath";
//    cmd[3] = classPath + File.pathSeparator + SystemProperties.getProperty("java.class.path");
//    if (outputPath != null) {
//      cmd[4] = "-d"; 
//      cmd[5] = outputPath.getCanonicalPath();
//    }
//    //String as[] = {"-classpath", quote + System.getProperty("java.class.path") + sepPath + "./services/servlet_jsp/servlet_jsp.jar" + sepPath + SBasic.servletCompilerClasspath + sepPath + addClassPAth + sepPath + warClassPath + sepPath + ejbClassPath + sepPath + addClasspath + quote, "-d", generateDir, file};
//    int filesForCompile = javaFiles.length;
//    int pt = 0;
//    int currentFilesForCompile = 0;
//
//    while (filesForCompile > 0) {
//      currentFilesForCompile = filesForCompile;
//      int offset = 4;
//      if (outputPath != null) {
//        offset = 6;  
//      }
//      String[] cmd1 = new String[currentFilesForCompile + offset];
//      System.arraycopy(cmd, 0, cmd1, 0, offset);
//
//      for (int i = 0; i < currentFilesForCompile; i++) {
//        cmd1[i + offset] = javaFiles[pt++];
//      }
//
//      filesForCompile = filesForCompile - currentFilesForCompile;
//      try {
//        compiled = ((Boolean) sun_tools_javac_Main_compile.invoke(main, new Object[] {cmd1})).booleanValue();
//      } catch (Exception exc) {
//        compiled = false;
//      }
//      String errors = null;
//
//      if (!compiled) {
//        errors = errStream.toString();
//        throw new IOException("Error in compilation..." + errors);
//      } //if not compiled
//    } //while
//  } //compileInternal

  private FileFilter dirFilter = new FileFilter() {

    public boolean accept(File file) {
      return file.isDirectory();
    }

  };
  private FileFilter javaFileFilter = new java.io.FileFilter() {

    public boolean accept(File file) {
      return file.getName().endsWith(".java");
    }

  };

  public void compile(String classPath, File dir) throws WSDLException, IOException {
    if (classPath != null) {
      classPath += File.pathSeparator + dir.getCanonicalPath();
    } else {
      classPath = dir.getCanonicalPath();
    }

    classPath += File.pathSeparator + SystemProperties.getProperty("java.class.path");
    //System.out.println("PackageBuilder classpath: " + classPath);
    traversThrough(classPath, dir);
  }

  public void compileExternal(String classPath, File workDir) throws IOException {
    this.traversThrough(classPath, workDir);
  }

}

