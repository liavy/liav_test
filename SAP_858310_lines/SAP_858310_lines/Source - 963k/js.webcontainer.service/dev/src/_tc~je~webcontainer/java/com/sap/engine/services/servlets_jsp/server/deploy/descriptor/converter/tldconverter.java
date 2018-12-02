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
import com.sap.engine.lib.io.FileUtils;
import com.sap.engine.lib.jar.InfoObject;
import com.sap.engine.lib.jar.JarUtils;
import com.sap.engine.services.deploy.container.DeploymentException;
import com.sap.engine.services.servlets_jsp.server.deploy.util.ConfigurationUtils;
import com.sap.engine.services.servlets_jsp.server.exceptions.WebDeploymentException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * This class encapsulates the tag library descriptor
 * conversion logic, from older versions to the newest one.
 *
 * @author Georgi Gerginov
 * @author Violeta Georgieva
 * @version 7.10
 */
public class TldConverter extends com.sap.engine.lib.converter.impl.TldConverter {

  public TldConverter() {
  }//end of constructor

  public InfoObject convertTldInJar(InfoObject entryInfo, InputStream jarIS, String convertedWarParent, Vector<String> filesForDelete,
                                    Configuration configuration, Map cache) throws IOException, SAXException, DeploymentException {
    String entryName = (entryInfo.getEntryName()).substring((entryInfo.getEntryName()).lastIndexOf("/") + 1);
    Configuration backupConfig = ConfigurationUtils.createSubConfiguration(configuration, entryName, entryName, true);

    String originalJarFile = null;
    String convertedJarFile = null;
    if (convertedWarParent == null) {
      originalJarFile = entryName;
      convertedJarFile = "converted_" + entryName;
    } else {
      originalJarFile = convertedWarParent + File.separator + entryName;
      convertedJarFile = convertedWarParent + File.separator + "converted_" + entryName;
    }

    File jarFile = new File(originalJarFile);
    FileUtils.writeToFile(jarIS, jarFile);

    File convJarFile = new File(convertedJarFile);

    Vector<InfoObject> allFileEntries = new Vector<InfoObject>();
    JarFile jar = null;
    String tldName = null;
    String convertedTld = null;
    try {
      jar = new JarFile(jarFile);

      boolean isFound = false;
      JarEntry jarEntry = null;
      Enumeration enumeration = jar.entries();
      while (enumeration.hasMoreElements()) {
        jarEntry = (JarEntry) enumeration.nextElement();
        String name = jarEntry.getName();

        if (!jarEntry.isDirectory() && name.toLowerCase().startsWith("meta-inf/") && name.toLowerCase().endsWith(".tld")) {
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
              doc = nonValidatingParser.parse(jar.getInputStream(jarEntry), name);
              XercesUtil.trimWhiteSpaces(doc.getDocumentElement());
              convert(doc, conversionContext);
            } catch (ConversionException e) {
              Throwable t = e.getFileExceptionPairs()[0].getThrowable();
              throw new WebDeploymentException(t.getMessage(), t);
            }
            doc = conversionContext.getConvertedDocument(TldConverter.TLD_FILENAME);
            version = conversionContext.getUnconvertedJ2EEVersion();
          }

          if (version.equals(J2EE_1_4) || version.equals(J2EE_1_5)) {
            allFileEntries.add(new InfoObject(name, jar.getName()));// If it's the new version then don't convert
          } else {
            isFound = true;
            tldName = name.substring(name.lastIndexOf("/") + 1);

            ConfigurationUtils.addFileAsStream(backupConfig, tldName, new ByteArrayInputStream(WebConverter.streamToByteArray(jar.getInputStream(jarEntry))), "", true, true);

            byte[] convertedTldInBytes = WebConverter.xmlToByteArray(doc);

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

            allFileEntries.add(new InfoObject(name, convertedTld));
          }
        } else {
          allFileEntries.add(new InfoObject(name, jar.getName()));
        }
      }

      filesForDelete.add(originalJarFile);

      if (!isFound) {
        return entryInfo;
      } else {
        JarUtils utils = new JarUtils();
        utils.setCompressMethod(ZipEntry.DEFLATED);
        utils.makeJarFromFiles(convJarFile.getPath(), allFileEntries);
        FileUtils.copyFile(convJarFile, jarFile);
        return new InfoObject(entryInfo.getEntryName(), originalJarFile);
      }
    } finally {
      if (jar != null) {
        jar.close();
      }

      if (convertedJarFile != null) {
        convJarFile.delete();
      }

      if (convertedTld != null) {
        (new File(convertedTld)).delete();
      }
    }
  }//end of convertTldInJar(InfoObject entryInfo, InputStream jarIS, String convertedWarParent, Vector filesForDelete, Configuration configuration)

}//end of class