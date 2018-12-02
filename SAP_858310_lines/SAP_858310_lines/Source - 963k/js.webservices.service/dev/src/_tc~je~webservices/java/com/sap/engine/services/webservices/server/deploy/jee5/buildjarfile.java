package com.sap.engine.services.webservices.server.deploy.jee5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sap.engine.lib.jar.JarUtils;
import com.sap.engine.services.webservices.webservices630.server.deploy.util.IOUtil;

/**
 * Title: BuildJarFile
 * Description: The class manages add/update web services deployment descriptors in archive.
 * Copyright: Copyright (c) 2006
 * Company: Sap Labs Sofia
 * @author Krasimir Atanasov
 * @version 7.10
 */

public class BuildJarFile {

	static final String EXTRACT_FOLDER = "webServicesExtract";
    private final static int BUFFER = 2048; 
    private static byte data[] = new byte[BUFFER]; 
    private static final String WEB_SERVICE_XML_FILE = "webservices-j2ee-engine-ext.xml";

    /**
	 * Build Jar file
	 * 
	 * @param webServicesClientFile - name web service descriptor (webservices.xml or  webservices-j2ee-engine-ext.xml) 
	 * @param tmpPath - path to war/jar archives location
	 * @param moduleRelativeFileUri - name of jar/war archive
	 */	 
  public static void buildJar(String webServicesClientFile, String tmpPath, String moduleRelativeFileUri) throws Exception {
    buildJar(webServicesClientFile, tmpPath, moduleRelativeFileUri, moduleRelativeFileUri); 
  }
  
  public static void buildJar(String webServicesClientFile, String tmpPath, String moduleRelativeFileUri, String archiveFileName) throws Exception {
			String outputDir = tmpPath+File.separator + EXTRACT_FOLDER + System.currentTimeMillis();
			try{
			/* Extract archive */ 	 
			extractArchive(outputDir, tmpPath, moduleRelativeFileUri); 

			/* Replace webservices.xml file */
			String webServicesXmlDir;
			if(WEB_SERVICE_XML_FILE.equals(webServicesClientFile)){
		webServicesXmlDir = "META-INF";
			}else{
	    if(moduleRelativeFileUri.endsWith(".war")) {
	      webServicesXmlDir = "WEB-INF";
	    } else {
		  webServicesXmlDir = "META-INF";
	    }	
			}
			
	  IOUtil.copyFile(tmpPath+File.separator+webServicesClientFile, outputDir + "/" + webServicesXmlDir + "/" +webServicesClientFile);
	  //new JarUtils().makeJarFromDir(tmpPath+File.separator+moduleRelativeFileUri, outputDir);
	  new JarUtils().makeJarFromDir(tmpPath + File.separator + archiveFileName, outputDir);

			/* Delete files and dirs */
	  IOUtil.deleteDir(outputDir);	 
	  new File(tmpPath+File.separator+webServicesClientFile).delete();
			} catch(Exception e){
				e.printStackTrace();
	  throw e; 
			}
	 }
	 
	 /**
	  * Extract archive
	  * 
	  * @param outputDir - indicate where to extract the archive (generatedXml+EXTRACT_FOLDER)
	  * @param generatedXml - path to war/jar archives location
	  * @param moduleRelativeFileUri - name of jar/war archive
	  */		 
	 private static void extractArchive(String outputDir, String generatedXml, String moduleRelativeFileUri){
		 FileInputStream fis =null;
		 ZipInputStream zis = null;
		 FileOutputStream fos = null;
		 try {
			 int count;
			 fis = new FileInputStream(new File(generatedXml+File.separator+moduleRelativeFileUri));
			 zis = new ZipInputStream(fis);
			 ZipEntry entry;
			 String fileName; 
			 while((entry = zis.getNextEntry()) != null) {
				 try {
				 if (entry.isDirectory()) {
        		 // todo : create empty dir with File.mkdirs() 
					 continue;
				 }
				 // write the files to the disk
				 fileName = outputDir + File.separator +  entry.getName();
				 File parent = new File(fileName).getParentFile();
				 if (!parent.exists()) {
					 parent.mkdirs();
				 }
				 fos = new FileOutputStream(fileName);
				 while ((count = zis.read(data, 0, BUFFER)) != -1) {
					 fos.write(data, 0, count);
				 }
				 } finally {
				   try {
					  if(fos!=null)
					    fos.close();
				   } catch(IOException io){
					  // $JL-EXC$
				   } 	 
				 }
				 //fos.close();
			 }
			 //zis.close();
		 } catch(Exception e) {
			 e.printStackTrace();
		 }finally {
			 try{
				 if(fis!=null)
					 fis.close();
			 }catch(IOException io){
				  // $JL-EXC$				
			 }			
			 try{
				 if (zis != null)
					 zis.close();
			 }catch(IOException io){
				  // $JL-EXC$
			 }
		 }
	 } 
	 
}
