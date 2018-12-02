/*
* Copyright (c) 2004-2006 by SAP AG, Walldorf.,
* http://www.sap.com
* All rights reserved.
*
* This software is the confidential and proprietary information
* of SAP AG, Walldorf. You shall not disclose such Confidential
* Information and shall use it only in accordance with the terms
* of the license agreement you entered into with SAP.
*/
package com.sap.engine.services.servlets_jsp.server.deploy.descriptor.converter;

import com.sap.engine.frame.core.configuration.Configuration;
import com.sap.engine.lib.converter.ConversionContext;
import com.sap.engine.lib.converter.ConversionException;
import com.sap.engine.lib.converter.util.XercesUtil;
import com.sap.engine.lib.descriptors5.web.WebAppType;
import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.lib.jar.InfoObject;
import com.sap.engine.lib.jar.JarUtils;
import com.sap.engine.lib.processor.SchemaProcessor;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.servlets_jsp.server.ServiceContext;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.deploy.util.Constants;
import com.sap.engine.services.servlets_jsp.server.deploy.util.XmlUtils;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebIOException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Converts Servlet 2.2 and 2.3 compatible web application in Servlet 2.4 compatible.
 *
 * @author Violeta Georgieva
 * @version 7.10
 */
public final class WebConverter extends com.sap.engine.lib.converter.impl.WebConverter {

  public WebConverter() {
  }//end of constructor

  /**
   * Converts Servlet 2.2 and 2.3 compatible web application in Servlet 2.4 compatible.
   * The old file is overwritten by the new one. If the file is already
   * Servlet 2.4 compatible, it remains unchanged.
   *
   * @param war                the web application archive.
   * @param warName            war URI
   * @param configuration      backup configuration.
   * @param transformationMode simple - if conversion is made during deploy time,
   *                           extended - if conversion is made during migration time.
   * @param cache              map contains all descriptors cached during JLinEE application validation.
   *                           It may be null if the method is called during online migration.
   * @throws IOException
   * @throws SAXException
   * @throws DeploymentException
   */
  public void convertWAR(File war, String warName, Configuration configuration, String transformationMode, Map cache) throws IOException, SAXException, DeploymentException {
    File convertedFile = null;
    String convertDir = null;

    if (war.getParent() == null) {
      convertDir = "convertwarfile" + File.separatorChar;
    } else {
      convertDir = war.getParent() + File.separatorChar + "convertwarfile" + File.separatorChar;
    }

    convertedFile = new File(convertDir + "converted_" + war.getName());

    convertWAR(war, convertedFile, warName, configuration, transformationMode, cache);

    FileUtils.copyFile(convertedFile, war);

    convertedFile.delete();

    FileUtils.deleteDirectory(new File(convertDir));
  }//end of convertWAR(File war, String warName, Configuration configuration, String transformationMode)

  /**
   * Converts Servlet 2.2 and 2.3 compatible web application in Servlet 2.4 compatible.
   * If the file is already Servlet 2.4 compatible, it is simply copied to
   * the location, specified by the second parameter.
   *
   * @param originalWar        the source web application archive.
   * @param convertedWar       the converted web application archive.
   * @param warName            war URI
   * @param configuration      backup configuration.
   * @param transformationMode simple - if conversion is made during deploy time,
   *                           extended - if conversion is made during migration time.
   * @param cache              map contains all descriptors cached during JLinEE application validation.
   *                           It may be null if the method is called during online migration.
   * @throws IOException
   * @throws SAXException
   * @throws DeploymentException
   */
  private void convertWAR(File originalWar, File convertedWar, String warName, Configuration configuration, String transformationMode, Map cache) throws IOException, SAXException, DeploymentException {
    // new sub configuration in the backup configuration with the name of the converted war
    Configuration backupConfig = ConfigurationUtils.createSubConfiguration(configuration, warName.replace('/', '_'), warName, true);

    String convertedWarParent = convertedWar.getParent();
    if (convertedWarParent != null) {
      File convertedWarParentFile = new File(convertedWarParent);
      if (!convertedWarParentFile.exists()) {
        if (!convertedWarParentFile.mkdirs()) {
          if (!convertedWarParentFile.mkdirs()) {
            throw new WebIOException(WebIOException.CANNOT_MAKE_DIRS, new Object[]{convertedWarParentFile.getAbsolutePath()});
          }
        }
      }
    }

    JarUtils jarUtils = new JarUtils();
    jarUtils.setCompressMethod(ZipEntry.DEFLATED);

    Vector<String> filesForDelete = new Vector<String>();
    Vector<InfoObject> allEntries = new Vector<InfoObject>();
    ZipFile file = null;
    try {
      file = new ZipFile(originalWar);

      ZipEntry entry = null;
      String entryName = null;

      String tldName = null;
      String convertedTld = null;

      Enumeration enumeration = file.entries();
      while (enumeration.hasMoreElements()) {
        entry = (ZipEntry) enumeration.nextElement();
        entryName = entry.getName();

        if (entry.isDirectory() || !(entryName.equals("WEB-INF/" + Constants.WEB_XML) || entryName.equals("WEB-INF/" + Constants.WEB_J2EE_ENGINE_XML))) {
          if (!entry.isDirectory() && entryName.toLowerCase().startsWith("web-inf/lib/") && entryName.toLowerCase().endsWith(".jar")) {
            allEntries.add(ServiceContext.getServiceContext().getDeployContext().getTldConverter().convertTldInJar(
              new InfoObject(entryName, file.getName()), file.getInputStream(entry), convertedWarParent, filesForDelete, backupConfig, cache));
          } else if (!entry.isDirectory() && entryName.toLowerCase().startsWith("web-inf/") && entryName.toLowerCase().endsWith(".tld")) {
            ConversionContext conversionContext = new ConversionContext(null, false);

            //THIS DOCUMENT MUST BE NOT SUBSTITUTED ALWAYS
            Document doc = null;
            String version = null;
            if (cache != null && cache.get("VERSION_" + entryName) != null) {
              version = (String) cache.get("VERSION_" + entryName);
              if (!version.equals(J2EE_1_4) && !version.equals(J2EE_1_5)) {
                doc = (Document) cache.get("DOCUMENT_" + entryName);
              }
            } else {
              //This case must be called only during online migration
              try {
                doc = nonValidatingParser.parse(file.getInputStream(entry), entryName);
                XercesUtil.trimWhiteSpaces(doc.getDocumentElement());
                ServiceContext.getServiceContext().getDeployContext().getTldConverter().convert(doc, conversionContext);
              } catch (ConversionException e) {
                Throwable t = e.getFileExceptionPairs()[0].getThrowable();
                throw new WebDeploymentException(t.getMessage(), t);
              }
              doc = conversionContext.getConvertedDocument(TldConverter.TLD_FILENAME);
              version = conversionContext.getUnconvertedJ2EEVersion();
            }

            if (version.equals(J2EE_1_4) || version.equals(J2EE_1_5)) {
              allEntries.add(new InfoObject(entryName, file.getName()));// If it's the new version then don't convert
            } else {
              tldName = entryName.substring(entryName.lastIndexOf("/") + 1);

              ConfigurationUtils.addFileAsStream(backupConfig, tldName,
                new ByteArrayInputStream(streamToByteArray(file.getInputStream(entry))), "", true, true);

              byte[] convertedTldInBytes = xmlToByteArray(doc);

              if (convertedWarParent == null) {
                convertedTld = tldName;
              } else {
                convertedTld = convertedWarParent + File.separator + tldName;
              }

              FileWriter writer = new FileWriter(convertedTld);
              try {
                writer.write(new String(convertedTldInBytes));
              } finally {
                writer.close();
              }

              allEntries.add(new InfoObject(entryName, convertedTld));

              filesForDelete.add(convertedTld);
            }
          } else {
            allEntries.add(new InfoObject(entryName, file.getName()));
          }
        }
      }

      convertXMLs(file, allEntries, filesForDelete, backupConfig, convertedWarParent, transformationMode, cache);

      jarUtils.makeJarFromFiles(convertedWar.getPath(), allEntries);
    } finally {
      if (file != null) {
        file.close();
      }

      if (filesForDelete.size() > 0) {
        for (int i = 0; i < filesForDelete.size(); i++) {
          (new File((String) filesForDelete.elementAt(i))).delete();
        }
      }
    }
  }//end of convertWAR(File originalWar, File convertedWar, String warName, Configuration configuration, String transformationMode)

  private synchronized void convertXMLs(ZipFile file, Vector<InfoObject> allEntries, Vector<String> filesForDelete, Configuration configuration,
                                        String convertedWarParent, String transformationMode, Map cache) throws IOException, SAXException, DeploymentException {
    Document[] docs = new Document[2];
    ConversionContext conversionContext = new ConversionContext(null, false);
    conversionContext.setAttribute("mode", transformationMode);
    InfoObject webInfo = new InfoObject();
    String version = parseXMLFromWAR(file, webInfo, Constants.WEB_XML, docs, cache, conversionContext);
    Document document = docs[0];
    Document doc22 = docs[1];

    String generatedWebXML25 = null;
    String convertedWebXML = null;
    String convertedSapWebXML2_2 = null;
    String convertedSapWebXML2_3 = null;
    if (version.equals(J2EE_1_5)) {
      WebAppType webApp = (WebAppType) conversionContext.getAttribute("webAppType");
      if (webApp == null) {
        webInfo.setFilePath(file.getName());
      } else {
        SchemaProcessor schema = ServiceContext.getServiceContext().getDeployContext().getWebSchemaProcessor();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        schema.build(webApp, baos);
        generatedWebXML25 = writeConvertedDocument(new String(baos.toByteArray()), convertedWarParent, "web");
        webInfo.setFilePath(generatedWebXML25);
        filesForDelete.add(generatedWebXML25);
      }
    } else if (version.equals(J2EE_1_4)) {
      webInfo.setFilePath(file.getName());
    } else if (document != null) {
      ConfigurationUtils.addFileAsStream(configuration, Constants.WEB_XML,
        new ByteArrayInputStream(streamToByteArray(file.getInputStream(file.getEntry(webInfo.getEntryName())))), "", true, true);

      convertedWebXML = writeConvertedDocument(new String(xmlToByteArray(document)), convertedWarParent, "web");
      webInfo.setFilePath(convertedWebXML);

      filesForDelete.add(convertedWebXML);

      if (version.equals(J2EE_1_2)) {
        convertedSapWebXML2_2 = writeConvertedDocument(new String(xmlToByteArray(doc22)), convertedWarParent, Constants.WEB_J2EE_ENGINE_TAG);

        filesForDelete.add(convertedSapWebXML2_2);
      }
    } else {
      throw new WebIOException(WebIOException.CANNOT_FIND_FILE_IN_WAR_FILE, new String[]{"WEB-INF/" + Constants.WEB_XML, file.getName()});
    }

    allEntries.add(webInfo);

    InfoObject addWebInfo = new InfoObject();
    String addVersion = parseXMLFromWAR(file, addWebInfo, Constants.WEB_J2EE_ENGINE_XML, docs, cache, conversionContext);
    document = docs[0];
    if (addVersion.equals(J2EE_1_5)) {
      addWebInfo.setFilePath(file.getName());
      allEntries.add(addWebInfo);
    } else if (addVersion.equals(J2EE_1_4)) {
      if (transformationMode.equals("simple")) {
        //This will be the right way to do this for customers.
        addWebInfo.setFilePath(file.getName());
      } else if (document != null) {
        //This is because of MS1/2 to MS3 migration
        //There might be new deployed applications with <user-name> and <group-name> tags
        //that are removed in MS3
        ConfigurationUtils.addFileAsStream(configuration, "version1" + Constants.WEB_J2EE_ENGINE_XML,
          new ByteArrayInputStream(streamToByteArray(file.getInputStream(file.getEntry(addWebInfo.getEntryName())))), "", true, true);

        convertedSapWebXML2_3 = writeConvertedDocument(new String(xmlToByteArray(document)), convertedWarParent, Constants.WEB_J2EE_ENGINE_TAG);
        addWebInfo.setFilePath(convertedSapWebXML2_3);

        filesForDelete.add(convertedSapWebXML2_3);
      }

      allEntries.add(addWebInfo);
    } else if (document != null) {
      ConfigurationUtils.addFileAsStream(configuration, Constants.WEB_J2EE_ENGINE_XML,
        new ByteArrayInputStream(streamToByteArray(file.getInputStream(file.getEntry(addWebInfo.getEntryName())))), "", true, true);

      convertedSapWebXML2_3 = writeConvertedDocument(new String(xmlToByteArray(document)), convertedWarParent, Constants.WEB_J2EE_ENGINE_TAG);
      addWebInfo.setFilePath(convertedSapWebXML2_3);

      filesForDelete.add(convertedSapWebXML2_3);

      allEntries.add(addWebInfo);
    } else if (!(version.equals(J2EE_1_4) || version.equals(J2EE_1_5))) {
      addWebInfo.setEntryName("WEB-INF/web-j2ee-engine.xml");
      if (convertedSapWebXML2_2 != null && !convertedSapWebXML2_2.equals("")) {
        addWebInfo.setFilePath(convertedSapWebXML2_2);
      } else {
        convertedSapWebXML2_3 = writeConvertedDocument(Constants.WEB_J2EE_ENGINE_DEFAULT_CONTENT, convertedWarParent, Constants.WEB_J2EE_ENGINE_TAG);
        addWebInfo.setFilePath(convertedSapWebXML2_3);

        filesForDelete.add(convertedSapWebXML2_3);
      }

      allEntries.add(addWebInfo);
    }
  }//end of convertXMLs(ZipFile file, Vector allEntries, Vector filesForDelete, Configuration configuration, String convertedWarParent, String transformationMode)

  private String parseXMLFromWAR(ZipFile file, InfoObject info, String xmlName, Document[] docs, Map cache, ConversionContext conversionContext) throws IOException {
    docs[0] = null;
    docs[1] = null;
    ZipEntry entry = file.getEntry("WEB-INF/" + xmlName);
    if (entry == null) {
      entry = file.getEntry("web-inf/" + xmlName);
      if (entry == null && xmlName.equals(Constants.WEB_J2EE_ENGINE_XML)) {
        return "unknown";
      }
    }

    if (entry != null) {
      info.setEntryName(entry.getName());
    }

    //THIS DOCUMENT MUST BE NOT SUBSTITUTED ALWAYS
    String version = "unknown";
    if (cache != null && cache.get("VERSION_WEB-INF/" + xmlName) != null) {
      version = (String) cache.get("VERSION_WEB-INF/" + xmlName);
      if (!version.equals(J2EE_1_5)) {
        docs[0] = (Document) cache.get("DOCUMENT_WEB-INF/" + xmlName);
        if (xmlName.equals(Constants.WEB_XML)) {
          docs[1] = (Document) cache.get("DOCUMENT_" + WEBJ2EE_FILENAME_2_2);
        }
      } else if (entry == null) {
        //this 'if' statement must be available only in case of web.xml
        info.setEntryName("WEB-INF/web.xml");
        conversionContext.setAttribute("webAppType", (WebAppType) cache.get(WEB_FILENAME));
      }
    } else if (entry != null) {
      //This case must be called only during online migration
      try {
        docs[0] = nonValidatingParser.parse(file.getInputStream(entry), entry.getName());
        XercesUtil.trimWhiteSpaces(docs[0].getDocumentElement());
        //Convert the document
        if (xmlName.equals(Constants.WEB_XML)) {
          convertWebIfNeeded(docs[0], conversionContext);
          docs[0] = conversionContext.getConvertedDocument(WEB_FILENAME);
          docs[1] = conversionContext.getConvertedDocument(WEBJ2EE_FILENAME_2_2);
        } else if (xmlName.equals(Constants.WEB_J2EE_ENGINE_XML)) {
          convertWebJ2EEIfNeeded(docs[0], conversionContext);
          docs[0] = conversionContext.getConvertedDocument(WEBJ2EE_FILENAME);
        }
      } catch (ConversionException e) {
        Throwable t = e.getFileExceptionPairs()[0].getThrowable();
        throw new WebIOException(t.getMessage(), t);
      }
      version = conversionContext.getUnconvertedJ2EEVersion();
    }

    return version;
  }//end of parseXMLFromWAR(ZipFile file, InfoObject info, String xmlName, Document[] docs, Map cache, ConversionContext conversionContext)

  private String writeConvertedDocument(String document, String dstDir, String fileName) throws IOException {
    String dst = null; 
    if (dstDir == null) {
      dst = fileName + ".xml";
    } else {
      dst = dstDir + File.separatorChar + fileName + ".xml";
    }

    FileWriter convertedXMLWriter = new FileWriter(dst);
    try {
      if (!document.toLowerCase().startsWith(Constants.ENCODING_EXTENDED)) { //changed to Constants.ENCODING_EXTENDED because JAVA SE 6.0
        document = Constants.ENCODING + document;
      }
      convertedXMLWriter.write(document);
    } finally { 
      convertedXMLWriter.close();
    }
    return dst;
  }//end of writeConvertedDocument(Document document, String dstDir, String fileName)

  /**
   * @param originalDescriptor
   * @param fileName
   * @param transformationMode
   * @return
   * @throws IOException
   * @throws SAXException
   */
  public synchronized byte[] convertXML(InputStream originalDescriptor, String fileName, String transformationMode) throws IOException, SAXException {
    Document document = null;
    try {
      document = nonValidatingParser.parse(originalDescriptor, fileName);
      XercesUtil.trimWhiteSpaces(document.getDocumentElement());
    } catch (ConversionException e) {
      Throwable t = e.getFileExceptionPairs()[0].getThrowable();
      throw new WebIOException(t.getMessage(), t);
    }

    String version = "unknown";
    if (document != null) {
      version = getWebAppVersion(document);

      if (version.equals("2.4") || version.equals("2.5")) {
        return xmlToByteArray(document);
      }

      if (Constants.WEB_J2EE_ENGINE_TAG.equals(fileName)) {
        Element root = document.getDocumentElement();
        XmlUtils.createSubElement(document, root, "2.3", "spec-version");
      }
      ConversionContext nonValidatingCtx = new ConversionContext(null, false);
      try {
        if ("web".equals(fileName)) {
          if (transformationMode.equals("simple")) {
            document = transform(WEB_FILENAME, document, web24TransformerSimple, nonValidatingCtx);
          } else if (transformationMode.equals("extended")) {
            document = transform(WEB_FILENAME, document, web24TransformerSimple, nonValidatingCtx);
          }
        } else {
          if (transformationMode.equals("simple")) {
            document = transform(WEBJ2EE_FILENAME, document, webJ2ee23To24TransformerSimple, nonValidatingCtx);
          } else if (transformationMode.equals("extended")) {
            document = transform(WEBJ2EE_FILENAME, document, webJ2ee23To24Transformer, nonValidatingCtx);
          }
        }
      } catch (ConversionException e) {
        Throwable t = e.getFileExceptionPairs()[0].getThrowable();
        throw new WebIOException(t.getMessage(), t);
      }

      return xmlToByteArray(document);
    } else {
      throw new WebIOException("The file: " + fileName + ".xml could not be parsed! ");
    }
  }//end of convertXML(InputStream originalDescriptor, String fileName, String transformationMode)

  private static Transformer xmlToStringTransformer = null;
  private static final Properties STANDARD_OUTPUT_PROPS = new Properties();

  public static synchronized byte[] xmlToByteArray(Document doc) throws SAXException {
    try {
      if (xmlToStringTransformer == null) {
        STANDARD_OUTPUT_PROPS.setProperty(OutputKeys.METHOD, "xml");
        STANDARD_OUTPUT_PROPS.setProperty(OutputKeys.INDENT, "yes");

        xmlToStringTransformer = TransformerFactory.newInstance().newTransformer();
        xmlToStringTransformer.setOutputProperties(STANDARD_OUTPUT_PROPS);
      }
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      xmlToStringTransformer.transform(new DOMSource(doc), new StreamResult(bos));
      bos.close();
      return bos.toByteArray();
    } catch (Exception e) {
      throw new SAXException("Could not transform document to byte[]", e);
    }
  }

  public static byte[] streamToByteArray(InputStream stream) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buf = new byte[1024];
    int len;
    while ((len = stream.read(buf)) > 0) {
      baos.write(buf, 0, len);
    }
    return baos.toByteArray();
  }//end of streamToByteArray(InputStream stream)

}//end of class