package com.sap.archtech.archconn.util;

import java.io.IOException;
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

import com.sap.tc.logging.Category;
import com.sap.tc.logging.Location;
import com.sap.tc.logging.Severity;

/**
 * Helper class used by XML Generator.
 * 
 * @author D025792
 * @version 1.0
 * 
 */
public class GenerateXMLThread extends Thread
{

   private static final Location loc = Location.getLocation("com.sap.archtech.archconn");
   private static final Category cat = Category.getCategory(Category.APPS_COMMON_ARCHIVING, "Connector");

   private int maxsize;
   private PipedOutputStream pos;

   GenerateXMLThread(int maxsize, PipedOutputStream pos)
   {
      this.maxsize = maxsize;
      this.pos = pos;
   }

   public void run()
   {
      Element person, email;
      Element name, family, given;
      Node n = null;
      Document xmldoc = null;
      Result result = new StreamResult(pos);
      Transformer transformer = null;

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
         cat.logThrowableT(Severity.ERROR, loc, "GenerateXMLThread.run()", pacex);
      }
      catch (TransformerConfigurationException tfex)
      {
         cat.logThrowableT(Severity.ERROR, loc, "GenerateXMLThread.run()", tfex);
      }
      if(xmldoc == null)
      {
        return;
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
        if(transformer != null)
        {
          Source source = new DOMSource(xmldoc);
          transformer.transform(source, result);
          pos.flush();
          pos.close();
        }
      }
      catch (TransformerException tfex)
      {
         cat.logThrowableT(Severity.ERROR, loc, "GenerateXMLThread.run()", tfex);
      }
      catch (IOException ioex)
      {
         cat.logThrowableT(Severity.ERROR, loc, "GenerateXMLThread.run()", ioex);
      }
   }
}
