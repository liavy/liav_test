package com.sap.archtech.archconn.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sap.archtech.archconn.exceptions.XMLGenerationException;
import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Creates sample XML documents and a sample
 * XML schema in the memory. Provides both as InputStream.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class XMLGenerator
{

   private static final Location loc = Location.getLocation("com.sap.archtech.archconn");
   private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");

   /**
    * Creates an XML sample document. The document is bufferd in
    * memory.
    * 
    * @param maxsize number of first level nodes contained in the document
    * @return the sample object can be read from the InputStream
    * @throws XMLGenerationException if DOM-Generation of the sample document fails
    * @throws IOException if streaming of the XML document fails
    */
   public InputStream genXMLdoc(int maxsize) throws XMLGenerationException, IOException
   {
      Element person, email;
      Element name, family, given;
      Node n = null;
      Document xmldoc = null;
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      Source source;
      Result result = new StreamResult(bos);
      Transformer transformer;

      try
      {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         //      DocumentBuilderFactory factory =
         //        (DocumentBuilderFactory) Class.forName("com.sap.engine.lib.jaxp.DocumentBuilderFactoryImpl").newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         transformer = transformerFactory.newTransformer();
         xmldoc = builder.newDocument();
      }
      catch (ParserConfigurationException pacex)
      {
         cat.logThrowableT(Severity.ERROR, loc, "XMLGenerator.genXMLdoc()", pacex);
         throw new XMLGenerationException("XMLGenerator: Parser Configuration Exception: " + pacex.getMessage());
      }
      catch (TransformerConfigurationException tfex)
      {
         cat.logThrowableT(Severity.ERROR, loc, "XMLGenerator.genXMLdoc()", tfex);
         throw new XMLGenerationException("XMLGenerator: Transformer Configuration Exception: " + tfex.getMessage());
      }

      Element root = xmldoc.createElement("personnel");
      root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
      root.setAttribute("xsi:noNamespaceSchemaLocation", "personal.xsd");

      for (int i = 0; i < maxsize; i++)
      {
         person = xmldoc.createElementNS(null, "person");
         person.setAttribute("id", "worker_".concat(Integer.toString(i)));
         name = xmldoc.createElementNS(null, "name");
         family = xmldoc.createElementNS(null, "family");
         n = xmldoc.createTextNode("developer_".concat(Integer.toString(i)));
         family.appendChild(n);
         given = xmldoc.createElementNS(null, "given");
         n = xmldoc.createTextNode("poor_".concat(Integer.toString(i)));
         given.appendChild(n);
         email = xmldoc.createElementNS(null, "email");
         n = xmldoc.createTextNode("stupido_".concat(Integer.toString(i)).concat("@foo.com"));
         email.appendChild(n);

         name.appendChild(family);
         name.appendChild(given);
         person.appendChild(name);
         person.appendChild(email);
         root.appendChild(person);
      }
      xmldoc.appendChild(root);

      try
      {
         source = new DOMSource(xmldoc);
         transformer.transform(source, result);
         bos.flush();
      }
      catch (TransformerException ex)
      {
         cat.logThrowableT(Severity.ERROR, loc, "XMLGenerator.genXMLdoc()", ex);
         throw new IOException("Problems creating XML: " + ex.getMessage());
      }
      return new ByteArrayInputStream(bos.toByteArray());
   }

   /**
    * Creates an XML schema matching the XML documents created
    * with genXMLdoc() and genXMLdoc2().
    * @return the schema can be read from the InputStream
    * @throws IOException if streaming of the XML schema fails
    */
   public InputStream genXSDdoc() throws IOException
   {
      PipedOutputStream pipeOut = new PipedOutputStream();
      PipedInputStream pipeIn = new PipedInputStream(pipeOut);

      new GenerateXSDThread(pipeOut).start();

      return pipeIn;
   }

   /**
    * Creates an XML sample document. The document is not bufferd in
    * memory (the DOM tree is still there). Error messages during 
    * creation of the XML document are written to the console (System.err).
    * 
    * @param maxsize number of first level nodes contained in the document
    * @return the sample object can be read from the InputStream
    * @throws IOException if streaming of the XML document fails
    */
   public InputStream genXMLdoc2(int maxsize) throws IOException
   {
      PipedOutputStream pipeOut = new PipedOutputStream();
      PipedInputStream pipeIn = new PipedInputStream(pipeOut);

      new GenerateXMLThread(maxsize, pipeOut).start();
      return pipeIn;
   }
}
