package com.sap.engine.services.webservices.jaxws;

/*
 * Copyright (c) 2004 by SAP AG, Walldorf.,
 * http://www.sap.com
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP AG, Walldorf. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import com.sap.engine.services.webservices.espbase.client.ProxyGeneratorConfigNew;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.tools.xjc.*;
import com.sun.tools.xjc.api.ClassNameAllocator;
import com.sun.tools.xjc.api.SpecVersion;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.util.ErrorReceiverFilter;

/**
 * @author Dimitar Velichkov (I033362) dimitar.velichkov@sap.com
 */
public class JaxbSchemaToJavaWrapper {

  private File                outDir      = null;
  private DOMSource[]         schemaFiles = null;
  private EntityResolver      resolver = null;
  private ClassNameAllocator      allocator = null;
  private boolean generateFiles = true;
  private int jaxbVersion = ProxyGeneratorConfigNew.JAXB_20;

  public void setJAXBVersion(int jaxbVersion) {
    this.jaxbVersion = jaxbVersion;
  }
  
  //private static final String JAXB_NS     = "http://java.sun.com/xml/ns/jaxb";

  public void setFileGeneration(boolean flag) {
    this.generateFiles = flag;  
  }
  
  public JaxbSchemaToJavaWrapper(DOMSource[] schemas, File outDir,EntityResolver resolver, boolean generateFiles) {
    
    if (schemas == null || (outDir == null && generateFiles == true)) {
      throw new IllegalArgumentException(
          "Must supply schema config, an array of input sources, an output directory");
    }

    this.generateFiles = generateFiles;
    this.outDir = outDir;
    if (this.outDir != null && !this.outDir.exists()) {
      outDir.mkdirs();
    }

    //add a default system ID or else NullPointerException is throws by XJC Options class!
    for(DOMSource ds : schemas){
      if(ds.getSystemId() == null){
        ds.setSystemId("");
      }
    }       
    
    schemaFiles = schemas;
    this.resolver = resolver;

  }
/*
  private void generateSimpleTypesForSchemas(Element schemaRoot){
     
//     Document d = schemaRoot.getOwnerDocument();
//    
//     schemaRoot.setAttributeNS(JAXB_NS, "jaxb:version", "2.0");
//     String xsdPrefix = schemaRoot.getPrefix();
//     Element annot = d.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI,
//     xsdPrefix +":annotation");
//     Element appInfo = d.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI,
//     xsdPrefix +":appinfo");
//     Element extension = d.createElementNS(JAXB_NS, "jaxb:globalBindings");
//     extension.setAttribute("mapSimpleTypeDef", "true");
//          
//     schemaRoot.appendChild(annot);
//     annot.appendChild(appInfo);
//     appInfo.appendChild(extension);
    //      
  }
*/
  /*
  private void serializeSchema(Element schemaRoot, String systemID, Options ops, Transformer tr, DocumentBuilder dBuilder) throws JaxbSchemaToJavaGenerationException {

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    StreamResult res = new StreamResult(buffer);
               
    //generateSimpleTypesForSchemas(schemaRoot);
    DOMSource newSource = new DOMSource(schemaRoot);

    try {
      tr.transform(newSource, res);
    } catch (TransformerException e) {
      throw new JaxbSchemaToJavaGenerationException(e);
    }

    // //serialize modified DOM to byte array, add it as a "grammar" to the
    // options object
    ByteArrayInputStream inBuffer = new ByteArrayInputStream(buffer.toByteArray());

    InputSource modifiedSchema = new SmartInputSource(inBuffer);       
    modifiedSchema.setSystemId(systemID);
    EntityResolver x;
    //ops.entityResolver = 
    ops.addGrammar(modifiedSchema);
    buffer.reset();

  }*/

  private Model schemaToJavaCompile(Options op) throws JaxbSchemaToJavaGenerationException{
// these are hardcoded defaults, should be good enough
    
    final Options ops = op;
    
    ops.targetDir = outDir;
    //ops.packageLevelAnnotations = false;
    ops.defaultPackage = null;
    if (this.jaxbVersion == ProxyGeneratorConfigNew.JAXB_20) {
      ops.target = SpecVersion.V2_0;      
    } else {
      ops.target = SpecVersion.V2_1;
    }
    
    ops.verbose = true;
    
    
    final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

    // stupid error-handling XJC logic, copy-pasted from RI
    class Listener extends XJCListener {
      ConsoleErrorReporter cer = new ConsoleErrorReporter(errorStream);

      public void generatedFile(String fileName) {
        message(fileName);
      }

      public void message(String msg) {

      }

      public void error(SAXParseException exception) {
        cer.error(exception);
      }

      public void fatalError(SAXParseException exception) {
        cer.fatalError(exception);
      }

      public void warning(SAXParseException exception) {
        cer.warning(exception);
      }

      public void info(SAXParseException exception) {
        cer.info(exception);
      }
    }

    Listener listener = new Listener();
    ErrorReceiver receiver = new ErrorReceiverFilter(listener) {
      public void info(SAXParseException exception) {
        if (ops.verbose)
          super.info(exception);
      }

      public void warning(SAXParseException exception) {
        if (!ops.quiet)
          super.warning(exception);
      }
    };

    // following code is 99% copy-pasted from XJC reference implementation
    // com.sun.tools.xjc.Driver
    Model model = null;
    try {

      JCodeModel javaModel = new JCodeModel();
      
      model = ModelLoader.load(ops, javaModel, receiver);
      if (model == null) {
        throw new JaxbSchemaToJavaGenerationException("Could not generate Java classes for schema!"
            + new String(errorStream.toByteArray(), "UTF-8"));
      }
            
      if (model.generateCode(ops, receiver) == null) {
        throw new JaxbSchemaToJavaGenerationException("Could not generate Java classes for schema!"
            + new String(errorStream.toByteArray(), "UTF-8"));
      }

      try {
        if (generateFiles) {
          CodeWriter cw = new FileCodeWriter(outDir, ops.readOnly);
          model.codeModel.build(cw);
        }
      } catch (IOException e) {
        throw new JaxbSchemaToJavaGenerationException("Could not write Java classes for schema to disk!", e);
      }     
      
    } catch (StackOverflowError e) {
      throw new JaxbSchemaToJavaGenerationException("Could not generate Java classes for schema!", e);
    } catch (UnsupportedEncodingException e) {
//       $JL-EXC$
    }
    return model;
    
  }
  
  private InputSource getInputSource(DOMSource domSource) {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    StreamResult res = new StreamResult(buffer);    
    try {
      TransformerFactory trFact = TransformerFactory.newInstance();
      Transformer transformer = trFact.newTransformer();    
      transformer.transform(domSource, res);
    } catch (TransformerException e) {
      e.printStackTrace(); // What happened !!!
      return null;
    }
    ByteArrayInputStream inBuffer = new ByteArrayInputStream(buffer.toByteArray());
    InputSource schemaSource = new SmartInputSource(inBuffer);       
    schemaSource.setSystemId(domSource.getSystemId().replace(" ","%20"));    
    return schemaSource;
  } 
  
  public void setAllocator(ClassNameAllocator allocator) {
    this.allocator = allocator;
  }
  
  private Model generateInternal() throws JaxbSchemaToJavaGenerationException {
    Options ops = new Options();        
    ops.entityResolver = this.resolver;   
    ops.noFileHeader = true;
    ops.packageLevelAnnotations = true;
    ops.strictCheck = false;
    ops.setSchemaLanguage(Language.XMLSCHEMA);
    if (this.allocator != null) {
      ops.classNameAllocator = this.allocator;
    }
    
    
    for (DOMSource schemaFile : schemaFiles) {
      InputSource input = getInputSource(schemaFile);
      ops.addGrammar(input);      
    }
    // XJC (JAXB 2.0 RI schema to java compiler) uses the options object to
    // generate java classes
    return schemaToJavaCompile(ops);
    
  }  
  
  private Document createEmptySchema() throws JaxbSchemaToJavaGenerationException{
    
    DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
    fact.setNamespaceAware(true);
    
    String schema  = "<?xml version='1.0' encoding='UTF-8'?><schema xmlns='http://www.w3.org/2001/XMLSchema' targetNamespace='http://www.example.org/NewXMLSchema' xmlns:tns='http://www.example.org/NewXMLSchema'></schema>";
    
    ByteArrayInputStream byteArr = null;
    try {
      byteArr = new ByteArrayInputStream(schema.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      //$JL-EXC$ //should never happen, UTF-8 not supported????
    }

    Document d = null;
    try {
      d = fact.newDocumentBuilder().parse(byteArr);
    } catch (Exception e) {
      throw new JaxbSchemaToJavaGenerationException(e); 
    } 
    return d;
    
  }
  
  public Model generate() throws JaxbSchemaToJavaGenerationException {

    Model model = null;
    
    //no schema, generate an empty one so JAXB can generate some default type mappings for us
    if(schemaFiles.length == 0){
      schemaFiles = new DOMSource[1];
      schemaFiles[0] = new DOMSource(createEmptySchema());     
      schemaFiles[0].setSystemId("");
    }
        
    model = generateInternal();
        
    return model;
  }

}
